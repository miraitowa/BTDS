package de.uni_stuttgart.informatik.canu.mobisim.extensions;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2004
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

/**
 * This module reports periodically positions of mobile nodes
 * @author Illya Stepanov
 */
public class ReportNodeMobility extends ExtensionModule
{
  /**
   * Step of reporting (in ms)
   */
  protected int step;

  /**
   * Output Stream
   */
  protected java.io.PrintStream o = System.err;

  /**
   * Constructor
   */
  public ReportNodeMobility()
  {
    super("ReportNodeMobility");
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
    Universe u = Universe.getReference();

    if ( (u.getTime() % step == 0) )
    {
      activate();
    }
    
    return 0;
  }

  /**
   * Dumps node positions
   */
  public void activate()
  {
    // dump node positions
    java.util.Iterator iter = u.getNodes().iterator();
    while (iter.hasNext())
    {
      Node node = (Node)iter.next();
      Movement movement = (Movement)node.getExtension("Movement");
      o.println(node.getID()+" "+(float)u.getTime()/1000+" "+
                node.getPosition().getX()+" "+node.getPosition().getY()+" "+
                movement.getSpeed()*1000);
    }
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Node positions and speeds reporting module";
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
      "Loading ReportNodeMobility extension"));

    super.load(element);

    org.w3c.dom.Node n;

    String outName = element.getAttribute("output");
    if (outName.length()>0)
      o = new java.io.PrintStream(new java.io.FileOutputStream(outName));    

    n=element.getElementsByTagName("step").item(0);
    if(n==null)
      throw new Exception("<step> is missing!");
    step=(int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000.0f);

    // checkout
    if (step<=0)
      throw new Exception("Step value is invalid: "+step);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading ReportNodeMobility extension"));
  }
}
