/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * ProxyTest is used to test the APIs of 
 * javax.servlet.sip.Proxy
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.ListIterator;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipRequest;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.utils.TestUtil;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class ProxyTest extends TestBase {
  private static Logger logger = Logger.getLogger(ProxyTest.class);

  public ProxyTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }
  
  
  /**
   * The call flow is:
   * 
   *          UA1                           PROXY                           UA2
   *           |                              |                              |
   *           |---------- (1)INVITE  ------->|                              |
   *           |                        <PROXY logic>                        |                                
   *           |                              |---------- (2)INVITE  ------->|         
   *           |                              |                        <UAS assertion>       
   *           |                              |<--------  (3)180      -------|
   *           |                        <PROXY Cancel>                       |                                
   *           |                              |---------- (4)CANCEL  ------->|         
   *           |                              |                          Assertion       
   *           |                              |<--------  (5)200      -------|
   *           |                              |                              |
   *
   */
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy1"},
      desc = "User agents can cancel this proxy transaction and any of its child "
        + "branches if recursion was enabled.")
  public void testCancel001() {
    assertSipInviteProxyCancel(null, null, null);
  }

  /**
   * The call flow is:
   * 
   *          UA1                           PROXY                           UA2
   *           |                              |                              |
   *           |---------- (1)INVITE  ------->|                              |
   *           |                        <PROXY logic>                        |                                
   *           |                              |---------- (2)INVITE  ------->|         
   *           |                              |                        <UAS assertion>       
   *           |                              |<--------  (3)180      -------|
   *           |                        <PROXY Cancel>                       |                                
   *           |                              |-- (4)CANCEL(with reason)  -->|         
   *           |                              |                          Assertion       
   *           |                              |<--------  (5)200      -------|
   *           |                              |                              |
   *
   */
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy2"},
      desc = "User agents can cancel this proxy transaction with a reason.")
  public void testCancel002() {
    assertSipInviteProxyCancel(null, null, new SipRequestAssertion(){
      public void assertRequest(RequestEvent reqEvent){
        Request req = reqEvent.getRequest();
        Header reasonHd = req.getHeader("Reason");
        if(reasonHd == null) fail("Fail to get \"Reason\" header from CANCEL.");
        String reasonHdStr = reasonHd.toString();
        if(!reasonHdStr.contains("sip") 
            || !reasonHdStr.contains("503") 
            || !reasonHdStr.contains("cancel reason")){
          fail("Fail to get correct \"Reason\" header from CANCEL.");
        }
      }
    });
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy3",
          "SipServlet:JAVADOC:Proxy9",
          "SipServlet:JAVADOC:Proxy10",
          "SipServlet:JAVADOC:Proxy31"},
      desc = "User agents can create and get ProxyBranch objects, and start proxy.")
  public void testGetCreateProxyBranches001() {
    assertSipMessageProxy(null, null, null, 1, new SipRequestAssertionProxy());
  }
  
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy1"},
      desc = "User agents can cancel this proxy transaction and any of its child "
        + "branches if recursion was enabled,test throw IllegalStateException")
  public void testCancel101() {
    assertSipInviteProxyOK();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy31"},
      desc = "User agents can create and get ProxyBranch objects, " +
      		"and start proxy,test throw IllegalStateException")
  public void testStartProxy101() {
    assertSipInviteProxyOK();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy3"},
      desc = "User agents can create and get ProxyBranch objects," +
      		" and start proxy,test throw IllegalArgumentException")
  public void testCreateProxyBranches101() {
    assertSipProxy(false);
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy8"},
      desc = "User agents can test getPathURI in proxy," +
      		"test throw IllegalStateException")
  public void testGetPathURI101() {
    assertSipProxy(true);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy12"},
      desc = "User agents can create proxy and get RecordRoute URI," +
      		"test throw IllegalStateException")
  public void testGetRecordRouteURI101() {
    assertSipProxy(true);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy19"},
      desc = "User agents can proxy a SIP request to the specified destination." +
      		"test throw NullPointerException")
  public void testProxyTo101() {
    assertSipProxy(true);
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy19"},
      desc = "User agents can proxy a SIP request to the specified destination." +
      		"test throw IllegalStateException")
  public void testProxyTo102() {
    assertSipInviteProxyOK();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy19"},
      desc = "User agents can proxy a SIP request to the specified destination." +
      		"test throw IllegalArgumentException")
  public void testProxyTo103() {
    assertSipProxy(false);
  }
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy18"},
      desc = "User agents can proxy a SIP request to the specified set of destinations." +
  "test throw NullPointerException")
  public void testProxyTo104() {
    assertSipProxy(true);
  }


  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy18"},
      desc = "User agents can proxy a SIP request to the specified set of destinations." +
  "test throw IllegalStateException")
  public void testProxyTo105() {
    assertSipInviteProxyOK();
  }  
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy18"},
      desc = "User agents can proxy a SIP request to the specified set of destinations." +
  "test throw IllegalArgumentException")
  public void testProxyTo106() {
    assertSipProxy(false);
  }
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy22"},
      desc = "If multihoming is supported, user agents can select the outbound interface "
      + "to use when sending requests for proxy branches. " +
       "test throw NullPointerException in set InetAddress")
  public void testSetOutboundInterface101() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy23"},
      desc = "If multihoming is supported, user agents can select the outbound interface "
        + "to use when sending requests for proxy branches." +
    "test throw NullPointerException in set InetSocketAddress")
  public void testSetOutboundInterface102() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy25"},
      desc = "User agents can set current value of the overall proxy timeout value." +
      		"test throw IllegalArgumentException")
  public void testSetProxyTimeout101() {
    assertSipProxy(true);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy26"},
      desc = "User agents can set the SipURI that the application can use to add "
        + "parameters to the Record-Route header.test throw IllegalStateException")
  public void testSetRecordRoute101() {
    assertSipInviteProxy180OK();
  }

  
  /**
   * The call flow is:
   * 
   *          UA1                           PROXY                      UA2(Registrar)
   *           |                              |                              |
   *           |--------- (1)REGISTER ------->|                              |
   *           |                        <PROXY logic>                        |                                
   *           |                              |--------- (2)REGISTER ------->|         
   *           |                              |                        <UAS assertion>       
   *           |                              |<------ (3)200/REGISTER ------|
   *           |<----- (4)200/REGISTER ------>|                              |
   *           |                              |                              |
   *
   */
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy4",
          "SipServlet:JAVADOC:Proxy8",
          "SipServlet:JAVADOC:Proxy20"},
      desc = "User agents can determine if subsequent invocations of proxyTo(URI) "
        + "or startProxy() will add a Path header to the proxied request.")
  public void testGetSetAddToPath001() throws ParseException {
    clientEntryLog();

    //create REGISTER request
    StackTraceElement stack = getBasePackageStack(new Exception().getStackTrace());
    String localServletName = getInterfaceName(stack.getClassName());
    String localMethodName = stack.getMethodName();
    Request registerReqUA1 = assembleRequest(Request.REGISTER, localServletName,
        localMethodName, TestConstants.SERVER_MODE_UA, 1);
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
    registerReqUA1.addHeader(header_factory.createHeader(TestConstants.UA2_URI,ua2URI));
    // UA2 listen to incoming message            
    ua2.listenRequestMessage();

    /////////////////////////// begin call flow ////////////////////////////
    // (1) UA1 Send REGISTER  
    SipTransaction transUA1 = ua1.sendRequestWithTransaction(registerReqUA1, false, null);
    assertNotNull(ua1.format(), transUA1);

    // (2) UA2 Receive REGISTER 
    RequestEvent eventUA2 = ua2.waitRequest(waitDuration);
    assertNotNull(eventUA2);
    Request registerReqUA2 = eventUA2.getRequest();
    
    //assertion of request
    Header pathHd = registerReqUA2.getHeader("Path");
    if(pathHd == null) fail("Fail to get \"Path\" header from REGISTER.");
    Header failReasonHd = registerReqUA2.getHeader(TestConstants.TEST_FAIL_REASON);
    if(failReasonHd != null) {
      fail(getFailReason(failReasonHd));   
    }

    // (3) UA2 send back 200/REGISTER
    try {
      ServerTransaction serverTransUA2 = ua2.getParent().getSipProvider()
        .getNewServerTransaction(registerReqUA2);
      Response reg200Resp = createResponse(registerReqUA2, ua2, 200, ua2.generateNewTag());
      //transfer specific headers
      Header servletNameHd = registerReqUA2.getHeader(TestConstants.SERVLET_HEADER);
      Header methodNameHd = registerReqUA2.getHeader(TestConstants.METHOD_HEADER);
      reg200Resp.addHeader(servletNameHd);
      reg200Resp.addHeader(methodNameHd);
      
      serverTransUA2.sendResponse(reg200Resp);
    } catch (Exception e) {
      logger.error("*** Exception when sending back 180/INVITE ***", e);
      throw new TckTestException(e);
    } 
 
    // (4) UA1 receive 200/REGISTER
    EventObject resp200Evt = waitNon100ResponseEvent(ua1, transUA1, waitDuration);
    assertEquals("Should have received OK", Response.OK, 
  	  ((ResponseEvent) resp200Evt).getResponse().getStatusCode());
  }
  
  /**
   * The call flow is:
   * 
   *          UAC                   PROXY                 UAS1                  UAS2
   *           |                     |                      |                     |
   *           |----- (1)INVITE ---->|                      |                     |
   *           |                <PROXY fork>                |                     |                                
   *           |                     |----- (2)INVITE ----->|                     |         
   *           |                     |<-----  (3) 180   ----|                     |
   *           |<----  (4) 180  -----|                      |                     |
   *           |                     |----------------- (5)INVITE --------------->|
   *           |                     |<------------------  (6) 180   -------------|
   *           |<----  (7) 180  -----|                      |                     |
   *           |                     |<-----  (8) 200   ----|                     |
   *           | <if NoCancel is true, no cancel sent out>  |                     |
   *           |                     |                      |           <no cancel received>
   *           |                     |                      |                     |
   *
   */
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy5",
          "SipServlet:JAVADOC:Proxy21"},
      desc = "User agents can determine if the proxy will not cancel outstanding "
        + "branches upon receiving the first 2xx INVITE response as in RFC 3841.")
  public void testGetSetNoCancel001() 
    throws InterruptedException, ParseException, SipException, InvalidArgumentException {
    clientEntryLog();

    ArrayList<Header> privateHeaders = getTckTestPrivateHeadersWithAR();
    
    //initialize SipCall
    SipCall callerA = ua1.createSipCall();
    SipCall callerB = ua2.createSipCall();
    SipCall callerC = ua3.createSipCall();
    callerB.listenForIncomingCall();    
    callerC.listenForIncomingCall();    
    Thread.sleep(10);

    // (1) A send INVITE(with UA2 URI) -serverURI
    boolean status = callerA.initiateOutgoingCall(ua1URI, ua2URI, null, 
        privateHeaders, null, null);
    assertTrue("Initiate outgoing call failed - " + callerA.format(), status);

    // (2) B receive INVITE
    callerB.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("callerB wait incoming call - " + callerB.format(), callerB);
    assertMsg(callerB.getLastReceivedRequest().getMessage());

    // (3) B send 180/INVITE
    callerB.sendIncomingCallResponse(Response.RINGING, null, -1, privateHeaders, null, null);
    assertLastOperationSuccess("callerB send OK - " + callerB.format(), callerB);

    // (5) C receive INVITE
    callerC.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("callerC wait incoming call - " + callerC.format(), callerC);

    // (6) C send 180/INVITE
    callerC.sendIncomingCallResponse(Response.RINGING, null, -1, privateHeaders, null, null);
    assertLastOperationSuccess("callerC send OK - " + callerC.format(), callerC);

    // (8) B send 200/INVITE
    callerB.sendIncomingCallResponse(Response.OK, null, -1, privateHeaders, null, null);
    assertLastOperationSuccess("callerB send OK - " + callerB.format(), callerB);
    
    // (9) C can not receive CANCEL from proxy
    boolean cancelResult = callerC.waitForCancel(waitDuration);
    assertFalse(cancelResult);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy6"},
      desc = "User agents can get the request received from the upstream caller.")
  public void testGetOriginalRequest001() {
    assertSipMessageProxy(null, null, null, 1, 
        new SipRequestAssertionProxy(), 
        new SipResponseAssertionProxy());
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy7",
          "SipServlet:JAVADOC:Proxy24"},
      desc = "User agents can determine if this proxy object is set to proxy in parallel.")
  public void testGetSetParallel001() {
    assertSipMessageProxy(null, null, null, 1, new SipRequestAssertionProxy());
  }  

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy11",
          "SipServlet:JAVADOC:Proxy25"},
      desc = "User agents can set and get the current value of the overall proxy timeout value.")
  public void testGetSetProxyTimeout001() {
    assertSipMessageProxy(null, null, null, 1, new SipRequestAssertionProxy());
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy12",
          "SipServlet:JAVADOC:Proxy13",
          "SipServlet:JAVADOC:Proxy26"},
      desc = "User agents can set and get the SipURI that the application can use to add "
        + "parameters to the Record-Route header.")
  public void testGetSetRecordRoute001() {
    assertSipMessageProxy(null, null, null, 1, new SipRequestAssertion(){
      public void assertRequest(RequestEvent reqEvent){
        Request req = reqEvent.getRequest();
        
        //assert "Fail-Reason" header
        Header failReasonHd = req.getHeader(TestConstants.TEST_FAIL_REASON);
        if(failReasonHd != null) {
          fail(getFailReason(failReasonHd));
        }
        
        //assert "Record-Route" header
        RecordRouteHeader recordRoute = (RecordRouteHeader)req.getHeader(
            RecordRouteHeader.NAME);
        if(recordRoute == null) fail("Fail to set RecordRoute through Proxy.");
      }      
    });
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy14",
          "SipServlet:JAVADOC:Proxy27"},
      desc = "User agents can determine if this proxy object is set to recurse.")
  public void testGetSetRecurse001() {
    assertSipMessageProxy(null, null, null, 1, new SipRequestAssertionProxy());
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy15",
          "SipServlet:JAVADOC:Proxy28"},
      desc = "Deprecated. use a more general purpose getProxyTimeout().")
  public void testGetSetSequentialSearchTimeout001() {
    clientEntryLog();
    //the APIs have been deprecated
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy16",
          "SipServlet:JAVADOC:Proxy29"},
      desc = "Deprecated. stateless proxy is no longer supported.")
  public void testGetSetStateful001() {
    clientEntryLog();
    // the APIs have been deprecated
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy17",
          "SipServlet:JAVADOC:Proxy30"},
      desc = "User agents can determine if the controlling servlet will be invoked on "
        + "incoming responses for this proxying operation.")
  public void testGetSetSupervised001() {
    assertSipMessageProxy(null, null, null, 1, new SipRequestAssertionProxy());
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy18"},
      desc = "User agents can proxy a SIP request to the specified set of destinations.")
  public void testProxyTo001() {
    assertSipMessageProxy(null, null, null, 1, null);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy19"},
      desc = "User agents can proxy a SIP request to the specified destination.")
  public void testProxyTo002() {
    assertSipMessageProxy(null, null, null, 1, null);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy22"},
      desc = "If multihoming is supported, user agents can select the outbound interface "
        + "to use when sending requests for proxy branches.")
  public void testSetOutboundInterface001() {
    assertSipMessageProxy(null, null, null, 1, new SipRequestAssertionOutboundInterface());
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Proxy23"},
      desc = "If multihoming is supported, user agents can select the outbound interface "
        + "and port number to use for proxy branches.")
  public void testSetOutboundInterface002() {
    assertSipMessageProxy(null, null, null, 1, new SipRequestAssertionOutboundInterface());
  }
  
  
  /**
   * Used to send a sip MESSAGE to another UA through proxy, and receive response or wait 
   * to expire.
   * The call flow is:
   * 
   *          UA1                           PROXY                           UA2
   *           |                              |                              |
   *           |---------- (1)MESSAGE ------->|                              |
   *           |                        <PROXY logic>                        |                                
   *           |                              |-- (2)MESSAGE(with result) -->|         
   *           |                              |                  <UAS result assertion>       
   *           |                              |<-------- (3) 200OK    -------|
   *           |<-------- (4) 200OK    -------|                              |
   *           |                              |                              |
   *
   * @param headerList  Custom headers of this message
   * @param servletName The servlet name in the server side to handle the message
   * @param methodName  The method in server side to handle the message
   * @param cseq        Cseq header of this message
   */
  private void assertSipMessageProxy(
      List<Header> headerList, 
      String servletName, 
      String methodName, 
      int cseq,
      SipRequestAssertion reqAssertion) {
    assertSipMessageProxy(headerList, servletName, methodName, cseq, reqAssertion, null);
  }
  
  private void assertSipMessageProxy(
      List<Header> headerList, 
      String servletName, 
      String methodName, 
      int cseq,
      SipRequestAssertion reqAssertion,
      SipResponseAssertion respAssertion) {
    clientEntryLog();

    StackTraceElement stack = getBasePackageStack(new Exception().getStackTrace());
    String localServletName = (TestUtil.hasText(servletName)) ? 
        servletName : getInterfaceName(stack.getClassName());
    String localMethodName = (TestUtil.hasText(methodName)) ? 
        methodName : stack.getMethodName();

    //create MESSAGE request
    Request req = assembleRequest(Request.MESSAGE, localServletName, 
        localMethodName, TestConstants.SERVER_MODE_PROXY, cseq);
    if (headerList != null) {
      for (Header header : headerList) {
        req.addHeader(header);
      }
    }

    //UA2 begin listen request
    ua2.listenRequestMessage();

    /////////////////////////// begin call flow ////////////////////////////
    // (1) UA1 Send MESSAGE  
    SipTransaction transUA1 = ua1.sendRequestWithTransaction(req, false, null);
    assertNotNull(ua1.format(), transUA1);

    // (2) UA2 Receive MESSAGE 
    RequestEvent eventUA2 = ua2.waitRequest(waitDuration);
    assertNotNull(eventUA2);
    Request messageReq = eventUA2.getRequest();
    //assertion
    if(reqAssertion != null){
      reqAssertion.assertRequest(eventUA2);
    }    

    // (3) UA2 send back 200/MESSAGE
    try {
      ServerTransaction serverTransUA2 = ua2.getParent().getSipProvider()
        .getNewServerTransaction(messageReq);
      Response msg200Resp = createResponse(messageReq, ua2, 200, ua2.generateNewTag());
      //transfer specific headers
      Header servletNameHd = messageReq.getHeader(TestConstants.SERVLET_HEADER);
      Header methodNameHd = messageReq.getHeader(TestConstants.METHOD_HEADER);
      msg200Resp.addHeader(servletNameHd);
      msg200Resp.addHeader(methodNameHd);

      serverTransUA2.sendResponse(msg200Resp);
    } catch (Exception e) {
      logger.error("*** Exception when sending back 200/MESSAGE ***", e);
      throw new TckTestException(e);
    }  
    
    // (4) UA1 receives 200/MESSAGE
    ResponseEvent responseEvent = waitIncomingResponse(ua1, transUA1, waitDuration);
    assertNotNull(responseEvent);
    Response msg200RespFromProxy = responseEvent.getResponse();
    //assertion
    if(respAssertion != null){
      respAssertion.assertResponse(responseEvent);
    }    
  }
  
  private ArrayList<Header> getTckTestPrivateHeaders(){
    StackTraceElement stack = getBasePackageStack(new Exception().getStackTrace());
    String localServletName = getInterfaceName(stack.getClassName());
    String localMethodName = stack.getMethodName();
    
    HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
    Header servletheader;
    Header methodHeader;

    try {
      servletheader = 
        headerFactory.createHeader(TestConstants.SERVLET_HEADER, localServletName);
      methodHeader = 
        headerFactory.createHeader(TestConstants.METHOD_HEADER, localMethodName);
    } catch (ParseException e) {
      logger.error("*** ParseException when retrieving private headers ***", e);
      throw new TckTestException(e);  
    }
    
    ArrayList<Header> additionalHeaderList = new ArrayList<Header>(1);
    additionalHeaderList.add(servletheader);
    additionalHeaderList.add(methodHeader);

    return additionalHeaderList;
  }
  
  private ArrayList<Header> getPrivateURIHeaders(){
    StackTraceElement stack = getBasePackageStack(new Exception().getStackTrace());
    String localServletName = getInterfaceName(stack.getClassName());
    String localMethodName = stack.getMethodName();
    
    HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
    Header servletheader;
    Header privateMethodHeader;

    try {
      servletheader = 
        headerFactory.createHeader(TestConstants.SERVLET_HEADER, localServletName);
      privateMethodHeader = 
        headerFactory.createHeader(TestConstants.PRIVATE_URI, localMethodName);      
    } catch (ParseException e) {
      logger.error("*** ParseException when retrieving private headers ***", e);
      throw new TckTestException(e);  
    }
    
    ArrayList<Header> additionalHeaderList = new ArrayList<Header>(1);
    additionalHeaderList.add(servletheader);
    additionalHeaderList.add(privateMethodHeader);    
    return additionalHeaderList;
  }
  
  private ArrayList<Header> getTckTestRouteHeaders(){
    
    HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
    Header routeHeader;

    try {

      routeHeader= headerFactory.createHeader(
          RouteHeader.NAME, ua2DispName + " <" + ua2URI + ";lr>");
    } catch (ParseException e) {
      throw new TckTestException(e);  
    }
    
    ArrayList<Header> additionalHeaderList = new ArrayList<Header>(1);
    additionalHeaderList.add(routeHeader);

    return additionalHeaderList;
  }
  
  
  
  
  
  /**
   * Used to send a sip INVITE to UA2 through proxy,
   * and receive response or wait to expire.
   * The call flow is:
   * <p/>
   * UA1                           PROXY                           UA2
   * |                              |                              |
   * |---------- (1)INVITE  ------->|                              |
   * |                        <PROXY logic>                        |
   * |                              |---------- (2)INVITE  ------->|
   * |                              |                              |
   * |                              |<--------  (3)200      -------|
   * |<----------(4)200-------------|                              |
   * |                              |                              |
   * |-----------(5)ACK------------>|                              |
   * |                        <ADD TEST RESULT>                    |   
   * |                              |---------- (6)ACK ----------->|
   * |                              |                              |      
   * |                              |<--------  (7)BYE      -------|
   * |                              |                              |      
   * |<----------(8)BYE-------------|                              |
   * |                              |                              |      
   * |-----------(9)200------------>|                              |
   * |                              |                              |      
   * |                              |----------(10)200 ----------->|   
   */
  protected void assertSipInviteProxyOK(){
    clientEntryLog();
    try {
      SipCall call1 = ua1.createSipCall();
      SipCall call2 = ua2.createSipCall();

      String viaNonProxyRoute = serverHost + ":" + serverPort + "/"
          + testProtocol;
      initiateOutgoingCall(call1, null, null, getTckTestRouteHeaders(),
          viaNonProxyRoute, ua2URI);
      
      call2.listenForIncomingCall();
      call2.waitForIncomingCall(waitDuration);
      assertLastOperationSuccess("b wait incoming call - " + call2.format(),
          call2);
      call2.sendIncomingCallResponse(200, "OK", 0, getTckTestRouteHeaders(),
          null, null);
      assertLastOperationSuccess("send 200 - " + call2.format(), call2);
      do {
        call1.waitOutgoingCallResponse(waitDuration);
        assertLastOperationSuccess("wait response - " + call1.format(), call1);
      } while (call1.getReturnCode() != Response.OK);
      call1.sendInviteOkAck(getPrivateURIHeaders(), null, null);
      assertLastOperationSuccess("Failure sending ACK - " + call1.format(),
          call1);

      call2.waitForAck(waitDuration);
      Request ack = (Request) call2.getLastReceivedRequest().getMessage();
      assertNotNull("Default ack not sent", ack);
      assertEquals("Expected ack", Request.ACK, ack.getMethod());
      assertNotNull(ack.getHeader(TestConstants.TEST_RESULT));
      assertTrue(ack.getHeader(TestConstants.TEST_RESULT).toString().indexOf(
          TestConstants.TEST_RESULT_OK) > -1);
      call1.disconnect();
      call2.waitForRequest(Request.BYE, waitDuration);
      call2.sendResponseToLastReceivedRequest(200, "OK", 0);
      call1.dispose();
      call2.dispose();
    } catch (Exception e) {
      fail("found exception" + e);
    }
  }
  
  /**
   * Used to send a sip INVITE to UA2 through proxy,
   * and receive response or wait to expire.
   * UA2 send reponse 180,200,and send ACK
   * The call flow is:
   * <p/>
   * UA1                           PROXY                           UA2
   * |                              |                              |
   * |---------- (1)INVITE  ------->|                              |
   * |                        <PROXY logic>                        |
   * |                              |---------- (2)INVITE  ------->|
   * |                              |                        <UAS assertion>
   * |                              |<--------  (3)180      -------|
   * |                        <ADD TEST RESULT>                    | 
   * |<----------(4)180-------------|                              |   
   * |                              |<--------  (5)200      -------|
   * |<----------(6)200-------------|                              |
   * |                              |                              |
   * |-----------(7)ACK------------>|                              |
   * |                              |                              |   
   * |                              |---------- (8)ACK ----------->|
   * |                              |                              |      
   * |                              |<--------  (9)BYE      -------|
   * |                              |                              |      
   * |<----------(10)BYE------------|                              |
   * |                              |                              |      
   * |-----------(11)200----------->|                              |
   * |                              |                              |      
   * |                              |----------(12)200 ----------->|   
   */
  
  
  
  protected void assertSipInviteProxy180OK(){
    clientEntryLog();
    try {
      SipCall call1 = ua1.createSipCall();
      SipCall call2 = ua2.createSipCall();
      String viaNonProxyRoute = serverHost + ":" + serverPort + "/"
          + testProtocol;
      initiateOutgoingCall(call1, null, null, getTckTestRouteHeaders(),
          viaNonProxyRoute, ua2URI);
      call2.listenForIncomingCall();
      call2.waitForIncomingCall(waitDuration);
      assertLastOperationSuccess("b wait incoming call - " + call2.format(),
          call2);
      call2.sendIncomingCallResponse(180, "Ringing", 0,
          getTckTestPrivateHeaders(), null, null);
      assertLastOperationSuccess("send 180 - " + call2.format(), call2);
      do {
        call1.waitOutgoingCallResponse(waitDuration);
        assertLastOperationSuccess("wait response - " + call1.format(), call1);
      } while (call1.getReturnCode() != Response.RINGING);
      assertNotNull(call1.getLastReceivedResponse().getMessage().getHeader(
          TestConstants.TEST_RESULT));
      assertTrue(call1.getLastReceivedResponse().getMessage().getHeader(
          TestConstants.TEST_RESULT).toString().indexOf(
          TestConstants.TEST_RESULT_OK) > -1);
      call2.sendIncomingCallResponse(200, "OK", 0, getTckTestPrivateHeaders(),
          null, null);
      do {
        call1.waitOutgoingCallResponse(waitDuration);
        assertLastOperationSuccess("wait response - " + call1.format(), call1);
      } while (call1.getReturnCode() != Response.OK);
      call1.sendInviteOkAck(getTckTestRouteHeaders(), null, null);
      assertLastOperationSuccess("Failure sending ACK - " + call1.format(),
          call1);
      call2.waitForAck(waitDuration);
      call1.disconnect();
      call2.waitForRequest(Request.BYE, waitDuration);
      call2.sendResponseToLastReceivedRequest(200, "OK", 0);
      call1.dispose();
      call2.dispose();
    } catch (Exception e) {
      fail("found exception" + e);
    }
  }
  
  /**
   * Used to send a sip INVITE to UA2 through proxy,
   * and receive response or wait to expire.
   * The call flow is:
   * <p/>
   * UA1                           PROXY                           UA2
   * |                              |                              |
   * |---------- (1)INVITE  ------->|                              |
   * |                        <ADD TEST RESULT>                    |   
   * |                              |---------- (2)INVITE  ------->|
   * |                              |                              |
   * |                              |<--------  (3)200      -------|
   * |<----------(4)200-------------|                              |
   * |                              |                              |
   * |-----------(5)ACK------------>|                              |
   * |                              |---------- (6)ACK ----------->|
   * |                              |                              |      
   * |                              |<--------  (7)BYE      -------|
   * |                              |                              |      
   * |<----------(8)BYE-------------|                              |
   * |                              |                              |      
   * |-----------(9)200------------>|                              |
   * |                              |                              |      
   * |                              |----------(10)200 ----------->|   
   */
  protected void assertSipProxy(boolean addRoute){
    clientEntryLog();
    try{
    SipCall call1 = ua1.createSipCall();
    SipCall call2 = ua2.createSipCall();
    String viaNonProxyRoute = serverHost + ":" + serverPort + "/"
    + testProtocol;    
    
    if(addRoute){
      initiateOutgoingCall(call1, null, null, getTckTestRouteHeaders(),
          viaNonProxyRoute, ua2URI);    
    }else{
      initiateOutgoingCall(call1, null, null, null,
          viaNonProxyRoute, ua2URI);    
    }
    
    call2.listenForIncomingCall();
    call2.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("b wait incoming call - " + call2.format(),
        call2);

    assertNotNull(call2.getLastReceivedRequest().getMessage().getHeader(
        TestConstants.TEST_RESULT));
      assertTrue(call2.getLastReceivedRequest().getMessage().getHeader(
          TestConstants.TEST_RESULT).toString().indexOf(
          TestConstants.TEST_RESULT_OK) > -1);
      call2.sendIncomingCallResponse(200, "OK", 0, getTckTestPrivateHeaders(),
          null, null);    
      do {
        call1.waitOutgoingCallResponse(waitDuration);
        assertLastOperationSuccess("wait response - " + call1.format(), call1);
      } while (call1.getReturnCode() != Response.OK);      
      call1.sendInviteOkAck(getTckTestRouteHeaders(),null,null);
      assertLastOperationSuccess("Failure sending ACK - " + call1.format(),
          call1);
      call2.waitForAck(waitDuration);
      call1.disconnect();
      call2.waitForRequest(Request.BYE, waitDuration);
      call2.sendResponseToLastReceivedRequest(200, "OK", 0);
      call1.dispose();
      call2.dispose();
    }catch(Exception e){
      fail("found exception"+e);
    }
  }
  
  
  
  
  private ArrayList<Header> getTckTestPrivateHeadersWithAR(){
    StackTraceElement stack = getBasePackageStack(new Exception().getStackTrace());
    String localServletName = getInterfaceName(stack.getClassName());
    String localMethodName = stack.getMethodName();
    
    HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
    Header appHeader;
    Header servletheader;
    Header methodHeader;
 
    try {
    	appHeader = 
    		headerFactory.createHeader(TestConstants.APP_HEADER, TestConstants.APP_APITEST);
      servletheader = 
        headerFactory.createHeader(TestConstants.SERVLET_HEADER, localServletName);
      methodHeader = 
        headerFactory.createHeader(TestConstants.METHOD_HEADER, localMethodName);
    } catch (ParseException e) {
      throw new TckTestException(e);  
    }
    
    ArrayList<Header> additionalHeaderList = new ArrayList<Header>(1);
    additionalHeaderList.add(appHeader);
    additionalHeaderList.add(servletheader);
    additionalHeaderList.add(methodHeader);
    additionalHeaderList.add(getUa3UriHeader());

    return additionalHeaderList;
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
  
  private void assertMsg(Message msg){
    Header failReasonHd = msg.getHeader(TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(getFailReason(failReasonHd));
    }
  } 
  
  public class SipRequestAssertionOutboundInterface implements SipRequestAssertion{
    public void assertRequest(RequestEvent reqEvent){
      Request req = reqEvent.getRequest();
      Header outboundItfHd = req.getHeader(TestConstants.PRIVATE_URI);
      assertNotNull("Fail to get private outbound interface header", outboundItfHd);
      String outboundItf = outboundItfHd.toString().replaceAll(TestConstants.PRIVATE_URI + ":", "").trim();
      boolean findMatch = false;
      ListIterator iter = req.getHeaders(ViaHeader.NAME);
      while(iter.hasNext()){
        ViaHeader via = (ViaHeader)iter.next();
       if(via.toString().contains(outboundItf)){
          findMatch = true;
          break;
        }
      }
      assert findMatch : "Outbound interface is not correct";
    }      
  }   
  
}
