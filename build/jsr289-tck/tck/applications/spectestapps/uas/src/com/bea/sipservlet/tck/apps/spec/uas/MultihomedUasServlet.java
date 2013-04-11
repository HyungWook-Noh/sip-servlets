/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * MultihomedUasServlet is used to test: 
 * SipSession.setOutboundInterface(java.net.InetSocketAddress address) 
 * SipSession.setOutboundInterface(java.net.InetAddress address)
 *
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.utils.TestUtil;

@javax.servlet.sip.annotation.SipServlet(name = "MultihomedUas")
public class MultihomedUasServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;
	private List<SipURI> outboundList = null; 
	private static final Logger logger = Logger.getLogger(MultihomedUasServlet.class);
	
	protected void doAck(SipServletRequest req) 
		throws ServletException, IOException {
		
		logger.info("=== doAck() in MultihomedUasServlet. ===");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.info("=== Here just delay the BYE sending. ===");
		}
		
		logger.info("=== Disconnect UAC... ===");
		
		SipSession sipSession = req.getSession();
		if (sipSession != null) {
			logger.info("=== Send BYE from MultihomedUasServlet. ===");
	    SipServletRequest byeReq = sipSession.createRequest("BYE");
	    byeReq.send();
	  }
	}

	protected void doInvite(SipServletRequest req) 
		throws ServletException, IOException {
		
		serverEntryLog();
		logger.info("=== INVITE received by MultihomedUasServlet. ===");
		logger.info("=== Sending 180 response. ===");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			logger.info("=== Here just delay the response sending for concerning " +
				"the message handling capability of Client side. ===");
		}
		req.createResponse(180).send();
		
		logger.info("=== Sending 200 response. ===");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			logger.info("=== Here just delay the response sending for concerning " +
				"the message handling capability of Client side. ===");
		}
		
		SipSession session = req.getSession();
		
		// get the outbound interface list from ServletContext
		outboundList = (List) getServletContext().getAttribute(
				SipServlet.OUTBOUND_INTERFACES);
		if (outboundList != null) {
			logger.info("=== OutboundList size is:" + outboundList.size() + "===");
		}
		else logger.error("*** OutboundList is null! ***");
		
		SipURI outboundURI = null;
		// if outboundList can not be gotten from ServletContext attribute
		if (outboundList == null) {
			logger.error("*** outboundList was not initialized. ***");
			throw new TckTestException("outboundList was not initialized.");
		}
		//if outboundList has no element
		if (outboundList.size() == 0) {
			logger.error("*** outboundList is empty. ***");
			throw new TckTestException("outboundList is empty.");
		}
		// if outboundList has at least 2 outbound interfaces
		if (outboundList.size() > 1) {
			if (TestUtil.isMultihomed(outboundList)) {
				logger.info("=== The tested container is multihmed!  ===");
			}
		}
		outboundURI = TestUtil.selectOutboundInterface(outboundList);
		
		if (outboundURI == null) {
			logger.error("*** Can not select one OutboundInterface for udp! ***");
			throw new TckTestException("Can not select one OutboundInterface!");
		}
		logger.info("=== OutboundURI get with " + outboundURI + "===");
		
		assert session != null;
		// set outbound SIPURI for SipSession to check Contact header
		InetSocketAddress inetSocketAddr = 
			new InetSocketAddress(outboundURI.getHost(), outboundURI.getPort());
		session.setOutboundInterface(inetSocketAddr);
		logger.info("=== OutboundInterface set in SipSession with " 
			+ inetSocketAddr + " ===");
		SipServletResponse response = req.createResponse(200);
		response.addHeader("Multihomed", inetSocketAddr.toString());
		
		// Test setOutboundInterface(java.net.InetAddress address)
		// Begin
		/*InetAddress inetAddr = InetAddress.getByName(outboundURI.getHost());
		session.setOutboundInterface(inetAddr);
		logger.info("=== OutboundInterface set in SipSession with "
		  + inettAddr + " ==="); 
		SipServletResponse response = req.createResponse(200);
		response.addHeader("Multihomed", inetAddr.toString());
		*/
		// End
		response.send();
		
		
	}

	protected void doSuccessResponse(SipServletResponse resp) 
		throws ServletException, IOException {
		
		logger.info("=== doSuccessResponse() in MultihomedUasServlet. ===");
		
		// Received the 200 OK for BYE which is sent by MultihomedUasServlet.
		if ("BYE".equals(resp.getMethod())) {  
			logger.info("=== 200 OK received by MultihomedUasServlet. ===");
		  resp.getApplicationSession().invalidate();
		} else {
			logger.info("=== Response "+ resp.getStatus()
			  + " received by MultihomedUasServlet. ===");
			super.doResponse(resp);
		}	
	}
	
}
