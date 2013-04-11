/**
 *(c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * SipSessionAttributeListenerServlet is used to test the Constructor of
 * javax.servlet.sip.SipSessionBindingEvent. Other APIs of SipSessionBindingEvent
 * are tested in SipSessionAttributeListenerServlet
 */
package com.bea.sipservlet.tck.apps.api;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import org.apache.log4j.Logger;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.annotation.SipServlet;

@SipServlet(name="SipSessionBindingEvent")
public class SipSessionBindingEventServlet extends BaseServlet {
  private static Logger logger =
      Logger.getLogger(SipSessionBindingEventServlet.class);

  @TestStrategy(
      strategy = TESTSTRATEGY_SIMPLEASSERT
  )
  public String testSipSessionBindingEvent001(SipServletRequest req){
    serverEntryLog();
    SipSession s = req.getSession();
    s.setAttribute("key","value");
    SipSessionBindingEvent event
        = new SipSessionBindingEvent(s,"eventName");
    if("eventName".equals(event.getName())
        && "value".equals(s.getAttribute("key")) ){
      logger.info("=== everything is ok ===");
      return null;
    }else{
      logger.error("*** event=" + event + "; " +
          "event name=" + event.getName() + 
          "get(key)=" + s.getAttribute("key") + "***");
      return "event=" + event + "; " +
          "event name=" + event.getName() +
          "get(key)=" + s.getAttribute("key"); 
    }
  }
}
