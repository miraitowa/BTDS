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
 * This module displays loader debug information
 * @author Illya Stepanov
 */
public class LoaderOutput extends ExtensionModule
{
  /**
   * Buffer to store messages before initialization is completed
   */
  protected StringBuffer sb = new StringBuffer();

  /**
   * Output Stream
   */
  protected java.io.PrintStream o = System.err;
  
  /**
   * Constructor
   */
  public LoaderOutput()
  {
    super("LoaderOutput");
    
    u.addNotificationListener(this);    
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Loader tracing module";
  }

  /**
   * Notification passing method. <br>
   * <br>
   * @param notification notification
   */
  public void sendNotification(Notification notification)
  {
    if (notification instanceof LoaderNotification)
    {
      if (sb!=null)
        sb.append(((LoaderNotification)notification).getDescription()).append('\n');
      else
        o.println(((LoaderNotification)notification).getDescription());
    }
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
    super.load(element);

    org.w3c.dom.Node n;

    String outName = element.getAttribute("output");
    if (outName.length()>0)
      o = new java.io.PrintStream(new java.io.FileOutputStream(outName));
    
    // print stored messages, if any  
    if (sb.length()>0)
      o.print(sb);
    // disable message buffering
    sb = null;
  }
}