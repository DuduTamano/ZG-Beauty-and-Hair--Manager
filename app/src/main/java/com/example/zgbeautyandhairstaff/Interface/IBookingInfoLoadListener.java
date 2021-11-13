package com.example.zgbeautyandhairstaff.Interface;

import com.example.zgbeautyandhairstaff.Model.BookingInformation;

public interface IBookingInfoLoadListener {
    void onBookingInfoLoadEmpty();
    void onBookingInfoLoadSuccess(BookingInformation bookingInformation, String documentId);
    void onBookingInfoLoadFailed(String message);
}
