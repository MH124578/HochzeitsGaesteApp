package com.example.hgapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.InputType;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends Activity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageView;
    private EditText editText;
    private Uri imageUri;
    private ImageButton btnSelectImage;
    private Button uploadButton;
    private Button downloadButton;
    private Button createCategoryButton;
    private NetworkService networkService;
    private ImagesAdapter imagesAdapter;
    private RecyclerView recyclerView;
    private Spinner categorySpinner;
    private Spinner spinnerViewCategory;
    private List<Category> categoriesList;
    private ArrayAdapter<Category> categoriesAdapter;
    private int selectedViewCategoryId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadButton = findViewById(R.id.uploadButton);
        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        recyclerView = findViewById(R.id.imagesRecyclerView);
        categorySpinner = findViewById(R.id.categorySpinner);
        categoriesList = new ArrayList<>();
        spinnerViewCategory = findViewById(R.id.spinnerViewCategory);


        initializeNetworkService();
        fetchAndDisplayCategories();

        btnSelectImage.setOnClickListener(v -> openImageChooser());
        uploadButton.setOnClickListener(v -> {
            if (imageUri != null) {
                Category selectedCategory = (Category) categorySpinner.getSelectedItem();
                if (selectedCategory != null) {
                    uploadImageToServer(imageUri, editText.getText().toString(), selectedCategory.getId());
                } else {
                    Toast.makeText(MainActivity.this, "Bitte wählen Sie eine Kategorie aus.", Toast.LENGTH_SHORT).show();
                }
            }
            imageView.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);
            uploadButton.setVisibility(View.GONE);
            btnSelectImage.setVisibility(View.VISIBLE);
        });

        Button createCategoryButton = findViewById(R.id.createCategoryButton);
        createCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateCategoryDialog();
            }
        });

        Button downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(v -> downloadSelectedImages());
    }

    private void initializeNetworkService() {
        networkService = new NetworkService();
    }


    private void downloadSelectedImages() {
        ImagesAdapter currentAdapter = (ImagesAdapter) recyclerView.getAdapter();
        if (currentAdapter != null) {
            List<ImageData> images = currentAdapter.getImages();
            for (ImageData imageData : images) {
                if (imageData.isSelected()) {
                    downloadImage(imageData.getImageUrl(), imageData.getEntryId());
                }
            }
        }
    }

    private void downloadImage(String imageUrl, int imageId) {
        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_" + imageId + ".jpg";

                File storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File imageFile = new File(storagePath, imageFileName);

                FileOutputStream outputStream = new FileOutputStream(imageFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
                // Fehlerbehandlung hier
            }
        }).start();
    }

    private void openImageChooser() {
        downloadButton = findViewById(R.id.downloadButton);
        createCategoryButton = findViewById(R.id.createCategoryButton);

        downloadButton.setVisibility(View.GONE);
        createCategoryButton.setVisibility(View.GONE);
        spinnerViewCategory.setVisibility(View.GONE);
        categorySpinner.setVisibility(View.VISIBLE);

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
            imageView.setVisibility(View.VISIBLE);
            editText.setVisibility(View.VISIBLE);
            uploadButton.setVisibility(View.VISIBLE);

            btnSelectImage.setVisibility(View.GONE);

            RecyclerView recyclerView = findViewById(R.id.imagesRecyclerView);
            recyclerView.setVisibility(View.GONE);

        }
    }

    private void uploadImageToServer(Uri imageUri, String text, int categoryId) {
        Category selectedCategory = (Category) categorySpinner.getSelectedItem();
        int selectedCategoryId = selectedCategory != null ? selectedCategory.getId() : -1;
        Log.d("UploadActivity", "Uploading image: " + imageUri.toString());
        InputStream inputStream;
        try {
            inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                RequestBody requestFile = new StreamRequestBody(inputStream, MediaType.parse("multipart/form-data"));
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);
                RequestBody textPart = text.isEmpty()
                        ? RequestBody.create(MediaType.parse("multipart/form-data"), "")
                        : RequestBody.create(MediaType.parse("multipart/form-data"), text);
                RequestBody categoryIdPart = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(selectedCategoryId));

                int userId = 1;  // Stellen Sie sicher, dass Sie die richtige Benutzer-ID verwenden.
                Call<ResponseBody> call = networkService.getApiService().uploadImage(userId, body, textPart, categoryIdPart);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String responseString = response.body().string();
                                JSONObject jsonResponse = new JSONObject(responseString);
                                int entryId = jsonResponse.getInt("entry_id");
                            } catch (IOException | JSONException e) {
                                Log.e("UploadActivity", "Fehler beim Parsen der Antwort", e);
                            }
                            imageView.setVisibility(View.GONE);
                            downloadButton.setVisibility(View.VISIBLE);
                            createCategoryButton.setVisibility(View.VISIBLE);
                            spinnerViewCategory.setVisibility(View.VISIBLE);
                            categorySpinner.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                        Category selectedCategory = (Category) spinnerViewCategory.getSelectedItem();
                        if (selectedCategory != null) {
                            int categoryId = selectedCategory.getId();
                            fetchAndDisplayImagesByCategory(categoryId);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("UploadActivity", "Upload failed", t);
                    }
                });
            }
        } catch (IOException e) {
            Log.e("UploadActivity", "Fehler beim Öffnen des Bildstreams", e);
        }
    }



    public class StreamRequestBody extends RequestBody {
        private final InputStream inputStream;
        private final MediaType contentType;

        public StreamRequestBody(InputStream inputStream, MediaType contentType) {
            this.inputStream = inputStream;
            this.contentType = contentType;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            byte[] buffer = new byte[2048];
            int read;
            try {
                while ((read = inputStream.read(buffer)) != -1) {
                    sink.write(buffer, 0, read);
                }
            } finally {
                inputStream.close();
            }
        }
    }

    private void updateUIWithImages(List<ImageData> images) {
        if (recyclerView.getAdapter() == null) {
            // Adapter und LayoutManager initialisieren, wenn noch nicht geschehen
            imagesAdapter = new ImagesAdapter(MainActivity.this, images);
            recyclerView.setAdapter(imagesAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        } else {
            // Adapter bereits initialisiert, aktualisiere nur die Daten
            imagesAdapter.updateImages(images);
            imagesAdapter.notifyDataSetChanged();
        }
    }


    public void deleteImage(int entryId) {
        Call<ResponseBody> call = networkService.getApiService().deleteImage(entryId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    fetchAndDisplayImagesByCategory(selectedViewCategoryId);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fehlerbehandlung
            }
        });
    }

    public void editImageText(int entryId, String newText) {
        Call<ResponseBody> call = networkService.getApiService().editImageText(entryId, newText);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    fetchAndDisplayImagesByCategory(selectedViewCategoryId);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fehlerbehandlung
            }
        });
    }
    public void showEditDialog(final int entryId, String currentText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Text bearbeiten");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentText);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editImageText(entryId, input.getText().toString());
            }
        });
        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showCreateCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Neue Kategorie erstellen");

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input); // Kommentar hinzufügen

        builder.setPositiveButton("Erstellen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = input.getText().toString();
                createCategory(categoryName);
            }
        });
        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }


    private void createCategory(String categoryName) {
        NetworkService.ApiService apiService = new NetworkService().getApiService();
        Call<ResponseBody> call = apiService.createCategory(categoryName);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Kategorie erfolgreich erstellt
                    Log.e("KategorieErstellung", "Fehler beim Erstellen der Kategorie: " + response.code() + ", " + response.message());
                    Toast.makeText(MainActivity.this, "Kategorie erfolgreich erstellt.", Toast.LENGTH_SHORT).show();
                    fetchAndDisplayCategories();
                } else {
                    // Fehlerbehandlung
                    Toast.makeText(MainActivity.this, "Fehler beim Erstellen der Kategorie.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("KategorieErstellung", "Netzwerkfehler: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Netzwerkfehler: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndDisplayCategories() {
        Call<List<Category>> call = networkService.getApiService().getCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoriesList = response.body();
                    // Update the UI on the main thread
                    runOnUiThread(() -> {
                        ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                                MainActivity.this,
                                android.R.layout.simple_spinner_item,
                                categoriesList
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        categorySpinner.setAdapter(adapter);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerViewCategory.setAdapter(adapter);
                        spinnerViewCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                Category selectedCategory = (Category) parent.getItemAtPosition(position);
                                selectedViewCategoryId = selectedCategory.getId();
                                fetchAndDisplayImagesByCategory(selectedViewCategoryId);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                selectedViewCategoryId = -1;
                            }
                        });
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Fehler beim Abrufen der Kategorien.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Netzwerkfehler: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndDisplayImagesByCategory(int categoryId) {
        Call<List<ImageData>> call = networkService.getApiService().getImagesByCategory(categoryId);
        call.enqueue(new Callback<List<ImageData>>() {
            @Override
            public void onResponse(Call<List<ImageData>> call, Response<List<ImageData>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<ImageData> images = response.body();
                    Log.d("MainActivity", "Anzahl der Bilder in Kategorie " + categoryId + ": " + images.size());
                    updateUIWithImages(images);
                } else {
                    Log.e("MainActivity", "Keine Bilder in der Kategorie gefunden oder leere Antwort");
                    Toast.makeText(MainActivity.this, "Keine Bilder in dieser Kategorie gefunden.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ImageData>> call, Throwable t) {
                Log.e("MainActivity", "Fehler beim Abrufen der Bilder: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Fehler beim Laden der Bilder: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}