package de.uni_stuttgart.informatik.canu.mobisim.notifications;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels.Movement;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:  
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

 /**
  * This class implements the notification about node's arrival to destination
  * @author Illya Stepanov
  */
public class DestinationReachedNotification extends MovementNotification
{
  /**
   * Destination
   */
  protected Position3D destination;
  /**
   * Duration of staying at the destination (in s)
   */
  protected float stay;

  /**
   * Constructor. <br>
   * <br>
   * @param sender notification's sender
   * @param receiver notification's receiver
   * @param destination destination
   * @param stay stay duration at the destination (in s)
   */
  public DestinationReachedNotification(Object sender, Object receiver,
                                        Position3D destination, float stay)
  {
    super(sender, receiver);
    this.destination = destination;
    this.stay = stay;
  }

  /**
   * Returns a string representation of the object. <br>
   * <br>
   * @return string representation of the object
   */
  public String toString()
  {
    Universe u = Universe.getReference();

    Node node = (Node)((Movement)sender).getOwner();
    return "at "+u.getTimeAsString()+" "+node.getID()+
        " arrived to "+destination+" and stays there for "+stay+" s";
  }
}