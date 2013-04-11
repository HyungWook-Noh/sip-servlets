/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.  
 *  
 * Rel100ExceptionServlet is used to test the APIs of 
 * javax.servlet.sip.Rel100Exception.
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;

import javax.servlet.sip.Rel100Exception;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;

@SipServlet 
( applicationName="com.bea.sipservlet.tck.apps.apitestapp",
  name="Rel100Exception")
public class Rel100ExceptionServlet extends BaseServlet{
  
  private static Logger logger = Logger.getLogger(Rel100ExceptionServlet.class);

  private static final long serialVersionUID = 1L;

  public void testRel100Exception001(SipServletRequest req) {
    serverEntryLog();
    Rel100Exception ex1 = new Rel100Exception(Rel100Exception.NO_REQ_SUPPORT);
    Rel100Exception ex2 = new Rel100Exception(Rel100Exception.NOT_100rel);
    Rel100Exception ex3 = new Rel100Exception(Rel100Exception.NOT_1XX);
    Rel100Exception ex4 = new Rel100Exception(Rel100Exception.NOT_INVITE);
    Rel100Exception ex5 = new Rel100Exception(Rel100Exception.NOT_SUPPORTED);
    if ((ex1 != null) && (ex2 != null) && (ex3 != null) && (ex4 != null)
        && (ex5 != null)) {
      sendResponse(req, SipServletResponse.SC_OK, "Construct Rel100Exception success.");
    } else {
      logger.error("*** Fail to construct the Rel100Exception. ***");
      sendResponse(req, SipServletResponse.SC_SERVER_INTERNAL_ERROR,
          "Construct Rel100Exception failed.");
    }
  }
  
  public void testGetMessage001(SipServletRequest req) {
    serverEntryLog();
    try {
      req.createResponse(SipServletResponse.SC_RINGING).sendReliably();
      sendResponse(req, SipServletResponse.SC_SERVER_INTERNAL_ERROR,
          "Should throw Rel100Exception,but not.");
    } catch (Rel100Exception e) {
      if (e.getMessage() != null) {
        sendResponse(req, SipServletResponse.SC_OK,
            "The Rel100Exception reason message phrase is: " + e.getMessage());
      } else {
        sendResponse(req, SipServletResponse.SC_SERVER_INTERNAL_ERROR,
            "The Rel100Exception reason message phrase is null.");
      }
    }
  }
  
  /**
   * Test the getReason() for Rel100Exception.
   * (1) NO_REQ_SUPPORT:(If UAC did not support reliable responses extension in the request.) 
   * (2) NOT_100rel:(If SipServletResponse.createPrack() was invoked on a provisional response that is not reliable.)
   * (3) NOT_1XX:(If SipServletResponse.sendReliably() was invoked on a final or a 100 response.)
   * (4) NOT_INVITE:(If SipServletResponse.sendReliably() was invoked for a response to a non-INVITE request.) 
   * (5) NOT_SUPPORTED:(If the container does not support reliable provisional response.)
   */
  
  public void testGetReason001(SipServletRequest req) {
    serverEntryLog();
    // (1) NO_REQ_SUPPORT:(If UAC did not support reliable responses 
    // extension in the request.)
    try {
      req.createResponse(SipServletResponse.SC_RINGING).sendReliably();
      sendResponse(req, SipServletResponse.SC_SERVER_INTERNAL_ERROR,
          "Should throw Rel100Exception,but not.");
    } catch (Rel100Exception e) {
      logger.info("=== The Rel100Exception reason is:" + e.getReason() 
          + "-The reason phrase  -" + e.getMessage() +" ===");
      if (e.getReason() == Rel100Exception.NO_REQ_SUPPORT) {
        sendResponse(req, SipServletResponse.SC_OK, "The reason is NO_REQ_SUPPORT.");
      } else if (e.getReason() == Rel100Exception.NOT_SUPPORTED){
        sendResponse(req, SipServletResponse.SC_OK, "The reason is not NOT_SUPPORTED.");
      } else {
        sendResponse(req,SipServletResponse.SC_SERVER_INTERNAL_ERROR,"Not the correct reason.");
      }
    }
  }
  
  public void testGetReason002(SipServletRequest req) {
    serverEntryLog();
    // (2) NOT_100rel:(If SipServletResponse.createPrack() 
    // was invoked on a provisional response that is not reliable.)
    try {
      req.createResponse(200).send();
      SipURI toURI = (SipURI) req.getFrom().getURI();
      SipURI fromURI = (SipURI) req.getTo().getURI();
      SipServletRequest invite = sipFactory.createRequest(req
          .getApplicationSession(), "INVITE", fromURI, toURI);
      String value = req.getHeader(TestConstants.METHOD_HEADER);
      invite.addHeader(TestConstants.METHOD_HEADER,value);
      invite.send();      
    } catch (IllegalArgumentException e) {
      logger.error("*** Thrown IllegalArgumentException. ***",e);
      throw new TckTestException(e);
    } catch (IllegalStateException e) {
      logger.error("*** Thrown IllegalStateException. ***",e);
      throw new TckTestException(e);
    } catch (IOException e) {
      logger.error("*** Thrown IOException. ***",e);
      throw new TckTestException(e);
    }
  }
    
  public void doProvisionalResponse(SipServletResponse resp) {
    if (resp.getStatus() == SipServletResponse.SC_RINGING) {
      try {
        resp.createPrack().send();
      } catch (Rel100Exception e) {
        logger.error("*** The reason is:" + e.getReason() + "--"
            + e.getMessage() + ".***");
      } catch (IllegalStateException e) {
        logger.error("*** Thrown IllegalStateException. ***", e);
        throw new TckTestException(e);
      } catch (IOException e) {
        logger.error("*** Thrown IOException. ***", e);
        throw new TckTestException(e);
      }
    }
  }
  
  public void doSuccessResponse(SipServletResponse resp){
    String header = resp.getHeader(TestConstants.METHOD_HEADER);
    String value = header.substring(header.indexOf(":") + 1).trim();
    int status = resp.getStatus();
    if (status == SipServletResponse.SC_OK){
      if("testGetReason002".equals(value)){
        try{
          resp.createAck().send();  
        }catch(IllegalStateException e){
          logger.error("*** Thrown IllegalStateException. ***",e);
          throw new TckTestException(e);
        }catch(IOException e){
          logger.error("*** Thrown IOException. ***",e);
          throw new TckTestException(e);          
        }                
      }  
    }    
  } 
   
  public void testGetReason003(SipServletRequest req) {
    serverEntryLog();
    // (3) NOT_1XX:(If SipServletResponse.sendReliably() was invoked on 
    // a final or a 100 response.)
    try {
      req.createResponse(SipServletResponse.SC_OK).sendReliably();
      sendResponse(req, SipServletResponse.SC_SERVER_INTERNAL_ERROR,
          "Should throw Rel100Exception,but not.");
    } catch (Rel100Exception e) {
      logger.info("=== The Rel100Exception reason is:" + e.getReason()
          + "-The reason phrase  -" + e.getMessage() + " ===");
      if (e.getReason() == Rel100Exception.NOT_1XX) {
        sendResponse(req, SipServletResponse.SC_OK, "The reason is NOT_1XX.");
      } else if (e.getReason() == Rel100Exception.NOT_SUPPORTED) {
        sendResponse(req, SipServletResponse.SC_OK,
            "The reason is not NOT_SUPPORTED.");
      } else {
        sendResponse(req, SipServletResponse.SC_SERVER_INTERNAL_ERROR,
            "Not the correct reason.");
      }
    } 
  }
  
  public void testGetReason004(SipServletRequest req) {
    serverEntryLog();
    // (4) NOT_INVITE:(If SipServletResponse.sendReliably() was invoked 
    // for a response to a non-INVITE request.)
    try {
      req.createResponse(SipServletResponse.SC_RINGING).sendReliably();
      sendResponse(req, SipServletResponse.SC_SERVER_INTERNAL_ERROR,
          "There is no Rel100Exception thrown.");
    } catch (Rel100Exception e) {
      logger.info("=== The Rel100Exception reason is:" + e.getReason()
          + "-The reason phrase  -" + e.getMessage() + " ===");
      if (e.getReason() == Rel100Exception.NOT_INVITE) {
        sendResponse(req, SipServletResponse.SC_OK, "The reason is NOT_INVITE.");
      } else if (e.getReason() == Rel100Exception.NOT_SUPPORTED) {
        sendResponse(req, SipServletResponse.SC_OK,
            "The reason is not NOT_SUPPORTED.");
      } else {
        sendResponse(req, SipServletResponse.SC_SERVER_INTERNAL_ERROR,
            "Not the correct reason.");
      }
    }
  }
  
  
  /**
   * Create a response with statusCode for req, and send it with try/catch.
   */
  private void sendResponse(SipServletRequest req, int statusCode, String reasonPhrase){
    try{
      req.createResponse(statusCode,reasonPhrase).send();
    }catch (IllegalArgumentException e) {
      logger.error("*** Thrown IllegalArgumentException. ***",e);
      throw new TckTestException(e);
    } catch (IllegalStateException e) {
      logger.error("*** Thrown IllegalStateException. ***",e);
      throw new TckTestException(e);
    } catch (IOException e) {
      logger.error("*** Thrown IOException. ***",e);
      throw new TckTestException(e);
    }
  }
    
  
}
