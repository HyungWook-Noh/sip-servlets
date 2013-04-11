/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * TckApiTestException is RuntimeException used when error occurs in TCK test 
 */
package com.bea.sipservlet.tck.common;

public class TckTestException extends RuntimeException {
  private static final long serialVersionUID = -4240872952699172974L;

  public TckTestException() {
    super();
  }

  public TckTestException(String message) {
    super(message);
  }

  public TckTestException(String message, Throwable cause) {
    super(message, cause);
  }

  public TckTestException(Throwable cause) {
    super(cause);
  }

}
