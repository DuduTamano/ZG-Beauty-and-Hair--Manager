package com.example.zgbeautyandhairstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zgbeautyandhairstaff.Adapter.MyUserContactAdapter;
import com.example.zgbeautyandhairstaff.Common.MySwipeHelper;
import com.example.zgbeautyandhairstaff.Model.UserContact;
import com.example.zgbeautyandhairstaff.ViewModel.ViewUsersContact;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class UsersContactActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private ViewUsersContact viewUsersContact;

    Unbinder unbinder;

    @BindView(R.id.recycler_users_contact)
    RecyclerView recycler_users_contact;

    @BindView(R.id.txt_users_filter)
    TextView txt_users_filter;

    @BindView(R.id.search_view)
    SearchView search_view;

    LayoutAnimationController layoutAnimationController;

    MyUserContactAdapter adapter;

    android.app.AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_contact);

        unbinder = ButterKnife.bind(this);

        viewUsersContact = new ViewModelProvider(this).get(ViewUsersContact.class);

        initViews();

        viewUsersContact.getMessageError().observe(this, s -> {
            Toast.makeText(UsersContactActivity.this, "" + s, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        viewUsersContact.getUserContactListMutable().observe(this, new Observer<List<UserContact>>() {
            @Override
            public void onChanged(List<UserContact> userContactList) {
                dialog.dismiss();

                adapter = new MyUserContactAdapter(UsersContactActivity.this, userContactList);
                recycler_users_contact.setAdapter(adapter);
                recycler_users_contact.setLayoutAnimation(layoutAnimationController);

                updateTextCounter();
            }
        });

        search_view = findViewById(R.id.search_view);

        search_view.setOnQueryTextListener(this);

    }

    private void initViews() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_users_contact.setLayoutManager(layoutManager);

        MySwipeHelper mySwipeHelper = new MySwipeHelper(this, recycler_users_contact, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(UsersContactActivity.this, "מחיקה", 50, 0, Color.parseColor("#000000"),
                        pos -> {

                            showUpdateDialog(adapter.getItemPosition(pos), pos);

                        }));
            }
        };

    }

    private void showUpdateDialog(UserContact userContact, int pos) {
        View layout_dialog;
        androidx.appcompat.app.AlertDialog.Builder builder;

        layout_dialog = LayoutInflater.from(UsersContactActivity.this)
                .inflate(R.layout.layout_users_delete_dialog, null);

        builder = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(layout_dialog);

        Button btn_delete_booking = (Button)layout_dialog.findViewById(R.id.btn_delete_booking);


        //Create Dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.show();

        //Custom dialog
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        btn_delete_booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show dialog confirm
                androidx.appcompat.app.AlertDialog.Builder confirmDialog = new androidx.appcompat.app.AlertDialog.Builder(UsersContactActivity.this)
                        .setCancelable(false)
                        .setTitle("היי!")
                        .setMessage("האם למחוק את "+" " + userContact.getFirstName() + " " + userContact.getLastName() + " מהמערכת ?")
                        .setNegativeButton("ביטול", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        }).setPositiveButton("מאשרת", (dialogInterface, i) -> {
                            deleteUser(userContact, pos);
                            //   SendNotificationDelete(pos, userContact);
                        });
                confirmDialog.show();
            }
        });
    }

    private void deleteUser(UserContact userContact, int pos) {
        DocumentReference deleteUser = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userContact.getPhoneNumber());

        deleteUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            adapter.removeItem(pos);
                            adapter.notifyItemRemoved(pos);

                            updateTextCounter();

                            resetStaticData();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UsersContactActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTextCounter() {
        txt_users_filter.setText(new StringBuilder("רשימת כל הלקוחות (")
                .append(adapter.getItemCount())
                .append(")"));
    }

    private void resetStaticData() {
        startActivity(new Intent(UsersContactActivity.this, StaffHomeActivity.class));
    }

    public void back(View view) {
        startActivity(new Intent(UsersContactActivity.this, StaffHomeActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        adapter.filter(s);
        return true;
    }
}