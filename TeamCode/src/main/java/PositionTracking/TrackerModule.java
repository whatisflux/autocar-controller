package PositionTracking;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import Hardware.FilteredMotor;
import Hardware.ModuleFunctions;
import Hardware.PID;
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
        this.minPower = minPower; // set minPower upon initialization of object
        this.angleToTurnAt = angleToTurnAt;//remember this
        //pid loop to control module rotation
        angleLoop = new PID(0.13, 0.3, 0.5, 0.05);




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

    private double currentClockTimeNanos = 0; //current time on the clock
    private double previousClockTime = 0; //previous update's clock time
    private double timeSinceUpdate = 0; //time of the update


    private double motor1Power = 0;
    private double motor2Power = 0;


    //the minimum power we are allowed to go
    private double minPower;

    //the pid controller for the angle
    private PID angleLoop;



    /**
     * Sets powers to motors to hit target angle/wheel power
     */
    public void calculatePowersFixed(double rawTargetAngle, double wheelPower) {
        if(reversed){wheelPower *= -1;}//reversed this if we are reversed
        currentClockTimeNanos = System.nanoTime();
        timeSinceUpdate = (currentClockTimeNanos - previousClockTime)/1e9;

        currentAngle_rad = ModuleFunctions.calculateAngle(motor1.getCurrentPosition(),
                motor2.getCurrentPosition());


        angleError = ModuleFunctions.subtractAngles(rawTargetAngle,currentAngle_rad);

        while (Math.abs(angleError) > Math.toRadians(90)) {
            if(rawTargetAngle > currentAngle_rad){
                rawTargetAngle -= Math.toRadians(180);
            }else{
                rawTargetAngle += Math.toRadians(180);
            }
            wheelPower *= -1;
            angleError = ModuleFunctions.subtractAngles(rawTargetAngle,currentAngle_rad);
        }




        //measure the current turning speed of the module
        currentTurnVelocity = ModuleFunctions.subtractAngles(currentAngle_rad,previousAngle_rad)
                /(timeSinceUpdate);



        myRobot.telemetry.addLine("curr turn velocity: "
                + Math.toDegrees(currentTurnVelocity));
        if(Math.abs(Math.toDegrees(currentTurnVelocity)) > 15){
            angleLoop.resetI();
        }

        double currentDWeight = 0;//angleLoop.calculateDWeight(currentTurnVelocity);



        double i = angleLoop.calculateIWeight(angleError, currentDWeight);

        //calculate the turn power
        turnPower = angleLoop.calculatePWeight(angleError, currentDWeight) + i;



        //remember the time to calculate delta the next update
        previousClockTime = currentClockTimeNanos;
        //remember the angle
        previousAngle_rad = currentAngle_rad;





        //don't go until we get to the target position
        if(Math.abs(angleError) > Math.toRadians(15)){
            wheelPower = 0;
        }
        motor1Power = wheelPower * 1.0 + turnPower * 1.0;
        motor2Power = -wheelPower * 1.0 + turnPower * 1.0;

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
    public double getCurrentErrorSum() {
        return angleLoop.calculateIWeight(angleError, this.getDWeight());
    }
    public double getCurrentTurnVelocity() {
        return currentTurnVelocity;
    }
    public double getTurnPower() {
        return turnPower;
    }
    public double getDWeight() {
        return angleLoop.calculateDWeight(currentTurnVelocity);
    }

    /**
     * Called every update:
     * - Calculates the motor powers
     * - Applies the powers
     */
    public void update() {
        calculatePowersFixed(currentTargetAngle, currentForwardsPower);
        applyPowers();
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
}
