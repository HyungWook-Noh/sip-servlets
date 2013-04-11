/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 *
 * Used to declare sip application 
 */
@javax.servlet.sip.annotation.SipApplication( 
	name = "com.bea.sipservlet.tck.apps.spectestapp.uas",
	displayName = "Spec Assertion Test UAS Application",  //ContextTest will check this value
	mainServlet = "UasMainServlet")
package com.bea.sipservlet.tck.apps.spec.uas;
