/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 * @version 1.0
 * @created 01-April-2008 17:01:42
 */

package com.bea.sipservlet.tck.ar;

import javax.servlet.sip.ar.spi.SipApplicationRouterProvider;
import javax.servlet.sip.ar.SipApplicationRouter;

public class TckApplicationRouterProvider extends SipApplicationRouterProvider {

  private static SipApplicationRouter router = new TckApplicationRouter();
  public SipApplicationRouter getSipApplicationRouter() {
    return router;
  }
}
