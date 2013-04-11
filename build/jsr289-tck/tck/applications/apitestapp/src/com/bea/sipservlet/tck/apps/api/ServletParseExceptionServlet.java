/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.
 *  
 * ServletParseExceptionServlet is used to test the APIs of 
 * javax.servlet.sip.ServletParseException.
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;


@SipServlet 
( applicationName="com.bea.sipservlet.tck.apps.apitestapp",
  name="ServletParseException")
public class ServletParseExceptionServlet extends BaseServlet {
  private static Logger logger = Logger
      .getLogger(ServletParseExceptionServlet.class);

  private static final long serialVersionUID = 1L;

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testServletParseException001(SipServletRequest req) {
    serverEntryLog();
    ServletParseException ex1 = new ServletParseException();
    if (ex1 != null) {
      return null;
    } else {
      logger.error("*** Construct ServletParseException failed. ***");
      return "Construct ServletParseException failed.";
    }
  }   
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testServletParseException002(SipServletRequest req) {
    serverEntryLog();
    ServletParseException ex2 = new ServletParseException("Constructs a"
        + " new parse exception with the specified message");
    if (ex2 != null) {
      return null;
    } else {
      logger.error("*** Construct ServletParseException failed. ***");
      return "Construct ServletParseException failed.";
    }
  }  

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testServletParseException003(SipServletRequest req) {
    serverEntryLog();
    ServletParseException ex3 = new ServletParseException("Constructs a"
        + " new parse exception with the specified detail message and cause",
        new Throwable());
    if (ex3 != null) {
      return null;
    } else {
      logger.error("*** Construct ServletParseException failed. ***");
      return "Construct ServletParseException failed.";
    }
  }  
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testServletParseException004(SipServletRequest req) {
    serverEntryLog();
    ServletParseException ex4 = new ServletParseException(new Throwable());
    if (ex4 != null) {
      return null;
    } else {
      logger.error("*** Construct ServletParseException failed. ***");
      return "Construct ServletParseException failed.";
    }
  }  
}
