/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * BaseHttpServlet is the main http servlet in the JSR 289 javaDoc API assertion test
 * application.
 */
package com.bea.sipservlet.tck.apps;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.utils.TestUtil;

public class BaseHttpServlet extends HttpServlet {
  private static final long serialVersionUID = 8521487636026840432L;
  private static Logger logger = Logger.getLogger(BaseHttpServlet.class);

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    String methodName = req.getHeader(TestConstants.METHOD_HEADER);
    if (!TestUtil.hasText(methodName)) {
      logger.warn("*** testMethodName header is null, and send back 500! ***");
      setErrorResp(res, "testMethodName header is null");
      return;
    }

    Method method = null;
    try {
      method = getClass()
          .getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
    } catch (SecurityException e) {
      logger.error("*** SecurityException when finding method \"" + methodName + "\" ***", e);
      setErrorResp(res, "SecurityException when finding method \"" + methodName + "\"");
      return;
    } catch (NoSuchMethodException e) {
      logger.error("*** NoSuchMethodException when finding method \"" + methodName + "\" ***", e);
      setErrorResp(res, "NoSuchMethodException when finding method \"" + methodName + "\"");
      return;
    }
    handleHttpRequest(method, req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    doGet(req, res);
  }

  public static void setSuccessResp(HttpServletResponse res) {
    res.setStatus(HttpServletResponse.SC_OK);
  }

  public static void setErrorResp(HttpServletResponse res, String errorMsg) {
    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMsg);
  }

  protected void handleHttpRequest(Method method, HttpServletRequest req, HttpServletResponse res) {
    assert method != null && req != null && res != null : 
      "Input method, HttpServletRequest or HttpServletResponse is not valid";

    try {
      method.invoke(this, req, res);
    } catch (IllegalArgumentException e) {
      logger.error("*** IllegalArgumentException when invoking method \"" + method.getName()
          + "\" ***", e);
      setErrorResp(res, "IllegalArgumentException when invoking method \"" + method.getName()
          + "\"");
      return;
    } catch (IllegalAccessException e) {
      logger.error("*** IllegalAccessException when invoking method \"" + method.getName()
          + "\" ***", e);
      setErrorResp(res, "IllegalAccessException when invoking method \"" + method.getName() + "\"");
      return;
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      logger.error("*** InvocationTargetException when invoking method \"" + method.getName()
          + "\" ***", e);
      setErrorResp(res, "InvocationTargetException when invoking method \"" + method.getName()
          + "\"");
      return;
    }
  }
  
  /**
   * Only be used at the entry of xxxServlet's testXXX() method. 
   */
  public void serverEntryLog(){
      StackTraceElement stack = (new Exception().getStackTrace())[1];
      Logger.getLogger(getClass()).info("=== "+stack.getMethodName()+"() ===");      
  }

}
