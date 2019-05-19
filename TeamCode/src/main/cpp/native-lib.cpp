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



    __android_log_print(ANDROID_LOG_VERBOSE, "whatIsFlux", "ERROR_LOG: debug 1: %lf debug2: %lf debug3: %lf",
        (double) debug1, (double) debug2, (double) debug3);


    const double scaleSize = 9;


    //let's not care about the alpha channel
    cvtColor(img,img,COLOR_RGBA2RGB);

    //copy to HSV
    cvtColor(img, imgCanny, CV_RGB2HSV);

    //lower noise with uniform blur
    blur(imgCanny, imgCanny, Size(3,3));

    //split into HSV channels
    vector<Mat> channels;
    split(imgCanny, channels);
    Mat hue = channels[0];
    Mat sat = channels[1];
    Mat val = channels[2];

    //shift Hue +100 (-80) to get good contrast
    Mat shiftedH = hue.clone();
    int shift = 80; // in openCV hue values go from 0 to 180 (so have to be doubled to get to 0 .. 360) because of byte range from 0 to 255
    for(int j=0; j<shiftedH.rows; ++j)
    {
        for(int i=0; i<shiftedH.cols; ++i)
        {
            shiftedH.at<unsigned char>(j,i) = (shiftedH.at<unsigned char>(j,i) + shift)%180;
        }
    }

    //canny edge detector
    //Canny(hue, img, 50, 150);

    shiftedH.copyTo(img);

    //this is what we will use for processing stuff (at lower resolution)
    Mat processingImage;

    //rescale the main image into the processing image. scaleSize of 4 = 1/16 the size
    resize(img, processingImage, Size(), 1/scaleSize, 1/scaleSize, INTER_CUBIC);

}