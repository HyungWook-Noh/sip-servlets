/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved.
 * 
 * SEEventListener is used to test the APIs of
 * javax.servlet.sip.SipErrorEvent
 * javax.servlet.sip.SipErrorListener
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.annotation.SipListener;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;

@SipListener
public final class SEEventListener 
	extends BaseServlet implements SipErrorListener {

	private static final long serialVersionUID = 1L;
	private static Logger logger = 
    Logger.getLogger(SEEventListener.class);


	public void noAckReceived(SipErrorEvent ee) {
		logger.info("=== SEEventListener.noAckReceived() ===");
		SipServletRequest evtReq = ee.getRequest();
		
		logger.info("=== request received in noAckReceived() :"+evtReq);
		if (evtReq != null) {
			
			String testedAPI2 = evtReq.getHeader(TestConstants.METHOD_HEADER);
			if (testedAPI2.equals("testNoAckReceived001")) {
				// Get the original request which has the METHOD_HEADER "testMethodName"
				SipApplicationSession appSession = evtReq.getApplicationSession();
				SipServletRequest origReq = 
					(SipServletRequest) appSession.getAttribute(SipErrorEventListenerServlet.ORIG_REQ);
				
				if (origReq != null) {
					// Get the METHOD_HEADER separately
					String testedAPI1 = origReq.getHeader(TestConstants.METHOD_HEADER);
					
					
					if (testedAPI1 != null && testedAPI2 != null) {
						// If the API gotten from original req match the API gotten from event req
						// then cancel the corresponding timer
						if (testedAPI1.equals(testedAPI2)) {
						
							String timerID = 
								(String) appSession.getAttribute(SipErrorEventListenerServlet.TIMER_ID);
							
							ServletTimer timer = appSession.getTimer(timerID);
							timer.cancel();
							appSession.removeAttribute(SipErrorEventListenerServlet.TIMER_ID);
							
							try {
								
								if (testedAPI1.equals("testNoAckReceived001")) {
									SipURI toURI = (SipURI)origReq.getAddressHeader("Contact").getURI();
							  	SipURI fromURI = (SipURI)origReq.getTo().getURI();
							  	
									SipServletRequest message;
									
									message = 
										sipFactory.createRequest(timer.getApplicationSession(), "MESSAGE", fromURI, toURI);
									// Add a special header "SEL" into MESSAGE
									// in order to inform client that the case is successful
									if (ee.getResponse() == null) {
										logger.info("=== ee.getResponse() is null ===");
										message.addHeader(
											"SEL", 
											"noAckReceived succeeded, getRequest() succeeded, getResponse() failed");
									} else {
										logger.info("=== ee.getResponse() is not null ===");
										message.addHeader(
											"SEL", 
											"noAckReceived succeeded, getRequest() succeeded, getResponse() succeeded");
									}
									logger.info("=== testNoAckReceived001: " 
										+	"send MESSAGE with private header SEL back to UAC ===");
									logger.info("== message:" + message + " ===");
									message.send();
								}
								
								
							} catch(IOException e) {
								logger.error("*** "
									+ "SEEventListener can not send MESSAGE to client. ***", e);
								throw new TckTestException(
									"SEEventListener can not send MESSAGE to client.", e);
							} catch (ServletParseException e) {
								logger.error("*** "
										+ "SEEventListener can not send MESSAGE to client. ***", e);
								throw new TckTestException(
										"SEEventListener can not send MESSAGE to client.", e);
							}
						}
					}
				} else {
					logger.error("The original request gotten from SipApplicationSession" 
						+ " is null!" );
					throw	new TckTestException("The original request gotten from " 
						+ "SipApplicationSession is null!");
				}
			} else {
				logger.info("=== noAckReceived() was invoked by other cases. ===");
			}
		} else {
			logger.error("The request gotten via getRequest() is null!" );
			logger.error("Can not get the request via getRequest() of SipErrorEvent.");
			throw	new TckTestException(
				"Can not get the request via getRequest() of SipErrorEvent.");
		}
	}

	public void noPrackReceived(SipErrorEvent ee) {
		logger.info("=== SEEventListener.noPrackReceived() ===");
			
		SipServletRequest evtReq = ee.getRequest();
		logger.info("=== request received in noPrackReceived() :"+evtReq);
		try {
			SipApplicationSession appSession = evtReq.getApplicationSession();
			String timerID = 
				(String) appSession.getAttribute(SipErrorEventListenerServlet.TIMER_ID);
			ServletTimer timer = appSession.getTimer(timerID);
			timer.cancel();
			appSession.removeAttribute(SipErrorEventListenerServlet.TIMER_ID);
			SipServletResponse res = 
				evtReq.createResponse(500, "noPrackReceived() invoked");
			res.addHeader("SEL", "successful");
			logger.info(
				"=== send 500 response with private header SEL back to UAC ===");
			res.send();
		} catch (IOException e) {
			logger.error("*** "
					+ "SEEventListener can not send 500 to client. ***", e);
			throw new TckTestException("SEEventListener can not send 500 to client.", e);
			
		}
	}

}
