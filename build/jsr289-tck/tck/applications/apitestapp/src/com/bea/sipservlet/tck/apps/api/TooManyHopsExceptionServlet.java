/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * TooManyHopsExceptionServlet is used to test the APIs of 
 * javax.servlet.sip.TooManyHopsException
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.TooManyHopsException;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;

@javax.servlet.sip.annotation.SipServlet(name = "TooManyHopsException")
public class TooManyHopsExceptionServlet extends BaseServlet{
  private static final long serialVersionUID = -8308268791508149592L;

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testTooManyHopsException001(SipServletRequest req) {
    serverEntryLog();
    
	  String msg = "this is a TooManyHopsException";
	  Exception emb = new Exception();
	  TooManyHopsException exception1 = new TooManyHopsException();
	  TooManyHopsException exception2 = new TooManyHopsException(msg);
	  TooManyHopsException exception3 = new TooManyHopsException(msg, emb);
	  TooManyHopsException exception4 = new TooManyHopsException(emb);
	  
	  return (exception1 != null 
	      && exception2 != null  
	      && exception3 != null  
	      && exception4 != null) ?
			  null : "Fail to construct TooManyHopsException";
  }
}
