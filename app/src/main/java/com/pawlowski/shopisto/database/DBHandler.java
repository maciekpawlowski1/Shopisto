package com.pawlowski.shopisto.database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.models.FriendModel;
import com.pawlowski.shopisto.models.GroupModel;
import com.pawlowski.shopisto.models.ListModel;
import com.pawlowski.shopisto.models.Model;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.Comparator;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ShoppingListsDatabase";
    private static final int DATABASE_VERSION = 18;

    private DBHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static DBHandler handler;

    public static DBHandler getInstance(Context context)
    {
        if(handler == null)
        {
            handler = new DBHandler(context.getApplicationContext());
        }
        return handler;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String creatingListsSql = "CREATE TABLE Lists(id INTEGER PRIMARY KEY, tittle TEXT, number_selected INTEGER, number_all INTEGER, is_deleted TEXT, deleted_time_string TEXT, list_firebase_key TEXT, download_timestamp TEXT, friends_timestamp TEXT, am_i_owner TEXT, create_timestamp TEXT)";
        String creatingProductsSql = "CREATE TABLE Products(id INTEGER PRIMARY KEY, tittle TEXT, list_id INTEGER, description TEXT, number INTEGER, selected TEXT, category_id INTEGER)";
        String creatingGroupSql = "CREATE TABLE Groups(id INTEGER PRIMARY KEY, tittle TEXT, is_deleted TEXT, deleted_time_string TEXT, group_key TEXT, download_timestamp LONG, create_timestamp TEXT)";
        String creatingGroupProductsSql = "CREATE TABLE GroupProducts(id INTEGER PRIMARY KEY, tittle TEXT, group_id INTEGER, description TEXT, number INTEGER, category_id INTEGER)";
        String creatingFriendsSql = "CREATE TABLE Friends(mail TEXT PRIMARY KEY, nickname TEXT, friend_uid TEXT)";
        String creatingFriendsInListsSql = "CREATE TABLE FriendsInLists(id INTEGER PRIMARY KEY, list_id INTEGER, friend_mail TEXT, is_owner TEXT, friend_uid TEXT)";



        db.execSQL(creatingListsSql);
        db.execSQL(creatingProductsSql);
        db.execSQL(creatingGroupSql);
        db.execSQL(creatingGroupProductsSql);
        db.execSQL(creatingFriendsSql);
        db.execSQL(creatingFriendsInListsSql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Lists");
        db.execSQL("DROP TABLE IF EXISTS Products");
        db.execSQL("DROP TABLE IF EXISTS Groups");
        db.execSQL("DROP TABLE IF EXISTS GroupProducts");
        db.execSQL("DROP TABLE IF EXISTS Friends");
        db.execSQL("DROP TABLE IF EXISTS FriendsInLists");
        onCreate(db);
    }


    public boolean amIListOwner(int listId)
    {
        ArrayList<FriendModel>friends = new ArrayList<>();
        String selectFriends = "SELECT L.am_i_owner FROM Lists L WHERE L.id = " + listId;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectFriends, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            String amI =  cursor.getString(0);
            cursor.close();
            Log.d("ownerQuery", amI);
            return Boolean.parseBoolean(amI);
        }
        cursor.close();


        return false;
    }



    public boolean isListSynced(String listKey, String validTimestamp)
    {
        return validTimestamp.equals(getListDownloadTimestamp(listKey));

    }

    public String getListDownloadTimestamp(String listKey)
    {
        String selectFriends = "SELECT L.download_timestamp FROM Lists L WHERE L.list_firebase_key LIKE '" + listKey + "'";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectFriends, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            String timestamp =  cursor.getString(0);
            cursor.close();
            return timestamp;
        }
        cursor.close();


        return null;
    }

    public boolean areListFriendsSynced(String listKey, String validTimestamp)
    {
        return validTimestamp.equals(getListFriendsTimestamp(listKey));

    }

    public String getListFriendsTimestamp(String listKey)
    {

        //ArrayList<FriendModel>friends = new ArrayList<>();
        String selectFriends = "SELECT L.friends_timestamp FROM Lists L WHERE L.list_firebase_key LIKE '" + listKey + "'";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectFriends, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            String timestamp =  cursor.getString(0);
            cursor.close();
            Log.d("timestamp", timestamp);
            return timestamp;
        }
        cursor.close();


        return null;
    }

    public void updateListFriendsTimestamp(String listKey, String timestamp)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("friends_timestamp", timestamp);

// updating row
        db.update("Lists", values, "list_firebase_key = ?",
                new String[]{listKey});

    }


    public void syncListFriends(List<FriendModel> mails, int listId, String owner, String timestamp, String listKey)
    {
        List<FriendModel> lastFriends = getFriendsWithoutNicknamesFromThisList(listId);
        List<Boolean> doesPositionExists = new ArrayList<>();

        for(int i=0;i<lastFriends.size();i++)
        {
            doesPositionExists.add(false);
            //Log.d("Last friends", lastFriends.get(i).getMail());
        }

        for(FriendModel m:mails)
        {
            //Log.d("new friends", m.getMail());
            boolean exists = false;
            for(int j=0;j<lastFriends.size();j++)
            {
                FriendModel friend = lastFriends.get(j);
                if(friend.getMail().equals(m.getMail()))
                {
                    exists = true;
                    doesPositionExists.set(j, true);
                    break;
                }
            }
            if(!exists)
            {
                m.setOwner(m.getMail().equals(owner));
                addFriendToList(m, listId);
            }

        }

        for(int i=0;i<lastFriends.size();i++)
        {
            if(!doesPositionExists.get(i))
            {
                //Log.d("Deleting friend from l", lastFriends.get(i).getMail());
                removeFriendFromList(lastFriends.get(i), listId);
            }

        }

        updateListFriendsTimestamp(listKey, timestamp);
    }

    public void syncList(List<ProductModel> products, int listId)
    {
        List<ProductModel> lastProducts = getAllProductOfList(listId);
        List<Boolean> doesPositionExists = new ArrayList<>();

        for(int i=0;i<lastProducts.size();i++)
        {
            doesPositionExists.add(false);
        }

        for(ProductModel p:products)
        {
            boolean exists = false;
            for(int j=0;j<lastProducts.size();j++)
            {
                ProductModel lp = lastProducts.get(j);
                if(p.getTittle().equals(lp.getTittle()))
                {
                    exists = true;
                    doesPositionExists.set(j, true);
                    if(!p.equals(lp))
                    {
                        p.setId(lp.getId());
                        updateProductWithPossibleSelectionChange(p, lp, listId);
                    }

                    break;
                }
            }
            if(!exists)
            {
                insertProduct(p, listId);
            }

        }

        for(int i=0;i<lastProducts.size();i++)
        {
            if(!doesPositionExists.get(i))
                deleteProduct(lastProducts.get(i), listId);
        }
    }


    public void syncGroup(List<ProductModel> products, int groupId, String tittle)
    {
        updateGroupTittle(groupId, tittle);
        List<ProductModel> lastProducts = getAllProductsFromGroup(groupId);
        List<Boolean> doesPositionExists = new ArrayList<>();

        for(int i=0;i<lastProducts.size();i++)
        {
            doesPositionExists.add(false);
        }

        for(ProductModel p:products)
        {
            boolean exists = false;
            for(int j=0;j<lastProducts.size();j++)
            {
                ProductModel lp = lastProducts.get(j);
                if(p.getTittle().equals(lp.getTittle()))
                {
                    exists = true;
                    doesPositionExists.set(j, true);
                    if(!p.equals(lp))
                    {
                        p.setId(lp.getId());
                        //updateProductWithPossibleSelectionChange(p, lp, listId);
                        updateProductInGroup(p);
                    }

                    break;
                }
            }
            if(!exists)
            {
                insertProductToGroup(p, groupId);
            }

        }

        for(int i=0;i<lastProducts.size();i++)
        {
            if(!doesPositionExists.get(i))
                deleteProductFromGroup(lastProducts.get(i));
        }
    }

    public Set<String> getListKeysSet()
    {
        Set<String> keys = new HashSet<>();
        String selectKeys = "SELECT L.list_firebase_key FROM Lists L";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectKeys, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                keys.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();


        return keys;
    }

    public Set<String> getGroupsKeysSet()
    {
        Set<String> keys = new HashSet<>();
        String selectKeys = "SELECT G.group_key FROM Groups G";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectKeys, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                keys.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();


        return keys;
    }

    public void updateListDownloadTimestamp(String listKey, String timestamp)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("download_timestamp", timestamp);

// updating row
        db.update("Lists", values, "list_firebase_key = ?",
                new String[]{listKey});

    }

    public boolean isListSaved(String listKey)
    {
        ArrayList<FriendModel>friends = new ArrayList<>();
        String selectFriends = "SELECT 1 FROM Lists L WHERE L.list_firebase_key LIKE " + listKey;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectFriends, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public void addFriend(FriendModel friend)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mail", friend.getMail());
        values.put("nickname", friend.getNickname());
        values.put("friend_uid", friend.getUid());
        db.insert("Friends", null, values);
    }


    public void deleteEverything()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM Lists");
        db.execSQL("DELETE FROM Products");
        db.execSQL("DELETE FROM Groups");
        db.execSQL("DELETE FROM GroupProducts");
        db.execSQL("DELETE FROM Friends");
        db.execSQL("DELETE FROM FriendsInLists");
        db.close();
    }


    public void deleteAllGroups()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM Groups");
        db.execSQL("DELETE FROM GroupProducts");
        db.close();
    }

    public void deleteAllLists()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM Lists");
        db.execSQL("DELETE FROM Products");
        db.execSQL("DELETE FROM FriendsInLists");
        db.close();
    }

    public void deleteAllFriends()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM Friends");
        db.execSQL("DELETE FROM FriendsInLists");
        db.close();
    }


    public void addFriendToList(FriendModel friend, int listId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("friend_mail", friend.getMail());
        values.put("list_id", listId);
        values.put("is_owner", friend.isOwner()+"");
        values.put("friend_uid", friend.getUid());

        db.insert("FriendsInLists", null, values);
        //Log.d("Friends", "Friend inserted");

    }


    public List<FriendModel> getAllFriends() {
        ArrayList<FriendModel> friends = new ArrayList<>();
        String selectFriends = "SELECT F.mail, F.nickname, F.friend_uid FROM Friends F ORDER BY F.mail ASC";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectFriends, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                FriendModel newFriend = new FriendModel(cursor.getString(1), cursor.getString(0), false, false);
                newFriend.setUid(cursor.getString(2));

                friends.add(newFriend);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return friends;
    }



    public List<FriendModel> getAllFriendsToShareActivity(int listId)
    {
        ArrayList<FriendModel>friends = new ArrayList<>();
        String selectFriends = "SELECT F.mail, F.nickname, F.friend_uid FROM Friends F ORDER BY F.mail ASC";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectFriends, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                FriendModel newFriend = new FriendModel(cursor.getString(1), cursor.getString(0), false, false);
                newFriend.setUid(cursor.getString(2));

                friends.add(newFriend);
            } while (cursor.moveToNext());
        }
        cursor.close();


        //Setting isOwner and inList
        List<FriendModel> friendsFromList = getFriendsWithoutNicknamesFromThisList(listId);//getFriendsFromThisList(listId);
        List<Boolean> isFriend = new ArrayList<>();
        for(int i =0;i<friendsFromList.size();i++)
        {
            isFriend.add(false);
        }


        for(int i=0;i<friends.size();i++)
        {
            FriendModel currentFriend = friends.get(i);
            for(int j=0;j<friendsFromList.size();j++)
            {
                FriendModel f = friendsFromList.get(j);
                if(f.getMail().equals(currentFriend.getMail()))
                {
                    //Log.d("Friends", "equals");
                    currentFriend.setInList(true);
                    currentFriend.setOwner(f.isOwner());
                    friends.set(i, currentFriend);

                    isFriend.set(j, true);
                    //Log.d("Friends", friends.get(0).isInList()+"");
                    break;
                }
            }
        }


        //Adding friends from list which are not our friends
        for(int i=0;i<friendsFromList.size();i++)
        {
            if(!isFriend.get(i))
                friends.add(friendsFromList.get(i));
        }

        //Log.d("Friends", friends.get(0).isInList()+"");

        return friends;

    }

    public List<FriendModel> getFriendsFromThisList(int listId)
    {
        ArrayList<FriendModel>friends = new ArrayList<>();
        String selectFriends = "SELECT F.mail, F.nickname, FL.is_owner, F.friend_uid FROM Friends F INNER JOIN FriendsInLists FL ON F.mail = FL.friend_mail WHERE FL.list_id = " + listId + " ORDER BY F.mail ASC";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectFriends, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                FriendModel newFriend = new FriendModel(cursor.getString(1), cursor.getString(0), true, Boolean.parseBoolean(cursor.getString(2)));
                //Log.d("mail", cursor.getString(1));
                newFriend.setUid(cursor.getString(3));
                friends.add(newFriend);
            } while (cursor.moveToNext());
        }
        cursor.close();


        return friends;
    }

    public boolean isThisUserYourFriend(String mail)
    {
        String selectFriends = "SELECT 1 FROM Friends F WHERE F.mail LIKE '" + mail + "'";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectFriends, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        }

        cursor.close();

        return false;
    }

    public List<FriendModel> getFriendsWithoutNicknamesFromThisList(int listId)
    {
        ArrayList<FriendModel>friends = new ArrayList<>();
        String selectFriends = "SELECT Fl.friend_mail, FL.is_owner, FL.friend_uid FROM FriendsInLists FL WHERE FL.list_id = " + listId + " ORDER BY Fl.friend_mail ASC";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectFriends, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                FriendModel newFriend = new FriendModel("", cursor.getString(0), true, Boolean.parseBoolean(cursor.getString(1)));
                //Log.d("mail", cursor.getString(0));
                newFriend.setUid(cursor.getString(2));

                friends.add(newFriend);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return friends;
    }



    public void removeFriendFromList(FriendModel friend, int listId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("FriendsInLists", "friend_mail = ? AND list_id = ?", new String[]{friend.getMail(), String.valueOf(listId)});
        db.close();

    }


    public void insertList(ListModel list, String createTimestamp)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tittle", list.getTittle());
        values.put("number_selected", list.getNumberSelected());
        values.put("number_all", list.getNumberAll());
        values.put("is_deleted", "false");
        values.put("list_firebase_key", list.getFirebaseKey());
        values.put("download_timestamp", list.getDownloadTimestamp());
        values.put("friends_timestamp", list.getFriendsTimestamp());
        values.put("am_i_owner", list.isAmIOwner()+"");
        values.put("create_timestamp", createTimestamp);

        db.insert("Lists", null, values);

        if(list.getProducts() != null)
        {
            int id = getIdOfLastList();
            for(ProductModel product: list.getProducts())
            {
                ContentValues values2 = new ContentValues();
                values2.put("list_id", id);
                values2.put("tittle", product.getTittle());
                values2.put("description", product.getDescription());
                values2.put("number", product.getNumber());
                values2.put("selected", product.isSelected()+"");
                values2.put("category_id", product.getCategoryId());
                db.insert("Products", null, values2);
            }
        }



        db.close();

    }


    public void copyList(ListModel list, Activity activity)
    {

        list.setNumberSelected(0);
        for(ProductModel p:list.getProducts())
            p.setSelected(false);


        if(!list.getTittle().endsWith(" " + activity.getString(R.string._copy)))
        {
            list.setTittle(list.getTittle() + " " + activity.getString(R.string._copy));
        }
        list.setDownloadTimestamp("0");
        list.setFriendsTimestamp("0");

        insertList(list, Calendar.getInstance().getTime().getTime()+"");
    }

    public void copyGroup(GroupModel group, Activity activity)
    {

        if(!group.getTittle().endsWith(" " + activity.getString(R.string._copy)))
        {
            group.setTittle(group.getTittle() + " " + activity.getString(R.string._copy));
        }

        insertGroup(group, Calendar.getInstance().getTime().getTime()+"");
    }

    public void insertProduct(ProductModel product, int listId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values2 = new ContentValues();
        values2.put("list_id", listId);
        values2.put("tittle", product.getTittle());
        values2.put("description", product.getDescription());
        values2.put("number", product.getNumber());
        values2.put("selected", product.isSelected()+"");
        values2.put("category_id", product.getCategoryId());
        db.insert("Products", null, values2);
        db.close();
        updateListNumberAll(listId, true);
        if(product.isSelected())
            updateListNumberSelected(listId, true);
    }

    public String getListTittle(String key)
    {
        ArrayList<ListModel>lists = new ArrayList<>();
        String selectLists = "SELECT L.tittle FROM Lists L WHERE L.list_firebase_key LIKE '" + key + "'";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectLists, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String tittle = cursor.getString(0);
                cursor.close();
                return tittle;
            } while (cursor.moveToNext());
        }

        cursor.close();
        return "";
    }


    public List<ListModel> getAllLists()
    {
        ArrayList<ListModel>lists = new ArrayList<>();
        String selectLists = "SELECT L.id, L.tittle, L.number_selected, L.number_all, L.list_firebase_key, L.download_timestamp, L.friends_timestamp, L.am_i_owner FROM Lists L WHERE L.is_deleted LIKE 'false' ORDER BY L.create_timestamp DESC, L.id DESC";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectLists, null);

// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ListModel newList = new ListModel(cursor.getString(1), cursor.getInt(2), cursor.getInt(3), new ArrayList<ProductModel>());
                newList.setId(cursor.getInt(0));
                newList.setFirebaseKey(cursor.getString(4));
                newList.setDownloadTimestamp(cursor.getString(5));
                newList.setFriendsTimestamp(cursor.getString(6));
                newList.setAmIOwner(Boolean.parseBoolean(cursor.getString(7)));

                lists.add(newList);
            } while (cursor.moveToNext());
        }

        cursor.close();

        Cursor cursor2 = db.rawQuery("SELECT P.list_id, P.tittle, P.description, P.selected, P.number, P.id, P.category_id FROM Products P", null);

        if(cursor2.moveToFirst())
        {
            do {
                int id = cursor2.getInt(0);
                ProductModel newProduct = new ProductModel(cursor2.getString(1), cursor2.getString(2), Boolean.valueOf(cursor2.getString(3)), cursor2.getInt(4));
                newProduct.setId(cursor2.getInt(5));
                newProduct.setCategoryId(cursor2.getInt(6));
                for(ListModel l:lists)
                {
                    if(l.getId() == id)
                    {
                        l.addProduct(newProduct);
                        break;
                    }
                }

            }while (cursor2.moveToNext());
        }
        cursor2.close();

        return lists;
    }

    public void setKeyOfList(int id, String key)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("list_firebase_key", key);

// updating row
        db.update("Lists", values, "id = ?",
                new String[]{String.valueOf(id)});
    }


    public void setKeyOfGroup(int id, String key)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("group_key", key);

// updating row
        db.update("Groups", values, "id = ?",
                new String[]{String.valueOf(id)});
    }

    public void updateProduct(ProductModel product)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tittle", product.getTittle());
        values.put("description", product.getDescription());
        values.put("selected", product.isSelected()+"");
        values.put("number", product.getNumber());
        values.put("category_id", product.getCategoryId());
// updating row
        db.update("Products", values, "id = ?",
                new String[]{String.valueOf(product.getId())});
        //Log.d("update", product.isSelected()+"");

    }

    public void updateProductWithPossibleSelectionChange(ProductModel newProduct, ProductModel lastProduct, int listId)
    {

        updateProduct(newProduct);
        if(newProduct.isSelected() != lastProduct.isSelected())
        {
            updateListNumberSelected(listId, newProduct.isSelected());

        }

    }



    public void deleteProduct(ProductModel product, int listId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Products", "id = ?", new String[]{String.valueOf(product.getId())});
        db.close();
        updateListNumberAll(listId, false);
        if(product.isSelected())
        {
            updateListNumberSelected(listId, false);
        }

    }

    public void deleteProductFromGroup(ProductModel product)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("GroupProducts", "id = ?", new String[]{String.valueOf(product.getId())});
        db.close();
        //updateListNumberAll(groupId, false);


    }

    public void deleteList(int listId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Lists", "id = ?", new String[]{String.valueOf(listId)});
        db.delete("Products", "list_id = ?", new String[]{String.valueOf(listId)});
        db.close();



    }

    public void moveGroupToTrash(int groupId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_deleted", "true");
        Date date = Calendar.getInstance().getTime();
        values.put("deleted_time_string", date.toString());

        db.update("Groups", values, "id = ?",
                new String[]{String.valueOf(groupId)});


    }

    public void moveListToTrash(int listId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_deleted", "true");
        Date date = Calendar.getInstance().getTime();
        values.put("deleted_time_string", date.toString());

        db.update("Lists", values, "id = ?",
                new String[]{String.valueOf(listId)});


    }

    public void restoreGroupFromTrash(int groupId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_deleted", "false");

        db.update("Groups", values, "id = ?",
                new String[]{String.valueOf(groupId)});
    }

    public void restoreListFromTrash(int listId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_deleted", "false");

        db.update("Lists", values, "id = ?",
                new String[]{String.valueOf(listId)});
    }

    public void deleteGroup(int groupId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Groups", "id = ?", new String[]{String.valueOf(groupId)});
        db.delete("GroupProducts", "group_id = ?", new String[]{String.valueOf(groupId)});
        db.close();



    }



    public void updateListNumberSelected(int id, boolean increase)
    {
        int l;
        if(increase)
            l = 1;
        else
            l = -1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("number_selected", getNumberSelectedOfList(id) + l);

        db.update("Lists", values, "id = ?",
                new String[]{String.valueOf(id)});
    }

    public void updateListNumberAll(int id, boolean increase)
    {
        int l;
        if(increase)
            l = 1;
        else
            l = -1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("number_all", getNumberSelectedAllOfList(id) + l);

        db.update("Lists", values, "id = ?",
                new String[]{String.valueOf(id)});
    }

    public int getNumberSelectedOfList(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor2 = db.rawQuery("SELECT L.number_selected FROM Lists L WHERE L.id = " + id, null);
        if(cursor2.moveToFirst())
        {
            int number = cursor2.getInt(0);
            cursor2.close();
            return number;
        }

        cursor2.close();
        return -1;
    }

    public int getNumberSelectedAllOfList(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor2 = db.rawQuery("SELECT L.number_all FROM Lists L WHERE L.id = " + id, null);
        if(cursor2.moveToFirst())
        {
            int number =  cursor2.getInt(0);
            cursor2.close();
            return number;
        }
        cursor2.close();
        return -1;
    }

    public void updateListTittle(int id, String newTittle)
    {


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tittle", newTittle);

        db.update("Lists", values, "id = ?",
                new String[]{String.valueOf(id)});
    }

    public long getGroupDownloadTimestamp(String key)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor2 = db.rawQuery("SELECT G.download_timestamp FROM Groups G WHERE G.group_key LIKE '" + key + "'", null);
        if(cursor2.moveToFirst())
        {
            int timestamp =  cursor2.getInt(0);
            cursor2.close();
            return timestamp;
        }
        cursor2.close();
        return -1;
    }

    public void increaseGroupTimestamp(String groupKey)
    {
        long last = getGroupDownloadTimestamp(groupKey);
        updateGroupTimestamp(groupKey, last+1);
    }

    public void updateGroupTimestamp(String key, long timestamp)
    {


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("download_timestamp", timestamp);

        db.update("Groups", values, "group_key = ?",
                new String[]{key});
    }

    public void updateGroupTittle(int id, String newTittle)
    {


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tittle", newTittle);

        db.update("Groups", values, "id = ?",
                new String[]{String.valueOf(id)});
    }



    public List<ProductModel> getAllProductOfList(int listId)
    {
        ArrayList<ProductModel>products = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor2 = db.rawQuery("SELECT P.list_id, P.tittle, P.description, P.selected, P.number, P.id, P.category_id FROM Products P WHERE P.list_id = " + listId + " ORDER BY P.selected ASC, P.category_id DESC, P.tittle ASC", null);
        if(cursor2.moveToFirst())
        {
            do {
                int id = cursor2.getInt(0);
                ProductModel newProduct = new ProductModel(cursor2.getString(1),
                        cursor2.getString(2), Boolean.parseBoolean(cursor2.getString(3)), cursor2.getInt(4));
                newProduct.setId(cursor2.getInt(5));
                newProduct.setCategoryId(cursor2.getInt(6));
                products.add(newProduct);

            }while (cursor2.moveToNext());
        }

        cursor2.close();

        return products;
    }

    public int getIdOfLastProduct()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT P.id FROM Products P ORDER BY P.id DESC LIMIT 1", null);
        if(cursor.moveToFirst())
        {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        else
        {
            cursor.close();
            return -1;
        }
    }



    public ArrayList<String> getAllTittlesOfProducts()
    {
        ArrayList<String>productTittles = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor2 = db.rawQuery("SELECT MIN(P.tittle) FROM Products P GROUP BY LOWER(P.tittle) ORDER BY LOWER(P.tittle) ASC", null);
        if(cursor2.moveToFirst())
        {
            do {
                productTittles.add(cursor2.getString(0));

            }while (cursor2.moveToNext());
        }

        cursor2.close();
        return productTittles;
    }

    public boolean isProductSaved(int productId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor2 = db.rawQuery("SELECT P.id FROM Products P WHERE P.id = " + productId, null);
        if(cursor2.moveToFirst())
        {
            return true;
        }
        else
            return false;

    }




    /*public int getListIdByTittle(String tittle)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT L.id FROM Lists L WHERE L.tittle LIKE '" + tittle + "'", null);
        if(cursor.moveToFirst())
        {
            return cursor.getInt(0);
        }
        else
        {
            return -1;
        }
    }*/

    public int getIdOfLastList()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT L.id FROM Lists L ORDER BY L.id DESC LIMIT 1", null);
        if(cursor.moveToFirst())
        {
            return cursor.getInt(0);
        }
        else
        {
            return -1;
        }
    }

    public int getListIdByKey(String key)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT L.id FROM Lists L WHERE L.list_firebase_key LIKE '" + key + "'", null);
        if(cursor.moveToFirst())
        {
            return cursor.getInt(0);
        }
        else
        {
            return -1;
        }
    }

    public int getGroupIdByKey(String key)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT G.id FROM Groups G WHERE G.group_key LIKE '" + key + "'", null);
        if(cursor.moveToFirst())
        {
            return cursor.getInt(0);
        }
        else
        {
            return -1;
        }
    }


    public void insertGroup(GroupModel group, String createTimestamp)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tittle", group.getTittle());
        values.put("is_deleted", "false");
        values.put("group_key", group.getKey());
        values.put("download_timestamp", group.getDownloadTimestamp());
        values.put("create_timestamp", createTimestamp);

        //values.put("number", group.getProducts().size());

        db.insert("Groups", null, values);

        if(group.getProducts() != null)
        {
            int id = getIdOfLastGroup();
            for(ProductModel product: group.getProducts())
            {
                ContentValues values2 = new ContentValues();
                values2.put("group_id", id);
                values2.put("tittle", product.getTittle());
                values2.put("description", product.getDescription());
                values2.put("number", product.getNumber());
                values2.put("category_id", product.getCategoryId());
                db.insert("GroupProducts", null, values2);
            }
        }



        db.close();

    }


    public int getIdOfLastGroup()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT G.id FROM Groups G ORDER BY G.id DESC", null);
        if(cursor.moveToFirst())
        {
            return cursor.getInt(0);
        }
        else
        {
            return -1;
        }
    }

    public ArrayList<GroupModel>getAllGroups()
    {
        ArrayList<GroupModel>groups = new ArrayList<>();
        String selectGroups = "SELECT G.id, G.tittle, G.group_key, G.download_timestamp FROM Groups G WHERE G.is_deleted LIKE 'false' ORDER BY G.create_timestamp DESC, G.id DESC";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectGroups, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                GroupModel newGroup = new GroupModel(cursor.getString(1));
                newGroup.setId(cursor.getInt(0));
                newGroup.setProducts(new ArrayList<>());
                newGroup.setKey(cursor.getString(2));
                newGroup.setDownloadTimestamp(cursor.getInt(3));

                groups.add(newGroup);
            } while (cursor.moveToNext());
        }


        Cursor cursor2 = db.rawQuery("SELECT P.group_id, P.tittle, P.description, P.number, P.id, P.category_id FROM GroupProducts P", null);
        if(cursor2.moveToFirst())
        {
            do {
                int groupId = cursor2.getInt(0);
                ProductModel newProduct = new ProductModel(cursor2.getString(1), cursor2.getString(2), false, cursor2.getInt(3));
                newProduct.setId(cursor2.getInt(4));
                newProduct.setCategoryId(cursor2.getInt(5));
                for(GroupModel g:groups)
                {
                    if(g.getId() == groupId)
                    {
                        g.addProduct(newProduct);
                        break;
                    }
                }

            }while (cursor2.moveToNext());
        }

        return groups;


    }

    public ArrayList<ProductModel> getAllProductsFromGroup(int groupId)
    {
        ArrayList<ProductModel>products = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor2 = db.rawQuery("SELECT P.tittle, P.description, P.number, P.id, P.category_id FROM GroupProducts P WHERE P.group_id = " + groupId + " ORDER BY P.category_id DESC, P.tittle ASC", null);
        if(cursor2.moveToFirst())
        {
            do {
                //int groupid = cursor2.getInt(0);
                ProductModel newProduct = new ProductModel(cursor2.getString(0),
                        cursor2.getString(1), false, cursor2.getInt(2));
                newProduct.setId(cursor2.getInt(3));
                newProduct.setCategoryId(cursor2.getInt(4));
                products.add(newProduct);

            }while (cursor2.moveToNext());
        }

        return products;
    }

    public void insertProductToGroup(ProductModel product, int groupId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tittle", product.getTittle());
        values.put("description", product.getDescription());
        values.put("number", product.getNumber());
        values.put("group_id", groupId);
        values.put("category_id", product.getCategoryId());
        //values.put("number", group.getProducts().size());

        db.insert("GroupProducts", null, values);
        db.close();
    }

    public void updateProductInGroup(ProductModel product)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tittle", product.getTittle());
        values.put("description", product.getDescription());
        values.put("number", product.getNumber());
        values.put("category_id", product.getCategoryId());
// updating row
        db.update("GroupProducts", values, "id = ?",
                new String[]{String.valueOf(product.getId())});
        //Log.d("update", product.isSelected()+"");

    }

    public List<Model>getListsAndGroupsFromTrash()
    {
        List<Model>listsAndGroups = new ArrayList<>();
        for(ListModel l:getListsFromTrash())
        {
            listsAndGroups.add(l);
        }

        for(GroupModel g:getGroupsFromTrash())
        {
            listsAndGroups.add(g);
        }

        //TODO: Sorting

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            listsAndGroups.sort(new Comparator<Model>() {
                @Override
                public int compare(Model o1, Model o2) {
                    if(o1.getTimeWhenDeleted() == o2.getTimeWhenDeleted())
                        return 0;
                    else if (o1.getTimeWhenDeleted() > o2.getTimeWhenDeleted())
                        return -1;
                    else
                        return 1;
                }
            });
        }

        return listsAndGroups;
    }

    public List<ListModel> getListsFromTrash()
    {
        ArrayList<ListModel>lists = new ArrayList<>();
        String selectLists = "SELECT L.id, L.tittle, L.number_selected, L.number_all, L.deleted_time_string, L.list_firebase_key FROM Lists L WHERE L.is_deleted LIKE 'true' ORDER BY L.id DESC";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectLists, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ListModel newList = new ListModel(cursor.getString(1), cursor.getInt(2), cursor.getInt(3), new ArrayList<ProductModel>());
                newList.setId(cursor.getInt(0));
                newList.setTimeWhenDeleted(Date.parse(cursor.getString(4)));
                newList.setFirebaseKey(cursor.getString(5));

                lists.add(newList);
            } while (cursor.moveToNext());
        }

        /*Cursor cursor2 = db.rawQuery("SELECT P.list_id, P.tittle, P.description, P.selected, P.number, P.id, P.category_id FROM Products P", null);
        if(cursor2.moveToFirst())
        {
            do {
                int id = cursor2.getInt(0);
                ProductModel newProduct = new ProductModel(cursor2.getString(1), cursor2.getString(2), Boolean.valueOf(cursor2.getString(3)), cursor2.getInt(4));
                newProduct.setId(cursor2.getInt(5));
                newProduct.setCategoryId(cursor2.getInt(6));
                for(ListModel l:lists)
                {
                    if(l.getId() == id)
                    {
                        l.addProduct(newProduct);
                        break;
                    }
                }

            }while (cursor2.moveToNext());
        }*/

        return lists;

    }


    public ArrayList<GroupModel>getGroupsFromTrash()
    {
        ArrayList<GroupModel>groups = new ArrayList<>();
        String selectGroups = "SELECT G.id, G.tittle, G.deleted_time_string, G.group_key FROM Groups G WHERE G.is_deleted LIKE 'true' ORDER BY G.id DESC";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectGroups, null);
// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                GroupModel newGroup = new GroupModel(cursor.getString(1));
                newGroup.setId(cursor.getInt(0));
                newGroup.setProducts(new ArrayList<>());
                newGroup.setTimeWhenDeleted(Date.parse(cursor.getString(2)));
                newGroup.setKey(cursor.getString(3));

                groups.add(newGroup);
            } while (cursor.moveToNext());
        }


        Cursor cursor2 = db.rawQuery("SELECT P.group_id, P.tittle, P.description, P.number, P.id, P.category_id FROM GroupProducts P", null);
        if(cursor2.moveToFirst())
        {
            do {
                int groupId = cursor2.getInt(0);
                ProductModel newProduct = new ProductModel(cursor2.getString(1), cursor2.getString(2), false, cursor2.getInt(3));
                newProduct.setId(cursor2.getInt(4));
                newProduct.setCategoryId(cursor2.getInt(5));
                for(GroupModel g:groups)
                {
                    if(g.getId() == groupId)
                    {
                        g.addProduct(newProduct);
                        break;
                    }
                }

            }while (cursor2.moveToNext());
        }

        return groups;


    }







}
