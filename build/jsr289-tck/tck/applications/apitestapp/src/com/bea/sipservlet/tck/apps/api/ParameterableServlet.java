/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * ParameterableServlet is used to test the APIs of 
 * javax.servlet.sip.Parameterable
 */
package com.bea.sipservlet.tck.apps.api;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletRequest;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;

@javax.servlet.sip.annotation.SipServlet(name = "Parameterable")
public class ParameterableServlet extends BaseServlet {
  private static final long serialVersionUID = 308407823380715342L;
  private static Logger logger = Logger.getLogger(ParameterableServlet.class);

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testClone001(SipServletRequest req) {
    serverEntryLog();
    Parameterable para1 = getParameterableHeader(req, "From");
    Parameterable para2 = (Parameterable)para1.clone();
    return (para1.equals(para2) && para1 != para2) ? 
        null : "Fail to clone Parameterable";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testEquals001(SipServletRequest req) {
  	serverEntryLog();
    Parameterable para1 = getParameterableHeader(req, "From");
    Parameterable para2 = getParameterableHeader(req, "To");
    return (!para1.equals(para2)) ? null : "Fail to compare Parameterables";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetRemoveParameter001(SipServletRequest req) {
  	serverEntryLog();
    Parameterable para1 = getParameterableHeader(req, "From");
    Parameterable para2 = (Parameterable)para1.clone();
    para2.setParameter("aa", "AA");
    String value1 = para2.getParameter("aa");
    if(!"AA".equals(value1)) return "Fail to get and set parameter of Parameterable";
    para2.removeParameter("aa");
    String value2 = para2.getParameter("aa");
    
    return (value2 == null) ? null : "Fail to remove parameter of Parameterable";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetParameter101(SipServletRequest req) {
  	serverEntryLog();
    Parameterable para = getParameterableHeader(req, "From");
    try {
    	String value = para.getParameter(null);
    } catch (NullPointerException e) {
    	logger.info("=== Expected NullPointerException thrown when getParameter(). ===");
    	return null;
    }
    return "Fail to throw NullPointerException.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testRemoveParameter101(SipServletRequest req) {
  	serverEntryLog();
    Parameterable para1 = getParameterableHeader(req, "From");
    try {
    	para1.removeParameter("tag");
    } catch (IllegalStateException e) {
    	logger.info("=== Expected IllegalStateException thrown when removeParameter(). ===");
    	return null;
    }
    return "Fail to throw IllegalStateException.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testRemoveParameter102(SipServletRequest req) {
  	serverEntryLog();
    Parameterable para1 = getParameterableHeader(req, "From");
    Parameterable para2 = (Parameterable)para1.clone();
    try {
    	para2.removeParameter(null);
    } catch (NullPointerException e) {
    	logger.info("=== Expected NullPointerException thrown when removeParameter(). ===");
    	return null;
    }
    return "Fail to throw NullPointerException.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetParameter101(SipServletRequest req) {
  	serverEntryLog();
    Parameterable para1 = getParameterableHeader(req, "From");
    try {
    	para1.setParameter("tag", "123456");
    } catch (IllegalStateException e) {
    	logger.info("=== Expected IllegalStateException thrown when setParameter(). ===");
    	return null;
    }
    return "Fail to throw IllegalStateException.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetParameter102(SipServletRequest req) {
  	serverEntryLog();
    Parameterable para1 = getParameterableHeader(req, "From");
    Parameterable para2 = (Parameterable)para1.clone();
    try {
    	para2.setParameter(null, "AA");
    } catch (NullPointerException e) {
    	logger.info("=== Expected NullPointerException thrown when setParameter(). ===");
    	return null;
    }
    return "Fail to throw NullPointerException.";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetParameterNames001(SipServletRequest req) {
  	serverEntryLog();
    Parameterable para1 = getParameterableHeader(req, "From");
    Parameterable para2 = (Parameterable) para1.clone();
    para2.setParameter("aa", "AA");
    para2.setParameter("bb", "BB");
    boolean isAA = false;
    boolean isBB = false;
    Iterator<String> it = para2.getParameterNames();
    while (it.hasNext()) {
      String name = it.next();
      if ("aa".equals(name))
        isAA = true;
      if ("bb".equals(name))
        isBB = true;
    }
    return (isAA && isBB) ? 
        null : "Fail to get the correct parameter name list of Parameterable";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetParameters001(SipServletRequest req) {
  	serverEntryLog();
    Parameterable para1 = getParameterableHeader(req, "From");
    Parameterable para2 = (Parameterable)para1.clone();
    para2.setParameter("aa", "AA");
    para2.setParameter("bb", "BB");
    boolean isAA = false;
    boolean isBB = false;
    Set<Map.Entry<String, String>> set = para2.getParameters();
    for(Map.Entry<String, String> entry : set){
      if("aa".equals(entry.getKey()) && "AA".equals(entry.getValue())) isAA = true;
      if("bb".equals(entry.getKey()) && "BB".equals(entry.getValue())) isBB = true;      
    }
    return (isAA && isBB) ? 
        null : "Fail to get the correct parameter set of Parameterable";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetValue001(SipServletRequest req) {
  	serverEntryLog();
    Parameterable para1 = getParameterableHeader(req, "From");
    Parameterable para2 = (Parameterable)para1.clone();
    para2.setValue("Alice <sip:alice@example.com:5021>");
    String value = para2.getValue();
    return ("Alice <sip:alice@example.com:5021>".equals(value)) ? null
        : "Fail to get and set value of Parameterable";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetValue101(SipServletRequest req) {
  	serverEntryLog();
    Parameterable para1 = getParameterableHeader(req, "From");
    try {
    	para1.setValue("Alice <sip:alice@example.com:5021>");
    }catch (IllegalStateException e) {
    	logger.info("=== Expected IllegalStateException thrown when setValue(). ===");
    	return null;
    }
    return "Fail to throw IllegalStateException.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetValue102(SipServletRequest req) {
  	serverEntryLog();
    Parameterable para1 = getParameterableHeader(req, "From");
    Parameterable para2 = (Parameterable)para1.clone();
    try {
    	para2.setValue(null);
    }catch (NullPointerException e) {
    	logger.info("=== Expected NullPointerException thrown when setValue(). ===");
    	return null;
    }
    return "Fail to throw NullPointerException.";
  }
  
  private static Parameterable getParameterableHeader(
      SipServletRequest req, String headerName){
    try {
      return req.getParameterableHeader(headerName);
    } catch (ServletParseException e) {
      logger.error("*** ServletParseException when retrieving parameterable header ***", e);
      throw new TckTestException(e);
    }
  }

}
