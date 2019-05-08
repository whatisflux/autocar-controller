package org.firstinspires.ftc.robotcontroller.Vision;

import android.os.Bundle;
import android.view.View;

import com.qualcomm.ftcrobotcontroller.R;

import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

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
    Mat mRgba;

    JavaCameraView javaCameraView;

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






    /**
     * This runs all our vision processing code and is called by the opencv camera listener
     * @param inputFrame mat (image) from the camera
     * @return the modified mat to display
     */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        //get the inputFrame data
        mRgba = inputFrame.rgba();

        Vision.readBallPattern(mRgba.getNativeObjAddr());

        return mRgba;
    }




    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        linkToInstance = this;//if you want an instance

        javaCameraView = (JavaCameraView) findViewById (R.id.java_camera_view);
        javaCameraView.setVisibility(View.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
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
