package org.firstinspires.ftc.teamcode;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="TeleOp Tank | Old")
public class TeleOpTester1 extends OpMode {
  
    double forward=0;
    double rotate=0;


    boolean a_prev = false;
    boolean a_state = false;  
  
    public double MAXSPEED = .75;
    TankDrive drive = new TankDrive(hardwareMap, new Pose2d(0,0,0));

    public void init(){
      telemetry.addLine("init complete");
      telemetry.update();
    }

    @Override
    public void loop() {
       
      rotate = gamepad1.right_stick_x * MAXSPEED;
      forward = -gamepad1.left_stick_y * MAXSPEED;

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

        drive.setDrivePowers(
           new PoseVelocity2d(
                new Vector2d(forward, 0),
                -rotate
           )
        );


        telemetry.addData("lift",gamepad2.right_stick_y);
        telemetry.update();

    }
}
