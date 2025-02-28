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
 * @author version 1.0-1.1 Canu Research group
 * @author version 1.2 Marco Fiore
 * @version 1.2
 */

/**
 * This class implements the Gauss-Markov Walk Mobility Model
 * @author Illya Stepanov
 */
public class GaussMarkovWalk extends Movement
{
  /**
   * Tuning parameter
   */
  protected float alpha = 0.0f;
  /**
   * Current movement vector
   */
  protected Vector3D movement;
  /**
   * Time while the movement is kept constant
   */
  protected long step = 0;

  /**
   * Constructor
   */
  public GaussMarkovWalk()
  {
    movement = new Vector3D(0, 0, 0);
  }

  /**
   * Constructor. <br>
   * <br>
   * @param node parent {@link Node Node} object
   */
  public GaussMarkovWalk(Node node)
  {
    super(node);
    movement = new Vector3D(0, 0, 0);
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Gauss-Markov Walk movement module";
  }

  /**
   * Chooses new movement
   */
  protected void chooseNewMovement()
  {
    java.util.Random rand=u.getRandom();
    Node owner=(Node)this.owner;

    Position3D destination = null;
    Vector3D newMovement   = null;
    Vector3D wholeMovement = null;

    // choose new motion parameters unless the movement takes place
    // in the simulation area
    do
    {
      newMovement = movement.mult(alpha).add(
        (new Vector3D(rand.nextGaussian(), rand.nextGaussian(), 0.0)).mult(
          Math.sqrt(1-alpha*alpha)));

      wholeMovement = newMovement.mult(step/u.getStepDuration());

      // calculate destination
      destination = owner.getPosition().add(wholeMovement);
    }
    while ( (destination.getX()<0.0f)||(destination.getX()>u.getDimensionX())
           ||(destination.getY()<0.0f)||(destination.getY()>u.getDimensionY()) );

    movement = newMovement;

    speed = (float)wholeMovement.getLength()/step;

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

    if (u.getTime() % step == 0 )
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
    Node owner = (Node)this.owner;

    u.sendNotification(new LoaderNotification(this, u,
      "Loading GaussMarkovWalk extension"));

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
      if(tag.equals("alpha"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <alpha> tag"));

        // read and convert alpha value
        alpha=Float.parseFloat(item.getFirstChild().getNodeValue());

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <alpha> tag"));
      }
      else
      if(tag.equals("step"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <step> tag"));

        // read and convert maximal speed
        step=(long)(Float.parseFloat(item.getFirstChild().getNodeValue())*1000);

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <step> tag"));
      }
    }

    // checkout
    if ( (alpha<0)||(alpha>1)||(step<=0) )
      throw new Exception("Movement parameters are invalid:\n"
        +"alpha="+alpha+", step="+(float)step/1000);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading GaussMarkovWalk extension"));
  }//proc
}