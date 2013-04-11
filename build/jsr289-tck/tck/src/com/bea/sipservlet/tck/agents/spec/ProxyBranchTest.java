/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved. 
 *  
 * ProxyBranchTest is used to test the spec of proxy branch
 */
package com.bea.sipservlet.tck.agents.spec;

import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.AddressParametersHeader;
import gov.nist.javax.sip.header.ExtensionHeaderImpl;
import gov.nist.javax.sip.header.SIPHeader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.RecordRouteHeader;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipRequest;
import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipStack;

import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;


@TargetApplication(ApplicationName.PROXY)
public class ProxyBranchTest extends TestBase  {
  private static Logger logger = Logger.getLogger(ProxyBranchTest.class);
  
  /**
   * For tracing the flow step
   */
  private int step;
  protected SipStack sipStack4;
  private SipPhone ua4;
  private String ua4Host;
  private int ua4Port;


  private String ua4UserName;
  private String ua4DispName;


  private Properties properties4;
  private String ua4Url;
  private String ua4URI;
  private String ua4Addr;
  public ProxyBranchTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
    Properties defaultProperties = new Properties();
    InputStream in = new FileInputStream("conf/default.properties");
    try{
      defaultProperties.load(in);
    }finally{
      in.close();
    }

    
    ua4Host = localHost;
    ua4Port = Integer.parseInt(defaultProperties.getProperty("ua4.port"));
    ua4UserName = defaultProperties.getProperty("ua4.username");
    ua4DispName = defaultProperties.getProperty("ua4.displayname");
    ua4Url = "sip:" + ua4UserName + "@" + domain;
    ua4URI = ua4Url + ":" + ua4Port;
    ua4Addr = ua4DispName + " <" + ua4URI + ">";


 
    properties4 = new Properties();
    properties4.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
    properties4.setProperty("javax.sip.STACK_NAME", "testAgent4");
    properties4.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
    properties4.setProperty("gov.nist.javax.sip.DEBUG_LOG",
        "testAgent4_debug.txt");
    properties4.setProperty("gov.nist.javax.sip.SERVER_LOG",
        "testAgent4_log.txt");
    properties4.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
    properties4.setProperty("sipunit.trace", "true");
    properties4.setProperty("sipunit.test.port", String.valueOf(ua4Port));
    properties4.setProperty("sipunit.test.protocol", testProtocol);
    properties4.setProperty("sipunit.test.domain", domain);
    properties4.setProperty("sipunit.proxy.host", serverHost);
    properties4.setProperty("sipunit.proxy.port", String.valueOf(serverPort));
  }
  
  public void setUp() throws Exception {
    super.setUp();
    step = 1;
    sipStack4 = new SipStack(testProtocol, ua4Port, properties4);

    ua4 = sipStack4.createSipPhone(serverHost, testProtocol, serverPort, ua4Url);
  }
  /**
   *     Creating proxy branch and Proxy.setParallel(false).
       <pre>
        UA1                Proxy                UA2                 UA3
         |                   |                   |                   |
         |(1) INVITE         |                   |                   |
         |------------------>|                   |                   |
         |                   |                   |                   |
         |                   |(2) INVITE         |                   |
         |                   |------------------>|                   |
         |                   |                   |                   |
         |                   |(3) 180            |                   |
         |                   |<------------------|                   |
         |                   |                   |                   |
         |(4) 180            |                   |                   |
         |<------------------|                   |                   |
         |                   |                   |                   |
         |                   |(5) 400            |                   |
         |                   |<------------------|                   |
         |                   |                   |                   |
         |                   |(6) ACK            |                   |
         |                   |------------------>|                   |
         |                   |                   |                   |
         |                   |(7) INVITE         |                   |
         |                   |-------------------------------------->|
         |                   |                   |                   |
         |                   |(8) 200            |                   |
         |                   |<--------------------------------------|
         |                   |                   |                   |
         |(9) 200            |                   |                   |
         |<------------------|                   |                   |
         |                   |                   |                   |
         |(10) ACK           |                   |                   |
         |------------------>|                   |                   |
         |                   |(11) ACK           |                   |
         |                   |-------------------------------------->|
         |                   |                   |                   |
         </pre>
   * @throws ParseException 
   */
  @AssertionIds(ids = { "SipServlet:SPEC:ProxyBranch1" }, 
      desc = "Creating and accessing proxy branches " +
      		"when Proxy.setParallel(false).")
  public void testCreatingBranchNoParallel() throws ParseException{
    clientEntryLog();
    // UA2 begin listen request
    ua2.listenRequestMessage();
    SipCall ua2Call = ua2.createSipCall();
    // UA3 begin listen request
    ua3.listenRequestMessage();
    SipCall ua3Call = ua3.createSipCall();
    Header failReasonHd = null;
    // (1) UA1 Send INVITE
    debug();
    SipCall ua1Call = ua1.createSipCall();
    List<Header> targets = createProxyTargetHeaders(ua1Call);
    boolean result = initiateOutgoingCall(ua1Call, null,
        "CreatingBranchNoParallel", targets, null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), result);
    debug();
    
    // (2) UA2 receive INVITE
    debug();
    assertTrue("Don't receive INVITE", ua2Call.waitForRequest(
        SipRequest.INVITE, waitDuration/2));
    failReasonHd = ua2Call.getLastReceivedRequest().getMessage().getHeader(
        TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(failReasonHd.toString());
    }
    debug();
    
    // (3) UA2 send 180
    debug();
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.RINGING, null, 0);
    debug();
    // (4) UA1 receive 180
    debug();
    waitNon100Response(ua1Call, waitDuration/2);
    assertResponseReceived("Don't receive 180", SipResponse.RINGING, ua1Call);
    debug();
    
    // (5) UA2 send 400
    debug();
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.BAD_REQUEST, "ERROR",
        0);
    debug();
    
    // (6) UA2 receive ACK
    debug();
    //    assertTrue("Don't receive ACK", ua2Call.waitForRequest(SipRequest.ACK,
    //        waitDuration/3));
    debug();
    
    // (7) UA3 receive INVITE
    debug();
    assertTrue("Don't receive INVITE", ua3Call.waitForRequest(
        SipRequest.INVITE, waitDuration / 2));
    failReasonHd = ua3Call.getLastReceivedRequest().getMessage().getHeader(
        TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(failReasonHd.toString());
    }
    debug();

    // (8) UA3 send 200
    debug();
    ua3Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);
    debug();
    
    // (9) UA1 receive 200
    debug();
    assertTrue(ua1Call.waitOutgoingCallResponse(waitDuration / 2));
    assertResponseReceived("Don't receive 200", SipResponse.OK, ua1Call);
    failReasonHd = ua1Call.getLastReceivedResponse().getMessage().getHeader(
        TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(failReasonHd.toString());
    }
    debug();
    // (10) UA1 send ACK
    debug();
    assertTrue(ua1Call.sendInviteOkAck());
    debug();
    // (11) UA3 receive ACK
    debug();
    assertTrue("Don't receive ACK", ua3Call.waitForRequest(SipRequest.ACK,
        waitDuration / 2));
    debug();
    
    ua1Call.disconnect();
       
  }
  /**
   *   Creating proxy branch and Proxy.setParallel(true). 
       <pre>
        UA1           Proxy           UA2            UA3            UA4
         |              |              |              |              |
         |(1) INVITE    |              |              |              |
         |------------->|              |              |              |
         |              |              |              |              |
         |              |(2) INVITE    |              |              |
         |              |------------->|              |              |
         |              |              |              |              |
         |              |(3) 180       |              |              |
         |              |<-------------|              |              |
         |              |              |              |              |
         |(4) 180       |              |              |              |
         |<-------------|              |              |              |
         |              |              |              |              |
         |              |(5) INVITE    |              |              |
         |              |---------------------------->|              |
         |              |              |              |              |
         |              |(6) INVITE    |              |              |
         |              |------------------------------------------->|
         |              |              |              |              |
         |              |(7) 180       |              |              |
         |              |<----------------------------|              |
         |              |              |              |              |
         |(8) 180       |              |              |              |
         |<-------------|              |              |              |
         |              |              |              |              |
         |              |(9) 180       |              |              |
         |              |<-------------------------------------------|
         |              |              |              |              |
         |(10) 180      |              |              |              |
         |<-------------|              |              |              |
         |              |              |              |              |
         |              |(11) 200      |              |              |
         |              |<-------------------------------------------|
         |              |              |              |              |
         |              |              |              |              |
         |              |(12) CANCEL   |              |              |
         |              |------------->|              |              |
         |              |              |              |              |
         |              |(13) CANCEL   |              |              |
         |              |---------------------------->|              |
         |              |              |              |              |          
         |              |(14) 200      |              |              |
         |              |<-------------|              |              |
         |              |              |              |              |          
         |              |(15) 487      |              |              |   
         |              |<-------------|              |              | 
         |              |              |              |              |            
         |              |(16) ACK      |              |              |       
         |              |------------->|              |              |     
         |              |              |              |              |                                                          
         |              |(17) 200      |              |              |
         |              |<----------------------------|              |
         |              |              |              |              |
         |              |(18) 487      |              |              |
         |              |<----------------------------|              |
         |              |              |              |              |
         |              |(19) ACK      |              |              |
         |              |---------------------------->|              |
         |              |              |              |              |
         |(20) 200      |              |              |              |
         |<-------------|              |              |              |
         |              |              |              |              |
         |(21) ACK      |              |              |              |
         |------------->|              |              |              |
         |              |(22) ACK      |              |              |
         |              |------------------------------------------->|    
        </pre>      
   * @throws ParseException 
   */
  @AssertionIds(ids = { "SipServlet:SPEC:ProxyBranch2" }, 
      desc = "Creating and accessing proxy branches " +
          "when Proxy.setParallel(true).")
  public void testCreatingBranchParallel() throws ParseException{
    clientEntryLog();
    // UA2 begin listen request
    ua2.listenRequestMessage();
    SipCall ua2Call = ua2.createSipCall();
    // UA3 begin listen request
    ua3.listenRequestMessage();
    SipCall ua3Call = ua3.createSipCall();
    // UA4 begin listen request
    ua4.listenRequestMessage();
    SipCall ua4Call = ua4.createSipCall();
    
    // (1) UA1 Send INVITE
    debug();
    SipCall ua1Call = ua1.createSipCall();
    List<Header> targets = createProxyTargetHeaders(ua1Call);
    boolean result = initiateOutgoingCall(ua1Call, null,
        "CreatingBranchParallel", targets, null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), result);
    debug();
    
    // (2) UA2 receive INVITE
    debug();
    assertTrue("Don't receive INVITE", ua2Call.waitForRequest(
        SipRequest.INVITE, waitDuration/2));
    debug();
    // (3) UA2 send 180
    debug();
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.RINGING, null, 0);
    debug();
    
    // (4) UA1 receive 180
    debug();
    waitNon100Response(ua1Call, waitDuration/2);
    assertResponseReceived("Don't receive 180", SipResponse.RINGING, ua1Call);
    debug();
    
    // (5) UA3 receive INVITE
    debug();
    assertTrue("Don't receive INVITE", ua3Call.waitForRequest(
        SipRequest.INVITE, waitDuration / 2));
    debug();
    
    // (6) UA4 receive INVITE
    debug();
    assertTrue("Don't receive INVITE", ua4Call.waitForRequest(
        SipRequest.INVITE, waitDuration / 2));
    debug();
    
    // (7) UA3 send 180
    debug();
    ua3Call.sendResponseToLastReceivedRequest(SipResponse.RINGING, null, 0);
    debug();
    
    // (8) UA1 receive 180
    debug();
    waitNon100Response(ua1Call, waitDuration/2);
    assertResponseReceived("Don't receive 180", SipResponse.RINGING, ua1Call);
    debug();
    
    // (9) UA4 send 180
    debug();
    ua4Call.sendResponseToLastReceivedRequest(SipResponse.RINGING, null, 0);
    debug();
    
    // (10) UA1 receive 180
    debug();
    waitNon100Response(ua1Call, waitDuration/2);
    assertResponseReceived("Don't receive 180", SipResponse.RINGING, ua1Call);
    debug();
    
    // (11) UA4 send 200
    debug();
    ua4Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);
    debug();
    
    
    // (12) UA2 receive CANCEL
    debug();
    assertTrue("Don't receive INVITE", ua2Call.waitForRequest(
        SipRequest.CANCEL, waitDuration/2));
    debug();
    
    // (13) UA3 receive CANCEL
    debug();
    assertTrue("Don't receive INVITE", ua3Call.waitForRequest(
        SipRequest.CANCEL, waitDuration/2));
    debug();
    
    // (14) UA2 send 200 for CANCEL
    debug();
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "CANCEL", 0);
    debug();

    // (15) UA2 send 487 for CANCEL
    debug();
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.REQUEST_TERMINATED,
        "CANCEL", 0);
    debug();
    
    // (16)
    debug();
    debug();
    
    // (17) UA3 send 200 for CANCEL
    debug();
    ua3Call.sendResponseToLastReceivedRequest(SipResponse.OK, "CANCEL", 0);
    debug();

    // (18) UA3 send 487 for CANCEL
    debug();
    ua3Call.sendResponseToLastReceivedRequest(SipResponse.REQUEST_TERMINATED,
        "CANCEL", 0);
    debug();
    
    //(19)
    debug();
    debug();
    
    
    // (20) UA1 receive 200
    debug();
    assertTrue(ua1Call.waitOutgoingCallResponse(waitDuration / 2));
    assertResponseReceived("Don't receive 200", SipResponse.OK, ua1Call);
    
    Header failReasonHd = ua1Call.getLastReceivedResponse().getMessage()
        .getHeader(TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(failReasonHd.toString());
    }
    
    ListIterator successHeaders = ua1Call.getLastReceivedResponse().getMessage()
        .getHeaders(TestConstants.TEST_RESULT);
    for (int i = 0; i < 3; i++) {
      assertTrue(successHeaders.next().toString().indexOf(
          TestConstants.TEST_RESULT_OK) > -1);
    }
    debug();
    
    // (21) UA1 send ACK
    debug();
    assertTrue(ua1Call.sendInviteOkAck());
    debug();
    
    // (22) UA4 receive ACK
    debug();
    assertTrue("Don't receive ACK", ua4Call.waitForRequest(SipRequest.ACK,
        waitDuration / 2));
    debug();
    
    ua1Call.disconnect();

  }
  
  /**
   * Receipt of a 3xx class redirect response on a branch can result
   * in recursed branches if the proxy or the branch has recursion enabled.
     <pre>
        UA1           Proxy            UA2           UA3
         |              |              |              |
         |(1) INVITE    |              |              |
         |------------->|              |              |
         |              |              |              |
         |              |(2) INVITE    |              |
         |              |------------->|              |
         |              |              |              |
         |              |(3) 301       |              |
         |              |<-------------|              |
         |              |              |              |
         |              |(4) ACK       |              |
         |              |------------->|              |
         |              |              |              |
         |              |(5) INVITE    |              |
         |              |---------------------------->|
         |              |              |              |
         |              |(6) 200       |              |
         |              |<----------------------------|
         |              |              |              |
         |              |              |              |
         |(7) 200       |              |              |
         |<-------------|              |              |
         |              |              |              |
         |(8) ACK       |              |              |
         |------------->|              |              |
         |              |(9) ACK       |              |
         |              |---------------------------->|
        </pre>
   * @throws ParseException 
   */
  @AssertionIds(ids = { "SipServlet:SPEC:ProxyBranch3" }, 
      desc = "Receipt of a 3xx class redirect response on a branch can result "
      + "in recursed branches if the proxy or the branch has recursion enabled.")
  public void testProxyBranchRecurse() throws ParseException{
    clientEntryLog();
    // UA2 begin listen request
    ua2.listenRequestMessage();
    SipCall ua2Call = ua2.createSipCall();
    // UA3 begin listen request
    ua3.listenRequestMessage();
    SipCall ua3Call = ua3.createSipCall();
    
    // (1) UA1 Send INVITE
    debug();
    SipCall ua1Call = ua1.createSipCall();
    List<Header> targets = createProxyTargetHeaders(ua1Call);
    boolean result = initiateOutgoingCall(ua1Call, null,
        "ProxyBranchRecurse", targets, null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), result);
    debug();
    
    // (2) UA2 receive INVITE
    debug();
    assertTrue("Don't receive INVITE", ua2Call.waitForRequest(
        SipRequest.INVITE, waitDuration/2));
    debug();
    
    // (3) UA2 send 301
    debug();
    HeaderFactory header_factory2 = ua2.getParent().getHeaderFactory();
    AddressFactory addr_factory2 = ua2.getParent().getAddressFactory();
    Address contact_address = addr_factory2.createAddress(addr_factory2
        .createURI(ua3URI));
    ContactHeader contactHeader = header_factory2
        .createContactHeader(contact_address);
    ArrayList<ContactHeader> contactHeaders = new ArrayList(1);
    contactHeaders.add(contactHeader);
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.MOVED_PERMANENTLY,
        "REDIRECT", 0, null, contactHeaders, null);
    debug();
    
    //(4)
    debug();
    debug();
    
    // (5) UA3 receive INVITE
    debug();
    assertTrue("Don't receive INVITE", ua3Call.waitForRequest(
        SipRequest.INVITE, waitDuration / 2));
    debug();
    
    // (6) UA3 send 200
    debug();
    ua3Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);
    debug();
    
    // (7) UA1 receive 200
    debug();
    waitNon100Response(ua1Call, waitDuration/2);
    assertResponseReceived("Don't receive 200", SipResponse.OK, ua1Call);
    Header failReasonHd = ua1Call.getLastReceivedResponse().getMessage()
    .getHeader(TestConstants.TEST_FAIL_REASON);
    if (failReasonHd != null) {
      fail(failReasonHd.toString());
    }
    
    debug();
    // (8) UA1 send ACK
    debug();
    assertTrue(ua1Call.sendInviteOkAck());
    debug();
    
    // (9) UA3 receive ACK
    debug();
    assertTrue("Don't receive ACK", ua3Call.waitForRequest(SipRequest.ACK,
        waitDuration / 2));
    debug();
    
    ua1Call.disconnect();
  }
  
  /**
   * Add/remove non system headers from the SipServletRequest 
   * before the request is proxied. The headers will be found on target UAS.
     <pre>
        UA1           Proxy           UA2           UA3
         |              |              |              |
         |(1) INVITE    |              |              |
         |------------->|              |              |
         |              |              |              |
         |              |(2) INVITE    |              |
         |              |------------->|              |
         |              |              |              |
         |              |(3) INVITE    |              |
         |              |---------------------------->|
         |              |              |              |
         |              |(4) 400       |              |
         |              |<----------------------------|
         |              |              |              |
         |              |(5) ACK       |              |
         |              |---------------------------->|
         |              |              |              |
         |              |(6) 200       |              |
         |              |<-------------|              |
         |              |              |              |
         |(7) 200       |              |              |
         |<-------------|              |              |
         |              |              |              |
         |(8) ACK       |              |              |
         |------------->|              |              |
         |              |              |              |
         |              |(9) ACK       |              |
         |              |------------->|              |         
        </pre> 
   * @throws ParseException 
   */
  @AssertionIds(ids = { "SipServlet:SPEC:ProxyBranch4" }, 
      desc = "Add/remove non system headers from the SipServletRequest"
        + " before the request is proxied. " 
        + "The headers will be found on target UAS.")
  public void testAddNoSysHeader() throws ParseException{
    clientEntryLog();
    // UA2 begin listen request
    ua2.listenRequestMessage();
    SipCall ua2Call = ua2.createSipCall();
    // UA3 begin listen request
    ua3.listenRequestMessage();
    SipCall ua3Call = ua3.createSipCall();
    
    // (1) UA1 Send INVITE
    debug();
    SipCall ua1Call = ua1.createSipCall();
    List<Header> targets = createProxyTargetHeaders(ua1Call);
    boolean result = initiateOutgoingCall(ua1Call, null,
        "AddNoSysHeader", targets, null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), result);
    debug();
    
    // (2) UA2 receive INVITE
    debug();
    assertTrue("Don't receive INVITE", ua2Call.waitForRequest(
        SipRequest.INVITE, waitDuration/2));
    RecordRouteHeader recordRouteHeader = (RecordRouteHeader) ua2Call
        .getLastReceivedRequest().getMessage().getHeader("Record-Route");
    assertEquals("ua2 Record-Route header's parameter isn't param2",
        ((SipUri) recordRouteHeader.getAddress().getURI())
            .getParameter("param2"), "param2");
    debug();
    
    // (3) UA3 receive INVITE
    debug();
    assertTrue("Don't receive INVITE", ua3Call.waitForRequest(
        SipRequest.INVITE, waitDuration / 2));
    recordRouteHeader = (RecordRouteHeader) ua3Call.getLastReceivedRequest()
        .getMessage().getHeader("Record-Route");
    assertEquals("ua3 Record-Route header's parameter isn't param3",
        ((SipUri) recordRouteHeader.getAddress().getURI())
            .getParameter("param3"), "param3");
    
    Header header = ua3Call.getLastReceivedRequest().getMessage().getHeader(
        "header3");
    System.out.println(header.getClass());
    assertEquals("ua3 header3 header's value isn't header3",
        ((SIPHeader) header).getValue(), "header3");
    debug();
    
    // (4) UA3 send 400
    debug();
    ua3Call.sendResponseToLastReceivedRequest(SipResponse.BAD_REQUEST, "ERROR", 0);
    debug();
    
    //(5) 
    debug();
    debug();
    
    // (6) UA2 send 200
    debug();
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);
    debug();
    
    // (7) UA1 receive 200
    debug();
    waitNon100Response(ua1Call, waitDuration/2);
    assertResponseReceived("Don't receive 200", SipResponse.OK, ua1Call);
    debug();
    // (8) UA1 send ACK
    debug();
    assertTrue(ua1Call.sendInviteOkAck());
    debug();
    
    // (9) UA2 receive ACK
    debug();
    assertTrue("Don't receive ACK", ua2Call.waitForRequest(SipRequest.ACK,
        waitDuration / 2));
    debug();
    
    ua1Call.disconnect();
  }
  
  /**
   *  Canceling on the Proxy and ProxyBranches.
      <pre>
        UA1           Proxy           UA2            UA3            UA4
         |              |              |              |              |
         |(1) INVITE    |              |              |              |
         |------------->|              |              |              |
         |              |              |              |              |
         |              |(2) INVITE    |              |              |
         |              |------------->|              |              |
         |              |              |              |              |
         |              |(3) 180       |              |              |
         |              |<-------------|              |              |
         |              |              |              |              |
         |(4) 180       |              |              |              |
         |<-------------|              |              |              |
         |              |              |              |              |
         |              |(5) CANCEL    |              |              |
         |              |------------->|              |              |
         |              |              |              |              |
         |              |(6) 200       |              |              |
         |              |<-------------|              |              |
         |              |              |              |              |
         |              |(7) 487       |              |              |
         |              |<-------------|              |              |
         |              |              |              |              |
         |              |(8) ACK       |              |              |
         |              |------------->|              |              |
         |              |              |              |              |
         |              |(9) INVITE    |              |              |
         |              |---------------------------->|              |
         |              |              |              |              |
         |              |(10) 180      |              |              |
         |              |<----------------------------|              |
         |              |              |              |              |
         |(11) 180      |              |              |              |
         |<-------------|              |              |              |
         |              |              |              |              |
         |              |(12) CANCEL   |              |              |
         |              |---------------------------->|              |
         |              |              |              |              |
         |              |(13) 200      |              |              |
         |              |<----------------------------|              |
         |              |              |              |              |
         |              |(14) 487      |              |              |
         |              |<----------------------------|              |
         |              |              |              |              |
         |              |(15) ACK      |              |              |
         |              |---------------------------->|              |
         |              |              |              |              |
         |              |(16) INVITE   |              |              |
         |              |------------------------------------------->|
         |              |              |              |              |
         |              |(17) 200      |              |              |
         |              |<-------------------------------------------|
         |              |              |              |              |
         |(18) 200      |              |              |              |
         |<-------------|              |              |              |
         |              |              |              |              |
         |(19) ACK      |              |              |              |
         |------------->|              |              |              |
         |              |(20) ACK      |              |              |
         |              |------------------------------------------->|
         |              |              |              |              |
        </pre>
   * @throws ParseException 
   */
  @AssertionIds(ids = { "SipServlet:SPEC:ProxyBranch5" }, 
      desc = "Canceling on the Proxy and ProxyBranches.")
  public void testCancelProxy() throws ParseException{
    clientEntryLog();
    // UA2 begin listen request
    ua2.listenRequestMessage();
    SipCall ua2Call = ua2.createSipCall();
    // UA3 begin listen request
    ua3.listenRequestMessage();
    SipCall ua3Call = ua3.createSipCall();
    // UA3 begin listen request
    ua4.listenRequestMessage();
    SipCall ua4Call = ua4.createSipCall();
    
    // (1) UA1 Send INVITE
    debug();
    SipCall ua1Call = ua1.createSipCall();
    List<Header> targets = createProxyTargetHeaders(ua1Call);
    boolean result = initiateOutgoingCall(ua1Call, null,
        "CancelProxy", targets, null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), result);
    debug();
    
    // (2) UA2 receive INVITE
    debug();
    assertTrue("Don't receive INVITE", ua2Call.waitForRequest(
        SipRequest.INVITE, waitDuration/2));
    debug();
    // (3) UA2 send 180
    debug();
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.RINGING, null, 0);
    debug();
    
    // (4) UA1 receive 180
    debug();
    waitNon100Response(ua1Call, waitDuration/2);
    assertResponseReceived("Don't receive 180", SipResponse.RINGING, ua1Call);
    debug();
    
    // (5) UA2 receive CANCEL
    debug();
    assertTrue("Don't receive INVITE", ua2Call.waitForRequest(
        SipRequest.CANCEL, waitDuration/2));
    debug();
    
    // (6) UA2 send 200 for CANCEL
    debug();
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.OK, "CANCEL", 0);
    debug();

    // (7) UA2 send 487 for CANCEL
    debug();
    ua2Call.sendResponseToLastReceivedRequest(SipResponse.REQUEST_TERMINATED,
        "CANCEL", 0);
    debug();
    
    // (8)
    debug();
    debug();
    
    // (9) UA3 receive INVITE
    debug();
    assertTrue("Don't receive INVITE", ua3Call.waitForRequest(
        SipRequest.INVITE, waitDuration/2));
    debug();
    // (10) UA3 send 180
    debug();
    ua3Call.sendResponseToLastReceivedRequest(SipResponse.RINGING, null, 0);
    debug();
    
    // (11) UA1 receive 180
    debug();
    waitNon100Response(ua1Call, waitDuration/2);
    assertResponseReceived("Don't receive 180", SipResponse.RINGING, ua1Call);
    debug();
    
    // (12) UA3 receive CANCEL
    debug();
    assertTrue("Don't receive INVITE", ua3Call.waitForRequest(
        SipRequest.CANCEL, waitDuration/2));
    debug();
    
    // (13) UA3 send 200 for CANCEL
    debug();
    ua3Call.sendResponseToLastReceivedRequest(SipResponse.OK, "CANCEL", 0);
    debug();

    // (14) UA3 send 487 for CANCEL
    debug();
    ua3Call.sendResponseToLastReceivedRequest(SipResponse.REQUEST_TERMINATED,
        "CANCEL", 0);
    debug();
    
    //(15)
    debug();
    debug();
    
    // (16) UA4 receive INVITE
    debug();
    assertTrue("Don't receive INVITE", ua4Call.waitForRequest(
        SipRequest.INVITE, waitDuration / 2));
    debug();
    
    // (17) UA4 send 200
    debug();
    ua4Call.sendResponseToLastReceivedRequest(SipResponse.OK, "OK", 0);
    debug();
    
    // (18) UA1 receive 200
    debug();
    assertTrue(ua1Call.waitOutgoingCallResponse(waitDuration / 2));
    assertResponseReceived("Don't receive 200", SipResponse.OK, ua1Call);
    debug();
    // (19) UA1 send ACK
    debug();
    assertTrue(ua1Call.sendInviteOkAck());
    debug();
    
    // (20) UA4 receive ACK
    debug();
    assertTrue("Don't receive ACK", ua4Call.waitForRequest(SipRequest.ACK,
        waitDuration / 2));
    debug();
    
    ua1Call.disconnect();
    
  }
  
  /**
   * Add proxy to target addresses into the request header for the proxy servlet
   * redirecting the targets
   * @param call 
   * @return
   * @throws ParseException 
   */
  private List<Header> createProxyTargetHeaders(SipCall call)
      throws ParseException {
    List<Header> additionalHeaders = new ArrayList<Header>(3);
    additionalHeaders.add(call.getHeaderFactory().createHeader("TARGET1",
        ua2URI));
    additionalHeaders.add(call.getHeaderFactory().createHeader("TARGET2",
        ua3URI));
    additionalHeaders.add(call.getHeaderFactory().createHeader("TARGET3",
        ua4URI));
    return additionalHeaders;
  }
  
  private void waitNon100Response(SipCall ua,int wait) {
    int tryTimes = 1;
    int maxTryTimes = 4;
    while (maxTryTimes > tryTimes) {
      if(ua.waitOutgoingCallResponse(wait)){
        logger.debug("response status:"+ua.getReturnCode());
        if(ua.getReturnCode() != Response.TRYING){
          return;
        }
      }
      tryTimes ++;
    }
    return;
  }
  private void debug(){
    if(step%2 != 0){
      logger.debug("----------------"+(step+1)/2+"-------------- begin");
    }else{
      logger.debug("-----------------"+(step)/2+"-------------- end");
    }
    step++;
  }
  public void tearDown() throws Exception {
    super.tearDown();
    ua4.dispose();    
    sipStack4.dispose();

  }
}
