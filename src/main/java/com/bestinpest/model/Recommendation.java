package com.bestinpest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class Recommendation {

    @Id
    @GeneratedValue
    private Long id;

    private String departureJunctionId;
    private String arrivalJunctionId;
    private Long senderPlayerId;
    private Long receiverPlayerId;

    @ManyToOne
    @JoinColumn(name="stepId")
    @JsonIgnore
    private DetectiveStep step;

    public Recommendation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartureJunctionId() {
        return departureJunctionId;
    }

    public void setDepartureJunctionId(String departureJunctionId) {
        this.departureJunctionId = departureJunctionId;
    }

    public String getArrivalJunctionId() {
        return arrivalJunctionId;
    }

    public void setArrivalJunctionId(String arrivalJunctionId) {
        this.arrivalJunctionId = arrivalJunctionId;
    }

    public Long getSenderPlayerId() {
        return senderPlayerId;
    }

    public void setSenderPlayerId(Long senderPlayerId) {
        this.senderPlayerId = senderPlayerId;
    }

    public Long getReceiverPlayerId() {
        return receiverPlayerId;
    }

    public void setReceiverPlayerId(Long receiverPlayerId) {
        this.receiverPlayerId = receiverPlayerId;
    }

    public DetectiveStep getStep() {
        return step;
    }

    public void setStep(DetectiveStep step) {
        this.step = step;
    }
}
