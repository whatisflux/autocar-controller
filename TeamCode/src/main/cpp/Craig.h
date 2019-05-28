//
// Created by peter on 5/28/19.
//

#ifndef AUTOCAR_CRAIG_H
#define AUTOCAR_CRAIG_H


#include <opencv2/core/types.hpp>
#include <opencv2/core/Mat.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <vector>

using namespace cv;

class Craig {
private:
    Point2f tranformPoint(Point2f o, Size imgSize);
public:
    std::vector<Point2f> processImage(cv::Mat &mat);
};


#endif //AUTOCAR_CRAIG_H
