package de.uni_stuttgart.informatik.canu.mobisim.notifications;

import de.uni_stuttgart.informatik.canu.mobisim.core.Notification;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:  
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

 /**
  * This class is a base class for notifications concerning node's movement
  * @author Illya Stepanov
  */
public class MovementNotification extends Notification
{
  /**
   * Constructor. <br>
   * <br>
   * @param sender notification's sender
   * @param receiver notification's receiver
   */
  public MovementNotification(Object sender, Object receiver)
  {
    super(sender, receiver);
  }
}