/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.  
 *  
 * SipApplicationSessionListenerServlet is used to test the APIs of 
 * javax.servlet.sip.SipApplicationSessionListener.
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;

@SipServlet(applicationName = "com.bea.sipservlet.tck.apps.apitestapp", 
    name = "SipApplicationSessionListener")
@SipListener(applicationName = "com.bea.sipservlet.tck.apps.apitestapp")
public class SipApplicationSessionListenerServlet extends BaseServlet implements
    SipApplicationSessionListener {

  private static final Logger logger = Logger
      .getLogger(SipApplicationSessionListenerServlet.class);

  private static final long serialVersionUID = -1652672419697066862L;

  private static SipServletRequest tempRequest = null;
  private static String destroyId = null;
  private static String expireId = null;
  private static String readyId = null;
  private static String tempAppSessionId = null;

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSessionCreated001(SipServletRequest req)
      throws InterruptedException {
    serverEntryLog();
    SipApplicationSession newAppSession = sipFactory.createApplicationSession();
    if (newAppSession != null) {
      String id = (String) newAppSession.getAttribute("appSessionID");
      logger.debug("appSessionId=" + id);
      //Sleeping is used to avoid the sesssionCreated() being called in another thread.
    // but but we don't know whether 2 seconds is a proper value. We can only say
    // 2 senconds might be a right value.
      Thread.sleep(2000);
      if (newAppSession.getId().equals(id)) {
        if (newAppSession.isValid()) {
          newAppSession.invalidate();
        }
        return null;
      } else {
        logger.error("*** sessionCreated() is not called. ***");
        return "SipApplicationSessionListener.sessionCreated() is not called.";
      }
    } else {
      return "Fail to create an appSession by "
          + "sipFactory.createApplicationSession().";
    }
  }
  
  public void sessionCreated(SipApplicationSessionEvent event) {
    if (event != null) {
      SipApplicationSession appSession = event.getApplicationSession();
      if (appSession != null) {        
        appSession.setAttribute("appSessionID",appSession.getId());        
      } else {
        logger.error("*** appSession get from SipApplicationSessionEvent"
            + " is null. ***");
      }
    }
  } 
  

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSessionDestroyed001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession newAppSession = sipFactory.createApplicationSession();
    if (newAppSession != null) {
      newAppSession.setAttribute("testSessionDestroyed001",
          "testSessionDestroyed001");
      tempAppSessionId = newAppSession.getId();

      if (newAppSession.isValid()){
        destroyId = newAppSession.getId();
        tempRequest = req;
        newAppSession.invalidate();
        return null;
      }
      return "The newAppSession is already invalidated.";
    } else {
      return "Fail to create an appSession by "
          + "sipFactory.createApplicationSession().";
    }
  }
  
  public void sessionDestroyed(SipApplicationSessionEvent event) {
    if (event != null && destroyId != null
        && event.getApplicationSession().getId().equals(destroyId)) {

      if(tempRequest == null){
        logger.error("*** original request is null, so we can't send " +
            "the message back!!!***");
        throw new TckTestException("original request is null! Can't send the" +
            " message back.");
      }
      SipApplicationSession appSession = event.getApplicationSession();
      if (appSession != null) {
        String appSessionId = appSession.getId();
        // Make sure the appSession is exactly the one be destroyed.
        if (tempAppSessionId != null && appSessionId != null) {
          if (tempAppSessionId.equals(appSessionId)) {
            try {
              String to = tempRequest.getFrom().toString();
              String from = tempRequest.getTo().toString();
              SipServletRequest messageReq = sipFactory.createRequest(
                  sipFactory.createApplicationSession(), "MESSAGE", from, to);
              messageReq.send();
              tempRequest = null;
            } catch (IOException e) {
              logger.error("*** Throw IOException during sessionExpired. ***", e);
              throw new TckTestException(e);
            } catch (ServletParseException e) {
              logger.error(
                  "*** Throw ServletParseException during sessionExpired. ***", e);
              throw new TckTestException(e);
            }
          }
        }
      }
    }
  }  
  
  public void testSessionExpired001(SipServletRequest req){
    serverEntryLog();
    tempRequest = req;
    SipApplicationSession newAppSession = sipFactory.createApplicationSession();
    if(newAppSession!=null){
      newAppSession.setAttribute("testSessionExpired001", "testSessionExpired001");
      try {
        newAppSession.setExpires(1);
        expireId = newAppSession.getId();
        req.createResponse(SipServletResponse.SC_OK).send();
      } catch (IOException e) {
        logger.error("*** Thrown IOException during send().***",e);
        throw new TckTestException(e);
      } catch (IllegalStateException e) {
        logger.error("*** Thrown IllegalStateException during send().***",e);
        throw new TckTestException(e);
      }
    }
  }

  public void sessionExpired(SipApplicationSessionEvent event) {
    if (event != null && expireId != null
        && event.getApplicationSession().getId().equals(expireId)) {

      if(tempRequest == null){
        logger.error("*** original request is null, so we can't send " +
            "the message back!!!***");
        throw new TckTestException("original request is null! Can't send the" +
            " message back.");
      }
      SipApplicationSession appSession = event.getApplicationSession();
      String value = (String) appSession.getAttribute("testSessionExpired001");
     
      // Make sure the appSession is the tested expired appSession.
      if ("testSessionExpired001".equals(value)) {
        logger.info("=== sessionExpired() get invoked. ===");
        try {
          String to = tempRequest.getFrom().toString();
          String from = tempRequest.getTo().toString();
          SipServletRequest messageReq = sipFactory.createRequest(
          		appSession, "MESSAGE", from, to);
          messageReq.send();
        } catch (IOException e) {
          logger.error("*** Throw IOException during sessionExpired. ***", e);
          throw new TckTestException(e);
        } catch (ServletParseException e) {
          logger.error(
              "*** Throw ServletParseException during sessionExpired. ***", e);
          throw new TckTestException(e);
        }
      }
    }
  }
  
  
  public void testSessionReadyToInvalidate001(SipServletRequest req) {
    serverEntryLog();

    SipApplicationSession appSession = req.getApplicationSession();
    appSession.setAttribute("theRequest", req);
    appSession.setAttribute("testSessionReadyToInvalidate001",
        "testSessionReadyToInvalidate001");
    try {
      req.createResponse(SipServletResponse.SC_OK).send();
    } catch (IOException e) {
      throw new TckTestException(e);
    }

  }  
  
  protected void doBye(SipServletRequest req){
    try {
      readyId = req.getApplicationSession().getId();
      req.createResponse(SipServletResponse.SC_OK).send();

    } catch (IOException e) {
      throw new TckTestException(e);
    }  
  }
  
  public void sessionReadyToInvalidate(SipApplicationSessionEvent event) {    
    if (event != null && readyId != null
        && event.getApplicationSession().getId().equals(readyId)) {
      
      SipApplicationSession appSession = event.getApplicationSession();
      if ("testSessionReadyToInvalidate001".equals(appSession
          .getAttribute("testSessionReadyToInvalidate001"))) {        
        SipServletRequest req = (SipServletRequest) appSession
            .getAttribute("theRequest");        
        SipApplicationSession newAppSession = sipFactory
            .createApplicationSession();
        try {          
          sipFactory.createRequest(newAppSession, "MESSAGE", req.getTo().toString(),              
              req.getHeader("Contact")).send();
        } catch (IOException e) {
          throw new TckTestException(e);
        } catch (ServletParseException e){
          throw new TckTestException(e);
        }
      }
    }
  }  
 
}
