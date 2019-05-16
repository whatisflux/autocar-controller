package org.firstinspires.ftc.robotcontroller.PathReception;


import java.util.ArrayList;


public class PathInterpreter {
    //the udp client
    private UdpClient udpClient;



    //all the path points
    private ArrayList<PathPoint> allPathPoints = new ArrayList<>();

    /**
     * Creates a new path interpreter
     */
    public PathInterpreter(){
        udpClient = new UdpClient(1235);
        //start the receiver
        new Thread(udpClient).start();
    }
}
