/*
 * $Id: I18N.java,v 1.5 2002/10/16 22:28:36 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 */
/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * I18NServlet is used to test the specification of  I18N
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;

/**

 * Tests internationalization...

 * 

 * <p>Both tests expect a chinese string in the body of the incoming

 * MESSAGE. They differ in how they read the body, i.e. in which method

 * is used to convert the body to a Java String. Likewise, they differ

 * in which method is used to write an I18N'ed string to the response.

 * 

 * <p>The first test uses getContent and setContent whereas the second

 * test uses getReader and getWriter.

 */
@javax.servlet.sip.annotation.SipServlet(name = "I18N")
public class I18NServlet extends BaseServlet {
  /** The word "chat" in chinese. */
  private static final String CHAR_ENC = "GB2312";

  private static final String CHAT = "\u804a\u5929";

  private static byte[] CHAT_BA;

  private static Logger logger = Logger.getLogger(I18NServlet.class);

  public void init() throws ServletException {
    try {
      CHAT_BA = CHAT.getBytes(CHAR_ENC);
    } catch (UnsupportedEncodingException ex) {
      throw new ServletException(CHAR_ENC + " character encoding not supported");
    }
  }

  protected void doMessage(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    logger.debug("--- doMessage ---");
    SipServletResponse resp;
    try {
      SipURI sipURI = (SipURI) req.getRequestURI();
      String user = sipURI.getUser();
      if ("I18N-1".equals(user)) {
        resp = test1(req);
      } else if ("I18N-2".equals(user)) {
        resp = test2(req);
      } else {
        throw new ServletException("Unexpected user name: " + user);
      }
      resp.send();
    } catch (Err e) {
      resp = req.createResponse(500, e.getMessage());
      resp.send();
    }
  }

  /**

   * Read textual content of request using getContent() and set content

   * on response using setContent().

   */
  private SipServletResponse test1(SipServletRequest req) {
    serverEntryLog();
    logger.debug("--- I18N.test1 - getContent/setContent ---");
    String ct = req.getContentType();
    if (ct == null || !ct.startsWith("text/plain")) {
      err("getContentType() doesn't start with text/plain, is " + ct);
    }
    equals("getCharacterEncoding()", CHAR_ENC, req.getCharacterEncoding());
    equals("getContentLength()", "" + CHAT_BA.length, "" +
        req.getContentLength());
    try {
      // the Reader should convert byte[] to chinese word for "chat"
      Object content = req.getContent();
      if (!(content instanceof String)) {
        err("getContent() returned non-String for text/plain");
      }
      String str = (String) content;
      if (!CHAT.equals(str)) {
        throw new Err("Reader didn't correctly convert bytes to " +
            "characters: got chars [" + toHexString(str) + "], expected [" +
            toHexString(CHAT) + "]");
      }
    } catch (IOException ex) {
      logger.error("*** Reading content with Reader failed ***", ex);
      throw new Err("Reading content with Reader failed: " + ex);
    }
    try {
      SipServletResponse resp = req.createResponse(200);
      resp.setContent(CHAT, "text/plain;charset=" + CHAR_ENC);
      return resp;
    } catch (IOException ex) {
      logger.error("*** Writing content with Writer failed ***", ex);
      throw new Err("Writing content with Writer failed: " + ex);
    }
  }

  /**

   * Read textual content of request using raw (byte[]) content

   * accessors.

   */
  private SipServletResponse test2(SipServletRequest req) {
    serverEntryLog();
    logger.debug("--- I18N.test1 - Reader/Writer ---");
    String ct = req.getContentType();
    if (ct == null || !ct.startsWith("text/plain")) {
      err("getContentType() doesn't start with text/plain, is " + ct);
    }
    equals("getCharacterEncoding()", CHAR_ENC, req.getCharacterEncoding());
    equals("getContentLength()", "" + CHAT_BA.length, "" +
        req.getContentLength());
    try {
      byte[] content = req.getRawContent();
      if (content == null || content.length != CHAT_BA.length) {
        err("getRawContent returned null of byte[] of unexpected length: " +
            (content == null ? 0 : content.length) + " vs. expected " +
            CHAT_BA.length);
      }
      for (int i = 0; i < CHAT_BA.length; i++) {
        if (content[i] != CHAT_BA[i]) {
          err("" + i + "'th byte=" + content[i] + ", expected: " + CHAT_BA[i]);
        }
      }
    } catch (IOException ex) {
      logger.error("*** Reading content with getRawContent failed ***", ex);
      throw new Err("Reading content with getRawContent failed: " + ex);
    }
    try {
      SipServletResponse resp = req.createResponse(200);
      resp.setContent(CHAT_BA, "text/plain;charset=" + CHAR_ENC);
      return resp;
    } catch (IOException ex) {
      logger.error("*** Writing content with setContent(byte[]) failed ***", ex);
      throw new Err("Writing content with setContent(byte[]) failed: " + ex);
    }
  }

  private static String toHexString(String s) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      int ch = (int) s.charAt(i);
      sb.append("\\u" + leftPad(Integer.toHexString(ch), '0', 4));
    }
    return sb.toString();
  }

  private static String leftPad(String s, char c, int minLen) {
    while (s.length() < minLen) {
      s = "" + c + s;
    }
    return s;
  }

  private static void equals(String errPrefix, String s1, String s2) {
    if (!s1.equals(s2)) {
      throw new Err(errPrefix + " expected \"" + s1 + "\", got \"" + s2 + "\"");
    }
  }

  private static void err(String msg) {
    throw new Err(msg);
  }
}
