package Main;


import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcontroller.PathReception.FinalPathWrapper;
import org.firstinspires.ftc.robotcontroller.PathReception.PathPoint;
import org.firstinspires.ftc.robotcontroller.PathReception.Waypoint;
import org.firstinspires.ftc.robotcontroller.Vision.FtcRobotControllerVisionActivity;

import java.util.ArrayList;
import java.util.Arrays;

import Debugging.ComputerDebugging;
import Debugging.TimeProfiler;
import HelperClasses.CurvePoint;
import MyMath.FloatPoint;
import PathFollowing.MovementEssentials;
import PositionTracking.MyPosition;
import PositionTracking.SwerveDriveController;

/**
 * This is the actual opmode
 */

@TeleOp

public class Main extends Robot{


    TimeProfiler tp1 = new TimeProfiler(200);


    /**
     * This is bad fix this
     */
    public void init(){
        super.init();
        MovementEssentials.initCurve();
    }
    /**
     * Called every loop update
     */
    public void loop(){
        tp1.markEnd();
        tp1.markStart();
        telemetry.addLine("Time : " +
                tp1.getAverageTimePerUpdateMillis());
        telemetry.addLine("Updates per second: " +
                1000.0/tp1.getAverageTimePerUpdateMillis());
        super.loop();

        //if we are pressing a, go follow the path




        if(gamepad1.x){
            swerveDrive.resetEncoders();
            myPosition.setPosition(0,0,0);
        }

        if(gamepad1.left_bumper){
            swerveDrive.fastMode();
        }else{
            swerveDrive.slowMode();
        }
        displayMyPosition();

        if(gamepad1.a){
            followPath();
        }else{
            controlMovement();
        }

        try{
            ArrayList<PathPoint> allPathPoints = (ArrayList<PathPoint>) FtcRobotControllerVisionActivity.allPathPoints.clone();

            for(int i = 0; i < allPathPoints.size() - 1; i ++){
                ComputerDebugging.sendLine(new FloatPoint(allPathPoints.get(i)),
                        new FloatPoint(allPathPoints.get(i+1)));
            }
        }catch(Exception e){

        }
    }



    /**
     * Follow the path if we are holding a for now
     */
    private void followPath() {
        SwerveDriveController.masterScale = 0.3;


        ArrayList<PathPoint> allPathPoints =
                (ArrayList<PathPoint>) FtcRobotControllerVisionActivity.allPathPoints.clone();

        if(allPathPoints != null &&  !(allPathPoints.size() > 1)){return;}
        ArrayList<CurvePoint> allPoints = new ArrayList<>();


        for(int i = 0; i < allPathPoints.size(); i ++){
            allPoints.add(new CurvePoint(allPathPoints.get(i).x,allPathPoints.get(i).y,
                    0.5,0.5,30,Math.toRadians(40),0.5));
        }


        MovementEssentials.followCurve(allPoints,Math.toRadians(90),false);
    }


    /**
     * Displays the robot location on the telemetry on the phone
     */
    private void displayMyPosition() {
        telemetry.addLine("X: " + df.format(MyPosition.worldXPosition) +
                " Y: " + df.format(MyPosition.worldYPosition) + " Angle: " +
                df.format(Math.toDegrees(MyPosition.worldAngle_rad)));
    }
}
