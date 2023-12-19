package com.example.hgapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageButton;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class MainActivity extends Activity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageView;
    private EditText editText;
    private Uri imageUri;
    private ImageButton btnSelectImage;
    private Button uploadButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fetchAndDisplayAllImages();
        uploadButton = findViewById(R.id.uploadButton);

        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);
        uploadButton = findViewById(R.id.uploadButton);
        btnSelectImage = findViewById(R.id.btnSelectImage);

        btnSelectImage.setOnClickListener(v -> openImageChooser());

        // Event-Listener für den Upload-Button
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

                ApiService service = getClient().create(ApiService.class);
                int userId = 1;  // Stellen Sie sicher, dass Sie die richtige Benutzer-ID verwenden.
                Call<ResponseBody> call = service.uploadImage(userId, body);

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

    private Retrofit getClient() {
        return new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public interface ApiService {
        @Multipart
        @POST("/upload_image/{user_id}")
        Call<ResponseBody> uploadImage(@Path("user_id") int userId, @Part MultipartBody.Part file);
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
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
    }

    public class ImageData {
        private int entryId;
        private String imageUrl;

        // Konstruktor
        public ImageData(int entryId, String imageUrl) {
            this.entryId = entryId;
            this.imageUrl = imageUrl;
        }

        // Getter und Setter
        public int getEntryId() {
            return entryId;
        }

        public void setEntryId(int entryId) {
            this.entryId = entryId;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }



    interface YourApiService {
        @GET("/all_images/")
        Call<List<ImageData>> getAllImages();
    }

    private void fetchAndDisplayAllImages() {
        Retrofit retrofit = getClient();
        YourApiService service = retrofit.create(YourApiService.class);
        Call<List<ImageData>> call = service.getAllImages();

        call.enqueue(new Callback<List<ImageData>>() {
            @Override
            public void onResponse(Call<List<ImageData>> call, Response<List<ImageData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ImageData> images = response.body();
                    RecyclerView recyclerView = findViewById(R.id.imagesRecyclerView);
                    ImagesAdapter adapter = new ImagesAdapter(MainActivity.this, images);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                }
            }

            @Override
            public void onFailure(Call<List<ImageData>> call, Throwable t) {
                // Fehlerbehandlung
            }
        });
    }


}