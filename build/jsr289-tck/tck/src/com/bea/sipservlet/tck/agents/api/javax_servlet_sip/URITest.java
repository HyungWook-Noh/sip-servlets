/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * URITest is used to test the APIs of 
 * javax.servlet.sip.URI
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class URITest extends TestBase {
  public URITest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }
  
  @AssertionIds(
	      ids = {"SipServlet:JAVADOC:URI1"},
	      desc = "User agents can get a clone of this URI.")
	  public void testClone001() {
	    assertSipMessage();
	  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:URI2"},
      desc = "User agents can compare the given URI with this URI.")
  public void testEquals001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:URI3",
          "SipServlet:JAVADOC:URI7",
          "SipServlet:JAVADOC:URI8"},
      desc = "User agents can get, set and remove a parameter of this URI.")
  public void testGetSetRemoveParameter001() {
    assertSipMessage();
  }  

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:URI4"},
      desc = "User agents can get an Iterator over the names of all parameters"
        + "present in this URI.")
  public void testGetParameterNames001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:URI5"},
      desc = "User agents can get the scheme of this URI, for example \"sip\", " 
        + "\"sips\" or \"tel\".")
  public void testGetScheme001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:URI6"},
      desc = "User agents can determine if this URI is a SipURI.")
  public void testIsSipURI001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:URI9"},
      desc = "User agents can get the String representation of this URI.")
  public void testToString001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:URI3"},
      desc = "NullPointerException should be thrown if the key is null.")
  public void testGetParameter101() {
    assertSipMessage();
  }   
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:URI7"},
      desc = "NullPointerException should be thrown on eithet name or value being null.")
  public void testSetParameter101() {
    assertSipMessage();
  }   
}
