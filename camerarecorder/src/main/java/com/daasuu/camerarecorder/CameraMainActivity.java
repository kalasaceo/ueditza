package com.daasuu.camerarecorder;

        import android.Manifest;
        import android.content.pm.PackageManager;
        import android.os.Build;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.widget.Toast;
public class CameraMainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 88888;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        PortrateActivity.startActivity(CameraMainActivity.this);
        /*setContentView(R.layout.activity_main);
        findViewById(R.id.portrate).setOnClickListener(v -> {
            PortrateActivity.startActivity(CameraMainActivity.this);
            finish();
        });
        findViewById(R.id.landscape).setOnClickListener(v -> {
            LandscapeActivity.startActivity(CameraMainActivity.this);
        });
        findViewById(R.id.square).setOnClickListener(v -> {
            SquareActivity.startActivity(CameraMainActivity.this);
        });*/
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        // request camera permission if it has not been grunted.
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(CameraMainActivity.this, "camera permission  granted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(CameraMainActivity.this, "[WARN] inablbe to grant camera permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


}
