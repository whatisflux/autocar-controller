package org.firstinspires.ftc.robotcontroller.Vision;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.qualcomm.ftcrobotcontroller.R;

import org.firstinspires.ftc.robotcontroller.Odometry.LocationVars;
import org.firstinspires.ftc.robotcontroller.PathReception.FinalPathWrapper;
import org.firstinspires.ftc.robotcontroller.PathReception.PathPoint;
import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * This class is a custom FtcRobotControllerActivity that implements a Camera Listener.
 * It handles all the vision stuff
 */
public class FtcRobotControllerVisionActivity extends FtcRobotControllerActivity
        implements CameraBridgeViewBase.CvCameraViewListener2
{
    /**
     * Use this to access the instance data because it is set in on create
     */
    public static FtcRobotControllerVisionActivity linkToInstance;


    /**
     * The rgb image
     */
    Mat mRgba;


    JavaCameraView javaCameraView;


    /**
     * Use this to send the image to craig
     */
//    private ImageCommunication imageCommunication;

    /**
     * Deals with Craig's data coming in
     */
    private FinalPathWrapper pathInterpreter;

    boolean loadedVision = false;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();

                    //only load this our native lib once
                    if(!loadedVision){
                        System.loadLibrary("native-lib");
                        loadedVision = true;
                    }
                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };

    public void onCameraViewStarted(int width, int height)
    {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped()
    {
        mRgba.release();
    }






//    //last send time
//    private long lastSendTime = 0;


    public static ArrayList<PathPoint> allPathPoints = new ArrayList<>();

    public double lastXPosition = 0;
    public double lastYPosition = 0;
    public double lastAngle = 0;

    /**
     * This runs all our vision processing code and is called by the opencv camera listener
     * @param inputFrame mat (image) from the camera
     * @return the modified mat to display
     */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        //get the inputFrame data
        mRgba = inputFrame.rgba();

        //save this
        lastXPosition = LocationVars.worldXPosition;
        lastYPosition = LocationVars.worldYPosition;
        lastAngle = LocationVars.worldAngle_rad;

        double[] xPositions = new double[10];
        double[] yPositions = new double[10];


        Vision.readBallPattern(mRgba.getNativeObjAddr(),debugBar1.getProgress(),
                debugBar2.getProgress(), debugBar3.getProgress(),xPositions,yPositions);

        allPathPoints = Vision.getCurrentPath();

        return mRgba;
    }





    SeekBar debugBar1;
    SeekBar debugBar2;
    SeekBar debugBar3;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        linkToInstance = this;//if you want an instance

        javaCameraView = (JavaCameraView) findViewById (R.id.java_camera_view);
        javaCameraView.setVisibility(View.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);


        debugBar1 = (SeekBar) findViewById(R.id.seekBar1);
        debugBar2 = (SeekBar) findViewById(R.id.seekBar2);
        debugBar3 = (SeekBar) findViewById(R.id.seekBar3);
        Log.d("ERROR_LOG", "initializing");
        //initialize the image communication
//        imageCommunication = new ImageCommunication();
//        pathInterpreter = new FinalPathWrapper();
        Log.d("ERROR_LOG", "done");

    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this, mLoaderCallback);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if(javaCameraView!=null){
            javaCameraView.disableView();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(javaCameraView!=null){
            javaCameraView.disableView();
        }
    }

}
