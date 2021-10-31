package com.pawlowski.shopisto.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class ProductModel implements Parcelable {



    public ProductModel(String tittle, String description, boolean selected, int number) {
        this.tittle = tittle;
        this.description = description;
        this.selected = selected;
        this.number = number;
        categoryId = 0;
    }

    private String tittle;
    private String description;
    private boolean selected;
    private int number;
    private int id;
    private int categoryId;

    protected ProductModel(Parcel in) {
        tittle = in.readString();
        description = in.readString();
        selected = in.readByte() != 0;
        number = in.readInt();
        id = in.readInt();
    }

    public static final Creator<ProductModel> CREATOR = new Creator<ProductModel>() {
        @Override
        public ProductModel createFromParcel(Parcel in) {
            ProductModel productModel = new ProductModel(in);

            return productModel;
        }

        @Override
        public ProductModel[] newArray(int size) {
            return new ProductModel[size];
        }


    };

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tittle);
        dest.writeString(description);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dest.writeBoolean(selected);
        }*/
        dest.writeByte((byte) (selected?1:0));
        dest.writeInt(number);



        //dest.writeBoolean(selected); Required 29 API
        dest.writeInt(id);
        //dest.writeInt(categoryId);
    }


    @Override
    public String toString() {
        return "ProductModel{" +
                "tittle='" + tittle + '\'' +
                ", description='" + description + '\'' +
                ", selected=" + selected +
                ", number=" + number +
                ", id=" + id +
                ", categoryId=" + categoryId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductModel that = (ProductModel) o;
        return selected == that.selected && number == that.number && categoryId == that.categoryId && tittle.equals(that.tittle) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tittle, description, selected, number, categoryId);
    }


}
