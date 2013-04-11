/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * ApplicationName is used to identify the application name 
 *
 */
package com.bea.sipservlet.tck.agents;

import com.bea.sipservlet.tck.common.TestConstants;

public enum ApplicationName {
  UAS(TestConstants.APP_UAS),
  UAC(TestConstants.APP_UAC),
  PROXY(TestConstants.APP_PROXY),
  B2BUA(TestConstants.APP_B2BUA),
  APITESTAPP(TestConstants.APP_APITEST);

  private final String value;
  
  ApplicationName(String value){
    this.value = value;
  }
  public String getValue(){
    return value;
  }

}
