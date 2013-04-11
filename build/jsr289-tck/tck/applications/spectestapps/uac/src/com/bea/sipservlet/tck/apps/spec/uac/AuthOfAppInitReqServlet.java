/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * AuthOfAppInitReqServlet is used to test
 * mechanism for authentication of application initiated request.
 *
 */

package com.bea.sipservlet.tck.apps.spec.uac;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import java.io.IOException;
import java.util.Iterator;

@javax.servlet.sip.annotation.SipServlet(name = "AuthOfAppInitReq")
public class AuthOfAppInitReqServlet extends BaseServlet{
 
  private static Logger logger = Logger.getLogger(AuthOfAppInitReqServlet.class);

  protected void doMessage(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    logger.info("=== AuthOfAppInitReq: receive MESSAGE ===");

    // send 200/MESSAGE
    req.createResponse(200).send();

    // create an REGISTER and send it
    URI reqUri = sipFactory.createURI(
        getUa2Uri(req, TestConstants.PRIVATE_URI));
    assert reqUri.isSipURI();

    SipURI toUri = (SipURI)reqUri.clone();
    toUri.setPort(-1);
    SipServletRequest reqRegister = sipFactory.createRequest(
        req.getApplicationSession(),
        "REGISTER",
        toUri,
        toUri);
    reqRegister.setRequestURI(reqUri);
    reqRegister.setExpires(3600);
    reqRegister.send();

  }

  protected void doErrorResponse(SipServletResponse resp)
      throws ServletException, IOException {
    logger.info("=== AuthOfAppInitReq: receive " + resp.getStatus() + " ===");

    if (resp.getStatus() == SipServletResponse.SC_UNAUTHORIZED) {

      SipServletRequest req = resp.getSession().createRequest("REGISTER");
      req.setRequestURI(sipFactory.createURI(
          getUa2Uri(resp, TestConstants.PRIVATE_URI)));
      AuthInfo authInfo = sipFactory.createAuthInfo();

      Iterator<String> realms = resp.getChallengeRealms();
      String userName = getUa2Uri(resp, TestConstants.USERNAME);
      String password = getUa2Uri(resp, TestConstants.PASSWORD);
      while (realms.hasNext()) {
        String realm = realms.next();
        logger.info("=== AuthOfAppInitReq: realm = " + realm + " ===");
        authInfo.addAuthInfo(resp.getStatus(), realm, userName,password);
      }
      req.addAuthHeader(resp, authInfo);
      req.send();
      logger.info("=== AuthOfAppInitReq: " +
          "send REGISTER after receiving 401 ===");
    } else {
      logger.error("*** AuthOfAppInitReq: receive an non-401 " +
          "response. The status is " + resp.getStatus() + " ***");
    }

  }

  @Override
  protected void doSuccessResponse(SipServletResponse resp)
    throws ServletException, IOException {
    logger.info("=== AuthOfAppInitReq: receive " + resp.getStatus() + " ===");
    if (resp.getSession().getAttribute("send3rdRegister") == null) {
      SipServletRequest req = resp.getSession().createRequest("REGISTER");
      req.setRequestURI(sipFactory.createURI(
          getUa2Uri(resp, TestConstants.PRIVATE_URI)));
      req.send();
      resp.getSession().setAttribute("send3rdRegister", "true");
    }
  }

  private String getUa2Uri(SipServletMessage req, String headerName){
    String header = req.getHeader(headerName);
    if(header == null){
      logger.error("*** AuthOfAppInitReq: Ua2 Uri is null ***");
      throw new TckTestException("Ua2 Uri is null");
    }
    return header;
  }

}
