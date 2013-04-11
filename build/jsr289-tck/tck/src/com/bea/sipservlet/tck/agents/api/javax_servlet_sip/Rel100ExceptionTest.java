/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.  
 *  
 * Rel100ExceptionTest is used to test the APIs of
 * javax.servlet.sip.Rel100Exception.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;

import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.header.Header;
import javax.sip.header.SupportedHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class Rel100ExceptionTest extends TestBase {
    
  private static final Logger logger = Logger
      .getLogger(SipApplicationSessionTest.class);

  private static final String testName = "Rel100Exception";

  public Rel100ExceptionTest(String arg0) throws IOException {
    super(arg0);
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:Rel100Exception1" }, 
    desc = "Construct a new Rel100Exception with the specified error reason:"
      + "NO_REQ_SUPPORT,NOT_100rel,NOT_1XX,NOT_INVITE,NOT_SUPPORTED.")
  public void testRel100Exception001() throws ParseException,
      InvalidArgumentException {    
    doRel100ExceptionMessageTest("testRel100Exception001", false);
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:Rel100Exception2"}, 
      desc = "Return message phrase suitable for the reason integer code." 
        + "The message phrase should not be null.")
  public void testGetMessage001() throws ParseException,
      InvalidArgumentException {    
    doRel100ExceptionInviteTest(false);
  }
   
  @AssertionIds(ids = { "SipServlet:JAVADOC:Rel100Exception3",
                        "SipServlet:JAVADOC:Rel100Exception4"}, 
      desc = "Check reason:" 
        + "NO_REQ_SUPPORT (the UAC didn't indicate support for the reliable " 
        + "responses extension in the request).")
  public void testGetReason001() throws ParseException,
      InvalidArgumentException {    
    doRel100ExceptionInviteTest(false);    
  }
  
  
  /*     
   * 
   *   UAC                                 UAS
   *    |                                   |
   *    |----------  (1)MESSAGE ----------->|
   *    |                                   |
   *    |<-----------(2)200 ----------------|
   *    |                                   |
   *    |<---------  (3)INVITE  ------------|
   *    |                                   |
   *    |----------  (4)180/INVITE -------->|
   *    |                                   |
   *    |<---------  (5)PRACK --------------|
   *    |                                   |
   *  <Check receive PRACK>
   *    |                                   |  
   *    |                                   |
   *    
   *    
   */    
  @AssertionIds(ids = { "SipServlet:JAVADOC:Rel100Exception3", 
                        "SipServlet:JAVADOC:Rel100Exception4"},  
    desc = "Check reason:"
      + "NOT_100rel (SipServletResponse.createPrack() was invoked on a "
      + "provisional response that is not reliable).")
  public void testGetReason002() throws ParseException,
      InvalidArgumentException {
    clientEntryLog();
    int cseq = 1;
    SipCall a = ua1.createSipCall();
    // UA1 listens for incoming INVITE
    a.listenForIncomingCall();
    
    // (1) UA1 sends MESSAGE
    SipTransaction trans = sendMessage(ua1, cseq);
    if (trans == null) {
      fail("Fail to send MESSAGE out.");
    }

    // (2) UA1 receives 200 OK
    EventObject event = ua1.waitResponse(trans, waitDuration);
    if(event == null){
      logger.warn("*** 200 ok of MESSAGE is not received, but case will continue***");
    }

    // (3) UA1 receives INVITE
    a.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("a wait incoming call - " + a.format(), a);

    // (4) UA1 sends 180/INVITE
    boolean status = false;
    status = a.sendIncomingCallResponse(Response.RINGING, "180 for INVITE",
        waitDuration);
    assertLastOperationSuccess("a send 180 - " + a.format(), a);
    if (!status) {
      fail("Fail to send 180 response out.");
    }
    
    // (5) UA1 receives PRACK. Case failed if no PRACK is received.
    a.waitForIncomingCall(waitDuration);
    assertLastOperationFail("a wait incoming call - " + a.format(), a);
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:Rel100Exception3", 
                        "SipServlet:JAVADOC:Rel100Exception4"}, 
    desc = "Check reason:"
      + "NOT_1XX (SipServletResponse.sendReliably() was invoked on a final "
      + "or a 100 response).")
  public void testGetReason003() throws ParseException,
      InvalidArgumentException {    
    doRel100ExceptionInviteTest(true);    
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:Rel100Exception3" , 
                        "SipServlet:JAVADOC:Rel100Exception4"}, 
    desc = "Check reason:"
      + "NOT_INVITE (SipServletResponse.sendReliably() was invoked for "
      + "a response to a non-INVITE request).")
  public void testGetReason004() throws ParseException,
      InvalidArgumentException {    
    doRel100ExceptionMessageTest("testGetReason004", true);
  }  
    
  /*     
   * 
   *   UAC                             UAS
   *    |                               |
   *    |----------(1)INVITE ---------->|
   *    |                               |
   *    |<---------(2)200/INVITE--------|
   *    |                               |
   *    |----------(3)ACK-------------->|
   *    |                               |
   *    |----------(4)BYE-------------->|
   *    |                                       |
   *    
   */    
  private void doRel100ExceptionInviteTest(boolean isSupported100Rel)
      throws ParseException, InvalidArgumentException {
    clientEntryLog();
    SipCall a = ua1.createSipCall();

    // (1) UAC sends INVITE with "Supported:100rel" if isSupported100Rel is true.
    ArrayList<Header> additionalHeaders = null;
    if (isSupported100Rel) {
      additionalHeaders = new ArrayList<Header>(1);
      additionalHeaders.add(a.getHeaderFactory()
          .createSupportedHeader("100rel"));
    }

    boolean status = initiateOutgoingCall(a, null, null, additionalHeaders, null);
    assertTrue("Initiate outgoing call failed - " + a.format(), status);

    // (2) UAC receives and assert 200/INVITE response
    a.waitOutgoingCallResponse(waitDuration);
    while (a.getReturnCode() == Response.TRYING) {
      a.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - "
          + a.format(), a);
    }
    assertResponseReceived("Unexpected response received", Response.OK, a);

    // (3) UAC sends ACK
    a.sendInviteOkAck();
    // (4) UAC sends BYE
    a.disconnect();
  }
  
  
  /**     
   * 
   *   UAC                              UAS
   *    |                                |
   *    |----------(1) MESSAGE --------->|
   *    |                                |
   *    |<---------(2) 200 OK  ----------|
   *    |                                |
   */    
  private void doRel100ExceptionMessageTest(String methodName,
      boolean isSupported100Rel) throws ParseException,
      InvalidArgumentException {
    clientEntryLog();
    // Build the Request message
    Request req = assembleRequest("MESSAGE", testName, methodName,
        TestConstants.SERVER_MODE_UA, 1);
    if (isSupported100Rel) {
      SupportedHeader supportedHeader = ua1.getParent().getHeaderFactory()
          .createSupportedHeader("100rel");
      req.addHeader(supportedHeader);
    }
    // (1) UAC sends the MESSAGE Request message
    SipTransaction trans = ua1.sendRequestWithTransaction(req, true, null);
    assertNotNull(ua1.format(), trans);
    logger.debug("---UAC send MESSAGE req is:" + req + "---");

    // (2) UAC receives 200/MESSAGE response
    EventObject event = ua1.waitResponse(trans, waitDuration);
    assertNotNull(event);

    ResponseEvent responseEvent = filterEvent(ua1, trans, event);
    // Assert 200/MESSAGE response
    Response response = responseEvent.getResponse();
    if (response.getStatusCode() != Response.OK) {
      fail("The response is not 200OK,but " + response.getStatusCode());
    }
    logger.debug("---UAC receive 200/MESSAGE resp is:" + response + "---");
  }
}
