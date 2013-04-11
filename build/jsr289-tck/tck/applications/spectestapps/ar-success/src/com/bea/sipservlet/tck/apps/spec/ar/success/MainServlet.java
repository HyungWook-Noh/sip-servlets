
/**
 * @author Copyright (c) 2008 by BEA Systems, Inc. All Rights Reserved.
 * @version 1.0
 * @created 2008-4-9 15:59:45
 *
 *     UA          SipServer
        |               |
        |               |
        |(1): INVITE    |
        |-------------->|
        |(2): 200 OK    |
        |<--------------|
        |(3): ACK       |
        |-------------->|
        |(4): BYE       |
        |-------------->|
        |(5): 200 OK    |
        |<--------------|
        |               |
        |               |

 */
package com.bea.sipservlet.tck.apps.spec.ar.success;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import java.io.IOException;

@javax.servlet.sip.annotation.SipServlet(
    name = "JSR289.TCK.AppRouter.SuccessServlet",
    loadOnStartup = 1
)
public class MainServlet extends SipServlet {
  private static final Logger logger = Logger.getLogger(MainServlet.class);

  protected void doInvite(SipServletRequest req)
      throws ServletException, IOException     {

    if (req.isInitial()) {
      logger.info("=== [TCK-AR-Success] --INVITE--> ===");
      req.createResponse(200).send();
      logger.info("=== [TCK-AR-Success] <--200-- ===");
    }
  }

  protected void doAck(SipServletRequest req)
      throws ServletException, IOException  {
  }

  protected void doBye(SipServletRequest req)
      throws ServletException, IOException   {

    logger.info("=== [TCK-AR-Success] --BYE--> ===");
    req.createResponse(200).send();
    logger.info("=== [TCK-AR-Success] <--200-- ===");
  }
}
