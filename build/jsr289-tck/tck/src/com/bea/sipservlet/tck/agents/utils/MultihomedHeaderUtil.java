/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved.
 * 
 *  MultihomedHeaderUtil contains some utils used in Multihomed cases
 */

package com.bea.sipservlet.tck.agents.utils;

import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.ContactHeader;

import javax.sip.header.ExtensionHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ViaHeader;

public class MultihomedHeaderUtil {
	
	/**
	 * Get the host from Multihomed header
	 * @param multihomed The private header for multihomed case.
	 * @return
	 */
	public static String getOutboundHost(String multihomed) {
		String host = "";

		multihomed = multihomed.trim().replace("Multihomed: ", "");

		if (multihomed.indexOf(":") != -1) {
			String[] tokens = multihomed.split(":");
			host = tokens[0];
		} else {
			host = multihomed;
		}

		return host;
	}
	
	/**
	 * Get the port from Multihomed header
	 * @param multihomed The private header for multihomed case.
	 * @return
	 */
	public static int getOutboundPort(String multihomed) {
		String port = "0";

		multihomed = multihomed.trim().replace("Multihomed: ", "");

		if (multihomed.indexOf(":") != -1) {
			String[] tokens = multihomed.split(":");
			port = tokens[1];
		}

		return new Integer(port.trim()).intValue();
	}
	
	/**
	 * Get the transport from Multihomed header
	 * @param multihomed The private header for multihomed case.
	 * @return
	 */
	public static String getOutboundTransport(String multihomed) {
		String transport = "";

		multihomed = multihomed.replace("Multihomed: ", "");
		if (multihomed.contains("transport")) {
			String[] tokens = multihomed.split("=");
			if (tokens[1].indexOf("=") != -1) {
				String[] subs = tokens[2].split(";");
				transport = subs[0];
			} else {
				if (tokens[1].indexOf(";") != -1) {
					String[] subs = tokens[1].split(";");
					transport = subs[0];
				} else {
					transport = tokens[1];
				}
			}
			
		}
		return transport;
	}
	
	/**
	 * Compare Contact header with multihomed header
	 * @param contact The contact header.
	 * @param multihomed The private header for multihomed case.
	 * @return
	 */
	public static boolean checkContactHeader(ContactHeader contact, String multihomed) {
		boolean result = false;
		
		String outboundHost = getOutboundHost(multihomed);

		int outboundPort = getOutboundPort(multihomed);
		
		URI uri = (URI)contact.getAddress().getURI();
    	if (uri.isSipURI()) {
    		SipURI sipURI = (SipURI) uri;
		
    		if (outboundHost.indexOf(sipURI.getHost()) != -1) {
    			
    			if (outboundPort == 0) {
    				result = true;

    			} else {

	    			if (sipURI.getPort() == outboundPort) {
	    				result = true;
	    			}
    			}
    		}
		}
		
		return result;
	}
	
	/**
	 * Compare Via header with multihomed header
	 * @param via The via header.
	 * @param multihomed The private header for multihomed case.
	 * @return
	 */
	public static boolean checkViaHeader(ViaHeader via, String multihomed) {

		boolean result = false;
		String outboundHost = getOutboundHost(multihomed);
		int outboundPort = getOutboundPort(multihomed);
		
		if (outboundHost.indexOf(via.getHost()) != -1) {
			if (outboundPort == 0) {
				result = true;

			} else {
				if (via.getPort()== outboundPort) {
					result = true;
				}
			}
		}
		
		return result;
	}

	/**
	 * Compare Record-Route header with multihomed header
	 * @param recordRoute The record-route header.
	 * @param multihomed The private header for multihomed case.
	 * @return
	 */
	public static boolean checkRecordRouteHeader(
			RecordRouteHeader recordRoute, String multihomed) {
		
		boolean result = false;
		String outboundHost = getOutboundHost(multihomed);
		int outboundPort = getOutboundPort(multihomed);
		
		SipURI uri = (SipURI)recordRoute.getAddress().getURI();
		if (outboundHost.indexOf(uri.getHost()) != -1) {
			if (outboundPort == 0) {
				result = true;

			} else {
				if (uri.getPort() == outboundPort) {
					result = true;	
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Compare Path header with multihomed header
	 * @param path The path header.
	 * @param multihomed The private header for multihomed case.
	 * @return
	 */
	public static boolean checkPathHeader(
			ExtensionHeader path, String multihomed) {
		
		boolean result = false;
		String outboundHost = getOutboundHost(multihomed);
		int outboundPort = getOutboundPort(multihomed);
		
		String pathStr = path.getValue();
		
		pathStr = pathStr.trim().replaceAll("<", "");
		String[] tokens = pathStr.split(";");
		pathStr = tokens[0];
		pathStr = pathStr.replaceAll("sip:", "");
		tokens = null;
		tokens = pathStr.split(":");
		String host = tokens[0];
		int port = new Integer(tokens[1]).intValue();
				
		
		if (outboundHost.indexOf(host) != -1) {
			if (outboundPort == 0) {
				result = true;

			} else {
				if (port == outboundPort) {
					result = true;
				}
			}
		}
		
		return result;
	}
}
