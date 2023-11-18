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

import java.io.IOException;
import java.io.InputStream;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);
        Button uploadButton = findViewById(R.id.uploadButton);

        imageView.setOnClickListener(v -> openImageChooser());
        uploadButton.setOnClickListener(v -> {
            Log.d("UploadActivity", "Upload button clicked");
            if (imageUri != null) {
                uploadImageToServer(imageUri, editText.getText().toString());
            } else {
                Log.d("UploadActivity", "Kein Bild ausgewählt");
            }
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
        }
    }

    private void uploadImageToServer(Uri imageUri, String text) {
        Log.d("UploadActivity", "Uploading image: " + imageUri.toString());
        InputStream inputStream = null;
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
                        Log.d("UploadActivity", "Upload successful: " + response.toString());
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String responseString = response.body().string();
                                int entryId = Integer.parseInt(responseString);  // Parse die ID aus der Antwort
                                fetchAndDisplayImage(entryId);  // Rufe die Methode auf, um das Bild anzuzeigen

                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } catch (IOException | NumberFormatException e) {
                                Log.e("UploadActivity", "Fehler beim Parsen der Antwort", e);
                            }
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

    interface YourApiService {
        @GET("/images/{entryId}")
        Call<ResponseBody> getImage(@Path("entryId") int entryId);
    }

    private void fetchAndDisplayImage(int entryId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        YourApiService service = retrofit.create(YourApiService.class);
        Call<ResponseBody> call = service.getImage(entryId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                    runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }
}
