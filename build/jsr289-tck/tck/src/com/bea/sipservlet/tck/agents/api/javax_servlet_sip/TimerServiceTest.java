/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * TimerServiceTest is used to test the APIs of 
 * javax.servlet.sip.TimerService
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.sip.message.Request;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class TimerServiceTest extends TestBase {
  public TimerServiceTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:TimerListener1",
          "SipServlet:JAVADOC:TimerService1"},
      desc = "User agents can create a one-time ServletTimer and schedules it " 
      	+ "to expire after the specified delay.")
  public void testCreateTimer001() {
  	assertSipMessageBiWay(null, "SipErrorEventListener", "testCreateTimer001", 1);;
  }

  @AssertionIds(
      ids = {"SipServlet:JAVADOC:TimerService2"},
      desc = "User agents can create a repeating ServletTimer and schedules it " 
      	+ "to expire after the specified delay and then again at approximately " 
      	+ "regular intervals.")
  public void testCreateTimer002() {
  	assertSipMessageBiWay(null, "SipErrorEventListener", "testCreateTimer002", 1);
  }
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:TimerListener1",
          "SipServlet:JAVADOC:TimerService1"},
      desc = "IllegalStateException should be thrown if the application " +
          "session is invalid.")
  public void testCreateTimer101() {
    assertSipMessage(null, "SipErrorEventListener", "testCreateTimer101", 
        Request.MESSAGE, 1);
  } 
  
  @AssertionIds(
      ids = {"SipServlet:JAVADOC:TimerService2"},
      desc = "IllegalStateException should be thrown if the application " +
          "session is invalid.")
  public void testCreateTimer102() {
    assertSipMessage(null, "SipErrorEventListener", "testCreateTimer102", 
        Request.MESSAGE, 1);
  }  
}


