/**
 *(c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipSessionListenerTest is used to test the APIs of
 * javax.servlet.sip.SipSessionListener and javax.servlet.sip.SipSessionEvent
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipTransaction;

import javax.sip.RequestEvent;
import javax.sip.header.Header;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.text.ParseException;
import java.util.EventObject;

public class SipSessionListenerTest extends TestBase {
  private static Logger logger = Logger.getLogger(SipSessionListenerTest.class);

  public SipSessionListenerTest(String arg0) throws IOException {
		super(arg0);
	}

  @AssertionIds(
      ids={"SipServlet:JAVADOC:SipSessionListener1",           
           "SipServlet:JAVADOC:SipSessionEvent2"
          },
      desc="Test the notification fired when Sip session was created, and test" +
          "SipSessionEvent APIs. Please NOTE if the " +
          "container implements the SipApplicationSessionListener in multi-thread" +
          " style, this case might fail because it is hard to decide how long to" +
          " wait the sessionCreated() method to be called back. So this case" +
          " is optional."
  )
  public void testSessionCreated001(){
    assertSipMessage();    
  }

  /**
   *    UAC                UAS                 UAS'(Session Listener)
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
   *
   *
   */
  @AssertionIds(
      ids="SipServlet:JAVADOC:SipSessionListener2",
      desc="Test the notification fired when Sip session was destroyed"
  )
  public void testSessionDestroyed001(){
    // send Message and receive 200 OK
    boolean listen = ua1.listenRequestMessage();
    logger.info("=== set listen request message:" + listen + "===");
    clientEntryLog();
    SipTransaction trans =
        sendMessage(ua1, null, null,
            "MESSAGE", 1, null);

    if (trans == null) {
      fail("Fail to send MESSAGE out.");
    }
    // (2) receive response
    //NOTE: for some unknown bug of Jain-SIP stack, when the cases execution speed
    // is too fast and if some of them are failed those cases cause either client side
    // or server side resend sip message, the response might be lost by the
    // JAIN-SIP stack, and consequently the SipStack.processResponse() of SipUnit
    // will not got the response. So we will not assert the response here.
    // the root cause of this phenomenon probably is that JAIN-SIP underlying
    // threads scheduling has some problem.
    EventObject event = ua1.waitResponse(trans, waitDuration);
    if(event == null){
      logger.warn("*** 200 ok of MESSAGE is not received, but case will continue***");
    }
    recvUasMessageAndCheck();
  }

  /**
   *    UAC                UAS                    UAS'(Session Listener)
   *     |     INVITE       |                         |
   *     |----------------->|                         |
   *     |                  |                         |
   *     |     200 OK       |                         |
   *     |<-----------------|                         |
   *     |     ACK          |                         |
   *     |----------------->|                         |
   *     |                  |                         |
   *     |     BYE          |                         |
   *     |----------------->|                         |
   *     |     200 OK       |                         |
   *     |<-----------------|                         |
   *     |                  |--.                      |
   *     |                  |  | session moves to     |
   *     |                  |  | ready-to-invalidate  |
   *     |                  |<-                       |
   *     |                  | the notification        |
   *     |                  |      fired              |
   *     |                  |------------------------>|sessionReadyToInvalidate()
   *     |                                            |
   *     |                 MESSAGE                    |
   *     |<-------------------------------------------|
   *     |                                            |
   *     |                 200 OK                     |
   *     |------------------------------------------->|
   *
   *
   */
  @AssertionIds(
      ids="SipServlet:JAVADOC:SipSessionListener3",
      desc="Test the notification fired when Sip session is" +
          " in the ready-to-invalidate state"
  )
  public void testSessionReadyToInvalidate001(){
    boolean listen = ua1.listenRequestMessage();
    logger.info("=== set listening for the incoming request:" + listen + "===");
    //(1) send INVITE
    SipCall call = ua1.createSipCall();
    initiateOutgoingCall(call);
    //(2) Receive and assert 200 response
    call.waitOutgoingCallResponse(waitDuration);
    while (call.getReturnCode() == Response.TRYING) {
      call.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - "
          + call.format(), call);
    }
    assertResponseReceived("Unexpected response received", Response.OK, call);
    // (3) send ACK
    call.sendInviteOkAck();
    // (4) send BYE
    call.disconnect();
    
    recvUasMessageAndCheck();
  }

  private void recvUasMessageAndCheck(){
    RequestEvent event = ua1.waitRequest(waitDuration);
    logger.debug("has got event:" + event.getRequest().toString());
    assertNotNull(event);
    logger.debug("has got event:" + event.getRequest().toString());
    //send 200 ok firstly and check the header
    try{
     sendResp(event);
    } catch (ParseException e) {
     // if send failure continue the following assertion
     logger.error("*** can't create response from request! ***",e);
    }
    Request req = event.getRequest();
    assertEquals("MESSAGE" ,req.getMethod());
    Header result = req.getHeader("Test-Result");
    String expected = "ok";
    String headerStr = result.toString().toLowerCase();
    assertEquals(expected,
       headerStr.substring(headerStr.indexOf(":") + 1).trim());
  }

  private void sendResp(RequestEvent request)throws ParseException{
    MessageFactory msgFactory = ua1.getParent().getMessageFactory();
    Response resp = null;

    resp = msgFactory.createResponse(Response.OK, request.getRequest());
    SipTransaction trans = ua1.sendReply(request,resp);
    logger.info("=== sending out 200 response ===");
    //whether or not sending successfully doesn't affect the test case
    logger.info("=== send response "
        + (trans!=null ? "successfully":"failed") + " ===");

  }
}
