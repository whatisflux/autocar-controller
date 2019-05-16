package org.firstinspires.ftc.robotcontroller.PathReception;

public class PathPoint {
    public double xPosition;
    public double yPosition;

    /**
     * Creates a new path point
     * @param x the relative x position to the robot
     * @param y the relative y position to the robot
     */
    public PathPoint(double x, double y){
        xPosition = x;
        yPosition = y;
    }
}
