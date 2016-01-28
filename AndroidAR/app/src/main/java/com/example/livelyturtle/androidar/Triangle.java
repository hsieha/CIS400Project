package com.example.livelyturtle.androidar;

import android.content.Context;
import android.opengl.GLES20;

import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Michael on 1/12/2016.
 */
public class Triangle {

    private FloatBuffer vertexBuffer;
    private final int mProgram;

    // number of coordinates per vertex in this array
    static final int TOTAL_VERTICES = 3;
    static final int COORDS_PER_VERTEX = 3;

    // assuming a user height of about 175cm
    // triangle starts on the ground and rises to eye height
    static final Vector TOP = Vector.of(1.f, 0f, -10f);
    static final Vector LEFT = Vector.of(-1.f, -1.75f, -5f);
    static final Vector RIGHT = Vector.of(1.f, -1.75f, -5f);

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.22265625f, 0.63671875f, 0.76953125f, 1.0f };

    public Triangle(Context ctxt, CardinalDirection dir) {
        // place the Triangle
        int deg;
        switch (dir) {
            case WEST:
                deg = 90;
                break;
            case EAST:
                deg = 270;
                break;
            case SOUTH:
                deg = 180;
                break;
            case NORTHWEST:
                deg = 45;
                break;
            case SOUTHWEST:
                deg = 135;
                break;
            case NORTHEAST:
                deg = 315;
                break;
            case SOUTHEAST:
                deg = 225;
                break;
            case NORTH:
            default:
                deg = 0;
                break;
        }
        Vector top = Moverio3D.rotateYAxis(TOP, (float) Math.toRadians(deg));
        Vector left = Moverio3D.rotateYAxis(LEFT, (float)Math.toRadians(deg));
        Vector right = Moverio3D.rotateYAxis(RIGHT, (float)Math.toRadians(deg));

        // in counterclockwise order:
        float triangleCoords[] = Vector.VectorsToFloatArray(top, left, right);

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);



        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                R.raw.vertexshader, ctxt);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                R.raw.fragmentshader, ctxt);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();
        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);
        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);
        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }


    private int mPositionHandle;
    private int mColorHandle;
    private final int vertexCount = TOTAL_VERTICES;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}
