package org.firstinspires.ftc.robotcontroller.PathReception;


import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class FinalPathWrapper {

    //the udp client
    private UdpClient udpClient;



    public static Semaphore pathPointsLock = new Semaphore(1);
    //all the path points
    private static ArrayList<PathPoint> allPathPoints = new ArrayList<>();


    /**
     * Creates a new path interpreter
     */
    public FinalPathWrapper(){
        udpClient = new UdpClient(1235);
        //start the receiver
        new Thread(udpClient).start();
    }

    /**
     * Converts Craig's path points to midpoints
     * @param parse the path parsed
     */
    public static void findMidPoints(Path parse) {
        //create a temporary arraylist
        ArrayList<PathPoint> tempPath = new ArrayList<>();

        for(int i = 0; i < parse.edge1.waypoints.size(); i ++){
            Waypoint left = parse.edge1.waypoints.get(i);
            Waypoint right = parse.edge2.waypoints.get(i);
            tempPath.add(new PathPoint(left,right));
        }
        try{
            pathPointsLock.acquire();
            allPathPoints = tempPath;
            pathPointsLock.release();
        }catch(Exception e){
            Log.d("ERROR_LOG","can't acquire path lock\n" +  e.toString());
        }
    }

    /**
     * Gets the midpoints of the current path
     */
    public static ArrayList<PathPoint> getCurrentPath(){
        try{
            pathPointsLock.acquire();
            ArrayList<PathPoint> copy = (ArrayList<PathPoint>) allPathPoints.clone();
            pathPointsLock.release();
            return copy;
        }catch(Exception e){
            Log.d("ERROR_LOG","can't get current path\n" +  e.toString());
        }
        return null;
    }
}
