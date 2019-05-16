package Main;

import android.os.SystemClock;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.openftc.revextensions2.ExpansionHubEx;
import org.openftc.revextensions2.RevBulkData;

import java.util.ArrayList;

import Hardware.RevMotor;

/**
 * Holds all the hardware data for the robot
 */
public class Robot extends OpMode {
    /**This controls the drive train movement*/
    SwerveDriveController swerveDrive;


    //these are the expansion hub objects
    private ExpansionHubEx revMaster;
    private ExpansionHubEx revSlave;
    //holds all the rev expansion hub motors
    private ArrayList<RevMotor> allMotors = new ArrayList<>();


    /**Contains the bulk data from the master hub*/
    private RevBulkData revExpansionMasterBulkData;
    /**Contains the bulk data from the slave hub*/
    private RevBulkData revExpansionSlaveBulkData;


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

        swerveDrive = new SwerveDriveController();

    }



    @Override
    public void loop() {
        getCurrentTime();
        getRevBulkData();
        controlMovement();
    }

    /**
     * Allows the user to control the swerve drive movement
     */
    private void controlMovement() {
        swerveDrive.setAmountTurn(gamepad1.left_stick_x);
        swerveDrive.setPowerOverall(gamepad1.right_stick_y);

    }


    /**
     * Remembers the current time in milliseconds
     */
    private void getCurrentTime(){
        //get the current time
        currTimeMillis = SystemClock.uptimeMillis();

    }



    //last time of updating the slave hub
    private long lastUpdateSlaveTime = 0;
    //last time of updating the master hub
    private long lastUpdateMasterTime = 0;

    /**
     * Gets all the data from the expansion hub in one command to increase loop times
     */
    public void getRevBulkData() {
        RevBulkData newDataMaster;
        try{
            newDataMaster = revMaster.getBulkInputData();
            if(newDataMaster != null){
                revExpansionMasterBulkData = newDataMaster;
            }
        }catch(Exception e){
            //don't set anything if we get an exception
        }
        lastUpdateMasterTime = currTimeMillis;



        /*
            We don't always need to poll the slave rev hub if not much is changing
         */
        boolean needToPollSlave = true;//currTimeMillis - lastUpdateSlaveTime > 400;

        if(needToPollSlave){
            RevBulkData newDataSlave;
            try{
                newDataSlave = revSlave.getBulkInputData();
                if(newDataSlave != null){
                    revExpansionSlaveBulkData = newDataSlave;
                }
            }catch(Exception e){
                //don't set anything if we get an exception
            }
            lastUpdateSlaveTime = currTimeMillis;
        }



        /////NOW WE HAVE THE BULK DATA BUT WE NEED TO SET THE MOTOR POSITIONS/////
        for(RevMotor revMotor : allMotors){
            if(revMotor == null){continue;}
            if(revMotor.isMaster){
                if(revExpansionMasterBulkData != null){
                    revMotor.setEncoderReading(
                            revExpansionMasterBulkData.getMotorCurrentPosition(revMotor.myMotor));
                }
            }else{
                if(revExpansionSlaveBulkData != null){
                    revMotor.setEncoderReading(
                            revExpansionSlaveBulkData.getMotorCurrentPosition(revMotor.myMotor));
                }
            }
        }

    }

}
