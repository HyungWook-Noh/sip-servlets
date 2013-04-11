/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved.  
 * 
 * SipFactoryServlet is used to test the APIs of
 * javax.servlet.sip.SipFactory
 *
 */
package com.bea.sipservlet.tck.apps.api;

import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.apps.TestStrategy;

@SipServlet(name = "SipFactory")
public class SipFactoryServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(SipFactoryServlet.class);
	
	public SipFactoryServlet() {
		super();
	}

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateAddress001(SipServletRequest req) {
		serverEntryLog();
		
		String orig = "<sip:alice@10.130.8.107>;tag=218845870";
		Address addr = null;
		try {
			addr = sipFactory.createAddress(orig);
		} catch (ServletParseException e) {
			logger.error("*** ServletParseException thrown when createAddress() ***", e);
			return "Fail to create address via createAddress(String addr).";
		}
		if (addr != null) {
			URI uri = addr.getURI();
			String value = addr.getParameter("tag");
			if (uri != null) {
				if (value != null){
					if ("sip:alice@10.130.8.107".equals(uri.toString())) {
						logger.info("=== uri gotten from Address is matched the orig String."
								+ "===");
					} else {
						logger.error("*** Uri gotten from Address is not matched " 
								+ "the orig String. ***");
						return "Uri gotten from Address is not matched the orig String.";
					}
					if ("218845870".equals(value)) {
						logger.info("=== parameter gotten from Address is matched " 
								+ "the orig String. ===");
						logger.info("=== Address is created correctly via createAddress()." 
								+ " ===");
						return null;
					} else {
						logger.error("*** Parameter gotten from Address is not matched " 
								+ "the orig String. ***");
						return "Parameter gotten from Address is not matched the orig String.";
					}
				} else {
					logger.error("*** Parameter gotten from Address is null. ***");
					return "Parameter gotten from Address is null.";
				}
			} else {
				logger.error("*** Uri gotten from Address is null. ***");
				return "Uri gotten from Address is null.";
			}
		} else {
			logger.error("*** Address is not created by " 
					+ "createAddress(String addr). ***");
			return "Fail to create address via createAddress(String addr).";
		}
	}
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
  public String testCreateAddress101(SipServletRequest req) {
  	serverEntryLog();
  	String orig = "alice@10.130.8.107";
		Address addr = null;
		try {
			addr = sipFactory.createAddress(orig);
		} catch (ServletParseException e) {
			logger.info("=== Expected ServletParseException thrown when createAddress() ===");
			return null;
		}
		return "Fail to throw ServletParseException.";
  }
	
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateAddress002(SipServletRequest req) {
  	serverEntryLog();
		
		String orig = "*";
		Address addr = null;
		
		try {
			addr = sipFactory.createAddress(orig);
		} catch (ServletParseException e) {
			logger.error("*** ServletParseException thrown when createAddress() ***", e);
			return "Fail to create address via createAddress(String addr).";
		}
		if (addr != null && addr.isWildcard()) {
				logger.info("=== Address is created with wildcard. ===");
				return null;
		} else {
			logger.error(
				"*** Address is not created correctly with wildcard " 
				+ "by createAddress(String addr). ***");
			return "Address is not created correctly with wildcard.";
		}
	}

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateAddress003(SipServletRequest req) {
  	serverEntryLog();
		
		Address cont = null;
		try {
			cont = req.getAddressHeader("From");
		} catch (ServletParseException e) {
			logger.error("*** ServletParseException thrown when getAddressHeader() ***", e);
		}

		if (cont != null) {
			URI orig = cont.getURI();
			if (orig != null) {
				Address addr = sipFactory.createAddress(orig);
				if (addr != null) {
					String dispName = addr.getDisplayName();
					if (dispName == null) {
						logger.info("=== Address is created correctly with specified URI. ===");
						return null;
					} else {
						logger.error("*** DisplayName is not null, " 
								+ "address is not created correctly with specified URI. ***");
						return "DisplayName is not null, address is not created correctly " 
							+ "with specified URI.";
					}
				} else {
					logger.error("*** Address is not created " 
							+ "by createAddress(URI uri). ***");
					return "Address can not be created via createAddress(URI uri).";
				}
			}
		}
		return "Fail to createAddress(URI uri).";
	}
	
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateAddress004(SipServletRequest req) {
  	serverEntryLog();
		
		Address cont = null;
		try {
			cont = req.getAddressHeader("From");
		} catch (ServletParseException e) {
			logger.error("*** ServletParseException thrown when getAddressHeader() ***", e);
		}

		if (cont != null) {
			URI orig = cont.getURI();
			if (orig != null) {
				Address addr = sipFactory.createAddress(orig, "test");
				if (addr != null) {
					String dispName = addr.getDisplayName();
					assert dispName != null;
					if (dispName != null && "test".equals(dispName)) {
						logger.info("=== Address is created correctly with specified URI. ===");
						return null;
					} else {
						logger.error("*** DisplayName is not null, " 
								+ "address is not created correctly with specified URI. ***");
						return "DisplayName is wrong, address is not created correctly " 
							+ "with specified URI.";
					}
				} else {
					logger.error("*** Address is not created by " 
								+ "SipFactory.createAddress(URI uri, String displayName). ***");
					return "Address can not be created via createAddress(URI uri, " 
						+ "String displayName).";
				}
			}
		}
		return "Fail to createAddress(URI uri, String displayName).";
	}
	
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateApplicationSession001(SipServletRequest req) {
  	serverEntryLog();
		
		SipApplicationSession appSession = sipFactory.createApplicationSession();
		if (appSession != null) {
				logger.info("=== SipApplicationSession is created. ===");
				return null;
					
		} else {
			logger.error("*** SipApplicationSession is not created " 
					+ "by createApplicationSession(). ***");
			return "SipApplicationSession is not created by createApplicationSession().";
		}
	}
	
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateApplicationSessionByKey001(SipServletRequest req) {
  	serverEntryLog();
		
		SipApplicationSession appSession = sipFactory.createApplicationSession();
		assert appSession != null;
		if (appSession != null) {
			String appID = appSession.getId();
			assert appID != null;
			if (appID != null) {
				SipApplicationSession newAppSession = 
					sipFactory.createApplicationSessionByKey(appID);
				if (newAppSession != null) {
					logger.info("=== SipApplicationSession is created. ===");
					return null;
				}
			}
		} 
		logger.error("*** SipApplicationSession can not be created correctly" 
			+ "by createApplicationSessionByKey(String	sipApplicationKey). ***");
		return "SipApplicationSession can not be created correctly" 
			+ "by createApplicationSessionByKey(String	sipApplicationKey).";
		
		
	}
	
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateAuthInfo001(SipServletRequest req) {
  	serverEntryLog();
		
		AuthInfo auth = sipFactory.createAuthInfo();

		if (auth != null) {
				logger.info("=== AuthInfo is created. ===");
				return null;					
		} else {
			logger.error(
					"*** AuthInfo is not created by createAuthInfo(). ***");
			return "AuthInfo is not created by createAuthInfo().";
		}
	}
	
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateParameterable001(SipServletRequest req) {
  	serverEntryLog();
		Parameterable hdr = null;
		Parameterable param = null;
		try {
			hdr = req.getParameterableHeader("From");
			if (hdr != null) {
				param = sipFactory.createParameterable(hdr.toString());
			} else {
				logger.error("*** ServletParseException thrown when createParameterable() ***");
				return "Fail to create Parameterable via createParameterable().";
			}
		} catch (ServletParseException e) {
			logger.error("*** ServletParseException thrown when createParameterable() ***", e);
			return "Fail to create Parameterable via createParameterable().";
		}
		
		if (param != null && param.equals(hdr)) { 
			logger.info("=== Parameterable is created. ===");
			return null; 		
		} else {
			logger.error(
				"*** Parameterable is not created by createParameterable(). ***");
			return "Parameterable is not created by createParameterable().";
		}

	}
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateRequest001(SipServletRequest req) {
  	serverEntryLog();
  	SipApplicationSession appSession = sipFactory.createApplicationSession();
  	if (appSession != null) {
  		SipServletRequest request = null;
  		try {
  			request = 
  				sipFactory.createRequest(appSession, "MESSAGE",	req.getTo(), req.getFrom());
  			if (request != null) {
  				Address oriFrom = (Address)req.getFrom().clone();
  				Address newFrom = (Address)request.getFrom().clone();
  				oriFrom.removeParameter("tag");
  				newFrom.removeParameter("tag");
  				if (newFrom.equals(req.getTo()) 
  						&& request.getTo().equals(oriFrom)) {
  					return null;
  				} else {
  					logger.error("*** Request created createRequest() is not correct. ***");
    				return "Request created createRequest() is not correct.";
  				}
  			} else {
  				logger.error("*** Request is not created by createRequest(). ***");
  				return "Request is not created by createRequest().";
  			}
  		} catch (Exception e) {
				logger.error("*** Request is not created by createRequest(). ***");
				return "Request is not created by createRequest().";
  		}
  	}
  	logger.error("Fail to create request.");
  	return "Fail to create request via " 
  		+ "createRequest(SipApplicationSession appSession, String method, " 
  		+ "Address from, Address to)";
		
	}
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateRequest101(SipServletRequest req) {
  	serverEntryLog();
  	SipApplicationSession appSession = sipFactory.createApplicationSession();
  	if (appSession != null) {
  		try {
  			SipServletRequest request = sipFactory.createRequest(
  				appSession, 
  				"ACK", 
  				req.getTo(), 
  				req.getFrom());
  		} catch (IllegalArgumentException  e) {
				logger.info("=== Expected IllegalArgumentException thrown when createRequest(). ===");
				return null;
  		}
  	}
  	return "Fail to throw IllegalArgumentException.";	
	}
  
	
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateRequest002(SipServletRequest req) {
  	serverEntryLog();
  	SipApplicationSession appSession = sipFactory.createApplicationSession();
  	if (appSession != null) {
  		SipServletRequest request = null;
			try {
				request = sipFactory.createRequest(
					appSession, 
					"MESSAGE", 
					req.getTo().toString(), 
					req.getFrom().toString());
				if (request != null) {
					Address oriFrom = (Address)req.getFrom().clone();
  				Address newFrom = (Address)request.getFrom().clone();
  				oriFrom.removeParameter("tag");
  				newFrom.removeParameter("tag");
  				if (newFrom.toString().equals(req.getTo().toString()) 
  						&& request.getTo().toString().equals(oriFrom.toString())) {
  					return null;
  				} else {
  					logger.error("*** Request created createRequest() is not correct. ***");
    				return "Request created createRequest() is not correct.";
  				}
  			} else {
  				logger.error("*** Request is not created by createRequest(). ***");
  				return "Request is not created by createRequest().";
  			}	
			} catch (Exception e) {
				logger.error("*** Request is not created by createRequest(). ***");
				return "Request is not created by createRequest().";
			} 
  	}
  	logger.error("Fail to create request.");
  	return "Fail to create request via " 
			+ "createRequest(SipApplicationSession appSession, String method, " 
			+ "String from, String to)";
	}
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateRequest102(SipServletRequest req) {
  	serverEntryLog();
  	SipApplicationSession appSession = sipFactory.createApplicationSession();
  	if (appSession != null) {
  		SipServletRequest request = null;
			try {
				request = sipFactory.createRequest(
					appSession, 
					"MESSAGE", 
					req.getTo().toString().replace("sip:", ""), 
					req.getFrom().toString().replace("sip:", ""));
			} catch (ServletParseException e) {
				logger.info("=== Expected ServletParseException thrown when createRequest(). ===");
				return null;
			}
  	}
  	return "Fail to throw ServletParseException.";	
	}
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateRequest103(SipServletRequest req) {
  	serverEntryLog();
  	SipApplicationSession appSession = sipFactory.createApplicationSession();
  	if (appSession != null) {
  		SipServletRequest request = null;
			try {
				request = sipFactory.createRequest(
					appSession, 
					"CANCEL", 
					req.getTo().toString(), 
					req.getFrom().toString());
			} catch (IllegalArgumentException  e) {
				logger.info("=== Expected IllegalArgumentException thrown when createRequest(). ===");
				return null;
			} catch (ServletParseException e) {
				logger.error("*** Unexpected ServletParseException thrown when createRequest(). ***");
				return "Unexpected ServletParseException thrown when createRequest().";
			}
  	}
  	return "Fail to throw IllegalArgumentException .";	
	}

  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateRequest003(SipServletRequest req) {
  	serverEntryLog();
  	SipApplicationSession appSession = sipFactory.createApplicationSession();
  	if (appSession != null) {
  		SipServletRequest request = null;
  		try {
	  		request = sipFactory.createRequest(
	  			appSession, 
	  			"MESSAGE", 
	  			req.getTo().getURI(), 
	  			req.getFrom().getURI());
	  		if (request != null) {
  				if (request.getFrom().getURI().equals(req.getTo().getURI()) 
  						&& request.getTo().getURI().equals(req.getFrom().getURI())) {
  					return null;
  				} else {
  					logger.error("*** Request created createRequest() is not correct. ***");
    				return "Request created createRequest() is not correct.";
  				}
  			} else {
  				logger.error("*** Request is not created by createRequest(). ***");
  				return "Request is not created by createRequest().";
  			}	
  		} catch (IllegalArgumentException e) {
  			logger.error("*** Request is not created by createRequest(). ***");
				return "Request is not created by createRequest().";
  		}
  	}
  	logger.error("Fail to create request.");
  	return "Fail to create request via " 
			+ "createRequest(SipApplicationSession appSession, String method, " 
			+ "URI from, URI to)";
		
	}
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateRequest104(SipServletRequest req) {
  	serverEntryLog();
  	SipApplicationSession appSession = sipFactory.createApplicationSession();
  	if (appSession != null) {
  		SipServletRequest request = null;
			try {
				request = sipFactory.createRequest(
						appSession, 
						"CANCEL", 
						req.getTo().getURI(), 
						req.getFrom().getURI());
			} catch (IllegalArgumentException  e) {
				logger.info("=== Expected IllegalArgumentException thrown when createRequest(). ===");
				return null;
			}
  	}
  	return "Fail to throw IllegalArgumentException .";
  }
	
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public void testCreateRequest004(SipServletRequest req) {
  	serverEntryLog();
  	logger.info(
			"=== SipFactory.createRequest(SipServletRequest origRequest, " 
			+	"boolean sameCallId) is deprecated. ===");
	}
	
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateSipURI001(SipServletRequest req) {
  	serverEntryLog();
  	SipURI uri = sipFactory.createSipURI("test", "bea.com");
  	if (uri != null 
  			&& uri.getUser().equals("test") 
  			&& uri.getHost().equals("bea.com")) {
  		return null;
  	} else {
  		logger.error("*** SipURI can not be created by createSipURI(). ***");
  		return "SipURI can not be created by createSipURI().";
  	}
  		
	}
	
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateURI001(SipServletRequest req) {
  	serverEntryLog();
  	
  	Address cont = null;
		try {
			cont = req.getAddressHeader("From");
		} catch (ServletParseException e) {
			logger.error("*** ServletParseException thrown when getAddressHeader() ***", e);
			return "Fail to create URI by createURI().";
		}
		
		if (cont != null) {
			URI orig = cont.getURI();
			if (orig != null) {
				URI uri = null;
				try {
					uri = sipFactory.createURI(orig.toString());
				} catch (ServletParseException e) {
					logger.error("*** URI can not be created by createURI(). ***");
		  		return "URI can not be created by createURI().";
				}
		  	if (uri != null && uri.equals(orig)) {
		  		return null;
		  	} else {
		  		logger.error("*** URI can not be created by createURI(). ***");
		  		return "URI can not be created by createURI().";
		  	}
			}
		}
		return "Fail to create URI by createURI().";
	}
  
  @TestStrategy(strategy = TESTSTRATEGY_SIMPLEASSERT)
	public String testCreateURI101(SipServletRequest req) {
  	serverEntryLog();
  	
  	String orig = "alice@10.130.8.107";
 
		try {
			URI uri = sipFactory.createURI(orig);
		} catch (ServletParseException e) {
			logger.info("=== Expected ServletParseException thrown when createURI(). ===");
			return null;
		} 	
		return "Fail to throw ServletParseException.";
	}
	
	
	
}
