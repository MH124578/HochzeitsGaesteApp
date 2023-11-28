package com.example.greeting_screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Boolean textFieldEmpty = Boolean.TRUE;

    public void AnimGSUnderline(View view) {

        float dpValue = 359f;
        float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (dpValue * scale + 0.5f);

        final View targetView = view;
        final int targetWidth = pixels;

        ValueAnimator anim = ValueAnimator.ofInt(view.getWidth(), targetWidth);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = targetView.getLayoutParams();
                params.width = animatedValue;
                targetView.setLayoutParams(params);
            }
        });
        anim.setDuration(2000);
        anim.start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTransMenu = (Button)findViewById(R.id.continue_button);
        EditText emailField = (EditText)findViewById(R.id.email_input_filed);
        String email = emailField.getText().toString();

        emailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Before Text changes
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = charSequence.toString().trim();
                boolean textEmptyNow = text.isEmpty();

                if (textFieldEmpty != textEmptyNow) {
                    textFieldEmpty = textEmptyNow;

                    if (textEmptyNow) {
                        Log.d("EmailTextField", "Kein Text");
                        emailField.setGravity(Gravity.CENTER);
                    } else {
                        Log.d("EmailTextField", "Text");
                        emailField.setGravity(Gravity.CENTER | Gravity.LEFT);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // After Text changes
            }
        });



        btnTransMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                /*intent.putExtra("entered_email", email);*/
                startActivity(intent);
            }
        });

        View underline_view = findViewById(R.id.greeting_screen_underline);
        TextView AGB_text_view = findViewById(R.id.AGB_textview);
        String AGB = (String) AGB_text_view.getText();
        SpannableString span_string = new SpannableString(AGB);

        ClickableSpan websiteTOS = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                openLink("https://www.google.com");
            }
        };

        ClickableSpan websitePP = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                openLink("https://giphy.com/clips/storyful-cat-animals-chess-95oDPmWBGhiZJZIGZZ");
            }
        };

        ClickableSpan websiteHint = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                openLink("https://www.linkedin.com/");
            }
        };

        span_string.setSpan(websiteTOS, 36, 39, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        span_string.setSpan(websitePP, 90, 112, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        span_string.setSpan(websiteHint, 127, 147, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        AGB_text_view.setText(span_string);

        AGB_text_view.setMovementMethod(LinkMovementMethod.getInstance());

        AnimGSUnderline(underline_view);
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}