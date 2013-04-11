/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * SipSessionServlet is used to test the APIs of 
 * javax.servlet.sip.SipSession
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.utils.TestUtil;

@javax.servlet.sip.annotation.SipServlet(name = "SipSession")
@javax.servlet.sip.annotation.SipListener
public class SipSessionServlet extends BaseServlet implements SipSessionListener {
  private static final long serialVersionUID = -4947112768223378275L;
  private static Logger logger = Logger.getLogger(SipSessionServlet.class);
  private static final String FROM_ADDR = "from-addr";
  private static final String TO_ADDR = "to-addr";
  private static String CURRENT_SERVLET_CASE = "SipSessionServlet";

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateRequest001(SipServletRequest req) {
    serverEntryLog();
    
    SipSession sipSession = req.getSession();
    SipServletRequest newReq = sipSession.createRequest("INVITE");
    return (newReq != null) ? null : "get null when creating INVITE";
  }
  
  public void testCreateRequest101(SipServletRequest req) {
    serverEntryLog();
    SipSession session = req.getSession();
    try {
      session.createRequest("ACK");
      sendResponse(req, 500, "Should throw IllegalArgumentException,since"
          + "the SIP method is ACK.");
    } catch (IllegalArgumentException e) {
      sendResponse(req, 200, "200OK for INVITE.");
    }
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateRequest102(SipServletRequest req) {
    serverEntryLog();
    SipSession newSipSession = createNewSipSession(req);
    if (newSipSession.isValid()) {
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession.createRequest("MESSAGE");
      } catch (IllegalStateException e) {
        return null;
      }
    }
    return "Should throw IllegalStateException, since this method is called "
        + "on an invalidated session.";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetApplicationSession001(SipServletRequest req) {
    serverEntryLog();
    
    return (req.getApplicationSession() != null) ? 
        null : "SipApplicationSession is null";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetRemoveAttribute001(SipServletRequest req) {
    serverEntryLog();
    
    SipSession sipSession = req.getSession();
    sipSession.setAttribute("aa", "AA");
    String att = (String) sipSession.getAttribute("aa");
    if (!"AA".equals(att))
      return "Fail to get attribute from SipSession";

    sipSession.removeAttribute("aa");
    att = (String) sipSession.getAttribute("aa");
    return (att == null) ? null : "Fail to remove attribute from SipSession";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAttribute101(SipServletRequest req) {
    serverEntryLog();
    SipSession newSipSession = createNewSipSession(req);
    if (newSipSession.isValid()) {
      newSipSession.setAttribute("testGetAttribute101", "testGetAttribute101");
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession.getAttribute("testGetAttribute101");
      } catch (IllegalStateException e) {
        return null;
      }
    }
    return "Should throw IllegalStateException, since this method is called "
        + "on an invalidated session.";
  }  
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAttribute102(SipServletRequest req) {
    serverEntryLog();    
    SipSession session = req.getSession();
    String name = null;
    try {
      session.getAttribute(name);
    } catch (NullPointerException e) {
        return null;
    }    
    return "Should throw NullPointerException, since the name is null.";
  }  
 
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetAttribute101(SipServletRequest req) {
    serverEntryLog();
    SipSession newSipSession = createNewSipSession(req);
    if (newSipSession.isValid()) {
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession
            .setAttribute("testSetAttribute101", "testSetAttribute101");
      } catch (IllegalStateException e) {
        return null;
      }
    }
    return "Should throw IllegalStateException, since this method is called "
        + "on an invalidated session.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetAttribute102(SipServletRequest req) {
    serverEntryLog();
    SipSession session = req.getSession();
    String name = null;
    try {
      session.setAttribute(name, "testSetAttribute102");
    } catch (NullPointerException e) {
      return null;
    }
    return "Should throw NullPointerException, since the name is null.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testRemoveAttribute101(SipServletRequest req) {
    serverEntryLog();
    SipSession newSipSession = createNewSipSession(req);
    String name = "testRemoveAttribute101";
    if (newSipSession.isValid()) {
      newSipSession.setAttribute(name, name);
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession.removeAttribute(name);
      } catch (IllegalStateException e) {
        return null;
      }
    }
    return "Should throw IllegalStateException, since this method is called "
    + "on an invalidated session.";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAttributeNames001(SipServletRequest req) {
    serverEntryLog();
    
    SipSession sipSession = req.getSession();
    sipSession.setAttribute("aa", "AA");
    sipSession.setAttribute("bb", "BB");
    Enumeration<String> enu = sipSession.getAttributeNames();
    if (enu == null)
      return "Fail to get attribute names enumeration";

    boolean isAAExist = false;
    boolean isBBExist = false;
    while (enu.hasMoreElements()) {
      String value = enu.nextElement();
      if ("aa".equals(value))
        isAAExist = true;
      if ("bb".equals(value))
        isBBExist = true;
    }
    return (isAAExist && isBBExist) ? 
        null : "Get wrong attribute names list from SipSession";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAttributeNames101(SipServletRequest req) {
    serverEntryLog();
    SipSession newSipSession = createNewSipSession(req);
    if (newSipSession.isValid()) {
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession.getAttributeNames();
      } catch (IllegalStateException e) {
        return null;
      }
    }
    return "Should throw IllegalStateException, since this method is called "
        + "on an invalidated session.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetCallId001(SipServletRequest req) {
    serverEntryLog();

    SipSession sipSession = req.getSession();
    String callID = sipSession.getCallId();
    return (TestUtil.hasText(callID) && req.getCallId().equals(callID)) ? 
        null : "Fail to get correct Call-ID from SipSession";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetCreationTime001(SipServletRequest req) {
    serverEntryLog();

    SipSession sipSession = req.getSession();
    long creationTime = sipSession.getCreationTime();
    return (creationTime > 0) ? 
        null : "Fail to get correct CreationTime from SipSession";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetId001(SipServletRequest req) {
    serverEntryLog();

    SipSession sipSession = req.getSession();
    String id = sipSession.getId();
    return (TestUtil.hasText(id)) ? 
        null : "Fail to get correct Call-ID from SipSession";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetSetInvalidateWhenReady001(SipServletRequest req) throws IOException {
    serverEntryLog();

    String method = req.getMethod();
    if("ACK".equals(method))return;

    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    if("INVITE".equals(method)){
      SipSession sipSession = req.getSession();
      //set flag
      sipSession.setAttribute(CURRENT_SERVLET_CASE, CURRENT_SERVLET_CASE);
      sipSession.setAttribute(TestConstants.METHOD_HEADER, 
          "testGetSetInvalidateWhenReady001");
      sipSession.setAttribute(FROM_ADDR, req.getTo().clone());
      sipSession.setAttribute(TO_ADDR, getPrivateUri(req));

      sipSession.setInvalidateWhenReady(false);
      boolean isSet1 = sipSession.getInvalidateWhenReady();
      sipSession.setInvalidateWhenReady(true);
      boolean isSet2 = sipSession.getInvalidateWhenReady();
      if (isSet1 || !isSet2) {
        resp.addHeader(TestConstants.TEST_FAIL_REASON, 
            "Fail to set and get 'InvalidateWhenReady' parameter from SipSession");
      }
    } 
    
    resp.send();
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetLastAccessedTime001(SipServletRequest req) {
    serverEntryLog();

    SipSession sipSession = req.getSession();
    long lastAccessedTime = sipSession.getLastAccessedTime();
    return (lastAccessedTime > 0) ? 
        null : "Fail to get correct LastAccessedTime from SipSession";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetLocalParty001(SipServletRequest req) {
    serverEntryLog();

    SipServletRequest newReq = sipFactory.createRequest(
        req.getApplicationSession(), "INVITE", req.getTo(), req.getFrom());
    SipSession newSession = newReq.getSession();
    Address local = newSession.getLocalParty();        
    return (local != null && local.equals(newReq.getFrom())) ? 
        null : "Fail to get correct LocalParty from SipSession";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetRemoteParty001(SipServletRequest req) {
    serverEntryLog();

    SipServletRequest newReq = sipFactory.createRequest(
        req.getApplicationSession(), "INVITE", req.getTo(), req.getFrom());
    SipSession newSession = newReq.getSession();
    Address remote = newSession.getRemoteParty();        
    return (remote != null && remote.equals(newReq.getTo())) ? 
        null : "Fail to get correct RemoteParty from SipSession";
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetState001(SipServletRequest req) throws IOException {
    serverEntryLog();

    String method = req.getMethod();
    if("ACK".equals(method))return;

    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    SipSession sipSession = req.getSession();
    sipSession.setInvalidateWhenReady(true);
    if("INVITE".equals(method)){
      //set flag
      sipSession.setAttribute(TestConstants.METHOD_HEADER, "testGetState001");
      sipSession.setAttribute(CURRENT_SERVLET_CASE, CURRENT_SERVLET_CASE);
      sipSession.setAttribute(FROM_ADDR, req.getTo().clone());
      sipSession.setAttribute(TO_ADDR, getPrivateUri(req));

      SipSession.State state = sipSession.getState();
      if (!SipSession.State.INITIAL.equals(state) 
          && !SipSession.State.EARLY.equals(state)) {
        resp.addHeader(TestConstants.TEST_FAIL_REASON, 
            "Fail to get correct state of SipSession");
      }
    } else if("BYE".equals(method)){
      if (!SipSession.State.CONFIRMED.equals(sipSession.getState())) {
        resp.addHeader(TestConstants.TEST_FAIL_REASON, 
            "Fail to get correct state of SipSession");
      }
    }
    
    resp.send();
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetState101(SipServletRequest req) {
    serverEntryLog();
    SipSession newSipSession = createNewSipSession(req);
    if (newSipSession.isValid()) {
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession.getState();
      } catch (IllegalStateException e) {
        return null;
      }
    }
    return "Should throw IllegalStateException, since this method is called "
        + "on an invalidated session.";
  }  
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testInvalidate001(SipServletRequest req) throws IOException {
    serverEntryLog();

    SipSession sipSession = req.getSession();
    String method = req.getMethod();
    if("INVITE".equals(method)){
      sipSession.setAttribute(CURRENT_SERVLET_CASE, CURRENT_SERVLET_CASE);
    }
    SipApplicationSession sipAppSession = sipSession.getApplicationSession();
    sipSession.invalidate();
    
    SipServletRequest newReq = sipFactory.createRequest(sipAppSession, "MESSAGE", 
        (Address)req.getTo().clone(), getPrivateUri(req));
    if(sipSession.isValid()){
      newReq.addHeader(TestConstants.TEST_FAIL_REASON, "Fail to invalidate SipSession.");
    }
    newReq.send();
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testInvalidate101(SipServletRequest req) {
    serverEntryLog();
    SipSession newSipSession = createNewSipSession(req);
    if (newSipSession.isValid()) {
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession.invalidate();
      } catch (IllegalStateException e) {
        return null;
      }
    }
    return "Should throw IllegalStateException, since this method is called "
    + "on an invalidated session."; 
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetInvalidateWhenReady101(SipServletRequest req) {
    serverEntryLog();
    SipSession newSipSession = createNewSipSession(req);
    if (newSipSession.isValid()) {
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession.getInvalidateWhenReady();
      } catch (IllegalStateException e) {
        return null;
      }
    }
    return "Should throw IllegalStateException, since this method is called "
        + "on an invalidated session.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetInvalidateWhenReady101(SipServletRequest req) {
    serverEntryLog();
    SipSession newSipSession = createNewSipSession(req);
    if (newSipSession.isValid()) {
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession.setInvalidateWhenReady(true);        
      } catch (IllegalStateException e) {
        return null;
      }
    }
    return "Should throw IllegalStateException, since this method is called "
    + "on an invalidated session.";
  }


  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testIsReadyToInvalidate001(SipServletRequest req) throws IOException {
    serverEntryLog();

    String method = req.getMethod();
    if("ACK".equals(method))return;

    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    SipSession sipSession = req.getSession();
    if("INVITE".equals(method)){
      sipSession.setInvalidateWhenReady(true);
      //set flag
      sipSession.setAttribute(TestConstants.METHOD_HEADER, "testIsReadyToInvalidate001");
      sipSession.setAttribute(CURRENT_SERVLET_CASE, CURRENT_SERVLET_CASE);
      sipSession.setAttribute(FROM_ADDR, req.getTo().clone());
      sipSession.setAttribute(TO_ADDR, getPrivateUri(req));
    } 

    if(sipSession.isReadyToInvalidate()){
      resp.addHeader(TestConstants.TEST_FAIL_REASON, 
        "SipSession should not be in 'isReadyToInvalidate' state.");
    }
    
    resp.send();
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testIsReadyToInvalidate101(SipServletRequest req) {
    serverEntryLog();
    SipSession newSipSession = createNewSipSession(req);
    if (newSipSession.isValid()) {
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession.isReadyToInvalidate();
      } catch (IllegalStateException e) {
        return null;
      }
    }
    return "Should throw IllegalStateException, since this method is called "
        + "on an invalidated session.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testIsValid001(SipServletRequest req) throws IOException {
    serverEntryLog();
    
    String method = req.getMethod();
    if("ACK".equals(method))return;
    
    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
    SipSession sipSession = req.getSession();
    if("INVITE".equals(method)){
      sipSession.setInvalidateWhenReady(true);
      //set flag
      sipSession.setAttribute(TestConstants.METHOD_HEADER, "testIsValid001");
      sipSession.setAttribute(CURRENT_SERVLET_CASE, CURRENT_SERVLET_CASE);
      sipSession.setAttribute(FROM_ADDR, req.getTo().clone());
      sipSession.setAttribute(TO_ADDR, getPrivateUri(req));
    }

    if(!sipSession.isValid()){
      resp.addHeader(TestConstants.TEST_FAIL_REASON, 
          "Fail to determine if SipSession is valid.");        
    }

    resp.send();
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testSetHandler001(SipServletRequest req) throws IOException {
    serverEntryLog();
    
    //this api has already been used in TCK test framework, so return 200 directly
    req.createResponse(SipServletResponse.SC_OK).send();
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetHandler101(SipServletRequest req) {
    serverEntryLog();
    SipSession session = req.getSession();
    try {
      session.setHandler("_*(Dlsfaea.dafue");
    } catch (IllegalStateException e) {
      throw new TckTestException(e);
    } catch (ServletException ex) {
      return null;
    }
    return "Should throw ServletException,since "
        + "no servlet with the specified name exists in this application.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetHandler102(SipServletRequest req) {
    serverEntryLog();
    SipSession newSipSession = createNewSipSession(req);
    if (newSipSession.isValid()) {
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession.setHandler("SipSession");
      } catch (IllegalStateException e) {
        return null;
      } catch (ServletException ex) {
        throw new TckTestException(ex);
      }
    }
    return "Should throw IllegalStateException,since "
        + "this method is called on an invalidated session.";
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
    req.getSession().setOutboundInterface(ins);
    req.createResponse(200).send();
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetOutboundInterface101(SipServletRequest req) {
    serverEntryLog();
    SipURI tempUri = getOutboundInterfaceSipURI(req);
    InetAddress ins = getInetAddress(tempUri.getHost());
    SipSession newSipSession = createNewSipSession(req);
    if (newSipSession.isValid()) {
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession.setOutboundInterface(ins);
      } catch (IllegalStateException e) {
        return null;
      }
    }
    return "Should throw IllegalStateException, since this method is called "
    + "on an invalidated session."; 
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetOutboundInterface102(SipServletRequest req) {
    serverEntryLog();
    InetAddress ins = null;
    SipSession session = req.getSession();
    if (session.isValid()) {
      try {
        session.setOutboundInterface(ins);
      } catch (NullPointerException e) {
        return null;
      }
    }
    return "Should throw NullPointerException since the address is null.";
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
    req.getSession().setOutboundInterface(ids);
    req.createResponse(200).send();
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetServletContext001(SipServletRequest req)
      throws IOException {
    serverEntryLog();
    return req.getSession().getServletContext() == null ? "Can not get ServletContext"
        : null;
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetOutboundInterface103(SipServletRequest req) {
    serverEntryLog();
    SipURI tempUri = getOutboundInterfaceSipURI(req);
    InetAddress ins = getInetAddress(tempUri.getHost());
    InetSocketAddress ids = new InetSocketAddress(ins, tempUri.getPort());
    SipSession newSipSession = createNewSipSession(req);
    if (newSipSession.isValid()) {
      newSipSession.invalidate();
    }
    if (!newSipSession.isValid()) {
      try {
        newSipSession.setOutboundInterface(ids);
      } catch (IllegalStateException e) {
        return null;
      }
    }
    return "Should throw IllegalStateException, since this method is called "
    + "on an invalidated session."; 
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetOutboundInterface104(SipServletRequest req) {
    serverEntryLog();
    InetSocketAddress ids = null;
    SipSession session = req.getSession();
    if (session.isValid()) {
      try {
        session.setOutboundInterface(ids);
      } catch (NullPointerException e) {
        return null;
      }
    }
    return "Should throw NullPointerException since the address is null.";
  }
  

  public void sessionCreated(SipSessionEvent se) {
  }

  public void sessionDestroyed(SipSessionEvent se) {
  }

  public void sessionReadyToInvalidate(SipSessionEvent se) {
    SipSession sipSession = se.getSession();
    //if this event doesn't belong to current servlet, ignore it
    Object mark = sipSession.getAttribute(CURRENT_SERVLET_CASE);
    if(mark == null) return;    
    
    Object obj = sipSession.getAttribute(TestConstants.METHOD_HEADER);
    if(obj == null) return;
    Address from = (Address)sipSession.getAttribute(FROM_ADDR);
    Address to = (Address)sipSession.getAttribute(TO_ADDR);
    assert (from != null && to != null);
    SipServletRequest newReq = sipFactory.createRequest(
        sipSession.getApplicationSession(), "MESSAGE", from, to);
    
    String methodName = (String)obj;
    if("testGetSetInvalidateWhenReady001".equals(methodName)){
      if (!sipSession.isReadyToInvalidate()) {
        newReq.addHeader(TestConstants.TEST_FAIL_REASON,
            "SipSession should be in 'ReadyToInvalidate' state.");
      }     
    } else if("testGetState001".equals(methodName)){
      if (!SipSession.State.TERMINATED.equals(sipSession.getState())) {
        newReq.addHeader(TestConstants.TEST_FAIL_REASON, 
            "Fail to get correct state of SipSession");
      }
    } else if("testInvalidate001".equals(methodName)){
      sipSession.invalidate();
      if(sipSession.isValid()){
        newReq.addHeader(TestConstants.TEST_FAIL_REASON,
            "SipSession should be invalid after invalidation.");
      }     
    } else if("testIsReadyToInvalidate001".equals(methodName)){
      if(!sipSession.isReadyToInvalidate()){
        newReq.addHeader(TestConstants.TEST_FAIL_REASON, 
          "SipSession should be in 'isReadyToInvalidate' state.");
      }
    } else if("testIsValid001".equals(methodName)){
      if (!sipSession.isValid()) {
        newReq.addHeader(TestConstants.TEST_FAIL_REASON,
            "Fail to determine if SipSession is valid.");
      } else {
        sipSession.invalidate();
        if (sipSession.isValid()) {
          newReq.addHeader(TestConstants.TEST_FAIL_REASON,
              "Fail to determine if SipSession is valid.");
        }
      }
    }
    
    try {
      newReq.send();
    } catch (IOException e) {
      logger.error("*** IOException when sending MESSAGE ***", e);
      throw new TckTestException(e);
    }    
  }
  
  
  private SipURI createURI(String uri) {
    try {
      SipURI sipUri = (SipURI) sipFactory.createURI(uri);
      return sipUri;
    } catch (ServletParseException e) {
      logger.error("*** Thrown ServletParseException during "
          + "sipFactory.createURI(uri). ***", e);
      throw new TckTestException(e);
    }
  }
  
  private InetSocketAddress createInetSocketAddress(InetAddress addr, int port) {
    try {
      InetSocketAddress ids = new InetSocketAddress(addr, port);
      return ids;
    } catch (IllegalArgumentException e) {
      logger.error("*** Thrown IllegalArgumentException during new "
          + "InetSocketAddress(addr, port). ***", e);
      throw new TckTestException(e);
    }
  }
  
  private InetAddress getInetAddress(String host) {
    try {
      InetAddress ins = InetAddress.getByName(host);
      return ins;
    } catch (UnknownHostException e) {
      logger.error("*** Thrown UnknownHostException during "
          + "InetAddress.getByName(host)***", e);
      throw new TckTestException(e);
    }
  }
  
  private SipURI getOutboundInterfaceSipURI(SipServletRequest req) {
    List<SipURI> uris = (List<SipURI>) getServletContext().getAttribute(
        OUTBOUND_INTERFACES);
    if (uris.size() <= 0) {
      return null;
    }

    SipURI tempUri = (SipURI) req.getTo().getURI();
    for (SipURI uri : uris) {
      if (!tempUri.equals(uri)) {
        tempUri = uri;
        break;
      }
    }
    return tempUri;
  }
  
  private void sendResponse(SipServletRequest req, int statusCode,
      String reasonPhrase) {
    try {
      req.createResponse(statusCode, reasonPhrase).send();
    } catch (IOException e) {
      logger.error("*** Thrown IOException during SipServletResponse.send()."
          + " ***");
      throw new TckTestException(e);
    }
  }
  
  private SipSession createNewSipSession(SipServletRequest req) {
    SipApplicationSession appSession = req.getApplicationSession();
    Address from = (Address) req.getTo().clone();
    Address to = (Address) req.getFrom().clone();
    // Create a new SipSession.
    SipServletRequest request = sipFactory.createRequest(appSession, "MESSAGE",
        from, to);
    return request.getSession();
  }
  
  
  private Address getPrivateUri(SipServletRequest req){
    Address header;
    try {
      header = req.getAddressHeader(TestConstants.PRIVATE_URI);
    } catch (ServletParseException e) {
      logger.error("*** ServletParseException when retrieving private-uri header from request ***", e);
      throw new TckTestException(e);
    }

    if(header == null){
      logger.error("*** Can not find private-uri header in request ***");
      throw new TckTestException("Can not find private-uri header in request");
    }
    
    return header;
  }

}
