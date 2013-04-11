/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipSessionAttributeListenerServlet is used to test the APIs of
 * javax.servlet.sip.SipSessionAttributeListener and
 * javax.servlet.sip.SipSessionBindingEvent.
 */
package com.bea.sipservlet.tck.apps.api;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TckTestException;
import org.apache.log4j.Logger;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.annotation.SipServlet;
import java.io.IOException;

@SipServlet(name = "SipSessionAttributeListener")
@SipListener
public class SipSessionAttributeListenerServlet
    extends BaseServlet implements SipSessionAttributeListener {
  private static Logger logger =
      Logger.getLogger(SipSessionAttributeListenerServlet.class);
  private static String ATTR_REQ = "request";


  public void testAttributeAdded001(SipServletRequest req){
    serverEntryLog();
    SipSession sess = req.getSession();
    SipApplicationSession appSess = req.getApplicationSession();
    appSess.setAttribute(ATTR_REQ, req);
    logger.info("=== request has been set into app session ===");
    sess.setAttribute("key1", "value1");
    logger.info("=== value1 has been set into sip session ===");
  }


  public void testAttributeRemoved001(SipServletRequest req){
    serverEntryLog();
    SipSession sess = req.getSession();
    SipApplicationSession appSess = req.getApplicationSession();
    appSess.setAttribute(ATTR_REQ, req);
    logger.info("=== request has been set into app session ===");
    sess.setAttribute("key2", "value2");
    logger.info("=== value2 has been set into sip session ===");
    sess.setAttribute("testRemoved", "true");
    sess.removeAttribute("key2");

    logger.info("=== value2 has been removed from sip session ===");
  }


  public void testAttributeReplaced001(SipServletRequest req){
    serverEntryLog();
    SipSession sess = req.getSession();
    SipApplicationSession appSess = req.getApplicationSession();
    appSess.setAttribute(ATTR_REQ, req);
    logger.debug("=== request has been set into app session ===");
    sess.setAttribute("key3", "value3");
    logger.debug("=== value3 has been set into sip session ===");
    sess.setAttribute("testReplaced", "true");
    sess.setAttribute("key3","_value3");

    logger.info("=== value3 has been replaced by _value3 in sip session ===");
  }

 
  public void attributeAdded(SipSessionBindingEvent e){
    logger.debug("=== in SipSessionAttributeListenerServlet" +
        ", SipSessionAttributeListener's attributeAdded is invoked!===");
    SipSession sess = e.getSession();

    String name = e.getName();
    logger.debug("=== SipSessionBingdingEvent name=" + name + "===");

    String o = (String)sess.getAttribute("key1");
    logger.debug("=== got value of the specified key(key1)=" + o + "===");
    //ensure it is only invoked
    boolean nameCorrect = false;
    if("value1".equals(o)){
      if("key1".equals(name)){
        nameCorrect = true;
      }
      // clear the object first
      sess.removeAttribute("key1");
      SipApplicationSession appSess = sess.getApplicationSession();
      SipServletRequest req = (SipServletRequest)appSess.getAttribute(ATTR_REQ);
      SipServletResponse resp = null;
      if(nameCorrect){
        resp = req.createResponse(SipServletResponse.SC_OK);
      }else{
        resp = req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
            "event.getName() returns " + name + "; expect 'key1'");
      }
      try{
        resp.send();
      }catch (IOException ex) {
        logger.error("*** IOException occurs during sending response ***",ex);
        throw new TckTestException(ex);
      }
    }
  }

  public void attributeRemoved(SipSessionBindingEvent e){
    logger.debug("===in SipSessionAttributeListenerServlet===" +
       ", SipSessionAttributeListener's attributeRemoved is invoked!===");
    SipSession sess = e.getSession();
    logger.debug("=== SipSessionBingdingEvent name=" + e.getName() + "===");
    String testRemoved = (String)sess.getAttribute("testRemoved");
    if("true".equals(testRemoved)){
      //clear the attribute
      sess.removeAttribute("testRemoved");
      SipApplicationSession appSess = sess.getApplicationSession();
      String o = (String)sess.getAttribute("key2");
      SipServletRequest req = (SipServletRequest)appSess.getAttribute(ATTR_REQ);
      SipServletResponse resp;
      if(o == null){
        resp = req.createResponse(SipServletResponse.SC_OK);
      }else{
        logger.info("=== get value of key2 is:" + o + "===");
        resp = req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
            "get value of key2 is:" + o);
      }
      try{
        resp.send();
      }catch (IOException ex) {
        logger.error("*** IOException occurs during sending response ***",ex);
        throw new TckTestException(ex);
      }
    }

  }

  public void attributeReplaced(SipSessionBindingEvent e){
    logger.debug("===in SipSessionAttributeListenerServlet===" +
       ", SipSessionAttributeListener's attributeReplaced is invoked!===");
    SipSession sess = e.getSession();
    logger.debug("=== SipSessionBingdingEvent name=" + e.getName() + "===");
    String testReplaced = (String)sess.getAttribute("testReplaced");
    if("true".equals(testReplaced)){
      //clear the attribute
      sess.removeAttribute("testReplaced");
      SipApplicationSession appSess = sess.getApplicationSession();
      String o = (String)sess.getAttribute("key3");
      SipServletRequest req = (SipServletRequest)appSess.getAttribute(ATTR_REQ);
      SipServletResponse resp;
      if("_value3".equals(o)){
        resp = req.createResponse(SipServletResponse.SC_OK);
      }else{
        logger.info("=== get value of key3 is:" + o + "===");
        resp = req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
            "get value of key3 is:" + o);
      }
      try{
        resp.send();
      }catch (IOException ex) {
        logger.error("*** IOException occurs during sending response ***",ex);
        throw new TckTestException(ex);
      }
    }
  }
}
