/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved. 
 *  
 * SipApplicationSessionBindingEventServlet is used to test the APIs of 
 * javax.servlet.sip.SipApplicationSessionBindingEvent.
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionBindingEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;


@SipServlet(applicationName = "com.bea.sipservlet.tck.apps.apitestapp", 
    name = "SipApplicationSessionBindingEvent")
public class SipApplicationSessionBindingEventServlet extends BaseServlet{

  private static final Logger logger = Logger
      .getLogger(SipApplicationSessionBindingEventServlet.class);

  private static final long serialVersionUID = -1652672419697066862L;

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSipApplicationSessionBindingEvent001(SipServletRequest req) {
		serverEntryLog();
		SipApplicationSession appSession = req.getApplicationSession();
		SipApplicationSessionBindingEvent bindingEvent = 
			new SipApplicationSessionBindingEvent(
				appSession, "testSipApplicationSessionBindingEvent001");
		return null;
	}

}
