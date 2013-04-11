/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * ProxyServlet is used to test the APIs of 
 * javax.servlet.sip.Proxy
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.utils.TestUtil;

@javax.servlet.sip.annotation.SipServlet(name = "Proxy")
public class ProxyServlet extends BaseServlet {
  private static final long serialVersionUID = 4457534214498902649L;
  private static Logger logger = Logger.getLogger(ProxyServlet.class);

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testCancel001(SipServletRequest req) {
    serverEntryLog();
    
    //proxy directly and wait for provisional response to do cancel
    return null;
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testCancel002(SipServletRequest req) {
    serverEntryLog();

    //proxy directly and wait for provisional response to do cancel
    return null;
  }


  public void testGetCreateProxyBranches001(SipServletRequest req) {
    serverEntryLog();
    
    List<URI> branchURIs = new ArrayList<URI>();
    SipURI uri = (SipURI)req.getRequestURI();
    branchURIs.add(uri);    

    Proxy p = getRequestProxy(req);
    p.setProxyTimeout(300);
    p.createProxyBranches(branchURIs);
    List<ProxyBranch> branches = p.getProxyBranches();
    ProxyBranch pb = p.getProxyBranch(uri);
    if(branches.size() != 1 || pb == null){
      logger.error("*** Fail to creat or get ProxyBranch through Proxy. ***");
      throw new TckTestException("Fail to creat or get ProxyBranch through Proxy");
    }   
    SipURI branchURI = (SipURI)branches.get(0).getRequest().getRequestURI();
    if(!branchURI.equals(uri)){
      logger.error("*** Fail to creat ProxyBranch with the correct request URI. ***");
      throw new TckTestException("Fail to creat ProxyBranch with the correct request URI");
    }
    p.startProxy(); 
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetSetAddToPath001(SipServletRequest req) {
    serverEntryLog();
    
    Proxy proxy = getRequestProxy(req);
    URI ua2Uri = getUa2Uri(req);
    
    //default value is false
    boolean isAddToPath1 = proxy.getAddToPath();
    boolean isExceptionThrown = false;
    try {
      proxy.getPathURI();
    } catch (IllegalStateException e) {
      isExceptionThrown = true;      
    }
    if(!isExceptionThrown) {
      req.addHeader(TestConstants.TEST_FAIL_REASON, 
          "IllegalStateException is expected when retrieving PathURI with AddToPath disabled");
    }
    else{
      proxy.setAddToPath(true);
      boolean isAddToPath2 = proxy.getAddToPath();    
      SipURI uri = proxy.getPathURI();
      if (uri == null) {
        req.addHeader(TestConstants.TEST_FAIL_REASON, 
            "Fail to retrieve PathURI through Proxy with AddToPath enabled");
      } else {
        if (isAddToPath1 || !isAddToPath2) { 
          req.addHeader(TestConstants.TEST_FAIL_REASON, 
              "Fail to set and get \"AddToPath\" parameter of Proxy");
        }             
      }      
    }   
    
    proxy.proxyTo(ua2Uri);  
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetSetNoCancel001(SipServletRequest req) {
    serverEntryLog();

    String method = req.getMethod();
    if("INVITE".equals(method)){
      List<URI> branchURIs = new ArrayList<URI>();
      URI ua2Uri = req.getRequestURI();
      branchURIs.add(ua2Uri);    
      URI ua3Uri = getUa3Uri(req);
      branchURIs.add(ua3Uri);    
      
      Proxy p = getRequestProxy(req);
      //assert
      ProxyBranch pb = p.getProxyBranch(ua2Uri);
      p.setNoCancel(true);
      boolean isTrue = p.getNoCancel();
      p.setNoCancel(false);
      boolean isFalse = p.getNoCancel();
      if(!isTrue || isFalse){
        pb.getRequest().addHeader(TestConstants.TEST_FAIL_REASON, 
            "Fail to set and get 'NoCancel' attribute of Proxy.");
      }
      
      req.getApplicationSession(true).setInvalidateWhenReady(false);
      req.getSession(true).setInvalidateWhenReady(false);
      p.setNoCancel(true);
      p.setProxyTimeout(100);
      p.createProxyBranches(branchURIs);
      
      p.startProxy(); 
    } 
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testGetOriginalRequest001(SipServletRequest req) {
    serverEntryLog();
    
    Proxy proxy = getRequestProxy(req);
    SipServletRequest newReq = proxy.getOriginalRequest();
    if (newReq != null && newReq.equals(req)){
      SipSession session = req.getSession(true);
      session.setAttribute("oringinal.request", req);
      return null;
    } else {
      return "Fail to get original request through Proxy";
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testGetSetParallel001(SipServletRequest req) {
    serverEntryLog();
    
    //more detailed cases are referred in cases of ProxyBranch
    Proxy proxy = getRequestProxy(req);
    //default value is true
    boolean isParallel1 = proxy.getParallel();    
    proxy.setParallel(false);
    boolean isParallel2 = proxy.getParallel();
    proxy.setParallel(true);
    boolean isParallel3 = proxy.getParallel();
    return (isParallel1 && !isParallel2 && isParallel3) ? 
        null : "Fail to set and get \"Parallel\" parameter of Proxy.";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testGetSetProxyTimeout001(SipServletRequest req) {
    serverEntryLog();
    
    Proxy proxy = getRequestProxy(req);
    //get default proxy timeout, that should be 180
    int proxyTimeout1 = proxy.getProxyTimeout();
    proxy.setProxyTimeout(333);
    int proxyTimeout2 = proxy.getProxyTimeout();
    return (proxyTimeout1 == 180 && proxyTimeout2 == 333) ? 
        null : "Fail to set and get \"ProxyTimeout\" parameter of Proxy.";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testGetSetRecordRoute001(SipServletRequest req) {
    serverEntryLog();

    Proxy proxy = getRequestProxy(req);
    // default value is false
    boolean isRecordRoute0 = proxy.getRecordRoute();
    proxy.setRecordRoute(true);
    boolean isRecordRoute1 = proxy.getRecordRoute();
    proxy.setRecordRoute(false);
    boolean isRecordRoute2 = proxy.getRecordRoute();
    boolean isExceptionThrown = false;
    try {
      proxy.getRecordRouteURI();
    } catch (IllegalStateException e) {
      isExceptionThrown = true;
    }
    if (!isExceptionThrown) {
      return "IllegalStateException is expected when retrieving RecordRouteURI with RecordRoute disabled.";
    }

    proxy.setRecordRoute(true);
    SipURI uri = proxy.getRecordRouteURI();
    if (uri == null) {
      return "Fail to retrieve RecordRouteURI through Proxy with RecordRoute enabled.";
    }
    return (!isRecordRoute0 && isRecordRoute1 && !isRecordRoute2) ? 
        null : "Fail to set and get 'RecordRoute' parameter of Proxy.";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testGetSetRecurse001(SipServletRequest req) {
    serverEntryLog();
    
    //more detailed cases are referred in cases of ProxyBranch
    Proxy proxy = getRequestProxy(req);
    //The default is true.
    boolean isRecurse1 = proxy.getRecurse();    
    proxy.setRecurse(false);
    boolean isRecurse2 = proxy.getRecurse();
    proxy.setRecurse(true);
    boolean isRecurse3 = proxy.getRecurse();
    return (isRecurse1 && !isRecurse2 && isRecurse3) ? 
        null : "Fail to set and get 'Recurse' parameter of Proxy.";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testGetSetSequentialSearchTimeout001(SipServletRequest req) {
    serverEntryLog();
    
    //the APIs have been deprecated
    return null;
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testGetSetStateful001(SipServletRequest req) {
    serverEntryLog();
    
    //the APIs have been deprecated
    return null;
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testGetSetSupervised001(SipServletRequest req) {
    serverEntryLog();
    
    Proxy proxy = getRequestProxy(req);
    proxy.setSupervised(false);
    boolean isSupervised1 = proxy.getSupervised();
    proxy.setSupervised(true);
    boolean isSupervised2 = proxy.getSupervised();    
    return (!isSupervised1 && isSupervised2) ? 
        null : "Fail to set and get \"Supervised\" parameter of Proxy.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testProxyTo001(SipServletRequest req) {
    serverEntryLog();
    
    Proxy proxy = getRequestProxy(req);
    proxy.proxyTo(req.getRequestURI());
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testProxyTo002(SipServletRequest req) {
    serverEntryLog();
    
    Proxy proxy = getRequestProxy(req);
    List<URI> list = new ArrayList<URI>();
    list.add(req.getRequestURI());
    proxy.proxyTo(list);
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testSetOutboundInterface001(SipServletRequest req) throws IOException {
    serverEntryLog();
    
    List<SipURI> uris = (List<SipURI>) getServletContext()
      .getAttribute(OUTBOUND_INTERFACES);
    assert uris.size() > 0;
    
    SipURI tempUri = (SipURI)req.getTo().getURI();
    for (SipURI uri : uris) {
      if(!tempUri.equals(uri)){
        tempUri = uri;
        break;
      }
    }
    InetAddress ins = InetAddress.getByName(tempUri.getHost());
    req.setHeader(TestConstants.PRIVATE_URI, ins.getHostAddress());
    
    Proxy proxy = getRequestProxy(req);
    proxy.setOutboundInterface(ins);
    proxy.proxyTo(req.getRequestURI());
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testSetOutboundInterface002(SipServletRequest req) throws IOException {
    serverEntryLog();
    
    List<SipURI> uris = (List<SipURI>) getServletContext()
      .getAttribute(OUTBOUND_INTERFACES);
    assert uris.size() > 0;
    
    SipURI tempUri = (SipURI)req.getTo().getURI();
    for (SipURI uri : uris) {
      if(!tempUri.equals(uri)){
        tempUri = uri;
        break;
      }
    }
    InetAddress ins = InetAddress.getByName(tempUri.getHost());
    InetSocketAddress ids = new InetSocketAddress(ins, tempUri.getPort());
    req.setHeader(TestConstants.PRIVATE_URI, ins.getHostAddress() + ":" + tempUri.getPort());    
    
    Proxy proxy = getRequestProxy(req);
    proxy.setOutboundInterface(ids);
    proxy.proxyTo(req.getRequestURI());
  } 
  
  public void doAck(SipServletRequest req) 
    throws ServletException, IOException {
    String methodName = req.getHeader(TestConstants.PRIVATE_URI);
    if ("testCancel101".equals(methodName)) {
      try {
        getProxy(req).cancel();
      } catch (IllegalStateException e) {
        addOKHeader(req);
      }

    } else

    if ("testStartProxy101".equals(methodName)) {

      try {
        getProxy(req).startProxy();
      } catch (IllegalStateException e) {
        addOKHeader(req);
      }
    } else

    if ("testProxyTo102".equals(methodName)) {
      try {
        getProxy(req).proxyTo(req.getFrom().getURI());
      } catch (IllegalStateException e) {
        addOKHeader(req);
      }
    }

    else if ("testProxyTo105".equals(methodName)) {
      try {
        List<URI> list = new ArrayList<URI>();
        list.add(req.getFrom().getURI());
        getProxy(req).proxyTo(list);
      } catch (IllegalStateException e) {
        addOKHeader(req);
      }
    }

  }

  
  private void addOKHeader(SipServletMessage msg) {
    msg.addHeader(TestConstants.TEST_RESULT, TestConstants.TEST_RESULT_OK);
    logger.info(msg.toString());
    logger.info(msg.getHeader(TestConstants.TEST_RESULT));
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
        "Fail to get correct URI of UA3.").send();
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
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public void testCancel101(SipServletRequest req) throws IOException {
    serverEntryLog();
    saveProxy(req);
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public void testStartProxy101(SipServletRequest req) throws IOException {
    serverEntryLog();
    saveProxy(req);
  }
  
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testSetProxyTimeout101(SipServletRequest req) throws ServletException, IOException{
    serverEntryLog();    
    Proxy proxy = getRequestProxy(req);
    try {
      proxy.setProxyTimeout(0);
    } catch (IllegalArgumentException e) {
      addOKHeader(req);
    }
    return null;
  }  

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testProxyTo101(SipServletRequest req) throws ServletException, IOException{
    serverEntryLog();    
    Proxy proxy = getRequestProxy(req);
    try {
      URI uri=null;
      proxy.proxyTo(uri);
    } catch (NullPointerException e) {
      addOKHeader(req);
    }
    return null;
  }
  //for IllegalStateException
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testProxyTo102(SipServletRequest req) throws ServletException, IOException{
    serverEntryLog();    
    saveProxy(req);
    return null;
  }
  //IllegalStateException
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testProxyTo103(SipServletRequest req) throws ServletException, IOException{
    serverEntryLog();    
    Proxy proxy = getRequestProxy(req);
    try {
      proxy.proxyTo(sipFactory.createURI("err:error.error"));
    } catch (IllegalArgumentException e) {
      addOKHeader(req);
    }
    return null;
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testProxyTo104(SipServletRequest req) throws ServletException, IOException{
    serverEntryLog();    
    Proxy proxy = getRequestProxy(req);
    try {
      List<URI> uris = new ArrayList<URI>();
      URI nullURI=null;
      uris.add(nullURI);
      proxy.proxyTo(uris);
    } catch (NullPointerException e) {
      addOKHeader(req);
    }
    return null;
  }
  //for IllegalStateException
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testProxyTo105(SipServletRequest req) throws ServletException, IOException{
    serverEntryLog();
    saveProxy(req);
    return null;
  }

  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testProxyTo106(SipServletRequest req) throws ServletException, IOException{
    serverEntryLog();    
    Proxy proxy = getRequestProxy(req);
    try {
      List<URI> uris = new ArrayList<URI>();
      uris.add(sipFactory.createURI("err:error.error"));
      proxy.proxyTo(uris);
    } catch (IllegalArgumentException e) {
      addOKHeader(req);
    }
    return null;
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testCreateProxyBranches101(SipServletRequest req) throws ServletException, IOException{
    serverEntryLog();    
    Proxy proxy = getRequestProxy(req);
    try {
      List<URI> uris = new ArrayList<URI>();
      uris.add(sipFactory.createURI("err:error.error"));
      proxy.createProxyBranches(uris);
    } catch (IllegalArgumentException e) {
      logger.info("get excetpion:"+e.getLocalizedMessage());
      addOKHeader(req);
    }
    return null;
  }  
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testSetRecordRoute101(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    return null;
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetOutboundInterface101(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    Proxy proxy = req.getProxy();
    try {
      InetAddress ins = null;
      proxy.setOutboundInterface(ins);
      return "NullPointerException is not thrown";
    } catch (NullPointerException e) {
      return null;
    }
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetOutboundInterface102(SipServletRequest req)
      throws ServletException, IOException{
    serverEntryLog();
    Proxy proxy = req.getProxy();

    try {
      InetSocketAddress ins = null;
      proxy.setOutboundInterface(ins);
      return "NullPointerException is not thrown";
    } catch (NullPointerException e) {
      return null;
    }
  }  

  

  public void doProvisionalResponse(SipServletResponse resp)
      throws ServletException, IOException {
    String methodName = resp.getHeader(TestConstants.METHOD_HEADER);
    if ("testCancel001".equals(methodName)) {
      Proxy p = resp.getProxy();
      p.setProxyTimeout(300);
      p.cancel();
    } else if ("testCancel002".equals(methodName)) {
      Proxy p = resp.getProxy();
      p.setProxyTimeout(300);
      p.cancel(new String[] { "sip" }, new int[] { 503 },
          new String[] { "cancel reason" });
    } 
    
    if ("testSetRecordRoute101".equals(methodName)) {
      Proxy proxy = resp.getProxy();
      try {
        proxy.setRecordRoute(false);
      } catch (IllegalStateException e) {
        addOKHeader(resp);
      }
    }
  }
  
  public void doSuccessResponse(SipServletResponse resp)
      throws ServletException, IOException {
    String methodName = resp.getHeader(TestConstants.METHOD_HEADER);
    if ("testGetOriginalRequest001".equals(methodName)) {
      Proxy p = resp.getProxy();
      SipSession session = resp.getSession();
      SipServletRequest req = (SipServletRequest)session.getAttribute("oringinal.request");
      if(!p.getOriginalRequest().equals(req)){
        resp.addHeader(TestConstants.TEST_FAIL_REASON, 
            "Fail to get the right original request from proxy");
      }
    } 
  }  

  /**
   * @param req
   * @return
   */
  private void saveProxy(SipServletRequest req) {
    Proxy proxy = getRequestProxy(req);
    proxy.setRecordRoute(true);
    req.getSession().setAttribute("PROXY", proxy);
  }
  
  private Proxy getProxy(SipServletRequest req){
    return (Proxy)req.getSession().getAttribute("PROXY");
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testGetPathURI101(SipServletRequest req) throws
    ServletException, IOException{
    serverEntryLog();    
    Proxy proxy = getRequestProxy(req);
    try {
      proxy.getPathURI();
    } catch (IllegalStateException e) {
      addOKHeader(req);
    }
    return null;
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT_PROXY)
  public String testGetRecordRouteURI101(SipServletRequest req) throws
    ServletException, IOException{
    serverEntryLog();    
    Proxy proxy = getRequestProxy(req);
    proxy.setRecordRoute(false);
    try {
      proxy.getRecordRouteURI();
    } catch (IllegalStateException e) {
      addOKHeader(req);
    }
    return null;  
  }
}
