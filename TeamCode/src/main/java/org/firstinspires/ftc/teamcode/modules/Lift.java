package org.firstinspires.ftc.teamcode.modules;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Lift extends AbstractModule
{
  private static final double SPEED_UP = 1.0;
  private static final double SPEED_DOWN = 1.0;

  private static final int ADJUST_UP   = 70;
  private static final int ADJUST_DOWN = 70;

  //coast down until we are close to our target
  private static final int FAR_AWAY = 200;

  //only coast down above a minimum height since
  //gravity does not seem to cause the lift as we get close
  //to the bottom and it is necessary to use the motors to pull the lift
  //the rest of the way down.
  private static final int MINIMUM_COAST_HEIGHT = 484;

  public static boolean allowReset = true;

  //Preset positions we can extend the arm to
  public enum Position
  {
    FLOOR( 0 ),
    MAX_LIFT( 3600 ),

    SAMPLE_FLOOR( 18 ),
    SPECIMEN_FLOOR( 40 ),

    //high enough that we don't hit the submersible bar when retracting
    TRAVEL_WITH_SPECIMEN( 332 ),

    //putting samples in baskets
    HIGH_BASKET( 3600 ),
    LOW_BASKET( 2206 ),

    //hanging specimens
    ABOVE_HIGH_SPECIMEN_BAR( 2342 ),
    ABOVE_LOW_SPECIMEN_BAR( 1323 ),
    SPECIMEN_CLIPPED_ONTO_HIGH_BAR( 2170 ),
    SPECIMEN_CLIPPED_ONTO_LOW_BAR( 1100 ),

    //level 1 ascent
    AT_LOW_HANG_BAR( 1465 ),

    //level 2 ascent
    ABOVE_ABOVE_HANG_BAR( 3095 ),
    TOUCHING_HIGH_HANG_BAR( 2551 ),

    //height above which we should limit extending the extension arm to avoid tipping over
    HIGH_UP( 2403 );

    Position( int value )
    {
      this.value = value;
    }

    public final int value;
  }

  DcMotorEx leftMotor = null;
  DcMotorEx rightMotor = null;

  private enum Action
  {
    MOVING,
    CLIMBING,
    STOPPED
  }

  private Action currentAction = Action.STOPPED;
  private double targetPower = 1;

  public Lift( HardwareMap hardwareMap, Telemetry telemetry )
  {
    super( hardwareMap, telemetry );
    initObjects();
    initState();
  }

  private void initObjects()
  {
    leftMotor = createMotor( "leftLiftMotor" );
    rightMotor = createMotor( "rightLiftMotor" );
  }

  private void initState()
  {
    final DcMotor.RunMode runMode = DcMotor.RunMode.RUN_TO_POSITION;
    initMotor( leftMotor, runMode, DcMotorSimple.Direction.REVERSE );
    initMotor( rightMotor, runMode, DcMotorSimple.Direction.FORWARD );
  }

  public boolean isHigh()
  {
    return liftPosition() >= Position.HIGH_UP.value;
  }

  public boolean isMoving()
  {
    return currentAction != Action.STOPPED;
  }

  public int liftPosition()
  {
    return leftMotor.getCurrentPosition();
  }

  public boolean raiseLift()
  {
    return turnMotors( DcMotorSimple.Direction.FORWARD, SPEED_UP );
  }

  public boolean lowerLift()
  {
    return turnMotors( DcMotorSimple.Direction.REVERSE, SPEED_DOWN );
  }

  public boolean climb()
  {
    double power = 1.0;
    targetPower = 1.0;
    final int position = Position.TOUCHING_HIGH_HANG_BAR.value;

    setMotorPosition( leftMotor, position, power );
    setMotorPosition( rightMotor, position, power );
    currentAction = Action.CLIMBING;
    return true;
  }

  private double adjustPower( int targetPosition, double power )
  {
    targetPower = power;

    //it is smoother if we coast downwards until we get
    //close to our target position
    final int liftCurPosition = liftPosition();

    if( targetPosition < liftCurPosition &&
        liftCurPosition > MINIMUM_COAST_HEIGHT &&
        liftCurPosition - targetPosition >= FAR_AWAY )
    {
      return 0;
    }
    else
    {
      return power;
    }
  }

  public void travelTo( Position position )
  {
    travelTo( position.value );
  }

  public void travelTo( int liftNewPosition )
  {
    final int liftCurPosition = liftPosition();

    double power = liftNewPosition > liftCurPosition ?
                   SPEED_UP :
                   SPEED_DOWN;

    power = adjustPower( liftNewPosition, power );

    setMotorPosition( leftMotor, liftNewPosition, power );
    setMotorPosition( rightMotor, liftNewPosition, power );
    currentAction = Action.MOVING;

    if( liftNewPosition < liftCurPosition &&
        power > 0 )
    {
      autoDetectStall = true;
      stallTimer.reset();
    }
    else
    {
      autoDetectStall = false;
    }
  }

  public void floatMotors()
  {
    leftMotor.setZeroPowerBehavior( DcMotor.ZeroPowerBehavior.FLOAT );
    rightMotor.setZeroPowerBehavior( DcMotor.ZeroPowerBehavior.FLOAT );
    leftMotor.setPower( 0 );
    rightMotor.setPower( 0 );
    currentAction = Action.STOPPED;
  }

  public void stop()
  {
    leftMotor.setZeroPowerBehavior( DcMotor.ZeroPowerBehavior.BRAKE );
    rightMotor.setZeroPowerBehavior( DcMotor.ZeroPowerBehavior.BRAKE );
    leftMotor.setTargetPosition( leftMotor.getCurrentPosition() );
    rightMotor.setTargetPosition( rightMotor.getCurrentPosition() );
    leftMotor.setPower( 1 );
    rightMotor.setPower( 1 );
    currentAction = Action.STOPPED;
    autoDetectStall = false;
  }

  public void updateState()
  {
    if( currentAction == Action.STOPPED )
    { return; }

    final int leftPos = leftMotor.getCurrentPosition();
    final int rightPos = rightMotor.getCurrentPosition();
    final int leftTarget = leftMotor.getTargetPosition();
    final int rightTarget = rightMotor.getTargetPosition();
    final int leftDiff = Math.abs( leftPos - leftTarget );
    final int rightDiff = Math.abs( rightPos - rightTarget );
    final int minDiff = Math.min( leftDiff, rightDiff );

    //detect stall
    if( autoDetectStall &&
        stallTimer.milliseconds() > 1000 &&
        Math.min( Math.abs( leftMotor.getVelocity() ),
                  Math.abs( rightMotor.getVelocity() ) ) <= 0.2 )
    {
      telemetry.log().add( "Stall detected, stopping lift!" );
      stop();

      //reset the motor position if we were lowering to the floor
      //since the belts must have slipped and we are at the new zero
      if( leftTarget == Position.FLOOR.value &&
          Math.max( leftPos, rightPos ) > leftTarget &&
          minDiff < 100 &&
          allowReset )
      {
        telemetry.log().add( "Resetting lift motor positions" );

        leftMotor.setMode( DcMotor.RunMode.STOP_AND_RESET_ENCODER );
        leftMotor.setTargetPosition( 0 );
        leftMotor.setMode( DcMotor.RunMode.RUN_TO_POSITION );

        rightMotor.setMode( DcMotor.RunMode.STOP_AND_RESET_ENCODER );
        rightMotor.setTargetPosition( 0 );
        rightMotor.setMode( DcMotor.RunMode.RUN_TO_POSITION );
      }
    }
    //stop once we get close to our target position
    else if( minDiff <= 5 )
    {
      telemetry.log().add( String.format( "Lift.updateState stopping, diff: %s", minDiff ) );

      leftMotor.setZeroPowerBehavior( DcMotor.ZeroPowerBehavior.BRAKE );
      rightMotor.setZeroPowerBehavior( DcMotor.ZeroPowerBehavior.BRAKE );
      leftMotor.setPower( 1 );
      rightMotor.setPower( 1 );
      currentAction = Action.STOPPED;
      autoDetectStall = false;
    }
    //switch from floating to using power once we get close to our target position
    else if( leftMotor.getZeroPowerBehavior() == DcMotor.ZeroPowerBehavior.FLOAT &&
             ( liftPosition() <= MINIMUM_COAST_HEIGHT ||
               minDiff < FAR_AWAY ) )
    {
      leftMotor.setPower( targetPower );
      rightMotor.setPower( targetPower );
      leftMotor.setZeroPowerBehavior( DcMotor.ZeroPowerBehavior.BRAKE );
      rightMotor.setZeroPowerBehavior( DcMotor.ZeroPowerBehavior.BRAKE );
      autoDetectStall = true;
      stallTimer.reset();
    }
  }

  //Prints out the extension arm motor position
  @Override
  public void printTelemetry()
  {
    telemetry.addLine( String.format( "Lift Action: %s", currentAction ) );

    if( leftMotor != null )
    {
      telemetry.addLine( String.format( "Left Lift Motor: %s", leftMotor.getCurrentPosition() ) );
      telemetry.addLine( String.format( "Left Lift Velocity: %f", leftMotor.getVelocity() ) );
    }

    if( rightMotor != null )
    { telemetry.addLine( String.format( "Right Lift Motor: %s", rightMotor.getCurrentPosition() ) ); }
  }

  private boolean turnMotors( DcMotorSimple.Direction direction, double power )
  {
    if( power <= 0 )
    {
      stop();
      return false;
    }

    int liftCurPosition = liftPosition();
    int liftNewPosition = direction == DcMotorSimple.Direction.FORWARD ?
                          liftCurPosition + ADJUST_UP :
                          liftCurPosition - ADJUST_DOWN;

    //Prevent moving too far
    if( liftNewPosition > Position.MAX_LIFT.value )
    {
      liftNewPosition = Position.MAX_LIFT.value;
    }
    else if( liftNewPosition < Position.FLOOR.value )
    {
      liftNewPosition = Position.FLOOR.value;
    }

    // Ensure we continue to lift to preset position as we release buttons
    if( currentAction != Action.STOPPED )
    {
      if( direction == DcMotorSimple.Direction.FORWARD &&
          liftNewPosition <= leftMotor.getTargetPosition() )
      { return false; }

      if( direction == DcMotorSimple.Direction.REVERSE &&
          liftNewPosition >= leftMotor.getTargetPosition() )
      { return false; }
    }

    if( liftNewPosition != leftMotor.getCurrentPosition() ||
        liftNewPosition != rightMotor.getCurrentPosition() )
    {
      //never coast when manually moving the lift
      targetPower = power;

      setMotorPosition( leftMotor, liftNewPosition, power );
      setMotorPosition( rightMotor, liftNewPosition, power );
      Action cachedAction = currentAction;
      currentAction = Action.MOVING;

      if( liftNewPosition < liftCurPosition )
      {
        autoDetectStall = true;
        stallTimer.reset();
      }
      return currentAction != cachedAction;
    }
    else
    { return false; }
  }

  private void setMotorPosition( DcMotorEx motor, int position, double power )
  {
    if( motor == null )
    { return; }

    motor.setTargetPosition( position );
    motor.setPower( power );
    motor.setZeroPowerBehavior( power == 0 &&
                                targetPower != 0 ?
                                DcMotor.ZeroPowerBehavior.FLOAT :
                                DcMotor.ZeroPowerBehavior.BRAKE );
  }

}
