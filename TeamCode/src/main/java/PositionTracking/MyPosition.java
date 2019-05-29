package PositionTracking;

import Main.Robot;
import PathFollowing.SpeedOmeter;

import static MyMath.MyMath.AngleWrap;

/**
 * The MyPosition class tracks our relative position between the updates from Craig
 */
public class MyPosition {

    public static double moveScalingFactor = 125.5;
    public static double turnScalingFactor = 352.6315;

    public static double worldXPosition = 0;
    public static double worldYPosition = 0;
    public static double worldAngle_rad = 0;


    //the left tracker module
    private TrackerModule wheelLeft;
    //the right tracker module
    private TrackerModule wheelRight;

    private Robot myRobot;

    /**
     * Creates a new TrackerModule
     * @param wheelLeft the left module
     * @param wheelRight the right module
     */
    public MyPosition(TrackerModule wheelLeft, TrackerModule wheelRight, Robot robot){
        this.wheelLeft = wheelLeft;
        this.wheelRight = wheelRight;
        myRobot = robot;
    }





    //the last positions of the wheels
    private double wheelLeftLast;
    private double wheelRightLast;


    //when we calibrate, what the wheels read
    private double wheelRightInitialReading;
    private double wheelLeftInitialReading;
    private double lastResetAngle;

    /**
     * Call this every frame to re-calculate the position
     */
    public void update(){
//        moveScalingFactor = myRobot.getDouble("MoveScale");
//        turnScalingFactor = myRobot.getDouble("TurnScale");

        double wheelLeftCurrent = wheelLeft.getOutputWheelTicks();
        double wheelRightCurrent= -wheelRight.getOutputWheelTicks();

        //compute how much the wheel data has changed
        double wheelLeftDelta = wheelLeftCurrent - wheelLeftLast;
        double wheelRightDelta = wheelRightCurrent - wheelRightLast;


        myRobot.telemetry.addLine("left wheel raw: " + wheelLeftCurrent +
                " right wheel raw: " + wheelRightCurrent);


        //get the real distance traveled using the movement scaling factors
        double wheelLeftDeltaScale = wheelLeftDelta*moveScalingFactor/1000.0;
        double wheelRightDeltaScale = wheelRightDelta*moveScalingFactor/1000.0;

        //get how much our angle has changed
        double angleIncrement = (wheelLeftDelta-wheelRightDelta)*turnScalingFactor/100000.0;



        //but use absolute for our actual angle
        double wheelLeftTotal = wheelLeft.getOutputWheelTicks()-wheelLeftInitialReading;
        double wheelRightTotal = -(wheelRight.getOutputWheelTicks()-wheelRightInitialReading);
        worldAngle_rad = AngleWrap(((wheelLeftTotal-wheelRightTotal)*turnScalingFactor/100000.0)
                + lastResetAngle);


        //relativeY will by defa
        double relativeY = (wheelLeftDeltaScale + wheelRightDeltaScale)/2.0;
        //assume relative x is 0 unless there has been an angle change
        double relativeX = 0;
        myRobot.telemetry.addLine("left wheel: " + (wheelLeftCurrent*moveScalingFactor/1000.0));
        myRobot.telemetry.addLine("right wheel: " + (wheelRightCurrent*moveScalingFactor/1000.0));


        //if angleIncrement is > 0 we can use steven's dumb stupid and stupid well you know the point
        //equations because he is dumb
        if(Math.abs(angleIncrement) > 0){
            //gets the radius of the turn we are in
            double radiusOfMovement = (wheelRightDeltaScale+wheelLeftDeltaScale)/(2*angleIncrement);

            relativeY = (radiusOfMovement * Math.sin(angleIncrement));

            relativeX = radiusOfMovement * (1 - Math.cos(angleIncrement));

            myRobot.telemetry.addLine("radius of movement: " + radiusOfMovement);
            myRobot.telemetry.addLine("relative y: " + relativeY);
            myRobot.telemetry.addLine("relative x: " + relativeX);
        }



        worldXPosition += (Math.cos(worldAngle_rad) * relativeY) + (Math.sin(worldAngle_rad) *
                relativeX);
        worldYPosition += (Math.sin(worldAngle_rad) * relativeY) - (Math.cos(worldAngle_rad) *
                relativeX);


        //Keep SpeedOmeter in the loop with the speeds
        SpeedOmeter.yDistTraveled += relativeY;
        SpeedOmeter.xDistTraveled += relativeX;

        //save the last positions for later
        wheelLeftLast = wheelLeftCurrent;
        wheelRightLast = wheelRightCurrent;

    }



    /**USE THIS TO SET OUR POSITION**/
    public void setPosition(double x,double y,double angle){
        worldXPosition = x;
        worldYPosition = y;
        worldAngle_rad= angle;

        //remember where we were at the time of the reset
        wheelLeftInitialReading = wheelLeft.getOutputWheelTicks();
        wheelRightInitialReading = wheelRight.getOutputWheelTicks();
        lastResetAngle = angle;
    }


}