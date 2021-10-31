package com.pawlowski.shopisto.models;

public class CategoryModel {

    public CategoryModel(int id, String tittle, int imageDrawableId) {
        this.id = id;
        this.tittle = tittle;
        this.imageDrawableId = imageDrawableId;
    }

    private int id;
    private String tittle;
    private int imageDrawableId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public int getImageDrawableId() {
        return imageDrawableId;
    }

    public void setImageDrawableId(int imageDrawableId) {
        this.imageDrawableId = imageDrawableId;
    }

    public int compareTo(CategoryModel c2)
    {
        return tittle.compareTo(c2.tittle);
    }
}
