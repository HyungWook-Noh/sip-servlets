/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * MultihomedProxyForRegisterServlet is used to check PATH header of REGISTER
 * under PROXY mode when tested container is multihomed
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
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.utils.TestUtil;

@javax.servlet.sip.annotation.SipServlet(name = "MultihomedProxyForRegister")
public class MultihomedProxyForRegisterServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;
	private List<SipURI> outboundList = null; 
	private static final Logger logger = 
		Logger.getLogger(MultihomedProxyForRegisterServlet.class);

	@Override
	protected void doRegister(SipServletRequest req) 
		throws ServletException, IOException {
		
		logger.info("=== REGISTER received by MultihomedProxyForRegisterServlet. ===");
		
		Proxy proxy = req.getProxy(); 
		
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
		
		Address route = req.getAddressHeader("Route");
		logger.info("=== Route header is:" + route + " ===");
		// set outbound SIPURI for Proxy to check Via header
		InetSocketAddress inetSocketAddr = 
			new InetSocketAddress(outboundURI.getHost(), outboundURI.getPort());
    proxy.setOutboundInterface(inetSocketAddr);
    logger.info("=== OutboundInterface set in Proxy with " + inetSocketAddr 
    	+ " ===");
    proxy.setAddToPath(true);
        
		proxy.setRecordRoute(true); 
		// add a Multihomed header for checking outbound interface setting
		req.addHeader("Multihomed", inetSocketAddr.toString());
		
		// Test setOutboundInterface(java.net.InetAddress address)
		// Begin
		/*InetAddress inetAddr = InetAddress.getByName(outboundURI.getHost());
    proxy.setOutboundInterface(inetAddr);
    logger.info("=== OutboundInterface set in Proxy with " + inetAddr
    	+ "===");
    proxy.setAddToPath(true);
        
		proxy.setRecordRoute(true); 
		// add a Multihomed header for checking outbound interface setting
		req.addHeader("Multihomed", inetAddr.toString());
		*/
		// End
		
		//FIXME: Limited by the SIPUnit capability, the route.getURI() can not be revised 
		// to an appropriate request-URI for REGISTER
		proxy.proxyTo(route.getURI());	
	}

	protected void doSuccessResponse(SipServletResponse resp) 
		throws ServletException, IOException {
		
		logger.info("=== doSuccessResponse() in MultihomedProxyForRegisterServlet. ===");
		
		// Received the 200 OK from UA2.
		if("REGISTER".equals(resp.getMethod())){
			logger.info("=== 200 OK for REGISTER. === ");         
    } else {
      logger.info("=== 200 OK for" + resp.getMethod() + " received. ===");
    }	
	}
}
