package com.pawlowski.shopisto.splash_screen;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.account.login_activity.LoginActivity;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.main.MainActivity;

public class SplashActivity extends BaseActivity {

    private ImageView logoImage;
    private TextView appNameTextView;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        logoImage = findViewById(R.id.logo_image_splash);
        appNameTextView = findViewById(R.id.app_text_splash);



        countDownTimer = new CountDownTimer(2000, 2000)
        {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                Intent i;
                if(FirebaseAuth.getInstance().getCurrentUser() != null || isOfflineModeOn())
                {
                    i = new Intent(SplashActivity.this, MainActivity.class);

                }
                else
                {
                    i = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(i);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        };



    }

    @Override
    protected void onStart() {
        super.onStart();

        appNameTextView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash_animation));
        logoImage.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash_animation));

        countDownTimer.start();
    }

    @Override
    protected void onStop() {
        if(countDownTimer != null)
            countDownTimer.cancel();
        super.onStop();

    }
}