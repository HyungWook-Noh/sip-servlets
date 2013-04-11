/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * MultihomedTest is used to test the multihomed functionality defined 
 * in JSR289 Spec chapter 14.2
 * 
 * Due to the SipUnit capability, testMultihomedProxy(), testMultihomedProxyBranch()
 * and testMultihomedProxyForRegister() can only be passed with udp.
 *
 */
package com.bea.sipservlet.tck.agents.spec;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.ListIterator;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipRequest;
import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.agents.utils.MultihomedHeaderUtil;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class MultihomedTest extends TestBase {

	public MultihomedTest(String arg0) throws IOException, UnknownHostException {
		super(arg0);
	}
	
	/**
	 * testMultihomedUas
	 * 
	 *            			    UAC                           UAS
	 *             			     |                             |
	 *                       |--------- (1) INVITE ------->|
	 *                       |                             |                                             
	 *                       |<-------- (2) 180 Ring ------|
	 *                       |                             |
	 *  check Contact header |<-------- (3) 200 OK --------|
	 *                       |                             |
	 *                       |--------- (4) ACK ---------->|
	 *                       |                             |
	 *                       |                             |
	 *  check Via header     |<-------- (5) BYE -----------|
	 *                       |                             |
	 *                       |--------- (6) 200 OK ------->|
	 *                       |                             |
	 *
	 */	
	@TargetApplication(value = ApplicationName.UAS)
	@AssertionIds(
			ids = {"SipServlet:SPEC:Multihomed1" }, 
			desc = "Tests the multihomed functionality with setOutboundInterface" 
				+ " of SipSession.")
	public void testMultihomedUas() throws Exception {
		
		clientEntryLog();
		SipCall call = ua1.createSipCall();
		
		// (1) UA1 send INVITE
		boolean status_ok = 
			initiateOutgoingCall(call, null, "MultihomedUas", null, null);
    assertTrue("Initiate outgoing call failed - " + call.format(), status_ok);
		
    // (2) UA1 receive 180/200 response
		do {
      call.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("ua waits for 200/INVITE - " 
      	+ call.format(), call);
    } while (call.getReturnCode() != Response.OK);
		
		SipResponse response = call.getLastReceivedResponse();
		// get the Contact header from 200 OK
    ContactHeader cont = 
    	(ContactHeader)response.getMessage().getHeader(ContactHeader.NAME);
    assertNotNull(cont);
    // get private header from 200 OK
    Header multihomedHeader = response.getMessage().getHeader("Multihomed");
    		
    // check Contact header with Multihomed header
    if (multihomedHeader != null) {

    	boolean result = 
    		MultihomedHeaderUtil.checkContactHeader(cont, multihomedHeader.toString());
    	if (!result) {
    		fail("Outbound interface setting is invalid, " +
    				"Contact header not equals the Multihomed header.");
    	}
    } else {
    	fail("No Multihomed header in the 200 OK.");
    }
		
		// (4) UA1 send ACK
    call.sendInviteOkAck();
    assertLastOperationSuccess("Failure sending ACK - " + call.format(), call);

    // wait for incoming request  
    call.listenForDisconnect();
    call.waitForDisconnect(waitDuration);
    SipRequest bye = call.getLastReceivedRequest(); 

	  assertEquals(true, bye.isBye());
	    	
	  // get Via header from BYE
	  ViaHeader via = (ViaHeader)bye.getMessage().getHeader(ViaHeader.NAME);
	  assertNotNull(via);
	  // check Via header with Multihomed header
	  if (multihomedHeader != null) {
	  	boolean result = 
	  		MultihomedHeaderUtil.checkViaHeader(via, multihomedHeader.toString());
	  		if (!result) {
	  			fail("Outbound interface setting is invalid," +
	  				"Via header not equals the Multihomed header.");
	      }
	  } else {
    	fail("No Multihomed header in the BYE.");
    }
	    	
	  // (6) UA1 send 200 for BYE 
	  call.respondToDisconnect();
	}
	
	@TargetApplication(value = ApplicationName.PROXY)
	@AssertionIds(
			ids = {"SipServlet:SPEC:Multihomed2" }, 
			desc = "Tests the multihomed functionality with setOutboundInterface " 
				+	"of Proxy.")
	public void testMultihomedProxy() 
		throws ParseException, SipException, InvalidArgumentException, 
		InterruptedException {
		clientEntryLog();
		checkMultihomedProxyCase("MultihomedProxy");
	}
	
	@TargetApplication(value = ApplicationName.PROXY)
	@AssertionIds(
			ids = {"SipServlet:SPEC:Multihomed3" }, 
			desc = "Tests the multihomed functionality with setOutboundInterface " 
				+	"of ProxyBranch.")
	public void testMultihomedProxyBranch() 
		throws ParseException, SipException, InvalidArgumentException, 
		InterruptedException {
		clientEntryLog();
		checkMultihomedProxyCase("MultihomedProxyBranch");
	}
	
	
	/**
	 * testMultihomedProxyForRegister
	 * 
	 *        UA1                 Proxy             Registrar(UA2)
	 *         |                   |                   |
	 *         |-- (1) REGISTER -->|                   |
	 *         |                   |                   |
	 *         |                   |-- (2) REGISTER -->| check Path, Via,
	 *         |                   |                   | Record-Route headers
	 *         |                   |<-- (3) 200 OK ----|
	 *         |                   |                   |
	 *         |<-- (4) 200 OK ----|                   |
	 *         |                   |                   |
	 */  
	@TargetApplication(value = ApplicationName.PROXY)
	@AssertionIds(
			ids = {"SipServlet:SPEC:Multihomed4" }, 
			desc = "Tests the multihomed functionality with REGISTER.")
	public void testMultihomedProxyForRegister() 
		throws ParseException, SipException, InvalidArgumentException {
		clientEntryLog();
    // UA2 listen to incoming message            
    ua2.listenRequestMessage();
    
    // (1) UA1 send REGISTER
    Request register = 
    	assembleRequest(Request.REGISTER, "MultihomedProxyForRegister", 
    		null, TestConstants.SERVER_MODE_PROXY, 1); 
    SipTransaction transUA1 = 
    	ua1.sendRequestWithTransaction(register.toString(), false, null);
    assertNotNull("Fail to send REGISTER out. - ", transUA1);
    
    // (2) UA2 receive REGISTER            
    RequestEvent registerEvt = ua2.waitRequest(waitDuration);
    assertLastOperationSuccess(
        "UA2 wait for REGISTER - " + ua2.format(), ua2);
    assertNotNull(registerEvt);
    
    Request reqRegister = registerEvt.getRequest();
    assertNotNull("Fail to receive REGISTER. - ", reqRegister);
    assertEquals("Unexpected request",
        Request.REGISTER, reqRegister.getMethod());
    
    // get private header 
    Header multihomedHeader = reqRegister.getHeader("Multihomed");
    
    // get Path header
    ExtensionHeader path = (ExtensionHeader)reqRegister.getHeader("Path");
    assertNotNull(path);
    boolean result = false;
    // check Path header
    if (multihomedHeader != null) {
    	result = 
    		MultihomedHeaderUtil.checkPathHeader(path, multihomedHeader.toString());
    	if (!result) {
    		fail("Outbound interface setting is invalid, " +
    				"Path header not equals the Multihomed header.");
    	}
    } else {
    	fail("No Multihomed header in the REGISTER.");
    }
    
    // get Via headers
	  ListIterator vias =  reqRegister.getHeaders(ViaHeader.NAME); 
	  assertNotNull(vias);
	  // check Via headers  
	  while (vias.hasNext()) {
	  	ViaHeader via = (ViaHeader) vias.next();
	   	result = 
	   		MultihomedHeaderUtil.checkViaHeader(via, multihomedHeader.toString());
	   	if (result) break;  	
	  }
	  if (!result) {
    	fail("Outbound interface setting is invalid, " +
    		"Via header not equals the Multihomed header.");
    }		
    
    // get Record-Route headers
    RecordRouteHeader recordRoute = 
    	(RecordRouteHeader)reqRegister.getHeader(RecordRouteHeader.NAME);
    assertNotNull(recordRoute);
    
    // check Record-Route header
    result = 
    	MultihomedHeaderUtil.checkRecordRouteHeader(recordRoute, multihomedHeader.toString());
    if (!result) {
    	fail("Outbound interface setting is invalid, " +
    		"Record-Route header not equals the Multihomed header.");
    }
     
    // (3) UA2 send 200 response for REGISTER	    
    ServerTransaction serverTransUA2 = 
    	ua2.getParent().getSipProvider().getNewServerTransaction(reqRegister);
    Response register200Resp = 
    	this.createResponse(reqRegister, ua2, 200, ua2.generateNewTag());            
    serverTransUA2.sendResponse(register200Resp);
    
    // (4) UA1 receive 200 OK
    EventObject resp200Evt = ua1.waitResponse(transUA1, waitDuration);
    resp200Evt = filterEvent(ua2, transUA1, resp200Evt);
    assertEquals("Should have received OK", Response.OK, 
	    	((ResponseEvent) resp200Evt).getResponse().getStatusCode());
	}
	
	/**
	 * testMultihomedProxy/testMultihomedProxyBranch
	 *    
	 *       UA1                           Proxy                        UA2
	 *        |                              |                           |
	 *        |---------- (1) INVITE ------->|                           |
	 *        |                              |-------- (2) INVITE ------>| check Via, 
	 *        |                              |                           | Record-Route headers
	 *        |                              |<--------(3) 180 RINGING --|
	 *        |                              |                           |
	 *        |<--------- (4) 180 RINGING ---|                           |
	 *        |                              |                           |
	 *        |                              |<------- (5) 200 OK -------|
	 *        |                              |                           |
	 *        |<--------- (6) 200 OK --------|                           |
	 *        |                              |                           |
	 *        |---------- (7) ACK ---------->|                           |
	 *        |                              |-------- (8) ACK --------->|
	 *        |                              |                           |
	 *        |---------- (9) BYE ---------->|                           |
	 *        |                              |-------- (10) BYE -------->|
	 *        |                              |                           |
	 *        |                              |<------- (11) 200 OK ------|
	 *        |<-------- (12) 200 OK --------|                           |
	 * 
	 *
	 */
	
	private void checkMultihomedProxyCase(String servletName) 
		throws ParseException, SipException, InvalidArgumentException, 
		InterruptedException {       
    
    SipCall call1 = ua1.createSipCall();
    SipCall call2 = ua2.createSipCall();
    
    // UA2 listen to incoming message            
    call2.listenForIncomingCall();
    
    ArrayList<Header> headers = new ArrayList<Header> (2);
    HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
    AddressFactory addressFactory = ua1.getParent().getAddressFactory();
    // add URI of UA2 with Route header
    Address addr = addressFactory.createAddress(ua2.getContactInfo().getURI());
    RouteHeader route = headerFactory.createRouteHeader(addr);           
    headers.add(0, route);
    
    // add the URI of Proxy with Route Header 
    addr = addressFactory.createAddress(
    	"<sip:" + serverHost+":" + serverPort + ";lr>");
    route = headerFactory.createRouteHeader(addr);
    headers.add(1, route);
    
    // (1) UA1 send INVITE
    boolean status_ok = 
    	initiateOutgoingCall(call1, null, servletName, headers, null);
    assertTrue("Initiate outgoing call failed - " + call1.format(), status_ok);
    
    // (2) UA2 receive INVITE            
    call2.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("UA2 waits for incoming call - " 
    	+ call2.format(), call2);
    
    // get private header
    SipRequest invite = call2.getLastReceivedRequest();
    Header multihomedHeader = 
    	invite.getMessage().getHeader("Multihomed");
    boolean result = false;
    // check Via headers
    if (multihomedHeader != null) {
    	// get Via headers
	    ListIterator vias =  invite.getMessage().getHeaders(ViaHeader.NAME); 
	    assertNotNull(vias);
	    
	    while (vias.hasNext()) {
	    	ViaHeader via = (ViaHeader) vias.next();
	    	result = 
	    		MultihomedHeaderUtil.checkViaHeader(via, multihomedHeader.toString());
	    	if (result) break;
	    	
	    }
	    if (!result) {
    		fail("Outbound interface setting is invalid, " +
    				"Via header not equals the Multihomed header.");
    	}	
    } else {
    	fail("No Multihomed header in the INVITE.");
    }
    
    // get Record-Route headers
    RecordRouteHeader recordRoute = 
    	(RecordRouteHeader)invite.getMessage().getHeader(RecordRouteHeader.NAME);
    assertNotNull(recordRoute);
    // check Record-Route header
    result = 
    	MultihomedHeaderUtil.checkRecordRouteHeader(recordRoute, multihomedHeader.toString());
    if (!result) {
    	fail("Outbound interface setting is invalid, " +
    		"Record-Route header not equals the Multihomed header.");
    }
    
    // (3) UA2 send 180
    call2.sendIncomingCallResponse(Response.RINGING, null, -1);
    assertLastOperationSuccess("UA2 sends 180 - " + call2.format(), call2);
    
    // (4) UA1 receive 180 
    call1.waitOutgoingCallResponse(waitDuration);
    while (call1.getReturnCode() == Response.TRYING) {
    	call1.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess(
        "Subsequent response never received - " + call1.format(), call1);
    }
    
    call1.waitOutgoingCallResponse(waitDuration);
    assertEquals("UA1 should have received 180", Response.RINGING, 
    	call1.getLastReceivedResponse().getStatusCode());
    
    SipResponse resp = call1.getLastReceivedResponse(); // watch for TRYING
    int status_code = resp.getStatusCode();
    while (status_code != Response.RINGING) {
      assertFalse("Unexpected final response, status = "
        + status_code, status_code > 200);

      assertFalse("Got OK but no RINGING", status_code == Response.OK);

      call1.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess(
        "Subsequent response never received - " + call1.format(), call1);
      resp = call1.getLastReceivedResponse();
      status_code = resp.getStatusCode();
    }
    
    // (5) UA2 send 200
    Thread.sleep(3000);
    call2.sendIncomingCallResponse(Response.OK, null, -1);
    assertLastOperationSuccess("UA2 sends 200 - " + call2.format(), call2);
    
    // (6) UA1 receive 200 
    call1.waitOutgoingCallResponse(waitDuration);
    assertLastOperationSuccess("UA1 waits response error - " + call1.format(), call1);
    // check for OK response.
    assertEquals("Unexpected response received", Response.OK, call1
            .getReturnCode());
    
    // (7) UA1 send ACK
    call1.sendInviteOkAck();
    assertLastOperationSuccess("UA1 sends ACK - " + call1.format(), call1);
    
    // (8) UA2 receive ACK            
    call2.waitForAck(waitDuration);
    assertEquals(true, call2.getLastReceivedRequest().isAck());
    call2.listenForDisconnect();
    
    // (9) UA1 send BYE
    call1.disconnect();
    assertLastOperationSuccess("UA1 sends BYE - " + call1.format(), call1);
    
    // (10) UA2 receive BYE
    call2.waitForDisconnect(waitDuration);
    assertEquals(true, call2.getLastReceivedRequest().isBye());
    
    // (11) UA2 send 200 for BYE            
    call2.respondToDisconnect();
    assertLastOperationSuccess("UA2 sends 200 OK - " + call2.format(), call2);
    
    // (12) UA1 recieve 200 for BYE
    
    try {
      Thread.sleep(waitDuration);
    } catch (InterruptedException e) {
      
    }
    Response okForBye = (Response)call1.getLastReceivedResponse().getMessage();
    assertNotNull("200/BYE", okForBye);
    assertEquals("UA2 wait for 200/BYE - ", Response.OK,
        okForBye.getStatusCode());
    assertEquals("Unexpected response", Request.BYE,
        ((CSeqHeader)okForBye.getHeader(CSeqHeader.NAME)).getMethod());
	}
	
}
