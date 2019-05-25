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

                //parse the bytes
                PathParser p = new PathParser(receivePacket.getData());

                Path path = p.Parse();
                FinalPathWrapper.findMidPoints(path);
                Log.d("ERROR_LOG","received: " + path.toString());


            }
        }catch (Exception e){
        }
    }
}
