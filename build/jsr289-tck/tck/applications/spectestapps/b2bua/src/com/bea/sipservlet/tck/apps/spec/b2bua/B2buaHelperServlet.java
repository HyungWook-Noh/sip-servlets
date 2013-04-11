/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * B2buaHelperTest is used to test the B2buaHelper functionality 
 * introduced in JSR289.
 *
 */

package com.bea.sipservlet.tck.apps.spec.b2bua;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;

import javax.servlet.ServletException;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.UAMode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

@javax.servlet.sip.annotation.SipServlet(name = "B2buaHelper")
public class B2buaHelperServlet extends BaseServlet{
 
  private static Logger logger = Logger.getLogger(B2buaHelperServlet.class);

  protected void doInvite(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    logger.info("=== B2buaHelper: receive INVITE ===");

    Map<String, List<String>> headerMap =
            new HashMap<String, List<String>>();

    List<String> fromvalues = new ArrayList<String>(1);
    fromvalues.add( req.getTo().toString());
    headerMap.put("From", fromvalues);

    List<String> tovalues = new ArrayList<String>(1);
    String Ua2Uri = getUa2Uri(req);
    tovalues.add(Ua2Uri);
    headerMap.put("To", tovalues);

    B2buaHelper b2bhelper = req.getB2buaHelper();
    SipServletRequest newReq = b2bhelper.createRequest(req, false, headerMap);
    newReq.setRequestURI(sipFactory.createURI(Ua2Uri));

    SipApplicationSession appSession = req.getApplicationSession();
    appSession.setAttribute("uasSession", req.getSession());
    appSession.setAttribute("uacSession", newReq.getSession());

    newReq.send();
    logger.info("=== B2buaHelper: [doInvite] send INVITE " + newReq + " ===");
  }

  @Override
  protected void doSuccessResponse(SipServletResponse resp)
      throws ServletException, IOException {
    SipApplicationSession appSession = resp.getApplicationSession();
    B2buaHelper b2bhelper = resp.getRequest().getB2buaHelper();
    String method = resp.getMethod();
    if(method.equals("INVITE")){
      logger.info("=== B2buaHelper: recive 200 OK for INVITE ===");

      SipSession uacSession = resp.getSession();
      SipSession uasSession = (SipSession) appSession.getAttribute("uasSession");
      b2bhelper.linkSipSessions(uacSession, uasSession);

      b2bhelper.createResponseToOriginalRequest(uasSession, 200, "Ok").send();
      logger.info("=== B2buaHelper: send the 200 OK for INVITE ===");
    } else if(method.equals("INFO")){
      logger.info("=== B2buaHelper: recive 200 OK for INFO ===");
      SipSession uacSession = resp.getSession();
      SipSession uasSession = (SipSession) appSession.getAttribute("uasSession");

      List<SipServletMessage> pendingMessages = b2bhelper.getPendingMessages(
          uasSession, UAMode.UAS);
      if (pendingMessages.size() != 1) {
        logger.error("*** B2buaHelper: pendingMessages are more than 1. ***");
        throw new TckTestException("pendingMessages are more than one.");
      }
      SipServletRequest infoReq = (SipServletRequest)pendingMessages.get(0);
      if (!infoReq.getMethod().equals("INFO")) {
        logger.error("*** B2buaHelper: Unexpected request. " +
            "Expect INFO but " + infoReq.getMethod() + "***");
        throw new TckTestException("Unexpected request. " +
            "Expect INFO but " + infoReq.getMethod());
      }
      infoReq.createResponse(200).send();
      logger.info("=== B2buaHelper: send 200 OK for INFO ===");

      Object times = appSession.getAttribute("getInfoTimes");
      if (times != null) {
        String getInfoTimes = (String) times;
        if (getInfoTimes.equals("2")) {
          uacSession.createRequest("BYE").send();
          logger.info("=== B2buaHelper: send 1st BYE ===");
        }
      } else {
        logger.error("*** B2buaHelper: Unexpected response." + resp + " ***");
        throw new TckTestException("Unexpected response." + resp);
      }
    } else if(method.equals("BYE")){
      SipSession uacSession = (SipSession)appSession.getAttribute("uacSession");
      if (resp.getSession().getId().equals(uacSession.getId())) {
        //receive 200/BYE from clientB
        logger.info("=== B2buaHelper: recive 1st 200 OK for BYE ===");
        try {
          b2bhelper.unlinkSipSessions(uacSession);
          logger.error("*** B2buaHelper: No IllegalArgumentException exception" +
              " caught when invoking unlinkSipSessions() ***");
          throw new TckTestException("IllegalArgumentException is expexted here");
        } catch (IllegalArgumentException e) {
          SipSession uasSession = (SipSession)appSession.getAttribute("uasSession");
          try {
            b2bhelper.linkSipSessions(uacSession, uasSession);
            logger.error("*** B2buaHelper: No IllegalArgumentException exception" +
                " caught when invoking linkSipSessions() ***");
            throw new TckTestException("IllegalArgumentException is expexted here");
          } catch (IllegalArgumentException e1) {
            uasSession.createRequest("BYE").send();
            logger.info("=== B2buaHelper: send 2nd BYE ===");
          }
        }
      } else {
        logger.info("=== B2buaHelper: recive 2nd 200 OK for BYE ===");
      }
    } else{
      logger.error("*** B2buaHelper: receive unexpected response"
          + resp + " ***");
      throw new TckTestException("receive unexpected response" + resp);
    }
  }

  protected void doAck(SipServletRequest req)
      throws ServletException, IOException {
    logger.info("=== B2buaHelper: recive ACK for INVITE ===");
    B2buaHelper b2bhelper = req.getB2buaHelper();
    List<SipServletMessage> pendingMessages = b2bhelper.getPendingMessages(
        b2bhelper.getLinkedSession(req.getSession()), UAMode.UAC);
    if (pendingMessages.size() != 1) {
      logger.error("*** B2buaHelper: pendingMessages are more than one. ***");
      throw new TckTestException("pendingMessages are more than one.");
    }

    SipServletResponse okResp = (SipServletResponse)pendingMessages.get(0);
    if (okResp.getStatus() != 200 || !"INVITE".equals(okResp.getRequest().getMethod())) {
      logger.error("*** B2buaHelper: pendingMessages aren't 200 for " +
          "INVITE but " + okResp + " ***");
      throw new TckTestException("pendingMessages should be 200 for " +
          "INVITE but "+ okResp);
    }

    okResp.createAck().send();
    logger.info("=== B2buaHelper: send ACK for INVITE ===");
  }

  protected void doInfo(SipServletRequest req)
      throws ServletException, IOException {
    logger.info("=== B2buaHelper: recive INFO ===");
    B2buaHelper b2bhelper = req.getB2buaHelper();
    SipSession uasSession = req.getSession();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.getAttribute("getInfoTimes") == null) {
      SipSession uacSession = b2bhelper.getLinkedSession(uasSession);
      if (!uacSession.getId().equals(
          ((SipSession)appSession.getAttribute("uacSession")).getId())) {
        logger.error("*** B2buaHelper: getLinkedSession doesn't " +
            "equal to the stored one ***");
        throw new TckTestException("getLinkedSession doesn't " +
            "equal to the stored one");
      }

      b2bhelper.unlinkSipSessions(uasSession);
      try {
        b2bhelper.unlinkSipSessions(uasSession);
        logger.error("*** B2buaHelper: No IllegalArgumentException " +
            "exception caught when invoking unlinkSipSessions() ***");
        throw new TckTestException("IllegalArgumentException is expected");
      } catch (IllegalArgumentException e) {
        SipSession linkedSession = b2bhelper.getLinkedSession(uasSession);
        if (linkedSession != null) {
          logger.error("*** B2buaHelper: linkedSession is not null ***");
          throw new TckTestException("linkedSession should be null " +
              "when invoking getLinkedSession() again");
        }
        uacSession.createRequest("INFO").send();
        appSession.setAttribute("getInfoTimes", "1");
        logger.info("=== B2buaHelper: send INFO ===");
      }
    } else {
      SipSession uacSession = (SipSession) appSession.getAttribute("uacSession");
      if (b2bhelper.getLinkedSession(uacSession) != null) {
        logger.error("*** B2buaHelper: getLinkedSession isn't null ***");
        throw new TckTestException("getLinkedSession should be null");
      }

      SipServletRequest newInfo = b2bhelper.createRequest(uacSession, req, null);

      if (!b2bhelper.getLinkedSession(uacSession).getId()
          .equals(uasSession.getId())) {
        logger.error("*** B2buaHelper: getLinkedSession doesn't " +
            "equal to the stored one. ***");
        throw new TckTestException("getLinkedSession doesn't " +
            "equal to the stored one");
      }

      if (!b2bhelper.getLinkedSipServletRequest(req).equals(newInfo)) {
        logger.error("*** B2buaHelper: getLinkedSipServletRequest" +
            " doesn't equal to the stored one. ***");
        throw new TckTestException("getLinkedSipServletRequest " +
            "doesn't equal to the stored one");
      }

      List<SipServletMessage> pendingMessages = b2bhelper.getPendingMessages(
          uacSession, UAMode.UAC);
      if (pendingMessages.size() != 0) {
        logger.error("*** B2buaHelper: Unexpected pendingMessages. ***");
        throw new TckTestException("PendingMessages should be 0");
      }

      newInfo.send();
      appSession.setAttribute("getInfoTimes", "2");
      logger.info("=== B2buaHelper: send INFO ===");
    }
  }

  private String getUa2Uri(SipServletRequest req){
    String header = req.getHeader(TestConstants.PRIVATE_URI);
    if(header == null){
      try {
        req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
        "Fail to get correct URI of ClientB.").send();
      } catch (IOException e) {
        logger.error("*** ServletParseException when creating URI ***", e);
        throw new TckTestException(e);
      }
    }

    return header;
  }

}
