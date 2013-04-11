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

public class TckApplicationRouterCaseHandler02 extends TckApplicationRouterCaseHandler {

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

    // mihirk - should we check for the specific application that this test is
    // dependent on? ie, AR-Success?
    // Store the list of apps passed in the AR.applicationDeployed method call in
    // the TckApplicationRouter and then check if the specific application that
    // this test is dependent on is deployed?
    if (!getRouter().isApplicationDeployCalled())
      throw new TckApplicationRouterException("Case02 failed: applicationDeployed() is not called.");

    return new SipApplicationRouterInfo(
        "AR-Success",
        SipApplicationRoutingRegion.ORIGINATING_REGION,
        initialRequest.getFrom().getURI().toString(),
        null,
        SipRouteModifier.NO_ROUTE,
        null);
  }

  // mihirk - why is the constructor at the bottom of the class?
  /**
	 * 
	 * @param router
	 */
	public TckApplicationRouterCaseHandler02(TckApplicationRouter router){
    super(router);

  }

}