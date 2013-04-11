/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipRouteModifierTest is used to test the APIs of
 * javax.servlet.sip.ar.SipRouteModifierTest.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip_ar;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

import java.io.IOException;

public class SipRouteModifierTest extends TestBase {
  public SipRouteModifierTest(String arg0) throws IOException {
    super(arg0);
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipRouteModifier1"
      )
  public void testValueOf001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipRouteModifier1")
  public void testValues001() {
    assertSipMessage();
  }
}
