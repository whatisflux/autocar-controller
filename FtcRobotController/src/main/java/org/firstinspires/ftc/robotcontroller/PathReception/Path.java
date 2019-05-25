package org.firstinspires.ftc.robotcontroller.PathReception;


public class Path {
    public Edge edge1;
    public Edge edge2;

    Path(Edge edge1, Edge edge2) {
        this.edge1 = edge1;
        this.edge2 = edge2;
    }


    /**
     * Converts to a string
     * @return
     */
    public String toString(){
        return "edge1: " + edge1.toString() + " edge2: " + edge2.toString();
    }

}
