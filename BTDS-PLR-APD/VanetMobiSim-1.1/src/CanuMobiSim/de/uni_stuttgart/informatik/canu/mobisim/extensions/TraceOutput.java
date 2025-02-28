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
 * This module displays mobility traces in own format
 * @author Illya Stepanov
 */
public class TraceOutput extends ExtensionModule
{
  protected boolean firstTime = true;

  /**
   * Output Stream
   */
  protected java.io.PrintStream o = System.out;
  
  /**
   * Constructor
   */
  public TraceOutput()
  {
    super("TraceOutput");
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Traces producing module";
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
   * Displays the notification. <br>
   * <br>
   * @param notification notification to display
   */
  public void outputNotification(MovementChangedNotification notification)
  {
    Position3D destination = notification.getDestination();
    Node node = (Node)((Movement)notification.getSender()).getOwner();

    int i = u.getNodes().indexOf(node);

    o.println(i+" "+u.getTime()/1000f+" "+
      destination.getX()+" "+
      destination.getY()+" "+
      notification.getSpeed());
  }

  /**
   * Displays the notification. <br>
   * <br>
   * @param notification notification to display
   */
  public void outputNotification(StartingPositionSetNotification notification)
  {
    if (firstTime)
    {
      displayScenario();
      firstTime = false;
    }

    Node node = (Node)notification.getSender();

    // output position of mobile nodes only
    if (node.getExtension("Movement")==null)
      return;

    int i = u.getNodes().indexOf(node);

    o.println(""+i+" "+node.getPosition().getX()+" "+node.getPosition().getY());
  }

  /**
   * Displays the parameters of simulation scenario 
   */
  protected void displayScenario()
  {
    o.println("# area: "+u.getDimensionX()+" m x "+u.getDimensionY()+" m");
    
    // enumerate node groups and movement parameters
    int count = 0;
    float minSpeed = Float.NaN;
    float maxSpeed = Float.NaN;
    java.util.Iterator iter = u.getNodes().iterator();
    while (iter.hasNext())
    {
      Node node = (Node)iter.next();
      Movement movement = (Movement)node.getExtension("Movement");
      
      if (count==0)
      {
        minSpeed = movement.getMinSpeed();
        maxSpeed = movement.getMaxSpeed();
      }
      else
      {
        float mins = movement.getMinSpeed();
        float maxs = movement.getMaxSpeed();
        
        if ((mins!=minSpeed)||(maxs!=maxSpeed))
        {
          o.println("# "+count+" nodes: "+minSpeed*1000.0f+" (m/s) - "+maxSpeed*1000.0f+" (m/s)");

          // start new group
          count=0;
        }
        
        minSpeed = mins;
        maxSpeed = maxs;
      }
      count++;
    }
    
    o.println("# "+count+" nodes: "+minSpeed*1000.0f+" (m/s) - "+maxSpeed*1000.0f+" (m/s)");
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
      "Loading TraceOutput extension"));

    super.load(element);

    org.w3c.dom.Node n;

    String outName = element.getAttribute("output");
    if (outName.length()>0)
      o = new java.io.PrintStream(new java.io.FileOutputStream(outName));    

    u.addNotificationListener(this);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading TraceOutput extension"));
  }
}