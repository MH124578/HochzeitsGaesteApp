package com.example.greeting_screen;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.widget.ImageView;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        ImageView loading_symbols1 = findViewById(R.id.loading_symbols1);
        ImageView loading_symbols2 = findViewById(R.id.loading_symbols2);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        int scrollDuration = 7500; //Duration in ms

        ObjectAnimator symbols1Animator = ObjectAnimator.ofFloat(loading_symbols1, "translationX", 0f, screenWidth);
        symbols1Animator.setDuration(scrollDuration);
        symbols1Animator.setRepeatCount(ObjectAnimator.INFINITE);

        ObjectAnimator symbols2Animator = ObjectAnimator.ofFloat(loading_symbols2, "translationX", -screenWidth, 0f);
        symbols2Animator.setDuration(scrollDuration);
        symbols2Animator.setRepeatCount(ObjectAnimator.INFINITE);

        AnimatorSet loadingAnimation = new AnimatorSet();
        loadingAnimation.playTogether(symbols1Animator, symbols2Animator);
        loadingAnimation.setInterpolator(new android.view.animation.LinearInterpolator());

// Add a listener to handle repositioning when the animations complete
        loadingAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // Reposition loading_symbols1 to the right of loading_symbols2
                loading_symbols1.setTranslationX(screenWidth);
                // Reposition loading_symbols2 to the left of loading_symbols1
                loading_symbols2.setTranslationX(0f);
            }
        });

        loadingAnimation.start();
    }
}