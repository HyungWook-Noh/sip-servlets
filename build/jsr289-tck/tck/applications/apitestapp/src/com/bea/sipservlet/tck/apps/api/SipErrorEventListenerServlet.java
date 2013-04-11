/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved.
 * 
 * SipErrorEventListenerServlet and SEEventListener is used to test the APIs of
 * javax.servlet.sip.SipErrorEvent
 * javax.servlet.sip.SipErrorListener
 * javax.servlet.sip.TimerService
 * 
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.annotation.SipServlet;


import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;

@SipServlet (name = "SipErrorEventListener")
@SipListener
public class SipErrorEventListenerServlet 
	extends BaseServlet implements TimerListener {

	private static final long serialVersionUID = 1L;
	private static Logger logger = 
    Logger.getLogger(SipErrorEventListenerServlet.class);
	
	@Resource
	private TimerService timerService;
	
	protected static final String TIMER_ID = "timerID";
	protected static final String ORIG_REQ = "origReq";
	
	private Address from = null;
  private Address to = null;
  private ServletTimer timer = null;
	
  /*
   * Test SipErrorEvent constructor
   */
	@TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testSipErrorEvent001(SipServletRequest req) {
		
		serverEntryLog();
		SipErrorEvent seEvt = new SipErrorEvent(req, null);
		return null;
			
	}
	
	/*
	 * Test TimerService.createTimer(
	 *                               SipApplicationSession appSession, 
	 *                               long delay, 
	 *                               boolean isPersistent, 
	 *                               java.io.Serializable info)
	 */
	@TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateTimer001(SipServletRequest req) {
		serverEntryLog();
		assert timerService != null;

    from = req.getTo();
    to = req.getFrom();
    
    timer = 
    	timerService.createTimer(req.getApplicationSession(), 50, false, null);
    req.getApplicationSession().setAttribute(ORIG_REQ, req);
    return null;
  }

	/*
	 * Test TimerService.createTimer(
	 * 															 SipApplicationSession appSession, 
	 *                               long delay, 
	 *                               long period, 
	 *                               boolean fixedDelay, 
	 *                               boolean isPersistent, 
	 *                               java.io.Serializable info)
	 */
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateTimer002(SipServletRequest req) {
  	serverEntryLog();
  	assert timerService != null;

    from = req.getTo();
    to = req.getFrom();
    
    timer = timerService.createTimer(req.getApplicationSession(), 50, 5000, false, false, null);
    req.getApplicationSession().setAttribute(ORIG_REQ, req);
    return null;
  }
	
	public void timeout(ServletTimer timer) {
		
		logger.info("=== Enter the timeout() of the timer. ===");
		
		SipApplicationSession appSession = timer.getApplicationSession();
		SipServletRequest req = (SipServletRequest) appSession.getAttribute(ORIG_REQ);
		String header = req.getHeader(TestConstants.METHOD_HEADER);
		// This indicates the timer of testCreateTimer001 or testCreateTimer002 timeout
		if (header.equals("testCreateTimer001") || header.equals("testCreateTimer002")) {
			logger.info("=== timeout() for testCreateTimer001 and testCreateTimer002. ===");
			timer.cancel();
	    SipServletRequest newReq = sipFactory.createRequest(appSession, "MESSAGE", from, to);
	    try {
	    	logger.info("=== newReq:" + newReq +" ===");
	      newReq.send();
	    } catch (IOException e) {
	    	logger.error("*** In timeout(),"
	    			+ " The timer of TimerService were not taken effect.");
	    	throw new TckTestException("Fail to send MESSAGE to client.", e);
	    }   
			
		} else if(header.equals("testNoPrackReceived001") || header.equals("testNoAckReceived001")){
		// If enter this timeout(), it indicates 
		// the container do not implement the noAckReceived() and noPrackReceived()
		// of SipErrorListener.
		// So here send the MESSAGE out to fail the case		
		
			SipServletRequest message;
			try {
				SipURI to = (SipURI)req.getAddressHeader("Contact").getURI();
		  	SipURI from = (SipURI)req.getTo().getURI();
				message = 
					sipFactory.createRequest(timer.getApplicationSession(), "MESSAGE", from, to);
				// Add a special header "SEL" into MESSAGE
				// in order to inform client that the case is failed
				message.addHeader("SEL", "failed");
				message.send();
				logger.error("*** The APIs of SipErrorListener were not invoked by container." 
						+ " So the application timer timeout! ***");
			} catch(Exception e) {
				logger.error("*** In timeout(), "
						+ "SipErrorEventListenerServlet can not send MESSAGE to client. ***", e);
				throw new TckTestException("Fail to send MESSAGE to client.", e);
			} 
		}
		
	}
	
	/*
	 * Test SipErrorListener.noAckReceived(SipErrorEvent ee)
	 */
	@TestStrategy(strategy = TESTSTRATEGY_NORMAL)
	public void testNoAckReceived001(SipServletRequest req) {
		serverEntryLog();
		assert timerService != null;
		
		try {
		
			req.createResponse(200).send();
			
			// ackTimer is longer than the container timer
			ServletTimer ackTimer = 
				timerService.createTimer(req.getApplicationSession(), 40000, false, null);
			req.getApplicationSession().setAttribute(TIMER_ID, ackTimer.getId());
			req.getApplicationSession().setAttribute(ORIG_REQ, req);
		} catch (Exception e) {
			logger.error("*** Fail to test noAckReceived(). ***", e);
			throw new TckTestException("Fail to test noAckReceived().", e);
		}
	}
	
	/*
	 * Test SipErrorListener.noPrackReceived(SipErrorEvent ee)
	 */
	@TestStrategy(strategy = TESTSTRATEGY_NORMAL)
	public void testNoPrackReceived001(SipServletRequest req) {
		serverEntryLog();
		assert timerService != null;
		
		try {
			req.createResponse(183).sendReliably();
			
			// prackTimer is longer than the container timer
			ServletTimer prackTimer = 
				timerService.createTimer(req.getApplicationSession(), 40000, false, null);
			req.getApplicationSession().setAttribute(TIMER_ID, prackTimer.getId());
			req.getApplicationSession().setAttribute(ORIG_REQ, req);
		} catch (Exception e) {
			logger.error("*** Fail to test noPrackReceived(). ***", e);
			throw new TckTestException("Fail to test noPrackReceived().", e);
		}
	}

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateTimer101(SipServletRequest req) {
    serverEntryLog();
    assert timerService != null;
    SipApplicationSession appSession2 = sipFactory.createApplicationSession();
    appSession2.invalidate();
    try {
      timerService.createTimer(appSession2, 50, false, null);
      return "Fail to get IllegalStateException";
    } catch (IllegalStateException  e) {
       return null;   
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateTimer102(SipServletRequest req) {
    serverEntryLog();
    assert timerService != null;
    SipApplicationSession appSession2 = sipFactory.createApplicationSession();
    appSession2.invalidate();
    try {
      timerService.createTimer(appSession2, 50, 5000, false, false, null);
      return "Fail to get IllegalStateException";
    } catch (IllegalStateException  e) {
       return null;   
    }
  }

	@Override
	protected void doBye(SipServletRequest req) throws ServletException, IOException {
		logger.info("=== doBye() ===");
		req.createResponse(200);
	}  
  

}
