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
 * A base class for notifications
 * @author Illya Stepanov
 */
public class Notification
{
  /**
   * The sender of notification
   */
  protected Object sender;
  /**
   * The receiver of notification
   */
  protected Object receiver;

  /**
   * Constructor. <br>
   * <br>
   * @param sender the notification's sender
   * @param receiver the notification's receiver
   */
  public Notification(Object sender, Object receiver)
  {
    this.sender   = sender;
    this.receiver = receiver;
  }

  /**
   * Gets the notification's sender. <br>
   * <br>
   * @return notification's sender
   */
  public Object getSender()
  {
    return sender;
  }

  /**
   * Gets the notification's receiver. <br>
   * <br>
   * @return notification's receiver
   */
  public Object getReceiver()
  {
    return receiver;
  }
}
