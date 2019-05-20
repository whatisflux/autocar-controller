package Hardware;

/**
 * Represents a PID object and sums the error
 */
public class PID {

    double pValue; //tuned value
    double iValue; // tuned value
    double dValue; // tuned value

    double pWeight; //what it is sending back in terms of p
    double iWeight;
    double dWeight;

    double currentErrorSum; //for i weight

    double previousClockTime = 0;
    double currentClockTime = 0;
    double timeSinceUpdate = 0;

    double deadBand; //can be + or - this value to turn off power; non inclusive i guess


    /**
     * Creates a new pid object
     * @param pValue
     * @param iValue
     * @param dValue
     * @param deadBand
     */
    public PID (double pValue, double iValue, double dValue, double deadBand) {
        this.pValue = pValue;
        this.iValue = iValue;
        this.dValue = dValue;
        this.deadBand = deadBand;
    }


    //proportional to distance from target (linear)
    public double calculatePWeight (double distanceFromTarget, double dWeight) {
        pWeight = (distanceFromTarget - dWeight) * pValue;
        return pWeight;
    }

    //proportional to the product of distance from target AND time
    public double calculateIWeight (double distanceFromTarget, double dWeight) {
        currentClockTime = System.nanoTime();

        //update time is current time minus the last known time
        timeSinceUpdate = (currentClockTime - previousClockTime)/1000000000.0;

        //adds to error sum proportional to time and distance
        currentErrorSum += (distanceFromTarget - dWeight) * timeSinceUpdate;

        previousClockTime = currentClockTime;


        iWeight = currentErrorSum * iValue;
        return iWeight;
    }

    //reset error sum
    //will probably be used when target is reached
    public void resetI() {
        currentErrorSum = 0;
    }


    //proportional to the rate at which current position is approaching the
    // target and inversely proportional to distance away
    public double calculateDWeight (double currentVelocity) {
        dWeight = dValue * currentVelocity;
        return dWeight;
    }


    public double getErrorSum() {
        return currentErrorSum;
    }
    public double getPWeight() {
        return pWeight;
    }
    public double getIWeight() {
        return iWeight;
    }
    public double getDWeight() {
        return dWeight;
    }

}