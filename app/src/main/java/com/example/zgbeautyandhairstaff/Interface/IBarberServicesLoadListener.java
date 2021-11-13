package com.example.zgbeautyandhairstaff.Interface;

import com.example.zgbeautyandhairstaff.Model.BarberServices;

import java.util.List;

public interface IBarberServicesLoadListener {
    void onBarberServicesLoadSuccess(List<BarberServices> barberServicesList);
    void onBarberServicesLoadFailed(String message);
}
