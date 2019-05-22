package Hardware;

import MyMath.MyMath;

import static MyMath.MyMath.AngleWrap;

/**
 * Math functions used by the Swerve Module
 */
public class ModuleFunctions {
    //how many centimeters the wheel output goes for 1 encoder tick
    private static final double CM_PER_TICK_SCALE_FACTOR = 0.005;
    //converts a delta in encoder ticks to radians
    private static double RADS_PER_TICK_SCALE_FACTOR = 0.005257015819258701;

    /**
     * calculates angle error
     */
    public static double subtractAngles(double currentAngle, double targetAngle) {
        double error = currentAngle - targetAngle;
        error = MyMath.AngleWrap(error);
        return error;
    }


    /**calculates equivalent angle that is within -pi to pi*/
    public static double wrapAngle(double angle) {
        int factorOff = (int) (angle / Math.PI);

        angle -= factorOff * Math.PI;
        if((factorOff < 0) && (factorOff % 2 != 0)) { //coming from the negative
            angle += Math.PI;
        }
        if((factorOff > 0) && (factorOff % 2 != 0)) {
            angle -= Math.PI;
        }

        return angle;

    }


    /**
     * calculates current module angle based on input encoder values
     */
    public static double calculateAngle(double encoder1, double encoder2) {
        double rawAngle = (encoder1 + encoder2) * RADS_PER_TICK_SCALE_FACTOR;
        return MyMath.AngleWrap(rawAngle);
    }


    /**
     * Calculates the amount a wheel turns on it's output
     * @param encoder1 reading of encoder 1
     * @param encoder2 reading of encoder 2
     * @return the amount of forwards amount
     */
    public static double calculateForwards(double encoder1, double encoder2) {
        return (encoder1-encoder2) * CM_PER_TICK_SCALE_FACTOR;
    }

}