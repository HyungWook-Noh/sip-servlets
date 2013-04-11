/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * ProxyTest is used to test the specification of proxying.
 */

package com.bea.sipservlet.tck.agents.spec;

import gov.nist.javax.sip.header.SIPHeader;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.TimeoutEvent;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipMessage;
import org.cafesip.sipunit.SipRequest;
import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

@TargetApplication(ApplicationName.PROXY)
public class ProxyTest extends TestBase {
  private static Logger logger = Logger.getLogger(ProxyTest.class);

  private static final String SERVLET_NAME = "Proxy";

  /**
   * header to store proxy options
   */
  private static final String HEADER_TCK_PROXY_OPTIONS = "TCK-Proxy-Options";
  /**
   * header to store proxy dest URIs,
   */
  private static final String HEADER_TCK_PROXY_DEST = "TCK-Proxy-Dest";

  public ProxyTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }

  private void logCaseStep(int step) {
    logger.debug("---  Step " + step + "  ---");
  }

  /**
   * <p>
   * Tests recursion in proxy applications. Recursion is enabled (it's enabled
   * by default), app proxies Alice INVITE to Bob, Bob sends 302 redirect with
   * Carol address in a Contact header, the container recurses and proxies the
   * INVITE to Carol who accepts. Alice subsequently sends BYE. For good measure
   * the application also record-routes.
   * </p>
   * 
   * <pre>
   *   UA1           proxy           UA2            UA3
   *   |              |              |              |
   *   |(1) INVITE    |              |              |
   *   |------------->|              |              |
   *   |              |(2) INVITE    |              |
   *   |              |------------->|              |
   *   |              |(3) 302       |              |
   *   |              |<-------------|              |
   *   |              |(4) ACK       |              |
   *   |              |------------->|              |
   *   |              |(5) INVITE    |              |
   *   |              |---------------------------->|
   *   |              |(6) 200       |              |
   *   |              |<----------------------------|
   *   |(7) 200       |              |              |
   *   |<-------------|              |              |
   *   |(8) ACK       |              |              |
   *   |------------->|              |              |
   *   |              |(9) ACK       |              |
   *   |              |---------------------------->|
   *   |(10) BYE      |              |              |
   *   |------------->|              |              |
   *   |              |(11) BYE      |              |
   *   |              |---------------------------->|
   *   |              |(12) 200      |              |
   *   |              |<----------------------------|
   *   |(13) 200      |              |              |
   *   |<-------------|              |              |
   *   |              |              |              |
   * </pre>
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Proxy1" }, 
      desc = "Tests recursion in proxy applications.")
  public void testProxyRecurseOn() throws SipException, ParseException,
      InvalidArgumentException, InterruptedException {
    clientEntryLog();

    // (0) preparation
    SipCall ua1Call = ua1.createSipCall();
    SipCall ua2Call = ua2.createSipCall();
    SipCall ua3Call = ua3.createSipCall();
    ua2Call.listenForIncomingCall();
    ua3Call.listenForIncomingCall();

    // (1) UA1 send invite
    {
      logCaseStep(1);
      List<Header> headers = new ArrayList<Header>();
      HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
      // proxy option header
      headers.add(headerFactory.createHeader(HEADER_TCK_PROXY_OPTIONS,
          "recordRoute=true"));
      // proxy dest header
      headers.add(headerFactory.createHeader(HEADER_TCK_PROXY_DEST, ua2
          .getContactInfo().getURI()));

      boolean status_ok = initiateOutgoingCall(ua1Call, null, SERVLET_NAME,
          headers, null, null);
      assertTrue("Initiate outgoing call failed - " + ua1Call.format(),
          status_ok);
    }
    ua1Call.waitOutgoingCallResponse(waitDuration); // get next response
    assertLastOperationSuccess("Wait response error - " + ua1Call.format(),
        ua1Call);

    // (2) UA2 receive invite
    {
      logCaseStep(2);
      boolean getCall = ua2Call.waitForIncomingCall(waitDuration * 2);
      SipRequest req = ua2Call.getLastReceivedRequest();
      assertNotNull(req);
      assertTrue(req.isInvite());
    }

    // (3) UA2 send 302
    {
      logCaseStep(3);
      HeaderFactory headerFactory = ua2.getParent().getHeaderFactory();
      ArrayList<Header> headers = new ArrayList<Header>();
      // proxy option header
      headers.add(headerFactory.createContactHeader(ua2.getParent()
          .getAddressFactory().createAddress(ua3.getContactInfo().getURI())));

      ua2Call.sendIncomingCallResponse(Response.MOVED_TEMPORARILY, null,
          waitDuration, null, headers, null);
    }
    // (4) UA2 receive ACK
    {
      logCaseStep(4);
      // Not necessary to assert ACK
      // because ACK received for ServerTransaction not delivering to
      // application by Jain sip.
      // assertTrue(ua2Call.waitForAck(waitDuration * 2));
      // SipRequest req = ua2Call.getLastReceivedRequest();
      // assertTrue(req.isAck());
    }

    // (5) UA3 receive invite
    {
      logCaseStep(5);
      boolean getCall = ua3Call.waitForIncomingCall(waitDuration * 2);
      SipRequest req = ua3Call.getLastReceivedRequest();
      assertNotNull(req);
      assertTrue(req.isInvite());
    }

    // (6) UA3 send 200 ok
    {
      logCaseStep(6);
      HeaderFactory headerFactory = ua3.getParent().getHeaderFactory();
      // proxy option header
      ArrayList<Header> additionalHeaders = new ArrayList<Header>();
      additionalHeaders.add(headerFactory.createHeader(
          HEADER_TCK_PROXY_OPTIONS, "recordRoute=true"));

      ua3Call.sendIncomingCallResponse(Response.OK, null, waitDuration,
          additionalHeaders, null, null);
      assertLastOperationSuccess("Failure sending ACK - " + ua3Call.format(),
          ua3Call);
    }

    // (7) UA1 receive 200 ok
    {
      logCaseStep(7);
      while (ua1Call.getReturnCode() == Response.TRYING) {
        ua1Call.waitOutgoingCallResponse(waitDuration);
        assertLastOperationSuccess("Subsequent response never received - "
            + ua1Call.format(), ua1Call);
      }
      SipResponse resp = ua1Call.getLastReceivedResponse();
      assertTrue(resp.getStatusCode() == Response.OK);
    }

    // (8) UA1 send ACK
    {
      logCaseStep(8);
      ua3Call.listenForRequest();
      ua1Call.sendInviteOkAck();
      assertLastOperationSuccess("Failure sending ACK - " + ua1Call.format(),
          ua1Call);
    }

    // (9) UA3 receive ACK
    {
      logCaseStep(9);
      // Not necessary to assert ACK
      // because ACK received for ServerTransaction not delivering to
      // application by Jain sip.
      assertTrue(ua3Call.waitForAck(waitDuration));
      SipRequest req = ua3Call.getLastReceivedRequest();
      assertTrue(req.isAck());
    }

    // (10) UA1 send bye
    {
      logCaseStep(10);
      ua3Call.listenForDisconnect();
      assertTrue(ua1Call.disconnect());
      assertLastOperationSuccess("a disc - " + ua1Call.format(), ua1Call);
    }

    // (11) us3 receive bye
    {
      logCaseStep(11);
      ua3Call.waitForDisconnect(waitDuration);
      SipRequest req = ua3Call.getLastReceivedRequest();
      assertNotNull(req);
      assertTrue(req.isBye());
    }

    // (12) UA3 send 200 ok
    {
      logCaseStep(12);
      ua3Call.respondToDisconnect();
      assertLastOperationSuccess("Failure sending ACK - " + ua3Call.format(),
          ua3Call);
    }

    // (13) UA1 receive 200 ok
    {
      logCaseStep(13);
      Thread.sleep(waitDuration / 2);
      assertResponseReceived("Did not receive 200 OK on BYE", Response.OK,
          Request.BYE, 2, ua1Call);
    }
  }

  /**
   * <p>
   * Tests ability of proxy applications to proxy to add URIs to the proxy
   * target set in the best-response callback if the best response is not a 2xx.
   * In this case the app proxies to uas1 with recursion disabled, uas1 returns
   * a 302 with a single SIP Contact header field value. The app is invoked with
   * the 302 as the best response, and at that point adds the Contact URI as a
   * new proxy target. The container proxies, uas2 accepts and the 200 is
   * subsequently forwarded upstream to the uac.
   * </p>
   * <p>
   * This testcase is the same as proxy-recurse-on, except recursion in this
   * case is handled by the application itself. The call flow is identical.
   * </p>
   * 
   * <pre>
   *   UA1           proxy           UA2            UA3
   *   |              |              |              |
   *   |(1) INVITE    |              |              |
   *   |------------->|              |              |
   *   |              |(2) INVITE    |              |
   *   |              |------------->|              |
   *   |              |(3) 302       |              |
   *   |              |<-------------|              |
   *   |              |(4) ACK       |              |
   *   |              |------------->|              |
   *   |              |(5) INVITE    |              |
   *   |              |---------------------------->|
   *   |              |(6) 200       |              |
   *   |              |<----------------------------|
   *   |(7) 200       |              |              |
   *   |<-------------|              |              |
   *   |(8) ACK       |              |              |
   *   |------------->|              |              |
   *   |              |(9) ACK       |              |
   *   |              |---------------------------->|
   *   |(10) BYE      |              |              |
   *   |------------->|              |              |
   *   |              |(11) BYE      |              |
   *   |              |---------------------------->|
   *   |              |(12) 200      |              |
   *   |              |<----------------------------|
   *   |(13) 200      |              |              |
   *   |<-------------|              |              |
   *   |              |              |              |
   * </pre>
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Proxy2" }, 
      desc = "Tests ability of proxy applications to proxy to add URIs to the "
      + "proxy target set in the best-response callback if the best response "
      + "is not a 2xx")
  public void testProxyRecurseApp() throws SipException, ParseException,
      InvalidArgumentException, InterruptedException {
    clientEntryLog();
    // (0) preparation
    SipCall ua1Call = ua1.createSipCall();
    SipCall ua2Call = ua2.createSipCall();
    SipCall ua3Call = ua3.createSipCall();
    ua2Call.listenForIncomingCall();
    ua3Call.listenForIncomingCall();

    // (1) UA1 send invite
    {
      logCaseStep(1);
      List<Header> headers = new ArrayList<Header>();
      HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
      // proxy option header
      headers.add(headerFactory.createHeader(HEADER_TCK_PROXY_OPTIONS,
          "recordRoute=true recurse=false"));
      // proxy dest header
      headers.add(headerFactory.createHeader(HEADER_TCK_PROXY_DEST, ua2
          .getContactInfo().getURI()));

      String toUri = "sip:proxy-recurse-app@" + this.serverHost + ":"
          + this.serverPort;

      boolean status_ok = initiateOutgoingCall(ua1Call, null, SERVLET_NAME,
          headers, null, toUri);
      assertTrue("Initiate outgoing call failed - " + ua1Call.format(),
          status_ok);
    }
    ua1Call.waitOutgoingCallResponse(waitDuration); // get next response
    assertLastOperationSuccess("Wait response error - " + ua1Call.format(),
        ua1Call);

    // (2) UA2 receive invite
    {
      logCaseStep(2);
      boolean getCall = ua2Call.waitForIncomingCall(waitDuration * 2);
      SipRequest req = ua2Call.getLastReceivedRequest();
      assertNotNull(req);
      assertTrue(req.isInvite());
    }

    // (3) UA2 send 302
    {
      logCaseStep(3);
      HeaderFactory headerFactory = ua2.getParent().getHeaderFactory();
      ArrayList<Header> headers = new ArrayList<Header>();
      // proxy option header
      headers.add(headerFactory.createContactHeader(ua2.getParent()
          .getAddressFactory().createAddress(ua3.getContactInfo().getURI())));

      ua2Call.sendIncomingCallResponse(Response.MOVED_TEMPORARILY, null,
          waitDuration, null, headers, null);
    }
    // (4) UA2 receive ACK
    {
      logCaseStep(4);
      // Not necessary to assert ACK
      // because ACK received for ServerTransaction not delivering to
      // application by Jain sip.
      // assertTrue(ua2Call.waitForAck(waitDuration * 2));
      // SipRequest req = ua2Call.getLastReceivedRequest();
      // assertTrue(req.isAck());
    }

    // (5) UA3 receive invite
    {
      logCaseStep(5);
      boolean getCall = ua3Call.waitForIncomingCall(waitDuration * 2);
      SipRequest req = ua3Call.getLastReceivedRequest();
      assertNotNull(req);
      assertTrue(req.isInvite());
    }

    // (6) UA3 send 200 ok
    {
      logCaseStep(6);
      HeaderFactory headerFactory = ua3.getParent().getHeaderFactory();
      // proxy option header
      ArrayList<Header> additionalHeaders = new ArrayList<Header>();
      additionalHeaders.add(headerFactory.createHeader(
          HEADER_TCK_PROXY_OPTIONS, "recordRoute=true"));

      ua3Call.sendIncomingCallResponse(Response.OK, null, waitDuration,
          additionalHeaders, null, null);
      assertLastOperationSuccess("Failure sending ACK - " + ua3Call.format(),
          ua3Call);
    }

    // (7) UA1 receive 200 ok
    {
      logCaseStep(7);
      while (ua1Call.getReturnCode() == Response.TRYING) {
        ua1Call.waitOutgoingCallResponse(waitDuration);
        assertLastOperationSuccess("Subsequent response never received - "
            + ua1Call.format(), ua1Call);
      }
      SipResponse resp = ua1Call.getLastReceivedResponse();
      assertTrue(resp.getStatusCode() == Response.OK);
    }

    // (8) UA1 send ACK
    {
      logCaseStep(8);
      ua3Call.listenForRequest();
      ua1Call.sendInviteOkAck();
      assertLastOperationSuccess("Failure sending ACK - " + ua1Call.format(),
          ua1Call);
    }

    // (9) UA3 receive ACK
    {
      logCaseStep(9);
      // Not necessary to assert ACK
      // because ACK received for ServerTransaction not delivering to
      // application by Jain sip.
      assertTrue(ua3Call.waitForAck(waitDuration));
      SipRequest req = ua3Call.getLastReceivedRequest();
      assertTrue(req.isAck());
    }

    // (10) UA1 send bye
    {
      logCaseStep(10);
      ua3Call.listenForDisconnect();
      assertTrue(ua1Call.disconnect());
      assertLastOperationSuccess("a disc - " + ua1Call.format(), ua1Call);
    }

    // (11) us3 receive bye
    {
      logCaseStep(11);
      ua3Call.waitForDisconnect(waitDuration);
      SipRequest req = ua3Call.getLastReceivedRequest();
      assertNotNull(req);
      assertTrue(req.isBye());
    }

    // (12) UA3 send 200 ok
    {
      logCaseStep(12);
      ua3Call.respondToDisconnect();
      assertLastOperationSuccess("Failure sending ACK - " + ua3Call.format(),
          ua3Call);
    }

    // (13) UA1 receive 200 ok
    {
      logCaseStep(13);
      Thread.sleep(waitDuration / 2);
      assertResponseReceived("Did not receive 200 OK on BYE", Response.OK,
          Request.BYE, 2, ua1Call);
    }
  }

  /**
   * Tests recursion. Recursion is disabled. uas1 sends 302 redirect and this
   * should be forwarded upstream to the caller. Call flow or proxy-recurse-on
   * 
   * <pre>
   *   UA1           proxy           UA2            UA3
   *   |              |              |              |
   *   |(1) INVITE    |              |              |
   *   |------------->|              |              |
   *   |              |(2) INVITE    |              |
   *   |              |------------->|              |
   *   |              |(3) 302       |              |
   *   |              |<-------------|              |
   *   |              |(4) ACK       |              |
   *   |              |------------->|              |
   *   |(5) 302       |              |              |
   *   |<-------------|              |              |
   *   |(6) ACK       |              |              |
   *   |------------->|              |              |
   *   |              |              |              |
   * </pre>
   * 
   * @throws ParseException
   * @throws InvalidArgumentException
   * @throws SipException
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Proxy3" }, 
      desc = "Tests recursion. Recursion is disabled.")
  public void testProxyRecurseOff() throws ParseException, SipException,
      InvalidArgumentException {
    clientEntryLog();
    // (0) preparation
    SipCall ua1Call = ua1.createSipCall();
    SipCall ua2Call = ua2.createSipCall();
    SipCall ua3Call = ua3.createSipCall();
    ua2Call.listenForIncomingCall();
    ua3Call.listenForIncomingCall();

    // (1) UA1 send invite
    {
      logCaseStep(1);
      List<Header> headers = new ArrayList<Header>();
      HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
      // proxy option header
      headers.add(headerFactory.createHeader(HEADER_TCK_PROXY_OPTIONS,
          "recurse=false"));
      // proxy dest header
      headers.add(headerFactory.createHeader(HEADER_TCK_PROXY_DEST, ua2
          .getContactInfo().getURI()));

      boolean status_ok = initiateOutgoingCall(ua1Call, null, SERVLET_NAME,
          headers, null, null);
      assertTrue("Initiate outgoing call failed - " + ua1Call.format(),
          status_ok);
    }
    ua1Call.waitOutgoingCallResponse(waitDuration); // get next response
    assertLastOperationSuccess("Wait response error - " + ua1Call.format(),
        ua1Call);

    // (2) UA2 receive invite
    {
      logCaseStep(2);
      boolean getCall = ua2Call.waitForIncomingCall(waitDuration * 2);
      SipRequest inviteReq = ua2Call.getLastReceivedRequest();
      assertNotNull(inviteReq);
      assertTrue(inviteReq.isInvite());
    }

    // (3) UA2 send 302
    {
      logCaseStep(3);
      HeaderFactory headerFactory = ua2.getParent().getHeaderFactory();
      ArrayList<Header> headers = new ArrayList<Header>();
      // proxy option header
      headers.add(headerFactory.createContactHeader(ua2.getParent()
          .getAddressFactory().createAddress(ua3.getContactInfo().getURI())));

      ua2Call.listenForAck();
      ua2Call.sendIncomingCallResponse(Response.MOVED_TEMPORARILY, null,
          waitDuration, null, headers, null);
    }
    // (4) UA2 receive ACK
    {
      logCaseStep(4);
      // Not necessary to assert ACK
      // because ACK received for ServerTransaction not delivering to
      // application by Jain sip.
      // ua2Call.waitForAck(waitDuration);
      // SipRequest req = ua2Call.getLastReceivedRequest();
      // assertTrue(req.isAck());
    }

    // (5) UA1 receive 302
    {
      logCaseStep(5);
      while (ua1Call.getReturnCode() == Response.TRYING) {
        ua1Call.waitOutgoingCallResponse(waitDuration);
        assertLastOperationSuccess("Subsequent response never received - "
            + ua1Call.format(), ua1Call);
      }
      assertResponseReceived("Unexpected response received",
          Response.MOVED_TEMPORARILY, ua1Call);
    }

    // (6) UA1 send ACK
    {
      logCaseStep(6);
      // ACK is sent by Jain sip
      // ua1Call.sendInviteErrorAck();
      // assertLastOperationSuccess("Failure sending ACK - " + ua1Call.format(),
      // ua1Call);
    }
  }

  /**
   * Tests cancellation of proxy operation in progress.
   * 
   * <pre>
   *  UA1           proxy           UA2
   *   |              |              |
   *   |(1) INVITE    |              |
   *   |------------->|              |
   *   |              |(2) INVITE    |
   *   |              |------------->|
   *   |(3) CANCEL    |              |
   *   |------------->|              |
   *   |(4) 200 CANCEL|              |
   *   |<-------------|              |
   *   |              |(5) CANCEL    |
   *   |              |------------->|
   *   |              |(6) 200 CANCEL|
   *   |              |<-------------|
   *   |              |(7) 487 INVITE|
   *   |              |<-------------|
   *   |              |(8) ACK       |
   *   |              |------------->|
   *   |(9) 487 INVITE|              |
   *   |<-------------|              |
   *   |(10) ACK      |              |
   *   |------------->|              |
   *   |              |              |
   * </pre>
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Proxy4" }, 
      desc = "Tests cancellation of proxy operation in progress.")
  public void testProxyCancel() throws SipException, ParseException,
      InvalidArgumentException, InterruptedException {
    clientEntryLog();
    // (0) preparation
    SipCall ua1Call = ua1.createSipCall();
    SipCall ua2Call = ua2.createSipCall();
    ua2Call.listenForIncomingCall();

    // (1) UA1 send invite
    {
      logCaseStep(1);
      List<Header> headers = new ArrayList<Header>();
      HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
      // proxy option header
      headers.add(headerFactory.createHeader(HEADER_TCK_PROXY_OPTIONS, ""));
      // proxy dest header
      headers.add(headerFactory.createHeader(HEADER_TCK_PROXY_DEST, ua2
          .getContactInfo().getURI()));

      boolean status_ok = initiateOutgoingCall(ua1Call, null, SERVLET_NAME,
          headers, null, null);
      assertTrue("Initiate outgoing call failed - " + ua1Call.format(),
          status_ok);
    }
    ua1Call.waitOutgoingCallResponse(waitDuration); // get next response
    assertLastOperationSuccess("Wait response error - " + ua1Call.format(),
        ua1Call);

    // (2) UA2 receive invite
    {
      logCaseStep(2);
      ua2Call.waitForIncomingCall(waitDuration * 2);
      SipRequest inviteReq = ua2Call.getLastReceivedRequest();
      assertNotNull(inviteReq);
      assertTrue(inviteReq.isInvite());
    }

    // (3) UA1 send cancel
    {
      logCaseStep(3);
      ua1Call.sendCancel();
      assertLastOperationSuccess("Wait response error - " + ua1Call.format(),
          ua1Call);
    }

    // (4) UA1 receive 200 OK
    {
      logCaseStep(4);
      Thread.sleep(1000);
      assertResponseReceived("Did not receive 200 OK on Cancel", Response.OK,
          Request.CANCEL, 1, ua1Call);
    }

    // (5) UA2 receive cancel
    {
      logCaseStep(5);
      ua2Call.waitForCancel(waitDuration);
      assertRequestReceived(Request.CANCEL, ua2Call);
    }

    // (6) UA2 send 200 ok
    {
      logCaseStep(6);
      ua2Call
          .sendResponseToLastReceivedRequest(Response.OK, null, waitDuration);
      assertLastOperationSuccess("Send response for cancel fail - "
          + ua2Call.format(), ua2Call);
    }

    // (7) UA2 send 487 invite
    {
      logCaseStep(7);
      ua2Call.sendIncomingCallResponse(Response.REQUEST_TERMINATED, null,
          waitDuration);
      assertLastOperationSuccess("Send 487 fail - " + ua2Call.format(), ua2Call);
    }

    // (8) UA2 receive ACK
    {
      logCaseStep(8);
      // Not necessary to assert ACK
      // because ACK received for ServerTransaction not delivering to
      // application by Jain sip.
      // assertTrue(ua2Call.waitForAck(waitDuration * 2));
      // SipRequest req = ua2Call.getLastReceivedRequest();
      // assertTrue(req.isAck());
    }

    // (9) UA1 receive 487 invite
    {
      logCaseStep(9);
      ua1Call.waitOutgoingCallResponse(waitDuration);
      SipResponse resp = ua1Call.getLastReceivedResponse();
      assertTrue(resp.getStatusCode() == Response.REQUEST_TERMINATED);
    }

    // (10) UA1 send ACK
    {
      logCaseStep(10);
      // Jain Sip will send ACK for non 2xx response
      // ua1Call.sendInviteErrorAck();
      // assertLastOperationSuccess("Send ACK fail - " + ua1Call.format(),
      // ua1Call);
    }
  }

  /**
   * Tests that a 483 Too Many Hops response is returned when an application
   * attempts to proxy a request with Max-Forwards: 0.
   * 
   * <pre>
   *   UA1           proxy           UA2 
   *   |              |              | 
   *   |(1) INVITE    |              | 
   *   | Max-Fwd: 0   |              | 
   *   |------------->|              |     
   *   |(2) 483       |              |    
   *   |<-------------|              |   
   *   |(3) ACK       |              |   
   *   |------------->|              |     
   *   |              |              |      
   * </pre>
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Proxy5" }, 
      desc = "Tests that a 483 Too Many Hops response is returned when an application "
      + " attempts to proxy a request with Max-Forwards: 0")
  public void testProxyTooManyHops() throws SipException, ParseException,
      InvalidArgumentException, InterruptedException {
    clientEntryLog();
    // (0) preparation
    SipCall ua1Call = ua1.createSipCall();
    SipCall ua2Call = ua2.createSipCall();
    ua2Call.listenForIncomingCall();

    // (1) UA1 send invite
    {
      logCaseStep(1);
      HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
      ArrayList<Header> additionalHeaders = new ArrayList<Header>();
      // proxy option header
      additionalHeaders.add(headerFactory.createHeader(
          HEADER_TCK_PROXY_OPTIONS, ""));
      // proxy dest header
      additionalHeaders.add(headerFactory.createHeader(HEADER_TCK_PROXY_DEST,
          ua2.getContactInfo().getURI()));

      ArrayList<Header> headers = new ArrayList<Header>();
      headers.add(headerFactory.createMaxForwardsHeader(0));

      boolean status_ok = initiateOutgoingCall(ua1Call, null, SERVLET_NAME,
          additionalHeaders, headers, null, null);
      assertTrue("Initiate outgoing call failed - " + ua1Call.format(),
          status_ok);
    }
    ua1Call.waitOutgoingCallResponse(waitDuration); // get next response
    assertLastOperationSuccess("Wait response error - " + ua1Call.format(),
        ua1Call);

    // (2) UA1 receive 483
    {
      logCaseStep(2);
      ua1Call.waitOutgoingCallResponse(waitDuration);
      SipResponse resp = ua1Call.getLastReceivedResponse();
      assertTrue(resp.getStatusCode() == Response.TOO_MANY_HOPS);
    }

    // (3) UA1 send ack
    {
      logCaseStep(3);
      // Jain Sip will send ACK
      // ua1Call.sendInviteErrorAck();
      // assertLastOperationSuccess("Send ACK fail - " + ua1Call.format(),
      // ua1Call);
    }
  }

  /**
   * <p>
   * Exercises ability of proxy app to generate its own 2xx response in the best
   * response callback, when the best response received was a non-2xx.
   * </p>
   * 
   * <pre>
   *   UA1           proxy           UA2
   *   |              |              |
   *   |(1) MESSAGE   |              |
   *   |------------->|              |
   *   |              |(2) MESSAGE   |
   *   |              |------------->|
   *   |              |(3) 408       |
   *   |              |<-------------|
   *   |(4) 202       |              |
   *   |<-------------|              |
   *   |              |              |
   * </pre>
   * 
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Proxy6" }, 
      desc = "Exercises ability of proxy app to generate its own 2xx response in "
      + " the best response callback, when the best response received was a non-2xx")
  public void testProxyGen2xx() throws SipException, ParseException,
      InvalidArgumentException, InterruptedException {
    clientEntryLog();
    SipTransaction ua1TX;
    ua2.listenRequestMessage();

    // (1) UA1 send message
    {
      logCaseStep(1);
      this.serverURI = "sip:" + "proxy-gen2xx" + "@" + serverHost + ":"
          + serverPort;
      Request message = assembleRequest(Request.MESSAGE, SERVLET_NAME, null,
          TestConstants.SERVER_MODE_UA, 1);

      HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
      message.addHeader(headerFactory.createHeader(HEADER_TCK_PROXY_DEST, ua2
          .getContactInfo().getURI()));
      message.setContent("What's up?", headerFactory.createContentTypeHeader(
          "text", "plain"));

      ua1TX = ua1.sendRequestWithTransaction(message, true, null);
      assertNotNull(ua1.format(), ua1TX);
      assertLastOperationSuccess(ua1);
    }

    // (2) UA2 receive message
    RequestEvent reqEv;
    {
      logCaseStep(2);
      reqEv = ua2.waitRequest(waitDuration);
      assertNotNull(reqEv);
    }

    // (3) UA2 send 408
    {
      logCaseStep(3);
      Request req = reqEv.getRequest();
      ServerTransaction st1 = ua2.getParent().getSipProvider()
          .getNewServerTransaction(req);
      Response resp = this.createResponse(req, ua2,
          Response.REQUEST_TERMINATED, ua2.generateNewTag());
      st1.sendResponse(resp);
    }

    // (4) UA1 receive 202
    {
      logCaseStep(4);
      Thread.sleep(waitDuration / 5);
      EventObject waitResponse = ua1.waitResponse(ua1TX, waitDuration);
      assertFalse("Operation timed out", waitResponse instanceof TimeoutEvent);
      assertEquals("Should have received OK", Response.ACCEPTED,
          ((ResponseEvent) waitResponse).getResponse().getStatusCode());
    }
  }

  boolean initiateOutgoingCall(SipCall call, String methodName,
      String servletName, ArrayList<Header> additionalHeaderList,
      ArrayList<Header> headerList, String viaNonProxyRoute, String toUri) {
    StackTraceElement stack = null;
    if (methodName == null || servletName == null) {
      stack = getBasePackageStack(new Exception().getStackTrace());
    }
    String localServletName = servletName == null ? getInterfaceName(stack
        .getClassName()) : servletName;

    String localMethodName = null;

    HeaderFactory headerFactory = call.getHeaderFactory();
    Header servletheader;
    Header methodHeader;
    ArrayList<Header> additionalHeaders = new ArrayList<Header>();
    try {
      servletheader = headerFactory.createHeader(TestConstants.SERVLET_HEADER,
          localServletName);
      additionalHeaders.add(servletheader);
      String appName = getAppHeader();
      additionalHeaders.add(headerFactory.createHeader(
          TestConstants.APP_HEADER, appName));
    } catch (ParseException e) {
      logger.error("*** ParseException when creating TCK private headers ***",
          e);
      throw new TckTestException(e);
    } catch (ClassNotFoundException e) {
      logger
          .error(
              "*** Class not found when getting the TargetApplication annotation***",
              e);
      throw new TckTestException(e);
    } catch (NoSuchMethodException e) {
      logger
          .error(
              "*** Method not found when getting the TargetApplication annotation***",
              e);
      throw new TckTestException(e);
    }

    if (additionalHeaderList != null) {
      for (Header header : additionalHeaderList) {
        additionalHeaders.add(header);
      }
    }
    if (toUri == null) {
      toUri = serverURI;
    }
    String fromUri = (call.getParent().getAddress().getURI()).toString();

    // send INVITE with private headers added
    return call.initiateOutgoingCall(fromUri, toUri, viaNonProxyRoute,
        additionalHeaders, headerList, null);
  }

  /**
   * Basic proxy test. Default Proxy settings (e.g. no record-routing). UAs
   * check that initial INVITE/180/200 makes it through the app (app adds
   * Proxy-Servlet header) and that ACK/BYE/200 does not. Caller sends BYE.
   * 
   * <pre>
   *   UA1           proxy           UA2
   *   |              |              |
   *   |(1) INVITE    |              |
   *   |------------->|              |
   *   |              |(2) INVITE    |
   *   |              |------------->|
   *   |              |(3) 200       |
   *   |              |<-------------|
   *   |(4) 200       |              |
   *   |<-------------|              |
   *   |(5) ACK       |              |
   *   |---------------------------->|
   *   |(6) BYE       |              |
   *   |---------------------------->|
   *   |(7) 200       |              |
   *   |<----------------------------|
   *   |              |              |
   * </pre>
   * 
   * @throws ParseException
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Proxy7" }, 
      desc = "Basic proxy caller bye test")
  public void testProxyBasicCallerBye() throws ParseException {
    clientEntryLog();
    // UA2 begin listen request
    ua2.listenRequestMessage();
    SipCall ua2Call = ua2.createSipCall();
    // (1) UA1 Send INVITE
    SipCall ua1Call = ua1.createSipCall();
    List<Header> targets = Collections.singletonList(ua1Call.getHeaderFactory()
        .createHeader(HEADER_TCK_PROXY_DEST, ua2URI));
    boolean result = initiateOutgoingCall(ua1Call, null, SERVLET_NAME, targets,
        null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), result);

    // (2) UA2 receive INVITE
    assertTrue("Don't receive INVITE", ua2Call.waitForRequest(
        SipRequest.INVITE, waitDuration / 2));

    // (3) UA2 send 200
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);

    // (4) UA1 receive 200
    waitNon100Response(ua1Call, waitDuration / 2);
    assertResponseReceived("Don't receive 200", SipResponse.OK, ua1Call);

    // (5) UA1 send ACK
    ua1Call.getLastReceivedResponse().getMessage().removeHeader(SERVLET_NAME);
    assertTrue(ua1Call.sendInviteOkAck());
    assertTrue("Don't receive ACK", ua2Call.waitForRequest(SipRequest.ACK,
        waitDuration / 2));
    if (isViaProxy(ua2Call.getLastReceivedRequest())) {
      fail("Subsequent request went through proxy app.");
    }

    // (6) UA1 send BYE
    assertTrue(ua1Call.disconnect());
    assertTrue("Don't receive BYE", ua2Call.waitForRequest(SipRequest.BYE,
        waitDuration / 2));
    if (isViaProxy(ua2Call.getLastReceivedRequest())) {
      fail("Subsequent request went through proxy app.");
    }

    // (7) UA2 send 200
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);
    ua1Call.waitOutgoingCallResponse(waitDuration / 2);
    assertResponseReceived("Did not receive 200 OK on BYE", Response.OK,
        Request.BYE, 2, ua1Call);
  }

  /**
   * As basic-proxy-caller-bye except the callee test agent sends the BYE.
   * Shouldn't matter for the container as it shouldn't see the BYE anyway since
   * it did not record-route.
   * 
   * <pre>
   *    UA1            proxy          UA2
   *     |              |              |
   *     |(1) INVITE    |              |
   *     |------------->|              |
   *     |              |(2) INVITE    |
   *     |              |------------->|
   *     |              |(3) 200       |
   *     |              |<-------------|
   *     |(4) 200       |              |
   *     |<-------------|              |
   *     |(5) ACK       |              |
   *     |---------------------------->|
   *     |(6) BYE       |              |
   *     |<----------------------------|
   *     |(7) 200       |              |
   *     |---------------------------->|
   *     |              |              |
   * </pre>
   * 
   * @throws ParseException
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Proxy8" },
      desc = "Basic proxy callee bye test")
  public void testProxyBasicCalleeBye() throws ParseException {
    clientEntryLog();
    // UA2 begin listen request
    ua2.listenRequestMessage();
    SipCall ua2Call = ua2.createSipCall();
    // (1) UA1 Send INVITE
    SipCall ua1Call = ua1.createSipCall();
    ua1.listenRequestMessage();
    List<Header> targets = Collections.singletonList(ua1Call.getHeaderFactory()
        .createHeader(HEADER_TCK_PROXY_DEST, ua2URI));
    boolean result = initiateOutgoingCall(ua1Call, null, SERVLET_NAME, targets,
        null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), result);

    // (2) UA2 receive INVITE

    assertTrue(ua2Call.waitForIncomingCall(waitDuration));
    assertTrue("Don't receive INVITE", ua2Call.getLastReceivedRequest()
        .isInvite());

    // (3) UA2 send 200
    assertTrue(ua2Call.sendIncomingCallResponse(SipResponse.OK, "OK", 0));

    // (4) UA1 receive 200
    waitNon100Response(ua1Call, waitDuration / 2);
    assertResponseReceived("Don't receive 200", SipResponse.OK, ua1Call);

    // (5) UA1 send ACK
    ua1Call.getLastReceivedResponse().getMessage().removeHeader(SERVLET_NAME);
    assertTrue(ua1Call.sendInviteOkAck());
    assertTrue("Don't receive ACK", ua2Call.waitForRequest(SipRequest.ACK,
        waitDuration / 2));
    if (isViaProxy(ua2Call.getLastReceivedRequest())) {
      fail("Subsequent request went through proxy app.");
    }

    // (6) UA2 send BYE
    assertTrue(ua2Call.disconnect());
    assertTrue("Don't receive BYE", ua1Call.waitForRequest(SipRequest.BYE,
        waitDuration / 2));
    if (isViaProxy(ua1Call.getLastReceivedRequest())) {
      fail("Subsequent request went through proxy app.");
    }

    // (7) UA1 send 200
    ua1Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);
    ua2Call.waitOutgoingCallResponse(waitDuration / 2);
    assertResponseReceived("Did not receive 200 OK on BYE", Response.OK,
        Request.BYE, 1, ua2Call);
  }

  /**
   * App record-routes. Caller sends subsequent BYE. It is checked that the BYE
   * passes through the container.
   * 
   * <pre>
   *    UA1            proxy          UA2
   *     |              |              |
   *     |(1) INVITE    |              |
   *     |------------->|              |
   *     |              |(2) INVITE    |
   *     |              |------------->|
   *     |              |(3) 200       |
   *     |              |<-------------|
   *     |(4) 200       |              |
   *     |<-------------|              |
   *     |(5) ACK       |              |
   *     |------------->|              |
   *     |              |(6) ACK       |
   *     |              |------------->|
   *     |(7) BYE       |              |
   *     |------------->|              |
   *     |              |(8) BYE       |
   *     |              |------------->|
   *     |              |(9) 200       |
   *     |              |<-------------|
   *     |(10) 200      |              |
   *     |<-------------|              |
   *     |              |              |
   * </pre>
   * 
   * @throws ParseException
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Proxy9" }, desc = "App record-routes. Proxy caller bye test")
  public void testProxyRRCallerBye() throws ParseException {
    clientEntryLog();
    // UA2 begin listen request
    ua2.listenRequestMessage();
    SipCall ua2Call = ua2.createSipCall();
    // (1) UA1 Send INVITE
    SipCall ua1Call = ua1.createSipCall();

    List<Header> headers = new ArrayList<Header>(2);
    headers.add(ua1Call.getHeaderFactory().createHeader(HEADER_TCK_PROXY_DEST,
        ua2URI));
    headers.add(ua1Call.getHeaderFactory().createHeader(
        HEADER_TCK_PROXY_OPTIONS, "recordRoute=true"));
    boolean result = initiateOutgoingCall(ua1Call, null, SERVLET_NAME, headers,
        null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), result);

    // (2) UA2 receive INVITE
    assertTrue("Don't receive INVITE", ua2Call.waitForRequest(
        SipRequest.INVITE, waitDuration / 2));

    // (3) UA2 send 200
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);

    // (4) UA1 receive 200
    waitNon100Response(ua1Call, waitDuration / 2);
    assertResponseReceived("Don't receive 200", SipResponse.OK, ua1Call);

    // (5) UA1 send ACK
    ua1Call.getLastReceivedResponse().getMessage().removeHeader(SERVLET_NAME);
    assertTrue(ua1Call.sendInviteOkAck());

    // (6) UA2 receive ACK
    assertTrue("Don't receive ACK", ua2Call.waitForRequest(SipRequest.ACK,
        waitDuration / 2));
    if (!isViaProxy(ua2Call.getLastReceivedRequest())) {
      fail("Subsequent request didn't go through proxy app.");
    }

    // (7) UA1 send BYE
    assertTrue(ua1Call.disconnect());

    // (8) UA2 receive BYE
    assertTrue("Don't receive BYE", ua2Call.waitForRequest(SipRequest.BYE,
        waitDuration / 2));
    if (!isViaProxy(ua2Call.getLastReceivedRequest())) {
      fail("Subsequent request didn't go through proxy app.");
    }

    // (9) UA2 send 200
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);

    // (10) UA1 receive 200
    ua1Call.waitOutgoingCallResponse(waitDuration / 2);
    assertResponseReceived("Did not receive 200 OK on BYE", Response.OK,
        Request.BYE, 2, ua1Call);
  }

  /**
   * As basic-proxy-caller-bye except that the callee test agent sends the BYE,
   * that is, the BYE traverses the servlet container in the opposite direction
   * of the INVITE.ua1 proxy ua2.
   * 
   * <pre>
   *    UA1            proxy          UA2
   *     |              |              |
   *     |(1) INVITE    |              |
   *     |------------->|              |
   *     |              |(2) INVITE    |
   *     |              |------------->|
   *     |              |(3) 200       |
   *     |              |<-------------|
   *     |(4) 200       |              |
   *     |<-------------|              |
   *     |(5) ACK       |              |
   *     |------------->|              |
   *     |              |(6) ACK       |
   *     |              |------------->|
   *     |              |(7) BYE       |
   *     |              |<-------------|
   *     |(8) BYE       |              |
   *     |<-------------|              |
   *     |(9) 200       |              |
   *     |------------->|              |
   *     |              |(10) 200      |
   *     |              |------------->|
   *     |              |              |
   * </pre>
   * 
   * @throws ParseException
   * 
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Proxy10" }, 
      desc = "App record-routes. Proxy callee bye test")
  public void testProxyRRCalleeBye() throws ParseException {
    clientEntryLog();
    // UA2 begin listen request
    ua2.listenRequestMessage();
    SipCall ua2Call = ua2.createSipCall();
    // (1) UA1 Send INVITE
    SipCall ua1Call = ua1.createSipCall();
    ua1.listenRequestMessage();
    List<Header> headers = new ArrayList<Header>(2);
    headers.add(ua1Call.getHeaderFactory().createHeader(HEADER_TCK_PROXY_DEST,
        ua2URI));
    headers.add(ua1Call.getHeaderFactory().createHeader(
        HEADER_TCK_PROXY_OPTIONS, "recordRoute=true"));
    boolean result = initiateOutgoingCall(ua1Call, null, SERVLET_NAME, headers,
        null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), result);

    // (2) UA2 receive INVITE

    assertTrue(ua2Call.waitForIncomingCall(waitDuration));
    assertTrue("Don't receive INVITE", ua2Call.getLastReceivedRequest()
        .isInvite());

    // (3) UA2 send 200
    ua2Call.sendIncomingCallResponse(SipResponse.OK, "OK", 0);

    // (4) UA1 receive 200
    waitNon100Response(ua1Call, waitDuration / 2);
    assertResponseReceived("Don't receive 200", SipResponse.OK, ua1Call);

    // (5) UA1 send ACK
    ua1Call.getLastReceivedResponse().getMessage().removeHeader(SERVLET_NAME);
    assertTrue(ua1Call.sendInviteOkAck());

    // (6) UA2 receive ACK
    assertTrue("Don't receive ACK", ua2Call.waitForRequest(SipRequest.ACK,
        waitDuration / 2));
    if (!isViaProxy(ua2Call.getLastReceivedRequest())) {
      fail("Subsequent request didn't go through proxy app.");
    }

    // (7) UA2 send BYE
    assertTrue(ua2Call.disconnect());
    // (8) UA1 receive BYE
    assertTrue("Don't receive BYE", ua1Call.waitForRequest(SipRequest.BYE,
        waitDuration / 2));
    if (!isViaProxy(ua1Call.getLastReceivedRequest())) {
      fail("Subsequent request didn't go through proxy app.");
    }

    // (9) UA1 send 200
    ua1Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);

    // (10) UA2 receive 200
    ua2Call.waitOutgoingCallResponse(waitDuration / 2);
    assertResponseReceived("Did not receive 200 OK on BYE", Response.OK,
        Request.BYE, 1, ua2Call);
  }

  /**
   * Tests ability of applications to push state to endpoints (typically in the
   * form of parameters of a Record-Route header). A parameter named "foo" and
   * with value "bar" is pushed to both sides. It is verified that the same
   * state is avilablable from the ACK received from the caller and the BYE
   * received from the callee.
   * 
   * <pre>
   *      UA1            proxy          UA2
   *       |              |              |
   *       |(1) INVITE    |              |
   *       |------------->|              |
   *       |              |(2) INVITE    |
   *       |              |------------->|
   *       |              |(3) 200       |
   *       |              |<-------------|
   *       |(4) 200       |              |
   *       |<-------------|              |
   *       |(5) ACK       |              |
   *       |------------->|              |
   *       |              |(6) ACK       |
   *       |              |------------->|
   *       |(7) BYE       |              |
   *       |------------->|              |
   *       |              |(8) BYE       |
   *       |              |------------->|
   *       |              |(9) 200       |
   *       |              |<-------------|
   *       |(10) 200      |              |
   *       |<-------------|              |
   *       |              |              |
   * </pre>
   * 
   * @throws ParseException
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Proxy11" }, 
      desc = "Tests ability of applications to push state to endpoints")
  public void testProxyPushState() throws ParseException {
    clientEntryLog();
    // UA2 begin listen request
    ua2.listenRequestMessage();
    SipCall ua2Call = ua2.createSipCall();
    // (1) UA1 Send INVITE
    SipCall ua1Call = ua1.createSipCall();

    List<Header> headers = new ArrayList<Header>(2);
    headers.add(ua1Call.getHeaderFactory().createHeader(HEADER_TCK_PROXY_DEST,
        ua2URI));
    headers.add(ua1Call.getHeaderFactory().createHeader(
        HEADER_TCK_PROXY_OPTIONS, "recordRoute=true param.foo=bar"));
    boolean result = initiateOutgoingCall(ua1Call, null, SERVLET_NAME, headers,
        null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), result);

    // (2) UA2 receive INVITE
    assertTrue("Don't receive INVITE", ua2Call.waitForRequest(
        SipRequest.INVITE, waitDuration / 2));

    // (3) UA2 send 200
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);

    // (4) UA1 receive 200
    waitNon100Response(ua1Call, waitDuration / 2);
    assertResponseReceived("Don't receive 200", SipResponse.OK, ua1Call);

    // (5) UA1 send ACK
    ua1Call.getLastReceivedResponse().getMessage().removeHeader(SERVLET_NAME);
    assertTrue(ua1Call.sendInviteOkAck());

    // (6) UA2 receive ACK
    assertTrue("Don't receive ACK", ua2Call.waitForRequest(SipRequest.ACK,
        waitDuration / 2));
    if (!isViaProxy(ua2Call.getLastReceivedRequest())) {
      fail("Subsequent request didn't go through proxy app.");
    }

    // (7) UA1 send BYE
    assertTrue(ua1Call.disconnect());

    // (8) UA2 receive BYE
    assertTrue("Don't receive BYE", ua2Call.waitForRequest(SipRequest.BYE,
        waitDuration / 2));
    if (!isViaProxy(ua2Call.getLastReceivedRequest())) {
      fail("Subsequent request didn't go through proxy app.");
    }

    // (9) UA2 send 200
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);

    // (10) UA1 receive 200
    ua1Call.waitOutgoingCallResponse(waitDuration / 2);
    assertResponseReceived("Did not receive 200 OK on BYE", Response.OK,
        Request.BYE, 2, ua1Call);
  }

  /**
   * Same callflow as proxy-rr-caller-bye testcase. This test differs in that
   * the supervised flag is set to false when the application proxies the
   * INVITE. When the UAC test agent receives the 2xx/INVITE it checks to see
   * that the Proxy-Servlet header is NOT present in the 2xx. This means the
   * servlet was (correctly) not invoked for the 2xx. The servlet app *should*
   * be invoked for the 200/BYE response and this is also checked.
   * 
   * <pre>
   *    UA1            proxy          UA2
   *     |              |              |
   *     |(1) INVITE    |              |
   *     |------------->|              |
   *     |              |(2) INVITE    |
   *     |              |------------->|
   *     |              |(3) 200       |
   *     |              |<-------------|
   *     |(4) 200       |              |
   *     |<-------------|              |
   *     |(5) ACK       |              |
   *     |------------->|              |
   *     |              |(6) ACK       |
   *     |              |------------->|
   *     |(7) BYE       |              |
   *     |------------->|              |
   *     |              |(8) BYE       |
   *     |              |------------->|
   *     |              |(9) 200       |
   *     |              |<-------------|
   *     |(10) 200      |              |
   *     |<-------------|              |
   *     |              |              |
   * </pre>
   * 
   * @throws ParseException
   */
  @AssertionIds(ids = { "SipServlet:SPEC:Proxy12" }, 
      desc = "Tests behavior of proxy when the supervised flag is set to false")
  public void testProxySupervisedOff() throws ParseException {
    clientEntryLog();
    // UA2 begin listen request
    ua2.listenRequestMessage();
    SipCall ua2Call = ua2.createSipCall();
    // (1) UA1 Send INVITE
    SipCall ua1Call = ua1.createSipCall();

    List<Header> headers = new ArrayList<Header>(2);
    headers.add(ua1Call.getHeaderFactory().createHeader(HEADER_TCK_PROXY_DEST,
        ua2URI));
    headers.add(ua1Call.getHeaderFactory().createHeader(
        HEADER_TCK_PROXY_OPTIONS, "supervised=false recordRoute=true"));
    boolean result = initiateOutgoingCall(ua1Call, null, SERVLET_NAME, headers,
        null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), result);

    // (2) UA2 receive INVITE
    assertTrue("Don't receive INVITE", ua2Call.waitForRequest(
        SipRequest.INVITE, waitDuration / 2));

    // (3) UA2 send 200
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);

    // (4) UA1 receive 200
    waitNon100Response(ua1Call, waitDuration / 2);
    assertResponseReceived("Don't receive 200", SipResponse.OK, ua1Call);
    if (isViaProxy(ua1Call.getLastReceivedResponse())) {
      fail("Subsequent response went through proxy app.");
    }

    // (5) UA1 send ACK
    assertTrue(ua1Call.sendInviteOkAck());

    // (6) UA2 receive ACK
    assertTrue("Don't receive ACK", ua2Call.waitForRequest(SipRequest.ACK,
        waitDuration / 2));
    if (!isViaProxy(ua2Call.getLastReceivedRequest())) {
      fail("Subsequent request didn't go through proxy app.");
    }

    // (7) UA1 send BYE
    assertTrue(ua1Call.disconnect());

    // (8) UA2 receive BYE
    assertTrue("Don't receive BYE", ua2Call.waitForRequest(SipRequest.BYE,
        waitDuration / 2));
    if (!isViaProxy(ua2Call.getLastReceivedRequest())) {
      fail("Subsequent request didn't go through proxy app.");
    }

    // (9) UA2 send 200
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);

    // (10) UA1 receive 200
    ua1Call.waitOutgoingCallResponse(waitDuration / 2);
    assertResponseReceived("Did not receive 200 OK on BYE", Response.OK,
        Request.BYE, 2, ua1Call);
    if (!isViaProxy(ua1Call.getLastReceivedResponse())) {
      fail("Subsequent response didn't go through proxy app.");
    }
  }

  private boolean isViaProxy(SipMessage sipMessage) {
    Header header = sipMessage.getMessage().getHeader(SERVLET_NAME);
    if (header != null && ((SIPHeader) header).getValue().equals(SERVLET_NAME)) {
      return true;
    }
    return false;
  }

  private void waitNon100Response(SipCall ua, int wait) {
    int tryTimes = 1;
    int maxTryTimes = 4;
    while (maxTryTimes > tryTimes) {
      if (ua.waitOutgoingCallResponse(wait)) {
        logger.debug("response status:" + ua.getReturnCode());
        if (ua.getReturnCode() != Response.TRYING) {
          return;
        }
      }
      tryTimes++;
    }
    return;
  }

}
