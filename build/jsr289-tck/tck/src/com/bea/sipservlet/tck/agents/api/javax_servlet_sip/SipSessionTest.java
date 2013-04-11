/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * SipSessionTest is used to test the APIs of 
 * javax.servlet.sip.SipSession
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class SipSessionTest extends TestBase {
  private static Logger logger = Logger.getLogger(SipSessionTest.class);

  public SipSessionTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession1"},
      desc = "User agents can create a request.")
  public void testCreateRequest001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession1"},
      desc = "Throw IllegalArgumentException if method is ACK.")
  public void testCreateRequest101() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipSession();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession1"},
      desc = "Throw IllegalStateException if this SipSession has been "
      + "invalidated.")
  public void testCreateRequest102() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }  
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession2"},
      desc = "User agents can get the application session with which this "
        + "SipSession is associated.")
  public void testGetApplicationSession001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession3",
          "SipServlet:JAVADOC:SipSession18",
          "SipServlet:JAVADOC:SipSession19"},
      desc = "User agents can get/set/remove the object bound with the specified "
        + "name in this session, or null if no object is bound under the name.")
  public void testGetSetRemoveAttribute001() {
    assertSipMessage();
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipSession3" }, 
      desc = "Throw IllegalStateException if this method is called on an "
      + "invalidated session .")
  public void testGetAttribute101() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipSession3" }, 
      desc = "Throw NullPointerException if the name is null.")
  public void testGetAttribute102() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession18"},
      desc = "Throw IllegalStateException if this method is called on an "
      + "invalidated session .")
  public void testSetAttribute101() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession18"},
      desc = "Throw NullPointerException if the name is null.")
  public void testSetAttribute102() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession19"},
      desc = "Throw IllegalStateException if this method is called on an "
      + "invalidated session .")
  public void testRemoveAttribute101() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession4"},
      desc = "User agents can get an Enumeration over the String  objects containing "
        + "the names of all the objects bound to this session.")
  public void testGetAttributeNames001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession4"},
      desc = "Throw IllegalStateException if this method is called on an "
      + "invalidated session.")
  public void testGetAttributeNames101() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession5"},
      desc = "User agents can get the Call-ID for this SipSession.")
  public void testGetCallId001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession6"},
      desc = "User agents can get the time when this session was created, measured"
        + " in milliseconds since midnight January 1, 1970 GMT.")
  public void testGetCreationTime001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession7"},
      desc = "User agents can get a string containing the unique identifier assigned"
        + " to this session.")
  public void testGetId001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession8",
          "SipServlet:JAVADOC:SipSession21"},
      desc = "Returns true if the container will notify the application when this "
        + "SipSession is in the ready-to-invalidate state.")
  public void testGetSetInvalidateWhenReady001() throws InterruptedException {
    assertCommonCallBiWay();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession8"},
      desc = "Throw IllegalStateException if this method is called on an "
      + "invalidated session.")
  public void testGetInvalidateWhenReady101() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipSession21" }, 
      desc = "Throw IllegalStateException if this method is called on an "
      + "invalidated session.")
  public void testSetInvalidateWhenReady101() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }

  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession9"},
      desc = "User agents can get the last time the client sent a request associated "
        + "with this session, as the number of milliseconds since midnight January 1, 1970 GMT.")
  public void testGetLastAccessedTime001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession10"},
      desc = "User agents can get the Address identifying the local party.")
  public void testGetLocalParty001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession12"},
      desc = "User agents can get the Address identifying the remote party.")
  public void testGetRemoteParty001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession13"},
      desc = "User agents can get the current SIP dialog state, which is one of "
        + "INITIAL, EARLY, CONFIRMED, or TERMINATED.")
  public void testGetState001() throws InterruptedException {
    assertCommonCallBiWay();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipSession13" }, 
      desc = "Throw IllegalStateException if this method is called on an "
      + "invalidated session.")
  public void testGetState101() throws InterruptedException, ParseException,
      SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  /**
   * The call flow is:
   * 
   *                      UAC                            UAS
   *                       |                              |
   *                       |---------- (1)MESSAGE ------->|
   *                       |                   <Invalidate session>                        
   *                       |<--------  (2)MESSAGE  -------|
   *                  <Assert msg>                        |                                          
   *                       |----------  (3)200    ------->|
   *                       |                              |
   *
   */
  @AssertionIds(
      ids = { "SipServlet:JAVADOC:SipSession15" }, 
      desc = "User agents can invalidate this session and unbinds any "
      + "objects bound to it.")
  public void testInvalidate001() throws InterruptedException, ParseException {
    clientEntryLog();

    ua1.listenRequestMessage();
    // (1) send MESSAGE  
    HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
    Header ua1URIHeader = headerFactory.createHeader(TestConstants.PRIVATE_URI, ua1URI);
    List<Header> additionalHeaderList = new ArrayList<Header>();
    additionalHeaderList.add(ua1URIHeader);
    SipTransaction trans = sendMessage(ua1, null, null, "MESSAGE", 1, additionalHeaderList);
    assertNotNull("Fail to send MESSAGE out.", trans);

    // (2) receive 2nd Message
    Request req = waitIncomingMessage(ua1, waitDuration);
    assertNotNull("Did not receive MESSGE from server side.", req);

    // (3) send 200 OK
    boolean status = sendResponseForMessage(ua1, req, Response.OK);
    assertTrue("Send response to MESSAGE", status);
    
    Header failReasonHd = req.getHeader(TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(getFailReason(failReasonHd));
    }
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipSession15" }, 
      desc = "Throw IllegalStateException if this method is called on an "
      + "invalidated session.")
  public void testInvalidate101() throws InterruptedException, ParseException,
      SipException, InvalidArgumentException {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession16"},
      desc = "User agents can determine if this session is in a ready-to-invalidate state.")
  public void testIsReadyToInvalidate001() throws InterruptedException {
    assertCommonCallBiWay();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipSession16" }, 
      desc = "Throw IllegalStateException if this method is called on an "
      + "invalidated session.")
  public void testIsReadyToInvalidate101() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession17"},
      desc = "User agents can determine if this SipSession if valid.")
  public void testIsValid001() throws InterruptedException {
    assertCommonCallBiWay();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession20"},
      desc = "User agents can set the handler for this SipSession.")
  public void testSetHandler001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession20"},
      desc = "Throw ServletException if no servlet with the specified name "
      + "exists in this application .")
  public void testSetHandler101() throws InterruptedException, ParseException,
      SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession20"},
      desc = "Throw IllegalStateException if this method is called on an "
      + "invalidated session.")
  public void testSetHandler102() throws InterruptedException, ParseException,
      SipException, InvalidArgumentException {
    assertSipMessage();
  }  
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession22"},
      desc = "If multihoming is supported, user agents can select the outbound "
        + "interface to use when sending requests for this SipSession.")
  public void testSetOutboundInterface001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession22"},
      desc = "Throw IllegalStateException if this method is called on an "
      + "invalidated session .")
  public void testSetOutboundInterface101() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession22"},
      desc = "Throw NullPointerException  if the address is null.")
  public void testSetOutboundInterface102() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession23"},
      desc = "If multihoming is supported, user agents can select the outbound "
        + "interface and source port number to use when sending requests for this SipSession.")
  public void testSetOutboundInterface002() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession23"},
      desc = "Throw IllegalStateException if this method is called on an "
      + "invalidated session .")
  public void testSetOutboundInterface103() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession23"},
      desc = "Throw NullPointerException  if the address is null.")
  public void testSetOutboundInterface104() throws InterruptedException,
      ParseException, SipException, InvalidArgumentException {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipSession24"},
      desc = "Returns the ServletContext to which this session belongs") 
 public void  testGetServletContext001() throws InterruptedException,
 ParseException, SipException, InvalidArgumentException {
   assertSipSession();
 }
 
  /**
   * The call flow is:
   * 
   *          UAC                           UAS 
   *           |                              |                              
   *           |---------- (1)INVITE  ------->|                              
   *           |                              |                                
   *           |                         <UAS logic>                           
   *           |                              |  
   *           |<----------(2)200/INVITE -----|
   *           |                              |
   *         <Check>                          |
   *           |                              |
   *           | --------- (3)ACK      ------>|
   *           |                              |
   *           |                              |
   *           | --------- (4)BYE      ------>|
   */
  private void assertSipSession()
      throws InterruptedException, ParseException, SipException,
      InvalidArgumentException {
    clientEntryLog();
    SipCall a = ua1.createSipCall();
    
    // (1) send INVITE 
    boolean status = initiateOutgoingCall(a, null, null, null, null);
    assertTrue("Initiate outgoing call failed - " + a.format(), status);

    // (2) Receive and assert 200 response
    a.waitOutgoingCallResponse(waitDuration);
    while (a.getReturnCode() == Response.TRYING) {
      a.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("Subsequent response never received - "
          + a.format(), a);
    }
    assertResponseReceived("Unexpected response received", Response.OK, a);

    // (3) send ACK
    a.sendInviteOkAck();
    // (4) send BYE
    a.disconnect();
  }
  
  
  
  /**
   * The call flow is:
   * 
   *          UAC                           UAS 
   *           |                              |                              
   *           |---------- (1)INVITE  ------->|                              
   *           |<-------- (2) 100    ---------|                              
   *           |                         <UAS logic>                           
   *           |<-------- (3) 200OK    -------|                              
   *           |--------- (4) ACK    -------->|                              
   *           |---------  (5) BYE  --------->|                              
   *           |                         <UAS logic>                           
   *           |<--------  (6) 200OK   -------|                              
   *           |                         <UAS logic>                           
   *           |<--(7) MESSAGE(with result) --|                              
   *           |-------- (8) 200OK    ------->|                              
   *           |                              |                              
   *
   */
  private void assertCommonCallBiWay() throws InterruptedException{
    ua1.listenRequestMessage();
    // (1) - (6)
    assertCommonCall();
    
    // (7) A receive MESSAGE(with result)    
    RequestEvent event = ua1.waitRequest(waitDuration);
    assertNotNull(event);
    Request req = event.getRequest();
    
    // (8) A send 200/MESSAGE    
    try {
      ServerTransaction serverTrans = ua1.getParent().getSipProvider()
        .getNewServerTransaction(req);
      Response msg200Resp = createResponse(req, ua1, 200, ua1.generateNewTag());
      serverTrans.sendResponse(msg200Resp);
    } catch (Exception e) {
      logger.error("*** Exception when sending back 200/MESSAGE ***", e);
    }    

    //assertion
    assertMsg(req);   
  }

  
  /**
   * The call flow is:
   * 
   *          UAC                           UAS 
   *           |                              |                              
   *           |---------- (1)INVITE  ------->|                              
   *           |<-------- (2) 100    ---------|                              
   *           |                         <UAS logic>                           
   *           |<-------- (3) 200OK    -------|                              
   *           |--------- (4) ACK    -------->|                              
   *           |---------  (5) BYE  --------->|                              
   *           |                         <UAS logic>                           
   *           |<--------  (6) 200OK   -------|                              
   *           |                              |                              
   *
   */
  private void assertCommonCall() throws InterruptedException{
    clientEntryLog();
    
    ArrayList<Header> privateHeaders = getTckTestPrivateHeaders();    
    //initialize SipCall
    SipCall callerA = ua1.createSipCall();   
    Thread.sleep(10);

    // (1) A send INVITE
    boolean status = callerA.initiateOutgoingCall(ua1URI, serverURI, null, 
        privateHeaders, null, null);
    //initiateOutgoingCall(callerA, getUa2UriHeader(), null);
    assertTrue("Initiate outgoing call failed - " + callerA.format(), status);

    // (3) A receive 200/INVITE
    do {
      callerA.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("callerA wait response - " + callerA.format(), callerA);
      assertMsg(callerA.getLastReceivedResponse().getMessage());
    } while (callerA.getReturnCode() <= 199 && callerA.getReturnCode() > 0);
    Response resp200 = (Response) callerA.getLastReceivedResponse().getMessage();
    assertEquals("Unexpected default reason", Response.OK, resp200.getStatusCode());

    // (4) A send ACK
    callerA.sendInviteOkAck(privateHeaders, null, null);
    assertLastOperationSuccess("Fail to send ACK - " + callerA.format(), callerA);

    // (5) A send BYE
    callerA.disconnect(privateHeaders, null, null);
    assertLastOperationSuccess("callerA disconnect - " + callerA.format(), callerA);

    // (6) A receive 200/BYE    
    Thread.sleep(500);
    Response okForBye = (Response) callerA.getLastReceivedResponse().getMessage();
    assertNotNull("Default response reason not sent", okForBye);
    assertEquals("Unexpected default reason", Response.OK, okForBye.getStatusCode());
    assertEquals("Unexpected default reason", Request.BYE, ((CSeqHeader) okForBye
        .getHeader(CSeqHeader.NAME)).getMethod());
    assertMsg(okForBye);
  }
  
  private ArrayList<Header> getTckTestPrivateHeaders(){
    StackTraceElement stack = getBasePackageStack(new Exception().getStackTrace());
    String localServletName = getInterfaceName(stack.getClassName());
    String localMethodName = stack.getMethodName();
    
    HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
    Header appHeader;
    Header servletheader;
    Header methodHeader;
    Header ua1URIHeader;

    try {
    	appHeader = 
    		headerFactory.createHeader(TestConstants.APP_HEADER, TestConstants.APP_APITEST);
      servletheader = 
        headerFactory.createHeader(TestConstants.SERVLET_HEADER, localServletName);
      methodHeader = 
        headerFactory.createHeader(TestConstants.METHOD_HEADER, localMethodName);
      ua1URIHeader = 
        headerFactory.createHeader(TestConstants.PRIVATE_URI, ua1URI);
    } catch (ParseException e) {
      logger.error("*** ParseException when retrieving TCK private headers ***", e);
      throw new TckTestException(e);  
    }
    
    ArrayList<Header> additionalHeaderList = new ArrayList<Header>(1);
    additionalHeaderList.add(appHeader);
    additionalHeaderList.add(servletheader);
    additionalHeaderList.add(methodHeader);
    additionalHeaderList.add(ua1URIHeader);

    return additionalHeaderList;
  }  
   
  private void assertMsg(Message msg){
    Header failReasonHd = msg.getHeader(TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(getFailReason(failReasonHd));
    }
  }  
 
}
