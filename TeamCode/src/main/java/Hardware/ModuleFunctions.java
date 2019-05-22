package Hardware;

import MyMath.MyMath;

import static MyMath.MyMath.AngleWrap;

/**
 * Math functions used by the Swerve Module
 */
public class ModuleFunctions {
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

}