package com.bestinpest.model;


public class Trip {

    private Long time;
    private Long predictedTime;
    private String relationName;
    private String relationId;
    private String headsign;
    private String tripId;


    public Trip() {
    }

    public Trip(Long time, Long predictedTime, String relationName, String relationId, String headsign, String tripId) {
        this.time = time;
        this.predictedTime = predictedTime;
        this.relationName = relationName;
        this.relationId = relationId;
        this.headsign = headsign;
        this.tripId = tripId;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getPredictedTime() {
        return predictedTime;
    }

    public void setPredictedTime(Long predictedTime) {
        this.predictedTime = predictedTime;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getRelationId() {
        return relationId;
    }

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
