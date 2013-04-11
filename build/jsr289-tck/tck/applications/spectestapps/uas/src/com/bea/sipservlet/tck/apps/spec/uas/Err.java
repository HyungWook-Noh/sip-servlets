/*
 * $Id: Err.java,v 1.2 2002/09/03 15:22:53 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 */
/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * err is used to record errant message
 */

package com.bea.sipservlet.tck.apps.spec.uas;

public class Err extends RuntimeException {

  public Err(String msg) {

    super(msg);

  }

}
