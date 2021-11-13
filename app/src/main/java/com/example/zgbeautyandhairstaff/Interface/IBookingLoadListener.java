package com.example.zgbeautyandhairstaff.Interface;

import com.example.zgbeautyandhairstaff.Model.BookingInformation;

import java.util.List;

public interface IBookingLoadListener {
    void onBookingLoadSuccess(List<BookingInformation> bookingInformationList);
    void onBookingLoadFailed(String message);
}
