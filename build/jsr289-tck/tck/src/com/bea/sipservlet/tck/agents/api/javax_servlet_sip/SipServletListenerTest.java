/** 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved.
 * 
 * SipServletListenerTest is used to test the APIs of 
 * javax.servlet.sip.SipServletListener
 */

package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class SipServletListenerTest extends TestBase {

    public SipServletListenerTest(String arg0) throws IOException {
        super(arg0);
    }

    @AssertionIds(ids = { "SipServlet:JAVADOC:SipServletListener1",
        "SipServlet:JAVADOC:SipServletContextEvent2" }, 
            desc = "test container notify servlet initialized ")
    public void testServletInitialized001() {
        assertSipMessage();
    }
}
