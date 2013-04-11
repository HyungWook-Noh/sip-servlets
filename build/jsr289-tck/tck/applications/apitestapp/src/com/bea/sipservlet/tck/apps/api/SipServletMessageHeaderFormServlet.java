/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved.
 * 
 * SipServletMessageHeaderFormServlet is used to test APIs of
 * javax.servlet.sip.SipServletMessage.HeaderForm  
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.annotation.SipServlet;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;

@SipServlet(name = "SipServletMessageHeaderForm")
public class SipServletMessageHeaderFormServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;
	
	@TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValueOf001(SipServletRequest req) {
		serverEntryLog();
		SipServletMessage.HeaderForm headerForm = 
			SipServletMessage.HeaderForm.valueOf("COMPACT");
	  return (headerForm != null 
	  		&& SipServletMessage.HeaderForm.COMPACT.equals(headerForm)) ? 
			  null : "Fail to transfer String to SipServletMessage.HeaderForm";
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testValues001(SipServletRequest req) {
	  
  	boolean isCOMPACT = false;
	  boolean isLONG = false;
	  boolean isDEFAULT = false;
	  
	  serverEntryLog();
	  
	  SipServletMessage.HeaderForm[] headerForms = SipServletMessage.HeaderForm.values();
	  if(headerForms != null){
		  for(SipServletMessage.HeaderForm headerForm : headerForms){
			  if(SipServletMessage.HeaderForm.COMPACT.equals(headerForm)) isCOMPACT = true;
			  if(SipServletMessage.HeaderForm.DEFAULT.equals(headerForm)) isDEFAULT = true;
			  if(SipServletMessage.HeaderForm.LONG.equals(headerForm)) isLONG = true;

		  }
		  return (isCOMPACT && isDEFAULT && isLONG) ?
				  null : "Fail to get all values of SipServletMessage.HeaderForm";
	  }
	  else{
		  return "Get null through SipServletMessage.HeaderForm.values()";
	  }
  }

}
