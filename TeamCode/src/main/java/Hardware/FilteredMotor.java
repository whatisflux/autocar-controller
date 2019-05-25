package Hardware;


import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.openftc.revextensions2.ExpansionHubMotor;


/**
 * This class holds a motor object and encoder readings of a motor
 */
public class FilteredMotor {

    //this is the DcMotor we will be using
    public DcMotor myMotor;


    /**
     * Initializes a FilteredMotor
     * @param motor the motor
     */
    public FilteredMotor(DcMotor motor){
        myMotor = motor;
    }

    //the last power the motor was set to
    private double lastPower = -10.0;

    /**
     * Sets the power of the motor
     * @param power the power you want to go
     */
    public void setPower(double power){
        if(Math.abs(power - lastPower) > 0.03 ||
                (power == 0 && lastPower != 0)){
            myMotor.setPower(power);//set the power of the motor
            lastPower = power;
        }
    }




    /**
     * Gets the current position of the motor
     * @return the current position of the motor
     */
    public int getCurrentPosition() {
        return myMotor.getCurrentPosition();
    }

    /**
     * Sets the runmode of the motor
     */
    public void setMode(DcMotor.RunMode runMode) {
        myMotor.setMode(runMode);
    }

    /**
     * Sets the direction of the motor
     * @param direction the direction
     */
    public void setDirection(DcMotorSimple.Direction direction) {
        myMotor.setDirection(direction);
    }

    /**
     * Sets the ZeroPowerBehavior of the motor
     * @param zeroPowerBehavior behaviour
     */
    public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior zeroPowerBehavior) {
        myMotor.setZeroPowerBehavior(zeroPowerBehavior);
    }

    /**
     * Sets the target position of the motor
     * @param targetPosition the target position
     */
    public void setTargetPosition(int targetPosition) {
        myMotor.setTargetPosition(targetPosition);
    }

    /**
     * Gets the runmode of us
     * @return the runmode
     */
    public DcMotor.RunMode getMode() {
        return myMotor.getMode();
    }

}