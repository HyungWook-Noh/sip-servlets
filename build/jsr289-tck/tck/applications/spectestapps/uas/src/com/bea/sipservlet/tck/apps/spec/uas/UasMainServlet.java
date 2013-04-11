/**
 * 
 * (c) 2007-08 by BEA Systems, Inc., or its suppliers, as applicable. 
 * All Rights Reserved.
 * 
 * This class is the main servlet in the JSR 289 javaDoc API assertion test
 * application. It is responsible to dispatch the requests to different servlet
 * according the Servlet-Name header value.
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TestConstants;

@javax.servlet.sip.annotation.SipServlet(name = "UasMainServlet")
public class UasMainServlet extends BaseServlet {
  private static final long serialVersionUID = -8834813875572692727L;
  private static Logger logger = Logger.getLogger(UasMainServlet.class);

  /**
   * Dispatch the initial request to proper servlet. If the request is not an
   * initial request, 500 response will be sent back.
   * @param req
   * @throws IOException
   * @throws ServletException
   */
  public void doRequest(SipServletRequest req)
      throws IOException, ServletException {
    if(req.isInitial()) {
      String handler = getServletName(req);
      
      if(handler == null){
        logger.error("*** no handler found in the request! ***");
        SipServletResponse resp = 
            req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
              "no Servlet-Name header or header value is null");
        resp.send();
        return;
      }
      
      logger.info("=== forward the request to the servlet, name = " + handler + " ===");
      SipSession session = req.getSession();
      session.setHandler(handler);
      RequestDispatcher dispatcher = 
          getServletContext().getNamedDispatcher(handler);
      dispatcher.forward(req,null);

    }else{
      //received subsequent request, send 500 resp back
      logger.info("=== unexpected subsequent request is received by main servlet:"
          + "method = " + req.getMethod()
          + "test case = " + getTestCaseName(req) + " ===");

      req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
         "servlet dispatching error : main servlet received subsequent request");
      req.send();
    }
  }

  /**
   * In theory the response will be sent to the proper servlet directly. If main
   * servlet received the response, it simply logs the information and does nothing
   * @param resp
   * @throws IOException
   * @throws ServletException
   */
  public void doResponse(SipServletResponse resp)
      throws IOException, ServletException {
    // main servlet should not receive any response!
    // Simply log and do nothing.
    logger.info("=== main servlet received response:" + resp.getMethod() + "/"
        + resp.getStatus() + " ===");

    super.doResponse(resp);
  }

  private String getServletName(SipServletRequest req){
    return req.getHeader(TestConstants.SERVLET_HEADER);
  }
}
