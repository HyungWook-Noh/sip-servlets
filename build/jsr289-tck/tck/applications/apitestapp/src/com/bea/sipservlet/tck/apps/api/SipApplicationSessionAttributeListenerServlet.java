/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.
 *  
 * SipApplicationSessionAttributeListenerServlet is used to test the APIs of
 * javax.servlet.sip.SipApplicationSessionAttributeListener.
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionAttributeListener;
import javax.servlet.sip.SipApplicationSessionBindingEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;

import java.io.IOException;

@SipServlet(
    name = "SipApplicationSessionAttributeListener")
@SipListener(applicationName = "com.bea.sipservlet.tck.apps.apitestapp")    

public class SipApplicationSessionAttributeListenerServlet extends BaseServlet
    implements SipApplicationSessionAttributeListener {

  private static Logger logger = Logger
      .getLogger(SipApplicationSessionAttributeListenerServlet.class);

  private static final long serialVersionUID = -1652672419697066862L;

  private static SipServletRequest tmpReq = null;

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testAttributeAdded001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSess = req.getApplicationSession();
    tmpReq = req;
    appSess.setAttribute("testAppSessValueAdded", "testAppSessValueAddedValue");
    logger.info("=== value1 has been set into app session ===");
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testAttributeReplaced001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    tmpReq = req;
    appSession.setAttribute("testAttributeReplaced001", "AA");
    appSession.setAttribute("testAttributeReplaced", "true");
    appSession.setAttribute("testAttributeReplaced001", "BB");
  } 

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testAttributeRemoved001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    appSession.setAttribute("testAttributeRemoved001", "AA");
    appSession.setAttribute("testRemoveAppSessionAttribute","true");
    tmpReq = req;
    appSession.removeAttribute("testAttributeRemoved001");
  }
  
  public void attributeAdded(SipApplicationSessionBindingEvent event) {
    logger.debug("=== in SipSessionAttributeListenerServlet" +
        ", SipSessionAttributeListener's attributeAdded is invoked!===");
    SipApplicationSession sess = event.getApplicationSession();

    String name = event.getName();
    logger.debug("=== SipApplicationSessionBindingEvent name=" + name + "===");

    String o = (String)sess.getAttribute("testAppSessValueAdded");
    logger.debug("=== got value of the specified key(testAppSessValueAdded)=" + o + "===");
    //ensure it is only invoked
    boolean nameCorrect = false;
    if("testAppSessValueAddedValue".equals(o)){
      if("testAppSessValueAdded".equals(name)){
        nameCorrect = true;
      }
      // clear the object first
      sess.removeAttribute("key1");

      SipServletResponse resp = null;
      if(nameCorrect){
        resp = tmpReq.createResponse(SipServletResponse.SC_OK);
      }else{
        resp = tmpReq.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
            "event.getName() returns " + name + "; expect 'testAppSessValueAdded'");
      }
      try{
        resp.send();
      }catch (IOException ex) {
        logger.error("*** IOException occurs during sending response ***",ex);
        throw new TckTestException(ex);
      }
    }
  }

  public void attributeReplaced(SipApplicationSessionBindingEvent event) {
    SipApplicationSession sess = event.getApplicationSession();
    logger.debug("=== SipApplicationSessionBindingEvent name=" + event.getName() + "===");
    String testReplaced = (String)sess.getAttribute("testAttributeReplaced");
    if("true".equals(testReplaced)){
      //clear the attribute
      sess.removeAttribute("testAttributeReplaced");
      String o = (String)sess.getAttribute("testAttributeReplaced001");

      SipServletResponse resp;
      if("BB".equals(o)){
        resp = tmpReq.createResponse(SipServletResponse.SC_OK);
      }else{
        logger.info("=== get value of testAttributeReplaced001 is:" + o + "===");
        resp = tmpReq.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
            "get value of testAttributeReplaced001 is:" + o);
      }
      try{
        resp.send();
      }catch (IOException ex) {
        logger.error("*** IOException occurs during sending response ***",ex);
        throw new TckTestException(ex);
      }
    }
  }

  public void attributeRemoved(SipApplicationSessionBindingEvent event) {
    SipApplicationSession sess = event.getApplicationSession();
    logger.debug("=== SipApplicationSessionBindingEvent name=" + event.getName() + "===");
    String testRemoved = (String)sess.getAttribute("testRemoveAppSessionAttribute");
    if("true".equals(testRemoved)){
      //clear the attribute
      sess.removeAttribute("testRemoveAppSessionAttribute");

      String o = (String)sess.getAttribute("testAttributeRemoved001");

      SipServletResponse resp;
      if(o == null){
        resp = tmpReq.createResponse(SipServletResponse.SC_OK);
      }else{
        logger.info("=== get value of testAttributeRemoved001 is:" + o + "===");
        resp = tmpReq.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
            "get value of testAttributeRemoved001 is:" + o);
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
