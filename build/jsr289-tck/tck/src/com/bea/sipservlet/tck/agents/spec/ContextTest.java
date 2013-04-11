/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * ContextTest is used to test a number of ServletContext methods,
 * including ability to retrieve content and servlet parameters. Other
 * tests ensure the servlet context includes SipFactory and TimerService.
 *
 */

package com.bea.sipservlet.tck.agents.spec;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.cafesip.sipunit.SipTransaction;

import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;

@TargetApplication(ApplicationName.UAS)
public class ContextTest extends TestBase {

  public ContextTest(String arg0) throws IOException {
    super(arg0);
  }

  /**
   * For testContext1(), the call flow is:
   *
   * UA1                              SUT
   *  |                                |
   *  |-------- (1) OPTION ----------->|
   *  |                                |
   *  |<------- (2) 200(500) ----------|
   *  |                                |
   *
   */
  @AssertionIds(
      ids = {"SipServlet:SPEC:Context1"},
      desc = "Tests a number of ServletContext methods, including " +
          "ability to retrieve content and servlet parameters. " +
          "Other tests ensure the servlet context includes " +
          "SipFactory and TimerService.")
  public void testContext1(){
    clientEntryLog();

    // (1) send OPTION
    Request option = assembleRequest(Request.OPTIONS, "Context", "", "ua", 1);
    SipTransaction trans = ua1.sendRequestWithTransaction(option, true, null);
    assertNotNull(ua1.format(), trans);

    // (2) receive 200 OK
    Response response = waitResponseForMessage(ua1, trans, waitDuration);
    if (response.getStatusCode() != Response.OK) {
      fail("Fail to receive 200 OK for OPTION." + response.getReasonPhrase());
    }
  }
}
