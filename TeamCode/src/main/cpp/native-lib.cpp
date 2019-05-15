#include <jni.h>
#include <string>
#include "opencv2/core.hpp"
#include <vector>
#include <cmath>
#include <sstream>
#include "opencv2/opencv.hpp"

using namespace cv;
using namespace std;

extern "C"
JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_robotcontroller_Vision_Vision_readBallPattern(JNIEnv *env, jclass type,
                                                                    jlong addrRgba) {

    //get our img by converting the long into a pointer
    Mat& img = *(Mat*) addrRgba;


    const double scaleSize = 8;


    //let's not care about the alpha channel
    cvtColor(img,img,COLOR_RGBA2RGB);



    //this is what we will use for processing stuff (at lower resolution)
    Mat processingImage;

    //rescale the main image into the processing image. scaleSize of 4 = 1/16 the size
    resize(img, processingImage, Size(), 1/scaleSize, 1/scaleSize, INTER_CUBIC);




}