package Main;


import PositionTracking.TrackerModule;

public class SwerveDriveController {

    //all the wheel modules
    PositionTracking.TrackerModule moduleTL;
    PositionTracking.TrackerModule moduleTR;
    PositionTracking.TrackerModule moduleBL;
    PositionTracking.TrackerModule moduleBR;

    /**The amount of overall power you want out of the swerve drive*/
    private double powerOverall = 0;
    /**How much turning you want in the overall drive*/
    private double amountTurn = 0;


    /**
     * Creates a new SwerveDriveController
     */
    public SwerveDriveController(){

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
    public void setPowerOverall(double power){
        powerOverall = power;
    }

    /**
     * Call this every loop update
     */
    public void update(){
        //update the modules
        moduleTL.update();
        moduleTR.update();
        moduleBL.update();
        moduleBR.update();

//        moduleTL.calculatePowers();
    }
}
