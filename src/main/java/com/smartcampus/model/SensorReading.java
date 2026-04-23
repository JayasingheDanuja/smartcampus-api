package com.smartcampus.model;

import java.util.UUID;

/*
 * an immutable snapshot of a single sensor measurement at a point in time
 * Each reading is stored in the historical log for its parent sensor.
 */
public class SensorReading {

    private String id;        // Auto generated UUID for this reading event
    private long timestamp;   // epoch milliseconds when the hardware captured the value
    private double value;     // actual metric measured

    public SensorReading() {}

    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    //getters n setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
