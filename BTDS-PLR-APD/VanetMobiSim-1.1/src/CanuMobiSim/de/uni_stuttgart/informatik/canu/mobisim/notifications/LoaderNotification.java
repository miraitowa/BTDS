package de.uni_stuttgart.informatik.canu.mobisim.notifications;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:  
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

/**
 * This class implements the debug loader notification
 * @author Illya Stepanov
 */

public class LoaderNotification extends DebugNotification
{
  /**
   * Constructor. <br>
   * <br>
   * @param sender notification's sender
   * @param receiver notification's receiver
   * @param string a string describing the event
   */
  public LoaderNotification(Object sender, Object receiver, String string)
  {
    super(sender, receiver, string);
  }

  /**
   * Gets the notification's description. <br>
   * <br>
   * @return notification's description
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Returns a string representation of the object. <br>
   * <br>
   * @return string representation of the object
   */
  public String toString()
  {
    return "";
  }
}