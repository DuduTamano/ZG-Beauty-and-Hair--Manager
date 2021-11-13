package com.example.zgbeautyandhairstaff.Interface;

import com.example.zgbeautyandhairstaff.Model.Category;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<Category> orderList);
    void onCategoryLoadFailed(String message);
}
