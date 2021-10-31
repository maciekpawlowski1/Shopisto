package com.pawlowski.shopisto.database;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pawlowski.shopisto.models.FriendModel;
import com.pawlowski.shopisto.models.GroupModel;
import com.pawlowski.shopisto.models.ListModel;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;

public class OnlineDBHandler {

    public static void addFriend(FriendModel friend, String foundFriendUid)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child("users");
            Map<String, Object> value = new HashMap<>();
            value.put(user.getUid()+"/f/" + foundFriendUid + "/m", friend.getMail());
            value.put(foundFriendUid +"/f/" + user.getUid() + "/m", user.getEmail());
            usersReference.updateChildren(value);

            /*usersReference.child(user.getUid())
                    .child("f").child(foundFriendUid).child("m").setValue(friend.getMail());
            usersReference.child(foundFriendUid).child("f").child(user.getUid()).child("m").setValue(user.getEmail());*/
        }

    }



    public static void addFriendToList(FriendModel friend, String listKey, String listTittle)
    {
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();





        //Map<String, Object>map = new HashMap<>();
        //map.put(friend.getUid(), friend.getMail());
        //map.put("t", ServerValue.TIMESTAMP);

        /*Task<Void> usersTask = reference.child("lu").child(listKey).child(friend.getUid()).setValue(friend.getMail());
        Task<Void> fTask = reference.child("f").child(friend.getUid()).child(listKey).setValue(ServerValue.TIMESTAMP);
        Task<Void> dTask = reference.child("d").child(friend.getUid()).child(listKey).setValue(ServerValue.TIMESTAMP);

        Tasks.whenAll(usersTask, fTask, dTask).continueWith(new Continuation<Void, Object>() {

            @Override
            public Object then(@NonNull Task<Void> task) throws Exception {

                reference.child("e").child(friend.getUid()).child(listKey).setValue(listTittle);
                return null;
            }
        });*/

        Map<String, Object> map = new HashMap<>();
        map.put("lu/" + listKey + "/" + friend.getUid(), friend.getMail());
        map.put("f/" + friend.getUid() + "/" + listKey, ServerValue.TIMESTAMP);
        map.put("d/" + friend.getUid() + "/" + listKey, ServerValue.TIMESTAMP);
        map.put("e/" + friend.getUid() + "/" + listKey, listTittle);
        map.put("a/" + friend.getUid(), ServerValue.increment(1));
        FirebaseDatabase.getInstance().getReference().updateChildren(map);

    }


    public static String insertNewGroup(String tittle)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String key = reference.child("g").push().getKey();
        Map<String, Object> value = new HashMap<>();
        value.put("t", tittle);
        value.put("o", user.getUid());
        //value.put("a", 0);
        value.put("c", ServerValue.TIMESTAMP);

        Task<Void> addListTask = reference.child("g").child(key).setValue(value);






        //reference.child("lu").child(key).child("a").setValue(0);


        reference.child("t").child(user.getUid()).child(key).setValue(0);

        /*Tasks.whenAll(addListTask, addListUsersTask).continueWith(new Continuation<Void, Object>() {


            @Override
            public Object then(@NonNull Task<Void> task) throws Exception {


                return null;
            }
        });*/

        //reference.child("lu").child(key).child(user.getUid()).setValue(user.getEmail());
        //reference.child("lu").child(key).child("o").setValue(user.getEmail());
        return key;
    }

    /*public void addProductToGroup(ProductModel product, String groupKey)
    {
        //Date date = Calendar.getInstance().getTime();
        Map<String, Object> value = new HashMap<>();

        String productPath = "g/" + groupKey + "/" + "p/" + product.getTittle() + "/";

        if(product.getNumber() != 1)
            value.put(productPath + "n", product.getNumber());



        //if(product.getCategoryId() != 0)
        value.put(productPath + "c", product.getCategoryId());

        if(product.getDescription() != null && product.getDescription().length() > 0)
            value.put(productPath + "d", product.getDescription());

        //value.putAll(getMakeChangesMap(listKey, getUidsFromFriendList(friendsFromList)));

        FirebaseDatabase.getInstance().getReference().updateChildren(value);

    }*/

    public static Map<String, Object> getMakeChangesInGroupMap(String groupKey, String uid)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("t/" + uid + "/" + groupKey, ServerValue.increment(1));
        return map;
    }

    public static void deleteProductsInGroup(List<ProductModel>products, String groupKey)
    {
        if(products.size() == 0)
            return;


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> value = new HashMap<>();
        for(ProductModel p:products)
        {
            value.put("g/" + groupKey + "/p/" + p.getTittle(), null);
        }

        value.putAll(getMakeChangesInGroupMap(groupKey, uid));


        FirebaseDatabase.getInstance().getReference().updateChildren(value);
    }




    public static String getDeleteProductInGroupPath(String productTittle, String groupKey)
    {
        return ("g/" + groupKey + "/p/" + productTittle);
        //FirebaseDatabase.getInstance().getReference().child("g").child(groupKey).child("p").child(productTittle).removeValue();
    }

    public static void updateProductInGroup(ProductModel product, String groupKey, boolean tittleChange, String lastTittle)
    {
        //Date date = Calendar.getInstance().getTime();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> value = new HashMap<>();

        if(tittleChange)
        {
            value.put(getDeleteProductInGroupPath(lastTittle, groupKey), null);
        }



        String productPath = "g/" + groupKey + "/" + "p/" + product.getTittle() + "/";

        if(product.getNumber() != 1)
            value.put(productPath + "n", product.getNumber());
        else
            value.put(productPath + "n", null);



        //if(product.getCategoryId() != 0)
        value.put(productPath + "c", product.getCategoryId());

        if(product.getDescription() != null && product.getDescription().length() > 0)
            value.put(productPath + "d", product.getDescription());
        else
            value.put(productPath + "d", null);


        value.putAll(getMakeChangesInGroupMap(groupKey, uid));

        //value.putAll(getMakeChangesMap(listKey, getUidsFromFriendList(friendsFromList)));

        FirebaseDatabase.getInstance().getReference().updateChildren(value);
    }


    public static void addManyProductsInGroup(List<ProductModel> products, String groupKey)
    {
        //Date date = Calendar.getInstance().getTime();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> value = new HashMap<>();




        for(ProductModel product:products)
        {
            String productPath = "g/" + groupKey + "/" + "p/" + product.getTittle() + "/";

            if(product.getNumber() != 1)
                value.put(productPath + "n", product.getNumber());
            else
                value.put(productPath + "n", null);



            //if(product.getCategoryId() != 0)
            value.put(productPath + "c", product.getCategoryId());

            if(product.getDescription() != null && product.getDescription().length() > 0)
                value.put(productPath + "d", product.getDescription());
            else
                value.put(productPath + "d", null);
        }



        value.putAll(getMakeChangesInGroupMap(groupKey, uid));

        //value.putAll(getMakeChangesMap(listKey, getUidsFromFriendList(friendsFromList)));

        FirebaseDatabase.getInstance().getReference().updateChildren(value);
    }

    public interface ActionWhenSuccess
    {
        abstract void action();
    }

    public static void downloadAllFriendsAndSync(DBHandler dbHandler, List<FriendModel> lastFriends, ActionWhenSuccess actionWhenSuccess)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("users").child(user.getUid()).child("f").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    Object[] uids = map.keySet().toArray();
                    for(int i=0;i<uids.length;i++)
                    {
                        String friendMail = ((Map<String, String>)map.get(uids[i].toString())).get("m");
                        boolean exists = false;
                        for(FriendModel f:lastFriends)
                        {
                            if(f.getMail().equals(friendMail))
                            {
                                exists = true;
                                break;
                            }

                        }


                        if(!exists)
                        {
                            FriendModel friend = new FriendModel("", friendMail, false, false);
                            friend.setUid(uids[i].toString());
                            dbHandler.addFriend(friend);
                        }

                    }
                    if(uids.length > 0 && actionWhenSuccess != null)
                    {
                        actionWhenSuccess.action();
                    }
                }
            }
        });




    }

    /*public static Task downloadFriendsFromList1(String listKey)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("lu").child(listKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

    public static void removeFriendFromList(FriendModel friend, String listKey)
    {
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        /*reference.child("d").child(friend.getUid()).child(listKey).removeValue();

        reference.child("lu").child(listKey).child(friend.getUid()).removeValue();
        reference.child("f").child(friend.getUid()).child(listKey).removeValue();
        reference.child("e").child(friend.getUid()).child(listKey).removeValue();*/

        Map<String, Object> map = new HashMap<>();
        map.put("lu/" + listKey + "/" + friend.getUid(), null);
        map.put("f/" + friend.getUid() + "/" + listKey, null);
        map.put("d/" + friend.getUid() + "/" + listKey, null);
        map.put("e/" + friend.getUid() + "/" + listKey, null);
        map.put("a/" + friend.getUid(), ServerValue.increment(1));
        FirebaseDatabase.getInstance().getReference().updateChildren(map);



    }

    /*public static void makeChangesInList(String listKey, List<FriendModel> listFriends)
    {
        if(listFriends != null && listFriends.size() > 0)
        {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Date date = Calendar.getInstance().getTime();
            reference.child("l").child(listKey).child("a").setValue(date.getTime());
            reference = reference.child("d");

            for(FriendModel f:listFriends)
            {
                reference.child(f.getUid()).child(listKey).setValue(date.getTime());
            }


        }
    }

    public static Task<Void> makeChangesInList2(String listKey, List<String> listFriendsUids, Date date)
    {
        if(listFriendsUids != null && listFriendsUids.size() > 0)
        {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            //Date date = Calendar.getInstance().getTime();
            Map<String, Object>map = new HashMap<>();
            Map<String, Object>map2 = new HashMap<>();
            Map<String, Object>map3 = new HashMap<>();

            map.put("l/" + listKey + "/a", date.getTime());



            //reference.child("lu").child(listKey).child("a").setValue(date.getTime());
            //reference = reference.child("d");

            for(String f:listFriendsUids)
            {
                //reference.child(f).child(listKey).setValue(date.getTime());
                map.put("d/" + f + "/" + listKey, date.getTime());
            }
            return reference.updateChildren(map);


        }
        else
            return null;
    }*/

    public static Map<String, Object> getMakeChangesMap(String listKey, List<String> listFriendsUids)
    {
        Map<String, Object>map = new HashMap<>();
        Date date = Calendar.getInstance().getTime();
        map.put("l/" + listKey + "/a", date.getTime());

        if(listFriendsUids != null && listFriendsUids.size() > 0)
        {

            //reference.child("lu").child(listKey).child("a").setValue(date.getTime());
            //reference = reference.child("d");

            for(String f:listFriendsUids)
            {
                //reference.child(f).child(listKey).setValue(date.getTime());
                map.put("d/" + f + "/" + listKey, date.getTime());
            }
            return map;


        }
        else
            return map;
    }



    public static void makeChangesInListFriends(String listKey, List<FriendModel> listFriends)
    {
        if(listFriends != null && listFriends.size() > 0)
        {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("f");
            for(FriendModel f:listFriends)
            {
                reference.child(f.getUid()).child(listKey).setValue(ServerValue.TIMESTAMP);
            }


        }
    }


    public static String addNewList(String tittle)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String key = reference.child("l").push().getKey();
        Map<String, Object> value = new HashMap<>();
        value.put("t", tittle);
        value.put("s", 0);
        value.put("a", 0);
        value.put("c", ServerValue.TIMESTAMP);

        Task<Void> addListTask = reference.child("l").child(key).setValue(value);






        //reference.child("lu").child(key).child("a").setValue(0);

        Map<String, String>map = new HashMap<>();
        map.put(user.getUid(), user.getEmail());
        map.put("o", user.getEmail());
        map.put("a", "0");
        Task<Void> addListUsersTask = reference.child("lu").child(key).setValue(map);

        reference.child("a").child(user.getUid()).setValue(ServerValue.increment(1));
        reference.child("f").child(user.getUid()).child(key).setValue("0"); //Add the beginning 0, then by other users timestamp
        reference.child("d").child(user.getUid()).child(key).setValue("0"); //Add the beginning 0, then by other users timestamp
        reference.child("e").child(user.getUid()).child(key).setValue(tittle);

        /*Tasks.whenAll(addListTask, addListUsersTask).continueWith(new Continuation<Void, Object>() {


            @Override
            public Object then(@NonNull Task<Void> task) throws Exception {


                return null;
            }
        });*/

        //reference.child("lu").child(key).child(user.getUid()).setValue(user.getEmail());
        //reference.child("lu").child(key).child("o").setValue(user.getEmail());
        return key;
    }


    public static void saveAfterOfflineMode(DBHandler dbHandler)
    {
        List<ListModel> lists = dbHandler.getAllLists();
        for(ListModel l:lists)
        {
            dbHandler.setKeyOfList(l.getId(), copyList(l));
        }

        List<GroupModel>groups = dbHandler.getAllGroups();

        for(GroupModel g:groups)
        {
            dbHandler.setKeyOfGroup(g.getId(), insertGroupWithProducts(g.getTittle(), g.getProducts()));
        }


    }

    public static String copyList(ListModel list)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String key = reference.child("l").push().getKey();
        Map<String, Object> value = new HashMap<>();
        value.put("t", list.getTittle());
        value.put("s", 0);
        value.put("a", 0);
        value.put("c", ServerValue.TIMESTAMP);

        reference.child("l").child(key).setValue(value);






        //reference.child("lu").child(key).child("a").setValue(0);

        Map<String, String>map = new HashMap<>();
        map.put(user.getUid(), user.getEmail());
        map.put("o", user.getEmail());
        map.put("a", "0");
        reference.child("lu").child(key).setValue(map);

        if(list.getProducts() != null && list.getProducts().size() > 0)
            addManyProductsWithDescription(key, list.getProducts(), new ArrayList<>());


        reference.child("a").child(user.getUid()).setValue(ServerValue.increment(1));
        reference.child("f").child(user.getUid()).child(key).setValue("0"); //Add the beginning 0, then by other users timestamp
        reference.child("d").child(user.getUid()).child(key).setValue("0"); //Add the beginning 0, then by other users timestamp
        reference.child("e").child(user.getUid()).child(key).setValue(list.getTittle());


        //reference.child("lu").child(key).child(user.getUid()).setValue(user.getEmail());
        //reference.child("lu").child(key).child("o").setValue(user.getEmail());
        return key;
    }

    /*public void removeListIfYouAreOwner(String listKey, List<FriendModel> friendsInList)
    {
        for(FriendModel f:friendsInList)
        {
            removeFriendFromList(f, listKey);
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


        reference.child("f").child(user.getUid()).child(listKey).removeValue();
        reference.child("d").child(user.getUid()).child(listKey).removeValue();

        reference.child("p").child(listKey).removeValue();
        reference.child("l").child(listKey).removeValue();

    }*/

    public static void removeYourselfFromList(String listKey)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


        //reference.child("f").child(user.getUid()).child(listKey).removeValue();
        //reference.child("d").child(user.getUid()).child(listKey).removeValue();

        FriendModel me = new FriendModel("", user.getEmail(), false, false);
        me.setUid(user.getUid());
        removeFriendFromList(me, listKey);

    }


    public static void addProductWithoutDescription(String listKey, String tittle, int number, List<FriendModel>friendsFromList)
    {
        //Date date = Calendar.getInstance().getTime();
        Map<String, Object> value = new HashMap<>();
        String productPath = "p/" + listKey + "/" + tittle + "/";
        if(number != 1)
            value.put(productPath + "n", number);
        //else
            //value.put("n", null);

        value.put(productPath + "s", false);

        value.putAll(getMakeChangesMap(listKey, getUidsFromFriendList(friendsFromList)));

        //value.put("c", 0);
        FirebaseDatabase.getInstance().getReference().updateChildren(value);

                /*.child("p").child(listKey).child(tittle).setValue(value);*/

                /*.continueWith(new Continuation<Void, Object>() {


            @Override
            public Object then(@NonNull Task<Void> task) throws Exception {
                makeChangesInList2(listKey, getUidsFromFriendList(friendsFromList), date);
                return null;
            }
        });*/





    }

    public static void addProductWithDescription(String listKey, ProductModel product, List<FriendModel>friendsFromList)
    {
        Date date = Calendar.getInstance().getTime();
        Map<String, Object> value = new HashMap<>();

        String productPath = "p/" + listKey + "/" + product.getTittle() + "/";

        if(product.getNumber() != 1)
            value.put(productPath + "n", product.getNumber());

        value.put(productPath + "s", product.isSelected());

        if(product.getCategoryId() != 0)
            value.put(productPath + "c", product.getCategoryId());

        if(product.getDescription() != null && product.getDescription().length() > 0)
            value.put(productPath + "d", product.getDescription());

        value.putAll(getMakeChangesMap(listKey, getUidsFromFriendList(friendsFromList)));

        FirebaseDatabase.getInstance().getReference().updateChildren(value);

                /*.child("p")
                .child(listKey).child(product.getTittle()).setValue(value).continueWith(new Continuation<Void, Object>() {


            @Override
            public Object then(@NonNull Task<Void> task) throws Exception {
                makeChangesInList2(listKey, getUidsFromFriendList(friendsFromList), date);
                return null;
            }
        });*/





    }

    public static void addManyProductsWithDescription(String listKey, List<ProductModel> products, List<FriendModel>friendsFromList)
    {
        //Date date = Calendar.getInstance().getTime();
        Map<String, Object> value = new HashMap<>();
        String productsPath = "p/" + listKey + "/";
        for(ProductModel p:products)
        {
            if(p.getNumber() != 1)
                value.put(productsPath + p.getTittle() + "/n", p.getNumber());

            value.put(productsPath + p.getTittle() + "/s", p.isSelected());

            if(p.getCategoryId() != 0)
                value.put(productsPath + p.getTittle() + "/c", p.getCategoryId());


            if(p.getDescription() != null && p.getDescription().length() > 0)
                value.put(productsPath + p.getTittle() + "/d", p.getDescription());
        }


        value.putAll(getMakeChangesMap(listKey, getUidsFromFriendList(friendsFromList)));

        FirebaseDatabase.getInstance().getReference().updateChildren(value);

    /*.child("p")
                .child(listKey).updateChildren(value).continueWith(new Continuation<Void, Object>() {


            @Override
            public Object then(@NonNull Task<Void> task) throws Exception {
                makeChangesInList2(listKey, getUidsFromFriendList(friendsFromList), date);
                return null;
            }
        });*/





    }

    public static void setSelectionOfProduct(String listKey, ProductModel product, List<FriendModel> friendsFromList)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        String productPath = "p/" + listKey + "/" + product.getTittle() + "/";
        Map<String, Object> value = new HashMap<>();
        value.put(productPath + "s", product.isSelected());

        value.putAll(getMakeChangesMap(listKey, getUidsFromFriendList(friendsFromList)));

        //Date date = Calendar.getInstance().getTime();
        FirebaseDatabase.getInstance().getReference().updateChildren(value);

                //child("p")
                //.child(listKey).child(product.getTittle())
                //.child("s").setValue(product.isSelected())

                /*
                .continueWith(new Continuation<Void, Object>() {


            @Override
            public Object then(@NonNull Task<Void> task) throws Exception {
                makeChangesInList(listKey, friendsFromList);
                //for(FriendModel f:friendsFromList)
                    //Log.d("friendsFrimList", f.getMail());
                return null;
            }
        });*/
        //makeChangesInList2(listKey, getUidsFromFriendList(friendsFromList), date);








    }

    public static void setNumberOfProduct(String listKey, ProductModel product, List<FriendModel> friendsFromList)
    {

        Integer number = null;
        if(product.getNumber() != 1)
            number = product.getNumber();

        //Date date = Calendar.getInstance().getTime();

        Map<String, Object> value = new HashMap<>();
        String productPath = "p/" + listKey + "/" + product.getTittle() + "/";
        value.put(productPath + "n", number);

        value.putAll(getMakeChangesMap(listKey, getUidsFromFriendList(friendsFromList)));
        FirebaseDatabase.getInstance().getReference().updateChildren(value);

        /*FirebaseDatabase.getInstance().getReference().child("p")
                .child(listKey).child(product.getTittle())
                .child("n").setValue(number).continueWith(new Continuation<Void, Object>() {

            @Override
            public Object then(@NonNull Task<Void> task) throws Exception {
                makeChangesInList2(listKey, getUidsFromFriendList(friendsFromList), date);
                return null;
            }
        });*/


    }

    public static List<String> getUidsFromFriendList(List<FriendModel>friends)
    {
        List<String> uids = new ArrayList<>();
        for(FriendModel f:friends)
        {
            uids.add(f.getUid());
        }
        return uids;
    }


    public static void changeGroupTittle(String key, String newTittle)
    {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> value = new HashMap<>();
        value.put("g/" + key + "/t", newTittle);
        value.putAll(getMakeChangesInGroupMap(key, uid));
        FirebaseDatabase.getInstance().getReference().updateChildren(value);

        //child("g").child(key).child("t").setValue(newTittle);
    }

    public static String insertGroupWithProducts(String tittle, List<ProductModel>products)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String key = reference.child("g").push().getKey();
        Map<String, Object> value = new HashMap<>();
        value.put("t", tittle);
        value.put("o", user.getUid());
        //value.put("a", 0);
        value.put("c", ServerValue.TIMESTAMP);

        for(ProductModel p:products)
        {
            String path = "p/" + p.getTittle() + "/";
            value.put(path + "c", p.getCategoryId());
            if(p.getNumber() != 1)
                value.put(path + "n", p.getNumber());

            if(p.getDescription().length() != 0)
                value.put(path + "d", p.getDescription());
        }

        reference.child("g").child(key).updateChildren(value);


        reference.child("t").child(user.getUid()).child(key).setValue(0);


        return key;


    }


    public static void deleteGroup(String key)
    {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> value = new HashMap<>();
        value.put("g/" + key, null);
        value.put("t/" + uid + "/" + key, null);
        FirebaseDatabase.getInstance().getReference().updateChildren(value);
    }


    public static void changeListTittle(String key, String newTittle)
    {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> value = new HashMap<>();
        value.put("a/" + uid, ServerValue.increment(1));
        value.put("e/" + uid + "/" + key, newTittle);
        FirebaseDatabase.getInstance().getReference().updateChildren(value);//.child("e").child(uid).child(key).setValue(newTittle);
    }

    public static void updateProduct(String listKey, ProductModel product, boolean tittleChange, String previousTittle, List<FriendModel> friendsFromList)
    {
        //Date date = Calendar.getInstance().getTime();
        //List<String>uids = getUidsFromFriendList(friendsFromList);

        /*Task<Void> deleteTask = null;
        if(tittleChange)
        {
            deleteTask = deleteProduct(listKey, previousTittle);
        }*/

        Map<String, Object> value = new HashMap<>();
        String productPath = "p/" + listKey + "/" + product.getTittle() + "/";


        if(tittleChange)
            value.put("p/" + listKey + "/" + previousTittle, null);

        if(product.getNumber() != 1)
            value.put(productPath + "n", product.getNumber());
        else
            value.put(productPath + "n", null);

        value.put(productPath + "s", product.isSelected());

        if(product.getDescription() != null && product.getDescription().length() > 0)
            value.put(productPath + "d", product.getDescription());
        else
            value.put(productPath + "d", null);

        if(product.getCategoryId() != 0)
            value.put(productPath + "c", product.getCategoryId());
        else
            value.put(productPath + "c", null);


        value.putAll(getMakeChangesMap(listKey, getUidsFromFriendList(friendsFromList)));

        FirebaseDatabase.getInstance().getReference().updateChildren(value);
        /*Task<Void> updateTask = FirebaseDatabase.getInstance().getReference().child("p")
                .child(listKey).child(product.getTittle()).setValue(value);


        if(deleteTask != null)
        {
            Tasks.whenAll(deleteTask, updateTask).continueWith(new Continuation<Void, Object>() {

                @Override
                public Object then(@NonNull Task<Void> task) throws Exception {
                    makeChangesInList2(listKey, getUidsFromFriendList(friendsFromList), date);
                    return null;
                }
            });
        }
        else
        {
            Tasks.whenAll(updateTask).continueWith(new Continuation<Void, Object>() {

                @Override
                public Object then(@NonNull Task<Void> task) throws Exception {
                    makeChangesInList2(listKey, getUidsFromFriendList(friendsFromList), date);
                    return null;
                }
            });
        }*/



    }

    public static Task<Void> deleteProduct(String listKey, String tittle)
    {
        return FirebaseDatabase.getInstance().getReference().child("p")
                .child(listKey).child(tittle).removeValue();
    }

    public static void deleteProductWithNotifying(String listKey, String tittle, List<FriendModel>friendsInList)
    {
        //Date date = Calendar.getInstance().getTime();
        Map<String, Object> value = new HashMap<>();
        value.put("p/" + listKey + "/" + tittle, null);
        value.putAll(getMakeChangesMap(listKey, getUidsFromFriendList(friendsInList)));

        FirebaseDatabase.getInstance().getReference().updateChildren(value);
        /*deleteProduct(listKey, tittle).continueWith(new Continuation<Void, Object>() {

            @Override
            public Object then(@NonNull Task<Void> task) throws Exception {
                makeChangesInList2(listKey, getUidsFromFriendList(friendsInList), date);
                return null;
            }
        });*/
    }
    /*public void removeFriend(FriendModel friend)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid())
                    .child("friends").equalTo(friend.getMail()).getRef().removeValue();

            //reference.child(friend.getKey()).removeValue();

        }
    }*/

}
