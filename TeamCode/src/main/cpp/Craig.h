//
// Created by peter on 5/28/19.
//

#ifndef AUTOCAR_CRAIG_H
#define AUTOCAR_CRAIG_H


#include <opencv2/opencv.hpp>
#include <vector>

using namespace cv;

class Craig {
private:
    Point2f tranformPoint(Point2f o, Size imgSize);
public:
    std::vector<Point2f> processImage(cv::Mat &mat);
};


#endif //AUTOCAR_CRAIG_H
