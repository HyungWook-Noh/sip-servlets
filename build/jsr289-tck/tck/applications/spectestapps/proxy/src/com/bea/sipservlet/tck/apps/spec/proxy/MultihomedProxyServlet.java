/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * MultihomedProxyServlet is used to test: 
 * Proxy.setOutboundInterface(java.net.InetSocketAddress address) 
 * Proxy.setOutboundInterface(java.net.InetAddress address)
 *
 */
package com.bea.sipservlet.tck.apps.spec.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.utils.TestUtil;

@javax.servlet.sip.annotation.SipServlet(name = "MultihomedProxy")
public class MultihomedProxyServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;
	private List<SipURI> outboundList = null; 
	private static final Logger logger = Logger.getLogger(MultihomedProxyServlet.class);

	protected void doAck(SipServletRequest req) 
		throws ServletException, IOException {
		logger.info("=== doAck() in MultihomedProxyServlet. ===");
	}

	protected void doBye(SipServletRequest req) 
		throws ServletException, IOException {
		logger.info("=== doBye() in MultihomedProxyServlet. ===");
	}

	protected void doInvite(SipServletRequest req) 
		throws ServletException, IOException {
		
		logger.info("=== INVITE received by MultihomedProxyServlet. ===");
		
		Proxy proxy = req.getProxy(); 
		
		//	get the outbound interface list from ServletContext
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

		Address route = req.getAddressHeader("Route");
		logger.info("=== Route header is:" + route + " ===");
		
		InetSocketAddress inetSocketAddr = 
			new InetSocketAddress(outboundURI.getHost(), outboundURI.getPort());
    proxy.setOutboundInterface(inetSocketAddr);
    logger.info("=== OutboundInterface set in Proxy with " + inetSocketAddr 
      + " ===");
        
		proxy.setRecordRoute(true); 
		
		// add a Multihomed header for checking outbound interface setting
		req.addHeader("Multihomed", inetSocketAddr.toString());
		
		
		// Test setOutboundInterface(java.net.InetAddress address)
		// Begin
		/*InetAddress inetAddr = InetAddress.getByName(outboundURI.getHost());
    proxy.setOutboundInterface(inetAddr);
    logger.info("=== OutboundInterface set in Proxy with " + inetAddr 
      + " ===");    
		proxy.setRecordRoute(true); 
		req.addHeader("Multihomed", inetAddr.toString());
		
		*/
		// End
		
    proxy.proxyTo(route.getURI());	
	}

	protected void doSuccessResponse(SipServletResponse resp) 
		throws ServletException, IOException {
		
		logger.info("=== doSuccessResponse() in MultihomedProxyServlet. ===");
		
		// Received the 200 OK from UA2.
		if("INVITE".equals(resp.getMethod())){
			logger.info("=== 200 OK for INVITE received by MultihomedProxyServlet." 
				+ " ===");         
    } else if ("BYE".equals(resp.getMethod())){
    	logger.info("=== 200 OK for BYE received by MultihomedProxyServlet." 
    		+	" ===");
    }
			
	}


	
	
	
}
