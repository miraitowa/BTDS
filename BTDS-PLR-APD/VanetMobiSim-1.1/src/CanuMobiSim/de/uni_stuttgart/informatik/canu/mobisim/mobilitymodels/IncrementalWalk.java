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
 * This class implements the Incremental Walk Mobility Model. <br>
 * @author Illya Stepanov
 */
public class IncrementalWalk extends Movement
{
  /**
   * Constant
   */
  protected final float pi2 = 2.0f*(float)Math.PI;
  /**
   * Minimal direction change angle (in radians)
   */
  protected float minAngle = 0.0f; // in radians
  /**
   * Maximal direction change angle (in radians)
   */
  protected float maxAngle = pi2;  // in radians
  /**
   * Current direction (in radians)
   */
  protected float angle = 0.0f;    // in radians
  /**
   * Current speed (in meters/ms)
   */
//  protected float speed = 0.0f;    // in m/ms
  /**
   * Minimal acceleration value (in m/ms^2)
   */
  protected float minAcc = 0.0f; // in m/ms^2
  /**
   * Maximal acceleration value (in m/ms^2)
   */
  protected float maxAcc = 0.0f;  // in m/ms^2
  /**
   * Current movement vector
   */
  protected Vector3D movement;

  /**
   * Constructor
   */
  public IncrementalWalk()
  {
  }

  /**
   * Constructor. <br>
   * <br>
   * @param node parent {@link Node Node} object
   */
  public IncrementalWalk(Node node)
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
    return "Incremental Walk movement module";
  }

  /**
   * Chooses new movement
   */
  protected void chooseNewMovement()
  {
    java.util.Random rand=u.getRandom();
    Node owner=(Node)this.owner;

    Position3D destination = null;
    // choose new motion parameters unless the movement takes place
    // in the simulation area
    do
    {
      speed = Math.min(Math.max(speed + minAcc +
        (maxAcc-minAcc)*rand.nextFloat(), minSpeed), maxSpeed);
      angle = angle + minAngle + (maxAngle-minAngle)*rand.nextFloat();

      movement = new Vector3D((double)speed*u.getStepDuration()*Math.cos(angle),
                              (double)speed*u.getStepDuration()*Math.sin(angle),
                              0.0);

      destination = owner.getPosition().add(movement);
    }
    while ( (destination.getX()<0.0f)||(destination.getX()>u.getDimensionX())
           ||(destination.getY()<0.0f)||(destination.getY()>u.getDimensionY()) );

    u.sendNotification(new MovementChangedNotification(this, u,
      destination, speed*1000.0f));
  }

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

    chooseNewMovement();

    owner.setPosition(owner.getPosition().add(movement));
    
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
    u.sendNotification(new LoaderNotification(this, u,
      "Loading IncrementalWalk extension"));

    super.load(element);

    Node owner = (Node)this.owner;

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
      if(tag.equals("minangle"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <minangle> tag"));

        // read and convert minimal direction change
        minAngle=Float.parseFloat(item.getFirstChild().getNodeValue());

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <minangle> tag"));
      }
      else
      if(tag.equals("maxangle"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <maxangle> tag"));

        // read and convert maximal direction change
        maxAngle=Float.parseFloat(item.getFirstChild().getNodeValue());

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <maxangle> tag"));
      }
      else
      if(tag.equals("minacc"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <minacc> tag"));

        // read and convert minimal direction change
        minAcc=Float.parseFloat(item.getFirstChild().getNodeValue())/1e6f;

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <minacc> tag"));
      }
      else
      if(tag.equals("maxacc"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <maxacc> tag"));

        // read and convert maximal direction change
        maxAcc=Float.parseFloat(item.getFirstChild().getNodeValue())/1e6f;

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <maxancc> tag"));
      }
    }

    // checkout
    if ( (minSpeed<=0)||(maxSpeed<minSpeed)||(minAngle>maxAngle)
         ||(minAcc>maxAcc) )
      throw new Exception("Movement parameters are invalid:\n"
        +"minSpeed="+minSpeed*1000+"(m/s), maxSpeed="+maxSpeed*1000
        +"(m/s), minAngle="+minAngle+"(rad), maxAngle="+maxAngle
        +"(rad), minAcc="+minAcc*1e6f+"(m/s^2), maxAcc="+maxAcc*1e6f+"(m/s^2)");

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading IncrementalWalk extension"));
  }//proc
}