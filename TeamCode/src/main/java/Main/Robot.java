package Main;

import android.os.SystemClock;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import net.frogbots.ftcopmodetunercommon.opmode.TunableOpMode;

import PositionTracking.SwerveDriveController;

/**
 * Holds all the hardware data for the robot
 */
public class Robot extends TunableOpMode {
    /**This controls the drive train movement*/
    SwerveDriveController swerveDrive;


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
        telemetry.setMsTransmissionInterval(3);
    }



    @Override
    public void loop() {
        getCurrentTime();
        swerveDrive.update();
    }

    /**
     * Allows the user to control the swerve drive movement
     */
    public void controlMovement() {
        swerveDrive.setAmountTurn(-gamepad1.left_stick_x);
        swerveDrive.setForwardsPower(gamepad1.right_stick_y);
        swerveDrive.setSidewaysPower(gamepad1.right_stick_x);
    }


    /**
     * Remembers the current time in milliseconds
     */
    private void getCurrentTime(){
        //get the current time
        currTimeMillis = SystemClock.uptimeMillis();

    }


}
