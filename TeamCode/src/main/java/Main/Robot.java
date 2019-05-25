package Main;

import android.os.SystemClock;

import net.frogbots.ftcopmodetunercommon.opmode.TunableOpMode;

import org.firstinspires.ftc.robotcontroller.Odometry.LocationVars;

import java.text.DecimalFormat;

import Debugging.ComputerDebugging;
import PathFollowing.SpeedOmeter;
import PositionTracking.MyPosition;
import PositionTracking.SwerveDriveController;
import RobotUtilities.MovementVars;

import static PositionTracking.MyPosition.worldAngle_rad;
import static PositionTracking.MyPosition.worldXPosition;
import static PositionTracking.MyPosition.worldYPosition;
import static RobotUtilities.MovementVars.movement_turn;
import static RobotUtilities.MovementVars.movement_x;
import static RobotUtilities.MovementVars.movement_y;

/**
 * Holds all the hardware data for the robot
 */
public class Robot extends TunableOpMode {
    /**Decimal formatting*/
    DecimalFormat df = new DecimalFormat("0.00##");

    public static boolean usingComputer = true;
    /**This controls the drive train movement*/
    SwerveDriveController swerveDrive;

    MyPosition myPosition;

    //sends telemetry information back to the laptop
    private ComputerDebugging computerDebugging;


    //this will be a milisecond time
    private long currTimeMillis = 0;



    /**
     * Creates a new robot
     */
    public Robot(){

    }

    /**
     * Initializes all the hardware
     */
    @Override
    public void init() {
        //initialize the SwerveDriveController
        swerveDrive = new SwerveDriveController(hardwareMap,this);
        telemetry.setMsTransmissionInterval(10);


        myPosition = new MyPosition(swerveDrive.moduleLeft,swerveDrive.moduleRight,
                this);


        //initialize the ComputerDebugging
        computerDebugging = new ComputerDebugging();
    }


    /**
     * Runs every update
     */
    @Override
    public void loop() {
        getCurrentTime();
        applyMovement();
        myPosition.update();
        SpeedOmeter.update();

        sendRobotLocationComputer();

        giveOtherModulePositionData();
    }

    /**
     * Gives other module position data
     */
    private void giveOtherModulePositionData() {
        LocationVars.worldXPosition = worldXPosition;
        LocationVars.worldYPosition = worldYPosition;
        LocationVars.worldAngle_rad = worldAngle_rad;
    }


    /**
     * Sets the movement vectors to the swerve drive
     */
    private void applyMovement() {
        swerveDrive.setAmountTurn(movement_turn);
        swerveDrive.setForwardsPower(movement_y);
        swerveDrive.setSidewaysPower(movement_x);
        swerveDrive.update();
    }

    /**
     * Sends the robot's location through a udp server to the laptop
     */
    private void sendRobotLocationComputer() {
        ComputerDebugging.sendRobotLocation(this);
        ComputerDebugging.markEndOfUpdate();
    }

    /**
     * Allows the user to control the swerve drive movement
     */
    public void controlMovement() {
        movement_turn = -gamepad1.left_stick_x;
        movement_y = gamepad1.right_stick_y;
//        movement_x = gamepad1.right_stick_x;
        movement_x = 0;
    }


    /**
     * Remembers the current time in milliseconds
     */
    private void getCurrentTime(){
        //get the current time
        currTimeMillis = SystemClock.uptimeMillis();

    }


    public double getXPos() {
        return worldXPosition;
    }

    public double getYPos() {
        return MyPosition.worldYPosition;
    }

    public double getAngle_rad() {
        return MyPosition.worldAngle_rad;
    }
}
