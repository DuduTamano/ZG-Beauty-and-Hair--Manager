package com.example.zgbeautyandhairstaff.ViewModel;

import android.app.SearchManager;
import android.content.Context;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.zgbeautyandhairstaff.Adapter.MyUserContactAdapter;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Interface.IUsersContactListener;
import com.example.zgbeautyandhairstaff.Model.Category;
import com.example.zgbeautyandhairstaff.Model.UserContact;
import com.example.zgbeautyandhairstaff.UsersContactActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewUsersContact extends ViewModel implements IUsersContactListener {
    private MutableLiveData<List<UserContact>> userContactListMutable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();

    private IUsersContactListener usersContactListener;

    CollectionReference usersRef;

    public ViewUsersContact() {
        usersContactListener = this;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public MutableLiveData<List<UserContact>> getUserContactListMutable() {
        if (userContactListMutable == null)
        {
            userContactListMutable = new MutableLiveData<>();
            messageError = new MutableLiveData<>();

            loadUserContactId();
        }
        return userContactListMutable;
    }

    public void loadUserContactId() {
        usersRef = FirebaseFirestore.getInstance().collection("Users");

        usersRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<UserContact> userContacts = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot userContactSnapshot : task.getResult()) {
                                UserContact userContact = userContactSnapshot.toObject(UserContact.class);

                                userContacts.add(userContact);
                                Common.currentUserContact = userContact;

                                userContact.setId(userContactSnapshot.getId());

                            }
                            usersContactListener.onUsersContactLoadSuccess(userContacts);
                        }
                    }
                });
    }

    @Override
    public void onUsersContactLoadSuccess(List<UserContact> userContactList) {
        userContactListMutable.setValue(userContactList);
    }

    @Override
    public void onUsersContactLoadFailed(String message) {
        messageError.setValue(message);
    }
}