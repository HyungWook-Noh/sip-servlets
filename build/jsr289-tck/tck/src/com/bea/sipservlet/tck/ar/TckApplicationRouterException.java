/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 * @version 1.0
 * @created 01-April-2008 17:01:42
 */

package com.bea.sipservlet.tck.ar;

public class TckApplicationRouterException extends Exception {
  public TckApplicationRouterException(String message) {
    super(message);
  }

  public TckApplicationRouterException(String message, Throwable e) {
    super(message, e);
  }
}
