package com.example.greeting_screen;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.greeting_screen.model.HomeInformationEntryCreate;
import com.example.greeting_screen.network.ApiService;
import com.example.greeting_screen.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEventDialogFragment extends DialogFragment {

    private ApiService apiService;

    public static AddEventDialogFragment newInstance() {
        return new AddEventDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_event, null);

        apiService = RetrofitClient.getApiService();

        EditText titleEditText = view.findViewById(R.id.event_title_inputfield);
        EditText descriptionEditText = view.findViewById(R.id.event_description_inputfield);
        EditText timeEditText = view.findViewById(R.id.event_time_inputfield);
        CheckBox eventTodayCheckBox = view.findViewById(R.id.event_day_checkbox);

        builder.setView(view)
                .setTitle("Add New Event")
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleEditText.getText().toString();
                    String description = descriptionEditText.getText().toString();
                    String time = timeEditText.getText().toString();
                    boolean eventToday = eventTodayCheckBox.isChecked();

                    HomeInformationEntryCreate entryCreate = new HomeInformationEntryCreate(title, description, time, eventToday);

                    postNewHomeEntry(entryCreate);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Handle negative button click (e.g., cancel the operation)
                    dismiss(); // Dismiss the dialog
                });

        return builder.create();
    }

    private void postNewHomeEntry(HomeInformationEntryCreate entryCreate) {
        // Make the API call using Retrofit
        Call<HomeInformationEntryCreate> call = apiService.postHomeInformationEntry(entryCreate, 5);

        call.enqueue(new Callback<HomeInformationEntryCreate>() {
            @Override
            public void onResponse(Call<HomeInformationEntryCreate> call, Response<HomeInformationEntryCreate> response) {
                if (response.isSuccessful()) {
                    // Handle successful response
                    // You may want to update your UI or perform other actions here
                } else {
                    // Handle error response
                    // You may want to show an error message or perform other actions here
                }
            }

            @Override
            public void onFailure(Call<HomeInformationEntryCreate> call, Throwable t) {
                // Handle failure (e.g., network error)
                // You may want to show an error message or perform other actions here
            }
        });
    }
}