package com.example.zgbeautyandhairstaff.Common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.zgbeautyandhairstaff.Model.EventBus.LoadBookingEvent;
import com.example.zgbeautyandhairstaff.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class BottomSheetBookingFragment extends BottomSheetDialogFragment {

    @OnClick(R.id.placed_filter)
    public void onBookingFilterClick(){
        EventBus.getDefault().postSticky(new LoadBookingEvent(0));
        dismiss();
    }

    @OnClick(R.id.shipping_filter)
    public void onShippingFilterClick(){
        EventBus.getDefault().postSticky(new LoadBookingEvent(1));
        dismiss();
    }

    private Unbinder unbinder;

    private static BottomSheetBookingFragment instance;

    public static BottomSheetBookingFragment getInstance() {
        return instance == null ? new BottomSheetBookingFragment() : instance;
    }

    public BottomSheetBookingFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.booking_filter, container, false);

        unbinder = ButterKnife.bind(this, itemView);
        return itemView;
    }
}
