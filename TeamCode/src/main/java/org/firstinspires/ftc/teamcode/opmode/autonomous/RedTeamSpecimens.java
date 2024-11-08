package org.firstinspires.ftc.teamcode.opmode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.enums.Team;

@Autonomous( name = "Red Team Specimens", group = "13702" )
public class RedTeamSpecimens extends AbstractAutonomousOpMode
{
  public RedTeamSpecimens()
  {
    super( Team.RED, GameStrategy.HANG_SPECIMENS_ON_BARS );
  }
}