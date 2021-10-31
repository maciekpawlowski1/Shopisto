package com.pawlowski.shopisto.models;

import java.util.ArrayList;

public class GroupModel extends Model {

    public GroupModel(String tittle) {
        this.tittle = tittle;
        this.type = GROUP_MODEL_TYPE;
    }

    public GroupModel(String tittle, ArrayList<ProductModel> products) {
        this.tittle = tittle;
        this.products = products;
    }

    public GroupModel getCopy()
    {
        return new GroupModel(tittle, new ArrayList<>(products));
    }

    private int id;
    private String tittle;
    private ArrayList<ProductModel>products;

    private String key;
    private long downloadTimestamp;


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

    public ArrayList<ProductModel> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<ProductModel> products) {
        this.products = products;
    }


    public void addProduct(ProductModel product)
    {
        products.add(product);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getDownloadTimestamp() {
        return downloadTimestamp;
    }

    public void setDownloadTimestamp(long downloadTimestamp) {
        this.downloadTimestamp = downloadTimestamp;
    }


}
