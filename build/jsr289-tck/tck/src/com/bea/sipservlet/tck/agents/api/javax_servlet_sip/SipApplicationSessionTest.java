/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.  
 *  All rights reserved.
 *  
 * SipApplicationSessionTest is used to test the APIs of
 * javax.servlet.sip.SipApplicationSession.
 */
package com.bea.sipservlet.tck.agents.api.javax_servlet_sip;

import java.io.IOException;

import com.bea.sipservlet.tck.agents.TestBase;
import com.bea.wcp.ant.ext.annotations.AssertionIds;

public class SipApplicationSessionTest extends TestBase{
    
  public SipApplicationSessionTest(String arg0) throws IOException {
    super(arg0);
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession1" }, 
      desc = "Encodes the ID of this SipApplicationSession into the specified "
      + "URI,Deprecated since V1.1.")
  public void testEncodeURI001() {
    // This API has been deprecated since V1.1, do not need test.
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession2" }, 
      desc = "Encode specified URL to include the application session ID in a "
      + "way such that the parameter used to encode the application session "
      + "ID should be unique across implementations.")
  public void testEncodeURL001(){
    assertSipMessage();
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession2" }, 
      desc = "Throw IllegalStateException if this application session is not "
      + "valid.")
  public void testEncodeURL101(){
    assertSipMessage();
  } 
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession3" }, 
      desc = "Return the application name.")
  public void testGetApplicationName001(){
    assertSipMessage();
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession4",
                        "SipServlet:JAVADOC:SipApplicationSession18"}, 
      desc = "Return the object bound with the specified name "
        + "in this session.")
  public void testGetAttribute001(){
    assertSipMessage(); 
  }
    
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession4",
                        "SipServlet:JAVADOC:SipApplicationSession18"}, 
      desc = "Return null if no object is bound under the name.")
  public void testGetAttribute002(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession4",
                        "SipServlet:JAVADOC:SipApplicationSession18" }, 
      desc = "Throw IllegalStateException,if the appSession is not valid.")
  public void testGetAttribute101(){
    assertSipMessage();   
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession5" }, 
      desc = "Return an Iterator over the String objects containing "
        + "the names of all the objects bound to this session.")
  public void testGetAttributeNames001(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession5" }, 
      desc = "Throw IllegalStateException, if the appSession is not valid.")
  public void testGetAttributeNames101(){
    assertSipMessage();
  }
     
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession6" }, 
      desc = "Return the creation time of appSession.")
  public void testGetCreationTime001(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession6" }, 
      desc = "Throw IllegalStateException,if the appSession is not valid.")
  public void testGetCreationTime101(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession7" }, 
      desc = "Returns the expiration time of appSession.")
  public void testGetExpirationTime001(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession7" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testGetExpirationTime101(){
    assertSipMessage();
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession8" }, 
      desc = "Return the appSession Id.")
  public void testGetId001(){    
    assertSipMessage();
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession9" }, 
      desc = "Return last accessed time.")
  public void testGetLastAccessedTime001(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession10" }, 
      desc = "Return an Iterator over all valid \"protocol\" "
        + "sessions associated with this application session.")
  public void testGetSessions001(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession10" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testGetSessions101(){
    assertSipMessage();
  }
      
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession11" }, 
      desc = "Return an Iterator over all valid \"protocol\" session "
        + "objects associated with the specified protocol associated "
        + "with this application session. If the specified protocol "
        + "is not supported, an empty Iterator is returned.")
  public void testGetSessions002(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession11" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testGetSessions102(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession11" }, 
      desc = "Throw IllegalArgumentException if the protocol is not understood "
      + "by container.")
  public void testGetSessions103(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession12" }, 
      desc = "Return null if not found.")
  public void testGetSipSession001(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession12" }, 
      desc = "Return the SipSession with the specified id belonging to "
      + "this application session.")
  public void testGetSipSession002(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession12" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testGetSipSession101(){
    assertSipMessage();
  }
  
  
  @AssertionIds(ids={"SipServlet:JAVADOC:SipApplicationSession13"},
          desc="Return the active timer identified by a specific id " 
            + "that is associated with this application session.")
  public void testGetTimer001(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession13" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testGetTimer101(){
    assertSipMessage();
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession14" }, 
      desc = "Return all active timers associated with the appSession.")
  public void testGetTimers001(){
    assertSipMessage();
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession14" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testGetTimers101(){
    assertSipMessage();
  }  

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession15" }, 
      desc = "Invalidates this application session.")
  public void testInvalidate001(){    
    assertSipMessage();
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession15" }, 
      desc = "Invalidates this application session which "
        + "will unbinds any objects bound to it.")
  public void testInvalidate002(){    
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession15" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testInvalidate101(){
    assertSipMessage();
  }  
    
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession16" }, 
      desc = "Return true, since the appSession is valid.")
  public void testIsValid001(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession17" }, 
      desc = "Remove the object bound with the specified name from "
      + "this session.")
  public void testRemoveAttribute001(){
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession17" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testRemoveAttribute101(){
    assertSipMessage();
  }

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession18" }, 
      desc = "Bind an object to this session, using the name specified.")
  public void testSetAttribute001(){
    assertSipMessage();
  }  

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession18" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testSetAttribute101(){
    assertSipMessage();
  } 

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession18" }, 
      desc = "Throw NullPointerException if the name is null.")
  public void testSetAttribute102(){
    assertSipMessage();
  } 
 
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession18" }, 
      desc = "Throw java.lang.NullPointerException if the attribute is null.")
  public void testSetAttribute103(){
    assertSipMessage();
  } 
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession19" }, 
      desc = "Return deltaMinutes, if deltaMinutes is not 0 or less.")
  public void testSetExpires001(){
    assertSipMessage();
  }    

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession19" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testSetExpires101(){
    assertSipMessage();
  }     
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession20" }, 
      desc = "Return null if not found.")
  public void testGetSession001(){
    assertSipMessage();
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession20" }, 
      desc = "Return the session object with the specified id associated with " 
        + "the specified protocol belonging to this application session.")
  public void testGetSession002(){
    assertSipMessage();
  } 

  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession20" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testGetSession101() {
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession21" }, 
      desc = "Return true if this application session is in a "
      + "ready-to-invalidate state meet the following conditions."
      + "(1)All the contained SipSessions within the SipApplicationSession "
      + "    are in the ready-to-invalidate state. "
      + "(2)None of the ServletTimers associated with the SipApplicationSession"
      + "   are active.")
  public void testIsReadyToInvalidate001() {
    assertSipMessage();
  }  
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession21" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testIsReadyToInvalidate101() {
    assertSipMessage();
  } 
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession22",
                        "SipServlet:JAVADOC:SipApplicationSession23"}, 
      desc = "Specify whether the container should notify the application when " 
        + "the SipApplicationSession is in the ready-to-invalidate state." 
        + "The container notifies the application using the " 
        + "SipApplicationSessionListener.sessionReadyToInvalidate callback.")
  public void testSetGetInvalidateWhenReady001() {
    assertSipMessage();
  } 
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession22" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testSetInvalidateWhenReady101() {
    assertSipMessage();
  }
  
  @AssertionIds(ids = { "SipServlet:JAVADOC:SipApplicationSession23" }, 
      desc = "Throw IllegalStateException if the appSession is not valid.")
  public void testGetInvalidateWhenReady101() {
    assertSipMessage();
  }
  
}
