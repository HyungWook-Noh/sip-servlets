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

package org.jboss.web.tomcat.service.session;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.catalina.Manager;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class ConvergedSessionInvalidationTracker {
	
	private static final Object MAP_VALUE = new Object();
	
	private static final ThreadLocal<Map<Manager, Map<String,Object>>> invalidatedSessions = new ThreadLocal<Map<Manager, Map<String,Object>>>();
	private static final ThreadLocal<Map<Manager, Map<SipSessionKey,Object>>> invalidatedSipSessions = new ThreadLocal<Map<Manager, Map<SipSessionKey,Object>>>();
	private static final ThreadLocal<Map<Manager, Map<SipApplicationSessionKey,Object>>> invalidatedSipApplicationSessions = new ThreadLocal<Map<Manager,Map<SipApplicationSessionKey,Object>>>();
	private static final ThreadLocal<Boolean> suspended = new ThreadLocal<Boolean>();

	public static void suspend() {
		suspended.set(Boolean.TRUE);
	}

	public static void resume() {
		suspended.set(null);
	}

	public static void sessionInvalidated(String id, Manager manager) {
		if (Boolean.TRUE != suspended.get()) {
			Map<Manager, Map<String,Object>> map = invalidatedSessions.get();
			if (map == null) {
				map = new HashMap<Manager, Map<String,Object>>(2);						
				invalidatedSessions.set(map);
			}
			Map<String,Object> managerMap = map.get(manager);
			if(managerMap == null) {
				managerMap = new WeakHashMap<String,Object>();
				map.put(manager, managerMap);
			}
			managerMap.put(id, MAP_VALUE);
		}
	}
	
	public static void sipSessionInvalidated(SipSessionKey key, Manager manager) {
		if (Boolean.TRUE != suspended.get()) {
			Map<Manager, Map<SipSessionKey,Object>> map = invalidatedSipSessions.get();
			if (map == null) {
				map = new HashMap<Manager, Map<SipSessionKey,Object>>(2);						
				invalidatedSipSessions.set(map);
			}
			Map<SipSessionKey,Object> managerMap = map.get(manager);
			if(managerMap == null) {
				managerMap = new WeakHashMap<SipSessionKey,Object>();
				map.put(manager, managerMap);
			}
			managerMap.put(key, MAP_VALUE);
		}
	}
	
	public static void sipApplicationSessionInvalidated(SipApplicationSessionKey key, Manager manager) {
		if (Boolean.TRUE != suspended.get()) {
			Map<Manager, Map<SipApplicationSessionKey,Object>> map = invalidatedSipApplicationSessions.get();
			if (map == null) {
				map = new HashMap<Manager, Map<SipApplicationSessionKey,Object>>(2);						
				invalidatedSipApplicationSessions.set(map);
			}
			Map<SipApplicationSessionKey,Object> managerMap = map.get(manager);
			if(managerMap == null) {
				managerMap = new WeakHashMap<SipApplicationSessionKey,Object>();
				map.put(manager, managerMap);
			}
			managerMap.put(key, MAP_VALUE);
		}
	}

	public static void clearInvalidatedSession(String id, Manager manager) {
		Map<Manager, Map<String,Object>> map = invalidatedSessions.get();
		if (map != null) {
			Map<String,Object> managerMap = map.get(manager);
			if (managerMap != null) {
				managerMap.remove(id);
			}
		}		
	}

	public static boolean isSessionInvalidated(String id, Manager manager) {
		boolean result = false;
		Map<Manager, Map<String,Object>> map = invalidatedSessions.get();
		if (map != null) {
			Map<String,Object> managerMap = map.get(manager);
			if (managerMap != null) {
				result = managerMap.containsKey(id);
			}
		}		
		return result;
	}
	
	public static boolean isSipSessionInvalidated(SipSessionKey key, Manager manager) {
		boolean result = false;
		Map<Manager, Map<SipSessionKey,Object>> map = invalidatedSipSessions.get();
		if (map != null) {
			Map<SipSessionKey,Object> managerMap = map.get(manager);
			if (managerMap != null) {
				result = managerMap.containsKey(key);
			}
		}		
		return result;
	}
	
	public static boolean isSipApplicationSessionInvalidated(SipApplicationSessionKey key, Manager manager) {
		boolean result = false;
		Map<Manager, Map<SipApplicationSessionKey,Object>> map = invalidatedSipApplicationSessions.get();
		if (map != null) {
			Map<SipApplicationSessionKey,Object> managerMap = map.get(manager);
			if (managerMap != null) {
				result = managerMap.containsKey(key);
			}
		}		
		return result;
	}
}
