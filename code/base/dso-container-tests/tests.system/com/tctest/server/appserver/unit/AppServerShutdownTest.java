/*
 * All content copyright (c) 2003-2006 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tctest.server.appserver.unit;

import org.apache.commons.httpclient.HttpClient;

import com.tc.test.ProcessInfo;
import com.tc.test.server.Server;
import com.tc.test.server.appserver.unit.AbstractAppServerTestCase;
import com.tc.test.server.util.HttpUtil;
import com.tc.util.Assert;
import com.tc.util.runtime.Os;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Test to make sure the app server shutdown normally with DSO
 */
public class AppServerShutdownTest extends AbstractAppServerTestCase {

  public AppServerShutdownTest() {
    // this.disableAllUntil("2007-04-08");
  }

  public final void testShutdown() throws Exception {

    startDsoServer();

    HttpClient client = HttpUtil.createHttpClient();
    int port1 = startAppServer(true).serverPort();
    int port2 = startAppServer(true).serverPort();

    URL url1 = createUrl(port1, ShutdownNormallyServlet.class, "cmd=insert");
    assertEquals("cmd=insert", "OK", HttpUtil.getResponseBody(url1, client));

    URL url2 = createUrl(port2, ShutdownNormallyServlet.class, "cmd=query");
    assertEquals("cmd=query", "OK", HttpUtil.getResponseBody(url2, client));

    String processes_before = ProcessInfo.ps_grep_java();
    
    System.out.println("Shut down app server normally...");
    for (Iterator iter = appservers.iterator(); iter.hasNext();) {
      Server server = (Server) iter.next();
      server.stop();
    }
    System.out.println("Shutting down completed.");

    // sanity check that all app servers are indeed shutdown
    try {
      url1 = createUrl(port1, ShutdownNormallyServlet.class, "cmd=insert");
      HttpUtil.getResponseBody(url1, client);
      fail("App server 1 is supposed to be shutdown!");      
    } catch (IOException e) {
      // expected
      System.out.println("Expected exception 1: " + e.getMessage());
    }
    
    try {
      url2 = createUrl(port2, ShutdownNormallyServlet.class, "cmd=query");
      HttpUtil.getResponseBody(url2, client);
      fail("App server 2 is supposed to be shutdown!");
    } catch (IOException e) {
      // expected
      System.out.println("Expected exception 2: " + e.getMessage());
    }
    
    
    // There could be 2 kinds of failures:
    // 1. Cargo didn't shutdown the appserver normally
    // 2. DSO didn't allow the appserver to shutdown -- We want to catch this
    // Thread.sleep(5 * 1000);
    System.out.println("Checking to see if any app server is still alive...");
    String processes_after = ProcessInfo.ps_grep_java();
    System.out.println("Java processes found: " + processes_after);
    
    if (Os.isLinux()) { 
      // Linux limits the classpath to 4K so we can't rely on cmd line arguments
      String[] count_before = processes_before.split("\\n");
      String[] count_after  = processes_after.split("\\n");      
      assertTrue("App server didn't shutdown", (count_after.length + 2 == count_before.length));
    } else {
      assertFalse("App server didn't shutdown", processes_after.indexOf("CargoLinkedChildProcess") > 0);
    }
    
  }

  public static final class ShutdownNormallyServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      HttpSession session = request.getSession(true);
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();

      String cmdParam = request.getParameter("cmd");
      if ("insert".equals(cmdParam)) {
        session.setAttribute("hung", "daman");
        out.println("OK");
      } else if ("query".equals(cmdParam)) {
        String data = (String) session.getAttribute("hung");
        if (data.equals("daman")) {
          out.println("OK");
        } else {
          out.println("ERROR: " + data);
        }
      } else {
        out.println("unknown cmd");
      }

    }
  }
}
