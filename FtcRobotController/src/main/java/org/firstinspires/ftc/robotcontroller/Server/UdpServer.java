package org.firstinspires.ftc.robotcontroller.Server;


import android.os.SystemClock;
import android.util.Log;

import com.qualcomm.robotcore.util.Range;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Semaphore;

/**
 * Sends the udp data
 */
public class UdpServer implements Runnable{
    //the port of the client
    private final int clientPort;
    public static boolean kill = false;

    /**
     * Initializes the udpServer
     * @param clientPort
     */
    public UdpServer(int clientPort) {
        this.clientPort = clientPort;
    }

    //guards thread collisions
    private Semaphore sendLock = new Semaphore(1);



    //this is the time of the last update in milliseconds
    private long lastSendMillis = 0;

    /**
     * This runs repeatedly (it's own thread). It looks to see if there are any messages to send
     * and if so which to send.
     */
    @Override
    public void run() {
        while(true){
            if(kill){break;}
            try {
                //never send data too fast
                if(SystemClock.uptimeMillis()-lastSendMillis < 50) {
                    continue;
                }
                //set the last send time
                lastSendMillis = SystemClock.uptimeMillis();

                //wait for semaphore to be available
                sendLock.acquire();


                //We will send either the current update or the last update depending on
                //if we are using the currentUpdate String or not
                if(currentUpdate.length() > 0){
                    //send the current update
                    sendUdpRAW(currentUpdate);
                    //now we scrap everything in currentUpdate to flag it is empty
                    currentUpdate = "";
                }else{
                    //if we are here, the currentUpdate is empty
                    if(lastUpdate.length() > 0){
                        sendUdpRAW(lastUpdate);
                        //now we scrap everything in lastUpdate to flag it is empty
                        lastUpdate = "";
                    }
                }

                //release the semaphore
                sendLock.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * This is a prate method to actually send a message over the udp protocol
     * @param message the message you wish to send
     */
    public void sendUdpRAW(String message){
        try(DatagramSocket serverSocket = new DatagramSocket()){
            DatagramPacket datagramPacket = new DatagramPacket(
                    message.getBytes(),
                    message.length(),
                    InetAddress.getByName("192.168.49.133"),
                    clientPort);

            Log.d("ERROR_LOG",message);
            serverSocket.send(datagramPacket);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This is a prate method to actually send a message over the udp protocol
     * @param message the message you wish to send
     */
    public void sendUdpRAW(byte[] message){
        try(DatagramSocket serverSocket = new DatagramSocket()){
            DatagramPacket datagramPacket = new DatagramPacket(
                    message,
                    message.length,
                    InetAddress.getByName("192.168.49.133"),
                    clientPort);

            serverSocket.send(datagramPacket);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //These are the double buffering system
    private String lastUpdate = "";
    private String currentUpdate = "";

    /**
     * This will queue a message for sending, utilizing the double buffer
     * @param string the message you wish to send
     */
    public void addMessage(String string){
        //depending on the state of the semaphore we can do two things
        if(!sendLock.tryAcquire()){
            //if it is being used, set the last update
            lastUpdate = string;
        }else{
            //we can update the current update if we got past the semaphore
            currentUpdate = string;
            //release the semaphore since we have acquired
            sendLock.release();
        }
    }
}
