package com.smartcampus.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * this class handles all unexpected errors in the app
 *
 * It prevents showing full error details to users.
 * instead, it returns a simple and safe error message.
 *
 * If the error is already a WebApplicationException(just like 404),
 * it will return that response as it is
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // allow normal JAX-RS errors(like 404)to pass through
        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse();
        }

        // log full error details on the server
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by global mapper", ex);

        //send simple error response to client
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", 500);
        errorBody.put("error", "Internal Server Error");
        errorBody.put("message", "An unexpected error occurred. Please contact the system administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorBody)
                .build();
    }
}
