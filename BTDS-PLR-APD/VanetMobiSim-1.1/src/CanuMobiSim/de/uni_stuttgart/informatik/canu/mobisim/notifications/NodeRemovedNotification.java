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
 * This class implements the notification about node's removal from simulation
 * @author Illya Stepanov
 */
public class NodeRemovedNotification extends Notification
{
  /**
   * Node removed from simulation
   */
   protected Node node;

  /**
   * Constructor. <br>
   * <br>
   * @param sender notification's sender
   * @param receiver notification's receiver
   * @param node node removed from the simulation
   */
  public NodeRemovedNotification(Object sender, Object receiver, Node node)
  {
    super(sender, receiver);
    this.node = node;
  }

  /**
   * Returns the node. <br>
   * <br>
   * @return node
   */
  public Node getNode()
  {
    return node;
  }

  /**
   * Returns a string representation of the object. <br>
   * <br>
   * @return string representation of the object
   */
  public String toString()
  {
    Universe u = Universe.getReference();

    return "at "+u.getTimeAsString()+" "+node.getID()+
        " was removed from the simulation";
  }
}