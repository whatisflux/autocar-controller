//
// Created by peter on 5/28/19.
//

#include <android/log.h>
#include "Craig.h"

Point2f Craig::tranformPoint(Point2f s, Size imgSize)
{
    Point2f t;
    auto o = Point2f(s.x / (float)imgSize.width * 80.f, s.y / (float)imgSize.height * 60.f);
    // Apply experimentally determined camera curve fit
    t.x = 4 * 1400 / (o.y + 7.5) * (o.x - imgSize.width / 2) / imgSize.width;
    t.y = 190000 / ((o.y + 16.9)*(o.y + 16.9));
    return t;
}

Point Craig::findNextPoint(Mat img, Point previous, Point current)
{
    Point2f ds = current - previous;
    Point2f u = ds / hypot(ds.x, ds.y);
    Point next = current + (Point)(u * WALK_LENGTH);
    if (next.y < MIN_SCAN_Y || next.y >= img.rows || next.x < 0 || next.x >= img.cols) return Point(-1, -1);

    if (img.at<uchar>(next) == 0)
    {
        // Scan sideways to look for path
        int x = next.x;
        while (x < img.cols && x - next.x <= HORIZ_SCAN_OFFSET && img.at<uchar>(next.y, x) == 0)
        {
            x++;
        }
        if (img.at<uchar>(next.y, x) == 0)
        {
            x = next.x;
            while (x >= 0 && next.x - x <= HORIZ_SCAN_OFFSET && img.at<uchar>(next.y, x) == 0)
            {
                x--;
            }
        }
        if (x < 0 || x >= img.cols || next.y < 0 || next.y >= img.rows || img.at<uchar>(next.y, x) == 0) return Point(-1, -1);
        next.x = x;
    }

    int xsum = 0;
    int xcount = 0;
    int x = next.x;
    while (x < img.cols && img.at<uchar>(next.y, x) > 0)
    {
        xsum += x;
        xcount++;
        x++;
    }
    x = next.x;
    while (x >= 0 && img.at<uchar>(next.y, x) > 0)
    {
        xsum += x;
        xcount++;
        x--;
    }

    next.x = xsum / xcount;

    return next;
}

Point Craig::findFirstPoint(Mat img)
{
    int height = img.rows;
    int width = img.cols;

    int xsum = 0;
    int xcount = 0;
    int lastx = -1;
    bool found = false;
    int windowBot = height - 1 + INIT_SCAN_HEIGHT;
    while (!found && windowBot >= MIN_SCAN_Y)
    {
        windowBot -= INIT_SCAN_HEIGHT;
        for (int y = windowBot; y >= windowBot - INIT_SCAN_HEIGHT; y--)
        {
            bool foundInRow = false;
            for (int x = 0; x < width / 2 && !foundInRow; x++)
            {
                if (lastx == -1 || x - lastx <= INIT_POINT_MAX_DIST)
                {
                    if (img.at<uchar>(y, x) > 0)
                    {
                        bool groupIsGood = true;
                        for (int j = x + 1; j < x + INIT_GROUP_SIZE; j++)
                        {
                            groupIsGood = groupIsGood && img.at<uchar>(y, j) > 0;
                        }
                        if (groupIsGood)
                        {
                            lastx = x + INIT_GROUP_SIZE / 2;
                            xsum += lastx;
                            xcount++;
                            foundInRow = true;
                        }
                    }
                }
            }
            found = foundInRow || found;
        }
    }

    if (xcount == 0) return Point(-1, -1);

    int avgx = xsum / xcount;
    int avgy = windowBot - INIT_SCAN_HEIGHT / 2;
    return Point(avgx, avgy);
}

std::vector<Point2f> Craig::processImage(Mat &mat)
{
    Mat kernel = getStructuringElement(MORPH_ELLIPSE, Size(3, 3), Point(-1, -1));
    Mat img = mat.clone();

    //morphologyEx(img, img, MORPH_OPEN, kernel);
    //morphologyEx(img, img, MORPH_CLOSE, kernel);
    //dilate(img, img, kernel);

    resize(img, img, Size(80, 60));
//    threshold(img, img, 1, 255, THRESH_BINARY);


    std::vector<Point2f> points(0);

    int height = img.rows;
    int width = img.cols;

    Point currentPoint = findFirstPoint(img);
    Point previousPoint = Point(currentPoint.x, currentPoint.y + 1);
    Point nextPoint;
    int i = 0;
    while (currentPoint.x != -1 && currentPoint.y != -1 && i < 100)
    {
//        __android_log_print(ANDROID_LOG_VERBOSE, "ERROR_LOG", "Current point: (%d, %d), Previous Point: (%d, %d)", currentPoint.x, currentPoint.y, previousPoint.x, previousPoint.y);
        Point2f currentPointFloat(currentPoint.x, currentPoint.y);
        points.push_back(tranformPoint(currentPointFloat, Size(width, height)));
        nextPoint = findNextPoint(img, previousPoint, currentPoint);
        previousPoint = currentPoint;
        currentPoint = nextPoint;

        i++;
    }


    return points;
}