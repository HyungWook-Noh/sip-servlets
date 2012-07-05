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

package org.jboss.web.tomcat.service.session.distributedcache.spi.sip;

import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;


/**
 * Callback interface to allow the distributed caching layer to invoke upon the
 * local session manager.
 * 
 * @author Brian Stansberry
 * @version $Revision: $
 */
public interface LocalDistributableConvergedSessionManager {
	void notifyRemoteSipApplicationSessionInvalidation(String sessId);

	void notifyRemoteSipSessionInvalidation(String sipAppSessionId,
			String sipSessionId);

	void notifySipApplicationSessionLocalAttributeModification(String sessId);

	void notifySipSessionLocalAttributeModification(String sipAppSessionId,
			String sipSessionId);

	boolean sipApplicationSessionChangedInDistributedCache(String realId,
			String owner, int intValue, long longValue,
			DistributableSessionMetadata distributableSessionMetadata);

	boolean sipSessionChangedInDistributedCache(String sipAppSessionId,
			String sipSessionId, String owner, int intValue, long longValue,
			DistributableSessionMetadata distributableSessionMetadata);

	void sipApplicationSessionActivated();

	void sipSessionActivated();
}