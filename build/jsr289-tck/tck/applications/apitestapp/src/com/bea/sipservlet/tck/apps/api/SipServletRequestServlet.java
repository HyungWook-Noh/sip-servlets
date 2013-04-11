/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 * 
 * SipServletRequestServlet is used to test the APIs of 
 * javax.servlet.sip.SipServletRequest.
 *
 */

package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.Proxy;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;
import javax.servlet.sip.annotation.SipServlet;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.SipApplicationSession;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.utils.TestUtil;

@SipServlet(applicationName = "com.bea.sipservlet.tck.apps.apitestapp", name = "SipServletRequest")
public class SipServletRequestServlet extends BaseServlet {
  private static Logger logger = Logger
      .getLogger(SipServletRequestServlet.class);
  private static final String RESULT = "Result";
  
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testAddAuthHeader001(SipServletRequest req) {
    serverEntryLog();

    try {
      req.createResponse(200).send();

      SipURI toURI = (SipURI) req.getAddressHeader("From").getURI();
      SipURI fromURI = (SipURI) req.getTo().getURI();

      SipServletRequest invite = sipFactory.createRequest(req
          .getApplicationSession(), "INVITE", fromURI, toURI);
      
      invite.send();
    } catch (ServletException e) {
      logger.error("***  testAddAuthHeader001 ServletException error. ***", e);
      throw new TckTestException("AddAuthHeader001 error.", e);
    }catch (IOException ioe) {
      logger.error("*** testAddAuthHeader001 IOException error. ***", ioe);
      throw new TckTestException("AddAuthHeader001 error.", ioe);
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testAddAuthHeader002(SipServletRequest req) {
    serverEntryLog();

    try {
      req.createResponse(200).send();

      SipURI toURI = (SipURI) req.getAddressHeader("From").getURI();
      SipURI fromURI = (SipURI) req.getTo().getURI();

      SipServletRequest invite = sipFactory.createRequest(req
          .getApplicationSession(), "INVITE", fromURI, toURI);
      invite.send();
    } catch (ServletException e) {
      logger.error("*** ServletException error. ***", e);
      throw new TckTestException("AddAuthHeader002 error.", e);
    }catch (IOException ioe) {
      logger.error("*** IOException error. ***", ioe);
      throw new TckTestException("AddAuthHeader002 error.", ioe);
    }

  }

  protected void doErrorResponse(SipServletResponse resp) {
    if (resp.getStatus() == SipServletResponse.SC_UNAUTHORIZED) {
      SipServletRequest req = resp.getSession().createRequest("INVITE");
      AuthInfo authInfo = sipFactory.createAuthInfo();
      Iterator<String> realms = resp.getChallengeRealms();
      while (realms.hasNext()) {
        String realm = realms.next();
        authInfo.addAuthInfo(resp.getStatus(), realm, "user1", "123456");
      }
      if ("Unauthorized with UserPwd".equalsIgnoreCase(resp.getReasonPhrase())) {
        req.addAuthHeader(resp, "user1", "123456");
      } else {
        req.addAuthHeader(resp, authInfo);
      }
      try {
        req.send();
      } catch (IOException e) {
        logger.error("*** IOException error. ***", e);
        throw new TckTestException("AddAuthHeader002 error.", e);
      }
    }
  }

  protected void doProvisionalResponse(SipServletResponse resp)
      throws ServletException, IOException {
    if ("INVITE".equalsIgnoreCase(resp.getMethod()) &&
        resp.getReasonPhrase().equalsIgnoreCase("testCreateCancel001") &&
        resp.getStatus() == 180) {
      SipServletRequest oriReq = resp.getRequest();
      try {
        oriReq.createCancel().send();
      } catch (IllegalStateException  e) {
        logger.error("*** Get IllegalStateException via testCreateCancel001. ***");
      }
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreateCancel001(SipServletRequest req) {
    serverEntryLog();
   
    try {
      req.createResponse(200).send();
      
      SipURI oriToURI = (SipURI) req.getTo().getURI().clone();
      SipURI oriFromURI = (SipURI) req.getFrom().getURI().clone();

      SipServletRequest reqInvite = sipFactory.createRequest(req
          .getApplicationSession(), "INVITE", oriToURI, oriFromURI);      
      reqInvite.send();

      SipServletRequest reqCancel = reqInvite.createCancel();      
      reqCancel.send();
    } catch (IllegalStateException argue) {
      logger.error("*** createCancel, wrong request status. ***", argue);
      throw new TckTestException("Fail to createCancel001.", argue);
    }catch (IOException e) {
      logger.error("***  IOException error. ***", e);
      throw new TckTestException("Fail to createCancel001.", e);
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreateResponse001(SipServletRequest req) {
    serverEntryLog();

    try {
      req.createResponse(200).send();
    } catch (IllegalArgumentException argue) {
      logger.error("*** CreateResponse, wrong Augument. ***", argue);
      throw new TckTestException("Fail to CreateResponse001.", argue);
    } catch (IllegalStateException argue) {
      logger.error("*** CreateResponse, wrong request status. ***", argue);
      throw new TckTestException("Fail to CreateResponse001.", argue);
    }catch (IOException e) {
      logger.error("*** IOException error. ***", e);
      throw new TckTestException("createResponse001. error", e);
    }   
  }
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreateResponse002(SipServletRequest req) {
    serverEntryLog();
    
    try {      
      SipServletResponse resp = req.createResponse(200, "reason 200");
      resp.addHeader("reason-des", "reason 200");
      resp.send();
    } catch (IllegalArgumentException argue) {
      logger.error("*** CreateResponse, wrong Augument. ***");
      throw new TckTestException("Fail to CreateResponse002.", argue);
    } catch (IllegalStateException argue) {
      logger.error("*** CreateResponse, wrong request status. ***", argue);
      throw new TckTestException("Fail to CreateResponse002.", argue);
    } catch (IOException e) {
      logger.error("*** IOException error. ***", e);
      throw new TckTestException("testCreateResponse002 error", e);
    }
    
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetB2buaHelper001(SipServletRequest req) {
    serverEntryLog();
    
    B2buaHelper b2buaHelper = req.getB2buaHelper();
    if (b2buaHelper == null) {
      return "get b2buaHelper null";
    }     
    return null;         
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetInputStream001(SipServletRequest req) {
    serverEntryLog();
    try {
      if (req.getInputStream() == null) {     
        return null;
      } else {
        return "test GetInputStream001 not null";
      }
    } catch (IOException e) {
      logger.error("*** test GetInputStream001 IOException. ***", e);
      throw new TckTestException(e);   
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetPoppedRoute001(SipServletRequest req) {
    serverEntryLog();

    Address poppedRoute = req.getPoppedRoute();
    
    if (null == poppedRoute) {
      return "the poppedRoute should not is null.";
    } else return null;   
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetInitialPoppedRoute001(SipServletRequest req)
      throws IOException {
    serverEntryLog();
    
    Address poppedRoute = req.getInitialPoppedRoute();        

    if (poppedRoute == null) {
      return "the InitialPoppedRoute should not is null.";
    } else {
      if (checkURI(poppedRoute.getURI())) return null;
      else return "getInitialPoppedRoute error.";
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetProxy001(SipServletRequest req) {
    serverEntryLog();

    try {
      Proxy reqProxy = req.getProxy();      
      SipURI uri = (SipURI) req.getRequestURI();
      reqProxy.proxyTo(uri);
    } catch (TooManyHopsException e) {
      logger.error("*** get TooManyHopsException via testGetProxy001(). ***", e);
      throw new TckTestException(
          "Fail to creat or get Proxy via testGetProxy001().", e);
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetProxy002(SipServletRequest req) {
    serverEntryLog();
    try {
      if (null != req.getProxy(false)) {
        logger.error("*** testGetProxy002 should get null. ***");
        throw new TckTestException("testGetProxy002 should get null.");
      }
      Proxy reqProxy = req.getProxy(true);
      SipURI uri = (SipURI) req.getRequestURI();
      
      reqProxy.proxyTo(uri);
    } catch (TooManyHopsException e) {
      logger.error("*** get TooManyHopsException via testGetProxy002(). ***");
      throw new TckTestException(
          "Fail to create or get Proxy via testGetProxy002().", e);
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetReader001(SipServletRequest req) {
    serverEntryLog();
    try {
      if (req.getReader() == null) {
        return null;
      } else {
        return "GetReader001 error";
      }
    } catch (IOException e) {
      logger.error("*** testGetReader001 IOException ***", e);
      throw new TckTestException(e);  
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetSetRoutingDirective001(SipServletRequest origReq) {
    serverEntryLog();

    try {      
      String host = ((SipURI) origReq.getRequestURI()).getHost();
      SipServletRequest newReq = sipFactory.createRequest(origReq
          .getApplicationSession(), "MESSAGE", sipFactory
          .createAddress("sip:B2BUA01@" + host), origReq.getTo());      

      newReq.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, origReq);
      
      if (!(SipApplicationRoutingDirective.CONTINUE.equals(newReq.getRoutingDirective()))) {      
        return "Fail to getRoutingDirective or setRoutingDirective.";      
      }else return null;

    } catch (ServletParseException e) {
      logger.error("*** testGetSetRoutingDirective001 ServletParseException ***", e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testIsInitial001(SipServletRequest req) {
    serverEntryLog();
    if (req.isInitial()) {
      return null;
    } else {
      return "testIsInitial error";
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testPushPath001(SipServletRequest req) {
    serverEntryLog();

    try {
      String sAddr = req.getHeader("new-uri");
      Address address1 = sipFactory.createAddress(sAddr);

      req.pushPath(address1);
      if (null == req.getHeader("Path")) {
        logger.error("*** testPushPath001 get path header is null. ***");
        handleSimpleAssertProxy("testPushPath001", req, "path is null", address1);
        throw new TckTestException("Fail to PushPath");
      } else if (req.getHeader("Path").equalsIgnoreCase(sAddr)) {
        handleSimpleAssertProxy("testPushPath001", req, null, null);
      } else {
        handleSimpleAssertProxy("testPushPath001", req,
            "test PushPath from request is not equals the original string", null);      
      }
    } catch (ServletParseException e) {
      logger.error("*** testPushPath001 ServletParseException ***", e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testPushRoute001(SipServletRequest req) {
    serverEntryLog();

    SipURI oriToURI = (SipURI) req.getTo().getURI().clone();
    Address address1 = sipFactory.createAddress(oriToURI);
    req.pushRoute(address1);

    String route = req.getHeader("route");
    if (null == req.getHeader("route")) {
      logger.error("*** testPushRoute001 get route header is null. ***");
      handleSimpleAssertProxy("testPushRoute001", req, "route is null", null);
      throw new TckTestException("Fail to test pushRoute001");
    } else if (address1.toString().equalsIgnoreCase(route)) {
      handleSimpleAssertProxy("testPushRoute001", req, null, null);
    } else {
      logger
          .error("*** testPushRoute001 get route header not equals the original route header. ***");
      handleSimpleAssertProxy("testPushRoute001", req,
          "testPushRoute001 error", null);
      throw new TckTestException("Fail to testPushRoute001");
    }

  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testPushRoute002(SipServletRequest req) {
    serverEntryLog();

    String user = req.getHeader("user");
    String host = req.getHeader("host");

    SipURI sipURI = sipFactory.createSipURI(user, host);
    req.pushRoute(sipURI);
    String route = req.getHeader("route");

    if (null == route) {
      logger.error("*** testPushRoute002 get route header is null. ***");
      handleSimpleAssertProxy("testPushRoute002", req, "route is null", null);
      throw new TckTestException("Fail to PushRoute002");
    } else if (("<" + sipURI.toString() + ">").equalsIgnoreCase(route)) {
      handleSimpleAssertProxy("testPushRoute002", req, null, null);
    } else {
      logger
          .error("*** testPushRoute002 get route header not equals the original route header. ***");
      handleSimpleAssertProxy("testPushRoute002", req,
          "testPushRoute002 error", null);
      throw new TckTestException("Fail to PushRoute002");
    }

  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)              
  public void testSend001(SipServletRequest req) {
    serverEntryLog();

    try {
      req.createResponse(200).send();
      
      SipURI oriToURI = (SipURI) req.getTo().getURI().clone();
      SipURI oriFromURI = (SipURI) req.getFrom().getURI().clone();
      
      SipServletRequest newReq = sipFactory.createRequest(req
          .getApplicationSession(), "MESSAGE", oriToURI, oriFromURI);
      
      newReq.send();
    } catch (IOException e) {
      logger.error("*** test testSend001 IOException ***", e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetSetMaxForwards001(SipServletRequest req) {
    serverEntryLog();

    try {
      req.createResponse(200).send();

      SipURI oriToURI = (SipURI) req.getTo().getURI().clone();
      SipURI oriFromURI = (SipURI) req.getFrom().getURI().clone();
      int maxFds = 2;
      SipServletRequest newReq = sipFactory.createRequest(req
          .getApplicationSession(), "MESSAGE", oriToURI, oriFromURI);
      newReq.setMaxForwards(maxFds);

      if (newReq.getHeader("Max-Forwards") == null) {      
        logger.error("*** Max-Forwards is null ***");
        newReq.addHeader(TestConstants.TEST_FAIL_REASON, "Max-Forwards is null");
        newReq.send();
        throw new TckTestException("Fail to GetSetMaxForwards001");
      } else if ((new Integer(newReq.getHeader("Max-Forwards"))) != maxFds) {
        logger.error("*** get/set Max-Forwards method error ***");
        newReq.addHeader(TestConstants.TEST_FAIL_REASON,
            "get/set Max-Forwards method error");      
        newReq.send();
        throw new TckTestException("Fail to GetSetMaxForwards001");
      }else {
        newReq.send();
      }
    } catch (NumberFormatException e) {
      logger.error("*** testGetSetMaxForwards001 NumberFormatException ***", e);
      throw new TckTestException(e);
    } catch (IOException e) {
      logger.error("*** test testGetSetMaxForwards001 IOException ***", e);
      throw new TckTestException(e);
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetSetRequestURI001(SipServletRequest req) {
    serverEntryLog();
    
    try {
      req.createResponse(200).send();

      SipURI oriToURI = (SipURI) req.getTo().getURI().clone();
      SipURI oriFromURI = (SipURI) req.getFrom().getURI().clone();

      String sreqURI = req.getHeader("new-uri");
      URI newURI = sipFactory.createURI(sreqURI);
      SipServletRequest newReq = sipFactory.createRequest(req
          .getApplicationSession(), "MESSAGE", oriToURI, oriFromURI);
      newReq.setRequestURI(newURI);
      String stmpURI = newReq.getRequestURI().toString();

      if (stmpURI == null) {
        newReq.addHeader(TestConstants.TEST_FAIL_REASON, "Request-URI is null");
      } else if (!stmpURI.equalsIgnoreCase(sreqURI)) {
        newReq.addHeader(TestConstants.TEST_FAIL_REASON,
            "Get/SetRequestURI method error");
      }
      newReq.send();
    } catch (ServletParseException e) {
      logger.error("*** testGetSetRequestURI001 ServletParseException ***", e);
      throw new TckTestException(e);
    } catch (IOException e) {
      logger.error("*** testGetSetRequestURI001 IOException ***", e);
      throw new TckTestException(e);
    }
  }

  protected void doSuccessResponse(SipServletResponse resp) {
    try {
      if ("INVITE".equalsIgnoreCase(resp.getMethod())) {
        resp.createAck().send();        
        if (resp.getReasonPhrase().equalsIgnoreCase("testCreateCancel101")){
          SipServletRequest oriReq = resp.getRequest();
          try {          
            oriReq.createCancel();  
            logger.error("*** Fail to get IllegalStateException via testCreateCancel101. ***");
          } catch (IllegalStateException  e) {
            logger.info("=== Get IllegalStateException ok via testCreateCancel101. ===");
            SipServletRequest reqBye = oriReq.getSession().createRequest("BYE");
            reqBye.send();
          }
        }
      }
    } catch (IOException e) {
      logger.error("*** doSuccessResponse IOException ***", e);
      throw new TckTestException(e);
    }
  }

  protected void handleSimpleAssertProxy(String method, SipServletRequest req,
      String reason, Address address) {
    if (req == null) {
      logger.error("*** input request should not is null ***");
      throw new TckTestException("input request is null");
    }

    try {
      Proxy proxy = getRequestProxy(req);
      if (TestUtil.hasText(reason)) {
        req.addHeader(TestConstants.TEST_FAIL_REASON, reason);
      }
      if ("testPushPath001".equalsIgnoreCase(method)) {
        URI toURI = sipFactory.createURI(req.getHeader("new-uri2"));
        proxy.proxyTo(toURI);
      } else {
        proxy.proxyTo(req.getRequestURI());
      }
    } catch (IllegalArgumentException e) {
      logger.error("*** IllegalArgumentException when invoking method \""
          + method + "\" ***", e);
      throw new TckTestException(e);
    } catch (ServletParseException e) {
      logger.error("*** handleSimpleAssertProxy ServletParseException ***", e);
      throw new TckTestException(e);
    }
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreateCancel101(SipServletRequest req) {
    serverEntryLog();

    try {
      req.createResponse(200).send();
      
      SipURI oriToURI = (SipURI) req.getTo().getURI().clone();
      SipURI oriFromURI = (SipURI) req.getFrom().getURI().clone();

      SipServletRequest reqInvite = sipFactory.createRequest(req
          .getApplicationSession(), "INVITE", oriToURI, oriFromURI);
      
      reqInvite.send(); 
    } catch (IllegalStateException  e) {
      logger.info("=== Get IllegalStateException ok via testCreateCancel101. ===");    
    }catch (IOException e) {
      logger.error("*** IOException error. ***", e);      
      throw new TckTestException("createResponse101. error", e);
    }
  }  
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateResponse101(SipServletRequest req) {
    serverEntryLog();
    int errStatus = -9999;
   
    try {
      req.createResponse(errStatus).send();   
    } catch (IllegalArgumentException argue) {
      logger.info("=== Success to get IllegalArgumentException via testCreateResponse101. ===");
      return null;
    }catch (IOException e) {
      logger.error("*** IOException error. ***", e);      
      throw new TckTestException("createResponse101. error", e);
    }  
    return "fail to get IllegalArgumentException via testCreateResponse101";
  }  
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreateResponse102(SipServletRequest req) {
    serverEntryLog();
    
    try {
      req.createResponse(200).send();
    } catch (IOException e) {
      logger.error("*** IOException error. ***", e);      
      throw new TckTestException("createResponse102. error", e);
    }
        
    try {
    	SipServletResponse response = req.createResponse(200);
    } catch (IllegalStateException argue) {
      logger.info("=== Success to get IllegalStateException via testCreateResponse102. ===");
      URI fromURI = req.getRequestURI();
      String toURIStr = req.getHeader("From");
      try {
				sipFactory.createRequest(
					req.getApplicationSession(true), "MESSAGE", fromURI.toString(), toURIStr).send();
				return;
			} catch (ServletParseException e) {
				logger.error("***ServletParseException occurs during creating request ***",e);
	      throw new TckTestException(e);
			} catch (IOException e) {
				logger.error("***IOException occurs during creating or sending a request ***",e);
	      throw new TckTestException(e);
			}

    }
    
    SipServletRequest req2 = null;
		try {
			req2 = sipFactory.createRequest(
				req.getApplicationSession(true), "MESSAGE", req.getRequestURI().toString(), 
				req.getHeader("From"));
			req2.setHeader(TestConstants.TEST_FAIL_REASON,"IllegalStateException not thrown");
	    req2.send();
		} catch (ServletParseException e) {
			logger.error("***ServletParseException occurs during creating request ***",e);
      throw new TckTestException(e);
		} catch (IOException e) {
			logger.error("***IOException occurs during creating or sending a request ***",e);
      throw new TckTestException(e);
		}
  
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateResponse103(SipServletRequest req) {
    serverEntryLog();
    int errStatus = -9999;
   
    try {
      req.createResponse(errStatus, "createResponse").send();   
    } catch (IllegalArgumentException argue) {
      logger.info("=== Success to get IllegalArgumentException via testCreateResponse103. ===");
      return null;
    }catch (IOException e) {
      logger.error("*** IOException error. ***", e);      
      throw new TckTestException("createResponse103. error", e);
    }  
    return "fail to get IllegalArgumentException via testCreateResponse103";
  }  
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testCreateResponse104(SipServletRequest req) {
    serverEntryLog();
    
    try {
      req.createResponse(200, "createResponse").send();
    } catch (IOException e) {
      logger.error("*** IOException error. ***", e);      
      throw new TckTestException("createResponse104. error", e);
    }
    
    try {
      SipServletResponse response = req.createResponse(200, "createResponse");
    } catch (IllegalStateException argue) {
      logger.info("=== Success to get IllegalStateException via testCreateResponse104. ===");
      URI fromURI = req.getRequestURI();
      String toURIStr = req.getHeader("From");
      try {
				sipFactory.createRequest(
					req.getApplicationSession(true), "MESSAGE", fromURI.toString(), toURIStr).send();
				return;
			} catch (ServletParseException e) {
				logger.error("***ServletParseException occurs during creating request ***",e);
	      throw new TckTestException(e);
			} catch (IOException e) {
				logger.error("***IOException occurs during creating or sending a request ***",e);
	      throw new TckTestException(e);
			}
    }
    
    SipServletRequest req2 = null;
		try {
			req2 = sipFactory.createRequest(
				req.getApplicationSession(true), "MESSAGE", req.getRequestURI().toString(), 
				req.getHeader("From"));
			req2.setHeader(TestConstants.TEST_FAIL_REASON,"IllegalStateException not thrown");
	    req2.send();
		} catch (ServletParseException e) {
			logger.error("***ServletParseException occurs during creating request ***",e);
      throw new TckTestException(e);
		} catch (IOException e) {
			logger.error("***IOException occurs during creating or sending a request ***",e);
      throw new TckTestException(e);
		}
  }  
 
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testGetB2buaHelper101(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy;
    try {
      proxy = req.getProxy();
    } catch (TooManyHopsException e) {
      logger.error("*** TooManyHopsException. ***", e);
      return "TooManyHopsException";
    }
    if (null != proxy) {
      try {
        req.getB2buaHelper();
      } catch (IllegalStateException  e) {
        return null;
      }
      return "fail to get IllegalStateException via testGetB2buaHelper101.";
    } else {
      return "Proxy is null";
    }                
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetMaxForwards101(SipServletRequest req) {
    serverEntryLog();   
    
    SipURI oriToURI = (SipURI) req.getTo().getURI().clone();
    SipURI oriFromURI = (SipURI) req.getFrom().getURI().clone();

    SipServletRequest newReq = sipFactory.createRequest(req
        .getApplicationSession(), "MESSAGE", oriToURI, oriFromURI);
    try {
      newReq.setMaxForwards(256);
      return "Fail to get IllegalArgumentException via testSetMaxForwards101.";
    } catch (IllegalArgumentException e) {
      logger.info("=== get  right IllegalArgumentException ===");
    }
    try {
      newReq.setMaxForwards(-1);
      return "Fail to get IllegalArgumentException via testSetMaxForwards101.";
    } catch (IllegalArgumentException e) {
      logger.info("=== get right IllegalArgumentException ===");
      return null;
    }  
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetProxy101(SipServletRequest req) {
    serverEntryLog();
    req.setMaxForwards(0);
    try {
      //container send 483 response to UAC
      if (0== req.getMaxForwards()){
        req.getProxy();   
        req.createResponse(500, "Fail to get TooManyHopsException via testGetProxy101").send();
      }else req.createResponse(500, "MaxForwards not 0").send();
    } catch (TooManyHopsException e1) {
      logger.info("==== testGetProxy101() get TooManyHopsException successfully. ===");
      try {
        req.createResponse(483, "get TooManyHopsException successfully").send();
      } catch (IOException e) {
        logger.error("*** testGetProxy101 IOException ***", e);
        throw new TckTestException(e);
      }
    }catch (IOException e) {
      logger.error("*** testGetProxy101 IOException ***", e);
      throw new TckTestException(e);
    } 
  }  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetProxy102(SipServletRequest req) {
    serverEntryLog();
    String failReason = null;
    try {
      req.createResponse(200).send();
    } catch (IOException e1) {
      logger.error("*** testGetProxy102 IOException ***", e1);
      throw new TckTestException(e1);
    }      
    try {
      req.getProxy();
    } catch (IllegalStateException  e) {
      logger.info("=== testGetProxy102 get IllegalStateException. ===");      
    } catch (TooManyHopsException e1) {
      logger.error("*** testGetProxy102 TooManyHopsException ***", e1);
      failReason = "fail to get TooManyHopsException via testGetProxy102.";
    }
    sendMessage(req, failReason);
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetProxy103(SipServletRequest req) {
    serverEntryLog();
    req.setMaxForwards(0);
    try {
      //container send 483 response to UAC
      if (0== req.getMaxForwards()){
        req.getProxy(true);   
        req.createResponse(500, "fail to get TooManyHopsException via testGetProxy103").send();
      }else req.createResponse(500, "MaxForwards not 0").send();
    } catch (TooManyHopsException e1) {
      logger.info("==== testGetProxy103() get TooManyHopsException ===");
      try {
        req.createResponse(483, "get TooManyHopsException successfully.").send();
      } catch (IOException e) {
        logger.error("*** testGetProxy103 IOException ***", e);
        throw new TckTestException(e);
      }
    }catch (IOException e) {
      logger.error("*** testGetProxy103 IOException ***", e);
      throw new TckTestException(e);
    } 
  }  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetProxy104(SipServletRequest req) {
    serverEntryLog();
    String failReason = null;
    try {
      req.createResponse(200).send();
    } catch (IOException e1) {
      logger.error("*** testGetProxy104 IOException ***", e1);
      throw new TckTestException(e1);
    }      
    try {
      req.getProxy(true);
    } catch (IllegalStateException  e) {
      logger.info("=== testGetProxy104 get IllegalStateException successfully. ===");      
    } catch (TooManyHopsException e1) {
      logger.error("*** testGetProxy104 get TooManyHopsException successfully.***", e1);
      failReason = "fail to get TooManyHopsException via testGetProxy104.";
    }
    sendMessage(req, failReason);
  }  
 
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testPushPath101(SipServletRequest req) {
    serverEntryLog();
 
    if (!"REGISTER".equalsIgnoreCase(req.getMethod())) {
      try {        
        Address address = sipFactory.createAddress((SipURI)req.getTo().getURI().clone());
        req.pushPath(address);
        return "Fail to get IllegalStateException via testPushPath101.";
      } catch (IllegalStateException  e) {
        logger.info("=== testPushPath101 get IllegalStateException successfully. ===");
        return null;
      }
    }else return "UAC should send none REGISTER request.";
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetRequestURI101(SipServletRequest req) {
    serverEntryLog();    
    
    try {
      req.setRequestURI(null);
      return "Fail to get NullPointerException via testSetRequestURI101.";
    } catch (NullPointerException e) {
      logger.info("=== testSetRequestURI101 get NullPointerException successful. ===");
      return null;
    }    
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testGetRoutingDirective101(SipServletRequest req) {
    serverEntryLog();
    
    if ("INVITE".equalsIgnoreCase(req.getMethod())){
      logger.info("=== testGetRoutingDirective101 received INVITE. ===");
      try {
        markHeader(req);
        req.createResponse(200).send();
      } catch (IOException e) {
        logger.error("*** testGetRoutingDirective101 IOException. ***");
        throw new TckTestException(e);
      }
    }else {
      try {
        req.createResponse(500).send();
      } catch (IOException e) {
        logger.error("*** testGetRoutingDirective101 IOException. ***");
        throw new TckTestException(e);
      }
    }
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_NORMAL)
  public void testSetRoutingDirective101(SipServletRequest req) {
    serverEntryLog();
    
    if ("INVITE".equalsIgnoreCase(req.getMethod())){
      logger.info("=== testSetRoutingDirective101 received INVITE. ===");
      try {
        markHeader(req);
        req.createResponse(200).send();
      } catch (IOException e) {
        logger.error("*** testSetRoutingDirective101 IOException. ***");
        throw new TckTestException(e);
      }
    }else {
      try {
        req.createResponse(500).send();
      } catch (IOException e) {
        logger.error("*** testSetRoutingDirective101 IOException. ***");
        throw new TckTestException(e);
      }
    }
  }
  
  private void sendMessage(SipServletRequest req, String failReason) {
    SipURI oriToURI = (SipURI) req.getTo().getURI().clone();
    SipURI oriFromURI = (SipURI) req.getFrom().getURI().clone();
    SipServletRequest newReq;    

    SipApplicationSession newAppSession = sipFactory.createApplicationSession();
    newReq = sipFactory.createRequest(newAppSession, "MESSAGE", oriToURI, oriFromURI);

    if (null != failReason) 
      newReq.addHeader(TestConstants.TEST_FAIL_REASON, failReason);        
      
    try {
      newReq.send();
    } catch (IOException e) {
      logger.error("*** IOException error. ***", e);
      throw new TckTestException(e);
    }
  }
 
  protected void doBye(SipServletRequest origReq) throws ServletException,
      IOException {     
    if ("testSetRoutingDirective101".equals(origReq.getSession().getAttribute(
        TestConstants.METHOD_HEADER))){
      try {
        String host = ((SipURI) origReq.getRequestURI()).getHost();
        SipServletRequest newReq = sipFactory.createRequest(origReq
            .getApplicationSession(), "MESSAGE", sipFactory
            .createAddress("sip:B2BUA102@" + host), origReq.getTo());
        newReq.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE,
            origReq);
        logger
            .error("*** fail to get IllegalStateException via setRoutingDirective(). ***");
        origReq.createResponse(500, "failed to get IllegalStateException via setRoutingDirective().").send();
      } catch (IllegalStateException e) {
        logger
            .info("=== get IllegalStateException successfully via testSetRoutingDirective101(). ===");
        origReq.createResponse(200).send();
      }
    } else if ("testGetRoutingDirective101".equals(origReq.getSession().getAttribute(
        TestConstants.METHOD_HEADER))){
      try {
        origReq.getRoutingDirective();
        logger
            .error("*** fail to get IllegalStateException via getRoutingDirective(). ***");
        origReq.createResponse(500, "failed to get IllegalStateException via getRoutingDirective().").send();
      } catch (IllegalStateException e) {
        logger
            .info("=== get IllegalStateException successfully via getRoutingDirective(). ===");
        origReq.createResponse(200).send();
      }
    } else {
      origReq.createResponse(200).send();
    }
  }
  protected void doAck(SipServletRequest req) 
    throws ServletException, IOException {
    req.getSession().setAttribute(RESULT, "ACK");
  }
  private void markHeader(SipServletRequest req) {
    req.getSession().setAttribute(TestConstants.METHOD_HEADER,
        req.getHeader(TestConstants.METHOD_HEADER));
    if ("testCreateCancel101".equalsIgnoreCase(req.getHeader(TestConstants.METHOD_HEADER))){
      req.getSession().setAttribute("oriRequest",req);
    }
  }
  protected void doInvite(SipServletRequest req) throws ServletException,
      IOException {
    req.createResponse(200).send();
  }
  private boolean checkURI(URI route){    
    String host = null;
    int port = -1;
    SipURI clone = (SipURI)route.clone();
    List<SipURI> uris = (List<SipURI>) getServletContext().getAttribute(OUTBOUND_INTERFACES);
    String routeHost = clone.getHost();
    if (uris.size() <= 0) return false;    
//    System.out.println("route uri="+clone.getHost() + ":" + clone.getPort());
    for (SipURI uri : uris) {      
      host = uri.getHost();
      port = uri.getPort();
//      System.out.println("get outbout interface:" + host + ":" + port);
      if (clone.getHost().equalsIgnoreCase(host) && (clone.getPort() == port)) {
        return true;
      }else if(routeHost.equals("127.0.0.1")|| routeHost.equalsIgnoreCase("localhost")){
        if(clone.getPort() == port) return true;
      }
    }
    return false;
    
  }
}
