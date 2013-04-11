/**
 *(c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipSessionsUtilTest is used to test the APIs of
 * javax.servlet.sip.SipSessionsUtil
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

import java.io.IOException;

public class SipSessionsUtilTest extends TestBase {
  public SipSessionsUtilTest(String arg0) throws IOException {
		super(arg0);
	}

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipSessionUtil1",
      desc = "Test to get the SipApplicationSession for" +
          " a given applicationSessionId. "
  )
  public void testGetApplicationSessionById001(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipSessionUtil1",
      desc = "NullPointerException should be thrown if the applicationSessionId is null"
  )
  public void testGetApplicationSessionById101(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipSessionUtil2",
      desc = "Test to get the SipApplicationSession for" +
          " a given session applicationSessionKey. "
  )
  public void testGetApplicationSessionByKey001(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipSessionUtil2",
      desc = "NullPointerException should be thrown if the applicationSessionKey is null"
  )
  public void testGetApplicationSessionByKey101(){
    assertSipMessage();
  }
}
