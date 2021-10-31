package com.pawlowski.shopisto.models;



public class Model {
    int type;
    long timeWhenDeleted;
    static final int LIST_MODEL_TYPE = 50;
    static final int GROUP_MODEL_TYPE = 51;

    public boolean isItList()
    {
        return type == LIST_MODEL_TYPE;
    }

    public boolean isItGroup()
    {
        return type == GROUP_MODEL_TYPE;
    }

    public long getTimeWhenDeleted()
    {
        return timeWhenDeleted;
    }

    public void setTimeWhenDeleted(long timeWhenDeleted) {
        this.timeWhenDeleted = timeWhenDeleted;
    }
}
