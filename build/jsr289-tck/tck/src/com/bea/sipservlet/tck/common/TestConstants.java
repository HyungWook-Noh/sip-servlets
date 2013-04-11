/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * TestConstants contains constants used in TCK test
 */
package com.bea.sipservlet.tck.common;

public class TestConstants {
  public static final String TEST_FAIL_REASON = "Fail-Reason";
  public static final String SERVER_MODE_PROXY = "proxy";
  public static final String SERVER_MODE_UA = "ua";
  
  /*
   * TEST_RESULT_HEADER
   */  
  public static final String TEST_RESULT = "Test-Result";
  /*
   * TEST_RESULT_HEADER OK VALUE
   */    
  public static final String TEST_RESULT_OK = "OK";
  /*
   * TEST_RESULT_HEADER FAILED VALUE
   */    
  public static final String TEST_RESULT_FAIL = "FAILED";
  

  public static final String REGISTER_EXPIRES_HEADER = "Expires";
  /*
   * METHOD_HEADER
   */
  public static final String METHOD_HEADER = "Method-Name";
  /*
   * Servlet-Name header, its value should keep the same as 
   * the value of the servlet name.
   */
  public static final String SERVLET_HEADER = "Servlet-Name";  

  /**
   * Application-Name header identifies the application to which the request will
   * be sent.
   */
  public static final String APP_HEADER = "Application-Name";
  /**
   * Application-Key header identifies the application target key the application
   * receiving it should return.
   */
  public static final String APPKEY_HEADER = "Application-Key";
  /**
   * Test-Step header indentifies the test step of the message call flow of the case
   */
  public static final String TEST_STEP_HEADER = "Test-Step";

  /**
   * application names
   */
  // name of api test app
  public static final String APP_APITEST = "com.bea.sipservlet.tck.apps.apitestapp";
  // name of UAC app, for spec test
  public static final String APP_UAC = "com.bea.sipservlet.tck.apps.spectestapp.uac";
  // name of UAS app, for spec test
  public static final String APP_UAS = "com.bea.sipservlet.tck.apps.spectestapp.uas";
  // name of Proxy app, for spec test
  public static final String APP_PROXY = "com.bea.sipservlet.tck.apps.spectestapp.proxy";
  // name of B2bua app, for spec test
  public static final String APP_B2BUA = "com.bea.sipservlet.tck.apps.spectestapp.b2bua";
  
  public static final String PRIVATE_URI = "private-uri";
  
  public static final String UA2_URI = "ua2-uri";
  
  public static final String UA3_URI = "ua3-uri";

  // the following 2 constants are used in SPEC test
  // the header used by client and server which identify the sip message is a
  // TCK message
  public static final String SERVER_HEADER = "Server";
  public static final String TCK = "TCK";

  public static final String USERNAME = "UserName";

  public static final String PASSWORD = "Password";
}
