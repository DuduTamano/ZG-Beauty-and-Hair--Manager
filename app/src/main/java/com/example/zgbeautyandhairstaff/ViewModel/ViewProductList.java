package com.example.zgbeautyandhairstaff.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Model.ShoppingItem;

import java.util.List;

public class ViewProductList extends ViewModel {
    private MutableLiveData<List<ShoppingItem>> mutableLiveDataProducttList;

    public ViewProductList() {
    }

    public MutableLiveData<List<ShoppingItem>> getMutableLiveDataProducttList() {
        if (mutableLiveDataProducttList == null)
            mutableLiveDataProducttList = new MutableLiveData<>();
        mutableLiveDataProducttList.setValue(Common.categorySelected.getItems());
        return mutableLiveDataProducttList;
    }
}
