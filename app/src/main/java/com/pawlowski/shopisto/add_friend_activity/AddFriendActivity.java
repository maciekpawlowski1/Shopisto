package com.pawlowski.shopisto.add_friend_activity;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.share_activity.ShareActivity;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.models.FriendModel;

import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;

public class AddFriendActivity extends BaseActivity implements AddFriendViewMvc.AddFriendButtonsClickListener {

    private int listId;
    private String listTittle;
    private String listKey;

    private String foundFriendUid;

    private AddFriendViewMvc viewMvc;

    @Inject
    DBHandler dbHandler;

    public static void launch(Context context, int listId, String listTittle, String listKey)
    {
        Intent i = new Intent(context, AddFriendActivity.class);
        i.putExtra("listId", listId);
        i.putExtra("listTittle", listTittle);
        i.putExtra("listKey", listKey);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresentationComponent().inject(this);
        viewMvc = getPresentationComponent().viewMvcFactory().newAddFriendViewMvcInstance(null);
        setContentView(viewMvc.getRootView());

        getSupportActionBar().setTitle(getString(R.string.find_friend));

        Bundle bundle = getIntent().getExtras();
        listId = bundle.getInt("listId");
        listTittle = bundle.getString("listTittle");
        listKey = bundle.getString("listKey");

        viewMvc.hideNotFoundImage();
        viewMvc.hideUserFound();

    }

    @Override
    protected void onStart() {
        super.onStart();
        viewMvc.registerListener(this);
        FirebaseDatabase.getInstance().goOffline();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewMvc.unregisterListener(this);
    }

    @Override
    public void onBackPressed() {
        ShareActivity.launch(this, listId, listTittle, listKey);
        finish();
    }

    public static boolean isMailValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isItYourMail(String mail)
    {
        if(mail.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
            return true;
        else
            return false;
    }

    @Override
    public void onSearchFriendButtonClick() {
        foundFriendUid = "";
        viewMvc.hideNotFoundImage();
        viewMvc.hideUserFound();
        String mail = viewMvc.getMailInputText();
        if(mail.length() > 3 && isMailValid(mail))
        {
            if(!isItYourMail(mail))
            {
                viewMvc.changeClickableOfSearchFriendButton(false);
                showProgressDialog(getString(R.string.please_wait));
                FirebaseDatabase.getInstance().goOnline();
                FirebaseDatabase.getInstance().getReference().child("users").orderByChild("mail").equalTo(mail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        viewMvc.changeClickableOfSearchFriendButton(true);
                        hideProgressDialog();
                        FirebaseDatabase.getInstance().goOffline();
                        if(snapshot.exists())
                        {

                            if(!dbHandler.isThisUserYourFriend(mail))
                            {
                                Map<String, Object> mapa = (Map<String, Object>) snapshot.getValue();
                                foundFriendUid = mapa.keySet().toArray()[0].toString();
                                //Log.d("user", foundFriendUid);
                                viewMvc.showUserFound(mail);
                            }
                            else
                                showErrorSnackbar(getString(R.string.user_already_your_friend), false);
                        }
                        else
                        {
                            viewMvc.showNotFoundImage();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hideProgressDialog();
                        showErrorSnackbar(getString(R.string.error_with_connection), true);
                        viewMvc.changeClickableOfSearchFriendButton(true);
                    }
                });
            }
            else
            {
                showErrorSnackbar(getString(R.string.you_cant_add_yourself_to_friend), true);
            }


        }
        else
        {
            showErrorSnackbar(getString(R.string.invalid_mail), true);
        }
    }

    @Override
    public void onAddFriendClick() {
        viewMvc.resetMailInput();
        viewMvc.hideUserFound();
        FriendModel friend = new FriendModel("", viewMvc.getMailInputText(), false, false);
        friend.setUid(foundFriendUid);
        dbHandler.addFriend(friend);
        //FirebaseDatabase.getInstance().goOnline();
        OnlineDBHandler.addFriend(friend, foundFriendUid);
        showErrorSnackbar(getString(R.string.friend_succesfully_added), false);
    }

    @Override
    public void onUserNotFoundTextClick() {
        ShareActivity.shareAction(listTittle, listId, AddFriendActivity.this);
    }
}