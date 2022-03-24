package com.pawlowski.shopisto;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.models.FriendModel;

import java.util.Map;
import java.util.regex.Pattern;

public class AddFriendActivity extends BaseActivity {

    int listId;
    String listTittle;
    String listKey;

    TextInputEditText mailInput;
    FloatingActionButton searchFriendButton;

    TextView userFoundTextView;
    CardView userFoundCard;
    FloatingActionButton addUserButton;
    TextView userMailTextView;

    TextView userNotFoundTextView;
    ImageView userNotFoundImage;

    String foundFriendUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        mailInput = findViewById(R.id.mail_input_add_friend);
        searchFriendButton = findViewById(R.id.search_button_add_friend);

        userFoundTextView = findViewById(R.id.user_found_text_add_friend);
        userFoundCard = findViewById(R.id.user_found_card_view_add_friend);
        addUserButton = findViewById(R.id.add_friend_button_add_friend);
        userMailTextView = findViewById(R.id.mail_text_add_friend);

        userNotFoundTextView = findViewById(R.id.not_found_text_add_friend);
        userNotFoundImage = findViewById(R.id.not_found_image_add_friend);



        getSupportActionBar().setTitle(getString(R.string.find_friend));

        Bundle bundle = getIntent().getExtras();
        listId = bundle.getInt("listId");
        listTittle = bundle.getString("listTittle");
        listKey = bundle.getString("listKey");

        hideNotFoundImage();
        hideUserFound();

        searchFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foundFriendUid = "";
                hideNotFoundImage();
                hideUserFound();
                String mail = mailInput.getText().toString();
                if(mail.length() > 3 && isMailValid(mail))
                {
                    if(!isItYourMail(mail))
                    {
                        searchFriendButton.setClickable(false);
                        showProgressDialog(getString(R.string.please_wait));
                        FirebaseDatabase.getInstance().goOnline();
                        FirebaseDatabase.getInstance().getReference().child("users").orderByChild("mail").equalTo(mail).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                searchFriendButton.setClickable(true);
                                hideProgressDialog();
                                FirebaseDatabase.getInstance().goOffline();
                                if(snapshot.exists())
                                {

                                    if(!DBHandler.getInstance(getApplicationContext()).isThisUserYourFriend(mail))
                                    {
                                        Map<String, Object> mapa = (Map<String, Object>) snapshot.getValue();
                                        foundFriendUid = mapa.keySet().toArray()[0].toString();
                                        //Log.d("user", foundFriendUid);
                                        showUserFound(mail);
                                    }
                                    else
                                        showErrorSnackbar(getString(R.string.user_already_your_friend), false);
                                }
                                else
                                {
                                    showNotFoundImage();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                hideProgressDialog();
                                showErrorSnackbar(getString(R.string.error_with_connection), true);
                                searchFriendButton.setClickable(true);
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
        });

        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailInput.setText("");
                hideUserFound();
                FriendModel friend = new FriendModel("", userMailTextView.getText().toString(), false, false);
                friend.setUid(foundFriendUid);
                DBHandler.getInstance(getApplicationContext()).addFriend(friend);
                //FirebaseDatabase.getInstance().goOnline();
                OnlineDBHandler.addFriend(friend, foundFriendUid);
                showErrorSnackbar(getString(R.string.friend_succesfully_added), false);
            }
        });


        userNotFoundTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareActivity.shareAction(listTittle, listId, AddFriendActivity.this);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().goOffline();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(AddFriendActivity.this, ShareActivity.class);
        i.putExtra("listId", listId);
        i.putExtra("listTittle", listTittle);
        i.putExtra("listKey", listKey);
        startActivity(i);
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

    public void showNotFoundImage()
    {
        userNotFoundTextView.setVisibility(View.VISIBLE);
        userNotFoundImage.setVisibility(View.VISIBLE);
    }

    public void hideNotFoundImage()
    {
        userNotFoundTextView.setVisibility(View.GONE);
        userNotFoundImage.setVisibility(View.GONE);
    }

    public void showUserFound(String mail)
    {

        userFoundCard.setVisibility(View.VISIBLE);
        userMailTextView.setText(mail);
        userFoundTextView.setVisibility(View.VISIBLE);
    }

    public void hideUserFound()
    {
        userFoundCard.setVisibility(View.GONE);
        userFoundTextView.setVisibility(View.GONE);

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
}