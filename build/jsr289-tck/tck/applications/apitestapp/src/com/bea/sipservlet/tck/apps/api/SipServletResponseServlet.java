/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.
 *  
 * SipServletResponseServlet is used in test the APIs of 
 * javax.servlet.sip.SipServletResponse
 * 
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.Rel100Exception;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.utils.TestUtil;

@javax.servlet.sip.annotation.SipServlet(name = "SipServletResponse")
public class SipServletResponseServlet extends BaseServlet {

  private static Logger logger = Logger
      .getLogger(SipServletResponseServlet.class);
  private static final long serialVersionUID = 1L;
  private static final String NEXT_MESSAGE = "Need Message";

  

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetRequest001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    return req.createResponse(200).getRequest() == null ? "Fail to get request"
        : null;
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetStatus001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    return req.createResponse(200).getStatus() != 200 ? "Fail to get Status"
        : null;

  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetOutputStream001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    return req.createResponse(200).getOutputStream() == null ? null
        : "Fail to getOutputStream";

  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetWriter001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    return req.createResponse(200).getWriter() == null ? null
        : "Fail to getOutputStream";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetStatus001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    SipServletResponse resp = req.createResponse(100);
    resp.setStatus(200);
    return resp.getStatus() != 200 ? "Fail to setStatus" : null;
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetStatus002(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    SipServletResponse resp = req.createResponse(100);
    String reason = "TEST_REASON";

    resp.setStatus(200, reason);
    if (resp.getStatus() != 200 || !resp.getReasonPhrase().equals(reason)) {
      return "Fail to setStatus";
    }
    return null;
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetStatus101(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    try {
      SipServletResponse resp = req.createResponse(200,"OK");
      resp.setStatus(-1,"ERROR");
    } catch (IllegalArgumentException e) {
      return null;
    }

    return "Fail to catch IllegalArgumentException";
  }  
  
  
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetReasonPhrase001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    String reason = "TEST_REASON";
    SipServletResponse resp = req.createResponse(100, reason);
    return resp.getReasonPhrase().equals(reason) ? null
        : "Fail to getReasonPhrase";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testGetProxy001(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    return null;
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSend001(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    return null;
  }

  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testSend101(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    req.createResponse(200).send();
    req.getSession().createRequest("MESSAGE").send();    
  }
  
  

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreateAck001(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    req.createResponse(200).send();
    sipFactory.createRequest(req.getApplicationSession(), "INVITE",
        req.getTo(), req.getFrom()).send();
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreateAck101(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    SipServletResponse resp = req.createResponse(200);    
    try {
      resp.createAck();
    } catch (IllegalStateException e) {
        resp.send();
    }
  }  
  
  

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetChallengeRealms001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    SipServletResponse resp = req.createResponse(401);

    String authStr = "Digest realm=\"atlanta.example.com\", qop=\"auth\","
        + "    nonce=\"ea9c8e88df84f1cec4341ae6cbe5a359\","
        + "    opaque=\"\", stale=FALSE, algorithm=MD5";
    resp.setHeader("WWW-Authenticate", authStr);
    Iterator<String> challengeRealms = resp.getChallengeRealms();
    if (challengeRealms != null) {
      while (challengeRealms.hasNext()) {
        if (challengeRealms.next() != null) {
          return null;
        }
      }
    }
    return "Fail to Get ChallengeRealms in response";
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreatePrack001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    req.createResponse(200).send();
    SipServletRequest request = sipFactory.createRequest(req
        .getApplicationSession(), "INVITE", req.getTo(), req.getFrom());
    request.setHeader("Require", "100rel");
    request.send();
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreatePrack101(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    try {
      req.createResponse(200).createPrack();
    } catch (Rel100Exception e) {
      req.createResponse(200).send();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreatePrack102(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    req.createResponse(200).send();
    SipServletRequest request = sipFactory.createRequest(req
        .getApplicationSession(), "INVITE", req.getTo(), req.getFrom());
    request.setHeader("Require", "100rel");
    request.send();        
  }
  
  protected void doProvisionalResponse(SipServletResponse resp)
      throws ServletException, IOException {
    String methodName = resp.getHeader(TestConstants.METHOD_HEADER);
    if ("testGetProxy001".equals(methodName)) {
      Proxy p = resp.getProxy();
      if (p != null) {
        p.cancel();
      }
      return;
    }

    if(NEXT_MESSAGE.equals(resp.getReasonPhrase())){
      resp.createPrack().send();
      try {
        resp.createPrack().send();
      } catch (IllegalStateException e) {
        resp.getSession().setAttribute(TestConstants.TEST_RESULT, 
            TestConstants.TEST_RESULT_OK);
      }
      return ;
    } 
    
    if (resp.getRequest().getMethod().equals("INVITE")) {
      resp.createPrack().send();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testIsBranchResponse001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    Proxy proxy = req.getProxy();
    List<URI> targets = new ArrayList<URI>(1);
    SipURI uri2 = (SipURI) getUa2Uri(req);
    SipURI uri3 = (SipURI) getUa3Uri(req);
    targets.add(uri2);
    targets.add(uri3);
    List<ProxyBranch> proxyBranches = proxy.createProxyBranches(targets);
    proxy.startProxy();

  }

  protected void doBranchResponse(SipServletResponse resp)
      throws ServletException, IOException {
    if (resp.isBranchResponse() && resp.getProxyBranch() != null) {
      resp.setHeader(TestConstants.TEST_RESULT, TestConstants.TEST_RESULT_OK);
    }
  }

  protected void doSuccessResponse(SipServletResponse resp)
      throws ServletException, IOException {
    if (resp.getRequest().getMethod().equals("INVITE")) {
      resp.createAck().send();
      return;
    }    
    
    if ("testSend101".equals(resp.getHeader(TestConstants.METHOD_HEADER))) {
      try {
        resp.send();
      } catch (IllegalStateException e) {
        resp.getSession().createRequest("MESSAGE").send();
      }
      return;
    }    
    
    if ("testSendReliably102".equals(resp.getHeader(TestConstants.METHOD_HEADER))) {
      resp.setStatus(180);
      try {
        resp.sendReliably();
      } catch (IllegalStateException e) {
        resp.getSession().createRequest("MESSAGE").send();
      }
      return;
    }
    
    if (resp.getRequest().getMethod().equals("PRACK") && 
        resp.getSession().getAttribute(TestConstants.TEST_RESULT)!=null) {
      resp.getSession().createRequest("INFO").send();
      return;
    }    

    
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testSendReliably001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    SipServletResponse resp = req.createResponse(180);
    resp.sendReliably();
  }
  
  
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testSendReliably101(SipServletRequest req)
      throws IOException {
    serverEntryLog();
    SipServletResponse resp = req.createResponse(180);
    try {
      resp.sendReliably();
    } catch (Rel100Exception e) {
      req.createResponse(200).send();
    }
  }  
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testSendReliably102(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    SipServletResponse resp = req.createResponse(180);
    req.getSession().setAttribute("testName", "testSendReliably102");
    resp.sendReliably();
    try {
      resp.sendReliably();
    } catch (IllegalStateException e) {
      req.getSession().setAttribute(TestConstants.TEST_RESULT,
          TestConstants.TEST_RESULT_OK);
    }
  }
  
  protected void doPrack(SipServletRequest req) throws ServletException,
      IOException {
    SipServletResponse resp = req.createResponse(200);
    if ("testSendReliably102".equals(req.getSession().getAttribute("testName"))
        && 
        TestConstants.TEST_RESULT_OK.equals(req.getSession().getAttribute(
            TestConstants.TEST_RESULT))) {
      resp.addHeader(TestConstants.TEST_RESULT, TestConstants.TEST_RESULT_OK);
    }
    resp.send();
  }

  protected void doBye(SipServletRequest req) throws ServletException,
      IOException {
    req.createResponse(200).send();
  }

  private URI getUa3Uri(SipServletRequest req){
    URI ua3 = null;
    String header = req.getHeader(TestConstants.UA3_URI);
    if(TestUtil.hasText(header)){
      try {
        ua3 = sipFactory.createURI(header);
      } catch (ServletParseException e) {
        logger.error("*** ServletParseException when creating URI ***", e);
        throw new TckTestException(e);
      }
    }

    if(ua3 == null){
      try {
        req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, 
        "Fail to get correct URI of UA2.").send();
      } catch (IOException e) {
        logger.error("*** ServletParseException when creating URI ***", e);
        throw new TckTestException(e);
      }
    }
    
    return ua3;
  }
  
  private URI getUa2Uri(SipServletRequest req){
    URI ua2 = null;
    String header = req.getHeader(TestConstants.UA2_URI);
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
