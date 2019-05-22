package org.firstinspires.ftc.robotcontroller.Server;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.CV_8UC4;

public class ImageCommunication {

    private UdpServer udpServer;

    /**
     * Starts the UdpServer
     */
    public ImageCommunication(){
        udpServer = new UdpServer(1234);
        new Thread(udpServer).start();
    }





    /**
     * This method condenses an image into a series of characters that represent
     * 8 pixels at a time, since each is just a 1 or 0
     * @param mRgba the input image (which for now is not binary but once dale
     *              finishes this it will be)
     */
    public void sendImage(Mat mRgba) {
        int width = 80;
        int height = 60;

        Mat smallImage = new Mat(height,width,CV_8UC4);
        Size size = new Size(width,height);
        Imgproc.resize( mRgba, smallImage, size );


//        Log.d("ERROR_LOG: ","rows: " + smallImage.rows() + " cols: " + smallImage.cols());

        int c = 0;
//        StringBuilder binaryMap = new StringBuilder();
        byte[] map = new byte[width * height/8];
        for(int y = 0; y < smallImage.rows(); y ++){
            for(int x = 0; x < smallImage.cols(); x += 8){
                //this will store the data for this byte (8 binary pixels) of information
//                StringBuilder thisByte = new StringBuilder();
                byte theByte = 0;
                //now scan ahead in the image
                for(int i = 0; i < 8; i ++){
                    //get the color
                    double[] color = smallImage.get(y,x + i);

                    if((color[0] + color[1] + color[2])/3 > 150){
//                        thisByte.append("1");
                        theByte += (1 << i);
                    }else{
                        // nothing
                    }
                }
                //convert the series of 1s and 0s to a char
//                char convertedToChar = (char) Integer.parseInt(thisByte.toString(),2);
                //append the char to the string builder
                //map[c++] = (byte) convertedToChar;
                map[c++] = theByte;
//                binaryMap.append(convertedToChar);
            }
        }

        udpServer.sendUdpRAW(map);
//        Log.d("ERROR_LOG: ","string: " + binaryMap.toString());

//        Log.d("ERROR_LOG: ","length: " + binaryMap.toString().length());

        //udpServer.addMessage(binaryMap.toString());
    }



}
