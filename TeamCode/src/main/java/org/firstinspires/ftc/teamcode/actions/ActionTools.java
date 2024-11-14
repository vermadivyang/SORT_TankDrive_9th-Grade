package org.firstinspires.ftc.teamcode.actions;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;

import org.firstinspires.ftc.teamcode.Gamepads;
import org.firstinspires.ftc.teamcode.JoeBot;
import org.firstinspires.ftc.teamcode.enums.Button;
import org.firstinspires.ftc.teamcode.enums.Participant;

public class ActionTools
{
  public static void runBlocking( JoeBot robot, Action action )
  {
    FtcDashboard dash = FtcDashboard.getInstance();
    Canvas previewCanvas = new Canvas();
    action.preview(previewCanvas);

    boolean running = true;

    while( running &&
           !Thread.currentThread().isInterrupted() )
    {
      if( robot.gamepads != null )
      {
        robot.gamepads.storeLastButtons();

        if( robot.gamepads.buttonPressed( Participant.OPERATOR, Button.B ) )
        {
          robot.stop();
          robot.gamepads.storeLastButtons();
          return;
        }
      }

      TelemetryPacket packet = new TelemetryPacket();
      packet.fieldOverlay().getOperations().addAll(previewCanvas.getOperations());

      robot.clearBulkCache();
      running = action.run(packet);

      dash.sendTelemetryPacket(packet);
    }
  }
}
