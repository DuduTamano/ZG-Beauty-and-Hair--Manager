package com.example.zgbeautyandhairstaff.Interface;

import com.example.zgbeautyandhairstaff.Model.Banner;

import java.util.List;

public interface IBannerLoadListener {
    void onBannerLoadSuccess(List<Banner> banners);
    void onBannerLoadFailed(String message);
}
