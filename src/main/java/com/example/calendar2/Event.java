package com.example.calendar2;

import java.util.Date;

public class Event implements Comparable<Event>{

    private int eventID;
    private int originEventID;
    private String startDateTime;
    private String endDateTime;
    private String eventName;
    private String recurringDays;
    private String location;
    private String phone;
    private String description;
    private boolean isDone;
    private boolean isRecurring;

    private int viewType;


    public Event(int eventID, String startDateTime, String eventName, String recurringDays, String location, String phone, String description) {
        this.eventID = eventID;
        this.startDateTime = startDateTime;
        this.eventName = eventName;
        this.recurringDays = recurringDays;
        this.location = location;
        this.phone = phone;
        this.description = description;
    }

    public Event() {
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public int getOriginEventID() {
        return originEventID;
    }

    public void setOriginEventID(int originEventID) {
        this.originEventID = originEventID;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public String getEventName() {
        return eventName;
    }

    public String getRecurringDays() {
        return recurringDays;
    }

    public String getLocation() {
        return location;
    }

    public String getPhone() {
        return phone;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setRecurringDays(String recurringDays) {
        this.recurringDays = recurringDays;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    @Override
    public int compareTo(Event o) {
        if (getStartDateTime() == null || o.getStartDateTime() == null)
            return 0;
        return getStartDateTime().compareTo(o.getStartDateTime());
    }
}
