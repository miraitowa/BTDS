package de.uni_stuttgart.informatik.canu.mobisim.extensions;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels.Movement;


/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

/**
 * This module displays mobility traces in NS-2 format
 * @author Illya Stepanov
 */
public class NSOutput extends ExtensionModule
{
  /**
   * Output Stream
   */
  protected java.io.PrintStream o = System.out;
  
  /**
   * Constructor
   */
  public NSOutput()
  {
    super("NSOutput");
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "NS-2 tracing module";
  }

  /**
   * Performs the module initialization. <br>
   * <br>
   * The method is called after finishing the scenario processing.
   */
  public void initialize()
  {
    super.initialize();
    
    // write scenario information
    o.println("#");
    o.println("# nodes: "+u.getNodes().size()+", pause: "+Float.MAX_VALUE+", max speed: "+Float.MAX_VALUE+"  max x = "+u.getDimensionX()+", max y: "+u.getDimensionY());
    o.println("#");
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
    if (notification instanceof MovementChangedNotification)
      outputNotification((MovementChangedNotification)notification);
  }

  /**
   * Displays the notification in NS-2 format. <br>
   * <br>
   * @param notification notification to display
   */
  public void outputNotification(MovementChangedNotification notification)
  {
    Position3D destination = notification.getDestination();
    Node node = (Node)((Movement)notification.getSender()).getOwner();

    int i = u.getNodes().indexOf(node);

    o.println("$ns_ at "+u.getTime()/1000f+
      " \"$node_("+i+") setdest "+
      (destination.getX()+0.000001)+" "+
      (destination.getY()+0.000001)+" "+
      notification.getSpeed()+"\"");
  }

  /**
   * Displays the notification in NS-2 format. <br>
   * <br>
   * @param notification notification to display
   */
  public void outputNotification(StartingPositionSetNotification notification)
  {
    Node node = (Node)notification.getSender();

    // output position of mobile nodes only
    if (node.getExtension("Movement")==null)
      return;

    int i = u.getNodes().indexOf(node);

    o.println("$node_("+i+") set X_ "+
      (node.getPosition().getX()+0.000001));
    o.println("$node_("+i+") set Y_ "+
      (node.getPosition().getY()+0.000001));
    o.println("$node_("+i+") set Z_ "+node.getPosition().getZ());
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
    return 0;
  }
  
  /**
    * Initializes the object from XML tag. <br>
    * <br>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
  public void load(org.w3c.dom.Element element) throws java.lang.Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading NSOutput extension"));

    super.load(element);

    org.w3c.dom.Node n;

    String outName = element.getAttribute("output");
    if (outName.length()>0)
      o = new java.io.PrintStream(new java.io.FileOutputStream(outName));    

    u.addNotificationListener(this);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading NSOutput extension"));
  }
}