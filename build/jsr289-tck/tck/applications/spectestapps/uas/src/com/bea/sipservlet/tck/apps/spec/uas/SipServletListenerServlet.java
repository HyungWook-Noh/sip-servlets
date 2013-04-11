/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * SipServletListenerServlet is used to test the specification of 
 * SipServletListener
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import com.bea.sipservlet.tck.apps.BaseServlet;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.annotation.SipListener;
import java.io.IOException;

@javax.servlet.sip.annotation.SipServlet(name = "sipservletlistener")
@SipListener(applicationName = "com.bea.sipservlet.tck.apps.spectestapp.uas") 
public class SipServletListenerServlet extends BaseServlet implements
    SipServletListener {
  private static Logger logger = Logger.getLogger(SipServletListenerServlet.class);
  private static final long serialVersionUID = -1652672419697066869L;

  private boolean isSipServletInitialized = false;
  
  private int invokedTimes = 0;
  
  private static String failReason = "servletInitialized() callback is not invoked.";

  @Override
  protected void doMessage(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    if (isSipServletInitialized && invokedTimes == 1) {
      req.createResponse(SipServletResponse.SC_OK).send();
    } else {
      req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
          failReason).send();
    }
  }

  public void servletInitialized(SipServletContextEvent event) {
    if("sipservletlistener".equals(event.getSipServlet().getServletName())){
      invokedTimes++;
      logger.warn("***the servletInitialized() is invoked for" + invokedTimes + " times ***");
      if (isSipServletInitialized) {
        failReason = "The SipServletListenerServlet has already been initialized.";
      } else if (event == null || event.getServletContext() == null
          || event.getSipServlet() == null) {
        isSipServletInitialized = false;
        failReason = "The event parameter passed by the callback "
            + "servletInitialized(SipServletContextEvent event) is not correct."
            + "Ethier the event is null, or event.getServletContext() is null,"
            + "or event.getSipServlet() is null.";
      } else if (event.getSipServlet() == this) {
        logger.debug("servletInitialized is invoked...");
        isSipServletInitialized = true;
      }
    }
  }
}
