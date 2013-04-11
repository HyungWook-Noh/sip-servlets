/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * The SessionLifetimeUacServlet class is used to test:
 *  1. The transitions of SipSession states
 *  2. The invalidate-when-ready mechanism for both SipSession and SipApplicationSession
 * as described in chapter 6 of JSR 289
*/
package com.bea.sipservlet.tck.apps.spec.uac;

import org.apache.log4j.Logger;

import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.IOException;


@javax.servlet.sip.annotation.SipServlet(name = "SessionLifetimeUac")
@SipListener

public class SessionLifetimeUacServlet extends SipServlet
    implements SipSessionListener, SipApplicationSessionListener {


  public SipFactory sipFactory;
  private static Logger logger = Logger.getLogger(SessionLifetimeUacServlet.class);

  // remote target address to which the MESSAGE will be sent
  private static Address remoteAddress = null;

  // local address that will be used as the From header in the MESSAGE request
  private static Address localAddress = null;

  // the address to which the INVITE will be sent to
  private Address uasAddress = null;

  public static final String SERVLET_NAME_ATTR = "TCK_SERVLET_NAME";
  private static final String HEADER_TCK_UA2_DEST = "TCK-UA2-Dest";

  /*
  * Initiate and get the SipFactory
  */
  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    sipFactory = (SipFactory) getServletContext().getAttribute(SipServlet.SIP_FACTORY);
    if (sipFactory == null) {
      throw new ServletException("No SipFactory in context");
    }
    logger.info("=== init SessionLifetimeB2buaServlet ===");
  }

  protected void checkState(SipSession session, SipSession.State state)
      throws ServletException {
    if (!session.getState().equals(state)) {
      String sessionStateError = "currently the state of sip session should be " + state + ", but it is " + session.getState();

      createAndSendBackMessage(sessionStateError);

      throw new ServletException(sessionStateError);
    }
  }


  // create and send back a MESSAGE in a new SipApplicationSession
  public void createAndSendBackMessage(String subjectStr) {
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    try {
      SipServletRequest msgReq =
          sipFactory.createRequest(appSession, "MESSAGE", localAddress, remoteAddress);
      msgReq.setHeader("Subject", subjectStr);
      msgReq.send();
      msgReq.getSession().setHandler(getServletName());
    } catch (Exception e) {
      logger.error("*** meet error when creating MESSAGE ***", e);
    }
  }


  // This is the message that initiates the call flow
  protected void doMessage(SipServletRequest req) throws ServletException, IOException {
    logger.info("=== receive MESSAGE ===");

    logger.info("=== send 200 for MESSAGE ===");

    // save the remote address for later use when sending the MESSAGE
    remoteAddress = req.getSession().getRemoteParty();
    localAddress = req.getSession().getLocalParty();

    // extract and save the adress of the UAS (ua2)
    String uasAddrString = req.getHeader(HEADER_TCK_UA2_DEST);

    if (uasAddrString != null) {
      uasAddress = sipFactory.createAddress(uasAddrString);
      req.createResponse(200).send();
      logger.info("send 200/MESSAGE ");

    } else {
      req.createResponse(500).send();
      logger.info("send 500/MESSAGE ");

    }

    logger.info("create and send INVITE in a new SipApplicationSession");

    // create and send the INVITE to UAS
    SipServletRequest invite = sipFactory.createRequest(
        sipFactory.createApplicationSession(),
        "INVITE",
        localAddress,
        uasAddress);

    invite.send();

    // sets the attribute on the relevant sipSession and sipAppSession
    SipSession inviteSession = invite.getSession();
    SipApplicationSession inviteAppSession = invite.getApplicationSession();

    inviteSession.setAttribute(SERVLET_NAME_ATTR, getServletName());
    inviteAppSession.setAttribute(SERVLET_NAME_ATTR, getServletName());

    invite.getSession().setHandler(getServletName());

    this.checkState(inviteSession, SipSession.State.INITIAL);

  }

  protected void doProvisionalResponse(SipServletResponse resp)
      throws ServletException, IOException {
    logger.info("=== receive 180 for INVITE ===");

    checkState(resp.getSession(), SipSession.State.EARLY);
  }

  protected void doSuccessResponse(SipServletResponse resp)
      throws ServletException, IOException {
    if (resp.getMethod().equals("INVITE")) {
      logger.info("receive 200 for INVITE");

      checkState(resp.getSession(), SipSession.State.CONFIRMED);

      logger.info("send ACK");
      resp.createAck().send();

      return;
    }

    if (resp.getMethod().equals("MESSAGE")) {
      logger.info("=== receive 200 for MESSAGE ===");
      return;
    }
  }

  protected void doBye(SipServletRequest req) throws ServletException, IOException {
    logger.info("=== received BYE ===");
    logger.info("=== sending 200/BYE ===");
    req.createResponse(200).send();
  }

  public void sessionCreated(SipSessionEvent event) {
  }

  public void sessionDestroyed(SipSessionEvent event) {
  }

  // When got notified that this sipSession is ready to be invalidated, let's
  // send back a MESSAGE to the UA1
  public void sessionReadyToInvalidate(SipSessionEvent event) {

    SipSession sipSession = event.getSession();
    String servletName = (String) sipSession.getAttribute(SERVLET_NAME_ATTR);
    if (!getServletName().equals(servletName)) {
      return;  // this is an irrelevant session
    }

    try {
      checkState(sipSession, SipSession.State.TERMINATED);
    } catch (ServletException e) {
      logger.error("***" + e.getMessage() + "***");
      return;
    }

    logger.info("=== sessionReadyToInvalidate for the SipSession get called back!"
        + " Sending back Message ===");

    createAndSendBackMessage("SUCCESS");

  }

  public void sessionCreated(SipApplicationSessionEvent event) {
  }

  public void sessionDestroyed(SipApplicationSessionEvent event) {
  }

  public void sessionExpired(SipApplicationSessionEvent event) {
  }

  // When got notified that this sipApplicationSession is ready to be invalidated, let's
  // send back a MESSAGE to UA1
  public void sessionReadyToInvalidate(SipApplicationSessionEvent event) {
    SipApplicationSession sipAppSession = event.getApplicationSession();
    String servletName = (String) sipAppSession.getAttribute(SERVLET_NAME_ATTR);
    if (!getServletName().equals(servletName)) {
      return;  // this is an irrelevant session
    }

    logger.info("=== sessionReadyToInvalidate for the SipApplicationSession get called back!"
        + " Sending back Message ===");

    createAndSendBackMessage("SUCCESS");
  }


}
