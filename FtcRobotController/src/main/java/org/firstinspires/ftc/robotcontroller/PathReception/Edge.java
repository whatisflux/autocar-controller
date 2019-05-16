package org.firstinspires.ftc.robotcontroller.PathReception;

import java.util.ArrayList;

public class Edge {
    public ArrayList<Waypoint> waypoints;
    public Boolean isClosed;

    Edge() {
        waypoints = new ArrayList<>();
        isClosed = false;
    }
}
