/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 * @version 1.0
 * @created 01-April-2008 17:01:42
 */

package com.bea.sipservlet.tck.ar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BNF:
 * userinfo 	=  token : token @
 * hostport	=  token : token
 * uri-parameters 	=  ( ; token = token)*
 * headers		= ? token = token ( & token = token )*
 * <p/>
 * SIP-URI		= "sip:" ( userinfo )? hostport uri-parameters ( headers )?
 * name-addr	=  [ display-name ] "<" SIP-URI ">"
 * route-param  	=  name-addr+ ( ; token ( = gen-value )? )*
 */

public class TckSipURIParser {

  // mihirk - could we remove this commented code ?
/*
  public static void main(String[] args) {
    //testcase01();
    //testcase02();
    //testcase03();
    //testcase04();
  }

  public static void testcase01() {
    String[] uris = {
        "sip:bigbox3.site3.atlanta.com;lr",
        "sip:alice@atlanta.com",
        "sip:alice:secretword@atlanta.com;transport=tcp",
        "sips:alice@atlanta.com?subject=project%20x&priority=urgent",
        "sip:+1-212-555-1212:1234@gateway.com;user=phone",
        "sips:1212@gateway.com",
        "sip:alice@192.0.2.4",
        "sip:atlanta.com;method=REGISTER?to=alice%40atlanta.com",
    };

    TckSipURIParser parser = new TckSipURIParser();
    for (String uri : uris) {
      System.out.println("Parsing uri: {" + uri + "} ");
      if (parser.parseURI(uri) == null)
        System.out.println(" failed.");

      System.out.println(parser.toString());
    }
  }

  public static void testcase02() {
    String[] routes = {
        "<sip:bigbox3.site3.atlanta.com;lr>",
        "<sip:server10.biloxi.com;lr>;name=grant;sex=male",
        "Name<sip:server10.biloxi.com;lr>;name=grant"
    };

    TckSipURIParser parser = new TckSipURIParser();
    for (String route : routes) {
      System.out.println("Parsing route: {" + route + "} ");
      if (parser.parseRoute(route) == null)
        System.out.println(" failed.");

      System.out.println(parser.toString());
    }
  }

  public static void testcase03() {
    String route01 = "Name<sip:server10.biloxi.com?tom>;name=grant";
    String route02 = "Name02<sip:server10.biloxi.com;lr?tom>;name=grant";

    TckSipURIParser parser01 = new TckSipURIParser();
    TckSipURIParser parser02 = new TckSipURIParser();

    parser01.parseRoute(route01);
    parser02.parseRoute(route02);

    if (parser01.equals(parser02))
      System.out.println("Routes are identical.");
    else
      System.out.println("Routes are not identical.");
  }

  public static void testcase04() {
    String uri01 = "sip:tom@server10.biloxi.com?tom=a";
    String uri02 = "sip:tom@server10.biloxi.com;lr?tom=a";

    TckSipURIParser parser01 = new TckSipURIParser();
    TckSipURIParser parser02 = new TckSipURIParser();

    parser01.parseURI(uri01);
    parser02.parseURI(uri02);

    //System.out.println(parser01);
    //System.out.println(parser02);

    if (parser01.equals(parser02))
      System.out.println("Uris are identical.");
    else
      System.out.println("Uris are not identical.");
  }
*/
  private String REGX_TOKEN = "(\\w|\\+|\\-|\\:|\\%|\\.)*";
  private String REGX_USER_INFO = REGX_TOKEN + "(\\:" + REGX_TOKEN + ")?@";
  private String REGX_HOST_PORT = REGX_TOKEN + "(\\:" + REGX_TOKEN + ")?";
  private String REGX_URI_PARAM = "(;" + REGX_TOKEN + "(=" + REGX_TOKEN + ")?)*";
  private String REGX_HEADERS = "\\?(" + REGX_TOKEN + "(=" + REGX_TOKEN + ")?)?(&" + REGX_TOKEN + "(=" + REGX_TOKEN + ")?)*";
  private String REGX_SIP_URI = "(sip:|sips:)(" + REGX_USER_INFO + ")?" + REGX_HOST_PORT + REGX_URI_PARAM + "(" + REGX_HEADERS + ")?";

  private String REGX_NAME_ADDR = "(" + REGX_TOKEN + ")?<" + REGX_SIP_URI + ">";
  private String REGX_ROUTE_ADDR = REGX_NAME_ADDR + "(;" + REGX_TOKEN + "(=" + REGX_TOKEN + ")?)*";

  private Map<String, String> uriParamMap = new HashMap<String, String>();
  private Map<String, String> headersMap = new HashMap<String, String>();
  private String userInfo = null;
  private String hostPort = null;
  private String addrDisplayName = null;
  private Map<String, String> addrParamMap = new HashMap<String, String>();

  @Override
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("-------Dumping TckSipURIParser-------\n");
    buff.append("addrDisplayName = ").append(addrDisplayName).append("\n");
    buff.append("userInfo = ").append(userInfo).append("\n");
    buff.append("hostPort = ").append(hostPort).append("\n");
    buff.append("addrDisplayName = ").append(addrDisplayName).append("\n");
    buff.append("uriParamMap:").append(uriParamMap).append("\n");
    buff.append("headersMap:").append(headersMap).append("\n");
    buff.append("addrParamMap:").append(addrParamMap).append("\n");

    return buff.toString();
  }

  public boolean equals(TckSipURIParser that) {
    if (this.userInfo != null) {
      if (!this.userInfo.equals(that.userInfo))
        return false;
    }

    if (this.hostPort != null) {
      if (!this.hostPort.equals(that.hostPort))
        return false;
    }

    /**
     * Compare uriParams. according RFC3261 only four items will have to be compared.
     *
     -  Any uri-parameter appearing in both URIs must match.

     -  A user, ttl, or method uri-parameter appearing in only one
     URI never matches, even if it contains the default value.

     -  A URI that includes an maddr parameter will not match a URI
     that contains no maddr parameter.

     -  All other uri-parameters appearing in only one URI are
     ignored when comparing the URIs.

     */
    List<String> mustCompareUriParams = new LinkedList<String>();

    mustCompareUriParams.add("user");
    mustCompareUriParams.add("ttl");
    mustCompareUriParams.add("method");
    mustCompareUriParams.add("maddr");
    mustCompareUriParams.add("transport");

    for (String uriParam : mustCompareUriParams) {
      if (this.uriParamMap.keySet().contains(uriParam)) {
        if (!(that.uriParamMap.keySet().contains(uriParam)))
          return false;
      }

      if (that.uriParamMap.keySet().contains(uriParam)) {
        if (!(this.uriParamMap.keySet().contains(uriParam)))
          return false;
      }
    }

    for (String uriParam : this.uriParamMap.keySet()) {
      if (that.uriParamMap.keySet().contains(uriParam)) {
        if (that.uriParamMap.get(uriParam) != null) {
          if (!(that.uriParamMap.get(uriParam).equals(this.uriParamMap.get(uriParam))))
            return false;
        }
        else {
          if (this.uriParamMap.get(uriParam) != null)
            return false;
        }
      }
    }

    /**
     * RFC3261: header have to be full compared
     *
     URI header components are never ignored.  Any present header
     component MUST be present in both URIs and match for the URIs
     to match.  The matching rules are defined for each header field
     in Section 20.
     */

    if (!this.headersMap.equals(that.headersMap))
      return false;

    /**
     * Compare addr param, if any.
     */
    if (!this.addrParamMap.equals(that.addrParamMap))
      return false;

    return true;
  }

  public String parseRoute(String rawString) {
    return parseRoute(rawString, addrParamMap);
  }

  private String parseRoute(String rawString, Map<String, String> addrParamMap) {
    assert addrParamMap != null;
    addrParamMap.clear();

    Pattern patternRoute = Pattern.compile(REGX_ROUTE_ADDR);
    Matcher matcherRoute = patternRoute.matcher(rawString);

    if (!matcherRoute.matches())
      return null;
    else {
      String routeAddr = rawString = matcherRoute.group();
      String nameAddr = parseNameAddr(rawString);

      if (nameAddr != null)
        rawString = rawString.substring(nameAddr.length());

      Pattern patternToken = Pattern.compile(REGX_TOKEN);
      Matcher matcherToken = patternToken.matcher(rawString);

      while (matcherToken.find()) {
        String name, value;
        name = matcherToken.group().trim();
        if (name.length() > 0) {
          while (matcherToken.find()) {
            value = matcherToken.group().trim();
            if (value.length() > 0) {
              addrParamMap.put(name, value);
              break;
            }
            addrParamMap.put(name, null);
          }
        }
      }
      if (routeAddr.trim().length() > 0)
        return routeAddr;
      else
        return null;
    }
  }

  private String parseNameAddr(String rawString) {
    Pattern patternNameAddr = Pattern.compile(REGX_NAME_ADDR);
    Matcher matcherRouteAddr = patternNameAddr.matcher(rawString);

    if (!matcherRouteAddr.find())
      return null;
    else {
      String nameAddr = rawString = matcherRouteAddr.group();

      Pattern patternToken = Pattern.compile(REGX_TOKEN);
      Matcher matcherToken = patternToken.matcher(rawString);

      if (matcherToken.find())
        addrDisplayName = matcherToken.group();

      if (addrDisplayName.length() > 0)
        rawString = rawString.substring(addrDisplayName.length());
      else
        addrDisplayName = null;

      parseURI(rawString);

      if (nameAddr.trim().length() > 0)
        return nameAddr;
      else
        return null;
    }
  }

  public String parseURI(String rawString) {
    Pattern patternURI = Pattern.compile(REGX_SIP_URI);
    Matcher matcherURI = patternURI.matcher(rawString);

    if (!matcherURI.find())
      return null;
    else {
      String sipURI = rawString = matcherURI.group();
      if (rawString.startsWith("sip:"))
        rawString = rawString.substring("sip:".length());
      else
        rawString = rawString.substring("sips:".length());

      userInfo = parseUserInfo(rawString);

      if (userInfo != null)
        rawString = rawString.substring(userInfo.length());

      hostPort = parseHostPort(rawString);

      if (hostPort != null)
        rawString = rawString.substring(hostPort.length());

      String uriParam = parseUriParam(rawString, uriParamMap);

      if (uriParam != null)
        rawString = rawString.substring(uriParam.length());

      parseHeaders(rawString, headersMap);
      return sipURI;
    }
  }

  private String parseUserInfo(String sipURI) {
    Pattern pattenUserInfo = Pattern.compile(REGX_USER_INFO);
    Matcher matcherUserInfo = pattenUserInfo.matcher(sipURI);
    while (matcherUserInfo.find()) {
      String token = matcherUserInfo.group();
      if (token.trim().length() > 0)
        return token;
    }

    return null;
  }

  private String parseHostPort(String sipURI) {
    Pattern pattenHostPort = Pattern.compile(REGX_HOST_PORT);
    Matcher matcherHostPort = pattenHostPort.matcher(sipURI);

    while (matcherHostPort.find()) {
      String token = matcherHostPort.group();
      if (token.trim().length() > 0)
        return token;
    }

    return null;
  }

  private String parseUriParam(String sipURI, Map<String, String> uriParams) {
    assert uriParams != null;

    Pattern pattenUriParam = Pattern.compile(REGX_URI_PARAM);
    Matcher matcherUriParam = pattenUriParam.matcher(sipURI);
    uriParams.clear();

    if (matcherUriParam.find()) {
      String uriParam = matcherUriParam.group();
      Pattern patternToken = Pattern.compile(REGX_TOKEN);
      Matcher matcherToken = patternToken.matcher(uriParam);

      while (matcherToken.find()) {
        String name, value;
        name = matcherToken.group().trim();
        if (name.length() > 0) {
          while (matcherToken.find()) {
            value = matcherToken.group().trim();
            if (value.length() > 0) {
              uriParams.put(name, value);
              break;
            }
            uriParams.put(name, null);
          }
        }
      }
      if (uriParam.trim().length() > 0)
        return uriParam;
      else
        return null;
    }
    return null;
  }

  private String parseHeaders(String sipURI, Map<String, String> headers) {
    assert headers != null;

    Pattern pattenHeader = Pattern.compile(REGX_HEADERS);
    Matcher matcherHeader = pattenHeader.matcher(sipURI);
    headers.clear();

    if (matcherHeader.find()) {
      String header = matcherHeader.group();
      Pattern patternToken = Pattern.compile(REGX_TOKEN);
      Matcher matcherToken = patternToken.matcher(header);

      while (matcherToken.find()) {
        String name, value;
        name = matcherToken.group().trim();
        if (name.length() > 0) {
          while (matcherToken.find()) {
            value = matcherToken.group().trim();
            if (value.length() > 0) {
              headers.put(name, value);
              break;
            }
            headers.put(name, null);
          }
        }
      }
      if (header.trim().length() > 0)
        return header;
      else
        return null;
    }
    return null;
  }
}
