package com.example.zgbeautyandhairstaff.Interface;

import com.example.zgbeautyandhairstaff.Model.Category;
import com.example.zgbeautyandhairstaff.Model.UserContact;

import java.util.List;

public interface IUsersContactListener {
    void onUsersContactLoadSuccess(List<UserContact> userContactList);
    void onUsersContactLoadFailed(String message);
}
