/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * CommittedStateTest is used to test  
 * committed state of SipServletMessage.
 *
 */
package com.bea.sipservlet.tck.agents.spec;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipResponse;

import javax.sip.InvalidArgumentException;
import javax.sip.address.URI;
import javax.sip.SipException;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.text.ParseException;

public class CommittedStateTest extends TestBase {

  public CommittedStateTest(String arg0) throws IOException {
    super(arg0);
  }
  /*
   *       UA1                             B2BUA                                UA2
   *        |                                |                                   |
   *        |-------- (1) MESSAGE----------->|                                   |
   *        |                                |                                   |
   *        |<------- (2) 200 OK ------------|                                   |
   *        |                                |------------- (3) INVITE --------->|
   *        |                                |                                   |
   *        |                                |<------------ (4) 180 Ring --------|
   *        |                                |                                   |
   *        |                                |<------------ (5) 200 OK ----------|
   *        |                                |                                   |
   *        |                                |------------- (6) ACK ------------>|
   *        |                                |                                   |
   *        |                                |                                   |
   *        |                                |<------------ (7) BYE  ------------|
   *        |                                |                                   |
   *        |                                |------------- (8) 200 OK  -------->|
   *        |                                |                                   |
   *
   */
  @TargetApplication(value = ApplicationName.B2BUA)
  @AssertionIds(
      ids = {"SipServlet:SPEC:Committed1"},
      desc = "To test the committed state of SipServletMessage under B2BUA mode")
  public void testCommittedStateUnderB2bua()
      throws SipException, ParseException, InvalidArgumentException, InterruptedException {

    String servletName = "CommittedStateUnderB2bua";

    //(0) UA2 listens to incoming invite
    SipCall callB = ua2.createSipCall();
    callB.listenForIncomingCall();

    //(1) and (2) UA1 sends Message
    String target = "sip:" + ua2UserName + "@" + ua2Host + ":" + ua2Port;
    ArrayList headers = new ArrayList(2);
    URI uri = callB.getAddressFactory().createURI(target);
    headers.add(callB.getHeaderFactory().createCallInfoHeader(uri));

    headers.add(callB.getHeaderFactory().createHeader("TCK",target));
    assertSipMessage(headers, servletName, null, Request.MESSAGE, 1);

    //(3) UA2 receives INVITE
    callB.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("UA2 waits for incoming call - " + callB.format(), callB);

    //(4) UA2 sends 180/INVITE
    callB.sendIncomingCallResponse(Response.RINGING, null, -1);
    assertLastOperationSuccess("UA2 sends 180 - " + callB.format(), callB);

    //(5) UA2 send 200/INVITE
    Thread.sleep(waitDuration);
    callB.sendIncomingCallResponse(Response.OK, null, -1);
    assertLastOperationSuccess("UA2 sends 200 - " + callB.format(), callB);

    //(6) UA2 waits for ACK
    callB.waitForAck(waitDuration);
    assertLastOperationSuccess("UA2 waits for ACK - " + callB.format(), callB);

    //(7) UA2 sends BYE
    callB.disconnect();
    assertLastOperationSuccess("UA2 sends BYE - " + callB.format(), callB);

    //(8) UA2 waits for 200/BYE
    Thread.sleep(waitDuration);
    SipResponse respBye = callB.getLastReceivedResponse();
    assertNotNull("Expected 200/BYE should not be null", respBye);
    assertEquals("Expected message should be 200/BYE", respBye.getStatusCode(), Response.OK);
  }

  /*
  *   UA1                                Proxy                            UA2
  *    |                                   |                               |
  *    |---------- (1) INVITE  ----------->|                               |
  *    |                                   |-------- (2) INVITE  --------->|
  *    |                                   |                               |
  *    |                                   |<------- (3) 200 OK -----------|
  *    |<--------- (4) 200 OK  ------------|                               |
  *    |                                   |                               |
  *    |---------- (5)  ACK    ----------->|                               |
  *    |                                   |--------  (6) ACK   ---------->|
  *    |                                   |                               |
  *    |---------- (7)  BYE    ----------->|                               |
  *    |                                   |--------  (8) BYE   ---------->|
  *    |                                   |                               |
  *    |                                   |<-------  (9) 200 OK ----------|
  *    |<--------- (10)200 OK -------------|                               |
  *    |                                   |                               |
  *
  *
  * committed state under proxy mode
  */
  @TargetApplication(value = ApplicationName.PROXY)
  @AssertionIds(
      ids = {"SipServlet:SPEC:Committed1"},
      desc = "To test the committed state of SipServletMessage under Proxy mode")
  public void testCommittedStateUnderProxy() throws Exception {

    String servletName = "CommittedStateUnderProxy";

    //(0) UA2 listens to incoming message
    SipCall callB = ua2.createSipCall();
    callB.listenForIncomingCall();
    SipCall callA = ua1.createSipCall();

    //(1) UA1 sends INVITE
    String target = "sip:" + ua2UserName + "@" + ua2Host + ":" + ua2Port;
    String viaNonProxyRoute = serverHost + ":" + serverPort + "/" + testProtocol;
    ArrayList headers = new ArrayList(1);
    headers.add(callA.getHeaderFactory().createRouteHeader(
        callA.getAddressFactory().createAddress(target)));
    initiateOutgoingCall(callA, null, servletName, headers, null);
    assertLastOperationSuccess("UA1 initiate call - " + callA.format(), callA);

    //(2) UA2 waits for incoming INVITE
    callB.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("UA1 initiate call - " + callB.format(), callB);

    //(3) UA2 sends 200/INVITE
    callB.sendIncomingCallResponse(Response.OK, null, -1);
    assertLastOperationSuccess("UA1 initiate call - " + callB.format(), callB);

    //(4) UA1 receives 200/INVITE
    do {
      callA.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("UA1 waits for 200/INVITE - " + callA.format(), callA);
    } while (callA.getReturnCode() != Response.OK);

    //(5) UA1 sends ACK
    callA.sendInviteOkAck();
    assertLastOperationSuccess("UA1 sends ACK - " + callA.format(), callA);

    //(6) UA2 waits for ACK
    callB.waitForAck(waitDuration);
    assertLastOperationSuccess("UA2 waits for ACK - " + callB.format(), callB);

    //(7) UA1 sends BYE
    callA.disconnect();
    assertLastOperationSuccess("UA1 sends BYE - " + callA.format(), callA);

    //(8) UA2 waits for BYE
    callB.waitForDisconnect(waitDuration);
    assertLastOperationSuccess("UA2 waits for BYE - " + callB.format(), callB);

    //(9) UA2 sends 200/BYE
    callB.respondToDisconnect();
    assertLastOperationSuccess("UA2 sends 200/BYE - " + callB.format(), callB);

    //(10) UA1 waits for 200/BYE
    Thread.sleep(waitDuration);
    SipResponse resp = callA.getLastReceivedResponse();
    assertNotNull("Expected 200/BYE should not be null", resp);
    assertEquals("Expected message should be 200/BYE",
        resp.getStatusCode(), Response.OK);
  }

}
