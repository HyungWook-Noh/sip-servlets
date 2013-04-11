/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.
 *  
 * SipApplicationSessionServlet is used to test the APIs of
 * javax.servlet.sip.SipApplicationSession.
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Resource;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionBindingEvent;
import javax.servlet.sip.SipApplicationSessionBindingListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;

@SipServlet(applicationName = "com.bea.sipservlet.tck.apps.apitestapp", 
    name = "SipApplicationSession")
public class SipApplicationSessionServlet extends BaseServlet implements
    SipApplicationSessionBindingListener {

  private static final long serialVersionUID = -1652672419697066869L;

  private static Logger logger = Logger
      .getLogger(SipApplicationSessionServlet.class);

  @Resource
  private TimerService timerService;

  private boolean isValueUnbound = false;  
  
  public void testEncodeURI001(SipServletRequest req) {
    // Do not need test, deprecated since V1.1
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testEncodeURL001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    URL oUrl = createURL("http://java.sun.com/index.html");
    if (appSession.isValid()) {
      try {
        URL url = appSession.encodeURL(oUrl);
        if (url != null) {
          return null;
        } else {
          logger.error("*** The encoded URL is null. ***");
          return "The encoded URL is null.";
        }
      } catch (IllegalStateException e) {
        return logShouldNotThrowIllegalStateException();
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testEncodeURL101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    URL oUrl = createURL("http://java.sun.com/index.html");
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null){
      return invalidateResult;
    } else{
      if (oUrl != null) {
        try {
          appSession.encodeURL(oUrl);
          return logShouldThrowIllegalStateException();
        } catch (IllegalStateException e) {
          return null;
        }
      } else {
        logger.error("*** The url for encodeURL is null. ***");
        return "The url for encodeURL is null.";
      }
    }   
  }  

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetApplicationName001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    String appName = appSession.getApplicationName();
    if ("com.bea.sipservlet.tck.apps.apitestapp".equals(appName)) {
      return null;
    } else {
      logger.error("*** Return the wrong appName which should be "
          + "com.bea.sipservlet.tck.apps.apitestapp. ***");
      return "Return the wrong appName which should be "
          + "com.bea.sipservlet.tck.apps.apitestapp.";
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAttribute001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.isValid()) {
      try {
        String name = "testGetAttribute001";
        String value = "setAttribute";
        appSession.setAttribute(name, value);
        String theValue = (String) appSession.getAttribute(name);
        if (value.equals(theValue)) {
          appSession.removeAttribute(name);
          return null;
        } else {
          appSession.removeAttribute(name);
          logger.error("*** Should return the object bound with "
              + "the specified name in this session, "
              + "but return the wrong object. ***");
          return "Should return the object bound with "
              + "the specified name in this session, "
              + "but return the wrong object.";
        }
      } catch (IllegalStateException e) {
        return logShouldNotThrowIllegalStateException();
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAttribute002(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.isValid()) {
      try {
        Object value = appSession.getAttribute("md7(34_=^%21?][{");
        if (value == null) {
          return null;
        } else {
          logger.error("*** Should return null, since no object is bound "
              + "under the name. ***");
          return "Should return null, since no object is bound under the name.";
        }
      } catch (IllegalStateException e) {
        return logShouldNotThrowIllegalStateException();
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAttribute101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String name = "testGetAttribute101";
    appSession.setAttribute(name, "setAttribute");
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null) {
      return invalidateResult;
    } else {
      try {
        appSession.getAttribute(name);
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAttributeNames001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.isValid()) {
      try {
        String name = "af0*9dd*^&_*&";
        appSession.setAttribute(name, "AA");
        Iterator<String> itr = appSession.getAttributeNames();
        while (itr.hasNext()) {
          String str = itr.next();
          if (name.equals(str)) {
            appSession.removeAttribute(name);
            return null;
          }
        }
        logger.error("*** Do not return correct objects bound to "
            + "this session. ***");
        return "Do not return correct objects bound to this session.";
      } catch (IllegalStateException e) {
        return logShouldNotThrowIllegalStateException();
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }

 @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetAttributeNames101(SipServletRequest req) {
    serverEntryLog();    
    SipApplicationSession appSession = sipFactory.createApplicationSession(); 
    appSession.setAttribute("testGetAttributeNames101S1", "AA");
    appSession.setAttribute("testGetAttributeNames101S2", "BB");
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null){
      return invalidateResult;
    } else{
      try {
        appSession.getAttributeNames();
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      } 
    }
  } 

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetCreationTime001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.isValid()) {
      try {
        long creationTime = appSession.getCreationTime();
        if (creationTime >0) {
          return null;
        } else {
          logger.error("*** Return the wrong creation time. ***");
          return "Return the wrong creation time.";
        }
      } catch (IllegalStateException e) {
        return logShouldNotThrowIllegalStateException();
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetCreationTime101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null) {
      return invalidateResult;
    } else {
      try {
        appSession.getCreationTime();
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetExpirationTime001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.isValid()) {
      try {
        long expirationTime = appSession.getExpirationTime();
        if (expirationTime >= 0) {
          return null;
        } else {
          logger.error("*** Return the wrong expiration time. ***");
          return "Return the wrong expiration time.";
        }
      } catch (IllegalStateException e) {
        return logShouldNotThrowIllegalStateException();
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetExpirationTime101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null) {
      return invalidateResult;
    } else {
      try {
        appSession.getExpirationTime();
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetId001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    String id = appSession.getId();
    if (id != null) {
      return null;
    } else {
      logger.error("*** Return the wrong id. ***");
      return "Return the wrong id.";
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetLastAccessedTime001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    long lastAccessTime = appSession.getLastAccessedTime();
    if (lastAccessTime > 0) {
      return null;
    } else {
      logger.error("*** Return the wrong last access time. ***");
      return "Return the wrong last access time.";
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSessions001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.isValid()) {
      Iterator<?> sessions = appSession.getSessions();
      if (sessions != null && sessions.hasNext()) {
        return null;
      } else {
        logger.error("*** Should not return null. ***");
        return "Should not return null.";
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSessions101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null) {
      return invalidateResult;
    } else {
      try {
        appSession.getSessions();
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSessions002(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.isValid()) {
      Iterator<?> sessions = appSession.getSessions("SIP");
      if (sessions != null && sessions.hasNext()) {
        return null;
      } else {
        logger.error("*** Should not return null. ***");
        return "Should not return null.";
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSessions102(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null) {
      return invalidateResult;
    } else {
      try {
        appSession.getSessions("SIP");
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSessions103(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.isValid()) {
      try {
        Iterator<?> sessions = appSession.getSessions("Ada&21*_&1");
        logger.error("*** Should throw java.lang.IllegalArgumentException ,"
            + "since the protocol is not understood by container.***");
        return "Should throw java.lang.IllegalArgumentException ,"
            + "since the protocol is not understood by container.";
      } catch (IllegalArgumentException e) {
        return null;
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSipSession001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.isValid()) {
      SipSession session = appSession.getSipSession("_&(y98sdf98(8_");
      if (session == null) {
        return null;
      } else {
        logger.error("*** Should return null if not found. ***");
        return "Should return null if not found.";
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSipSession002(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.isValid()) {
      Iterator<?> sessions = appSession.getSessions("SIP");
      if (sessions.hasNext()) {
        SipSession s = (SipSession) sessions.next();
        String id = s.getId();
        SipSession session = appSession.getSipSession(id);
        if (id.equals(session.getId())) {
          return null;
        } else {
          logger.error("*** Return the wrong sip session. ***");
          return "Return the wrong sip session.";
        }
      } else {
        logger.error("*** There is no any sip session. ***");
        return "There is no any sip session.";
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }  

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSipSession101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null) {
      return invalidateResult;
    } else {
      try {
        SipSession session = appSession.getSipSession("id");
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetTimer001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    ServletTimer tm = createAppTimer(appSession, 50, 5000, false, false, null);
    if (tm != null) {
      String timerId = tm.getId();
      try {
        ServletTimer timer = appSession.getTimer(timerId);
        if (timer != null) {
          timer.cancel();
          return null;
        } else {
          logger.error("*** Can not get time by getTimer(String id). ***");
          tm.cancel();
          return "Can not get time by getTimer(String id).";
        }
      } catch (IllegalStateException e) {
        tm.cancel();
        return logShouldNotThrowIllegalStateException();
      }

    } else {
      return "Failed to create ServletTimer.";
    }
  }

 @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetTimer101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null) {
      return invalidateResult;
    } else {
      try {
        ServletTimer tm = appSession.getTimer("timer");
        //if come here exception should be thrown, but timer should be cancel firstly
        tm.cancel();
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }
 

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetTimers001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    ServletTimer timer = createAppTimer(appSession, 50, 5000, false, false,
        null);
    if(timer == null){
      return "Failed to create ServletTimer.";
    }
    if (appSession.isValid()) {
      Collection<ServletTimer> timers = appSession.getTimers();
      for(ServletTimer t : timers){
        if(t.getId().equals(timer.getId())){
          timer.cancel();
          return null;
        }
      }
      timer.cancel();
      return "SipApplicationSession.getTimers() can not get the correct times.";
    } else {
      timer.cancel();
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetTimers101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    ServletTimer timer = createAppTimer(appSession, 50, 5000, false, false,
        null);
    String invalidateResult = invalidateAppSession(appSession);
    try {
      if (invalidateResult != null) {
        timer.cancel();
        return invalidateResult;
      } else {
        try {
          appSession.getTimers();
          //if come here exception should be thrown, but timer should be cancel firstly
          timer.cancel();
          return logShouldThrowIllegalStateException();
        } catch (IllegalStateException e) {          
          return null;
        }
      }
    } finally {
      timer.cancel();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testInvalidate001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    return invalidateResult;
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testInvalidate002(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    SipApplicationSessionServlet servlet = new SipApplicationSessionServlet();
    appSession.setAttribute("testInvalidate002Servlet", servlet);
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null){
      return invalidateResult;
    } else{
      if (servlet.isValueUnbound) {
        return null;
      } else {
        logger.error("*** Objects not unbound from this appSession "
            + "when it has been invalidated.***");
        return "Objects not unbound from this appSession "
            + "when it has been invalidated.";
      }
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testInvalidate101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null){
      return invalidateResult;
    } else{
      try {
        appSession.invalidate();
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }    

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testIsValid001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();    
    if (appSession.isValid()) {
      appSession.invalidate();      
      return null;
    } else {
      logger.error("*** The new created appSession should be valid," 
          + "but appSession.isValid() return false. ***");
      return "The new created appSession should be valid," 
          + "but appSession.isValid() return false.";
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testRemoveAttribute001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if(appSession.isValid()){
      try{
        appSession.setAttribute("RemoveAttribute", "RES");
        appSession.removeAttribute("RemoveAttribute");
        Object removedAttribute = appSession.getAttribute("RemoveAttribute");
        if (removedAttribute == null) {
          return null;
        } else {
          logger.error("*** The attribute has not been removed. ***");
          return "The attribute has not been removed.";
        }
      } catch(IllegalStateException e){
        return logShouldNotThrowIllegalStateException();
      }
    }else{
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testRemoveAttribute101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    appSession.setAttribute("removeATTR", "ASS");
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null){
      return invalidateResult;
    } else{
      try {
        appSession.removeAttribute("removeATTR");
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }  

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetAttribute001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.isValid()){
      try {
        String name = "testSetAttribute001";
        appSession.setAttribute(name , "AAS");
        appSession.setAttribute(name, "BBS");
        String value = (String) appSession.getAttribute(name);
        if ("BBS".equals(value)) {
          appSession.removeAttribute("testSetAttribute001");
          return null;
        } else {
          logger.error("*** setAttribute does not work properly. ***");
          return "setAttribute does not work properly.";
        }
      } catch (IllegalStateException e) {
        return logShouldNotThrowIllegalStateException();
      } 
    }else{
      return logAppSessionIsNotValid();
    }
  }

 @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetAttribute101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null){
      return invalidateResult;
    } else{
      try {
        appSession.setAttribute("setAttribute", "AAS");
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetAttribute102(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    try {
      appSession.setAttribute(null, "setAttribute");
      logger.error("*** Should throw java.lang.NullPointerException, "
          + "since the name is null.***");
      return "Should throw java.lang.NullPointerException, "
          + "since the name is null.";
    } catch (NullPointerException e) {
      return null;
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetAttribute103(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    try {
      appSession.setAttribute("ASS", null);
      logger.info("*** Should throw java.lang.NullPointerException, "
          + "since the attribute is null.***");
      return "Should throw java.lang.NullPointerException,"
          + "since the attribute is null.";
    } catch (NullPointerException e) {
      return null;
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetExpires001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    if(appSession.isValid()){
      try{
        appSession.setExpires(3000);
        return null;
      }catch(IllegalStateException e){
        return logShouldNotThrowIllegalStateException();
      }
    }else{
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetExpires101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null) {
      return invalidateResult;
    } else {
      try {
        appSession.setExpires(1000);
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSession001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if(appSession.isValid()){
      try {
        Object ob = appSession.getSession("_&(&(^(#(E$__",
            SipApplicationSession.Protocol.SIP);
        if (ob == null) {
          return null;
        } else {
          logger.error("*** Should return null,"
              + "if the session can not be found.***");
          return "Should return null,if the session can not be found.";
        }
      } catch (IllegalStateException e) {
        return logShouldNotThrowIllegalStateException();
      }
    }else{
      return logAppSessionIsNotValid();
    }
  } 

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSession002(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession.isValid()) {
      SipSession sipSession = req.getSession();
      String sipSessionId = sipSession.getId();
      try {
        Object ob = appSession.getSession(sipSessionId,
            SipApplicationSession.Protocol.SIP);
        if (ob != null) {
          SipSession session = (SipSession) ob;
          if (sipSessionId.equals(session.getId())) {
            return null;
          } else {
            logger.error("***Get the wrong sipSession by "
                + "getSession(String id, Protocol protocol).***");
            return "Get the wrong sipSession by "
                + "getSession(String id, Protocol protocol).";
          }
        } else {
          logger.error("*** Failed to "
              + "getSession((String id, Protocol protocol)).***");
          return "Failed to getSession((String id, Protocol protocol)).";
        }
      } catch (IllegalStateException e) {
        return logShouldNotThrowIllegalStateException();
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSession101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();    
    SipSession sipSession = req.getSession();
    String sipSessionId = sipSession.getId();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null){
      return invalidateResult;
    } else{
      try {
        appSession.getSession(sipSessionId, SipApplicationSession.Protocol.SIP);
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }  

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testIsReadyToInvalidate001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    if (appSession.isValid()) {
      try {
        // Make all the contained SipSessions are in ready-to-invalidate state
        Iterator<?> sipSessions = appSession.getSessions("SIP");
        while (sipSessions.hasNext()) {
          SipSession sipSession = (SipSession) sipSessions.next();
          sipSession.invalidate();
        }
        // Make all ServletTimers associated with the appSession inactive.
        Collection<ServletTimer> timers = appSession.getTimers();
        for (ServletTimer timer : timers) {
          timer.cancel();
        }
        
        if (appSession.isReadyToInvalidate()) {
          return null;
        } else {
          logger.error("*** Should return true, since appSession is in a "
              + "ready-to-invalidate state. ***");
          return "Should return true, since appSession is in a "
              + "ready-to-invalidate state";
        }
      } catch (IllegalStateException e) {
        return logShouldNotThrowIllegalStateException();
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testIsReadyToInvalidate101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null) {
      return invalidateResult;
    } else {
      try {
        boolean isReadyToInvalidtate = appSession.isReadyToInvalidate();
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }    

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetInvalidateWhenReady101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null) {
      return invalidateResult;
    } else {
      try {
        appSession.setInvalidateWhenReady(true);
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }  

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetGetInvalidateWhenReady001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession newAppSession = sipFactory.createApplicationSession();
    if (newAppSession != null && newAppSession.isValid()) {
      try {
        newAppSession.setInvalidateWhenReady(true);
        boolean isInvalidateWhenReady1 = newAppSession.getInvalidateWhenReady();
        newAppSession.setInvalidateWhenReady(false);
        boolean isInvalidateWhenReady2 = newAppSession.getInvalidateWhenReady();
        if (isInvalidateWhenReady1 && !isInvalidateWhenReady2) {
          return null;
        } else {
          logger.error("*** Can not get the correct state of "
              + "ready-to-invalidate. ***");
          return "Can not get the correct state of ready-to-invalidate.";
        }
      } catch (IllegalStateException e) {
        return logShouldNotThrowIllegalStateException();
      } finally {
        if (newAppSession.isValid()) {
          newAppSession.invalidate();
        }
      }
    } else {
      return logAppSessionIsNotValid();
    }
  }   

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetInvalidateWhenReady101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = sipFactory.createApplicationSession();
    String invalidateResult = invalidateAppSession(appSession);
    if (invalidateResult != null) {
      return invalidateResult;
    } else {
      try {
        appSession.getInvalidateWhenReady();
        return logShouldThrowIllegalStateException();
      } catch (IllegalStateException e) {
        return null;
      }
    }
  }  
  

  
  public void valueBound(SipApplicationSessionBindingEvent event) {
    //
  }

  public void valueUnbound(SipApplicationSessionBindingEvent event) {
    if (event != null) {
      SipApplicationSession appSession = event.getApplicationSession();
      if (appSession != null) {
        Object value = appSession.getAttribute("testInvalidate002Servlet");
        if (value == null) {
          isValueUnbound = true;
        }
      }
    }
  }
  
  
  private String logShouldThrowIllegalStateException() {
    logger.error("*** Should throw IllegalStateException "
        + "since the appSession has been invalidated. ***");

    return "Should throw IllegalStateException since the appSession has been "
        + "invalidated.";
  }

  
  private String logShouldNotThrowIllegalStateException() {
    logger.error("*** Should not throw IllegalStateException "
        + "since the appSession is still valid. ***");
    return "Should not throw IllegalStateException since the appSession is "
        + "still valid.";
  }
  
  private String logAppSessionIsNotValid() {
    logger.error("*** The appSession is not valid. ***");
    return "The appSession is not valid.";
  }
  
  
  /**
   * Create a ServletTimer associated with the appSession with try/catch.
   */
  private ServletTimer createAppTimer(SipApplicationSession appSession,
      long delay, long period, boolean fixedDelay, boolean isPersistent,
      Serializable info) {    
    if (appSession == null) {
      return null;
    } 
      
    ServletTimer timer = null;    
    try {
      timer = timerService.createTimer(appSession, delay, period, fixedDelay,
          isPersistent, info);
      return timer;
    } catch (IllegalStateException e) {
      logger.error("*** Thrown IllegalStateException," 
          + "timerService.createTimer() is operated on an" 
          + " invalidate appSession.***", e);
      throw new TckTestException(e);
    }    
  }
  

  private URL createURL(String spec) {
    try {
      return new URL(spec);      
    } catch (MalformedURLException e) {
      logger.error("*** Thrown MalformedURLException "
          + "during new URL(String spec).***");
      throw new TckTestException(e);
    }
  }
  
  /**
   * Call appSession.invalidate with try/catch.
   * Return null if successfully invalidate the appSession or return the failed 
   * reason.
   * (1) return "The appSession is null.",  
   *            if appSession == null
   * (2) return "Fail to invalidate the appSession.", 
   *            if appSession is still valid after appSession.invalidate().
   * (3) return null 
   *            if appSession is actually invalidated by appSession.invalidate().
   * (4) return "The appSession has already been invalidated."
   *            if the appSession is not valid before appSession.invalidate().
   */
  private String invalidateAppSession(SipApplicationSession appSession) {
    if (appSession == null) {
      return "The appSession is null.";
    } else if (appSession.isValid()) {
      try {
        appSession.invalidate();
        if (appSession.isValid()) {
          return "Fail to invalidate the appSession.";
        } else {
          return null;
        }
      } catch (IllegalStateException e) {
        return logShouldNotThrowIllegalStateException();
      }
    } else {
      return "The appSession has already been invalidated.";
    }
  }  
}

