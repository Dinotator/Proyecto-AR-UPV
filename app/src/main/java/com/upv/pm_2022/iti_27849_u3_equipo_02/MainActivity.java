package com.upv.pm_2022.iti_27849_u3_equipo_02;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.QRCodeDetector;
import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";
    Context CX = this;

    private Mat mRgba;
    private ResultPoint[] points;
    private CameraBridgeViewBase mOpenCvCameraView;
    int evaluar = 0;

    private GLSurfaceView GLSurfaceViewEdificioA,GLSurfaceViewEdificioB,GLSurfaceViewEdificioH,GLSurfaceViewEdificioI, GLSurfaceViewEdificioNuevo, GLSurfaceViewCafeteria;
    
    private MyRendererLight MyRendererLightEdificioA, MyRendererLightEdificioB, MyRendererLightEdificioH, MyRendererLightEdificioI, MyRendererLightEdificioNuevo, MyRendererLightCafeteria;

    private static final int REQUEST_PERMISSION_CAMERA = 100;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 101;
    private static final int REQUEST_PERMISSION_INTERNET = 102;
    private static final int REQUEST_PERMISSION_ACCESS_NETWORK_STATE = 103;
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 104;
    /**
     * Called when the activity is first created.
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        askPermissionOnly();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        AsyncTask task = new ProgressTask(MainActivity.this).execute();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int) event.getX() - xOffset;
        int y = (int) event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        return false;
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        Bitmap bMapR = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba, bMapR);
        int cols = mRgba.cols();
        int rows = mRgba.rows();
        if (evaluar == 0) {
            return mRgba;
        }
        try {
            String qrDecodificado = decodeQRCode(bMapR);
            String valorQR = qrDecodificado;
            if (!qrDecodificado.equals("QR Not found")) {
                valorQR = qrDecodificado;
            }
            int ejeX = 0;
            int ejeY = 0;
            int tamanioX = 0;
            int tamanioY = 0;
            if (!valorQR.equals("QR Not found")) {
                double distanciaY = Math.sqrt(Math.pow(points[0].getX() - points[1].getX(), 2) + Math.pow(points[0].getY() - points[1].getY(), 2));
                double distanciaX = Math.sqrt(Math.pow(points[2].getX() - points[1].getX(), 2) + Math.pow(points[2].getY() - points[1].getY(), 2));
                double punto1X = (points[0].getX() + points[2].getX()) / 2;
                double punto1Y = (points[0].getY() + points[2].getY()) / 2;

                double resta = punto1X - (cols / 2);
                ejeX = (int) (Math.ceil(resta / (cols / 42)));
                resta = punto1Y - (rows / 2);
                ejeY = (int) (Math.ceil(resta / (rows / 42)));
                tamanioX = (int) (Math.ceil(distanciaX / (cols / 42)));
                tamanioY = (int) (Math.ceil(distanciaY / (rows / 42)));

                if (ejeY > 6) {
                    ejeY -= 4;
                }
                if (ejeY < -6) {
                    ejeY += 4;
                }
            }
            final int ejeXE = ejeX;
            final int ejeYE = ejeY;
            final int tamanioXE = tamanioX;
            final int tamanioYE = tamanioY;
            if (valorQR.equals("Edificio A")) {
                runOnUiThread(() -> {
                    GLSurfaceViewEdificioA.setVisibility(View.VISIBLE);
                    MyRendererLightEdificioA.angulo(ejeXE, ejeYE, tamanioXE, tamanioYE);
                });
            } else if (valorQR.equals("Edificio B")) {
                runOnUiThread(() -> {
                    GLSurfaceViewEdificioB.setVisibility(View.VISIBLE);
                    MyRendererLightEdificioB.angulo(ejeXE, ejeYE, tamanioXE, tamanioYE);
                });
            } else if (valorQR.equals("Edificio H")) {
                runOnUiThread(() -> {
                    GLSurfaceViewEdificioH.setVisibility(View.VISIBLE);
                    MyRendererLightEdificioH.angulo(ejeXE, ejeYE, tamanioXE, tamanioYE);
                });
            } else if (valorQR.equals("Edificio I")) {
                runOnUiThread(() -> {
                    GLSurfaceViewEdificioI.setVisibility(View.VISIBLE);
                    MyRendererLightEdificioI.angulo(ejeXE, ejeYE, tamanioXE, tamanioYE);
                });
            } else if (valorQR.equals("Edificio Nuevo")) {
                runOnUiThread(() -> {
                    GLSurfaceViewEdificioNuevo.setVisibility(View.VISIBLE);
                    MyRendererLightEdificioNuevo.angulo(ejeXE, ejeYE, tamanioXE, tamanioYE);
                });
            } else if (valorQR.equals("Cafeteria")) {
                runOnUiThread(() -> {
                    GLSurfaceViewCafeteria.setVisibility(View.VISIBLE);
                    MyRendererLightCafeteria.angulo(ejeXE, ejeYE, tamanioXE, tamanioYE);
                });
            }
            if (valorQR.equals("QR Not found")) {
                runOnUiThread(() -> {
                    GLSurfaceViewEdificioA.setVisibility(View.GONE);
                    GLSurfaceViewEdificioB.setVisibility(View.GONE);
                    GLSurfaceViewEdificioH.setVisibility(View.GONE);
                    GLSurfaceViewEdificioI.setVisibility(View.GONE);
                    GLSurfaceViewEdificioNuevo.setVisibility(View.GONE);
                    GLSurfaceViewCafeteria.setVisibility(View.GONE);
                });
            }
        } catch (Exception e) {
            Log.d("Ayuda", "Error1" + e.getMessage() + e.getCause() + e.getLocalizedMessage());
        }
        return mRgba;

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "cancelado", Toast.LENGTH_SHORT).show();
            } else {

            }
        }
    }

    public String decodeQRCode(Bitmap bMap) {
        int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            Result result = new MultiFormatReader().decode(bitmap);
            points = result.getResultPoints();
            return result.getText();
        } catch (Exception e) {
            return "QR Not found";
        }
    }


    private void askPermissionOnly() {
        this.askPermission(REQUEST_PERMISSION_CAMERA, Manifest.permission.CAMERA);
        this.askPermission(REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        this.askPermission(REQUEST_PERMISSION_INTERNET, Manifest.permission.INTERNET);
        this.askPermission(REQUEST_PERMISSION_ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_NETWORK_STATE);
        this.askPermission(REQUEST_PERMISSION_READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    // With Android Level >= 23, you have to ask the user
    // for permission with device (For example read/write data on the device).
    private boolean askPermission(int requestId, String permissionName) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            // Check if we have permission
            int permission = ActivityCompat.checkSelfPermission(this, permissionName);


            if (permission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{permissionName},
                        requestId
                );
                return false;
            }
        }
        return true;
    }

    // When you have the request results
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        // Note: If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0) {
            switch (requestCode) {
                case REQUEST_PERMISSION_ACCESS_NETWORK_STATE:
                case REQUEST_PERMISSION_CAMERA:
                case REQUEST_PERMISSION_INTERNET:
                case REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
                case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "Permission Lectura Concedido!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;

            }
        } else {
        }
    }

    public class ProgressTask extends AsyncTask<String, Void, Boolean> {

        public ProgressTask(MainActivity activity) {
            this.activity = activity;
            this.dialog = new ProgressDialog(activity);
        }

        /**
         * progress dialog to show user that the backup is processing.
         */
        private ProgressDialog dialog;
        /**
         * application context.
         */
        private MainActivity activity;


        @Override
        protected void onPostExecute(final Boolean success) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(CX);
            if (success) {
                builder.setMessage("La aplicación ha iniciado correctamente" +
                        "\nDesarrollado por:" +
                        "\nMelisa Marisol Charles Charles" +
                        "\nMaría Fernanda Coronado Alejos" +
                        "\nAna Karen Echartea Juárez" +
                        "\nJulio Antonio Tovar García" +
                        "\nAnnel Fernanda Uresti Barrón");

            } else {
                builder.setMessage("Dispositivo no soportado");
            }
            builder.setCancelable(false);
            builder.setPositiveButton("CONFIRMAR", (dialog, id) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
            evaluar = 1;

        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {

                if ((android.os.Build.VERSION.SDK_INT >= 27)) {
                    MyRendererLightEdificioA = new MyRendererLight(CX, 1);
                    MyRendererLightEdificioB = new MyRendererLight(CX, 2);
                    MyRendererLightEdificioH = new MyRendererLight(CX, 3);
                    MyRendererLightEdificioI = new MyRendererLight(CX, 4);
                    MyRendererLightEdificioNuevo = new MyRendererLight(CX, 5);
                    MyRendererLightCafeteria = new MyRendererLight(CX, 6);
                }

                GLSurfaceViewEdificioA = findViewById(R.id.edificio_a);
                GLSurfaceViewEdificioA.setZOrderOnTop(true);
                GLSurfaceViewEdificioA.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
                GLSurfaceViewEdificioA.getHolder().setFormat(PixelFormat.RGBA_8888);
                GLSurfaceViewEdificioA.setRenderer(MyRendererLightEdificioA);
                GLSurfaceViewEdificioA.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

                GLSurfaceViewEdificioB = findViewById(R.id.edificio_b);
                GLSurfaceViewEdificioB.setZOrderOnTop(true);
                GLSurfaceViewEdificioB.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
                GLSurfaceViewEdificioB.getHolder().setFormat(PixelFormat.RGBA_8888);
                GLSurfaceViewEdificioB.setRenderer(MyRendererLightEdificioB);
                GLSurfaceViewEdificioB.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

                GLSurfaceViewEdificioH = findViewById(R.id.edificio_h);
                GLSurfaceViewEdificioH.setZOrderOnTop(true);
                GLSurfaceViewEdificioH.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
                GLSurfaceViewEdificioH.getHolder().setFormat(PixelFormat.RGBA_8888);
                GLSurfaceViewEdificioH.setRenderer(MyRendererLightEdificioH);
                GLSurfaceViewEdificioH.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

                GLSurfaceViewEdificioI = findViewById(R.id.edificio_i);
                GLSurfaceViewEdificioI.setZOrderOnTop(true);
                GLSurfaceViewEdificioI.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
                GLSurfaceViewEdificioI.getHolder().setFormat(PixelFormat.RGBA_8888);
                GLSurfaceViewEdificioI.setRenderer(MyRendererLightEdificioI);
                GLSurfaceViewEdificioI.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

                GLSurfaceViewEdificioNuevo = findViewById(R.id.edificio_nuevo);
                GLSurfaceViewEdificioNuevo.setZOrderOnTop(true);
                GLSurfaceViewEdificioNuevo.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
                GLSurfaceViewEdificioNuevo.getHolder().setFormat(PixelFormat.RGBA_8888);
                GLSurfaceViewEdificioNuevo.setRenderer(MyRendererLightEdificioNuevo);
                GLSurfaceViewEdificioNuevo.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);


                GLSurfaceViewCafeteria = findViewById(R.id.cafeteria);
                GLSurfaceViewCafeteria.setZOrderOnTop(true);
                GLSurfaceViewCafeteria.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
                GLSurfaceViewCafeteria.getHolder().setFormat(PixelFormat.RGBA_8888);
                GLSurfaceViewCafeteria.setRenderer(MyRendererLightCafeteria);
                GLSurfaceViewCafeteria.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

                return true;
            } catch (Exception e) {
                Log.d(TAG, "Error" + e.getMessage() + " " + e.getLocalizedMessage());
            }

            return false;
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Cargando Edificios, espere porfavor...");
            this.dialog.show();
            this.dialog.setCanceledOnTouchOutside(false);
            if (!(android.os.Build.VERSION.SDK_INT >= 27)) {
                MyRendererLightEdificioA = new MyRendererLight(CX, 1);
                MyRendererLightEdificioB = new MyRendererLight(CX, 2);
                MyRendererLightEdificioH = new MyRendererLight(CX, 3);
                MyRendererLightEdificioI = new MyRendererLight(CX, 4);
                MyRendererLightEdificioNuevo = new MyRendererLight(CX, 5);
                MyRendererLightCafeteria = new MyRendererLight(CX, 6);
            }
        }
    }
}