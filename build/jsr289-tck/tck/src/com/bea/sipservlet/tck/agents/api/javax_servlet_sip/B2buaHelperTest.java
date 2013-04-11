/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * B2buaHelperTest is used to test the APIs of 
 * javax.servlet.sip.B2buaHelper
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;


public class B2buaHelperTest extends TestBase {
  private static Logger logger = Logger.getLogger(B2buaHelperTest.class);

  public B2buaHelperTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper1"},
      desc = "User agents can create a new request object belonging to a new SipSession.")
  public void testCreateRequest001(){
    assertSipMessage();
  }

  /**
   * The call flow is:
   * 
   *          UA1                           B2BUA                           UA2
   *           |                              |                              |
   *           |---------- (1)INVITE  ------->|                              |
   *           |<-------- (2) 100    ---------|                              |
   *           |                        <B2BUA logic>                        |                                
   *           |                              |---------- (3)INVITE  ------->|
   *           |                              |<-------- (4) 200OK  ---------|
   *           |                        <B2BUA logic>                        |                                
   *           |<-------- (5) 200OK    -------|                              |
   *           |--------- (6) ACK      ------>|                              |
   *           |                        <B2BUA logic>                        |                                
   *           |                              |--------- (7) ACK   --------->|
   *           |                              |                              |
   *           |---------  (8) BYE  --------->|                              |
   *           |                              |--------- (9) BYE   --------->|
   *           |                              |<-------- (10) 200OK    -------|
   *           |<-------- (11) 200OK   -------|                              |
   *           |                              |                              |
   *
   */
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper2",
          "SipServlet:JAVADOC:B2buaHelper3",
          "SipServlet:JAVADOC:B2buaHelper4",
          "SipServlet:JAVADOC:B2buaHelper5",
          "SipServlet:JAVADOC:B2buaHelper6",
          "SipServlet:JAVADOC:B2buaHelper7"},
      desc = "User agents can create a new request, get linked SipSession or "
        + "SipServletRequest, and check pending messages in the SipSession.")
  public void testCreateRequest002() throws InterruptedException {
    clientEntryLog();
    ArrayList<Header> privateHeaders = getTckTestPrivateHeaders();
    
    //initialize SipCall
    SipCall callerA = ua1.createSipCall();
    SipCall callerB = ua2.createSipCall();
    callerB.listenForIncomingCall();    
    Thread.sleep(10);

    // (1) A send INVITE(with UA2 URI)
    boolean status = callerA.initiateOutgoingCall(ua1URI, serverURI, null, 
        privateHeaders, null, null);
    assertTrue("Initiate outgoing call failed - " + callerA.format(), status);

    // (3) B receive INVITE
    callerB.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("callerB wait incoming call - " + callerB.format(), callerB);

    // (4) B send 200/INVITE
    callerB.sendIncomingCallResponse(Response.OK, null, -1, privateHeaders, null, null);
    assertLastOperationSuccess("callerB send OK - " + callerB.format(), callerB);

    // (5) A receive 200/INVITE
    do {
      callerA.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("callerA wait response - " + callerA.format(), callerA);
    } while (callerA.getReturnCode() != Response.OK);

    // (6) A send ACK
    callerA.sendInviteOkAck(privateHeaders, null, null);
    assertLastOperationSuccess("Fail to send ACK - " + callerA.format(), callerA);
    
    // (7) B receive ACK
    callerB.waitForAck(waitDuration);
    assertLastOperationSuccess("Fail to receive ACK - " + callerB.format(), callerB);

    Thread.sleep(100);

    // B ready to receive BYE
    callerB.listenForDisconnect();
    assertLastOperationSuccess("callerB listen disconnect - " + callerB.format(), callerB);

    // (8) A send BYE
    callerA.disconnect(privateHeaders, null, null);
    assertLastOperationSuccess("callerA disconnect - " + callerA.format(), callerA);

    // (9) B receive BYE
    callerB.waitForDisconnect(waitDuration);
    assertLastOperationSuccess("callerB wait disconnect - " + callerB.format(), callerB);

    // (10) B send 200/BYE
    callerB.respondToDisconnect(Response.OK, null, privateHeaders, null, null);
    assertLastOperationSuccess("callerB respond to disconnect - " + callerB.format(), callerB);

    // (11) A receive 200/BYE    
    Thread.sleep(500);
    Response okForBye = (Response) callerA.getLastReceivedResponse().getMessage();
    assertNotNull("Default response reason not sent", okForBye);
    assertEquals("Unexpected default reason", Response.OK, okForBye.getStatusCode());
    assertEquals("Unexpected default reason", Request.BYE, ((CSeqHeader) okForBye
        .getHeader(CSeqHeader.NAME)).getMethod());
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper1"},
      desc = "IllegalArgumentException should be thrown if the headerMap contains " +
          "system header other than From, To, Contact or Route.")
  public void testCreateRequest101() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper1"},
      desc = "NullPointerException should be thrown if the original request is null")
  public void testCreateRequest102() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper1"},
      desc = "TooManyHopsException should be thrown if the original request's" +
          "Max-Forwards header value is 0")
  public void testCreateRequest103() throws InvalidArgumentException {
    Map map = new HashMap();
    StackTraceElement stack = getBasePackageStack(new Exception().getStackTrace());
		String	servletName = getInterfaceName(stack.getClassName());
		String	methodName = stack.getMethodName();
    map.put(TestConstants.SERVLET_HEADER, servletName);
    map.put(TestConstants.METHOD_HEADER, methodName);
    Request req = assembleEmptyRequest("MESSAGE", 1, map, TestConstants.SERVER_MODE_UA);
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
    //replace the current Max-Forwards Header    
    req.setHeader(header_factory.createMaxForwardsHeader(0));
    SipTransaction trans = ua1.sendRequestWithTransaction(req,
        true, null);
    assertNotNull(ua1.format(), trans);
    EventObject event = ua1.waitResponse(trans, waitDuration);

    assertNotNull(event);

    if (event instanceof ResponseEvent) {
      Response resp = ((ResponseEvent)event).getResponse();
      assertEquals("the reason got from UAS:" + resp.getReasonPhrase()+ ". ",
          Response.OK, resp.getStatusCode());
    } else {
      fail("did not recieve response from server side");
    }
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper1"},
      desc = "test IllegalArgumentException of createRequest(session," +
          "origRequest,headerMap)")
  public void testCreateRequest104() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper1"},
      desc = "test NullPointerException of createRequest(session," +
          "origRequest,headerMap)")
  public void testCreateRequest105() {
    assertSipMessage();
  }

  /**
   *   |                          |
   *   |                          |
   *   |--------(1) MESSAGE------>|
   *   |                          |
   *   |                          |
   *   |<-------(2) 200 OK -------|
   *   |                          |
   *   |                          |
   *   |<-------(3) MESSAGE-------|
   *   |                          |
   *   |                          |
   *   |--------(4) 200 OK ------>|
   *
   */
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper4"},
      desc = "test IllegalStateException for createResponseToOriginalRequest")
  public void testCreateResponseToOriginalRequest101() {
    // the case sometimes can't receive first 200 ok, so assertSipMessageBiWay
    // is not used
    clientEntryLog();
    ua1.listenRequestMessage();
    SipTransaction trans =
				sendMessage(ua1, "B2buaHelper", "testCreateResponseToOriginalRequest101",
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
    EventObject event = ua1.waitResponse(trans, waitDuration);
    if(event == null){
      logger.warn("*** 200 ok of MESSAGE is not received, but case will continue***");
    }
    logger.debug("the responseEvent=null?" + event);    


    Request req = waitIncomingMessage(ua1, waitDuration);
  	if (req == null) {
	    	fail("Did not receive MESSGE from server side.");
	  }
		// send 200 OK
    boolean succ = sendResponseForMessage(ua1, req, Response.OK);
    // even if sending response is failed, the case is also considered passed,
    // and just log the information here
    if(!succ){
      logger.warn("*** failed to send the 200 ok back to UAS ***");
    }

  }

  /**
   *   ua1                       UAS
   *   |                          |
   *   |--------(1) MESSAGE------>|
   *   |                          |
   *   |<-------(2) 200 OK -------|
   *   |                          |--.
   *   |                          |  |invalide the session
   *   |                          |  |
   *   |                          |<-
   *   |                          |
   *   |                          |
   *   |<-------(3) MESSAGE-------|
   *   |                          |
   *   |                          |
   *   |--------(4) 200 OK ------>|
   *
   */
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper4"},
      desc = "test IllegalArgumentException for createResponseToOriginalRequest")
  public void testCreateResponseToOriginalRequest102() {
    
    clientEntryLog();
    ua1.listenRequestMessage();
    SipTransaction trans =
        sendMessage(ua1, "B2buaHelper", "testCreateResponseToOriginalRequest102",
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
    logger.debug("the responseEvent=null?" + event);


    //(3)
    Request req1 = waitIncomingMessage(ua1, waitDuration);
    //(4)
    sendResponseForMessage(ua1, req1, Response.OK);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper5"},
      desc = "test IllegalArgumentException for getLinkedSession")
  public void testGetLinkedSession101(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper7"},
      desc = "test IllegalArgumentException for getLinkedSession")
  public void testGetPendingMessages101(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper8",
          "SipServlet:JAVADOC:B2buaHelper9"},
      desc = "User agents can link and unlink the specified sessions, such that there "
        + "is a 1-1 mapping between them.")
  public void testLinkUnlinkSipSessions001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper8"},
      desc = "test IllegalArgumentException for linkSipSessions"
  )
  public void testLinkSipSessions101(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper8"},
      desc = "test NullPointerException for linkSipSessions"
  )
  public void testLinkSipSessions102(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper9"},
      desc = "test IllegalArgumentException for unlinkSipSessions"
  )
  public void testUnlinkSipSessions101(){
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper10"},
      desc = "test Creates a new CANCEL request to cancel" +
      		" the initial request sent on the other call leg"
  )
  public void testCreateCancel101(){
    assertSipMessage();
  }
  
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:B2buaHelper10"},
      desc = "test Creates a new CANCEL request to cancel" +
      		" the initial request sent on the other call leg"
  )
  public void testCreateCancel001(){
    clientEntryLog();
    try {
      SipCall call1 = ua1.createSipCall();
      SipCall call2 = ua2.createSipCall();

      String viaNonProxyRoute = serverHost + ":" + serverPort + "/"
          + testProtocol;

      initiateOutgoingCall(call1, null, null, getTckTestPrivateHeaders(), viaNonProxyRoute);
      
      call2.listenForIncomingCall();
      call2.waitForIncomingCall(waitDuration);
      assertLastOperationSuccess("b wait incoming call - " + call2.format(),
          call2);
      call2.sendIncomingCallResponse(180, "OK", 0, null,
          null, null);
      assertLastOperationSuccess("send 180 - " + call2.format(), call2);
      do {
        call1.waitOutgoingCallResponse(waitDuration);
        assertLastOperationSuccess("wait response - " + call1.format(), call1);
      } while (call1.getReturnCode() != Response.RINGING);
      call1.sendCancel();
      assertLastOperationSuccess("Failure sending Cancel - " + call1.format(),
          call1);
      assertTrue(call2.waitForCancel(waitDuration));
      assertTrue(call2.sendResponseToLastReceivedRequest(200, "OK", 0));
      assertLastOperationSuccess("Failure sending Cancel - " + call2.format(),
          call2);
      assertTrue(call1.waitOutgoingCallResponse(waitDuration));
      
      call1.disconnect();
      call2.waitForRequest(Request.BYE, waitDuration);
      call2.sendResponseToLastReceivedRequest(200, "OK", 0);
      call1.dispose();
      call2.dispose();
    } catch (Exception e) {
      fail("found exception" + e);
    }
  }  
  
  
  private ArrayList<Header> getTckTestPrivateHeaders(){
    StackTraceElement stack = getBasePackageStack(new Exception().getStackTrace());
    String localServletName = getInterfaceName(stack.getClassName());
    String localMethodName = stack.getMethodName();
    
    HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
    Header appHeader;
    Header servletHeader;
    Header methodHeader;

    try {
    	appHeader = 
    		headerFactory.createHeader(TestConstants.APP_HEADER, TestConstants.APP_APITEST);
      servletHeader = 
        headerFactory.createHeader(TestConstants.SERVLET_HEADER, localServletName);
      methodHeader = 
        headerFactory.createHeader(TestConstants.METHOD_HEADER, localMethodName);
    } catch (ParseException e) {
      logger.error("*** ParseException when retrieving private headers ***", e);
      throw new TckTestException(e);  
    }
    
    ArrayList<Header> additionalHeaderList = new ArrayList<Header>(1);
    additionalHeaderList.add(appHeader);
    additionalHeaderList.add(servletHeader);
    additionalHeaderList.add(methodHeader);
    additionalHeaderList.add(getUa2UriHeader());

    return additionalHeaderList;
  }
  
  private Header getUa2UriHeader(){
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
    try {
      return header_factory.createHeader(TestConstants.PRIVATE_URI, ua2URI);
    } catch (ParseException e) {
      logger.error("*** ParseException when creating private header ***", e);
      throw new TckTestException(e);
    }    
  }

}
