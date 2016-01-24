package com.example.livelyturtle.androidar;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by Michael on 1/12/2016.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Context ctxt;
    private Triangle mTriangle;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private float[] currentAPR = new float[3];

    public MyGLRenderer(Context c) {
        ctxt = c;
        currentAPR[0] = 0;
        currentAPR[1] = 0;
        currentAPR[2] = 0;
    }

    public void updateAPR(float[] APRvalues) {
        currentAPR[0] = APRvalues[0];
        currentAPR[1] = APRvalues[1];
        currentAPR[2] = APRvalues[2];
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // initialize a triangle
        mTriangle = new Triangle(ctxt, CardinalDirection.SOUTHWEST);
    }

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // default upV is straight up (0,1,0)
        // default toCoV is straight forward (0,0,-1)
        // these are the numbers that would be used when A,P,R are all 0
        // the eye is always at the origin

        // the order of multiplication is around x first, then z, then y
        // so on paper, it goes y(azimuth), z(roll), x(pitch)
        // I'm not too sure, but I believe the order doesn't matter (matrix mult is assoc)

        // calculate correct vector values from currentAPR (orientation)
        // this is done with homogeneous coordinates and 4x4 matrices
        // http://www.it.hiof.no/~borres/j3d/math/homo/p-homo.html

        // Android APR values are reversed. (It's a left-hand system where clockwise goes up.)
        // OpenGL uses a right-hand system! So, all values must be negated. (Counter-clockwise goes up.)
        float A = -1*currentAPR[0];
        float P = -1*currentAPR[1];
        float R = -1*currentAPR[2];

        Vector eye = Vector.zero();
        Vector upV = Vector.of(
                (float)( -1.*Math.cos(A)*Math.sin(R)*Math.cos(P) + Math.sin(A)*Math.sin(P) ),
                (float)(     Math.cos(R)*Math.cos(P) ),
                (float)(     Math.sin(A)*Math.sin(R)*Math.cos(P) + Math.cos(A)*Math.sin(P) )
        );
        Vector toCoV = Vector.of(
                (float)( -1.*Math.cos(A)*Math.sin(R)*Math.sin(P) - Math.sin(A)*Math.cos(P) ),
                (float)(     Math.cos(R)*Math.sin(P) ),
                (float)(     Math.sin(A)*Math.sin(R)*Math.sin(P) - Math.cos(A)*Math.cos(P) )
        );

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                eye.x(), eye.y(), eye.z(),
                eye.x()+toCoV.x(), eye.y()+toCoV.y(), eye.z()+toCoV.z(),
                upV.x(), upV.y(), upV.z());

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        mTriangle.draw(mMVPMatrix);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public static int loadShader(int type, int resource, Context ctxt) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        try {
            GLES20.glShaderSource(shader, getRawText(resource, ctxt));
            GLES20.glCompileShader(shader);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return shader;
    }

    // http://www.java2s.com/Code/Android/File/GetFileContentsasString.htm
    private static String getRawText(int resource, Context ctxt) throws IOException {
        final InputStream inputStream = ctxt.getResources().openRawResource(resource);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder stringBuilder = new StringBuilder();

        boolean done = false;
        while (!done) {
            final String line = reader.readLine();
            done = (line == null);

            if (line != null) {
                stringBuilder.append(line);
            }
        }
        reader.close();
        inputStream.close();
        return stringBuilder.toString();
    }
}
