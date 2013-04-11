/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * This class checks the ability for a servlet to set/modify the contact header
 * on various requests/responses
 *
 */

package com.bea.sipservlet.tck.apps.spec.uas;

import org.apache.log4j.Logger;

import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipFactory;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import java.io.IOException;

@javax.servlet.sip.annotation.SipServlet(name = "ContactHeader")
public class ContactHeaderServlet extends SipServlet {

  public SipFactory sipFactory;
  private static Logger logger = Logger.getLogger(ContactHeaderServlet.class);
  
  

  public Address contactAddress1 = null;

  public Address contactAddress2 = null;

  // remote target address to which the MESSAGE will be sent
  private Address remoteAddress = null;

  // local address that will be used as the From header in the MESSAGE request
  private Address localAddress = null;


  public static final String SPEC_CONTACT_HEADER_TEST = "x-wlss-spec-test-contact-header";
  public static final String INVITE_301 = "Invite301";
  public static final String INVITE_485 = "Invite485";  
  
  /*
   * Initiate and get the SipFactory
   */
  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    sipFactory = (SipFactory) getServletContext().getAttribute(SipServlet.SIP_FACTORY);
    if (sipFactory == null) {
      throw new ServletException("No SipFactory in context");
    }
    logger.info("=== init SessionLifetimeUASServlet ===");

    contactAddress1 = sipFactory.createAddress("<sip:tck@sipservlet.com>;expires=3600");
  }

  @Override
  protected void doRegister(SipServletRequest req) throws ServletException, IOException {
    SipServletResponse reg200Response = req.createResponse(200);

    logger.info("=== received Register ===");

    // add two contact addresses to the 200/Register response
    contactAddress2 = (Address) req.getAddressHeader("contact").clone();
    if (contactAddress2.getExpires() == -1) {
      contactAddress2.setExpires(3600);
    }

    reg200Response.addAddressHeader("contact", contactAddress2, true);
    reg200Response.addAddressHeader("contact", contactAddress1, true);

    logger.info("=== sending 200/REGISTER ===");
    reg200Response.send();
  }


  protected void doInvite(SipServletRequest req) throws ServletException, IOException {

    logger.info("=== received INVITE ===");

    String testPurpose = req.getHeader(SPEC_CONTACT_HEADER_TEST);
    
    if (testPurpose.equalsIgnoreCase(INVITE_301)) {
        // send 301 response
        SipServletResponse resp = req.createResponse(301);
        
        resp.addAddressHeader("contact", contactAddress2, true);
        resp.addAddressHeader("contact", contactAddress1, true);

        resp.send();
        logger.info("=== sending 301 to UA ===");
    
        return;
    }
    
    if (testPurpose.equalsIgnoreCase(INVITE_485)) {
    	
        // send 485 response
        SipServletResponse resp = req.createResponse(485);

        resp.addAddressHeader("contact", contactAddress2, true);
        resp.addAddressHeader("contact", contactAddress1, true);

        resp.send();
        logger.info("=== sending 485 to UA ===");      
        return;
    }
    
  }


  protected void doOptions(SipServletRequest req) throws ServletException, IOException {
    logger.info("=== receive OPTIONS ===");
    // send 200 response
    SipServletResponse resp = req.createResponse(200);

    resp.addAddressHeader("contact", contactAddress2, true);
    resp.addAddressHeader("contact", contactAddress1, true);

    logger.info("=== sending 200/OPTIONS to UA ===");
    resp.send();

  }

  protected void doSuccessResponse(SipServletResponse resp) throws ServletException, IOException {
    if(resp.getMethod().equalsIgnoreCase("REGISTER")){
      logger.info("=== receive 200 for REGISTER ===");
      return;
    }

  }


  
}
