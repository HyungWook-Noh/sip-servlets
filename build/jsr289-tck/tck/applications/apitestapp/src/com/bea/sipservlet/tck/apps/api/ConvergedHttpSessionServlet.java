/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * ConvergedHttpSessionServlet is used to test the APIs of 
 * javax.servlet.sip.ConvergedHttpSession
 */
package com.bea.sipservlet.tck.apps.api;

import com.bea.sipservlet.tck.apps.BaseHttpServlet;
import com.bea.sipservlet.tck.utils.TestUtil;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.sip.ConvergedHttpSession;
import javax.servlet.sip.SipApplicationSession;
import java.io.IOException;
import java.io.PrintWriter;

public class ConvergedHttpSessionServlet extends BaseHttpServlet {
  private static final long serialVersionUID = 8155926621046655920L;
  private static Logger logger = Logger.getLogger(ConvergedHttpSessionServlet.class);
  private boolean testEncodeURL001Flag = true;
  private boolean testEncodeURL002Flag = true;
  private static final String SESSION_MARKER = "session.marker";

  public void testEncodeURL001(HttpServletRequest req, HttpServletResponse res) throws IOException {
    serverEntryLog();
    ConvergedHttpSession chs = (ConvergedHttpSession) req.getSession(true);
    if (testEncodeURL001Flag) {      
      String baseURL = req.getRequestURL().toString();
      String encodedURL = chs.encodeURL(baseURL + "#ref");
      logger.debug("the baseURL=" + baseURL);
      logger.debug("the encodedURL=" + encodedURL);
      if (TestUtil.hasText(encodedURL)) {
        chs.setAttribute(SESSION_MARKER, SESSION_MARKER);
        setSuccessResp(res);
        PrintWriter out = res.getWriter();
        out.println(encodedURL);
        out.close();
      } else {
        setErrorResp(res, "Fail to encode absolute URL with ConvergedHttpSession");
      }
    } else {
      String marker = (String) chs.getAttribute(SESSION_MARKER);
      if (SESSION_MARKER.equals(marker)) {
        setSuccessResp(res);
      } else {
        setErrorResp(res, "Fail to get the same ConvergedHttpSession through session ID");
      }
    }
    testEncodeURL001Flag = !testEncodeURL001Flag;
  }

  public void testEncodeURL002(HttpServletRequest req, HttpServletResponse res) throws IOException {
    serverEntryLog();
    ConvergedHttpSession chs = (ConvergedHttpSession) req.getSession(true);
    if (testEncodeURL002Flag) {      
      String relativeURL = "/foo.jsp";
      String encodedURL = chs.encodeURL(relativeURL, "http");
      logger.debug("the baseURL=" + req.getRequestURL().toString());
      logger.debug("the encodedURL=" + encodedURL);
      if (TestUtil.hasText(encodedURL)) {
        chs.setAttribute(SESSION_MARKER, SESSION_MARKER);
        setSuccessResp(res);
        PrintWriter out = res.getWriter();
        out.println(encodedURL);
        out.close();
      } else {
        setErrorResp(res, "Fail to encode relative URL with ConvergedHttpSession");
      }
    } else {
      String marker = (String) chs.getAttribute(SESSION_MARKER);
      if (SESSION_MARKER.equals(marker)) {
        setSuccessResp(res);
      } else {
        setErrorResp(res, "Fail to get the same ConvergedHttpSession through session ID");
      }
    }
    testEncodeURL002Flag = !testEncodeURL002Flag;
  }

  public void testGetApplicationSession001(HttpServletRequest req, HttpServletResponse res) {
    serverEntryLog();
    ConvergedHttpSession chs = (ConvergedHttpSession) req.getSession(true);
    SipApplicationSession sas = chs.getApplicationSession();
    if (sas == null) {
      setErrorResp(res, "Fail to get ApplicationSession from ConvergedHttpSession");
    } else {
      setSuccessResp(res);
    }
  }
}
