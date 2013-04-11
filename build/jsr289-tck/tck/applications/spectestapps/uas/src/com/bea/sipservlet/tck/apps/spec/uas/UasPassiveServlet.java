/*
 * $Id: UasPassive.java,v 1.2 2002/09/03 15:22:54 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 */
/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * UasPassiveServlet is used to test the specification of  UAS
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;

/**
 * 
 * Simple UAS servlet. The successful message flow is:
 * 
 * 
 * 
 * <ul>
 * 
 * <li>receive initial INVITE
 * 
 * <li>send 200
 * 
 * <li>receive ACK
 * 
 * <li>receive BYE
 * 
 * <li>send 200
 * 
 * </ul>
 * 
 * 
 * 
 * <p>
 * This class tests that SipServletRequest.isInitial has correct value
 * 
 * (true for initial INVITE, false elsewhere) and uses a SipSession to
 * 
 * store state for the purpose of making sure the above flow is followed.
 * 
 * Also checks getContacts for the INVITE.
 * 
 */
@javax.servlet.sip.annotation.SipServlet(name = "UasPassive")
public class UasPassiveServlet extends BaseServlet {
  private static final String STATE = "STATE";

  private static final String ACCEPTED = "ACCEPTED";

  private static final String ESTABLISHED = "ESTABLISHED";

  private static final String ERROR = "ERROR";

  private static Logger logger = Logger.getLogger(UasPassiveServlet.class);

  protected void doInvite(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    logger.debug("---doInvite---");
    if (!req.isInitial()) {
      // we don't handle re-INVITEs here
      throw new ServletException("isInitial() false for INVITE");
    }
    Iterator contacts = req.getAddressHeaders("Contact");
    if (!contacts.hasNext()) {
      throw new ServletException("Expected a Contact in INVITE");
    }
    Address c = (Address) contacts.next();
    if (contacts.hasNext()) {
      throw new ServletException("INVITE has multiple Contacts");
    }
    SipSession s = req.getSession();
    s.setAttribute(STATE, ACCEPTED);
    SipServletResponse resp = req.createResponse(200, "OK");
    resp.send();
  }

  protected void doAck(SipServletRequest req) throws ServletException {
    serverEntryLog();
    logger.debug("---doAck---");
    SipSession s = req.getSession();
    Object state = s.getAttribute(STATE);
    if (state.equals(ACCEPTED)) {
      s.setAttribute(STATE, ESTABLISHED);
    } else if (!state.equals(ESTABLISHED)) {
      s.setAttribute(STATE, ERROR);
      throw new ServletException("got ACK in unexpected state: " +
          s.getAttribute(STATE));
    }
  }

  protected void doBye(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    logger.debug("---doBye---");
    SipServletResponse resp;
    if (req.isInitial()) {
      throw new ServletException("isInitial() true for BYE");
    }
    SipSession s = req.getSession();
    Object state = s.getAttribute(STATE);
    if (!state.equals(ESTABLISHED)) {
      s.setAttribute(STATE, ERROR);
      throw new ServletException("got BYE in state " + state);
    }
    s.removeAttribute(STATE);
    resp = req.createResponse(200, "OK");
    resp.send();
  }

  public String getServletInfo() {
    return "Passive UAS - accepts INVITE and BYE";
  }
}
