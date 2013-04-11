/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.
 *  
 * SipApplicationSessionAttributeListenerTest is used to test the APIs of
 * javax.servlet.sip.SipApplicationSessionAttributeListener.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;


public class SipApplicationSessionAttributeListenerTest extends TestBase{
    
  public SipApplicationSessionAttributeListenerTest(String arg0)
      throws IOException {
    super(arg0);
  }
 
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSessionAttributeListener1" }, 
      desc = "Notification that an attribute has been added to "
      + "an application session. Called after the attribute is added.")
  public void testAttributeAdded001() {
    assertSipMessage();
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSessionAttributeListener2" }, 
      desc = "Notification that an attribute has been removed from "
      + "an application session. Called after the attribute is removed.")
  public void testAttributeRemoved001() {
    assertSipMessage();
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSessionAttributeListener3" }, 
      desc = "Notification that an attribute has been replaced in "
      + "an application session. Called after the attribute is replaced.")
  public void testAttributeReplaced001() {
    assertSipMessage();
  }
}