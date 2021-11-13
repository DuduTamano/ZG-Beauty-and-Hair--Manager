package com.example.zgbeautyandhairstaff;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zgbeautyandhairstaff.Adapter.MyCategoriesAdapter;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Common.MySwipeHelper;
import com.example.zgbeautyandhairstaff.Model.Category;
import com.example.zgbeautyandhairstaff.Model.EventBus.ToastEvent;
import com.example.zgbeautyandhairstaff.ViewModel.ViewCategory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class CategoryActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 7234;

    private ViewCategory viewCategory;

    Unbinder unbinder;

    @BindView(R.id.recycler_category)
    RecyclerView recycler_category;

    ImageView img_category;

    List<Category> categoryModels;

    AlertDialog dialog;

    LayoutAnimationController layoutAnimationController;

    MyCategoriesAdapter adapter;

    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;

    Uri imgUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        viewCategory = new ViewModelProvider(this).get(ViewCategory.class);

        unbinder = ButterKnife.bind(this);

        firebaseFirestore = FirebaseFirestore.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();

        initViews();

        viewCategory.getMessageError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(CategoryActivity.this, "" + s, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        viewCategory.getCategoryListMutable().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categoryList) {
                dialog.dismiss();

                categoryModels = categoryList;

                adapter = new MyCategoriesAdapter( CategoryActivity.this, categoryModels );
                recycler_category.setAdapter( adapter );
                recycler_category.setLayoutAnimation( layoutAnimationController );

            }
        } );

    }

    private void initViews() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
      //  dialog.show();

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_category.setLayoutManager(layoutManager);
      //  recycler_category.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(this, recycler_category, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(CategoryActivity.this, "עדכון", 50, 0, Color.parseColor("#000000"),
                        pos -> {

                        Common.categorySelected = categoryModels.get(pos);

                        showUpdateDialog();
                        
                        }));
            }
        };
    }

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("עדכון");
        builder.setMessage("תמלאי את המידע");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_update_category, null);
        EditText edt_category_name = (EditText) itemView.findViewById(R.id.edt_category_name);
        img_category = (ImageView) itemView.findViewById(R.id.img_category);

        //Set Data
        edt_category_name.setText(new StringBuilder("").append(Common.categorySelected.getName()));
        Glide.with(this).load(Common.categorySelected.getImage()).into(img_category);

        //Set Event
        img_category.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "בחרי תמונה"), PICK_IMAGE_REQUEST);
        });

        builder.setView(itemView);
        builder.setNegativeButton("ביטול", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        builder.setPositiveButton("עדכון", (dialogInterface, i) -> {

            Map<String, Object> updateDate = new HashMap<>();
            updateDate.put("name", edt_category_name.getText().toString());


            if (imgUri != null)
            {
                //we will firestore to upload image
                dialog.setMessage("מעדכן...");
                dialog.show();

                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);

                imageFolder.putFile(imgUri)
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnSuccessListener(task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateDate.put("image", uri.toString());

                        updateCategory(updateDate);

                        resetStaticData();
                        finish();

                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = Math.round(100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.show();
                    dialog.setMessage(new StringBuilder("מעדכן: ").append(progress).append("%"));

                    Toast toast = new Toast(CategoryActivity.this);
                    toast.setDuration(Toast.LENGTH_LONG);
                    View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                    TextView textView = toastView.findViewById(R.id.txt_message);
                    toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                    textView.setText("מוצר עודכן בהצלחה");
                    toast.setView(toastView);
                    toast.show();
                });
            }
            else
            {
                updateCategory(updateDate);
            }

        });

        builder.setView(itemView);
       AlertDialog dialog = builder.create();
        dialog.show();

        Button negativeButton = dialog.getButton(dialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(Color.GRAY);
        Button positiveButton = dialog.getButton(dialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(Color.RED);
    }

    private void resetStaticData() {
        startActivity(new Intent(CategoryActivity.this, StaffHomeActivity.class));
    }

    private void updateCategory(Map<String, Object> updateDate) {
        DocumentReference categoryUpdate = FirebaseFirestore.getInstance().collection("Shopping")
                .document(Common.categorySelected.getCategoryId());

        categoryUpdate.update(updateDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            viewCategory.loadCategoriesId();

                            EventBus.getDefault().postSticky(new ToastEvent(true, false));
                        }
                    }
                }).addOnFailureListener(e ->
                Toast.makeText(CategoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if (data != null && data.getData() != null)
            {
                imgUri = data.getData();
                img_category.setImageURI(imgUri);
            }
        }
    }

    public void back(View view) {
        startActivity(new Intent(CategoryActivity.this, StaffHomeActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}