package com.example.livelyturtle.androidar.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.android.texample2.GLText;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;
import com.example.livelyturtle.androidar.R;


/*
 * MyGLRenderer mostly handles drawing implementation.
 * For sensor data handling, see World3DActivity.
 *
 *
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {


    // colors (I just made these up, there is no standardization
    final Vector BLACK = Vector.of(0,0,0);
    final Vector WHITE = Vector.of(1,1,1);
    final Vector DARK_GRAY = Vector.of(.3f,.3f,.3f);
    final Vector GRAY = Vector.of(.55f,.55f,.55f);
    final Vector LIGHT_GRAY = Vector.of(.8f,.8f,.8f);
    final Vector LIGHT_BLUE = Vector.of(.7f,.7f,.9f);
    final Vector BLUE = Vector.of(.45f,.45f,.8f);


    // call setScale on glText with this value for default text size
    private final float TEXT_SCALE_CONSTANT = 0.00022f;
    // font in assets folder
    private final String FONT = "nobile-bold.ttf";
    private final int FONT_SIZE = 144;
    private final int FONT_PAD = 2;


    // gl viewport
    private final float NEAR_CLIP_DISTANCE = .5f; // 50cm
    private final float FAR_CLIP_DISTANCE = 500f; // half a kilometer


    // essential member variables
    private Context ctxt;
    private GLText glText;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private float[] currentAPR = new float[] {0,0,0};

    Vector eye;
    Vector upV;
    Vector toCoV;

    // other variables
    private Triangle mTriangle;
    private Square mSquare;


    // string the Context through the constructor
    public MyGLRenderer(Context c) {
        ctxt = c;
    }





    // -----DRAWING METHODS-----

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(BLACK.x(), BLACK.y(), BLACK.z(), 1.0f);

        // Create the GLText
        glText = new GLText(ctxt.getAssets());

        // Load the font from file (set size + padding), creates the texture
        // NOTE: after a successful call to this the font is ready for rendering!
        // font, int height, int padX, int padY
        // size of 144 runs close to GLText's "MAX_FONT_SIZE". Too high and load() will return false
        if(!glText.load(FONT, FONT_SIZE, FONT_PAD, FONT_PAD))
            System.out.println("***GLTEXT LOAD FAILED. THE APP PROBABLY CRASHED.");

        // enable texture + alpha blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);



        // initialize demo shapes
        //mTriangle = new Triangle(ctxt, CardinalDirection.WEST);
        //mSquare = new Square(ctxt, CardinalDirection.NORTHWEST);

         final Moverio3D.Vector TOP_RIGHT = Moverio3D.Vector.of(2f, -1.75f, -50.0f);
         final Moverio3D.Vector TOP_LEFT = Moverio3D.Vector.of(-2f, -1.75f, -50.0f);
         final Moverio3D.Vector BOT_LEFT = Moverio3D.Vector.of(-2f, -1.75f, -20.0f);
         final Moverio3D.Vector BOT_RIGHT = Moverio3D.Vector.of(2f, -1.75f, -20.0f);
        List<Vector> vs = new LinkedList<>();
        vs.add(TOP_RIGHT);
        vs.add(TOP_LEFT);
        vs.add(BOT_LEFT);
        vs.add(BOT_RIGHT);
        List<Short> ss = new LinkedList<>();
        ss.add((short)0);
        ss.add((short)1);
        ss.add((short)2);
        ss.add((short)0);
        ss.add((short)2);
        ss.add((short)3);
        drawDirectory.put("Square", new DrawExecutor(vs, ss, LIGHT_BLUE, 1));
        textDirectory.put("Sample1", new TextExecutor("HELLO", Vector.of(-20,5,2), BLUE, 1));
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // As we are programming for just one device, we assume that the w/h ratio is always 16/9

        // this projection matrix is applied to object coordinates in the onDrawFrame() method above
        // we can define top, bottom, left, and right using class constants and Moverio3D constants
        float shrinkRatio = NEAR_CLIP_DISTANCE/Moverio3D.VIRTUAL_SCREEN_DISTANCE;
        float shrinkHalfW = shrinkRatio * Moverio3D.VIRTUAL_SCREEN_WIDTH / 2;
        float shrinkHalfH = shrinkRatio * Moverio3D.VIRTUAL_SCREEN_HEIGHT / 2;
        Matrix.frustumM(mProjectionMatrix, 0,
                -1.f * shrinkHalfW, shrinkHalfW,
                -1.f * shrinkHalfH, shrinkHalfH,
                NEAR_CLIP_DISTANCE, FAR_CLIP_DISTANCE);
    }

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


        // -----3D VIEWING CALCULATIONS-----

        // default upV is straight up (0,1,0)
        // default toCoV is straight forward 1 unit (0,0,-1) (magnitude DOES NOT MATTER, I have tested this)
        // these are the numbers that would be used when A,P,R are all 0
        // the eye is NOT always at (0,0,0); it moves based on user location data (GPS & external sources)

        // the order of multiplication is around x first, then z, then y
        // so on paper, it goes y(azimuth), z(roll), x(pitch)
        // (This order doesn't agree with codeproject.com/Articles/729759/Android-Sensor-Fusion-Tutorial,
        // but the results still seem correct.)

        // calculate correct vector values from currentAPR (orientation)
        // this is done with homogeneous coordinates and 4x4 matrices
        // http://www.it.hiof.no/~borres/j3d/math/homo/p-homo.html

        // Android APR values are reversed. (It's a left-hand system where clockwise goes up.)
        // OpenGL uses a right-hand system! So, all values must be negated. (Counter-clockwise goes up.)
        float A = -1*currentAPR[0];
        float P = -1*currentAPR[1];
        float R = -1*currentAPR[2];

        eye = Vector.zero();//getEyeLocation functionality inline
        // TODO: update based on user location, but with "DEMO" turned on, force a certain user location.
        upV = Vector.of(
                (float) (-1. * Math.cos(A) * Math.sin(R) * Math.cos(P) + Math.sin(A) * Math.sin(P)),
                (float) (Math.cos(R) * Math.cos(P)),
                (float) (Math.sin(A) * Math.sin(R) * Math.cos(P) + Math.cos(A) * Math.sin(P))
        );
        toCoV = Vector.of(
                (float) (-1. * Math.cos(A) * Math.sin(R) * Math.sin(P) - Math.sin(A) * Math.cos(P)),
                (float) (Math.cos(R) * Math.sin(P)),
                (float) (Math.sin(A) * Math.sin(R) * Math.sin(P) - Math.cos(A) * Math.cos(P))
        );

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                eye.x(), eye.y(), eye.z(),
                eye.x() + toCoV.x(), eye.y() + toCoV.y(), eye.z() + toCoV.z(),
                upV.x(), upV.y(), upV.z());

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);


        // -----DRAWING THE SCENE-----
        //mTriangle.draw(mMVPMatrix);
        //mSquare.draw(mMVPMatrix);

        drawAll();


    }




    // -----EXPOSED METHODS-----

    /**
     *
     * @return true on success
     */
    public boolean addDrawing(String id, List<Vector> vertices, List<Short> order, Vector color, float opacity) {
        drawDirectory.put(id, new DrawExecutor(vertices, order, color, opacity));
        return true;
    }

    /**
     *
     * @return true on success
     */
    public boolean addText(String id, String text, Vector location, Vector color, float opacity) {
        textDirectory.put(id, new TextExecutor(text, location, color, opacity));
        return true;
    }




    // -----CALCULATION IMPLEMENTATION-----

    // holds all openGL non-text things to draw
    private Map<String, DrawExecutor> drawDirectory = new HashMap<>();
    // holds all text to draw (text is drawn with texample2)
    private Map<String, TextExecutor> textDirectory = new HashMap<>();


    // inner classes
    // ONCE AN EXECUTOR HAS BEEN PREPARED, IT CANNOT BE CHANGED - use the key to remove it from the directory,
    // and add it again
    // CALL TextExecutor draws LAST
    private final class DrawExecutor {
        private DrawExecutor(List<Vector> v, List<Short> o, Vector c, float op){
            vertices = v;
            order = o;
            color = c;
            opacity = op;

            vertexArray = Vector.vectorsToFloatArray(vertices);
            drawOrder = new short[order.size()];
            int i = 0;
            for (Short s : order) { drawOrder[i++] = s; }

            ByteBuffer bb = ByteBuffer.allocateDirect(vertexArray.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(vertexArray);
            vertexBuffer.position(0);

            ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(drawOrder);
            drawListBuffer.position(0);

            int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                    R.raw.vertexshader, ctxt);
            int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                    R.raw.fragmentshader, ctxt);
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);
        };

        List<Vector> vertices;
        List<Short> order;
        Vector color;
        float opacity;

        private FloatBuffer vertexBuffer;
        private ShortBuffer drawListBuffer;
        private final int mProgram = GLES20.glCreateProgram();

        private float[] vertexArray;
        private short[] drawOrder;

        private void draw() {
            GLES20.glUseProgram(mProgram);

            int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3,
                    GLES20.GL_FLOAT, false,
                    12, vertexBuffer);

            int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
            GLES20.glUniform4fv(mColorHandle, 1, new float[] {color.x(), color.y(), color.z(), opacity}, 0);

            int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

            // NOTE: glDrawElements must be used if not drawing a triangle
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexArray.length,
                    GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    private final class TextExecutor { // a simple class that just helps specify the proper GLText call
        private TextExecutor(String t, Vector l, Vector c, float op) {
            text = t;
            location = l;
            color = c;
            opacity = op;
        }

        String text;
        Vector location;
        Vector color;
        float opacity;

        private void draw() {
            glText.begin(color.x(), color.y(), color.z(), opacity, mMVPMatrix);

            Vector textToAdjustedLocation = Vector.difference(eye, location);
            Vector shortened = textToAdjustedLocation.scalarMultiply(
                    textToAdjustedLocation.magnitude() <= 1f ? 0f :
                    (textToAdjustedLocation.magnitude()-1f) / textToAdjustedLocation.magnitude());
            Vector adjustedLocation = Vector.sum(location, shortened);

            // for some reason, the default size is absolutely ENORMOUS. Thankfully scaling seems
            // to work at any small order of magnitude
            glText.setScale(calculateAdjustedTextSize(textToAdjustedLocation.magnitude()));

            // NOTE: for some reason, it seems text is drawn without regard to other gl objects's z-values
            // if glText.draw() is called before an overlapping triangle, it is blocked; if called after, it shows.
            // this is a good thing for our purposes; we will only draw text when we want it to be seen.
            // so, text calls come after everything else.
            glText.drawC(text,
                    adjustedLocation.x(), adjustedLocation.y(), adjustedLocation.z(), // location
                    // TODO: the x and z values need to be calculated with respect to azimuth as well (unit circle)
                    (float)(currentAPR[1] * 180./Math.PI),
                    (float)(currentAPR[0] * -180./Math.PI),
                    (float)(currentAPR[2] * 180./Math.PI)); // rotation - text always directly faces user

            // TODO: more functionality: if text would be occluded by a building, still draw it but make it gray

            glText.end();
        }
    }


    // methods
    private void drawAll() {
        drawAllShapes();
        drawAllText();
    }
    private void drawAllShapes() {
        for (DrawExecutor dx : drawDirectory.values()) {
            dx.draw();
        }
    }
    private void drawAllText() {
        for (TextExecutor tx : textDirectory.values()) {
            tx.draw();
        }
    }
    private float calculateAdjustedTextSize(float distance) {
        // this is really some terrible code style, I apologize but it is 6am

        // ***if you need to change stuff, ONLY CHANGE d AND p, and the # of else-if calls***
        // this represents a linear-piecewise function
        int[] d = new int[] {5,20,50,500}; // distance anchors
        float[] p = new float[] {1.f,.8f,.3f,.1f}; // corresponding percent anchors (size)

        // DO NOT TOUCH - not that you want to, probably
        int ptr = 0;
        if (distance < 0) return -1;
        // # else-if = # anchors. First else-if returns TEXT_SCALE_CONSTANT. All other else-if lines are equal.
        else if (distance < d[ptr++]) return TEXT_SCALE_CONSTANT;
        else if (distance < d[ptr++]) return (p[ptr-2]-((distance-d[ptr-2])*(p[ptr-2]-p[ptr-1])/(d[ptr-1]-d[ptr-2]))) * TEXT_SCALE_CONSTANT;
        else if (distance < d[ptr++]) return (p[ptr-2]-((distance-d[ptr-2])*(p[ptr-2]-p[ptr-1])/(d[ptr-1]-d[ptr-2]))) * TEXT_SCALE_CONSTANT;
        else if (distance < d[ptr++]) return (p[ptr-2]-((distance-d[ptr-2])*(p[ptr-2]-p[ptr-1])/(d[ptr-1]-d[ptr-2]))) * TEXT_SCALE_CONSTANT;
        else return 0;
    }






    // -----UTILITY AND HELPER METHODS-----

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
    // we use this to maintain shader code in separate files
    // the shader files are in res/raw
    public static String getRawText(int resource, Context ctxt) throws IOException {
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

    public void updateAPR(float[] APRvalues) {
        currentAPR[0] = APRvalues[0];
        currentAPR[1] = APRvalues[1];
        currentAPR[2] = APRvalues[2];
    }

}
