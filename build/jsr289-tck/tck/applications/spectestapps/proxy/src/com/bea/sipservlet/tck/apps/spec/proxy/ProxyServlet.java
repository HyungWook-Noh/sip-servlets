/*
 * $Id: ProxyServlet.java,v 1.3 2002/11/20 22:14:45 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 */

/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * ProxyServlet is used to test the specification of prosying.
 */
package com.bea.sipservlet.tck.apps.spec.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;

@SipServlet(name = "Proxy")
public class ProxyServlet extends BaseServlet {
  private static final String USER_PROXY_GEN2XX = "proxy-gen2xx";

  private static final String USER_PROXY_RECURSE_APP = "proxy-recurse-app";

  private static final long serialVersionUID = 1L;

  private static final String PROXY_SERVLET = "Proxy";

  /**
   * header to store proxy options
   */
  private static final String HEADER_TCK_PROXY_OPTIONS = "TCK-Proxy-Options";
  /**
   * header to store proxy dest URIs, 
   */
  private static final String HEADER_TCK_PROXY_DEST = "TCK-Proxy-Dest";
  /**
   * When pushing state to endpoints, the parameters set are also stored
   * in a map in the SipSession under this name. This is done so that we
   * can verify that we get back the same set of parameters with teh same
   * values on subsequent requests.
   */
  public static final String RR_PARAMS = "params";

  protected SipFactory sipFactory;

  protected String servletName;
  private static Logger logger = Logger.getLogger(ProxyServlet.class);

  @Override
  public void init() throws ServletException {
    logger.debug("---ProxyServlet init---");
    sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
    if (sipFactory == null) {
      throw new ServletException("No SipFactory context attribute");
    }
    servletName = getServletName();
  }

  @Override
  protected void doInvite(SipServletRequest req) throws ServletException,
      IOException{
    logger.debug("---doInvite---");
    proxyInitialRequest(req);
  }

  @Override
  protected void doMessage(SipServletRequest req)throws ServletException, IOException {
    logger.debug("---doMessage---");
    proxyInitialRequest(req);
  }

  private void proxyInitialRequest(SipServletRequest req)  throws ServletException, IOException {
    setProxyOptions(req);
    List dest = getProxyDest(req, sipFactory);

    if (dest.size() == 0) {
      req.createResponse(500, "No destinations").send();
    } else {
      logger.debug("---proxying to " + dest + "---");
      req.addHeader(PROXY_SERVLET, servletName);
      Proxy proxy = req.getProxy();
      proxy.proxyTo(dest);
    }
  }

  @Override
  protected void doBye(SipServletRequest req) throws ServletException {
    logger.debug("---doBye " + req.getRequestURI() + "---");
    if (req.isInitial()) {
      throw new ServletException("Got initial BYE");
    }

    checkRRParam(req);
    req.addHeader(PROXY_SERVLET, servletName);
  }

  @Override
  protected void doAck(SipServletRequest req) throws ServletException {
    logger.debug("---doAck " + req.getRequestURI() + "---");
    if (req.isInitial()) {
      throw new ServletException("Got initial ACK");
    }

    checkRRParam(req);
    req.addHeader(PROXY_SERVLET, servletName);
  }

  /**
   * Invoked for 2xx response to INVITE to verify that, if we pushed
   * state to the UAS, it's available now (typically in a Record-Route
   * header being returned to the UAC although that's implementation
   * dependent).
   */
  protected void checkRRParam(SipServletResponse resp, SipURI rrURI) throws ServletException {
    SipSession sipSession = resp.getSession(false);
    if (sipSession == null)
      return;
    Map params = (Map) sipSession.getAttribute(RR_PARAMS);
    if (params == null)
      return;

    Iterator iter = params.keySet().iterator();
    while (iter.hasNext()) {
      String name = (String) iter.next();
      String value = (String) params.get(name);
      String respValue = rrURI.getParameter(name);

      if (!value.equals(respValue)) {
        throw new ServletException(
            "Didn't get back expected state, name=" + name
                + ", value=" + value + ", got: " + respValue);
      }
    }
  }

  /**
   * Invoked for subsequent requests to verify that, if we pushed state
   * to the endpoints on the initial request, that we get it back
   * unchanged on this subsequent request [SSA, 8.4].
   */
  protected void checkRRParam(SipServletRequest req)  throws ServletException  {
    SipSession sipSession = req.getSession(false);
    if (sipSession == null)
      return;

    Map params = (Map) sipSession.getAttribute(RR_PARAMS);
    if (params == null)
      return;

    Iterator iter = params.keySet().iterator();
    while (iter.hasNext()) {
      String name = (String) iter.next();
      String value = (String) params.get(name);
      String reqValue = req.getParameter(name);

      if (!value.equals(reqValue)) {
        throw new ServletException(
            "Didn't get back expected state, name=" + name
                + ", value=" + value + ", got: " + reqValue);
      }
    }
  }

  @Override
  protected void doCancel(SipServletRequest req) {
    logger.debug("---doCancel " + req.getRequestURI() + "---");
  }

  @Override
  protected void doProvisionalResponse(SipServletResponse resp) {
    logger.debug("---doProvisionalResponse: " + getSummary(resp) + "---");
    resp.addHeader(PROXY_SERVLET, servletName);
  }

  @Override
  protected void doErrorResponse(SipServletResponse resp)  throws IOException  {
    logger.debug("---doErrorResponse: " + getSummary(resp) + "---");

    // in the proxy-gen2xx testcase we receive a 408 best response
    // and the app generates its own 202 [SSA, section 8.2.2].
    SipServletRequest req = resp.getProxy().getOriginalRequest();
    SipURI ruri = (SipURI) req.getRequestURI();
    if (USER_PROXY_GEN2XX.equals(ruri.getUser())) {
      logger.debug("---generating own 202 response");
      SipServletResponse resp202 = req.createResponse(202);

      resp202.addHeader(PROXY_SERVLET, servletName);
      resp202.send();
      return;
    } else {
      // all other testcases
      resp.addHeader(PROXY_SERVLET, servletName);
    }
  }

  @Override
  protected void doRedirectResponse(SipServletResponse resp) throws ServletException {
    logger.debug("---doRedirectResponse: " + getSummary(resp) + "---");

    SipServletRequest req = resp.getProxy().getOriginalRequest();
    SipURI ruri = (SipURI) req.getRequestURI();

    if (USER_PROXY_RECURSE_APP.equals(ruri.getUser())) {
      // in proxy-recurse-app do app-level redirection in this callback
      logger.debug("---doRedirectResponse: adding targets to proxy operation" + "---");
      ListIterator iter = resp.getAddressHeaders("Contact");

      int n = 0;
      while (iter.hasNext()) {
        Address addr = (Address) iter.next();
        if (addr.getURI().isSipURI()) {
          resp.getProxy().proxyTo(addr.getURI());
          n++;
        }
      }
      logger.debug("---doRedirectResponse: added " + n + " targets" + "---");
    } else {
      resp.addHeader(PROXY_SERVLET, servletName);
    }
  }

  @Override
  protected void doSuccessResponse(SipServletResponse resp) throws ServletException {
    logger.debug("---doSuccessResponse: " + getSummary(resp) + "---");
    resp.addHeader(PROXY_SERVLET, servletName);
  }

  /**
   * Returns one-line description of the specified request object.
   */
  private static String getSummary(SipServletRequest req) {
    return "" + req.getMethod();
  }

  /**
   * Returns one-line description of the specified response object.
   */
  private static String getSummary(SipServletResponse resp) {
    return "" + resp.getStatus() + "/" + resp.getMethod();
  }


  /**
   * This method interprets the Proxy-Options header as consisting
   * of a space separated list of name-value pairs and sets the
   * corresponding values on the Proxy objects. The recognized options
   * are exactly the set of Proxy properties.
   */
  static void setProxyOptions(SipServletRequest req) throws TooManyHopsException {
    Map rrParams = null;
    Proxy proxy = req.getProxy();
    String opts = req.getHeader(HEADER_TCK_PROXY_OPTIONS);

    if (opts == null)
      return;

    StringTokenizer tok = new StringTokenizer(opts);

    while (tok.hasMoreTokens()) {
      String pair = tok.nextToken();
      int equal = pair.indexOf('=');
      if (equal < 1)
        continue;

      String name = pair.substring(0, equal).intern();
      String value = pair.substring(equal + 1);

      if (name == "recurse") {
        proxy.setRecurse("true".equals(value));
      } else if (name == "recordRoute") {
        proxy.setRecordRoute("true".equals(value));
        // force statefulness:
        req.getSession();
      } else if (name == "parallel") {
        proxy.setParallel("true".equals(value));
      } else if (name == "supervised") {
        proxy.setSupervised("true".equals(value));
      } else if (name == "stateful") {
        proxy.setStateful("true".equals(value));
      } else if (name == "sequentialSearchTimeout") {
        try {
          proxy.setSequentialSearchTimeout(
          Integer.parseInt(value));
        } catch (Exception ex) {
        }
      } else if (name.startsWith("param.")) {
        String pName = name.substring(6);
        SipURI rrURI = proxy.getRecordRouteURI();

        rrURI.setParameter(pName, value);
        // store options Map so we can verify params of subsequent req
        if (rrParams == null) {
          rrParams = new HashMap();
          SipSession sipSession = req.getSession();
          sipSession.setAttribute(RR_PARAMS, rrParams);
        }
        rrParams.put(pName, value);
      }

    }

  }

  /**
   * Returns the value of the custom Proxy-Dest header as a List
   * of URIs. The returned List can be used as an argument to
   * Proxy.proxy().
   */
  static List getProxyDest(SipServletRequest req, SipFactory sf) throws ServletException {
    ArrayList l = new ArrayList();
    String value = req.getHeader(HEADER_TCK_PROXY_DEST);
    if (value != null) {
      StringTokenizer tok = new StringTokenizer(value);
      while (tok.hasMoreTokens()) {
        URI uri = sf.createURI(tok.nextToken());
        l.add(uri);
      }
    }

    return l;
  }
}
