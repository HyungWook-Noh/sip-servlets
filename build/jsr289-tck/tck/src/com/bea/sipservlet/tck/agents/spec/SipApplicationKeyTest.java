/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 * 
 * SipApplicationKeyTest is used to test the specification of
 * Application Session Key feauture
 */
package com.bea.sipservlet.tck.agents.spec;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.cafesip.sipunit.SipTransaction;

import javax.sip.ResponseEvent;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;

public class SipApplicationKeyTest extends TestBase {
  static final int WAIT_DURATION = 500;
  public SipApplicationKeyTest(String arg0) throws IOException {
    super(arg0);
  }

  /**
   *       UA1(1st)            B2BUA                 UAS                  UA1(2nd)
   *        |                    |                    |                    |
   *        |                  key1                   |                    |
   *        |--- (1) MESSAGE --/>|                  key1                   |
   *        |                 /  |--- (2) MESSAGE --->|\                   |
   *        |                /   |                    | \                  |
   *        |               /    |<-- (3) 200 --------|  \                 |
   *        |<-- (4) 200 --/-----|                    |   \                |
   *        |             /      |                    | different app session
   *        |            /       |                    |   /                |
   *        |           /        |                    |  /                 |
   *        |    same app session|                    | /                  |
   *        |                  \ |                    |/                   |
   *        |                  key1                 key2                   |
   *        |                    |<------------ (5) MESSAGE ---------------|
   *        |                    |--- (6) MESSAGE --->|                    |
   *        |                    |                    |                    |
   *        |                    |<-- (7) 200 --------|                    |
   *        |                    |--------------(8) 200 ------------------>|
   *        |                    |                    |                    |
   *        |                    |                    |                    |
   *        |                    |                    |                    |
   *        |                    |                    |                    |
   *
   * Description: verify same application and same key target to the same app session;
   *   same application and different key target to different app sessions;
   *   different application and same key target to different app sessions.
   *
   * @throws Exception
   */
  @AssertionIds(ids = {"SipServlet:SPEC:SipApplicationSessionKey1"},
      desc = "verify same application and same key target to the same app session;" +
          " same application and different key target to different app sessions;" +
          " different application and same key target to different app sessions.")
  @TargetApplication(ApplicationName.B2BUA)
  public void testGetApplicationSessionKey1() throws Exception {
    clientEntryLog();

    // Init test case
    String testCaseName = "SipApplicationKey";

    // (1) UA1 send MESSAGE
    Request request1 = assembleRequest("MESSAGE", testCaseName, null, "UA", 1);
    addStepHeader(request1, "1");
    // (4) Wait for 200/MESSAGE
    ResponseEvent responseEvent1 = (ResponseEvent)
        ua1.waitResponse(ua1.sendRequestWithTransaction(request1, false, null), WAIT_DURATION);
    Response response1 = responseEvent1.getResponse();
    assertEquals(response1.getReasonPhrase(), Response.OK,
        response1.getStatusCode());

    // (5) UA1 send MESSAGE
    Request request2 = assembleRequest("MESSAGE", testCaseName, null, "UA", 1);
    addStepHeader(request2, "5");

    // (8) Wait for 200/MESSAGE
    ResponseEvent responseEvent2 = (ResponseEvent)
        ua1.waitResponse(ua1.sendRequestWithTransaction(request2, false, null), WAIT_DURATION);
    Response response2 = responseEvent2.getResponse();
    assertEquals(response2.getReasonPhrase(), Response.OK,
        response2.getStatusCode());
  }

  /**
   *       UA1(1st)            Proxy                 UAS                  UA1(2nd)
   *        |                    |                    |                    |
   *        |                   null                  |                    |
   *        |--- (1) INVITE ---->|                  key2                   |
   *        |                    |--- (2) INVITE ---->|\                   |
   *        |                    |                    | \                  |
   *        |                    |<-- (3) 200 --------|  \                 |
   *        |<-- (4) 200 --------|                    |   \                |
   *        |                    |                    | same app session   |
   *        |--- (5) ACK ------->|                    |    /               |
   *        |                    |--- (6) ACK ------->|   /                |
   *        |  different app sessions                 |  /                 |
   *        |                    |                    | /                  |
   *        |                   null                  |/                   |
   *        |                    |<------------- (7) INVITE ---------------|
   *        |                    |                  key2                   |
   *        |                    |--- (8) INVITE ---->|                    |
   *        |                    |                    |                    |
   *        |                    |<-- (9) 200 --------|                    |
   *        |                    --------------- (10) INVITE --------------|
   *        |                    |                    |                    |
   *        |                    |                    |                    |
   *
   * Description: verify same application with same key target to the same app session;
   *   same application and null key target to different app sessions;
   *
   * @throws Exception
   */
  @AssertionIds(ids = {"SipServlet:SPEC:SipApplicationSessionKey2"},
      desc = "Verify same application with same key target to the same app session;" +
          " and same application with null key target to different app sessions.")  
  @TargetApplication(ApplicationName.PROXY)
  public void testGetApplicationSessionKey2() throws Exception {
    clientEntryLog();

    // Init test case
    String testCaseName = "SipApplicationKey";

    // (1) UA1 send INVITE
    Request request1 = assembleRequest("INVITE", testCaseName, null, "UA", 1);
    addStepHeader(request1, "1");
    // (4) Wait for 200/INVITE
    waitInvite200(ua1.sendRequestWithTransaction(request1, false, null));


    // (7) UA1 send INVITE
    Request request2 = assembleRequest("INVITE", testCaseName, null, "UA", 1);
    addStepHeader(request2, "7");
    // (8) Wait for 200/INVITE
    waitInvite200(ua1.sendRequestWithTransaction(request2, false, null));
  }

  public void addStepHeader(Message message, String step) throws Exception {
    message.addHeader(ua1.getParent().getHeaderFactory().createHeader(TestConstants.TEST_STEP_HEADER, step));
  }

  public void waitInvite200(SipTransaction transaction) {
    Response response;
    do {
      ResponseEvent responseEvent2 = (ResponseEvent)
          ua1.waitResponse(transaction, WAIT_DURATION);
      response = responseEvent2.getResponse();
    } while (Response.TRYING == response.getStatusCode());
    assertEquals(response.getReasonPhrase(), Response.OK,
        response.getStatusCode());
  }
}
