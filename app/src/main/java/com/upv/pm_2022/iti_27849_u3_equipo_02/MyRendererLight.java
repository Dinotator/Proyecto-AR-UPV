package com.upv.pm_2022.iti_27849_u3_equipo_02;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRendererLight extends GLSurfaceView implements Renderer {

    /**
     * Triangle instance
     */
    private OBJParser parser;
    private TDModel model = null;

    private float z = 15.0f;



    int x = 0;
    int y = 0;
    int xS = 0;
    int yS = 0;


    String TAG = "Ayuda";
    private float[] lightAmbient = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] lightPosition = {100.0f, 100.0f, -5000.0f, 1.0f};
    private FloatBuffer lightAmbientBuffer;
    private FloatBuffer lightDiffuseBuffer;
    private FloatBuffer lightPositionBuffer;

    public int edificio;

    public MyRendererLight(Context ctx, int edificio) {
        super(ctx);
        this.edificio = edificio;

        switch (edificio) {
            case 1:
                parser = new OBJParser(ctx, R.raw.edificio_a_obj);
                break;
            case 2:
                parser = new OBJParser(ctx, R.raw.edificio_b_obj);
                break;
            case 3:
                parser = new OBJParser(ctx, R.raw.edificio_h_obj);
                break;
            case 4:
                parser = new OBJParser(ctx, R.raw.edificio_i_obj);
                break;
            case 5:
                parser = new OBJParser(ctx, R.raw.edificio_nuevo_obj);
                break;
            case 6:
                parser = new OBJParser(ctx, R.raw.cafeteria_obj);
                break;
        }
        this.cargar();

        if (edificio == 1){
            float [] lightAmbient = {1.0f, 13.0f, 0.0f, 3.0f};
            ByteBuffer byteBuf = ByteBuffer.allocateDirect(lightAmbient.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            lightAmbientBuffer = byteBuf.asFloatBuffer();
            lightAmbientBuffer.put(lightAmbient);
            lightAmbientBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            lightDiffuseBuffer = byteBuf.asFloatBuffer();
            lightDiffuseBuffer.put(lightDiffuse);
            lightDiffuseBuffer.position(0);
            float[] lightPositionAux2 = {150.0f, 750.0f, 1000.0f, 10.0f};
            byteBuf = ByteBuffer.allocateDirect(lightPositionAux2.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            lightPositionBuffer = byteBuf.asFloatBuffer();
            lightPositionBuffer.put(lightPositionAux2);
            lightPositionBuffer.position(0);
        }else {

            ByteBuffer byteBuf = ByteBuffer.allocateDirect(lightAmbient.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            lightAmbientBuffer = byteBuf.asFloatBuffer();
            lightAmbientBuffer.put(lightAmbient);
            lightAmbientBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            lightDiffuseBuffer = byteBuf.asFloatBuffer();
            lightDiffuseBuffer.put(lightDiffuse);
            lightDiffuseBuffer.position(0);


            byteBuf = ByteBuffer.allocateDirect(lightPosition.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            lightPositionBuffer = byteBuf.asFloatBuffer();
            lightPositionBuffer.put(lightPosition);
            lightPositionBuffer.position(0);
        }

    }

    public void cargar() {

        try {
            model = parser.parseOBJ();
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }


    }

    /**
     * The Surface is created/init()
     */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);
        gl.glEnable(GL10.GL_LIGHT0);


        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }

    /**
     * Here we do our drawing
     */
    public void onDrawFrame(GL10 gl) {

        //Clear Screen And Depth Buffer
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glEnable(GL10.GL_LIGHTING);

        gl.glTranslatef(x / 2, -y / 2, -z);    //Move down 1.2 Unit And Into The Screen 6.0
        gl.glRotatef( x*3, x, 1.0f, 0.0f);

        if (edificio == 4){
            gl.glScalef(0.41f, 0.41f, 0.41f);
        }else if(edificio == 2){
            gl.glScalef(0.60f, 0.60f, 0.60f);
        }
        gl.glScalef(0.50f, 0.50f, 0.50f);
        gl.glRotatef(0.0f, 1.0f, 0.0f, 0.0f);

        if (model != null) {
            model.draw(gl);                        //Draw the square
        }



        gl.glLoadIdentity();
    }

    /**
     * If the surface changes, reset the view
     */
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (height <= 0) {                        //Prevent A Divide By Zero By
            height = 1;                        //Making Height Equal One
        }

        gl.glViewport(0, 0, width, height);    //Reset The Current Viewport
        gl.glMatrixMode(GL10.GL_PROJECTION);    //Select The Projection Matrix
        gl.glLoadIdentity();                    //Reset The Projection Matrix

        //Calculate The Aspect Ratio Of The Window
        GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 1f, 500.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);    //Select The Modelview Matrix
        gl.glLoadIdentity();                    //Reset The Modelview Matrix
    }


    public void angulo(int x, int y, int xS, int yS) {
        this.x = x;
        this.y = y;
        this.xS = xS / 2;
        this.yS = yS / 2;
    }
}