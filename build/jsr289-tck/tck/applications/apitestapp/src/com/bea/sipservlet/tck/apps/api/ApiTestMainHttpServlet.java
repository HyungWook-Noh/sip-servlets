/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * ApiTestMainHttpServlet is used in test the APIs of to dispatch http request
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseHttpServlet;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.utils.TestUtil;

public class ApiTestMainHttpServlet extends BaseHttpServlet {
  private static final long serialVersionUID = 1479172144636331019L;
  private static Logger logger = Logger.getLogger(ApiTestMainHttpServlet.class);

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) 
  throws ServletException,
      IOException {
    //dispatch the request to corresponding http servlet
    String servletName = req.getHeader(TestConstants.SERVLET_HEADER);
    if (!TestUtil.hasText(servletName)) {
      logger.warn("*** Servlet-Name header is null, and send back 500! ***");
      setErrorResp(res, "Servlet-Name header is null");
      return;
    }
    RequestDispatcher rd1 = getServletContext().getRequestDispatcher("/" + servletName);
    rd1.forward(req, res);
  }

}
