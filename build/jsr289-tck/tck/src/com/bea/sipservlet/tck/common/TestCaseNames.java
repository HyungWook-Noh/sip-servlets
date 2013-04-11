/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * TestCaseNames contains the tested feature names used in TCK spec test
 */
package com.bea.sipservlet.tck.common;

public class TestCaseNames {

  // SipServletResponse.setCharacterEncoding does not throw
	// UnsupportedEncodingException
	public static final String TEST_CASE_RESP_SETCHARENC_VALID_CHARSET = 
		"NOT_THROW_UNSUPPORTEDENCODINGEXCEPTION";

	public static final String TEST_CASE_RESP_SETCHARENC_INVALID_CHARSET = 
		"NOT_THROW_UNSUPPORTEDENCODINGEXCEPTION_INVALID_ENCODING";

	public static final String TEST_CASE_PARAMETERABLE = "parameterable";

	public static final String TEST_CASE_MAINSERVLET1 = "mainservlet1";

	public static final String TEST_CASE_MAINSERVLET2 = "mainservlet2";
	
	public static final String TEST_CASE_MAINSERVLET3 = "mainservlet3";
	
	public static final String TEST_CASE_MAINSERVLET4 = "mainservlet4";
	
	public static final String TEST_CASE_MAINSERVLET5 = "mainservlet5";
	
	public static final String TEST_CASE_MAINSERVLET6 = "mainservlet6";
	
	public static final String TEST_CASE_MAINSERVLET10 = "mainservlet10";

	public static final String TEST_CASE_SIPSERVLETLISTENER = "sipservletlistener";
    
  public static final String TEST_CASE_COMMITTED_B2BUA = "committedUnderB2bua";
   
  public static final String TEST_CASE_COMMITTED_PROXY = "committedUnderProxy";

  public static final String TEST_CASE_GETCONTENT1 = "getContent1";

  public static final String TEST_CASE_GETCONTENT2 = "getContent2";

  public static final String TEST_CASE_GETCONTENT3 = "getContent3";

  public static final String TEST_CASE_GETCONTENT4 = "getContent4";
  
  public static final String TEST_CASE_MULTIHOMED1 = "multihomedUAS";
	
	public static final String TEST_CASE_MULTIHOMED2 = "multihomedProxy";
	
	public static final String TEST_CASE_MULTIHOMED3 = "multihomedProxyBranch";
	
	public static final String TEST_CASE_MULTIHOMED4 = "multihomedOutboundInterfaceScopeA";
	
	public static final String TEST_CASE_MULTIHOMED5 = "multihomedOutboundInterfaceScopeB";
	
	public static final String TEST_CASE_MULTIHOMED6 = "multihomedOutboundInterfaceScopeC";
	
	public static final String TEST_CASE_MULTIHOMED7 = "multihomedProxyForRegister";
	
	public static final String TEST_CASE_MULTIHOMED8 = "multihomedAppCompositionA";
	
	public static final String TEST_CASE_MULTIHOMED9 = "multihomedAppCompositionB";
}
