package org.firstinspires.ftc.teamcode.opmode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.enums.Team;

@Autonomous( name = "Red Team Basket", group = "13702" )
public class RedTeamBaskets extends AbstractAutonomousOpMode
{
  public RedTeamBaskets()
  {
    super( Team.RED, GameStrategy.PLACE_SAMPLES_IN_BASKETS );
  }
}