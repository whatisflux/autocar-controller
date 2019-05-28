#include <jni.h>
#include <string>
#include "opencv2/core.hpp"
#include <vector>
#include <cmath>
#include <sstream>
#include "opencv2/opencv.hpp"

#include <android/log.h>

using namespace cv;
using namespace std;

extern "C"
JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcontroller_Vision_Vision_readBallPattern(JNIEnv *env, jclass type,
                                                                    jlong addrRgba, jdouble debug1,
                                                                    jdouble debug2,
                                                                    jdouble debug3) {



    //get our img by converting the long into a pointer
    Mat& img = *(Mat*) addrRgba;
    Mat imgCanny;
    flip(img, img, -1);









    __android_log_print(ANDROID_LOG_VERBOSE, "whatIsFlux", "ERROR_LOG: debug 1: %lf debug2: %lf debug3: %lf",
        (double) debug1, (double) debug2, (double) debug3);


    const double scaleSize = 9;


    //let's not care about the alpha channel
    cvtColor(img,img,COLOR_RGBA2RGB);

    //copy to HSV
    cvtColor(img, imgCanny, CV_RGB2HSV);

    //lower noise with uniform blur
    blur(imgCanny, imgCanny, Size(5,5));

    //split into HSV channels
    vector<Mat> channels;
    split(imgCanny, channels);
    Mat hue = channels[0];
    Mat sat = channels[1];
    Mat val = channels[2];





    //threshold
    int h_thresh_value = ((double) debug1) * 1.7;   //85
    int s_thresh_value = ((double) debug2) * 2.54;   //50
    int v_thresh_value = ((double) debug3) * 2.54;   //67
    int max_binary_value = 255;
//    int inv_threshold_type = 1; //inverted threshold


//    Mat hueThresh = img.clone();
//    Mat satThresh = img.clone();
//    Mat valThresh = img.clone();

    Mat hsv;
    cvtColor(img,hsv,COLOR_RGB2HSV);
    Mat blurred;
    blur(hsv,blurred,Size(1,7));
    Mat thresh;
    inRange(blurred,Scalar(h_thresh_value ,s_thresh_value,v_thresh_value),
            Scalar(h_thresh_value+18,max_binary_value,max_binary_value),thresh);

//    cv::erode(thresh,thresh,getStructuringElement(MORPH_ELLIPSE,Size(3,3),Point(-1,1)));
    thresh.copyTo(img);
//    threshold( img, hueThresh, h_thresh_value, max_binary_value, inv_threshold_type );
//    threshold()
//    threshold( sat, satThresh, s_thresh_value, max_binary_value, 0);
//    threshold( val, valThresh, v_thresh_value, max_binary_value, 0);


//    Mat thresh;
//    bitwise_and(hueThresh, valThresh, thresh);
//    bitwise_and(thresh, satThresh, thresh);

//    thresh.copyTo(img);



    //canny edge detector
    //double thresh = ((double) debug2);
    //Canny(shiftedH, img, thresh, 3.0 * thresh);

    //shiftedH.copyTo(img);

    //this is what we will use for processing stuff (at lower resolution)
    //Mat processingImage;

    //rescale the main image into the processing image. scaleSize of 4 = 1/16 the size
    //resize(img, processingImage, Size(), 1/scaleSize, 1/scaleSize, INTER_CUBIC);

}