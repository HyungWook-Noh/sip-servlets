/*
 * $Id: RuleTest3.java,v 1.3 2002/09/03 15:22:53 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 */
/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * RuleTest3Servlet is used to test the specification of  Rules
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipServlet;

import com.bea.sipservlet.tck.apps.BaseServlet;

@SipServlet(name = "RuleTest3")
public class RuleTest3Servlet extends BaseServlet {
  public void doRequest(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    if ("RULETEST3".equals(req.getMethod())) {
      req.createResponse(200).send();
    } else {
      req.createResponse(500).send();
    }
  }
}
