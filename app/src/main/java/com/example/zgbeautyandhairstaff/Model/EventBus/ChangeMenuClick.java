package com.example.zgbeautyandhairstaff.Model.EventBus;

public class ChangeMenuClick {
    private boolean isFromOrderList;

    public ChangeMenuClick(boolean isFromOrderList) {
        this.isFromOrderList = isFromOrderList;
    }

    public boolean isFromOrderList() {
        return isFromOrderList;
    }

    public void setFromOrderList(boolean fromOrderList) {
        isFromOrderList = fromOrderList;
    }
}
