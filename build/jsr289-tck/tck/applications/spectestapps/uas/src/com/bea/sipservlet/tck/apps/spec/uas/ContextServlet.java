/*
 * $Id: Context.java,v 1.2 2002/09/03 15:22:53 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 */
/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * ContextServlet is used to test a number of ServletContext methods,
 * including ability to retrieve content and servlet parameters. Other
 * tests ensure the servlet context includes SipFactory and TimerService.
 *
 */

package com.bea.sipservlet.tck.apps.spec.uas;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TckTestException;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * <p>A 200 is returned if everything OK, otherwise a 500 with a
 * hopefully informative reason phrase.
 *
 */

@javax.servlet.sip.annotation.SipServlet(name = "Context")
public class ContextServlet extends BaseServlet {

  private static Logger logger = Logger.getLogger(ContextServlet.class);

  protected void doOptions(SipServletRequest req) throws  IOException {
    serverEntryLog();
    logger.info("=== Context: receive OPTION ===");
    try {
      checkContext();
      ServletContext ctxt = getServletContext();
      RequestDispatcher dispatcher = ctxt.getNamedDispatcher("Context2");
      dispatcher.forward(req, null);  // sends 200
    } catch (Exception e) {
      logger.error("*** Context: Failed ***" + e);
      SipServletResponse resp = req.createResponse(500, e.getMessage());
      resp.send();
    }
  }

  public void checkContext() {
    ServletConfig cfg = getServletConfig();
    ServletContext ctxt = getServletContext();

    if (cfg.getServletContext() != ctxt) {
      throw new TckTestException("ServletContext differs depending on getter");
    }

    if (!"Context".equals(getServletName())) {
      throw new TckTestException("getServletName() != \"ContextServlet\"");
    }

    equals("getServletContextName()",
        "Spec Assertion Test UAS Application",
        ctxt.getServletContextName());
    
    if ((ctxt.getMajorVersion() * 10 + ctxt.getMinorVersion()) < 23)
      throw new TckTestException("getMajorVersion and getMinorVersion must be " +
          "compatible with Servlet Specification 2.3 or higher.");

    // context parameters
    equals("ServletContext.getInitParameter(\"pet\")", "cat",
        ctxt.getInitParameter("pet"));

    equals("ServletContext.getInitParameter(\"hobby\")", "halo",
        ctxt.getInitParameter("hobby"));

    equals("ServletContext.getInitParameter(\"foo\")", null,
        ctxt.getInitParameter("foo"));

    List<String> expected = new ArrayList<String>(2);
    expected.add("pet");
    expected.add("hobby");
    verifyStringEnum(ctxt.getInitParameterNames(), expected,
        "ServletContext.getInitParameterNames()");

    // servlet parameters
    equals("SipServlet.getInitParameter(\"eyes\")", "brown",
        getInitParameter("eyes"));

    equals("SipServlet.getInitParameter(\"hair\")", "black",
        getInitParameter("hair"));

    equals("SipServlet.getInitParameter(\"foo\")", null,
        getInitParameter("foo"));

    expected.clear();
    expected.add("eyes");
    expected.add("hair");
    verifyStringEnum(getInitParameterNames(), expected,
        "SipServlet.getInitParameterNames()");
  }

  private static void equals(String errPrefix,
                             String s1, String s2) {
    if (s1 == null) {
      if (s2 != null) {
        throw new TckTestException(
            errPrefix + ": expected null, got \""+s2+"\"");
      }
    } else if (!s1.equals(s2)) {
      throw new TckTestException(
          errPrefix + ": expected \""+s1+"\", got \""+s2+"\"");
    }
  }

  private void verifyStringEnum(Enumeration enum1,List<String> expected,
                                String errPrefix) {
    while (enum1.hasMoreElements()) {
      String name = (String) enum1.nextElement();
      if (!expected.remove(name)) {
        throw new TckTestException(
            errPrefix + " included unexpected name: " + name);
      }
    }
    if (expected.size() > 0) {
      throw new TckTestException(
          errPrefix + " didn't include headers " + expected);
    }

  }

}

