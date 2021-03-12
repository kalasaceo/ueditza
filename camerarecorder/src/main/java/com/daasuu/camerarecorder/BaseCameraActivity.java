package com.daasuu.camerarecorder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.opengl.GLException;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.camerarecorder.widget.Filters;
import com.daasuu.camerarecorder.widget.SampleGLView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;
public class BaseCameraActivity extends AppCompatActivity {
    private SampleGLView sampleGLView;
    protected CameraRecorder cameraRecorder;
    private String filepath;
    protected LensFacing lensFacing = LensFacing.BACK;
    protected int cameraWidth = 1280;
    protected int cameraHeight = 720;
    protected int videoWidth = 720;
    protected int videoHeight = 720;
    public static String vidtextFileName="empty";
    public boolean bUpdate;
    int i=0;
    int j=0;
    int z=0;
    public static final int WRITE_REQUEST_CODE = 101;
    public Canvas tcanvas;
    private AlertDialog filterDialog;
    private boolean toggleClick = false;
    public ProgressBar progressBar;
    public int progressStatus = 0;
    public Handler handler = new Handler();
    @SuppressLint("Range")
    protected void onCreateActivity() {
        getSupportActionBar().hide();
        ImageButton trecordBtn = (ImageButton)findViewById(R.id.btn_trecord);
        findViewById(R.id.btn_flash).setOnClickListener(v -> {
            if (cameraRecorder != null && cameraRecorder.isFlashSupport()) {
                cameraRecorder.switchFlashMode();
                cameraRecorder.changeAutoFocus();
           }
        });
        trecordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(i==0) {
                    bUpdate=true;
                    trecordBtn.setScaleX((float)1);
                    trecordBtn.setScaleY((float)1);
                    filepath = getVideoFilePath();
                    cameraRecorder.start(filepath);
                    i++;
                    UpdateProgress();
                }
                else
                {
                    trecordBtn.setScaleX((float)0.6);
                    trecordBtn.setScaleY((float)0.6);
                    cameraRecorder.stop();
                    i=0;
                    bUpdate=false;
                }
            }
        });
        findViewById(R.id.EmojiButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(j%2==0) {
                    findViewById(R.id.bubble_view).setVisibility(View.VISIBLE);
                    j++;
                }
                else {
                    findViewById(R.id.bubble_view).setVisibility(View.GONE);
                    j++;
                }
            }
        });
        findViewById(R.id.AddtextButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(z%2==0) {
                    findViewById(R.id.video_text).setVisibility(View.VISIBLE);
                    z++;
                }
                else
                {
                    findViewById(R.id.video_text).setVisibility(View.GONE);
                    z++;
                }
            }
        });
        findViewById(R.id.btn_switch_camera).setOnClickListener(v -> {
            releaseCamera();
            if (lensFacing == LensFacing.BACK) {
                lensFacing = LensFacing.FRONT;
            } else {
                lensFacing = LensFacing.BACK;
            }
            toggleClick = true;
        });
        findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox OptionChoosen_1=(CheckBox) findViewById(R.id.check_option1);
                CheckBox OptionChoosen_2=(CheckBox) findViewById(R.id.check_option2);
                CheckBox OptionChoosen_3=(CheckBox) findViewById(R.id.check_option3);
                if(OptionChoosen_1.isChecked() && OptionChoosen_2.isChecked() && OptionChoosen_3.isChecked())
                {
                    Toast.makeText(BaseCameraActivity.this, "Kindly Choose an option", Toast.LENGTH_SHORT).show();
                }
                if(OptionChoosen_1.isChecked())
                {
                    findViewById(R.id.video_proceed).setVisibility(View.GONE);
                    progressStatus=0;
                    bUpdate=false;
                }
                if(OptionChoosen_2.isChecked())
                {
                    findViewById(R.id.video_proceed).setVisibility(View.GONE);
                    exportMp4ToGallery(getApplicationContext(), filepath);
                    EditText mEditText=(EditText)findViewById(R.id.video_text);
                    bUpdate=false;
                    writeInFile(mEditText.getText().toString());
                    finish();
                }
                if(OptionChoosen_3.isChecked())
                {
                    Toast.makeText(BaseCameraActivity.this, "Your story cannot be editted in standalone mode", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.bFilter1).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.INVERT);
            }
        });
        findViewById(R.id.bFilter2).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.BULGE_DISTORTION);
            }
        });
        findViewById(R.id.bFilter3).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.VIGNETTE);
            }
        });
        findViewById(R.id.bFilter4).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.BILATERAL);
            }
        });
        findViewById(R.id.bFilter5).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.BOX_BLUR);
            }
        });
        findViewById(R.id.bFilter6).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.FILTER_GROUP);
            }
        });
        findViewById(R.id.bFilter7).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.GAUSSIAN_BLUR);
            }
        }); findViewById(R.id.bFilter8).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.GLAY_SCALE);
            }
        });
        findViewById(R.id.bFilter9).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.MONOCHROME);
            }
        });
        findViewById(R.id.bFilter10).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.OVERLAY);
            }
        });
        findViewById(R.id.bFilter11).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.SEPIA);
            }
        }); findViewById(R.id.bFilter12).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.SHARPEN);
            }
        });
        findViewById(R.id.bFilter13).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.SPHERE_REFRACTION);
            }
        }); findViewById(R.id.bFilter14).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.TONE);
            }
        });
        findViewById(R.id.bFilter15).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.TONE_CURVE);
            }
        });
        findViewById(R.id.bFilter16).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.VIGNETTE);
            }
        });
        findViewById(R.id.bFilter17).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.WEAKPIXELINCLUSION);
            }
        });
        findViewById(R.id.bFilter17).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeFilter(Filters.CGA_COLOR_SPACE);
            }
        });
        findViewById(R.id.video_text).setBackgroundResource(R.drawable.edittext_bg);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    @Override
    protected void onResume() {
        super.onResume();
        setUpCamera();
    }
    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
    }
    private void releaseCamera() {
        if (sampleGLView != null) {
            sampleGLView.onPause();
        }
        if (cameraRecorder != null) {
            cameraRecorder.stop();
            cameraRecorder.release();
            cameraRecorder = null;
        }
        if (sampleGLView != null) {
            ((RelativeLayout) findViewById(R.id.relative_view)).removeView(sampleGLView);
            sampleGLView = null;
        }
    }
    public void FinishRecording()
    {
        cameraRecorder.stop();
        i=0;
        bUpdate=false;
    }
    private void writeInFile(@NonNull String text) {
        OutputStream outputStream;
        String textfile="/storage/emulated/0/Android/com.epep.notes/" + vidtextFileName + ".txt";
        try {
            outputStream = getContentResolver().openOutputStream(Uri.fromFile(new File(textfile)));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
            bw.write(text);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setUpCameraView() {
        runOnUiThread(() -> {
            RelativeLayout frameLayout = findViewById(R.id.relative_view);
            //frameLayout.removeAllViews();
            sampleGLView = null;
            sampleGLView = new SampleGLView(getApplicationContext());
            sampleGLView.setTouchListener((event, width, height) -> {
                if (cameraRecorder == null) return;
                cameraRecorder.changeManualFocusPoint(event.getX(), event.getY(), width, height);
            });
            frameLayout.addView(sampleGLView);
            //SimpleTextView tview = new SimpleTextView(this,sampleGLView);
            //frameLayout.addView(tview);

        });
    }
    private void setUpCamera() {
        setUpCameraView();

        cameraRecorder = new CameraRecorderBuilder(this, sampleGLView)
                //.recordNoFilter(true)
                .cameraRecordListener(new CameraRecordListener() {
                    @Override
                    public void onGetFlashSupport(boolean flashSupport) {
                        runOnUiThread(() -> {
                            findViewById(R.id.btn_flash).setEnabled(flashSupport);
                        });
                    }

                    @Override
                    public void onRecordComplete() {
                        findViewById(R.id.video_proceed).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onRecordStart() {

                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e("CameraRecorder", exception.toString());
                    }

                    @Override
                    public void onCameraThreadFinish() {
                        if (toggleClick) {
                            runOnUiThread(() -> {
                                setUpCamera();
                            });
                        }
                        toggleClick = false;
                    }
                })
                .videoSize(videoWidth, videoHeight)
                .cameraSize(cameraWidth, cameraHeight)
                .lensFacing(lensFacing)
                .build();


    }
    private void changeFilter(Filters filters) {
        cameraRecorder.setFilter(Filters.getFilterInstance(filters, getApplicationContext()));
    }
    public void UpdateProgress()
    {
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                        progressStatus += 1;
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        Thread.sleep(1800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private interface BitmapReadyCallbacks {
        void onBitmapReady(Bitmap bitmap);
    }

    private void captureBitmap(final BitmapReadyCallbacks bitmapReadyCallbacks) {
        sampleGLView.queueEvent(() -> {
            EGL10 egl = (EGL10) EGLContext.getEGL();
            GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
            Bitmap snapshotBitmap = createBitmapFromGLSurface(sampleGLView.getMeasuredWidth(), sampleGLView.getMeasuredHeight(), gl);

            runOnUiThread(() -> {
                bitmapReadyCallbacks.onBitmapReady(snapshotBitmap);
            });
        });
    }
    private Bitmap createBitmapFromGLSurface(int w, int h, GL10 gl) {
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);
        try {
            gl.glReadPixels(0, 0, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2, texturePixel, blue, red, pixel;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    texturePixel = bitmapBuffer[offset1 + j];
                    blue = (texturePixel >> 16) & 0xff;
                    red = (texturePixel << 16) & 0x00ff0000;
                    pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            Log.e("CreateBitmap", "createBitmapFromGLSurface: " + e.getMessage(), e);
            return null;
        }
        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }
    public void saveAsPngImage(Bitmap bitmap, String filePath) {
        try {
            File file = new File(filePath);
            FileOutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void exportMp4ToGallery(Context context, String filePath) {
        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, filePath);
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + filePath)));
    }
    public static String getVideoFilePath() {
        vidtextFileName=new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date());
        return getAndroidMoviesFolder().getAbsolutePath() + "/" +vidtextFileName+ "camerarecorder.mp4";
    }
    public static File getAndroidMoviesFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    }
    private static void exportPngToGallery(Context context, String filePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
    public static String getImageFilePath() {
        return getAndroidImageFolder().getAbsolutePath() + "/" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + "cameraRecorder.png";
    }
    public static File getAndroidImageFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }
}