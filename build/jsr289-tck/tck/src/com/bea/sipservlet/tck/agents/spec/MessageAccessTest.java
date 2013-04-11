/**
*
* (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
* All rights reserved.
*
* MessageAccessTest is used to test the ability of SipServlet application
* to access various components on a given SIP Message.
*
* The corresponding SipServlet is "MessageAccessServlet"
*
*/
package com.bea.sipservlet.tck.agents.spec;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipTransaction;

import javax.sip.ListeningPoint;
import javax.sip.ResponseEvent;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;


@TargetApplication(ApplicationName.UAS)
public class MessageAccessTest extends TestBase {

	public MessageAccessTest(String arg0) throws IOException {
		super(arg0);
	}

	private static Logger logger = Logger.getLogger(MessageAccessTest.class);
	private static final String SERVLET_NAME = "MessageAccess";

	
   /**
	 * Tests the ability of SipServletApplication to access various components
	 * of a given SIP message
	 * 
	 * If everything works fine on the servlet side, an 200 will be received by the UA.
	 * Otherwise, a 500 message with a brief error description will be received.
	 * <pre>
	 * 	 UA1                                 UA (Servlet)
	 * 	 |                                    |
	 * 	 |-------------  MESSAGE  ----------->|
	 * 	 |                                    |
	 * 	 |<------------  200 OK   ------------|
	 * </pre>
	 */

  @AssertionIds(
      ids = { "SipServlet:SPEC:MessageAccess1" }, 
      desc = "Tests the ability to access Message headers and contents")
  public void testMessageAccess() throws Exception {
		clientEntryLog();
		
		// prepare ServerSide expected headers
		List<Header> headers = new ArrayList<Header>(2);
		
		String dateStr = "Sat, 13 Nov 2010 23:29:00 GMT";
		headers.add(ua1.getParent().getHeaderFactory().createHeader("date",
				dateStr));

		headers.add(ua1.getParent().getHeaderFactory().createHeader(
				"tck-test-name", "testMessageAccess"));

		headers.add(ua1.getParent().getHeaderFactory().createHeader(
				"organization", "Acme, Corp."));
		
		headers.add(ua1.getParent().getHeaderFactory().createHeader(
				"expires", "300"));

		headers.add(ua1.getParent().getHeaderFactory().createHeader("ExpectedTo", serverAddr));
		
	    // Build the MESSAGE request for UA1
	    Request ua1Req = assembleRequest(
	        Request.MESSAGE,
	        SERVLET_NAME,
	        null, TestConstants.SERVER_MODE_UA, 1);

	    for (Header header:headers) {
	    	ua1Req.addHeader(header);
	    }
	    
		//	    content-type="text/plain"
		//      content="Hello, World!"
	    ContentTypeHeader contentHeader = ua1.getParent().getHeaderFactory()
				.createContentTypeHeader("text", "plain");
	    String contentStr = "Hello, World!";
	    ua1Req.setContent(contentStr, contentHeader);
	    
	   // set Transport parameter of the request uri
    	String transportParam = ListeningPoint.UDP;

	    SipURI requestURI = (SipURI)ua1Req.getRequestURI();
	    requestURI.setTransportParam(transportParam);

	    //set the host part of the From Header uri
	    FromHeader fromHeader = (FromHeader)ua1Req.getHeader("From");
	    SipURI fromURI = ua1.getParent().getAddressFactory().createSipURI(
				"alice", "example.com");
	    fromHeader.getAddress().setURI(fromURI);
	    fromHeader.getAddress().setDisplayName("Alice");
	    // set Max-Forwards to 70
	    ((MaxForwardsHeader)ua1Req.getHeader("Max-Forwards")).setMaxForwards(70);
	    
	    // (1) ua1 sends the MESSAGE message. 
	    SipTransaction transUA1 = ua1.sendRequestWithTransaction(
	        ua1Req, true, null);
	    assertNotNull(ua1.format(), transUA1);
	    logger.debug("---UA1 sent req is:" + ua1Req + "---");
	     

	    // (2) ua1 receives 200 response
	    EventObject event = ua1.waitResponse(transUA1, waitDuration);
	    assertNotNull(event);
	    
	    // ua1 assert received 200 response
	    if (event instanceof ResponseEvent) {
	      ResponseEvent responseEvent = filterEvent(ua1, transUA1, event);
	      Response response = responseEvent.getResponse();
	      if (response.getStatusCode() != Response.OK) {
	        fail("UA1 received " + response.toString());
	      }

	      assertEquals(Request.MESSAGE, 
	    		  ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod() );
	      
	      logger.debug("---UA1 receive 200/MESSAGE resp :\n" + response + "---");

	    } else {
	      fail("The event for UA1 receiving 200/MESSAGE is null.");
	    }
	}

}
