/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.
 *
 * This class is used to test the APIs of the interface
 * javax.servlet.sip.SipServlet.   
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.address.AddressFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class SipServletTest extends TestBase {

	private static final String NEXT_MESSAGE = "Need Message";

	private static Logger logger = Logger.getLogger(SipServletTest.class);

	public SipServletTest(String arg0) throws IOException, UnknownHostException {
		super(arg0);
	}


	
	
	 /**
   * The call flow is:
   * <p/>
   * UA1                           UAS
   * |                              |
   * |---------- (1)INVITE  ------->|
   * |<-------- (2) 100    ---------|
   * |                              |
   * |                              |
   * |<-------- (4) 200OK    -------|
   * |--------- (5) ACK      ------>|
   * |                              |
   * |                              |
   * |---------  (6) BYE  --------->|
   * |                              |
   * |<-------- (7) 200OK ----------|
   * |                              |
   */


  @AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet1",
          "SipServlet:JAVADOC:SipServlet22",
          "SipServlet:JAVADOC:SipServlet17",
          "SipServlet:JAVADOC:SipServlet7" ,
          "SipServlet:JAVADOC:SipServlet6"
          },
          desc = "SipServlet receive message,Request,Invite,Ack,Info,Bye")
  public void testDoInviteAckBye001() {
      clientEntryLog();
    SipCall call = ua1.createSipCall();
    initiateOutgoingCall(call);

    do {
      call.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("wait response - " + call.format(), call);
    } while (call.getReturnCode() != Response.OK);
    call.sendInviteOkAck();
    assertLastOperationSuccess("send ack - " + call.format(), call);
    assertTrue(call.disconnect());
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      logger.error("Exception in testDoInviteAckBye001", e);
      throw new TckTestException(e);
    }

    Response okForBye = (Response) call.getLastReceivedResponse().getMessage();
    assertNotNull("Default response reason not sent", okForBye);
    assertEquals("Unexpected default reason", Response.OK,
            okForBye.getStatusCode());
    assertEquals("Unexpected default reason", Request.BYE,
            ((CSeqHeader) okForBye.getHeader(CSeqHeader.NAME)).getMethod());

    call.dispose();
  }
	
	
	
	
	/**
	 * The call flow is:
	 * <p/>
	 * UA1                           UAS
	 * |                              |
	 * |---------- (1)INVITE  ------->|
	 * |<-------- (2) 100    ---------|
	 * |                              |
	 * |                              |
	 * |<-------- (4) 200OK    -------|
	 * |--------- (5) ACK      ------>|
	 * |                              | 
	 * |--------- (6) INFO     ------>|
	 * |                              |
	 * |<-------- (7) 200OK    -------|
	 * |                              |
	 * |--------- (8) BYE   --------->|
	 * |                              |
	 * |<-------- (9) 200OK ----------|
	 * |                              |
	 */


	@AssertionIds(ids = {
					"SipServlet:JAVADOC:SipServlet6"},
					desc = "SipServlet receive Info")
	public void testDoInfo001() {
	    clientEntryLog();
		SipCall call = ua1.createSipCall();
		initiateOutgoingCall(call);

		do {
			call.waitOutgoingCallResponse(waitDuration);
			assertLastOperationSuccess("wait response - " + call.format(), call);
		} while (call.getReturnCode() != Response.OK);
		call.sendInviteOkAck();
		assertLastOperationSuccess("send ack - " + call.format(), call);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.error("*** Exception in testDoInfo001", e);
			throw new TckTestException(e);
		}
    call.sendRequest(Request.INFO);
		assertLastOperationSuccess("send INFO - " + call.format(), call);
		
		while (call.getReturnCode() != SipPhone.TIMEOUT_OCCURRED) {
      call.waitOutgoingCallResponse(waitDuration);
    }
    assertResponseReceived("Unexpected response received", Response.OK,
        Request.INFO, 2, call);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.error("*** Exception in testDoInfo001", e);
			throw new TckTestException(e);
		}
		call.disconnect();
    assertLastOperationSuccess("call disc - " + call.format(), call);
    while (call.getReturnCode() != SipPhone.TIMEOUT_OCCURRED) {
      call.waitOutgoingCallResponse(waitDuration);
    }
    assertResponseReceived("Did not receive 200 OK on BYE", Response.OK,
        Request.BYE, 3, call);
	}
	
	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet22"},
      desc = "SipServlet Request")
	public void testDoRequest101(){
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
	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet2"},
					desc = "SipServlet receive BranchResponse()")
	public void testDoBranchResponse001() {
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


	/**
	 * The call flow is:
	 * <p/>
	 * UA1                           UAS
	 * |                              |
	 * |---------- (1)INVITE  ------->|
	 * |<-------- (2) 100    ---------|
	 * |                              |
	 * |                              |
	 * |<-------- (3) 180      -------|
	 * |--------- (4) CANCEL   ------>|
	 * |                              |
	 * |<-------- (5) 200OK ----------|
	 * |                              |
	 */

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet4"},
					desc = "SipServlet receive Cancel")
	public void testDoCancel001() {
    clientEntryLog();
		SipCall call = ua1.createSipCall();
		initiateOutgoingCall(call);

		do {
			call.waitOutgoingCallResponse(waitDuration);
			assertLastOperationSuccess("wait response - " + call.format(), call);
		} while (call.getReturnCode() != Response.RINGING);

		call.sendCancel();
		try {
			Thread.sleep(waitDuration);
		} catch (InterruptedException e) {
			logger.error("Exception in thread sleep", e);
			throw new TckTestException(e);
		}


		assertEquals("response received", Response.OK, call.getReturnCode());
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
	 * |--------- (4) 400    -------->|
	 * |                              |
	 * |<--------- (5)MESSAGE --------|
	 * |--------- (6) 200    -------->|
	 * |                              |
	 */

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet5"},
					desc = "SipServlet receive 4xx Error Response")
	public void testDoErrorResponse001() {
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
		try {
			ua1.listenRequestMessage();
			RequestEvent eventUA1 = ua1.waitRequest(waitDuration);
			assertNotNull(eventUA1);
			Request messageReqUA1 = eventUA1.getRequest();
	     ServerTransaction serverTransUA1 = ua1.getParent().getSipProvider()
       .getNewServerTransaction(messageReqUA1);
			Response reg400Resp = createResponse(messageReqUA1, ua1, 400, ua1
							.generateNewTag());
			reg400Resp.setReasonPhrase(NEXT_MESSAGE);
			serverTransUA1.sendResponse(reg400Resp);

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
			logger.error("Exception in Error Response", e);
			throw new TckTestException(e);
		}
	}


	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet8"},
					desc = "SipServlet receive Message")
	public void testDoMessage001() {
		assertSipMessage();
	}

	
	/**
     * The call flow is:
     * <p/>
     * UA1                           UAS
     * |                              |
     * |---------- (1)NOTIFY  ------->|
     * |<-------- (2) 200    ---------|
     */
	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet9"},
					desc = "SipServlet receive Notify")
	public void testDoNotify001() {
		try {
			HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
			Header SubState = header_factory
							.createSubscriptionStateHeader("ACTIVE");
			Header event = header_factory.createEventHeader("presence");
			Header contact = header_factory.createContactHeader();
			Header expire = header_factory.createExpiresHeader(3600);
			List<Header> headerList = new ArrayList(4);
			headerList.add(SubState);
			headerList.add(event);
			headerList.add(contact);
			headerList.add(expire);

			// Subscription-State
			assertSipMessage(headerList, null, null, "NOTIFY", 1);

		} catch (Exception e) {
			logger.error("notify error", e);
			throw new TckTestException(e);
		}
	}

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet10"},
					desc = "SipServlet receive Options")
	public void testDoOptions001() {
		assertSipMessage(null, null, null, "OPTIONS", 1);
	}


	/**
	 * The call flow is:
	 * <p/>
	 * UA1                           UAS
	 * |                              |
	 * |---------- (1)MESSAGE ------->|
	 * |<-------- (2) 200    ---------|
	 * |                              |
	 * |<--------- (3)INVITE  --------|
	 * |--------- (4) 183    -------->|
	 * |                              |
	 * |<-------- (5) PRACK ----------|
	 * |--------- (6) 200 OK -------->|
	 * |--- (7) 200 OK for Invite---->|
	 * |<------(8) ACK ---------------|
	 * |--------- (9) BYE    -------->|
	 * |<-------- (10) 200   ---------|
	 */


	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet12"},
					desc = "SipServlet receive 1xx Provisional Response")
	public void testDoProvisionalResponse001() {
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
		call.listenForIncomingCall();
		call.waitForIncomingCall(waitDuration);
		call.sendIncomingCallReliableResponse(183, NEXT_MESSAGE, 3600);
		assertTrue(call.waitForPrack(waitDuration));
		assertTrue(call.sendResponseToLastReceivedRequest(Response.OK, "OK", 0));
		assertTrue(call.sendIncomingCallResponse(Response.OK, "Answer", 0));
		assertTrue(call.waitForAck(waitDuration));
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			logger.error("test doProvisionalResponse error", e);
			throw new TckTestException(e);
		}

		call.disconnect();
		call.dispose();

	}

	/**
     * The call flow is:
     * <p/>
     * UA1                           UAS
     * |                              |
     * |---------- (1)PUBLISH ------->|
     * |<-------- (2) 200    ---------|
     */
	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet13"},
					desc = "SipServlet receive Publish")
	public void testDoPublish001() {
        clientEntryLog();
		try {
			HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
			Header event = header_factory.createEventHeader("presence");
			List<Header> headerList = new ArrayList(1);
			headerList.add(event);

			assertSipMessage(headerList, null, null, "PUBLISH", 1);
		} catch (ParseException e) {
			logger.error("add header error", e);
			throw new TckTestException(e);
		}

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
	 * |--------- (4) 302    -------->|
	 * |                              |
	 * |---------- (5)MESSAGE ------->|
	 * |<-------- (6) 200    ---------|
	 * |                              |
	 */

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet14"},
					desc = "SipServlet receive 3xx Redirect Response")
	public void testDoRedirectResponse001() {
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
		try {
			ua1.listenRequestMessage();
			RequestEvent eventUA1 = ua1.waitRequest(waitDuration);
			assertNotNull(eventUA1);
			Request messageReqUA1 = eventUA1.getRequest();
			ServerTransaction serverTransUA1 = ua1.getParent().getSipProvider()
							.getNewServerTransaction(messageReqUA1);
			Response reg3xxResp = createResponse(messageReqUA1,
							ua1, Response.MOVED_TEMPORARILY, ua1
							.generateNewTag());
			reg3xxResp.setReasonPhrase(NEXT_MESSAGE);
			serverTransUA1.sendResponse(reg3xxResp);

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
			logger.error("test DoRedirectResponse error", e);
			throw new TckTestException(e);
		}

	}

    /**
     * The call flow is:
     * <p/>
     * UA1                           UAS
     * |                              |
     * |---------- (1)REFER   ------->|
     * |<-------- (2) 200    ---------|
     */
	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet15"},
					desc = "SipServlet receive Refer")
	public void testDoRefer001() {
        clientEntryLog();
		try {
			HeaderFactory header_factory = ua1.getParent().getHeaderFactory();

			AddressFactory add_factory = ua1.getParent().getAddressFactory();

			Header referTo = header_factory.createReferToHeader(add_factory
							.createAddress(ua2Addr));
			Header contact = header_factory.createContactHeader();
			List<Header> headerList = new ArrayList(3);
			headerList.add(referTo);
			headerList.add(contact);
			assertSipMessage(headerList, null, null, "REFER", 1);
		} catch (ParseException e) {
			logger.error("add header error", e);
			throw new TckTestException(e);

		}

	}
	
	
    /**
     * The call flow is:
     * <p/>
     * UA1                           UAS
     * |                              |
     * |---------- (1)REGISTER ------>|
     * |<-------- (2) 200    ---------|
     */
	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet16"},
					desc = "SipServlet receive Register")
	public void testDoRegister001() {
		assertSipMessage(null, null, null, "REGISTER", 1);
	}

	

    /**
     * The call flow is:
     * <p/>
     * UA1                           UAS
     * |                              |
     * |---------- (1)SUBSCRIBE------>|
     * |<-------- (2) 200    ---------|
     */

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet19"},
					desc = "SipServlet receive Subscribe")
	public void testDoSubscribe001() {
        clientEntryLog();
		try {
			HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
			Header contact = header_factory.createContactHeader();
			Header expire = header_factory.createExpiresHeader(3600);
			List<Header> headerList = new ArrayList<Header>(2);
			headerList.add(contact);
			headerList.add(expire);
			assertSipMessage(headerList, null, null, "SUBSCRIBE", 1);
		} catch (InvalidArgumentException e) {
			logger.error("add header error", e);
			throw new TckTestException(e);
		}

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
	 * |---------- (5)MESSAGE ------->|
	 * |<-------- (6) 200    ---------|
	 * |                              |
	 */

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet20",
					"SipServlet:JAVADOC:SipServlet18"},
					desc = "SipServlet receive 2xx Success Response")
	public void testDoSuccessResponse001() {
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
		try {
			ua1.listenRequestMessage();
			RequestEvent eventUA1 = ua1.waitRequest(waitDuration);
			assertNotNull(eventUA1);
			Request messageReqUA1 = eventUA1.getRequest();
			ServerTransaction serverTransUA1 = ua1.getParent().getSipProvider()
							.getNewServerTransaction(messageReqUA1);
			Response reg200Resp = createResponse(messageReqUA1, ua1, 200, ua1
							.generateNewTag());
			reg200Resp.setReasonPhrase(NEXT_MESSAGE);
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
			logger.error("test DoSuccessResponse error", e);
			throw new TckTestException(e);
		}
	}

	/**
	 * The call flow is:
	 * <p/>
	 * UA1                           UAS
	 * |                              |
	 * |---------- (1)INVITE  ------->|
	 * |<-------- (2) 100    ---------|
	 * |                              |
	 * |                              |
	 * |<-------- (3) 180      -------|
	 * |--------- (4) PRACK    ------>|
	 * |<-------- (5) 200OK ----------|
	 * |                              |
	 * |--------- (6) UPDATE  ------->|
	 * |<-------- (7) 200OK ----------|
	 * |--------- (8) BYE   --------->|
	 * |                              |
	 * |<-------- (9) 200OK ----------|
	 * |                              |
	 */

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet21",
					"SipServlet:JAVADOC:SipServlet11"},
					desc = "SipServlet receive PRACK and Update")
	public void testDoPrackUpdate001() {
        clientEntryLog();
		SipCall call = ua1.createSipCall();
		Header event = createRequire100relHeader();
		List<Header> headerList = new ArrayList<Header>();
		headerList.add(event);
		String viaNonProxyRoute = serverHost + ":" + serverPort + "/" + testProtocol;
		initiateOutgoingCall(call, null, null, headerList, viaNonProxyRoute);
		do {
			call.waitOutgoingCallResponse(waitDuration);
			assertLastOperationSuccess("wait response - " + call.format(), call);
		} while (call.getReturnCode() != Response.RINGING);

		call.sendPrack();
		assertLastOperationSuccess("send PRACK - " + call.format(), call);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.error("test DoUpdate error", e);
			throw new TckTestException(e);
		}

		Response okForPrack = (Response) call.getLastReceivedResponse()
						.getMessage();
		assertNotNull("Default response not sent", okForPrack);
		assertEquals("Unexpected default reason", Response.OK,
						okForPrack.getStatusCode());
		assertEquals("Unexpected default reason", Request.PRACK,
						((CSeqHeader) okForPrack.getHeader(CSeqHeader.NAME)).getMethod());

		call.sendUpdate();
		assertLastOperationSuccess("send update - " + call.format(), call);
		call.waitOutgoingCallResponse(waitDuration);
		assertEquals("response received", Response.OK, call
        .getLastReceivedResponse().getStatusCode());
    assertEquals("response received", Request.UPDATE,
        ((CSeqHeader) call.getLastReceivedResponse().getMessage()
        .getHeader(CSeqHeader.NAME)).getMethod());
		
		call.sendIncomingCallResponse(200, "OK", 0);
		call.sendInviteOkAck();
		call.disconnect();
		call.dispose();

	}


	private Header createRequire100relHeader() {
		HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
		Header event = null;
		try {
			event = header_factory.createRequireHeader("100rel");
		} catch (ParseException e) {
			logger.error("Exception in create 100rel header", e);
			throw new TckTestException(e);
		}
		return event;
	}

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet23"},
					desc = "log message")
	public void testLog001() {
		assertSipMessage();
	}

	@AssertionIds(ids = {"SipServlet:JAVADOC:SipServlet24"},
					desc = "log throwable message")
	public void testLog002() {
		assertSipMessage();
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
