package MyMath;

import org.firstinspires.ftc.robotcontroller.PathReception.PathPoint;

/**
 * This is a point with doubles, that's all have fun kids
 */
public class FloatPoint {
    public double x;
    public double y;

    public FloatPoint(double x, double y){
        this.x = x;
        this.y = y;
    }
    public FloatPoint(PathPoint p){
        this.x = p.x;
        this.y = p.y;
    }
}