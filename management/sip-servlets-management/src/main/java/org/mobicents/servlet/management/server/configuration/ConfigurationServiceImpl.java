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

package org.mobicents.servlet.management.server.configuration;

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Set;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

<<<<<<< HEAD

import org.apache.catalina.mbeans.MBeanUtils;
=======
>>>>>>> issue-136
import org.mobicents.javax.servlet.CongestionControlPolicy;
import org.mobicents.servlet.management.client.configuration.ConfigurationService;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConfigurationServiceImpl  extends RemoteServiceServlet implements ConfigurationService {
	static final long serialVersionUID = 1L;
	private static MBeanServer mserver;
	static {
		if( MBeanServerFactory.findMBeanServer(null).size() > 0 ) {
            mserver = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
           
        } else {
            mserver = ManagementFactory.getPlatformMBeanServer();
           
        }
	}
	
	private ObjectName getApplicationDispatcher() {
		try {
			ObjectName dispatcherQuery = new ObjectName("*:type=SipApplicationDispatcher");
			ObjectInstance dispatcherInstance = (ObjectInstance) 
			mserver.queryMBeans(dispatcherQuery, null).iterator().next();
			ObjectName dispatcherName = dispatcherInstance.getObjectName();
			return dispatcherName;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	private ObjectName getSglcMBean() {
		try {
			ObjectName dispatcherQuery = new ObjectName("*:service=SimpleGlobalLoggingConfiguration");
			Iterator<ObjectInstance> objectInstances = mserver.queryMBeans(dispatcherQuery, null).iterator();
			ObjectName sglcName = null;
			if(objectInstances.hasNext()) {
				sglcName = objectInstances.next().getObjectName();
			}
			return sglcName;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	public String getConcurrencyControlMode() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			ConcurrencyControlMode mode = 
				(ConcurrencyControlMode) mserver.getAttribute(
						dispatcherName, "ConcurrencyControlMode");
			return mode.toString();
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}
	
	public String getLoggingMode() {
		try {
			ObjectName sglcName = getSglcMBean();
			if(sglcName == null) 
				return null;
				
			String mode = 
				(String) mserver.invoke(sglcName, "getCurrentProfile", new Object[]{}, new String[]{});
			return mode.toString();
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public int getQueueSize() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			String size = 
				mserver.getAttribute(
						dispatcherName, "QueueSize").toString();
			return Integer.parseInt(size);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}
	
	public int getBaseTimerInterval() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			String size = 
				mserver.getAttribute(
						dispatcherName, "BaseTimerInterval").toString();
			return Integer.parseInt(size);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}
	
	public int getT2Interval() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			String size = 
				mserver.getAttribute(
						dispatcherName, "T2Interval").toString();
			return Integer.parseInt(size);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}
	
	public int getT4Interval() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			String size = 
				mserver.getAttribute(
						dispatcherName, "T4Interval").toString();
			return Integer.parseInt(size);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}
	
	public int getTimerDInterval() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			String size = 
				mserver.getAttribute(
						dispatcherName, "TimerDInterval").toString();
			return Integer.parseInt(size);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public void setConcurrencyControlMode(String mode) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			Attribute att = new Attribute("ConcurrencyControlModeByName", mode);
			mserver.setAttribute(dispatcherName, att);			
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}
	
	public void setLoggingMode(String mode) {
		try {
			ObjectName sglcName = getSglcMBean();
			if(sglcName == null) 
				return;
			
			mserver.invoke(sglcName, "switchLoggingConfiguration", new Object[]{mode}, new String[]{"java.lang.String"});
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}
	
	public String[] listLoggingProfiles() {
		try {
			ObjectName sglcName = getSglcMBean();
			if(sglcName == null) 
				return null;
			
			Set<String> profiles = (Set<String>) mserver.invoke(sglcName, "listProfiles", new Object[]{}, new String[]{});
			String[] sglcModes = new String[0];
			if(profiles != null) {
				Iterator<String> profilesIt = profiles.iterator();
				sglcModes = new String[profiles.size()];
				int i = 0;
				while (profilesIt.hasNext()) {
					String profile = profilesIt.next();
					sglcModes[i] = profile;
					i++;
				}
			}
			return sglcModes;
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public void setQueueSize(int queueSize) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Attribute att = new Attribute("QueueSize", new Integer(queueSize));
			mserver.setAttribute(dispatcherName, att);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
		
	}
	
	public void setBaseTimerInterval(int baseTimerInterval) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Attribute att = new Attribute("BaseTimerInterval", new Integer(baseTimerInterval));
			mserver.setAttribute(dispatcherName, att);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
		
	}
	
	public void setT2Interval(int t2Interval) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Attribute att = new Attribute("T2Interval", new Integer(t2Interval));
			mserver.setAttribute(dispatcherName, att);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
		
	}
	
	public void setT4Interval(int t4Interval) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Attribute att = new Attribute("T4Interval", new Integer(t4Interval));
			mserver.setAttribute(dispatcherName, att);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
		
	}
	
	
	public void setTimerDInterval(int timerDInterval) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Attribute att = new Attribute("TimerDInterval", new Integer(timerDInterval));
			mserver.setAttribute(dispatcherName, att);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
		
	}
	
	public String getCongestionControlPolicy() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			CongestionControlPolicy policy = 
				(CongestionControlPolicy) mserver.getAttribute(
						dispatcherName, "CongestionControlPolicy");
			return policy.toString();
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public int getMemoryThreshold() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			String memoryThreshold = 
				mserver.getAttribute(
						dispatcherName, "MemoryThreshold").toString();
			return Integer.parseInt(memoryThreshold);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public void setCongestionControlPolicy(String policy) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();	
			
			Attribute att = new Attribute("CongestionControlPolicyByName", policy);
			mserver.setAttribute(dispatcherName, att);			
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public void setMemoryThreshold(int memoryThreshold) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Attribute att = new Attribute("MemoryThreshold", new Integer(memoryThreshold));
			mserver.setAttribute(dispatcherName, att);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
		
	}
	
	public long getCongestionControlCheckingInterval() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			String checkingInterval = 
				mserver.getAttribute(
						dispatcherName, "CongestionControlCheckingInterval").toString();
			return Long.parseLong(checkingInterval);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public void setCongestionControlCheckingInterval(long interval) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Attribute att = new Attribute("CongestionControlCheckingInterval", new Long(interval));
			mserver.setAttribute(dispatcherName, att);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
		
	}

}
