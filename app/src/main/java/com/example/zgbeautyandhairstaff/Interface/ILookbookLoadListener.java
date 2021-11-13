package com.example.zgbeautyandhairstaff.Interface;

import com.example.zgbeautyandhairstaff.Model.Banner;

import java.util.List;

public interface ILookbookLoadListener {
    void onLookbookSuccess(List<Banner> banners);
    void onLookbookFailed(String message);
}
