/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.  
 * 
 * ProxyBranchServlet is used to test the APIs of javax.servlet.sip.ProxyBranch.
 */
package com.bea.sipservlet.tck.apps.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;
import com.bea.sipservlet.tck.common.TckTestException;
import com.bea.sipservlet.tck.common.TestConstants;
import com.bea.sipservlet.tck.utils.TestUtil;

@SipServlet(applicationName = "com.bea.sipservlet.tck.apps.apitestapp", 
    name = "ProxyBranch")
public class ProxyBranchServlet extends BaseServlet {
  private static Logger logger = Logger.getLogger(ProxyBranchServlet.class);

  private static final long serialVersionUID = -1652672419697066869L;

  private ProxyBranch branchForCancel001 = null;

  private ProxyBranch branchForCancel101 = null;

  private ProxyBranch branchForCancel002 = null;

  private ProxyBranch branchForGetRecursedProxyBranches001 = null;

  private ProxyBranch branchForgetResonse001 = null;
  

  public void testCancel001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branchForCancel001 = branch;
        branch.setRecordRoute(true);
        startProxy(proxy);
      }
    }
  }
  
  public void testCancel101(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branchForCancel101 = branch;
        branch.setRecordRoute(true);
        startProxy(proxy);
      }
    }
  } 
  
  public void testCancel002(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branchForCancel002 = branch;
        branch.setRecordRoute(true);
        startProxy(proxy);
      }
    }
  }
  
  public void testGetAddToPath001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        // test setAddToPath(boolean flag) and getAddToPath()
        // UA2 check the received REGISTER message or not.
        branch.setAddToPath(false);
        boolean isAddToPath1 = branch.getAddToPath();
        branch.setAddToPath(true);
        boolean isAddToPath2 = branch.getAddToPath();
        if (!isAddToPath1 && isAddToPath2) {
          startProxy(proxy);
        }
      }
    }
  }
  
  public void testGetPathURI001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req,TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null){        
        branch.setRecordRoute(true);
        // test getPathURI() will return null uri with AddToPath enabled.
        // UA1 check the received 200/REGISTER, and with the Path header or not.
        branch.setAddToPath(true);
        try{
          SipURI uri = branch.getPathURI();
          if (uri == null) {
            req.addHeader(TestConstants.TEST_FAIL_REASON, "Fail to retrieve "
                + "PathURI through Proxy with AddToPath enabled.");        
          }
        } catch (IllegalStateException e) {
          req.addHeader(TestConstants.TEST_FAIL_REASON, "getPathURI() should "
              + "not throw IllegalStateException with AddToPath enabled.");
          logger.error("*** getPathURI() Should not throw IllegalStateException "
              + "with AddToPath enabled.***", e);
        }         
        startProxy(proxy);  
      } 
    }
  }
    
  public void testGetPathURI101(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        // getPathURI will throw IllegalStateException, if addToPath is disable.
        branch.setAddToPath(false);
        try {
          branch.getPathURI();
          logger.error("*** Should throw IllegalStateException, since the "
              + "addToPath is disable. ***");
        } catch (IllegalStateException e) {
          logger.info("=== Throw IllegalStateException, since the "
              + "addToPath is disable. ===");
          startProxy(proxy);
        }        
      }
    }
  }
  
 
  
  public void testGetProxy001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        Proxy branchProxy = branch.getProxy();
        // Judge whether the proxy is the associated proxy with this branch.
        boolean isProxyEqual = isSameProxy(proxy, branchProxy);
        if (!isProxyEqual) {
          req.addHeader(TestConstants.TEST_FAIL_REASON,
              "The proxy get from the ProxyBranch is not the one associated "
                  + "with this branch.");
          logger.error("*** The proxy get from the ProxyBranch is not the one "
              + "associated with this branch.***");
        }
        startProxy(proxy);
      }
    }
  }
  
  public void testGetProxyBranchTimeout001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        branch.setProxyBranchTimeout(30);
        int proxyBranchTimeout = branch.getProxyBranchTimeout();
        if (proxyBranchTimeout < 0) {
          req.addHeader(TestConstants.TEST_FAIL_REASON,
              "The proxyBranchTimeout get from the branch is negative.");
          logger.error("*** The proxyBranchTimeout get from the branch is "
              + "negative.***");
        }
        startProxy(proxy);
      }
    }
  }
  
  public void testGetRecordRoute001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        // test setRecordRoute(boolean flag) and getRecordRoute()
        // UA2 check the received MESSAGE message or not.
        branch.setRecordRoute(false);
        boolean isRecordRoute1 = branch.getRecordRoute();
        branch.setRecordRoute(true);
        boolean isRecordRoute2 = branch.getRecordRoute();
        if (!isRecordRoute1 && isRecordRoute2) {
          startProxy(proxy);
        }
      }
    }
  }
  
  public void testGetRecordRouteURI001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        // test setRecordRoute(boolean flag) and getRecordRouteURI()
        try {
          branch.setRecordRoute(true);
          SipURI uri = branch.getRecordRouteURI();
          if (uri == null) {
            req.addHeader(TestConstants.TEST_FAIL_REASON, "Fail to retrieve "
                + "Record-Route through Proxy with record-routing enabled.");
            logger.error("*** Fail to retrieve Record-Route through Proxy" +
                " with record-routing enabled. ***");
          }
        } catch (IllegalStateException e) {
          req.addHeader(TestConstants.TEST_FAIL_REASON, "Should not throw "
              + "IllegalStateException since record-routing is enabled.");
          logger.error("*** Should not throw IllegalStateException "
              + "since record-routing is enabled.***", e);
        }
        startProxy(proxy);
      }
    }
  }

  public void testGetRecordRouteURI101(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        // Throws IllegalStateException - if record-routing is not enabled.
        try {
          branch.setRecordRoute(false);
          SipURI uri = branch.getRecordRouteURI();
          logger.error("*** Should throw IllegalStateException since  "
              + "record-routing is not enabled. ***");
        } catch (IllegalStateException e) {
          logger.info("=== Thrown IllegalStateException since record-routing "
              + "is not enabled. ===");
          startProxy(proxy);
        }        
      }
    }
  }
  
  public void testGetSetRecurse001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        // test setRecurse(boolean flag) and getRecurse()
        // UA2 check the received MESSAGE message or not.
        branch.setRecurse(false);
        boolean isRecurse1 = branch.getRecurse();
        branch.setRecurse(true);
        boolean isRecurse2 = branch.getRecurse();
        if (!isRecurse1 && isRecurse2) {
          startProxy(proxy);
        }
      }
    }
  }
  
  public void testgetRecursedProxyBranches001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        // test setAddToPath(boolean flag) and getAddToPath()
        // UA2 check the received REGISTER message or not.
        branch.setAddToPath(false);
        branch.setRecurse(true);
        proxy.setParallel(true);
        branchForGetRecursedProxyBranches001 = branch;
        startProxy(proxy);
      }
    }
  }    
    
  public void testGetRequest001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        // test getRequest()
        branch.setAddToPath(false);
        SipServletRequest request = branch.getRequest();
        // Judge whether the request is equal to req.
        boolean isSameReq = isSameRequest(req,request);
        if(!isSameReq){
          req.addHeader(TestConstants.TEST_FAIL_REASON, "Failed "
              + "the returned request from branch is not correct.");
          logger.error("*** The returned request from branch is not correct.***");
        }
        startProxy(proxy);
      }
    }
  } 
  
  public void testGetResponse001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        // test getRequest()
        branch.setAddToPath(false);
        branchForgetResonse001 = branch;
        startProxy(proxy);
      }
    }
  }  
  
  public void testIsStarted001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        // test isStarted()
        boolean isStarted = branch.isStarted();
        if (isStarted) {
          req.addHeader(TestConstants.TEST_FAIL_REASON, "Failed,branch is not "
              + "started yet, but ProxyBranch.isStarted() return true");
          logger.error("*** Failed,branch is not "
              + "started yet, but ProxyBranch.isStarted() return true.***");
        }
        startProxy(proxy);
      }
    }
  }
  
  public void testSetAddToPath001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        // test setAddToPath(true)
        // UA1 check the received 200/REGISTER, and with the Path header or not.
        branch.setAddToPath(true);
        startProxy(proxy);
      }
    }
  }
  
  public void testSetOutboundInterface001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        SipURI tempUri = getOutboundInterfaceSipURI(req);
        if (tempUri != null) {
          InetAddress ins = getInetAddress(tempUri.getHost());
          if (req.getApplicationSession().isValid() && ins != null) {
            try {
              branch.setOutboundInterface(ins);
            } catch (IllegalStateException e) {
              req.addHeader(TestConstants.TEST_FAIL_REASON, "Should not throw "
                  + "IllegalStateException, since appSession is valid.");
            } catch (NullPointerException e) {
              req.addHeader(TestConstants.TEST_FAIL_REASON, "Should not throw "
                  + "NullPointerException, since the address is not null.");
            }
          }
        }
        startProxy(proxy);
      }
    }
  }
  
  
  public void testSetOutboundInterface101(SipServletRequest req) {
    serverEntryLog();
    SipApplicationSession newAppSession = sipFactory.createApplicationSession();
    String to = req.getHeader(TestConstants.UA2_URI);
    String from = req.getFrom().toString();
    
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    Proxy proxy = getProxyFromRequest(req);
    ProxyBranch branch = createProxyBranch(proxy, ua2URI);
    if (branch != null) {
      branch.setRecordRoute(true);
      SipURI tempUri = getOutboundInterfaceSipURI(req);
      if (tempUri != null) {
        InetAddress ins = getInetAddress(tempUri.getHost());
        req.getApplicationSession().invalidate();
        if (!req.getApplicationSession().isValid() && ins != null) {
          try {
            branch.setOutboundInterface(ins);
          } catch (IllegalStateException e) {
            try {
              SipServletRequest messageReq = sipFactory.createRequest(
                  newAppSession, "MESSAGE", from, to);
              messageReq.send();
            } catch (IOException e1) {
              logger.error("*** Throw IOException during sessionExpired. ***",
                  e1);
              throw new TckTestException(e1);
            } catch (ServletParseException e2) {
              logger.error(
                  "*** Throw ServletParseException during sessionExpired. ***",
                  e2);
              throw new TckTestException(e2);
            }
          } catch (NullPointerException e) {
            logger.error("*** Should not throw NullPointerException, "
                + "since the address is not null.***", e);
            throw new TckTestException(e);
          }
        }
      }
    }
  }

  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetOutboundInterface102(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    ProxyBranch branch = createProxyBranch(proxy, req.getFrom().getURI());
    InetAddress ins = null;
    try {
      branch.setOutboundInterface(ins);
    } catch (NullPointerException e) {
      return null;
    }
    return "Should throw NullPointerException,since the "
        + "InetAddress is null.";
  }
  
  public void testSetOutboundInterface002(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        SipURI tempUri = getOutboundInterfaceSipURI(req);
        if (tempUri != null) {
          InetAddress ins = getInetAddress(tempUri.getHost());
          InetSocketAddress ids = createInetSocketAddress(ins, tempUri
              .getPort());
          if (ids != null) {
            try {
              branch.setOutboundInterface(ids);
            } catch (NullPointerException e) {
              req.addHeader(TestConstants.TEST_FAIL_REASON, "Should not throw "
                  + "NullPointerException, since the address is not null.");
            } catch (IllegalArgumentException e) {
              req.addHeader(TestConstants.TEST_FAIL_REASON,"Should not throw "
                  + "IllegalArgumentException, since the address is from " 
                  + "outbound interface.");
            }
          }
        }
        startProxy(proxy);
      }
    }
  }

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetOutboundInterface103(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    ProxyBranch branch = createProxyBranch(proxy, req.getFrom().getURI());
    InetSocketAddress ids = null;
    try {
      branch.setOutboundInterface(ids);
    } catch (NullPointerException e) {
      return null;
    }
    return "Should thrown NullPointerException, since address is null";
  }
  
  public void testSetProxyBranchTimeout001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        branch.setRecordRoute(true);
        try {
          branch.setProxyBranchTimeout(10);
          int proxyBranchTimeout = branch.getProxyBranchTimeout();
          if (proxyBranchTimeout != 10) {
            req.addHeader(TestConstants.TEST_FAIL_REASON,
                "The proxyBranchTimeout get from the branch is not correct.");
            logger.error("*** The proxyBranchTimeout get from the branch is "
                + "not correct.***");
          }
        } catch (IllegalArgumentException e) {
          req.addHeader(TestConstants.TEST_FAIL_REASON,
              "Thrown IllegalArgumentException since the value is negative.");
          logger.error("*** Thrown IllegalArgumentException"
              + " since the value is negative.***");
        }
        startProxy(proxy);
      }
    }
  }
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testSetProxyBranchTimeout101(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    ProxyBranch branch = createProxyBranch(proxy, req.getFrom().getURI());
    try {
      branch.setProxyBranchTimeout(-10);     
    } catch (IllegalArgumentException e) {
      return null;
    }
    return "Should throw IllegalArgumentException since this value is negative.";
  }  
  
  public void testSetRecordRoute001(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        // test setRecordRoute(true)
        // UA1 check the received 200/MESSAGE, and with the Record-route header
        // or not.
        try {
          branch.setRecordRoute(true);
        } catch (IllegalStateException e) {
          req.addHeader(TestConstants.TEST_FAIL_REASON, "Thrown "
              + "IllegalStateException during branch.setRecordRoute(true).");
          logger.error("*** Thrown IllegalStateException during "
              + "branch.setRecordRoute(true). ***", e);
        }
        startProxy(proxy);
      }
    }
  }
  
  public void testSetRecordRoute101(SipServletRequest req) {
    serverEntryLog();
    Proxy proxy = getProxyFromRequest(req);
    URI ua2URI = getUaUri(req, TestConstants.UA2_URI);
    if (ua2URI != null) {
      ProxyBranch branch = createProxyBranch(proxy, ua2URI);
      if (branch != null) {
        startProxy(proxy);
        try {
          branch.setRecordRoute(true);
          logger.error("*** Should throw IllegalStateException "
              + "since proxy has already been started. ***");
        } catch (IllegalStateException e) {
          logger.info("=== Thrown IllegalStateException during "
              + "branch.setRecordRoute(true),"
              + "since the proxy has already been started. ===");
          SipServletRequest messageReq = sipFactory.createRequest(req
              .getApplicationSession(), "MESSAGE", req.getTo().getURI(), ua2URI);          
          try {
            messageReq.send();
          } catch (IOException e1) {
            logger
                .error("*** Throw IOException during request.send(). ***", e1);
          }
        }
      }
    }
  }
    
  public void doProvisionalResponse(SipServletResponse resp) {
    String header = resp.getRequest().getHeader(TestConstants.METHOD_HEADER);
    String value = header.substring(header.indexOf(":") + 1).trim();
    if ("testCancel001".equals(value)
        && SipServletResponse.SC_RINGING == resp.getStatus()
        && branchForCancel001 != null) {
      // Test ProxyBranch.cancel();
      cancleProxyBranch(branchForCancel001, false);
    } else if ("testCancel002".equals(value)
        && SipServletResponse.SC_RINGING == resp.getStatus()
        && branchForCancel002 != null) {
      // Test ProxyBranch.cancel(protocol,reasonPhase,reasonText);
      cancleProxyBranch(branchForCancel002, true);
    }
  }  
  
  public void doSuccessResponse(SipServletResponse resp) {
    String header = resp.getRequest().getHeader(TestConstants.METHOD_HEADER);    
    if (header != null) {
	    String value = header.substring(header.indexOf(":") + 1).trim();
	    if (resp.getStatus() == SipServletResponse.SC_OK) {
	      if ("testgetRecursedProxyBranches001".equals(value)
	          && branchForGetRecursedProxyBranches001 != null) {
	        String getRecursedOK = "NO";
	        List<ProxyBranch> recursedBranch = branchForGetRecursedProxyBranches001
	            .getRecursedProxyBranches();
	        if (recursedBranch != null & recursedBranch.size() > 0) {
	          getRecursedOK = "getRecursedOK";
	        }
	        resp.addHeader("getRecursedOK", getRecursedOK);
	      } else if ("testGetResponse001".equals(value)
	          && branchForgetResonse001 != null) {
	        logger.debug("--- 200OK for testgetResponse001 ---");
	        SipServletResponse response = branchForgetResonse001.getResponse();
	        if (response != null) {
	          URI respURI = resp.getRequest().getRequestURI();
	          URI theURI = response.getRequest().getRequestURI();
	          if (respURI.equals(theURI)) {
	            resp.addHeader("ResponseHeader", "true");
	          } else {
	            logger.error("*** response get from branch is not correct. ***");
	          }
	        }
	      } else if ("testCancel101".equals(value) && branchForCancel101 != null) {
	        try {
	          branchForCancel101.cancel();
	        } catch (IllegalStateException e) {
	          resp.addHeader("cancleWhenTransactionFinished",
	              "cancleWhenTransactionFinished");
	        }
	      }
	    }
    }
  } 
  
/*******************************************************************************
 * 
 *                          Utility methods.
 * 
 * *****************************************************************************
 */  
  
  
  /**
   * Judge whether the two request are equal or not. 
   * Both of the two request should contain the Header of "TestConstants.UA2_URI"
   */
  private boolean isSameRequest(SipServletRequest req1, SipServletRequest req2) {
    if (req1 == req2) {
      return true;
    }
    // Compare the CallID,From,To,requestURI,Method,Specific Header.
    // if any of them are not equal return false,otherwise return true;
    if (req1 != null && req2 != null) {
      Address from1 = req1.getFrom();
      Address from2 = req2.getFrom();
      if (!from1.equals(from2)) {
        return false;
      } else {
        URI reqURI1 = req1.getRequestURI();
        URI reqURI2 = req2.getRequestURI();
        if (!reqURI1.equals(reqURI2)) {
          return false;
        } else {
          Address to1 = req1.getTo();
          Address to2 = req2.getTo();
          if (!to1.equals(to2)) {
            return false;
          } else {
            String callID1 = req1.getCallId();
            String callID2 = req2.getCallId();
            if (!callID1.equals(callID2)) {
              return false;
            } else {
              String method1 = req1.getMethod();
              String method2 = req2.getMethod();
              if (!method1.equals(method2)) {
                return false;
              } else {
                String hd1 = req1.getHeader(TestConstants.UA2_URI);
                String hd2 = req2.getHeader(TestConstants.UA2_URI);
                if (!hd1.equals(hd2)) {
                  return false;
                }
              }
            }
          }
        }
      }
    } else {
      return false;
    }
    return true;
  }
  
  /**
   * Return true if the two proxy are logically equal.  
   */
  private boolean isSameProxy(Proxy p1, Proxy p2) {
    if (p1 == p2) {
      return true;
    }
    if (p1 != null && p2 != null) {
      // (1) check qual of the requestURI of the original request
      URI uri1 = p1.getOriginalRequest().getRequestURI();
      URI uri2 = p2.getOriginalRequest().getRequestURI();
      if (!uri1.equals(uri2)) {
        return false;
      } else {
        // (2) Check the proxy branches are logically equal
        List<ProxyBranch> branchList1 = p1.getProxyBranches();
        List<ProxyBranch> branchList2 = p2.getProxyBranches();
        if (branchList1.size() != branchList2.size()) {
          return false;
        } else {
          // for each branch in list1 and list2, check equal of branch
          List<URI> requestURIs = new ArrayList<URI>();
          for (ProxyBranch br : branchList2) {
            URI uri3 = br.getRequest().getRequestURI();
            requestURIs.add(uri3);
          }
          for (ProxyBranch br : branchList1) {
            URI uri4 = br.getRequest().getRequestURI();
            if (!requestURIs.contains(uri4)) {
              return false;
            }
          }
        }
      }
    } else {
      return false;
    }
    return true;
  }
  
  /**
   * Return the created SipURI by sipFactory.createURI(String uri)
   * with try/catch 
   */
  private SipURI createURI(String uri){
    try{
      SipURI sipUri = (SipURI) sipFactory.createURI(uri);
      return sipUri;
    }catch(ServletParseException e){
      logger.error("*** Thrown ServletParseException during "
          + "sipFactory.createURI(uri). ***", e);      
      throw new TckTestException(e);      
    }
  }
  
  private InetAddress getInetAddress(String host) {
    try {
      InetAddress ins = InetAddress.getByName(host);
      return ins;
    } catch (UnknownHostException e) {
      logger.error("*** Thrown UnknownHostException during "
          + "InetAddress.getByName(host)***", e);
      throw new TckTestException(e);
    }
  }
  
  private InetSocketAddress createInetSocketAddress(InetAddress addr, int port) {
    try {
      InetSocketAddress ids = new InetSocketAddress(addr, port);
      return ids;
    } catch (IllegalArgumentException e) {
      logger.error("*** Thrown IllegalArgumentException during new "
          + "InetSocketAddress(addr, port). ***", e);
      throw new TckTestException(e);
    }
  }
  
  /**
   * Get the proxy by SipServletRequest with try/catch.
   */
  private Proxy getProxyFromRequest(SipServletRequest req) {
    try {
      return req.getProxy();
    } catch (TooManyHopsException e) {
      logger.error("*** Thrown TooManyHopsException when retrieving Proxy "
          + "from SipServletRequest. ***", e);
      throw new TckTestException(e);
    }
  }
  
  /**
   * Create a ProxyBranch with the uri.
   */
  private ProxyBranch createProxyBranch(Proxy proxy, URI uri) {
    if (proxy == null || uri == null) {
      return null;
    }
    try {
      List<URI> targets = new ArrayList<URI>(1);
      targets.add(uri);
      List<ProxyBranch> branches = proxy.createProxyBranches(targets);
      ProxyBranch branch = branches.get(0);
      return branch;
    } catch (IllegalArgumentException e) {
      logger.error("*** Thrown IllegalArgumentException during "
          + "proxy.createProxyBranches(targets). ***", e);
      throw new TckTestException(e);
    }
  }
  
  /**
   * Cancel a ProxyBranch with try/catch.
   */
  private void cancleProxyBranch(ProxyBranch branch, boolean withParameters) {
    try {
      if (branch != null) {
        if (withParameters) {
          String[] protocol = { "SIP" };
          int[] reasonCode = { 200 };
          String[] reasonTest = { "Response is 200OK." };
          branch.cancel(protocol, reasonCode, reasonTest);
        } else {
          branch.cancel();
        }
      } else {
        logger.error("*** The branch is null and can not be canceled. ***");
      }
    } catch (IllegalStateException e) {
      logger.error("***  Thrown IllegalStateException during "
          + "ProxyBranch.cancel(). ***", e);
      throw new TckTestException(e);
    }
  }
  
  /**
   * Start the Proxy with try/catch.
   */
  private void startProxy(Proxy proxy) {
    try {
      if (proxy != null) {
        proxy.startProxy();
      }
    } catch (IllegalStateException e) {
      logger.error("*** Thrown IllegalStateException during "
          + "proxy.startProxy(). ***", e);
      throw new TckTestException(e);
    }
  }

  
  /**
   * Get the URI of ua from the private header in req. (Eg,get UA2,UA3 URI from
   * private header)
   */
  private URI getUaUri(SipServletRequest req, String ua) {
    if (req == null || ua == null) {
      return null;
    }
    URI uri = null;
    String header = req.getHeader(ua);
    if (TestUtil.hasText(header)) {
      try {
        uri = sipFactory.createURI(header);
      } catch (ServletParseException e) {
        logger.error("*** ServletParseException when creating URI. ***", e);
        throw new TckTestException(e);
      }
    }
    return uri;
  }
  
  /**
   * Return a SipURI which is selected from OUTBOUND_INTERFACES.
   * This returned SipURI must not equal to the req.getTo().getURI().
   */
  private SipURI getOutboundInterfaceSipURI(SipServletRequest req) {
    List<SipURI> uris = (List<SipURI>) getServletContext().getAttribute(
        OUTBOUND_INTERFACES);
    if (uris.size() <= 0) {
      return null;
    }

    SipURI tempUri = (SipURI) req.getTo().getURI();
    logger.debug("--- The URI of req.getTo() is:" + tempUri.toString() + " ---");
    for (SipURI uri : uris) {
      if (!tempUri.equals(uri)) {
        logger.debug("--- The URI in OUTBOUND_INTERFACES which not equal to "
            + "the req.getTo().getURI() is:" + uri.toString() + " ---");
        tempUri = uri;
        break;
      }
    }    
    return tempUri;
  }
}
