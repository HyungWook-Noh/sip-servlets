/** 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipServletMessageTest is used to test the APIs of 
 * javax.servlet.sip.SipServletMessage
 *
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipTransaction;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.AcceptHeader;
import javax.sip.header.AcceptLanguageHeader;
import javax.sip.header.CallInfoHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

public class SipServletMessageTest extends TestBase {
  private static Logger logger = Logger.getLogger(SipServletMessageTest.class);

  private static final String servletName = "SipServletMessage";

  public SipServletMessageTest(String arg0) throws IOException {
		super(arg0);
	}
  /**
   *  UAC                     UAS
   *   |                          |
   *   |         MESSAGE          |
   *   |------------------------->|
   *   |                          |
   *   |           415            |
   *   |<-------------------------|
   */
  @AssertionIds (
    ids = "SipServlet:JAVADOC:SipServletMessage1"
  )
  public void testAddAcceptLanguage001()
      throws InvalidArgumentException, ParseException {
    clientEntryLog();
    
    Response response = assertRespForMessage();
    
    assertEquals(Response.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
    ListIterator<AcceptLanguageHeader> itr =
            response.getHeaders(AcceptLanguageHeader.NAME);
    assertNotNull("Accept-Language headers iterator is null ",itr);
    int count = 0;
    AcceptLanguageHeader cn = null;
    AcceptLanguageHeader us = null;
    while(itr.hasNext()){
      AcceptLanguageHeader header = itr.next();
      count ++;
      Locale locale = header.getAcceptLanguage();
      if(locale.getLanguage().equals(Locale.CHINA.getLanguage())){
        cn = header;
      }else if(locale.getLanguage().equals(Locale.US.getLanguage())){
        us = header;
      }else{
        fail("receive unexpected Accept-Language header:"
            + header.getAcceptLanguage());
      }
    }
    assertEquals("2 Accept-Language headers should be accepted",2,count);
    assertNotNull(cn);
    assertNotNull(us);
    float cnQvalue = cn.getQValue();
    float usQvalue = us.getQValue();
    assertTrue("qvalue of new header should be lower than existing value",
        usQvalue <= cnQvalue);

  }

  @AssertionIds (
    ids = "SipServlet:JAVADOC:SipServletMessage2"
  )
  public void testAddAddressHeader001()
      throws InvalidArgumentException, ParseException {
    clientEntryLog();

    Response resp = assertRespForMessage();

    assertEquals(Response.OK, resp.getStatusCode());
    //check the address headers

    CallInfoHeader callInfo = (CallInfoHeader)resp.getHeader(CallInfoHeader.NAME);
    assertNotNull(callInfo);
    String purp = callInfo.getPurpose();
    assertEquals("icon",purp);
    URI callInfoUri = callInfo.getInfo();
    logger.info("=== got header:" + callInfoUri.toString() + "===");
    assertEquals("http://wwww.example.com/alice/photo.jpg",
                  callInfoUri.toString());

    ListIterator itr = resp.getHeaders("My-Address");
    assertNotNull(itr);
    int count = 0;
    while(itr.hasNext()){
      Header header = (Header)itr.next();
      String headerStr = header.toString().toLowerCase();
      logger.info("=== got header:" + headerStr + "===");
      count ++;
      if(count == 1){
        assertTrue(compareHeader("my-address","sip:tck2@domain.com",headerStr) );
      }else{
        assertTrue(compareHeader("my-address","sip:tck1@domain.com",headerStr) );
      }
    }
    assertEquals(2,count);
  }

  @AssertionIds(
    ids = "SipServlet:JAVADOC:SipServletMessage2",
    desc= "test IllegalArgumentException will be thrown if " +
        "the specified header field is a system header"
  )
  public void testAddAddressHeader101(){
    assertSipMessage();
  }

  @AssertionIds (
    ids = "SipServlet:JAVADOC:SipServletMessage3" )
  public void testAddHeader001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();

    Response resp = assertRespForMessage();
    assertEquals(Response.OK, resp.getStatusCode());

    //check 3 headers set by UAS
    AcceptLanguageHeader acceptLang =
        (AcceptLanguageHeader)resp.getHeader(AcceptLanguageHeader.NAME);
    assertNotNull(acceptLang);
    Locale locale = acceptLang.getAcceptLanguage();
    assertEquals("en",locale.getLanguage());

    ListIterator itr = resp.getHeaders("My-Header");
    assertNotNull(itr);
    int count = 0;
    Map<String,String> expected = new HashMap<String,String>();
    expected.put("header1","my-header");
    expected.put("header2","my-header");
    while(itr.hasNext()){
      Header header = (Header)itr.next();
      assertTrue(expected.containsKey(getMsgHeaderValue(header.toString()).toLowerCase()));
      assertTrue(expected.containsValue(getMsgHeaderName(header.toString()).toLowerCase()));
      expected.remove(getMsgHeaderValue(header.toString()).toLowerCase());
      count ++;
    }
    assertEquals(2, count);
  }

  @AssertionIds (
    ids = "SipServlet:JAVADOC:SipServletMessage3",
    desc= "test IllegalArgumentException will be thrown if the header " +
    "is a system header"
  )
  public void testAddHeader101(){
    assertSipMessage();
  }


  @AssertionIds (
    ids = "SipServlet:JAVADOC:SipServletMessage4" )
  public void testAddParameterableHeader001()
       throws InvalidArgumentException, ParseException{
    clientEntryLog();

    Response resp = assertRespForMessage();
    assertEquals(Response.OK, resp.getStatusCode());

    //check parameterable headers set by UAS
    ContentTypeHeader ctype = (ContentTypeHeader)resp.getHeader(ContentTypeHeader.NAME);
    assertNotNull(ctype);
    assertEquals("text", ctype.getContentType().toLowerCase());
    assertEquals("html", ctype.getContentSubType().toLowerCase());
    assertEquals("iso-8859-4", ctype.getParameter("charset").toLowerCase());

    ListIterator itr = resp.getHeaders(AcceptHeader.NAME);
    assertNotNull(itr);
    int count = 0;
    while(itr.hasNext()){
      AcceptHeader header = (AcceptHeader)itr.next();
      count ++;
      if(count == 1){
        assertEquals("application",header.getContentType().toLowerCase());
        assertEquals("x-private", header.getContentSubType().toLowerCase());
        assertEquals("2", header.getParameter("level"));
      }else{
        assertEquals("application",header.getContentType().toLowerCase());
        assertEquals("sdp", header.getContentSubType().toLowerCase());
        assertEquals("1", header.getParameter("level"));
      }
    }
    assertEquals(2,count);
  }

  @AssertionIds (
    ids = "SipServlet:JAVADOC:SipServletMessage4",
    desc= "test IllegalArgumentException will be thrown if the header " +
    "can not hold parameterable values"
  )
  public void testAddParameterableHeader101(){
    assertSipMessage();
  }

  @AssertionIds (
    ids = "SipServlet:JAVADOC:SipServletMessage5" )
  public void testGetAcceptLanguage001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    sendMsgWithMultiAcceptLangHdrs("testGetAcceptLanguage001");
  }

  @AssertionIds (
    ids = "SipServlet:JAVADOC:SipServletMessage6" )
  public void testGetAcceptLanguages001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    sendMsgWithMultiAcceptLangHdrs("testGetAcceptLanguages001");
  }

  private void sendMsgWithMultiAcceptLangHdrs(String methodName)
        throws InvalidArgumentException, ParseException {
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    AcceptLanguageHeader h1 = hdrFactory.createAcceptLanguageHeader(Locale.US);
    AcceptLanguageHeader h2 = hdrFactory.createAcceptLanguageHeader(Locale.CHINA);
    AcceptLanguageHeader h3 = hdrFactory.createAcceptLanguageHeader(Locale.FRANCE);
    h1.setQValue(0.8f);
    h2.setQValue(1.0f);
    h3.setQValue(0.5f);

    List hdrs = new ArrayList();
    hdrs.add(h1);
    hdrs.add(h2);
    hdrs.add(h3);
    Response resp = assertRespForMessage("OPTIONS",hdrs);
    assertEquals("the reason got from UAS:" + resp.getReasonPhrase()+ ". ",
          Response.OK, resp.getStatusCode());
  }
  

  @AssertionIds(
      ids="SipServlet:JAVADOC:SipServletMessage7"
  )
  public void testGetAddressHeader001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();

    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    AddressFactory addrFactory = ua1.getParent().getAddressFactory();
    Header h1 = hdrFactory.createHeader("My-Header","sip:tck1@domain.com");
    Header h2 = hdrFactory.createHeader("My-Header","sip:tck2@domain.com");

    CallInfoHeader callInfo =
        hdrFactory.createCallInfoHeader(
            addrFactory.createURI("http://wwww.example.com/alice/photo.jpg"));
    callInfo.setParameter("purpose","icon");

    List hdrs = new ArrayList();
    hdrs.add(h1);
    hdrs.add(h2);
    hdrs.add(callInfo);

    simpleCheckRespForMessage(hdrs);
  }

  @AssertionIds(
      ids="SipServlet:JAVADOC:SipServletMessage7",
      desc = "test ServletParseException"
  )
  public void testGetAddressHeader101() throws ParseException {
    clientEntryLog();
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    AddressFactory addrFactory = ua1.getParent().getAddressFactory();
    //set a wrong addr header
    Header h1 = hdrFactory.createHeader("My-Header","sip%tck1.domain.com");
    List hdrs = new ArrayList();
    hdrs.add(h1);
    simpleCheckRespForMessage(hdrs);    
  }
  
  @AssertionIds(
      ids="SipServlet:JAVADOC:SipServletMessage8"
  )
  public void testGetAddressHeaders001() throws ParseException{
    clientEntryLog();

    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();

    Header h1 = hdrFactory.createHeader("My-Header","sip:tck1@domain.com");
    Header h2 = hdrFactory.createHeader("My-Header","sip:tck2@domain.com");

    List hdrs = new ArrayList();
    hdrs.add(h1);
    hdrs.add(h2);
    simpleCheckRespForMessage(hdrs);
  }

  @AssertionIds(
      ids="SipServlet:JAVADOC:SipServletMessage8",
      desc = "test ServletParseException"
  )
  public void testGetAddressHeaders101() throws ParseException {
    clientEntryLog();
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    AddressFactory addrFactory = ua1.getParent().getAddressFactory();
    //set a wrong addr header
    Header h1 = hdrFactory.createHeader("My-Header","sip%tck1.domain.com");
    List hdrs = new ArrayList();
    hdrs.add(h1);
    simpleCheckRespForMessage(hdrs);
  }

  @AssertionIds(
      ids="SipServlet:JAVADOC:SipServletMessage9",
      desc = "test get App session without parameter"
  )
  public void testGetApplicationSession001(){
    assertSipMessage();
  }

  @AssertionIds(
      ids="SipServlet:JAVADOC:SipServletMessage10",
      desc = "test get App session with a boolean parameter"
  )
  public void testGetApplicationSession002()
      throws InvalidArgumentException, ParseException{
    assertSipMessage();
  }

  @AssertionIds(
      ids={"SipServlet:JAVADOC:SipServletMessage11",
          "SipServlet:JAVADOC:SipServletMessage12",
          "SipServlet:JAVADOC:SipServletMessage43",
          "SipServlet:JAVADOC:SipServletMessage48"
          },
      desc = "test get/set/removeAttribute and getAttributeNames, if "
          + "anyone of those assertions doesn't pass the case will fail"
  )
  public void testGetAttribute001(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage13"
  )
  public void testGetCallId001()
      throws InvalidArgumentException, ParseException{
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipServletMessage14",
             "SipServlet:JAVADOC:SipServletMessage49"
          },      
      desc = "getCharacterEncoding/setCharacterEncoding"
  )
  public void testGetCharacterEncoding001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();     

    Response resp = assertRespForMessage();
    assertEquals("the reason got from UAS:" + resp.getReasonPhrase(),
        Response.OK, resp.getStatusCode());
    String contentStr;
    Object content = resp.getContent();
    if(content instanceof String){
      contentStr = (String) content;
    }else{
      logger.warn("*** content got by JainSip is not a string, conversion is needed!***");
      logger.warn("*** content.class=" + content.getClass().getName()+"***");
      logger.info("=== byte array is assumed! ===");
      contentStr = new String((byte[])content);
    }
    assertEquals("hello world", contentStr);

  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage15",
      desc = "test getContent() with content of String"
  )
  public void testGetContent001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    sendMsgWithStringContent("testGetContent001");
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage15",
      desc = "if message's character encoding is not supported by the platform" +
          " UnsupportedEncodingException will be thrown"
  )
  public void testGetContent101(){
    assertSipMessage();
  }


  
  @AssertionIds(
      ids="SipServlet:JAVADOC:SipServletMessage16"
  )
  public void testGetContentLanguage001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    List hdrs = new ArrayList();
    hdrs.add(hdrFactory.createContentLanguageHeader(Locale.ENGLISH));
    simpleCheckRespForMessage(hdrs);    
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage17"
  )
  public void testGetContentLength001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    // just send a string content, and uas try to get content length and check it
    sendMsgWithStringContent("testGetContentLength001");
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage18"
  )
  public void testGetContentType001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    sendMsgWithStringContent("testGetContentType001");
  }

  private void sendMsgWithStringContent(String methodName)
      throws InvalidArgumentException, ParseException {
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    ContentTypeHeader cType = hdrFactory.createContentTypeHeader(
          "text", "plain");
    Request message = assembleRequest(
            "MESSAGE",
            servletName,
            methodName,
            TestConstants.SERVER_MODE_UA,
            1);

    message.setContent("test String", cType);
    SipTransaction trans = ua1.sendRequestWithTransaction(message,
        true, null);
    assertNotNull(ua1.format(), trans);
    EventObject event = ua1.waitResponse(trans, waitDuration);

    simpleCheckResp(event);
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage19"
  )
  public void testGetExpires001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    List hdrs = new ArrayList();
    hdrs.add(hdrFactory.createExpiresHeader(5));
    simpleCheckRespForMessage(hdrs);    
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage19",
      desc = "the Expires header doesn't exist and -1 returned"
  )
  public void testGetExpires002(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage20"
  )
  public void testGetFrom001()throws InvalidArgumentException, ParseException{
    clientEntryLog();
    Request message = assembleRequest(
        "MESSAGE",
        servletName,
        "testGetFrom001",
        TestConstants.SERVER_MODE_UA,
        1);
    FromHeader from = (FromHeader) message.getHeader(FromHeader.NAME);
    //save header detail into content and send it to UAS for checking
    StringBuffer sb = new StringBuffer();
    String displayName = from.getAddress().getDisplayName();
    displayName = (displayName==null ? "":displayName);
    
    sb.append("tag=" + from.getTag() + "||");
    sb.append("displayName=" + displayName + "||");
    sb.append("URI=" + from.getAddress().getURI().toString() + "||");
    Iterator itr = from.getParameterNames();
    if(itr != null){
      while(itr.hasNext()){
        String name = (String)itr.next();
        if(name.equals("tag")) continue;
        String value = from.getParameter(name);
        value = (value == null ? "" : value);
        sb.append(name + "=" + value + "||");
      }
    }
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    message.setContent(sb.toString(), hdrFactory.createContentTypeHeader(
          "text", "plain"));
    SipTransaction trans = ua1.sendRequestWithTransaction(message,
        true, null);
    assertNotNull(ua1.format(), trans);

    EventObject event = ua1.waitResponse(trans, waitDuration);
    simpleCheckResp(event);
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage21"
  )
  public void testGetHeader001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();                                                  
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    Header h1 = hdrFactory.createHeader("My-Header","some header");
    List hdrs = new ArrayList();
    hdrs.add(h1);
    simpleCheckRespForMessage(hdrs);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipServletMessage22",
          "SipServlet:JAVADOC:SipServletMessage56"},
      desc = "test set/getHeaderForm"
  )
  public void testHeaderForm001()
      throws InvalidArgumentException, ParseException{
    //COMMENT: Jain-sip can't get the compact header. For example, if compact
      //form is used, toString should return "i:XXX", and if Long form used the
      //return is "Call-ID:XXX"
      //COMMENT: But Jain-sip will convert the compact header to LONG fomat, so we
      // can not know what the real sip message header looks like.
//      String hdrStr = h.toString();
//      String name = hdrStr.substring(0,hdrStr.indexOf(":"));
//      assertEquals("check if the compact header is used.","i",name.toLowerCase());
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage23"
  )
  public void testGetHeaderNames001()
      throws InvalidArgumentException, ParseException{
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage24"
  )
  public void testGetHeaders001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    Header h1 = hdrFactory.createHeader("My-Header","some header 1");
    Header h2 = hdrFactory.createHeader("My-Header","some header 2");
    List hdrs = new ArrayList();
    hdrs.add(h1);
    hdrs.add(h2);
    simpleCheckRespForMessage(hdrs);
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage25"
  )
  public void testGetLocalAddr001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    Header h1 = hdrFactory.createHeader("My-Header",serverHost);    
    List hdrs = new ArrayList();
    hdrs.add(h1);
    simpleCheckRespForMessage(hdrs);
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage26"
  )
  public void testGetLocalPort001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();

    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    Header h1 = hdrFactory.createHeader("My-Header",serverPort+"");
    List hdrs = new ArrayList();
    hdrs.add(h1);
    simpleCheckRespForMessage(hdrs);
  }

  
  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage27"
  )
  public void testGetMethod001()
      throws InvalidArgumentException, ParseException{
    assertSipMessage();
  }

  
  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage28"
  )
  public void testGetParameterableHeader001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();

    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    ContentTypeHeader cType = hdrFactory.createContentTypeHeader(
          "text", "plain");
    Request message = assembleRequest(
            "MESSAGE",
            servletName,
            "testGetParameterableHeader001",
            TestConstants.SERVER_MODE_UA,
            1);
    cType.setParameter("charset","ISO-8859-4");
    message.setContent("test String", cType);
    SipTransaction trans = ua1.sendRequestWithTransaction(message,
        true, null);
    assertNotNull(ua1.format(), trans);
    EventObject event = ua1.waitResponse(trans, waitDuration);

    simpleCheckResp(event);
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage28",
      desc = "ServletParseException should be thrown if the specified header " +
          "field cannot be parsed as a SIP parameterable object "
  )
  public void testGetParameterableHeader101(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage28",
      desc = "NullPointerException should be thrown if the name is null; "
  )
  public void testGetParameterableHeader102(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage29"
  )
  public void testGetParameterableHeaders001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    AcceptHeader hdr1 = hdrFactory.createAcceptHeader("application","sdp");
    AcceptHeader hdr2 = hdrFactory.createAcceptHeader("application","x-private");
    hdr1.setParameter("level","1");
    hdr2.setParameter("level","2");    
    List hdrs = new ArrayList();
    hdrs.add(hdr1);
    hdrs.add(hdr2);
    simpleCheckRespForMessage(hdrs);
  }


  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage29",
      desc = "ServletParseException should be thrown if the specified header " +
          "field cannot be parsed as a SIP parameterable object "
  )
  public void testGetParameterableHeaders101(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage28",
      desc = "NullPointerException should be thrown if the name is null; "
  )
  public void testGetParameterableHeaders102(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage30"
  )
  public void testGetProtocol001()
      throws InvalidArgumentException, ParseException{
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage31"
  )
  public void testGetRawContent001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    sendMsgWithStringContent("testGetRawContent001");
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage32"
  )
  public void testGetRemoteAddr001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    Response resp = assertRespForMessage();
    assertEquals("the reason got from UAS:" + resp.getReasonPhrase(),
        Response.OK, resp.getStatusCode());
    String remote = getHeaderValue(resp.getHeader("Remote-Addr"));
    logger.info("=== remote address got by the UAS:" + remote + "===");
    if(localHost != null && localHost.equals(remote)){
      return;
    }
    if(getAllAddress().contains(remote)){
      return;
    }
    assertTrue("remote addr got by UAS:" + remote, false);    
  }

  private String getHeaderValue(Header header){
    String hdr = header.toString();
    return hdr.substring(hdr.indexOf(":") + 1).trim();
  }

  private List getAllAddress(){
    try{
      String str = InetAddress.getLocalHost().getHostName();
      InetAddress[] addrs = InetAddress.getAllByName(str);
      List list = new ArrayList();
      for(InetAddress addr: addrs){
        logger.info("=== got local address:" + addr.getHostAddress() + "===");
        list.add(addr.getHostAddress());
      }
      return list;
    }catch(UnknownHostException e){
      logger.warn("*** can't got the local host addresses manually, " +
          "so can't compare the result with them ***");
      return null;
    }
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage33"
  )
  public void testGetRemotePort001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    Response resp = assertRespForMessage();
    assertEquals("the reason got from UAS:" + resp.getReasonPhrase(),
        Response.OK, resp.getStatusCode());
    String port = getHeaderValue(resp.getHeader("Remote-Port"));
    logger.info("=== remote port got by the UAS:" + port + "===");
    assertEquals("the port got by UAS is not equal with the one used by UAC",
        ua1Port+"", port);
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage35",
      desc = "test getSession()"
  )
  public void testGetSession001()
      throws InvalidArgumentException, ParseException{
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage36",
      desc = "test getSession(boolean create). Some containers always create " +
          "the sesson regardless the 'create' value, so the test only asserts " +
          "if the container support the API."
  )
  public void testGetSession002()
      throws InvalidArgumentException, ParseException{
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage37"
  )
  public void testGetTo001()throws InvalidArgumentException, ParseException{
    clientEntryLog();
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    Header h1 = hdrFactory.createHeader("My-Header",serverAddr);
    List hdrs = new ArrayList();
    hdrs.add(h1);
    simpleCheckRespForMessage(hdrs);
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage38"
  )
  public void testGetTransport001()throws InvalidArgumentException, ParseException{
    clientEntryLog();                   
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    Header h1 = hdrFactory.createHeader("My-Header",testProtocol);
    List hdrs = new ArrayList();
    hdrs.add(h1);
    simpleCheckRespForMessage(hdrs);
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage40"
  )
  public void testIsCommitted001()
      throws InvalidArgumentException, ParseException{
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage41"
  )
  public void testIsSecure001()throws InvalidArgumentException, ParseException{
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage44"
  )
  public void testRemoveHeader001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    sendMsgWithMultiAcceptLangHdrs("testRemoveHeader001");
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage44",
      desc = "IllegalArgumentException should be thrown if the header is a " +
          "system header"
  )
  public void testRemoveHeader101(){
    assertSipMessage();
  }

  /**
   *  UAC                     UAS
   *   |                       |
   *   |      MESSAGE          |
   *   |---------------------->|
   *   |                       |
   *   |      200 OK           |
   *   |<----------------------|
   *   |                       |
   *   |       MESSAGE         |
   *   |<----------------------|
   *   |                       |
   *   |      200 OK           |
   *   |---------------------->|
   * @throws InvalidArgumentException
   * @throws ParseException
   */

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage45"
  )
  public void testSend001(){
    assertSipMessageBiWay();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage45"
  )
  public void testSend101(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage46"
  )
  public void testSetAcceptLanguage001()
      throws InvalidArgumentException, ParseException{
    clientEntryLog();
    Response response = assertRespForMessage();
    
    assertEquals(Response.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
    ListIterator<AcceptLanguageHeader> itr =
        response.getHeaders(AcceptLanguageHeader.NAME);
    assertNotNull("Accept-Language headers itrator is null ",itr);
    int count = 0;

    while(itr.hasNext()){
      AcceptLanguageHeader header = itr.next();
      count ++;
      Locale locale = header.getAcceptLanguage();
      assertEquals(Locale.CHINA.getLanguage(), locale.getLanguage());
    }
    assertEquals("1 Accept-Language headers should be accepted",1,count);   
  }

  @AssertionIds (
    ids = "SipServlet:JAVADOC:SipServletMessage47"
  )
  public void testSetAddressHeader001()
      throws InvalidArgumentException, ParseException {
    clientEntryLog();
    Response resp = assertRespForMessage();
    assertEquals(Response.OK, resp.getStatusCode());

    //check the address headers
    ListIterator itr = resp.getHeaders("My-Address");
    assertNotNull(itr);
    int count = 0;
    //only one header exists
    while(itr.hasNext()){
      Header header = (Header)itr.next();
      String headerStr = header.toString().toLowerCase();
      logger.info("=== got header:" + headerStr + "===");
      count ++;
      assertEquals("my-address",getMsgHeaderName(headerStr));
      assertEquals("sip:tck2@domain.com",getMsgHeaderValue(headerStr));

    }
    assertEquals(1,count);
  }

  @AssertionIds (
      ids = "SipServlet:JAVADOC:SipServletMessage47",
      desc = "IllegalArgumentException should be thrown if the header is a system header"
  )
  public void testSetAddressHeader101(){
    assertSipMessage();
  }

  @AssertionIds (
      ids = "SipServlet:JAVADOC:SipServletMessage48",
      desc = "NullPointerException should be thrown if the value is null"
  )
  public void testSetAttribute101(){
    assertSipMessage();
  }

  @AssertionIds (
      ids = "SipServlet:JAVADOC:SipServletMessage49",
      desc = "UnsupportedEncodingException should be thrown if an invalide encoding is used"
  )
  public void testSetCharacterEncoding101(){
    assertSipMessage();
  }

  /**
   * test the setContent on sipRequest
   *  UAC                     UAS
   *   |                          |
   *   |         MESSAGE          |
   *   |------------------------->|
   *   |                          |
   *   |         200 OK           |
   *   |<-------------------------|
   *   |                          |
   *   |  MESSAGE(contain content)|
   *   |<-------------------------|
   *   |                          |
   *   |        200 OK            |
   *   |------------------------->|
   */
  @AssertionIds (
    ids = "SipServlet:JAVADOC:SipServletMessage50"
  )
  public void testSetContent001()throws InvalidArgumentException, ParseException {    
    // send MESSAGE, receive 200 ok, and receive MESSAGE
    RequestEvent reqEvent = triggerUASMsg();
    assertNotNull("request Event is null.",reqEvent);        
    Object content = reqEvent.getRequest().getContent();
    String contentStr;

    if(content instanceof String){
      contentStr = (String) content;
    }else{
      logger.warn("*** content got by JainSip is not a string, conversion is needed!***");
      logger.warn("*** content.class=" + content.getClass().getName());
      logger.info("=== byte array is assumed! ===");
      contentStr = new String((byte[])content);
    }
    assertEquals("test String", contentStr);
    //send 200 OK
    sendResp(reqEvent);
  }

  @AssertionIds (
      ids = "SipServlet:JAVADOC:SipServletMessage50",
      desc = "UnsupportedEncodingException should be thrown if the encoding is not supported"
  )
  public void testSetContent101(){
    assertSipMessage();
  }

  @AssertionIds (
      ids = "SipServlet:JAVADOC:SipServletMessage50",
      desc = "IllegalArgumentException should be thrown if MIME type is unknown"
  )
  public void testSetContent102(){
    assertSipMessage();
  }

  /**
   *   |                          |
   *   |         MESSAGE          |
   *   |------------------------->|
   *   |                          |
   *   |         200 OK           |
   *   |<-------------------------|--.
   *   |                          |  | setContent on the sent Message
   *   |         MESSAGE          |<-
   *   |<-------------------------|
   *   |                          |   
   *   |                          |
   *   |        200 OK            |
   *   |------------------------->|
   *
   */
  @AssertionIds (
      ids = "SipServlet:JAVADOC:SipServletMessage50",
      desc = "IllegalStateException  should be thrown if message has already been sent."
  )
  public void testSetContent103(){

    assertSipMessageBiWay();

  }

  /**
   *
   *  UAC                          UAS
   *   |                            |
   *   |         MESSAGE            |
   *   |--------------------------->|
   *   |                            |
   *   |         200 OK             |
   *   |<---------------------------|
   *   |                            |
   *   |MESSAGE(contain the header) |
   *   |<---------------------------|
   *   |                            |
   *   |           200 OK           |
   *   |--------------------------->|
   */
  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage51"
  )
  public void testSetContentLanguage001()
      throws InvalidArgumentException, ParseException {
    // send MESSAGE, receive 200 ok, and receive MESSAGE
    RequestEvent reqEvent = triggerUASMsg();
    Locale locale = reqEvent.getRequest().getContentLanguage().getContentLanguage();
    assertEquals(Locale.CHINA.getLanguage(), locale.getLanguage());
    //send 200 OK
    sendResp(reqEvent);
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage52"
  )
  public void testSetContentLength001()
      throws InvalidArgumentException, ParseException {
    clientEntryLog();
    Response resp = assertRespForMessage();
    assertEquals("the reason got from UAS:" + resp.getReasonPhrase(),
        Response.OK, resp.getStatusCode());
    //NOTE:Jain sip will compute the real content length according the content
    //    so can't assert the content length by resp.getContentLength
    //   assertEquals(20, resp.getContentLength());    
  }

  /**
   *   |                          |
   *   |         MESSAGE          |
   *   |------------------------->|
   *   |                          |
   *   |         200 OK           |
   *   |<-------------------------|--.
   *   |                          |  | setContentLength on the sent Message
   *   |         MESSAGE          |<-
   *   |<-------------------------|
   *   |                          |
   *   |                          |
   *   |        200 OK            |
   *   |------------------------->|
   *
   */
  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage52",
      desc = "IllegalStateException should be thrown if the message has already been sent."
  )
  public void testSetContentLength101(){
    assertSipMessageBiWay();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage53"
  )
  public void testSetContentType001()
      throws InvalidArgumentException, ParseException {
    clientEntryLog();      
    Response resp = assertRespForMessage();
    assertEquals("the reason got from UAS:" + resp.getReasonPhrase(),
        Response.OK, resp.getStatusCode());
    ContentTypeHeader hdr = (ContentTypeHeader)resp.getHeader(ContentTypeHeader.NAME);
    assertEquals("text",hdr.getContentType());
    assertEquals("plain",hdr.getContentSubType());
  }


  /**
   *
   *  UAC                          UAS
   *   |                            |
   *   |         MESSAGE            |
   *   |--------------------------->|
   *   |                            |
   *   |         200 OK             |
   *   |<---------------------------|
   *   |                            |
   *   |MESSAGE(contain the header) |
   *   |<---------------------------|
   *   |                            |
   *   |           200 OK           |
   *   |--------------------------->|
   */
  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage54"
  )
  public void testSetExpires001()
      throws InvalidArgumentException, ParseException {
    // send MESSAGE, receive 200 ok, and receive MESSAGE
    RequestEvent reqEvent = triggerUASMsg();
    ExpiresHeader hdr =
        (ExpiresHeader)reqEvent.getRequest().getHeader(ExpiresHeader.NAME);
    assertEquals(3,hdr.getExpires());
    //send 200 OK
    sendResp(reqEvent);
  }

  /**
   *
   *  UAC                          UAS
   *   |                            |
   *   |         MESSAGE            |
   *   |--------------------------->|
   *   |                            |
   *   |         200 OK             |
   *   |<---------------------------|
   *   |                            |
   *   |MESSAGE(contain the header) |
   *   |<---------------------------|
   *   |                            |
   *   |           200 OK           |
   *   |--------------------------->|
   */
  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage55"
  )
  public void testSetHeader001()
      throws InvalidArgumentException, ParseException {
    RequestEvent reqEvent = triggerUASMsg();
    ExpiresHeader hdr =
        (ExpiresHeader)reqEvent.getRequest().getHeader(ExpiresHeader.NAME);
    assertEquals(3,hdr.getExpires());
    sendResp(reqEvent);
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage55",
      desc = "IllegalArgumentException should be thrown if the header is a system header"
  )
  public void testSetHeader101(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage55",
      desc = "NullPointerException  should be thrown if the name is null"
  )
  public void testSetHeader102(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage55",
      desc = "NullPointerException  should be thrown if the value is null"
  )
  public void testSetHeader103(){
    assertSipMessage();
  }

  
  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage57"
  )
  public void testSetParameterableHeader001()
      throws InvalidArgumentException, ParseException {
    clientEntryLog();
    Response resp = assertRespForMessage();
    assertEquals(Response.OK, resp.getStatusCode());

    //check parameterable headers set by UAS
    ContentTypeHeader ctype = (ContentTypeHeader)resp.getHeader(ContentTypeHeader.NAME);
    assertNotNull(ctype);
    assertEquals("text", ctype.getContentType().toLowerCase());
    assertEquals("html", ctype.getContentSubType().toLowerCase());
    assertEquals("iso-8859-4", ctype.getParameter("charset").toLowerCase());        
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage57",
      desc = "IllegalArgumentException should be thrown if the header " +
    "can not hold parameterable values"
  )
  public void testSetParameterableHeader101(){
    assertSipMessage();
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage58"
  )
  public void testGetInitialRemoteAddr001()
      throws InvalidArgumentException, ParseException {
    clientEntryLog();
    Response resp = assertRespForMessage();
    assertEquals("the reason got from UAS:" + resp.getReasonPhrase(),
        Response.OK, resp.getStatusCode());
    String remote = getHeaderValue(resp.getHeader("Remote-Addr"));
    logger.info("=== remote address got by the UAS:" + remote + "===");
    if(localHost != null && localHost.equals(remote)){
      return;
    }
    if(getAllAddress().contains(remote)){
      return;
    }
    assertTrue("remote addr got by UAS:" + remote, false);    
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage59"
  )
  public void testGetInitialRemotePort001()
      throws InvalidArgumentException, ParseException {
    clientEntryLog();
    Response resp = assertRespForMessage();
    assertEquals("the reason got from UAS:" + resp.getReasonPhrase(),
        Response.OK, resp.getStatusCode());
    String port = getHeaderValue(resp.getHeader("Remote-Port"));
    logger.info("=== remote port got by the UAS:" + port + "===");
    assertEquals("the port got by UAS is not equal with the one used by UAC",
        ua1Port+"", port);
  }

  @AssertionIds(
      ids = "SipServlet:JAVADOC:SipServletMessage60"
  )
  public void testGetInitialTransport001() throws ParseException {
    clientEntryLog();
    HeaderFactory hdrFactory = ua1.getParent().getHeaderFactory();
    Header h1 = hdrFactory.createHeader("My-Header",testProtocol);
    List hdrs = new ArrayList();
    hdrs.add(h1);
    simpleCheckRespForMessage(hdrs);
  }
  
  /**
   * send a msg to the specified UAS method and UAS send back 200ok and a new
   * msg   
   */
  private RequestEvent triggerUASMsg()
      throws InvalidArgumentException, ParseException {
    assertSipMessage();
    //test request.send()
    boolean listen = ua1.listenRequestMessage();
    logger.debug("=== set listen request message:" + listen + "===");    
    return ua1.waitRequest(waitDuration);
  }

  private void sendResp(RequestEvent request){
    MessageFactory msgFactory = ua1.getParent().getMessageFactory();
    Response resp = null;
    try {
      resp = msgFactory.createResponse(Response.OK, request.getRequest());
      SipTransaction trans = ua1.sendReply(request,resp);
      logger.info("=== sending out 200 response ===");
      //whether or not sending successfully doesn't affect the test case
      logger.info("=== send response "
          + (trans!=null ? "successfully":"failed") + " ===");
//      assertNotNull(trans);
    } catch (ParseException e) {
      logger.error("*** can't create response from request! ***",e);
    }
  }

  private void simpleCheckResp(EventObject event){
    assertNotNull(event);
    if (event instanceof ResponseEvent) {
      ResponseEvent responseEvent = (ResponseEvent) event;
      Response resp = responseEvent.getResponse();      
      assertEquals("the reason got from UAS:" + resp.getReasonPhrase()+ ". ",
          Response.OK, resp.getStatusCode());
    } else {
      failByNoResponse();
    }
  }

  /**
   * send a MESSAGE with specified headers, and wait for a response for the message
   * @param headers the specified headers, the elements are object of
   *      javax.sip.header.Header
   * @return Response
   */
  private Response assertRespForMessage(List headers){    
    SipTransaction trans = sendMessage(ua1, null, null, "MESSAGE", 1, headers);
    return waitResponseForMessage(ua1, trans, waitDuration);
  }

  private Response assertRespForMessage(String sipMethod, List headers){    
    SipTransaction trans = sendMessage(ua1, null, null, sipMethod, 1, headers);
    return waitResponseForMessage(ua1, trans, waitDuration);
  }

  private Response assertRespForMessage(){
    return assertRespForMessage(null);
  }

  /**
   * send a MESSAGE with specified headers, and check if the response is 200 OK. 
   * @param headers
   */
  private void simpleCheckRespForMessage(List headers){
    Response resp = assertRespForMessage(headers);
    assertEquals("the reason gotten from Server:" + resp.getReasonPhrase()+ ". ",
          Response.OK, resp.getStatusCode());
  }

  private static void failByNoResponse() {
    fail("Did not recieve response from server side.");
  }

  private static String getMsgHeaderName(String header){
    return header.substring(0,header.indexOf(":")).trim();
  }
  
  private static String getMsgHeaderValue(String header){
    return header.substring(header.indexOf(":") + 1).trim();
  }

}
