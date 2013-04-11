/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * SipSessionStateServlet is used to test the APIs of 
 * javax.servlet.sip.SipSession.State
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;

@javax.servlet.sip.annotation.SipServlet(name = "SipSessionState")
public class SipSessionStateServlet extends BaseServlet {
	private static final long serialVersionUID = 4182951812643069558L;
	
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValueOf001(SipServletRequest req) {
    serverEntryLog();
    
	  SipSession.State state = SipSession.State.valueOf("TERMINATED");
	  return (state != null && SipSession.State.TERMINATED.equals(state)) ? 
			  null : "Fail to transfer String to SipSession.State";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValues001(SipServletRequest req) {
    serverEntryLog();
    
	  boolean isINITIAL = false;
	  boolean isEARLY = false;
	  boolean isCONFIRMED = false;
	  boolean isTERMINATED = false;
	  SipSession.State[] states = SipSession.State.values();
	  if(states != null){
		  for(SipSession.State state : states){
			  if(SipSession.State.INITIAL.equals(state)) isINITIAL = true;
			  if(SipSession.State.EARLY.equals(state)) isEARLY = true;
			  if(SipSession.State.CONFIRMED.equals(state)) isCONFIRMED = true;
			  if(SipSession.State.TERMINATED.equals(state)) isTERMINATED = true;
		  }
		  return (isINITIAL && isEARLY && isCONFIRMED && isTERMINATED) ?
				  null : "Fail to get all values of SipSession.State";
	  }
	  else{
		  return "Get null through SipSession.State.values()";
	  }
  }
}
