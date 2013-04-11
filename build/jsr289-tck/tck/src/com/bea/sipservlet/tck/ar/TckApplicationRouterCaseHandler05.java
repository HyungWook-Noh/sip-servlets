/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 * @version 1.0
 * @created 01-April-2008 17:01:42
 */

package com.bea.sipservlet.tck.ar;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipRouteModifier;
import java.io.Serializable;

public class TckApplicationRouterCaseHandler05 extends TckApplicationRouterCaseHandler {

  static class CaseStateInfo implements Serializable {
    private String routeAddr;

    public CaseStateInfo(String routeAddr) {
      this.routeAddr = routeAddr;
    }

    public String getRouteAddr() {
      return routeAddr;
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
    String[] routes = null;
    SipRouteModifier routeModifier = null;

    switch (directive) {
    case NEW:
      nextAppName = "AR-InternalRouteModifier";
      region = SipApplicationRoutingRegion.ORIGINATING_REGION;
      routeModifier = SipRouteModifier.ROUTE;
      routes = new String[3];

      String localHost = ((SipURI)initialRequest.getPoppedRoute().getURI()).getHost();
      int localPort = ((SipURI)initialRequest.getPoppedRoute().getURI()).getPort();

      routes[0] = "<sip:" + localHost + ":" + localPort + ";entry=correct;lr>";
      routes[1] = "<sip:" + localHost + ":" + localPort + ";entry=bad;lr>";
      routes[2] = "<sip:" + localHost + ":" + localPort + ";entry=wrong;lr>";

      caseStateInfo = new CaseStateInfo(routes[0]);
      break;
    case CONTINUE: {
      caseStateInfo = (CaseStateInfo)stateInfo;
      String routeInPrivateHeader = initialRequest.getHeader("x-tck.jsr289.net-case5.PopedRoute");
      if (routeInPrivateHeader == null)
        throw new TckApplicationRouterException("Case05 failed in continue direction: header x-tck.jsr289.net-case5.PopedRoute is empty.");

      if (!compareAddress(routeInPrivateHeader, caseStateInfo.getRouteAddr()))
        throw new TckApplicationRouterException("Case05 failed in continue direction: PopedRoute are not identical.");

      routes = null;
      routeModifier = SipRouteModifier.NO_ROUTE;
      region = SipApplicationRoutingRegion.ORIGINATING_REGION;
      nextAppName = "AR-Success";
      break;
    }
    case REVERSE: {
        throw new TckApplicationRouterException("Case03 failed in reverse direction: Wrongly routing.");
    }
    }

    routerInfo = new SipApplicationRouterInfo(
        nextAppName,
        region,
        initialRequest.getRequestURI().toString(),
        routes,
        routeModifier,
        caseStateInfo);

    return routerInfo;
	}

	/**
	 * 
	 * @param router
	 */
	public TckApplicationRouterCaseHandler05(TckApplicationRouter router){
    super(router);

  }

}