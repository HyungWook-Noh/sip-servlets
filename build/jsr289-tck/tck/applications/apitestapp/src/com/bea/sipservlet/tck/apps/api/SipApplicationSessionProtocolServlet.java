/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * SipApplicationSessionProtocolServlet is used to test the APIs of 
 * javax.servlet.sip.SipApplicationSession.Protocol
 * 
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.annotation.SipServlet;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;

@SipServlet(name = "SipApplicationSessionProtocol")
public class SipApplicationSessionProtocolServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;
	
	@TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValueOf001(SipServletRequest req) {
		
		serverEntryLog();
		SipApplicationSession.Protocol protocol = 
			SipApplicationSession.Protocol.valueOf("SIP");
	  return (protocol != null 
	  		&& SipApplicationSession.Protocol.SIP.equals(protocol)) ? 
			  null : "Fail to transfer String to SipApplicationSession.Protocol";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValues001(SipServletRequest req) {
  	
  	serverEntryLog();
  	boolean isHTTP = false;
	  boolean isSIP = false;

	  SipApplicationSession.Protocol[] protocols = 
	  	SipApplicationSession.Protocol.values();
	  if(protocols != null){
		  for(SipApplicationSession.Protocol protocol : protocols){
			  if(SipApplicationSession.Protocol.HTTP.equals(protocol)) isHTTP = true;
			  if(SipApplicationSession.Protocol.SIP.equals(protocol)) isSIP = true;  
		  }
		  return (isHTTP && isSIP) ?
				  null : "Fail to get all values of SipApplicationSession.Protocol";
	  }
	  else{
		  return "Get null through SipApplicationSession.Protocol.values()";
	  }
  }

}
