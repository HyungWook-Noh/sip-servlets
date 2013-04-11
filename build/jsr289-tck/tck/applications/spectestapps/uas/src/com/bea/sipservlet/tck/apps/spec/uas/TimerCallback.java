/*
 * $Id: TimerCallback.java,v 1.3 2002/11/20 22:21:23 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 */

package com.bea.sipservlet.tck.apps.spec.uas;

import java.io.*;

import java.util.Iterator;

import javax.servlet.*;

import javax.servlet.sip.*;
import javax.servlet.sip.annotation.SipListener;

/**

 * TimerListener -- handles timeout notifications for UasActive and

 * UasCancel servlets.

 */

@SipListener
public class TimerCallback implements TimerListener {

  public void timeout(ServletTimer timer) {

    SipSession sipSession = getSipSession(timer.getApplicationSession());

    // invoke timeout() on either UasActive or UasCancel

    if (UasActiveServlet.class.getName().equals(timer.getInfo())) {

      UasActiveServlet uasActive = UasActiveServlet.getInstance();

      if (uasActive != null) {

        uasActive.timeout(timer, sipSession);

      } else {

        // error

      }

    } else if (UasCancelServlet.class.getName().equals(timer.getInfo())) {

      UasCancelServlet uasCancel = UasCancelServlet.getInstance();

      if (uasCancel != null) {

        uasCancel.timeout(timer, sipSession);

      } else {

        // error

      }

    }

  }

  private SipSession getSipSession(SipApplicationSession appSession) {

    Iterator iter = appSession.getSessions("SIP");

    if (iter.hasNext()) {

      return (SipSession) iter.next();

    } else {

      // error

      return null;

    }

  }

}
