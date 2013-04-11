/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved.
 * 
 * AuthInfoServlet is used to test the APIs of
 * javax.servlet.sip.AuthInfo
 * 
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;

@SipServlet(name = "AuthInfo")
public class AuthInfoServlet extends BaseServlet {
  private static final long serialVersionUID = 6643469804969683311L;
  private static Logger logger = Logger.getLogger(AuthInfoServlet.class);

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testAddAuthInfo001(SipServletRequest req) {
  	serverEntryLog();
		
  	if ("MESSAGE".equals(req.getMethod())) {
  		doMessage(req);
  	}
		
  }
  
  public void doMessage(SipServletRequest req) {
  	logger.info("=== doMessage() ===");
  	
  	try {
	  	req.createResponse(200).send();
	  	
	  	SipURI toURI = (SipURI)req.getAddressHeader("From").getURI();
	  	SipURI fromURI = (SipURI)req.getTo().getURI();
	  	
	  	SipServletRequest invite = 
	  		sipFactory.createRequest(req.getApplicationSession(), "INVITE", fromURI, toURI);
	  	invite.send();
  	} catch (Exception e) { 
  		logger.error("*** Fail to handle the MESSAGE. ***", e);
      throw new TckTestException("Fail to handle the MESSAGE.", e);
    }
  
  }

	@Override
	protected void doErrorResponse(SipServletResponse resp) {
		logger.info("=== doErrorResponse() ===");
		try {
			if (resp.getStatus() == SipServletResponse.SC_UNAUTHORIZED) {
				 
				SipServletRequest req = resp.getSession().createRequest("INVITE");
				AuthInfo authInfo = sipFactory.createAuthInfo(); 
	      Iterator<String> realms = resp.getChallengeRealms();
	      while (realms.hasNext()) {
	        String realm = realms.next();
	        authInfo.addAuthInfo(resp.getStatus(), realm, "user1","123456"); 
	      }
	      req.addAuthHeader(resp, authInfo);
	      req.send();
			}
		} catch (Exception e) {
			logger.error("*** Fail to handle the MESSAGE. ***", e);
      throw new TckTestException("Fail to handle the MESSAGE.", e);
		}
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse resp) 
		throws ServletException, IOException {
		logger.info("=== doSuccessResponse() ===");
		try {
			resp.createAck().send();
		}  catch (Exception e) {
			logger.error("*** Fail to handle the MESSAGE. ***", e);
      throw new TckTestException("Fail to handle the MESSAGE.", e);
		}
	}  

}
