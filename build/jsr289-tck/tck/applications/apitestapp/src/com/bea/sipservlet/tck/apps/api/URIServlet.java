/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * URIServlet is used to test the APIs of 
 * javax.servlet.sip.URI
 */
package com.bea.sipservlet.tck.apps.api;

import java.util.Iterator;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.utils.TestUtil;

@javax.servlet.sip.annotation.SipServlet(name = "URI")
public class URIServlet extends BaseServlet {
  private static final long serialVersionUID = 9146274435855459620L;
  private static Logger logger = Logger.getLogger(URIServlet.class);

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testClone001(SipServletRequest req) {
    serverEntryLog();
    
    URI uri1 = createURI("sip:joe@example.com");
    URI uri2 = uri1.clone();
    return (uri1.equals(uri2) && uri1 != uri2) ? null : "Fail to clone URI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testEquals001(SipServletRequest req) {
    serverEntryLog();
    
    URI uri1 = createURI("sip:joe@example.com");
    URI uri2 = createURI("sip:Alice@example.com");
    return (!uri1.equals(uri2)) ? null : "Fail to clone URIs";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetRemoveParameter001(SipServletRequest req) {
    serverEntryLog();
    
    URI uri = createURI("sip:joe@example.com");
    uri.setParameter("aa", "AA");
    String para = uri.getParameter("aa");
    if (!"AA".equals(para)) return "Fail to set and get parameter of this URI";
    uri.removeParameter("aa");
    para = uri.getParameter("aa");
    return (para == null) ? null : "Fail to remove parameter of this URI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetParameterNames001(SipServletRequest req) {
    serverEntryLog();
    
    URI uri = createURI("sip:joe@example.com");
    uri.setParameter("aa", "AA");
    uri.setParameter("bb", "BB");
    boolean isAA = false;
    boolean isBB = false;
    Iterator<String> it = uri.getParameterNames();
    while (it.hasNext()) {
      String value = it.next();
      if ("aa".equals(value)) isAA = true;
      if ("bb".equals(value)) isBB = true;
    }
    return (isAA && isBB) ? 
        null : "Fail to get the correct parameter name list of this URI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetScheme001(SipServletRequest req) {
    serverEntryLog();
    
    URI uri = createURI("sip:joe@example.com");
    String scheme = uri.getScheme();
    return "sip".equals(scheme) ? null : "Fail to get the correct scheme of this URI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testIsSipURI001(SipServletRequest req) {
    serverEntryLog();
    
    URI uri = createURI("sip:joe@example.com");
    return uri.isSipURI() ? null : "Fail to determine if this URI is a SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testToString001(SipServletRequest req) {
    serverEntryLog();
    
    String uriStr = "sip:joe@example.com";
    URI uri = createURI(uriStr);
    String str = uri.toString();
    return TestUtil.hasText(str) && uriStr.equals(str)? 
        null : "Fail to get toString result of URI";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetParameter101(SipServletRequest req) {
    serverEntryLog();
    
    URI uri = createURI("sip:joe@example.com");
    try {
      uri.getParameter(null);
      return "Fail to get NullPointerException";
    } catch (NullPointerException  e) {
      return null;
    }
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetParameter101(SipServletRequest req) {
    serverEntryLog();
    
    URI uri = createURI("sip:joe@example.com");
    try {
      uri.setParameter("name", null);
      return "Fail to get NullPointerException001";
    } catch (NullPointerException  e) {
      logger.info("=== get NullPointerException ===");
    }
    
    try {
      uri.setParameter(null, "value");
      return "Fail to get NullPointerException002";
    } catch (NullPointerException e) {
      logger.info("=== get NullPointerException ===");
    }
    
    try {
      uri.setParameter(null, null);
      return "Fail to get NullPointerException003";
    } catch (NullPointerException e) {
      return null;
    }
  }
  
  private static URI createURI(String uriString) {
    if(uriString == null) return null;
    try {
      return sipFactory.createURI(uriString);
    } catch (ServletParseException e) {
      logger.error("*** ServletParseException when creating URI with SipFactory ***", e);
      throw new TckTestException(e);
    }
  } 
}
