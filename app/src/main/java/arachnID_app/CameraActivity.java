package arachnID_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;

    // Sets the Activity's display and prompts camera access permission check
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        previewView = findViewById(R.id.previewView);

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100); // Request code arbitrary, unused elsewhere
        }

        setViewFinder();
    }

    // Requests a CameraProvider and creates a listener
    public void setViewFinder() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> { // lambda expression replacing Runnable()
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                viewFinder(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Attaches new image preview to display element, binds camera to Activity lifecycle
    protected void viewFinder(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        // COMPATIBLE ImplementationMode uses a TextureView for the preview; when using a SurfaceView (PERFORMANCE), app slows down tremendously
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);
    }

    // onClick method of Camera button, specifies a filepath and saves the captured image to it
    public void takePhoto(View v) {
        File path = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "img_" + System.currentTimeMillis() + ".jpeg");
        ImageCapture.OutputFileOptions imageCaptureOptions = new ImageCapture.OutputFileOptions.Builder(path).build();
        imageCapture.takePicture(imageCaptureOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                moveToResults(path);
            }
            @Override
            public void onError(ImageCaptureException error) {
                error.printStackTrace();
            }
        });
    }

    // Move to Results Activity
    public void moveToResults(File file) {
        Intent results = new Intent(this, ResultsActivity.class);
        results.putExtra("IMG_FILE", file.getPath());
        startActivity(results);
    }


}
