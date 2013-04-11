/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * CreatingBranchNoParallelServlet is used to test the spec of proxy branch
 *
 */

package com.bea.sipservlet.tck.apps.spec.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.URI;
import javax.servlet.sip.Address;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;
import com.bea.sipservlet.tck.common.TestConstants;



@javax.servlet.sip.annotation.SipServlet(name="CreatingBranchNoParallel")
public class CreatingBranchNoParallelServlet extends BaseServlet{
  private static Logger logger = Logger.getLogger(CreatingBranchNoParallelServlet.class);
  private URI ua2;
  private URI ua3;
  private Proxy p;
  protected void doInvite(SipServletRequest req)
      throws ServletException, IOException {
    serverEntryLog();
    logger.info("===[doInvite]: " + req.getRequestURI() + "===");
    // Proxy application perform the following operations:
    //
    // * Call SipServletRequest.getProxy() and save the reference.
    // * Proxy.createProxyBranches(uas1&uas2); // "uas1&uas2" means the list
    // that contians URIs refering to UAS1 and UAS2.
    // * Proxy.setParallel(false);
    // * Retrieve the two proxy branches via Proxy.getProxyBranches() and
    // Proxy.getProxyBranch(uri_uas1) and Proxy.getProxyBranch(uri_uas2) and
    // assert equality among the results
    // * Invoke Proxy.startProxy() to start the proxying process.
    p = req.getProxy();
    p.setRecordRoute(true);
    p.setSupervised(true);
    p.setParallel(false);

    ua2 = ((Address) req.getAddressHeader("TARGET1")).getURI();
    ua3 = ((Address) req.getAddressHeader("TARGET2")).getURI();
    List<URI> uriList = new ArrayList<URI>(2);
    uriList.add(ua2);
    uriList.add(ua3);
    logger.debug(uriList);
    p.createProxyBranches(uriList);

    List<ProxyBranch> branches = p.getProxyBranches();
    ProxyBranch ua2Branch = p.getProxyBranch(ua2);
    ProxyBranch ua3Branch = p.getProxyBranch(ua3);
    System.out.println(branches.toArray().getClass());
    Object[] branches1 = branches.toArray();
    ProxyBranch[] branches2 = new ProxyBranch[]{ua2Branch,ua3Branch};
    Arrays.sort(branches1,new BranchComparator());
    Arrays.sort(branches2,new BranchComparator());
    if (!isSameProxyBranch((ProxyBranch)branches1[0],branches2[0])) {
      ua2Branch.getRequest().addHeader(
              TestConstants.TEST_FAIL_REASON,
              "ProxyBranch which receives via getProxyBranches() is unequal to " +
              "proxy branch which recieves via getProxyBranch(URI uri).");
      logger.error("*** ProxyBranch which receives via getProxyBranches() is unequal to " +
          		"proxy branch which recieves via getProxyBranch(URI uri). ***");
    }
    if(!isSameProxyBranch((ProxyBranch)branches1[1], branches2[1])){
      ua3Branch.getRequest().addHeader(
          TestConstants.TEST_FAIL_REASON,
          "ProxyBranch which receives via getProxyBranches() is unequal to " +
          "proxy branch which recieves via getProxyBranch(URI uri).");
      logger.error("*** ProxyBranch which receives via getProxyBranches() is unequal to " +
          "proxy branch which recieves via getProxyBranch(URI uri). ***");
    }
    p.startProxy();   
  }
  protected void doProvisionalResponse(SipServletResponse res)
  throws ServletException, IOException {
    // When notified of the provisional response, Proxy application performs the
    // following operations:
    // * Check the boolean value of ProxyBranch.isStarted() on the two branches
    // retrieved in the above steps and do the assertions ( one should be true
    // and the other one should be false)
    if(res.getRequest().getRequestURI().equals(ua2)){
      ProxyBranch ua2Branch = res.getProxy().getProxyBranch(ua2);
      ProxyBranch ua3Branch = res.getProxy().getProxyBranch(ua3);
      if(!ua2Branch.isStarted()){
        res.addHeader(
            TestConstants.TEST_FAIL_REASON,
            "The ua2 proxy branch status is incorrectness.");
        logger.error("*** The ua2 proxy branch status is incorrectness.***");
      }else if(ua3Branch.isStarted()){
        res.addHeader(
            TestConstants.TEST_FAIL_REASON,
            "The ua3 proxy branch status is incorrectness.");
        logger.error("*** The ua3 proxy branch status is incorrectness.***");
      }
    }
  }
  
  protected void doSuccessResponse(SipServletResponse res)
  throws ServletException, IOException {
    // On the branch to UAS2, Call ProxyBranch.getProxy() and do assertion of
    // equality between the returned value and the one retrieved in the above
    // step 2.
    // On the branch to UAS2, call ProxyBranch.getResponse() and it should
    // return the 400 response.
    // On the branch to UAS3, Call ProxyBranch.getResponse() and it should
    // return the 200 response.
      ProxyBranch ua2Branch = res.getProxy().getProxyBranch(ua2);
      if(!isSameProxy(ua2Branch.getProxy(), p)){
        res.addHeader(
            TestConstants.TEST_FAIL_REASON,
            "The ua2 proxy branch's proxy object is unequal"
            +" to original request's proxy object");
        logger.error("*** The ua2 proxy branch's proxy object is unequal"
            +" to original request's proxy object.***");
        return;
      }
      if(ua2Branch.getResponse().getStatus() != SipServletResponse.SC_BAD_REQUEST){
        res.addHeader(TestConstants.TEST_FAIL_REASON,
          "The ua2 proxy branch's status is not "
              + SipServletResponse.SC_BAD_REQUEST);
        logger.error("*** The ua2 proxy branch response status is not "
          + SipServletResponse.SC_BAD_REQUEST + "***");
        return;
      }        
      ProxyBranch ua3Branch = res.getProxy().getProxyBranch(ua3);      
      if(ua3Branch.getResponse().getStatus() != SipServletResponse.SC_OK){
        res.addHeader(
            TestConstants.TEST_FAIL_REASON,
            "The ua3 proxy branch's status is not "+SipServletResponse.SC_OK);
        logger.error("*** The ua3 proxy branch response status is not "
          + SipServletResponse.SC_OK + "***");
        return;
      }  
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
  private boolean isSameProxyBranch(ProxyBranch branch1, ProxyBranch branch2){
    URI uri1 = branch1.getRequest().getRequestURI();
    URI uri2 = branch2.getRequest().getRequestURI();
    return uri1.equals(uri2);

  }
  
  private class BranchComparator implements Comparator{

    public int compare(Object arg1, Object arg2) {
      ProxyBranch branch1 = (ProxyBranch)arg1;
      ProxyBranch branch2 = (ProxyBranch)arg2;
      URI uri1 = branch1.getRequest().getRequestURI();
      URI uri2 = branch2.getRequest().getRequestURI();
      return uri1.toString().compareTo(uri2.toString());
    }
    
  }
}
