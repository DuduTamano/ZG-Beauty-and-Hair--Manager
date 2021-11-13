package com.example.zgbeautyandhairstaff.Interface;

import com.example.zgbeautyandhairstaff.Model.Order;

import java.util.List;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<Order> orderList);
    void onOrderLoadFailed(String message);

}
