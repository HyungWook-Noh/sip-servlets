/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved. 
 *  
 * SipApplicationSessionEventServlet is used to test the APIs of
 * javax.servlet.sip.SipApplicationSessionEvent.
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;

@SipServlet 
( applicationName="com.bea.sipservlet.tck.apps.apitestapp",
  name="SipApplicationSessionEvent")
public class SipApplicationSessionEventServlet extends BaseServlet {
  private static final Logger logger = Logger
      .getLogger(SipApplicationSessionEventServlet.class);

  private static final long serialVersionUID = 1L;

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSipApplicationSessionEvent001(SipServletRequest req) {
		serverEntryLog();
		SipApplicationSession appSession = req.getApplicationSession();
		SipApplicationSessionEvent event = new SipApplicationSessionEvent(
				appSession);
		return null;
	}    
}
