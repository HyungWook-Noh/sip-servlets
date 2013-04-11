/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.
 *  
 * SipServletListenerServlet is used in test the APIs of 
 * javax.servlet.sip.SipServletListener
 * 
 */

package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;

@SipServlet(applicationName = "com.bea.sipservlet.tck.apps.apitestapp", name = "SipServletListener", loadOnStartup = 1)
@SipListener(applicationName = "com.bea.sipservlet.tck.apps.apitestapp")
public class SipServletListenerServlet extends BaseServlet implements
    SipServletListener {
  private static final long serialVersionUID = -1652672419697066869L;

  private static Logger logger = Logger
      .getLogger(SipServletListenerServlet.class);

  private boolean IS_INIT = false;

  public void servletInitialized(SipServletContextEvent ce) {
    if (IS_INIT || ce == null || ce.getServletContext() == null
        || ce.getSipServlet() == null) {
      return;
    }
    logger.info("servlet init: " + ce.getSipServlet().getServletName());
    IS_INIT = true;
  }

  /*
   * Used to check the API: servletInitialized(SipServletContextEvent ce) return
   * String
   */
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testServletInitialized001(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    if (!IS_INIT) {
      return "Fail to get initialize Servlet event";
    }
    return null;
  }

}
