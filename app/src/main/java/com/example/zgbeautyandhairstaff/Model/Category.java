package com.example.zgbeautyandhairstaff.Model;

import java.util.List;

public class Category {
    String categoryId, image, name, dec;
    List<ShoppingItem> Items;
    private Long price;

    public Category() {
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getDec() {
        return dec;
    }

    public void setDec(String dec) {
        this.dec = dec;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<ShoppingItem> getItems() {
        return Items;
    }

    public void setItems(List<ShoppingItem> items) {
        Items = items;
    }
}
