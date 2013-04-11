/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * SipURITest is used to test the APIs of 
 * javax.servlet.sip.SipURI
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class SipURITest extends TestBase {
  public SipURITest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI1"},
      desc = "User agents can compare the given SipURI with this SipURI.")
  public void testEquals001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI2",
          "SipServlet:JAVADOC:SipURI15",
          "SipServlet:JAVADOC:SipURI16"},
      desc = "User agents can get, set and remove the value of the specified header.")
  public void testGetSetRemoveHeader001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI3"},
      desc = "User agents can get an Iterator over the names of all headers "
        + "present in this SipURI.")
  public void testGetHeaderNames001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI4",
          "SipServlet:JAVADOC:SipURI17"},
      desc = "User agents can get and set the host part of this SipURI.")
  public void testGetSetHost001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI5",
          "SipServlet:JAVADOC:SipURI18"},
      desc = "User agents can determine if Lr parameter is set.")
  public void testGetSetLrParam001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI6",
          "SipServlet:JAVADOC:SipURI19"},
      desc = "User agents can get the value of the 'maddr' parameter, or null "
        + "if this is not set.")
  public void testGetSetMAddrParam001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI7",
          "SipServlet:JAVADOC:SipURI20"},
      desc = "User agents can get and set the value of the 'method' parameter, or"
        + " null if this is not set.")
  public void testGetSetMethodParam001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI8",
          "SipServlet:JAVADOC:SipURI21"},
      desc = "User agents can get and set the port number of this SipURI, or -1 "
        + "if this is not set.")
  public void testGetSetPort001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI9",
          "SipServlet:JAVADOC:SipURI23"},
      desc = "User agents can get and set the value of the 'transport' parameter, "
        + "or null if this is not set.")
  public void testGetSetTransportParam001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI10",
          "SipServlet:JAVADOC:SipURI24"},
      desc = "User agents can get and set the value of the 'ttl' parameter, or -1 "
        + "if this is not set.")
  public void testGetSetTTLParam001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI11",
          "SipServlet:JAVADOC:SipURI25"},
      desc = "User agents can get and set the user part of this SipURI.")
  public void testGetSetUser001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI12",
          "SipServlet:JAVADOC:SipURI26"},
      desc = "User agents can get and set the value of the 'user' parameter, or " 
        + "null if this is not set.")
  public void testGetSetUserParam001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI13",
          "SipServlet:JAVADOC:SipURI27"},
      desc = "User agents can get and set the password of this SipURI, or null "
        + "if this is not set.")
  public void testGetSetUserPassword001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI14",
          "SipServlet:JAVADOC:SipURI22"},
      desc = "User agents can determine if this SipURI is secure, that is, if "
        + "this it represents a sips URI.")
  public void testIsSetSecure001() {
    assertSipMessage();
  }
 
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipURI28"},
      desc = "User agents can get the String representation of this SipURI.")
  public void testToString001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids =  {"SipServlet:JAVADOC:SipURI2"},
      desc = "NullPointerException should be thrown if the name is null.")
  public void testGetHeader101() {
    assertSipMessage();
  }
}
