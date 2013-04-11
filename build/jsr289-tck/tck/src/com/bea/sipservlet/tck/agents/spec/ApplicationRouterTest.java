
/**
 * @author Copyright (c) 2008 by BEA Systems, Inc. All Rights Reserved.
 * @version 1.0
 * @created 2008-4-10 14:44:36
 *
 */

package com.bea.sipservlet.tck.agents.spec;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.cafesip.sipunit.SipCall;

import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.RouteHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;


public class ApplicationRouterTest extends TestBase {

  public ApplicationRouterTest(String arg0) throws IOException {
    super(arg0);
  }

  @AssertionIds(ids = { "SipServlet:SPEC:AppRouter1" },
      desc = "Container shall keep route-info and region and then send back to router for " +
          "each request, for directive is CONTINUE / REVERSE.")
  public void testApplicationRouterCase1() {
    try {
      applicationRouterCase("sip:sipservlet.spec.approuter.case01");
    } catch (InterruptedException e) {
      fail(e.getMessage());
    } catch (ParseException e) {
      fail(e.getMessage());
    }
  }


  @AssertionIds(ids = { "SipServlet:SPEC:AppRouter2" },
      desc = "AR shall be notified by a list of new deployed applications. \n" +
            "applicationDeployed() is called during AR initialize time. ")
  public void testApplicationRouterCase2() {
    try {
      applicationRouterCase("sip:sipservlet.spec.approuter.case02");
    } catch (InterruptedException e) {
      fail(e.getMessage());
    } catch (ParseException e) {
      fail(e.getMessage());
    }

  }

  @AssertionIds(ids = { "SipServlet:SPEC:AppRouter3" },
      desc = "Application Selection process is aligned with JSR289 description\n" +
          "Directive: CONTINUE, REVERSE (Region shall be reversed), NEW.")
  public void testApplicationRouterCase3() {
    try {
      applicationRouterCase("sip:sipservlet.spec.approuter.case03");
    } catch (InterruptedException e) {
      fail(e.getMessage());
    } catch (ParseException e) {
      fail(e.getMessage());
    }

  }

  @AssertionIds(ids = { "SipServlet:SPEC:AppRouter4" },
      desc = "Subscriber Identity and Routing Region in application are accessiable and " +
          "identical with application router returned value\n" +
          "SipSession.getSubscriberURI()\n" +
          "SipSesision.getRegion()")
  public void testApplicationRouterCase4() {
    try {
      applicationRouterCase("sip:sipservlet.spec.approuter.case04");
    } catch (InterruptedException e) {
      fail(e.getMessage());
    } catch (ParseException e) {
      fail(e.getMessage());
    }
  }

  @AssertionIds(ids = { "SipServlet:SPEC:AppRouter5" },
      desc = "Route Header: getRouteModifier() = ROUTE\n" +
          "* If the first returned route is internal then the container MUST make it " +
          "available to the applications via the SipServletRequest.getPoppedRoute() method " +
          "and ignore the remaining ones.")
  public void testApplicationRouterCase5() {
    try {
      applicationRouterCase("sip:sipservlet.spec.approuter.case05");
    } catch (InterruptedException e) {
      fail(e.getMessage());
    } catch (ParseException e) {
      fail(e.getMessage());
    }
  }

  /*
  @AssertionIds(ids = { "InternalTestOfDefaultAppName" },
      desc = "Test whether system deliver the request to default application.")
  public void testApplicationRouterCase6() {
    try {
      applicationRouterCase("sip:you-will-not-know-the-case");
    } catch (InterruptedException e) {
      fail(e.getMessage());
    } catch (ParseException e) {
      fail(e.getMessage());
    }
  }
  */


  /**
   *
   * @param testCase: the testcase name of application router test.
   * @throws InterruptedException
   * @throws ParseException
   *
   *  * Actually, for all the test regarding application router, what is required on client
   * is to check whether the server returns 200 OK, or 500 error, this is all.
   *
   *     UA          SipServer
          |               |
          |               |
          |(1): INVITE    |
          |-------------->|
          |((2): 500 ERR) |
          |<--------------|
          |(2): 200 OK    |
          |<--------------|
          |(3): ACK       |
          |-------------->|
          |(4): BYE       |
          |-------------->|
          |(5): 200 OK    |
          |<--------------|
          |               |
          |               |
   */

  private void applicationRouterCase(String testCase) throws InterruptedException, ParseException {

    String testCaseUri = testCase + serverURI.substring(serverURI.indexOf("@"));
    SipCall sipCall = ua1.createSipCall();

    //(0) preparation
    HeaderFactory header_factory = sipCall.getHeaderFactory();
    ArrayList<Header> pduHeader = new ArrayList<Header>();

    pduHeader.add(header_factory.createHeader(
        RouteHeader.NAME, "JSR289-TCK<" + serverURI + ";lr>"));

    //(1) send invite
    boolean status = sipCall.initiateOutgoingCall(ua1URI, testCaseUri, null, pduHeader, null, null);
    assertTrue("Initiate outgoing call failed - " + sipCall.format(), status);

    //(2,3) receive invite response (100, 200, or 500)
    do {
      sipCall.waitOutgoingCallResponse(waitDuration * 5);
      assertLastOperationSuccess("sipCall response/INVITE not received correctly - " + sipCall.format(), sipCall);
      if (sipCall.getReturnCode() >= 400)
        fail(sipCall.getReturnCode() + " : " + sipCall.getLastReceivedResponse().getReasonPhrase());
    } while (sipCall.getReturnCode() != Response.OK);

    //(4) send ACK for 200 OK
    sipCall.sendInviteOkAck(pduHeader, null, null);
    assertLastOperationSuccess("sipCall send ACK failed - " + sipCall.format(), sipCall);

    Thread.sleep(100);

    //(5) terminate the call
    sipCall.disconnect(pduHeader, null, null);
    assertLastOperationSuccess("sipCall disconnect failed - " + sipCall.format(), sipCall);

    Thread.sleep(500);
    //(6) receive the 200 OK of BYE
    sipCall.waitForAnswer(waitDuration);
    assertLastOperationSuccess("sipCall 200/BYE not received correctly - " + sipCall.format(), sipCall);
    assertResponseReceived("Did not receive 200 OK on BYE", Response.OK, Request.BYE, 2, sipCall);
  }
}
