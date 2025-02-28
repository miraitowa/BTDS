package de.uni_stuttgart.informatik.canu.uomm;

/**
 * <p>Title: User-Oriented Mobility Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * <p>Patches: </p>
 * <p> <i> Version 1.3 by Marco Fiore (fiore@tlc.polito.it) on 22/05/2006:
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Speed attribute moved at Movement class level </i> </p>
 *
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.2 Marco Fiore
 * @version 1.2
 */

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.tripmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;

/**
 * This class implements Smooth Motion Movement Behavior.
 * 
 * The implementation is based on C. Bettstetter, "Smooth is Better than Sharp: A Random Mobility Model for Simulation of Wireless Networks",
 * Proceedings of the 4th ACM International Workshop on Modeling, Analysis, and Simulation of Wireless and Mobile Systems (MSWiM'01),
 * Rome, Italy, July 2001. <br>
 * <br>
 * @author Illya Stepanov
 */
public class SmoothMotion extends UserOrientedMovement
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
   * Target speed (in meters/ms)
   */
  protected float targetSpeed = 0.0f;

  /**
   * Current acceleration (in meters/ms^2)
   */
  protected float a = 0.0f;

  /**
   * Minimal acceleration (in meters/ms^2)
   */
  protected float minA = 0.0f;

  /**
   * Maximal acceleration (in meters/ms^2)
   */
  protected float maxA = 0.0f;
  
  /**
   * Maximal interval between speed change (in steps)
   */
  protected long maxSpeedChangeInterval = 0;
  
  /**
   * Preferred speeds, key - Float(speed), value - Float(p)
   */
  protected java.util.Map preferredSpeeds = new java.util.HashMap();

  /**
   * Time when the next speed changing occurs (in steps)
   */
  protected long nextSpeedChangeTime = Long.MAX_VALUE;
  
  /**
   * Constructor
   */
  public SmoothMotion()
  {
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Smooth Motion Movement Behavior";
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
   * Chooses a new movement speed 
   */
  protected void chooseNewSpeed()
  {
    java.util.Random rand = u.getRandom();

    float p = rand.nextFloat();

    Float ts = null;
    java.util.Iterator iter = preferredSpeeds.keySet().iterator();
    while (iter.hasNext())
    {
      Float s = (Float)iter.next();
      float f = ((Float)preferredSpeeds.get(s)).floatValue();
      if (p<=f)
      {
        ts = s;
        break;
      }

      p-=f;
    }

    targetSpeed = (ts==null)? rand.nextFloat()*maxSpeed : ts.floatValue();
    a = (targetSpeed>speed)? (1.0f-rand.nextFloat())*maxA : (1.0f-rand.nextFloat())*minA;

    nextSpeedChangeTime = u.getTimeInSteps()+(long)((1.0f-rand.nextFloat())*maxSpeedChangeInterval);

    u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+((Node)owner).getID()+" changes speed from "+speed*1000.0f+" m/s to "+targetSpeed*1000.0f+" m/s with a="+a*1e6+" m/s^2"));
  }

  /**
   * Chooses a new movement path
   */
  protected void chooseNewPath()
  {
    Node node = (Node)this.owner;

    trip = tripGenerator.genTrip(node);

    u.sendNotification(new DebugNotification(this, u, "New trip generated:"));
    for (int i=0; i<trip.getPath().size(); i++)
    {
      Point p = (Point)trip.getPath().get(i);
      u.sendNotification(new DebugNotification(this, u,
        ""+p.getX()+" "+p.getY()));
    }
    
    // delete the current node position from the path
    trip.getPath().remove(0);

    chooseNewSpeed();
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

    movement = owner.getPosition().getNormalizedDirectionVector(destination);
  }

  /**
   * Chooses a time of staying at the current position or continues
   * movement to destination.
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
        
      speed = 0.0f;
    }
    else
    {
      chooseNewMovement();
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

    boolean speedChanged = false;
    boolean targetSpeedReached = false;
    boolean checkPointReached = false;

    //if node has arrived to destination and stayed enough, a new destination
    //choosen
    if ((destination==null)||(owner.getPosition().equals(destination)))
    {
      if(movement != null)
      {
        movement = null;
        chooseNewStayDuration();
        checkPointReached = true;
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
      if (a!=0)
      {
        if (Math.abs(targetSpeed-speed)>Math.abs(a*u.getStepDuration()))
        {
          speed+=a*u.getStepDuration();
          speedChanged = true;
        }
        else
        {
          speed = targetSpeed;
          a = 0.0f;
          targetSpeedReached = true;
        }
      }

      if (u.getTimeInSteps()>=nextSpeedChangeTime)
      {
        chooseNewSpeed();
        speedChanged = true;
      }

      //move towards destination
      Vector3D m = movement.mult(speed*u.getStepDuration());
      if(owner.getPosition().getDistance(destination) >= m.getLength())
      {
        Position3D d = owner.getPosition().add(m);

        if (speedChanged)
          u.sendNotification(new MovementChangedNotification(this, u, d, speed*1000f));
        else
        if (targetSpeedReached || checkPointReached)
        {
          // check if speed change event is about to occur before arriving to destination
          double dist = (nextSpeedChangeTime-u.getTimeInSteps()-1)*m.getLength();
          if (d.getDistance(destination) < dist)
          {
            // move to destination
            u.sendNotification(new MovementChangedNotification(this, u, destination, speed*1000f));
          }
          else
          {
            // move until the next speed change event occur
            u.sendNotification(new MovementChangedNotification(this, u, d.add(movement.mult(dist)), speed*1000f));
          }
        }
        
        owner.setPosition(d);
      }
      else
      {
        if (speedChanged || targetSpeedReached || checkPointReached)
          u.sendNotification(new MovementChangedNotification(this, u, destination, speed*1000f));

        owner.setPosition(destination);
      }
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
      "Loading SmoothMotion extension"));

    super.load(element);

    org.w3c.dom.Node n;

    n = element.getElementsByTagName("maxspeed").item(0);
    if(n==null)
      throw new Exception("<maxspeed> is missing!");
    maxSpeed = Float.parseFloat(n.getFirstChild().getNodeValue())/1000;

    n = element.getElementsByTagName("minacc").item(0);
    if(n==null)
      throw new Exception("<minacc> is missing!");
    minA = Float.parseFloat(n.getFirstChild().getNodeValue())/1e6f;

    n = element.getElementsByTagName("maxacc").item(0);
    if(n==null)
      throw new Exception("<maxacc> is missing!");
    maxA = Float.parseFloat(n.getFirstChild().getNodeValue())/1e6f;

    n = element.getElementsByTagName("speedchangeinterval").item(0);
    if(n==null)
      throw new Exception("<speedchangeinterval> is missing!");
    maxSpeedChangeInterval = (int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);
    if ((maxSpeedChangeInterval<u.getStepDuration())||(maxSpeedChangeInterval%u.getStepDuration()!=0))
      throw new Exception("Invalid <speedchangeinterval> value: "+(float)maxSpeedChangeInterval/1000.0f);      
    maxSpeedChangeInterval = maxSpeedChangeInterval / u.getStepDuration();

    n = element.getElementsByTagName("stay").item(0);
    if (n!=null)
    {
      String randTag = ((org.w3c.dom.Element)n).getAttribute("random");
      if ((randTag.length()>0) && Boolean.valueOf(randTag).booleanValue())
        stayRandom = true;
      else
        stay=(int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);
    }

    float cum_p = 0.0f;
    n = element.getElementsByTagName("speeds").item(0);
    if (n!=null)
    {
      org.w3c.dom.NodeList v_list = ((org.w3c.dom.Element)n).getElementsByTagName("v");
      org.w3c.dom.NodeList p_list = ((org.w3c.dom.Element)n).getElementsByTagName("p");

      for (int i=0; i<v_list.getLength(); i++)
      {
        n = v_list.item(i);
        float v = Float.parseFloat(n.getFirstChild().getNodeValue())/1000;
        if (v<0)
          throw new Exception("Invalid <v> value: "+v*1000);
        
        n = p_list.item(i);
        float p = Float.parseFloat(n.getFirstChild().getNodeValue());
        
        cum_p+=p;

        preferredSpeeds.put(new Float(v), new Float(p));
      }
    }

    if (cum_p>1.0f)
      throw new Exception("Invalid speeds' probability!");

    // check
    if (minA>=0)
      throw new Exception("Invalid <minacc> value: "+(float)minA*1e6f);
    if (maxA<=0)
      throw new Exception("Invalid <maxacc> value: "+(float)maxA*1e6f);
    if (maxSpeed<minSpeed)
      throw new Exception("Invalid <maxspeed> value: "+(float)maxSpeed*1000);
    if (stay<0)
      throw new Exception("Invalid <stay> value: "+(float)stay/1000);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading SmoothMotion extension"));
  }
}