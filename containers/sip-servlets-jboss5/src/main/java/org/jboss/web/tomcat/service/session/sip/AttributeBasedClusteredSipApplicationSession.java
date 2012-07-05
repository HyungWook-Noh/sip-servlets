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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSipApplicationSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingAttributeGranularitySessionData;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;

/**
 * This class is based on the following Jboss class
 * org.jboss.web.tomcat.service.session.AttributeBasedClusteredSession JBOSS AS
 * 5.1.0.GA Tag
 * 
 * Implementation of a clustered sip application session where the replication
 * granularity level is attribute based; that is, we replicate only the dirty
 * attributes.
 * <p/>
 * Note that the isolation level of the cache dictates the concurrency behavior.
 * Also note that session and its associated attribtues are stored in different
 * nodes. This will be ok since cache will take care of concurrency. When
 * replicating, we will need to replicate both session and its attributes.
 * </p>
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * @author Ben Wang
 * @author Brian Stansberry
 * 
 */
public class AttributeBasedClusteredSipApplicationSession extends
		ClusteredSipApplicationSession<OutgoingAttributeGranularitySessionData> {
	private static transient Logger logger = Logger
			.getLogger(AttributeBasedClusteredSipSession.class);

	protected static final String EMPTY_ARRAY[] = new String[0];
	/**
	 * Descriptive information describing this Session implementation.
	 */
	protected static final String info = "AttributeBasedClusteredSipApplicationSession/1.0";

	// Issue 2450 : Deadlock when replicating application session/executing transaction that uses it
	// moved those structures to concurrent ones to avoid synchronization on the full session
	
	// Transient map to store attr changes for replication.
	private transient Map<String, Object> attrModifiedMap_ = new ConcurrentHashMap<String, Object>();
	// Transient set to store attr removals for replication
	private transient Set<String> attrRemovedSet_ = new CopyOnWriteArraySet<String>();

	protected AttributeBasedClusteredSipApplicationSession(
			SipApplicationSessionKey key, SipContext sipContext, boolean useJK) {
		super(key, sipContext, useJK);
	}

	@Override
	public String getInfo() {
		return (info);
	}

	/**
	 * Override the superclass to additionally reset this class' fields.
	 * <p>
	 * <strong>NOTE:</strong> It is not anticipated that this method will be
	 * called on a ClusteredSession, but we are overriding the method to be
	 * thorough.
	 * </p>
	 */
	// public void recycle() {
	// super.recycle();
	//
	// attributes_.clear();
	// clearAttrChangedMaps();
	// }
	// -------------------------------------------- Overridden Protected Methods
	@Override
	protected OutgoingAttributeGranularitySessionData getOutgoingSipApplicationSessionData() {
		Map<String, Object> modAttrs = null;
		Set<String> removeAttrs = null;
		if (isSessionAttributeMapDirty()) {
			if (attrModifiedMap_.size() > 0) {
				modAttrs = new HashMap<String, Object>(attrModifiedMap_);
			}

			if (attrRemovedSet_.size() > 0) {
				removeAttrs = new HashSet<String>(attrRemovedSet_);
			}

			clearAttrChangedMaps();
		}
		DistributableSipApplicationSessionMetadata metadata = (DistributableSipApplicationSessionMetadata)getSessionMetadata();
		Long timestamp = modAttrs != null || removeAttrs != null
				|| metadata != null || getMustReplicateTimestamp() ? Long
				.valueOf(getSessionTimestamp()) : null;
		OutgoingData outgoingData = new OutgoingData(null, getVersion(), timestamp, key.getId(), metadata,
				modAttrs, removeAttrs);
		outgoingData.setSessionMetaDataDirty(isSessionMetadataDirty());
		return outgoingData;
	}

	@Override
	protected Object getAttributeInternal(String name) {
		Object result = getAttributesInternal().get(name);

		// Do dirty check even if result is null, as w/ SET_AND_GET null
		// still makes us dirty (ensures timely replication w/o using ACCESS)
		if (isGetDirty(result) && !replicationExcludes.contains(name)) {
			attributeChanged(name, result, false);
		}

		return result;
	}

	@Override
	protected Object removeAttributeInternal(String name, boolean localCall,
			boolean localOnly) {
		Object result = getAttributesInternal().remove(name);
		if (localCall && !replicationExcludes.contains(name))
			attributeChanged(name, result, true);
		return result;
	}

	@Override
	protected Object setAttributeInternal(String key, Object value) {
		Object old = getAttributesInternal().put(key, value);
		if (!replicationExcludes.contains(key))
			attributeChanged(key, value, false);
		return old;
	}

	// ------------------------------------------------------- Private Methods

	private void attributeChanged(String key, Object value,
			boolean removal) {
		if (removal) {
			if (attrModifiedMap_.containsKey(key)) {
				attrModifiedMap_.remove(key);
			}
			attrRemovedSet_.add(key);
		} else {
			if (attrRemovedSet_.contains(key)) {
				attrRemovedSet_.remove(key);
			}
			attrModifiedMap_.put(key, value);
		}
		sessionAttributesDirty();
	}

	private void clearAttrChangedMaps() {
		attrRemovedSet_.clear();
		attrModifiedMap_.clear();
	}

	// ----------------------------------------------------------------- Classes

	private static class OutgoingData extends
			OutgoingDistributableSipApplicationSessionDataImpl implements
			OutgoingAttributeGranularitySessionData {
		private final Map<String, Object> modifiedAttributes;
		private final Set<String> removedAttributes;

		public OutgoingData(String realId, int version, Long timestamp, String key,
				DistributableSipApplicationSessionMetadata metadata,
				Map<String, Object> modifiedAttributes,
				Set<String> removedAttributes) {
			super(realId, version, timestamp, key, metadata);
			this.modifiedAttributes = modifiedAttributes;
			this.removedAttributes = removedAttributes;
		}

		public Map<String, Object> getModifiedSessionAttributes() {
			return modifiedAttributes;
		}

		public Set<String> getRemovedSessionAttributes() {
			return removedAttributes;
		}
	}
}
