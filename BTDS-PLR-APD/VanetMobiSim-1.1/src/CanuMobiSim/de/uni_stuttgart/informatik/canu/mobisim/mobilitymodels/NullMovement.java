package de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.3
 */

/**
 * This class implements the Null Movement
 * @author Illya Stepanov
 */
public class NullMovement extends Movement
{
  /**
   * Constructor
   */
  public NullMovement()
  {
  }

  /**
   * Constructor. <br>
   * <br>
   * @param node parent {@link Node Node} object
   */
  public NullMovement(Node node)
  {
    super(node);
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Null movement module";
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
  public void load(org.w3c.dom.Element element) throws Exception
  {
    Node owner = (Node)this.owner;

    u.sendNotification(new LoaderNotification(this, u,
      "Loading NullMovement extension"));

    super.load(element);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading NullMovement extension"));
  }//proc
}