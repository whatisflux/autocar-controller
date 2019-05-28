package org.firstinspires.ftc.robotcontroller.Vision;


import android.util.Log;

import org.firstinspires.ftc.robotcontroller.PathReception.PathPoint;
import org.opencv.core.Point;

import java.util.ArrayList;

public class Vision {

    public static ArrayList<Point> allPathPoints = new ArrayList<>();
    /**
     * Gets the midpoints of the current path
     */
    public static ArrayList<PathPoint> getCurrentPath(){
        ArrayList<PathPoint> returnMe = new ArrayList<>();
        for(Point p : allPathPoints){
            returnMe.add(new PathPoint(p.x,p.y));
        }

        allPathPoints.clear();
        return returnMe;
    }
    public native static void readBallPattern(long addrRgba, double debug1, double debug2, double debug3);
    public native static void readBallPattern(long addrRgba, double debug1, double debug2, double debug3,
                                              double[] xPositions,double[] yPositions);
}
