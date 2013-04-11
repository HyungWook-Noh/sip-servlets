/**
 *(c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipSessionBindingListenerTest is used to test the APIs of
 * javax.servlet.sip.SipSessionBindingListener.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;

import java.io.IOException;

public class SipSessionBindingListenerTest extends TestBase {
  private static Logger logger =
      Logger.getLogger(SipSessionBindingListenerTest.class);

  public SipSessionBindingListenerTest(String arg0) throws IOException {
		super(arg0);
	}

  @AssertionIds(
      ids="SipServlet:JAVADOC:SipSessionBindingListener1",
      desc="Test if notification if fired when the attribute has been " +
        "bound with a session."
  )
  public void testValueBound001(){
    assertSipMessage();
  }
  @AssertionIds(
      ids="SipServlet:JAVADOC:SipSessionBindingListener2",
      desc="Test if notification if fired when the attribute has been " +
        "unbound from a session."
  )
  public void testValueUnbound001(){
    assertSipMessage();
  }
}
