/**
 *
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * ContactHeaderTest is used to test the manipulation of contact headers.
 * The corresponding SipServlet is "ContactHeaderServlet"
 *
 */

package com.bea.sipservlet.tck.agents.spec;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.ListIterator;

import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.address.Address;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

@TargetApplication(value = ApplicationName.UAS)
public class ContactHeaderTest extends TestBase {

  private static final Logger logger = Logger.getLogger(ContactHeaderTest.class);
  private static final String SERVLET_NAME = "ContactHeader";
  private static final String SPEC_CONTACT_HEADER_TEST = "x-wlss-spec-test-contact-header";
  private static final String INVITE_301 = "Invite301";
  private static final String INVITE_485 = "Invite485";  
  

  private final String[] expectedContactHeaders = new String[2];
  
  private final Address[] expectedContactHeaderAddresses = new Address[2];
  
  public ContactHeaderTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);

  }

  public void setUp() throws Exception {
	  super.setUp();
	  expectedContactHeaders[0] = "<sip:tck@sipservlet.com>;expires=3600;";
	  expectedContactHeaders[1] = ua1.getContactInfo().getContactHeader().getAddress().toString();
	  expectedContactHeaderAddresses[0] = ua1.getParent().getAddressFactory().createAddress(expectedContactHeaders[0]);
	  expectedContactHeaderAddresses[1] = ua1.getParent().getAddressFactory().createAddress(expectedContactHeaders[1]);	  

	  logger.debug("expectedContactHeaders[0] = " + expectedContactHeaders[0]);
	  logger.debug("expectedContactHeaders[1] = " + expectedContactHeaders[1]);
	  
	  logger.debug("expectedContactHeaderAddresses[0] = " + expectedContactHeaderAddresses[0]);
	  logger.debug("expectedContactHeaderAddresses[1] = " + expectedContactHeaderAddresses[1]);
  }
  
  
  public void testContactheader() throws Exception {
	  checkRegister200();
	  checkOption200();
	  checkInvite301();
	  checkInvite485();
  }
  
  /**
   * <p>
   * Tests the ability of SIP Server to add contacts to 200/OPTIONS response.
   * </p>
   * <pre>
    UA1                      UAS
      |                       |
      |----- (1) OPTION  ---->|
      |<---- (2) 200  --------|           
   * </pre>
   * 
   */
  @AssertionIds(
          ids = {"SipServlet:SPEC:ContactHeader1"},
          desc = "Tests the ability of SIP Server to set contact header(s) for non-system header")  
  public void checkOption200() throws Exception {
	    clientEntryLog();
	    
	    // Build the OPTIONS request for UA1
	    Request ua1Req = assembleRequest(
	        Request.OPTIONS,
	        SERVLET_NAME,
	        null, TestConstants.SERVER_MODE_UA, 1);

	    // (1) UA1 sends the OPTIONS message. 
	    SipTransaction transUA1 = ua1.sendRequestWithTransaction(
	        ua1Req, true, null);
	    assertNotNull(ua1.format(), transUA1);
	    logger.debug("--- UA1 sent req is:" + ua1Req + "---");
	     

	    // (2) UA1 receives 200/OPTIONS response
	    EventObject event = ua1.waitResponse(transUA1, waitDuration);
	    assertNotNull(event);
	    
	    // UA1 assert received 200/OPTIONS response
	    if (event instanceof ResponseEvent) {
	      ResponseEvent responseEvent = filterEvent(ua1, transUA1, event);
	      Response response = responseEvent.getResponse();
	      if (response.getStatusCode() != Response.OK) {
	        fail("UA1 did not receive 200/REGISTER response from UA2.");
	      }

	      assertEquals(Request.OPTIONS, 
	    		  ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod() );
	      
	      logger.debug("--- UA1 receive 200/OPTION resp :\n" + response + "---");

	      checkContactsInMessage(responseEvent);

	    } else {
	      fail("The event for UA1 receive 200/OPTIONS is null.");
	    }

  }

  
  /**
   * <p>
   * Tests the ability of SIP Server to add contacts to 200/REGISTER response.
   * </p>
   * <pre>
    UA1                     UAS
     |                       |
     |----- (1) REGISTER---->|
     |<---- (2) 200----------|
    </pre>
   * 
   */
  @AssertionIds(
          ids = {"SipServlet:SPEC:ContactHeader1"},
          desc = "Tests the ability of SIP Server to set contact header(s) for non-system header")  
  public void checkRegister200() throws Exception {
	    clientEntryLog();
	    
	    // Build the REGISTER request for UA1
	    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
	    Request ua1SendRegisterReq = assembleRequest(
	        Request.REGISTER,
	        SERVLET_NAME,
	        null, TestConstants.SERVER_MODE_UA, 1);

	    // (1) UA1 sends the REGISTER message. 
	    // The contact header will be added to the request by SipUNIT automatically while sending out
	    SipTransaction transUA1 = ua1.sendRequestWithTransaction(
	        ua1SendRegisterReq, true, null);
	    assertNotNull(ua1.format(), transUA1);
	    logger.debug("---UA1 send REGISTER req is:" + ua1SendRegisterReq + "---");


	    // (2) UA1 receives 200/REGISTER response
	    EventObject event = ua1.waitResponse(transUA1, waitDuration);
	    assertNotNull(event);
	    // UA1 assert received 200/REGISTER response
	    if (event instanceof ResponseEvent) {
	      ResponseEvent responseEvent = filterEvent(ua1, transUA1, event);
	      Response response = responseEvent.getResponse();
	      if (response.getStatusCode() != Response.OK) {
	        fail("UA1 did not receive 200/REGISTER response from UA2.");
	      }
	      logger.debug("---UA1 receive 200/REGISTER resp is:" + response + "---");

	      checkContactsInMessage(responseEvent);

	    } else {
	      fail("The event for UA1 receive 200/REGISTER is null.");
	    }

  }
  

  /**
   * <p>
   * Tests the ability of SIP Server to add contacts to 3xx/INVITE response.
   * </p>
   * <pre>
   UA1                     UAS
     |                      |
     |----- (1)INVITE------>|
     |<---- (2)301 ---------|
     |------(3)ACK -------->|
    </pre>
   * 
   */
  @AssertionIds(
          ids = {"SipServlet:SPEC:ContactHeader1"},
          desc = "Tests the ability of SIP Server to set contact header(s) for non-system header")  
  public void checkInvite301() throws Exception {
    clientEntryLog();

    List<Header> headers = new ArrayList<Header>();
    HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();
    // additional header which indicates the purpose of this invite
    headers.add(headerFactory.createHeader(SPEC_CONTACT_HEADER_TEST, INVITE_301));

    // (1) UA1 send invite
    SipCall ua1Call = ua1.createSipCall();

    boolean status_ok = initiateOutgoingCall(ua1Call, null, SERVLET_NAME, headers, null, null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), status_ok);

    // (2) UA1 wait for 301 response
    waitNon100Response(ua1Call, waitDuration / 2);
    assertResponseReceived("Failed to receive 301", SipResponse.MOVED_PERMANENTLY, ua1Call);

    logger.debug("--- received response ---\n" + ua1Call.getLastReceivedResponse());

    // check the contact headers in the 301 response
    checkContactsInMessage(ua1Call.getLastReceivedResponse());

  }

  
  /**
   * <p>
   * Tests the ability of SIP Server to add contacts to 4xx/INVITE response.
   * </p>
   * <pre>
   UA1                     UAS      
     |----- (1) INVITE------>|
     |<---- (2) 485 ---------|
     |------(3)ACK --------->|
    </pre>
   * 
   */
  @AssertionIds(
          ids = {"SipServlet:SPEC:ContactHeader1"},
          desc = "Tests the ability of SIP Server to set contact header(s) " 
          	+	"for non-system header")  
  public void checkInvite485() throws Exception {
    clientEntryLog();

    List<Header> headers = new ArrayList<Header>();
    HeaderFactory headerFactory = ua1.getParent().getHeaderFactory();

    // additional header which indicates the purpose of this invite
    headers.add(headerFactory.createHeader(SPEC_CONTACT_HEADER_TEST, INVITE_485));

    // (1)  UA1 send invite
    SipCall ua1Call = ua1.createSipCall();

    boolean status_ok = 
    	initiateOutgoingCall(ua1Call, null, SERVLET_NAME, headers, null, null);
    assertTrue("Initiate outgoing call failed - " + ua1Call.format(), status_ok);

    // (2)  UA1 wait for 485 response
    waitNon100Response(ua1Call, waitDuration / 2);
    assertResponseReceived("Failed to receive 485", SipResponse.AMBIGUOUS, ua1Call);

    logger.debug("--- received response ---\n" + ua1Call.getLastReceivedResponse());

    // check the contact headers in the 485 response
    checkContactsInMessage(ua1Call.getLastReceivedResponse());

  }

  
  private void checkContactsInMessage(SipResponse res ) {
	  // check if there're two contacts contained in this response

	  ListIterator<ContactHeader> itr = null;
	  itr = res.getMessage().getHeaders(ContactHeader.NAME);

	  checkedReceivedContactHeaderAddresses(itr);
	  
  }

  private void checkContactsInMessage(EventObject event) {
	  // check if there're two contacts contained in this Event Object

	  ListIterator<ContactHeader> itr = null;
	  if (event instanceof ResponseEvent) {
		  ResponseEvent resEvent = (ResponseEvent)event;
		  itr = resEvent.getResponse().getHeaders(ContactHeader.NAME);
	  } else {
		  RequestEvent reqEvent = (RequestEvent)event;
		  itr = reqEvent.getRequest().getHeaders(ContactHeader.NAME);
		  
	  }

	  checkedReceivedContactHeaderAddresses(itr);
  }

private void checkedReceivedContactHeaderAddresses(ListIterator<ContactHeader> itr) {
	ArrayList<Address> receivedContactHeaderAddresses = new ArrayList<Address>();

	  while(itr.hasNext()){
		  ContactHeader header = itr.next();
		  receivedContactHeaderAddresses.add(header.getAddress());
	  }

	  assertEquals("2 contact addresses should be received", 2, 
	  	receivedContactHeaderAddresses.size());
	  for (int i = 0; i<2; i++) {
		  Address receivedContactAddress = receivedContactHeaderAddresses.get(i);
		  assertTrue("The received address " + receivedContactAddress 
		  		+ " is not the same as expected address " + expectedContactHeaderAddresses[i] , 
		  		expectedContactHeaderAddresses[i].equals(receivedContactAddress) );
	  }
}
  
  private void waitNon100Response(SipCall ua,int wait) {
    int tryTimes = 1;
    int maxTryTimes = 4;
    while (maxTryTimes > tryTimes) {
	    if(ua.waitOutgoingCallResponse(wait)){
	      logger.debug("--- response status:"+ua.getReturnCode());
	      if(ua.getReturnCode() != Response.TRYING){
	         return;
	       }
	    }
	    tryTimes ++;
    }
    return;
  }

  
  
}
