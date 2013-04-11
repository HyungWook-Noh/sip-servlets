/**
 *(c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipSessionBindingListenerServlet is used to test the APIs of
 * javax.servlet.sip.SipSessionBindingListener.
 */
package com.bea.sipservlet.tck.apps.api;

import com.bea.sipservlet.tck.apps.BaseServlet;
import org.apache.log4j.Logger;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.annotation.SipServlet;

@SipServlet(name="SipSessionBindingListener")
public class SipSessionBindingListenerServlet extends BaseServlet {
  private static Logger logger
      = Logger.getLogger(SipSessionBindingListenerServlet.class);


  public void testValueBound001(SipServletRequest req){
    serverEntryLog();
    SipSession sess = req.getSession();
//    SipApplicationSession appSess = req.getApplicationSession();
    sess.setAttribute("request", req);
    logger.info("=== request has been set into app session ===");
    sess.setAttribute("testValueBound001", "true");
    sess.setAttribute("testObject", new SipSessionBindingObject());
    logger.info("=== value1 has been set into sip session ===");
  }

  
  public void testValueUnbound001(SipServletRequest req){
    serverEntryLog();
    SipSession sess = req.getSession();
//    SipApplicationSession appSess = req.getApplicationSession();
    sess.setAttribute("request", req);
    logger.info("=== request has been set into app session ===");
    sess.setAttribute("testValueUnbound001", "true");
    sess.setAttribute("testObject",new SipSessionBindingObject());
    //trigger the valueUnbound in SipSessionBindingObject
    sess.removeAttribute("testObject");

  }

}
