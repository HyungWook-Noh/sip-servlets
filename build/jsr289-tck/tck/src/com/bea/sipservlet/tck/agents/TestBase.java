/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * Used to implement some basic and common functionalities 
 */

package com.bea.sipservlet.tck.agents;

import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.utils.TestUtil;
import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTestCase;
import org.cafesip.sipunit.SipTransaction;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestBase extends SipTestCase {
  private static Logger logger = Logger.getLogger(TestBase.class);

  protected static final String SERVER_HOST = "server.host";
  protected static final String SERVER_PORT = "server.port";
  protected static final String SERVER_HTTP_PORT = "server.http.port";
  protected static final String SERVER_APP_HTTPROOT = "server.application.httproot";
  
  protected static final String DOMAIN = "domain";
  protected static final String TRANSPORT = "transport";
  protected static final String UA1_USERNAME = "ua1.username";
  protected static final String UA1_DISPNAME = "ua1.displayname";
  protected static final String UA1_PORT = "ua1.port";
  protected static final String UA2_USERNAME = "ua2.username";
  protected static final String UA2_DISPNAME = "ua2.displayname";
  protected static final String UA2_PORT = "ua2.port";
  protected static final String UA3_USERNAME = "ua3.username";
  protected static final String UA3_DISPNAME = "ua3.displayname";
  protected static final String UA3_PORT = "ua3.port";
  protected static final String WAIT_DURATION = "wait.duration";
  // the package for apitest agents
  private static final String apiPackage = "com.bea.sipservlet.tck.agents.api";
  private static final String specPackage = "com.bea.sipservlet.tck.agents.spec";
  private static final String testMethodPrefix = "test";
  
  protected static final String displayName =  "JSR289_TCK";


  protected SipStack sipStack1;
  protected SipStack sipStack2;
  protected SipStack sipStack3;

  protected SipPhone ua1;
  protected SipPhone ua2;
  protected SipPhone ua3;

  protected int proxyPort;

  protected String ua1Host;
  protected String ua2Host;
  protected String ua3Host;

  protected int ua1Port;
  protected int ua2Port;
  protected int ua3Port;

  protected String testProtocol;
  protected String domain;

  protected String ua1UserName;
  protected String ua1DispName;
  protected String ua2UserName;
  protected String ua2DispName;
  protected String ua3UserName;
  protected String ua3DispName;

  protected String ua1Url;
  protected String ua2Url;
  protected String ua3Url;

  protected String localHost;
  protected String serverHost;
  protected int serverPort;
  protected int serverHttpPort;
  protected String serverAppHttpRoot;
  
  protected int waitDuration;

  protected Properties properties1;
  protected Properties properties2;
  protected Properties properties3;

  protected String serverURI;
  protected String serverAddr;
  protected String ua1URI;
  protected String ua1Addr;
  protected String ua2URI;
  protected String ua2Addr;
  protected String ua3URI;
  protected String ua3Addr;

  public TestBase(String arg0) throws IOException, UnknownHostException {
    super(arg0);
    Properties defaultProperties = new Properties();

    defaultProperties.load(new FileInputStream("conf/default.properties"));

    localHost = "localhost";
    localHost = InetAddress.getLocalHost().getHostAddress();

    domain = localHost;
    waitDuration = Integer.parseInt(defaultProperties.getProperty(WAIT_DURATION));
    
    ua1Host = localHost;
    ua1Port = Integer.parseInt(defaultProperties.getProperty(UA1_PORT));
    ua1UserName = defaultProperties.getProperty(UA1_USERNAME);
    ua1DispName = defaultProperties.getProperty(UA1_DISPNAME);
    ua1Url = "sip:" + ua1UserName + "@" + domain;
    ua1URI = ua1Url + ":" + ua1Port;
    ua1Addr = ua1DispName + " <" + ua1URI + ">";
        
    //ua2Host = "127.0.0.1";
    ua2Host = localHost;
    ua2Port = Integer.parseInt(defaultProperties.getProperty(UA2_PORT));
    ua2UserName = defaultProperties.getProperty(UA2_USERNAME);
    ua2DispName = defaultProperties.getProperty(UA2_DISPNAME);
    ua2Url = "sip:" + ua2UserName + "@" + domain;
    ua2URI = ua2Url + ":" + ua2Port;
    ua2Addr = ua2DispName + " <" + ua2URI + ">";

    ua3Host = localHost;
    ua3Port = Integer.parseInt(defaultProperties.getProperty(UA3_PORT));
    ua3UserName = defaultProperties.getProperty(UA3_USERNAME);
    ua3DispName = defaultProperties.getProperty(UA3_DISPNAME);
    ua3Url = "sip:" + ua3UserName + "@" + domain;
    ua3URI = ua3Url + ":" + ua3Port;
    ua3Addr = ua3DispName + " <" + ua3URI + ">";
    
    serverHost = defaultProperties.getProperty(SERVER_HOST);
    serverPort = Integer.parseInt(defaultProperties.getProperty(SERVER_PORT));
    serverHttpPort = Integer.parseInt(defaultProperties.getProperty(SERVER_HTTP_PORT));
    serverAppHttpRoot = defaultProperties.getProperty(SERVER_APP_HTTPROOT);

    testProtocol = defaultProperties.getProperty(TRANSPORT);
    serverURI = "sip:" + displayName + "@" + serverHost + ":" + serverPort;
    serverAddr = displayName + " <" + serverURI + ">";

    properties1 = new Properties();
    //properties1.setProperty("javax.sip.IP_ADDRESS", ua1Host);
    properties1.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
    properties1.setProperty("javax.sip.STACK_NAME", "testAgent");
    properties1.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
    properties1.setProperty("gov.nist.javax.sip.DEBUG_LOG",
        "testAgent1_debug.txt");
    properties1.setProperty("gov.nist.javax.sip.SERVER_LOG",
        "testAgent1_log.txt");
    properties1.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
    properties1.setProperty("sipunit.trace", "true");
    properties1.setProperty("sipunit.test.port", String.valueOf(ua1Port));
    properties1.setProperty("sipunit.test.protocol", testProtocol);
    properties1.setProperty("sipunit.test.domain", domain);
    properties1.setProperty("sipunit.proxy.host", serverHost);
    properties1.setProperty("sipunit.proxy.port", String.valueOf(serverPort));

    properties2 = new Properties();
    //properties2.setProperty("javax.sip.IP_ADDRESS", ua2Host);
    properties2.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
    properties2.setProperty("javax.sip.STACK_NAME", "testAgent2");
    properties2.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
    properties2.setProperty("gov.nist.javax.sip.DEBUG_LOG",
        "testAgent2_debug.txt");
    properties2.setProperty("gov.nist.javax.sip.SERVER_LOG",
        "testAgent2_log.txt");
    properties2.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
    properties2.setProperty("sipunit.trace", "true");
    properties2.setProperty("sipunit.test.port", String.valueOf(ua2Port));
    properties2.setProperty("sipunit.test.protocol", testProtocol);
    properties2.setProperty("sipunit.test.domain", domain);
    properties2.setProperty("sipunit.proxy.host", serverHost);
    properties2.setProperty("sipunit.proxy.port", String.valueOf(serverPort));
    
    properties3 = new Properties();
    properties3.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
    properties3.setProperty("javax.sip.STACK_NAME", "testAgent3");
    properties3.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
    properties3.setProperty("gov.nist.javax.sip.DEBUG_LOG",
        "testAgent3_debug.txt");
    properties3.setProperty("gov.nist.javax.sip.SERVER_LOG",
        "testAgent3_log.txt");
    properties3.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
    properties3.setProperty("sipunit.trace", "true");
    properties3.setProperty("sipunit.test.port", String.valueOf(ua3Port));
    properties3.setProperty("sipunit.test.protocol", testProtocol);
    properties3.setProperty("sipunit.test.domain", domain);
    properties3.setProperty("sipunit.proxy.host", serverHost);
    properties3.setProperty("sipunit.proxy.port", String.valueOf(serverPort));
    
  }

  /*
   * @see SipTestCase#setUp()
   */
  public void setUp() throws Exception {
    SipStack.setTraceEnabled(true);
    sipStack1 = new SipStack(testProtocol, ua1Port, properties1);
    sipStack2 = new SipStack(testProtocol, ua2Port, properties2);
    sipStack3 = new SipStack(testProtocol, ua3Port, properties3);

    ua1 = sipStack1.createSipPhone(serverHost, testProtocol, serverPort, ua1Url);
    ua2 = sipStack2.createSipPhone(serverHost, testProtocol, serverPort, ua2Url);
    ua3 = sipStack3.createSipPhone(serverHost, testProtocol, serverPort, ua3Url);
  }

  /*
   * @see SipTestCase#tearDown()
   */
  public void tearDown() throws Exception {
    ua1.dispose();
    ua2.dispose();
    ua3.dispose();
    
    sipStack1.dispose();
    sipStack2.dispose();
    sipStack3.dispose();
  }

  /**
   * @param method
   * @return
   */
  protected String getMethodName(String method) {

    if ("ACK".equalsIgnoreCase(method)) {
      return Request.ACK;
    } else if ("BYE".equalsIgnoreCase(method)) {
      return Request.BYE;
    } else if ("CANCEL".equalsIgnoreCase(method)) {
      return Request.CANCEL;
    } else if ("INFO".equalsIgnoreCase(method)) {
      return Request.INFO;
    } else if ("INVITE".equalsIgnoreCase(method)) {
      return Request.INVITE;
    } else if ("OPTIONS".equalsIgnoreCase(method)) {
      return Request.OPTIONS;
    } else if ("MESSAGE".equalsIgnoreCase(method)) {
      return Request.MESSAGE;
    } else if ("NOTIFY".equalsIgnoreCase(method)) {
      return Request.NOTIFY;
    } else if ("PRACK".equalsIgnoreCase(method)) {
      return Request.PRACK;
    } else if ("REGISTER".equalsIgnoreCase(method)) {
      return Request.REGISTER;
    } else if ("REFER".equalsIgnoreCase(method)) {
      return Request.REFER;
    } else if ("SUBSCRIBE".equalsIgnoreCase(method)) {
      return Request.SUBSCRIBE;
    } else if ("UPDATE".equalsIgnoreCase(method)) {
      return Request.UPDATE;
    } else if ("PUBLISH".equalsIgnoreCase(method)) {
        return Request.PUBLISH;
    } else {
      throw new IllegalArgumentException("Bad Method Name :[" + method + "]");
    }
  }

  /**
   * Construct a sip request assuming the uac is ua1, and destination is decided
   * by the mode parameter
   * Contact and Expires headers will be added according the mode value
   * the Application-Name header will be added automatically by this method
   * according to the @TargetApplication annotation applied on the agents classes
   * or the methods in those classes.
   *
   * @param sipMethod The Sip method of assembled request.
   * @param cseq Cseq header of assembled request.
   * @param specifiedHeaders
   *      Map of specified headers, key is the headers' name, vlaue is the headers'
   *      value. For single header, the value is a string, and if there are multiple
   *      header value correponding to one header name, the value is a list which
   *      contains the header values
   * @param mode
   *      Mode of server side, can be "proxy" or "ua"
   * @return Request
   */
  protected Request assembleEmptyRequest(
    String sipMethod, int cseq, Map<String, Object> specifiedHeaders, String mode) {
    try {
      HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
      AddressFactory address_factory = ua1.getParent().getAddressFactory();
      MessageFactory message_factory = ua1.getParent().getMessageFactory();
      
      String sipMethod1 = getMethodName(sipMethod);
      //create the start line according to the mode 
      String reqURI = "";
      if(Request.REGISTER.equals(sipMethod1)){
        reqURI = "sip:" + serverHost;
      }else{
        reqURI = TestConstants.SERVER_MODE_UA.equals(mode) ? serverURI : ua2URI;
      }
      URI request_uri = address_factory.createURI(reqURI);
      
      //From header
      String fromStr = ua1Addr + ";tag=" + ua1.generateNewTag();
      // To header
      String toStr = null;
      if(Request.REGISTER.equals(sipMethod1)){
        toStr = ua1Addr;
      }else{
        toStr = TestConstants.SERVER_MODE_UA.equals(mode) ? serverAddr : ua2Addr;
      }
      // Via headers
      ArrayList<Header> via_headers = ua1.getViaHeaders();
      Request message = message_factory.createRequest(request_uri, sipMethod1, 
      		ua1.getParent().getSipProvider().getNewCallId(), 
      		header_factory.createCSeqHeader((long)cseq, sipMethod1), 
      		(FromHeader) header_factory.createHeader(FromHeader.NAME, fromStr), 
      		(ToHeader) header_factory.createHeader(ToHeader.NAME, toStr), 
      		via_headers, 
      		header_factory.createMaxForwardsHeader(5));
      logger.debug("=== created message:" + message + " ===");
      
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
      //another Route header when the server mode is proxy
      if(TestConstants.SERVER_MODE_PROXY.equals(mode)){
        message.addHeader(header_factory.createHeader(
            RouteHeader.NAME, ua2DispName + " <" + ua2URI + ";lr>"));
      }
      // add Application-Name header which is needed by both api and spec test
      String appName = getAppHeader();
      message.addHeader(header_factory.createHeader(TestConstants.APP_HEADER, appName));

      //add the specified headers
      if(specifiedHeaders != null){
        Iterator itr = specifiedHeaders.keySet().iterator();
        while(itr.hasNext()){
          String name = (String)itr.next();
          Object value = specifiedHeaders.get(name);
          if(value == null) continue;
          if(value instanceof String){
            message.addHeader(header_factory.createHeader(name,(String)value));
          }else if(value instanceof List){
            Iterator values = ((List)value).iterator();
            while(values.hasNext()){
              message.addHeader(header_factory.createHeader(name,(String)values.next()));
            }
          }else{
            logger.warn("*** the header value type is invalide:"
                + value.getClass().getName() + ", its toString() will be used!!***");
            message.addHeader(header_factory.createHeader(name,value.toString()));
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
    } catch (ClassNotFoundException e){
      logger.error("*** Class not found when getting the TargetApplication annotation***",e);
      throw new TckTestException(e);
    } catch(NoSuchMethodException e){
      logger.error("*** Method not found when getting the TargetApplication annotation***",e);
      throw new TckTestException(e);
    }

  }

  /**
   *  check the method level annotation first, and then class level, at last
   *  the default "apitestapp" is used.
   * @return the application-name header value, i.e. the application name
   * @throws ClassNotFoundException
   * @throws NoSuchMethodException
   * @throws ParseException
   */
  protected String getAppHeader( )
      throws ClassNotFoundException, NoSuchMethodException, ParseException {

    StackTraceElement stack = getBasePackageStack(new Exception().getStackTrace());
    Class<?> clazz = Class.forName(stack.getClassName());
    Method method = clazz.getMethod(stack.getMethodName());
    if(method.isAnnotationPresent(TargetApplication.class)){
      return method.getAnnotation(TargetApplication.class).value().getValue();
    }else if(clazz.isAnnotationPresent(TargetApplication.class)){
      return clazz.getAnnotation(TargetApplication.class).value().getValue();
    }else{
      return ApplicationName.APITESTAPP.getValue();
    }
  }

  /**
   * Need to be considered later.
   */
  /**
   * Generate a request for spec test which needs a header named "Server"
   * @param sipMethod
   * @param TCKName
   * @param cseq
   * @return Request
   */
  protected Request generateReqWithTCKHdr(
      String sipMethod,
      String TCKName,
      int cseq) {
    Map map = new HashMap();
    map.put(TestConstants.SERVER_HEADER,TestConstants.TCK + "/" + TCKName);
    return assembleEmptyRequest(sipMethod, cseq, map, TestConstants.SERVER_MODE_UA);
  }
  


  /**
   * Construct a sip response
   * @param req The request for creating response.
   * @param ua The SipPhone used to create response.
   * @param statusCode The status code for created response.
   * @param toTag The To tag added in created response.
   * @return resp
   * @throws ParseException
   */
  protected Response createResponse(
  	Request req, SipPhone ua, int statusCode, String toTag) throws ParseException {
    Response resp = ua.getParent().getMessageFactory().createResponse(
        statusCode,
        req);
    ToHeader toHeader = (ToHeader) resp.getHeader(ToHeader.NAME);
    toHeader.setParameter("tag", toTag);
    Address contactAddress = ua.getParent().getAddressFactory()
      .createAddress(req.getRequestURI());
    ContactHeader contact = ua.getParent().getHeaderFactory()
      .createContactHeader(contactAddress);
    resp.setHeader(contact);
    return resp;
  }

  /**
   * Wait for non-100 incoming response
   *
   * @param ua The SipPhone used to wait for the incoming response.
   * @param trans The SipTransaction associated with the incoming response.
   * @param dur The wait duration.
   * @return responseEvent
   */
  protected ResponseEvent waitIncomingResponse(
      SipPhone ua, SipTransaction trans, int dur) {

    EventObject event = ua.waitResponse(trans, dur);
    assertNotNull(event);
    ResponseEvent responseEvent = filterEvent(ua, trans, event);
    return responseEvent;
  }

  /**
   * Filter the event of non-100 response
   *
   * @param ua The SipPhone used to wait for response.
   * @param st The SipTransaction associated with the incoming response.
   * @param event The filtered event
   * @return responseEvent
   */
  protected ResponseEvent filterEvent(
      SipPhone ua, SipTransaction st, EventObject event) {

    assertNotNull(event);
    if (event instanceof ResponseEvent) {
      ResponseEvent responseEvent = (ResponseEvent) event;
      while (Response.TRYING == responseEvent.getResponse().getStatusCode()) {
        EventObject newEvent = ua.waitResponse(st, waitDuration);
        return filterEvent(ua, st, newEvent);
      }
      return responseEvent;
    }
    fail("Event received is not response event.");
    return null;
  }
  
  /**
   * Try multiple times to get a non-100trying response
   * @param ua The SipPhone used to wait for non-100 response.
   * @param st The SipTransaction associated with the response.
   * @return responseEvent
   */
  protected ResponseEvent waitNon100ResponseEvent(SipPhone ua, SipTransaction st, int wait) {
    int tryTimes = 0;
    int maxTryTimes = 4;
    while (true) {
      EventObject event = ua.waitResponse(st, wait);
      if (event instanceof ResponseEvent) {
        ResponseEvent responseEvent = (ResponseEvent) event;
        if (Response.TRYING == responseEvent.getResponse().getStatusCode()) {
          if (tryTimes > maxTryTimes) {
            fail("Can not get a non-100 response after trying " + maxTryTimes + " times.");
          }
          tryTimes++;
        } else {
          return responseEvent;
        }
      }
    }
  }

  protected void assertSipMessageBiWay() {
    assertSipMessageBiWay(null, null, null, 1);
  }

  /**
   * Used to send a sip message to server side, and receive response or wait to expire,
   * Then server will send back a sip message with a result header for final determination.
   * The call flow is:
   * 
   *                      UAC                            UAS
   *                       |                              |
   *                       |---------- (1)MESSAGE ------->|
   *                       |               <Execute determination logic 1>                        
   *                       |<--------- (2)200/500  -------|
   *                       |               <Execute determination logic 2> 
   *                       |<------(3)MESSAGE(result)-----|
   *                <determine the result>                |
   *                       |----------  (4)200    ------->|
   *                       |                              |
   *
   * @param additionalHeaderList  Custom headers of this message
   * @param servletName The servlet name in the server side to handle the message
   * @param methodName  The method in server side to handle the message
   * @param cseq        Cseq header of this message
   */
  protected void assertSipMessageBiWay(
      List<Header> additionalHeaderList, 
      String servletName, 
      String methodName, 
      int cseq) {    
    ua1.listenRequestMessage();
  	
  	SipTransaction trans =
			sendMessage(ua1, servletName, methodName, "MESSAGE", cseq, additionalHeaderList);

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
	  	
  	
    // wait for the MESSAGE request with assertion result
  	Request req = waitIncomingMessage(ua1, waitDuration);
  	if (req == null) {
	    	fail("Did not receive MESSGE from server side.");
	  }	
		// send 200 OK
    boolean status = sendResponseForMessage(ua1, req, Response.OK);
    assertTrue("Send response to MESSAGE", status);
  	
    Header failReasonHd = req.getHeader(TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(getFailReason(failReasonHd));
    }
  }  
  
  
  /**
   * Assert MESSAGE in UA mode and get response
   * This method make use of the call stack information which is generated by creating
   * a Exception to get the servlet name and the method name. In order to get the
   * servlet name and method name correctly the following convention must be followed:
   * 1) the client side class name must be same with the servlet name except the last suffix
   * which is "Test" in client but "Servlet" in server side
   * 2) the method name of the client must be same with the one of the server side.
   *
   * If you don't want to follow the previous rules and want to specify other servlet
   * name and method name, you can use the overloaded method with those parameters.  
   */
  protected void assertSipMessage() {
    assertSipMessage(null, null, null,Request.MESSAGE, 1);
  }
  
   
  
  /**
   * Used to send a sip MESSAGE to server side, and receive response or wait to expire.
   * The call flow is:
   * 
   *                      UAC                            UAS
   *                       |                              |
   *                       |---- (1)MESSAGE/INFO/UPDATE-->|
   *                       |                <Execute determination logic>                                            
   *                       |<--- (2)200/500  -------------|
   *                       |                              |
   *
   * @param additionalHeaderList  The additional headers need to be added into 
   * the Sip message. 
   * @param servletName The private header to be added for TCK.
   * @param methodName  The private header to be added for TCK. 
   * @param cseq        Cseq header of this message.
   */
  protected void assertSipMessage(
      List<Header> additionalHeaderList, 
      String servletName, 
      String methodName,
      String sipMethod, 
      int cseq) {
  	
  	clientEntryLog();
  	if (sipMethod == null) {
    	logger.error("*** The sip method can not be null! ***");
    	throw new TckTestException("The sip method can not be null!");
    }

  	// (1) send MESSAGE  
		SipTransaction trans = null;
    trans =
				sendMessage(ua1, servletName, methodName, sipMethod, cseq, additionalHeaderList);

		if (trans == null) {
			fail("Fail to send MESSAGE out.");
		}
	  // (2) receive response
		Response response = waitResponseForMessage(ua1, trans, waitDuration);
		assertNotNull(response);
    String reason = (response.getReasonPhrase()== null || response.getReasonPhrase().length()==0)
        ? "":"the reason gotten from Server:" + response.getReasonPhrase()+ ". ";
    assertEquals(reason, Response.OK, response.getStatusCode());
		
  }
  
  /**
   * Get the class name
   * @param testClassName The tested class name
   * @return itfName
   */
  protected static String getInterfaceName(String testClassName) {
    String itfName = testClassName;
    if (itfName.endsWith("Test")) {
      itfName = itfName.substring(0, itfName.length() - 4);
    }
    itfName = itfName.substring(itfName.lastIndexOf('.') + 1);
    return itfName;
  }

  /**
   * Get the base package stack
   * @param stackes The stacks gotten via Exception().getStackTrace()
   * @return stack
   */
  protected static StackTraceElement getBasePackageStack(StackTraceElement[] stackes) {
    for (StackTraceElement stack : stackes) {
      if ((stack.getClassName().contains(apiPackage)
            || stack.getClassName().contains(specPackage))
          && stack.getMethodName().startsWith(testMethodPrefix)
          && isPublicMethod(stack.getClassName(),stack.getMethodName())) {
        return stack;
      }
    }
    return stackes[1];
  }

  /**
   * the specified method should be public and have no parameters
   * @param className
   * @param methodName
   * @return
   */
  private static boolean isPublicMethod(String className, String methodName){
    try{
      Class<?> clazz = Class.forName(className);
      return Modifier.isPublic(clazz.getDeclaredMethod(methodName).getModifiers());
    }catch(ClassNotFoundException e){
      logger.warn("*** Class not found when getting the package stack.***", e);
    }catch(NoSuchMethodException e){
      logger.warn("*** No such method:" + methodName
          + "(). the required method might have parameters***", e);
    }
    return false;
  }
  /**
   * Construct a sip request
   * 
   * @param sipMethod the sip method name
   * @param servletName String used to dispatch sip message to specific servlet.
   * @return methodName: String used to distinguish the tested api.
   * @param mode Mode of server side, can be "proxy" or "ua"
   * @param cseq Cseq header of this message
   * @return Request
   *          
   */
  protected Request assembleRequest(String sipMethod, 
    String servletName, String serverMethodName, String mode, int cseq) {
    Map map = new HashMap();
    map.put(TestConstants.SERVLET_HEADER, servletName);
    map.put(TestConstants.METHOD_HEADER, serverMethodName);
    return assembleEmptyRequest(sipMethod, cseq, map, mode);
  }

  /**
   * Compare the received header equals with the expected header name and value
   * @param hdrName The expected header name.
   * @param expectedValue The expected header value.
   * @param hdr The target header.
   * @return result
   */
  protected static boolean compareHeader(String hdrName, String expectedValue, String hdr){
    String name = hdr.substring(0,hdr.indexOf(":")).trim();
    String value = hdr.substring(hdr.indexOf(":") + 1).trim();
    logger.debug("=== expected header name=\""
        + hdrName + "\"; real name=\""+ name + "\"===");
    logger.debug("=== expected header value=\""
        + expectedValue + "\"; real value=\""+ value + "\"===");

    boolean result = (hdrName.equals(name)) && (expectedValue.equals(value));
    return result;
  }

  /**
   * handler interface used to assert sip request
   */
  protected interface SipRequestAssertion{
    public void assertRequest(RequestEvent reqEvent);    
  }
  
  public class SipRequestAssertionProxy implements SipRequestAssertion{
    public void assertRequest(RequestEvent reqEvent){
      Request req = reqEvent.getRequest();
      Header failReasonHd = req.getHeader(TestConstants.TEST_FAIL_REASON);
      if(failReasonHd != null) {
        fail(getFailReason(failReasonHd));
      }
    }
  }
  
  /**
   * handler interface used to assert sip response
   */
  protected interface SipResponseAssertion{
    public void assertResponse(ResponseEvent respEvent);    
  }
  
  public class SipResponseAssertionProxy implements SipResponseAssertion{
    public void assertResponse(ResponseEvent respEvent){
      Response resp = respEvent.getResponse();
      Header failReasonHd = resp.getHeader(TestConstants.TEST_FAIL_REASON);
      if(failReasonHd != null) {
        fail(getFailReason(failReasonHd));
      }
    }
  }
  
  /**
   * Used to send INVITE 
   * @param call The SipCall used to send INVITE.
   * @return 
   */
  protected boolean initiateOutgoingCall(SipCall call) {
    String viaNonProxyRoute = serverHost + ":" + serverPort + "/" + testProtocol;
    return initiateOutgoingCall(call,null, null, null, viaNonProxyRoute);
  }
  
  
  protected boolean initiateOutgoingCall(
      SipCall call, String methodName, String servletName,
      List<Header> additionalHeaderList, String viaNonProxyRoute) {
    return initiateOutgoingCall(
        call,methodName,servletName,
        additionalHeaderList,viaNonProxyRoute,null);
  }
   
  
  /**
   * Used to send INVITE with TCK private headers
   * @param call The SipCall used to send INVITE.
   * @param additionalHeaderList The additional headers used to be add into INVITE.
   * @param viaNonProxyRoute Indicates whether to route the INVITE 
   * 												 via Proxy or some other route.
   * @return status 
   */
  protected boolean initiateOutgoingCall(
  	SipCall call, String methodName, String servletName,
    List<Header> additionalHeaderList, String viaNonProxyRoute,String toUri) {
    StackTraceElement stack = null;
    if(methodName == null || servletName == null){
  	  stack = getBasePackageStack(new Exception().getStackTrace());
    }
    String localServletName =
        servletName==null ? getInterfaceName(stack.getClassName()) : servletName;

    String localMethodName = null;
    boolean needMethodHdr = stack.getClassName().contains(apiPackage);
    if(needMethodHdr){
      localMethodName = methodName==null ? stack.getMethodName() : methodName;
    }
    
    HeaderFactory headerFactory = call.getHeaderFactory();
    Header servletheader;
    Header methodHeader;
    ArrayList<Header> additionalHeaders = new ArrayList<Header>();
    try {
	    servletheader = 
	    	headerFactory.createHeader(TestConstants.SERVLET_HEADER, localServletName);
      additionalHeaders.add(servletheader);
      if(needMethodHdr){
        methodHeader =
	    	  headerFactory.createHeader(TestConstants.METHOD_HEADER, localMethodName);
        additionalHeaders.add(methodHeader);
      }
      String appName = getAppHeader();
      additionalHeaders.add(headerFactory.createHeader(TestConstants.APP_HEADER,appName));
    } catch (ParseException e) {
    	logger.error("*** ParseException when creating TCK private headers ***", e);
    	throw new TckTestException(e);	
    } catch (ClassNotFoundException e){
      logger.error("*** Class not found when getting the TargetApplication annotation***",e);
      throw new TckTestException(e);
    } catch(NoSuchMethodException e){
      logger.error("*** Method not found when getting the TargetApplication annotation***",e);
      throw new TckTestException(e);
    }

    if (additionalHeaderList != null) {
    	for (Header header : additionalHeaderList) {
    		additionalHeaders.add(header);
    	}
    }
    if(toUri == null){
      toUri = serverURI;
    }    
    String fromUri = (call.getParent().getAddress().getURI()).toString();
    
    // send INVITE with private headers added
  	return 
  		call.initiateOutgoingCall(fromUri, toUri, viaNonProxyRoute, additionalHeaders, 
  		    null, null);  	
  }
  
  /**
   * Used to wait incoming MESSAGE request from server side
   * @param ua The SipPhone used to wait MESSAGE.
   * @param duration The wait duration for receiving MESSAGE.
   * @return The received MESSAGE.
   */
  protected Request waitIncomingMessage(SipPhone ua, int duration) {
  	
  	RequestEvent msgEvt = ua.waitRequest(duration);
  	if (msgEvt != null) {

  		Request req = msgEvt.getRequest();
  		assertNotNull(req);
  		return req;
  	}
  	return null;
  	
  }
  
  /**
   * Used to send response for MESSAGE
   * @param ua The SipPhone used to send response for MESSAGE.
   * @param req	The request used for generate response.
   * @param resCode	The status code of response to be generated.
   * @return status
   */
  protected boolean  sendResponseForMessage(SipPhone ua, Request req, int resCode) {

		try {
			ServerTransaction serverTrans = 
				ua.getParent().getSipProvider().getNewServerTransaction(req);
			Response resp = 
				createResponse(req, ua, resCode, ua.generateNewTag()); 
   		serverTrans.sendResponse(resp);
   		assertLastOperationSuccess(
   				"Send 200 OK response for MESSAGE failed - " + ua.format(), ua);
   		return true;
		} catch (SipException e) {
			logger.info("=== Need not to care the exceptions. ===", e);
			return false;
		} catch (ParseException e) {
			logger.info("=== Need not to care the exceptions. ===", e);
			return false;
		} catch (InvalidArgumentException e) {
			logger.info("=== Need not to care the exceptions. ===", e);
			return false;
		}
  }
  
  /**
   * Used to send MESSAGE 
   * @param phone
   *@param cseq Cseq header of MESSAGE. @return trans The transaction generated.
   */
  protected SipTransaction sendMessage(SipPhone phone, int cseq) {
  	return sendMessage(phone,null, null, Request.MESSAGE, cseq, null);
  }
  
 
	/**
   * Used to send specified request with private headers
   * @param phone
   *@param servletName The servlet name in the server side to handle the message.
   * @param methodName The method in server side to handle the message.
   * @param sipMethod The sip method used to be created request.
   * @param cseq The cseq used in MESSAGE.
   * @param additionalHeaderList The private header to be added in MESSAGE. @return trans The transaction generated.
   */
  protected SipTransaction sendMessage(
  	SipPhone phone,
  	String servletName, 
  	String methodName,
  	String sipMethod,
  	int cseq, 
  	List<Header> additionalHeaderList) {
  	StackTraceElement stack = getBasePackageStack(new Exception().getStackTrace());
    String localServletName = servletName==null ? 
        getInterfaceName(stack.getClassName()) : servletName;

    boolean needMethodHdr = stack.getClassName().contains(apiPackage);
    String localMethodName = null;
    if(needMethodHdr){
      localMethodName = methodName==null ? stack.getMethodName() : methodName;
    }
    Request message =
        assembleRequest(sipMethod, localServletName, localMethodName, TestConstants.SERVER_MODE_UA, cseq);      
  	if (additionalHeaderList != null) { 
  		for (Header header : additionalHeaderList) {
  			message.addHeader(header);
  		}
  	}
	  SipTransaction trans = 
	  	phone.sendRequestWithTransaction(message, true, null);
	  logger.debug("---The Sip request is:" + message.toString() + " ---");
	  assertNotNull(phone.format(), trans);
	  assertLastOperationSuccess(phone);
  	return trans;
  }
  
  /**
   * Used to wait response for MESSAGE
   * @param phone The SipCall used to wait for response of MESSAGE.
   * @param trans The transaction used to wait for response of MESSAGE.
   * @param duration The wait duration of waiting for response of MESSAGE.
   * @return response The response received.
   */
  protected Response waitResponseForMessage(
  	SipPhone phone, SipTransaction trans, int duration) {
  	
  	ResponseEvent responseEvent = waitIncomingResponse(phone, trans, duration);
    assertNotNull(responseEvent);
    Response response = responseEvent.getResponse();
    
  	return response;
  }

  /**
   * Used to log current testing method name
   */
  public void clientEntryLog(){
      StackTraceElement stack = getBasePackageStack(new Exception().getStackTrace());
      Logger runtimeLogger = Logger.getLogger(stack.getClassName());
      runtimeLogger.info("=== " + stack.getMethodName() + "() ===");
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
	 * |                              |                        <UAS assertion>
	 * |                              |<--------  (3)180      -------|
	 * |                        <PROXY Cancel>                       |
	 * |                              |---------- (4)CANCEL  ------->|
	 * |                              |                          Assertion
	 * |                              |<--------  (5)200      -------|
	 * |                              |                              |
	 */
	protected void assertSipInviteProxyCancel(String servletName,
																						String methodName,
																						SipRequestAssertion reqAssertion) {
	  clientEntryLog();
	  StackTraceElement stack = getBasePackageStack(new Exception()
						.getStackTrace());
		String localServletName = (TestUtil.hasText(servletName)) ? servletName
						: getInterfaceName(stack.getClassName());
		String localMethodName = null;
    if(stack.getClassName().contains(apiPackage)){
      localMethodName = (TestUtil.hasText(methodName)) ?
          methodName : stack.getMethodName();
    }
    // create INVITE request
		Request reqUA1 = assembleRequest(Request.INVITE, localServletName,
						localMethodName, TestConstants.SERVER_MODE_PROXY, 1);

		// UA2 begin listen request
		ua2.listenRequestMessage();

		// ///////////////////////// begin call flow
		// ////////////////////////////
		// (1) UA1 Send INVITE
		SipTransaction transUA1 = ua1.sendRequestWithTransaction(reqUA1, false,
						null);
		assertNotNull(ua1.format(), transUA1);

		// (2) UA2 Receive INVITE
    Request inviteReq = null;
    do {
      RequestEvent eventUA2 = ua2.waitRequest(waitDuration);
      assertNotNull(eventUA2);
      inviteReq = eventUA2.getRequest();
    } while (inviteReq == null || !Request.INVITE.equals(inviteReq.getMethod()));

		// (3) UA2 send back 180/INVITE
		try {
			ServerTransaction serverTransUA2 = ua2.getParent().getSipProvider()
							.getNewServerTransaction(inviteReq);
			Response inv180Resp = createResponse(inviteReq, ua2, 180, ua2
							.generateNewTag());
			// transfer specific headers
			Header servletNameHd = inviteReq
							.getHeader(TestConstants.SERVLET_HEADER);
			Header methodNameHd = inviteReq
							.getHeader(TestConstants.METHOD_HEADER);
			inv180Resp.addHeader(servletNameHd);
			inv180Resp.addHeader(methodNameHd);

			serverTransUA2.sendResponse(inv180Resp);
		} catch (Exception e) {
			logger.error("Exception when sending back 180/INVITE", e);
			throw new TckTestException(e);
		}

		// (4) UA2 receive CANCEL and assert
		RequestEvent eventUA2_2 = ua2.waitRequest(waitDuration);
		assertNotNull(eventUA2_2);
		assertEquals("Unexpected request", Request.CANCEL, 
    		(eventUA2_2.getRequest()).getMethod());
		if (reqAssertion != null) {
			reqAssertion.assertRequest(eventUA2_2);
		}

		// (5) UA2 send 200/CANCEL
		try {
			Request cancelReq = eventUA2_2.getRequest();
			Response cancel200Resp = ua2.getParent().getMessageFactory()
							.createResponse(Response.OK, cancelReq);
			ua2.sendReply(eventUA2_2, cancel200Resp);
		} catch (Exception e) {
			logger.error("Exception when sending back 200/CANCEL", e);
      throw new TckTestException(e);
    }
	}
	public static String getFailReason(Header failReasonHd){
	  if(failReasonHd == null) return "";
	  return failReasonHd.toString().replaceFirst(TestConstants.TEST_FAIL_REASON + ":", "").trim();
	}

}

