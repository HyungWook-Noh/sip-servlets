/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * SipServletListenerTest is used to test the specification of 
 * SipServletListener
 */
package com.bea.sipservlet.tck.agents.spec;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestCaseNames;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipTransaction;

import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.text.ParseException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

@TargetApplication(value = ApplicationName.UAS)
public class SipServletListenerTest extends TestBase {
  private static Logger logger = Logger.getLogger(SipServletListenerTest.class);

  public SipServletListenerTest(String arg0) throws IOException {
    super(arg0);
  }
    
  
  /**     
   * 
   *   uac                              uas
   *    |                                |
   *    |----------  MESSAGE ----------->|
   *    |                                |
   *    |<---------  200/500 ------------|
   *    |                                |
   *    
   *    Success: if receive 200/MESSAGE
   *    Fail: if receive 500/MESSAGE
   */
  @AssertionIds(ids = { "SipServlet:SPEC:SipServletListener1" }, 
      desc = "The servletInitialized(ServletContextEvent context) callback" 
        + " will be invoked if SipServletListener present and the " 
        + "initialization of the Servlet is complete.")
  public void testSipServletListener() throws ParseException,
      InvalidArgumentException {
    clientEntryLog();
    // Build the MESSAGE Request message
    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put(TestConstants.SERVLET_HEADER,
        TestCaseNames.TEST_CASE_SIPSERVLETLISTENER);
    Request message = assembleEmptyRequest(Request.MESSAGE, 1, headers,
        TestConstants.SERVER_MODE_UA);

    // (1) UAC sends the MESSAGE Request message.
    SipTransaction trans = ua1.sendRequestWithTransaction(message, true, null);
    assertNotNull(ua1.format(), trans);

    // (2) UAC receives and assert the response.
    EventObject event = ua1.waitResponse(trans, waitDuration);
    assertNotNull(event);

    // Assert the response.
    if (event instanceof ResponseEvent) {
      ResponseEvent responseEvent = (ResponseEvent) event;
      Response response = responseEvent.getResponse();

      assertEquals(response.getReasonPhrase(),200,response.getStatusCode());      
    } else {
      fail("did not recieve response from server side");
    }
  }
}
