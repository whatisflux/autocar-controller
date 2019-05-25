package org.firstinspires.ftc.robotcontroller.PathReception;

import java.util.ArrayList;

public class Edge {
    public ArrayList<Waypoint> waypoints;
    public Boolean isClosed;

    Edge() {
        waypoints = new ArrayList<>();
        isClosed = false;
    }

    /**
     * Converts to a string
     * @return a string
     */
    public String toString(){
        return waypoints.toString() + " closed: " + isClosed;
    }
}
