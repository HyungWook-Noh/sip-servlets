/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * AuthOfAppInitReqTest is used to test  
 * mechanism for authentication of application initiated request.
 *
 */

package com.bea.sipservlet.tck.agents.spec;

import com.bea.sipservlet.tck.agents.ApplicationName;
import com.bea.sipservlet.tck.agents.TargetApplication;
import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.wcp.ant.ext.annotations.AssertionIds;
import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipTransaction;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

@TargetApplication(ApplicationName.UAC)
public class AuthOfAppInitReqTest extends TestBase {

  private static Logger logger = Logger.getLogger(AuthOfAppInitReqTest.class);
  String userName = "tckUser";
  String password = "beaSystem";

  public AuthOfAppInitReqTest(String arg0) throws IOException {
    super(arg0);
  }

  /**
   * For testSingleChallenge(), the call flow is:
   *
   * UA1                              SUT                               UA2
   *  |                                |                                |
   *  |-------- (1) Message----------->|                                |
   *  |                                |                                |
   *  |<------- (2) 200 OK ------------|                                |
   *  |                                |                                |
   *  |                                |-------- (3) REGISTER---------->|
   *  |                                |                                |
   *  |                                |<------- (4) 401 Unauthorized---|
   *  |                                |                                |
   *  |                                |-------- (5) REGISTER---------->|
   *  |                                |                                |
   *  |                                |<------- (6) 200 OK-------------|
   *  |                                |                                |
   *  |                                |-------- (7) REGISTER---------->|
   *  |                                |                                |
   *  |                                |<------- (8) 200 OK-------------|
   *  |                                |                                |
   *
   *
   */
  @AssertionIds(
      ids = {"SipServlet:SPEC:AuthOfAppInitReq1"},
      desc = "Challenge(401) to Register message.")
  public void testAuthOfAppInitReq1()
      throws ParseException, InvalidArgumentException, SipException {
    clientEntryLog();

    ua2.listenRequestMessage();

    //(1) UA1 send MESSAGE
    ArrayList<Header> addHeaders = new ArrayList<Header>(2);
    addHeaders.add(getUa2UriHeader());
    SipTransaction trans =
        sendMessage(ua1,null, null, Request.MESSAGE, 1, addHeaders);
    assertNotNull("Fail to send MESSAGE out. - ", trans);

    //(2) UA1 receive 200 OK
    if (ua1.waitResponse(trans, waitDuration) == null) {
      logger.warn("*** No 200/Message received ***");
    }

    //(3) UA2 receive REGISTER
    RequestEvent regEvent = ua2.waitRequest(waitDuration);
    assertLastOperationSuccess(
        "UA2 wait for REGISTER - " + ua2.format(), ua2);
    Request reqRegister = regEvent.getRequest();
    assertNotNull("Fail to receive 1st REGISTER. - ", reqRegister);
    assertEquals("Unexpected request",
        Request.REGISTER, reqRegister.getMethod());

    //(4) send 401
    Header hdr = reqRegister.getHeader(AuthorizationHeader.NAME);
    assertNull("Authorization Header already in 1st REGISTER - ", hdr);

    String challenge = "Digest realm =\"bea.com\", " +
            "nonce=\"dbffb13c8a4d30117c25c753d1fa7710\", " +
            "opaque=\"\", stale=FALSE, algorithm=MD5, qop=\"auth\"";
    HeaderFactory headerFactory = ua2.getParent().getHeaderFactory();
    WWWAuthenticateHeader wwwAuthenticationHeader =
        headerFactory.createWWWAuthenticateHeader(challenge);
    addHeaders.add(wwwAuthenticationHeader);
    addHeaders.add(headerFactory.createHeader(TestConstants.USERNAME, userName));
    addHeaders.add(headerFactory.createHeader(TestConstants.PASSWORD, password));

    ua2.sendReply(regEvent, Response.UNAUTHORIZED,
            "UNAUTHORIZED", ua2.generateNewTag(), null, 3600,
            addHeaders, null, null);

    //(5) UA2 receive 2nd REGISTER
    RequestEvent regEvent2 = ua2.waitRequest(waitDuration);
    assertLastOperationSuccess(
        "UA2 wait for 2nd REGISTER - " + ua2.format(), ua2);
    Request reqRegister2 = regEvent2.getRequest();
    assertNotNull("Fail to receive 2nd REGISTER. - ", reqRegister2);
    assertEquals("Unexpected request",
        Request.REGISTER, reqRegister2.getMethod());
    assertTrue("Authorization Header in 2nd REGISTER is invalid. - ",
        checkAuthorHeader(wwwAuthenticationHeader, reqRegister2));

    //(6) send 200 OK
    addHeaders.remove(1);
    ua2.sendReply(regEvent2, Response.OK,
            "OK", ua2.generateNewTag(), null, 3600,
            addHeaders, null, null);

    //(7) UA2 receive 3rd REGISTER
    RequestEvent regEvent3 = ua2.waitRequest(waitDuration);
    assertLastOperationSuccess(
        "UA2 wait for 3rd REGISTER - " + ua2.format(), ua2);
    Request reqRegister3 = regEvent3.getRequest();
    assertNotNull("Fail to receive 3rd REGISTER. - ", reqRegister3);
    assertEquals("Unexpected request",
        Request.REGISTER, reqRegister3.getMethod());
    assertTrue("Authorization Header in 3rd REGISTER is invalid. - ",
        checkAuthorHeader(wwwAuthenticationHeader, reqRegister3));

    //(8) send 200 OK
    ua2.sendReply(regEvent3, Response.OK,
            "OK", ua2.generateNewTag(), null, 3600,
            null, null, null);
  }
    
  private Header getUa2UriHeader(){
    HeaderFactory header_factory = ua1.getParent().getHeaderFactory();
    try {
      return header_factory.createHeader(TestConstants.PRIVATE_URI, ua2URI);
    } catch (ParseException e) {
      logger.error("*** ParseException when creating private header ***", e);
      throw new TckTestException(e);
    }
  }

  private boolean checkAuthorHeader(WWWAuthenticateHeader authenticateHeader,
                                    Request reqRegister){
    AuthorizationHeader authorHeader =
        (AuthorizationHeader) reqRegister.getHeader(AuthorizationHeader.NAME);
    if (authorHeader == null) {
      return false;
    }
    String challengeResponse = authorHeader.toString();
    challengeResponse = challengeResponse.substring(
        challengeResponse.indexOf("Digest"), challengeResponse.length()-2);
    HashMap<String,String> challengeResponseMap =
                parseDigestChallenge(challengeResponse);

    String challengeStr = authenticateHeader.toString();
    challengeStr = challengeStr.substring(challengeStr.indexOf("Digest"));

    HashMap<String,String> challengeMap =
                parseDigestChallenge(challengeStr);
    String realm = challengeMap.get("realm");
    String cnonce = challengeResponseMap.get("cnonce");
    String ncStr = challengeResponseMap.get("nc");
    String method = reqRegister.getMethod();
    String requestURIStr = reqRegister.getRequestURI().toString();

    String digestResponse = createDigestResponseWithHA1(userName,
        getHash(userName + ":" + realm + ":" + password),
        challengeMap, method, requestURIStr,
        ncStr, cnonce);

    HashMap<String,String> standardMap =
                parseDigestChallenge(digestResponse);

    return standardMap.equals(challengeResponseMap);
  }

  private static HashMap<String,String> parseDigestChallenge(String challenge) {
    if (challenge.startsWith("Digest ")) {
      challenge = challenge.substring("Digest ".length());
    }
    HashMap<String,String> map = new HashMap<String, String>();
    StringTokenizer st = new StringTokenizer(challenge, ",");
    while (st.hasMoreTokens()) {
      parseKeyValue(map, st.nextToken());
    }
    return map;
  }

  private static void parseKeyValue(HashMap<String,String> map, String token) {
    int end = token.length() - 1;
    while (Character.isWhitespace(token.charAt(end))) end--;

    int index = token.indexOf("=");
    if (index != -1) {
      String key = token.substring(0, index).trim();
      int valStart = (token.charAt(index+1) == '\"') ? index+2 : index+1;
      int valEnd   = (token.charAt(end)     == '\"') ? end-1 : end;
      String value = token.substring(valStart, valEnd + 1).trim();
      map.put(key, value);
    }
  }

  private static String getHash(String input) {
    MessageDigest md5;
    try {
      md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      logger.error("*** NoSuchAlgorithmException when creating " +
          "challenge response ***", e);
      throw new TckTestException(e);
    }

    byte[] digest = md5.digest(input.getBytes());
    char[] ca = new char[2 * digest.length];
    int j = 0;
    for (byte b : digest) {
      ca[j++] = toHexChar((b >> 4) & 0xf);
      ca[j++] = toHexChar(b & 0xf);
    }
    return new String(ca);
  }

  private static char toHexChar(int n) {
    return (char) (n < 10 ? '0' + n : 'a' - 10 + n);
  }

  private static String createDigestResponseWithHA1(String username,
                                                   String ha1,
                                                   Map challengeMap,
                                                   String method,
                                                   String uri,
                                                   String nonceCnt,
                                                   String cnonce) {
    String realm = (String) challengeMap.get("realm");
    String nonce = (String) challengeMap.get("nonce");
    String algorithm = (String) challengeMap.get("algorithm");
    String qop = (String) challengeMap.get("qop");

    String response = getDigest(ha1, nonce, nonceCnt, cnonce, qop, method, uri);

    StringBuilder digestResponse = new StringBuilder("Digest username=\"")
        .append(username).append("\",realm=\"").append(realm).append("\",");
    digestResponse.append("cnonce=\"").append(cnonce).append("\",nc=")
        .append(nonceCnt).append(",qop=\"").append(qop).append("\",");
    digestResponse.append("uri=\"").append(uri)
        .append("\",nonce=\"").append(nonce)
        .append("\",response=\"").append(response)
        .append("\",algorithm=\"").append(algorithm).append("\"");
    return digestResponse.toString();
  }

  private static String getDigest(String ha1,
                                 String nonce,
                                 String nonceCount,
                                 String clientNonce,
                                 String qop,
                                 String method,
                                 String digestUri) {
    StringBuilder input = new StringBuilder();
    input.append(method).append(":");
    input.append(digestUri);
    String ha2 = getHash(input.toString());
    return getDigest(ha1, nonce, nonceCount, clientNonce, qop, ha2);
  }

  private static String getDigest(String ha1,
                                 String nonce,
                                 String nonceCount,
                                 String clientNonce,
                                 String qop,
                                 String ha2) {
    StringBuilder input = new StringBuilder();
    input.append(ha1).append(":");
    input.append(nonce).append(":");
    input.append(nonceCount).append(":");
    input.append(clientNonce).append(":");
    input.append(qop).append(":");
    input.append(ha2);
    return getHash(input.toString());
  }

}
