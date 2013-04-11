/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.
 *  
 * SipServletServlet is used in test the APIs of 
 * javax.servlet.sip.SipServlet
 * 
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
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

@javax.servlet.sip.annotation.SipServlet(name = "SipServlet")
public class SipServletServlet extends BaseServlet {

  private static Logger logger = Logger.getLogger(SipServletServlet.class);

  private static final String NEXT_MESSAGE = "Need Message";

  private static final String RESULT = "Result";

  protected void doInvite(SipServletRequest req) throws ServletException,
      IOException {

    if (req.getHeader(TestConstants.METHOD_HEADER).equals(
        "testDoPrackUpdate001")) {
      SipServletResponse resp = req.createResponse(180);
      resp.sendReliably();
      return;
    }

    req.createResponse(200).send();
  }

  private void markHeader(SipServletRequest req) {
    req.getSession().setAttribute(TestConstants.METHOD_HEADER,
        req.getHeader(TestConstants.METHOD_HEADER));
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoInviteAckBye001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    markHeader(req);
    doInvite(req);
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoInfo001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    markHeader(req);
    doInvite(req);
  }
  
  
  protected void doAck(SipServletRequest req) throws ServletException,
      IOException {
    req.getSession().setAttribute(RESULT, "ACK");
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoOptions001(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    doOptions(req);
  }

  protected void doOptions(SipServletRequest req) throws ServletException,
      IOException {
    req.createResponse(200).send();
  }

  protected void doBye(SipServletRequest req) throws ServletException,
      IOException {
    logger.info("get bye message");

    if ("testDoInviteAckBye001".equals(req.getSession().getAttribute(
        TestConstants.METHOD_HEADER))
        && !"ACK".equals(req.getSession().getAttribute(RESULT))) {
      req.createResponse(500, "Can not get ACK").send();
      return;
    }

    req.createResponse(200).send();

  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoCancel001(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    req.createResponse(180).send();
  }

  protected void doCancel(SipServletRequest req) throws ServletException,
      IOException {
    req.createResponse(200).send();
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoRegister001(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    doRegister(req);
  }

  protected void doRegister(SipServletRequest req) throws ServletException,
      IOException {
    req.createResponse(200).send();
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoSubscribe001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    doSubscribe(req);
  }

  protected void doSubscribe(SipServletRequest req) throws ServletException,
      IOException {

    SipServletResponse resp = req.createResponse(200);
    resp.setHeader("Expires", "3600");
    resp.send();
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoNotify001(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    doNotify(req);
  }

  protected void doNotify(SipServletRequest req) throws ServletException,
      IOException {
    SipServletResponse resp = req.createResponse(200);
    resp.setHeader("Expires", "3600");
    resp.send();
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoMessage001(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    doMessage(req);
  }

  protected void doMessage(SipServletRequest req) throws ServletException,
      IOException {
    req.createResponse(200).send();
    if (req.getHeader(TestConstants.METHOD_HEADER).equals("testDoResponse001")) {
      req.getSession().createRequest("INVITE").send();
    }
  }



  protected void doInfo(SipServletRequest req) throws ServletException,
      IOException {
    req.createResponse(200).send();
  }

  protected void doPrack(SipServletRequest req) throws ServletException,
      IOException {
    req.createResponse(200).send();
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoPrackUpdate001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    doInvite(req);
  }

  protected void doUpdate(SipServletRequest req) throws ServletException,
      IOException {
    req.createResponse(200).send();
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoRefer001(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    doRefer(req);
  }

  protected void doRefer(SipServletRequest req) throws ServletException,
      IOException {
    req.createResponse(200).send();
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoPublish001(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    doPublish(req);
  }

  protected void doPublish(SipServletRequest req) throws ServletException,
      IOException {
    req.createResponse(200).send();
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoProvisionalResponse001(SipServletRequest req)
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
    if (resp.getStatus() == 183) {
      resp.createAck().send();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoSuccessResponse001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    req.createResponse(200).send();
    req.getSession().createRequest("MESSAGE").send();
  }

  protected void doSuccessResponse(SipServletResponse resp)
      throws ServletException, IOException {
    if (resp.getRequest().getMethod().equals("INVITE")) {
      resp.createAck().send();
      return;
    }

    if (resp.getReasonPhrase().endsWith(NEXT_MESSAGE)) {
      resp.getSession().createRequest("MESSAGE").send();
      return;
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoRedirectResponse001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    req.createResponse(200).send();
    req.getSession().createRequest("MESSAGE").send();
  }

  protected void doRedirectResponse(SipServletResponse resp)
      throws ServletException, IOException {
    if (resp.getReasonPhrase().endsWith(NEXT_MESSAGE)) {
      resp.getSession().createRequest("MESSAGE").send();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoErrorResponse001(SipServletRequest req)
      throws ServletException, IOException {    
    serverEntryLog();
    req.createResponse(200).send();
    req.getSession().createRequest("MESSAGE").send();
  }

  protected void doErrorResponse(SipServletResponse resp)
      throws ServletException, IOException {
    if (resp.getReasonPhrase().endsWith(NEXT_MESSAGE)) {
      resp.getSession().createRequest("MESSAGE").send();
    }
  }

  public void testDoBranchResponse001(SipServletRequest req)
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

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testLog001(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    log("TEST SipServlet.log()");
    req.createResponse(200).send();
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testLog002(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    log("TEST SipServlet.log() with Throwable", new Throwable(
        "TEST SipServlet.log()"));
    req.createResponse(200).send();
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testDoRequest101(SipServletRequest req) throws ServletException,
      IOException {
    try {
      throw new ServletException("TEST ServletException");
    } catch (ServletException e) {
      req.createResponse(200).send();
    }    
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
