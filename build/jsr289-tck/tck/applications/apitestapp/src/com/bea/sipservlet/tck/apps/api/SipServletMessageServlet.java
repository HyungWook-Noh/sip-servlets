/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipServletMessageServlet is used to test the APIs of 
 * javax.servlet.sip.SipServletMessage
 */
package com.bea.sipservlet.tck.apps.api;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import org.apache.log4j.Logger;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;
import javax.servlet.sip.annotation.SipServlet;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

@SipServlet
( applicationName="com.bea.sipservlet.tck.apps.apitestapp",
  name="SipServletMessage")
public class SipServletMessageServlet extends BaseServlet {
  private static Logger logger = Logger.getLogger(SipServletMessageServlet.class);

  public void testAddAcceptLanguage001(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp =
        req.createResponse(SipServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
    resp.addAcceptLanguage(Locale.CHINA);
    resp.addAcceptLanguage(Locale.US);
    try {
      resp.send();
    } catch (IOException e) {
      logger.error("*** IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }


  public void testAddAddressHeader001(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    //set Call-Info which is a Address header
    try{
      URI callInfoUri = sipFactory.createURI(
          "http://wwww.example.com/alice/photo.jpg");
      Address callInfo = sipFactory.createAddress(callInfoUri);
      callInfo.setParameter("purpose","icon");
      resp.addAddressHeader("Call-Info",callInfo,false);

      URI myAddrUri1 = sipFactory.createURI("sip:tck1@domain.com");
      Address myAddr1 = sipFactory.createAddress(myAddrUri1);
      resp.addAddressHeader("My-Address",myAddr1,false);

      URI myAddrUri2 = sipFactory.createURI("sip:tck2@domain.com");
      Address myAddr2 = sipFactory.createAddress(myAddrUri2);
      resp.addAddressHeader("My-Address",myAddr2,true);

      resp.send();
    }catch(ServletParseException e){
      logger.error("*** create URI and Address error! ***", e);
      throw new TckTestException(e);
    }catch(IOException e){
      logger.error("*** IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  public void testAddAddressHeader101(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    try{
      URI callInfoUri = sipFactory.createURI(
          "http://wwww.example.com/alice/photo.jpg");
      Address addr = sipFactory.createAddress(callInfoUri);
      addr.setParameter("purpose","icon");
      try{
        // set System header which cause the exception thrown
        resp.addAddressHeader("To",addr,false);
      }catch(IllegalArgumentException e){
        resp.send();
        return;
      }
      resp.setStatus(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
          "set Address header to system header but no exception is thrown");
      resp.send();
    }catch(ServletParseException e){
      logger.error("*** create URI and Address error! ***", e);
      throw new TckTestException(e);
    }catch(IOException e){
      logger.error("*** IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }


  public void testAddHeader001(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    resp.addHeader("My-Header", "header1");
    resp.addHeader("My-Header", "header2");
    resp.addHeader("Accept-Language","en");
    try {
      resp.send();
    } catch (IOException e) {
      logger.error("*** IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  public void testAddHeader101(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    try{
      try{
        resp.addHeader("Record-Route","sip:server10.biloxi.com;lr");
      }catch(IllegalArgumentException e){
        resp.send();
        return;
      }
      resp.setStatus(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
          "add a system header but no exception is thrown");
      resp.send();
    } catch (IOException e) {
      logger.error("*** IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }

  }


  public void testAddParameterableHeader001(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    try {
      Parameterable param =
          sipFactory.createParameterable("text/html;charset=ISO-8859-4");
      resp.addParameterableHeader("Content-Type",param,true);
      Parameterable accept1 =
          sipFactory.createParameterable("application/sdp;level=1");
      Parameterable accept2 =
          sipFactory.createParameterable("application/x-private;level=2");
      resp.addParameterableHeader("Accept", accept1, false);
      resp.addParameterableHeader("Accept", accept2, true);
      resp.send();
    } catch (ServletParseException e) {
      logger.error("*** can't create a Parameterable object ***",e);
      throw new TckTestException(e);
    } catch (IOException e) {
      logger.error("*** IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testAddParameterableHeader101(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    try {
      Parameterable param =
          sipFactory.createParameterable("text/html;charset=ISO-8859-4");
      try{
        //Max-Forwards can't hold parameterable value
        resp.addParameterableHeader("Max-Forwards",param,true);
      }catch(IllegalArgumentException e){
        return null;
      }
      return "add a parameterable value to Max-Forwards header but no exception is thrown.";

    } catch (ServletParseException e) {
      logger.error("*** can't create a Parameterable object ***",e);
      throw new TckTestException(e);
    } 
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAcceptLanguage001(SipServletRequest req){
    serverEntryLog();
    Locale locale = req.getAcceptLanguage();
    if(locale.getLanguage().equals(Locale.CHINA.getLanguage())){
      return null;
    }else{
      logger.error("*** get wrong preferred language: " + locale + "***");
      return "get wrong preferred language:" + locale.getLanguage();
    }    
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAcceptLanguages001(SipServletRequest req){
    serverEntryLog();
    Iterator<Locale> itr = req.getAcceptLanguages();

    SipServletResponse resp = null;
    if(itr == null){
      return "no Accept-Language headers found";
    }
    List<String> expectedLang = new ArrayList<String>();
    expectedLang.add(Locale.US.getLanguage());
    expectedLang.add(Locale.CHINA.getLanguage());
    expectedLang.add(Locale.FRANCE.getLanguage());
    int account = 0;
    while(itr.hasNext()){
      Locale locale = itr.next();
      if(expectedLang.contains(locale.getLanguage())){
        expectedLang.remove(locale.getLanguage());
        account ++;
        continue;
      }
      logger.error("*** found unexpected Accept-Language header:\""
         + locale.getLanguage() + "\"");
      return "found unexpected Accept-Language header:\""
              + locale.getLanguage() + "\"";
    }
    if(account == 3){
      return null;
    }else{
      StringBuffer sb = new StringBuffer();
      for(String s : expectedLang){
        sb.append("\"" + s + "\" ");
      }
      return "didn't found the expected headers:" + sb.toString();
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAddressHeader001(SipServletRequest req){
    serverEntryLog();
    try{
      Address addr = req.getAddressHeader("My-Header");
      Address callInfo = req.getAddressHeader("Call-Info");
      if(addr == null || callInfo == null){
        logger.error("***\"My-Header\" or \"Call-Info\" header is null***");
        return "\"My-Header\" or \"Call-Info\" header is null";
      }

      //here the value doesn't include the '<' and '>' because the container can
      //remove them during parsing the Parameterable object.
      String expectedCallInfoValue = "http://wwww.example.com/alice/photo.jpg";
      if( addr.getValue().toLowerCase().equals("sip:tck1@domain.com")
          && "icon".equals(callInfo.getParameter("purpose").toLowerCase())
          && callInfo.getValue().toLowerCase().indexOf(expectedCallInfoValue)>=0 ){
        logger.debug("=== everything is ok ===");
        return null;
      }else{
        logger.error("*** can't get address header correctly, addr.getValue()="
           + addr.getValue() + "; callInfo.getValue=" + callInfo.getValue()
           + "purpose=" + callInfo.getParameter("purpose"));
        return "can't get address header correctly.";
      }
    }catch(ServletParseException e){
      logger.error("***can't parse the addressHeader!***",e);
      return "can't parse the addressHeader!";
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAddressHeader101(SipServletRequest req){
    serverEntryLog();
    try{
      Address addr = req.getAddressHeader("My-Header");
      //if come here the case fails
      return "ServletParseException is not thrown";
    }catch(ServletParseException e){
      return null;
    }
  } 

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAddressHeaders001(SipServletRequest req){
    serverEntryLog();
    try{
      ListIterator<Address> itr = req.getAddressHeaders("My-Header");

      SipServletResponse resp = null;
      if(itr == null){
        return "\"My-Header\" header is null";
      }
      String expected1 = "sip:tck1@domain.com";
      String expected2 = "sip:tck2@domain.com";
      int count = 0;
      while(itr.hasNext()){
        Address addr = itr.next();
        String value = addr.getValue().toLowerCase();
        count ++;
        if(count == 1 && value.indexOf(expected1)<0){
          return "the first header is not \"sip:tck1@domain.com\"";
        }else if(count == 2 && value.indexOf(expected2)<0){
          return "the second header is not \"sip:tck2@domain.com\"";
        }
      }
      if(count != 2){
        return "should receive 2 headers, but " + count;
      }
      return null;
    }catch(ServletParseException e){
      logger.error("***can't parse the addressHeader!***",e);
      return "can't parse the addressHeader!";
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAddressHeaders101(SipServletRequest req){
    serverEntryLog();
    try{
      ListIterator<Address> itr = req.getAddressHeaders("My-Header");
      //if come here the case fails
      return "ServletParseException is not thrown";
    }catch(ServletParseException e){
      return null;
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetApplicationSession001(SipServletRequest req){
    serverEntryLog();
    SipApplicationSession sess = req.getApplicationSession();
     
    if(sess == null){
      return "can't get SipApplicationSession";
    }
    if(sess.getId() == null){
      return "application session ID is null";
    }
    return null;
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetApplicationSession002(SipServletRequest req){
    serverEntryLog();
    // if the param is false, the container can return null or a session, which
    // depends the container implementation, so it is not tested here

    SipApplicationSession sess = req.getApplicationSession(true);
    if(sess == null){
      return "can't get SipApplicationSession";
    }
    if(sess.getId() == null){
      return "application session ID is null";
    }
    return null;
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAttribute001(SipServletRequest req){
    serverEntryLog();
    //test setAttribute
    req.setAttribute("attr1","test1");

    //test getAttribute and verify the previous setAttribute()
    String s = (String)req.getAttribute("attr1");

    if(!"test1".equals(s)){
      return "can't get attribute correctly: expected=test1, real value=" + s;
    }
    req.setAttribute("attr2","test2");

    //test getAttributeNames()
    Enumeration<String> names = req.getAttributeNames();
    if(names == null){
      return "can't get attribute names correctly: null is returned";

    }
    List expected = new ArrayList();
    expected.add("attr1");
    expected.add("attr2");
    int count = 0;
    while(names.hasMoreElements()){
      String name = names.nextElement();
       count ++;
      if(expected.contains(name)){
        expected.remove(name);
        continue;
      }
      return "get a wrong attr name:" + name;
    }
    if( count != 2){
      return "get " +  count + "attr names, but expected 2";
    }
    //test removeAttribute()
    req.removeAttribute("attr2");
    if(req.getAttribute("attr2")!= null){
      return "failed to remove an attribute!";
    }
    return null;
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetCallId001(SipServletRequest req){
    serverEntryLog();

    String callId = req.getCallId();
    if(callId == null){
      return "get null call-id";
    }
    logger.debug("===get Call-Id = " + callId);
    return null;

  }


  public void testGetCharacterEncoding001(SipServletRequest req){
    serverEntryLog();
    //the encoding of req probably is not set, so the result can't be asserted
    //and just be logged
    String enc = req.getCharacterEncoding();
    logger.info("===req.getCharacterEncoding()=" + enc);
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    resp.setCharacterEncoding("US-ASCII");
    try{
    resp.setContent("hello world","text/plain");
    resp.send();
    } catch (UnsupportedEncodingException e) {
      logger.warn("*** can't setContent() because the UnsupportedEncodingExcepiton"
      +", the encoding=US-ASCII; content=hello world");
      throw new TckTestException(e);
    }catch (IOException e) {
      logger.error("*** IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetContent001(SipServletRequest req){
    serverEntryLog();
    try {
      Object content = req.getContent();
      if(!(content instanceof String) ){
        logger.warn("*** the content.toString=" + content + "***");
        return "can't cast the content to String";
      }else{
        if("test String".equals(content)){
          return null;
        }else{
          logger.warn("*** the content=" + content
              + "; expected = \"test String\"***");
          return "get wrong content:\"" + content + "\";expected=\"test String\"";
        }
      }      
    } catch (UnsupportedEncodingException e) {
      logger.warn("***can't getContent() because the UnsupportedEncodingExcepiton"
        +", the encoding=" + req.getCharacterEncoding()
        + " ; content should be \"test String\"");
      return "can't getContent() because the UnsupportedEncodingExcepiton"
        +", the encoding=" + req.getCharacterEncoding()
        + " ; content should be \"test String\"";
    }catch (IOException e) {
      logger.error("***IOException occurs during sending response ***",e);
      return "IOException occurs during getContent";
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetContent101(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    try {
      resp.setContent("test string","text/plain");
    } catch (UnsupportedEncodingException e) {
      return "UnsupportedEncodingException is thrown while set content.";
    }
    // the encoding is absolutely invalide on any platform
    resp.setCharacterEncoding("test-encoding");
    try {
      resp.getContent();
    }catch (UnsupportedEncodingException e) {
      return null;
    } catch (IOException e) {
      return "IOException is thrown while get content";
    }
    return "UnsupportedEncodingException is not thrown while get content";
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetContentLanguage001(SipServletRequest req){
    serverEntryLog();
    Locale locale = req.getContentLanguage();
    SipServletResponse resp = null;
    if(locale == null){
      return "got null content language";
    }else if(locale.getLanguage().equals(Locale.ENGLISH.getLanguage())){
      return null;
    }else{
      return "got wrong content language:" + locale;
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetContentLength001(SipServletRequest req){
    serverEntryLog();
    int leng = req.getContentLength();
    if(leng == "test String".getBytes().length){
      return null;
    }else{
      return "get incorrect length:" + leng;
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetContentType001(SipServletRequest req){
    serverEntryLog();
    String ctype = req.getContentType();
    SipServletResponse resp = null;
    if(ctype == null){
      return "got null content type";
    }else if(ctype.toLowerCase().indexOf("text/plain")>=0){
      return null;
    }else{
      logger.warn("*** expected:\"text/plain\", got \"" + ctype + "\"***");
      return "got incorrect content type:" + ctype;
    }   
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetExpires001(SipServletRequest req){
    serverEntryLog();
    int expires = req.getExpires();       
    if(expires == 5){
      return null;
    }else{
      logger.error("*** got incorrect expires header:" + expires + "***");
      return "got incorrect expires header:" + expires;
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetExpires002(SipServletRequest req){
    serverEntryLog();
    int expires = req.getExpires();
    SipServletResponse resp = null;
    if(expires == -1){
      return null;
    }else{
      logger.error("*** got incorrect expires header:" + expires + "***");
      return "got incorrect expires header:" + expires;
    }    
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetFrom001(SipServletRequest req){
    serverEntryLog();
    try{
      Address from = req.getFrom();
      String reference = (String)req.getContent();
      logger.debug("===reference From value=" + reference + "===");
      Map<String, String> map = getProperties(reference);
      String displayName = from.getDisplayName();
      displayName = displayName==null ? "" : displayName;

      StringBuffer sb  = new StringBuffer();
      boolean pass = true;
      if(!displayName.equals(map.get("displayName"))){
        sb.append("displayName not equals!");
        logger.error("*** expected displayName" + map.get("displayName")
            + "; displayName=" + displayName + "***");
        pass = false;
      }
      URI uri = from.getURI();
      if(! uri.toString().equalsIgnoreCase(map.get("URI"))){
        sb.append("URI (in String) not equals!");
        logger.error("*** expected URI" + map.get("URI")
            + "; URI=" + uri.toString() + "***");
        pass = false;
      }

      Iterator<String> itr = from.getParameterNames();
      if(itr == null){
        sb.append("tag missed!");
        logger.error("*** URI missed ***");
        pass = false;
      }
      while(itr.hasNext()){
        String pName = itr.next();
        String value = from.getParameter(pName);
        value = (value==null ? "" : value);
        if(! value.equals(map.get(pName))){
          sb.append(pName + "is not correct!");
          logger.error("*** parameter of " + pName + "is not right! Expected="
              + map.get(pName) + "but got\"" + value + "\"");
          pass = false;
        }
      }
      
      if(pass){
        return null;
      }else{
        return sb.toString();
      }
    }catch (UnsupportedEncodingException e) {
      logger.warn("***can't get the content for reference" +
          " because the UnsupportedEncodingExcepiton");
      return "can't get the content for reference because " +
          "the UnsupportedEncodingExcepiton";
    }catch (IOException e) {
      logger.error("***IOException occurs during get content of message ***",e);
      return "IOException occurs during get content of message"; 
    }
  }
  private static Map<String, String> getProperties(String str){
    if(str==null || str.trim().length()==0) return null;
    String[] ss = str.split("\\|\\|");
    Map<String, String> map = new HashMap<String, String>();
    for(String s : ss){
      int equalsInd = s.indexOf("=");
      map.put(s.substring(0,equalsInd),s.substring(equalsInd + 1));
    }
    return map;
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetHeader001(SipServletRequest req){
    serverEntryLog();
    String headerValue = req.getHeader("My-Header");
    logger.info("===got My-Header value=\"" + headerValue + "\"===" );

    if("some header".equals(headerValue)){
      return null;
    }else{
      return "got incorrect header value, expected=\"some header\"," +
              "got \"" + headerValue + "\"";
    }
  }

  public void testHeaderForm001(SipServletRequest req){
    serverEntryLog();
    SipServletMessage.HeaderForm form = req.getHeaderForm();
    logger.info("=== In request the headerForm=" + form.toString() + "===");
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    resp.setHeaderForm(SipServletMessage.HeaderForm.COMPACT);
    SipServletMessage.HeaderForm form1 = resp.getHeaderForm();
    if(!form1.equals(SipServletMessage.HeaderForm.COMPACT)){
      resp.setStatus(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
         "headerForm got from resp is not equal to the one setted");
    }
    try{
      resp.send();
    }catch (IOException e) {
      logger.error("***IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetHeaderNames001(SipServletRequest req){
    serverEntryLog();
    Iterator itr = req.getHeaderNames();
    //COMMENTS:according the javadoc,this method can return null
    //if the servlet containers do not allow servlets to access headers
    // using this method
  
    if(itr == null){
      logger.warn("*** getHeaderNames() returns null!! Please check whether or" +
          "not the container support this method");
      return "getHeaderNames() returns null!! Please check whether or" +
          "not the container support this method";
    }
    int count = 0;
    //just simply check the headers count
    //there should be 9 headers in the message:
    //Servlet-Name; testMethodName; Call-Id; cseq; From; To;
    //Route;Max-Forwards; Via
    while(itr.hasNext()){
      count++;
      logger.debug("=== got " + itr.next() + " header ===");
    }

    if(count >= 9){
      return null;
    }else{
     return "got " + count
         + " headers, but expect at least 9, pls. check server side log";
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetHeaders001(SipServletRequest req){
    serverEntryLog();
    ListIterator<String> itr = req.getHeaders("My-Header");
    SipServletResponse resp = null;
    List<String> expected = new ArrayList<String>();
    expected.add("some header 1");
    expected.add("some header 2");

    if(itr == null){
      logger.error("***can't get the header of My-Header!***");
      return "can't get the header of My-Header!";

    }
    boolean valueChecked = true;

    int account = 0;
    StringBuffer sb = new StringBuffer("bad header value(s):");

    while(itr.hasNext()){
      String value = itr.next();
      account ++;
      if(expected.contains(value.toLowerCase())){
        expected.remove(value.toLowerCase());
        continue;
      }else{
        sb.append(value + ";");
        valueChecked = false;
      }
    }

    if(!valueChecked){
      sb.deleteCharAt(sb.lastIndexOf(";")).append(". ");
    }else{
      sb = new StringBuffer();
    }

    if(account != 2){
      sb.append("Got " + account + " headers, but expect 2.");
      valueChecked = false;
    }
    if(valueChecked){
      return null;
    }else{
      return sb.toString();
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetLocalAddr001(SipServletRequest req){
    serverEntryLog();
    String expected1 = null;
    String addr = req.getLocalAddr();
    logger.info("=== req.getLocalAddr()=" + addr + "===");
    expected1 = req.getHeader("My-Header");
    if(expected1 != null && expected1.equals(addr)){
      return null;
    }
    return checkAddr(addr,getAllAddress(), req);
  }

  private String checkAddr(String real, List candidate, SipServletRequest req){
    if(candidate == null){
      logger.error("*** can't check the local address because getting" +
          " all addresses failed***");
      return "can't check the result, because can't get local address in server side " +
              "for comparing reference";
    }
    if(candidate.contains(real)){
      return null;
    }else{
      return "local address got from sip message is not equal with the one got " +
              "from local api";
    }
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
      // some containers bind the following ip in order to listen for any networks
      list.add("0.0.0.0");
      // ipv6
      list.add("0:0:0:0:0:0:0:0");
      return list;
    }catch(UnknownHostException e){
      logger.warn("*** can't got the local host addresses manually, " +
          "so can't compare the result with them ***");
      return null;
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetLocalPort001(SipServletRequest req){
    serverEntryLog();
    String expected = null;
    int port = req.getLocalPort();
    logger.info("=== req.getLocalPort()=" + port + "===");
    expected = req.getHeader("My-Header");
    if(expected != null && expected.equals(port + "")){
      return null;
    }
    return "expected=\"" + expected + "\"; but SipServletMessage.getLocalPort()=\""
              + port + "\"";
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetMethod001(SipServletRequest req){
    serverEntryLog();
    String method = req.getMethod();
    // according javadoc the method name returnd should be all upper-case letters
    if("MESSAGE".equals(method)){
      return null;
    }else{
      return "got method name=" + method + "; but expected=MESSAGE";
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetParameterableHeader001(SipServletRequest req){
    serverEntryLog();
    Parameterable p = null;
    try {
      p = req.getParameterableHeader("Content-Type");
    } catch (ServletParseException e) {
      logger.error("***can't parse the parameterable header of Content-Type! ***",e);
      return "can't parse the parameterable header of Content-Type";

    }
    if("text/plain".equals(p.getValue())
        && "ISO-8859-4".equals(p.getParameter("charset"))){
      return null;
    }else{
      logger.error("*** got incorrect parameter headers:value=" + p.getValue()
          + "; expected=text/plain. parameter expect is charset=ISO-8859-4; "
          + "got charset=" + p.getParameter("charset"));
      return "parameter header got is not equals with the one expected. Pls. " +
              "check the server side log";
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetParameterableHeader101(SipServletRequest req){
    serverEntryLog();
    try {
      // the call-Id is not a parameterable header
      Parameterable p = req.getParameterableHeader("Call-ID");
    } catch (ServletParseException e) {
      return null;
    }
    return "ServletParseException is not thrown when get value from Call-ID header";
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetParameterableHeader102(SipServletRequest req){
    try{
      Parameterable p = req.getParameterableHeader(null);
    }catch(NullPointerException e){
      return null;
    }catch (ServletParseException e) {
      return "ServletParseException is thrown when the name is null";
    }
    return "NullPointerException is not thrown.";
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetParameterableHeaders001(SipServletRequest req){
    serverEntryLog();

    ListIterator<Parameterable> itr = null;
    try{
      itr = (ListIterator<Parameterable>) req.getParameterableHeaders("Accept");
    } catch (ServletParseException e) {
      logger.error("***can't parse the parameterable header of Accept!***",e);
      return "can't parse the parameterable header of Accept";
    }

    Map<String, Map<String,String>> expected =
        new HashMap<String,Map<String,String>>();
    Map<String,String> value1= new HashMap<String,String>();
    value1.put("level","1");
    Map<String,String> value2= new HashMap<String,String>();
    value2.put("level","2");
    expected.put("application/sdp",value1);
    expected.put("application/x-private",value2);
    int account = 0;
    while(itr.hasNext()){
      Parameterable p = itr.next();
      account ++;
      logger.info("=== compare " + account + " headers ===");
      String value = p.getValue().trim();
      Map<String,String> expectedParam = expected.get(value);
      if(expectedParam != null
          && expectedParam.get("level").equals(p.getParameter("level").trim())){
        continue;
      }else{
        logger.error("*** got header value=" + value + "; parameter level="
            + p.getParameter("level") + "; expected value=application/sdp" +
            " or application/x-private; corresponding parameter" +
            " is level=1 and level =2 ***");
        return "Parameterable headers got from client are not same with the expected." +
                "Please check the UAS side log!";
      }
    }
    if(account == 2){
      return null;
    }else {
      logger.error("*** got " + account + "headers! will return 500. ***");
      return "got " + account + "headers, but expected 2";
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetParameterableHeaders101(SipServletRequest req){
    serverEntryLog();
    try {
      // the call-Id is not a parameterable header
      ListIterator<Parameterable> itr =
          (ListIterator<Parameterable>) req.getParameterableHeaders("Call-ID");
    } catch (ServletParseException e) {
      return null;
    }
    return "ServletParseException is not thrown when get value from Call-ID header";
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetParameterableHeaders102(SipServletRequest req){
    try{
      ListIterator<Parameterable> itr =
          (ListIterator<Parameterable>)req.getParameterableHeaders(null);
    }catch(NullPointerException e){
      return null;
    }catch (ServletParseException e) {
      return "ServletParseException is thrown when the name is null";
    }
    return "NullPointerException is not thrown.";
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetProtocol001(SipServletRequest req){
    serverEntryLog();
    String protocol = req.getProtocol();
    //as the javadoc specifies:
    //  For this version of the SIP Servlet API this is always "SIP/2.0".
    // so uppercase will be returned    
    if("SIP/2.0".equals(protocol)){
      return null;
    }else{
      return "protocol=" + protocol  + "the one expected=SIP/2.0";
    }
  }
    
  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetRawContent001(SipServletRequest req){
    serverEntryLog();
    byte[] raw = null;
    boolean got = true;

    try{
      raw = req.getRawContent();
    }catch(IOException e){
      logger.error("*** IOException when got raw content! ***", e);
      return "IOException when got raw content!";
    }

    if(Arrays.equals(raw, "test String".getBytes())){
      return null;
    }else{
      logger.error("*** got bytes=" + bytesToStr(raw) + "; but expected="
          +  bytesToStr("test String".getBytes()));
      return "raw content got from request is not equal with the expected.";
    }    
  }

  private static String bytesToStr(byte[] bytes){
    if(bytes == null) return "";
    StringBuffer sb = new StringBuffer();
    for(byte b : bytes){
      sb.append(b + ":");
    }
    return sb.deleteCharAt(sb.length()-1).toString();    
  }


  public void testGetRemoteAddr001(SipServletRequest req){
    serverEntryLog();
    String remote = req.getRemoteAddr();
    logger.info("=== the remote address got from request:" + remote + "===");
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    resp.setHeader("Remote-Addr", remote);
    try{
      resp.send();
    }catch (IOException e) {
      logger.error("***IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }


  public void testGetRemotePort001(SipServletRequest req){
    serverEntryLog();
    int port = req.getRemotePort();
    logger.info("=== the remote address got from request:" + port + "===");
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    resp.setHeader("Remote-Port", port+"");
    try{
      resp.send();
    }catch (IOException e) {
      logger.error("***IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSession001(SipServletRequest req){
    serverEntryLog();
    SipSession sess = req.getSession();
    if(sess != null){
      return null;
    }else{
      return "got a null session";
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSession002(SipServletRequest req){
    serverEntryLog();
    SipSession sess = req.getSession(false);
    //some container always create a session regardless of the parameter's value
    // so we can't assert the result by the session returned.
    return null;

  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetTo001(SipServletRequest req){
    serverEntryLog();
    Address to = req.getTo();
    SipServletResponse resp = null;
    String urlStr = to.getValue();
    logger.info("===get To header:" + to.toString() + "===");
    logger.info("===the value of the To header:" + urlStr + "===");

    String refer = req.getHeader("My-Header");
    if(urlStr!=null && urlStr.equalsIgnoreCase(refer)){
      return null;
    }else{
      return "the To header's url=" + urlStr + "but the expected=" + refer;
    }   
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetTransport001(SipServletRequest req){
    serverEntryLog();
    String refer = req.getHeader("My-Header");
    String transport = req.getTransport();
    if(transport !=null && transport.equalsIgnoreCase(refer)){
      return null;
    }else{
      return "the transport=" + transport + "but the expected=" + refer;
    }                                        
  }

  public void testIsCommitted001(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = null;
    StringBuffer sb = new StringBuffer();
    boolean stateBad = false;
    if(req.isCommitted()){
      logger.error("*** Before the final response is generated the request has " +
          "already been committed! ***");
      sb.append("Before the final response is generated the request" +
                  " has already been committed! ");
      stateBad = true;
    }

    //create a final response and test the request's state
    resp = req.createResponse(SipServletResponse.SC_OK);
    if(resp.isCommitted()){
      logger.error("*** the response is generated but not sended out, but " +
          "the state of response is committed! ***");
      sb.append("the response is generated but not sended out but " +
              "the state is committed! ");
      stateBad = true;
    }
    if(!req.isCommitted()){
      logger.error("*** the final response has been generated but the " +
          "request's state is also not committed! ***");
      sb.append("The response is generated but not sended out but " +
              "the state is committed!");
      stateBad = true;

    }
    if(stateBad){
      resp.setStatus(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
          sb.toString());
    }
    try{
      resp.send();
    }catch (IOException e) {
      logger.error("***IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testIsSecure001(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = null;
    if(!req.isSecure()){
      return null;
    }else{
      logger.warn("*** request.isSecure()=true ***");
      return "request.isSecure()=true while secure channel is not use";
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testRemoveHeader001(SipServletRequest req){
    serverEntryLog();
    req.removeHeader("Accept-Language");
    if(req.getHeader("Accept-Language") == null){
      return null;
    }else{
      logger.warn("*** Accept-Language header still exists after removed:" +
          "value = " + req.getHeader("Accept-Language") + " ***");
      return "Accept-Language header still exists after removed";
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testRemoveHeader101(SipServletRequest req){
    serverEntryLog();
    try{
      req.removeHeader("Call-ID");
    }catch(IllegalArgumentException e){
      return null;
    }
    return "IllegalArgumentException is not thrown";
  }

  public void testSend001(SipServletRequest req){
    serverEntryLog();
    try{
      req.createResponse(SipServletResponse.SC_OK).send();
      createRequestByOriReq(req).send();      
    }catch(ServletParseException e){
      logger.error("***ServletParseException occurs during create request ***",e);
      throw new TckTestException(e);
    }catch (IOException e) {
      logger.error("***IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testSend101(SipServletRequest req){
    try{
      req.send();
    }catch(IOException e){
      return "IOException thrown during sending request";
    }catch(IllegalStateException e){
      return null;
    }
    return "IllegalStateException is not thrown";
  }


  public void testSetAcceptLanguage001(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp =
        req.createResponse(SipServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
    // the last setting is valid
    resp.addAcceptLanguage(Locale.US);
    resp.setAcceptLanguage(Locale.CHINA);

    try {
      resp.send();
    } catch (IOException e) {
      logger.error("*** IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }


  public void testSetAddressHeader001(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    //set Call-Info which is a Address header
    try{

      URI myAddrUri1 = sipFactory.createURI("sip:tck1@domain.com");
      Address myAddr1 = sipFactory.createAddress(myAddrUri1);
      resp.setAddressHeader("My-Address",myAddr1);
       // the latter one will replace the previous one
      URI myAddrUri2 = sipFactory.createURI("sip:tck2@domain.com");
      Address myAddr2 = sipFactory.createAddress(myAddrUri2);
      resp.setAddressHeader("My-Address",myAddr2);

      resp.send();
    }catch(ServletParseException e){
      logger.error("*** create URI and Address error! ***", e);
      throw new TckTestException(e);
    }catch(IOException e){
      logger.error("*** IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testSetAddressHeader101(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    try{
      URI myAddrUri1 = sipFactory.createURI("sip:tck1@domain.com");
      Address myAddr1 = sipFactory.createAddress(myAddrUri1);
      resp.setAddressHeader("To",myAddr1);
    }catch(ServletParseException e){
      logger.error("*** create URI and Address error! ***", e);
      return "ServletParseException is thrown during creating URI and Address";
    }catch(IllegalArgumentException e){
      return null;
    }
    return "IllegalArgumentException is not thrown.";
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testSetAttribute101(SipServletRequest req){
    serverEntryLog();
    try{
      req.setAttribute("testAttr",null);
    }catch(NullPointerException e){
      return null;
    }
    return "NullPointerException is not thrown while the value is null";
  }
  
  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testSetCharacterEncoding101(SipServletRequest req){
    serverEntryLog();
    try{
      //response's setCharacterEncoding will not throw the exception
      req.setCharacterEncoding("test-encoding");
    }catch(UnsupportedEncodingException e){
      return null;
    }
    return "UnsupportedEncodingException is not thrown";
  }

  public void testSetContent001(SipServletRequest req){
    serverEntryLog();
    try{
      req.createResponse(SipServletResponse.SC_OK).send();
      SipServletRequest message = createRequestByOriReq(req);
      message.setContent("test String", "text/plain");
      message.send();
    }catch(ServletParseException e){
      logger.error("***ServletParseException occurs during create request ***",e);
      throw new TckTestException(e);
    }catch (IOException e) {
      logger.error("***IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testSetContent101(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    resp.setCharacterEncoding("test-encoding");
    try {
      resp.setContent("test string","text/plain");
    } catch (UnsupportedEncodingException e) {
      return null;
    }
    return "UnsupportedEncodingException is not thrown";   
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testSetContent102(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    try{
      // the content type should be invalide on any platform
      resp.setContent(new Object(),"type/subtype");
    }catch(IllegalArgumentException e){
      return null;
    }catch(UnsupportedEncodingException e){
      return "UnsupportedEncodingException is thrown while encoding is not set.";
    }
    return "IllegalArgumentException is not thrown.";
  }

  public void testSetContent103(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = null;
    try {
      resp = req.createResponse(SipServletResponse.SC_OK);
      resp.send();

      try{
        resp.setContent("test String", "text/plain");
      }catch(IllegalStateException e){
        SipServletRequest req1 = createRequestByOriReq(req);
        req1.send();
        return;
      }
      SipServletRequest req2 = createRequestByOriReq(req);

      req2.setHeader(TestConstants.TEST_FAIL_REASON,"IllegalStateException not thrown");
      req2.send();
    } catch (ServletParseException e) {
      logger.error("***ServletParseException occurs during creating request ***",e);
      throw new TckTestException(e);
    } catch (IOException e) {
      logger.error("***IOException occurs during creating or sending a request ***",e);
      throw new TckTestException(e);
    }
  }


  public void testSetContentLanguage001(SipServletRequest req){
    serverEntryLog();
    try{
      req.createResponse(SipServletResponse.SC_OK).send();
      SipServletRequest message = createRequestByOriReq(req);
      message.setContentLanguage(Locale.CHINA);
      message.send();
    }catch(ServletParseException e){
      logger.error("***ServletParseException occurs during create request ***",e);
      throw new TckTestException(e);
    }catch (IOException e) {
      logger.error("***IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }

  }

  public void testSetContentLength101(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = null;
    try {
      resp = req.createResponse(SipServletResponse.SC_OK);
      resp.send();

      try{
        resp.setContentLength(20);
      }catch(IllegalStateException e){
        SipServletRequest req1 = createRequestByOriReq(req);
        req1.send();
        return;
      }
      SipServletRequest req2 = createRequestByOriReq(req);

      req2.setHeader(TestConstants.TEST_FAIL_REASON,"IllegalStateException not thrown");
      req2.send();
    } catch (ServletParseException e) {
      logger.error("***ServletParseException occurs during creating request ***",e);
      throw new TckTestException(e);
    } catch (IOException e) {
      logger.error("***IOException occurs during creating or sending a request ***",e);
      throw new TckTestException(e);
    }
  }
  

  public void testSetContentLength001(SipServletRequest req){
    serverEntryLog();
    try{
      SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
      // actually the content length is 0 but manually set to 20
      resp.setContentLength(20);
      int leng = resp.getContentLength();
      if(leng == 20){
        resp.send();
      }else{
        resp = req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
            "got length=" + leng + ", but expect 20");
        resp.send();
      }
    }catch(IOException e){
      logger.error("*** IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }


  public void testSetContentType001(SipServletRequest req){
    serverEntryLog();
    try{
      SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
      resp.setContentType("text/plain");
      resp.send();
    }catch(IOException e){
      logger.error("*** IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }


  public void testSetExpires001(SipServletRequest req){
    serverEntryLog();
    try{
      req.createResponse(SipServletResponse.SC_OK).send();
      SipServletRequest message = createRequestByOriReq(req);
      message.setExpires(3);
      message.send();
    }catch(ServletParseException e){
      logger.error("***ServletParseException occurs during create request ***",e);
      throw new TckTestException(e);
    }catch (IOException e) {
      logger.error("***IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }


  public void testSetHeader001(SipServletRequest req){
    serverEntryLog();
    try{
      req.createResponse(SipServletResponse.SC_OK).send();
      SipServletRequest message = createRequestByOriReq(req);
      message.setHeader("Expires","3");
      message.send();
    }catch(ServletParseException e){
      logger.error("***ServletParseException occurs during create request ***",e);
      throw new TckTestException(e);
    }catch (IOException e) {
      logger.error("***IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetHeader101(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    try{
      resp.setHeader("From","sip:tck@domain.com");
    }catch(IllegalArgumentException e){
      return null;
    }
    return "IllegalArgumentException is not thrown";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetHeader102(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    try{
      resp.setHeader(null,"sip:tck@domain.com");
    }catch(NullPointerException e){
      return null;
    }
    return "NullPointerException is not thrown";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetHeader103(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    try{
      resp.setHeader("Expires",null);
    }catch(NullPointerException e){
      return null;
    }
    return "NullPointerException is not thrown";
  }


  public void testSetParameterableHeader001(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    try {
      Parameterable param =
          sipFactory.createParameterable("text/html;charset=ISO-8859-4");
      resp.setParameterableHeader("Content-Type",param);
      resp.send();
    } catch (ServletParseException e) {
      logger.error("*** can't create a Parameterable object ***",e);
      throw new TckTestException(e);
    }  catch (IOException e) {
      logger.error("*** IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetParameterableHeader101(SipServletRequest req){
    serverEntryLog();
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    try {
      Parameterable param =
          sipFactory.createParameterable("text/html;charset=ISO-8859-4");
      try{
        //Max-Forwards can't hold parameterable value
        resp.setParameterableHeader("Max-Forwards",param);
      }catch(IllegalArgumentException e){
        return null;
      }
      return "add a parameterable value to Max-Forwards header but no exception is thrown.";

    } catch (ServletParseException e) {
      logger.error("*** can't create a Parameterable object ***",e);
      throw new TckTestException(e);
    }
  }


  public void testGetInitialRemoteAddr001(SipServletRequest req){
    serverEntryLog();
    String remote = req.getInitialRemoteAddr();
    logger.info("=== the remote address got from request:" + remote + "===");
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    if(remote == null) remote = "null";
    resp.setHeader("Remote-Addr", remote);
    try{
      resp.send();
    }catch (IOException e) {
      logger.error("***IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }


  public void testGetInitialRemotePort001(SipServletRequest req){
    serverEntryLog();
    int port = req.getInitialRemotePort();
    logger.info("=== the remote address got from request:" + port + "===");
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    resp.setHeader("Remote-Port", port+"");
    try{
      resp.send();
    }catch (IOException e) {
      logger.error("***IOException occurs during sending response ***",e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy=TESTSTRATEGY_SIMPLEASSERT)
  public String testGetInitialTransport001(SipServletRequest req){
    serverEntryLog();
    String refer = req.getHeader("My-Header");
    String transport = req.getInitialTransport();
    if(transport !=null && transport.equalsIgnoreCase(refer)){
      return null;
    }else{
      return "the transport=" + transport + "but the expected=" + refer;
    }
  }
}
