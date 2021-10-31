package com.pawlowski.shopisto.models;

import java.util.ArrayList;
import java.util.List;

public class ListModel extends Model {

    public ListModel(String tittle, int numberSelected, int numberAll, List<ProductModel> products) {
        this.tittle = tittle;
        this.numberSelected = numberSelected;
        this.numberAll = numberAll;
        this.products = products;
        this.type = LIST_MODEL_TYPE;
        this.downloadTimestamp = "0";
        this.friendsTimestamp = "0";
        this.amIOwner = true;
    }

    public ListModel getCopy()
    {
        return new ListModel(tittle, numberSelected, numberAll, new ArrayList<>(products));
    }

    private int id;
    private String tittle;
    private int numberSelected;
    private int numberAll;
    private String firebaseKey;
    private String downloadTimestamp;
    private String friendsTimestamp;
    private boolean amIOwner;

    private List<ProductModel> products;

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public int getNumberSelected() {
        return numberSelected;
    }

    public void setNumberSelected(int numberSelected) {
        this.numberSelected = numberSelected;
    }

    public int getNumberAll() {
        return numberAll;
    }

    public void setNumberAll(int numberAll) {
        this.numberAll = numberAll;
    }

    public List<ProductModel> getProducts() {
        return products;
    }

    public void setProducts(List<ProductModel> products) {
        this.products = products;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addProduct(ProductModel product)
    {
        if(product != null)
            products.add(product);
    }


    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }


    public String getDownloadTimestamp() {
        return downloadTimestamp;
    }

    public void setDownloadTimestamp(String downloadTimestamp) {
        this.downloadTimestamp = downloadTimestamp;
    }

    public String getFriendsTimestamp() {
        return friendsTimestamp;
    }

    public void setFriendsTimestamp(String friendsTimestamp) {
        this.friendsTimestamp = friendsTimestamp;
    }

    public boolean isAmIOwner() {
        return amIOwner;
    }

    public void setAmIOwner(boolean amIOwner) {
        this.amIOwner = amIOwner;
    }
}
