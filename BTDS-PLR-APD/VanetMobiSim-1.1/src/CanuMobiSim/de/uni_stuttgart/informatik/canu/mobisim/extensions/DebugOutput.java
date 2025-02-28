package de.uni_stuttgart.informatik.canu.mobisim.extensions;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
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
 * This module enables displaying of debug information
 * @author Illya Stepanov
 */
public class DebugOutput extends ExtensionModule
{
  /**
   * Output Stream
   */
  protected java.io.PrintStream o = System.err;
  
  /**
   * Constructor
   */
  public DebugOutput()
  {
    super("DebugOutput");
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Debug output displaying module";
  }

  /**
   * Notification passing method. <br>
   * <br>
   * @param notification notification
   */
  public void sendNotification(Notification notification)
  {
    String s = notification.toString();
    if (s.length()>0)
      o.println(s);
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
      "Loading DebugOutput extension"));

    super.load(element);

    org.w3c.dom.Node n;

    String outName = element.getAttribute("output");
    if (outName.length()>0)
      o = new java.io.PrintStream(new java.io.FileOutputStream(outName));    

	u.addNotificationListener(this);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading DebugOutput extension"));
  }
}