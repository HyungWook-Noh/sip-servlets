/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved. 
 *  
 * SipApplicationSessionEventTest is used to test the APIs of
 * javax.servlet.sip.SipApplicationSessionEvent.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;


public class SipApplicationSessionEventTest extends TestBase {

  public SipApplicationSessionEventTest(String arg0) throws IOException {
    super(arg0);
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSessionEvent1" }, 
      desc = "Creates a new SipApplicationSessionEvent object.")
  public void testSipApplicationSessionEvent001() {
    assertSipMessage();
  }
}
