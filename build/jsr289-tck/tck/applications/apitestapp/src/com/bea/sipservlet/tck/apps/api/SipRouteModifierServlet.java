/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * SipTargetedRequestTypeServlet is used to test the APIs of
 * javax.servlet.sip.ar.SipTargetedRequestType
 *
 */
package com.bea.sipservlet.tck.apps.api;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipServlet;
import javax.servlet.sip.ar.SipRouteModifier;

@SipServlet(name = "SipRouteModifier")
public class SipRouteModifierServlet extends BaseServlet {
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValueOf001(SipServletRequest req){
    serverEntryLog();
    SipRouteModifier _enum =
        SipRouteModifier.valueOf("ROUTE");
	  return (_enum != null
	  		&& SipRouteModifier.ROUTE.equals(_enum)) ?
			  null : "Fail to transfer String to SipRouteModifier";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValues001(SipServletRequest req){
    serverEntryLog();
    SipRouteModifier[] enums = SipRouteModifier.values();
    if(enums == null) return "SipRouteModifier.values() return null";
    int count = 0;
    for(SipRouteModifier _enum : enums){
      if(_enum.equals(SipRouteModifier.ROUTE)) count++;
      if(_enum.equals(SipRouteModifier.NO_ROUTE)) count++;
      if(_enum.equals(SipRouteModifier.ROUTE_BACK)) count++;
    }
    if(count==3) return null;
    return "Fail to get all values of SipRouteModifier";
  }
}
