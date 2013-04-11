/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved.
 * 
 *  TestUtil contains some utils used in TCK test
 */

package com.bea.sipservlet.tck.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipURI;

public class TestUtil {
  public static boolean hasText(String str) {
    if (str == null || str.trim().length() <= 0) {
      return false;
    }
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean isMultihomed(List<SipURI> outboundList) {
  	
  	HashMap<String, SipURI> map = new HashMap<String, SipURI>();            
  	int count = outboundList.size();
  	for (int i = 0; i < count; i++){
  		map.put(outboundList.get(i).toString(), outboundList.get(i));
  	}
  	
  	if (map.size() <= count && map.size() != 1) {
  		return true;
  	}
  	return false;
  }
  public static SipURI selectOutboundInterface(List<SipURI> outboundList) {

  	HashMap<String, SipURI> map = new HashMap<String, SipURI>();            
  	int count = outboundList.size();
  	for (int i = 0; i < count; i++){
  		map.put(outboundList.get(i).toString(), outboundList.get(i));
  	}
  	
  	Iterator i = map.keySet().iterator();            
    while(i.hasNext()){
    	Object key = i.next();
      SipURI outboundInterface = map.get(key);
      String transport = outboundInterface.getTransportParam();
      if (transport != null) {
      	// Due to the SipUnit capability, here can only select UDP
      	if (transport.equalsIgnoreCase("udp")) {
      		return outboundInterface;
        }
      } else {
      	return outboundInterface;
       }
    }            
    return null;
  }
  
}
