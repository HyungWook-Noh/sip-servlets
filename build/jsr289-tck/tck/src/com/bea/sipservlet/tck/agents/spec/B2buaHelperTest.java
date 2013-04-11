/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * B2buaHelperTest is used to test the B2buaHelper functionality
 * introduced in JSR289.
 *
 */

package com.bea.sipservlet.tck.agents.spec;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipRequest;

import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@TargetApplication(ApplicationName.B2BUA)
public class B2buaHelperTest extends TestBase {

  private static final Logger logger = Logger.getLogger(B2buaHelperTest.class);

  public B2buaHelperTest(String arg0) throws IOException {
    super(arg0);
  }

  /**
   * For testB2buaHelper(), the call flow is:
   *
   * UA1                              SUT                               UA2
   *  |                                |                                |
   *  |-------- (1) INVITE ----------->|                                |
   *  |                                |                                |
   *  |                                |-------- (2) INVITE ----------->|
   *  |                                |                                |
   *  |                                |<------- (3) 200 OK ------------|
   *  |                                |                                |
   *  |<------- (4) 200 OK ------------|                                |
   *  |                                |                                |
   *  |-------- (5) ACK -------------->|                                |
   *  |                                |                                |
   *  |                                |-------- (6) ACK -------------->|
   *  |                                |                                |
   *  |-------- (7) INFO ------------->|                                |
   *  |                                |                                |
   *  |                                |-------- (8) INFO ------------->|
   *  |                                |                                |
   *  |                                |<------- (9) 200 OK ------------|
   *  |                                |                                |
   *  |<------- (10) 200 OK -----------|                                |
   *  |                                |                                |
   *  |-------- (11) INFO ------------>|                                |
   *  |                                |                                |
   *  |                                |-------- (12) INFO ------------>|
   *  |                                |                                |
   *  |                                |<------- (13) 200 OK -----------|
   *  |                                |                                |
   *  |<------- (14) 200 OK -----------|                                |
   *  |                                |                                |
   *  |                                |-------- (15) BYE ------------->|
   *  |                                |                                |
   *  |                                |<------- (16) 200 OK -----------|
   *  |                                |                                |
   *  |<------- (17) BYE --------------|                                |
   *  |                                |                                |
   *  |-------- (18) 200 OK ---------->|                                |
   *  |                                |                                |
   *
   * testB2buaHelper 
   * a. check SipServletRequest.getB2buaHelper() 
   * b. check B2buaHelper.createRequest(SipServletRequest origRequest,
   *    boolean linked, 
   *    java.util.Map<java.lang.String,java.util.List<java.lang.String>> headerMap)
   * c. check B2buaHelper.createRequest(SipSession session,
   *    SipServletRequest origRequest,
   *    java.util.Map<java.lang.String,java.util.List<java.lang.String>> headerMap)
   * d. check B2buaHelper.createResponseToOriginalRequest(SipSession session,
   *    int status, java.lang.String reasonPhrase) 
   * e. check B2buaHelper.linkSipSessions(SipSession session1, SipSession session2)
   * f. check B2buaHelper.unlinkSipSessions(SipSession session)
   * g. check B2buaHelper.getLinkedSession(SipSession session)
   * h. check B2buaHelper.getPendingMessages(SipSession session, UAMode mode)
   * i. check B2buaHelper.getLinkedSipServletRequest(SipServletRequest req)
   *
   */
  @AssertionIds(
      ids = {"SipServlet:SPEC:B2buaHelper1"},
      desc = "Make a simple call by B2buaHelper and send INFO messages in dialog.")
  public void testB2buaHelper(){
    clientEntryLog();

    SipCall callB = ua2.createSipCall();
    callB.listenForIncomingCall();

    // (1) UA1 make a outgoing call
    SipCall callA = ua1.createSipCall();
    List<Header> privateHeaders = new ArrayList<Header>(1);
    privateHeaders.add(getUa2UriHeader());
    initiateOutgoingCall(callA, null, null, privateHeaders, null);
    assertLastOperationSuccess("UA1 initiate call - " + callA.format(), callA);

    // (2) UA2 receives invite
    callB.waitForIncomingCall(waitDuration);
    assertLastOperationSuccess("UA2 wait for incoming call - " + callB.format(), callB);

    // (3) UA2 send 200/invite
    callB.sendIncomingCallResponse(Response.OK, null, -1);
    assertLastOperationSuccess("UA2 send 200 - " + callB.format(), callB);

    // (4) UA1 receives 200/invite
    do {
      callA.waitOutgoingCallResponse(waitDuration);
      assertLastOperationSuccess("UA1 wait for 200/INVITE - " + callA.format(), callA);
    } while (callA.getReturnCode() != Response.OK);

    // (5) UA1 send Ack
    callA.sendInviteOkAck();
    assertLastOperationSuccess("Failure sending ACK - " + callA.format(), callA);

    // (6) UA2 receive Ack
    callB.waitForAck(waitDuration);
    assertLastOperationSuccess("UA2 wait for ACK - " + callB.format(), callB);

    // (7) UA1 send Info
    //check B2buaHelper.unlinkSipSessions(SipSession session)
    //check B2buaHelper.getLinkedSession(SipSession session)
    callA.sendRequest(Request.INFO);
    assertLastOperationSuccess("UA1 send INFO - " + callA.format(), callA);

    // (8) UA2 receive INFO
    callB.waitForRequest(Request.INFO, waitDuration);
    assertLastOperationSuccess("UA2 wait for INFO - " + callB.format(), callB);
    SipRequest received_info = callB.getLastReceivedRequest();
    assertNotNull(received_info);
    assertEquals("Unexpected request", Request.INFO,
        ((Request)received_info.getMessage()).getMethod());

    // (9) UA2 send 200/INFO
    callB.sendResponseToLastReceivedRequest(Response.OK, "INFO Answer", 0);
    assertLastOperationSuccess("UA2 send 200/INFO" + callB.format(), callB);

    // (10) UA1 receive 200/INFO
    try {
      Thread.sleep(waitDuration);
    } catch (InterruptedException e) {
      logger.error("Exception in testB2buaHelper", e);
      throw new TckTestException(e);
    }
    Response okForInfo = (Response)callA.getLastReceivedResponse().getMessage();
    assertNotNull("Default response not sent", okForInfo);
    assertEquals("Unexpected default reason", Response.OK,
        okForInfo.getStatusCode());
    assertEquals("Unexpected default reason", Request.INFO,
        ((CSeqHeader)okForInfo.getHeader(CSeqHeader.NAME)).getMethod());

    // (11) UA1 send 2nd Info
    //check B2buaHelper.createRequest(SipSession session,
    //                                SipServletRequest origRequest,
    //                                Map<String,List<String>> headerMap)
    //check B2buaHelper.getLinkedSipServletRequest(SipServletRequest req)
    callA.sendRequest(Request.INFO);
    assertLastOperationSuccess("UA1 send 2nd INFO - " + callA.format(), callA);

    // (12) UA2 receive 2nd INFO
    callB.waitForRequest(Request.INFO, waitDuration);
    assertLastOperationSuccess("UA2 wait for INFO - " + callB.format(), callB);
    SipRequest received_info2 = callB.getLastReceivedRequest();
    assertNotNull(received_info2);
    assertEquals("Unexpected request", Request.INFO,
        ((Request)received_info2.getMessage()).getMethod());

    // (13) UA2 send 2nd 200/INFO
    callB.sendResponseToLastReceivedRequest(Response.OK, "INFO Answer", 0);
    assertLastOperationSuccess("UA2 send 2nd 200/INFO" + callB.format(), callB);

    // (14) UA1 receive 2nd 200/INFO
    try {
      Thread.sleep(waitDuration);
    } catch (InterruptedException e) {
      logger.error("Exception in testB2buaHelper", e);
      throw new TckTestException(e);
    }
    Response okForInfo2 = (Response)callA.getLastReceivedResponse().getMessage();
    assertNotNull("Default response not sent", okForInfo2);
    assertEquals("Unexpected default reason", Response.OK,
        okForInfo2.getStatusCode());
    assertEquals("Unexpected default reason", Request.INFO,
        ((CSeqHeader)okForInfo2.getHeader(CSeqHeader.NAME)).getMethod());

    // (15) UA2 receive BYE
    callB.waitForDisconnect(waitDuration);
    assertLastOperationSuccess("UA2 wait for BYE - " + callB.format(), callB);

    // (16) UA2 send 200/BYE
    callB.respondToDisconnect();
    assertLastOperationSuccess("UA2 respond to disc - " + callB.format(), callB);

    // (17) UA1 receive BYE
    callA.listenForDisconnect();
    assertLastOperationSuccess("UA1 listen disc - " + callA.format(), callA);
    callA.waitForDisconnect(waitDuration);
    assertLastOperationSuccess("UA1 wait for BYE - " + callA.format(), callA);

    // (18) UA1 send 200/BYE
    callA.respondToDisconnect();
    assertLastOperationSuccess("UA1 respond to disc - " + callA.format(), callA);

  }
    
  private Header getUa2UriHeader(){
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
    try {
      return header_factory.createHeader(TestConstants.PRIVATE_URI, ua2URI);
    } catch (ParseException e) {
      logger.error("*** ParseException when creating private header ***", e);
      throw new TckTestException(e);
    }
  }
}
