package com.example.livelyturtle.androidar;

import android.content.Context;
import android.opengl.GLES20;

import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Darren on 1/24/16.
 */
public class Square {

    private FloatBuffer vertexBuffer;
    private final int mProgram;

    // number of coordinates per vertex in this array
    static final int TOTAL_VERTICES = 4;
    static final int COORDS_PER_VERTEX = 3;

    static final Moverio3D.Vector TOP_LEFT = Moverio3D.Vector.of(-2f, -1f, -12.0f);
    static final Moverio3D.Vector TOP_RIGHT = Moverio3D.Vector.of(2f, -1f, -12.0f);
    static final Moverio3D.Vector BOT_LEFT = Moverio3D.Vector.of(-2f, -1f, 12.0f);
    static final Moverio3D.Vector BOT_RIGHT = Moverio3D.Vector.of(2f, -1f, 12.0f);

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.22265625f, 0.63671875f, 0.76953125f, 1.0f };

    public Square(Context ctxt, Moverio3D.CardinalDirection dir) {
        // place the Square
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
        Moverio3D.Vector top_right = Moverio3D.rotateYAxis(TOP_RIGHT, (float) Math.toRadians(deg));
        Moverio3D.Vector top_left = Moverio3D.rotateYAxis(TOP_LEFT, (float)Math.toRadians(deg));
        Moverio3D.Vector bot_right = Moverio3D.rotateYAxis(BOT_RIGHT, (float)Math.toRadians(deg));
        Moverio3D.Vector bot_left = Moverio3D.rotateYAxis(BOT_LEFT,(float)Math.toRadians(deg));

        float squareCoords[] = {   // in counterclockwise order:
                top_right.x(), top_right.y(), top_right.z(), // top right
                top_left.x(), top_left.y(), top_left.z(), // top left
                bot_left.x(), bot_left.y(), bot_left.z(),
                bot_right.x(), bot_right.y(), bot_right.z()  // bottom right
        };

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(squareCoords);
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
