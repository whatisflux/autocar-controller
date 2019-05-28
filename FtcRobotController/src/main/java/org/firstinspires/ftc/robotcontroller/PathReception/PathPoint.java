package org.firstinspires.ftc.robotcontroller.PathReception;

import static org.firstinspires.ftc.robotcontroller.Odometry.LocationVars.worldAngle_rad;
import static org.firstinspires.ftc.robotcontroller.Odometry.LocationVars.worldXPosition;
import static org.firstinspires.ftc.robotcontroller.Odometry.LocationVars.worldYPosition;

/**
 * Defines the path we are following
 */
public class PathPoint {
    public double x;
    public double y;

    /**
     * Creates a new path point
     * @param x world x coordinate the robot should go through
     * @param y world y coordinate
     */
    public PathPoint(double x, double y){
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a new PathPoint in world coordinates
     * @param left the left waypoint
     * @param right the right waypoint
     */
    public PathPoint(Waypoint left, Waypoint right) {
        double relativeX = (left.x + right.x)/2;
        relativeX -= 5.34;
        double relativeY = (left.y + right.y)/2;
        double distanceAway = Math.hypot(relativeX,relativeY);
        double relativeAngle = Math.atan2(relativeY,-relativeX);
        this.x = worldXPosition + Math.sin(-worldAngle_rad + relativeAngle) * distanceAway;
        this.y = worldYPosition + Math.cos(-worldAngle_rad + relativeAngle) * distanceAway;
    }
}
