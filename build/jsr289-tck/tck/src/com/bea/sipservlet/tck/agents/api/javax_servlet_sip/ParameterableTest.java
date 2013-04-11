/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * ParameterableTest is used to test the APIs of 
 * javax.servlet.sip.Parameterable
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class ParameterableTest extends TestBase {

  public ParameterableTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable1"},
      desc = "User agents can get a clone of this Parameterable.")
  public void testClone001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable2"},
      desc = "User agents can compares the given Parameterable type with this one.")
  public void testEquals001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable3",
          "SipServlet:JAVADOC:Parameterable7",
          "SipServlet:JAVADOC:Parameterable8"},
      desc = "User agents can get, set and remove a parameter of this Parameterable.")
  public void testGetSetRemoveParameter001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable3"},
      desc = "Checks NullPointerException thrown when getParameter() failed.")
  public void testGetParameter101() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable8"},
      desc = "Checks IllegalStateException thrown when setParameter() failed.")
  public void testSetParameter101() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable8"},
      desc = "Checks NullPointerException thrown when setParameter() failed.")
  public void testSetParameter102() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable7"},
      desc = "Checks IllegalStateException thrown when removeParameter() failed.")
  public void testRemoveParameter101() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable7"},
      desc = "Checks NullPointerException thrown when removeParameter() failed.")
  public void testRemoveParameter102() {
    assertSipMessage();
  }
  
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable4"},
      desc = "User agents can get an Iterator of the names of all parameters contained"
        + " in this object.")
  public void testGetParameterNames001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable5"},
      desc = "User agents can get a Collection view of the parameter name-value "
        + "mappings contained in this Parameterable.")
  public void testGetParameters001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable6",
          "SipServlet:JAVADOC:Parameterable9"},
      desc = "User agents can get and set the field value of this Parameterable.")
  public void testGetSetValue001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable9"},
      desc = "Checks IllegalStateException thrown when setValue() failed.")
  public void testSetValue101() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:Parameterable9"},
      desc = "Checks NullPointerException thrown when setValue() failed.")
  public void testSetValue102() {
    assertSipMessage();
  }
  
}
