/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved.
 *  
 * SipServletRequestTest is used to test the APIs of 
 * javax.servlet.sip.SipServletRequest. 
 */

package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipRequest;
import org.cafesip.sipunit.SipTransaction;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.ResponseEvent;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SipServletRequestTest extends TestBase {
  private static Logger logger = Logger.getLogger(SipServletRequestTest.class);

  private static final String servletName = "SipServletRequest";

  public SipServletRequestTest(String arg0) throws IOException {
    super(arg0);
  }
   /**
   *                      UAC                              UAS
   *                       |                                |
   *                       |-------- (1) MESSAGE ---------->|
   *                       |                                |                                             
   *                       |<------- (2) 200 OK ------------|
   *                       |                                |
   *                       |<------- (3) INVITE ------------|
   *                       |                                |
   *                       |-------- (4) 401 Unauthorized ->|
   *                       |                                |
   *                       |<------- (5) ACK ---------------|
   *                       |                                |
   *                       |<------- (6) INVITE ------------|
   *                       |                                |
   *                       |-------- (7) 200 OK ----------->|
   *                       |                                |
   *                       |<------- (8) ACK ---------------|
   *                       
   */                       
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest1")
  public void testAddAuthHeader001() throws InvalidArgumentException,
      ParseException, SipException {
    clientEntryLog();
    send401ByUa1("001");
  }
  /**
   * The call flow is same as testAddAuthHeader001
   */
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest2")
  public void testAddAuthHeader002() throws InvalidArgumentException,
      ParseException, SipException {
    clientEntryLog();
    send401ByUa1("002");
  }

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest3")
  public void testCreateCancel001() throws InvalidArgumentException,
      ParseException {
    clientEntryLog();
    triggerUACMsg("testCreateCancel001");
  }

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest4")
  public void testCreateResponse001() throws InvalidArgumentException,
      ParseException {    
    assertSipMessage();
  }

  /**
  * Used to send a sip MESSAGE to server side, and receive response or wait to 
  * expire. The call flow is:
  * 
  *                      UAC                            UAS
  *                       |                              |
  *                       |---------- (1)MESSAGE ------->|
  *                       |                <Execute determination logic>                                            
  *                       |<--------- (2)200/500  -------|
  *                       |                              |
  */                       
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest5")
  public void testCreateResponse002() throws InvalidArgumentException,
      ParseException {  
    assertSipMessage();
  }

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest6")
  public void testGetB2buaHelper001() throws InvalidArgumentException,
      ParseException {
    assertSipMessage();
  }

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest7")
  public void testGetInitialPoppedRoute001() throws InvalidArgumentException,
      ParseException {
    assertSipMessage();
  }

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest8")
  public void testGetInputStream001() throws InvalidArgumentException,
      ParseException {
    assertSipMessage();
  }

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest10")
  public void testGetPoppedRoute001() throws InvalidArgumentException,
      ParseException {    
    assertSipMessage();
  }

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest11")
  public void testGetProxy001() throws InvalidArgumentException, ParseException {
    clientEntryLog();
    sendMessageProxyByUa1(servletName, "testGetProxy001");
  }

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest12")
  public void testGetProxy002() throws InvalidArgumentException, ParseException {
    clientEntryLog();
    sendMessageProxyByUa1(servletName, "testGetProxy002");
  }

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest13")
  public void testGetReader001() throws InvalidArgumentException,
      ParseException {
    assertSipMessage();
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipServletRequest15",
    "SipServlet:JAVADOC:SipServletRequest23" },
    desc = "test getRoutingDirective and setRoutingDirective")
  public void testGetSetRoutingDirective001() throws InvalidArgumentException,
      ParseException {
    assertSipMessage();
  }

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest16")
  public void testIsInitial001() throws InvalidArgumentException,
      ParseException {
    assertSipMessage();    
  }


  /**
   * The call flow is:
   * 
   *          UA1                           PROXY                           UA2
   *           |                              |                              |
   *           |--------- (1)REGISTER ------->|                              |
   *           |                        <PROXY logic>                        |                                
   *           |                              |--------- (2)REGISTER ------->|         
   *           |                              |                  <UAS assertion>       
   *           |                              |<------ (3)200/REGISTER ------|
   *           |<----- (4)200/REGISTER ------>|                              |
   *           |                              |                              |
   *
   */

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest17")
  public void testPushPath001() throws InvalidArgumentException, ParseException {
    clientEntryLog();
    StackTraceElement stack = getBasePackageStack(new Exception()
        .getStackTrace());
    String localServletName = getInterfaceName(stack.getClassName());
    String localMethodName = stack.getMethodName();

    Map map = new HashMap();
    map.put(TestConstants.APP_HEADER, TestConstants.APP_APITEST);
    map.put(TestConstants.SERVLET_HEADER, servletName);
    map.put(TestConstants.METHOD_HEADER, "testPushPath001");
    map.put("Supported", "Path");    
    map.put("new-uri", "sip:" + serverHost + ":" + serverPort);
    map.put("new-uri2", ua2URI);

    Request registerReqUA1 = assembleEmptyRequest(Request.REGISTER, 1, map,
        TestConstants.SERVER_MODE_PROXY);

    // UA2 listen to incoming message
    ua2.listenRequestMessage();
   
    // (1) UA1 Send REGISTER
    SipTransaction transUA1 = ua1.sendRequestWithTransaction(registerReqUA1,
        false, null);
    assertLastOperationSuccess("Fail to send REGISTER- " + ua1.format(), ua1);

    // (2) UA2 Receive REGISTER
    RequestEvent eventUA2 = ua2.waitRequest(waitDuration);
    assertLastOperationSuccess("Fail to receive REGISTER- " + ua2.format(), ua2);
    Request registerReqUA2 = eventUA2.getRequest();

    Header pathHd = registerReqUA2.getHeader("Path");
    if (pathHd == null)
      fail("Fail to get \"Path\" header from REGISTER.");
    Header failReasonHd = registerReqUA2
        .getHeader(TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(getFailReason(failReasonHd));
    }

    // (3) UA2 send back 200/REGISTER
    sendResponseForMessage(ua2, registerReqUA2, Response.OK);      

    // (4) UA1 receive 200/REGISTER
    waitNon100ResponseEvent(ua1, transUA1,
        waitDuration);    
    assertLastOperationSuccess(ua1.format(), ua1);
  }

  /**
   * The call flow is:
   * 
   *          UA1                           PROXY                         UA2
   *           |                              |                            |
   *           |--------- (1)MESSAGE -------> |                            |
   *           |                        <PROXY logic>                      |                                
   *           |                              |--------- (2)MESSAGE------->|         
   *           |                              |                 <UAS assertion>       
   *           |                              |<------ (3)200/MESSAGE------|
   *           |<----- (4)200/MESSAGE ------- |                            |
   *           |                              |                            |
   *
   */
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest18")
  public void testPushRoute001() throws InvalidArgumentException,
      ParseException {
    clientEntryLog();
    StackTraceElement stack = getBasePackageStack(new Exception()
        .getStackTrace());
    String localServletName = getInterfaceName(stack.getClassName());
    String localMethodName = stack.getMethodName();

    Map specifiedHeaders = new HashMap();
    specifiedHeaders.put(TestConstants.APP_HEADER, TestConstants.APP_APITEST);
    specifiedHeaders.put(TestConstants.SERVLET_HEADER, servletName);
    specifiedHeaders.put(TestConstants.METHOD_HEADER, localMethodName);
    
    Request reqUA1 = assembleEmptyRequestProxy(Request.MESSAGE, 1,
        specifiedHeaders, TestConstants.SERVER_MODE_PROXY);
    assertNotNull(reqUA1);
    // UA2 listen to incoming message
    ua2.listenRequestMessage();

    // (1) UA1 Send request
    SipTransaction transUA1 = ua1.sendRequestWithTransaction(reqUA1,
        false, null);
    assertLastOperationSuccess(ua1.format(), ua1);

    // (2) UA2 Receive request
    RequestEvent eventUA2 = ua2.waitRequest(waitDuration);
    assertLastOperationSuccess(ua2.format(), ua2);
    Request reqUA2 = eventUA2.getRequest();
    
    // assertion of request
    Header route = reqUA2.getHeader("Route");  

    if (null == route){
      fail("Fail to get \"Route\" header from Request, route is null");
    }
    Header failReasonHd = reqUA2
        .getHeader(TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(getFailReason(failReasonHd));
    }

    // (3) UA2 send back 200/request
    sendResponseForMessage(ua2, reqUA2, Response.OK);

    // (4) UA1 receive 200/request
    waitNon100ResponseEvent(ua1, transUA1, waitDuration);   
    assertLastOperationSuccess(ua1.format(), ua1);
  }
  
  /**
   * The call flow is:
   * 
   *          UA1                           PROXY                         UA2
   *           |                              |                            |
   *           |--------- (1)MESSAGE -------->|                            |
   *           |                        <PROXY logic>                      |                                
   *           |                              |-------- (2)MESSAGE ------->|         
   *           |                              |                  <UAS assertion>       
   *           |                              |<----- (3)200/MESSAGE ------|
   *           |<----- (4)200/MESSAGE --------|                            |
   *           |                              |                            |
   *
   */
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest19")
  public void testPushRoute002() throws InvalidArgumentException,
      ParseException {
    clientEntryLog();

    StackTraceElement stack = getBasePackageStack(new Exception()
        .getStackTrace());
    String localServletName = getInterfaceName(stack.getClassName());
    String localMethodName = stack.getMethodName();
    
    Map specifiedHeaders = new HashMap();
    specifiedHeaders.put(TestConstants.APP_HEADER, TestConstants.APP_APITEST);
    specifiedHeaders.put(TestConstants.SERVLET_HEADER, servletName);
    specifiedHeaders.put(TestConstants.METHOD_HEADER, localMethodName);
    specifiedHeaders.put("user", ua2DispName);
    specifiedHeaders.put("host", ua2Host + ":" + ua2Port + ";lr");
    Request reqUA1 = assembleEmptyRequestProxy(Request.MESSAGE, 1,
        specifiedHeaders, TestConstants.SERVER_MODE_PROXY);
    assertNotNull(reqUA1);
   
    ua2.listenRequestMessage();

    // (1) UA1 Send request
    SipTransaction transUA1 = ua1.sendRequestWithTransaction(reqUA1,
        false, null);
    assertLastOperationSuccess(ua1.format(), ua1);

    // (2) UA2 Receive request
    RequestEvent eventUA2 = ua2.waitRequest(waitDuration);
    assertLastOperationSuccess(ua2.format(), ua2);
    Request reqUA2 = eventUA2.getRequest();
    Header route = reqUA2.getHeader("Route");
    if (null == route){
      fail("Fail to get \"Route\" header from Request, route is null");
    }   
    Header failReasonHd = reqUA2
        .getHeader(TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(getFailReason(failReasonHd));
    }

    // (3) UA2 send back 200/reques
    sendResponseForMessage(ua2, reqUA2, Response.OK);
    
    // (4) UA1 receive 200/request
    waitNon100ResponseEvent(ua1, transUA1, waitDuration);
    assertLastOperationSuccess(ua1.format(), ua1);
  }

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest20")
  public void testSend001() throws InvalidArgumentException, ParseException {
    assertSipMessageBiWay();
  }

 
 @AssertionIds(ids = { "SipServlet:JAVADOC:SipServletRequest9",
   "SipServlet:JAVADOC:SipServletRequest21" },
   desc = "test getMaxForwards and setMaxForwards")
  public void testGetSetMaxForwards001() throws InvalidArgumentException,
      ParseException {
   assertSipMessageBiWay();
  }

  /**
   * Used to send a sip MESSAGE to another UA through proxy, and receive 
   * response or wait to expire. The call flow is:
   * 
   *          UA1                           UAS                           UA2
   *           |                              |                              |
   *           |---------- (1)MESSAGE ------->|                              |
   *           |                        <logic>                              |                                
   *           |                              |-- (2)MESSAGE(with result) -->|         
   *           |                              |         <UAS result assertion>       
   *           |                              |<-------- (3) 200OK    -------|
   *           |                              |                              |
   *           |<-------- (4) 200OK    -------|                              |
   *
   */

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipServletRequest14",
      "SipServlet:JAVADOC:SipServletRequest22" },
      desc = "test getRequestURI and setRequestURI")
  public void testGetSetRequestURI001() throws InvalidArgumentException,
      ParseException {
    clientEntryLog();

    Map map = new HashMap();
    map.put(TestConstants.APP_HEADER, TestConstants.APP_APITEST);
    map.put(TestConstants.SERVLET_HEADER, servletName);
    map.put(TestConstants.METHOD_HEADER, "testGetSetRequestURI001");
    map.put("new-uri", ua2URI);
    Request req = assembleEmptyRequest(Request.MESSAGE, 1, map,  
        TestConstants.SERVER_MODE_UA);


    ua2.listenRequestMessage();   
    //  (1) UA1 Send MESSAGE    
    SipTransaction trans = ua1.sendRequestWithTransaction(req, true, null);
    assertLastOperationSuccess(ua1.format(), ua1);
   
    //  (2) UA2 Receive MESSAGE    
    RequestEvent messageEvent = ua2.waitRequest(waitDuration);
    assertNotNull(messageEvent);
    Request messageReq =  messageEvent.getRequest();    
    assertNotSame(Request.MESSAGE, messageReq.getMethod()
        .toUpperCase());
    ua2.getParent().getMessageFactory().createResponse(
        Response.OK, messageEvent.getRequest());
 
    Header failReasonHd = messageReq.getHeader(TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(getFailReason(failReasonHd));
    }
    
    
    // (3) UA2 send back 200/MESSAGE    
    sendResponseForMessage(ua2, messageReq, Response.OK);    
    
    // (4) UA1 Receive response 200/MESSAGE    
    waitResponseForMessage(ua1, trans, waitDuration);
    assertLastOperationSuccess(
        "Receive 200 OK response for MESSAGE failed - " + ua1.format(), ua1); 
    
    
  }
  
  /**
   * send a message to the specified UAS, UAS sends back 200ok and INVITE, CANCEL
   * 
   *          UA1                           UAS       
   *           |                              | 
   *           |---------- (1)MESSAGE ------->|                       
   *           |                              |                                
   *           |<-------- (2) 200OK    -------|       
   *           |                              |  
   *           |<-------- (3) INVITE    ------|
   *           |                              | 
   *           |----------(4) 180/INVITE----->|
   *           |                              |
   *           |<-------- (5) CANCEL    ------|
   *           |                              |
   *           |----------(6) 200/CANCEL----->|
   *           |                              |
   *           |----------(7) 487/INVITE----->|
   *            
   * 
   */
  private void triggerUACMsg(String UASMethodName)
      throws InvalidArgumentException, ParseException {
    int cseq = 1;
    SipCall a = ua1.createSipCall();
    a.listenForIncomingCall();
    // (1) send message
    SipTransaction trans = sendMessage(ua1, cseq);
    if (trans == null) {
      fail("Fail to send MESSAGE out.");
    }
    // (2) receive response
    ua1.waitResponse(trans, waitDuration);

    // (3) receive INVITE
    a.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("a wait incoming call - " + a.format(), a);

    // (4) send 180/invite
    a.sendIncomingCallResponse(Response.RINGING, null, -1);
    assertLastOperationSuccess("a send 180 - " + a.format(), a);

    // (5) receive CANCEL
    a.waitForRequest(Request.CANCEL, waitDuration);
    assertLastOperationSuccess("a wait for CANCEL - " + a.format(), a);
    SipRequest receivedCancel = a.getLastReceivedRequest();
    assertNotNull(receivedCancel);
    assertEquals("Unexpected request", Request.CANCEL,
        ((Request)receivedCancel.getMessage()).getMethod());

    // (6) send 200/CANCEL
    a.sendResponseToLastReceivedRequest(Response.OK, "Cancel Answer", 0);
    assertLastOperationSuccess("a send 200/CANCEL" + a.format(), a);

    // (7) send 487/invite
    a.sendIncomingCallResponse(Response.REQUEST_TERMINATED, null, -1);
    assertLastOperationSuccess("a send 487 - " + a.format(), a);

  }
  
  /**
   * The call flow is:
   * 
   *          UA1                           PROXY                         UA2
   *           |                            |                              |
   *           |--------- (1)MESSAGE ------>|                              |
   *           |                            |                              |                                
   *           |                            |--------- (2)MESSAGE -------->|         
   *           |                            |                   <UAS assertion>       
   *           |                            |<------ (3)200/MESSAGE -------|
   *           |<----- (4)200/MESSAGE ------|                              |
   *           |                            |                              |
   *
   */
  private EventObject sendMessageProxyByUa1(String servletName,
      String serverMethod) throws InvalidArgumentException, ParseException {
    Request request = assembleRequest("MESSAGE", servletName, serverMethod,
        TestConstants.SERVER_MODE_PROXY, 1);

    // UA2 begin listen request
    ua2.listenRequestMessage();

    // (1) UA1 Send MESSAGE
    SipTransaction trans = ua1.sendRequestWithTransaction(request, true, null);
    assertLastOperationSuccess("Send MESSAGE failed - " + ua1.format(), ua1);

    // (2) UA2 Receive MESSAGE
    RequestEvent eventUA2 = ua2.waitRequest(waitDuration);
    assertLastOperationSuccess("Wait MESSAGE failed - " + ua2.format(), ua2);
    Request messageReq = eventUA2.getRequest();

    // (3) UA2 send back 200/MESSAGE    
    sendResponseForMessage(ua2, messageReq, Response.OK);
    
    // (4) UA1 receive 200/MESSAGE
    EventObject event = ua1.waitResponse(trans, waitDuration);
    assertLastOperationSuccess("Wait Response failed - " + ua1.format(), ua1);
    return event;
  }

  private void send401ByUa1(String auth) throws InvalidArgumentException,
      ParseException, SipException {
    int cseq = 1;
    String challenge = "Digest realm =\"beasys.com\", "
        + "nonce=\"dbffb13c8a4d30117c25c753d1fa7710\","
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
    boolean status = false;
    if (hdr == null) {
      ArrayList<Header> additionalHeaders = new ArrayList<Header>(1);
      additionalHeaders.add(a.getHeaderFactory().createWWWAuthenticateHeader(
          challenge));
      additionalHeaders.add(a.getHeaderFactory().createWWWAuthenticateHeader(
          challenge));
      if (auth.equalsIgnoreCase("001")) {
        status = a.sendIncomingCallResponse(Response.UNAUTHORIZED,
            "Unauthorized", 1, additionalHeaders, null, null);
      } else {
        status = a.sendIncomingCallResponse(Response.UNAUTHORIZED,
            "Unauthorized with UserPwd", 1, additionalHeaders, null, null);
      }

      assertLastOperationSuccess("a send 401 - " + a.format(), a);
    }
    if (!status) {
      fail("Fail to send 401 response out.");
    }

    // (5) ua receive ACK
    // Not necessary to assert ACK
    // because ACK received for ServerTransaction not delivering to application
    // by Jain sip.

    // (6) receive INVITE again
    a.listenForReinvite();
    SipTransaction tx = a.waitForReinvite(waitDuration);

    // (7) send 200 OK
    Request req = tx.getRequest();
    if (req.getHeader(AuthorizationHeader.NAME) != null) {
      String contactUri = a.getParent().getContactInfo().getURI();
      String contact = contactUri
          .substring(0, contactUri.lastIndexOf("lr") - 1);
      assertTrue(a.respondToReinvite(tx, Response.OK, "OK", 2, contact, null,
          null, (String) null, null));
    }

    // (8) UA1 receive ACK
    // assertTrue(a.waitForAck(waitDuration));
  }

  /**
   * Construct a sip request assuming the uac is ua1, and destination is decided
   * by the mode parameter
   * Contact and Expires headers will be added according the mode value
   *
   */
  private Request assembleEmptyRequestProxy(
    String sipMethod, int cseq, Map<String, Object> specifiedHeaders, String mode) {
    try {
      HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
      AddressFactory address_factory = ua1.getParent().getAddressFactory();
      String sipMethod1 = getMethodName(sipMethod);
      //create the start line according to the mode 
      String reqURI = null;
      if(Request.REGISTER.equals(sipMethod1)){
        reqURI = "sip:" + serverHost;
      }else{
        reqURI = TestConstants.SERVER_MODE_UA.equals(mode) ? serverURI : ua2URI;
      }
      URI request_uri = address_factory.createURI(reqURI);

      String fromStr = ua1Addr + ";tag=" + ua1.generateNewTag();

      String toStr = null;
      if(Request.REGISTER.equals(sipMethod1)){
        toStr = ua1Addr;
      }else{
        toStr = TestConstants.SERVER_MODE_UA.equals(mode) ? serverAddr : ua2Addr;
      }
      
      ArrayList<Header> via_headers = ua1.getViaHeaders();
      
      Request message = ua1.getParent().getMessageFactory().createRequest(
          request_uri, sipMethod1, ua1.getParent().getSipProvider().getNewCallId(), 
          header_factory.createCSeqHeader(new Long(cseq).longValue(), sipMethod1), 
          (FromHeader) header_factory.createHeader(FromHeader.NAME, fromStr), 
          (ToHeader) header_factory.createHeader(ToHeader.NAME, toStr), 
          via_headers, 
          header_factory.createMaxForwardsHeader(5));              
      
      // Contact header in Invite
      if(Request.INVITE.equals(sipMethod1) || Request.REGISTER.equals(sipMethod1)){
        message.addHeader(ua1.getContactInfo().getContactHeader());
      }
      //Expires header in Register
      if(Request.REGISTER.equals(sipMethod1)){
        message.addHeader(header_factory.createHeader(
            TestConstants.REGISTER_EXPIRES_HEADER, "7200"));
      }

      //Route header
      message.addHeader(header_factory.createHeader(
          RouteHeader.NAME, displayName + " <" + serverURI + ";lr>"));      

      //add the specified headers
      if(specifiedHeaders != null){
        Iterator itr = specifiedHeaders.keySet().iterator();
        while(itr.hasNext()){
          String name = (String)itr.next();
          Object value = specifiedHeaders.get(name);
          if(value instanceof String){
            message.addHeader(header_factory.createHeader(name,(String)value));
          }else if(value instanceof List){
            Iterator values = ((List)value).iterator();
            while(values.hasNext()){
              message.addHeader(header_factory.createHeader(name,
                  (String) values.next()));
            }
          }else{
            logger.warn("*** the header value type is invalide:"
                + value.getClass().getName() + "***");            
          }
        }
      }
      return message;
    } catch (ParseException e) {
      logger.error("*** ParseException when creating sip request ***", e);
      throw new TckTestException(e);
    } catch (InvalidArgumentException e) {
      logger.error("*** InvalidArgumentException when creating sip request ***", e);
      throw new TckTestException(e);
    }
  } 
  
  @AssertionIds(
    ids = "SipServlet:JAVADOC:SipServletRequest3",
    desc = "IllegalStateException should be thrown if the transaction state " +
        "is such that it doesn't allow a CANCEL request to be sent")
  public void testCreateCancel101() {
    assertReqInvite();
  }  
  
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest4", 
      desc = "IllegalArgumentException should be thrown if the statuscode " +
          "is not a valid SIP status code")
  public void testCreateResponse101() {
    assertSipMessage();
  }  

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest4", 
      desc = "IllegalStateException should be thrown if this request has " +
          "already been responded to with a final status code with " +
          "statuscode parameter.")
  public void testCreateResponse102() {
  	// the case sometimes can't receive first 200 ok, so assertSipMessageBiWay
    // is not used
    clientEntryLog();
    ua1.listenRequestMessage();
    SipTransaction trans =
				sendMessage(ua1, "SipServletRequest", "testCreateResponse102",
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
  
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest5",
      desc = "IllegalArgumentException should be thrown if the statuscode " +
          "is not a valid SIP status with statuscode and reasonPhrase " +
          "parameters.")
  public void testCreateResponse103() {    
    assertSipMessage();
  }  
  
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest5",
      desc = "IllegalStateException should be thrown if this request has " +
          "already been responded to with a final status code with " +
          "statuscode and reasonPhrase parameters.")
  public void testCreateResponse104() {    
  	// the case sometimes can't receive first 200 ok, so assertSipMessageBiWay
    // is not used
    clientEntryLog();
    ua1.listenRequestMessage();
    SipTransaction trans =
				sendMessage(ua1, "SipServletRequest", "testCreateResponse104",
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
  
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest6",
      desc = "IllegalStateException should be thrown if getProxy() " +
          "had already been called")
  public void testGetB2buaHelper101() {  
    assertSipMessage();
  }
  
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest21",
    desc = "IllegalArgumentException should be thrown if the argument is " +
        "not in the range 0 to 255")
  public void testSetMaxForwards101() throws InvalidArgumentException,
     ParseException {
   assertSipMessage();
  }
  
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest22",
      desc = "NullPointerException should be thrown on null uri.")
  public void testSetRequestURI101() {
    assertSipMessage();
  }
  
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest11",
    desc = "TooManyHopsException should be thrown if the request has a " +
        "Max-Forwards header field value of 0 ")
  public void testGetProxy101() throws InvalidArgumentException, ParseException {
    clientEntryLog();
    Map map = new HashMap();
    map.put(TestConstants.APP_HEADER, TestConstants.APP_APITEST);
    map.put(TestConstants.SERVLET_HEADER, servletName);
    map.put(TestConstants.METHOD_HEADER, "testGetProxy101");
    assertSipResponse483(map);    
  }  

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest11",
    desc = "IllegalStateException should be thrown if the transaction " +
        "has already completed.")
  public void testGetProxy102() throws InvalidArgumentException, ParseException {    
    clientEntryLog();
    assertSipMessageBiWay();
  }  

  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest12",
    desc = "TooManyHopsException should be thrown if the request has a " +
        "Max-Forwards header field value of 0.")
  public void testGetProxy103() throws InvalidArgumentException, ParseException {
    clientEntryLog();
    Map map = new HashMap();
    map.put(TestConstants.APP_HEADER, TestConstants.APP_APITEST);
    map.put(TestConstants.SERVLET_HEADER, servletName);
    map.put(TestConstants.METHOD_HEADER, "testGetProxy103");
    assertSipResponse483(map);    
  }
  
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest12",
  desc = "IllegalStateException should be thrown if the transaction has " +
      "already completed.")
  public void testGetProxy104() throws InvalidArgumentException, ParseException {    
    assertSipMessageBiWay();
  }   
  

  
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest15", 
     desc = "IllegalStateException should be thrown if called on a request " +
         "that is not initial.")
  public void testGetRoutingDirective101() throws InvalidArgumentException,
    ParseException, SipException {
    clientEntryLog();  
    assertInviteBye();
  }
  
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest23", 
  desc = "llegalStateException should be thrown if called on a request " +
         "that is not initial.")
  public void testSetRoutingDirective101() throws InvalidArgumentException,
  ParseException, SipException {
  clientEntryLog();  
  assertInviteBye();
 }
  
  @AssertionIds(ids = "SipServlet:JAVADOC:SipServletRequest17",
      desc = "IllegalStateException should be thrown if invoked on " +
          "non-REGISTER Request.")
  public void testPushPath101() {
    assertSipMessage();
  }
 
   /**
   * The call flow is:
   * 
   *                      UAC                            UAS
   *                       |                              |
   *                       |---------- (1)MESSAGE ------->|
   *                       |               <Execute determination logic 1>                        
   *                       |<--------- (2)483/MESSAGE-----|
   *                       |                              |
   *
   */
  private void assertSipResponse483(Map map) {
    Request reqUA1 = assembleEmptyRequest(Request.MESSAGE, 1, map,
        TestConstants.SERVER_MODE_UA);
    // (1) Send MESSAGE
    SipTransaction trans = ua1.sendRequestWithTransaction(reqUA1, true, null);
    assertNotNull(ua1.format(), trans);
    // (2) Receive 483/MESSAGE
    ResponseEvent resEvt = waitIncomingResponse(ua1, trans, waitDuration);
    if (resEvt.getResponse().getStatusCode() != Response.TOO_MANY_HOPS) {
      fail("Fail to receive TOO_MANY_HOPS Response for MESSAGE.");
    }
  }    
  /**
   * The call flow is:
   * UA1                           UAS
   * |                              |
   * |---------- (1)INVITE  ------->|
   * |<--------- (2) 100    --------|
   * |                              |
   * |                              |
   * |<--------- (3) 200OK  --------|
   * |---------- (4) ACK    ------->|
   * |                              |
   * |                              |
   * |---------  (5) BYE  --------->|
   * |                              |
   * |<--------  (6) 200OK ---------|
   * |                              |
   */  
  private void assertInviteBye()   
    throws InvalidArgumentException, ParseException, SipException {
    SipCall call = ua1.createSipCall();
    // (1) send INVITE
    initiateOutgoingCall(call);
    // (2), (3) receive response  
    int times = 0;
    do {
      call.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("wait response - " + call.format(), call);
      times++;
    } while (call.getReturnCode() != Response.OK && times < 2);
    assertEquals("response received", Response.OK, call
        .getLastReceivedResponse().getStatusCode());
    
    // (4) send ACK
    call.sendInviteOkAck();
    assertLastOperationSuccess("send ack - " + call.format(), call);
    
    // (5) send BYE
    assertTrue(call.disconnect());
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      logger.error("Exception in testDoInviteAckBye001", e);
      throw new TckTestException(e);
    }
    // (6) receive 200/BYE
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
   * UA1                           UAS
   * |                              |
   * |--------- (1)MESSAGE  ------->|
   * |                              |
   * |<-------- (2) 200OK    -------|   
   * 
   * |<---------(3)INVITE  ---------|
   * |                              |
   * |--------- (4)200OK  --------->|
   * |                              |
   * |<-------- (5)ACK    ----------|
   * |                              |
   * |<-------- (6)BYE  ------------|
   * |                              |
   * |--------- (7) 200OK --------->|
   * |                              |
   */
  private void assertReqInvite(){

    int cseq = 1;
    SipCall a = ua1.createSipCall();
    // listen to incoming INVITE
    a.listenForIncomingCall();

    // (1) send MESSAGE
    SipTransaction trans = sendMessage(ua1, cseq);
    if (trans == null) {
      fail("Fail to send MESSAGE out.");
    }

    // (2) receive 200/MESSAGE
    EventObject event = ua1.waitResponse(trans, waitDuration);
    if(event == null){
      logger.warn("*** 200 ok of MESSAGE is not received, but case will continue***");
    }

    // (3) receive INVITE
    a.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("a wait incoming call - " + a.format(), a);    
    a.getLastReceivedRequest();

    // (4) send 200/INVITE    
    boolean status = a.sendIncomingCallResponse(Response.OK,
          "testCreateCancel101", 1);
    assertLastOperationSuccess("a send 200 - " + a.format(), a);    
    if (!status) {
      fail("Fail to send 200 response out.");
    }
    
    // (6) receive BYE
    a.listenForDisconnect();
    assertLastOperationSuccess("a listen disc - " + a.format(), a);
    a.waitForDisconnect(waitDuration);
    assertLastOperationSuccess("a wait BYE - " + a.format(), a);

    // (7) send 200/BYE
    a.respondToDisconnect();
    assertLastOperationSuccess("a respond to disc - " + a.format(), a);
  }
}
