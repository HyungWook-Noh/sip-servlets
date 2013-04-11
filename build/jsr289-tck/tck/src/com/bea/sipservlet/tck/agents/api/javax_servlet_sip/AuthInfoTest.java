/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * AuthInfoTest is used to test the APIs of 
 * javax.servlet.sip.AuthInfo
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipRequest;
import org.cafesip.sipunit.SipTransaction;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.Header;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;

public class AuthInfoTest extends TestBase {
  private static Logger logger = Logger.getLogger(AuthInfoTest.class);

  public AuthInfoTest(String arg0) throws IOException {
    super(arg0);
  }
  
  /**	
	 * For testAddAuthInfo001(), the call flow is:
	 * 
	 *          			      UAC                              UAS
	 *             			 		 |                                |
	 *                       |-------- (1) MESSAGE ---------->|
	 *                       |                                |                                             
	 * 											 |<------- (2) 200 OK ------------|
	 *                       |                                |
	 *                       |<------- (3) INVITE ------------|
	 *                       |                                |
	 *                       |-------- (4) 401 Unauthorized ->|
	 *                       |                                |
	 *                       |<------- (5) ACK ---------------|
	 *                       |                                |
	 *                       |<------- (6) INVITE ------------|
	 *                       |																|
	 *                       |-------- (7) 200 OK ----------->|
	 *                       |                                |
	 *                       |<------- (8) ACK ---------------|
	 *                       
	 *                       
	 */
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:AuthInfo1"},
      desc = "Add authentication info into the AuthInfo object" 
      	+ " for a challenge response of a specific type (401/407) and realm. ")
  public void testAddAuthInfo001() 
  	throws ParseException, SipException, InvalidArgumentException {
  	
  	clientEntryLog();
		int cseq = 1;
		String challenge = 
			"Digest realm =\"beasys.com\", nonce=\"dbffb13c8a4d30117c25c753d1fa7710\"," 
			+ " opaque=\"\", stale=FALSE, algorithm=MD5, qop=\"auth\"";
		
		SipCall a = ua1.createSipCall();
		// ua listens to incoming INVITE
		a.listenForIncomingCall(); 
		
		// (1) send MESSAGE
		SipTransaction trans = sendMessage(ua1, cseq);
		if (trans == null) {
			fail("Fail to send MESSAGE out.");
		}
		
    // (2) receive 200 OK
		EventObject event = ua1.waitResponse(trans, waitDuration);
    if(event == null){
      logger.warn("*** 200 ok of MESSAGE is not received, but case will continue***");
    }
    
    // (3) ua receive INVITE            
    a.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("a wait incoming call - " + a.format(), a);
		    
    // (4) send 401
    SipRequest request = a.getLastReceivedRequest();
    Header hdr = request.getMessage().getHeader(AuthorizationHeader.NAME);
    assertTrue("Authorization Header already in 1st INVITE - ", hdr == null);
    boolean status = false;
    if (hdr == null) {
    	ArrayList<Header> additionalHeaders = new ArrayList<Header>(1);
    	additionalHeaders.add(a.getHeaderFactory().createWWWAuthenticateHeader(challenge));
    	status = a.sendIncomingCallResponse(
    		Response.UNAUTHORIZED, "Unauthorized", 1, additionalHeaders, null, null);
    	assertLastOperationSuccess("a send 401 - " + a.format(), a);	
    }	
    if (!status) {
    	fail("Fail to send 401 response out.");
    }
       
    // (5) ua receive ACK
    // Not necessary to assert ACK
    // because ACK received for ServerTransaction not delivering to application by Jain sip.

    
    // (6) receive INVITE again
  	a.listenForReinvite();
  	SipTransaction tx = a.waitForReinvite(waitDuration);
  	assertLastOperationSuccess("a wait for INVITE - " + a.format(), a);
    
    // (7) send 200 OK
  	Request req = tx.getRequest();
  	if (req.getHeader(AuthorizationHeader.NAME) != null) { 
  		String contactUri = a.getParent().getContactInfo().getURI();
      String contact = contactUri.substring(0,
      		contactUri.lastIndexOf("lr") - 1);
      assertTrue(
      		a.respondToReinvite(tx, Response.OK, "OK", 2, contact, null, null,(String) null, null));
  	} else {
  		fail("No Authorization Header in 2nd INVITE.");
  	}
    
    // (8) UA1 receive ACK  
  	assertTrue(a.waitForAck(waitDuration));
  	
  }

}
