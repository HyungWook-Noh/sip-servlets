/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * UasTest is used to test the specification of  UAS
 */
package com.bea.sipservlet.tck.agents.spec;

import gov.nist.javax.sip.header.SIPHeaderNames;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.sip.header.ContentTypeHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipMessage;
import org.cafesip.sipunit.SipPhone;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

@TargetApplication(value = ApplicationName.UAS)
public class UasTest extends TestBase {

  public UasTest(String arg0) throws IOException {
    super(arg0);
  }

  /**
    *  check uas-passive function
    * 
    *                      UAC                           UAS
    *                       |                                         |
    *                       |------ (1) INVITE ------>|
    *                       |                                         |                                             
    *                       |<-----(2) 200 OK --------|
    *                       |                                         |
    *                       |------ (3) ACK --------->|
    *                       |                                         |
    *                       |------ (4) BYE ---------->|
    *                       |                                         |
    *                       |<----- (5) 200 OK -------|
    *                       |                                         |
   * @throws InterruptedException 
    */
  @AssertionIds(ids = { "SipServlet:SPEC:UasPassive1" }, desc = "Non-200 "
      + "final response should be received if the call flow makes an error")
  public void testUasPassive() throws InterruptedException {
    clientEntryLog();
    String testName = "UasPassive";
    SipCall a = ua1.createSipCall();
    boolean status_ok = initiateOutgoingCall(a, null, testName, null, null);
    assertTrue("Initiate outgoing call failed - " + a.format(), status_ok);

    a.waitOutgoingCallResponse(waitDuration); // get next response
    assertLastOperationSuccess("Wait response error - " + a.format(), a);

    while (a.getReturnCode() == Response.TRYING) {
      a.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - " +
          a.format(), a);
    }
    assertResponseReceived("Unexpected response received", Response.OK, a);

    a.sendInviteOkAck();
    assertLastOperationSuccess("Failure sending ACK - " + a.format(), a);

    Thread.sleep(waitDuration / 5);

    a.disconnect();
    assertLastOperationSuccess("a disc - " + a.format(), a);
    while (a.getReturnCode() != SipPhone.TIMEOUT_OCCURRED) {
      a.waitOutgoingCallResponse(waitDuration);
    }
    assertResponseReceived("Did not receive 200 OK on BYE", Response.OK,
        Request.BYE, 2, a);
  }

  /**
    *  check uas-active function
    * 
    *                      UAC                           UAS
    *                       |                                         |
    *                       |-------(1) INVITE------->|
    *                       |                                         |                                             
    *                       |<-----(2) 200 OK --------|
    *                       |                                         |
    *                       |------ (3) ACK --------->|
    *                       |<---- (4) INFO --------- |
    *                       |-------(5)200 OK ------->|
    *                       |                                         |
    *                       |<------(6) BYE ---------|
    *                       |                                         |
    *                       |------- (7) 200 OK ------>|
    *                       |                                         |
    */
  @AssertionIds(ids = { "SipServlet:SPEC:UasActive1" }, desc = "Non-200 "
      + "final response should be received if the call flow makes an error")
  public void testUasActive() {
    clientEntryLog();
    String testName = "UasActive";
    SipCall a = ua1.createSipCall();
    boolean status_ok = initiateOutgoingCall(a, null, testName, null, null);
    assertTrue("Initiate outgoing call failed - " + a.format(), status_ok);

    a.waitOutgoingCallResponse(waitDuration); // get next response
    assertLastOperationSuccess("Wait response error - " + a.format(), a);

    while (a.getReturnCode() == Response.TRYING) {
      a.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - " +
          a.format(), a);
    }
    while (a.getReturnCode() == Response.RINGING) {
      a.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - " +
          a.format(), a);
    }

    assertResponseReceived("Unexpected response received", Response.OK, a);
    verifyMessage(a.getLastReceivedResponse());

    a.sendInviteOkAck();
    assertLastOperationSuccess("Failure sending ACK - " + a.format(), a);

    a.listenForRequest();

    a.waitForRequest(Request.INFO, waitDuration);
    assertLastOperationSuccess("Failure receiving INFO - " + a.format(), a);
    verifyMessage(a.getLastReceivedRequest());
    // a.respondToRequest(Response.OK, null);
    a.sendResponseToLastReceivedRequest(200, Request.INFO, waitDuration);
    assertLastOperationSuccess(
        "Failure sending 200 OK on INFO - " + a.format(), a);

    a.waitForDisconnect(waitDuration);
    assertLastOperationSuccess("Failure receiving BYE - " + a.format(), a);
    verifyMessage(a.getLastReceivedRequest());
    a.respondToDisconnect();
    assertLastOperationSuccess("Failure sending 200 OK on BYE - " + a.format(),
        a);
  }

  /**
    *  check uas-cancel function
    * 
    *                      UAC                           UAS
    *                       |                                         |
    *                       |-------(1) INVITE ----->|
    *                       |                                         |
    *                       |                                         | app waits
    *                       |                                         |                                             
    *                       |-------(2) CANCEL---->|
    *                       |                                         |
    *                       |<-----(3)200 CANCEL---|
    *                       |<-----(4)487 INVITE----|
    *                       |--------(5) ACK --------> |
    *                       |                                         |
   * @throws InterruptedException 
    */
  @AssertionIds(ids = { "SipServlet:SPEC:UasCancel1" }, desc = "Non-200"
      + " final response should be received if the call flow makes an error")
  public void testUasCancel() throws InterruptedException {
    clientEntryLog();
    String testName = "UasCancel";
    SipCall a = ua1.createSipCall();
    boolean status_ok = initiateOutgoingCall(a, null, testName, null, null);
    assertTrue("Initiate outgoing call failed - " + a.format(), status_ok);

    a.waitOutgoingCallResponse(waitDuration);
    assertLastOperationSuccess("Wait response error - " + a.format(), a);

    while (a.getReturnCode() == Response.TRYING) {
      a.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - " +
          a.format(), a);
    }
    assertResponseReceived("Did not receive 180 Ringing", Response.RINGING, a);

    Thread.sleep(waitDuration / 5);

    a.sendCancel();
    assertLastOperationSuccess("Failed to send CANCEL - " + a.format(), a);

    // create a more generic method or waitCancelResponse
    while (a.getReturnCode() != SipPhone.TIMEOUT_OCCURRED) {
      a.waitOutgoingCallResponse(waitDuration);
    }
    assertResponseReceived("Unexpected response received", Response.OK,
        Request.CANCEL, 1, a);
    assertResponseReceived("Unexpected response received",
        Response.REQUEST_TERMINATED, Request.INVITE, 1, a);

    // The NIST stack seems to send the ACK by itself.
  }

  private void verifyMessage(SipMessage msg) {
    assertHeaderContains("Response did not contain expected header value", msg,
        "Foo", "bar");
    assertHeaderContains("Response did not contain expected header value", msg,
        "Foo", "baz");
    assertHeaderContains("Response did not contain expected header value", msg,
        "User-Agent", "UasActive");

    assertEquals("Response did not contain expected header value",
        ((ContentTypeHeader) msg.getMessage().getHeader(
            SIPHeaderNames.CONTENT_TYPE)).getContentType(), "text");
    assertEquals("Response did not contain expected header value",
        ((ContentTypeHeader) msg.getMessage().getHeader(
            SIPHeaderNames.CONTENT_TYPE)).getContentSubType(), "plain");
    assertHeaderContains("Response did not contain expected header value", msg,
        SIPHeaderNames.CONTENT_LENGTH, String.valueOf(BA_CONTENT.length));
    assertBodyContains("Response did not contain expected body", msg, CONTENT);
  }

  private static final String CONTENT = "Active UAS";

  private static final byte[] BA_CONTENT = getBytes_UTF8(CONTENT);

  static final byte[] getBytes_UTF8(String s) {
    try {
      return s.getBytes("UTF-8");
    } catch (UnsupportedEncodingException _) {
      // can't happen - all platforms MUST support UTF-8
      return null;
    }
  }

}
