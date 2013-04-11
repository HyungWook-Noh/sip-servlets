/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * The SessionLifetimeProxyServlet class is used to test:
 *  1. The transitions of SipSession states
 *  2. The invalidate-when-ready mechanism for both SipSession and SipApplicationSession
 * as described in chapter 6 of JSR 289
*/
package com.bea.sipservlet.tck.apps.spec.proxy;

import org.apache.log4j.Logger;

import javax.servlet.sip.annotation.SipListener;
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
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.URI;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@javax.servlet.sip.annotation.SipServlet(name = "SessionLifetimeProxy")
@SipListener
public class SessionLifetimeProxyServlet extends SipServlet
    implements SipSessionListener, SipApplicationSessionListener {

  public SipFactory sipFactory;
  private static Logger logger = Logger.getLogger(SessionLifetimeProxyServlet.class);

  // remote target address to which the MESSAGE will be sent
  private static Address remoteAddress = null;

  // local address that will be used as the From header in the MESSAGE request
  private static Address localAddress = null;

  public static final String SERVLET_NAME_ATTR = "TCK_SERVLET_NAME";
  
  /**
   * header to store proxy dest URIs, 
   */
  private static final String HEADER_TCK_PROXY_DEST = "TCK-Proxy-Dest";
  

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

  /**
   * We'll proxy this request to UA2
   * @param req
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doInvite(SipServletRequest req)
      throws ServletException, IOException {
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

    
    // proxy the request to UA2    
    List dest = getProxyDest(req, sipFactory);

    if (dest.size() == 0) {
        req.createResponse(500, "No destinations").send();
    } else {
        logger.debug("---proxying to " + dest + " ---");
        Proxy proxy = req.getProxy();
        proxy.setRecordRoute(true);        
        proxy.proxyTo(dest);
    }
  }

  protected void doAck(SipServletRequest req) throws ServletException {
    logger.info("=== received ACK! ===");
    checkState(req.getSession(), SipSession.State.CONFIRMED);
  }

  protected void doBye(SipServletRequest req)
      throws ServletException, IOException {
    logger.info("=== received BYE! ===");

    // the relevant SipSession maybe automatically invalidated, therefore any attempt
    // after the send() may cause IllegalStateException.
  }

  protected void doProvisionalResponse(SipServletResponse resp)
      throws ServletException, IOException {
    logger.info("=== receive 180 for INVITE ===");

//    checkState(resp.getSession(), SipSession.State.EARLY);
  }

  protected void doSuccessResponse(SipServletResponse resp)
      throws ServletException, IOException {
    if (resp.getMethod().equals("INVITE")) {
      logger.info("=== receive 200 for INVITE ===");
      return;
    }

    if (resp.getMethod().equals("BYE")) {
      logger.info("=== receive 200 for BYE ===");
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
  // send back a MESSAGE to UA1
  public void sessionReadyToInvalidate(SipSessionEvent event) {

    SipSession sipSession = event.getSession();
    String servletName = (String)sipSession.getAttribute(SERVLET_NAME_ATTR);
    if (!getServletName().equals(servletName)) {
      return;  // this is an irrelevant session
    }

    try {
      checkState(sipSession, SipSession.State.TERMINATED);
    } catch (ServletException e) {
      logger.error("failed to check the state of SipSession", e);
    }

    logger.info("=== sessionReadyToInvalidate for the SipSession get called back!" +
        " Sending back Message");

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

    logger.info("=== sessionReadyToInvalidate for the SipApplicationSession "
        + "get called back! Sending back Message ===");

    createAndSendBackMessage("SUCCESS");
  }

  
  /**
   * Returns the value of the custom Proxy-Dest header as a List
   * of URIs. The returned List can be used as an argument to
   * Proxy.proxy().
   */
  static List getProxyDest(SipServletRequest req, SipFactory sf)
    throws ServletException {
    ArrayList l = new ArrayList();
    String value = req.getHeader(HEADER_TCK_PROXY_DEST);
    if (value != null) {
      StringTokenizer tok = new StringTokenizer(value);
      while (tok.hasMoreTokens()) {
        URI uri = sf.createURI(tok.nextToken());
        l.add(uri);
      }
    }
    return l;
  }


}
