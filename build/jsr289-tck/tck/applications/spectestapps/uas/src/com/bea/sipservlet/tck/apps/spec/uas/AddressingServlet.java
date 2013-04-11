/*
 * $Id: Addressing.java,v 1.4 2003/02/05 00:33:13 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 */

/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * AdressingServlet is used to test the specification of addressing
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TelURL;
import javax.servlet.sip.URI;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;

@SipServlet(name = "Addressing")
public class AddressingServlet extends BaseServlet {

  private static final long serialVersionUID = 1L;

  /** Some SIP URI with prop that there's only one way to format it. */
  private static String SIP_URI = "sip:alice@example.com:4000";
  private static String SIPS_URI = "sips:alice@example.com:4000";
  private static String SIP_ADDR = "Alice Cooper <sip:alice@example.com>";
  private static String SIP_ADDR_PARAMS = "Alice Cooper <sip:alice@example.com>;foo=bar;baz";

  private static String SIP_ADDR_URI = "sip:alice@example.com";

  private static String CONTACT = "Alice <sip:alice@1.2.3.4>;expires=3600;q=0.4";

  private static String CONTACT2 = "Alice <sip:alice@1.2.3.4>";

  private static final String SIP_FACTORY = "javax.servlet.sip.SipFactory";
  
  private static Logger logger = Logger.getLogger(AddressingServlet.class);

  /**
   * Holds name of testcase. Used in case of error to generate more
   * meaningful error message.
   */
  private static final ThreadLocal<String> testName = new ThreadLocal<String>();

  private SipFactory sf;

  public void init() throws ServletException {
    sf = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
    if (sf == null) {
      throw new ServletException("No SipFactory context attribute");
    }
  }

  private final void startTest(String name) {
    logger.debug("--- start test: " + name + "---");
    testName.set(name);
  }

  protected void doMessage(SipServletRequest req) throws ServletException,
      IOException {
  	serverEntryLog();
    logger.debug("---doMessage---");
    try {
      checkSipURI();
      checkSipsURI();
      checkTelURL();
      checkMiscURI();
      checkAddress();
      checkContact();
      SipServletResponse resp = req.createResponse(200, "OK");
      resp.send();
    } catch (Err e) {
      String name = (String) testName.get();
      logger.error("*** test " + name + " failed ***", e);
      SipServletResponse resp = req.createResponse(500, "" + name + ": "
          + e.getMessage());
      resp.send();
    }
  }

  void checkSipURI() throws ServletException {
    startTest("checkSipURI 1");
    SipURI u1;
    SipURI u2;
    u1 = (SipURI) sf.createURI(SIP_URI);
    if (!SIP_URI.equals(u1.toString())) {
      err("Unexpected string form of SipURI, u1=" + u1 + ", u2=" + SIP_URI);
    }

    if (!"alice".equals(u1.getUser())) {
      err("getUser() returned " + u1.getUser() + ", expected \"alice\"");
    }

    if (u1.getUserPassword() != null) {
      err("getUserPassword() returned non-null: " + u1.getUserPassword());
    }

    if (!"example.com".equals(u1.getHost())) {
      err("getHost() returned " + u1.getHost() + ", expected \"example.com\"");
    }

    if (u1.getPort() != 4000) {
      err("getport() returned " + u1.getPort() + ", expected 4000");
    }

    u2 = (SipURI) u1.clone();
    AddrUtils.equals(u1, u2);

    startTest("checkSipURI 2");
    u1.setMAddrParam("1.2.3.4");
    u1.setMethodParam("FOO");
    u1.setTTLParam(17);
    u1.setUserParam("ip");
    u1.setTransportParam("tls");
    u1.setLrParam(true);

    boolean err = false;
    try {
      // this *should* cause exception
      AddrUtils.equals(u1, u2);
      err = true;
    } catch (Err _) {
    }

    if (err) {
      throw new Err("setting params didn't make SipURIs unequal, " + "u1=" + u1 + ", u2=" + u2);
    }

    startTest("checkSipURI 3");
    u2.setParameter("maddr", "1.2.3.4");
    u2.setParameter("method", "FOO");
    u2.setParameter("ttl", "17");
    u2.setParameter("user", "ip");
    u2.setParameter("transport", "tls");
    u2.setParameter("lr", "");
    AddrUtils.equals(u1, u2);

    startTest("checkSipURI 4");
    u1 = (SipURI) sf.createURI("sip:alice@example.com:4000");
    u2 = (SipURI) sf.createURI("sip:alice@example.com");
    u1.setPort(-1); // unset port number
    AddrUtils.equals(u1, u2);
  }

  void checkSipsURI() throws ServletException {
    startTest("checkSipsURI");
    SipURI u1 = (SipURI) sf.createURI(SIP_URI);
    SipURI u2 = (SipURI) sf.createURI(SIPS_URI);

    // we just check sips URIs aren't equal to sip URIs (tests scheme only)
    try {
      AddrUtils.equals((URI) u1, (URI) u2);
    } catch (Err _) {
      return;
    }

    throw new Err("sip and sips URIs indistinguishable, sip=" + u1 + ", sips=" + u2);
  }

  void checkTelURL() throws ServletException {
    startTest("checkTelURL");
    TelURL u1;
    TelURL u2;

    u1 = (TelURL) sf.createURI("tel:+358-555-1234567");
    if (!"tel".equals(u1.getScheme())) {
      err("TelURL.getScheme() not \"tel\"");
    }

    if (!"358-555-1234567".equals(u1.getPhoneNumber())) {
      err("getPhoneNumber() returned " + u1.getPhoneNumber() + ", expected 358-555-1234567");
    }

    if (!u1.isGlobal()) {
      err("expected url to be global: " + u1);
    }

    u2 = (TelURL) u1.clone();
    AddrUtils.equals(u1, u2);
    u2 = (TelURL) sf.createURI("tel:+358-555-1234567;postd=pp22");
    equals("u2.getParameter(\"postd\")", "pp22", u2.getParameter("postd"));

    boolean err = false;
    try {
      AddrUtils.equals(u1, u2);
      err = true;
    } catch (Err _) {
    }

    if (err) {
      err("TelURLs erroneously equal, u1=" + u1 + ", u2=" + u2);
    }
  }

  void checkMiscURI() throws ServletException {
    startTest("checkMiscURI");
    URI u1;
    URI u2;
    u1 = sf.createURI("mailto:alice@example.com");
    equals("getScheme()", "mailto", u1.getScheme());
    equals("isSipURI()", "false", "" + u1.isSipURI());

    u2 = (URI) u1.clone();
    AddrUtils.equals(u1, u2);

    u1 = sf.createURI("http://example.com/foo/bar");
    equals("getScheme()", "http", u1.getScheme());
    equals("isSipURI()", "false", "" + u1.isSipURI());
  }

  void checkAddress() throws ServletException {
    startTest("checkAddress");
    Address a1;
    Address a2;

    SipURI u1;
    List<String> l = new ArrayList<String>();
    a1 = sf.createAddress(SIP_ADDR);
    equals("getDisplayName()", "Alice Cooper", a1.getDisplayName());

    if (!AddrUtils.same(a1.getParameterNames(), l.iterator())) {
      err("getParameterNames() for " + SIP_ADDR + " not empty");
    }

    a1.setParameter("foo", "bar");
    l.add("foo");
    if (!AddrUtils.same(a1.getParameterNames(), l.iterator())) {
      err("getParameterNames() for " + SIP_ADDR + " not [\"foo\"]");
    }

    a2 = sf.createAddress(SIP_ADDR_PARAMS);
    equals("getParameter(\"baz\")", "", a2.getParameter("baz"));
    a2.removeParameter("baz");

    AddrUtils.equals(a1, a2);
    u1 = (SipURI) sf.createURI(SIP_ADDR_URI);

    AddrUtils.equals(a1.getURI(), u1);
  }

  //param names are case insensitive -- try removeParameter("eXpIres")
  void checkContact() throws ServletException {
    Address c1;
    Address c2;

    startTest("checkContact 1");
    c1 = sf.createAddress(CONTACT);
    equals("getDisplayName()", "Alice", c1.getDisplayName());
    equals("getExpires()", "3600", "" + c1.getExpires());
    equals("getQ()", "0.4", "" + c1.getQ());

    startTest("checkContact 2");
    c2 = sf.createAddress(CONTACT2);
    c2.setExpires(3600);
    c2.setQ(0.4F);
    AddrUtils.equals(c1, c2);

    startTest("checkContact 3");
    c2 = sf.createAddress(CONTACT2);
    c2.setParameter("expires", "3600");
    c2.setParameter("q", "0.4");
    AddrUtils.equals(c1, c2);

    startTest("checkContact 4");
    c1 = sf.createAddress(CONTACT);
    c2 = sf.createAddress(CONTACT2);
    c1.setQ(-1);
    c1.setExpires(-1);
    AddrUtils.equals(c1, c2);

    startTest("checkContact 5");
    c1 = sf.createAddress(CONTACT);
    c2 = sf.createAddress(CONTACT2);
    c1.removeParameter("q");
    c1.removeParameter("expires");

    // wildcard tests
    if (c1.isWildcard()) {
      err("isWildcard() true for " + c1);
    }

    if (c2.isWildcard()) {
      err("isWildcard() true for " + c2);
    }

    Address wildcard = sf.createAddress("*");
    if (!wildcard.isWildcard()) {
      err("SipFactory.createAddress(\"*\").isWildcard() == false");
    }
  }

  private static void equals(String errPrefix, String s1, String s2) {
    if (!s1.equals(s2)) {
      throw new Err(errPrefix + " expected \"" + s1 + "\", got \"" + s2 + "\"");
    }
  }

  private static void err(String msg) {
    throw new Err(msg);
  }

  public String getServletInfo() {
    return "Tests addressing related API";
  }
}

/**
 * Collection of utility methods for comparing URIs for equality.
 */
abstract class AddrUtils {

  public static void equals(URI u1, URI u2) {
    String s1 = u1.getScheme().intern();
    String s2 = u2.getScheme().intern();
    if (s1 != s2) {
      err("URI schemes differ: " + s1 + ", " + s2);
    }

    if (s1 == "sip") {
      equals((SipURI) u1, (SipURI) u2);
    } else if (s1 == "tel") {
      equals((TelURL) u1, (TelURL) u2);
    } else {
      // reasonable to require "misc" URIs to be string equal?
      if (!u1.toString().equals(u2.toString())) {
        throw new Err("Two non-SIP, non-Tel URIs not identical: " +
        "u1=" + u1 + ", u2=" + u2);
      }
    }
  }

  /**
   * Returns true if the two specified SipURI objects are equal,
   * i.e. if they have the same user, password, host, port, parameters,
   * and headers.
   */
  static void equals(SipURI u1, SipURI u2) {
    // compare user parts
    if (!exclEquals(u1.getUser(), u2.getUser())) {
      err("user part differs: u1=" + u1 + ", u2=" + u2 +
      ", user parts: " + u1.getUser() + ", " + u2.getUser());
    }

    // password
    if (!exclEquals(u1.getUserPassword(), u2.getUserPassword())) {
      err("user password differs: u1=" + u1 + ", u2=" + u2 +
      ", user password: " + u1.getUserPassword() + ", " +
      u2.getUserPassword());
    }

    // host part
    if (!u1.getHost().equals(u2.getHost())) {
      err("host part differs: u1=" + u1 + ", u2=" + u2 +
      ", host parts: " + u1.getHost() + ", " + u2.getHost());
    }

    // port numbers
    if (u1.getPort() != u2.getPort()) {
      err("port numbers differs: u1=" + u1 + ", u2=" + u2 +
      ", ports: " + u1.getPort() + ", " + u2.getPort());
    }

    // parameters
    if (!same(u1.getParameterNames(), u2.getParameterNames())) {
      err("different set of parameter names " +
      "u1=" + u1 + ", u2=" + u2);
    }

    Iterator iter = u1.getParameterNames();
    while (iter.hasNext()) {
      String paramName = (String) iter.next();
      String p1 = u1.getParameter(paramName);
      String p2 = u2.getParameter(paramName);
      if (!p1.equals(p2)) {
        err("parameters differ: u1=" + u1 + ", u2=" + u2 +
        ", param=" + paramName + ", p1=" + p1 + ", p2=" + p2);
      }
    }

    // headers
    if (!same(u1.getHeaderNames(), u2.getHeaderNames())) {
      err("different set of header names " +
      "u1=" + u1 + ", u2=" + u2);
    }

    iter = u1.getHeaderNames();
    while (iter.hasNext()) {
      String headerName = (String) iter.next();
      String h1 = u1.getHeader(headerName);
      String h2 = u2.getHeader(headerName);
      if (!h1.equals(h2)) {
        err("headers differ: u1=" + u1 + ", u2=" + u2 +
        ", header=" + headerName + ", h1=" + h1 + ", h2=" + h2);
      }
    }
  }

  public static void equals(TelURL u1, TelURL u2) {
    if (!u1.getPhoneNumber().equals(u2.getPhoneNumber())) {
      err("getPhoneNumber() differs for " +
      "u1=" + u1 + ", u2=" + u2);
    }

    if (u1.isGlobal() != u2.isGlobal()) {
      err("isGlobal differs for " +
      "u1=" + u1 + ", u2=" + u2);
    }

    if (!same(u1.getParameterNames(), u2.getParameterNames())) {
      err("different set of parameter names " +
      "u1=" + u1 + ", u2=" + u2);
    }

    Iterator iter = u1.getParameterNames();
    while (iter.hasNext()) {
      String paramName = (String) iter.next();
      String p1 = u1.getParameter(paramName);
      String p2 = u2.getParameter(paramName);
      if (!p1.equals(p2)) {
        err("parameters differ: u1=" + u1 + ", u2=" + u2 +
        ", param=" + paramName + ", p1=" + p1 + ", p2=" + p2);
      }
    }
  }

  public static void equals(Address a1, Address a2) {
    // URIs
    equals(a1.getURI(), a2.getURI());
    // display name
    String n1 = a1.getDisplayName();
    String n2 = a2.getDisplayName();

    if ((n1 == null && n2 != null) || (n1 != null && n2 == null) || !n1.equals(n2))  {
      err("display names differ: a1=" + a1 + ", a2=" + a2 +
      ", display names: " + n1 + ", " + n2);
    }

    // parameters
    if (!same(a1.getParameterNames(), a2.getParameterNames())) {
      err("different set of parameter names " +
      "a1=" + a1 + ", a2=" + a2);
    }

    Iterator iter = a1.getParameterNames();
    while (iter.hasNext()) {
      String paramName = (String) iter.next();
      String p1 = a1.getParameter(paramName);
      String p2 = a2.getParameter(paramName);
      if (!p1.equals(p2)) {
        err("parameters differ: a1=" + a1 + ", a2=" + a2 +
        ", param=" + paramName + ", p1=" + p1 + ", p2=" + p2);
      }
    }
  }

  /**
   * Returns true if the two Iterators return the same set of elements
   * as reported by Object.equals(). If the test is successful both
   * methods will have been exhausted.
   */
  public static boolean same(Iterator i1, Iterator i2) {
    LinkedList l = new LinkedList();
    while (i1.hasNext()) {
      l.add(i1.next());
    }

    while (i2.hasNext()) {
      if (!l.remove(i2.next())) {
        // i2 had elm not in i1
        return false;
      }
    }

    if (l.size() > 0) {
      // i1 had elm not in i2
      return false;
    }

    return true;
  }

  private static boolean exclEquals(Object o1, Object o2) {
    if (o1 == null) {
      return o2 == null;
    } else {
      return o1.equals(o2);
    }
  }

  private static void err(String msg) {
    throw new Err(msg);
  }
}
