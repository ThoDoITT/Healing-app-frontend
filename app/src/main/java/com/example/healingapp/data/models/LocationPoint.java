package com.example.healingapp.data.models;

public class LocationPoint {
    public double lat, lng;
    public long timestamp;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public LocationPoint(double lat, double lng, long timestamp) {
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }
}
