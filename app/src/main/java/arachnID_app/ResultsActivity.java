package arachnID_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
    private RelativeLayout loadingLayout;

    // Sets the Activity's display and notifies user of image save location
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        if (getIntent().getStringExtra("IMG_FILE") != null) {
            filepath = getIntent().getStringExtra("IMG_FILE");
            // Text popup at bottom of screen
            Toast toast = Toast.makeText(getApplicationContext(),filepath, Toast.LENGTH_LONG);
            toast.show();
        } else {
            returnToMain(findViewById(R.id.backButton));
        }

        try {
            uploadImage(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Sets image display, sends POST request to server
    public void uploadImage(String path) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();
        File file = new File(path);
        String url = "https://www.arachnid.app/img_upload";
        MediaType MEDIA_TYPE_IMG = MediaType.parse("image/jpeg");

        // Set ImageView in Results Activity to the user's photo -- might eventually replace with generic image of the detected species
        ImageView classifiedImage = findViewById(R.id.classifiedImage);
        classifiedImage.setImageURI(Uri.fromFile(file));

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(file, MEDIA_TYPE_IMG))
                .build();

        // .enqueue() instead of .execute() -- allows for interruption and better error handling if necessary
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    response.close();
                    handleResponse(responseString);
                } else {
                    // System.out.println(request.body());
                    // System.out.println(response);
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.conn_noresponse, Toast.LENGTH_LONG);
                    toast.show();
                    response.close();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("onFailure called");
                Toast toast = Toast.makeText(getApplicationContext(), R.string.conn_failure, Toast.LENGTH_LONG);
                toast.show();
                returnToMain(findViewById(R.id.backButton));
            }
        });
    }

    // Populates display with parsed server response data
    public void handleResponse(String response) {
        String[] classificationInfo = response.split("\\|");
        // 0: name of class (species)
        // 1: venom status, 0 1 or 2
        // 2: more info on class

        runOnUiThread(() -> { // remove loading icon
            loadingLayout = findViewById(R.id.loadingLayout);
            loadingLayout.setVisibility(View.GONE);
        });

        TextView classification = findViewById(R.id.classification);
        classification.setText(classificationInfo[0]);

        venomStatus = findViewById(R.id.venomStatus);
        if (classificationInfo[1].equals("0")) {
            runOnUiThread(() -> { // only Main Activity can set UI fields dynamically, have to run all other activity changes on UiThread
                venomStatus.setText(R.string.venomLow);
                venomStatus.setBackgroundColor(android.graphics.Color.rgb(74, 176, 91)); // Green
            });
        } else if (classificationInfo[1].equals("1")) {
            runOnUiThread(() -> {
                venomStatus.setText(R.string.venomMed);
                venomStatus.setBackgroundColor(android.graphics.Color.rgb(229, 214, 126)); // Yellow
            });
        } else { // classificationInfo[1].equals("2")
            runOnUiThread(() -> {
                venomStatus.setText(R.string.venomHigh);
                venomStatus.setBackgroundColor(android.graphics.Color.rgb(255, 117, 112)); // Red
            });
        }

        TextView moreInfo = findViewById(R.id.moreInfo);
        moreInfo.setText(classificationInfo[2]);
    }

    // onClick method of back button, allows the user to return to the Main Activity (the initial 'page')
    public void returnToMain(View v) {
        Intent main = new Intent(this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clears Activity stack so that each use of the app doesn't build infinitely on top of itself
        startActivity(main);
    }

}
