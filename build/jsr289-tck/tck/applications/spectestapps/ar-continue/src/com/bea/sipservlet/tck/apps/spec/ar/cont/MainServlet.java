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


package com.bea.sipservlet.tck.apps.spec.ar.cont;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.UAMode;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@javax.servlet.sip.annotation.SipServlet(
    name = "JSR289.TCK.AppRouter.ContinueServlet",
    loadOnStartup = 1
)
public class MainServlet extends SipServlet {

  private static final Logger logger = Logger.getLogger(MainServlet.class);

  private final Set<String>  sessionUasSet = new HashSet<String>();
  private final Set<String>  sessionUacSet = new HashSet<String>();

  private boolean isFromUas(SipServletRequest req)  {
    return sessionUasSet.contains(req.getSession().getId());
  }

  private boolean isFromUac(SipServletResponse resp)  {
    return sessionUacSet.contains(resp.getSession().getId());
  }

  protected void doInvite(SipServletRequest req)
      throws ServletException, IOException    {

    if (req.isInitial()) {
      logger.info("=== [TCK-AR-Continue-App] --INVITE--> ===");

      SipSession sessionUas = req.getSession();
      SipSession sessionUac;

      B2buaHelper b2buaHelper = req.getB2buaHelper();
      SipServletRequest newReq = b2buaHelper.createRequest(req, true, null);

      newReq.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, req);
      sessionUac = newReq.getSession();

      sessionUasSet.add(sessionUas.getId());
      sessionUacSet.add(sessionUac.getId());

      newReq.send();
      logger.info("=== [TCK-AR-Continue-App] --B2BUA-INVITE-->: ===");
    }
  }

  protected void doErrorResponse(SipServletResponse resp)
      throws ServletException, IOException  {

    if (isFromUac(resp)) {
      logger.info("=== [TCK-AR-Continue-App] <--ERROR-- ===");

      SipServletRequest origRequest =
          resp.getRequest().getB2buaHelper().getLinkedSipServletRequest(resp.getRequest());

      if (origRequest != null)
        origRequest.createResponse(500, resp.getReasonPhrase()).send();
    }
  }

  protected void doSuccessResponse(SipServletResponse resp)
      throws ServletException, IOException  {

    if (isFromUac(resp)) {
      logger.info("=== [TCK-AR-Continue-App] <--200-- ===");

      SipServletResponse resToSend = null;

      if (resp.getMethod().equalsIgnoreCase("INVITE"))
        resToSend = resp.getRequest().getB2buaHelper().createResponseToOriginalRequest(
            resp.getRequest().getB2buaHelper().getLinkedSession(resp.getSession()),
            resp.getStatus(),
            resp.getReasonPhrase());
      else if (resp.getMethod().equalsIgnoreCase("BYE"))
        resToSend = resp.getRequest().getB2buaHelper().getLinkedSipServletRequest(resp.getRequest()).createResponse(200);

      if (resToSend != null)
        resToSend.send();
    }
  }

  protected void doAck(SipServletRequest req)
      throws ServletException, IOException    {

    if (isFromUas(req)) {
      logger.info("=== [TCK-AR-Continue-App] --ACK--> ===");

      SipServletResponse succResponse =
          (SipServletResponse)req.getB2buaHelper().getPendingMessages(
              req.getB2buaHelper().getLinkedSession(req.getSession()),
              UAMode.UAC).get(0);

      if (succResponse != null)
        succResponse.createAck().send();
    }
  }

  protected void doBye(SipServletRequest req)
      throws ServletException, IOException  {
    
    logger.info("=== [TCK-AR-Continue-App] --BYE--> ===");
    
    if (isFromUas(req)) {
      req.getB2buaHelper().createRequest(
          req.getB2buaHelper().getLinkedSession(req.getSession()),
          req,
          null).send();
    }
  }
}
