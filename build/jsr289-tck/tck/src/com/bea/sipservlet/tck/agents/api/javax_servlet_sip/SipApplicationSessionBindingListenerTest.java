/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved. 
 *  
 * SipApplicationSessionBindingListenerTest is used to test the APIs of
 * javax.servlet.sip.SipApplicationSessionBindingListener.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class SipApplicationSessionBindingListenerTest extends TestBase {

  public SipApplicationSessionBindingListenerTest(String arg0)
      throws IOException {
    super(arg0);
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSessionBindingListener1" }, 
      desc = "Notify the object that it is being bound to an "
      + "application session and identifies the application session.")
  public void testValueBound001() {
    assertSipMessage();
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSessionBindingListener2" }, 
      desc = "Notify the object that it is being unbound from an "
      + "application session and identifies the application session.")
  public void testValueUnbound001() {
    assertSipMessage();
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSessionBindingEvent2" }, 
      desc = "Returns the application session to or from "
      + "which the object is bound or unbound.")
  public void testGetApplicationSession001() {
    assertSipMessage();
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSessionBindingEvent3" }, 
      desc = "Returns the name with which the object is bound or unbound from the"
      + " application session.")
  public void testGetName001() {
    assertSipMessage();
  }
}
