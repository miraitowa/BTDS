package de.uni_stuttgart.informatik.canu.uomm;

/**
 * <p>Title: User-Oriented Mobility Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * <p>Patches: </p>
 * <p> <i> Version 1.2 by Marco Fiore (fiore@tlc.polito.it) on 22/05/2006:
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Speed attribute moved at Movement class level </i> </p>
 *
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.2 Marco Fiore
 * @version 1.2
 */

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.tripmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.senv.core.*;

/**
 * This class implements the Constant Speed Movement Behavior
 * @author Illya Stepanov
 */
public class ConstantSpeedMotion extends UserOrientedMovement
{
  /**
   * Flag to indicate that a random stay duration must be chosen at the beginning of the simulation
   */
  protected boolean stayRandom = false;
  
  /**
   * Current stay duration at destination (ms)
   */
  protected int stay = 0;           // in ms
  
  /**
   * Minimal pause duration at a street crossing (ms) 
   */
  protected int minPauseAtCrossing = 0; //in ms 

  /**
   * Maximal pause duration at a street crossing (ms) 
   */
  protected int maxPauseAtCrossing = 0; //in ms
  
  /**
   * Current pausings at crossings
   * Key: SpatialModelElement corresponding to junction
   * Value: Long representing time in ms when the pause elapses
   */
  protected static java.util.HashMap currentPausesAtCrossings = new java.util.HashMap();

  /**
   * Current speed (in meters/ms)
   */
//  protected float speed = 0.0f;    // in m/ms

  /**
   * Destination of current movement
   */
  protected Position3D destination;

  /**
   * Current movement vector
   */
  protected Vector3D movement;

  /**
   * Current trip
   */
  protected Trip trip = new Trip();

  /**
   * Constructor
   */
  public ConstantSpeedMotion()
  {
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Constant Speed Motion Movement Behavior";
  }

  /**
   * Performs the module initialization. <br>
   * <br>
   * The method is called after finishing the scenario file processing.
   */
  public void initialize()
  {
    Node node = (Node)owner;

    // set initial position
    Point pos = initialPositionGenerator.getInitialPosition(node);
    node.setPosition(pos.getPosition());
    
    if (stayRandom)
    {
      stay = tripGenerator.chooseStayDuration(node);
    }

    super.initialize();
  }

  /**
   * Chooses a new movement path
   */
  protected void chooseNewPath()
  {
    Node node = (Node)this.owner;

    trip = tripGenerator.genTrip(node);

    u.sendNotification(new DebugNotification(this, u, "New trip generated for "+node.getID()+":"));
    for (int i=0; i<trip.getPath().size(); i++)
    {
      Point p = (Point)trip.getPath().get(i);
      u.sendNotification(new DebugNotification(this, u,
        ""+p.getX()+" "+p.getY()));
    }
    
    // delete the current node position from the path
    trip.getPath().remove(0);

    speed=minSpeed + (maxSpeed-minSpeed)*u.getRandom().nextFloat();
  }

  /**
   * Chooses a new destination and movement speed
   */
  protected void chooseNewMovement()
  {
    Node owner = (Node)this.owner;

    if (trip.getPath().size()==0)
      chooseNewPath();

    Point p = (Point)trip.getPath().get(0);
    trip.getPath().remove(0);

    destination = p.getPosition();

    stay = 0;

    movement = owner.getPosition().getNormalizedDirectionVector(destination).
      mult(speed*u.getStepDuration());

    u.sendNotification(new MovementChangedNotification(this, u,
      destination, speed*1000f));
  }

  /**
   * Chooses a time of staying at current position or continues
   * the movement to destination.
   */
  protected void chooseNewStayDuration()
  {
    Node owner = (Node)this.owner;

    if (trip.getPath().size()==0)
    {
      // wait at destination
      stay = tripGenerator.chooseStayDuration(owner);

      u.sendNotification(new DestinationReachedNotification(this, u,
        owner.getPosition(), stay/1000.0f));
    }
    else
    {
      // check if a street crossing
      SpatialModel spatialModel = (SpatialModel)u.getExtension("SpatialModel");
      if (spatialModel!=null)
      {
        Vertex v = spatialModel.getGraph().getVertex(owner.getPosition().getX(), owner.getPosition().getY());
        SpatialModelElement element = spatialModel.mapVertexToJunction(v);
        if (element!=null)
        {
          // check current pause at the crossing
          Long pauseAsLong = (Long)currentPausesAtCrossings.get(element);
          if (pauseAsLong!=null)
          {
            int pause = (int)(pauseAsLong.longValue()-u.getTime());
            if (pause<0)
            {
              // the value is obsolete, choose a new one
              currentPausesAtCrossings.remove(element);
              // initiate a pause at the street crossing
              stay = (int)(minPauseAtCrossing+(maxPauseAtCrossing-minPauseAtCrossing)*u.getRandom().nextFloat());
            }
            else
              stay = pause;
          }
          else
          {
            // initiate a pause at the street crossing
            stay = (int)(minPauseAtCrossing+(maxPauseAtCrossing-minPauseAtCrossing)*u.getRandom().nextFloat());
          }
      
          if (stay!=0)
          {
            u.sendNotification(new DestinationReachedNotification(this, u,
              owner.getPosition(), stay/1000.0f));
            
            // make other arriving nodes pause too
            currentPausesAtCrossings.put(element, new Long(u.getTime()+stay));
          }
          else
          {
            // continue the movement
            chooseNewMovement();
          }
        }
        else
        {
          // continue the movement
          chooseNewMovement();
        }
      }
      else
      {
        // continue the movement
        chooseNewMovement();
      }
    }
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
    u.sendNotification(new LoaderNotification(this, u,
      "Loading ConstantSpeedMovement extension"));

    super.load(element);

    org.w3c.dom.Node n;

    n = element.getElementsByTagName("minspeed").item(0);
    if(n==null)
      throw new Exception("<minspeed> is missing!");
    minSpeed = Float.parseFloat(n.getFirstChild().getNodeValue())/1000;

    n = element.getElementsByTagName("maxspeed").item(0);
    if(n==null)
      throw new Exception("<maxspeed> is missing!");
    maxSpeed = Float.parseFloat(n.getFirstChild().getNodeValue())/1000;

    n = element.getElementsByTagName("stay").item(0);
    if (n!=null)
    {
      String randTag = ((org.w3c.dom.Element)n).getAttribute("random");
      if ((randTag.length()>0) && Boolean.valueOf(randTag).booleanValue())
        stayRandom = true;
      else
        stay=(int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);
    }

    n = element.getElementsByTagName("minpause").item(0);
    if(n!=null)
      minPauseAtCrossing = (int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);

    n = element.getElementsByTagName("maxpause").item(0);
    if(n!=null)
      maxPauseAtCrossing = (int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);
    
    // check
    if (minSpeed<=0)
      throw new Exception("Invalid <minspeed> value: "+(float)minSpeed*1000);
    if (maxSpeed<minSpeed)
      throw new Exception("Invalid <maxspeed> value: "+(float)maxSpeed*1000);
    if (stay<0)
      throw new Exception("Invalid <stay> value: "+(float)stay/1000);
    if (minPauseAtCrossing<0)
      throw new Exception("Invalid <minpause> value: "+(float)minPauseAtCrossing/1000);
    if (maxPauseAtCrossing<minPauseAtCrossing)
      throw new Exception("Invalid <maxpause> value: "+(float)maxPauseAtCrossing/1000);
    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading ConstantSpeedMovement extension"));
  }
}