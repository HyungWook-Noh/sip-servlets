/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved. 
 *  
 * SipApplicationSessionBindingListenerServlet is used to test the APIs of
 * javax.servlet.sip.SipApplicationSessionBindingListener.
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionBindingEvent;
import javax.servlet.sip.SipApplicationSessionBindingListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;

@SipServlet(applicationName = "com.bea.sipservlet.tck.apps.apitestapp", 
    name = "SipApplicationSessionBindingListener")   

public class SipApplicationSessionBindingListenerServlet extends BaseServlet
    implements SipApplicationSessionBindingListener {

  private static final Logger logger = Logger
      .getLogger(SipApplicationSessionBindingListenerServlet.class);

  private static final long serialVersionUID = -1652672419697066862L;

  public boolean isValueBound = false;

  public boolean isValueUnbound = false;

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValueBound001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    SipApplicationSessionBindingListenerServlet listener = 
      new SipApplicationSessionBindingListenerServlet();
    // appSession.setAttribute("valueBound", listener) will trigger valueBound()
    // being invoked.
    appSession.setAttribute("testValueBound001", "testValueBound001");
    appSession.setAttribute("valueBound", listener);
    //JSR289 says:The valueBound method must be called before the object is made
    //available via the getAttribute method of the SipApplicationSession
    //interface.
    appSession.getAttribute("valueBound");
    //make sure the valueBound() is finished.
    synchronized(listener){
      if (listener.isValueBound) {
        return null;
      } else {
        logger.error("*** valueBound() is not called successfully! ***");
        return "valueBound() is not called successfully!";
      }
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValueUnbound001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    SipApplicationSessionBindingListenerServlet listener = 
      new SipApplicationSessionBindingListenerServlet();
    // appSession.removeAttribute("valueUnbound") will trigger valueUnbound()
    // being invoked.
    appSession.setAttribute("testValueUnbound001","testValueUnbound001");
    appSession.setAttribute("valueUnbound", listener);
    appSession.removeAttribute("valueUnbound");
    appSession.getAttribute("valueUnbound");
    synchronized(listener){
      if (listener.isValueUnbound) {
        return null;
      } else {
        logger.error("*** valueUnbound() is not called successfully!***");
        return "valueUnbound() is not called successfully!";
      }   
    }
  }
  

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetName001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    SipApplicationSessionBindingListenerServlet listener = 
      new SipApplicationSessionBindingListenerServlet();
    appSession.setAttribute("testGetName001", "testGetName001");
    appSession.setAttribute("testGetName", listener);
    appSession.getAttribute("testGetName");
    synchronized(listener){
      SipApplicationSessionBindingEvent event = (SipApplicationSessionBindingEvent) appSession
          .getAttribute("SipApplicationSessionBindingEvent");    
      if (event != null) {
        String name = event.getName();
        if ("testGetName".equals(name)) {
          return null;
        } else {
          logger.error("*** The name get from SipApplicationSessionBindingEvent"
              + " is not correct. ***");
          return "The name get from SipApplicationSessionBindingEvent "
              + "is not correct.";
        }
      } else{
        return "Failed to get name from SipApplicationSessionBindingEvent.";
      }
    }
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetApplicationSession001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    SipApplicationSessionBindingListenerServlet listener = 
      new SipApplicationSessionBindingListenerServlet();
    appSession.setAttribute("testGetApplicationSession001",
        "testGetApplicationSession001");
    appSession.setAttribute("testGetApplicationSession", listener);
    appSession.getAttribute("testGetApplicationSession");
    synchronized(listener){
      SipApplicationSessionBindingEvent event = (SipApplicationSessionBindingEvent) appSession
          .getAttribute("SipApplicationSessionBindingEvent");
      if (event != null) {
        SipApplicationSession session = event.getApplicationSession();
        if (appSession.getId().equals(session.getId())) {
          return null;
        } else {
          logger.error("*** The appSession get from the "
              + "SipApplicationSessionBindingEvent is not correct. ***");
          return "The appSession get from the SipApplicationSessionBindingEvent"
              + " is not correct.";
        }
      } else{
        return "Failed to get appSession from SipApplicationSessionBindingEvent.";
      }
    }
  }
  
  public synchronized void valueBound(SipApplicationSessionBindingEvent event) {
    if (event != null) {
      SipApplicationSession appSession = event.getApplicationSession();
      if (appSession != null) {
        String value = (String) appSession.getAttribute("testValueBound001");
        if ("testValueBound001".equals(value)) {
          isValueBound = true;
        }        
        String getName = (String)appSession.getAttribute("testGetName001");
        String getAppSession = (String)appSession.getAttribute("testGetApplicationSession001");
        if ("testGetName001".equals(getName)|| "testGetApplicationSession001".equals(getAppSession)){
          appSession.setAttribute("SipApplicationSessionBindingEvent", event);
        }
      }
    }
  }

  public synchronized void valueUnbound(SipApplicationSessionBindingEvent event) {
    if (event != null){
      SipApplicationSession appSession = event.getApplicationSession();
      if (appSession!=null){
        String value = (String)appSession.getAttribute("testValueUnbound001");
        if ("testValueUnbound001".equals(value)){
          isValueUnbound = true;    
        }
      }
    }
  }
}
