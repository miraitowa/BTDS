package de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * Patches:      Version 1.2 by Marco Fiore (fiore@tlc.polito.it) on 22/05/2006:
 *               Speed attribute moved at Movement class level
 *
 * @author 1.0-1.1 Canu Research group
 * @author 1.2 Marco Fiore
 * @version 1.2
 */

/**
 * This class implements the Random Waypoint Mobility Model
 * @author Illya Stepanov
 * @author Gregor Schiele
 */
public class RandomWaypointWalk extends Movement
{
  /**
   * Minimal stay duration at destination (ms)
   */
  protected int minStay = 0;        // in ms
  /**
   * Maximal stay duration at destination (ms)
   */
  protected int maxStay = 0;        // in ms
  /**
   * Current stay duration at destination (ms)
   */
  protected int stay = 0;           // in ms
  /**
   * Current speed (in meters/ms)
   */
//  protected float speed = 0.0f;    // in m/ms
  /**
   * Destination of the current movement
   */
  protected Position3D destination;
  /**
   * Current movement vector
   */
  protected Vector3D movement;

  /**
   * Constructor
   */
  public RandomWaypointWalk()
  {
  }

  /**
   * Constructor. <br>
   * <br>
   * @param node parent {@link Node Node} object
   */
  public RandomWaypointWalk(Node node)
  {
    super(node);
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Random Waypoint movement module";
  }

  /**
   * Chooses new destination and movement speed. <br>
   * <br>
   * Randomly chooses destination in source area and moving speed.
   */
  protected void chooseNewMovement()
  {
    java.util.Random rand=u.getRandom();
    Node owner=(Node)this.owner;

    double x = rand.nextDouble()*u.getDimensionX();
    double y = rand.nextDouble()*u.getDimensionY();
    double z = rand.nextDouble()*u.getDimensionZ();

    destination = new Position3D(x, y, z);

    speed=minSpeed + (maxSpeed-minSpeed)*rand.nextFloat();
    stay = 0;

    movement = owner.getPosition().getNormalizedDirectionVector(destination).
      mult(speed*u.getStepDuration());

    u.sendNotification(new MovementChangedNotification(this, u,
      destination, speed*1000.0f));
  }

  /**
   * Chooses time of staying at the destination. <br>
   * <br>
   * Randomly chooses time of staying at the destination.
   */
  protected void chooseNewStayDuration()
  {
    Node owner=(Node)this.owner;

    stay=(int)(minStay+(maxStay-minStay)*u.getRandom().nextFloat());

    u.sendNotification(new DestinationReachedNotification(this, u,
      owner.getPosition(), stay/1000.0f));
  }//proc

  /**
   * Executes the extension. <br>
   * <br>
   * The method is called on every simulation timestep.
   * @return 0 - the module should be executed on next timesteps,
   *        -1 - the module should not be executed on further timesteps and should be removed from the extensions' list
   */
  public int act()
  {
    Node owner = (Node)this.owner;

    //if node has arrived to destination and stayed enough, a new destination
    //choosen
    if ((destination==null)||(owner.getPosition().equals(destination)))
    {
      if(movement != null)
      {
        movement = null;
        chooseNewStayDuration();
      }
      else
      if(stay <= 0)
      {
        chooseNewMovement();
      }
      else
        stay -= u.getStepDuration();
    }

    if (movement!=null)
    {
      //move towards destination
      if(owner.getPosition().getDistance(destination) >= movement.getLength())
        owner.setPosition(owner.getPosition().add(movement));
      else
        owner.setPosition(destination);
    }
    
    return 0;
  }

  /**
   * Initializes the object from XML tag. <br>
   * <br>
   * @param element source tag
   * @throws Exception Exception if parameters are invalid
   */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    Node owner = (Node)this.owner;

    u.sendNotification(new LoaderNotification(this, u,
      "Loading RandomWaypointWalk extension"));

    super.load(element);

    // process child tags
    org.w3c.dom.NodeList list = element.getChildNodes();
    int len=list.getLength();

    for(int i=0; i<len; i++)
    {
      org.w3c.dom.Node item = list.item(i);
      String tag = item.getNodeName();

      if(tag.equals("#text"))
      {
        // skip it
        continue;
      }
      else
      if(tag.equals("#comment"))
      {
        // skip it
        continue;
      }
      else
      if(tag.equals("minspeed"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <minspeed> tag"));

        // read and convert minimal speed
        minSpeed=Float.parseFloat(item.getFirstChild().getNodeValue())/1000;

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <minspeed> tag"));
      }
      else
      if(tag.equals("maxspeed"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <maxspeed> tag"));

        // read and convert maximal speed
        maxSpeed=Float.parseFloat(item.getFirstChild().getNodeValue())/1000;

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <maxspeed> tag"));
      }
      else
      if(tag.equals("minstay"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <minstay> tag"));

         // read and convert minimal stay duration
         minStay=(int)(Float.parseFloat(item.getFirstChild().
           getNodeValue())*1000);

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <minstay> tag"));
      }
      else
      if(tag.equals("maxstay"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <maxstay> tag"));

         // read and convert maximal stay duration
         maxStay=(int)(Float.parseFloat(item.getFirstChild().
           getNodeValue())*1000);

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <maxstay> tag"));
      }
      else
      if(tag.equals("stay"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <stay> tag"));

         // read and set current stay
         // check for random
         org.w3c.dom.Element e = (org.w3c.dom.Element)item;
         String randTag = e.getAttribute("random");
         if ((randTag.length()>0) && Boolean.valueOf(randTag).booleanValue())
         {
           u.sendNotification(new LoaderNotification(this, u,
             "Processing 'random' attribute"));

           stay=(int) (minStay+(maxStay-minStay) * u.getRandom().nextFloat());

           u.sendNotification(new LoaderNotification(this, u,
             "Finished processing 'random' attribute"));
         }
         else
           stay=((int)(Float.parseFloat(item.getFirstChild().
             getNodeValue())*1000));

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <stay> tag"));
      }
    }

    // checkout
    if ( (minSpeed<=0)||(maxSpeed<minSpeed)||(minStay<0)||(maxStay<minStay)
         ||(stay<0) )
      throw new Exception("Movement parameters are invalid:\n"
        +"minSpeed="+minSpeed*1000+"(m/s), maxSpeed="+maxSpeed*1000
        +"(m/s), minStay="+(float)minStay/1000+"(s), maxStay="+(float)maxStay/1000
        +"(s), stay="+(float)stay+"(s)");

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading RandomWaypointWalk extension"));
  }//proc
}