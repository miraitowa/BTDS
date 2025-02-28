package de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels;

import java.util.ArrayList;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2004
 * Company:      University of Stuttgart
 * Patches:      Version 1.2 by Marco Fiore (fiore@tlc.polito.it) on 22/05/2006:
 *               Speed attribute moved at Movement class level
 *
 * @author version 1.0-1.2 Canu Research group
 * @author version 1.3 Marco Fiore
 * @version 1.3
 */

/**
 * This class implements the Random Waypoint Mobility Model
 * with deviations
 * @author Illya Stepanov
 */
public class DeviatedRandomWaypointWalk extends RandomWaypointWalk
{
  /**
   * Name of a notification listener for the non-deviated movement
   */
  protected String altListenerName;
  /**
   * Notification listener for the non-deviated movement
   */
  protected NotificationListener altListener;
  /**
   * Maximum deviation of a position (m)
   */
  protected float eps = 0.0f;   // in m
  /**
   * Array of intermediate points of a movement
   */
  protected ArrayList intermediatePositions;
  /**
   * Number of intermediate points of a movement
   */
  protected int n_points = 5; 
  /**
   * Time of arrival to the destination (in ms)
   */
  protected long arrivalTime; // in ms

  /**
   * Constructor
   */
  public DeviatedRandomWaypointWalk()
  {
    intermediatePositions = new ArrayList();
  }

  /**
   * Constructor. <br>
   * <br>
   * @param node parent {@link Node Node} object
   */
  public DeviatedRandomWaypointWalk(Node node)
  {
    super(node);
    intermediatePositions = new ArrayList();
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Deviated Random Waypoint movement module";
  }

  /**
   * Performs the modules' initialization
   */
  public void initialize()
  {
    super.initialize();
    
    if (altListenerName!=null)
      altListener = u.getExtension(altListenerName);
  }

  /**
   * Chooses a new destination point and calculates the non-deviated movement path.
   */
  public void chooseNewPath()
  {
    java.util.Random rand=u.getRandom();
    Node owner=(Node)this.owner;

    double x = rand.nextDouble()*u.getDimensionX();
    double y = rand.nextDouble()*u.getDimensionY();

    destination = new Position3D(x, y, 0);
    speed = minSpeed + (maxSpeed-minSpeed)*rand.nextFloat();

    double dist = owner.getPosition().getDistance(destination);
    arrivalTime = u.getTime()+(long)Math.floor(dist/speed);
    
    movement = owner.getPosition().getNormalizedDirectionVector(destination).mult(dist/n_points);

    // calculate movement intermediate positions
    Position3D oldPos = owner.getPosition();
    while (oldPos.getDistance(destination)>movement.getLength())
    {
      oldPos = oldPos.add(movement);
      intermediatePositions.add(oldPos);
    };
    
    intermediatePositions.add(destination);

    // inform about the non-deviated path
    u.sendNotification(new DebugNotification(this, u,
      owner.getID()+" moves to "+destination+" with speed "+speed*1000f+" m/s"));

    if (altListener!=null)
      altListener.sendNotification(new MovementChangedNotification(this, altListener,
        destination, speed*1000f));
  }

  /**
   * Chooses a deviated point of movement.
   */
  public void chooseNewMovement()
  {
    java.util.Random rand=u.getRandom();
    Node owner = (Node)this.owner;

    if (intermediatePositions.size()==0)
      chooseNewPath();

    destination=(Position3D)intermediatePositions.get(0);
    intermediatePositions.remove(0);

    if (intermediatePositions.size()!=0)
    {
      // calculate deviations
      double dx = rand.nextFloat();
      if (rand.nextBoolean())
        dx = 1-dx;
      else
        dx = dx-1;
      dx*=eps/Math.sqrt(2);
  
      double dy = rand.nextFloat();
      if (rand.nextBoolean())
        dy = 1-dy;
      else
        dy = dy-1;
      dy*=eps/Math.sqrt(2);
  
      // deviate the destination
      double x = destination.getX()+dx;
      double y = destination.getY()+dy;
      
      // check if within the simulation area
      if (x<0)
        x=0;
      else
      if (x>u.getDimensionX())
        x = u.getDimensionX();
  
      if (y<0)
        y=0;
      else
      if (y>u.getDimensionY())
        y = u.getDimensionY();
      
      destination = new Position3D(x, y, 0);
    }

    speed = (float)owner.getPosition().getDistance(destination)/((arrivalTime-u.getTime())/(intermediatePositions.size()+1));
    
    stay = 0;

    movement = owner.getPosition().getNormalizedDirectionVector(destination).
      mult(speed*u.getStepDuration());

    u.sendNotification(new MovementChangedNotification(this, u,
      destination, speed*1000f));
  }

  /**
   * Chooses a time of staying at the destination. <br>
   * <br>
   * Randomly chooses time of staying at the destination.
   */
  protected void chooseNewStayDuration()
  {
    if (intermediatePositions.size()==0)
    {
      // wait at the destination
      Node owner=(Node)this.owner;

      stay=(int)(minStay+(maxStay-minStay)*u.getRandom().nextFloat());

      u.sendNotification(new DestinationReachedNotification(this, u,
        owner.getPosition(), stay/1000.0f));
    }
    else
    {
      chooseNewMovement();
    }
  }//proc

  /**
   * Executes the extension.
   */
  public int act()
  {
    if (u.getTimeInSteps()==0)
    {
      // remove the alternative listener from the list, we'll keep him informed directly
      if (altListener!=null)
        u.removeNotificationListener(altListener);
    }

    return super.act();
  }

  /**
   * Initializes the object from XML tag. <br>
   * <br>
   * @param element source tag
   * @throws Exception Exception if parameters are invalid
   */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading DeviatedRandomWaypointWalk extension"));

    super.load(element);

    // process child tags
    org.w3c.dom.NodeList list = element.getChildNodes();
    int len=list.getLength();

    for(int i=0; i<len; i++)
    {
      org.w3c.dom.Node item = list.item(i);
      String tag = item.getNodeName();

      if(tag.equals("alt_listener"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <alt_listener> tag"));

        // read a name of the alternative listener 
        altListenerName=item.getFirstChild().getNodeValue();

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <alt_listener> tag"));
      }
      else
      if(tag.equals("eps"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <eps> tag"));

        // read eps
        eps=Float.parseFloat(item.getFirstChild().getNodeValue());

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <eps> tag"));
      }
      else
      if(tag.equals("n_points"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <n_points> tag"));

         // read and convert a number of intermediate points
        n_points=Integer.parseInt(item.getFirstChild().getNodeValue());

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <n_points> tag"));
      }
    }

    // checkout
    if (eps<0)
      throw new Exception("Invalid <eps> value: "+eps);
    if (n_points<1)
      throw new Exception("Invalid <n_points> value: "+n_points);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading DeviatedRandomWaypointWalk extension"));
  }//proc
}