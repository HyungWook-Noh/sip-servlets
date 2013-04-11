/**
 *(c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 *  All rights reserved.
 *
 * The object to be bound with some sip session
 */
package com.bea.sipservlet.tck.apps.api;

import com.bea.sipservlet.tck.common.TckTestException;
import org.apache.log4j.Logger;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.SipSessionBindingListener;
import javax.servlet.sip.annotation.SipListener;
import java.io.IOException;
import java.io.Serializable;

@SipListener
public class SipSessionBindingObject
    implements SipSessionBindingListener, Serializable {
  private static Logger logger = Logger.getLogger(SipSessionBindingObject.class);
  
  public void valueBound(SipSessionBindingEvent event){
    logger.debug("=== step into SipSessionBindingObject.valueBound ===");
    logger.debug("event.name=" +event.getName());
    SipSession s = event.getSession();
    Object o = s.getAttribute("testValueBound001");
    if("true".equals(o)){
      // clear the object first
      s.removeAttribute("testValueBound001");

      SipServletRequest req = (SipServletRequest)s.getAttribute("request");
      SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
      try{
        resp.send();
      }catch (IOException ex) {
        logger.error("*** IOException occurs during sending response ***",ex);
        throw new TckTestException(ex);
      }
    }
  }

  public void valueUnbound(SipSessionBindingEvent event){
    logger.debug("=== step into SipSessionBindingObject.valueUnbound ===");
    logger.debug("event.name=" +event.getName());
    SipSession sess = event.getSession();

    String testUnbound = (String)sess.getAttribute("testValueUnbound001");
    if("true".equals(testUnbound)){
      //clear the attribute
      sess.removeAttribute("testValueUnbound001");

      SipServletRequest req = (SipServletRequest)sess.getAttribute("request");
      SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);

      try{
        resp.send();
      }catch (IOException ex) {
        logger.error("*** IOException occurs during sending response ***",ex);
        throw new TckTestException(ex);
      }
    }
  }
}
