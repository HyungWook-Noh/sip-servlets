/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 * All rights reserved.  
 * 
 * SipErrorEventListenerTest is used to test the APIs of 
 * javax.servlet.sip.SipErrorEvent
 * javax.servlet.sip.SipErrorListener
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.sip.DialogDoesNotExistException;
import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class SipErrorEventListenerTest extends TestBase {

	private static Logger logger = 
    Logger.getLogger(SipErrorEventListenerTest.class);
	// The wait duration for timer timeout
	private static final int waitMsg = 60000;
	
	public SipErrorEventListenerTest(String arg0) throws IOException {
		super(arg0);
	}
	
	/**	
	 * Test constructor of SipErrorEvent, the call flow is:
	 * 
	 *             			    UAC                           UAS
	 *             			 		 |                             |
	 *                       |--------- (1) MESSAGE ------>|
	 *                       |                             |                                             
	 * 											 |<-------- (2) 200 OK/500 ----|
	 *                       |                             |
	 */
	
	@AssertionIds(
			ids={"SipServlet:JAVADOC:SipErrorEvent1"},
      desc="Constructs a new SipErrorEvent.")
	public void testSipErrorEvent001() 
		throws ParseException, InvalidArgumentException {
		assertSipMessage(); 
	}
	
	/**	
	 * Test noAckReceived() of SipErrorEventListener, the call flow is:
	 * 
	 *             			    UAC                            UAS
	 *             			 		 |                              |
	 *                       |---------- (1) INVITE ------->|
	 *                       |                              |                                             
	 * 											 |<--------- (2) 200 OK --------|
	 *                       |              .               |
	 *                       |              .               |
	 *                       |              .               | 
	 *  										 |<--------- (3) MESSAGE -------|
	 *                       |                              |
	 *                       |---------- (4) 200 OK ------->|
	 *                       |                              |
	 */
	@AssertionIds(
			ids={"SipServlet:JAVADOC:SipErrorListener1",
					"SipServlet:JAVADOC:SipErrorEvent2", 
					"SipServlet:JAVADOC:SipErrorEvent3"},
			desc="Invoked by the servlet container to notify an application " 
      	+ "that no ACK was received for an INVITE transaction for " 
      	+ "which a final response has been sent upstream. " 
      	+ "And test SipErrorEvent.getRequest() and SipErrorEvent.getResponse()")
	public void testNoAckReceived001() 
		throws ParseException, InvalidArgumentException {
		
		clientEntryLog();
		
		SipCall a = ua1.createSipCall();
		// a listen to the incoming request
		a.listenForIncomingCall();
		
		// (1) send INVITE
		boolean status = initiateOutgoingCall(a, null, null, null, null);
		assertTrue("Initiate outgoing call failed - " + a.format(), status);
		
		// receive 100
		a.waitOutgoingCallResponse(waitDuration);
		while (a.getReturnCode() == Response.TRYING)
		{
			a.waitOutgoingCallResponse(waitDuration);
			assertLastOperationSuccess(
					"Subsequent response never received - " + a.format(), a);
		}

		// (2) receive 200
		a.waitOutgoingCallResponse(waitDuration);
		assertResponseReceived("Unexpected response received", Response.OK, a);

		// (3) receive MESSAGE
		Request message = null;

		do {
			message = waitIncomingMessage(a.getParent(), waitMsg);
		} while (message != null && message.getHeader("SEL") == null);
		
		logger.info("=== received MESSAGE:" + message + " ===");
		Header selHeader = message.getHeader("SEL");
    if (selHeader != null) {
	    if ("SEL: noAckReceived succeeded, getRequest() succeeded, getResponse() failed"
	    		.equals(selHeader.toString())) {
	    	fail("The getResponse() of SipErrorEvent failed.");
	    }
	    if ("SEL: failed".equals(selHeader.toString())) {
	    	fail("The API noAckReceived() of SipErrorListener were not invoked by Container.");
	    }
    } else {
    	fail("No SEL header in the MESSAGE.");
    }
 
    // (4) send 200
    status = sendResponseForMessage(a.getParent(), message, Response.OK);
    assertLastOperationSuccess(
				"Send 200 OK response for MESSAGE failed - " + a.getParent().format(), 
				a.getParent());
				
	}
	
	/**	
	 * Test noPrackReceived() of SipErrorEventListener, the call flow is:
	 * 
	 *             			    UAC                            UAS
	 *             			 		 |                              |
	 *                       |---------- (1) INVITE ------->|
	 *                       |                              |                                             
	 *                       |<--------- (2) 183 rel -------|
	 *                       |              .               |
	 *                       |              .               |
	 *                       |              .               | 
	 *  										 |<--------- (3) 500 -----------|
	 *                       |                              |
	 *                       |---------- (4) ACK ---------->|
	 *                       |                              |
	 * @throws SipException 
	 * @throws DialogDoesNotExistException 
	 */
	@AssertionIds(ids={"SipServlet:JAVADOC:SipErrorListener2"},
      desc="Invoked by the servlet container for applications acting as a UAS " 
      	+ "when no PRACK was received for a previously sent reliable " 
      	+ "provisional response.")
	public void testNoPrackReceived001() 
		throws ParseException, InvalidArgumentException, DialogDoesNotExistException, SipException {
		
		clientEntryLog();

		SipCall a = ua1.createSipCall();
		
		// (1) send INVITE with "Supported:100rel"
		ArrayList<Header> additionalHeaders = new ArrayList<Header>(1);
		additionalHeaders.add(a.getHeaderFactory().createSupportedHeader("100rel"));
		boolean status = 
			initiateOutgoingCall(a, null, null, additionalHeaders, null);
		assertTrue("Initiate outgoing call failed - " + a.format(), status);
		
		// (2) receive 183 response untill (3) 500 response received
		do {
			a.waitOutgoingCallResponse(); 
			assertLastOperationSuccess("Wait response error - " + a.format(), a);
		} while (a.getReturnCode() != 500);
	
		assertEquals("500 response not reveived -", a.getReturnCode(), 500);
		
		Header selHeader = a.getLastReceivedResponse().getMessage().getHeader("SEL");
		assertNotNull(selHeader);
    if ("SEL: failed".equals(selHeader.toString())) {
    	fail("The API noPrackReceived() of SipErrorListener were not invoked by Container.");
    }
		
    // (4) send ACK
    a.sendInviteOkAck();
    
	}
	
}
