/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * UAModeServlet is used to test the APIs of 
 * javax.servlet.sip.UAMode
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.UAMode;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;

@javax.servlet.sip.annotation.SipServlet(name = "UAMode")
public class UAModeServlet extends BaseServlet {
	private static final long serialVersionUID = 4182951812643069558L;

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValueOf001(SipServletRequest req) {
    serverEntryLog();
    
	  UAMode mode = UAMode.valueOf("UAC");
	  return (mode != null && UAMode.UAC.equals(mode)) ? 
	      null : "Fail to transfer String to UAMode";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValues001(SipServletRequest req) {
    serverEntryLog();
    
	  boolean isUAC = false;
	  boolean isUAS = false;
	  UAMode[] modes = UAMode.values();
	  if(modes != null){
		  for(UAMode mode : modes){
			  if(UAMode.UAC.equals(mode)) isUAC = true;
			  if(UAMode.UAS.equals(mode)) isUAS = true;
		  }
		  return (isUAC && isUAS) ? null : "Fail to get all values of UAMode";
	  }
	  else{
		  return "Get null through UAMode.values()";
	  }
  }
}
