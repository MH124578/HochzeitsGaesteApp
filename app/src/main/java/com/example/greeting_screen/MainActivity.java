package com.example.greeting_screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;

import com.example.greeting_screen.model.User;
import com.example.greeting_screen.model.UserBase;
import com.example.greeting_screen.network.ApiService;
import com.example.greeting_screen.network.RetrofitClient;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    Boolean textFieldEmpty = Boolean.TRUE;
    private boolean isRequestInProgress = false;
    public static boolean shouldAutoRedirect = true;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_EMAIL = "saved_email";
    private static final String KEY_TEXT_ALIGNMENT = "text_alignment";

    // UI Elements
    private Button btnTransMenu;
    private EditText emailField;

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

        btnTransMenu = findViewById(R.id.continue_button);
        emailField = findViewById(R.id.email_input_field);

        // Restore the saved email
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedEmail = settings.getString(KEY_EMAIL, "");
        emailField.setText(savedEmail);

        // Restore text alignment preference
        boolean isTextAlignedLeft = settings.getBoolean(KEY_TEXT_ALIGNMENT, false);
        setTextFieldAlignment(emailField, isTextAlignedLeft);

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
                        setTextFieldAlignment(emailField, true); // Center-align when text is empty
                    } else {
                        Log.d("EmailTextField", "Text");
                        setTextFieldAlignment(emailField, false); // Left-align when text is not empty
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
                // Check if a request is already in progress
                if (isRequestInProgress) {
                    showToast("A request is already in progress");
                    return; // Do nothing if a request is still in progress
                }

                String email = emailField.getText().toString();

                // Set the flag to indicate that a request is in progress
                isRequestInProgress = true;

                // Disable the button while the request is in progress
                btnTransMenu.setEnabled(false);

                // Check if email exists in the database
                checkEmailExistence(email, new EmailExistenceCallback() {
                    @Override
                    public void onEmailExists() {
                        // Email exists, start MenuActivity
                        shouldAutoRedirect = true;
                        automaticRedirectToMenu();
                        onRequestComplete(); // Reset the flag and enable the button
                    }

                    @Override
                    public void onEmailDoesNotExist() {
                        // Email does not exist, register the email
                        registerEmail(email);

                        // Save the entered email
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(KEY_EMAIL, email);
                        editor.apply();

                        // Save text alignment preference
                        editor.putBoolean(KEY_TEXT_ALIGNMENT, false); // Left-align when button is clicked
                        editor.apply();

                        shouldAutoRedirect = true;
                        automaticRedirectToMenu();
                        onRequestComplete(); // Reset the flag and enable the button
                    }
                });
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

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldAutoRedirect) {
            // Check if email exists in the database
            String emailToCheck = emailField.getText().toString();
            checkEmailExistence(emailToCheck, new EmailExistenceCallback() {
                @Override
                public void onEmailExists() {
                    shouldAutoRedirect = true;
                    automaticRedirectToMenu();
                }

                @Override
                public void onEmailDoesNotExist() {
                    // Handle case where email does not exist
                }
            });
        }
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void checkEmailExistence(String email, EmailExistenceCallback callback) {
        ApiService apiService = RetrofitClient.getApiService();

        Call<Boolean> call = apiService.checkUserEmail(email);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    Boolean emailExists = response.body();
                    if (emailExists != null && emailExists) {
                        // Email exists
                        callback.onEmailExists();
                    } else {
                        // Email does not exist
                        callback.onEmailDoesNotExist();
                    }
                } else {
                    Log.d("Email Check", "Unsuccessful response. HTTP status code: " + response.code());
                    try {
                        Log.d("Email Check", "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.d("Email Check", "Failed to check email existence", t);
                showToast("Server currently not reachable");
                onRequestComplete(); // Reset the flag and enable the button
            }
        });
    }

    private void registerEmail(String email) {
        UserBase userBase = new UserBase(email);

        ApiService apiService = RetrofitClient.getApiService();

        Call<User> call = apiService.addUserEmail(userBase);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    shouldAutoRedirect = true;
                    Log.d("Email Registration", "Successful response");
                } else {
                    Log.d("Email Registration", "Unsuccessful response. HTTP status code: " + response.code());
                    try {
                        Log.d("Email Registration", "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                onRequestComplete(); // Reset the flag and enable the button
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("Email Registration", "Failed to register", t);
                showToast("Server currently not reachable");
                onRequestComplete(); // Reset the flag and enable the button
            }
        });
    }

    private void setTextFieldAlignment(EditText editText, boolean isCenterAligned) {
        if (isCenterAligned) {
            editText.setGravity(Gravity.CENTER);
        } else {
            editText.setGravity(Gravity.CENTER | Gravity.LEFT);
        }
    }

    private void automaticRedirectToMenu() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String storedEmail = settings.getString(KEY_EMAIL, "");

        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        intent.putExtra("userEmail", storedEmail);
        Log.d("email", storedEmail);
        startActivity(intent);
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void onRequestComplete() {
        // Reset the flag after the request is completed
        isRequestInProgress = false;

        // Enable the button again
        btnTransMenu.setEnabled(true);
    }

    interface EmailExistenceCallback {
        void onEmailExists();
        void onEmailDoesNotExist();
    }
}