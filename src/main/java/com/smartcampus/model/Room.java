package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;

/*
 * Represents a physical room on campus
 * Holds basic metadata and tracks which sensors are deployed inside it.
 */
public class Room {

    private String id;       // unique identifier ex: "LIB-301"
    private String name;     // human readable label shown in the UI
    private int capacity;    // max occupancy used for safety checks
    private List<String> sensorIds = new ArrayList<>(); // IDs of sensors installed here

    public Room() {}

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    //Getters and Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }
}
