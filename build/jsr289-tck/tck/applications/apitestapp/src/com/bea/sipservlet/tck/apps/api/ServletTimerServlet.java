/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved. 
 *  
 * ServletTimerServlet is used to test the APIs of javax.servlet.sip.ServletTimer.
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;


@SipServlet(applicationName = "com.bea.sipservlet.tck.apps.apitestapp", 
    name = "ServletTimer") 
public class ServletTimerServlet extends BaseServlet{
  
  private static Logger logger = Logger.getLogger(ServletTimerServlet.class);

  private static final long serialVersionUID = 1L;
  
  private static SipServletRequest theReq = null;
  
  private static ServletTimer theTimer = null;

  @Resource
  public TimerService timerService;

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCancel001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession != null) {
      ServletTimer timer = createAppTimer(appSession, 50, 5000, false, false,
          null);
      if (timer != null) {
        String timerId = timer.getId();
        timer.cancel();
        // Check the timer is cancled or not.
        Collection<ServletTimer> timers = appSession.getTimers();
        boolean isTimerCancled = true;
        for (ServletTimer theTimer : timers) {
          if (theTimer.getId().equals(timerId)) {
            isTimerCancled = false;
            break;
          }
        }
        if (isTimerCancled) {
          return null;
        } else {
          logger.error("*** ServletTimer.Cancel() failed. ***");
          return "ServletTimer.Cancel() failed.";
        }
      } else {
        logger.error("*** Create timer failed. ***");
        return "Create timer failed.";
      }
    } else {
      return "The appSession is null.";
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetApplicationSession001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession != null) {
      ServletTimer timer = createAppTimer(appSession, 50, 5000, false, false,
          null);
      if (timer != null) {
        SipApplicationSession session = timer.getApplicationSession();
        if (session != null) {
          String sessionId = session.getId();
          if (appSession.getId().equals(sessionId)) {
            timer.cancel();
            return null;
          } else {
            timer.cancel();
            logger.error("*** ServletTimer.getApplicationSession() "
                + "return the wrong appSession. ***");
            return "ServletTimer.getApplicationSession() "
                + "return the wrong appSession.";
          }
        } else {
          return "Failed to getApplicationSession from ServletTimer.";
        }
      } else {
        logger.error("*** Fail to create ServletTimer. ***");
        return "Fail to create ServletTimer.";
      }
    } else {
      return "The appSession is null.";
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetId001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession != null) {
      ServletTimer timer = createAppTimer(appSession, 50, 5000, false, false,
          null);
      if (timer != null) {
        String timerId = timer.getId();
        timer.cancel();
        if (timerId != null) {
          return null;
        } else {
          logger.error("*** ServletTimer.getId failed. ***");
          return "ServletTimer.getId failed.";
        }
      } else {
        logger.error("*** Fail to create Servlet Timer. ***");
        return "Fail to create Servlet Timer.";
      }
    } else {
      return "The appSession is null.";
    }
  }    

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetInfo001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession != null) {
      ServletTimer timer = createAppTimer(appSession, 50, 5000, false, false,
          (Serializable) new String("GetInfo"));
      if (timer != null) {
        Serializable timerInfo = timer.getInfo();
        timer.cancel();
        if (timerInfo != null && "GetInfo".equals(timerInfo.toString())) {
          return null;
        } else {
          logger.error("*** ServletTimer.getInfo failed. ***");
          return "ServletTimer.getInfo failed.";
        }
      } else {
        logger.error("*** Fail to create Servlet Timer. ***");
        return "Fail to create Servlet Timer.";
      }
    } else {
      return "The appSession is null.";
    }
  }

  public void testGetTimeRemaining001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    theReq = req;
    try {
      req.createResponse(SipServletResponse.SC_OK).send();
    } catch (IOException e) {
      throw new TckTestException(e);
    }
    ServletTimer timer = createAppTimer(appSession, 400, 500, true, false, null);
    theTimer = timer;
    Timer timer01 = new Timer();
    timer01.schedule(new NewTask(), 1200);
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetTimeRemaining002(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession != null) {
      ServletTimer timer = createOneTimeTimer(appSession, 10, false, null);
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new TckTestException(e);
      }
      if (timer != null) {
        long timeRemaining = timer.getTimeRemaining();
        if (timeRemaining < 0) {
          timer.cancel();
          return null;
        } else {
          logger.error("*** getTimeRemaining() should return "
              + "negative value, but not. ***");
          return "getTimeRemaining() should return negative valuem,but not.";
        }
      } else {
        logger.error("*** Fail to create Servlet Timer. ***");
        return "Fail to create Servlet Timer.";
      }
    } else {
      return "The appSession is null.";
    }
  }  
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testScheduledExecutionTime001(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession appSession = req.getApplicationSession();
    if (appSession != null) {
      ServletTimer timer = createAppTimer(appSession, 400, 500, false, false,
          null);
      try {
        Thread.sleep(700);
      } catch (InterruptedException e) {
        throw new TckTestException(e);
      }
      if (timer != null) {
        long scheduledExecutionTime = timer.scheduledExecutionTime();
        timer.cancel();
        if (System.currentTimeMillis() - scheduledExecutionTime >= 0) {
          return null;
        } else {
          logger.error("*** ServletTimer.scheduledExecutionTime() failed. ***");
          return "ServletTimer.scheduledExecutionTime() failed.";
        }
      } else {
        logger.error("*** Timer is not created succesfully. ***");
        return "Timer is not created succesfully.";
      }
    } else {
      return "The appSession is null.";
    }
  }  
  
  /**
   * Create a one-time ServletTimer associated with the appSession with
   * try/catch.
   */
  private ServletTimer createOneTimeTimer(SipApplicationSession appSession,
      long delay, boolean isPersistent, Serializable info) {
    ServletTimer timer = null;
    if (appSession == null) {
      return null;
    }
    try {
      timer = timerService.createTimer(appSession, delay, isPersistent, info);
    } catch (IllegalStateException e) {
      logger.error("*** Thrown IllegalStateException,since app is not valid.",
          e);
      throw new TckTestException(e);
    }
    return timer;
  }
  
  /**
   * Create a repeating ServletTimer associated with the appSession with
   * try/catch.
   */
  private ServletTimer createAppTimer(SipApplicationSession appSession,
      long delay, long period, boolean fixedDelay, boolean isPersistent,
      Serializable info) {
    ServletTimer timer = null;
    if (appSession == null) {
      return null;
    }
    try {
      timer = timerService.createTimer(appSession, delay, period, fixedDelay,
          isPersistent, info);
    } catch (IllegalStateException e) {
      logger.error("*** Thrown IllegalStateException,since app is not valid.",
          e);
      throw new TckTestException(e);
    }
    return timer;
  }
  
  class NewTask extends TimerTask {
    public void run() {
      long timeRemaining = theTimer.getTimeRemaining();
      theTimer.cancel();
      if (timeRemaining >= 0) {
        Address from = (Address) theReq.getTo().clone();
        Address to = (Address) theReq.getFrom().clone();
        try {
          SipServletRequest request = sipFactory.createRequest(
              sipFactory.createApplicationSession(),
              "MESSAGE", from, to);
          request.send();
        } catch (IOException e) {
          throw new TckTestException(e);
        }
      }
    }
  }
}

