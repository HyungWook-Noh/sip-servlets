/**
 *(c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 * 
 * SipSessionsUtilServlet is used to test the APIs of
 * javax.servlet.sip.SipSessionsUtil
 */

package com.bea.sipservlet.tck.apps.api;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TestConstants;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.annotation.SipApplicationKey;
import javax.servlet.sip.annotation.SipServlet;

@SipServlet(name="SipSessionsUtil")
public class SipSessionsUtilServlet extends BaseServlet {
  public static Logger logger = Logger.getLogger(SipSessionsUtilServlet.class);

  private SipSessionsUtil util;

  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    util = (SipSessionsUtil)getServletContext()
        .getAttribute("javax.servlet.sip.SipSessionsUtil");
    if(util == null){
      logger.error("*** No SipSessionsUtil in context ***");
      throw new ServletException("No SipSessionsUtil in context");
    }
  }

  @TestStrategy(
      strategy = TESTSTRATEGY_SIMPLEASSERT
  )
  public String testGetApplicationSessionById001(SipServletRequest req){
    serverEntryLog();
    SipApplicationSession s = req.getApplicationSession();
    String id = s.getId();
    logger.info("=== session id=" + id + "|||||||" + s + " ===");
    s.setAttribute("testGetById","true");
    return testGetSessionById(id,"testGetById","true");
  }

  @TestStrategy(
      strategy = TESTSTRATEGY_SIMPLEASSERT
  )
  public String testGetApplicationSessionById101(SipServletRequest req){
    serverEntryLog();
    try{
      util.getApplicationSessionById(null);
    }catch(NullPointerException e){
      return null;
    }
    return "NullPointerException is not thrown when the sid is null.";
  }
  
  @TestStrategy(
      strategy = TESTSTRATEGY_SIMPLEASSERT
  )
  public String testGetApplicationSessionByKey001(SipServletRequest req){
    serverEntryLog();
    String key = "TESTKEY001";
    SipApplicationSession s = req.getApplicationSession();

    s.setAttribute("testGetByKey","true");
    return testGetSessionByKey(key,"testGetByKey","true"); 
  }

  @TestStrategy(
      strategy = TESTSTRATEGY_SIMPLEASSERT
  )
  public String testGetApplicationSessionByKey101(SipServletRequest req){
    serverEntryLog();
    try{
      util.getApplicationSessionByKey(null,true);
    }catch(NullPointerException e){
      return null;
    }
    return "NullPointerException is not thrown when the key is null.";
  }

  @SipApplicationKey
  public static String generateSessionKey(SipServletRequest req){
    logger.debug("=== ready to generateSessionKey ===");
    if("SipSessionsUtil".equals(req.getHeader(TestConstants.SERVLET_HEADER))){
      logger.info("=== generate a key for SipSessionsUtilServlet ===");
    //return the same key
      return "TESTKEY001";
    }else{
      return null;
    }
  }


  //it is ensured that expectAttrVal is not null in the program context
  private String testGetSessionById(String sid,
                                          String attrName,
                                          String expectAttrVal){
    logger.debug("=== util is null?:" + (util==null?true:false) + " ===");
    
    SipApplicationSession s = util.getApplicationSessionById(sid);

    if(s == null){
      logger.error("*** app session is null! ***");
      return "session is null";
    }
    String value = (String)s.getAttribute(attrName);
    logger.info("=== get value:" + value + " ===");
    if(expectAttrVal.equals(value)){
      return null;
    }else{
      return "value is '" + value + "' which is not equal with the expected. ";
    }
  }

  private String testGetSessionByKey(String key,
                                           String attrName,
                                           String expectAttrVal){
    // the flag is set to false because we can assume the session was created before
    // if the session is not found the test fails
    SipApplicationSession s = util.getApplicationSessionByKey(key,false);
    if(s == null){
      logger.error("*** app session is null! ***");
      return "session is null";
    }
    String value = (String)s.getAttribute(attrName);
    logger.info("=== get value:" + value + " ===");
    if(expectAttrVal.equals(value)){
      return null;
    }else{
      return "value is '" + value + "' which is not equal with the expected. ";
    }
  }

}
