/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * AddressingTest is used to test the specification of addressing.
 *
 */
package com.bea.sipservlet.tck.agents.spec;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.EventObject;

import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.TimeoutEvent;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

@TargetApplication(ApplicationName.UAS)
public class AddressingTest extends TestBase{
  private static final String SERVLET_NAME = "Addressing";
  private static Logger logger = Logger.getLogger(AddressingTest.class);


  public AddressingTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
  }

  private void logCaseStep(int step){
  	logger.debug("---   Step " + step + "   ---");
  }

  /**
   * <p>
   * Tests ability to create addressing constrcuts: Address, URI, SipURI, 
   * TelURL. Also tests that these behave like they're supposed to. 
   * A 200 is returned if everything OK, otherwise a 500 with a hopefully 
   * informative reason phrase.
   * </p>
   * <pre>
 UA1            UAS
  |              |        
  |(1) MESSAGE   |        
  |------------->|        
  |(2) 200       |        
  |<-------------|        
  |              |        
   * </pre>
   * 
   */
  @AssertionIds(
      ids = {"SipServlet:SPEC:Addressing"},
      desc = "Tests ability to create addressing constrcuts: Address, URI, SipURI, TelURL. "
        + " Also tests that these behave like they're supposed to. A 200 is returned if "
        + "everything OK, otherwise a 500 with a hopefully informative reason phrase.")
  public void testAddressing() throws SipException,  ParseException, InvalidArgumentException, InterruptedException{
  	clientEntryLog();
    SipTransaction ua1TX;
    ua2.listenRequestMessage();


    // step (1), ua1 send message
    {
      logCaseStep(1);
      this.serverURI = "sip:" + "proxy-gen2xx" + "@" + serverHost + ":" + serverPort;
      Request message = assembleRequest(Request.MESSAGE, SERVLET_NAME, null,
          TestConstants.SERVER_MODE_UA, 1);

      HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
      message.setContent("What's up?", headerFactory.createContentTypeHeader("text", "plain"));

      ua1TX = ua1.sendRequestWithTransaction(message, true, null);
      assertNotNull(ua1.format(), ua1TX);
      assertLastOperationSuccess(ua1);
    }

    // step (2), ua1 receive 200
    {
      logCaseStep(2);
      Thread.sleep(waitDuration / 5);
      EventObject waitResponse = ua1.waitResponse(ua1TX, waitDuration);
      assertFalse("Operation timed out", waitResponse instanceof TimeoutEvent);
      assertEquals("Should have received OK", Response.OK,
          ((ResponseEvent) waitResponse).getResponse().getStatusCode());
    }
  }
}
