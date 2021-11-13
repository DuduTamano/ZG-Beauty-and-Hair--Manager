package com.example.zgbeautyandhairstaff.Model.EventBus;

public class ChangeBookingClick {
    private boolean isFromBookingList;

    public ChangeBookingClick(boolean isFromBookingList) {
        this.isFromBookingList = isFromBookingList;
    }

    public boolean isFromBookingList() {
        return isFromBookingList;
    }

    public void setFromBookingList(boolean fromBookingList) {
        isFromBookingList = fromBookingList;
    }
}
