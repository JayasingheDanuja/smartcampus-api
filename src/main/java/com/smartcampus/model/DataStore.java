package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Simple in memory data store for the whole app
 * It keeps data alive between API requests
 * Uses ConcurrentHashMap for safe access when multiple requests happenning
 */

public class DataStore {

    //Singleton instance
    private static final DataStore INSTANCE = new DataStore();

    public static DataStore getInstance() {
        return INSTANCE;
    }

    private DataStore() {
        seedData();
    }

    //Stores all rooms
    public final Map<String, Room> rooms = new ConcurrentHashMap<>();

    //Stores all sensors
    public final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Reading history keyed by sensor ID -> list of readings
    public final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // Sample data for testing
    private void seedData() {
        //seed rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-102", "Computer Science Lab", 30);
        Room r3 = new Room("HALL-A1", "Lecture Hall A", 200);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // Seed sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 21.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE", 420.0, "LAB-102");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE", 0.0, "HALL-A1");

        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        //link sensors back to their rooms
        r1.getSensorIds().add(s1.getId());
        r2.getSensorIds().add(s2.getId());
        r3.getSensorIds().add(s3.getId());

        // Seed a couple of historical readings for TEMP 001
        List<SensorReading> tempHistory = new ArrayList<>();
        tempHistory.add(new SensorReading(20.8));
        tempHistory.add(new SensorReading(21.2));
        tempHistory.add(new SensorReading(21.5));
        readings.put(s1.getId(), tempHistory);
    }
}
