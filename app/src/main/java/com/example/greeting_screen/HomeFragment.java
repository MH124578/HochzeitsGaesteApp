package com.example.greeting_screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greeting_screen.model.HomeInformationEntry;
import com.example.greeting_screen.network.ApiService;
import com.example.greeting_screen.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private HomeInformationAdapter adapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the adapter here
        adapter = new HomeInformationAdapter(new ArrayList<>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);

        Button addEventButton = rootView.findViewById(R.id.add_event_button);
        addEventButton.setOnClickListener(view -> showAddEventDialog());

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // Set the adapter to RecyclerView
        recyclerView.setAdapter(adapter);

        // Make a network request to fetch home information entries
        fetchHomeInformationEntries();

        return rootView;
    }

    private void showAddEventDialog() {
        // Create an instance of the dialog fragment and show it
        AddEventDialogFragment dialogFragment = AddEventDialogFragment.newInstance();
        dialogFragment.show(getChildFragmentManager(), "AddEventDialogFragment");
    }

    private void fetchHomeInformationEntries() {
        ApiService apiService = RetrofitClient.getApiService();

        // Assuming you want to skip 0 entries and limit to 10, modify as needed
        Call<List<HomeInformationEntry>> call = apiService.getHomeInformationEntries(0, 100);
        call.enqueue(new Callback<List<HomeInformationEntry>>() {
            @Override
            public void onResponse(@NonNull Call<List<HomeInformationEntry>> call, @NonNull Response<List<HomeInformationEntry>> response) {
                if (response.isSuccessful()) {
                    // Update adapter data and notify changes
                    adapter.setData(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<HomeInformationEntry>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Network request failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}