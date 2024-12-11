
        package org.firstinspires.ftc.teamcode.ConfigRR;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.matrices.GeneralMatrixF;
import org.firstinspires.ftc.robotcore.external.matrices.MatrixF;

public class TeleOpTank {
     DcMotor motorL;
     DcMotor motorR;


    public static double GEAR_RATIO = 1.0; // for simulator - ours should be 0.5f;

    public static double WHEEL_RADIUS = 5.0;  // 5 cm
    public static double TICKS_PER_ROTATION = 834.645664;  // From NeveRest (for simulator)  GoBilda should be 383.6f


    public static double CM_PER_TICK = (2 * Math.PI * GEAR_RATIO * WHEEL_RADIUS) / TICKS_PER_ROTATION;

    private double maxSpeed = 0.5;

    private MatrixF conversion;
    private GeneralMatrixF encoderMatrix = new GeneralMatrixF(3, 1);

    private int LeftOffset;
    private int RightOffset;



    public TeleOpMecanum() {
        float[] data = {1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f};
        conversion = new GeneralMatrixF(3, 3, data);
        conversion = conversion.inverted();
    }



    public void init(HardwareMap hwMap) {
        motorL = hwMap.get(DcMotor.class, "left");
       // motor0.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorR = hwMap.get(DcMotor.class, "right");
    //    motor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


//DcMotor.ZeroPowerBehavior.FLOAT


        //Updated by gcf to test motor direction issues.
        motorL.setDirection(DcMotorSimple.Direction.FORWARD);
        motorR.setDirection(DcMotorSimple.Direction.REVERSE);


        motorL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


    }

    private void setSpeeds(double LSpeed, double RSpeed) {
        double largest = maxSpeed;
        largest = Math.max(largest, Math.abs(LSpeed));
        largest = Math.max(largest, Math.abs(RSpeed));


        motorL.setPower(LSpeed / largest);
        motorR.setPower(RSpeed / largest);

    }

    public void driveMecanum(double forward, double rotate) {
        double LeftSpeed = forward + rotate;
        double RightSpeed = forward - rotate;

        setSpeeds(LeftSpeed, RightSpeed);
    }

    // Returns forward, strafe
    double[] getDistanceCm() {
        double[] distances = {0.0, 0.0};

        encoderMatrix.put(0, 0, (float) ((motorL.getCurrentPosition() - LeftOffset) * CM_PER_TICK));
        encoderMatrix.put(1, 0, (float) ((motorR.getCurrentPosition() - RightOffset) * CM_PER_TICK));
        //encoderMatrix.put(2, 0, (float) ((motor2.getCurrentPosition() - backLeftOffset) * CM_PER_TICK));

        MatrixF distanceMatrix = conversion.multiplied(encoderMatrix);
        distances[0] = distanceMatrix.get(0, 0);
        distances[1] = distanceMatrix.get(1, 0);

        return distances;
    }

    void setMaxSpeed(double speed) {
        maxSpeed = Math.min(speed, 1.0);
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setEncoderOffsets() {
        LeftOffset = motorL.getCurrentPosition();
        RightOffset = motorR.getCurrentPosition();

    }
}
