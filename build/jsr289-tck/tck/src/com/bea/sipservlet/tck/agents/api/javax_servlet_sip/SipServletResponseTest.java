/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.
 *
 * This class is used to test the APIs of the interface
 * javax.servlet.sip.SipServletResponse.
 *
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
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

public class SipServletResponseTest extends TestBase {

	private static Logger logger = Logger
					.getLogger(SipServletResponseTest.class);
	
  private static final String NEXT_MESSAGE = "Need Message";


	public SipServletResponseTest(String arg0) throws IOException,
					UnknownHostException {
		super(arg0);
	}

	/**
	 * The call flow is:
	 * <p/>
	 * UAS                           UA1
	 * |<--------- (1)MESSAGE --------|
	 * |--------- (2) 200    -------->|
	 * |                              |
	 * |---------- (3)INVITE  ------->|
	 * |                              |
	 * |<-------- (4) 200OK    -------|
	 * |--------- (5) ACK      ------>|
	 * |                              |
	 * |                              |
	 * |<--------  (6) BYE  ----------|
	 * |                              |
	 * |--------- (7) 200OK --------->|
	 * |                              |
	 */
	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse1"},
					desc = "Create Ack")
	public void testCreateAck001() {
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
		SipCall call = ua1.createSipCall();
		assertTrue(call.listenForIncomingCall());
		call.waitForIncomingCall(waitDuration);
		call.sendIncomingCallResponse(200, "OK", 3600);
		assertTrue(call.waitForAck(waitDuration));
		call.disconnect();
		call.dispose();
	}

	 @AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse1"},
       desc = "test Create Ack throw IllegalStateException")
  public void testCreateAck101() {
    assertSipMessage();
  }

	/**
	 * The call flow is:
	 * <p/>
	 * UAS                           UA1
	 * |<--------- (1)MESSAGE --------|
	 * |--------- (2) 200    -------->|
	 * |                              |
	 * |---------- (3)INVITE  ------->|
	 * |                              |
	 * |<-------- (4) 183Rel   -------|
	 * |--------- (5) PRACK    ------>|
	 * |                              |
	 * |<-------- (6) 200OK ----------|
   * |<---- (7) 200OK for invite----|
   * |--------- (8) ACK   --------->| 
	 * |<--------  (9) BYE  ----------|
	 * |                              |
	 * |--------- (10) 200OK --------->|
	 * |                              |
	 */
	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse2"},
					desc = "Create Prack")
	public void testCreatePrack001() throws InterruptedException{
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
		SipCall call = ua1.createSipCall();
		assertTrue(call.listenForIncomingCall());
		call.waitForIncomingCall(waitDuration);
		call.sendIncomingCallReliableResponse(183, "rel", 0);
		assertTrue(call.waitForPrack(waitDuration));
		assertTrue(call.sendResponseToLastReceivedRequest(200, "OK", 0));
    assertTrue(call.sendIncomingCallResponse(Response.OK, "Answer", 0));
    assertTrue(call.waitForAck(waitDuration));
    Thread.sleep(100);
    call.disconnect();
		call.dispose();

	}
	
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipServletResponse2" },
      desc = "Test CreatePrack throw Rel100Exception")
  public void testCreatePrack101() {
    assertSipMessage();
  }
  
  
  
  /**
   * The call flow is:
   * <p/>
   * UAS                           UA1
   * |<--------- (1)MESSAGE --------|
   * |--------- (2) 200    -------->|
   * |                              |
   * |---------- (3)INVITE  ------->|
   * |                              |
   * |<-------- (4) 183Rel   -------|
   * |--------- (5) PRACK    ------>|
   * |<-------- (6) 200OK for prack-| 
   * |--------- (7) INFO  --------->|
   * |<-------- (8) 200OK -for info-|
   * |<----- (9) 200OK -for invite--| 
   * |---------  (10) ACK --------->| 
   * |<-------- (11) BYE  ----------|
   * |                              |
   * |--------- (12) 200OK -------->|
   * |                              |
   */
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipServletResponse2" },
      desc = "Test CreatePrack throw IllegalStateException")
  public void testCreatePrack102() throws InterruptedException{
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
    SipCall call = ua1.createSipCall();
    assertTrue(call.listenForIncomingCall());
    call.waitForIncomingCall(waitDuration);
    call.sendIncomingCallReliableResponse(183,NEXT_MESSAGE, 0);
    assertTrue(call.waitForPrack(waitDuration));
    call.sendResponseToLastReceivedRequest(200, "OK", 0);
    assertTrue(call.waitForRequest(Request.INFO, waitDuration));
    assertTrue(call.sendResponseToLastReceivedRequest(200, "OK", 0));
    Thread.sleep(100);
    call.sendIncomingCallResponse(200, "OK", 0);
    call.waitForAck(waitDuration);
    Thread.sleep(100);
    call.disconnect();
    call.dispose();
  }  

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse3"},
					desc = "Returns an Iterator over all the" +
									" realms associated in challenge response")
	public void testGetChallengeRealms001() {
		assertSipMessage();
	}

	@AssertionIds(ids = { "SipServlet:JAVADOC:SipServletResponse4" },
	        desc = "Get OutputStream,return null")
    public void testGetOutputStream001() {
      assertSipMessage();
    }

	/**
	 * The call flow is:
	 * <p/>
	 * UA1                           PROXY                           UA2
	 * |                              |                              |
	 * |---------- (1)INVITE  ------->|                              |
	 * |                        <PROXY logic>                        |
	 * |                              |---------- (2)INVITE  ------->|
	 * |                              |                        <UAS assertion>
	 * |                              |<--------  (3)180      -------|
	 * |                        <PROXY Cancel>                       |
	 * |                              |---------- (4)CANCEL  ------->|
	 * |                              |                          Assertion
	 * |                              |<--------  (5)200      -------|
	 * |                              |                              |
	 */
	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse5"},
					desc = "Get Proxy")
	public void testGetProxy001() {
	  clientEntryLog();
		assertSipInviteProxyCancel(null, null,  null);
	}

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse6"},
					desc = "Get response reason")
	public void testGetReasonPhrase001() {
		assertSipMessage();
	}

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse7"},
					desc = "Get Request")
	public void testGetRequest001() {
		assertSipMessage();
	}

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse8"},
					desc = "Get response status")
	public void testGetStatus001() {
		assertSipMessage();
	}

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse9"},
					desc = "Get response Writer,return null")
	public void testGetWriter001() {
		assertSipMessage();
	}



  /**
   * The call flow is:
   * <p/>
   * UA1                     PROXY                   UA2        UA3
   * |                        |                       |          |    
   * |---- (1)MESSAGE ------->|                       |          |    
   * |                  <PROXY logic>                 |          |    
   * |                        |--- (2)MESSAGE ------->|          |
   * |                        |                       |          |
   * |                        |               <UAS assertion>    |   
   * |                        |<-- (3)600 ------------|          |    
   * |                        |                       |          |    
   * |                        |------------ (4)MESSAGE --------->|    
   * |                        |                       |          | 
   * |                        |<----------(5)400-----------------|
   * |                        |                       |          |
   * |<--- (6)406   --------- |                       |          |    
   * |                        |                       |          |    
   */                                                              
	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse10"
	    ,"SipServlet:JAVADOC:SipServletResponse15"},
					desc = "judge response is or not  Branch Response" +
							" and get proxyBranch Object")
	public void testIsBranchResponse001() {
	    clientEntryLog();
		try {

			StackTraceElement stack = getBasePackageStack(new Exception()
							.getStackTrace());
			String localServletName = getInterfaceName(stack.getClassName());
			String localMethodName = stack.getMethodName();
			Request reqUA1 = assembleRequest(Request.MESSAGE, localServletName,
							localMethodName, "OTHER", 1);
			reqUA1.addHeader(getUa2UriHeader());
			reqUA1.addHeader(getUa3UriHeader());
			// UA2 begin listen request
			ua2.listenRequestMessage();
			// UA3 listen request but do nothing
			ua3.listenRequestMessage();

			SipTransaction transUA1 = ua1.sendRequestWithTransaction(reqUA1,
							false, null);
			assertNotNull(ua1.format(), transUA1);

			RequestEvent eventUA2 = ua2.waitRequest(waitDuration);
			assertNotNull(eventUA2);
			Request messageReq = eventUA2.getRequest();

			ServerTransaction serverTransUA2 = ua2.getParent().getSipProvider()
							.getNewServerTransaction(messageReq);
			Response proxyResponse = createResponse(messageReq, ua2,
          Response.BUSY_EVERYWHERE, ua2.generateNewTag());

      serverTransUA2.sendResponse(proxyResponse);

      RequestEvent eventUA3 = ua3.waitRequest(waitDuration);
      assertNotNull(eventUA3);
      Request messageReqUA3 = eventUA3.getRequest();

      ServerTransaction serverTransUA3 = ua3.getParent().getSipProvider()
          .getNewServerTransaction(messageReqUA3);
      Response proxyResponseUA3 = createResponse(messageReqUA3, ua3,
          Response.BAD_REQUEST, ua3.generateNewTag());
      serverTransUA3.sendResponse(proxyResponseUA3);
			
			
			// wait for ua3 time out
			EventObject eventUA1 = ua1.waitResponse(transUA1, waitDuration);
			assertNotNull(eventUA1);


      if (eventUA1 instanceof ResponseEvent) {
        ResponseEvent responseEvent = (ResponseEvent) eventUA1;
        Response response = responseEvent.getResponse();

        assertEquals("Can get response", response.getStatusCode(),
            Response.BUSY_EVERYWHERE);
        assertNotNull(response.getHeader(TestConstants.TEST_RESULT));
        assertTrue(response.getHeader(TestConstants.TEST_RESULT).toString()
            .indexOf(TestConstants.TEST_RESULT_OK) > -1);

      } else {
        fail("fail to receive any response.");
      }

    } catch (Exception e) {
      logger.error("Exception in testIsBranchResponse001", e);
      throw new TckTestException(e);
    }
  }


	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse11"},
					desc = "send response")
	public void testSend001() {
		assertSipMessage();
	}

	 @AssertionIds(ids = { "SipServlet:JAVADOC:SipServletResponse11" },
	     desc = "test send response throw IllegalStateException")
  public void testSend101() {
     clientEntryLog();
     sendResponseByMessage("testSend101");
  }

	/**
	 * The call flow is:
	 * <p/>
	 * UA1                           UAS
	 * |                              |
	 * |---------- (1)INVITE  ------->|
	 * |                              |
	 * |                              |
	 * |<-------- (3) 180      -------|
	 * |--------- (4) PRACK    ------>|
	 * |                              |
   * |<-------- (5) 200OK ----------|
   * |---------  (6) ACK  --------->| 
	 * |---------  (7) BYE  --------->|
	 * |                              |
	 * |<-------- (8) 200OK ----------|
	 * |                              |
	 */
	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse12"},
					desc = "sent reliably response using the 100rel")
	public void testSendReliably001() {
    clientEntryLog();
    SipCall call = ua1.createSipCall();
    Header event = createRequire100relHeader();
    List<Header> headerList = new ArrayList<Header>();
    headerList.add(event);
    String viaNonProxyRoute = serverHost + ":" + serverPort + "/"
        + testProtocol;
    initiateOutgoingCall(call, null, null, headerList, viaNonProxyRoute);
    do {
      call.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("wait response - " + call.format(), call);
    } while (call.getReturnCode() != Response.RINGING);

    call.sendPrack();
    assertLastOperationSuccess(" send PRACK - " + call.format(), call);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      logger.error("test SendReliably001 error", e);
      throw new TckTestException(e);
    }
    Response okForPrack = (Response) call.getLastReceivedResponse()
        .getMessage();
    assertNotNull("Default response not sent", okForPrack);
    assertEquals("Unexpected default reason", Response.OK, okForPrack
        .getStatusCode());
    call.sendInviteOkAck();
    call.disconnect();
    call.dispose();
  }
	
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipServletResponse12" },
      desc = "test sent reliably response throw Rel100Exception")
  public void testSendReliably101() {
    assertSipMessage();
  }
  
  
  /**
   * The call flow is:
   * <p/>
   * UA1                           UAS
   * |                              |
   * |---------- (1)INVITE  ------->|
   * |                              |
   * |                              |
   * |<-------- (2) 180      -------|
   * |--------- (3) PRACK    ------>|
   * |                              |
   * |<-------- (4) 200OK ----------|
   * |--------- (5) ACK    -------->| 
   * |---------  (6) BYE  --------->|
   * |                              |
   * |<-------- (7) 200OK ----------|
   * |                              |
   */
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipServletResponse12" },
      desc = "test sent reliably response throw IllegalStateException")
  public void testSendReliably102() {
    clientEntryLog();
    SipCall call = ua1.createSipCall();
    Header event = createRequire100relHeader();
    List<Header> headerList = new ArrayList<Header>();
    headerList.add(event);
    String viaNonProxyRoute = serverHost + ":" + serverPort + "/"
        + testProtocol;
    initiateOutgoingCall(call, null, null, headerList, viaNonProxyRoute);
    do {
      call.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("wait response - " + call.format(), call);
    } while (call.getReturnCode() != Response.RINGING);

    call.sendPrack();
    assertLastOperationSuccess(" send PRACK - " + call.format(), call);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      logger.error("test SendReliably102 error", e);
      throw new TckTestException(e);
    }
    Response okForPrack = (Response) call.getLastReceivedResponse()
        .getMessage();
    assertNotNull("Default response not sent", okForPrack);
    assertEquals("Unexpected default reason", Response.OK, okForPrack
        .getStatusCode());
    assertNotNull(okForPrack.getHeader(TestConstants.TEST_RESULT));
    assertTrue(okForPrack.getHeader(TestConstants.TEST_RESULT).toString().indexOf(
        TestConstants.TEST_RESULT_OK) > -1);
    call.sendInviteOkAck();
    call.disconnect();
    call.dispose();
  }

  /**
   * The call flow is:
   * <p/>
   * UA1                           UAS
   * |                              |
   * |---------- (1)MESSAGE ------->|
   * |<-------- (2) 200    ---------|
   * |                              |
   * |<--------- (3)MESSAGE --------|
   * |--------- (4) 200    -------->|
   * |                              |
   * |<--------- (5)MESSAGE --------|
   * |--------- (6) 200    -------->|
   * |                              |
   */
  private void sendResponseByMessage(String method) {

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
    try {
      ua1.listenRequestMessage();
      RequestEvent eventUA1 = ua1.waitRequest(waitDuration);
      assertNotNull(eventUA1);
      Request messageReqUA1 = eventUA1.getRequest();
      ServerTransaction serverTransUA1 = ua1.getParent().getSipProvider()
              .getNewServerTransaction(messageReqUA1);
      Response reg200Resp = createResponse(messageReqUA1, ua1, 200, ua1
              .generateNewTag());
      reg200Resp.addHeader(getMethodHeader(method));
      serverTransUA1.sendResponse(reg200Resp);

      RequestEvent eventUA2 = ua1.waitRequest(waitDuration);
      assertNotNull(eventUA2);
      Request messageReqUA2 = eventUA2.getRequest();
      ServerTransaction serverTransUA2 = ua1.getParent().getSipProvider()
              .getNewServerTransaction(messageReqUA2);

      assertEquals("Unexpected request", Request.MESSAGE, messageReqUA2
              .getMethod());
      Response reg200Resp2 = createResponse(messageReqUA2, ua1, 200, ua1
              .generateNewTag());

      serverTransUA2.sendResponse(reg200Resp2);
    } catch (Exception e) {
      logger.error("test SipServeltResponse error", e);
      throw new TckTestException(e);
    }
  }
  
  
  
	
	

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse13"},
					desc = "set response status")
	public void testSetStatus001() {
		assertSipMessage();
	}

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServletResponse14"},
					desc = "set response status and reason")
	public void testSetStatus002() {
		assertSipMessage();
	}

	 @AssertionIds(ids = { "SipServlet:JAVADOC:SipServletResponse14" }, 
	     desc = "test set response status throw IllegalArgumentException")
  public void testSetStatus101() {
    assertSipMessage();
  }

	
	

	/**
	 * @return 100rel header for invite
	 */
	private Header createRequire100relHeader() {
		HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
		Header event = null;
		try {
			event = header_factory.createRequireHeader("100rel");
		} catch (Exception e) {
			logger.error("error in create 100rel header");
			throw new TckTestException(e);
		}
		return event;
	}

  private Header getMethodHeader(String method){
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
    try {
      return header_factory.createHeader(TestConstants.METHOD_HEADER, method);
    } catch (ParseException e) {
      logger.error("*** ParseException when creating private header ***", e);
      throw new TckTestException(e);
    }    
  }
		
  private Header getUa2UriHeader(){
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
    try {
      return header_factory.createHeader(TestConstants.UA2_URI, ua2URI);
    } catch (ParseException e) {
      logger.error("*** ParseException when creating private header ***", e);
      throw new TckTestException(e);
    }    
  }
  private Header getUa3UriHeader(){
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
    try {
      return header_factory.createHeader(TestConstants.UA3_URI, ua3URI);
    } catch (ParseException e) {
      logger.error("*** ParseException when creating private header ***", e);
      throw new TckTestException(e);
    }    
  }  
  
}
