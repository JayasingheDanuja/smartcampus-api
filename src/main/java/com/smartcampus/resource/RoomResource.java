package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
 * handles all /rooms API operations (GET, POST, DELETE)
*/
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/rooms return every room currently in the system
    @GET
    public Response getAllRooms() {
        Collection<Room> roomList = store.rooms.values();
        return Response.ok(new ArrayList<>(roomList)).build();
    }

    // POST /api/v1/rooms register a new room
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Room 'id' field is required."))
                    .build();
        }

        if (store.rooms.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorBody("A room with id '" + room.getId() + "' already exists."))
                    .build();
        }

        // ensure sensorIds list is never null
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        store.rooms.put(room.getId(), room);

        return Response.status(Response.Status.CREATED)
                .entity(room)
                .build();
    }

    // GET /api/v1/rooms/{roomId} 
    //get room by ID
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.rooms.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Room '" + roomId + "' not found."))
                    .build();
        }

        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId} 
    //delete a room,only if no sensors
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.rooms.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Room '" + roomId + "' does not exist or was already removed."))
                    .build();
        }

        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted because it still has " +
                    room.getSensorIds().size() + " active sensor(s). " +
                    "Decommission all sensors first."
            );
        }

        store.rooms.remove(roomId);

        return Response.ok(successBody("Room '" + roomId + "' successfully deleted.")).build();
    }

    //Helper for error response
    private java.util.Map<String, String> errorBody(String message) {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("status", "error");
        body.put("message", message);
        return body;
    }
    //helper for success response.
    private java.util.Map<String, String> successBody(String message) {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("status", "success");
        body.put("message", message);
        return body;
    }
}
