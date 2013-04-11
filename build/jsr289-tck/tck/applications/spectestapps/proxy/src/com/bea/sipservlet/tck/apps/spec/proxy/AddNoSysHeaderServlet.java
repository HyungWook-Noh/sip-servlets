/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * AddNoSysHeaderServlet is used to test the spec of proxy branch
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
import javax.servlet.sip.URI;
import javax.servlet.sip.Address;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;



@javax.servlet.sip.annotation.SipServlet(name="AddNoSysHeader")
public class AddNoSysHeaderServlet extends BaseServlet{
  private static Logger logger = Logger.getLogger(AddNoSysHeaderServlet.class);

  protected void doInvite(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    logger.info("===[doInvite]: " + req.getRequestURI() + "===");
    // Proxy application perform the following operations:
    //
    // * SipServletRequest.getProxy()
    // * Proxy.setRecordRoute(true)
    // * Call Proxy.createProxyBranches(uas1&uas2) and name the newly created
    // branches as PB2 and PB3.
    // * PB2.getRequest() to get the request on proxyBranch PB2. Then push the route
    // to uas2 on this request.
    // * PB3.getRequest() to get the request on proxyBranch PB3. Then push the route
    // to uas3 on this request. Also add a private header on this request.
    // * PB2.getRecordRouteURI() and add PB2 specific parameters on the returned
    // URI, do the same thing on PB3 to add PB3 specific parameters.
    // * Proxy.startProxy()
    Proxy p = req.getProxy();

    p.setRecordRoute(true);
    p.setSupervised(true);
    p.setParallel(true);
    
    URI ua2 = ((Address) req.getAddressHeader("TARGET1")).getURI();
    URI ua3 = ((Address) req.getAddressHeader("TARGET2")).getURI();    
    List<URI> uriList = new ArrayList<URI>(2);
    uriList.add(ua2);
    uriList.add(ua3);
    logger.info("===[doInvite]: " + uriList + "===");
    List<ProxyBranch> branchs = p.createProxyBranches(uriList);

    ProxyBranch pb2 = p.getProxyBranch(ua2);
    ProxyBranch pb3 = p.getProxyBranch(ua3);
    pb2.getRequest().pushRoute((Address) req.getAddressHeader("TARGET1"));
    pb3.getRequest().pushRoute((Address) req.getAddressHeader("TARGET2"));
    pb3.getRequest().addHeader("header3", "header3");
    pb2.getRecordRouteURI().setParameter("param2", "param2");
    pb3.getRecordRouteURI().setParameter("param3", "param3");
       
    p.startProxy();   
  }
}
