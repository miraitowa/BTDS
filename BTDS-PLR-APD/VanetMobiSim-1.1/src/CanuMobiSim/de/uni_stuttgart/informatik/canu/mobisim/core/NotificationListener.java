package de.uni_stuttgart.informatik.canu.mobisim.core;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:  
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

/**
 * This interface is for notification receiving
 * @author Illya Stepanov
 */
public interface NotificationListener
{
  /**
   * Notification passing method. <br>
   * <br>
   * @param notification notification
   */
  public void sendNotification(Notification notification);
}
