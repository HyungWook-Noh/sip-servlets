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

public class TckApplicationRouterCaseHandler03 extends TckApplicationRouterCaseHandler {

  static class CaseStateInfo implements Serializable {
    private String last_directive;
    public CaseStateInfo(String last_directive) {
      this.last_directive = last_directive;
    }

    public void setLastDirective(String directive) {
      this.last_directive = directive;
    }

    public String getLastDirective(){
      return last_directive;
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
      nextAppName = "AR-Continue";
      caseStateInfo = new CaseStateInfo(directive.toString());
      region = SipApplicationRoutingRegion.ORIGINATING_REGION;
      break;
    case CONTINUE: {
      caseStateInfo = (CaseStateInfo)stateInfo;
      SipApplicationRoutingDirective lastDirective = SipApplicationRoutingDirective.valueOf(caseStateInfo.getLastDirective());
      switch (lastDirective) {
        case REVERSE:
          nextAppName = "AR-Success";
          break;
        case NEW:
          nextAppName = "AR-Reverse";
          break;
        default:
          throw new TckApplicationRouterException("Case03 failed in continue direction: Wrongly routing.");
      }
      caseStateInfo.setLastDirective(directive.toString());
      region = SipApplicationRoutingRegion.ORIGINATING_REGION;
      break;
    }
    case REVERSE: {
      caseStateInfo = (CaseStateInfo)stateInfo;
      SipApplicationRoutingDirective lastDirective = SipApplicationRoutingDirective.valueOf(caseStateInfo.getLastDirective());
      if (lastDirective != SipApplicationRoutingDirective.CONTINUE)
        throw new TckApplicationRouterException("Case03 failed in reverse direction: Wrongly routing.");

      if (region != null)
        throw new TckApplicationRouterException("Case03 failed in reverse direction: Region is not null.");
      else
        region = SipApplicationRoutingRegion.TERMINATING_REGION;

      nextAppName = "AR-Continue";
      caseStateInfo.setLastDirective(directive.toString());
      break;
    }
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

	/**
	 * 
	 * @param router
	 */
	public TckApplicationRouterCaseHandler03(TckApplicationRouter router){
    super(router);

  }

}