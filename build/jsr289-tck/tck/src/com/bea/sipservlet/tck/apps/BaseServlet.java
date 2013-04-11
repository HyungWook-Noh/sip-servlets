/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 * 
 * Used to implement some basic and common functionalities
 */

package com.bea.sipservlet.tck.apps;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;
import javax.servlet.sip.ServletParseException;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.utils.TestUtil;

public class BaseServlet extends SipServlet {
  private static final long serialVersionUID = 497416976481246306L;

  private static Logger logger = Logger.getLogger(BaseServlet.class);

  public static SipFactory sipFactory;

  protected static final int TESTSTRATEGY_NORMAL = 1;
  protected static final int TESTSTRATEGY_SIMPLEASSERT = 2;
  protected static final int TESTSTRATEGY_SIMPLEASSERT_PROXY = 3;

  /**
   * Abstract the method header and forward the request to the specified method
   * @param req
   */
  @Override
  public void doRequest(SipServletRequest req) throws IOException, ServletException {
    String methodName = req.getHeader(TestConstants.METHOD_HEADER);
    if (!TestUtil.hasText(methodName)) {
      logger.warn("*** testMethodName header is null, super.doRequest" + " is invoked ***");
      super.doRequest(req);
      return;
    }

    Method method = null;
    try {
      method = getClass().getMethod(methodName, SipServletRequest.class);
    } catch (SecurityException e) {
      logger.error("*** SecurityException when finding method \"" + methodName + "\" ***", e);
      throw new TckTestException(e);
    } catch (NoSuchMethodException e) {
      logger.error("*** NoSuchMethodException when finding method \"" + methodName + "\" ***", e);
      throw new TckTestException(e);
    }
    int strategy = TESTSTRATEGY_NORMAL;
    if (method.isAnnotationPresent(TestStrategy.class)) {
      strategy = method.getAnnotation(TestStrategy.class).strategy();
    }
    switch (strategy) {
    case TESTSTRATEGY_SIMPLEASSERT:
      handleStrategySimpleAssert(method, req);
      break;
    case TESTSTRATEGY_SIMPLEASSERT_PROXY:
      handleStrategySimpleAssertProxy(method, req);
      break;
    case TESTSTRATEGY_NORMAL:
    default:
      handleStrategyNormal(method, req);
      break;
    }
  }

  /* 
   * Initiate and get the SipFactory
   * @param req
   */
  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    sipFactory = (SipFactory) getServletContext().getAttribute("javax.servlet.sip.SipFactory");
    if (sipFactory == null) {
      throw new ServletException("No SipFactory in context");
    }
    log("init InterfaceTestServlet");
  }

  /**
   * Check the sip request is related to TCK test
   * @param req
   * @return
   */
  protected boolean isTckRelatedMessage(SipServletRequest req) {
    String tckValue = req.getHeader(TestConstants.SERVER_HEADER);
    if (tckValue == null) {
      return false;
    }
    if (tckValue.indexOf(TestConstants.TCK) != -1) {
      return true;
    }
    return false;
  }

  /**
   * Get the test case name 
   * @param req
   * @return
   */
  protected String getTestCaseName(SipServletRequest req) {
    String tckValue = req.getHeader(TestConstants.SERVER_HEADER);
    String[] tokens = tckValue.split("/");
    if (tokens.length != 2) {
      return null;
    }
    return tokens[1];
  }

  /**
   * Get the user part
   * @param uri
   * @return
   */
  protected String getUser(URI uri) {

    if (uri.isSipURI()) {
      return ((SipURI) uri).getUser();
    } else {
      return null;
    }
  }

  /**
   * Process the normal strategy 
   * @param method
   * @param req
   */
  protected void handleStrategyNormal(Method method, SipServletRequest req) {
    assert method != null && req != null : "Input method or sipServletRequest is not valid";

    try {
      method.invoke(this, req);
    } catch (IllegalArgumentException e) {
      logger.error("*** IllegalArgumentException when invoking method \"" + method.getName()
          + "\" ***", e);
      throw new TckTestException(e);
    } catch (IllegalAccessException e) {
      logger.error("*** IllegalAccessException when invoking method \"" + method.getName()
          + "\" ***", e);
      throw new TckTestException(e);
    } catch (InvocationTargetException e) {
      logger.error("*** InvocationTargetException when invoking method \"" + method.getName()
          + "\" ***", e);
      throw new TckTestException(e);
    }
  }

  /**
   * Process the simple assert strategy 
   * @param method
   * @param req
   */
  protected void handleStrategySimpleAssert(Method method, SipServletRequest req) {
    assert method != null && req != null : "Input method or sipServletRequest is not valid";

    try {
      String reason = (String) method.invoke(this, req);
      if (TestUtil.hasText(reason)) {
        req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, reason).send();
      } else {
        req.createResponse(SipServletResponse.SC_OK).send();
      }
    } catch (IllegalArgumentException e) {
      logger.error("*** IllegalArgumentException when invoking method \"" + method.getName()
          + "\" ***", e);
      throw new TckTestException(e);
    } catch (IllegalStateException e) {
    	logger.error("*** IllegalStateException when invoking method \"" + method.getName()
          + "\" ***", e);
      throw new TckTestException(e);
    } catch (IllegalAccessException e) {
      logger.error("*** IllegalAccessException when invoking method \"" + method.getName()
          + "\" ***", e);
      throw new TckTestException(e);
    } catch (InvocationTargetException e) {
      logger.error("*** InvocationTargetException when invoking method \"" + method.getName()
          + "\" ***", e);
      throw new TckTestException(e);
    } catch (IOException e) {
      logger.error("*** IOException when sending response ***", e);
      throw new TckTestException(e);
    }
  }

  /**
   * Process the simple assert for proxy strategy 
   * @param method
   * @param req
   */
  protected void handleStrategySimpleAssertProxy(Method method, SipServletRequest req) {
    assert method != null && req != null : "Input method or sipServletRequest is not valid";

    try {
      Proxy proxy = getRequestProxy(req);
      String reason = (String) method.invoke(this, req);
      if (TestUtil.hasText(reason)) {
        req.addHeader(TestConstants.TEST_FAIL_REASON, reason);
      }
      proxy.proxyTo(req.getRequestURI());
    } catch (IllegalArgumentException e) {
      logger.error("*** IllegalArgumentException when invoking method \"" + method.getName()
          + "\" ***", e);
      throw new TckTestException(e);
    } catch (IllegalStateException e) {
    	logger.error("*** IllegalStateException when invoking method \"" + method.getName()
          + "\" ***", e);
      throw new TckTestException(e);
    } catch (IllegalAccessException e) {
      logger.error("*** IllegalAccessException when invoking method \"" + method.getName()
          + "\" ***", e);
      throw new TckTestException(e);
    } catch (InvocationTargetException e) {
      logger.error("*** InvocationTargetException when invoking method \"" + method.getName()
          + "\" ***", e);
      throw new TckTestException(e);
    } 
  }
  
  /**
   * Get the proxy
   * @param req
   */
  protected static Proxy getRequestProxy(SipServletRequest req){
    try {
      return req.getProxy();
    } catch (TooManyHopsException e) {
      logger.error("*** TooManyHopsException when retrieving Proxy from SipServletRequest ***", e);
      throw new TckTestException(e);
    }
  }

  /**
   * Only be used at the entry of xxxServlet's testXXX() method. 
   */
  public void serverEntryLog(){
      StackTraceElement stack = (new Exception().getStackTrace())[1];
      Logger.getLogger(getClass()).info("=== "+stack.getMethodName()+"() ===");      
  }

  protected SipServletRequest createRequestByOriReq(SipServletRequest req)
      throws ServletParseException{
    logger.debug("=== begin to create a request...===");
    URI fromURI = req.getRequestURI();
    String toURIStr = req.getHeader("From");
    logger.debug("=== the toURI=" + toURIStr + "===");
    return sipFactory.createRequest(
              req.getApplicationSession(true),
              "MESSAGE",
              fromURI.toString(),
              toURIStr
              );
  }
  
}
