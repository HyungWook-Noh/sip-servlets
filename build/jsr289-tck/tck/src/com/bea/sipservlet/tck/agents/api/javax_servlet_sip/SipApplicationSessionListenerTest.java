/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.  
 *  
 * SipApplicationSessionListenerTest is used to test the APIs of 
 * javax.servlet.sip.SipApplicationSessionListener.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.text.ParseException;
import java.util.EventObject;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;


public class SipApplicationSessionListenerTest extends TestBase {
    
  private static final Logger logger = Logger
      .getLogger(SipApplicationSessionListenerTest.class);

  private static final String testName = "SipApplicationSessionListener";

  public SipApplicationSessionListenerTest(String arg0) throws IOException {
    super(arg0);
  }
  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSessionListener1",
                        "SipServlet:JAVADOC:SipApplicationSessionEvent2"}, 
      desc = "Notification that a session was created. Please NOTE if the " +
          "container implements the SipApplicationSessionListener in multi-thread" +
          " style, this case might fail because it is hard to decide how long to" +
          "wait the sessionCreated() method to be called back. So this case" +
          " is optional.")
  public void testSessionCreated001() {
    assertSipMessage();
  }
  /**
   *    UAC                UAS                 Session Listener
   *     |     MESSAGE      |                    |
   *     |----------------->|                    |
   *     |                  |                    |
   *     |     200 OK       |                    |
   *     |<-----------------|                    |
   *     |                  |--.                 |
   *     |                  |  |  session        |
   *     |                  |  | invalidate      |
   *     |                  |<-                  |
   *     |                  | Session Destroyed  |
   *     |                  |      fired         |
   *     |                  |------------------->|sessionDestroyed()
   *     |                                       |
   *     |                 MESSAGE               |
   *     |<--------------------------------------|
   *     |                                       |
   *     |                 200 OK                |
   *     |-------------------------------------->|
   * this case is optional because we don't know the how long we need to wait
   * for the sessionDestroyed() being invoked since the appsession is invalidated.
   */
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSessionListener2" }, 
      desc = "Notification that a session was invalidated.")
  public void testSessionDestroyed001() {
    clientEntryLog();
    ua1.listenRequestMessage();
    // (1) UAC Build and Send MESSAGE Request message
    Request req = assembleRequest(Request.MESSAGE, testName,
        "testSessionDestroyed001" , TestConstants.SERVER_MODE_UA, 1);
    SipTransaction trans = ua1.sendRequestWithTransaction(req, true, null);
    assertNotNull(ua1.format(), trans);
    logger.debug("--- UAC send the MESSAGE req is:" + req + "---");

    // (2) UAC Receive and Assert 200 response
    EventObject event = ua1.waitResponse(trans, waitDuration);
    assertNotNull(event);

    if (event instanceof ResponseEvent) {
      ResponseEvent responseEvent = (ResponseEvent) event;
      Response response = responseEvent.getResponse();
      logger.debug("--- UAC receive the MESSAGE response is:" + response + "---");
      assertEquals(Response.OK,response.getStatusCode());
    } else {
      fail("did not receive any response from server side");
    }

    // (3) UAC Receive and Assert the result MESSAGE
    RequestEvent testEvent = ua1.waitRequest(waitDuration);
    assertNotNull(testEvent);
    Request messageReq = testEvent.getRequest();
    if (testEvent != null ) {
      logger.debug("--- UAC receive the MESSAGE req is:" + req + "---");
      assertEquals(Request.MESSAGE, messageReq.getMethod());
    }

    // (4) UAC send 200/MESSAGE back.
    sendResponseForMessage(ua1,messageReq,Response.OK);
  }  
  
  /**     
   * 
   *   UAC                                 UAS
   *    |                                   |
   *    |----------  (1)MESSAGE ----------->|
   *    |                                   |
   *    |<-----------(2)200 ----------------|
   *    |                                   |
   *    |<---------  (3)MESSAGE ------------|
   *    |                                   |
   *    |------------(4)200---------------->|
   *    |
   */  
  @AssertionIds(ids={"SipServlet:JAVADOC:SipApplicationSessionListener3"},
      desc="Notification that an application session has expired.")
  public void testSessionExpired001() {
    clientEntryLog();    
    ua1.listenRequestMessage();  
    // (1) UAC Build and Send MESSAGE Request message
    Request req = assembleRequest(Request.MESSAGE, testName,
        "testSessionExpired001" , TestConstants.SERVER_MODE_UA, 1);
    SipTransaction trans = ua1.sendRequestWithTransaction(req, true, null);
    assertNotNull(ua1.format(), trans);
    logger.debug("--- UAC send the MESSAGE req is:" + req + "---");

    // (2) UAC Receive and Assert 200 response
    EventObject event = ua1.waitResponse(trans, waitDuration);
    assertNotNull(event);

    if (event instanceof ResponseEvent) {
      ResponseEvent responseEvent = (ResponseEvent) event;
      Response response = responseEvent.getResponse();
      logger.debug("--- UAC receive the MESSAGE response is:" + response + "---");
      assertEquals(Response.OK,response.getStatusCode());      
    } else {
      fail("did not receive any response from server side");
    }
    
    // (3) UAC Receive and Assert the result MESSAGE      
    RequestEvent testEvent = ua1.waitRequest(66000);
    assertNotNull(testEvent);
    Request messageReq = testEvent.getRequest();
    if (testEvent != null ) {    
      logger.debug("--- UAC receive the MESSAGE req is:" + req + "---");
      assertEquals(Request.MESSAGE, messageReq.getMethod());
    }
    
    // (4) UAC send 200/MESSAGE back.
    sendResponseForMessage(ua1,messageReq,Response.OK);
  }

  
  /**
   * The call flow is:
   * 
   * UAC                           UAS
   * |                              |
   * |----------(1)INVITE  -------->|
   * |                              |
   * |<-------- (2)200/INVITE-------|
   * |                              |
   * |--------- (3)ACK  ----------->|
   * |                              |
   * |--------- (4)BYE   ---------->|
   * |                              |
   * |<-------- (5)200/BYE----------|
   * |                              |
   * |                        <Check Point>
   * |                              |
   * |<-------- (6)MESSAGE----------|
   * |                              |
   * |--------- (7)200/MESSAGE----->|
   * |                              |
   * @throws ParseException 
   * @throws InvalidArgumentException 
   * @throws SipException 
   */
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSessionListener2" }, 
      desc = "Notification that a SipApplicationSession is in the"
      + " ready-to-invalidate state.")
  public void testSessionReadyToInvalidate001() throws ParseException,
      SipException, InvalidArgumentException {
    clientEntryLog();
    SipCall a = ua1.createSipCall();  
    ua1.listenRequestMessage();
    // (1) UAC sends INVITE
    initiateOutgoingCall(a, null, null, null, null);
    assertLastOperationSuccess("a initiate call - " + a.format(), a);
    
    // (2) UAC receives 200/INVITE
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
    
    // (6) UAC receives and assert MESSAGE    
    RequestEvent eventUA1 = ua1.waitRequest(waitDuration);
    assertNotNull(eventUA1);
    
    Request messageReq = eventUA1.getRequest();
    if (!Request.MESSAGE.equals(messageReq.getMethod())) {
      fail("The request UAC received is not MESSAGE, but"
          + messageReq.getMethod());
    }
    logger.debug("---UA2 receive the MESSAGE req is:" + messageReq + "---");
    
    //  (7)UAC sends back 200/MESSAGE    
    ServerTransaction serverTransUAC = ua1.getParent().getSipProvider()
        .getNewServerTransaction(messageReq);
    Response msg200Resp = createResponse(messageReq, ua1, 200, ua1
        .generateNewTag());
    logger.debug("--- UAC send 200 Response is:" + msg200Resp.toString()
        + " ---");
    serverTransUAC.sendResponse(msg200Resp);

  }
  
}
