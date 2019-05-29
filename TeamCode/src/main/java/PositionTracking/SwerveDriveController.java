package PositionTracking;


import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import Hardware.FilteredMotor;
import Main.Robot;

public class SwerveDriveController {

    //all the wheel modules
    public TrackerModule moduleRight;
    public TrackerModule moduleLeft;

    /**The amount of overall power you want out of the swerve drive*/
    private double forwardsPower = 0;

    /**The amount of sidewaysPower */
    private double sidewaysPower = 0;
    /**How much turning you want in the overall drive*/
    private double amountTurn = 0;



    private Robot myRobot;
    /**
     * Creates a new SwerveDriveController
     */
    public SwerveDriveController(HardwareMap hardwareMap,Robot myRobot){

        this.myRobot = myRobot;
        moduleRight = new TrackerModule(new FilteredMotor((DcMotor) hardwareMap.get("motor11")),
                new FilteredMotor((DcMotor) hardwareMap.get("motor12")),
                0.04,false, Math.toRadians(0),myRobot);


        moduleLeft = new TrackerModule(new FilteredMotor((DcMotor) hardwareMap.get("motor21")),
                new FilteredMotor((DcMotor) hardwareMap.get("motor22")),
                0.04,false, Math.toRadians(180),myRobot);
    }


    /**
     * Sets the desired amount of turning from the drive train
     * @param amount
     */
    public void setAmountTurn(double amount){
        amountTurn = amount;
    }

    /**
     * Sets the overall power of the swerve drive
     * @param power
     */
    public void setForwardsPower(double power){
        forwardsPower = power;
    }

    /**
     * Sets the amount of sideways power
     * @param power the amount of sideways power
     */
    public void setSidewaysPower(double power){
        sidewaysPower = power;
    }


    /**
     * Call this every loop update
     */
    public void update(){
//        myRobot.telemetry.addLine("amount turn: " + amountTurn);
//        myRobot.telemetry.addLine("currTargetAngle: " + moduleLeft.getCurrentTargetAngle_rad());
//
//        myRobot.telemetry.addLine("current angle: " + Math.toDegrees(moduleLeft.getCurrentAngle_rad()));
//
//        myRobot.telemetry.addLine("Encoder1: " + moduleLeft.motor1.getCurrentPosition());
//        myRobot.telemetry.addLine("Encoder2: " + moduleLeft.motor2.getCurrentPosition());



        //now we can ask them to calculate the drive train direction
//        moduleRight.setDriveTrainDirection(forwardsPower,0,amountTurn);
//        moduleLeft.setDriveTrainDirection(forwardsPower,0,amountTurn);
        moduleRight.setDriveTrainDirection(forwardsPower,sidewaysPower,amountTurn);
        moduleLeft.setDriveTrainDirection(forwardsPower,sidewaysPower,amountTurn);

        //update the modules
        moduleRight.update();
        moduleLeft.update();
    }

    /**
     * Resets the encoders
     */
    public void resetEncoders() {
        moduleRight.resetEncoders();
        moduleLeft.resetEncoders();
    }

    public static double masterScale = 0.2;
    public void fastMode() {
        masterScale = 0.7;
    }
    public void slowMode(){
        masterScale = 0.2;
    }
}
