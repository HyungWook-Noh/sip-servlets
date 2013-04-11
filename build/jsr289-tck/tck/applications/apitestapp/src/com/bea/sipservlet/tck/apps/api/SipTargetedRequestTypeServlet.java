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
import javax.servlet.sip.ar.SipTargetedRequestType;

@SipServlet(name = "SipTargetedRequestType")
public class SipTargetedRequestTypeServlet extends BaseServlet {
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValueOf001(SipServletRequest req){
    serverEntryLog();
    SipTargetedRequestType _enum =
        SipTargetedRequestType.valueOf("ENCODED_URI");
	  return (_enum != null
	  		&& SipTargetedRequestType.ENCODED_URI.equals(_enum)) ?
			  null : "Fail to transfer String to SipTargetedRequestType";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValues001(SipServletRequest req){
    serverEntryLog();
    SipTargetedRequestType[] enums = SipTargetedRequestType.values();
    if(enums == null) return "SipTargetedRequestType.values() return null";
    int count = 0;
    for(SipTargetedRequestType _enum : enums){
      if(_enum.equals(SipTargetedRequestType.ENCODED_URI)) count++;
      if(_enum.equals(SipTargetedRequestType.JOIN)) count++;
      if(_enum.equals(SipTargetedRequestType.REPLACES)) count++;
    }
    if(count==3) return null;
    return "Fail to get all values of SipTargetedRequestType";
  }
}
