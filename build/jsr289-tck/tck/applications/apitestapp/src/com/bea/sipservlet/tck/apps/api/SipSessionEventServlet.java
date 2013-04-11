/**
 *(c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipSessionEventServlet is used to test the constructor of
 * javax.servlet.sip.SipSessionEvent. Other APIs are tested in
 * SipSessionListenerServlet.
 */
package com.bea.sipservlet.tck.apps.api;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import org.apache.log4j.Logger;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.annotation.SipServlet;

@SipServlet(name="SipSessionEvent")
public class SipSessionEventServlet extends BaseServlet {
  private static Logger logger =
      Logger.getLogger(SipSessionEventServlet.class);

  @TestStrategy(
      strategy = TESTSTRATEGY_SIMPLEASSERT
  )
  public String testSipSessionEvent001(SipServletRequest req){
    serverEntryLog();
    SipSession s = req.getSession();
    s.setAttribute("method","testSipSessionEvent001");
    SipSessionEvent event
        = new SipSessionEvent(s);
    if("testSipSessionEvent001".equals(s.getAttribute("method")) ){
      logger.info("=== everything is ok ===");
      return null;
    }else{
      logger.error("*** event=" + event + "; "  +
          "get(method)=" + s.getAttribute("method") + "***");
      return "event=" + event + "; " +
          "get(method)=" + s.getAttribute("method");
    }
  }
}
