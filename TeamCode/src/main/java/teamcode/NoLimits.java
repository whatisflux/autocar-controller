package teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import HelperClasses.Auto;
import HelperClasses.Robot;


@Autonomous(name = "NoLimits", group = "NoLimits")
public class NoLimits extends Auto {

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void init_loop(){
        super.init_loop();
    }

    @Override
    public void start(){
        super.start();
    }

    @Override
    public void loop(){
        super.loop();
        hangMechanism.setPowerNoLimits(gamepad1.right_trigger-gamepad1.left_trigger);
        ControlMovement();
        hangMechanism.ResetEncoder();
    }


}

