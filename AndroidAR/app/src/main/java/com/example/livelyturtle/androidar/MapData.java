package com.example.livelyturtle.androidar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

import android.app.Activity;

/**
 * Created by LivelyTurtle on 1/27/2016.
 */
public class MapData {
    public enum DataType {
        BUILDING, STREET, POI
    }

    HashSet<Building> buildings;
    HashSet<Street> streets;
    HashSet<POI> pois;

    // Parsing a .kml file from Google Maps
    public MapData(String filename, Activity activity) {
        String line = null;
        buildings = new HashSet<Building>();
        streets = new HashSet<Street>();
        pois = new HashSet<POI>();
        try {
            InputStream iS = activity.getAssets().open("UPennCampus.kml");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(iS));
            DataType type = DataType.BUILDING;
            String name = "";
            ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
            float height = 10f;
            while((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if(line.equals("<name>Buildings</name>")) {
                    type = DataType.BUILDING;
                }
                else if(line.equals("<name>Streets</name>")) {
                    type = DataType.STREET;
                }
                else if(line.equals("<name>POI</name>")) {
                    type = DataType.POI;
                }
                else if(line.startsWith("<name>")) {
                    name = line.substring(6, line.length() - 7);
                }
                else if(line.startsWith("<description>")) {
                    if(line.indexOf("h:") >= 0) {
                        height = Float.parseFloat(line.substring(line.indexOf("h:") + 2, line.indexOf(']')));
                    }
                }
                else if(line.startsWith("<coordinates>")) {
                    coordinates = new ArrayList<Coordinate>();
                    String coordString = line.substring(13, line.length() - 14);
                    for(String oneCoord : coordString.split(",0.0 ")){
                        String[] latlong = oneCoord.split(",");
                        Coordinate coord = new Coordinate(Double.parseDouble(latlong[1]),
                                Double.parseDouble(latlong[0]));
                        coordinates.add(coord);
                    }
                    if(type == DataType.BUILDING) {
                        buildings.add(new Building(name,coordinates, height));
                        System.out.println("building " + name + " has height " + height);
                        height = 10f;
                    }
                    else if(type == DataType.STREET) {
                        for(int i = 0; i < coordinates.size()-1; i++) {
                            ArrayList<Coordinate> partialCoord = new ArrayList<Coordinate>(coordinates.subList(i,i+2));
                            streets.add(new Street(name+"_"+i, partialCoord));
                        }
                        //streets.add(new Street(name, coordinates));
                    }
                    else {
                        pois.add(new POI(name, coordinates));
                    }
                }
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to find " + filename);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void addData(WorldObject object) {
        if(object instanceof Building) {
            buildings.add((Building) object);
        }
        else if(object instanceof Street) {
            streets.add((Street) object);
        }
        else {
            throw new IllegalArgumentException("Object not of type Building or Street");
        }
    }

    public HashSet<Building> getBuildings() {
        return buildings;
    }

    public HashSet<Street> getStreets() {
        return streets;
    }

    private class Node implements Comparable<Node> {
        Coordinate c;
        Street s0;
        Street s1;
        HashSet<Node> neighbors;
        Node end;

        public Node() {
            c = null;
            s0 = null;
            s1 = null;
            neighbors = new HashSet<Node>();
            end = null;
        }

        public Node(Coordinate c, Street s0, Street s1, Node end) {
            this.c = c;
            this.s0 = s0;
            this.s1 = s1;
            this.end = end;
            neighbors = new HashSet<Node>();
        }

        public double dist() {
            return c.dist(end.c);
        }

        @Override
        public int compareTo(Node other) {
            Double dist = dist();
            return dist.compareTo(other.dist());
        }

        @Override
        public String toString() {
            return s0.name + "_" + s1.name + "_" + c;
        }
    }

    private class PQElem implements Comparable<PQElem> {
        Node cur;
        ArrayList<Node> path;
        Double pathDist;

        public PQElem(ArrayList<Node> path, Node cur, double dist) {
            this.cur = cur;
            this.path = path;
            this.pathDist = dist;
        }

        @Override
        public int compareTo(PQElem other) {
            Double total = pathDist + cur.dist();
            return total.compareTo(other.pathDist+other.cur.dist());
        }

        @Override
        public String toString() {
            return cur.toString();
        }
    }

    // returns a hashset of streets to be drawn
    public HashSet<Path> getStreetsPath(Coordinate start, Coordinate end) {
        Node startStreetNode = coordToNode(start);
        Node endStreetNode = coordToNode(end);
        startStreetNode.end = endStreetNode;
        endStreetNode.end = endStreetNode;
        HashSet<Node> nodes = initNodes(startStreetNode, endStreetNode);
        if(nodes == null) {
            System.out.println("MapData: getStreetPath - nodes are null");
            return null;
        }
        System.out.println("startStreetNode: " + startStreetNode);
        System.out.println("endStreetNode: " + endStreetNode);
        ArrayList<Node> path = getPath(startStreetNode, endStreetNode);
        if(path == null) {
            System.out.println("MapData: getStreetPath - path are null");
            return null;
        }
        System.out.println("MapData: path - " + path);
        HashSet<Path> streetPath = new HashSet<>();
        for(int i = 0; i < path.size()-1; i++) {
            ArrayList<Coordinate> coord = new ArrayList<>();
            Node n0 = path.get(i);
            Node n1 = path.get(i+1);
            coord.add(n0.c);
            coord.add(n1.c);
            Path s = new Path("PATH_"+n0.s0.name+n0.s1.name+"_"+n1.s0.name+n1.s1.name,coord);
            streetPath.add(s);
        }
        ArrayList<Coordinate> coordStart = new ArrayList<>();
        coordStart.add(start);
        coordStart.add(startStreetNode.c);
        Path first = new Path("PATH_start_"+startStreetNode.s0.name,coordStart);
        ArrayList<Coordinate> coordEnd = new ArrayList<>();
        coordEnd.add(endStreetNode.c);
        coordEnd.add(end);
        Path last = new Path("PATH_end_"+endStreetNode.s0.name,coordEnd);
        streetPath.add(first);
        streetPath.add(last);
        return streetPath;
    }

    // a* - returns an arraylist of nodes in the path
    private ArrayList<Node> getPath(Node startNode, Node endNode) {
        PriorityQueue<PQElem> pq = new PriorityQueue<>();
        HashSet<Node> seen = new HashSet<Node>();
        seen.add(startNode);
        ArrayList<Node> startPath = new ArrayList<Node>();
        startPath.add(startNode);
        pq.add(new PQElem(startPath, startNode, 0));
        while(!pq.isEmpty()) {
            PQElem elem = pq.poll();
            if(elem.cur.equals(endNode)){
                return elem.path;
            }
            HashSet<Node> neighbors = elem.cur.neighbors;
            System.out.println("neighbors for " + elem.cur + ": " + neighbors);
            for(Node neighbor : neighbors) {
                if(seen.contains(neighbor)) {
                    continue;
                }
                seen.add(neighbor);
                ArrayList<Node> newPath = new ArrayList<Node>(elem.path);
                newPath.add(neighbor);
                pq.add(new PQElem(newPath, neighbor, elem.pathDist+elem.cur.c.dist(neighbor.c)));
            }
        }
        System.out.println("Did not find path");
        return null;
    }

    private HashSet<Node> initNodes(Node startNode, Node endNode) {
        HashSet<Coordinate> cIntersection = new HashSet<Coordinate>();
        HashSet<Node> nodes = new HashSet<Node>();
        nodes.add(startNode);
        nodes.add(endNode);
        for(Street street : streets) {
            for (Street other : streets) {
                if (street.equals(other)) {
                    continue;
                }
                Coordinate c = street.findIntersection(other);
                if (c != null && !cIntersection.contains(c)) {
                    cIntersection.add(c);
                    nodes.add(new Node(c, street, other, endNode));
                }
            }

        }
        for(Node node : nodes) {
            for(Node other : nodes) {
                if(node.equals(other)) {
                    continue;
                }
                if(node.s0.equals(other.s0) || node.s0.equals(other.s1) ||
                        node.s1.equals(other.s0) || node.s1.equals(other.s1)) {
                    node.neighbors.add(other);
                    other.neighbors.add(node);
                }
            }
        }
        System.out.println("endNode neighbors: " + endNode.neighbors);
        return nodes;
    }

    // create node of coord closest to nearest street
    private Node coordToNode(Coordinate coord) {
        Node n = new Node();
        double bestDist = Double.POSITIVE_INFINITY;
        for(Street street : streets) {
            Coordinate pot = pointNearestStreet(coord,street);
            if(bestDist > pot.dist(coord)){
                bestDist = pot.dist(coord);
                n.c = pot;
                n.s0 = street;
                n.s1 = street;
                // end is initialized somewhere else
            }
        }
        return n;
    }

    private Coordinate pointNearestStreet(Coordinate point, Street wall) {
        double bestDist = Double.POSITIVE_INFINITY;
        Coordinate bestC = null;
        for(int i = 0; i < wall.getCoordinates().size()-1; i++) {
            Coordinate v = wall.getCoordinates().get(i);
            Coordinate w = wall.getCoordinates().get(i+1);
            Coordinate p = point;
            double l2 = Math.pow(v.dist(w), 2);
            if (l2 == 0) {
                if(bestDist > point.dist(v)) {
                    bestDist = point.dist(v);
                    bestC = v;
                }
            }
            else {
                double t = Math.max(0, Math.min(1, Coordinate.dot(Coordinate.subtract(p, v), Coordinate.subtract(w, v)) / l2));
                Coordinate ans = Coordinate.add(v, Coordinate.mult(Coordinate.subtract(w, v), t));
                if (bestDist > point.dist(ans)) {
                    bestDist = point.dist(ans);
                    bestC = ans;
                }
            }
        }
        return bestC;
    }
}
