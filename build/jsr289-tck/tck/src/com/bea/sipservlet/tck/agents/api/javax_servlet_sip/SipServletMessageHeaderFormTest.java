/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * SipServletMessageHeaderFormTest is used to test the APIs of 
 * javax.servlet.sip.SipApplicationSession.Protocol
 *
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class SipServletMessageHeaderFormTest extends TestBase {

	public SipServletMessageHeaderFormTest(String arg0) throws IOException {
		super(arg0);
	}
	
	@AssertionIds(
      ids = {"SipServlet:JAVADOC:SipServletMessageHeaderForm1"},
      desc = "User agents can the enum constant of this type with the specified name.")
  public void testValueOf001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipServletMessageHeaderForm2"},
      desc = "User agents can get an array containing the constants " 
      	+ "of this enum type, in the order they're declared.")
  public void testValues001() {
    assertSipMessage();
  }

}
