
/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * The SessionLifetimeXXXServlet class is used to test:
 *  1. The transitions of SipSession states
 *  2. The invalidate-when-ready mechanism for both SipSession and SipApplicationSession
 * as described in chapter 6 of JSR 289
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import org.apache.log4j.Logger;

import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.IOException;


/**
 * Declare the servlet name as well as register itself as a siplistener for
 * SipSession and SipApplicationSession event
 */
@javax.servlet.sip.annotation.SipServlet(name = "SessionLifetimeUas")
@SipListener

public class SessionLifetimeUasServlet extends SipServlet implements
    SipSessionListener, SipApplicationSessionListener {

  public SipFactory sipFactory;
  private static Logger logger = Logger.getLogger(SessionLifetimeUasServlet.class);

  // remote target address to which the MESSAGE will be sent
  private static Address remoteAddress = null;

  // local address that will be used as the From header in the MESSAGE request
  private static Address localAddress = null;
  
  public static final String SERVLET_NAME_ATTR = "TCK_SERVLET_NAME";
  
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
    logger.info("=== init SessionLifetimeUASServlet ===");
  }

  protected void checkState(SipSession session, SipSession.State state)
      throws ServletException{
     if(! session.getState().equals(state)){
       String sessionStateError = "currently the state of sip session should be "
           + state + ", but it is " + session.getState();

       createAndSendBackMessage(sessionStateError);

       throw new ServletException(sessionStateError);
     }
  }


// create and send back a MESSAGE in a new SipApplicationSession
  public void createAndSendBackMessage(String subjectStr){
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


  protected void doInvite(SipServletRequest req) throws ServletException, IOException {
    logger.info("=== receive INVITE ===");
    checkState(req.getSession(), SipSession.State.INITIAL);

    SipSession inviteSession = req.getSession();
    SipApplicationSession inviteAppSession = req.getApplicationSession();

    // sets the attribute on the relevant sipSession and sipAppSession
    inviteSession.setAttribute(SERVLET_NAME_ATTR, getServletName());
    inviteAppSession.setAttribute(SERVLET_NAME_ATTR, getServletName());

    // save the remote address for later sending the MESSAGE
    remoteAddress = req.getAddressHeader("Contact");
    localAddress = inviteSession.getLocalParty();

    logger.info("=== send 200 for INVITE ===");    
    req.createResponse(200).send();
    checkState(req.getSession(), SipSession.State.CONFIRMED);    

  }

  protected void doAck(SipServletRequest req) throws ServletException, IOException {
    logger.info("=== received ACK! ===");

    logger.info("=== sending BYE ===");
    req.getSession().createRequest("BYE").send();
  }
  

  protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		if (resp.getMethod().equals("INVITE")) {
			logger.info("=== receive 200 for INVITE ===");

			checkState(resp.getSession(), SipSession.State.CONFIRMED);

			logger.info("=== send ACK ===");
			resp.createAck().send();

			return;
		}

		if (resp.getMethod().equals("MESSAGE")) {
			logger.info("=== receive 200 for MESSAGE ===");
			return;
		}
	}

  public void sessionCreated(SipSessionEvent event) {
  }

  public void sessionDestroyed(SipSessionEvent event) {
  }

  // When got notified that this sipSession is ready to be invalidated, let's
  // send back a MESSAGE to the SipUNIT client
  public void sessionReadyToInvalidate(SipSessionEvent event) {

    SipSession sipSession = event.getSession();
    String servletName = (String)sipSession.getAttribute(SERVLET_NAME_ATTR);
    if (!getServletName().equals(servletName)) {
      return;  // this is an irrelevant session
    }

    logger.info("=== sessionReadyToInvalidate for the SipSession get called back!" +
        " Sending back Message ===");

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
    String servletName = (String)sipAppSession.getAttribute(SERVLET_NAME_ATTR);
    if (!getServletName().equals(servletName)) {
      return;  // this is an irrelevant session
    }

    logger.info("=== sessionReadyToInvalidate for the SipApplicationSession get called back!"
        +  " Sending back Message ===");

    createAndSendBackMessage("SUCCESS");
  }


}
