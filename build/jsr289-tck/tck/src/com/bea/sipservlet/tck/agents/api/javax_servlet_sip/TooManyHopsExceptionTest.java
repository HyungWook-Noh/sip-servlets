/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * TooManyHopsExceptionTest is used to test the APIs of 
 * javax.servlet.sip.TooManyHopsException
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class TooManyHopsExceptionTest extends TestBase {
  public TooManyHopsExceptionTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:TooManyHopsException1",
    		  "SipServlet:JAVADOC:TooManyHopsException2",
    		  "SipServlet:JAVADOC:TooManyHopsException3",
    		  "SipServlet:JAVADOC:TooManyHopsException4"},
      desc = "User agents can construct a new TooManyHopsException.")
  public void testTooManyHopsException001() {
    assertSipMessage();
  }
}


