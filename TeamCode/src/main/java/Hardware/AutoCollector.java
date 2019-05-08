package Hardware;


import android.os.SystemClock;

import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcontroller.Vision.FtcRobotControllerVisionActivity;

import java.util.ArrayList;

import CompleteAutonomous.PitScannerInterface;
import Globals.Globals;
import HelperClasses.CollectingLocation;
import HelperClasses.Robot;
import RobotUtilities.MovementEssentials;
import RobotUtilities.MovementEssentials.movementResult;
import RobotUtilities.MovementVars;
import RobotUtilities.MyPosition;
import RobotUtilities.SpeedOmeter;

import static RobotUtilities.MovementEssentials.minPower;
import static RobotUtilities.MovementEssentials.pointAngle;
import static RobotUtilities.MovementVars.movement_turn;

/**
 * Handles everything to do with auto collecting
 */
public class AutoCollector {

    /** This is the speed at which the collector extends */
    private final double collectorExtensionSpeed = 0.38;

    private Collector myCollector;
    private Lift myLift;
    private Robot myRobot;

    //this is the maximum speed the collector reached while collecting. If we see a drop
    //significant from this, we know we are jammed
    private double collectorTopSpeed = 0;


    //where the collector was at the beginning of the mini backup
    private double initialCollectorMiniBackUpPos = 0;
    //time when we put down the collector but only in state delay put down collector
    private long delayCollectorDeployTime = 0;


    /**
     * Creates a new auto collector
     * @param mRobot link to the robot
     * @param collector link to the collector
     * @param lift link to the lift
     */
    public AutoCollector(Robot mRobot, Collector collector, Lift lift){
        myRobot = mRobot;
        myCollector = collector;
        myLift = lift;
        abortAutoCollect();
    }



    //if the user is allowed to control the collector
    public static boolean canControlCollector = true;






    /**
     * This is used in emergencies where auto feed or auto collect has failed and will abort everything
     */
    public void abortAutoCollect() {
        nextStage();
        myState = myStates.waiting.ordinal();
        myLift.setExtensionPowerRaw(0);
        myCollector.setExtensionPowerNice(0);
        MovementVars.movement_x = 0;
        MovementVars.movement_y = 0;
        MovementVars.movement_turn = 0;
        canControlCollector = true;
    }


    /**
     * State variables and stuff
     */
    private boolean collectorDeployed = false;


    /**
     * Call this at the beginning of auto feed and auto collect
     */
    private void initializeMasterVariables() {
        collectorDeployed = false;
    }




    public enum myStates {
        waiting,
        /** AUTO COLLECT STUFF */
        driveAndDeployCollector,//hope we get 2 for now
        putDumperDown,
        backupCollectorFirstTrip,//tries to dig in on the first trip
        scoutingForFood,
        miniBackUp,//used for small jams
        turnAndRetractToNext,//repositions for another attempt
        delayBeforeScouting,//waits for collector to deploy before scouting again
    }
    public static int myState = myStates.waiting.ordinal();

    private long stateStartTime = 0;
    private boolean stageFinished = true;





    //during scouting for food if we have inverted scan yet
//    private boolean changedScan = false;







    private double blockStartingX = 0;
    private double blockStartingY = 0;
    private double blockStartingAngle = 0;
    /**
     * Call this the first update of each state
     */
    private void initializeStateVariables(){
        stageFinished = false;
        stateStartTime = SystemClock.uptimeMillis();

        //record our starting coordinates
        blockStartingX = myRobot.getXPos();
        blockStartingY = myRobot.getYPos();
        blockStartingAngle = myRobot.getAngle_rad();
    }


    /**
     * Call this to increment the stage
     */
    public void nextStage(){
        stageFinished = true;
        myState ++;
    }

    /**
     * Call this to change to a specific state
     * @param number the index of the state
     */
    public void nextStage(int number){
        stageFinished = true;
        myState = number;
    }




    //the time of the last update
    private long lastUdpateTime = 0;

    /**
     * This is called every update runs the state machine
     */
    public void update(){
        long currTime = SystemClock.uptimeMillis();
        myRobot.telemetry.addLine("My auto COLLECT state: " + myState);






        /** GOES TO THE CRATER AT THE QUEUED COLLECT LOCATION */
        if(myState == myStates.driveAndDeployCollector.ordinal()){
            if(stageFinished){
                initializeStateVariables();
//                canControlCollector = false;
                myCollector.retractDumper();//raise the myCollector to clear crater
                myRobot.initAutoCollect();//need to call this since we are using followCurve

                //in case we don't have any, add a collecting location to the middle
                if(allCollectingLocations.size() == 0){
                    allCollectingLocations.add(
                            new CollectingLocation(0.5,
                                    Globals.isCrater()));
                }
                myCollector.turnOnRoller();//turn on the collector
                myCollector.setExtensionPowerRaw(0);
            }




            double distanceFromCrater = 29;
            //this will make the distance away from crater more based on how off from
            //45 degrees the collecting location is
            double percentFrom45= Range.clip(Math.abs(MyPosition.subtractAngles(
                    currLocation().getAngle(),
                    Math.toRadians(-141)))/Math.toRadians(30),0,1);
            distanceFromCrater += percentFrom45 * 10;


            boolean doneDriving =
                    myRobot.driveToAutoCollect(currLocation(),
                            distanceFromCrater, 30);

            if(doneDriving){
                myRobot.stopMovement();//we need to stop movement if we are done driving

                //we can actually drive a bit forwards
                //to make sure we're exactly at the edge of the crater
                MovementVars.movement_y = 0.2;//move slowly in



                pointAngle(Math.atan2(currLocation().pointLocationY-myRobot.getYPos(),
                        currLocation().pointLocationX-myRobot.getXPos()),0.4,Math.toRadians(40));



                myCollector.setRunToPositionMode(Extension.RunToPositionModes.powerControl);
                //yeah vroom the collector extension. Eat gobbies eat!
                myCollector.setExtensionPowerRaw(collectorExtensionSpeed);


                //deploy the collector when we are over the crater
                collectorDeployed = myCollector.deployWhenOverCrater();
                canControlCollector = false;//why is this here
            }

            //if we have deployed the collector we can go to the next stage
            if(collectorDeployed){
                myRobot.stopMovement();
                nextStage();
            }
        }


        /**
         * This puts the dumper down or in this case just waits for it
         */
        if(myState == myStates.putDumperDown.ordinal()){
            if(stageFinished){
                initializeStateVariables();
            }


            double distanceFromCrater = 24;
            //this will make the distance away from crater more based on how off from
            //45 degrees the collecting location is
            double percentFrom45= Range.clip(Math.abs(MyPosition.subtractAngles(
                    currLocation().getAngle(),
                    Math.toRadians(-141)))/Math.toRadians(30),0,1);
            distanceFromCrater += percentFrom45 * 10;


            boolean doneDriving =
                    myRobot.driveToAutoCollect(currLocation(),
                            distanceFromCrater, 30);

//            MovementVars.movement_y = 0.2;//move slowly in


            pointAngle(Math.atan2(currLocation().pointLocationY-myRobot.getYPos(),
                    currLocation().pointLocationX-myRobot.getXPos()),
                    0.4,Math.toRadians(20));

            myCollector.setRunToPositionMode(Extension.RunToPositionModes.powerControl);
            //keep extending the extension until we find anything or we are fully extended
            myCollector.setExtensionPowerRaw(collectorExtensionSpeed*0.7);



            //every other trip will be just for 1000 millis
            boolean exit = currTime - stateStartTime > 1000;

            //the state we go to when done depends on if this is a first trip or not
            if(exit){
                if(AutoFeeder.numAutoFeeds <= 1){
                    nextStage();
                }else{
                    nextStage(myStates.scoutingForFood.ordinal());
                }
            }
        }

        /**
         * This state backs up the collector on the first trip
         */
        if(myState == myStates.backupCollectorFirstTrip.ordinal()){
            if(stageFinished){
                initializeStateVariables();
            }
            myCollector.setRunToPositionMode(Extension.RunToPositionModes.powerControl);
            myCollector.setExtensionPowerRaw(-collectorExtensionSpeed/1.5);

            if(myCollector.getExtensionPercent() <= 0.24 || currTime - stateStartTime > 1000){
                nextStage();
                myCollector.setExtensionPowerRaw(0);
            }
        }




        /**
         * This state searches for minerals
         */
        if(myState == myStates.scoutingForFood.ordinal()){
            if(stageFinished){
                initializeStateVariables();
                myCollector.activateDumper();

                collectorTopSpeed = 0;
//                changedScan = false;
            }
            double currentCollectorSpeedPercent = myCollector.getExtensionCurrentSpeedPercent();
            //see if we set a new speed record and flag it
            if(currentCollectorSpeedPercent > collectorTopSpeed){
                collectorTopSpeed = currentCollectorSpeedPercent;
            }


            MovementVars.movement_y = 0.04;//go a bit forwards
            long stateElapsedTime = SystemClock.uptimeMillis() - stateStartTime;//get the elapsed time



//            myCollector.setRunToPositionMode(Extension.RunToPositionModes.powerControl);
            //extend the collector, faster the farther we get
            double power = collectorExtensionSpeed;

            double distToTarget = Math.hypot(myCollector.getExtensionPosition().x -
                    currLocation().pointLocationX, myCollector.getExtensionPosition().y -
                            currLocation().pointLocationY);
            if(distToTarget < 30){
                power *= collectorExtensionSpeed * 0.3;
            }

            myCollector.setExtensionPowerNice(power);


            //add some oscillation to help the minerals get in
            if(stateElapsedTime > 50){
                oscillateAroundCollectingPoint(currLocation(),currTime);
            }



            //if our speed has changed a lot (decreased) from the top speed, we can do a small
            //backup sequence
            if(collectorTopSpeed > 0){
                if(Math.abs(currentCollectorSpeedPercent)/Math.abs(collectorTopSpeed) < 0.3
                        && currTime-stateStartTime > 500){
                    nextStage(myStates.miniBackUp.ordinal());
                }
            }


            //this is the percent at which we will turn and try a different scan
            double COLLECTOR_GIVE_UP_PERCENT = 0.95;
            double collectorPredictedPos = myCollector.getExtensionPercent() +
                    myCollector.getExtensionCurrentSpeedPercent() * 0.05;
            if(collectorPredictedPos > COLLECTOR_GIVE_UP_PERCENT ||
                    distToTarget < 10 ||
                    (currentCollectorSpeedPercent < 0.3 &&
                        currTime-stateStartTime > 500)){
                nextStage(myStates.turnAndRetractToNext.ordinal());


                //now we can remove the 0th index so we go to the next one
                nextCollectingLocation();
            }
        }
        /**
         * This state is a mini backup and moves the collector back a small amount before
         * continuing with scouting for food.
         */
        if(myState == myStates.miniBackUp.ordinal()){
            if(stageFinished){
                initializeStateVariables();
                initialCollectorMiniBackUpPos = myCollector.getExtensionPercent();
            }

            //back up lightly
            myCollector.setExtensionPowerRaw(-0.4);

            //if we have traveled enough, go back to scouting for food
            if(initialCollectorMiniBackUpPos - myCollector.getExtensionPercent() > 0.06){
                nextStage(myStates.scoutingForFood.ordinal());
            }
        }


        /**
         * This will move turn towards the next scanning location as well as retract the collector
         */
        if(myState == myStates.turnAndRetractToNext.ordinal()){
            if(stageFinished){
                initializeStateVariables();
                //retract the dumper for this
                myCollector.retractDumper();
            }


            double retractPercent = 0.1;
            //retract at full speed until we cross 20%
            myCollector.setExtensionPowerNice(
                    myCollector.getExtensionPercent() > retractPercent ? -0.6 : 0);



            /*
            Now we need to see if we need to straif/drive to the new collecting location's
            approach point
             */
            //first get the angle to this collecting location's point location
            double angleToPointLocation =
                    Math.atan2(currLocation().pointLocationY-myRobot.getYPos(),
                            currLocation().pointLocationX-myRobot.getXPos());

            //just go to the position, don't worry about point angle and stuff
            MovementEssentials.goToPosition(currLocation().xApproach,
                    currLocation().yApproach,-1,0.25,-1);

            //now worry about pointing
            movementResult r = pointAngle(angleToPointLocation,
                    0.6,Math.toRadians(20));


            //if we are more than 50% extended it is very hard to turn, so apply a minimum power to the turn
            if(myCollector.getExtensionPercent() > 0.5){
                movement_turn = minPower(movement_turn,0.1);
            }


            //if we are taking a long time, increase the turn by 2x to encourage movement
            if(currTime-stateStartTime > 1000){
                movement_turn *= 2;
            }

            //if we are done go back to the other state
            if(Math.abs(r.turnDelta_rad) < Math.toRadians(4) &&
                    myCollector.getExtensionPercent() <= retractPercent
                    ||
                    (currTime - stateStartTime > 1000 &&
                    myCollector.getExtensionCurrentSpeedPercent() < 0.1)){//check if it is stalled
                myRobot.stopMovement();
                nextStage(myStates.delayBeforeScouting.ordinal());

            }
        }

        /**
         * waits for dumper to deploy before going back to scouting
         */
        if(myState == myStates.delayBeforeScouting.ordinal()){
            if(stageFinished){
                initializeStateVariables();
                delayCollectorDeployTime = 0;
            }

            //attempt to put the collector down
            if(!myCollector.deployWhenOverCrater()){
                //if it fails, move forwards slowly
                myCollector.setExtensionPowerRaw(collectorExtensionSpeed*0.7);
            }else{
                //this means it has succeeded
                if(delayCollectorDeployTime == 0){
                    //flag the current time
                    delayCollectorDeployTime = currTime;
                }

                //stop moving the extension since we are deploying
                myCollector.setExtensionPowerRaw(0);
            }

            //exit if 300 millis have past since deployment
            if(currTime - delayCollectorDeployTime > 150 &&
                    delayCollectorDeployTime != 0){
                //go back to scouting for food
                nextStage(myStates.scoutingForFood.ordinal());
            }
        }





        //save the lastUpdateTime to currTime
        lastUdpateTime = currTime;
    }

    /**
     * Gets the current collecting location
     * @return the current CollectingLocation
     */
    private CollectingLocation currLocation() {
        return allCollectingLocations.get(0);
    }

    /**
     * Removes the 0th index collecting location so we go to the next one
     */
    private void nextCollectingLocation() {
        allCollectingLocations.remove(0);
    }

    /**
     * This method oscillates from left to right around a collecting point
     * @param collectingLocation
     * @param timeMillis
     */
    private void oscillateAroundCollectingPoint(CollectingLocation collectingLocation, double timeMillis) {
        //get the angle to the place we want to points
        double angleToCollectingLocation =
                Math.atan2(collectingLocation.pointLocationY-myRobot.getYPos(),
                        collectingLocation.pointLocationX - myRobot.getXPos());


        //now add an oscillation component to the angle (10 degrees max)
        double deltaAngleOscillation = Math.sin(timeMillis/750.0 * 2.0 * 3.14159)
                * Math.toRadians(8);

        //now calculate the currentTargetAngle which is the sum of the two
        double currentTargetAngle = angleToCollectingLocation + deltaAngleOscillation;

        //now point towards that angle (deceleration radius of 15 degrees)
        MovementEssentials.pointAngle(currentTargetAngle,0.4,Math.toRadians(10));
    }




    /**
     * Starts the autoCollect sequence
     */
    public void autoCollect() {
        //you could call an abort auto feed but we don't want to stop the lift if it's resetting
        myCollector.setExtensionPowerNice(0);
        MovementVars.movement_x = 0;
        MovementVars.movement_y = 0;
        movement_turn = 0;
        canControlCollector = true;


        /**
         * GET THE COLLECTING LCOATION FROM THE PITSCANNERINTERFACE (AUTOMATICALLY)
         */
        if(allCollectingLocations.size() == 0){
            CollectingLocation collectingLocation =
                    new CollectingLocation(0.45,Globals.isCrater());
            allCollectingLocations.add(collectingLocation);
        }

//        grabNewScan();

        nextStage();
        myState = myStates.driveAndDeployCollector.ordinal();
        initializeMasterVariables();
    }

    /**
     * Grabs a new scan
     */
    private void grabNewScan() {
        //set the point location x
        currLocation().pointLocationX = PitScannerInterface.scanXField;
        currLocation().pointLocationY = PitScannerInterface.scanYField;
    }


    /**
     * Returns if we are done collecting
     * @return a boolean that is if we are not collecting (true) or collecting (false)
     */
    public boolean isDoneAutoCollect() {
        return myState == myStates.waiting.ordinal();
    }




    /////////////////////////////QUEUING COLLECT LOCATIONS/////////////////////////////////////////
    public ArrayList<CollectingLocation> allCollectingLocations = new ArrayList<>();

    /**
     * ADDS A COLLECTING LOCATION TO THE QUEUE
     * This is specified by a percent from bottom right to top left
     * (from the driver perspective)
     */
    public void addCollectingLocation(double percent, boolean homeCrater){
        allCollectingLocations.add(new CollectingLocation(percent,homeCrater));
    }

    /**
     * Overload for above
     * @param location the collecting location
     */
    public void addCollectingLocation(CollectingLocation location){
        allCollectingLocations.add(location);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////



    //ECAR: extend collector at rate.
    //This is the time of the last update
    long lastECARUpdateTime = 0;
    /**
     * This will extend the collector using position mode at a controlled rate.
     * @param secondsFor1Extension
     */
    public void extendCollectorAtRate(double secondsFor1Extension){
        long currTime = SystemClock.uptimeMillis();//get the current time
        long elapsedTime = currTime - lastECARUpdateTime;//get the elapsed time
        lastECARUpdateTime = currTime;//save the time for next update

        //don't update if too long has elapsed
        if(elapsedTime > 500){ return;}

        //get the current target percent
        double currPercent = myCollector.getTargetPercent();
        //the new percent is the elapsed time divided by how may seconds 100% takes
        double newPercent = ((currTime-lastUdpateTime)/secondsFor1Extension) + currPercent;
        //go to the new percent
        myCollector.setExtensionTargetPercent(newPercent);
        myCollector.setExtensionPowerRaw(0.2);
    }

}
