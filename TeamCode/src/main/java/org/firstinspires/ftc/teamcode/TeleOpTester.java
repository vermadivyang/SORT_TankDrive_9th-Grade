package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="TeleOp | Tank")
public class TeleOpTester extends OpMode {
  
    double forward=0;
    double strafe=0;
    double rotate=0;

    boolean a_prev = false;
    boolean a_state = false;

    public double MAXSPEED = .75;
    TeleOpTank d = new TeleOpTank();

    public void init(){
        d.init(hardwareMap);

      telemetry.addLine("init complete");
      telemetry.update();
    }

    @Override
    public void loop() {
       
        forward=gamepad1.left_stick_y*-MAXSPEED;
        rotate = -gamepad1.right_stick_x*MAXSPEED;
        strafe = gamepad1.left_trigger*MAXSPEED-gamepad1.right_trigger*MAXSPEED;

        d.driveTank(forward,rotate);


        //Turtle Mode
        if (gamepad1.a && !a_prev) {
            if(!a_state) {
                MAXSPEED = 0.3;
                a_state = true;
            } else {
                MAXSPEED = 0.85;
                a_state = false;
            }
        }

        telemetry.addData("lift",gamepad2.right_stick_y);
        telemetry.update();

    }
}
