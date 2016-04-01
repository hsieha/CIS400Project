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

    public static final TourMode TOUR_MODE = TourMode.DEMO;


    public Tour(){

        //Assume you start at 33rd and Walnut
        //39.952500, -75.190194
        if(TOUR_MODE == TourMode.CAMPUS) {
            tour_queue.add(new Coordinate(39.952480, -75.190554));    //ENIAC in Moore
            tour_queue.add(new Coordinate(39.952159, -75.193682));    //College Green
            tour_queue.add(new Coordinate(39.952135, -75.195205));    //Arch Building
            tour_queue.add(new Coordinate(39.952258, -75.197017));    //Compass + Ben Statue
            tour_queue.add(new Coordinate(39.952557, -75.198213));    //Huntsman
            tour_queue.add(new Coordinate(39.952717, -75.200666));    //Covenant (Dueling Tampons)
        }
        //Assume you start at 33rd and Walnut
        //39.952500, -75.190194
        else if (TOUR_MODE == TourMode.DEMO){

            tour_queue.add(new Coordinate(39.952556, -75.190040));
            tour_queue.add(new Coordinate(39.952673, -75.190997));
            tour_queue.add(new Coordinate(39.952812, -75.192101));

            //tour_queue.add(new Coordinate(39.952480, -75.190554));  //ENIAC in Moore
            //tour_queue.add(new Coordinate(39.952735, -75.192033));  //End of street, 34th/Walnut
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
