package org.firstinspires.ftc.robotcontroller.PathReception;

import android.animation.PointFEvaluator;
import android.graphics.PointF;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Receives data from the server
 */
public class UdpClient implements Runnable{


    //the port we are using
    private int serverPort = 0;

    /**
     * Creates a new udp client
     */
    public UdpClient(int port){
        serverPort = port;//remember the port
    }

    @Override
    public void run() {
        try{
            DatagramSocket serverSocket = new DatagramSocket(serverPort);
            byte[] receiveData = new byte[4096];
            while(true)
            {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String received = new String( receivePacket.getData());
//                Log.d("ERROR_LOG","RECEIVED: " + received);
//                interpretPath("ep         p    ?�   p�        zep?�      �p?�  ?�   p?       �");
            }
        }catch (Exception e){
        }
    }

//    /**
//     * Converts a path to points
//     * @param received the received string from Craig
//     */
//    private void interpretPath(String received) {
//        char[] chars = received.toCharArray();
//
//        //the edge starts with an e
//        int indexFirstEdgeStart = received.indexOf("e")+1;
//        Log.d("ERROR_LOG", "indexFirstEdgeStart: " + indexFirstEdgeStart);
//
//        int indexFirstEdgeEnd = received.substring(indexFirstEdgeStart).indexOf("e")
//                + indexFirstEdgeStart;
//        Log.d("ERROR_LOG", "indexFirstEdgeEnd: " + indexFirstEdgeEnd);
//
//
//        //parse the first edge into points
//        ArrayList<PointF> edge1Points = parseEdge(
//                received.substring(indexFirstEdgeStart,indexFirstEdgeEnd));
//        //do the same for the second edge
//        ArrayList<PointF> edge2Points = parseEdge(
//                received.substring(indexFirstEdgeEnd));
//
//
//        Log.d("ERROR_LOG", edge1Points.toString() + "," + edge2Points.toString());
//    }
//
//    /**
//     * Parses the serialized edge into the points
//     * @param edgeSerialized the serialized
//     */
//    private ArrayList<PointF> parseEdge(String edgeSerialized) {
//        //contains all the points along this edge
//        ArrayList<PointF> allPoints = new ArrayList<>();
//
//        Log.d("ERROR_LOG", "edgeSerialized: " + edgeSerialized);
//
//        while (true) {
//
//            //add the processed point but the first char is a p so don't include that
//            allPoints.add(parseWayPoint(edgeSerialized.substring(1,11)));
//        }
//
//        return allPoints;
//    }
//
//    /**
//     * Parses a serialized waypoint
//     * @param serialized the serialized two points
//     */
//    private PointF parseWayPoint(String serialized){
//        Log.d("ERROR_LOG", "wayPointSerialized: " + serialized);
//
//        return new PointF(parseFloat(serialized.substring(0,4).toCharArray()),
//                parseFloat(serialized.substring(4).toCharArray()));
//    }
//
//
//
//    /**
//     * Converts a char[] that is the bits of a float to the original float
//     * @param chars the char[]
//     * @return the original float
//     */
//    private float parseFloat(char[] chars){
//        Log.d("ERROR_LOG", "float serialized: " + Arrays.toString(chars));
//
//        int bits = 0;
//        for(int c = 0; c < 4; c++){
//            bits += ((int) chars[c]) << (8 * (3 - c));
//        }
//        return Float.intBitsToFloat(bits);
//    }
}
