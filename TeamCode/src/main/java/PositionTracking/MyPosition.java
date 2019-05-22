package PositionTracking;

/**
 * The MyPosition class tracks our relative position between the updates from Craig
 */
public class MyPosition {




    //the last positions of the wheels
    private double wheelLeftLastPosition;
    private double wheelRightLastPosition;


    private double worldXPosition = 0;
    private double worldYPosition = 0;



    //the left tracker module
    private TrackerModule wheelLeft;
    //the right tracker module
    private TrackerModule wheelRight;

    /**
     * Creates a new TrackerModule
     * @param wheelLeft the left module
     * @param wheelRight the right module
     */
    public MyPosition(TrackerModule wheelLeft, TrackerModule wheelRight){
        this.wheelLeft = wheelLeft;
        this.wheelRight = wheelRight;
    }





    /**
     * Call this every frame to re-calculate the position
     */
    public void update(double angle){
        //get the left wheel angle
        double wheelLeftAngle = wheelLeft.getCurrentAngle_rad();
        //get the right wheel angle
        double wheelRightAngle = wheelRight.getCurrentAngle_rad();

        double wheelLeftCurrent = wheelLeft.getForwardsCM();
        double wheelRightCurrent = wheelRight.getForwardsCM();


        double wheelLeftDelta = wheelLeftCurrent - wheelLeftLastPosition;
        double wheelRightDelta = wheelRightCurrent - wheelRightLastPosition;

        wheelLeftLastPosition = wheelLeftCurrent;//remember the last position
        wheelRightLastPosition = wheelRightCurrent;//remember the last position

        double wheelLeftX = wheelLeftDelta * Math.cos(wheelLeftAngle);
        double wheelLeftY = wheelLeftDelta * Math.sin(wheelLeftAngle);

        double wheelRightX = wheelRightDelta * Math.cos(wheelRightAngle);
        double wheelRightY = wheelRightDelta * Math.sin(wheelRightAngle);

        //calculate the relative translation of the robot
        double robotRelativeXDelta = (wheelLeftX + wheelRightX)/2.0;
        double robotRelativeYDelta = (wheelLeftY + wheelRightY)/2.0;



        worldXPosition += Math.cos(angle) * robotRelativeXDelta +
                Math.sin(angle) * robotRelativeYDelta;
        worldYPosition += Math.sin(angle) * robotRelativeYDelta +
                Math.cos(angle) * robotRelativeYDelta;




    }



}