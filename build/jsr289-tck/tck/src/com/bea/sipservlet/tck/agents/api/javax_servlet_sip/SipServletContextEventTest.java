/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. All rights
 * reserved.
 * 
 * SipServletContextEventTest is used to test the APIs of
 * javax.servlet.sip.SipServletContextEvent
 */

package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;


public class SipServletContextEventTest extends TestBase {

    public SipServletContextEventTest(String arg0) throws IOException {
        super(arg0);
    }

    @AssertionIds(ids = { "SipServlet:JAVADOC:SipServletContextEvent1" },
            desc = "SipServletContextEvent construction method.")
    public void testSipServletContextEvent001() {
        assertSipMessage();
    }
}
