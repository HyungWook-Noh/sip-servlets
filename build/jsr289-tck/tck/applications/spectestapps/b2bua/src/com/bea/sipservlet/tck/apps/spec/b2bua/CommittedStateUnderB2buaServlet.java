/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * CommittedStateUnderB2buaServlet is used to test the committed state
 * of SipServletMessage under B2BUA mode
 *
 */
package com.bea.sipservlet.tck.apps.spec.b2bua;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TckTestException;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.URI;
import javax.servlet.sip.annotation.SipListener;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

@javax.servlet.sip.annotation.SipServlet(name = "CommittedStateUnderB2bua")
@javax.servlet.sip.annotation.SipListener()
public class CommittedStateUnderB2buaServlet extends BaseServlet
    implements TimerListener {

  private static final long serialVersionUID = 1L;
  private static final Logger logger =
      Logger.getLogger(CommittedStateUnderB2buaServlet.class);
  private static final String SERVLET_NAME = "SERVLET_NAME";

  @Override
  protected void doMessage(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    log("receive message " + req.getRequestURI());

    log("check committed state of incomming MESSAGE");
    checkUncommitted(req);

    req.createResponse(200).send();

    checkCommitted(req);
    modifyCommittedMessage(req);

    log("create app session for invite");
    SipApplicationSession appSession = sipFactory.createApplicationSession();

    log("start timer to send invite");
    startTimer(appSession, req.getHeader("Call-Info"));
  }

  @Override
  protected void doProvisionalResponse(SipServletResponse resp)
      throws ServletException, IOException {
    serverEntryLog();
    log("receive 180 response");
    checkCommitted(resp);
  }

  @Override
  protected void doSuccessResponse(SipServletResponse resp)
      throws ServletException, IOException {
    serverEntryLog();
    log("recive 200 OK for INVITE");

    checkUncommitted(resp);

    log("create and send ACK ");
    resp.createAck().send();

    checkCommitted(resp);

  }

  @Override
  protected void doBye(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    log("receive bye");

    checkUncommitted(req);

    req.createResponse(200).send();

    checkCommitted(req);
  }

  @Override
  public void log(String message) {
    logger.debug("---Committed Servlet B2BUA ##########" + message + "---");
  }

  private void checkCommitted(SipServletMessage message)
      throws ServletException {
    if (!message.isCommitted()) {
      throw new ServletException("Here the message should be committed");
    }
  }

  private void checkUncommitted(SipServletMessage message)
      throws ServletException {
    if (message.isCommitted()) {
      throw new ServletException("Here the message should not be committed");
    }
  }

  private void modifyCommittedMessage(SipServletMessage message)
      throws ServletException {
    try {
      log("check addHeader() method ");
      message.addHeader("TCK", "value");
      throw new ServletException("should not permit to add header"
          +  " to a committed message");
    } catch (IllegalStateException e) { }

    try {
      log("check setHeader() method ");
      message.setHeader("TCK", "value");
      throw new ServletException("should not permit to set header "
          + "to a committed message");
    } catch (IllegalStateException e) { }

    try {
      log("check setHeader() method ");
      message.removeHeader("TCK");
      throw new ServletException("should not permit to remove header"
          + " to a committed message");
    } catch (IllegalStateException e) { }

    Address header = message.getAddressHeader("TCK");
    try {
      log("modify display name ");
      header.setDisplayName("tck");
      throw new ServletException("should not permit to modify the "
          + "display name in an address located in a committed message");
    } catch (IllegalStateException e) { }

    try {
      log("modify uri ");
      URI tmpURI = message.getFrom().getURI();
      header.setURI(tmpURI);
      throw new ServletException("should not permit to modify uri in "
          + "an address located in a committed message");
    } catch (IllegalStateException e) { }
  }


  private void startTimer(SipApplicationSession appSession, String info) {
    TimerService timerService =
        (TimerService) getServletContext().getAttribute(TIMER_SERVICE);
    appSession.setAttribute(SERVLET_NAME, getServletName());
    timerService.createTimer(appSession, 500, false, info);
  }

  public void timeout(ServletTimer timer) {
    //check if the timeout is for my servlet or not
    SipApplicationSession appSession = timer.getApplicationSession();
    if(!getServletName().equals(appSession.getAttribute(SERVLET_NAME))){
      return;
    }

    try {
      SipServletRequest invite = sipFactory.createRequest(
          appSession,
          "INVITE",
          "sip:tck@domain.com",
          (String) timer.getInfo());

      log("check the committed state of outgoing INVITE");
      checkUncommitted(invite);

      invite.send();

      log("check the committed state of INVITE which has been sent");
      checkCommitted(invite);

      invite.getSession().setHandler(getServletName());

    } catch (Exception e) {
      throw new TckTestException(e);
    }
  }

}
