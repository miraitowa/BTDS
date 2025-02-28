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
  * This class implements the notification about changing of movement parameters
  * @author Illya Stepanov
  */
public class MovementChangedNotification extends MovementNotification
{
  /**
   * Destination of movement
   */
  protected Position3D destination;
  /**
   * Speed of movement (in m/s)
   */
  protected float speed;

  /**
   * Constructor. <br>
   * <br>
   * @param sender notification's sender
   * @param receiver notification's receiver
   * @param destination destination
   * @param speed movement speed (in m/s)
   */
  public MovementChangedNotification(Object sender, Object receiver,
                                     Position3D destination, float speed)
  {
    super(sender, receiver);
    this.destination = destination;
    this.speed = speed;
  }

  /**
   * Gets the movement speed. <br>
   * <br>
   * @return movement speed
   */
  public float getSpeed()
  {
    return speed;
  }

  /**
   * Gets the destination of movement. <br>
   * <br>
   * @return destination of movement
   */
  public Position3D getDestination()
  {
    return destination;
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
    
    float t = (float)node.getPosition().getDistance(destination)/speed;
     
    return "at "+u.getTimeAsString()+" "+node.getID()+
        " started new movement from "+node.getPosition()+
        " to "+destination+" with speed "+speed+" m/s, estimated arrival in "+t+" s";
  }
}