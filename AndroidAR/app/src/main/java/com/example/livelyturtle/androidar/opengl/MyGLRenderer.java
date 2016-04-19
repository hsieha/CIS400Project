package com.example.livelyturtle.androidar.opengl;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
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
import java.util.Set;
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
import com.example.livelyturtle.androidar.ThreeChevron;
import com.example.livelyturtle.androidar.Chevron;
import com.example.livelyturtle.androidar.Tour;
import android.media.MediaPlayer;
import android.view.MotionEvent;

import com.example.livelyturtle.androidar.Path;

import org.w3c.dom.Text;

import static com.example.livelyturtle.androidar.opengl.DefaultEffect.*;
import com.example.livelyturtle.androidar.MoverioLibraries.DataDebug.*;
import com.example.livelyturtle.androidar.activities.World3DActivity;

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
    public static final Vector LIGHT_GRAY = Vector.of(.7f,.7f,.7f);
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
    private final float FAR_CLIP_DISTANCE = 2500f; // 2.5km


    // essential member variables
    private Context ctxt;
    private GLText glText;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private float[] currentAPR = new float[] {0,0,0};

    // user eye height assumed to be 1.75m
    Vector eye = Vector.of(0,1.75f,0); // default to 0 if no info available
    Vector upV;
    Vector toCoV;

    Coordinate hardCoord = new Coordinate(DataDebug.HARDCODE_LAT, DataDebug.HARDCODE_LONG);
    private boolean noLocationDataAvailable = true;
    private String locationStatus = "NO LOC DATA";

    // give user the option to correct APR angles in case of sensor fusion inaccuracies
    float azimuthCorrection = 0;
    float pitchCorrection = 0;
    float rollCorrection = 0;

    MediaPlayer mp = null;
    int media_counter = 0;
    Tour tour = null;
    public boolean arrived = true;  //if user has arrived to the next location or not
    Beacon dest_beacon = null;

    public Coordinate getEyeCoord() {
        return Coordinate.fromXZ(eye.x(), eye.z());
    }

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

        stlock_eyePosUpdated = true;
        compassRecenter_eyePosUpdated = true;
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

    // pathsim vars
    private long ps_lastTimeMultiple;
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
        float A = -1*(currentAPR[0] + azimuthCorrection);
        float P = -1*(currentAPR[1] + pitchCorrection);
        float R = -1*(currentAPR[2] + rollCorrection);

        // *** Eye, Up, and View vectors *** //
        // for debug, insert eye location here (it's handled by receiveGPSDataTask for LocationMode.REAL)
        long now = System.currentTimeMillis();
        if (DataDebug.LOCATION_MODE == LocationMode.HARDCODE) {
            eye = Vector.of((float)hardCoord.x, eye.y(), (float)hardCoord.z);
            locationStatus = "HRDCD";
        }
        else if (DataDebug.LOCATION_MODE == LocationMode.PATH_SIMULATION) {
            Coordinate c = DataDebug.getPathSimulationCoordinate();
            locationStatus = "PATHSIM";

            if (ps_lastTimeMultiple != now/DataDebug.PATH_SIM_UPDATE_PERIOD) {
                ps_lastTimeMultiple = now/DataDebug.PATH_SIM_UPDATE_PERIOD;
                eye = Vector.of((float)c.x, eye.y(), (float)c.z);
                stlock_eyePosUpdated = true;
                compassRecenter_eyePosUpdated = true;
            }
        }


        // correct eye value by locking it to nearest street
        streetLock();

        // detect which endpoint we're heading towards (used for compass adjustment)
        headingDetectionService();


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

        // -- TOUR LOGIC -- //
        if(tour != null) {
            //check to see if you've arrived at a beacon
            if (dest_beacon != null) {
                arrived = dest_beacon.hasArrived(eye);
            }

            //when user has arrived at a destination
            if (arrived) {

                System.out.println("we've arrived");

                //when you have arrived, remove the beacon
                if (dest_beacon != null) {
                    removeDrawing(dest_beacon.getName());
                }
                removePath();

                //end previous clip if still playing
                if (mp != null) {
                    endClip();
                }

                //play the audio clip
                playClip(media_counter);
                media_counter++;

                //obtain the next point on the tour
                Coordinate next_point = tour.next();

                //check if next point exists
                if (next_point != null) {

                    //render the path to that point
                    renderPath(next_point);

                    //create a beacon at the destination
                    ArrayList<Coordinate> beacon_list = new ArrayList<Coordinate>();
                    beacon_list.add(next_point);
                    dest_beacon = new Beacon("Destination Beacon", beacon_list);
                    System.out.println("draw new beacon for tour");

                    addDrawing(dest_beacon.getName(), dest_beacon.vectors(),
                            dest_beacon.vector_order(), PURE_GREEN, 1, new Throb(WHITE, 3667));
                }
            }
        }
        // -- TOUR END -- //

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

    // IMPORTANT: for text associated with the same building, USE THE SAME NAME!!

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
            System.out.println("addDrawing WARNING: id \"" + id + "\" already exists. Call ignored.");
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
        return drawDirectory.remove(id) != null;
    }



    public boolean addText(String id, String text, Vector location, Vector color, float opacity) {
        if (!textDirectory.containsKey(id)) {
            textDirectory.put(id, new TextExecutor(text, location, color, opacity));
            return true;
        }
        else {
            System.out.println("addText WARNING: id \"" + id + "\" already exists. Call ignored.");
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
        return textDirectory.remove(id) != null;
    }


    public void initializeTour() {
        tour = new Tour();
    }

    public void addMapData(MapData mapData) {
        HashSet<Building> buildings = mapData.getBuildings();
        HashSet<Street> streets = mapData.getStreets();

        for (Building building : buildings) {

//            System.out.println(building.getName() + ": ");
//            System.out.println("vectors: " + building.vectors());
//            System.out.println("vector_order:" + building.vector_order());

            addDrawing(building.getName(), building.vectors(), building.vector_order(), building.getColor(), 1);
            Coordinate tcoord = building.getTextCoord();
            addText(building.getName(), building.getName(), Vector.of((float)tcoord.x,1.5f,(float)tcoord.z), WHITE, 1);
        }
        for (Street street : streets) {

//            System.out.println(street.getName() + ": ");
//            System.out.println("vectors: " + street.vectors());
//            System.out.println("vector_order:" + street.vector_order());

            addDrawing(street.getName(), street.vectors(), street.vector_order(), GRAY, 1);
        }

        // necessary for streetLock algorithm
        allSegments = new HashSet<>();
        for (Street str : streets) {
            List<Coordinate> coords = str.getCoordinates();
            allSegments.add(new Segment(
                    new Point((float)coords.get(0).x, (float)coords.get(0).z),
                    new Point((float)coords.get(1).x, (float)coords.get(1).z)));
        }


        // ===== ALL THE CODE BELOW WAS FOR TESTING ======//
//        //Beacon test draw
//        Coordinate beacon_coordinate = Coordinate.fromXZ(5,5);
//        ArrayList<Coordinate> beacon_list = new ArrayList<Coordinate>();
//        beacon_list.add(beacon_coordinate);
//        Beacon test_beacon = new Beacon("Test Beacon", beacon_list);
//
////        System.out.println(test_beacon.getName() + ": ");
////        System.out.println("vectors: " + test_beacon.vectors());
////        System.out.println("vector_order:" + test_beacon.vector_order());
//
//        addDrawing(test_beacon.getName(), test_beacon.vectors(), test_beacon.vector_order(), WHITE, 1);
//        //end of beacon test draw code
//
//        //chevron test draw
//        Coordinate chevron_coordinate = new Coordinate(DataDebug.HARDCODE_LAT + 0.0001, DataDebug.HARDCODE_LONG - 0.0005);
//        Coordinate three_coordinate = new Coordinate(DataDebug.HARDCODE_LAT - 0.0004, DataDebug.HARDCODE_LONG);
//        ArrayList<Coordinate> chevron_coor_list = new ArrayList<Coordinate>();
//        ArrayList<Coordinate> three_coor_list = new ArrayList<Coordinate>();
//        chevron_coor_list.add(chevron_coordinate);
//        three_coor_list.add(three_coordinate);
//        ThreeChevron test_chevron = new ThreeChevron("Test ThreeChevron", three_coor_list, 180.0f);
//        Chevron test_chev1 = new Chevron("Test Chevron1", chevron_coor_list, 180.0f);  //facing south at 0.0f
//
//        Coordinate one = new Coordinate(39.952699, -75.200927);
//        Coordinate two = new Coordinate(39.952702, -75.200938);
//        Coordinate three = new Coordinate(39.952704, -75.200951);
//
////        System.out.println("ONE: " + one.x + ", " + one.z);
////        System.out.println("TWO: " + two.x + ", " + two.z);
////        System.out.println("THREE: " + three.x + ", " + three.z);
////
////        System.out.println(test_chev1.getName() + ": ");
////        System.out.println("vectors: " + test_chev1.vectors());
////        System.out.println("vector_order:" + test_chev1.vector_order());
//
//        Chevron[] chev_list = test_chevron.chevron_list();
//
//        addDrawing(test_chev1.getName(), test_chev1.vectors(), test_chev1.vector_order(), PURE_GREEN, 1);
//
////        System.out.println(chev_list[0].getName() + ": ");
////        System.out.println("vectors: " + test_chevron.vectors().get(0));
////        System.out.println("vector_order:" + test_chevron.vector_order());
////
////        System.out.println(chev_list[1].getName() + ": ");
////        System.out.println("vectors: " + test_chevron.vectors().get(1));
////        System.out.println("vector_order:" + test_chevron.vector_order());
////
////        System.out.println(chev_list[2].getName() + ": ");
////        System.out.println("vectors: " + test_chevron.vectors().get(2));
////        System.out.println("vector_order:" + test_chevron.vector_order());
//
//        addDrawing(chev_list[0].getName(), test_chevron.vectors().get(0), test_chevron.vector_order(), PURE_GREEN, 1);
//        addDrawing(chev_list[1].getName(), test_chevron.vectors().get(1), test_chevron.vector_order(), PURE_GREEN, 1);
//        addDrawing(chev_list[2].getName(), test_chevron.vectors().get(2), test_chevron.vector_order(), PURE_GREEN, 1);
//
//        Coordinate chevron_coordinate_1 = new Coordinate(DataDebug.HARDCODE_LAT + 0.00006, DataDebug.HARDCODE_LONG - 0.0003);
//        Coordinate chevron_coordinate_2 = new Coordinate(DataDebug.HARDCODE_LAT + 0.00008, DataDebug.HARDCODE_LONG - 0.00033);
//        Coordinate chevron_coordinate_3 = new Coordinate(DataDebug.HARDCODE_LAT + 0.00010, DataDebug.HARDCODE_LONG - 0.00036);
//        ArrayList<Coordinate> chevron_list_1 = new ArrayList<Coordinate>();
//        ArrayList<Coordinate> chevron_list_2 = new ArrayList<Coordinate>();
//        ArrayList<Coordinate> chevron_list_3 = new ArrayList<Coordinate>();
//        chevron_list_1.add(chevron_coordinate_1);
//        chevron_list_2.add(chevron_coordinate_2);
//        chevron_list_3.add(chevron_coordinate_3);
//        Chevron test_chevron_1 = new Chevron("Test Chevron 1", chevron_list_1, 0.0f); //facing north
//        Chevron test_chevron_2 = new Chevron("Test Chevron 2", chevron_list_2, 0.0f); //facing north
//        Chevron test_chevron_3 = new Chevron("Test Chevron 3", chevron_list_3, 0.0f); //facing north
//
////        System.out.println(test_chevron.getName() + ": ");
////        System.out.println("vectors: " + test_chevron.vectors());
////        System.out.println("vector_order:" + test_chevron.vector_order());
//
//        addDrawing(test_chevron_1.getName(), test_chevron_1.vectors(), test_chevron_1.vector_order(), PURE_BLUE, 1,
//                new DrawEffect.Blink(0,2000,0,500));
//        addDrawing(test_chevron_2.getName(), test_chevron_2.vectors(), test_chevron_2.vector_order(), PURE_BLUE, 1,
//                new DrawEffect.Blink(0,2000,500,1000));
//        addDrawing(test_chevron_3.getName(), test_chevron_3.vectors(), test_chevron_3.vector_order(), PURE_BLUE, 1,
//                new DrawEffect.Blink(0,2000,1000,1500));
//
//        //end of chevron test draw
//
//        // MICHAEL: testing effects on beacons
//        Beacon effect1 = new Beacon("b1", new ArrayList<Coordinate>(){{add(new Coordinate(39.953, -75.203));}});
//        Beacon effect2 = new Beacon("b2", new ArrayList<Coordinate>(){{add(new Coordinate(39.9545, -75.2025));}});
//        addDrawing(effect1.getName(), effect1.vectors(), effect1.vector_order(), PURE_GREEN, 1,
//                new Throb(WHITE,3667));
//        addDrawing(effect2.getName(), effect2.vectors(), effect2.vector_order(), DARK_GRAY, 1,
//                new Blink(0,400,0,200));
        // END EFFECTS TESTING


        //===== FOR ANTHONY =====//
/*        Coordinate three_coordinate = new Coordinate(39.951609, -75.191695);
        ArrayList<Coordinate> three_coor_list = new ArrayList<Coordinate>();
        three_coor_list.add(three_coordinate);
        ThreeChevron test_chevron = new ThreeChevron("Test ThreeChevron", three_coor_list, 0f); // 0.of is south and goes counter clockwise

        drawThreeChevron(test_chevron, PURE_GREEN);
*/
        // ===== ALL THE CODE ABOVE IS FOR TESTING ======//
    }

    public void renderPath(Coordinate end) {
        HashSet<Path> path = mapData.getStreetsPath(getEyeCoord(), end);
        for (Path street : path) {
            //addDrawing(street.getName(), street.vectors(), street.vector_order(), WHITE, 1);

            int max_count = (int) (street.length() / 0.0001);
            System.out.println("renderpath " + street + " max_count " + max_count);
            int count = 0;
            Coordinate start = street.getCoordinates().get(0);
            Coordinate slope = Coordinate.subtract(street.getCoordinates().get(street.getCoordinates().size() - 1), start);
            float chevron_angle = 0f;
            if(Math.abs(slope.longitude) < 1e-8) {
                if(slope.latitude > 0) {
                    chevron_angle = 0f;
                }
                else {
                    chevron_angle = 180f;
                }
            }
            else {
                float angle = (float) Math.toDegrees(Math.atan(-slope.latitude / slope.longitude));
                if(slope.longitude > 0) {
                    chevron_angle = 90 - angle;
                }
                else {
                    chevron_angle = 270 - angle;
                }
            }
            slope = Coordinate.divide(slope, slope.dist(new Coordinate(0,0)));
            while(count < max_count) {
                Coordinate three_coordinate = Coordinate.add(start,Coordinate.mult(slope,0.0001*count));
                System.out.println("render chevron " + three_coordinate + " at angle " + chevron_angle);
                ArrayList<Coordinate> three_coor_list = new ArrayList<Coordinate>();
                three_coor_list.add(three_coordinate);
                ThreeChevron test_chevron = new ThreeChevron(street.getName()+"_"+count, three_coor_list, chevron_angle); // 0.of is south and goes counter clockwise
                drawThreeChevron(test_chevron, WHITE);
                count++;
            }
        }
    }

    public void removePath() {
        for(String key : drawDirectory.keySet()) {
            if (key.length() > 4 && key.substring(0, 5).equals("PATH_")) {
                System.out.println("removing " + key);
                removeDrawing(key);
            }
        }
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

        private void draw(float whiteCutoff, float grayCutoff) {
            Vector textToAdjustedLocation = Vector.difference(eye, location);
            Vector shortened = textToAdjustedLocation.scalarMultiply(
                    textToAdjustedLocation.magnitude() <= 1f ? 0f :
                            (textToAdjustedLocation.magnitude() - 1f) / textToAdjustedLocation.magnitude());
            Vector adjustedLocation = Vector.sum(location, shortened);

            // for some reason, the default size is absolutely ENORMOUS. Thankfully scaling seems
            // to work at any small order of magnitude
            float scale = calculateAdjustedTextSize(textToAdjustedLocation.magnitude());
            float textDist = textToAdjustedLocation.magnitude();
            float colorMult;

            if (textDist <= whiteCutoff) {
                colorMult = 1;
            }
            else if (textDist <= grayCutoff) {
                // at the graycutoff, colorMult is .5. Linear decrease from whiteCutoff until then.
                colorMult = 1 - (.5f * (textDist - whiteCutoff) / (grayCutoff - whiteCutoff));
            }
            else {
                return;
            }
            glText.begin(WHITE.x() * colorMult, WHITE.y() * colorMult, WHITE.z() * colorMult, opacity, mMVPMatrix);
            glText.setScale(scale);
            glText.drawC(text,
                    adjustedLocation.x(), adjustedLocation.y(), adjustedLocation.z(), // location
                    0,
                    (float)((currentAPR[0]+azimuthCorrection) * -180./Math.PI),
                    0); // rotation - text always directly faces user (azimuth only)

            glText.end();
        }
    }


    // methods
    private void drawAll() {
        drawAllShapes();
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        drawAllTextAtDistance();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
    private void drawAllShapes() {
        for (DrawExecutor dx : drawDirectory.values()) {
            dx.draw();
        }
    }
    private void drawAllTextAtDistance() {
        for (TextExecutor tx : textDirectory.values()) {
            tx.draw(50,325);
        }
    }
    private float calculateAdjustedTextSize(float distance) {
        // this is really some terrible code style, I apologize but it is 6am

        // ***if you need to change stuff, ONLY CHANGE d AND p, and the # of else-if calls***
        // this represents a linear-piecewise function
        int[] d = new int[] {5,20,50,100,400}; // distance anchors
        float[] p = new float[] {.5f,.42f,.3f,.09f,.015f}; // corresponding percent anchors (size)

        // DO NOT TOUCH - not that you want to, probably
        int ptr = 0;
        if (distance < 0) return -1;
        // # else-if = # anchors. First else-if returns TEXT_SCALE_CONSTANT. All other else-if lines are equal.
        else if (distance < d[ptr++]) return p[0] * TEXT_SCALE_CONSTANT;
        else if (distance < d[ptr++]) return (p[ptr-2]-((distance-d[ptr-2])*(p[ptr-2]-p[ptr-1])/(d[ptr-1]-d[ptr-2]))) * TEXT_SCALE_CONSTANT;
        else if (distance < d[ptr++]) return (p[ptr-2]-((distance-d[ptr-2])*(p[ptr-2]-p[ptr-1])/(d[ptr-1]-d[ptr-2]))) * TEXT_SCALE_CONSTANT;
        else if (distance < d[ptr++]) return (p[ptr-2]-((distance-d[ptr-2])*(p[ptr-2]-p[ptr-1])/(d[ptr-1]-d[ptr-2]))) * TEXT_SCALE_CONSTANT;
        else if (distance < d[ptr++]) return (p[ptr-2]-((distance-d[ptr-2])*(p[ptr-2]-p[ptr-1])/(d[ptr-1]-d[ptr-2]))) * TEXT_SCALE_CONSTANT;
        else                          return p[p.length-1] * TEXT_SCALE_CONSTANT;
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
        locationStatusDisplayString += "|DIR:" + ((int) (currentAPR[0] * 180 / (float) Math.PI));
        locationStatusDisplayString += " - " + Moverio3D.getDirectionFromAzimuth(currentAPR[0]).name();
        locationStatusDisplayString += "|CORR:" + ((int) (azimuthCorrection * 180 / (float) Math.PI));
        if (streetSwitch) locationStatusDisplayString += " ( ! STREETLOCK SWITCH ! ) ";
        if (compassRecenterNoEffect) locationStatusDisplayString += " ( ! CANNOT RECENTER ! ) ";

        // use cross to move the displayed string left on the screen
        Vector cross = Vector.cross(upV, toCoV).scalarMultiply(.15f);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        glText.draw(locationStatusDisplayString,

                eye.x() + toCoV.x() + cross.x(),
                eye.y() + toCoV.y() + cross.y(),
                eye.z() + toCoV.z() + cross.z(), // location

                0,
                (float) ((currentAPR[0] + azimuthCorrection) * -180. / Math.PI), // rotation - text always directly faces user (azimuth only)
                0
        );
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        glText.end();
    }






    /**
     * Modify the current eye value so that it is directly over a street. Picks a point on a street
     * such that no other point on any street is closer.
     * The private classes assume "rise" is z and "run" is x.
     */
    static float eps = .00001f;
    static class Point {
        float x;
        float z;
        Point(float x, float z) {
            this.x = x;
            this.z = z;
        }

        float distanceFrom(Point other) {
            return (float) Math.sqrt(Math.pow(other.x - x,2) + Math.pow(other.z - z,2));
        }

        /**
         * Suppose a line is drawn through this, with slope perpendicular to sg.
         * This method returns the intersection of that line with sg.
         * Null is returned if there is no intersection.
         * @param sg the segment to test with
         */
        Point perpendicularIntersectWithSegment(Segment sg) {
            float perpSlope = (sg.slope() == Float.POSITIVE_INFINITY) ? 0 :
                              (Math.abs(sg.slope()) < .00001) ? Float.POSITIVE_INFINITY :
                              -1 / sg.slope();
            // taken from wikipedia
            float x1 = sg.endpt1.x;
            float y1 = sg.endpt1.z;
            float x2 = sg.endpt2.x;
            float y2 = sg.endpt2.z;
            float x3 = x;
            float y3 = z;
            float x4 = perpSlope == Float.POSITIVE_INFINITY ? x : x + 1;
            float y4 = perpSlope == Float.POSITIVE_INFINITY ? z + 1 : z + perpSlope;
            // candidate point for isect
            Point cnd = new Point(

                    ((x1*y2 - y1*x2)*(x3-x4) - (x1-x2)*(x3*y4 - y3*x4)) /
                            ((x1-x2)*(y3-y4) - (y1-y2)*(x3-x4))

                    ,

                    ((x1*y2 - y1*x2)*(y3-y4) - (y1-y2)*(x3*y4 - y3*x4)) /
                            ((x1-x2)*(y3-y4) - (y1-y2)*(x3-x4))

            );
            // ensure possibleIsect is between sg's 2 endpts, with epsilon allowed
            // horizontal
            if (Math.abs(sg.endpt1.z - sg.endpt2.z) < eps) {
                if (cnd.x >= Math.min(sg.endpt1.x,sg.endpt2.x) - eps &&
                    cnd.x <= Math.max(sg.endpt1.x,sg.endpt2.x) + eps) {
                    return cnd;
                }
            }
            // vertical
            if (sg.slope() == Float.POSITIVE_INFINITY) {
                if (cnd.z >= Math.min(sg.endpt1.z,sg.endpt2.z) - eps &&
                    cnd.z <= Math.max(sg.endpt1.z,sg.endpt2.z) + eps) {
                    return cnd;
                }
            }
            // positive slope
            if (sg.slope() > 0) {
                Point r = (sg.endpt1.x > sg.endpt2.x) ? sg.endpt1 : sg.endpt2;
                Point l = (r == sg.endpt1) ? sg.endpt2 : sg.endpt1;
                if (cnd.x >= l.x - eps &&
                    cnd.x <= r.x + eps &&
                    cnd.z >= l.z - eps &&
                    cnd.z <= r.z + eps) {
                    return cnd;
                }
            }
            // negative slope
            if (sg.slope() < 0) {
                Point r = (sg.endpt1.x > sg.endpt2.x) ? sg.endpt1 : sg.endpt2;
                Point l = (r == sg.endpt1) ? sg.endpt2 : sg.endpt1;
                if (cnd.x >= l.x - eps &&
                    cnd.x <= r.x + eps &&
                    cnd.z <= l.z + eps &&
                    cnd.z >= r.z - eps) {
                    return cnd;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return "(" + x + "," + z + ")";
        }
    }
    /**
     * each Segment represents a street. After initial Segment generation from MapData, DO NOT
     * generate new segments; reuse old ones. Segments will be tested for pointer equality (==) to
     * determine if they represent particular streets
     */
    static class Segment {
        Point endpt1;
        Point endpt2;

        Segment(Point a, Point b) {
            endpt1 = a;
            endpt2 = b;
        }

        float slope() {
            if (Math.abs(endpt1.x - endpt2.x) < eps) return Float.POSITIVE_INFINITY;
            return (endpt1.z - endpt2.z)/(endpt1.x - endpt2.x);
        }

        @Override
        public String toString() {
            return "[(" + endpt1.x + "," + endpt1.z + ")(" +
                    endpt2.x + "," + endpt2.z + ")]";
        }
    }
    static class PtSegTuple {
        Point pt;
        Segment sg;
        PtSegTuple(Point p, Segment s) {
            pt = p;
            sg = s;
        }

        @Override
        public String toString() {
            return pt.toString() + "|" + sg.toString();
        }
    }
    private Segment currStreet;
    private Segment candidateStreet;
    private float lastDistanceFromCurrStreet;
    private int numberOfFixesOffCurrStreet; // increments as long as new fixes are farther away from the old street
    private boolean streetSwitch = false;
    private boolean stlock_eyePosUpdated = false;
    private Set<Segment> allSegments = null; // this is set in addMapData
    private void streetLock() {
        /*
        Algorithm:
         - represent each street as a line (endpt1, slope, endpt2)
         - for each street-line:
           - get perpendicular slope
           - run line with such a slope through eye
           - if this line intersects the street SEGMENT (calculcate intersection, check either
             endpt1<=isect<=endpt2 || endpt2<=isect<=endpt1, with epsilon) AND isect is closer than
             all previously discovered isects, update candidate
           - if it doesn't intersect the segment, calculate whichever endpt is closer. If the closest
             endpt is closer than all previously discovered candidates, update candidate. (easy to
             prove that a point that doesn't perpendicularly intersect a segment has one of the endpts
             as a closest point)
           - ! in either case above, save the segment (pointer) as well This is necessary to properly
             test the edge case (below)
         - after all street-lines have been analyzed, set eye to candidate if edge case passed (below)

         NOTES:
         There's an edge case involving coming up to an intersection, if GPS accuracy is low. To fix,
         do not switch to a new street unless 3 consecutive points are (A) on a new street AND (B) monotonically
         increasing farther away from old street. (Need a variable to hold recentClosestStreet as well)
         If edge case does not pass filter, just calculate closest pt on current street, given eye.

         Expose a static method that takes in a set of segments and a point, and returns the closest
         point that is on a segment and also returns the segment.
         */

        Point eyePt = new Point(eye.x(), eye.z());

        PtSegTuple ptseg = getClosestPointOnSomeSegment(allSegments, eyePt);

        // do not bother with any below logic if the eye hasn't moved (e.g. no GPS update has arrived yet)
        if (!stlock_eyePosUpdated) {
            eye = Vector.of(ptseg.pt.x, eye.y(), ptseg.pt.z);
            return;
        }
        stlock_eyePosUpdated = false;
//        System.out.println(" !!!!! STLOCK ALGORITHM BEING PERFORMED NOW...");

        if (currStreet == null || currStreet == ptseg.sg) {
            currStreet = ptseg.sg;
            candidateStreet = null;
            lastDistanceFromCurrStreet = 0;
            numberOfFixesOffCurrStreet = 0;
            eye = Vector.of(ptseg.pt.x, eye.y(), ptseg.pt.z);
            streetSwitch = false;
        }
        else {
//            System.out.println(" !!!!! NOT ON CURRENT STREET...");
//            System.out.println(" curr: " + currStreet);
//            System.out.println(" cand: " + candidateStreet);
//            System.out.println(" new: " + ptseg);
            Point currFix = getClosestPointOnSpecificSegment(currStreet, eyePt);
            if (ptseg.sg == candidateStreet) {
//                System.out.println(" !!! numberFixes: " + numberOfFixesOffCurrStreet);
                if (++numberOfFixesOffCurrStreet >= 4) { ///////// MAGIC NUMBER. This number is 1 GREATER than
                                                                // the number of monotonically increasing distance fixes needed
                    currStreet = candidateStreet;
                    candidateStreet = null;
                    lastDistanceFromCurrStreet = 0;
                    numberOfFixesOffCurrStreet = 0;
                    eye = Vector.of(ptseg.pt.x, eye.y(), ptseg.pt.z);
                    streetSwitch = true;
                }
                else {
                    float newDist = eyePt.distanceFrom(currFix);
                    if (newDist < lastDistanceFromCurrStreet) {
                        numberOfFixesOffCurrStreet = 1;
                    }
                    lastDistanceFromCurrStreet = newDist;
                    eye = Vector.of(currFix.x, eye.y(), currFix.z);
                    streetSwitch = false;
                }
            }
            else {
//                System.out.println(" !!! setting new candidateStreet");
                candidateStreet = ptseg.sg;
                lastDistanceFromCurrStreet = eyePt.distanceFrom(currFix);
                numberOfFixesOffCurrStreet = 1;
                eye = Vector.of(currFix.x, eye.y(), currFix.z);
                streetSwitch = false;
            }
        }

    }
    static PtSegTuple getClosestPointOnSomeSegment(Set<Segment> segments, Point inputPt) {
        Segment closestSegSoFar = null;
        Point closestPtSoFar = null;

        for (Segment sg : segments) {
            Point ptOnSg = getClosestPointOnSpecificSegment(sg, inputPt);
            if (closestSegSoFar == null ||
                inputPt.distanceFrom(ptOnSg) < inputPt.distanceFrom(closestPtSoFar)) {
                closestSegSoFar = sg;
                closestPtSoFar = ptOnSg;
            }
        }

        return new PtSegTuple(closestPtSoFar, closestSegSoFar);
    }
    // if perpendicular point is null, return the closer endpoint
    static Point getClosestPointOnSpecificSegment(Segment segment, Point inputPt) {
        Point perpIsect = inputPt.perpendicularIntersectWithSegment(segment);
        if (perpIsect != null) {
            return perpIsect;
        }
        else {
            float d1 = inputPt.distanceFrom(segment.endpt1);
            float d2 = inputPt.distanceFrom(segment.endpt2);
            if (d1 < d2) return segment.endpt1;
            else return segment.endpt2;
        }
    }


    int headingFixes = 0;
    final int REQUIRED_NUMBER_OF_HEADING_FIXES = 2;
    boolean towardsEndpt1 = false;
    boolean towardsEndpt2 = false;
    float lastEndpt1Dist = -1;
    boolean compassRecenter_eyePosUpdated = false;
    boolean compassRecenterNoEffect = false;
    private void headingDetectionService() {

        if (!compassRecenter_eyePosUpdated) return; // skip if no location update
        compassRecenter_eyePosUpdated = false;
        if (streetSwitch) { // reset when streetlock, which was called right before this, indiates switch
            headingFixes = 0;
            towardsEndpt1 = false;
            towardsEndpt2 = false;
            lastEndpt1Dist = -1;
        }


        if (candidateStreet == null && currStreet != null) {
            Point eyePt = new Point(eye.x(), eye.z());
            float newDist = currStreet.endpt1.distanceFrom(eyePt);
            if (lastEndpt1Dist == -1) {
                lastEndpt1Dist = newDist;
            }
            else if (newDist < lastEndpt1Dist - .4) { // getting closer to endpt1 (40cm between fix)
                lastEndpt1Dist = newDist;
                if (!towardsEndpt1) {
                    headingFixes = 0;
                    towardsEndpt1 = true;
                    towardsEndpt2 = false;
                }
                else {
                    headingFixes = Math.min(REQUIRED_NUMBER_OF_HEADING_FIXES,headingFixes+1);
                }
            }
            else if (newDist > lastEndpt1Dist + .4) { // getting farther away from endpt1
                lastEndpt1Dist = newDist;
                if (!towardsEndpt2) {
                    headingFixes = 0;
                    towardsEndpt1 = false;
                    towardsEndpt2 = true;
                }
                else {
                    headingFixes = Math.min(REQUIRED_NUMBER_OF_HEADING_FIXES,headingFixes+1);
                }
            }
            // don't do anything for tiny movements
        }

    }
    public void recenterCompass() {
        if (headingFixes < REQUIRED_NUMBER_OF_HEADING_FIXES) {
            compassRecenterNoEffect = true;
            return;
        }
        compassRecenterNoEffect = false;

        Point currentHeadingEndpt;
        if (towardsEndpt1) {
            currentHeadingEndpt = currStreet.endpt1;
        }
        else if (towardsEndpt2) {
            currentHeadingEndpt = currStreet.endpt2;
        }
        else {
            System.out.println(" (!) STATE ERROR IN recenterCompass");
            return;
        }

        float xDiff = currentHeadingEndpt.x - eye.x();
        float zDiff = currentHeadingEndpt.z - eye.z();
        float correctHeading = (float)Math.atan2(xDiff,-zDiff);
        azimuthCorrection = correctHeading - currentAPR[0];
        pitchCorrection = -currentAPR[1];
        rollCorrection = -currentAPR[2];

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

    // ==== Media Player Functions ==== //

    //play the clip given a context and Uri file
    public void playClip(int num){
        if (Tour.TOUR_MODE == Tour.TourMode.DEMO){
            switch (num) {
                case 0:
                    //mp = MediaPlayer.create(ctxt, R.raw.engineering);
                    break;
                case 1:
                    //mp = MediaPlayer.create(ctxt, R.raw.chemistry);
                    break;
                case 2:
                    //mp = MediaPlayer.create(ctxt, R.raw.music);
                    break;
                default:
                    System.out.println("no more clips to play");
                    break;
            }
        } else {
            // FOR CAMPUS TOUR
            switch (num) {
                case 0:
                    //mp = MediaPlayer.create(ctxt, R.raw.intro);
                    mp = MediaPlayer.create(ctxt, R.raw.jess_theme);
                    break;
                case 1:
                    //mp = MediaPlayer.create(ctxt, R.raw.button);
                    break;
                case 2:
                    //mp = MediaPlayer.create(ctxt, R.raw.arch);
                    break;
                case 3:
                    //mp = MediaPlayer.create(ctxt, R.raw.locust);
                    break;
                case 4:
                    //mp = MediaPlayer.create(ctxt, R.raw.compass);
                    break;
                case 5:
                    //mp = MediaPlayer.create(ctxt, R.raw.huntsman);
                    break;
                case 6:
                    //mp = MediaPlayer.create(ctxt, R.raw.1920);
                    break;
                case 7:
                    //mp = MediaPlayer.create(ctxt, R.raw.covenant);
                    break;
                default:
                    System.out.println("no more clips to play");
                    break;
            }
        }
        if(mp != null) {
            mp.setLooping(false);
            mp.start();
        }
    }

    //end the clip, release the resources for the media player
    public void endClip() {
        if(mp.isPlaying()){
            mp.stop();
        }
        mp.release();
        mp = null;
    }

    public void drawThreeChevron(ThreeChevron t, Vector color) {

        addDrawing(t.chev_one.getName(), t.chev_one.vectors(), t.chev_one.vector_order(), color, 1,
                new DrawEffect.Blink(0, 2000, 1000, 1500));
        addDrawing(t.chev_two.getName(), t.chev_two.vectors(), t.chev_two.vector_order(), color, 1,
                new DrawEffect.Blink(0, 2000, 500, 1000));
        addDrawing(t.chev_three.getName(), t.chev_three.vectors(), t.chev_three.vector_order(), color, 1,
                new DrawEffect.Blink(0, 2000, 0, 500));
    }

}
