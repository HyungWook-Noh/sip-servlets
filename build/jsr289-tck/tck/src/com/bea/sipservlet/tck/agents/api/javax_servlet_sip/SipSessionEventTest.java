/**
 *(c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipSessionEventTest is used to test the constructor of
 * javax.servlet.sip.SipSessionEvent. Other APIs are tested in
 * SipSessionListenerTest.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

import java.io.IOException;

public class SipSessionEventTest extends TestBase {
  public SipSessionEventTest(String arg0) throws IOException {
		super(arg0);
	}

  @AssertionIds(
      ids="SipServlet:JAVADOC:SipSessionEvent1",
      desc="Test the constructor of the SipSessionEventTest. Other APIs" +
          "of SipSessionEvent are tested in SipSessionListenerTest"
  )
  public void testSipSessionEvent001(){
    assertSipMessage();
  }
}
