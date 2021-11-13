package com.example.zgbeautyandhairstaff.ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Interface.ICategoryCallbackListener;
import com.example.zgbeautyandhairstaff.Model.Category;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewCategory extends ViewModel implements ICategoryCallbackListener {
    private MutableLiveData<List<Category>> categoryListMutable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();

    private ICategoryCallbackListener categoryCallbackListener;

    CollectionReference categoryRef;

    public ViewCategory() {
        categoryCallbackListener = this;
    }

    public MutableLiveData<List<Category>> getCategoryListMutable() {
        if (categoryListMutable == null)
        {
            categoryListMutable = new MutableLiveData<>();
            messageError = new MutableLiveData<>();

            loadCategoriesId();
        }
        return categoryListMutable;
    }

    public void loadCategoriesId() {
        categoryRef = FirebaseFirestore.getInstance().collection("Shopping");

        categoryRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Category> categories = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot categorySnapshot : task.getResult()) {
                                Category category= categorySnapshot.toObject(Category.class);

                                categories.add(category);
                                Common.categorySelected = category;

                                category.setCategoryId(categorySnapshot.getId());

                            }
                            categoryCallbackListener.onCategoryLoadSuccess(categories);
                        }
                    }
                });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onCategoryLoadSuccess(List<Category> shoppingItems) {
        categoryListMutable.setValue(shoppingItems);
    }

    @Override
    public void onCategoryLoadFailed(String message) {
        messageError.setValue(message);
    }

}
