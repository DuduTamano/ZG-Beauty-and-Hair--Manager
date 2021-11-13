package com.example.zgbeautyandhairstaff.Model.EventBus;

public class ToastEvent {
    private boolean isUpdate;
    private boolean isFromItemList;

    public ToastEvent(boolean isUpdate, boolean isFromItemList) {
        this.isUpdate = isUpdate;
        this.isFromItemList = isFromItemList;
    }

    public boolean isFromItemList() {
        return isFromItemList;
    }

    public void setFromItemList(boolean fromItemList) {
        isFromItemList = fromItemList;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }
}
