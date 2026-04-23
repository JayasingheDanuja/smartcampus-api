package com.smartcampus.application;

import javax.ws.rs.core.Application;

/*
 * JAX-RS App class
 *
 * Kept as part of the standard JAX-RS project structure and Resource and provider
 * registration is handled programmatically in Main.java via ResourceConfig,
 * which gives us full control over what Jersey loads when running on
 * embedded Tomcat no annotation scanning conflicts.
 */
public class SmartCampusApplication extends Application {
    // Registration handled in Main.java via ResourceConfig
}