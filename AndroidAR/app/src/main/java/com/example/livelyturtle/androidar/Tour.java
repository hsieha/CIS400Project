package com.example.livelyturtle.androidar;

import java.util.LinkedList;
import com.example.livelyturtle.androidar.Coordinate;
import java.util.ListIterator;

/**
 * Created by Darren on 3/21/16.
 */
public class Tour {

    LinkedList<Coordinate> tour_queue = new LinkedList<Coordinate>();
    ListIterator<Coordinate> iterator;

    public enum TourMode {
        DEMO,
        CAMPUS
    }

    public static final TourMode TOUR_MODE = TourMode.CAMPUS;


    public Tour(){

        if(TOUR_MODE == TourMode.CAMPUS) {
            tour_queue.add(new Coordinate(39.952749, -75.192248));  // 34th/Walnut
            tour_queue.add(new Coordinate(39.952155, -75.193684));  // Button
            tour_queue.add(new Coordinate(39.952749, -75.192248));  // Arch
            tour_queue.add(new Coordinate(39.952148, -75.196047));  // Steiny D.
            tour_queue.add(new Coordinate(39.952259, -75.197009));  // Compass + Ben on Bench
            tour_queue.add(new Coordinate(39.952415, -75.198175));  // Huntsman
            tour_queue.add(new Coordinate(39.952550, -75.199369));  // 1920 Commons
            tour_queue.add(new Coordinate(39.952724, -75.200779));  //The Convenant (Dueling Tampons)

        }
        else if (TOUR_MODE == TourMode.DEMO){

            tour_queue.add(new Coordinate(39.951485, -75.190791));  // Smith Walk top of Stairs
            tour_queue.add(new Coordinate(39.951609, -75.191695));  // Smith / Front of Town
            tour_queue.add(new Coordinate(39.952249, -75.191561));  // Chancellor st / front of levine

        }

        iterator = tour_queue.listIterator(0);
    }

    //returns coordinate if it has a next coordinate, otherwise return null
    public Coordinate next(){
        if(iterator.hasNext()){
            return iterator.next();
        }
        return null;
    }

}
