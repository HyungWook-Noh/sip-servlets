/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * B2buaHelperServlet is used to test the APIs of 
 * javax.servlet.sip.B2buaHelper
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.UAMode;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.utils.TestUtil;

@javax.servlet.sip.annotation.SipServlet(name = "B2buaHelper")
public class B2buaHelperServlet extends BaseServlet {
  private static final long serialVersionUID = 3554858456471879038L;
  private static Logger logger = Logger.getLogger(B2buaHelperServlet.class);
  private static URI ua2Uri;

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateRequest001(SipServletRequest req) {
    serverEntryLog();
    
    SipServletRequest newReq = req.getB2buaHelper().createRequest(req);  
    //The From header field of the new request has a new tag chosen by the container. 
    Address from1 = req.getFrom();
    Address from2 = newReq.getFrom();
    if(!from1.getURI().equals(from2.getURI()) 
        || from1.getParameter("tag").equals(from2.getParameter("tag"))){
      return "The new From header is incorrect";
    }
    //The To header field of the new request has no tag.
    Address to1 = req.getTo();
    Address to2 = newReq.getTo();
    if(!to1.getURI().equals(to2.getURI())
        || TestUtil.hasText(to2.getParameter("tag"))){
      return "The new To header is incorrect";
    }
    // The new request (and the corresponding SipSession)is assigned a new Call-ID. 
    if(newReq.getCallId().equals(req.getCallId())){
      return "The new Call-ID header is incorrect";
    }
    //Record-Route and Via header fields are not copied. As usual, the container 
    // will add its own Via header field to the request when it's actually sent 
    // outside the application server. 
    ListIterator<String> it = newReq.getHeaders("Via");
    if(it != null){
      while(it.hasNext()){
        String via = it.next();
        if(!via.contains(to1.getURI().toString())) {
          return "The new Via headers are incorrect";
        }
      }
    }
    
    return null;
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreateRequest002(SipServletRequest req) throws IOException,
      IllegalArgumentException, TooManyHopsException {
    serverEntryLog();

    String method = req.getMethod();
    B2buaHelper b2buaHelper = req.getB2buaHelper();
    if("INVITE".equals(method)){
      if(ua2Uri == null)ua2Uri = getUa2Uri(req);
      Map<String, List<String>> headerMap = constructHeaderMap(req, ua2Uri);
      if (ua2Uri != null) {
        // create new request        
        SipServletRequest newReq = b2buaHelper.createRequest(req, true, headerMap);
        newReq.setRequestURI(ua2Uri);

        // test linked session
        SipSession session = req.getSession();
        SipSession newSession = newReq.getSession();
        SipSession tmpSession = b2buaHelper.getLinkedSession(session);
        if (tmpSession == null || !newSession.equals(tmpSession)) {
          req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
              "Fail to get linked SipSession through B2buaHelper.").send();
          return;
        }

        // test linked request
        SipServletRequest reqTmp = b2buaHelper.getLinkedSipServletRequest(req);
        if (reqTmp == null || !newReq.equals(reqTmp)) {
          req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
              "Fail to get linked SipServletRequest through B2buaHelper.").send();
          return;
        }

        newReq.send();
      }      
    } else if ("ACK".equals(method)) {
      SipSession linkedSession = b2buaHelper.getLinkedSession(req.getSession());
      List<SipServletMessage> msgs = b2buaHelper.getPendingMessages(linkedSession, UAMode.UAC);
      if (msgs.size() != 1 || !(msgs.get(0) instanceof SipServletResponse)) {
        logger.error("*** Can not get pending response from linked SipSession ***");
        return;
      }

      SipServletResponse res = (SipServletResponse)msgs.get(0);
      res.createAck().send();
    } else if ("BYE".equals(method)) {
      Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
      SipSession linkedSession = b2buaHelper.getLinkedSession(req.getSession());
      SipServletRequest newReq = b2buaHelper.createRequest(linkedSession, req, headerMap);
      newReq.send();
    } 
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateRequest101(SipServletRequest req){
    serverEntryLog();
    Map<String,List<String>> map = new HashMap<String,List<String>>();
    List<String> list = new ArrayList<String>();
    list.add("invalideCallId");
    map.put("Call-ID",list);
    try{
      req.getB2buaHelper().createRequest(req,true,map);
    }catch(IllegalArgumentException e){
      return null;
    }catch(TooManyHopsException e){
      logger.error("*** TooManyHopsException occures ***", e);
      return "TooManyHopsException is thrown";
    }
    return "IllegalArgumentException is not thrown";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateRequest102(SipServletRequest req){
    serverEntryLog();
    Map<String,List<String>> map = new HashMap<String,List<String>>();
    List<String> list = new ArrayList<String>();
    list.add("testValue");
    map.put("Test-Header",list);
    try{
      req.getB2buaHelper().createRequest(null,true,map);
    }catch(IllegalArgumentException e){
      logger.error("*** IllegalArgumentException occures ***", e);
      return "IllegalArgumentException is thrown";
    }catch(TooManyHopsException e){
      logger.error("*** TooManyHopsException occures ***", e);
      return "TooManyHopsException is thrown";
    }catch(NullPointerException e){
      return null;
    }
    return "NullPointerException is not thrown";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateRequest103(SipServletRequest req){
    serverEntryLog();
    Map<String,List<String>> map = new HashMap<String,List<String>>();
    List<String> list = new ArrayList<String>();
    list.add("testValue");
    map.put("Test-Header",list);
    try{
      // the reqest's Max-Forwards is 0
      req.getB2buaHelper().createRequest(req,true,map);
    }catch(IllegalArgumentException e){
      logger.error("*** IllegalArgumentException occures ***", e);
      return "IllegalArgumentException is thrown";
    }catch(TooManyHopsException e){
      return null;
    }
    return "TooManyHopsException is not thrown";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateRequest104(SipServletRequest req){
    serverEntryLog();
    Map<String,List<String>> map = new HashMap<String,List<String>>();
    List<String> list = new ArrayList<String>();
    list.add("testValue");
    map.put("Test-Header",list);
    //for the second request
    Map<String,List<String>> map2 = new HashMap<String,List<String>>();
    List<String> list2 = new ArrayList<String>();
    list2.add("invalideCallId");
    map2.put("Call-ID",list);
    try{
      SipServletRequest req1 = req.getB2buaHelper().createRequest(req,false,map);
      SipSession sess1 = req1.getSession();
      //will throw IllegalArgumentException
      try{
        req.getB2buaHelper().createRequest(sess1,req,map2);
      }catch(IllegalArgumentException e){
        return null;
      }
    }catch(IllegalArgumentException e){
      return "IllegalArgumentException is thrown before createRequest() is invoked";
    }catch(TooManyHopsException e){
      logger.error("*** TooManyHopsException occures ***", e);
      return "TooManyHopsException is thrown before createRequest() is invoked";
    }
    return "IllegalArgumentException is not thrown";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateRequest105(SipServletRequest req){
    serverEntryLog();
    Map<String,List<String>> map = new HashMap<String,List<String>>();
    List<String> list = new ArrayList<String>();
    list.add("testValue");
    map.put("Test-Header",list);

    try{
      SipServletRequest req1 = req.getB2buaHelper().createRequest(req,false,map);
      SipSession sess1 = req1.getSession();
      //will throw NullPointerException
      try{
        req.getB2buaHelper().createRequest(sess1,null,map);
      }catch(NullPointerException e){
        return null;
      }
    }catch(IllegalArgumentException e){
      return "IllegalArgumentException is thrown before createRequest() is invoked";
    }catch(TooManyHopsException e){
      logger.error("*** TooManyHopsException occures ***", e);
      return "TooManyHopsException is thrown before createRequest() is invoked";
    }
    return "IllegalArgumentException is not thrown";
  }

  public void testCreateResponseToOriginalRequest101(SipServletRequest req){
    serverEntryLog();
    SipSession sess = req.getSession();
    try{
      req.createResponse(SipServletResponse.SC_OK).send();
      try{
      req.getB2buaHelper()
          .createResponseToOriginalRequest(sess,SipServletResponse.SC_SERVER_INTERNAL_ERROR,"");
      }catch(IllegalStateException e){
        SipServletRequest req1 = createRequestByOriReq(req);
        req1.send();
      }
    }catch(IOException e){
      logger.error("*** IOException is thrown during sending message***",e);
      throw new TckTestException(e);
    }catch(ServletParseException e){
      logger.error("*** IOException is thrown during creating request***",e);
      throw new TckTestException(e);
    }
  }

  public void testCreateResponseToOriginalRequest102(SipServletRequest req){
    serverEntryLog();
    SipSession sess = req.getSession();
    try{
      req.createResponse(SipServletResponse.SC_OK).send();
      sess.invalidate();
      try{
      req.getB2buaHelper()
          .createResponseToOriginalRequest(sess,SipServletResponse.SC_OK,"");
      }catch(IllegalArgumentException e){
        SipServletRequest req1 = createRequestByOriReq(req);
        req1.send();
      }
    }catch(IOException e){
      logger.error("*** IOException is thrown during sending message***",e);
      throw new TckTestException(e);
    }catch(ServletParseException e){
      logger.error("*** IOException is thrown during creating request***",e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetLinkedSession101(SipServletRequest req){
    serverEntryLog();
    Map<String,List<String>> map = new HashMap<String,List<String>>();
    List<String> list = new ArrayList<String>();
    list.add("testValue");
    map.put("Test-Header",list);
    try{
      SipServletRequest req1 = req.getB2buaHelper().createRequest(req,true,map);
      SipSession sess1 = req1.getSession();
      sess1.invalidate();
      try{
      req.getB2buaHelper().getLinkedSession(sess1);
      }catch(IllegalArgumentException e){
        return null;
      }
      return "IllegalArgumentException is not thrown.";
    }catch(TooManyHopsException e){
      logger.error("*** TooManyHopsException occures ***", e);
      return "TooManyHopsException is thrown when creating a request according" +
          "original request.";
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetPendingMessages101(SipServletRequest req){
    serverEntryLog();
    Map<String,List<String>> map = new HashMap<String,List<String>>();
    List<String> list = new ArrayList<String>();
    list.add("testValue");
    map.put("Test-Header",list);
    try{
      SipServletRequest req1 = req.getB2buaHelper().createRequest(req,true,map);
      SipSession sess1 = req1.getSession();
      sess1.invalidate();
      try{
      req.getB2buaHelper().getPendingMessages(sess1,UAMode.UAC);
      }catch(IllegalArgumentException e){
        return null;
      }
      return "IllegalArgumentException is not thrown.";
    }catch(TooManyHopsException e){
      logger.error("*** TooManyHopsException occures ***", e);
      return "TooManyHopsException is thrown when creating a request according" +
          "original request.";
    }
  }
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testLinkUnlinkSipSessions001(SipServletRequest req) 
    throws IOException, IllegalArgumentException, TooManyHopsException {
    serverEntryLog();

    B2buaHelper b2buaHelper = req.getB2buaHelper();
    Map<String, List<String>> headerMap = constructHeaderMap(req, null);

    SipSession originalSession = req.getSession();
    SipServletRequest newReq = b2buaHelper.createRequest(req, false, headerMap);
    SipSession secondSession = newReq.getSession();
    SipSession tmpSession = b2buaHelper.getLinkedSession(originalSession);
    if (tmpSession != null) {
      return "Fail to create unlinked SipServletRequest through B2buaHelper.";
    }

    // link the two SipSession
    b2buaHelper.linkSipSessions(originalSession, secondSession);
    //assertion 1
    tmpSession = b2buaHelper.getLinkedSession(originalSession);
    if (tmpSession == null || !tmpSession.equals(secondSession)) {
      return "Fail to link two SipSession through B2buaHelper.";
    }

    // assertion 2
    SipServletRequest newReq2 = b2buaHelper.createRequest(req, false, headerMap);
    SipSession secondSession2 = newReq2.getSession();
    boolean isExceptionThrown = false;
    try {
      b2buaHelper.linkSipSessions(originalSession, secondSession2);
    } catch (IllegalArgumentException e) {
      isExceptionThrown = true;
    }
    if (!isExceptionThrown) {
      return "More than one SipSession can be linked to a SipSession through B2buaHelper.";
    }

    // assertion 3
    b2buaHelper.unlinkSipSessions(originalSession);
    tmpSession = b2buaHelper.getLinkedSession(originalSession);
    if (tmpSession != null) return "Fail to get unlink SipSession through B2buaHelper.";
    
    return null;
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testLinkSipSessions101(SipServletRequest req){
    serverEntryLog();
    Map<String,List<String>> map = new HashMap<String,List<String>>();
    List<String> list = new ArrayList<String>();
    list.add("testValue");
    map.put("Test-Header",list);
    try{
      B2buaHelper helper = req.getB2buaHelper();
      SipServletRequest req1 = helper.createRequest(req,true,map);
      
      SipServletRequest req2 = helper.createRequest(req);
      try{
      //throw IllegalArgumentException
        helper.linkSipSessions(req.getSession(), req2.getSession());
      }catch(IllegalArgumentException e){
        return null;
      }
      return "IllegalArgumentException is not thrown.";
    }catch(TooManyHopsException e){
      logger.error("*** TooManyHopsException occures ***", e);
      return "TooManyHopsException is thrown when creating a request according" +
          "original request.";
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testLinkSipSessions102(SipServletRequest req){
    serverEntryLog();
    try{
      req.getB2buaHelper().linkSipSessions(req.getSession(),null);
    }catch(NullPointerException e){
      return null;
    }
    return "NullPointerException is not returned";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testUnlinkSipSessions101(SipServletRequest req){
    serverEntryLog();
    try{
      req.getB2buaHelper().unlinkSipSessions(req.getSession());
    }catch(IllegalArgumentException e){
      return null;
    }
    return "IllegalArgumentException is not returned";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreateCancel001(SipServletRequest req) throws IOException,
  IllegalArgumentException, TooManyHopsException{
    serverEntryLog();

      String method = req.getMethod();
      B2buaHelper b2buaHelper = req.getB2buaHelper();

        if("INVITE".equals(method)){
          if(ua2Uri == null)ua2Uri = getUa2Uri(req);
          Map<String, List<String>> headerMap = constructHeaderMap(req, ua2Uri);
          if (ua2Uri != null) {
            // create new request        
            SipServletRequest newReq = b2buaHelper.createRequest(req, true, headerMap);
            newReq.setRequestURI(ua2Uri);
            newReq.send();
          }      
        } else if ("ACK".equals(method) || "BYE".equals(method)) {
          Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
          SipSession linkedSession = b2buaHelper.getLinkedSession(req.getSession());
          SipServletRequest newReq = b2buaHelper.createRequest(linkedSession, req, headerMap);
          newReq.send();
        } else if("CANCEL".equals(method)){
          b2buaHelper.createCancel(b2buaHelper.getLinkedSession(req.getSession())).send();
        }
  }
  
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateCancel101(SipServletRequest req) {
    try {
      req.getB2buaHelper().createCancel(null);
    } catch (NullPointerException e) {
      return null;
    }
    return "Can not catch NullPointerException";
  }
  
  
  
  private Map<String, List<String>> constructHeaderMap(SipServletRequest req, URI uri){
    //Create new Request
    Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
    // Construct the headerMap as follows:
    Address from = req.getTo();
    List<String> fromvalues = new ArrayList<String>();
    fromvalues.add(from.toString());
    headerMap.put("From", fromvalues);

    if (uri != null) {
      List<String> tovalues = new ArrayList<String>();
      tovalues.add(uri.toString());
      headerMap.put("To", tovalues);
    }

    List<String> contactvalues = new ArrayList<String>();
    contactvalues.add(from.toString());
    headerMap.put("Contact", contactvalues);
    
    return headerMap;    
  }
  
  @Override
  protected void doProvisionalResponse(SipServletResponse resp)
      throws ServletException, IOException {
    SipServletRequest currentReq = resp.getRequest();
    B2buaHelper b2buaHelper = currentReq.getB2buaHelper();
    SipSession session = resp.getSession();
    SipSession originalSession = b2buaHelper.getLinkedSession(session);
    SipServletResponse originalResp = b2buaHelper
        .createResponseToOriginalRequest(originalSession, resp.getStatus(),
            null);
    originalResp.send();     
  }  
  
  @Override
  public void doSuccessResponse(SipServletResponse resp){
    try {
      String methodName = resp.getHeader(TestConstants.METHOD_HEADER);
      if ("testCreateRequest002".equals(methodName)||
          "testCreateCancel001".equals(methodName)) {
        SipServletRequest currentReq = resp.getRequest();
        B2buaHelper b2buaHelper = currentReq.getB2buaHelper();
        SipServletRequest originalReq = b2buaHelper.getLinkedSipServletRequest(currentReq);

        if (!TestUtil.hasText(methodName)) {
          originalReq.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
              "Fail to get method header from UA2 response.").send();
        }


        String method = resp.getMethod();
        SipSession session = resp.getSession();
        SipSession originalSession = b2buaHelper.getLinkedSession(session);
        if("INVITE".equals(method)){
          SipServletResponse originalResp = b2buaHelper.createResponseToOriginalRequest(
              originalSession, SipServletResponse.SC_OK, null);
          if(originalResp == null){
            originalReq.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
            "Fail to create response to original request through B2buaHelper.").send();
          } else{
            originalResp.send(); 
          }          
        }
        else if ("BYE".equals(method)){
          List<SipServletMessage> pendingList = b2buaHelper
            .getPendingMessages(originalSession, UAMode.UAS);
          if(pendingList == null || pendingList.size() != 1){
            originalReq.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
              "Fail to get pending message list through B2buaHelper.").send();
          }
          else{
            SipServletRequest originalReq2 = (SipServletRequest)pendingList.get(0);
            if(originalReq2 == null || !originalReq.equals(originalReq2)){
              originalReq.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
              "Fail to get correct pending message list through B2buaHelper.").send();
            }
            else{
              originalReq2.createResponse(SipServletResponse.SC_OK).send();
            }
          }          
        }
      }
    } catch (IOException e) {
      logger.error("*** IOException when handling successful response ***", e);
      throw new TckTestException(e);
    } 
  }
  
  private URI getUa2Uri(SipServletRequest req){
    URI ua2 = null;
    String header = req.getHeader(TestConstants.PRIVATE_URI);
    if(TestUtil.hasText(header)){
      try {
        ua2 = sipFactory.createURI(header);
      } catch (ServletParseException e) {
        logger.error("*** ServletParseException when creating URI ***", e);
        throw new TckTestException(e);
      }
    }

    if(ua2 == null){
      try {
        req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, 
        "Fail to get correct URI of UA2.").send();
      } catch (IOException e) {
        logger.error("*** ServletParseException when creating URI ***", e);
        throw new TckTestException(e);
      }
    }
    
    return ua2;
  }

}
