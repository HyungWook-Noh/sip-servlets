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

public class TckApplicationRouterApiCaseHandler extends TckApplicationRouterCaseHandler {


  /**
	 * @param initialRequest
	 * @param region
	 * @param directive
	 * @param stateInfo
   * @return
   * nextApplicationName: The name in header  <br>
   * routingRegion:       SipApplicationRoutingRegion("BEA-TCK", ORIGINATING_REGION);   <br>
   * subscriberURI:       request.getFrom().getURI() <br>
   * routes: 	            null         <br>
   * modifier: 	          NO_ROUTE    <br>
	 */
	public SipApplicationRouterInfo handleRouterEnquery(
      SipServletRequest initialRequest,
      SipApplicationRoutingRegion region,
      SipApplicationRoutingDirective directive,
      Serializable stateInfo) throws TckApplicationRouterException {

    String nextApplicationName = initialRequest.getHeader("Application-Name");
    if (nextApplicationName == null)
      throw new TckApplicationRouterException("Can not find application-name header in request");

    return new SipApplicationRouterInfo(
      nextApplicationName,
      new SipApplicationRoutingRegion("BEA-TCK", SipApplicationRoutingRegionType.ORIGINATING),
      initialRequest.getFrom().getURI().toString(),
      null,
      SipRouteModifier.NO_ROUTE,
      null);
	}

	/**
	 * 
	 * @param router
	 */
	public TckApplicationRouterApiCaseHandler(TckApplicationRouter router){
    super(router);

  }

}