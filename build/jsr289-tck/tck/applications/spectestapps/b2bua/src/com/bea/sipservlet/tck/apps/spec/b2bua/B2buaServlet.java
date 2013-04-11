/*
 * $Id: B2buaServlet.java,v 1.7 2003/02/05 00:32:25 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 */
/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * B2buaServlet is used to test
 * mechanism for B2bua without B2buaHelper
 *
 */

package com.bea.sipservlet.tck.apps.spec.b2bua;

import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.apps.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;
import java.io.IOException;

import org.apache.log4j.Logger;

@javax.servlet.sip.annotation.SipServlet(name = "B2bua")
public class B2buaServlet extends BaseServlet {
  private static final Logger logger = Logger.getLogger(B2buaServlet.class);

  protected void doInvite(SipServletRequest req1)
      throws ServletException, IOException {
    serverEntryLog();
    logger.info("=== B2bua: receive INVITE ===");

    if (!req1.isInitial()) {
      throw new TckTestException("unexpectedly got re-INVITE");
    }

    String destStr = req1.getHeader(TestConstants.PRIVATE_URI);
    if (destStr == null) {
      req1.createResponse(500, "No destinations").send();
      return;
    }
    URI dest = sipFactory.createURI(destStr);
    SipServletRequest req2 = sipFactory.createRequest(req1.getApplicationSession(),
                                            "INVITE",
                                            req1.getFrom(),
                                            req1.getTo());
    req2.setRequestURI(dest);

    // associate the two dialogs through the "peer" attribute
    SipSession dialog1 = req1.getSession();
    SipSession dialog2 = req2.getSession();
    dialog1.setAttribute("peer", dialog2);
    dialog2.setAttribute("peer", dialog1);

    // associate the two requests through the "peer.req" attribute
    req2.setAttribute("peer.req", req1);
            
    // so we can relay the response:
    req2.send();
  }

  protected void doAck(SipServletRequest ack)
      throws ServletException, IOException {
    logger.info("=== B2bua: receive ACK ===");
    if (ack.isInitial()) {
      throw new TckTestException("Got unexpected initial ACK");
    }

    SipSession dialog1 = ack.getSession();
    SipSession dialog2 = getPeerSipSession(dialog1);

    SipServletResponse resp2 = (SipServletResponse) dialog2.getAttribute("response");
    dialog2.removeAttribute("response");
    if (resp2 == null) {
      throw new TckTestException("no peer response; cannot forward ACK");
    }
    resp2.createAck().send();
  }

  protected void doBye(SipServletRequest bye1)
      throws ServletException, IOException {
    if (bye1.isInitial()) {
      throw new TckTestException("got initial BYE");
    }

    SipSession dialog1 = bye1.getSession();
    SipSession dialog2 = getPeerSipSession(dialog1);

    SipServletRequest bye2 = dialog2.createRequest("BYE");
    bye2.setAttribute("peer.req", bye1);
    bye2.send();
  }

  protected void doResponse(SipServletResponse resp2) throws IOException {
    logger.info("=== B2bua: receive Response: " + resp2.getStatus() + "/"
        + resp2.getMethod() + " ===");

    SipServletRequest req1 = (SipServletRequest) resp2.getRequest()
        .getAttribute("peer.req");

    if (req1 == null) {
      logger.info("=== B2bua: Failed to forward response:" + resp2 + " ===");
      throw new TckTestException("Failed to forward response");
    } else {
      int sc = resp2.getStatus();
      SipServletResponse resp1 = req1.createResponse(sc);
      resp1.setStatus(sc, resp2.getReasonPhrase() + " (relayed)");
      if (req1.getMethod().equals("INVITE")) {
        // store resp2 so we can forward ACK for resp1 later
        resp2.getSession().setAttribute("response", resp2);
      }
      resp1.send();
    }
  }

  private SipSession getPeerSipSession(SipSession oriSipSession){
    SipSession dialog2 = (SipSession) oriSipSession.getAttribute("peer");

    if (dialog2 == null) {
      throw new TckTestException("no peer SipSession");
    }
    
    return dialog2;
  }

}
