package com.example.meetup.model;

import java.io.Serializable;

public class Meet implements Serializable {
    private long timestamp;
    private String startTime,endTime,date,number;
    private User user;

    public Meet() {
    }

    public Meet(long timestamp, String number , String date, String startTime, String endTime, User user) {
        this.timestamp = timestamp;
        this.number=number;
        this.date=date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.user = user;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
