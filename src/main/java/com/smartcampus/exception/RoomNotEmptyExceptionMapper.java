package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/*
 * This converts RoomNotEmptyException into HTTP 409 Conflict response
 * it is used when trying to delete a room that still has sensors
 */

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", 409);
        errorBody.put("error", "Conflict");
        errorBody.put("message", ex.getMessage());

        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorBody)
                .build();
    }
}
