package de.uni_stuttgart.informatik.canu.uomm;

/**
 * <p>Title: User-Oriented Mobility Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * <p>Patches: </p>
 * <p> <i> Version 1.2 by Marco Fiore (fiore@tlc.polito.it) on 06/12/2005:
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Added support for multidirectional and multiflow roads </i> </p>
 * <p> <i> Version 1.3 by Marco Fiore (fiore@tlc.polito.it) on 22/05/2006:
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Speed attribute moved at Movement class level </i> </p>
 * <p> <i> Version 1.4 by Marco Fiore (fiore@tlc.polito.it) and Jerome Haerri (haerri@eurecom.fr) on 20/09/2006:
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Multi-segment roads and speed limits support </i> </p>
 *
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.2-1.3 Marco Fiore
 * @author 1.4 Marco Fiore, Jerome Haerri
 * @version 1.4
 */

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels.Movement;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.tripmodel.core.*;
import polito.uomm.*;

/**
 * This class implements Intelligent Driver Motion Behavior.
 * 
 * The implementation is based on M. Treiber and D. Helbing,
 * "Explanation of Observed Features of Self-Organization in Traffic Flow",
 * Preprint cond-mat/9901239 (1999). <br>
 * <br> 
 * <p>Patches: </p>
 * <p> <i> Version 1.2 by Marco Fiore (fiore@tlc.polito.it) on 06/12/2005:
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Added support for multidirectional and multiflow roads </i> </p>
 * <p> <i> Version 1.3 by Marco Fiore (fiore@tlc.polito.it) on 22/05/2006:
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Speed attribute moved at Movement class level </i> </p>
 * <p> <i> Version 1.4 by Marco Fiore (fiore@tlc.polito.it) and Jerome Haerri (haerri@eurecom.fr) on 20/09/2006:
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Multi-segment roads and speed limits support </i> </p>
 *
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.2-1.3 Marco Fiore
 * @author 1.4 Marco Fiore and Jerome Haerri
 * @version 1.4
 */
public class IntelligentDriverMotion extends UserOrientedMovement
{
  /**
   * Flag to indicate that a random stay duration must be chosen at the beginning of the simulation
   */
  protected boolean stayRandom = false;
  
  /**
   * Current stay duration at destination (ms)
   */
  protected int stay = 0;                 // in ms

  /**
   * Desired speed (in meters/ms)
   */
  protected float desiredSpeed = 0.0f;    // in m/ms

  /**
   * Movement recalculation step (in steps)
   */
  protected int recalculation_step = 0;   // in steps

  /**
   * Vehicle length (in meters)
   */
  protected float vehicleLength = 5.0f;
  
  /**
   * Maximum vehicle acceleration (in meters/ms^2) 
   */
  protected float a = 0.6e-6f;            // in m/ms^2
  
  /**
   * Comfortable vehicle deceleration (in meters/ms^2)
   */
  protected float b = 0.9e-6f;            // in m/ms^2
  
  /**
   * Minimum "jam" distance to a standing vehicle (in meters)
   */
  protected float s0 = 2.0f;              // in m
  
  /**
   * Safe time headway (in ms)
   */
  protected float t = 1.5e3f;             // in ms

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
   * Allowed speed (in meters/ms)
   */
  protected float allowedSpeed = 80e-3f;    // in m/ms, around 300 km/h

  /**
   * Current road edge (NULL if node has no movement)
   */
  protected Edge currentRoad = null;

  /**
   * Next ahead junction (NULL if trip ends before a junction)
   */
  protected Point nextIntersection = null;

  /**
   * Distance to next ahead junction or end of the trip
   */
  protected float distanceToIntersection = 0.0f;


  /**
   * Constructor
   */
  public IntelligentDriverMotion()
  {
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Intelligent Driver Movement Behavior";
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
   * Gets the current road edge. <br>
   * <br>
   * @return the current road edge
   */
  public Edge getCurrentRoad()
  {
    return currentRoad;
  }

  /**
   * Gets the next intersection (NULL if trip ends before an intersection). <br>
   * <br>
   * @return the next intersection (NULL if trip ends before an intersection)
   */
  public Point getNextIntersection()
  {
    return nextIntersection;
  }

  /**
   * Gets the distance to next intersection or end of trip. <br>
   * <br>
   * @return the distance to next intersection or end of trip
   */
  public float getDistanceToIntersection()
  {
    return distanceToIntersection;
  }

  /**
   * Retrieves the next junction. <br>
   * <br>
   * @return the next junction, NULL if movement ends before next junction
   */
  public Point findNextIntersection() {
    Graph graph = null;
    Point intersectionPoint = null;

    if (spatialModel != null) {
      graph = spatialModel.getGraph();
    }

    if (graph != null) {
      // check if current destination corresponds to an intersection
      Vertex vertexDest = graph.getVertex(destination.getX(), destination.getY());
      SpatialModelElement elementDest = spatialModel.mapVertexToJunction(vertexDest);
      if (elementDest != null) {
        intersectionPoint = (Point)elementDest.getGeometry();
        return intersectionPoint;
      }

      // go through trip, looking for a match with a vertex
      for (int i=0; i<trip.getPath().size(); i++) {
        intersectionPoint = (Point)trip.getPath().get(i);		

        Vertex vertex = graph.getVertex(intersectionPoint.getX(), intersectionPoint.getY());
        SpatialModelElement element = spatialModel.mapVertexToJunction(vertex);
        if (element != null) {
          return intersectionPoint;
        }
      }
    }

    return intersectionPoint;
  }
	
  /**
   * Computes the distance to next junction. <br>
   * <br>
   * @return the distance to next junction
   */
  public float findDistanceToIntersection() {
    Graph graph = null;
		
    if (spatialModel != null) {
      graph = spatialModel.getGraph();
    }
		
    float distance = 0.0f;
    
    if (graph != null) {
      Node owner = (Node)this.owner;

      // init distance with current destination
      distance = (float)owner.getPosition().getDistance(destination);
			
      // check if current destination corresponds to an intersection
      Vertex vertexDest = graph.getVertex(destination.getX(), destination.getY());
      SpatialModelElement elementDest = spatialModel.mapVertexToJunction(vertexDest);
      if (elementDest != null) {
        return distance;
      }

      // update distance through trip, until next junction is reached
      Point prevPoint = new Point(destination.getX(),destination.getY());
      Point nextPoint = null;
      for (int i=0; i<trip.getPath().size(); i++) {
        nextPoint = (Point)trip.getPath().get(i);
        Vertex vertex = graph.getVertex(nextPoint.getX(), nextPoint.getY());
        SpatialModelElement element = spatialModel.mapVertexToJunction(vertex);
        distance += prevPoint.getDistance(nextPoint);
        prevPoint = nextPoint;
        if (element != null) {
          return distance;
        }
      }	
    }

    return distance;
  }

  /**
   * Checks if nodes are on the same road. <br>
   * <br>
   * @param node1 the first Node
   * @param node2 the second Node
   * @return true if nodes are on the same road, false otherwise
   */
  protected boolean onSameRoad (Node node1, Node node2) {
    Graph graph = null;

    if (spatialModel !=null) {
      graph = spatialModel.getGraph(); 
    }

    boolean onSameRoad = false;
    
    if (graph != null) {
      // find node1 edge
      IntelligentDriverMotion mov1 = (IntelligentDriverMotion) node1.getExtension("Movement");
      Edge edge1 = mov1.getCurrentRoad();
    
      // find node2 edge
      IntelligentDriverMotion mov2 = (IntelligentDriverMotion) node2.getExtension("Movement");
      Edge edge2 = mov2.getCurrentRoad();
      
      if (edge1 != null && edge2 != null) {
        SpatialModelElement roadElement1 = spatialModel.mapEdgeToElement(edge1);
        SpatialModelElement roadElement2 = spatialModel.mapEdgeToElement(edge2);
        
        // if a road topology is being used, check that nodes are
        // on the same road and direction
        if (roadElement1 != null && roadElement2 != null) {
          if (roadElement1.getID() == roadElement2.getID()) {
            Point dest1 = mov1.getNextIntersection();
            Point dest2 = mov2.getNextIntersection();
            if (dest1 != null && dest2 != null && dest1.contains(dest2)) {
              onSameRoad = true;
            }
          }
        }
        // if a graph is being used, check that
        // source and destination vertices match
        else {
          Vertex vs1 = graph.getVertex(mov1.getOldPosition().getX(), mov1.getOldPosition().getY());
          Vertex vd1 = graph.getVertex(mov1.getDestination().getX(), mov1.getDestination().getY());
          Vertex vs2 = graph.getVertex(mov2.getOldPosition().getX(), mov2.getOldPosition().getY());
          Vertex vd2 = graph.getVertex(mov2.getDestination().getX(), mov2.getDestination().getY());  
          if (vs1 == vs2 && vd1 == vd2)
            onSameRoad = true;
        }
      }
    }
    
    return onSameRoad;
  }

  /**
   * Chooses a new movement path
   */
  protected void chooseNewPath()
  {
    java.util.Random rand = u.getRandom();
    
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

    desiredSpeed = (minSpeed + (maxSpeed-minSpeed)*rand.nextFloat());

    // here the lane is reset so that a new lane is
    // randomly chosen at the beginning of a new path
    node.setLane(0);
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

    // store current road element
    Graph graph = null;
    if (spatialModel !=null) {
      graph = spatialModel.getGraph();
    }

    currentRoad = null;
    Vertex vs = graph.getVertex(getOldPosition().getX(), getOldPosition().getY());
    Vertex vd = graph.getVertex(getDestination().getX(), getDestination().getY());
    if (vs != null && vd != null)
      currentRoad = spatialModel.findEdge(vs,vd);

    // store next road junction, if the previous
    // intersection doesn't exist or has been reached
    if (nextIntersection == null ||
        (nextIntersection != null && oldPosition.equals(nextIntersection.getPosition()))) {
      nextIntersection = findNextIntersection();
      distanceToIntersection = findDistanceToIntersection();
    }

    // look for a new maximum allowed speed, if any,
    // and for the number of available lanes
    allowedSpeed = 80e-3f;

    if (graph!=null) {
      Vertex vertex_i = graph.getVertex(oldPosition.getX(), oldPosition.getY());
      Vertex vertex_a = graph.getVertex(destination.getX(), destination.getY());

      if((vertex_i != null) && (vertex_a != null)) {
        // find current road
        Edge edge_a = spatialModel.findEdge(vertex_i, vertex_a);
	SpatialModelElement road_a = spatialModel.mapEdgeToElement(edge_a);

	if(road_a != null) {
          // get speed limit on current road, if any
          String segmentAttribute = (String)road_a.getAttributes().get("SP");
          if (segmentAttribute != null) {
            allowedSpeed = Float.parseFloat(segmentAttribute)/1000.0f;
            u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                     ((Node)owner).getID()+" new maximum"+
                                                     " allowed speed "+allowedSpeed*1000.0f));
          }

          // get number of lanes on current road
          String lanesAttribute = (String)road_a.getAttributes().get("NL");
          if (lanesAttribute != null) {
            int currNumLanes = Integer.parseInt(lanesAttribute);

            // if the current lane does not exist in this road,
            // move to a random new lane among these available
            if(owner.getLane() == 0 || owner.getLane() > currNumLanes) {
              java.util.Random rand = u.getRandom();
              owner.setLane(rand.nextInt(currNumLanes)+1);
              u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                       ((Node)owner).getID()+" moving to lane "+
                                                       owner.getLane()));

            }
            // otherwise, keep moving on the same lane
            else {
              u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                       ((Node)owner).getID()+" staying in lane "+
                                                       owner.getLane()));
            }
          }
        }
      }
    }

    recalculateSpeed();
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
        
      speed = 0.0f;        
    }
    else
    {
      chooseNewMovement();
    }
  }

  /**
   * Recalculates the vehicle's speed
   */
  protected void recalculateSpeed()
  {
    Graph graph = null;
	
    if (spatialModel !=null) {
      graph = spatialModel.getGraph(); 
    }

    if (graph!=null)
    {
      Node owner = (Node)this.owner;

      Vertex vs = graph.getVertex(oldPosition.getX(), oldPosition.getY());
      Vertex vd = graph.getVertex(destination.getX(), destination.getY());
      
      Edge currentEdge = null;
      if ((vs!=null)&&(vd!=null))
        currentEdge = spatialModel.findEdge(vs, vd);
        
      if (currentEdge!=null)
      {
        IntelligentDriverMotion n1_mf = null;
        float d_speed1 = 0.0f;
        float d_1 = Float.MAX_VALUE;

        float d_speed2 = 0.0f;
        float d_2 = Float.MAX_VALUE;

        // this vechile's distance from next intersection
        float thisDistance = getDistanceToIntersection();

        // find two closest cars in front
        java.util.Iterator iter = u.getNodes().iterator();
        while (iter.hasNext())
        {
          Node node = (Node)iter.next();
          if (owner==node)
            continue;

          Movement n_m = (Movement)node.getExtension("Movement");
          if (!(n_m instanceof IntelligentDriverMotion))
            continue;
          
          IntelligentDriverMotion n_mf = (IntelligentDriverMotion)n_m;
          // ignore paused cars
          if ((n_mf.destination==null)||(n_mf.stay!=0))
            continue;

          // check if on the same road
          if(!onSameRoad(owner, node))
            continue;

          // check if on the same lane
          if(owner.getLane() != node.getLane())
            continue;

          // current vehicle's distance from intersection
          float nodeDistance = n_mf.getDistanceToIntersection();

          // check if in front
          if (thisDistance >= nodeDistance)
          {
            float d = thisDistance - nodeDistance - n_mf.vehicleLength;
            // check if closer than at jam distance
            if (d<s0)
            {
              if (speed==0)
              {
                // wait till nodes with lower IDs depart
                //if (owner.getID().compareTo(node.getID())>=0)
                if (Integer.parseInt(owner.getID().substring(1)) >= Integer.parseInt(node.getID().substring(1)))
                {
                  //speed = Float.NaN;
                  speed = 0.0f;
                  u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                   ((Node)owner).getID()+" found identical node "+
                                                   ((Node)node).getID()));
                  return;
                }
                else {
                  // if distance is less than zero, it means that the nodes
                  // are overlapped at some entry/exit point. Ignore the node
                  if (d <= 0)
                    continue;
                  // if the distance is less than than the safety, but higher
                  // than zero, it means that the current car stopped within
                  // safety distance from the ahead vehicle, due to the finite
                  // precision of the model. The node has to be considered in
                  // the front node discovery process
                  else
                    u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                   ((Node)owner).getID()+" found node within safety distance "+
                                                   ((Node)node).getID()));
                }
              }
            }
            
            if (d<d_1)
            {
              d_2 = d_1;
              d_speed2 = d_speed1;

              d_1 = d;
              d_speed1 = speed - n_mf.speed;
              n1_mf = n_mf;
            }
            else
            if (d<d_2)
            {
              d_2 = d;
              d_speed2 = speed - n_mf.speed;
            }
          }
        }

        float ss = (float)(s0 + Math.max(speed*t+speed*d_speed1/(2.0*Math.sqrt(a*b)), 0));
        float dv = (float)(a*(1-Math.pow(speed/desiredSpeed, 4)-Math.pow(ss/d_1, 2)));
        
        speed+=dv*u.getStepDuration()*recalculation_step;
        
        if (speed>desiredSpeed)
          speed = desiredSpeed;

        if (speed>allowedSpeed)
          speed = allowedSpeed;

        // do a traffic jam
        if (Float.isNaN(speed)||(speed<0.0f))
          speed = 0.0f;
      }
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
    
    // if node has arrived to destination and
    // stayed enough, a new destination is choosen
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
        recalculateSpeed();
        speedChanged = true;
      }

      // do not move in a jam, wait for the next recalculation
      if (speed==0.0f)
        return 0;
      
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
            // update distance to next intersection
            distanceToIntersection -= (float)owner.getPosition().getDistance(destination);
            u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                     ((Node)owner).getID()+" new distance to intersection "+
                                                     distanceToIntersection));
          }
          else
          {
            // move until the next speed change event occur
            u.sendNotification(new MovementChangedNotification(this, u, owner.getPosition().add(movement.mult(dist)), speed*1000f));
            // update distance to next intersection
            distanceToIntersection -= (float)dist;
            u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                     ((Node)owner).getID()+" new distance to intersection "+
                                                     distanceToIntersection));
          }
        }
        
        owner.setPosition(owner.getPosition().add(m));
      }
      else
      {
        if (speedChanged) {
          u.sendNotification(new MovementChangedNotification(this, u, destination, speed*1000f));
          // update distance to next intersection
          distanceToIntersection -= (float)m.getLength();
          u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                   ((Node)owner).getID()+" new distance to intersection "+
                                                   distanceToIntersection));
       }

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
      "Loading IntelligentDriverMotion extension"));

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

    n = element.getElementsByTagName("step").item(0);
    if(n==null)
      throw new Exception("<step> is missing!");
    int i = (int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);
    if ((i<=u.getStepDuration())||(i%u.getStepDuration()!=0))
      throw new Exception("Invalid <step> value: "+(float)i/1000.0f);      
    recalculation_step = i/u.getStepDuration();

    n = element.getElementsByTagName("l").item(0);
    if(n!=null)
      vehicleLength = Float.parseFloat(n.getFirstChild().getNodeValue());
    
    n = element.getElementsByTagName("a").item(0);
    if(n!=null)
      a = Float.parseFloat(n.getFirstChild().getNodeValue())/1e6f;

    n = element.getElementsByTagName("b").item(0);
    if(n!=null)
      b = Float.parseFloat(n.getFirstChild().getNodeValue())/1e6f;

    n = element.getElementsByTagName("s0").item(0);
    if(n!=null)
      s0 = Float.parseFloat(n.getFirstChild().getNodeValue());

    n = element.getElementsByTagName("t").item(0);
    if(n!=null)
      t = Float.parseFloat(n.getFirstChild().getNodeValue())*1e3f;

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
    if (vehicleLength<0)
      throw new Exception("Invalid <l> value: "+vehicleLength);
    if (a<=0)
      throw new Exception("Invalid <a> value: "+a*1e6f);
    if (b<=0)
      throw new Exception("Invalid <b> value: "+b*1e6f);
    if (s0<=0)
      throw new Exception("Invalid <s0> value: "+s0);
    if (t<=0)
      throw new Exception("Invalid <t> value: "+t/1e3f);
    if (stay<0)
      throw new Exception("Invalid <stay> value: "+(float)stay/1000);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading IntelligentDriverMotion extension"));
  }
}
