package de.uni_stuttgart.informatik.canu.mobisim.notifications;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:  
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

 /**
  * This class implements the notification about setting the node's initial position
  * @author Illya Stepanov
  */
public class StartingPositionSetNotification extends MovementNotification
{
  /**
   * Constructor. <br>
   * <br>
   * @param sender notification's sender
   * @param receiver notification's receiver
   */
  public StartingPositionSetNotification(Object sender, Object receiver)
  {
    super(sender, receiver);
  }

  /**
   * Returns a string representation of the object. <br>
   * <br>
   * @return string representation of the object
   */
  public String toString()
  {
    Universe u = Universe.getReference();

    Node node = (Node)sender;
    return "at "+u.getTimeAsString()+" "+node.getID()+
        " located at "+node.getPosition();
  }
}