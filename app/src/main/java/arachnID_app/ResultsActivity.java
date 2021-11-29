package arachnID_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultsActivity extends AppCompatActivity {
    private String filepath;
    private TextView venomStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        if (getIntent().getStringExtra("IMG_FILE") != null) {
            filepath = getIntent().getStringExtra("IMG_FILE");
            Toast toast = Toast.makeText(getApplicationContext(),filepath, Toast.LENGTH_LONG);
            toast.show();
        } else {
            returnToMain(findViewById(R.id.backButton));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            uploadImage(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadImage(String path) throws IOException {
        ImageView classifiedImage = findViewById(R.id.classifiedImage);
        OkHttpClient client = new OkHttpClient();
        String url = "https://www.arachnid.app/img_upload";
        File file = new File(path);
        classifiedImage.setImageURI(Uri.fromFile(file));
        MediaType MEDIA_TYPE_IMG = MediaType.parse("image/jpeg");

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(file, MEDIA_TYPE_IMG))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    response.close();
                    handleResponse(responseString);
                } else {
                    System.out.println(request.body());
                    System.out.println(response);
                    response.close();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("onFailure called");
                returnToMain(findViewById(R.id.backButton));
            }
        });
    }

    public void handleResponse(String response) {
        // 0: name of class (species)
        // 1: venom status, 0 or 1
        // 2: more info on class
        String[] classificationInfo = response.split(",");

        TextView classification = findViewById(R.id.classification);
        classification.setText(classificationInfo[0]);

        venomStatus = findViewById(R.id.venomStatus);
        if (classificationInfo[1].equals("1")) {
            runOnUiThread(() -> {
                venomStatus.setText(R.string.venomTrue);
                venomStatus.setBackgroundColor(android.graphics.Color.rgb(255, 117, 112)); // Red
            });
        } else { // classificationInfo[1].equals("0")
            runOnUiThread(() -> {
                venomStatus.setText(R.string.venomFalse);
                venomStatus.setBackgroundColor(android.graphics.Color.rgb(74, 176, 91)); // Green
            });
        }

        TextView moreInfo = findViewById(R.id.moreInfo);
        moreInfo.setText(classificationInfo[2]);
    }

    public void returnToMain(View v) {
        Intent main = new Intent(this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(main);
    }

}
