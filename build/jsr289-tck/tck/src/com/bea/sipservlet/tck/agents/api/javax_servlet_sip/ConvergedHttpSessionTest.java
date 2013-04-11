/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * ConvergedHttpSessionTest is used to test the APIs of 
 * javax.servlet.sip.ConvergedHttpSession
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.utils.TestUtil;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class ConvergedHttpSessionTest extends TestBase {
  private static Logger logger = Logger.getLogger(ConvergedHttpSessionTest.class);
  private String reqURL;

  public ConvergedHttpSessionTest(String arg0) throws IOException, UnknownHostException {
    super(arg0);
    reqURL = "http://" + serverHost + ":" + serverHttpPort + "/" + serverAppHttpRoot + "/";
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:ConvergedHttpSession1" },
      desc = "User agents can encode the HTTP URL with the jsessionid.")
  public void testEncodeURL001() {
    String newURL = assertHttpMessage(reqURL, null, null, null, null);
    assertNotNull("Fail to get encodedURL from server", newURL);
    assertHttpMessage(newURL, null, null, null, null);
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:ConvergedHttpSession2" },
      desc = "User agents can convert the given relative path to an absolute URL "
      + "by prepending the contextPath for the current ServletContext.")
  public void testEncodeURL002() {
    String newURL = assertHttpMessage(reqURL, null, null, null, null);
    assertNotNull("Fail to get encodedURL from server", newURL);
    assertHttpMessage(newURL, null, null, null, null);
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:ConvergedHttpSession3" },
      desc = "User agents can get parent SipApplicationSession if it exists.")
  public void testGetApplicationSession001() {
    assertHttpMessage(reqURL, null, null, null, null);
  }

  /**
   * Used to send a HTTP GET request to server side, and receive response or 
   * wait to expire.
   * The call flow is:
   * 
   *                    Client                         Server
   *                       |                              |
   *                       |-------- (1)HTTP(GET) ------->|
   *                       |                <Execute determination logic>                                            
   *                       |<----- (2)HTTP(200/500) ------|
   *                       |                              |
   *
   * @param reqURL      The URL of this http request
   * @param httpMethod  The method of this http request
   * @param headerMap   The map of the specific headers
   * @param servletName The servlet name in server side to handle the message
   * @param methodName  The method in server side to handle the message
   */
  protected String assertHttpMessage(String reqURL, String httpMethod,
      Map<String, String> headerMap, String servletName, String methodName) {
    try {
      clientEntryLog();

      String localHttpMethod = (TestUtil.hasText(httpMethod)) ? httpMethod : "GET";
      StackTraceElement stack = getBasePackageStack(new Exception().getStackTrace());
      String localServletName = (TestUtil.hasText(servletName)) ? servletName
          : getInterfaceName(stack.getClassName());
      String localMethodName = (TestUtil.hasText(methodName)) ? methodName : stack.getMethodName();

      URL url = new URL(reqURL);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setUseCaches(false);
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setRequestMethod(localHttpMethod);
      //set specific headers
      if (headerMap != null) {
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
          conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
      }
      conn.setRequestProperty("Content-Length", "0");
      conn.setRequestProperty(TestConstants.SERVLET_HEADER, localServletName);
      conn.setRequestProperty(TestConstants.METHOD_HEADER, localMethodName);

      //get response
      int respCode = conn.getResponseCode();
      if (respCode != 200) {
        fail(conn.getResponseMessage());
      }

      //read content
      StringBuilder data = new StringBuilder();
      if (conn.getContentLength() > 0) {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          data.append(inputLine);
        }
        in.close();
      }

      conn.disconnect();
      return data.toString();
    } catch (MalformedURLException e) {
      logger.error("*** MalformedURLException when creating HTTP request ***", e);
      throw new TckTestException(e);
    } catch (ProtocolException e) {
      logger.error("*** ProtocolException when creating HTTP request ***", e);
      throw new TckTestException(e);
    } catch (IOException e) {
      logger.error("*** IOException when creating HTTP request ***", e);
      throw new TckTestException(e);
    }
  }
}
