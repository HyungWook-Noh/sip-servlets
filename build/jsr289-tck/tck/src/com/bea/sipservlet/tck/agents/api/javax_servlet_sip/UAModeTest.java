/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * UAModeTest is used to test the APIs of 
 * javax.servlet.sip.UAMode
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class UAModeTest extends TestBase {
  public UAModeTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:UAMode1"},
      desc = "User agents can get the enum constant of this type with the specified name.")
  public void testValueOf001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:UAMode2"},
      desc = "User agents can get an array containing the constants of this enum type, "
        + "in the order they're declared.")
  public void testValues001() {
    assertSipMessage();
  }
}
