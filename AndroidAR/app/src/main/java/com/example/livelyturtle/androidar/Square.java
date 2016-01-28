package com.example.livelyturtle.androidar;

import android.content.Context;
import android.opengl.GLES20;

import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Darren on 1/24/16.
 */
public class Square {

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private final int mProgram;

    static final int TOTAL_VERTICES = 4;
    static final int COORDS_PER_VERTEX = 3;
    static final int TOTAL_TRIANGLES_NEEDED = 2;

    // meters
    // OpenGL only draws triangles!
    // Thus, the coordinates array must have 6 elements: TR,TL,BL,TR,BL,BR
    static final Moverio3D.Vector TOP_RIGHT = Moverio3D.Vector.of(1f, -1.75f, -10.0f);
    static final Moverio3D.Vector TOP_LEFT = Moverio3D.Vector.of(-1f, -1.75f, -10.0f);
    static final Moverio3D.Vector BOT_LEFT = Moverio3D.Vector.of(-1f, -1.75f, -3.0f);
    static final Moverio3D.Vector BOT_RIGHT = Moverio3D.Vector.of(1f, -1.75f, -3.0f);

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.82265625f, 0.63671875f, 0.76953125f, 1.0f };

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

        // in counterclockwise order...
        float squareCoords[] = Vector.VectorsToFloatArray(
                top_right, top_left, bot_left, bot_right);
        short drawOrder[] = { 0, 1, 2, 0, 2, 3 };

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


        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);



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

        // NOTE: glDrawElements must be used if not drawing a triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 3*TOTAL_TRIANGLES_NEEDED,
                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}
