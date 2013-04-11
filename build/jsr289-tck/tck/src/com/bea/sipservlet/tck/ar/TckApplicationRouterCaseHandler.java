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
import java.io.Serializable;

public abstract class TckApplicationRouterCaseHandler {

  private final TckApplicationRouter router;
  public TckApplicationRouterCaseHandler(TckApplicationRouter router) {
    this.router = router; 
  }

  protected TckApplicationRouter getRouter() {
    return router;
  }

  // mihirk - spell-check: this should be handleRouterInquiry.
  public abstract SipApplicationRouterInfo handleRouterEnquery(
      SipServletRequest initialRequest,
      SipApplicationRoutingRegion region,
      SipApplicationRoutingDirective directive,
      Serializable stateInfo) throws TckApplicationRouterException;

  protected boolean compareSipUri(String uri01, String uri02) {

    TckSipURIParser parser01 = new TckSipURIParser(),
                    parser02 = new TckSipURIParser();

    parser01.parseURI(uri01);
    parser02.parseURI(uri02);

    return parser01.equals(parser02);
  }

  protected boolean compareAddress(String addr01, String addr02) {

    TckSipURIParser parser01 = new TckSipURIParser(),
                    parser02 = new TckSipURIParser();

    parser01.parseRoute(addr01);
    parser02.parseRoute(addr02);

    return parser01.equals(parser02);
  }
}