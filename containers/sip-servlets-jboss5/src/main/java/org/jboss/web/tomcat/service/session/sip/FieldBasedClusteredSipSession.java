/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.web.tomcat.service.session.sip;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.aop.Advised;
import org.jboss.web.tomcat.service.session.Util;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSipSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheConvergedSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSipSessionData;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * 
 * <p>
 * Implementation of a clustered sip session for the JBossCacheManager. This
 * class is based on the following Jboss class
 * org.jboss.web.tomcat.service.session.FieldBasedClusteredSession JBOSS AS
 * 5.1.0.GA Tag
 * 
 * The replication granularity level is field based; that is, we replicate only
 * the dirty field in a POJO that is part of a session attribute. E.g., once a
 * user do setAttribute("pojo", pojo), pojo will be monitored automatically for
 * field changes and accessing. It offers couple of advantages:
 * <ul>
 * <li>pojo.setName(), for example, will only replicate the name field in the
 * pojo. And thus is more efficient.</li>
 * <li>If pojo has a complex object graph, we will handle that automtically
 * providing that the children object is also aspectized.</li>
 * </ul>
 * Note that in current version, all the attributes and its associated childre
 * graph objects are required to be aspectized. That is, you can't simply
 * declare them as Serializable. This is restricted because of the
 * marshalling/unmarshalling issue.
 * </p>
 * 
 * <p>
 * We use PojoCache for our internal, replicated data store.
 * <p/>
 * 
 * @author Ben Wang
 * @author Brian Stansberry
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * 
 */
public class FieldBasedClusteredSipSession extends
		ClusteredSipSession<OutgoingDistributableSessionData> {
	private static transient final Logger logger = Logger
			.getLogger(AttributeBasedClusteredSipSession.class);
	/**
	 * Descriptive information describing this Session implementation.
	 */
	protected static final String info = "FieldBasedClusteredSipSession/1.0";	

	protected FieldBasedClusteredSipSession(SipSessionKey key,
			SipFactoryImpl sipFactoryImpl,
			MobicentsSipApplicationSession mobicentsSipApplicationSession,
			boolean useJK) {
		super(key, sipFactoryImpl, mobicentsSipApplicationSession, useJK);
	}

	// ----------------------------------------------- Overridden Public Methods

	@Override
	public String getInfo() {
		return (info);
	}

	// --------------------------------------------- Overridden Protected
	// Methods

	@Override
	protected OutgoingDistributableSessionData getOutgoingSipSessionData() {
		DistributableSipSessionMetadata metadata = (DistributableSipSessionMetadata)getSessionMetadata();
		Long timestamp = metadata != null || isSessionAttributeMapDirty()
				|| getMustReplicateTimestamp() ? Long
				.valueOf(getSessionTimestamp()) : null;
		OutgoingDistributableSipSessionData outgoingData = new OutgoingDistributableSipSessionDataImpl(null,
				getVersion(), timestamp, sipApplicationSessionKey.getId(), SessionManagerUtil.getSipSessionHaKey(key), metadata);
		outgoingData.setSessionMetaDataDirty(isSessionMetadataDirty());
		return outgoingData;
	}

	/**
	 * Overrides the superclass to treat classes implementing Subject as
	 * "immutable", since as an Observer we will detect any changes to those
	 * types.
	 */
	@Override
	protected boolean isMutable(Object attribute) {
		boolean pojo = (attribute instanceof Advised);
		boolean mutable = (!pojo && super.isMutable(attribute));
		return mutable;
	}

	@Override
	protected Object removeAttributeInternal(String name, boolean localCall,
			boolean localOnly) {
		// Remove it from the underlying store
		if (localCall && !replicationExcludes.contains(name)) {
			if (localOnly)
				((DistributedCacheConvergedSipManager)getDistributedCacheManager()).removeSipSessionAttributeLocal(sipApplicationSessionKey.getId(), SessionManagerUtil.getSipSessionHaKey(key),
						name);
			else
				((DistributedCacheConvergedSipManager)getDistributedCacheManager()).removeSipSessionAttribute(sipApplicationSessionKey.getId(), SessionManagerUtil.getSipSessionHaKey(key), name);

			sessionAttributesDirty();
		}
		return getAttributesInternal().remove(name);
	}

	/**
	 * Overrides the superclass to allow instrumented classes and
	 * non-serializable Collections and Maps.
	 */
	@Override
	protected boolean canAttributeBeReplicated(Object attribute) {
		return (attribute == null || Util.checkPojoType(attribute));
	}

	/**
	 * This is the hook for setAttribute. Note that in this
	 * FieldBasedClusteredSession using aop, user should not call setAttribute
	 * call too often since this will re-connect the attribute with the internal
	 * cache (and this is expensive).
	 * 
	 * @param key
	 * @param value
	 * @return Object
	 */
	@Override
	protected Object setAttributeInternal(String key, Object value) {
		if (!replicationExcludes.contains(key)) {
			((DistributedCacheConvergedSipManager)getDistributedCacheManager()).
				putSipSessionAttribute(sipApplicationSessionKey.getId(), SessionManagerUtil.getSipSessionHaKey(this.key), key, value);

			// Special case for Collection classes.
			if (value instanceof Map || value instanceof Collection) {
				// We need to obtain the proxy first.
				value = ((DistributedCacheConvergedSipManager)getDistributedCacheManager())
						.getSipSessionAttribute(sipApplicationSessionKey.getId(), SessionManagerUtil.getSipSessionHaKey(this.key), key);
			}

			// Only mark session dirty if we can replicate the attribute
			sessionAttributesDirty();
		}

		// Still need to put it in the map to track locally.
		return getAttributesInternal().put(key, value);
	}
}
