/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * MultihomedProxyServlet is used to test: 
 * ProxyBranch.setOutboundInterface(java.net.InetSocketAddress address) 
 * ProxyBranch.setOutboundInterface(java.net.InetAddress address)
 *
 */
package com.bea.sipservlet.tck.apps.spec.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.utils.TestUtil;

@javax.servlet.sip.annotation.SipServlet(name = "MultihomedProxyBranch")
public class MultihomedProxyBranchServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;
	private List<SipURI> outboundList = null; 
	private static final Logger logger = 
		Logger.getLogger(MultihomedProxyBranchServlet.class);

	protected void doAck(SipServletRequest req) 
		throws ServletException, IOException {
		logger.info("=== doAck() in MultihomedProxyBranchServlet. ===");
	}

	protected void doBye(SipServletRequest req) 
		throws ServletException, IOException {
		logger.info("=== doBye() in MultihomedProxyBranchServlet. ===");
	}

	protected void doInvite(SipServletRequest req) 
		throws ServletException, IOException {
		
		logger.info("=== INVITE received by MultihomedProxyBranchServlet. ===");
		
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
		
		List<SipURI> targetList =  new ArrayList<SipURI>(1);
		targetList.add((SipURI)route.getURI());
		
		List<ProxyBranch> branches = proxy.createProxyBranches(targetList);
		
		if (branches.size() == 1) {
			InetSocketAddress inetSocketAddr = 
				new InetSocketAddress(outboundURI.getHost(), outboundURI.getPort());
			ProxyBranch branch = proxy.getProxyBranch(route.getURI());
			branch.setRecordRoute(true);
			branch.setOutboundInterface(inetSocketAddr);
			logger.info("=== OutboundInterface set in ProxyBranch with " 
				+ inetSocketAddr.toString() + " ===");
			SipServletRequest fReq = branch.getRequest();
			fReq.addHeader("Multihomed", inetSocketAddr.toString());
			
			// Test setOutboundInterface(java.net.InetAddress address)
			// Begin
			/*InetAddress inetAddr = InetAddress.getByName(outboundURI.getHost());
			ProxyBranch branch = proxy.getProxyBranch(route.getURI());
			branch.setRecordRoute(true);
			branch.setOutboundInterface(inetAddr);
			logger.info("=== OutboundInterface set in ProxyBranch with " 
				+ inetAddr.toString() + " ===");
			SipServletRequest fReq = branch.getRequest();
			fReq.addHeader("Multihomed", inetAddr.toString());
			*/
			// End
		}
		proxy.startProxy();	
	}

	protected void doSuccessResponse(SipServletResponse resp) 
		throws ServletException, IOException {
		
		logger.info("=== doSuccessResponse() in MultihomedProxyBranchServlet. ===");
		
		// Received the 200 OK from UA2.
		if("INVITE".equals(resp.getMethod())){
			logger.info("=== 200 OK/INVITE received by MultihomedProxyBranchServlet." 
				+ " ===");         
    } else if ("BYE".equals(resp.getMethod())){
    	logger.info("=== 200 OK/BYE received by MultihomedProxyBranchServlet." 
    		+	" ===");
    }
	}	
}
