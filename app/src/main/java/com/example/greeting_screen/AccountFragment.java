package com.example.greeting_screen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.greeting_screen.model.User;
import com.example.greeting_screen.model.UserBase;
import com.example.greeting_screen.network.ApiService;
import com.example.greeting_screen.network.RetrofitClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private ImageView topSpikes1;
    private ImageView topSpikes2;
    private ImageView bottomSpikes1;
    private ImageView bottomSpikes2;

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        topSpikes1 = view.findViewById(R.id.top_spikes_1);
        topSpikes2 = view.findViewById(R.id.top_spikes_2);
        bottomSpikes1 = view.findViewById(R.id.bottom_spikes_1);
        bottomSpikes2 = view.findViewById(R.id.bottom_spikes_2);

        EditText nameInputField = view.findViewById(R.id.nameInputField);
        EditText passwordInputField = view.findViewById(R.id.passwordInputField);

        Button registerButton = view.findViewById(R.id.register_data_button);
        Button logOffButton = view.findViewById(R.id.log_off_button);

        String userEmail = getArguments().getString("userEmail", "");

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = nameInputField.getText().toString();
                String password = passwordInputField.getText().toString();

                fillOutEmailUser(userEmail, name, password);
            }
        });


        logOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle log off button click
                navigateToMainActivity();
            }
        });

        startContinuousScrolling(bottomSpikes1, bottomSpikes2, true);
        startContinuousScrolling(topSpikes1, topSpikes2, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensure shouldAutoRedirect is set to false when returning to the AccountFragment
        MainActivity.shouldAutoRedirect = false;
    }

    private void navigateToMainActivity() {
        MainActivity.shouldAutoRedirect = false;

        requireActivity().onBackPressed();
    }

    private void fillOutEmailUser(String userEmail, String userName, String userPassword) {

        // Make the API call
        ApiService apiService = RetrofitClient.getApiService();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", userEmail);
        requestBody.put("name", userName);
        requestBody.put("password", userPassword);

        Call<User> call = apiService.fillOutEmailUser(requestBody);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    // Handle successful response
                    Log.d("FillOutEmailUser", "Filled out data");
                } else {
                    // Handle error response
                    Log.d("FillOutEmailUser", "Could not fill out data");
                    try {
                        Log.d("FillOutEmailUser", "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Handle network or other errors
                Log.d("FillOutEmailUser", "Network Error");
            }
        });
    }

    private void startContinuousScrolling(ImageView imgView_1, ImageView imgView_2, boolean scrollLeft) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        int animationSpeed = 8000;

        PropertyValuesHolder spikes1_pvhX;
        PropertyValuesHolder spikes2_pvhX;

        if (scrollLeft) {
            spikes1_pvhX = PropertyValuesHolder.ofFloat("translationX", 0f, -screenWidth);
            spikes2_pvhX = PropertyValuesHolder.ofFloat("translationX", screenWidth, 0f);
        } else {
            spikes1_pvhX = PropertyValuesHolder.ofFloat("translationX", 0f, screenWidth);
            spikes2_pvhX = PropertyValuesHolder.ofFloat("translationX", -screenWidth, 0f);
        }

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(imgView_1, spikes1_pvhX);
        animator.setDuration(animationSpeed);
        animator.setRepeatCount(ObjectAnimator.INFINITE);

        ObjectAnimator followAnimator = ObjectAnimator.ofPropertyValuesHolder(imgView_2, spikes2_pvhX);
        followAnimator.setDuration(animationSpeed);
        followAnimator.setRepeatCount(ObjectAnimator.INFINITE);

        AnimatorSet loadingAnimation = new AnimatorSet();
        loadingAnimation.playTogether(animator, followAnimator);
        loadingAnimation.setInterpolator(new android.view.animation.LinearInterpolator());

        loadingAnimation.start();
    }
}
