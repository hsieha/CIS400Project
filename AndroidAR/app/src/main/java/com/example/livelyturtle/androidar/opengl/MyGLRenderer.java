package com.example.livelyturtle.androidar.opengl;

import android.content.Context;
import android.location.Location;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.android.texample2.GLText;
import com.example.livelyturtle.androidar.Building;
import com.example.livelyturtle.androidar.Coordinate;
import com.example.livelyturtle.androidar.MapData;
import com.example.livelyturtle.androidar.MoverioLibraries.DataDebug;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D;
import com.example.livelyturtle.androidar.MoverioLibraries.Moverio3D.*;
import com.example.livelyturtle.androidar.R;
import com.example.livelyturtle.androidar.Street;
import com.example.livelyturtle.androidar.Beacon;
import com.example.livelyturtle.androidar.Chevron;

import org.w3c.dom.Text;

import static com.example.livelyturtle.androidar.opengl.DefaultEffect.*;
import com.example.livelyturtle.androidar.MoverioLibraries.DataDebug.*;

/*
 * MyGLRenderer mostly handles drawing implementation.
 * For sensor data handling, see World3DActivity.
 *
 *
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {


    // colors (I just made these up, there is no standardization
    public static final Vector BLACK = Vector.of(0,0,0);
    public static final Vector WHITE = Vector.of(1,1,1);
    public static final Vector DARK_GRAY = Vector.of(.3f,.3f,.3f);
    public static final Vector GRAY = Vector.of(.55f,.55f,.55f);
    public static final Vector LIGHT_GRAY = Vector.of(.8f,.8f,.8f);
    public static final Vector LIGHT_BLUE = Vector.of(.7f,.7f,.9f);
    public static final Vector PURE_BLUE = Vector.of(0,0,1f);
    public static final Vector BLUE = Vector.of(.45f,.45f,.8f);
    public static final Vector RED = Vector.of(.8f, .45f, .45f);
    public static final Vector PURE_GREEN = Vector.of(0, 1f, 0);


    // call setScale on glText with this value for default text size
    private final float TEXT_SCALE_CONSTANT = 0.00055f;
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

    // user eye height assumed to be 1.75m
    Vector eye = Vector.of(0,1.75f,0);
    Vector upV;
    Vector toCoV;

    Coordinate hardCoord = new Coordinate(DataDebug.HARDCODE_LAT, DataDebug.HARDCODE_LONG);
    private boolean noLocationDataAvailable = true;
    private String locationStatus = "NO LOC DATA";

    /**
     * updateEye is only called when location updates are available - NOT for manual setting
     * @param la the latitude obtained from the location sensor
     * @param lo the longitude obtained from the location sensor
     */
    public void updateEye(double la, double lo) {
        Coordinate c = new Coordinate(la, lo);
        eye = Vector.of((float)c.x, eye.y(), (float)c.z);
        noLocationDataAvailable = false;

        // the status is the time
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        locationStatus = "lst upd:[" + df.format(cal.getTime()) + "]";
    }

    // other variables
    //private Triangle mTriangle;
    //private Square mSquare;

    private MapData mapData;

    // string the Context through the constructor
//    public MyGLRenderer(Context c) {
//        ctxt = c;
//    }
    private int mProgram;
    public MyGLRenderer(Context c, MapData mapData) {
        ctxt = c; this.mapData = mapData;
    }





    // -----DRAWING METHODS-----

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {


        // -- start opengl program creation code --
        mProgram = GLES20.glCreateProgram();
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                R.raw.vertexshader, ctxt);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                R.raw.fragmentshader, ctxt);
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        // -- end opengl program creation code --

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

        // depth testing enabled
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glDepthMask(true);



        // initialize demo shapes
        //mTriangle = new Triangle(ctxt, CardinalDirection.WEST);
        //mSquare = new Square(ctxt, CardinalDirection.NORTHWEST);

        // ***DEMO DATA... you should use addDrawing and addText***
        ///////////////////////////////////////////////////////////////
/*
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

         */
/*
        final Moverio3D.Vector BOT_LEFT = Moverio3D.Vector.of(  -390.77f,      0.00f,   -117.79f);
        final Moverio3D.Vector TOP_LEFT = Moverio3D.Vector.of(  -390.77f,      2.50f,   -117.79f);
        final Moverio3D.Vector BOT_RIGHT = Moverio3D.Vector.of(  -393.64f,      0.00f,    -95.30f);
        final Moverio3D.Vector TOP_RIGHT = Moverio3D.Vector.of(  -393.64f,      2.50f,    -95.30f);

        List<Vector> vs = new LinkedList<>();
        vs.add(BOT_LEFT);
        vs.add(TOP_LEFT);
        vs.add(BOT_RIGHT);
        vs.add(TOP_RIGHT);
        List<Short> ss = new LinkedList<>();
        ss.add((short)1);
        ss.add((short)0);
        ss.add((short)2);
        ss.add((short)1);
        ss.add((short)2);
        ss.add((short) 3);

        addDrawing("Square", vs, ss,BLUE, 1);*/

        addMapData(mapData);
        //drawDirectory.put("Square", new DrawExecutor(vs, ss, WHITE, 1));


//        textDirectory.put("Sample1", new TextExecutor("WEST NEAR", Vector.of(-8,0,0), BLUE, 1));
//        textDirectory.put("Sample2", new TextExecutor("WEST HIGH", Vector.of(-8,10,0), BLUE, 1));
//        textDirectory.put("Sample3", new TextExecutor("WEST FAR (and up a bit)", Vector.of(-80,4,0), BLUE, 1));
//        textDirectory.put("Sample4", new TextExecutor("northwest", Vector.of(-20,0,-20), BLUE, 1));
//        textDirectory.put("Sample5", new TextExecutor("north", Vector.of(0,-3,-60), BLUE, 1));
//        textDirectory.put("Sample6", new TextExecutor("northeast", Vector.of(50,3,-50), BLUE, 1));
//        textDirectory.put("Sample7", new TextExecutor("east", Vector.of(35,0,0), BLUE, 1));

        ///////////////////////////////////////////////////////////////
        // -----END DEMO DATA-----


        // start the path simulation timer, if necessary
        if (DataDebug.LOCATION_MODE == LocationMode.PATH_SIMULATION) {
            System.out.println("STARTING PATH TIMER: " + DataDebug.startPathTimer(ctxt));
        }

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

        // Set the background frame color
        // clear old depth buffer info (learnopengl.com/#!Getting-started/Coordinate-Systems)
        GLES20.glClearColor(BLACK.x(), BLACK.y(), BLACK.z(), 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);


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

        // for debug, insert eye location here (it's handled by a bluetooth thread for LocationMode.REAL)
        if (DataDebug.LOCATION_MODE == LocationMode.HARDCODE) {
            eye = Vector.of((float)hardCoord.x, eye.y(), (float)hardCoord.z);
            locationStatus = "HRDCD";
        }
        else if (DataDebug.LOCATION_MODE == LocationMode.PATH_SIMULATION) {
            Coordinate c = DataDebug.getPathSimulationCoordinate();
            eye = Vector.of((float)c.x, eye.y(), (float)c.z);
            locationStatus = "PATHSIM";
        }

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


        // DEBUG DYNAMIC DRAWING - remove soon
//        if (Moverio3D.getDirectionFromAzimuth(currentAPR[0]) == CardinalDirection.SOUTHWEST) {
//            System.out.println("*** LOOKING SW");
//
//            // add a bright green square somewhere, id "hello1"
//            List<Vector> vlist = new LinkedList<>();
//            Vector BL = Vector.of(-400,0,-50);
//            Vector TL = Vector.of(-400,30,-50);
//            Vector TR = Vector.of(-400,30,-20);
//            Vector BR = Vector.of(-400,0,-20);
//            vlist.add(BL);vlist.add(TL);vlist.add(TR);vlist.add(BR);
//            List<Short> order = new LinkedList<>();
//            order.add((short)0);order.add((short)1);order.add((short)2);
//            order.add((short)0);order.add((short)2);order.add((short)3);
//            addDrawing("hello1", vlist, order, PURE_GREEN, 1f);
//
//            // add light blue text into the square, id "hello2"
//            addText("hello2", "TEST TEST TEST", Vector.of(-390,10,-35), LIGHT_BLUE, 1f);
//        }
//        else {
//            System.out.println("*** NOT");
//            // remove id "hello1"
//            removeDrawing("hello1");
//            // remove id "hello2"
//            removeText("hello2");
//        }
//        if (Moverio3D.getDirectionFromAzimuth(currentAPR[0]) == CardinalDirection.SOUTHWEST) {
//            doJunk();
//        }
        // END DEBUG DYNAMIC DRAWING - remove soon

        // -----DRAWING THE SCENE-----
        //mTriangle.draw(mMVPMatrix);
        //mSquare.draw(mMVPMatrix);

        drawAll();

        drawLocationStatus();

        //System.out.println("*** ===DRAW CYCLE OVER===");

    }

//    public void doJunk() {
//        List<Moverio3D.Vector> vlist = new LinkedList<>();
//        Moverio3D.Vector BL = Moverio3D.Vector.of(-400, 0, -50);
//        Moverio3D.Vector TL = Moverio3D.Vector.of(-400, 30, -50);
//        Moverio3D.Vector TR = Moverio3D.Vector.of(-400, 30, -20);
//        Moverio3D.Vector BR = Moverio3D.Vector.of(-400, 0, -20);
//        vlist.add(BL);vlist.add(TL);vlist.add(TR);vlist.add(BR);
//        List<Short> order = new LinkedList<>();
//        order.add((short)0);order.add((short)1);order.add((short)2);
//        order.add((short) 0);
//        order.add((short) 2);
//        order.add((short)3);
//        addDrawing("hello1", vlist, order, MyGLRenderer.PURE_GREEN, 1f);
//        System.out.println("*** JUNK DONE");
//    }

    // -----EXPOSED METHODS-----
    // changing drawings or text is expensive - don't do it often!

    public boolean addDrawing(String id, List<Vector> vertices, List<Short> order, Vector color, float opacity) {
        return addDrawing(id, vertices, order, color, opacity, DEFAULT_EFFECT);
    }
    public boolean addDrawing(String id,
                           List<Vector> vertices,
                           List<Short> order,
                           Vector color,
                           float opacity,
                           DrawEffect eff) {
        if (!drawDirectory.containsKey(id)) {
            drawDirectory.put(id, new DrawExecutor(vertices, order, color, opacity, eff));
            return true;
        }
        else {
            System.out.println("addDrawing WARNING: id already exists. Call ignored.");
            return false;
        }
    }
    public boolean changeDrawingVerticesAndOrder(String id, List<Vector> vertices, List<Short> order) {
        DrawExecutor old = drawDirectory.get(id);
        if (old != null) {
            drawDirectory.put(id, new DrawExecutor(vertices, order, old.color, old.opacity, old.drawEffect));
            return true;
        }
        return false;
    }
    public boolean changeDrawingColor(String id, Vector color) {
        DrawExecutor old = drawDirectory.get(id);
        if (old != null) {
            drawDirectory.put(id, new DrawExecutor(old.vertices, old.order, color, old.opacity, old.drawEffect));
            return true;
        }
        return false;
    }
    public boolean changeDrawingEffect(String id, DrawEffect eff) {
        DrawExecutor old = drawDirectory.get(id);
        if (old != null) {
            drawDirectory.put(id, new DrawExecutor(old.vertices, old.order, old.color, old.opacity, eff));
            return true;
        }
        return false;
    }
    public boolean removeDrawing(String id) {
        if (drawDirectory.remove(id) == null) return false;
        return true;
    }



    public boolean addText(String id, String text, Vector location, Vector color, float opacity) {
        if (!textDirectory.containsKey(id)) {
            textDirectory.put(id, new TextExecutor(text, location, color, opacity));
            return true;
        }
        else {
            System.out.println("addText WARNING: id already exists. Call ignored.");
            return false;
        }
    }
    public boolean changeTextString(String id, String text) {
        TextExecutor old = textDirectory.get(id);
        if (old != null) {
            textDirectory.put(id, new TextExecutor(text, old.location, old.color, old.opacity));
            return true;
        }
        return false;
    }
    public boolean changeTextColor(String id, Vector color) {
        TextExecutor old = textDirectory.get(id);
        if (old != null) {
            textDirectory.put(id, new TextExecutor(old.text, old.location, color, old.opacity));
            return true;
        }
        return false;
    }
    public boolean changeTextLocation(String id, Vector location) {
        TextExecutor old = textDirectory.get(id);
        if (old != null) {
            textDirectory.put(id, new TextExecutor(old.text, location, old.color, old.opacity));
            return true;
        }
        return false;
    }
    public boolean removeText(String id) {
        if (textDirectory.remove(id) == null) return false;
        return true;
    }



    public void addMapData(MapData mapData) {
        HashSet<Building> buildings = mapData.getBuildings();
        HashSet<Street> streets = mapData.getStreets();

        for (Building building : buildings) {

//            System.out.println(building.getName() + ": ");
//            System.out.println("vectors: " + building.vectors());
//            System.out.println("vector_order:" + building.vector_order());

            addDrawing(building.getName(), building.vectors(), building.vector_order(), BLUE, 1);
            Coordinate tcoord = building.getTextCoord();
            addText(building.getName(), building.getName(), Vector.of((float)tcoord.x,1.5f,(float)tcoord.z), WHITE, 1);
        }
        for (Street street : streets) {

//            System.out.println(street.getName() + ": ");
//            System.out.println("vectors: " + street.vectors());
//            System.out.println("vector_order:" + street.vector_order());

            addDrawing(street.getName(), street.vectors(), street.vector_order(), RED, 1);
        }

        //Beacon test draw
        System.out.println("Drawing dah beacon");

        Coordinate beacon_coordinate = new Coordinate(39.952258, -75.197008);
        ArrayList<Coordinate> beacon_list = new ArrayList<Coordinate>();
        beacon_list.add(beacon_coordinate);
        Beacon test_beacon = new Beacon("Test Beacon", beacon_list);

        System.out.println(test_beacon.getName() + ": ");
        System.out.println("vectors: " + test_beacon.vectors());
        System.out.println("vector_order:" + test_beacon.vector_order());

        addDrawing(test_beacon.getName(), test_beacon.vectors(), test_beacon.vector_order(), WHITE, 1);
        //end of beacon test draw code
        
        //chevron test draw
        System.out.println("Drawing dah chevron");

        Coordinate chevron_coordinate_1 = new Coordinate(DataDebug.HARDCODE_LAT + 0.00006, DataDebug.HARDCODE_LONG - 0.0003);
        Coordinate chevron_coordinate_2 = new Coordinate(DataDebug.HARDCODE_LAT + 0.00008, DataDebug.HARDCODE_LONG - 0.00033);
        Coordinate chevron_coordinate_3 = new Coordinate(DataDebug.HARDCODE_LAT + 0.00010, DataDebug.HARDCODE_LONG - 0.00036);
        ArrayList<Coordinate> chevron_list_1 = new ArrayList<Coordinate>();
        ArrayList<Coordinate> chevron_list_2 = new ArrayList<Coordinate>();
        ArrayList<Coordinate> chevron_list_3 = new ArrayList<Coordinate>();
        chevron_list_1.add(chevron_coordinate_1);
        chevron_list_2.add(chevron_coordinate_2);
        chevron_list_3.add(chevron_coordinate_3);
        Chevron test_chevron_1 = new Chevron("Test Chevron 1", chevron_list_1, 0.0f); //facing north
        Chevron test_chevron_2 = new Chevron("Test Chevron 2", chevron_list_2, 0.0f); //facing north
        Chevron test_chevron_3 = new Chevron("Test Chevron 3", chevron_list_3, 0.0f); //facing north

//        System.out.println(test_chevron.getName() + ": ");
//        System.out.println("vectors: " + test_chevron.vectors());
//        System.out.println("vector_order:" + test_chevron.vector_order());

        addDrawing(test_chevron_1.getName(), test_chevron_1.vectors(), test_chevron_1.vector_order(), PURE_BLUE, 1,
                new DrawEffect.Blink(0,2000,0,500));
        addDrawing(test_chevron_2.getName(), test_chevron_2.vectors(), test_chevron_2.vector_order(), PURE_BLUE, 1,
                new DrawEffect.Blink(0,2000,500,1000));
        addDrawing(test_chevron_3.getName(), test_chevron_3.vectors(), test_chevron_3.vector_order(), PURE_BLUE, 1,
                new DrawEffect.Blink(0,2000,1000,1500));

        //end of chevron test draw

        // MICHAEL: testing effects on beacons
        Beacon effect1 = new Beacon("b1", new ArrayList<Coordinate>(){{add(new Coordinate(39.953, -75.203));}});
        Beacon effect2 = new Beacon("b2", new ArrayList<Coordinate>(){{add(new Coordinate(39.9545, -75.2025));}});
        addDrawing(effect1.getName(), effect1.vectors(), effect1.vector_order(), PURE_GREEN, 1,
                new Throb(WHITE,3667));
        addDrawing(effect2.getName(), effect2.vectors(), effect2.vector_order(), DARK_GRAY, 1,
                new Blink(0,400,0,200));
        // END EFFECTS TESTING
    }



    // -----CALCULATION IMPLEMENTATION-----

    // holds all openGL non-text things to draw
    private Map<String, DrawExecutor> drawDirectory = new ConcurrentHashMap<>();
    // holds all text to draw (text is drawn with texample2)
    private Map<String, TextExecutor> textDirectory = new ConcurrentHashMap<>();


    // inner classes
    // ONCE AN EXECUTOR HAS BEEN PREPARED, IT CANNOT BE CHANGED - use the key to remove it from the directory,
    // and add it again
    // CALL TextExecutor draws LAST
    private final class DrawExecutor {

        List<Vector> vertices;
        List<Short> order;
        Vector color;
        float opacity;

        private FloatBuffer vertexBuffer;
        private ShortBuffer drawListBuffer;

        private float[] vertexArray;
        private short[] drawOrder;

        @NonNull
        private DrawEffect drawEffect;

        private DrawExecutor(List<Vector> v, List<Short> o, Vector c, float op, DrawEffect eff){
            vertices = v;
            order = o;
            color = c;
            opacity = op;
            drawEffect = eff;

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
        }

        private void draw() {

            // don't draw if you can't see anything
            if (!drawEffect.isVisible()) return;

            // -- "glUseProgram" code starts below --
            GLES20.glUseProgram(mProgram);

            int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3,
                    GLES20.GL_FLOAT, false,
                    12, vertexBuffer);

            int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
            Vector modColor = drawEffect.getModifiedColor(color);
            GLES20.glUniform4fv(mColorHandle, 1, new float[] {modColor.x(), modColor.y(), modColor.z(), opacity}, 0);

            int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

            // NOTE: glDrawElements must be used if not drawing a triangle
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                    GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    private final class TextExecutor { // a simple class that just helps specify the proper GLText call

        String text;
        Vector location;
        Vector color;
        float opacity;

        private TextExecutor(String t, Vector l, Vector c, float op) {
            text = t;
            location = l;
            color = c;
            opacity = op;
        }

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
                    0,
                    (float)(currentAPR[0] * -180./Math.PI),
                    0); // rotation - text always directly faces user (azimuth only)

            // TODO: more functionality: if text would be occluded by a building, still draw it but make it gray

            glText.end();
        }
    }


    // methods
    private void drawAll() {
        drawAllShapes();
        GLES20.glDisable(GLES20.GL_DEPTH_TEST); // text always on top
        drawAllText();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
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
        int[] d = new int[] {5,20,50,100,500}; // distance anchors
        float[] p = new float[] {1.f,.9f,.75f,.15f,.03f}; // corresponding percent anchors (size)

        // DO NOT TOUCH - not that you want to, probably
        int ptr = 0;
        if (distance < 0) return -1;
        // # else-if = # anchors. First else-if returns TEXT_SCALE_CONSTANT. All other else-if lines are equal.
        else if (distance < d[ptr++]) return TEXT_SCALE_CONSTANT;
        else if (distance < d[ptr++]) return (p[ptr-2]-((distance-d[ptr-2])*(p[ptr-2]-p[ptr-1])/(d[ptr-1]-d[ptr-2]))) * TEXT_SCALE_CONSTANT;
        else if (distance < d[ptr++]) return (p[ptr-2]-((distance-d[ptr-2])*(p[ptr-2]-p[ptr-1])/(d[ptr-1]-d[ptr-2]))) * TEXT_SCALE_CONSTANT;
        else if (distance < d[ptr++]) return (p[ptr-2]-((distance-d[ptr-2])*(p[ptr-2]-p[ptr-1])/(d[ptr-1]-d[ptr-2]))) * TEXT_SCALE_CONSTANT;
        else if (distance < d[ptr++]) return (p[ptr-2]-((distance-d[ptr-2])*(p[ptr-2]-p[ptr-1])/(d[ptr-1]-d[ptr-2]))) * TEXT_SCALE_CONSTANT;
        else return 0;
    }
    private void drawLocationStatus() {
        if (noLocationDataAvailable) {
            // red text
            glText.begin(1, 0, 0, 1, mMVPMatrix);
        }
        else {
            // white text
            glText.begin(1, 1, 1, 1, mMVPMatrix);
        }
        glText.setScale(TEXT_SCALE_CONSTANT * .064f, TEXT_SCALE_CONSTANT * .13f);

        // I can't use the same string or else it will keep appending to itself, which is a bug
        // that is very bad for text-drawing performance
        String locationStatusDisplayString = locationStatus;
        locationStatusDisplayString += "|E:" + eye;
        locationStatusDisplayString += "|DIR:" + Moverio3D.getDirectionFromAzimuth(currentAPR[0]).name();

        // TODO: if you want to start drawing text to the left of the screen center, use a cross product
        glText.draw(locationStatusDisplayString,
                eye.x() + toCoV.x(), eye.y() + toCoV.y(), eye.z() + toCoV.z(), // location
                0,
                (float)(currentAPR[0] * -180./Math.PI), // rotation - text always directly faces user (azimuth only)
                0);

        glText.end();
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
