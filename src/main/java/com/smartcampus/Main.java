package com.smartcampus;

import com.smartcampus.filter.ApiLoggingFilter;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import com.smartcampus.exception.GlobalExceptionMapper;
import com.smartcampus.exception.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.exception.RoomNotEmptyExceptionMapper;
import com.smartcampus.exception.SensorUnavailableExceptionMapper;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.servlets.DefaultServlet;

import java.io.File;
import java.util.logging.Logger;

/*
 * app's entry point.
 * Main class that starts the embedded Tomcat server
 * runs the Smart Campus REST API without external Tomcat setup.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final int    PORT         = 8080;
    private static final String CONTEXT_PATH = "/smart-campus-api";

    public static void main(String[] args) throws Exception {

        // Register every JAX-RS resource, provider and filter explicitly.
        // Using ResourceConfig directly is more reliable than classpath scanning
        // when running inside an embedded (non deployed) Tomcat instance
        
        ResourceConfig config = new ResourceConfig();
        config.register(DiscoveryResource.class);
        config.register(RoomResource.class);
        config.register(SensorResource.class);
        config.register(ApiLoggingFilter.class);
        config.register(GlobalExceptionMapper.class);
        config.register(LinkedResourceNotFoundExceptionMapper.class);
        config.register(RoomNotEmptyExceptionMapper.class);
        config.register(SensorUnavailableExceptionMapper.class);
        config.register(org.glassfish.jersey.jackson.JacksonFeature.class);

        // Set up embedded Tomcat on port 8080
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.setBaseDir(System.getProperty("java.io.tmpdir"));

        //create empty servlet context (no web.xml needed)
        Context ctx = tomcat.addContext(CONTEXT_PATH, new File(".").getAbsolutePath());

        // Tomcat requires a DefaultServlet to handle static resources and initialise correctly
        Wrapper defaultServlet = Tomcat.addServlet(ctx, "default", new DefaultServlet());
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        ctx.addServletMappingDecoded("/", "default");

        // Wire the Jersey servlet using our ResourceConfig (all /api/v1/* traffic goes here)
        ServletContainer jerseyContainer = new ServletContainer(config);
        Wrapper jerseyServlet = Tomcat.addServlet(ctx, "jersey", jerseyContainer);
        jerseyServlet.setLoadOnStartup(2);
        ctx.addServletMappingDecoded("/api/v1/*", "jersey");

        tomcat.getConnector();
        tomcat.start();

        LOGGER.info("=======================================================");
        LOGGER.info("  Smart Campus API started successfully");
        LOGGER.info("  Discovery: http://localhost:" + PORT + CONTEXT_PATH + "/api/v1/");
        LOGGER.info("  Rooms    : http://localhost:" + PORT + CONTEXT_PATH + "/api/v1/rooms");
        LOGGER.info("  Sensors  : http://localhost:" + PORT + CONTEXT_PATH + "/api/v1/sensors");
        LOGGER.info("=======================================================");

        // block main thread (keeps the server alive until stopped in NetBeans)
        tomcat.getServer().await();
    }
}