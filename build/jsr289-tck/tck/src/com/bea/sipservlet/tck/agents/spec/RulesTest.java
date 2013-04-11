/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * RulesTest is used to test the specification of  Rules
 */
package com.bea.sipservlet.tck.agents.spec;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.EventObject;

import javax.sip.ResponseEvent;
import javax.sip.TimeoutEvent;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

@TargetApplication(value = ApplicationName.UAS)
public class RulesTest extends TestBase {
  public RulesTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }

  /**
   * check rule function about RULETEST1 method
   * 
   *                      UAC                                       UAS
   *                       |                                               |
   *                       |-------(1) RULETEST1---->|
   *                       |                                               |                                             
   *                       |<--------(2) 200 OK ---------|
   *                       |                                               |
   *                       
   */
  @AssertionIds(
      ids = { "SipServlet:SPEC:Rule1" }, 
      desc = "200 final response should be " 
        + "received if application executes defined rule")
  public void testRule1() {
    clientEntryLog();
    String methodName = "RULETEST1";
    String messageName = "RuleTest1";
    ruleLogic(methodName, messageName);
  }

  /**
   * check rule function about RULETEST2 method
   * 
   *                      UAC                                       UAS
   *                       |                                               |
   *                       |-------(1) RULETEST2---->|
   *                       |                                               |                                             
   *                       |<--------(2) 200 OK ---------|
   *                       |                                               |
   *                       
   */
  @AssertionIds(
      ids = { "SipServlet:SPEC:Rule2" }, 
      desc = "200 final response should be " 
        + "received if application executes defined rule")
  public void testRule2() {
    clientEntryLog();
    String methodName = "RULETEST2";
    String messageName = "RuleTest2";
    ruleLogic(methodName, messageName);
  }

  /**
   * check rule function about RULETEST3 method
   * 
   *                      UAC                                       UAS
   *                       |                                               |
   *                       |-------(1) RULETEST3---->|
   *                       |                                               |                                             
   *                       |<--------(2) 200 OK ---------|
   *                       |                                               |
   *                       
   */
  @AssertionIds(
      ids = { "SipServlet:SPEC:Rule3" }, 
      desc = "200 final response should be " 
        + "received if application executes defined rule")
  public void testRule3() {
    clientEntryLog();
    String methodName = "RULETEST3";
    String messageName = "RuleTest3";
    ruleLogic(methodName, messageName);
  }

  private void ruleLogic(String methodName, String messageName) {
    try {
      Request message = assembleRequest(Request.MESSAGE, messageName, null,
          TestConstants.SERVER_MODE_UA, 1);
      message.setMethod(methodName);
      SipTransaction trans = ua1
          .sendRequestWithTransaction(message, true, null);
      assertNotNull(ua1.format(), trans);
      assertLastOperationSuccess(ua1);
      Thread.sleep(waitDuration / 5);
      EventObject waitResponse = ua1.waitResponse(trans, waitDuration);
      assertFalse("Operation timed out", waitResponse instanceof TimeoutEvent);
      assertEquals("Should have received OK", Response.OK,
          ((ResponseEvent) waitResponse).getResponse().getStatusCode());
    } catch (Exception e) {
      fail("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }
  }
}
