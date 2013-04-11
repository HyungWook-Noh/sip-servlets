/*
 * $Id: MessageAccess.java,v 1.3 2003/02/05 00:34:56 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 *
 *
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 *
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;


/**

 * Tests a variety of message accessors, validity of an incoming
 * request, in this case a MESSAGE.
 * 
 * <p>Some error case are checked: ?
 * 
 * <p>Also tests use of libraries via the WEB-INF/lib directory
 * in the sar file. In this case we're using the Apache ORO regular
 * expression library.
 * 
 * <p>Internationalization is tested...
 * 
 * <p>A 200 is returned if everything OK, otherwise a 500 with a
 * hopefully informative reason phrase.
 */

@javax.servlet.sip.annotation.SipServlet(name="MessageAccess")
public class MessageAccessServlet extends BaseServlet {
	
	private static Logger logger = Logger.getLogger(MessageAccessServlet.class);
	  
    private static final String CONTENT = "Hello, World!";

    private static final String[] expectedHeaders = {
        "via",
        "call-id",
        "from",
        "to",
        "cseq",
        "date",
        "tck-test-name",
        "organization",
        "max-forwards",
        "expires",
        "content-type",
        "content-length"
    };
    

    private static final String REQURI_PATTERN = "sip:JSR289_TCK@(.+);transport=udp";
    private static final String FROM_PATTERN = ".*Alice.*<sip:alice@example\\.com>.*tag.*=.*";

    private Matcher regexMatcher;
    private Pattern reqUriPattern;
    private Pattern fromPattern;

    public void init() throws ServletException {
        try {
            reqUriPattern = Pattern.compile(REQURI_PATTERN);
            fromPattern = Pattern.compile(FROM_PATTERN);
        } catch (Exception ex) {
            logger.debug("--- init failed ---", ex);
            throw new ServletException("regex failure: " + ex);
        }
    }



    protected void doMessage(SipServletRequest req) throws IOException {
		logger.debug("--- doMessage ---");
		SipServletResponse resp;
		try {
			checkIsInitial(req);
			checkIsSecure(req);
			checkRequestLine(req);
			checkHeaderNames(req);
			checkContent(req);
			checkFrom(req);
			checkTo(req);
			checkMiscHeaders(req);
			resp = req.createResponse(200, "OK");
			resp.send();
		} catch (Err e) {
			resp = req.createResponse(500, e.getMessage());
			resp.send();
		}
	}



    void checkIsInitial(SipServletRequest req) {
        logger.debug("--- checkIsInitial ---");
        if (! req.isInitial()) err("isInitial() false");
    }



    void checkIsSecure(SipServletRequest req) {
        logger.debug("--- checkIsSecure ---");
        if (req.isSecure()) err("isSecure() true");
    }



    void checkRequestLine(SipServletRequest req) {
        logger.debug("--- checkRequestLine ---");
        if (! "MESSAGE".equals(req.getMethod())) {
            throw new Err("Method is not MESSAGE: " + req.getMethod());
        }



        if (!"SIP/2.0".equals(req.getProtocol())) {
            throw new Err("Bad protocol: " + req.getProtocol() +
                          ", expected: SIP/2.0");
        }

        if (! "sip".equals(req.getScheme())) {
            throw new Err("Bad scheme: " + req.getScheme() +
                          ", expected: sip");
        }

        

        URI uri = req.getRequestURI();
        if (!"sip".equals(uri.getScheme())) {
            throw new Err("getScheme didn't return \"sip\"");
        }

        if (! uri.isSipURI()) {
            throw new Err("isSipURI() returned false");
        }

        // check equals(uri, uri.clone());
        // check equals(uri, sf.createURI(uri.toString()))
        SipURI sipURI = (SipURI) uri;
        if (! "JSR289_TCK".equals(sipURI.getUser())) {
            throw new Err("Bad user part");
        }

        HashMap reqUriParams = new HashMap();
        reqUriParams.put("transport", "transport");
        verifyStringIter(sipURI.getParameterNames(), reqUriParams,
                         "R-URI getParameterNames");

        if (! "udp".equals(sipURI.getTransportParam())) {
            throw new Err("R-URI getTransportParam != \"udp\"");
        }

        
        regexMatcher = reqUriPattern.matcher(uri.toString());
        if (! regexMatcher.find()) {
            throw new Err("R-URI didn't match pattern " + REQURI_PATTERN);
        }

    }

    

    void checkHeaderNames(SipServletRequest req) {
        logger.debug("--- checkHeaderNames ---");
        HashSet<String> headerNames = new HashSet<String>();

        Iterator iter = req.getHeaderNames();
	    while (iter.hasNext()) {
	  	  headerNames.add(((String)iter.next()).toLowerCase());
	    }
        
        for (int i = 0; i < expectedHeaders.length; i++) {
        	if (!headerNames.contains(expectedHeaders[i])) {
        		err( " didn't include headers " + expectedHeaders[i] );        		
        	}
        }
    }

    

    void checkContent(SipServletRequest req) {

        logger.debug("--- checkContent ---");
        equals("getContentType()", "text/plain", req.getContentType());
        equals("getContentLength()", "13", ""+req.getContentLength());
        
        try {
            if (req.getContent() == null) {
                throw new Err("getContent() returned null");
            }

            if (req.getRawContent() == null) {
                throw new Err("getRawContent() returned null");
            }



            // I18N issue with this test?
            equals("content", CONTENT, new String(req.getRawContent()));

        } catch (IOException ex) {
            logger.debug("--- get(Raw)Content failed ---", ex);
            throw new Err("failed to get (raw) content: " + ex);
        }

    }



    void checkFrom(SipServletRequest req) {
        logger.debug("--- checkFrom ---");
        Address from = req.getFrom();
        URI fromURI = from.getURI();
        
        checkEqual(fromURI, "sip", "alice", "example.com", -1,
        		"sip:alice@example.com", "From URI");

        if (! "Alice".equals(from.getDisplayName())) {
            throw new Err("From getDisplayName didn't return \"Alice\"");
        }

        if (from.getParameter("tag") == null) {
            throw new Err("from.getParameter(\"tag\") returned null");

        }



        // check that getHeaders("From") returns right thing
        Iterator iter = req.getHeaders("fRom");
        if (iter == null) err("getHeaders(\"fRom\") returned null");
        if (! iter.hasNext()) err("getHeaders(\"fRom\") returned empty iterator");
        try {
            String fromStr = (String) iter.next();
            regexMatcher = fromPattern.matcher(fromStr);

            if (! regexMatcher.find() ) {
                err("getHeaders(\"fRom\") String didn't match pattern " +
                    FROM_PATTERN);
            }

        } catch (ClassCastException ex) {
            err("getHeaders(\"fRom\") Iterator returned non-String");
        }



        if (iter.hasNext()) {
            err("getHeaders(\"fRom\") returned iterator with multiple " +
                "elements; 2nd elm: " + iter.next());
        }

    }



    void checkTo(SipServletRequest req) {
        logger.debug("--- checkTo ---");
        Address to = req.getTo();
        URI toURI = to.getURI();

        Address expectedTo = null;
        try {
        	expectedTo = sipFactory.createAddress(req.getHeader("ExpectedTo"));
				} catch (ServletParseException e) {
					logger.error("*** Error occurred when create ExpectedTo header ***");
					throw new Err("Error occurred when create ExpectedTo header");
				}
				SipURI expectedToURI = (SipURI) expectedTo.getURI();
        
        checkEqual(
        	toURI, "sip", expectedToURI.getUser(), null, expectedToURI.getPort(), null, "To URI");
        if (to.getDisplayName() == null ) {
            throw new Err("To - can not retrieve display name");
        }

        if (to.getParameter("tag") != null) {
            throw new Err("to.getParameter(\"tag\") returned non-null");
        }

    }



    /** 
     * Throws Err if the specified uri does not have all the listed
     * characteristica.
     */

    private void checkEqual(URI uri, String scheme, String user,
                            String host, int port, String uriAsString,
                            String errPrefix)
    {
        if (uriAsString != null) {
            equals(errPrefix, uriAsString, uri.toString());
        }
        equals(errPrefix + " getScheme()", scheme, uri.getScheme());
        equals(errPrefix + " isSipURI()", "true", "" + uri.isSipURI());
        SipURI sipURI = (SipURI) uri;
        logger.debug("--- SipURI =" + sipURI + "---");
        equals(errPrefix + " getUser()", user, sipURI.getUser());
        if (host != null) {
            equals(errPrefix + " getHost()", host, sipURI.getHost());
        }
        equals(errPrefix + " getPort()", ""+port, ""+sipURI.getPort());
    }

    /**
     * Sanity checks on Call-ID, Via, Organization, Max-Forwards,
     * Expires.
     */
    void checkMiscHeaders(SipServletRequest req) {
        logger.debug("--- checkMiscHeaders ---");
        
        // Call-ID
        if (req.getHeader("Call-ID") == null) {
            throw new Err("getHeader(\"Call-ID\") returned null");
        }

        // Via
        String via = req.getHeader("Via");
        if (via == null) err("getHeader(\"Via\") null");
        if (! via.startsWith("SIP/2.0/UDP ")) {
            err("getHeader(\"Via\") doesn't start with \"SIP/2.0/UDP \": " +
                via);
        }
        if (via.indexOf("branch") == -1) {
            err("apparently no branch in top Via");
        }

        // Organization
        equals("getHeader(\"Organization\")",
               "Acme, Corp.", req.getHeader("Organization"));
        equals("getHeader(\"orGAniZation\")",
               "Acme, Corp.", req.getHeader("orGAniZation"));

        // Max-Forwards
        equals("getMaxForwards()", "70", ""+req.getMaxForwards());
        equals("getHeader(\"Max-Forwards\")", "70", req.getHeader("Max-Forwards"));

        // Expires
        equals("getExpires()", "300", ""+req.getExpires());
        equals("getHeader(\"Expires\")", "300", ""+req.getHeader("Expires"));
    }

    private static void equals(String errPrefix, String s1, String s2) {
        if (! s1.equals(s2)) {
            throw new Err(errPrefix + " expected \""+s1+"\", got \""+s2+"\"");
        }
    }

    private static void err(String msg) {
        throw new Err(msg);
    }
    
    public String getServletInfo() {
        return "Tests a variety of message accessors";
    }

    private void verifyStringIter(Iterator iter,
                                  HashMap expected,
                                  String methodName)
    {
    	Set<String> ignoreList = new HashSet<String>();
    	ignoreList.add("servlet-name");
    	ignoreList.add("route");
    	ignoreList.add("wlss-popped-route");

        while (iter.hasNext()) {
            String name = (String) iter.next();
            name = name.toLowerCase();
            if (expected.remove(name) == null) {
 
                err(methodName + " included unexpected header name: " + name);
            }
        }
        
        if (expected.size() > 0) {
            err(methodName + " didn't include headers " + expected.keySet());
        }
    }
}
