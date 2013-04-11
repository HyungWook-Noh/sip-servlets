/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * TelURLTest is used to test the APIs of 
 * javax.servlet.sip.TelURL
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class TelURLTest extends TestBase {
  public TelURLTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:TelURL1"},
      desc = "User agents can compare the given TelURL with this TelURL.")
  public void testEquals001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:TelURL2"},
      desc = "User agents can get the phone context of this TelURL for local "
        + "numbers or null if the phone number is global.")
  public void testGetPhoneContext001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:TelURL3",
          "SipServlet:JAVADOC:TelURL5",
          "SipServlet:JAVADOC:TelURL6"},
      desc = "User agents can get and set global/local phone number of this TelURL.")
  public void testGetSetPhoneNumber001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:TelURL4"},
      desc = "User agents can determine if this TelURL is global.")
  public void testIsGlobal001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:TelURL7"},
      desc = "User agents can get the String representation of this TelURL.")
  public void testToString001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = "SipServlet:JAVADOC:TelURL5",
      desc = "IllegalArgumentException should be thrown if the phone number " +
          "was invalid according to validation rules specified in RFC3966.")
  public void testSetPhoneNumber101() {
    assertSipMessage();
  }  
  
  @AssertionIds(
      ids = "SipServlet:JAVADOC:TelURL6",
      desc = "IllegalArgumentException should be thrown if the phone number " +
          "was invalid according to validation rules specified in RFC3966.")
  public void testSetPhoneNumber102() {
    assertSipMessage();
  }   
}


