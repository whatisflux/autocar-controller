package PositionTracking;

import android.os.SystemClock;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import Hardware.FilteredMotor;
import Hardware.ModuleFunctions;
import Main.Robot;

/**
 * This represents one fourth of the modules in the swerve drive
 */
public class TrackerModule {
    public FilteredMotor motor1;
    public FilteredMotor motor2;


    private Robot myRobot;

    /** The power dedicated to turning the wheel in the direction it's pointed*/
    private double currentForwardsPower = 0;

    /** The current target angle we are trying to make*/
    private double currentTargetAngle = 0;


    /** The angle we will go to when we want the robot to turn */
    private double angleToTurnAt = 0;

    /**if the wheel output speed (not angle) is reversed*/
    private boolean reversed = false;

    /**
     * Creates a new SwerveDriveController
     * @param motor1 the first motor
     * @param motor2 the second motor
     * @param reversed if the wheel output speed (not angle) is reversed
     * @param angleToTurnAt the angle we will go to when turning at maximum power
     */
    public TrackerModule(FilteredMotor motor1, FilteredMotor motor2, double minPower,
                         boolean reversed, double angleToTurnAt, Robot myRobot){
        this.reversed = reversed;
        this.motor1 = motor1;
        this.motor2 = motor2;
        motor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        this.angleToTurnAt = angleToTurnAt;//remember this


        this.myRobot = myRobot;

    }

    /**
     * Sets the current target angle of the module
     * @param angle the angle in radians
     */
    public void setCurrentTargetAngle(double angle){
        currentTargetAngle = angle;
    }


    /**
     * Sets the current forwards power
     */
    public void setCurrentForwardsPower(double power){
        currentForwardsPower = power;
    }


    /**
     * Gets the output wheel position in encoder ticks
     * @return the number of ticks the output wheel
     * would have recorded if it had a single encoder
     */
    public int getOutputWheelTicks(){
        return (motor1.getCurrentPosition() - motor2.getCurrentPosition())/2;
    }

    /**
     * Gets how much the wheel has turned (the smaller one)
     * @return angle of the swerve drive module in ticks
     */
    public int getAngleOutputWheelTicks(){
        return (motor1.getCurrentPosition() + motor2.getCurrentPosition())/2;
    }



    private double currentAngle_rad = 0; //real angle
    private double previousAngle_rad = 0; // angle from previous update, used for velocity

    private double angleError = 0;
    private double turnPower = 0; //not user input, calculated based on error
    private double currentTurnVelocity = 0; //current rate at which the module is turning

    private long currentTimeNanos = 0; //current time on the clock
    private long lastTimeNanos = 0; //previous update's clock time
    private double elapsedTimeThisUpdate = 0; //time of the update


    private double motor1Power = 0;
    private double motor2Power = 0;




    //used for debugging
    double angleTurnedSum = 0;





    //the current error sum when turning toward the target
    private double turnErrorSum = 0;

    /**
     * Sets powers to motors to hit target angle/wheel power
     */
    public void calculatePowersFixed(double rawTargetAngle, double wheelPower) {
        if(reversed){wheelPower *= -1;}//reversed this if we are reversed
        currentTimeNanos = SystemClock.elapsedRealtimeNanos();
        elapsedTimeThisUpdate = (currentTimeNanos - lastTimeNanos)/1e9;

        if(elapsedTimeThisUpdate < 0.003){
            return;//don't do anything if it is too fast
        }
        //remember the time to calculate delta the next update
        lastTimeNanos = currentTimeNanos;
        //if there has been an outrageously long amount of time, don't bother
        if(elapsedTimeThisUpdate > 1){
            return;
        }


        //calculate our current angle
        currentAngle_rad = ModuleFunctions.calculateAngle(motor1.getCurrentPosition(),
                motor2.getCurrentPosition());


        angleError = ModuleFunctions.subtractAngles(rawTargetAngle,currentAngle_rad);
        //we should never turn more than 180 degrees, just reverse the direction
        while (Math.abs(angleError) > Math.toRadians(90)) {
            if(rawTargetAngle > currentAngle_rad){
                rawTargetAngle -= Math.toRadians(180);
            }else{
                rawTargetAngle += Math.toRadians(180);
            }
            wheelPower *= -1;
            angleError = ModuleFunctions.subtractAngles(rawTargetAngle,currentAngle_rad);
        }

        double angleErrorVelocity = angleError -
                ((getCurrentTurnVelocity() / Math.toRadians(300)) * Math.toRadians(30)
                        * myRobot.getDouble("d"));







        myRobot.telemetry.addLine("curr angle: " + Math.toDegrees(currentAngle_rad));

//        angleTurnedSum +=  ModuleFunctions.subtractAngles(currentAngle_rad,
//                previousAngle_rad);

//        myRobot.telemetry.addLine("turn sum: " + Math.toDegrees(angleTurnedSum));

        myRobot.telemetry.addLine("curr turn velocity: "
                + Math.toDegrees(getCurrentTurnVelocity()));


        turnErrorSum += angleError * elapsedTimeThisUpdate;

        if(Math.abs(Math.toDegrees(getCurrentTurnVelocity())) > 1100){
            //reset the error sum if going too fast
            turnErrorSum = 0;
        }






        //calculate the turn power
        turnPower = Range.clip((angleErrorVelocity / Math.toRadians(100)),-1,1)
                * myRobot.getDouble("p");
        turnPower += turnErrorSum * myRobot.getDouble("i");


        turnPower *= Range.clip(Math.abs(angleError)/Math.toRadians(5),0,1);

        //remember the angle
        previousAngle_rad = currentAngle_rad;

        //don't go until we get to the target position
        if(Math.abs(angleError) > Math.toRadians(20)){
            wheelPower = 0;
        }

        motor1Power = wheelPower * SwerveDriveController.masterScale + turnPower * 1.0;
        motor2Power = -wheelPower * SwerveDriveController.masterScale + turnPower * 1.0;

        maximumPowerScale();
    }

    /**
     * Makes sure that we don't go above maximum power for any
     */
    private void maximumPowerScale() {
        double scaleAmount = 1;
        if(motor1Power > 1 && motor1Power > motor2Power){
            scaleAmount = 1/motor1Power;
        }
        if(motor2Power > 1 && motor2Power > motor1Power){
            scaleAmount = 1/motor2Power;
        }
        motor1Power *= scaleAmount;
        motor2Power *= scaleAmount;
    }


    public double getMotor1Power() {
        return motor1Power;
    }
    public double getMotor2Power() {
        return motor2Power;
    }
    public double getCurrentAngle_rad() {
        return currentAngle_rad;
    }
    public double getAngleError() {
        return angleError;
    }


    /**
     * Gets the current rotational velocity in rad/s
     * @return
     */
    public double getCurrentTurnVelocity() {
        return currentTurnVelocity;
    }
    public double getTurnPower() {
        return turnPower;
    }

    /**
     * Called every update:
     * - Calculates the motor powers
     * - Applies the powers
     */
    public void update() {
        calculateCurrentModuleRotationVelocity();
        calculatePowersFixed(currentTargetAngle, currentForwardsPower);
        applyPowers();
    }



    //the last angle we were at
    private double previousMeasureVelocityAngle = 0;
    //last time we updated the measure velocity
    private long lastMeasureVelocityTime = 0;
    /**
     * Measures the current module rotational velocity
     */
    private void calculateCurrentModuleRotationVelocity() {
        long currTime = SystemClock.uptimeMillis();
        if(currTime - lastMeasureVelocityTime > 40){
            //measure the current turning speed of the module
            currentTurnVelocity = ModuleFunctions.subtractAngles(currentAngle_rad,
                    previousMeasureVelocityAngle)/((currTime - lastMeasureVelocityTime)/1000.0);

            previousMeasureVelocityAngle = currentAngle_rad;
            lastMeasureVelocityTime = currTime;
        }
    }

    /**
     * Applies the current powers to the motors
     */
    private void applyPowers(){
        motor1.setPower(motor1Power);
        motor2.setPower(motor2Power);
    }

    /**
     * Gets the current target angle
     * @return the angle in radians
     */
    public double getCurrentTargetAngle_rad() {
        return currentTargetAngle;
    }


    /**
     * This accepts a desired shape of the drive train and translates that into our
     * angle and forwards speed
     * @param amountForwards
     * @param amountTurn
     */
    public void setDriveTrainDirection(double amountForwards,double amountSideWays,
                                       double amountTurn) {

        double xComponent = amountForwards * 1 + amountSideWays * 0 +
                Math.cos(angleToTurnAt) * amountTurn;
        double yComponent = amountForwards * 0 + amountSideWays * 1 +
                Math.sin(angleToTurnAt) * amountTurn;


        currentForwardsPower = Math.hypot(xComponent,yComponent);
        if(Math.abs(currentForwardsPower) > 0.03){
            currentTargetAngle = Math.atan2(yComponent,xComponent);
        }

    }

    public void resetEncoders() {
        motor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }




    /**
     * Gets how far the wheel has spun in CM
     */
    public double getForwardsCM() {
        return ModuleFunctions.calculateForwards(motor1.getCurrentPosition(),
                motor2.getCurrentPosition());
    }
}
