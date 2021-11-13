package com.example.zgbeautyandhairstaff.Model.EventBus;

public class LoadBookingEvent {
    private int status;

    public LoadBookingEvent(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
