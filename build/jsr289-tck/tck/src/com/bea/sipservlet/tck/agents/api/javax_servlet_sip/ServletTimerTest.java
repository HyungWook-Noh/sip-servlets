/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved. 
 *  
 * ServletTimerTest is used to test the APIs of javax.servlet.sip.ServletTimer.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;
import java.text.ParseException;
import java.util.EventObject;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.header.Header;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class ServletTimerTest extends TestBase {
  
  private static final Logger logger = Logger.getLogger(ServletTimerTest.class);
  
  public ServletTimerTest(String arg0) throws IOException {
    super(arg0);
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ServletTimer1" }, 
      desc = "Cancel this timer.")
  public void testCancel001() {
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ServletTimer2" }, 
      desc = "Return the application session associated with this ServletTimer.")
  public void testGetApplicationSession001() {
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ServletTimer3" }, 
      desc = "Return a string containing the unique identifier assigned to " 
        + "this timer task. The identifier is assigned by the servlet container " 
        + "and is implementation dependent.")
  public void testGetId001(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ServletTimer4" }, 
      desc = "Get the information associated with the timer at the time of creation.")
  public void testGetInfo001(){
    assertSipMessage();
  }

 
  /**     
   * 
   *   UAC                                 UAS
   *    |                                   |
   *    |----------  (1)MESSAGE ----------->|
   *    |                                   |
   *    |<-----------(2)200 ----------------|
   *    |                                   |
   *    |<---------  (3)MESSAGE ------------|
   *    |                                   |
   *  <Check>                               |
   *    |                                   |
   *    |----------  (4)200/MESSAGE-------->|
   *    |                                   |
   *    
   */   

  @AssertionIds(ids = { "SipServlet:JAVADOC:ServletTimer5" }, 
      desc = "Test getTimeRemaining for a repeating ServletTimer")
  public void testGetTimeRemaining001() throws ParseException, SipException,
      InvalidArgumentException {
    clientEntryLog();
    // UAC begin listen for request
    ua1.listenRequestMessage();

    // Build the Request message
    Request req = assembleRequest("MESSAGE", "ServletTimer",
        "testGetTimeRemaining001", TestConstants.SERVER_MODE_UA, 1);
    // (1) UAC sends the MESSAGE Request message
    SipTransaction trans = ua1.sendRequestWithTransaction(req, true, null);
    assertNotNull(ua1.format(), trans);
    logger.debug("---UAC send MESSAGE req is:" + req + "---");

    // (2) UAC receives 200/MESSAGE response
    
    EventObject event = ua1.waitResponse(trans, waitDuration);
    if(event == null){
      logger.warn("*** 200 ok of MESSAGE is not received, but case will continue***");
    }


    // (3) UAC receives MESSAGE request
    RequestEvent eventUA1 = ua1.waitRequest(waitDuration);
    assertNotNull(eventUA1);
    Request messageReq = eventUA1.getRequest();
    if (!Request.MESSAGE.equals(messageReq.getMethod())) {
      fail("The request UAC received is not MESSAGE, but"
          + messageReq.getMethod());
    }
    logger.debug("---UA2 receive the MESSAGE req is:" + messageReq + "---");
    
    //  (4)UAC sends back 200/MESSAGE    
    ServerTransaction serverTransUAC = ua1.getParent().getSipProvider()
        .getNewServerTransaction(messageReq);
    Response msg200Resp = createResponse(messageReq, ua1, 200, ua1
        .generateNewTag());
    logger.debug("--- UAC send 200 Response is:" + msg200Resp.toString()
        + " ---");
    serverTransUAC.sendResponse(msg200Resp);
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ServletTimer5" }, 
      desc = "For a one-time timer that has already expired (i.e., "
      + "current time > scheduled expiry time) this method will return "
      + "the time remaining as a negative value. ")
  public void testGetTimeRemaining002(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:ServletTimer6" }, 
      desc = "Return the scheduled expiration time of the most recent actual "
      + "expiration of this timer.")
  public void testScheduledExecutionTime001(){
    assertSipMessage();
  }
}
