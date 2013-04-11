/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * @version 1.0
 * @created 01-April-2008 17:01:42
 */

package com.bea.sipservlet.tck.ar;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipTargetedRequestInfo;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TckApplicationRouter implements SipApplicationRouter {

	/**
	* Mapping between application session id and test case handler.
	* Key: request uri user part. e.g. sipservlet.spec.approuter.case01
	*/
  private final Map<String, TckApplicationRouterCaseHandler> handlerMap;
  private boolean applicationDeployCalled = false;

  private SipApplicationRouterInfo getEmptyRouterInfo() {
    return new SipApplicationRouterInfo(null, null, null, null, null, null);
  }

  public TckApplicationRouter(){
		handlerMap = new HashMap<String, TckApplicationRouterCaseHandler>();
    handlerMap.put("sipservlet.spec.approuter.case01", new TckApplicationRouterCaseHandler01(this));
    handlerMap.put("sipservlet.spec.approuter.case02", new TckApplicationRouterCaseHandler02(this));
    handlerMap.put("sipservlet.spec.approuter.case03", new TckApplicationRouterCaseHandler03(this));
    handlerMap.put("sipservlet.spec.approuter.case04", new TckApplicationRouterCaseHandler04(this));
    handlerMap.put("sipservlet.spec.approuter.case05", new TckApplicationRouterCaseHandler05(this));
    handlerMap.put("api-test", new TckApplicationRouterApiCaseHandler(this));
  }

  public boolean isApplicationDeployCalled() {
    return this.applicationDeployCalled;
  }

  public SipApplicationRouterInfo getNextApplication(
      SipServletRequest initialRequest,
      SipApplicationRoutingRegion region,
      SipApplicationRoutingDirective directive,
      SipTargetedRequestInfo targetedRequestInfo,
      Serializable stateInfo){

    if (initialRequest == null)
      throw new NullPointerException();

    if (!initialRequest.isInitial())
      throw new IllegalStateException("Request is not initial.");

    try {
      TckApplicationRouterCaseHandler handler = getHandler(initialRequest);
      return handler.handleRouterEnquery(initialRequest, region, directive, stateInfo);
    } catch (TckApplicationRouterException e) {

      //can't use log4j in appRouter
      //System.out.println("[TCK-AppRouter]: Enquery TCK-AR failed. " +  e.getMessage());
      return getEmptyRouterInfo();
    }
  }

	private TckApplicationRouterCaseHandler getHandler(SipServletRequest request)
    throws TckApplicationRouterException {

    if (request.getHeader("Application-Name") != null)
      return handlerMap.get("api-test");

    if (!request.getRequestURI().isSipURI())
      throw new TckApplicationRouterException("Illegal request URI.");

    SipURI sipURI = (SipURI)request.getRequestURI();

    /*
    if (!(sipURI.getHost().contains(TCK_AR_CASE_DOMAIN_NAME)))
      throw new TckApplicationRouterException("Not a TCK-AR case.");
    */

    String caseUser = sipURI.getUser();
    if (caseUser == null)
      throw new TckApplicationRouterException("Not a TCK-AR case.");

    if (!handlerMap.containsKey(caseUser))
      throw new TckApplicationRouterException("Unknown TCK-AR case in request URI");
    else
      return handlerMap.get(caseUser);
  }
	

	/**
	 * Container notifies application router that new applications are deployed.
	 * 
	 * @param newlyDeployedApplicationNames    A list of names of the newly added
	 * applications
	 */
	public void applicationDeployed(List<String> newlyDeployedApplicationNames){
    applicationDeployCalled = true;
  }

	/**
	 * Container notifies application router that some applications are undeployed.
	 * 
	 * @param undeployedApplicationNames    A list of names of the undeployed
	 * applications
	 */
	public void applicationUndeployed(List<String> undeployedApplicationNames){

	}

	/**
	 * Container calls this method when it finishes using this application router.
	 */
	public void destroy(){

	}

	
	
	/**
	 * Initializes the SipApplicationRouter. This method is called by the SIP
	 * container and it can only be invoked once. 
	 */
	public void init(){

	}

	/**
	 * Initializes the SipApplicationRouter and passes in initialization properties.
	 * This method is called by the SIP container. The way in which the container
	 * obtains the properties is implementation-dependent. <br/> This method can only
	 * be invoked once.
	 * 
	 * @param properties    AR initialization properties
	 */
	public void init(Properties properties){

	}

}