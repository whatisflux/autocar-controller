package Main;


import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * This is the actual opmode
 */

@TeleOp

public class Main extends Robot{



    public void loop(){
        super.loop();
        controlMovement();

        if(gamepad1.x){
            swerveDrive.resetEncoders();
        }

    }
}
