//
// Created by peter on 5/28/19.
//

#include "Craig.h"

Point2f Craig::tranformPoint(Point2f o, Size imgSize)
{
    Point2f t;
    // Apply experimentally determined camera curve fit
    t.x = 4 * 1400 / (o.y + 7.5) * (o.x - imgSize.width / 2) / imgSize.width;
    t.y = 190000 / ((o.y + 16.9)*(o.y + 16.9));
    return t;
}

std::vector<Point2f> Craig::processImage(cv::Mat &mat)
{
    Mat kernel = getStructuringElement(MORPH_ELLIPSE, Size(3, 3), Point(-1, -1));
    Mat img = mat.clone();
    morphologyEx(img, img, MORPH_OPEN, kernel);
    morphologyEx(img, img, MORPH_CLOSE, kernel);

    std::vector<Point2f> points;
    const int scanHeight = 8;
    const int maxScanHeight = 40;
    const int walkSize = 5;
    const int minWindowPoints = 5;

    int height = img.rows;
    int width = img.cols;

    for (int y = height - 1; y >= height - maxScanHeight; y -= scanHeight)
    {
        int xsum = 0;
        int xcount = 0;
        for (int h = y; h >= y - scanHeight; y++)
        {
            int lastFound = -walkSize;
            for (int x = 0; x < width; x++)
            {
                if (x - lastFound < walkSize) continue;
                if (img.at<uchar>(h, x) > 0)
                {
                    lastFound = x;
                    xsum += x;
                    xcount++;
                }
            }
        }
        if (xcount < minWindowPoints) continue;

        float x = (float)xsum / (float)xcount;
        float y2 = y - (float)scanHeight / 2.f;
        Point2f untranslatedPoint = Point2f(x, y2);
        Point2f translatedPoint = tranformPoint(untranslatedPoint, Size(width, height));
        points.push_back(translatedPoint);
    }

    return points;
}