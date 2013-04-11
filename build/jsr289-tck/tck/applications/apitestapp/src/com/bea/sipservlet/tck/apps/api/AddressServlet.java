/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * AddressServlet is used to test the APIs of 
 * javax.servlet.sip.Address
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.utils.TestUtil;

@javax.servlet.sip.annotation.SipServlet(name = "Address")
public class AddressServlet extends BaseServlet {
  private static final long serialVersionUID = -8208883216338783969L;
  private static Logger logger = Logger.getLogger(AddressServlet.class);
  
  private static final String ADDR_STR_1 = 
    "\"Jeo\" <sip:joe@example.com:5060;lr>;q=0.6;expires=3601";
  private static final String ADDR_STR_2 = 
    "\"Alice\" <sip:alice@example.com:5061;lr>;q=0.7;expires=3602";
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testClone001(SipServletRequest req) {
    serverEntryLog();
    Address addr1 = createAddress(ADDR_STR_1);
    Address addr2 = (Address)addr1.clone();
    return (addr1.equals(addr2) && addr1 != addr2) ? 
        null : "Fail to clone Address";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testEquals001(SipServletRequest req) {
    serverEntryLog();
    
    String failCase = "";
    //case 1
    if(!compareAddress(
        "\"Alice\" <sip:%61lice@bea.com;transport=TCP;lr>;q=0.6;expires=3601",
        "\"Alice02\" <sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601")){
      failCase += "\"case 1\"";
    }
    
    //case 2
    if(!compareAddress(
        "<sip:%61lice@bea.com;transport=TCP;lr>;expires=3601;q=0.6",
        "<sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601")){
      failCase += "\"case 2\"";
    }
    
    //case 3
    if(!compareAddress(
        "<sip:%61lice@bea.com;transport=TCP;lr>;q=0.6",
        "<sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601")){
      failCase += "\"case 3\"";
    }
    
    //case 4
    if(compareAddress(
        "<sip:%61lice@bea.com;transport=TCP;lr>;q=0.5",
        "<sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601")){
      failCase += "\"case 4\"";
    }    
    
    return (failCase.length() > 0) ? 
        "Fail to compare Addresses in " + failCase : null;
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetDisplayName001(SipServletRequest req) {
    serverEntryLog();
    Address addr1 = createAddress(ADDR_STR_1);
    addr1.setDisplayName("John");
    String dispName = addr1.getDisplayName();
    return ("John".equals(dispName)) ? 
        null : "Fail to set and get display name of Address";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetExpires001(SipServletRequest req) {
    serverEntryLog();
    Address addr1 = createAddress(ADDR_STR_1);
    addr1.setExpires(128);
    return (addr1.getExpires() == 128) ? 
        null : "Fail to set and get \"Expires\" parameter of Address";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetQ001(SipServletRequest req) {
    serverEntryLog();
    Address addr1 = createAddress(ADDR_STR_1);
    addr1.setQ(0.3f);
    return (addr1.getQ() == 0.3f) ? 
        null : "Fail to set and get \"Q\" parameter of Address";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetURI001(SipServletRequest req) {
    serverEntryLog();
    Address addr1 = createAddress(ADDR_STR_1);
    URI uri1 = createURI("sip:john@example2.com");
    addr1.setURI(uri1.clone());
    return (uri1.equals(addr1.getURI())) ? 
        null : "Fail to set and get URI of Address";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testIsWildcard001(SipServletRequest req) {
    serverEntryLog();
    Address addr1 = createAddress(ADDR_STR_1);
    Address addr2 = createAddress("*");
    return (!addr1.isWildcard() && addr2.isWildcard()) ? 
        null : "Fail to determine if this Address is wildcard";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testToString001(SipServletRequest req) {
    serverEntryLog();
    Address addr1 = createAddress(ADDR_STR_1);
    return TestUtil.hasText(addr1.toString()) ? 
        null : "Fail to get toString result of Address";
  }
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetDisplayName101(SipServletRequest req) {
    serverEntryLog();
    Address addr1 = req.getFrom();
    try {
      addr1.setDisplayName("John");
      return "Fail to get IllegalStateException";
    } catch (IllegalStateException  e) {
      return null;      
    }
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetURI101(SipServletRequest req) {
    serverEntryLog();    
   
    Address addr = req.getFrom();
    try {
      addr.setURI(createURI("sip:uas@domain:5060"));
      return "Fail to get IllegalStateException";
    } catch (IllegalStateException  e) {
      return null; 
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetURI102(SipServletRequest req) {
    serverEntryLog();    
    Address addr = createAddress(ADDR_STR_1);
    try {
      addr.setURI(null);
      return "Fail to get NullPointerException";
    } catch (NullPointerException  e1) {
      logger.info("=== get NullPointerException ===");
      return null; 
    }
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetQ101(SipServletRequest req) {
    serverEntryLog();  
    Address addr1 = createAddress(ADDR_STR_1);
    try {
      addr1.setQ(-1.2f);
      return "Fail to get IllegalArgumentException";
    } catch (IllegalArgumentException  e) {
      logger.info("=== get IllegalArgumentException ===");
    }
    try {
      addr1.setQ(1.1f);
      return "Fail to get IllegalArgumentException";
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
  private static Address createAddress(String addrStr) {
    if(addrStr == null) return null;
    try {
      return sipFactory.createAddress(addrStr);
    } catch (ServletParseException e) {
      logger.error("*** ServletParseException when creating Address with SipFactory ***", e);
      throw new TckTestException(e);
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
  
  private boolean compareAddress(String add1, String add2){
    Address addr1 = createAddress(add1);
    Address addr2 = createAddress(add2);
    return addr1.equals(addr2);   
  }

}
