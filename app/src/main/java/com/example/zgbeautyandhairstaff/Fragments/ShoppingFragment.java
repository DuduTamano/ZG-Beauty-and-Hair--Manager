package com.example.zgbeautyandhairstaff.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zgbeautyandhairstaff.Adapter.MyShoppingItemAdapter;
import com.example.zgbeautyandhairstaff.Common.SpacesItemDecorationGrid;
import com.example.zgbeautyandhairstaff.Interface.IOnShoppingItemSelected;
import com.example.zgbeautyandhairstaff.Interface.IShoppingDataLoadListener;
import com.example.zgbeautyandhairstaff.Model.ProductItem;
import com.example.zgbeautyandhairstaff.Model.ShoppingItem;
import com.example.zgbeautyandhairstaff.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ShoppingFragment extends BottomSheetDialogFragment implements IShoppingDataLoadListener, IOnShoppingItemSelected {
    IOnShoppingItemSelected callBackToActivity;

    CollectionReference shoppingItemRef;

    IShoppingDataLoadListener iShoppingDataLoadListener;
    Unbinder unbinder;

    @BindView(R.id.chip_group)
    ChipGroup chipGroup;

    @BindView(R.id.chip_brazilian_hair)
    Chip chip_brazilian_hair;

    @OnClick(R.id.chip_brazilian_hair)
    void waveCareChipClick(){
        setSelectedChip(chip_brazilian_hair);
        loadShoppingItem("Brazilian Hair");
    }

    @BindView(R.id.chip_wax)
    Chip chip_wax;

    @OnClick(R.id.chip_wax)
    void waxChipClick(){
        setSelectedChip(chip_wax);
        loadShoppingItem("Wax");
    }

    @BindView(R.id.chip_spray)
    Chip chip_spray;

    @OnClick(R.id.chip_spray)
    void sprayChipClick(){
        setSelectedChip(chip_spray);
        loadShoppingItem("Spray");
    }

    @BindView(R.id.chip_hair_cream)
    Chip chip_hair_cream;

    @OnClick(R.id.chip_hair_cream)
    void hairCareChipClick(){
        setSelectedChip(chip_hair_cream);
        loadShoppingItem("Hair Cream");
    }

    @BindView(R.id.chip_wave)
    Chip chip_wave;

    @OnClick(R.id.chip_wave)
    void bodyCareChipClick(){
        setSelectedChip(chip_wave);
        loadShoppingItem("פאות");
    }

    @BindView(R.id.recycler_items)
    RecyclerView recycler_items;


    private static ShoppingFragment instance;

    public static ShoppingFragment getInstance(IOnShoppingItemSelected iOnShoppingItemSelected) {
        return instance == null? new ShoppingFragment(iOnShoppingItemSelected):instance;
    }

    private void loadShoppingItem(String itemMenu) {
        shoppingItemRef = FirebaseFirestore.getInstance().collection("Shopping")
                .document(itemMenu)
                .collection("items");

        //get data
        shoppingItemRef.get()
                .addOnFailureListener(e -> iShoppingDataLoadListener.onShoppingDataLoadFailed(e.getMessage()))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        List<ShoppingItem> shoppingItems = new ArrayList<>();
                        for (DocumentSnapshot itemSnapShot:task.getResult())
                        {
                            ShoppingItem shoppingItem = itemSnapShot.toObject(ShoppingItem.class);
                            //Add to don't get null reference
                            shoppingItem.setId(itemSnapShot.getId());
                            shoppingItems.add(shoppingItem);

                        }
                        iShoppingDataLoadListener.onShoppingDataLoadSuccess(shoppingItems);
                    }
                });
    }

    private void setSelectedChip(Chip chip) {
        //Set color
        for (int i=0; i<chipGroup.getChildCount(); i++)
        {
            Chip chipItem = (Chip)chipGroup.getChildAt(i);

            //if not selected
            if (chipItem.getId() != chip.getId())
            {
                chipItem.setChipBackgroundColorResource(android.R.color.white);
                chipItem.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }

            //if selected
            else
            {
                chipItem.setChipBackgroundColorResource(android.R.color.white);
                chipItem.setTextColor(getResources().getColor(android.R.color.black));
            }

        }
    }

    @SuppressLint("ValidFragment")
    public ShoppingFragment(IOnShoppingItemSelected callBackToActivity) {
        this.callBackToActivity = callBackToActivity;
    }

    public ShoppingFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_shopping, container, false);

        unbinder = ButterKnife.bind(this, itemView);

        //Default load
        loadShoppingItem("Brazilian Hair");

        init();
        
        initView();

        return itemView;
    }

    private void initView() {
        recycler_items.setHasFixedSize(true);
        recycler_items.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recycler_items.addItemDecoration(new SpacesItemDecorationGrid(2));
    }

    private void init() {
        iShoppingDataLoadListener = this;
    }

    @Override
    public void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList) {
        MyShoppingItemAdapter adapter = new MyShoppingItemAdapter(getContext(), shoppingItemList, this);
        recycler_items.setAdapter(adapter);
    }

    @Override
    public void onShoppingDataLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem, ProductItem productItem) {
        callBackToActivity.onShoppingItemSelected(shoppingItem, productItem);
    }
}
