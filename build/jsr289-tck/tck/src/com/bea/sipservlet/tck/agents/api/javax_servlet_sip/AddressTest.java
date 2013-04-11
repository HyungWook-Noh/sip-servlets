/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * AddressTest is used to test the APIs of 
 * javax.servlet.sip.Address
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class AddressTest extends TestBase {
  public AddressTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }
  
  @AssertionIds(
      ids = { "SipServlet:JAVADOC:Address1" }, 
      desc = "User agents can get a clone of this Address.")
  public void testClone001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Address2"},
      desc = "User agents can compare the given Address with this Address.")
  public void testEquals001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Address3",
          "SipServlet:JAVADOC:Address8"},
      desc = "User agents can get and set 'DisplayName' parameter of this Address.")
  public void testGetSetDisplayName001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Address4",
          "SipServlet:JAVADOC:Address9"},
      desc = "User agents can get and set 'Expires' parameter of this Address.")
  public void testGetSetExpires001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Address5",
          "SipServlet:JAVADOC:Address10"},
      desc = "User agents can get and set 'q' parameter of this Address.")
  public void testGetSetQ001() {
    assertSipMessage();
  }

  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Address6",
          "SipServlet:JAVADOC:Address11"},
      desc = "User agents can get and set 'URI' of this Address.")
  public void testGetSetURI001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Address7"},
      desc = "User agents can determine if this Address represents the 'wildcard' "
        + "contact address.")
  public void testIsWildcard001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Address12"},
      desc = "User agents can get the String representation of this Address.")
  public void testToString001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Address8"},
      desc = "IllegalStateException should be thrown if this Address is used in a context where it cannot be modified")
  public void testSetDisplayName101() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Address11"},
      desc = "IllegalStateException should be thrown if this Address is used in a context where it cannot be modified.") 
  public void testSetURI101() {
    assertSipMessage();
  }  

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Address11"},
      desc = "NullPointerException be thrown on null uri.")   
  public void testSetURI102() {
    assertSipMessage();
  }  
  
  @AssertionIds(
    ids = "SipServlet:JAVADOC:Address10",
    desc = "IllegalArgumentException should be thrown if the new qvalue isn't between 0.0 and 1.0 (inclusive) and isn't -1.0.")
  public void testSetQ101() {
    assertSipMessage();
  }  
}
