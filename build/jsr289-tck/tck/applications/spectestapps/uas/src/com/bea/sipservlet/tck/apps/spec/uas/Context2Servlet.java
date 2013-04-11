/*
 * $Id: Context2.java,v 1.2 2002/09/03 15:22:53 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 */
/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * Context2Servlet is used to help ContextServlet to test ServletContext's
 * getNamedDispatcher methods,
 *
 */

package com.bea.sipservlet.tck.apps.spec.uas;

import com.bea.sipservlet.tck.apps.BaseServlet;

import javax.servlet.sip.SipServletRequest;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Responds to OPTIONS with 200 response.
 */
@javax.servlet.sip.annotation.SipServlet(name = "Context2")
public class Context2Servlet extends BaseServlet {

  private static Logger logger = Logger.getLogger(ContextServlet.class);

  protected void doOptions(SipServletRequest req) throws IOException {
    logger.info("=== Context: Context2 receive OPTION ===");
    req.createResponse(200).send();
  }
}
