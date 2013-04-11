/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.
 *  
 * ServletParseExceptionTest is used to test the APIs of 
 * javax.servlet.sip.ServletParseException.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;


public class ServletParseExceptionTest extends TestBase {
  public ServletParseExceptionTest(String arg0) throws IOException {
    super(arg0);
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ServletParseException1" }, 
      desc = "Construct a new parse exception, without any message.")
  public void testServletParseException001() {
    assertSipMessage();
  } 

  @AssertionIds(ids = { "SipServlet:JAVADOC:ServletParseException2" }, 
      desc = "Construct a new parse exception with the specified message.")
  public void testServletParseException002() {
    assertSipMessage();
  } 
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ServletParseException3" }, 
      desc = "Construct a new parse exception with the specified detail"
      + " message and cause.")
  public void testServletParseException003() {
    assertSipMessage();
  } 
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ServletParseException4" }, 
      desc = "Construct a new parse exception with the specified cause.")
  public void testServletParseException004() {
    assertSipMessage();
  } 
}
