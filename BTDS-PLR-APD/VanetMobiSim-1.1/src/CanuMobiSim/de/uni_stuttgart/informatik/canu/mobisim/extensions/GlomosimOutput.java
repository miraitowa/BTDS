package de.uni_stuttgart.informatik.canu.mobisim.extensions;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.simulations.*;
import de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels.Movement;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

/**
 * This module displays mobility traces in GlomoSim format
 * @author Illya Stepanov
 */
public class GlomosimOutput extends ExtensionModule
{
  java.io.PrintStream nodePlacement;

  java.io.PrintStream nodeMobility;

  TimeSimulation simulation;

  /**
   * Constructor
   */
  public GlomosimOutput() throws Exception
  {
    super("GlomosimOutput");
    
    nodePlacement = new java.io.PrintStream(
      new java.io.FileOutputStream("nodes.input"));

    nodeMobility = new java.io.PrintStream(
      new java.io.FileOutputStream("mobility.in"));

    u.addNotificationListener(this);
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return module's description
   */
  public String getDescription()
  {
    return "GlomoSim tracing module";
  }

  /**
   * Performs the module initialization. <br>
   * <br>
   * The method is called after finishing the scenario processing.
   */
  public void initialize()
  {
    super.initialize();

    // get TimeSimulation extension
    simulation = (TimeSimulation)u.getExtension("TimeSimulation");
  }

  /**
   * Notification passing method. <br>
   * <br>
   * @param notification notification
   */
  public void sendNotification(Notification notification)
  {
    if (notification instanceof StartingPositionSetNotification)
      outputNotification((StartingPositionSetNotification)notification);
    else
    if (notification instanceof MovementNotification)
      outputNotification((MovementNotification)notification);
  }

  /**
   * Displays the notification in GlomoSim trace format. <br>
   * <br>
   * @param notification notification to display
   */
  public void outputNotification(MovementNotification notification)
  {
    Node node = (Node)((Movement)notification.getSender()).getOwner();

    int i = u.getNodes().indexOf(node);

    nodeMobility.println(i+" "+u.getTime()/1000f+"S"+
      " ("+node.getPosition().getX()+" "
      +node.getPosition().getY()+" "+node.getPosition().getZ()+")");
  }

  /**
   * Displays the notification in GlomoSim trace format. <br>
   * <br>
   * @param notification notification to display
   */
  public void outputNotification(StartingPositionSetNotification notification)
  {
    Node node = (Node)notification.getSender();

    // output position of a mobile node only
    if (node.getExtension("Movement")==null)
      return;

    int i = u.getNodes().indexOf(node);

    nodePlacement.println(i+" 0 ("+node.getPosition().getX()+" "
      +node.getPosition().getY()+" "+node.getPosition().getZ()+")");
  }

  /**
   * Executes the extension. <br>
   * <br>
   * The method is called on every simulation timestep.
   * @return 0 - the module should be executed on next timesteps,
   *        -1 - the module should not be executed on further timesteps and should be removed from the extensions' list
   */
  public int act()
  {
    // check if has to finish soon - GlomoSim fix
    if (u.getTime()+u.getStepDuration() >= simulation.getFinishTime())
    {
      // output positions of mobile nodes
      for (int i=0; i<u.getNodes().size(); i++)
      {
        Node node = (Node)u.getNodes().get(i);
        nodeMobility.println(i+" "+u.getTime()/1000f+"S"+
          " ("+node.getPosition().getX()+" "
          +node.getPosition().getY()+" "+node.getPosition().getZ()+")");
     }
    }
    
    return 0;
  }
}