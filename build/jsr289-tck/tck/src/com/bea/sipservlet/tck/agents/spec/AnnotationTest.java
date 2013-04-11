/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * UasTest is used to test the specification of  UAS
 */
package com.bea.sipservlet.tck.agents.spec;

import java.io.IOException;

import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

@TargetApplication(value = ApplicationName.UAS)
public class AnnotationTest extends TestBase {

  public AnnotationTest(String arg0) throws IOException {
    super(arg0);
  }


  /**     
   * 
   *   uac                              uas
   *    |                                |
   *    |--------- (1) MESSAGE --------->|
   *    |                                |
   *    |<-------- (2) 200/500 ----------|
   *    |                                |
   *    
   *    Success: if receive 200/MESSAGE
   *    Fail: if receive 500/MESSAGE
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Annotation1" }, desc = "This case"
      + " checks if @PostConstruct and DI of SipSessionsUtil work.")
  public void testAnnotation() {
    clientEntryLog();

    Request message = assembleRequest(Request.MESSAGE, "Annotation", null,
        TestConstants.SERVER_MODE_UA, 1);
    SipTransaction ua1TX = ua1.sendRequestWithTransaction(message, true, null);
    assertNotNull(ua1.format(), ua1TX);
    assertLastOperationSuccess(ua1);

    Response response = waitResponseForMessage(ua1, ua1TX, waitDuration);
    assertNotNull(response);
    String reason = (response.getReasonPhrase() == null || response
        .getReasonPhrase().length() == 0) ? ""
        : "the reason gotten from Server:" + response.getReasonPhrase() + ". ";
    assertEquals(reason, Response.OK, response.getStatusCode());
  }
}
