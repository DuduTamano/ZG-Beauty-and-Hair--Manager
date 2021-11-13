package com.example.zgbeautyandhairstaff.Model.EventBus;

public class EnableNextButton {
    private int timeSlot;
    private boolean button = false;

    public EnableNextButton(int step, int timeSlot, boolean enabledButton) {
        this.timeSlot = timeSlot;
        this.button = enabledButton;
    }

    public boolean isButton() {
        return button;
    }

    public int getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(int timeSlot) {
        this.timeSlot = timeSlot;
    }

}
