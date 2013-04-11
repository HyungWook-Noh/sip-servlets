/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * TelURLServlet is used to test the APIs of 
 * javax.servlet.sip.TelURL
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.TelURL;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;

@javax.servlet.sip.annotation.SipServlet(name = "TelURL")
public class TelURLServlet extends BaseServlet {
  private static final long serialVersionUID = 2253198568624482543L;
  private static Logger logger = Logger.getLogger(TelURLServlet.class);

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testEquals001(SipServletRequest req) {
    serverEntryLog();
    
    TelURL telURL1 = createTelURL("tel:+1-201-555-0123");
    TelURL telURL2 = createTelURL("tel:7042;phone-context=example.com");
    return (!telURL1.equals(telURL2)) ? null : "Fail to compare TelURLs";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetPhoneContext001(SipServletRequest req) {
    serverEntryLog();
    
    TelURL telURL = createTelURL("tel:7042;phone-context=example.com");
    String context = telURL.getPhoneContext();
    return ("example.com".equals(context)) ? 
        null : "Fail to get phone context of this TelURL";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetPhoneNumber001(SipServletRequest req) {
    serverEntryLog();
    
    TelURL telURL = createTelURL("tel:+1-201-555-0123");
    telURL.setPhoneNumber("+1-201-555-0124");
    if (!"1-201-555-0124".equals(telURL.getPhoneNumber()))
      return "Fail to set and get andget phone number of this TelURL";
    telURL.setPhoneNumber("7024", "example.com");
    return "7024".equals(telURL.getPhoneNumber()) ? 
        null : "Fail to set and get andget phone number of this TelURL";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testIsGlobal001(SipServletRequest req) {
    serverEntryLog();
    
    TelURL telURL1 = createTelURL("tel:+1-201-555-0123");
    TelURL telURL2 = createTelURL("tel:7042;phone-context=example.com");
    return (telURL1.isGlobal() && !telURL2.isGlobal()) ? 
        null : "Fail to determine if the TelURL is global";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testToString001(SipServletRequest req) {
    serverEntryLog();
    
    TelURL telURL = createTelURL("tel:+1-201-555-0123");
    telURL.setPhoneNumber("+1-201-555-0124");
    if (!"1-201-555-0124".equals(telURL.getPhoneNumber()))
      return "Fail to set and get andget phone number of this TelURL";
    telURL.setPhoneNumber("7024", "example.com");
    return "7024".equals(telURL.getPhoneNumber())? 
        null : "Fail to set and get andget phone number of this TelURL";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetPhoneNumber101(SipServletRequest req) {
    serverEntryLog();
    
    TelURL telURL = createTelURL("tel:+1-201-555-0123");
    try {
      telURL.setPhoneNumber("+1-201-555-012/4");
      return "Fail to get IllegalArgumentException";
    } catch (IllegalArgumentException  e) {
      logger.info("=== IllegalArgumentException ===");
      return null;
    }
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetPhoneNumber102(SipServletRequest req) {
    serverEntryLog();
    
    TelURL telURL = createTelURL("tel:+1-201-555-0123");   
    try {
      telURL.setPhoneNumber("+1-201-555-012/4", "example.com");
      return "Fail to get IllegalArgumentException with two parameters";
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
  
  private static TelURL createTelURL(String uriString) {
	if(uriString == null) return null;
    try {
      return (TelURL) sipFactory.createURI(uriString);
    } catch (ServletParseException e) {
      logger.error("*** ServletParseException when creating URI with SipFactory ***", e);
      throw new TckTestException(e);
    }
  }
}
