
/**
 * @author Copyright (c) 2008 by BEA Systems, Inc. All Rights Reserved.
 * @version 1.0
 * @created 2008-4-9 15:59:45
 *
 *     UA          SipServer
        |               |
        |               |
        |(1): INVITE    |
        |-------------->|
        |((2): 500 ERR) |
        |<--------------|
        |(2): 200 OK    |
        |<--------------|
        |(3): ACK       |
        |-------------->|
        |(4): BYE       |
        |-------------->|
        |(5): 200 OK    |
        |<--------------|
        |               |
        |               |
 */
package com.bea.sipservlet.tck.apps.spec.ar.internalroutemodifier;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import java.io.IOException;

@javax.servlet.sip.annotation.SipServlet(
    name = "JSR289.TCK.AppRouter.InternalRouteModifierServlet",
    loadOnStartup = 1
)
public class MainServlet extends SipServlet {
  private static final Logger logger = Logger.getLogger(MainServlet.class);

  private SipSession          sessionUas    = null,
                              sessionUac    = null;
  private SipServletRequest   origRequest   = null;
  private SipServletResponse  succResponse  = null;

  protected void doInvite(SipServletRequest req)
      throws ServletException, IOException       {

    if (req.isInitial()) {
      logger.info("=== [TCK-AR-Internal-Route-Modifier] --INVITE--> ===");

      origRequest = req;
      sessionUas  = req.getSession();

      SipServletRequest newReq = req.getB2buaHelper().createRequest(req);
      newReq.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, req);

      newReq.setHeader("x-tck.jsr289.net-case5.PopedRoute", req.getPoppedRoute().toString());
      sessionUac = newReq.getSession();
      newReq.send();
      logger.info("=== [TCK-AR-Internal-Route-Modifier] --B2BUA INVITE--> ===");
    }
  }

  protected void doErrorResponse(SipServletResponse resp)
      throws ServletException, IOException  {

    if (resp.getSession().getId().equals(sessionUac.getId())) {
      logger.info("=== [TCK-AR-Internal-Route-Modifier] <--ERROR-- ===");
      if (origRequest != null)
        origRequest.createResponse(500, resp.getReasonPhrase()).send();
    }
  }

  protected void doSuccessResponse(SipServletResponse resp)
      throws ServletException, IOException  {

    if (resp.getSession().getId().equals(sessionUac.getId())) {
      logger.info("=== [TCK-AR-Internal-Route-Modifier] <--200-- ===");
      succResponse = resp;
      if (origRequest != null)
        origRequest.createResponse(200).send();
    }
  }

  protected void doAck(SipServletRequest req)
      throws ServletException, IOException  {

    if (req.getSession().getId().equals(sessionUas.getId())) {
      logger.info("=== [TCK-AR-Internal-Route-Modifier] --ACK--> ===");
      if (succResponse != null)
        succResponse.createAck().send();
    }
  }

  protected void doBye(SipServletRequest req)
      throws ServletException, IOException  {

    if (req.getSession().getId().equals(sessionUas.getId())) {
      logger.info("=== [TCK-AR-Internal-Route-Modifier] --BYE--> ===");
      origRequest = req;
      if (sessionUac != null)
        sessionUac.createRequest("BYE").send();
    }
  }
}
