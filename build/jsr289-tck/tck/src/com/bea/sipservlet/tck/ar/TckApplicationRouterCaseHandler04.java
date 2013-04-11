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
import javax.servlet.sip.ar.SipApplicationRoutingRegionType;
import javax.servlet.sip.ar.SipRouteModifier;
import java.io.Serializable;

public class TckApplicationRouterCaseHandler04 extends TckApplicationRouterCaseHandler {

  static class CaseStateInfo implements Serializable {
    private String subscriber_uri;
    private SipApplicationRoutingRegion region;

    public CaseStateInfo(String subscriber_uri, SipApplicationRoutingRegion region) {
      this.subscriber_uri = subscriber_uri;
      this.region = region;
    }

    public String getSubscriberURI() {
      return subscriber_uri;
    }

    public SipApplicationRoutingRegion getRegion(){
      return region;
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
      Serializable stateInfo) throws TckApplicationRouterException{
    SipApplicationRouterInfo routerInfo = null;
    String nextAppName = null;
    CaseStateInfo caseStateInfo = null;

    switch (directive) {
    case NEW:
      nextAppName = "AR-SuburiRegion";
      region = new SipApplicationRoutingRegion("BEA-TCK", SipApplicationRoutingRegionType.NEUTRAL);
      caseStateInfo = new CaseStateInfo(initialRequest.getRequestURI().toString(), region);
      break;
    case CONTINUE: {
      caseStateInfo = (CaseStateInfo)stateInfo;
      String regionInPrivateHeader = initialRequest.getHeader("x-tck.jsr289.net-case4.Region");
      if (regionInPrivateHeader == null)
        throw new TckApplicationRouterException("Case04 failed in continue direction: header x-tck.jsr289.net-case4.Region is empty.");

      if (!regionInPrivateHeader.equals(caseStateInfo.getRegion().toString()))
        throw new TckApplicationRouterException("Case04 failed in continue direction: Region are not identical.");

      String uriInPrivateHeader = initialRequest.getHeader("x-tck.jsr289.net-case4.SubscriberURI");
      if (uriInPrivateHeader == null)
        throw new TckApplicationRouterException("Case04 failed in continue direction: header x-tck.jsr289.net-case4.SubscriberURI is empty.");

      if (!compareSipUri(uriInPrivateHeader, caseStateInfo.getSubscriberURI().toString()))
        throw new TckApplicationRouterException("Case04 failed in continue direction: SubscriberURI are not identical.");

      region = new SipApplicationRoutingRegion("BEA-TCK", SipApplicationRoutingRegionType.NEUTRAL);
      nextAppName = "AR-Success";
      break;
    }
    case REVERSE: {
        throw new TckApplicationRouterException("Case04 failed in reverse direction: Wrongly routing.");
    }
    }

    routerInfo = new SipApplicationRouterInfo(
        nextAppName,
        region,
        caseStateInfo.getSubscriberURI(),
        null,
        SipRouteModifier.NO_ROUTE,
        caseStateInfo);

    return routerInfo;
	}

  /**
	 * 
	 * @param router
	 */
	public TckApplicationRouterCaseHandler04(TckApplicationRouter router){
    super(router);

  }

}