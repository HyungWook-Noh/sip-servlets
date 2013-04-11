/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.  
 * 
 * ProxyBranchTest is used to test the APIs of javax.servlet.sip.ProxyBranch.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipRequest;
import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;


public class ProxyBranchTest extends TestBase {

  private static final Logger logger = Logger.getLogger(ProxyBranchTest.class);
  
  private static final String testName = "ProxyBranch";
  
  public ProxyBranchTest(String arg0) throws IOException {
    super(arg0); 
  }
    
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch1" }, 
      desc = "Test ProxyBranch.cancel().\\"
      + "Cancels this branch and all the child branches if recursion is "
      + "enabled.")
  public void testCancel001() throws ParseException, SipException,
      InvalidArgumentException {
    assertProxyBranchCancel("testCancel001");
  }
  
  /**     
   * 
   *      UA1                     Proxy                    UA2
   *       |                        |                       | 
   *       |                        |                       | 
   *       |------(1) INVITE------->|                       | 
   *       |                        |                       | 
   *       |                        |                       | 
   *       |                        |------(2) INVITE ----->| 
   *       |                        |                       | 
   *       |                        |                       |
   *       |                        |<------(3) 200---------|
   *       |                        |                       | 
   *       |                        |                       | 
   *       |                        |-------(4) CANCLE----->|
   *       |                        |                       |
   *       |                     <Add Header>               |
   *       |                        |                       | 
   *       |                        |                       |
   *       |<-----(5) 200 ----------|                       |
   *       |                        |                       |
   *  <Check Header value>          |                       |
   *       |                        |                       |
   *       |-------(6) ACK--------->|                       |
   *       |                        |                       |
   *       |-------(7) BYE--------->|                       |
   *       |                        |                       | 
   */       

   @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch1" }, 
       desc = "If the transaction has already been completed and "
       + "it has no child branches, thrown java.lang.IllegalStateException.")
   public void testCancel101() throws ParseException, SipException,
      InvalidArgumentException {
    clientEntryLog();

    SipCall a = ua1.createSipCall();
    SipCall b = ua2.createSipCall();
    b.listenForIncomingCall();
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();

    // (1) ua1 sends INVITE request message with specific header.
    List<Header> additionalHeaders = new ArrayList<Header>(1);
    additionalHeaders.add(header_factory.createHeader(TestConstants.UA2_URI,
        ua2URI));
    initiateOutgoingCall(a, null, null, additionalHeaders, null);
    assertLastOperationSuccess("a initiate call - " + a.format(), a);

    // (2) ua2 receives INVITE request message.
    b.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("b wait incoming call - " + b.format(), b);

    // (3) ua2 sends back 200/INVITE
    b.sendIncomingCallResponse(Response.OK, "200 for INVITE", waitDuration);
    assertLastOperationSuccess("b send 200 for INVITE - " + b.format(), b);

    // (5) ua1 receives 200 response and asserts the special header
    // "cancleWhenTransactionFinished"
    a.waitOutgoingCallResponse(waitDuration);
    while (a.getReturnCode() == Response.TRYING) {
      a.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - "
          + a.format(), a);
    }
    assertResponseReceived("Unexpected response received", Response.OK, a);    
    Header header = a.getLastReceivedResponse().getMessage().getHeader(
        "cancleWhenTransactionFinished");
    assertNotNull("The header of cancleWhenTransactionFinished is null", header);
    String sHeader = header.toString();
    String value = sHeader.substring(sHeader.indexOf(":") + 1).trim();
    assertEquals("cancleWhenTransactionFinished", value);

    // (6) ua1 sends ACK
    a.sendInviteOkAck();
    
    // (7) ua1 sends BYE
    a.disconnect();
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch2" }, 
      desc = "Test ProxyBranch.cancel(protocol,reasonCode,reasonText).\\"
      + "Cancels this branch and all the child branches if recursion is "
      + "enabled. This method provides a way to specify the reason for "
      + "cancelling this Proxy by including the appropriate Reason headers.")
  public void testCancel002() throws ParseException, SipException,
      InvalidArgumentException {
    assertProxyBranchCancel("testCancel002");
  }  
 
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch3" }, 
      desc = "Returns true if subsequent invocations of startProxy() will add "
      + "a Path header to the proxied request, false otherwise .")
  public void testGetAddToPath001() throws ParseException, SipException,
      InvalidArgumentException {
    assertProxyBranchPathHeader("testGetAddToPath001", true);
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch4" }, 
      desc = "If addToPath flag is set to true, the subsequent invocations of "
      + "startProxy() will add a Path header to the proxied request.")
  public void testGetPathURI001() throws ParseException, SipException,
      InvalidArgumentException {
    assertProxyBranchPathHeader("testGetPathURI001", true);
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch4" }, 
      desc = "Throw IllegalStateException,If addToPath flag is set to false.")
  public void testGetPathURI101() throws ParseException, SipException,
      InvalidArgumentException {
    assertProxyBranchPathHeader("testGetPathURI101", false);
  }   
    
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch5" }, 
      desc = "Returns the associated proxy with this branch.")
  public void testGetProxy001() throws ParseException, SipException,
      InvalidArgumentException {    
    assertProxyBranchSimpleCase("testGetProxy001", false, false);
  }
    
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch6" }, 
      desc = "Returns the current value of the search timeout associated with "
      + "this ProxyBranch object.")
  public void testGetProxyBranchTimeout001() throws ParseException,
      SipException, InvalidArgumentException {    
    assertProxyBranchSimpleCase("testGetProxyBranchTimeout001", false, false);
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch7" }, 
      desc = "Returns true if subsequent invocations of proxyTo(URI) will add "
      + "a Record-Route header to the proxied request, false otherwise.")
  public void testGetRecordRoute001() throws ParseException, SipException,
      InvalidArgumentException {    
    assertProxyBranchSimpleCase("testGetRecordRoute001", true, false);
  } 
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch8" }, 
      desc = "if record-routing flag is set to true, a Record-Route header will "
      + "be added to the proxied request.")
  public void testGetRecordRouteURI001() throws ParseException, SipException,
      InvalidArgumentException {    
    assertProxyBranchSimpleCase("testGetRecordRouteURI001", true, false);
  } 
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch8" }, 
      desc = "Throws IllegalStateException - if record-routing is not enabled.")
  public void testGetRecordRouteURI101() throws ParseException, SipException,
      InvalidArgumentException {    
    assertProxyBranchSimpleCase("testGetRecordRouteURI001", false, false);
  } 
  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch9",
                        "SipServlet:JAVADOC:ProxyBranch19" }, 
      desc = "Returns true if this proxy branch object is set to recurse, "
      + "or false otherwise.")
  public void testGetSetRecurse001() throws ParseException, SipException,
      InvalidArgumentException {    
    assertProxyBranchSimpleCase("testGetSetRecurse001", false, false);
  }
  
   
  /**     
   * 
   *      UA1           Proxy           UA2            UA3
   *       |              |              |              |
   *       |(1) INVITE    |              |              |
   *       |------------->|              |              |
   *       |              |              |              |
   *       |              |(2) INVITE    |              |
   *       |              |------------->|              |
   *       |              |              |              |
   *       |              |(3) 301       |              |
   *       |              |<-------------|              |
   *       |              |              |              |
   *       |              |(4) ACK       |              |
   *       |              |------------->|              |
   *       |              |              |              |
   *       |              |(5) INVITE    |              |
   *       |              |---------------------------->|
   *       |              |              |              |
   *       |              |(6) 200       |              |
   *       |              |<----------------------------|
   *       |              |              |              |
   *       |(7) 200       |              |              |
   *       |<-------------|              |              |
   *       |              |              |              |
   *     <Assert point>   |              |              |
   *       |              |              |              |
   *       |(8) ACK       |              |              |
   *       |------------->|              |              |
   *       |              |              |              |
   *       |              |(9) ACK       |              |
   *       |              |---------------------------->|
   *       |              |              |              |
   *       |(10) BYE      |              |              |
   *       |------------->|              |              |
   *       |              |              |              |
   * @throws InvalidArgumentException 
   * @throws SipException 
   *           
   */   
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch10" }, 
      desc = "Receipt of a 3xx class redirect response on a branch can result "
      + "in recursed branches if the proxy or the branch has recursion enabled."
      + " This can result in several levels of recursed branches in a tree "
      + "like fashion. This method returns the top level branches directly."
      + "below this ProxyBranch . ")
  public void testgetRecursedProxyBranches001() throws ParseException,
      SipException, InvalidArgumentException {
    clientEntryLog();
    
    SipCall a = ua1.createSipCall();
    SipCall b = ua2.createSipCall();
    SipCall c = ua3.createSipCall();
    b.listenForIncomingCall();
    c.listenForIncomingCall();
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();

    // (1) ua1 sends INVITE request message with specific header.
    List<Header> additionalHeaders = new ArrayList<Header>(1);
    additionalHeaders.add(header_factory.createHeader(TestConstants.UA2_URI,
        ua2URI));
    initiateOutgoingCall(a, null, null, additionalHeaders, null);
    assertLastOperationSuccess("a initiate call - " + a.format(), a);

    // (2) ua2 receives the INVITE request message.
    b.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("b wait incoming call - " + b.format(), b);

    // (3) ua2 sends back 301/INVITE
    ArrayList<Header> addHeaders = new ArrayList<Header>(1);
    AddressFactory addr_factory = ua1.getParent().getAddressFactory();
    Address contact_address = addr_factory.createAddress(addr_factory
        .createURI(ua3URI));
    ContactHeader contactHeader = header_factory
        .createContactHeader(contact_address);
    addHeaders.add(contactHeader);
    b.sendIncomingCallResponse(Response.MOVED_PERMANENTLY, "301 for INVITE",
        waitDuration, addHeaders, addHeaders, null);
    assertLastOperationSuccess("b send 301 for INVITE - " + b.format(), b);

    // (5) ua3 receives INVITE
    c.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("c wait incoming call - " + c.format(), c);

    // (6) ua3 sends back 200/INVITE
    c.sendIncomingCallResponse(Response.OK, "200 for INVITE", waitDuration);
    assertLastOperationSuccess("c send 200 for INVITE - " + c.format(), c);

    // (7) ua1 receives 200/INVITE response and asserts special header
    a.waitOutgoingCallResponse(waitDuration);
    while (a.getReturnCode() == Response.TRYING) {
      a.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - "
          + a.format(), a);
    }
    assertResponseReceived("Unexpected response received", Response.OK, a);
    SipResponse response = a.getLastReceivedResponse();

    Header respHd = response.getMessage().getHeader("getRecursedOK");
    if (respHd == null) {
      fail("Fail to get \"ResponseHeader\" header from 200/INVITE.");
    } else {
      String header = respHd.toString();
      String value = header.substring(header.indexOf(":") + 1).trim();
      if (!"getRecursedOK".equals(value)) {
        fail("ProxyBranch.getRecursedProxyBranches() failed.");
      }
    }

    // (8) ua1 sends ACK
    a.sendInviteOkAck();

    // (10) ua1 sends BYE
    a.disconnect();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch11" }, 
      desc = "Return the request associated with this branch.")
  public void testGetRequest001() throws ParseException, SipException,
      InvalidArgumentException {
    assertProxyBranchSimpleCase("testGetRequest001", false, false);
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch12" }, 
      desc = "Return the last SipServletResponse received, "
      + "or null if no response has been received so far.")
  public void testGetResponse001() throws ParseException, SipException,
      InvalidArgumentException {
    assertProxyBranchSimpleCase("testGetResponse001", false, true);
  }
  
 @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch13" }, 
     desc = "This method tells if the given branch has been started yet or not. "
      + "The branches created as a result of proxyTo are always started "
      + "on creation.")
  public void testIsStarted001() throws ParseException, SipException,
      InvalidArgumentException {
    assertProxyBranchSimpleCase("testIsStarted001", false, false);
  } 
    
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch14" }, 
      desc = "Specify whether the subsequent invocations of startProxy() will "
      + "add a Path header to the proxied request.")
  public void testSetAddToPath001() throws ParseException, SipException,
      InvalidArgumentException {
    assertProxyBranchPathHeader("testSetAddToPath001", true);
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch15" }, 
      desc = "If multihoming is supported, then this method can be used to "
      + "select the outbound interface and source port number when forwarding "
      + "requests for this proxy branch. The specified address must be "
      + "the address of one of the configured outbound interfaces. "
      + "The set of SipURI objects which represent the supported outbound "
      + "interfaces can be obtained from the servlet context attribute "
      + "named javax.servlet.sip.outboundInterfaces.")
  public void testSetOutboundInterface001() throws ParseException,
      SipException, InvalidArgumentException {
    assertProxyBranchSimpleCase("testSetOutboundInterface001", false, false);
  }

  
  /**
   * The call flow         
   * 
   * 
   *     UA1                        PROXY                         UA2
   *      |                           |                            |
   *      |------ (1)MESSAGE  ------->|                            |
   *      |                    <PROXY logic>                       |                                     
   *      |                           |------- (2)new MESSAGE ---->|         
   *      |                           |                      <UAS assertion>
   *      |                           |                            |
   *
   */
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch15" }, 
      desc = "Throw IllegalStateException if this method is called "
      + "on an invalidated session.")
  public void testSetOutboundInterface101() throws ParseException,
      SipException, InvalidArgumentException {
    clientEntryLog();
    // Build the MESSAGE req for UA1
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
    Request ua1SendMessageReq = assembleRequest(Request.MESSAGE, testName,
        "testSetOutboundInterface101", TestConstants.SERVER_MODE_UA, 1);
    ua1SendMessageReq.addHeader(header_factory.createHeader(
        TestConstants.UA2_URI, ua2URI));
    // ua2 listen to incoming message
    ua2.listenRequestMessage();

    // (1) ua1 sends the MESSAGE message.
    SipTransaction transUA1 = ua1.sendRequestWithTransaction(ua1SendMessageReq,
        true, null);
    assertNotNull(ua1.format(), transUA1);
    logger.debug("---UA1 send MESSAGE req is:" + ua1SendMessageReq + "---");

    // (2) ua2 receives and asserts MESSAGE message.
    // Success if UA2 recevie MESSAGE message, failed otherwise.
    RequestEvent eventUA2 = ua2.waitRequest(waitDuration);
    assertNotNull(eventUA2);
    Request ua2ReceivedMessageReq = eventUA2.getRequest();
    if (!(ua2ReceivedMessageReq != null && Request.MESSAGE
        .equals(ua2ReceivedMessageReq.getMethod()))) {
      fail("UA2 did not receive the REGISTER request message from UA1.");
    }
    logger.debug("---UA2 receive REGISTER req is:" + ua2ReceivedMessageReq
        + "---");
  }
  


  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch15" }, 
      desc = "Throw NullPointerException. if null address.")
  public void testSetOutboundInterface102() throws ParseException,
      SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch16" }, 
      desc = "If multihoming is supported, then this method can be used to "
      + "select the outbound interface to use when sending requests for "
      + "this proxy branch. The specified address must be the address of "
      + "one of the configured outbound interfaces. The set of SipURI objects "
      + "which represent the supported outbound interfaces can be obtained "
      + "from the servlet context attribute named "
      + "javax.servlet.sip.outboundInterfaces.")
  public void testSetOutboundInterface002() throws ParseException,
      SipException, InvalidArgumentException {
    assertProxyBranchSimpleCase("testSetOutboundInterface002", false, false);
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch16" },
      desc = "Throw NullPointerException, if address is null.")
  public void testSetOutboundInterface103() throws ParseException,
      SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch17" }, 
      desc = "Sets the search timeout value for this ProxyBranch object.")
  public void testSetProxyBranchTimeout001() throws ParseException,
      SipException, InvalidArgumentException {
    assertProxyBranchSimpleCase("testSetProxyBranchTimeout001", false, false);
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch17" }, 
      desc = "Throws IllegalArgumentException if this value is negative.")
  public void testSetProxyBranchTimeout101() throws ParseException,
      SipException, InvalidArgumentException {
    assertSipMessage();
  }
  

  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch18" }, 
      desc = "Specifies whether this branch should include a Record-Route "
      + "header for this servlet engine or not. if true the engine will "
      + "record-route, otherwise it won't.")
  public void testSetRecordRoute001() throws ParseException, SipException,
      InvalidArgumentException {
    assertProxyBranchSimpleCase("testSetRecordRoute001", true, false);
  } 
  
  
  /**
   * The call flow to test Simple ProxyBranch test cases with assert the
   * failed reason.
   * 
   *     UA1                        PROXY                         UA2
   *      |                           |                            |
   *      |------ (1)INVITE  -------->|                            |
   *      |                           |                            |                               
   *      |                           |------- (2)INVITE --------->|
   *      |                           |                            |      
   *      |                    <PROXY logic>                       |
   *      |                           |                            |
   *      |                           |--------(3)MESSAGE--------->|
   *      |                           |                            |         
   *      |                           |                      <UAS assertion>
   *      |                           |                            |
   *      |                           |<------ (4)200/MESSAGE -----|
   *      |                           |                            |       
   *      |                           |<------ (5)200/INVITE ------|
   *      |<----- (6)200/INVITE  -----|                            |
   *      |                           |                            |
   *      |-------(7)ACK------------->|                            |
   *      |                           |                            |
   *      |                           |---------(8)ACK------------>|
   *      |                           |                            |
   *      |-------(9)BYE------------->|                            |
   *      |                           |                            |
   *      |                           |---------(10)BYE----------->|
   *      |                           |                            |       
   *
   */
  @AssertionIds(ids = { "SipServlet:JAVADOC:ProxyBranch18" }, 
      desc = "Throws IllegalStateException - if the proxy has already been "
      + "started.")
  public void testSetRecordRoute101() throws ParseException, SipException,
      InvalidArgumentException {
    clientEntryLog();
    SipCall a = ua1.createSipCall();
    SipCall b = ua2.createSipCall();
    b.listenForIncomingCall();

    // (1) ua1 sends INVITE request message with specific header.
    List<Header> additionalHeaders = new ArrayList<Header>(1);
    additionalHeaders.add(ua1.getParent().getHeaderFactory().createHeader(
        TestConstants.UA2_URI, ua2URI));
    initiateOutgoingCall(a, null, null, additionalHeaders, null);
    assertLastOperationSuccess("a initiate call - " + a.format(), a);

    // (2) ua2 receives the INVITE request message.
    b.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("b wait incoming call - " + b.format(), b);
    SipRequest received_invite = b.getLastReceivedRequest();
    assertNotNull(received_invite);
    assertEquals("Unexpected request", Request.INVITE,
        ((Request) received_invite.getMessage()).getMethod());

    // (3) ua2 receives the MESSAGE request message.
    Request request = waitIncomingMessage(ua2, waitDuration);
    assertNotNull(request);
    assertEquals("Unexpected request", Request.MESSAGE, request.getMethod());

    // (4) ua2 sends back 200/MESSAGE
    ServerTransaction serverTrans = ua2.getParent().getSipProvider()
        .getNewServerTransaction(request);
    String toTag = ua2.generateNewTag();
    Response msg200Resp = createResponse(request, ua2, Response.OK, toTag);
    serverTrans.sendResponse(msg200Resp);

    // (5) ua2 sends back 200/INVITE
    b.sendIncomingCallResponse(Response.OK, "200 for INVITE", waitDuration);
    assertLastOperationSuccess("b send 200 for INVITE - " + b.format(), b);

    // (6) ua1 receives 200/INVITE response
    a.waitOutgoingCallResponse(waitDuration);
    while (a.getReturnCode() == Response.TRYING) {
      a.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - "
          + a.format(), a);
    }
    assertResponseReceived("Unexpected response received", Response.OK, a);

    // (7) ua1 sends ACK.
    a.sendInviteOkAck();

    // (9) ua1 sends BYE.
    a.disconnect();
  } 

  
  /**
   * The call flow to test: ProxyBranch.getAddToPath()
   *                        ProxyBranch.setAddToPath(boolean p) 
   *                        ProxyBranch.getPathURI()          
   * 
   * 
   *     UA1                        PROXY                   UA2(Registrar)
   *      |                           |                            |
   *      |------ (1)REGISTER ------->|                            |
   *      |                    <PROXY logic>                       |                                
   *      |                           |------- (2)REGISTER ------->|         
   *      |                           |                      <UAS assertion>       
   *      |                           |<------ (3)200/REGISTER ----|
   *      |<----- (4)200/REGISTER --->|                            |
   *      |                           |                            |
   *
   */
  private void assertProxyBranchPathHeader(String testMethod,
      boolean assertPathHeader) throws ParseException, SipException,
      InvalidArgumentException {
    
    clientEntryLog();

    // Build the REGISTER request for UA1
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
    Request ua1SendRegisterReq = assembleRequest(Request.REGISTER, testName,
        testMethod, TestConstants.SERVER_MODE_UA, 1);
    ua1SendRegisterReq.addHeader(header_factory.createHeader(
        TestConstants.UA2_URI, ua2URI));

    // ua2 listen to incoming message
    ua2.listenRequestMessage();

    // (1) ua1 sends the REGISTER message.
    SipTransaction transUA1 = ua1.sendRequestWithTransaction(
        ua1SendRegisterReq, true, null);
    assertNotNull(ua1.format(), transUA1);
    logger.debug("---UA1 send REGISTER req is:" + ua1SendRegisterReq + "---");

    // (2) ua2 receives and asserts REGISTER message.
    RequestEvent eventUA2 = ua2.waitRequest(waitDuration);    
    assertNotNull(eventUA2);
    Request ua2ReceivedRegisterReq = eventUA2.getRequest();
    if (!(ua2ReceivedRegisterReq != null && Request.REGISTER
        .equals(ua2ReceivedRegisterReq.getMethod()))) {
      fail("UA2 did not receive the REGISTER request message from UA1.");
    }
    logger.debug("---UA2 receive REGISTER req is:" + ua2ReceivedRegisterReq
        + "---");
    // assertion of request
    if (assertPathHeader) {
      Header pathHd = ua2ReceivedRegisterReq.getHeader("Path");
      if (pathHd == null) {
        fail("Fail to get \"Path\" header from REGISTER.");
      }
    }

    Header failReasonHd = ua2ReceivedRegisterReq
        .getHeader(TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(getFailReason(failReasonHd));
    }

    // (3)ua2 send back 200/REGISTER
    ServerTransaction serverTrans2RegisterUA2 = ua2.getParent()
        .getSipProvider().getNewServerTransaction(ua2ReceivedRegisterReq);
    String toTag = ua2.generateNewTag();
    Response reg200Resp = createResponse(ua2ReceivedRegisterReq, ua2,
        Response.OK, toTag);
    serverTrans2RegisterUA2.sendResponse(reg200Resp);
    logger.debug("---UA2 send 200/REGISTER resp is:" + reg200Resp + "---");

    // (4) ua1 receives 200/REGISTER response
    EventObject event = ua1.waitResponse(transUA1, waitDuration);
    assertNotNull(event);
    // ua1 assert received 200/REGISTER response
    if (event instanceof ResponseEvent) {
      ResponseEvent responseEvent = filterEvent(ua1, transUA1, event);
      Response response = responseEvent.getResponse();
      if (response.getStatusCode() != Response.OK) {
        fail("UA1 did not receive 200/REGISTER response from UA2.");
      }
      logger.debug("---UA1 receive 200/REGISTER resp is:" + response + "---");
    } else {
      fail("The event for UA1 receive 200/REGISTER is null.");
    }    
  }
  
  

  
  /**
   * The call flow to test Simple ProxyBranch test cases with assert the
   * failed reason.
   * 
   *     UA1                        PROXY                         UA2
   *      |                           |                            |
   *      |------ (1)INVITE  -------->|                            |
   *      |                    <PROXY logic>                       |                                
   *      |                           |------- (2)INVITE --------->|         
   *      |                           |                      <UAS assertion>       
   *      |                           |<------ (3)200/INVITE ------|
   *      |<----- (4)200/INVITE  -----|                            |
   *      |                           |                            |
   *      |                           |                            |
   *      |-------(5)ACK------------->|                            |
   *      |                           |                            |
   *      |                           |---------(6)ACK------------>|
   *      |                           |                            |
   *      |-------(7)BYE------------->|                            |
   *      |                           |                            |
   *      |                           |---------(8)BYE------------>|
   *      |                           |                            |       
   *
   */
  private void assertProxyBranchSimpleCase(String testMethod,
      boolean assertRecordRouteHeader, boolean assertResponseHeader)
      throws ParseException, SipException, InvalidArgumentException {
    clientEntryLog();
    
    SipCall a = ua1.createSipCall();
    SipCall b = ua2.createSipCall();
    b.listenForIncomingCall();

    // (1) ua1 sends INVITE request message with specific header.
    List<Header> additionalHeaders = new ArrayList<Header>(1);
    additionalHeaders.add(ua1.getParent().getHeaderFactory().createHeader(
        TestConstants.UA2_URI, ua2URI));
    initiateOutgoingCall(a, null, null, additionalHeaders, null);
    assertLastOperationSuccess("a initiate call - " + a.format(), a);

    // (2) ua2 receives the INVITE request message and asserts special header.
    b.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("b wait incoming call - " + b.format(), b);
    SipRequest received_invite = b.getLastReceivedRequest();
    assertNotNull(received_invite);
    assertEquals("Unexpected request", Request.INVITE,
        ((Request) received_invite.getMessage()).getMethod());
    if (assertRecordRouteHeader) {
      Header recordRouteHd = received_invite.getMessage().getHeader(
          "Record-Route");
      if (recordRouteHd == null) {
        fail("Fail to get \"Record-Route\" header from INVITE.");
      }
    }
    Header failReasonHd = received_invite.getMessage().getHeader(
        TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(getFailReason(failReasonHd));
    }

    // (3) ua2 sends back 200/INVITE
    b.sendIncomingCallResponse(Response.OK, "200 for INVITE", waitDuration);
    assertLastOperationSuccess("b send 200 for INVITE - " + b.format(), b);

    // (4) ua1 receives 200 response
    a.waitOutgoingCallResponse(waitDuration);
    while (a.getReturnCode() == Response.TRYING) {
      a.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - "
          + a.format(), a);
    }
    assertResponseReceived("Unexpected response received", Response.OK, a);
    SipResponse response = a.getLastReceivedResponse();
    if (assertResponseHeader) {
      Header respHd = response.getMessage().getHeader("ResponseHeader");
      if (respHd == null) {
        fail("Fail to get \"ResponseHeader\" header from 200/INVITE.");
      }
    }

    // (5) ua1 sends ACK.
    a.sendInviteOkAck();

    // (7) ua1 sends BYE.
    a.disconnect();
  }
  
  /**
   * This call flow is used to test 
   * (1) ProxyBranch.cancel()
   * (2) ProxyBranch.cancel(java.lang.String[] protocol,
   *                        int[] reasonCode,
   *                        java.lang.String[] reasonText)
   * 
   *    UA1                         PROXY                           UA2
   *     |                            |                              |
   *     |-------- (1)INVITE  ------->|                              |
   *     |                            |                              |                                
   *     |                            |---------- (2)INVITE  ------->|         
   *     |                            |                              |       
   *     |                            |<--------- (3)180/INVITE------|
   *     |                            |                              |    
   *     |<--------(4)180/INVITE------|                              |
   *     |                            |                              | 
   *     |                            |---------- (5)CANCEL  ------->|         
   *     |                            |                              |
   *     |                            |                  <Assert received CANCEL>
   *     |                            |                              |        
   *     |                            |<--------- (6)200(CANCLE)-----|
   *     |                            |                              |
   *     |                            |<--------- (7)487/INVITE------|
   *     |                            |                              | 
   *     |                            |                              |
   *     |<---------(8)408/INVITE-----|                              |
   *     |                            |                              |
   *     |----------(9) ACK---------->|                              |
   *     |                            |                              |
   *     |                            |-----------(10)ACK----------  |
   *     |                            |                              |
   *     |----------(11)BYE---------->|                              |
   *     |                            |                              |
   *     |                            |------------(12)BYE---------->|
   *     |                            |                              |
   */
  private void assertProxyBranchCancel(String testMethod)
      throws ParseException, SipException, InvalidArgumentException {
    clientEntryLog();

    SipCall a = ua1.createSipCall();
    SipCall b = ua2.createSipCall();
    b.listenForIncomingCall();

    // (1) ua1 sends INVITE request message with specific header.
    List<Header> additionalHeaders = new ArrayList<Header>(1);
    additionalHeaders.add(ua1.getParent().getHeaderFactory().createHeader(
        TestConstants.UA2_URI, ua2URI));
    initiateOutgoingCall(a, null, null, additionalHeaders, null);
    assertLastOperationSuccess("a initiate call - " + a.format(), a);

    //(2) ua2 receives the INVITE request message.
    b.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("b wait incoming call - " + b.format(), b);

    //(3) ua2 sends back 180/INVITE
    b
        .sendIncomingCallResponse(Response.RINGING, "180 for INVITE",
            waitDuration);
    assertLastOperationSuccess("b send 180 for INVITE - " + b.format(), b);

    // (4) ua1 receives 180 response 
    a.waitOutgoingCallResponse(waitDuration);
    while (a.getReturnCode() == Response.TRYING) {
      a.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - "
          + a.format(), a);
    }
    assertResponseReceived("Unexpected response received", Response.RINGING, a);

    // (5) ua2 receives CANCEL
    b.waitForCancel(waitDuration);
    assertLastOperationSuccess("b receive CANCLE - " + b.format(), b);
    SipRequest received_cancel = b.getLastReceivedRequest();
    assertNotNull(received_cancel);
    assertEquals("Unexpected request", Request.CANCEL,
        ((Request) received_cancel.getMessage()).getMethod());

    // (6) ua2 sends 200/CANCEL
    b.sendResponseToLastReceivedRequest(Response.OK, "CANCEL Answer", 0);
    assertLastOperationSuccess("b send 200/CANCEL - " + b.format(), b);

    // (7) ua2 sends 487/INVITE
    b.sendIncomingCallResponse(Response.REQUEST_TERMINATED,
        "Answer 487- request terminated", 0);
    assertLastOperationSuccess("b send 487 - " + b.format(), b);

    // (8) ua1 receives 408/INVITE
    a.waitOutgoingCallResponse(waitDuration);
    while (a.getReturnCode() == Response.TRYING) {
      a.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - "
          + a.format(), a);
    }
    assertResponseReceived("Unexpected response received",
        Response.REQUEST_TIMEOUT, a);

    //(9) UA1 send ACK.
    a.sendInviteOkAck();

    //(11) UA1 send BYE
    a.disconnect();
  }  


}
