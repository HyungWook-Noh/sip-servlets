/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * CommittedStateUnderB2buaServlet is used to test the committed state
 * of SipServletMessage under Proxy mode
 *
 */
package com.bea.sipservlet.tck.apps.spec.proxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;

@javax.servlet.sip.annotation.SipServlet(name = "CommittedStateUnderProxy")
public class CommittedStateUnderProxyServlet extends BaseServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger logger =
      Logger.getLogger(CommittedStateUnderProxyServlet.class);


  @Override
  protected void doInvite(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    log("receive " + req.getMethod() + " " + req.getRequestURI());
    req.getSession().setHandler(getServletName());

    checkUncommitted(req);

    Proxy proxy = req.getProxy();


    Address route = req.getAddressHeader("Route");

    proxy.setSupervised(true);
    proxy.setRecordRoute(true);
    proxy.proxyTo(route.getURI());

    checkUncommitted(req);
  }


  @Override
  protected void doAck(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    log("receive ACK");
  }

  @Override
  protected void doBye(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    log("receive bye");
  }

  @Override
  protected void doSuccessResponse(SipServletResponse resp)
      throws ServletException, IOException {
    log("receive response " + resp.getStatus() + " " + resp.getMethod());

    if ("INVITE".equals(resp.getMethod())) {
      log("check committted state of response for Invite");
      checkUncommitted(resp);
    } else if ("BYE".equals(resp.getMethod())) {
      log("check committed state of response for Bye");
      checkUncommitted(resp);
    }

  }


  @Override
  public void log(String message) {
    logger.debug("---Committed Servlet Proxy ##########" + message + "---");
  }

  private void checkUncommitted(SipServletMessage message)
      throws ServletException {
    if (message.isCommitted()) {
      throw new ServletException("The message should be uncommitted before forwarded");
    }
  }

}
