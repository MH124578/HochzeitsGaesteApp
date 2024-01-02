package com.example.hgapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.InputType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
    private NetworkService networkService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadButton = findViewById(R.id.uploadButton);
        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);
        btnSelectImage = findViewById(R.id.btnSelectImage);

        initializeNetworkService();

        btnSelectImage.setOnClickListener(v -> openImageChooser());
        uploadButton.setOnClickListener(v -> {
            Log.d("UploadActivity", "Upload button clicked");
            if (imageUri != null) {
                uploadImageToServer(imageUri, editText.getText().toString());
            } else {
                Log.d("UploadActivity", "Kein Bild ausgewählt");
            }
            imageView.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);
            uploadButton.setVisibility(View.GONE);
            btnSelectImage.setVisibility(View.VISIBLE);
        });
    }

    private void initializeNetworkService() {
        networkService = new NetworkService();
        fetchAndDisplayAllImages();
    }

    private void openImageChooser() {
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

    private void uploadImageToServer(Uri imageUri, String text) {
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

                int userId = 1;  // Stellen Sie sicher, dass Sie die richtige Benutzer-ID verwenden.
                Call<ResponseBody> call = networkService.getApiService().uploadImage(userId, body, textPart);

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

                            RecyclerView recyclerView = findViewById(R.id.imagesRecyclerView);
                            recyclerView.setVisibility(View.VISIBLE);

                            fetchAndDisplayAllImages();
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




    private void fetchAndDisplayAllImages() {
        new Thread(() -> {
            NetworkService.ApiService service = networkService.getApiService();
            Call<List<ImageData>> call = service.getAllImages();

            call.enqueue(new Callback<List<ImageData>>() {
                @Override
                public void onResponse(Call<List<ImageData>> call, Response<List<ImageData>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        runOnUiThread(() -> updateUIWithImages(response.body()));
                    }
                }

                @Override
                public void onFailure(Call<List<ImageData>> call, Throwable t) {
                    // Fehlerbehandlung
                }
            });
        }).start();
    }

    private void updateUIWithImages(List<ImageData> images) {
        RecyclerView recyclerView = findViewById(R.id.imagesRecyclerView);
        ImagesAdapter adapter = new ImagesAdapter(MainActivity.this, images);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }

    public void deleteImage(int entryId) {
        Call<ResponseBody> call = networkService.getApiService().deleteImage(entryId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    fetchAndDisplayAllImages();
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
                    fetchAndDisplayAllImages();
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

}