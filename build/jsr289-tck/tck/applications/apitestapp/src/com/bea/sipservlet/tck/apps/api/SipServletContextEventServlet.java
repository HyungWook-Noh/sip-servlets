/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.
 *  
 * SipServletContextEventServlet is used in test the APIs of 
 * javax.servlet.sip.SipServletContextEvent
 * 
 */

package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletRequest;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;

@javax.servlet.sip.annotation.SipServlet(name = "SipServletContextEvent")
public class SipServletContextEventServlet extends BaseServlet {
  private static Logger logger = Logger
      .getLogger(SipServletContextEventServlet.class);

  /*
   * Used to check the API: SipServletContextEvent(ServletContext
   * context,SipServlet servlet) Check return String.
   */
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSipServletContextEvent001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    SipServletContextEvent contextEvent = new SipServletContextEvent(
        getServletContext(), this);
    return null;
  }
}
