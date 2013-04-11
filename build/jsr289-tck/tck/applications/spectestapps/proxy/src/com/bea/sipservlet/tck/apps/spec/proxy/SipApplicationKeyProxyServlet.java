/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 * 
 * SipApplicationKeyProxyServlet is used to test the specification of
 * Application Session Key feauture as the Proxy application
 * Refer to com.bea.sipservlet.tck.agents.spec.SipApplicationKeyTest for Call Flow
 */
package com.bea.sipservlet.tck.apps.spec.proxy;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TestConstants;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipApplicationKey;
import java.io.IOException;

@javax.servlet.sip.annotation.SipServlet(
    name = "SipApplicationKey")
public class SipApplicationKeyProxyServlet  extends BaseServlet {
  private static final Logger logger = Logger.getLogger(SipApplicationKeyProxyServlet.class);
  
  /**
   * The applicationKey method
   * @param req
   * @return
   */
  @SipApplicationKey
  public static String getApplicationKey(SipServletRequest req) {
    return null;
  }

  protected void doInvite(SipServletRequest sipServletRequest) throws ServletException, IOException {
    serverEntryLog();
    logger.info("===" + "sipServletRequest=" + sipServletRequest + "===");

    if ("1".equals(sipServletRequest.getHeader(TestConstants.TEST_STEP_HEADER))) {
      // The application session should be kept alive to be targetted by another session
      sipServletRequest.getApplicationSession().setInvalidateWhenReady(false);
      sipServletRequest.getApplicationSession()
          .setAttribute("AppSessionId", sipServletRequest.getApplicationSession().getId());
      sipServletRequest.setHeader(TestConstants.APPKEY_HEADER, "key2");
      sipServletRequest.setHeader(TestConstants.APP_HEADER, TestConstants.APP_UAS);
      sipServletRequest.setHeader(TestConstants.TEST_STEP_HEADER, "2");
      sipServletRequest.getProxy().proxyTo(sipServletRequest.getRequestURI());
    } else if ("7".equals(sipServletRequest.getHeader(TestConstants.TEST_STEP_HEADER))) {
      // The application session should be kept alive to be targetted by another session
      sipServletRequest.getApplicationSession().setInvalidateWhenReady(false);      
      if (!sipServletRequest.getApplicationSession().getId()
          .equals(sipServletRequest.getApplicationSession().getAttribute("AppSessionId"))) {
        sipServletRequest.setHeader(TestConstants.APPKEY_HEADER, "key2");
        sipServletRequest.setHeader(TestConstants.APP_HEADER, TestConstants.APP_UAS);
        sipServletRequest.setHeader(TestConstants.TEST_STEP_HEADER, "8");
        sipServletRequest.getProxy().proxyTo(sipServletRequest.getRequestURI());
      } else {
        sipServletRequest.createResponse(500, "Null keys target to the same application sessions").send();
      }
    }
  }
}
