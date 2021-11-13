package com.example.zgbeautyandhairstaff;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.bumptech.glide.Glide;
import com.example.zgbeautyandhairstaff.Adapter.MyProductListAdapter;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Common.MySwipeHelper;
import com.example.zgbeautyandhairstaff.Interface.IShoppingDataLoadListener;
import com.example.zgbeautyandhairstaff.Model.CartItem;
import com.example.zgbeautyandhairstaff.Model.Category;
import com.example.zgbeautyandhairstaff.Model.EventBus.ChangeMenuClick;
import com.example.zgbeautyandhairstaff.Model.EventBus.ToastEvent;
import com.example.zgbeautyandhairstaff.Model.ShoppingItem;
import com.example.zgbeautyandhairstaff.ViewModel.ViewProductList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class ProductListActivity extends AppCompatActivity implements IShoppingDataLoadListener {
    private ViewProductList productListViewModels;

    Unbinder unbinder;

    @BindView(R.id.recycler_category_list)
    RecyclerView recycler_category_list;

    TextView txt_name_selected;

    LayoutAnimationController layoutAnimationController;

    MyProductListAdapter adapter;

    CollectionReference shoppingItemRef;

    IShoppingDataLoadListener iShoppingDataLoadListener;

    List<ShoppingItem> shoppingItemList;

    private static final int PICK_IMAGE_REQUEST = 7234;

    ImageView img_item;

    StorageReference storageReference;

    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    android.app.AlertDialog dialog;

    Uri imgUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        productListViewModels = new ViewModelProvider(this).get(ViewProductList.class);

        txt_name_selected = findViewById(R.id.txt_name_selected);

        unbinder = ButterKnife.bind(this);

        initViews();

        init();

        productListViewModels.getMutableLiveDataProducttList().observe( this, new Observer<List<ShoppingItem>>() {
            @Override
            public void onChanged(List<ShoppingItem> shoppingItems) {
                if (shoppingItems != null) {
                      shoppingItemList = shoppingItems;

                    recycler_category_list.setLayoutAnimation( layoutAnimationController );
                }
            }
        });

    }

    private void init() {
        iShoppingDataLoadListener = this;
    }

    private void initViews() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        loadShoppingItem();

        storageReference = FirebaseStorage.getInstance().getReference();

        txt_name_selected.setText(Common.categorySelected.getName());

        recycler_category_list.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_category_list.setLayoutManager(layoutManager);

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_item_from_left);

        MySwipeHelper mySwipeHelper = new MySwipeHelper(this, recycler_category_list, 300) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(ProductListActivity.this, "מחיקה", 50, 0, Color.parseColor("#000000"),
                        pos -> {

                    if (shoppingItemList != null)
                        Common.selectedItem = shoppingItemList.get(pos);

                        AlertDialog.Builder builder = new AlertDialog.Builder(ProductListActivity.this)
                                .setTitle("מחיקה")
                                .setMessage("האם את בטוחה שאת רוצה למחוק את המוצר?")
                                .setNegativeButton("ביטול", (dialog, i) -> dialog.dismiss())
                                .setPositiveButton("מחיקה", (dialog, which) -> {

                                    FirebaseFirestore.getInstance().collection("Shopping")
                                            .document(Common.categorySelected.getCategoryId())
                                            .collection("items")
                                            .get().addOnCompleteListener( new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot updateSnapshot = task.getResult().getDocuments().get(pos);

                                                String Id = updateSnapshot.getId();

                                                DocumentReference UpdateItems = FirebaseFirestore.getInstance()
                                                        .collection("Shopping")
                                                        .document(Common.categorySelected.getCategoryId())
                                                        .collection("items")
                                                        .document(Id);

                                                UpdateItems.get().addOnFailureListener( new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull @NotNull Exception e) {
                                                        Toast.makeText( ProductListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
                                                    }
                                                }).addOnSuccessListener( new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                        updateItem(Id, true);

                                                    }
                                                });
                                            }
                                        }
                                    });

                                });

                            //create dialog
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            Button negativeButton = dialog.getButton(dialog.BUTTON_NEGATIVE);
                            negativeButton.setTextColor(Color.GRAY);
                            Button positiveButton = dialog.getButton(dialog.BUTTON_POSITIVE);
                            positiveButton.setTextColor(Color.RED);
                        }));

                buf.add(new MyButton(ProductListActivity.this, "עדכון", 50, 0, Color.parseColor("#484848"),
                        pos -> {

                            FirebaseFirestore.getInstance().collection("Shopping")
                                    .document(Common.categorySelected.getCategoryId())
                                    .collection("items")
                                    .get().addOnCompleteListener( new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot updateSnapshot = task.getResult().getDocuments().get(pos);

                                        String Id = updateSnapshot.getId();

                                        String name = updateSnapshot.getString("name");

                                        String image = updateSnapshot.getString("image");

                                        Long price = updateSnapshot.getLong("price");

                                        String dec = updateSnapshot.getString("dec");

                                        DocumentReference UpdateItems = FirebaseFirestore.getInstance()
                                                .collection("Shopping")
                                                .document(Common.categorySelected.getCategoryId())
                                                .collection("items")
                                                .document(Id);

                                        UpdateItems.get().addOnFailureListener( new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull @NotNull Exception e) {
                                                Toast.makeText( ProductListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
                                            }
                                        }).addOnSuccessListener( new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                showUpdateDialog(name, image, dec, Id, price);

                                            }
                                        });
                                    }
                                }
                            });

                        }));
            }
        };

    }

    private void showUpdateDialog(String name, String image, String dec, String Id, Long price) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("עדכון מוצר");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_update_item, null);
        EditText edt_item_name = (EditText) itemView.findViewById(R.id.edt_item_name);
        EditText edt_item_price = (EditText) itemView.findViewById(R.id.edt_item_price);
        EditText edt_item_description = (EditText) itemView.findViewById(R.id.edt_item_description);
        img_item = (ImageView) itemView.findViewById(R.id.img_item);


        edt_item_name.setText(new StringBuilder("").append(name));
        edt_item_price.setText(new StringBuilder().append(price));
        edt_item_description.setText(dec);
        Glide.with(this).load(image).into(img_item);

        //Set event
        img_item.setOnClickListener(v -> {
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

            Map<String, Object> updateItem = new HashMap<>();

            updateItem.put("name", edt_item_name.getText().toString());
            updateItem.put("dec",edt_item_description.getText().toString());
            updateItem.put("price", TextUtils.isEmpty(edt_item_price.getText().toString()) ? 0 :
                    Long.parseLong(edt_item_price.getText().toString()));

            if (imgUri != null)
            {
                //we will firestore to upload image
                dialog.setMessage("טוען...");
                dialog.show();

                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);

                imageFolder.putFile(imgUri)
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot task) {
                        dialog.dismiss();
                        imageFolder.getDownloadUrl().addOnSuccessListener( uri -> {
                            updateItem.put("image", uri.toString() );

                            FirebaseFirestore.getInstance().collection("Shopping")
                                    .document(Common.categorySelected.getCategoryId())
                                    .collection("items")
                                    .document(Id).set(updateItem);

                        } );
                    }
                } ).addOnProgressListener( taskSnapshot -> {
                    double progress = Math.round(100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.show();
                    dialog.setMessage(new StringBuilder("מעדכן: ").append(progress).append("%"));
                });
            }

            FirebaseFirestore.getInstance().collection("Shopping")
                    .document(Common.categorySelected.getCategoryId())
                    .collection("items")
                    .document(Id)
                    .update(updateItem)
                    .addOnSuccessListener( new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            EventBus.getDefault().postSticky(new ToastEvent(true,true));
                            resetStaticData();
                            finish();

                            Toast toast = new Toast(ProductListActivity.this);
                            toast.setDuration(Toast.LENGTH_LONG);
                            View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                            TextView textView = toastView.findViewById(R.id.txt_message);
                            toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                            textView.setText("עדכון בהצלחה");
                            toast.setView(toastView);
                            toast.show();
                        }
                    }).addOnFailureListener( new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast toast = new Toast(ProductListActivity.this);
                    toast.setDuration(Toast.LENGTH_LONG);
                    View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                    TextView textView = toastView.findViewById(R.id.txt_message);
                    toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                    textView.setText("עדכון נכשל");
                    toast.setView(toastView);
                    toast.show();
                }
            });
        });

        builder.setView(itemView);
        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        Button negativeButton = dialog.getButton(dialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(Color.GRAY);

        Button positiveButton = dialog.getButton(dialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(Color.RED);
    }

    private void resetStaticData() {
        startActivity(new Intent(ProductListActivity.this, CategoryActivity.class));
    }

    private void updateItem(String items, boolean isDelete) {

        Map<String, Object> updateItem = new HashMap<>();
        updateItem.put("items", items);

        FirebaseFirestore.getInstance().collection("Shopping")
                .document(Common.categorySelected.getCategoryId())
                .collection("items")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot snapshot : task.getResult()) {
                        ShoppingItem shoppingItem = snapshot.toObject(ShoppingItem.class);
                        String id = snapshot.getId();
                        DocumentReference shoppingRef = firebaseFirestore.collection("Shopping")
                                .document(Common.categorySelected.getCategoryId())
                                .collection("items")
                                .document(items);

                        shoppingRef.delete()
                                .addOnSuccessListener( new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (task.isSuccessful())
                                        {
                                            productListViewModels.getMutableLiveDataProducttList();
                                            EventBus.getDefault().postSticky(new ToastEvent(!isDelete, true));
                                            resetStaticData();
                                            finish();

                                        }
                                    }
                                }).addOnFailureListener( new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProductListActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });

    }

    private void loadShoppingItem() {
        shoppingItemRef = FirebaseFirestore.getInstance().collection("Shopping")
                .document(Common.categorySelected.getCategoryId())
                .collection("items");

        //get data
        shoppingItemRef.get()
                .addOnFailureListener(e ->
                        iShoppingDataLoadListener.onShoppingDataLoadFailed(e.getMessage()))
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

    @Override
    public void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList) {

        adapter = new MyProductListAdapter( ProductListActivity.this, shoppingItemList);
        recycler_category_list.setAdapter( adapter );
    }

    @Override
    public void onShoppingDataLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if (data != null && data.getData() != null)
            {
                imgUri = data.getData();
                img_item.setImageURI(imgUri);
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }

    public void back(View view) {
        startActivity(new Intent(ProductListActivity.this, CategoryActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}