/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * SipApplicationRoutingDirectiveServlet is used to test the APIs of
 * javax.servlet.sip.ar.SipApplicationRoutingDirective
 *
 */
package com.bea.sipservlet.tck.apps.api;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipServlet;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;

@SipServlet(name = "SipApplicationRoutingDirective")
public class SipApplicationRoutingDirectiveServlet extends BaseServlet {
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValueOf001(SipServletRequest req){
    serverEntryLog();
		SipApplicationRoutingDirective _enum =
			SipApplicationRoutingDirective.valueOf("CONTINUE");
	  return (_enum != null
	  		&& SipApplicationRoutingDirective.CONTINUE.equals(_enum)) ?
			  null : "Fail to transfer String to SipApplicationRoutingDirective";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValues001(SipServletRequest req){
    serverEntryLog();
    SipApplicationRoutingDirective[] enums = SipApplicationRoutingDirective.values();
    if(enums == null) return "SipApplicationRoutingDirective.values() return null";
    int count = 0;
    for(SipApplicationRoutingDirective _enum : enums){
      if(_enum.equals(SipApplicationRoutingDirective.CONTINUE)) count++;
      if(_enum.equals(SipApplicationRoutingDirective.NEW)) count++;
      if(_enum.equals(SipApplicationRoutingDirective.REVERSE)) count++;
    }
    if(count==3) return null;
    return "Fail to get all values of SipApplicationRoutingDirective";
  }
}
