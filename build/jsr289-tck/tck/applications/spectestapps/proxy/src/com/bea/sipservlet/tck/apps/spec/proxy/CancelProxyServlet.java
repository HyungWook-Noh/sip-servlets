/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * CancelProxyServlet is used to test the spec of proxy branch
 *
 */

package com.bea.sipservlet.tck.apps.spec.proxy;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.URI;
import javax.servlet.sip.Address;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;



@javax.servlet.sip.annotation.SipServlet(name="CancelProxy")
public class CancelProxyServlet extends BaseServlet{
  private static Logger logger = Logger.getLogger(CancelProxyServlet.class);
  private URI ua2;
  private URI ua3;
  private URI ua4;

  protected void doInvite(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    logger.info("===[doInvite]: " + req.getRequestURI() + "===");
    
    Proxy p = req.getProxy();

    p.setRecordRoute(true);
    p.setSupervised(true);
    p.setParallel(true);
    
    ua2 = ((Address) req.getAddressHeader("TARGET1")).getURI();
    ua3 = ((Address) req.getAddressHeader("TARGET2")).getURI();
    ua4 = ((Address) req.getAddressHeader("TARGET3")).getURI();  
    p.proxyTo(ua2); 
  }
  
  protected void doProvisionalResponse(SipServletResponse res)
  throws ServletException, IOException {
    // 1 UAS2 responds with 180.
    // 2 Proxy application is notified of the 180 response and it performs the
    //   following operations:
    //
    // * Proxy.Cancel()
    // * Proxy.ProxyTo(UAS3)
    //
    // 3 UAS3 responds with 180.
    // 4 Proxy application is notified of this 180 response and it performs the
    //   following operations:
    //
    //  * Retrieve the proxyBranch via Proxy.getProxyBranch(uas3)
    //  * Call ProxyBranch.cancel(String[] protocol, int[] reasonCode, String[]
    //    reasonText) to cancel this the processing of the INVITE request on this
    //    branch.
    //  * Create one new ProxyBranch by calling Proxy.createProxyBranch(uas4) and
    //    then call Proxy.startProxy() to begin proxying to uas4
    if (res.getRequest().getRequestURI().equals(ua2)) {
      res.getProxy().cancel();
      res.getProxy().proxyTo(ua3);
    }
    if (res.getRequest().getRequestURI().equals(ua3)) {
      String[] protocol = { "SIP" };
      int[] reasonCode = { 180 };
      String[] reasonTest = { "APP cancels." };
      res.getProxy().getProxyBranch(ua3).cancel(protocol, reasonCode, reasonTest);
      res.getProxy().createProxyBranches(Collections.singletonList(ua4));
      res.getProxy().startProxy();
    }
  }
}
