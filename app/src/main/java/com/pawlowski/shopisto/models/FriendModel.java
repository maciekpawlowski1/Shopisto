package com.pawlowski.shopisto.models;

public class FriendModel {

    public FriendModel(String nickname, String mail, boolean inList, boolean owner) {
        this.nickname = nickname;
        this.mail = mail;
        this.owner = owner;
        this.inList = inList;
    }

    private String nickname; //Will be avaible to set for user
    private String mail;
    String uid;
    private boolean owner;
    private boolean inList;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public boolean isInList() {
        return inList;
    }

    public void setInList(boolean inList) {
        this.inList = inList;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
