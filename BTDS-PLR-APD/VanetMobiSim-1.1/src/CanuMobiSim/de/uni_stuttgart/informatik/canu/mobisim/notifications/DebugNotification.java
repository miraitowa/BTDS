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
 * This class implements the notification with textual description
 * @author Illya Stepanov
 */
public class DebugNotification extends Notification
{
  /**
   * Debug event's description
   */
   protected String description;

  /**
   * Constructor. <br>
   * <br>
   * @param sender notification's sender
   * @param receiver notification's receiver
   * @param string a string describing the event
   */
  public DebugNotification(Object sender, Object receiver, String string)
  {
    super(sender, receiver);
    description = string;
  }

  /**
   * Returns a string representation of the object. <br>
   * <br>
   * @return string representation of the object
   */
  public String toString()
  {
    return description;
  }
}
