/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipSessionAttributeListenerTest is used to test the APIs of
 * javax.servlet.sip.SipSessionAttributeListener and
 * javax.servlet.sip.SipSessionBindingEvent.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;

import java.io.IOException;

public class SipSessionAttributeListenerTest extends TestBase {
  private static Logger logger =
      Logger.getLogger(SipSessionAttributeListenerTest.class);

  public SipSessionAttributeListenerTest(String arg0) throws IOException {
		super(arg0);
	}

  @AssertionIds (
    ids = {"SipServlet:JAVADOC:SipSessionAttributeListener1",
           "SipServlet:JAVADOC:SipSessionBindingEvent2",
           "SipServlet:JAVADOC:SipSessionBindingEvent3"},
    desc = "Test if notification if fired when an attribute has been " +
        "added to a session."
  )
  public void testAttributeAdded001(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipSessionAttributeListener2",
      desc = "Test if notification if fired when an attribute has been " +
          "removed from a session."
  )
  public void testAttributeRemoved001(){
    assertSipMessage();
  }

  @AssertionIds(
    ids = "SipServlet:JAVADOC:SipSessionAttributeListener3",
      desc = "Test if notification if fired when an attribute has been " +
          "replaced in a session."
  )
  public void testAttributeReplaced001(){
    assertSipMessage();
  }    

}
