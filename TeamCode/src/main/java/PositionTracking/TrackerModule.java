package PositionTracking;

import Hardware.ModuleFunctions;
import Hardware.PID;
import Hardware.RevMotor;

/**
 * This represents one fourth of the modules in the swerve drive
 */
public class TrackerModule {
    private RevMotor motor1;
    private RevMotor motor2;


    /** The power dedicated to turning the wheel in the direction it's pointed*/
    private double currentForwardsPower = 0;

    /** The current target angle we are trying to make*/
    private double currentTargetAngle = 0;

    /**
     * Creates a new SwerveDriveController
     * @param motor1 the first motor
     * @param motor2 the second motor
     */
    public TrackerModule(RevMotor motor1, RevMotor motor2, double minPower){
        this.motor1 = motor1;
        this.motor2 = motor2;
        this.minPower = minPower; // set minPower upon initialization of object

        //pid loop to control module rotation
        angleLoop = new PID(0.1, 0.0000000005, -500.0, 0.05);
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
    public void calculatePowers(double rawTargetAngle, double wheelPower) {
        currentClockTimeNanos = System.nanoTime();
        timeSinceUpdate = currentClockTimeNanos - previousClockTime;

        currentAngle_rad = ModuleFunctions.calculateAngle(motor1.getCurrentPosition(),
                motor1.getCurrentPosition());

        angleError = ModuleFunctions.subtractAngles(currentAngle_rad, rawTargetAngle);

        //measure the current turning speed of the module
        currentTurnVelocity = (currentAngle_rad - previousAngle_rad)/timeSinceUpdate;

        double currentDWeight = angleLoop.calculateDWeight(currentTurnVelocity);

        //calculate the turn power
        turnPower = angleLoop.calculatePWeight(angleError, currentDWeight) +
                angleLoop.calculateIWeight(angleError, currentDWeight);



        //remember the time to calculate delta the next update
        previousClockTime = currentClockTimeNanos;
        //remember the angle
        previousAngle_rad = currentAngle_rad;

        motor1Power = wheelPower + turnPower;
        motor2Power = -wheelPower + turnPower;

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
        calculatePowers(currentTargetAngle, currentForwardsPower);
        applyPowers();
    }

    /**
     * Applies the current powers to the motors
     */
    private void applyPowers(){
        motor1.setPower(motor1Power);
        motor2.setPower(motor2Power);
    }
}
