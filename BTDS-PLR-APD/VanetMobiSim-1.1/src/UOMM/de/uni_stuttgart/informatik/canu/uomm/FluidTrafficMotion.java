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
import de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels.Movement;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.tripmodel.core.*;

/**
 * This class implements Fluid Traffic Movement Behavior.
 * 
 * The implementation is based on I. Seskar, S.V. Marie, J. Holtzman, J. Wasserman,
 * "Rate of Location Area Updates in Cellular Systems",
 * Proceedings of IEEE VTC'92, Denver, CO, May 1992. <br>
 * <br>
 * @author Illya Stepanov
 */
public class FluidTrafficMotion extends UserOrientedMovement
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
   * Traffic jam density (in 1/meter)
   */
  protected float jam_density = 0.0f;

  /**
   * Traffic parameters recalculation step (in steps)
   */
  protected int recalculation_step = 0; // in steps

  /**
   * Destination of previous movement
   */
  protected Position3D oldPosition;

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
  public FluidTrafficMotion()
  {
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Fluid Traffic Movement Behavior";
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
    oldPosition = pos.getPosition();
    
    if (stayRandom)
    {
      stay = tripGenerator.chooseStayDuration(node);
    }

    super.initialize();
  }

  /**
   * Gets the destination of the previous movement. <br>
   * <br>
   * @return the destination of the previous movement
   */
  public Position3D getOldPosition()
  {
    return oldPosition;
  }
  /**
   * Gets the destination of the current movement. <br>
   * <br>
   * @return the destination of the current movement
   */
  public Position3D getDestination()
  {
    return destination;
  }

  /**
   * Gets the current speed of movement. <br>
   * <br>
   * @return the current speed of movement (in meters/ms)
   */ /*
  public float getSpeed()
  {
    return speed;
  } */

  /**
   * Chooses a new movement path of movement
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
  }

  /**
   * Chooses a new destination and movement speed
   */
  protected void chooseNewMovement()
  {
    Node owner = (Node)this.owner;

    if (trip.getPath().size()==0)
      chooseNewPath();

    // get the next vertex
    Point p = (Point)trip.getPath().get(0);
    trip.getPath().remove(0);
    
    destination = p.getPosition();

    stay = 0;

    movement = owner.getPosition().getNormalizedDirectionVector(destination);
    
    recalculateTrafficSpeed();
  }

  /**
   * Chooses a time of staying at current position or continues
   * the movement to destination.
   */
  protected void chooseNewStayDuration()
  {
    Node owner = (Node)this.owner;

    oldPosition = owner.getPosition();

    if (trip.getPath().size()==0)
    {
      // wait at destination
      stay = tripGenerator.chooseStayDuration(owner);

      u.sendNotification(new DestinationReachedNotification(this, u,
        owner.getPosition(), stay/1000.0f));
    }
    else
    {
      chooseNewMovement();
    }
  }

  /**
   * Recalculates the speed of traffic flow
   */
  protected void recalculateTrafficSpeed()
  {
    SpatialModel spatialModel = (SpatialModel)u.getExtension("SpatialModel");
    Graph graph = spatialModel.getGraph();

    if (graph==null)
    {
      speed = maxSpeed;
    }
    else
    {
      float k = 0.0f;
            
      Node owner = (Node)this.owner;

      Vertex vs = graph.getVertex(oldPosition.getX(), oldPosition.getY());
      Vertex vd = graph.getVertex(destination.getX(), destination.getY());
      
      Edge currentEdge = null;
      if ((vs!=null)&&(vd!=null))
        currentEdge = spatialModel.findEdge(vs, vd);
        
      if (currentEdge!=null)
      {
        // calculate the number of nodes on the edge
        int n = 1;
        
        // find other nodes on the same edge
        java.util.Iterator iter = u.getNodes().iterator();
        while (iter.hasNext())
        {
          Node node = (Node)iter.next();
          if (owner==node)
            continue;

          Movement n_m = (Movement)node.getExtension("Movement");
          if (n_m instanceof FluidTrafficMotion)
          {
            FluidTrafficMotion n_mf = (FluidTrafficMotion)n_m;
            if (n_mf.destination!=null)
            {
              Vertex n_vs = graph.getVertex(n_mf.oldPosition.getX(), n_mf.oldPosition.getY());
              Vertex n_vd = graph.getVertex(n_mf.destination.getX(), n_mf.destination.getY());

              if ( ((vs==n_vs)&&(vd==n_vd)))
                n++;
            }
          }
        }
        
        if (n>1)
          k = (float)(n/currentEdge.getDistance());
      }
      
      speed = maxSpeed*(1-k/jam_density);
      if (speed<=0.0f)
        speed = minSpeed;
    }
    
    u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+((Node)owner).getID()+" changes speed to "+speed*1000.0f+" m/s"));    
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
    
    //if node has arrived to destination and stayed enough, a new destination
    //choosen
    if ((destination==null)||(owner.getPosition().equals(destination)))
    {
      if(movement != null)
      {
        movement = null;
        chooseNewStayDuration();
        speedChanged = true;        
      }
      else
      if(stay <= 0)
      {
        chooseNewMovement();
        speedChanged = true;
      }
      else
        stay -= u.getStepDuration();
    }

    if (movement!=null)
    {
      if ((!speedChanged) && (u.getTimeInSteps()%recalculation_step==0))
      {
        recalculateTrafficSpeed();
        speedChanged = true;
      }

      //move towards destination
      Vector3D m = movement.mult(speed*u.getStepDuration());
      if(owner.getPosition().getDistance(destination) >= m.getLength())
      {
        if (speedChanged)
        {
          // check if the next speed change event is about to occur before arriving to destination
          double dist = (recalculation_step-u.getTimeInSteps()%recalculation_step)*u.getStepDuration()*(double)speed;
          if (owner.getPosition().getDistance(destination) < dist)
          {
            // move to destination
            u.sendNotification(new MovementChangedNotification(this, u, destination, speed*1000f));
          }
          else
          {
            // move until the next speed change event occur
            u.sendNotification(new MovementChangedNotification(this, u, owner.getPosition().add(movement.mult(dist)), speed*1000f));
          }
        }
        
        owner.setPosition(owner.getPosition().add(m));
      }
      else
      {
        if (speedChanged)
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
      "Loading FluidTrafficMotion extension"));

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

    n = element.getElementsByTagName("k_jam").item(0);
    if(n==null)
      throw new Exception("<k_jam> is missing!");
    jam_density = Float.parseFloat(n.getFirstChild().getNodeValue());

    n = element.getElementsByTagName("step").item(0);
    if(n==null)
      throw new Exception("<step> is missing!");
    int i = (int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);
    if ((i<u.getStepDuration())||(i%u.getStepDuration()!=0))
      throw new Exception("Invalid <step> value: "+(float)i/1000.0f);      
    recalculation_step = i/u.getStepDuration();

    n = element.getElementsByTagName("stay").item(0);
    if (n!=null)
    {
      String randTag = ((org.w3c.dom.Element)n).getAttribute("random");
      if ((randTag.length()>0) && Boolean.valueOf(randTag).booleanValue())
        stayRandom = true;
      else
        stay=(int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);
    }

    // check
    if (minSpeed<=0)
      throw new Exception("Invalid <minspeed> value: "+(float)minSpeed*1000);
    if (maxSpeed<minSpeed)
      throw new Exception("Invalid <maxspeed> value: "+(float)maxSpeed*1000);
    if (jam_density<=0)
      throw new Exception("Invalid <k_jam> value: "+jam_density);
    if (stay<0)
      throw new Exception("Invalid <stay> value: "+(float)stay/1000);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading FluidTrafficMotion extension"));
  }
}