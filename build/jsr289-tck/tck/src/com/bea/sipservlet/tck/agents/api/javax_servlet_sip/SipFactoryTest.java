/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved.
 * 
 * SipFactoryTest is used to test the APIs of 
 * javax.servlet.sip.SipFactory
 * 
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class SipFactoryTest extends TestBase {

	private static Logger logger = Logger.getLogger(SipFactoryTest.class);
  public SipFactoryTest(String arg0) throws IOException {
    super(arg0);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory1"},
      desc = "Returns a Address corresponding to the specified string.")
  public void testCreateAddress001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory1"},
      desc = "Checks ServletParseException thrown when createAddress() failed.")
  public void testCreateAddress101() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory1"},
      desc = "Returns a Address corresponding to the specified string when " 
      	+ "the argument is \"*\" .")
  public void testCreateAddress002() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory2"},
      desc = "Returns an Address with the specified URI and no display name.")
  public void testCreateAddress003() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory3"},
      desc = "Returns a new Address with the specified URI and display name.")
  public void testCreateAddress004() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory4"},
      desc = "Returns a new SipApplicationSession.")
  public void testCreateApplicationSession001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory5"},
      desc = "Returns a new SipApplicationSession belonging to a SIP application " 
      	+ "identified by a specific name. ")
  public void testCreateApplicationSessionByKey001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory6"},
      desc = "Creates a new AuthInfo object that can be used to provide "
      	+ "authentication information on servlet initiated requests.")
  public void testCreateAuthInfo001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory7"},
      desc = "Creates a new Parameterable parsed from the specified string. ")
  public void testCreateParameterable001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory8"},
      desc = "Returns a new request object with the specified request method, " 
      	+ "From, and To headers.")
  public void testCreateRequest001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory9"},
      desc = "Returns a new request object with the specified request method, " 
      	+ "From, and To headers.")
  public void testCreateRequest002() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory10"},
      desc = "Returns a new request object with the specified request method, " 
      	+ "From, and To headers. ")
  public void testCreateRequest003() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory11"},
      desc = "Deprecated.")
  public void testCreateRequest004() {
  	clientEntryLog();
  	logger.info("=== " 
  		+ "SipFactory.createRequest(SipServletRequest origRequest, boolean sameCallId) " 
  		+ "is deprecated. ===");
    // Deprecated. 
  	// Not neccessary to send SIP message out.
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory8"},
      desc = "Checks IllegalArgumentException thrown when " 
      	+ "createRequest(SipApplicationSession appSession, java.lang.String method, "
      	+ "Address from, Address to) failed.")
  public void testCreateRequest101() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory9"},
      desc = "Checks ServletParseException thrown when " 
      	+ "createRequest(SipApplicationSession appSession, java.lang.String method, "
      	+ "java.lang.String from, java.lang.String to) failed.")
  public void testCreateRequest102() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory9"},
      desc = "Checks IllegalArgumentException thrown when " 
      	+ "createRequest(SipApplicationSession appSession, java.lang.String method, "
      	+ "java.lang.String from, java.lang.String to) failed.")
  public void testCreateRequest103() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory10"},
      desc = "Checks IllegalArgumentException thrown when " 
      	+ "createRequest(SipApplicationSession appSession, java.lang.String method, "
      	+ "URI from, URI to) failed.")
  public void testCreateRequest104() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory12"},
      desc = "Constructs a SipURI with the specified user and host components.")
  public void testCreateSipURI001() {
    assertSipMessage();
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory13"},
      desc = "Returns a URI object corresponding to the specified string.")
  public void testCreateURI001() {
    assertSipMessage();
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:SipFactory13"},
      desc = "Checks ServletParseException thrown when createURI() failed.")
  public void testCreateURI101() {
    assertSipMessage();
  }
}
