/**
 *(c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 * 
 * SipSessionListenerServlet is used to test the APIs of
 * javax.servlet.sip.SipSessionListener and javax.servlet.sip.SipSessionEvent
 */
package com.bea.sipservlet.tck.apps.api;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;
import org.apache.log4j.Logger;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.annotation.SipServlet;
import java.io.IOException;

@SipServlet(name="SipSessionListener")
public class SipSessionListenerServlet extends BaseServlet {
  private static Logger logger = Logger.getLogger(SipSessionListenerServlet.class);

  @TestStrategy(
      strategy = TESTSTRATEGY_SIMPLEASSERT
  )
  public String testSessionCreated001(SipServletRequest req)
      throws InterruptedException {
    serverEntryLog();
    SipSession s = req.getSession();
    //Sleeping is used to avoid the sesssionCreated() being called in another thread.
    // but but we don't know whether 2 seconds is a proper value. We can only say
    // 2 senconds might be a right value.
    Thread.sleep(2000);
    Object o = s.getAttribute("testSessionCreated");
    if("true".equals(o)){
      return null;
    }else{      
      logger.error("*** s.getAttribute()="
          + o + "; the expected is  'true'***");
      return "attribute got from session is " + o + "; but \"true\" is"
          + "expected";
    }
  }

  public void testSessionDestroyed001(SipServletRequest req){
    serverEntryLog();
    SipSession s = req.getSession();
    SipApplicationSession appSess = req.getApplicationSession();
    appSess.setAttribute("testSessionDestroyed","true");
    logger.info("=== session id=" + s.getId() + " ===");
    appSess.setAttribute("sessionId", s.getId());

    logger.info("=== original request is set into app session ===");
    appSess.setAttribute("orig_request", req);
    try {
      SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
      logger.info("=== 200 ok will be sent back ===");
      resp.send();
    } catch (IOException e) {
      logger.error("*** IOException occurs during sending 200 response, and the " +
          "method will return! ***",e);
    }
    logger.info("=== session will be invalidated ===");
    s.invalidate();
  }

  public void testSessionReadyToInvalidate001(SipServletRequest req){
    serverEntryLog();
    
    SipSession s = req.getSession();
    SipApplicationSession appSess = req.getApplicationSession();
    appSess.setAttribute("testSessionReadyToInvalidate","true");
    logger.info("=== session id=" + s.getId() + " ===");
    appSess.setAttribute("sessionId", s.getId());

    logger.info("=== original request is set into app session ===");
    appSess.setAttribute("orig_request", req);
    s.setInvalidateWhenReady(true);
    try {
      SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
      logger.info("=== 200 ok will be sent back ===");
      resp.send();
    } catch (IOException e) {
      logger.error("*** IOException occurs during sending 200 response, and the " +
          "method will return! ***",e);
      throw new TckTestException(e);
    }
  }

  public void doBye(SipServletRequest req){
    logger.debug("--- in doBye ---");
    try{
      req.createResponse(SipServletResponse.SC_OK).send();
      //here the session moves to readyForInvalidate
    }catch(IOException e){
      logger.error("*** can't send BYE for request:" + req, e);
      throw new TckTestException(e);
    }
  }
}
