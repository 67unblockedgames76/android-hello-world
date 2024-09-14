package com.example.agenttracker;

public class LocationData {
    public double latitude;
    public double longitude;

    public LocationData() {
        // Default constructor required for Firebase
    }

    public LocationData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
