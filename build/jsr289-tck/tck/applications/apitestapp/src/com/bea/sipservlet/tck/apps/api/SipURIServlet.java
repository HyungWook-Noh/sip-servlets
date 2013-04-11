/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * SipURIServlet is used to test the APIs of 
 * javax.servlet.sip.SipURI
 */
package com.bea.sipservlet.tck.apps.api;

import java.util.Iterator;

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.utils.TestUtil;

@javax.servlet.sip.annotation.SipServlet(name = "SipURI")
public class SipURIServlet extends BaseServlet {
  private static final long serialVersionUID = 4182951812643069558L;
  public static Logger logger = Logger.getLogger(SipURIServlet.class);

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testEquals001(SipServletRequest req) {
    serverEntryLog();
    
    if(!compareSipURI(
        "sip:%61lice@bea.com;transport=TCP;lr",
        "sip:alice@BeA.CoM;Transport=tcp;lr")){
      return "Fail to compare SipURIs";
    }

    if(!compareSipURI(
        "sip:biloxi.com;transport=tcp;method=REGISTER?to=sip:bob%40biloxi.com",
        "sip:biloxi.com;method=REGISTER;transport=tcp?to=sip:bob%40biloxi.com")){
      return "Fail to compare SipURIs";
    }

    if(!compareSipURI(
        "sip:alice@atlanta.com?subject=project%20x&priority=urgent",
        "sip:alice@atlanta.com?priority=urgent&subject=project%20x")){
      return "Fail to compare SipURIs";
    }
    
    if(compareSipURI(
        "sip:%61lice@bea.com;transport=TCP;lr",
        "sip:alice@bea.com:5060;transport=TCP;lr")){
      return "Fail to compare SipURIs";
    }
    
    if(compareSipURI(
        "sip:ALICE@AtLanTa.CoM;Transport=udp",
        "sip:alice@AtLanTa.CoM;Transport=UDP")){
      return "Fail to compare SipURIs";
    }
    
    if(compareSipURI(
        "sip:bob@biloxi.com",
        "sip:bob@biloxi.com;transport=udp")){
      return "Fail to compare SipURIs";
    }
    
    if(compareSipURI(
        "sip:carol@chicago.com",
        "sip:carol@chicago.com?Subject=next%20meeting")){
      return "Fail to compare SipURIs";
    }
    
    if(!compareSipURI(
        "sip:carol@chicago.com",
        "sip:carol@chicago.com;security=on")){
      return "Fail to compare SipURIs";
    }

    if(compareSipURI(
        "sip:carol@chicago.com;security=off",
        "sip:carol@chicago.com;security=on")){
      return "Fail to compare SipURIs";
    }  
  
    return null;
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetRemoveHeader001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    sipURI.setHeader("priority", "emergency");
    String value = sipURI.getHeader("priority");
    if (!"emergency".equals(value))
      return "Fail to set and get header of SipURI";

    sipURI.removeHeader("priority");
    value = sipURI.getHeader("priority");
    return value == null ? null : "Fail to remove header of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetHeaderNames001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    sipURI.setHeader("priority", "emergency");
    sipURI.setHeader("subject", "bad things");
    boolean isPriority = false;
    boolean isSubject = false;
    Iterator<String> it = sipURI.getHeaderNames();
    while (it.hasNext()) {
      String name = it.next();
      if ("priority".equals(name)) isPriority = true;
      if ("subject".equals(name)) isSubject = true;
    }

    return (isPriority && isSubject) ? 
        null : "Fail to get the correct header name list of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetHost001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    if (!"example.com".equals(sipURI.getHost()))
      return "Fail to set and get \"Host\" of SipURI";
    sipURI.setHost("example2.com");
    return "example2.com".equals(sipURI.getHost()) ? 
        null : "Fail to set and get \"Host\" of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetLrParam001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    if (sipURI.getLrParam()) return "Fail to set and get \"Lr\" of SipURI";
    sipURI.setLrParam(true);
    return sipURI.getLrParam() ? null : "Fail to set and get \"Lr\" of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetMAddrParam001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    sipURI.setMAddrParam("239.255.255.1");
    return "239.255.255.1".equals(sipURI.getMAddrParam()) ? 
        null : "Fail to set and get \"maddr\" parameter of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetMethodParam001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    sipURI.setMethodParam("REGISTER");
    return "REGISTER".equals(sipURI.getMethodParam()) ? 
        null : "Fail to set and get \"method\" parameter of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetPort001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    sipURI.setPort(5061);
    return sipURI.getPort() == 5061 ? null : "Fail to set and get Port of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetTransportParam001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    sipURI.setTransportParam("sctp");
    return "sctp".equals(sipURI.getTransportParam()) ? 
        null : "Fail to set and get \"transport\" parameter of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetTTLParam001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    sipURI.setTTLParam(61);
    return sipURI.getTTLParam() == 61 ? 
        null : "Fail to set and get \"ttl\" parameter of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetUser001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    sipURI.setUser("Alice");
    return "Alice".equals(sipURI.getUser()) ? 
        null : "Fail to set and get User of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetUserParam001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    sipURI.setUserParam("John");
    return "John".equals(sipURI.getUserParam()) ? 
        null : "Fail to set and get \"user\" parameter of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetUserPassword001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    sipURI.setUserPassword("123abc");
    return "123abc".equals(sipURI.getUserPassword())? 
        null : "Fail to set and get user password of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testIsSetSecure001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    sipURI.setSecure(true);
    return sipURI.isSecure() ? null : "Fail to set and get security of SipURI";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testToString001(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    return TestUtil.hasText(sipURI.toString()) ? 
        null : "Fail to get toString result of SipURI";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetHeader101(SipServletRequest req) {
    serverEntryLog();
    
    SipURI sipURI = sipFactory.createSipURI("joe", "example.com");
    sipURI.setHeader("priority", "emergency");
    try {
      sipURI.getHeader(null);
      return "Fail to get NullPointerException";
    } catch (NullPointerException e) {
      return null;
    }
  } 
  
  private static SipURI createURI(String uriString) {
    if(uriString == null) return null;
    try {
      return (SipURI)sipFactory.createURI(uriString);
    } catch (ServletParseException e) {
      logger.error("*** ServletParseException when creating URI with SipFactory ***", e);
      throw new TckTestException(e);
    }
  } 
  
  private boolean compareSipURI(String uri1, String uri2){
    SipURI sipURI1 = createURI(uri1);
    SipURI sipURI2 = createURI(uri2);
    return sipURI1.equals(sipURI2);   
  }
}
