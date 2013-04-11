/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * MessageTestContent used in TCK spec test
 */
package com.bea.sipservlet.tck.common;

public class MessageTestContent {
  public final static String contentStr = "This is a test";
  public final static byte[] gifImageSample = new byte[]{
      71,73,70,56,57,97,4,0,7,0,-128,0,0,-1,-3,-16,
      17,48,-81,33,-7,4,0,0,0,0,0,44,0,0,0,0,4,0,7,
      0,0,2,8,12,30,6,105,-20,-85,20,40,0,59
  };
  public final static String base64OfGif = "R0lGODlhBAAHAIAAAP/"+
         "98BEwryH5BAAAAAAALAAAAAAEAAcAAAIIDB4GaeyrFCgAOw==";

  public final static String firtPartContent = "This is a text";
  
  public final static String multipartSample =
      "Date: Fri, 09 Jul 2004 12:51:17 -0500\r\n" +
          "From: Richard@Monson-Haefel.com\r\n" +
          "Reply-To: Richard@Monson-Haefel.com\r\n" +
          "MIME-Version: 1.0\r\n" +
          "To: Buffy.Summers@upn.com\r\n" +
          "Subject: The book files\r\n" +
          "Content-Type: multipart/mixed; " +
          " boundary=------------56C4BEFA835541B020058DF8\r\n\r\n" +


          "This is a multi-part message in MIME format.\r\n" +
          "--------------56C4BEFA835541B020058DF8\r\n" +
          "Content-Type: text/plain; charset=us-ascii\r\n" +
          "Content-Transfer-Encoding: 7bit\r\n\r\n"+
          firtPartContent + "\r\n" +
          "--------------56C4BEFA835541B020058DF8\r\n" +
          "Content-Type: image/gif;\r\n" +
          " name=\"dot.gif\"\r\n" +
          "Content-Transfer-Encoding: base64\r\n" +
          "Content-Disposition: inline;\r\n" +
          " filename=\"dot.gif\"\r\n\r\n" +
          base64OfGif + "\r\n\r\n" +
          "--------------56C4BEFA835541B020058DF8--";
  
  public final static String sdpStr =
      "v=0\r\n" +
          "o=collins 123456 001 IN IP4 station1.work.com\r\n" +
          "s=vacation\r\n" +
          "i=Discussion about time off work\r\n" +
          "c=IN IP4 station1.work.com\r\n" +
          "t=0 0\r\n" +
          "m=audio 4444 RTP/AVP 2 4 15\r\n" +
          "a=rtpmap 2 G726/8000\r\n" +
          "a=rtpmap 4 G723/8000\r\n" +
          "a=rtpmap 15 G728/8000";
}
