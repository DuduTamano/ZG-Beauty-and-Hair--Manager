package com.example.zgbeautyandhairstaff.Model;

import com.google.firebase.firestore.DocumentId;

import java.util.List;

public class ProductItem {

    @DocumentId
    String name, image, productId, dec;
    Long price;
    List<CartItem> cartItemList;
    double quantity;
    long size_price;

    public ProductItem() {
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public long getSize_price() {
        return size_price;
    }

    public void setSize_price(long size_price) {
        this.size_price = size_price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDec() {
        return dec;
    }

    public void setDec(String dec) {
        this.dec = dec;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public List<CartItem> getCartItemList() {
        return cartItemList;
    }

    public void setCartItemList(List<CartItem> cartItemList) {
        this.cartItemList = cartItemList;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

}
