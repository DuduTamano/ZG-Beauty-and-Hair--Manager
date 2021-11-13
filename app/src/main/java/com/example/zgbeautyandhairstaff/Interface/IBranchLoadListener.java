package com.example.zgbeautyandhairstaff.Interface;

import com.example.zgbeautyandhairstaff.Model.Salon;

import java.util.List;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> salonList);
    void onBranchLoadFailed(String message);

}
