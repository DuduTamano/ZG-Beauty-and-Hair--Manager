package com.example.zgbeautyandhairstaff;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zgbeautyandhairstaff.Adapter.MyStateAdapter;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Common.SpacesItemDecoration;
import com.example.zgbeautyandhairstaff.Interface.IOnAllStateLoadListener;
import com.example.zgbeautyandhairstaff.Model.Barber;
import com.example.zgbeautyandhairstaff.Model.City;
import com.example.zgbeautyandhairstaff.Model.Salon;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements IOnAllStateLoadListener {

    @BindView(R.id.recycler_state)
    RecyclerView recycler_state;

    CollectionReference allSalonCollection;

    IOnAllStateLoadListener iOnAllStateLoadListener;

    MyStateAdapter adapter;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        FirebaseInstanceId.getInstance()
                                .getInstanceId()
                                .addOnFailureListener(e ->
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful())
                                    {
                                        Common.updateToken(MainActivity.this,
                                                Objects.requireNonNull(task.getResult()).getToken());
                                        Log.d("Z&G Tokens", task.getResult().getToken());
                                    }
                                });

                        Paper.init(MainActivity.this);
                        String user = Paper.book().read(Common.LOGGED_KEY);

                        //if user not login before
                        if (TextUtils.isEmpty(user))
                        {
                            setContentView(R.layout.activity_main);
                            ButterKnife.bind(MainActivity.this);
                            initView();
                            init();
                            loadAllStateFromFireStore();
                        }

                        //if user already login
                        else
                        {
                            //Auto login start
                            Gson gson = new Gson();
                            Common.state_name = Paper.book().read(Common.STATE_KEY);
                            Common.selected_salon = gson.fromJson(Paper.book().read(Common.SALON_KEY,""),
                                    new TypeToken<Salon>(){}.getType());
                            Common.currentBarber = gson.fromJson(Paper.book().read(Common.BARBER_KEY,""),
                                    new TypeToken<Barber>(){}.getType());

                            Intent intent = new Intent(MainActivity.this, StaffHomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();
    }

    private void loadAllStateFromFireStore() {
        dialog.dismiss();

        allSalonCollection
                .get()
                .addOnFailureListener(e -> iOnAllStateLoadListener.onAllStateLoadFailed(e.getMessage()))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        List<City> cities = new ArrayList<>();
                        for (DocumentSnapshot citySnapShot: Objects.requireNonNull(task.getResult()))
                        {
                            City city = citySnapShot.toObject(City.class);
                            cities.add(city);
                        }
                        iOnAllStateLoadListener.onAllStateLoadSuccess(cities);

                    }
                });
    }

    private void init() {
        allSalonCollection = FirebaseFirestore.getInstance().collection("AllSalon");
        iOnAllStateLoadListener = this;

        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this)
                .build();
    }

    private void initView() {
        recycler_state.setHasFixedSize(true);
        recycler_state.setLayoutManager(new GridLayoutManager(this,2));
        recycler_state.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onAllStateLoadSuccess(List<City> cityList) {
        adapter = new MyStateAdapter(this,cityList);
        recycler_state.setAdapter(adapter);

        dialog.dismiss();
    }

    @Override
    public void onAllStateLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }
}
