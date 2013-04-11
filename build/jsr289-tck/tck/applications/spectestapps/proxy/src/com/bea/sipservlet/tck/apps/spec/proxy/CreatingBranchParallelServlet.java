/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * CreatingBranchParallelServlet is used to test the spec of proxy branch
 *
 */

package com.bea.sipservlet.tck.apps.spec.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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



@javax.servlet.sip.annotation.SipServlet(name="CreatingBranchParallel")
public class CreatingBranchParallelServlet extends BaseServlet{
  private static Logger logger = Logger.getLogger(CreatingBranchParallelServlet.class);
  private URI[] ua = new URI[5];

  protected void doInvite(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    logger.info("===[doInvite]: " + req.getRequestURI() + "===");
    
    Proxy p = req.getProxy();

    p.setRecordRoute(true);
    p.setSupervised(true);
    p.setParallel(true);
    ua[2] = ((Address) req.getAddressHeader("TARGET1")).getURI();
    ua[3] = ((Address) req.getAddressHeader("TARGET2")).getURI();
    ua[4] = ((Address) req.getAddressHeader("TARGET3")).getURI();    
    p.proxyTo(ua[2]);
  }
  protected void doProvisionalResponse(SipServletResponse res)
  throws ServletException, IOException {
    if(res.getRequest().getRequestURI().equals(ua[2])){
      List<URI> uriList = new ArrayList<URI>(2);
      uriList.add(ua[3]);
      uriList.add(ua[4]);
      logger.info("===[doProvisionalResponse]: " + uriList + "===");
      res.getProxy().createProxyBranches(uriList);    
      res.getProxy().startProxy();   
    }
  }
  
  protected void doSuccessResponse(SipServletResponse res)
  throws ServletException, IOException {
    // UAS3 responds to the INVITE with a 200 response. Proxy application
    // performs the following operations:
    //
    // 1. Call ProxyBranch.getRequest() on all the three branches and check the
    // requestURIs of these requests.
    // 2. Try SipServletRequest.send() on the requests retrieved above and
    // exception is expected to be caught.
    // 3. Call ProxyBranch.getResponse() on all the three branches, they should
    // be 180, 200 and 180 on these three proxyBranches.
    for (int i = 2; i < 5; i++) {
      ProxyBranch uaBranch = res.getProxy().getProxyBranch(ua[i]);
      if (!uaBranch.getRequest().getRequestURI().equals(ua[i])) {
        String message = "The ua" + i
            + " proxy branch's request uri  is unequal to original ua" + i
            + " request uri";
        res.addHeader(TestConstants.TEST_FAIL_REASON, message);
        logger.error("***" + message + "***");
        return;
      }
      if (i == 2 || i == 3) {
        if (uaBranch.getResponse().getStatus() != SipServletResponse.SC_RINGING) {
          String message = "The ua" + i
              + " proxy branch response status is not "
              + SipServletResponse.SC_RINGING;
          res.addHeader(TestConstants.TEST_FAIL_REASON, message);
          logger.error("***" + message + "***");
          return;
        }
      }

      try {
        uaBranch.getRequest().send();
      } catch (java.lang.IllegalStateException e) {
        res.addHeader(TestConstants.TEST_RESULT, TestConstants.TEST_RESULT_OK);
      }

      if (i == 4) {
        if (uaBranch.getResponse().getStatus() != SipServletResponse.SC_OK) {
          String message = "The ua" + i
              + " proxy branch response status is not "
              + SipServletResponse.SC_OK;
          res.addHeader(TestConstants.TEST_FAIL_REASON, message);
          logger.error("***" + message + "***");
          return;
        }
      }
    }

  }
  
}
