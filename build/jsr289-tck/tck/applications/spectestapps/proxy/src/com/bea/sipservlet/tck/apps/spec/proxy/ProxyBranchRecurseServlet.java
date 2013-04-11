/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * ProxyBranchRecurseServlet is used to test the spec of proxy branch
 *
 */

package com.bea.sipservlet.tck.apps.spec.proxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.URI;
import javax.servlet.sip.Address;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TestConstants;



@javax.servlet.sip.annotation.SipServlet(name="ProxyBranchRecurse")
public class ProxyBranchRecurseServlet extends BaseServlet{
  private static Logger logger = Logger.getLogger(ProxyBranchRecurseServlet.class);
  private ProxyBranch ua2Branch;
  private URI ua3;
  protected void doInvite(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    logger.info("===[doInvite]: " + req.getRequestURI() + "===");
    
    Proxy p = req.getProxy();

    p.setRecordRoute(true);
    p.setSupervised(true);

    URI ua2 = ((Address) req.getAddressHeader("TARGET1")).getURI(); 
    ua3 = ((Address) req.getAddressHeader("TARGET2")).getURI(); 
    p.proxyTo(ua2);  
    ua2Branch = p.getProxyBranch(ua2);
  }
  protected void doSuccessResponse(SipServletResponse res)
  throws ServletException, IOException {
    // When proxy application is notified of the 200 response it performs the
    // following operation:
    //
    // 1. PB2.getResursedProxyBranches(); it should returns a non-empty list
    // which contains the branch to UAS3, name it PB3. This fact can be verified
    // by the following assertions:
    // * PB3.getRequest().getRequestURI() should be equal to uri of UAS3
    // * PB3.getResponse() should return 200.
    ProxyBranch ua3Branch = ua2Branch.getRecursedProxyBranches().get(0);
    if (!ua3Branch.getRequest().getRequestURI().equals(ua3)) {
      String message = "The ua3 recursed proxy branch uri="
          + ua3Branch.getRequest().getRequestURI()
          + " is unequal to original ua3 uri=" + ua3;

      res.addHeader(TestConstants.TEST_FAIL_REASON, message);
      logger.error("***" + message + "***");
      return;
    }
    if (ua3Branch.getResponse().getStatus() != SipServletResponse.SC_OK) {
      res.addHeader(TestConstants.TEST_FAIL_REASON,
          "The ua3 recursed proxy branch response's status is not "
              + SipServletResponse.SC_OK);
      logger
          .error("*** The ua3 recursed proxy branch response's status is not "
              + SipServletResponse.SC_OK + "***");
      return;
    }
  }
}
