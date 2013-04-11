/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * SipApplicationKeyB2buaServlet is used to test the specification of
 * Application Session Key feauture as the B2BUA application
 * Refer to com.bea.sipservlet.tck.agents.spec.SipApplicationKeyTest for Call Flow
 */
package com.bea.sipservlet.tck.apps.spec.b2bua;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TestConstants;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.annotation.SipApplicationKey;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.servlet.sip.annotation.SipServlet(
    name = "SipApplicationKey")
public class SipApplicationKeyB2buaServlet extends BaseServlet {
  private static final Logger logger = Logger.getLogger(SipApplicationKeyB2buaServlet.class);

  /**
   * The applicationKey method
   * @param req
   * @return
   */
  @SipApplicationKey
  public static String getApplicationKey(SipServletRequest req) {
    // Always return the same key for this testcase
    return "SipApplicationKey".equalsIgnoreCase(req.getHeader(TestConstants.SERVLET_HEADER)) ? "key1" : null;
  }

  protected void doSuccessResponse(SipServletResponse sipServletResponse)
      throws ServletException, IOException {
    serverEntryLog();
    logger.info("===" + "sipServletResponse=" + sipServletResponse + "===");

    // This case depends B2BUA functions properly
    SipServletRequest downStreamReq = sipServletResponse.getRequest();
    downStreamReq.getB2buaHelper().getLinkedSipServletRequest(downStreamReq)
        .createResponse(200).send();
  }

  protected void doMessage(SipServletRequest sipServletRequest)
      throws ServletException, IOException {
    serverEntryLog();
    logger.info("===" + "sipServletRequest=" + sipServletRequest + "===");

    SipServletRequest downStreamReq = null;
    if ("1".equals(sipServletRequest.getHeader(TestConstants.TEST_STEP_HEADER))) {
      // The application session should be kept alive to be targetted by another session
      sipServletRequest.getApplicationSession().setInvalidateWhenReady(false);
      sipServletRequest.getApplicationSession()
          .setAttribute("AppSessionId", sipServletRequest.getApplicationSession().getId());
      downStreamReq = sipServletRequest.getB2buaHelper().createRequest(sipServletRequest, true, null);
      downStreamReq.setHeader(TestConstants.APPKEY_HEADER, "key1");
      downStreamReq.setHeader(TestConstants.APP_HEADER, TestConstants.APP_UAS);
      downStreamReq.setHeader(TestConstants.TEST_STEP_HEADER, "2");
      downStreamReq.setHeader("AppSessionId", sipServletRequest.getApplicationSession().getId());
    } else if ("5".equals(sipServletRequest.getHeader(TestConstants.TEST_STEP_HEADER))) {
      if (sipServletRequest.getApplicationSession().getId()
          .equals(sipServletRequest.getApplicationSession().getAttribute("AppSessionId"))) {
        downStreamReq = sipServletRequest.getB2buaHelper().createRequest(sipServletRequest, true, null);
        downStreamReq.setHeader(TestConstants.APPKEY_HEADER, "key2");
        downStreamReq.setHeader(TestConstants.APP_HEADER, TestConstants.APP_UAS);
        downStreamReq.setHeader(TestConstants.TEST_STEP_HEADER, "6");
        // Remove this attribute after the case is over, otherwise a 2nd round test may target to
        //  this same application session
        sipServletRequest.getApplicationSession().removeAttribute("AppSessionId");
      } else {
        sipServletRequest.createResponse(500, "Same keys target to different application sessions").send();
        // Remove this attribute after the case is over, otherwise a 2nd round test may target to
        //  this same application session
        sipServletRequest.getApplicationSession().removeAttribute("AppSessionId");
        return;
      }
    }
    downStreamReq.send();
  }
}
