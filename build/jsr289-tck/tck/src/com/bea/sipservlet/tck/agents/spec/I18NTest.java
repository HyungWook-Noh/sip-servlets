/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. All rights
 * reserved. I18NTest is used to test the specification of I18N
 */
package com.bea.sipservlet.tck.agents.spec;

import gov.nist.javax.sip.header.ContentType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.EventObject;

import javax.sip.ResponseEvent;
import javax.sip.TimeoutEvent;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipTransaction;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

@TargetApplication(value = ApplicationName.UAS)
public class I18NTest extends TestBase {
  private static final String CHAR_ENC = "GB2312";

  private static final String CHAT = "\u804a\u5929"; // chinese word

  private static byte[] CHAT_BA = null;

  public I18NTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
    try {
      CHAT_BA = CHAT.getBytes(CHAR_ENC);
    } catch (UnsupportedEncodingException ex) {
      throw new TckTestException(CHAR_ENC + " character encoding not supported");
    }
  }

  /**
    * check the function of reading textual content of request using getContent() and set content
    * on response using setContent()
    * 
    *                      UAC                               UAS
    *                       |                                         |
    *                       |------(1) MESSAGE---->|
    *                       |                                         |                                             
    *                       |<-----(2) 200 OK --------|
    *                       |                                         |
    *                       
    */
  @AssertionIds(
      ids = { "SipServlet:SPEC:I18N1" }, 
      desc = "Non-200 final response should be "
      + "received if the original request does not contain proper message body")
  public void testI18N1() {
    clientEntryLog();
    this.serverURI = "sip:" + "I18N-1" + "@" + serverHost + ":" + serverPort;
    try {
      Request message = assembleRequest(Request.MESSAGE, "I18N", null,
          TestConstants.SERVER_MODE_UA, 1);
      ContentType type = new ContentType();
      type.setContentType("text", "plain");
      type.setParameter("charset", CHAR_ENC);
      message.setContent(CHAT, type);
      SipTransaction trans = ua1
          .sendRequestWithTransaction(message, true, null);
      assertNotNull(trans);
      assertLastOperationSuccess(ua1);
      Thread.sleep(waitDuration / 5);
      EventObject event = ua1.waitResponse(trans, waitDuration);
      assertNotNull(event);
      if (event instanceof ResponseEvent) {
        Response resp = ((ResponseEvent) event).getResponse();
        assertEquals(
            "the reason got from UAS:" + resp.getReasonPhrase() + ". ",
            Response.OK, resp.getStatusCode());
        Object cont = resp.getContent();
        String content = null;
        if (cont instanceof String) {
          content = (String) cont;
        } else {
          content = new String((byte[]) cont, CHAR_ENC);
        }
        assertEquals("Content doesn't contain chinese word 'chat' ", CHAT, content);
      } else {
        fail("did not recieve response from server side");
      }
    } catch (Exception e) {
      fail("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  /**
    * check the function of reading textual content of request using getRawContent() and set content
    * on response using setContent()
    * 
    *                      UAC                               UAS
    *                       |                                         |
    *                       |------(1) MESSAGE---->|
    *                       |                                         |                                             
    *                       |<-----(2) 200 OK --------|
    *                       |                                         |
    *                       
    */
  @AssertionIds(
      ids = { "SipServlet:SPEC:I18N2" }, 
      desc = "Non-200 final response should be "
      + "received if the original request does not contain proper message body")
  public void testI18N2() {
    clientEntryLog();
    this.serverURI = "sip:" + "I18N-2" + "@" + serverHost + ":" + serverPort;
    try {
      Request message = assembleRequest(Request.MESSAGE, "I18N", null,
          TestConstants.SERVER_MODE_UA, 1);
      ContentType type = new ContentType();
      type.setContentType("text", "plain");
      type.setParameter("charset", CHAR_ENC);
      message.setContent(CHAT_BA, type);
      SipTransaction trans = ua1
          .sendRequestWithTransaction(message, true, null);
      assertNotNull(trans);
      assertLastOperationSuccess(ua1);
      Thread.sleep(waitDuration / 5);
      EventObject event = ua1.waitResponse(trans, waitDuration);
      if (event instanceof ResponseEvent) {
        Response resp = ((ResponseEvent) event).getResponse();
        assertEquals(
            "the reason got from UAS:" + resp.getReasonPhrase() + ". ",
            Response.OK, resp.getStatusCode());
        Object cont = resp.getContent();
        String content = null;
        if (cont instanceof String) {
          content = (String) cont;
        } else {
          content = new String((byte[]) cont, CHAR_ENC);
        }
        assertEquals("Content doesn't contain chinese word 'chat' ", CHAT, content);
      } else {
        fail("did not recieve response from server side");
      }
    } catch (Exception e) {
      fail("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }
  }
}
