package org.firstinspires.ftc.robotcontroller.PathReception;

public class Waypoint {
    public float x;
    public float y;
    public Boolean insideIsLeft;

    Waypoint(float x, float y, Boolean insideIsLeft) {
        this.x = x;
        this.y = y;
        this.insideIsLeft = insideIsLeft;
    }
}
