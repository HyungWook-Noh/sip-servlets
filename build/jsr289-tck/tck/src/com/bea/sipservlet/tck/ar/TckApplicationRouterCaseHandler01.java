/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 * @version 1.0
 * @created 01-April-2008 17:01:42
 */

package com.bea.sipservlet.tck.ar;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipRouteModifier;
import java.io.Serializable;

public class TckApplicationRouterCaseHandler01 extends TckApplicationRouterCaseHandler {

  // mihirk - It is not easily understandable that this is the stateInfo, need
  // javadocs that states that this is the serializable StateInfo to be
  // passed between AR invocations.
  static class CaseStateInfo implements Serializable {
    private String magic_number;

    public CaseStateInfo(String magic_number) {
      this.magic_number = magic_number;
    }

    public String getMagicNumber(){
      return magic_number;
    }
  }

	/**
	 * 
	 * @param initialRequest
	 * @param region
	 * @param directive
	 * @param stateInfo
	 */
	public SipApplicationRouterInfo handleRouterEnquery(
      SipServletRequest initialRequest,
      SipApplicationRoutingRegion region,
      SipApplicationRoutingDirective directive,
      Serializable stateInfo) throws TckApplicationRouterException {

    SipApplicationRouterInfo routerInfo = null;
    String nextAppName = null;
    CaseStateInfo caseStateInfo = null;

    switch (directive) {
    case NEW:
      caseStateInfo = new CaseStateInfo(this.getClass().getName());
      nextAppName = "AR-Continue";
      region = SipApplicationRoutingRegion.ORIGINATING_REGION;
      break;
    case CONTINUE:
      caseStateInfo = (CaseStateInfo)stateInfo;
      if (!caseStateInfo.getMagicNumber().equals(this.getClass().getName()))
        throw new TckApplicationRouterException("Case01 failed in continue direction: state_info lost.");

      if (region!= null)
        throw new TckApplicationRouterException("Case01 failed in continue direction: region wrong.");
      else
        region = SipApplicationRoutingRegion.ORIGINATING_REGION;

      /*
      if (region!= SipApplicationRoutingRegion.ORIGINATING_REGION)
        throw new TckApplicationRouterException("Case01 failed in continue direction: region wrong.");
      */

      nextAppName = "AR-Reverse";
      break;
    case REVERSE:
      caseStateInfo = (CaseStateInfo)stateInfo;
      if (!caseStateInfo.getMagicNumber().equals(this.getClass().getName()))
        throw new TckApplicationRouterException("Case01 failed in reverse direction: state_info lost.");

      if (region!= null)
        throw new TckApplicationRouterException("Case01 failed in continue direction: region wrong.");
      else
        region = SipApplicationRoutingRegion.TERMINATING_REGION;

      /*
      if (region!= SipApplicationRoutingRegion.TERMINATING_REGION)
        throw new TckApplicationRouterException("Case01 failed in reverse direction: region wrong.");
      */

      nextAppName = "AR-Success";
      break;
    // mihirk: default case missing, maybe you want to throw an exception
    }

    routerInfo = new SipApplicationRouterInfo(
        nextAppName,
        region,
        initialRequest.getFrom().getURI().toString(),
        null,
        SipRouteModifier.NO_ROUTE,
        caseStateInfo);

    return routerInfo;
  }

  // mihirk - why is the constructor at the bottom of the class?
  /**
	 * 
	 * @param router
	 */
	public TckApplicationRouterCaseHandler01(TckApplicationRouter router){
    super(router);

  }

}