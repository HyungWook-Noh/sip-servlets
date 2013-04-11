/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 * 
 * SipApplicationKeyUasServlet is used to test the specification of
 * feauture as the UAS application
 * Refer to com.bea.sipservlet.tck.agents.spec.SipApplicationKeyTest for Call Flow
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TestConstants;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipApplicationKey;
import java.io.IOException;

@javax.servlet.sip.annotation.SipServlet(
    name = "SipApplicationKey")
public class SipApplicationKeyUasServlet extends BaseServlet {
  private static final Logger logger = Logger.getLogger(SipApplicationKeyUasServlet.class);

  /**
   * The applicationKey method
   * @param req
   * @return
   */
  @SipApplicationKey
  public static String getApplicationKey(SipServletRequest req) {
    return req.getHeader(TestConstants.APPKEY_HEADER);
  }

  protected void doMessage(SipServletRequest sipServletRequest) throws ServletException, IOException {
    serverEntryLog();
    logger.info("===" + "sipServletRequest=" + sipServletRequest + "===");

    if ("2".equals(sipServletRequest.getHeader(TestConstants.TEST_STEP_HEADER))) {
      // The application session should be kept alive to be targetted by another session
      sipServletRequest.getApplicationSession().setInvalidateWhenReady(false);
      sipServletRequest.getApplicationSession()
          .setAttribute("AppSessionId", sipServletRequest.getApplicationSession().getId());
      if (!sipServletRequest.getHeader("AppSessionId").equals(sipServletRequest.getApplicationSession().getId())) {
        sipServletRequest.createResponse(200).send();
      } else {
        sipServletRequest.createResponse(500, "Same key of different applications"
            + " should not target to the same application session").send();
      }
    } else if ("6".equals(sipServletRequest.getHeader(TestConstants.TEST_STEP_HEADER))) {
      if (!sipServletRequest.getApplicationSession().getId()
          .equals(sipServletRequest.getApplicationSession().getAttribute("AppSessionId"))) {
        sipServletRequest.createResponse(200).send();
      } else {
        sipServletRequest.createResponse(500, "Different keys"
            + " should not target to the same application session").send();
      }
      // Remove this attribute after the case is over, otherwise a 2nd round test may target to
      //  this same application session
      sipServletRequest.getApplicationSession().removeAttribute("AppSessionId");
    }
  }

  protected void doInvite(SipServletRequest sipServletRequest) throws ServletException, IOException {
    serverEntryLog();
    logger.info("===" + "sipServletRequest=" + sipServletRequest + "===");
    
    if ("2".equals(sipServletRequest.getHeader(TestConstants.TEST_STEP_HEADER))) {
      // The application session should be kept alive to be targetted by another session
      sipServletRequest.getApplicationSession().setInvalidateWhenReady(false);
      sipServletRequest.getApplicationSession()
          .setAttribute("AppSessionId", sipServletRequest.getApplicationSession().getId());
      sipServletRequest.createResponse(200).send();
    } else if ("8".equals(sipServletRequest.getHeader(TestConstants.TEST_STEP_HEADER))) {
      if (sipServletRequest.getApplicationSession().getId()
          .equals(sipServletRequest.getApplicationSession().getAttribute("AppSessionId"))) {
        sipServletRequest.createResponse(200).send();
      } else {
        sipServletRequest.createResponse(500, "Same keys"
            + " should not target to different application sessions").send();
      }
      // Remove this attribute after the case is over, otherwise a 2nd round test may target to
      //  this same application session
      sipServletRequest.getApplicationSession().removeAttribute("AppSessionId");      
    }
  }
}
