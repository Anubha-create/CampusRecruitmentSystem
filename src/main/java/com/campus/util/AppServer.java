package com.campus.util;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

/**
 * Embedded Apache Tomcat Application Server Launcher.
 * Allows running the web application instantly via `mvn exec:java` or IDE execution.
 */
public class AppServer {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String portStr = System.getenv("PORT");
        if (portStr != null && !portStr.isEmpty()) {
            port = Integer.parseInt(portStr);
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // Initialize default HTTP connector

        // Set docBase to webapp directory
        String webappDirLocation = "src/main/webapp";
        File webappDir = new File(webappDirLocation);
        if (!webappDir.exists()) {
            webappDirLocation = ".";
        }

        Context ctx = tomcat.addWebapp("", new File(webappDirLocation).getAbsolutePath());
        System.out.println("[AppServer] Starting Apache Tomcat on http://localhost:" + port);
        System.out.println("[AppServer] Configuring web application directory: " + ctx.getDocBase());

        tomcat.start();
        System.out.println("[AppServer] Server started successfully! Open http://localhost:" + port + " in your web browser.");
        tomcat.getServer().await();
    }
}
