/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved. 
 *  
 * SipApplicationSessionBindingEventTest is used to test the APIs of 
 * javax.servlet.sip.SipApplicationSessionBindingEvent.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;


public class SipApplicationSessionBindingEventTest extends TestBase {

  public SipApplicationSessionBindingEventTest(String arg0) throws IOException {
    super(arg0);
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSessionBindingEvent1" }, 
      desc = "Constructs an event that notifies an object that it "
      + "has been bound to or unbound from an application session.")
  public void testSipApplicationSessionBindingEvent001() {
    assertSipMessage();
  }
}
