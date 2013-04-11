/**
 *
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * SessionLifetimeProxyTest is used to test the session lifetime mechanism
 * in JSR289. The corresponding SipServlet is "SessionLifetimeProxyServlet",
 * "SessionLifetimeUacServlet" and "SessionLifetimeUasServlet".
 *
 */
package com.bea.sipservlet.tck.agents.spec;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipRequest;
import org.cafesip.sipunit.SipResponse;

import javax.sip.RequestEvent;
import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.SubjectHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class SessionLifetimeTest extends TestBase {

  private static Logger logger = Logger.getLogger(SessionLifetimeTest.class);

  public SessionLifetimeTest(String arg0)
      throws IOException {
    super(arg0);
  }

  /**
   * Tests the ability of SIP Server to perform the InvalidateWhenReady
   * mechanism and notify application through ReadyToInvalidate() callback.
   * <pre>
   * UA1                             Proxy                       UA2
   * |                                |                           |
   * |---------- (1) INVITE  -------->|                           |
   * |                                |-------- (2) INVITE ------>|
   * |                                |                           |
   * |                                |<------- (3) 200 ----------|
   * |<--------- (4) 200  ------------|                           |
   * |                                |                           |
   * |---------- (5) ACK  ----------->|                           |
   * |                                |-------- (6) ACK --------->|
   * |                                |                           |
   * |---------- (7)  BYE    -------->|                           |
   * |                                |-------- (8) BYE  -------->|
   * |                                |                           |
   * |                                |<------- (9) 200 ----------|
   * |<--------- (10) 200 ------------|                           |
   * |                                |                           |
   * |<--------- (11) MESSAGE --------|
   * |                                |
   * |---------- (12) 200   --------->|
   * |                                |
   * |<--------- (13) MESSAGE --------|
   * |                                |
   * |---------- (14) 200 ----------->|
   * <p/>
   * </pre>
   * MESSAGEs will be sent to the UA1 when the sessionReadyToInvalidate()
   * get called back for the relevant SipSession and SipApplicationSession.
   */
  @TargetApplication(value = ApplicationName.PROXY)
  @AssertionIds(
      ids = {"SipServlet:SPEC:SessionLifetime1"},
      desc = " Tests the ability of SIP Server to perform the " 
      	+	"InvalidateWhenReady mechanism under proxy")
  public void testProxySessionLifetime() throws ParseException {
    clientEntryLog();

    String SERVLET_NAME = "SessionLifetimeProxy";
    String HEADER_TCK_PROXY_DEST = "TCK-Proxy-Dest";

    ua1.listenRequestMessage();
    ua2.listenRequestMessage();
    SipCall ua2Call = ua2.createSipCall();

    // (1) UA1 Send INVITE
    SipCall ua1Call = ua1.createSipCall();

    List<Header> headers = new ArrayList<Header>(2);
    headers.add(ua1Call.getHeaderFactory()
    	.createHeader(HEADER_TCK_PROXY_DEST, ua2URI));

    boolean result = 
    	initiateOutgoingCall(ua1Call, null, SERVLET_NAME, headers, null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), result);

    // (2) UA2 receive INVITE
    assertTrue("Don't receive INVITE", ua2Call.waitForRequest(
        SipRequest.INVITE, waitDuration / 2));

    // (3) UA2 send 200
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);

    // (4) UA1 receive 200
    waitNon100Response(ua1Call, waitDuration / 2);
    assertResponseReceived("Don't receive 200", SipResponse.OK, ua1Call);

    // (5) UA1 send ACK
    assertTrue(ua1Call.sendInviteOkAck());

    // (6) UA2 receive ACK
    assertTrue("Don't receive ACK", 
    	ua2Call.waitForRequest(SipRequest.ACK, waitDuration / 2));

    // (7) UA1 send BYE
    assertTrue(ua1Call.disconnect());

    // (8) UA2 receive BYE
    assertTrue("Don't receive BYE", 
    	ua2Call.waitForRequest(SipRequest.BYE, waitDuration / 2));

    // (9) UA2 send 200
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);

    // (10) UA1 receive 200/BYE 
    try {
		Thread.sleep(waitDuration / 2);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
    assertResponseReceived("Did not receive 200 OK on BYE", Response.OK,
              Request.BYE, 2, ua1Call);

    // (11) UA1 receive the MESSAGE indicating the proxy SipSession has been
    // automatically invalidated
    logger.debug("--- waiting for MESSAGE---");

    RequestEvent reqEvent = ua1.waitRequest(waitDuration * 4);
    assertNotNull(reqEvent);
    Request messageReq = reqEvent.getRequest();
    logger.debug("--- UA1 receive the MESSAGE req is:" + messageReq + "---");
    assertEquals(
    	"didn't receive MESSAGE", Request.MESSAGE, messageReq.getMethod());

    checkMessageContent(messageReq);

    // (12) UAC send 200/MESSAGE back.
    sendResponseForMessage(ua1, messageReq, Response.OK);

    // (13) UA1 receive the MESSAGE indicating the proxy SipSession has been
    // automatically invalidated
    reqEvent = ua1.waitRequest(waitDuration * 4);
    assertNotNull(reqEvent);
    messageReq = reqEvent.getRequest();
    logger.debug("--- UA1 receive the MESSAGE req is:" + messageReq + "---");
    assertEquals(
    	"didn't receive MESSAGE", Request.MESSAGE, messageReq.getMethod());

    checkMessageContent(messageReq);

    // (14) UAC send 200/MESSAGE back.
    sendResponseForMessage(ua1, messageReq, Response.OK);
  }

  /**
   * Tests the ability of SIP Server to perform the InvalidateWhenReady
   * mechanism and notify application through ReadyToInvalidate() callback.
   * <pre>
   * UA1                                 B2BUA (Servlet)                   UA2
   * |                                    |                               |
   * |------------- (1) MESSAGE  -------->|                               |
   * |                                    |                               |
   * |<------------ (2) 200 OK -----------|                               |
   * |                                    |--------- (3) INVITE --------->|
   * |                                    |                               |
   * |                                    |<-------- (4) 180  ------------|
   * |                                    |                               |
   * |                                    |<-------- (5)  200   ----------|
   * |                                    |                               |
   * |                                    |--------- (6)  ACK   --------->|
   * |                                    |                               |
   * |                                    |                               |
   * |                                    |<-------- (7)  BYE   ----------|
   * |                                    |                               |
   * |                                    |--------- (8)  200   --------->|
   * |                                    |                               |
   * |<----------- (9)  MESSAGE  ---------|                               |
   * |                                    |                               |
   * |------------ (10)  200 OK   ------->|                               |
   * |                                    |                               |
   * |<----------- (11) MESSAGE  ---------|                               |
   * |                                    |                               |
   * |------------ (12)  200 OK --------->|                               |
   * |                                    |                               |
   * </pre>
   * MESSAGEs will be sent to the UA1 when the sessionReadyToInvalidate() get
   * called back for the relevant SipSession and SipApplicationSession.
   */
  @TargetApplication(value = ApplicationName.UAC)
  @AssertionIds(
      ids = {"SipServlet:SPEC:SessionLifetime2"},
      desc = " Tests the ability of SIP Server to perform the InvalidateWhenReady "
          + "mechanism for sipsession working in UAC mode")
  public void testUacSessionLifetime() throws Exception {

    clientEntryLog();

    // The target servlet for this test
    String SERVLET_NAME = "SessionLifetimeUac";
    String HEADER_TCK_UA2_DEST = "TCK-UA2-Dest";

    ua1.listenRequestMessage();

    // (1) UA1 send MESSAGE to trigger the start of call flow, 
    // and (2) receives 200/MESSAGE

    List<Header> headers = new ArrayList<Header>(2);
    headers.add(ua1.getParent().getHeaderFactory()
    	.createHeader(HEADER_TCK_UA2_DEST, ua2URI));

    assertSipMessage(headers, SERVLET_NAME, null, Request.MESSAGE, 1);

    //    (3) UA2 receive INVITE
    SipCall ua2Call = ua2.createSipCall();
    ua2Call.listenForIncomingCall();

    ua2Call.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess(
    	"clientB wait for incoming call - " + ua2Call.format(), ua2Call);

    //     (4) UA2 send 180
    ua2Call.sendIncomingCallResponse(Response.RINGING, null, -1);
    assertLastOperationSuccess(
    	"ClientB send 180 - " + ua2Call.format(), ua2Call);

    //    (5) UA2 send 200/invite
    ua2Call.sendIncomingCallResponse(Response.OK, null, -1);
    assertLastOperationSuccess(
    	"ClientB send 200 - " + ua2Call.format(), ua2Call);

    //  (6) UA2 receive ACK
    ua2Call.waitForAck(waitDuration);
    assertLastOperationSuccess(
    	"clientB wait for ACK - " + ua2Call.format(), ua2Call);

    logger.debug("--- UA2 received ACK ---");

    // (7) UA2 send BYE
    assertTrue(ua2Call.disconnect());

    // (8) UA2 receive 200/BYE
    waitFor200Bye(ua2Call);

    // (9) UA1 receive the MESSAGE indicating the UAC SipSession has been
    // automatically invalidated
    logger.debug("--- UA1 waiting for MESSAGE---");

    RequestEvent reqEvent = ua1.waitRequest(waitDuration * 4);
    assertNotNull(reqEvent);
    Request messageReq = reqEvent.getRequest();
    logger.debug("--- UA1 receive the MESSAGE req is:" + messageReq + "---");
    assertEquals("didn't receive MESSAGE", Request.MESSAGE, messageReq.getMethod());

    checkMessageContent(messageReq);

    // (10) UA1 send 200/MESSAGE back.
    sendResponseForMessage(ua1, messageReq, Response.OK);

    // (11) UA1 receive the MESSAGE indicating the UAC SipSession has been
    // automatically invalidated
    reqEvent = ua1.waitRequest(waitDuration * 4);
    assertNotNull(reqEvent);
    messageReq = reqEvent.getRequest();
    logger.debug("--- UA1 receive the MESSAGE req is:" + messageReq + "---");
    assertEquals("didn't receive MESSAGE", Request.MESSAGE, messageReq.getMethod());

    checkMessageContent(messageReq);

    // (12) UA1 send 200/MESSAGE back.
    sendResponseForMessage(ua1, messageReq, Response.OK);
  }

  /**
   * Tests the ability of SIP Server to perform the InvalidateWhenReady
   * mechanism and notify application through ReadyToInvalidate() callback.
   * <p/>
   * <pre>
   * UAC                           UAS( servlet)
   * |                                |
   * |----------- (1) INVITE -------->|
   * |                                |
   * |<---------- (2) 200   ----------|
   * |                                |
   * |----------- (3) ACK  ---------->|
   * |                                |
   * |<---------- (4) BYE   ----------|
   * |                                |
   * |----------  (5) 200 ----------->|
   * |                                |
   * |<---------- (6) MESSAGE --------|
   * |                                |
   * |----------- (7) 200   --------->|
   * |                                |
   * |<---------- (8) MESSAGE --------|
   * |                                |
   * |----------- (9) 200   --------->|
   * </pre>
   * <p/>
   * MESSAGEs will be sent to the UA1 when the sessionReadyToInvalidate() get
   * called back for the relevant SipSession and SipApplicationSession.
   */
  @TargetApplication(value = ApplicationName.UAS)
	@AssertionIds(ids = { "SipServlet:SPEC:SessionLifetime3" }, desc = " Tests the ability of SIP Server to perform the InvalidateWhenReady"
			+ " mechanism for sipsession working in UAS mode")
	public void testUasSessionLifetime() throws Exception {

		clientEntryLog();
		// The target servlet for this test
		String SERVLET_NAME = "SessionLifetimeUas";

		// (1) UA1 make a outgoing call
		SipCall callA = ua1.createSipCall();
		String viaNonProxyRoute = serverHost + ":" + serverPort + "/"
				+ testProtocol;
		initiateOutgoingCall(callA, null, SERVLET_NAME, null, viaNonProxyRoute);
		assertLastOperationSuccess("UA1 initiate call - " + callA.format(),
				callA);

		// (2) UA1 receives 200/invite
		do {
			callA.waitOutgoingCallResponse(waitDuration);
			assertLastOperationSuccess("UA1 wait for 200/INVITE - "
					+ callA.format(), callA);
		} while (callA.getReturnCode() != Response.OK);
		logger.debug("--- UA1 received 200 ---");

		// (3) UA1 send Ack
		callA.sendInviteOkAck();
		assertLastOperationSuccess("Failure sending ACK - " + callA.format(),
				callA);
		logger.debug("--- UA1 sent ACK ---");

		// (4) UA1 receive BYE
		callA.listenForDisconnect();
		assertLastOperationSuccess("UA1 listen disc - " + callA.format(), callA);
		callA.waitForDisconnect(waitDuration);
		assertLastOperationSuccess("UA1 wait for BYE - " + callA.format(),
				callA);

		// (5) UA1 send 200/BYE
		callA.respondToDisconnect();
		assertLastOperationSuccess("UA1 respond to disc - " + callA.format(),
				callA);

		// (6) UA1 receive the MESSAGE indicating the UAS SipSession has been
		// automatically invalidated
		logger.debug("--- UA1 waiting for MESSAGE---");

		RequestEvent reqEvent = ua1.waitRequest(waitDuration);
		assertNotNull(reqEvent);
		Request messageReq = reqEvent.getRequest();
		logger
				.debug("--- UA1 receive the MESSAGE req is:" + messageReq
						+ "---");
		assertEquals("didn't receive MESSAGE", Request.MESSAGE, messageReq
				.getMethod());

		checkMessageContent(messageReq);

		// (7) UA1 send 200/MESSAGE back.
		sendResponseForMessage(ua1, messageReq, Response.OK);

		// (8) UA1 receive the MESSAGE indicating the UAS SipSession has been
		// automatically invalidated
		reqEvent = ua1.waitRequest(waitDuration);
		assertNotNull(reqEvent);
		messageReq = reqEvent.getRequest();
		logger
				.debug("--- UA1 receive the MESSAGE req is:" + messageReq
						+ "---");
		assertEquals("didn't receive MESSAGE", Request.MESSAGE, messageReq
				.getMethod());

		checkMessageContent(messageReq);

		// (9) UA1 send 200/MESSAGE back.
		sendResponseForMessage(ua1, messageReq, Response.OK);
	}


  private void waitFor200Bye(SipCall sipCall) throws InterruptedException {
    Thread.sleep(waitDuration);
    assertLastOperationSuccess("clientB wait for 200/BYE - "
        + sipCall.format(), sipCall);
    Response okForBye = (Response) sipCall.getLastReceivedResponse()
        .getMessage();
    assertNotNull("200/BYE", okForBye);
    assertEquals("clientB wait for 200/BYE - ", Response.OK, okForBye
        .getStatusCode());
    assertEquals("Unexpected response", Request.BYE, ((CSeqHeader) okForBye
        .getHeader(CSeqHeader.NAME)).getMethod());
  }

  private void waitNon100Response(SipCall ua, int wait) {
    int tryTimes = 1;
    int maxTryTimes = 4;
    while (maxTryTimes > tryTimes) {
      if (ua.waitOutgoingCallResponse(wait)) {
        logger.debug("--- response status:" + ua.getReturnCode());
        if (ua.getReturnCode() != Response.TRYING) {
          return;
        }
      }
      tryTimes++;
    }
    return;
  }

  //Check the content of this Message Request
  private void checkMessageContent(Request messageReq) {
    SubjectHeader subjectHdr = (SubjectHeader) messageReq.getHeader("Subject");
    assertTrue("Got failure Message when waiting for ReadyToInvalidate callback",
        subjectHdr.getSubject().equals("SUCCESS"));

  }
    
}
