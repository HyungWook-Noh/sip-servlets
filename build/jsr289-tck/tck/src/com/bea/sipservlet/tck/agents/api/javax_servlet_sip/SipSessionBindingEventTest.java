/**
 *(c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 * 
 * SipSessionBindingEventTest is used to test the Constructor of
 * javax.servlet.sip.SipSessionBindingEvent.Other APIs of SipSessionBindingEvent
 * are tested in SipSessionAttributeListenerTest
 *
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;

import java.io.IOException;

public class SipSessionBindingEventTest  extends TestBase {
  private static Logger logger =
      Logger.getLogger(SipSessionBindingEventTest.class);

  public SipSessionBindingEventTest(String arg0) throws IOException {
		super(arg0);
	}

  @AssertionIds(
      ids="SipServlet:JAVADOC:SipSessionBindingEvent1",
      desc="Test the constructor of the SipSessionBindingEventTest. Other APIs" +
          "of SipSessionBindingEvent are tested in SipSessionAttributeListenerTest"
  )
  public void testSipSessionBindingEvent001(){
    assertSipMessage();
  }
}
