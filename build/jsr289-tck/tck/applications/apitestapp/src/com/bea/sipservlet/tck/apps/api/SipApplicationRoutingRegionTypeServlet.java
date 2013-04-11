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
import javax.servlet.sip.ar.SipApplicationRoutingRegionType;

@SipServlet(name = "SipApplicationRoutingRegionType")
public class SipApplicationRoutingRegionTypeServlet extends BaseServlet {
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValueOf001(SipServletRequest req){
    serverEntryLog();
    SipApplicationRoutingRegionType _enum =
        SipApplicationRoutingRegionType.valueOf("NEUTRAL");
	  return (_enum != null
	  		&& SipApplicationRoutingRegionType.NEUTRAL.equals(_enum)) ?
			  null : "Fail to transfer String to SipApplicationRoutingRegionType";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValues001(SipServletRequest req){
    serverEntryLog();
    SipApplicationRoutingRegionType[] enums = SipApplicationRoutingRegionType.values();
    if(enums == null) return "SipApplicationRoutingRegionType.values() return null";
    int count = 0;
    for(SipApplicationRoutingRegionType _enum : enums){
      if(_enum.equals(SipApplicationRoutingRegionType.NEUTRAL)) count++;
      if(_enum.equals(SipApplicationRoutingRegionType.TERMINATING)) count++;
      if(_enum.equals(SipApplicationRoutingRegionType.ORIGINATING)) count++;
    }
    if(count==3) return null;
    return "Fail to get all values of SipApplicationRoutingRegionType";
  }

}
