/**
 *(c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipSessionListenerObjectServlet is used together with SipSessionListenerServlet
 * to test the APIs of javax.servlet.sip.SipSessionListener
 */
package com.bea.sipservlet.tck.apps.api;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TckTestException;
import org.apache.log4j.Logger;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.URI;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.annotation.SipServlet;
import java.io.IOException;

@SipServlet(name="SipSessionListenerObject")
@SipListener
public class SipSessionListenerObjectServlet
    extends BaseServlet implements SipSessionListener {
  private static Logger logger = Logger.getLogger(SipSessionListenerObjectServlet.class);

  public void sessionCreated(SipSessionEvent e){
    logger.debug("=== SipSessionListener Object detects a session is created ===");
    SipSession s = e.getSession();
    s.setAttribute("testSessionCreated","true");
  }

  public void sessionDestroyed(SipSessionEvent e){
    logger.debug("=== SipSessionListener Object detects a session is destroyed ===");
    SipSession s = e.getSession();
    logger.debug("=== get session = " + s + " ===" );
    SipApplicationSession appSess = s.getApplicationSession();

    if("true".equals(appSess.getAttribute("testSessionDestroyed"))){
      checkSessionState(appSess, s);
      appSess.removeAttribute("testSessionDestroyed");
    } //end if
  }

  public void sessionReadyToInvalidate(SipSessionEvent e){
    logger.debug("=== SipSessionListener Object detects a session is ready to" +
        " invalidate ===");
    SipSession s = e.getSession();
    logger.debug("=== get session = " + s + " ===" );
    SipApplicationSession appSess = s.getApplicationSession();

    if("true".equals(appSess.getAttribute("testSessionReadyToInvalidate"))){
      checkSessionState(appSess, s);
      appSess.removeAttribute("testSessionReadyToInvalidate");
    } //end if
  }

  private void checkSessionState(SipApplicationSession appSess, SipSession s){
    SipServletRequest oriReq
        = (SipServletRequest)appSess.getAttribute("orig_request");
    if(oriReq == null){
      logger.error("*** can't get original request to generate a sip " +
          "message to send back, the method will exit *** ");
      return;
    }
    String sipMethod = oriReq.getMethod();
    String toURIStr = null;
    if("INVITE".equalsIgnoreCase(sipMethod)){
      toURIStr = oriReq.getHeader("Contact");
    }else{
      toURIStr = oriReq.getHeader("From");
    }

    URI fromURI = oriReq.getRequestURI();

    String sid = s.getId();
    logger.debug("in listener the session id=" + s.getId());
    SipServletRequest req = null;
    try{
      req = sipFactory.createRequest(
              sipFactory.createApplicationSession(),
              "MESSAGE",
              fromURI.toString(),
              toURIStr
              );

      if(sid.equals(appSess.getAttribute("sessionId"))){
        req.setHeader("Test-Result","ok");
      }else{
        req.setHeader("Test-Result","sessionId not equal");
      }
      req.send();
    }catch(ServletParseException ex){
      logger.error("*** can't generate a request according the original" +
          "request because of ServletParseException ***", ex);
      throw new TckTestException(ex);
    }catch (IOException ex) {
      logger.error("***IOException occurs during sending response ***",ex);
      throw new TckTestException(ex);
    }
  }
}
