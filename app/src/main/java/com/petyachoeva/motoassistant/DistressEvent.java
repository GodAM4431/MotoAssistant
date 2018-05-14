package com.petyachoeva.motoassistant;

public class DistressEvent {
    String sender_username;
    double latitude;
    double longitude;
    long timestamp;

    public DistressEvent() {
    }

    public DistressEvent(String sender_username, double latitude, double longitude, long timestamp) {
        this.sender_username = sender_username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public String getSender_username() {
        return sender_username;
    }

    public void setSender_username(String sender_username) {
        this.sender_username = sender_username;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
