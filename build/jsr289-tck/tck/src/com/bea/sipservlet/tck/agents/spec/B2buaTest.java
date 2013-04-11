/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * B2buaTest is used to test  
 * mechanism for B2bua without B2buaHelper
 *
 */

package com.bea.sipservlet.tck.agents.spec;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;

import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@TargetApplication(ApplicationName.B2BUA)
public class B2buaTest extends TestBase {

  private static Logger logger = Logger.getLogger(B2buaTest.class);

  public B2buaTest(String arg0) throws IOException {
    super(arg0);
  }

  /**
   * For testB2bua1(), the call flow is:
   *
   * UA1                              SUT                              UA2
   *  |                                |                                |
   *  |-------- (1) INVITE ----------->|                                |
   *  |                                |                                |
   *  |                                |-------- (2) INVITE ----------->|
   *  |                                |                                |
   *  |                                |<------- (3) 404 OK ------------|
   *  |                                |                                |
   *  |<------- (4) 404 OK ------------|                                |
   *  |                                |                                |
   *  |-------- (5) ACK -------------->|                                |
   *  |                                |                                |
   *  |                                |-------- (6) ACK -------------->|
   *  |                                |                                |
   *
   *
   */
  @AssertionIds(
      ids = {"SipServlet:SPEC:B2bua1"},
      desc = "Application acts as B2BUA for an INVITE request. " +
          "The request is rejected with a 404 and " +
          "no subsequent request is sent.")
  public void testB2bua1(){
    clientEntryLog();

    SipCall callB = ua2.createSipCall();
    callB.listenForIncomingCall();

    // (1) UA1 make a outgoing call
    SipCall callA = ua1.createSipCall();
    List<Header> privateHeaders = new ArrayList<Header>(1);
    privateHeaders.add(getPrivateHeader(TestConstants.PRIVATE_URI, ua2URI));
    initiateOutgoingCall(callA, null, null, privateHeaders, null);
    assertLastOperationSuccess(
        "UA1 initiate call - " + callA.format(), callA);

    // (2) UA2 receives invite
    callB.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess(
        "UA2 wait for incoming call - " + callB.format(), callB);

    // (3) UA2 send 404/invite
    callB.sendIncomingCallResponse(Response.NOT_FOUND, null, -1);
    assertLastOperationSuccess("UA2 send 404 - " + callB.format(), callB);

    // (4) UA1 receives 404/invite
    do {
      callA.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess(
          "UA1 wait for 404/INVITE - " + callA.format(), callA);
    } while (callA.getReturnCode() != Response.NOT_FOUND);

  }
    
  /**
   * For testB2bua2(), the call flow is:
   *
   *  UA1                             SUT                             UA2
   *  |                                |                                |
   *  |-------- (1) INVITE ----------->|                                |
   *  |                                |                                |
   *  |                                |-------- (2) INVITE ----------->|
   *  |                                |                                |
   *  |                                |<------- (3) 200 OK ------------|
   *  |                                |                                |
   *  |<------- (4) 200 OK ------------|                                |
   *  |                                |                                |
   *  |-------- (5) ACK -------------->|                                |
   *  |                                |                                |
   *  |                                |-------- (6) ACK -------------->|
   *  |                                |                                |
   *  |                                |<------- (7) BYE ---------------|
   *  |                                |                                |
   *  |<------- (8) BYE ---------------|                                |
   *  |                                |                                |
   *  |-------- (9) 200 OK ----------->|                                |
   *  |                                |                                |
   *  |                                |-------- (10) 200 OK ---------->|
   *  |                                |                                |
   *
   *
   * @throws InterruptedException Thread.sleep
   */
  @AssertionIds(
      ids = {"SipServlet:SPEC:B2bua2"},
      desc = "Application acts as b2bua for INVITE. Dialog is " +
          "established and callee sends BYE.")
  public void testB2bua2(){
    clientEntryLog();

    SipCall callB = ua2.createSipCall();
    callB.listenForIncomingCall();

    // (1) UA1 make a outgoing call
    SipCall callA = ua1.createSipCall();
    String viaNonProxyRoute = serverHost + ":" + serverPort + "/" + testProtocol;
    List<Header> pricateHeaders = new ArrayList<Header>(1);
    pricateHeaders.add(getPrivateHeader(TestConstants.PRIVATE_URI, ua2URI));
    initiateOutgoingCall(callA, null, null, pricateHeaders, viaNonProxyRoute);
    assertLastOperationSuccess(
        "UA1 initiate call - " + callA.format(), callA);

    // (2) UA2 receives invite
    callB.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess(
        "UA2 wait for incoming call - " + callB.format(), callB);

    // (3) UA2 send 200/invite
    callB.sendIncomingCallResponse(Response.OK, null, -1);
    assertLastOperationSuccess("UA2 send 200 - " + callB.format(), callB);

    // (4) UA1 receives 200/invite
    do {
      callA.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess(
          "UA1 wait for 200/INVITE - " + callA.format(), callA);
    } while (callA.getReturnCode() != Response.OK);

    // (5) UA1 send Ack
    callA.sendInviteOkAck();
    assertLastOperationSuccess(
        "UA1 sending ACK - " + callA.format(), callA);

    // (6) UA2 receive Ack
    callB.waitForAck(waitDuration);
    assertLastOperationSuccess(
        "UA2 wait for ACK - " + callB.format(), callB);

    // (7) UA2 send BYE
    callB.disconnect();
    assertLastOperationSuccess("UA2 send BYE - " + callB.format(), callB);

    // (8) UA1 receive BYE
    callA.listenForDisconnect();
    assertLastOperationSuccess(
        "UA1 listen disc - " + callA.format(), callA);
    callA.waitForDisconnect(waitDuration);
    assertLastOperationSuccess(
        "UA1 wait for BYE - " + callA.format(), callA);

    // (9) UA1 send 200/BYE
    callA.respondToDisconnect();
    assertLastOperationSuccess(
        "UA1 respond to disc - " + callA.format(), callA);

    // (10) UA2 receive 200/BYE
    try {
      Thread.sleep(waitDuration);
    } catch (InterruptedException e) {
      logger.error(
          "*** InterruptedException when waiting for 200/BYE ***", e);
      throw new TckTestException(e);
    }
    Response okForBye = (Response)callB.getLastReceivedResponse().getMessage();
    assertNotNull("200/BYE", okForBye);
    assertEquals("UA2 wait for 200/BYE - ", Response.OK,
        okForBye.getStatusCode());
    assertEquals("Unexpected response", Request.BYE,
        ((CSeqHeader)okForBye.getHeader(CSeqHeader.NAME)).getMethod());
  }

  private Header getPrivateHeader(String headerName, String value){
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
    try {
      return header_factory.createHeader(headerName, value);
    } catch (ParseException e) {
      logger.error(
          "*** ParseException when creating private header ***", e);
      throw new TckTestException(e);
    }
  }
}
