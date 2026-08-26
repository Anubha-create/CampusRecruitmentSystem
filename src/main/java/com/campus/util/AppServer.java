package com.campus.util;

import com.campus.servlet.*;
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
        
        // Register Servlets programmatically for guaranteed embedded execution
        Tomcat.addServlet(ctx, "LoginServlet", new LoginServlet());
        ctx.addServletMappingDecoded("/api/login", "LoginServlet");

        Tomcat.addServlet(ctx, "RegisterServlet", new RegisterServlet());
        ctx.addServletMappingDecoded("/api/register", "RegisterServlet");

        Tomcat.addServlet(ctx, "LogoutServlet", new LogoutServlet());
        ctx.addServletMappingDecoded("/api/logout", "LogoutServlet");

        Tomcat.addServlet(ctx, "StudentServlet", new StudentServlet());
        ctx.addServletMappingDecoded("/api/student", "StudentServlet");

        Tomcat.addServlet(ctx, "CompanyServlet", new CompanyServlet());
        ctx.addServletMappingDecoded("/api/company", "CompanyServlet");

        Tomcat.addServlet(ctx, "JobDriveServlet", new JobDriveServlet());
        ctx.addServletMappingDecoded("/api/job-drive", "JobDriveServlet");

        Tomcat.addServlet(ctx, "ApplicationServlet", new ApplicationServlet());
        ctx.addServletMappingDecoded("/api/application", "ApplicationServlet");

        Tomcat.addServlet(ctx, "AdminServlet", new AdminServlet());
        ctx.addServletMappingDecoded("/api/admin", "AdminServlet");

        System.out.println("[AppServer] Starting Apache Tomcat on http://localhost:" + port);
        System.out.println("[AppServer] Configuring web application directory: " + ctx.getDocBase());

        tomcat.start();
        System.out.println("[AppServer] Server started successfully! Open http://localhost:" + port + " in your web browser.");
        tomcat.getServer().await();
    }
}
