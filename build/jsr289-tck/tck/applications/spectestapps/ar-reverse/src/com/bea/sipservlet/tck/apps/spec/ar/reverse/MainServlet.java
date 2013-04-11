package com.bea.sipservlet.tck.apps.spec.ar.reverse;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import java.io.IOException;

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
@javax.servlet.sip.annotation.SipServlet(
    name = "JSR289.TCK.AppRouter.ReverseServlet",
    loadOnStartup = 1
)
public class MainServlet extends SipServlet {
  private static final Logger logger = Logger.getLogger(MainServlet.class);

  private SipSession          sessionUas = null;
  private SipSession          sessionUac = null;
  // mihirk - servlets are stateless and not thread-safe. you cannot store the
  // origRequest as a class variable as it may be over-written by another req.
  // you should store it in the session instead. As you are using a b2buaHelper
  // in doInvite, you could make use of b2buaHelper API instead.

  // grant - mihir, you are quite right; while according test case design only one request will
  // be passed to this servlet. balanced with the effort changing it to b2buahelper may we
  // consider keep the current code.

  private SipServletRequest   origRequest   = null;
  private SipServletResponse  succResponse  = null;

  protected void doInvite(SipServletRequest req)
      throws ServletException, IOException  {

    if (req.isInitial()) {
      logger.info("=== [TCK-AR-Reverse] --INVITE--> ===");

      origRequest = req;
      sessionUas  = req.getSession();

      SipServletRequest newReq = req.getB2buaHelper().createRequest(req);
      newReq.setRoutingDirective(SipApplicationRoutingDirective.REVERSE, req);

      sessionUac = newReq.getSession();
      newReq.send();
      logger.info("=== [TCK-AR-Reverse] --B2BUA INVITE--> ===");
    }
  }

  protected void doErrorResponse(SipServletResponse resp)
      throws ServletException, IOException  {

    if (resp.getSession().getId().equals(sessionUac.getId())) {
      logger.info("=== [TCK-AR-Reverse] <--ERROR-- ===");

      if (origRequest != null)
        origRequest.createResponse(500, resp.getReasonPhrase()).send();
    }
  }

  protected void doSuccessResponse(SipServletResponse resp)
      throws ServletException, IOException  {

    if (resp.getSession().getId().equals(sessionUac.getId())) {
      logger.info("=== [TCK-AR-Reverse] <--200-- ===");
      succResponse = resp;
      if (origRequest != null)
        origRequest.createResponse(200).send();
    }
  }

  protected void doAck(SipServletRequest req)
      throws ServletException, IOException  {

    if (req.getSession().getId().equals(sessionUas.getId())) {
      logger.info("=== [TCK-AR-Reverse] --ACK--> ===");
      if (succResponse != null)
        succResponse.createAck().send();
    }
  }

  protected void doBye(SipServletRequest req)
      throws ServletException, IOException   {

    if (req.getSession().getId().equals(sessionUas.getId())) {
      logger.info("=== [TCK-AR-Reverse] --BYE--> ===");

      origRequest = req;
      if (sessionUac != null)
        sessionUac.createRequest("BYE").send();
    }
  }
}
