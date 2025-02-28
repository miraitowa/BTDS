package polito.uomm;

/**
 * <p>Title: Module for User-Oriented Mobility Model</p>
 * <p>Description: Intelligent Driver Model with Intersection Management (IDM-IM)
 *    adds intersection management features to the Intelligent Driver Model (IDM)</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Politenico di Torino</p>
 * @author Marco Fiore
 * @version 1.0
 */

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels.Movement;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.tripmodel.core.*;
import de.uni_stuttgart.informatik.canu.uomm.*;

/**
 * This class implements Intelligent Driver Motion with Intersection Management behavior.
 * 
 * The implementation is based on M. Treiber and D. Helbing,
 * "Explanation of Observed Features of Self-Organization in Traffic Flow",
 * Preprint cond-mat/9901239 (1999). Original Intersection Management added.<br>
 * <br>
 *
 * @author Marco Fiore
 * @version 1.0
 */
public class IDM_IM extends IntelligentDriverMotion
{ 
  /**
   * Maximum deceleration factor
   */  
  protected float kappa = 5;

  /**
   * Safety distance to stop from center the intersection
   */  
  protected float S = 7.0f;                  // in m

  /**
   * True if crossing an intersection regulated by a stop sign
   */
  protected boolean lightsManagement = false;

  /**
   * True if crossing an intersection regulated by a stop sign
   */
  protected boolean stopManagement = false;

  /**
   * Time still to wait before crossing the intersection in presence of stop
   */
  protected float stopWaitTime = 0.0f;       // in ms
  
  /**
   * Time it takes for a vehicle to cross the intersection
   */  
  protected float stopWaitStep = 2.0f*1000;   // in ms

  /**
   * Toggle borders management on/off
   */  
  protected boolean ignoreBorders = false;

  /**
   * Road segements to next ahead junction or end of the trip
   */
  protected int segmentsToIntersection = 0;

  /**
   * Distance to trip end
   */
  protected float distanceToTripEnd = 0.0f;

  /**
   * Constructor
   */
  public IDM_IM()
  {
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Intelligent Driver Model with Intersection Management Behavior";
  }

  /**
   * Gets the number of segments to next intersection. <br>
   * <br>
   * @return the number of segments to next intersection
   */
  public int getSegmentsToIntersection()
  {
    return segmentsToIntersection;
  }

  /**
   * Gets the distance to end of trip. <br>
   * <br>
   * @return the distance to end of trip
   */
  public float getDistanceToTripEnd()
  {
    return distanceToTripEnd;
  }

  /**
   * Measures the number of segments to next junction. <br>
   * <br>
   * @return the number of segments to next junction, including the current one
   */
  public int findSegmentsToIntersection() {
    Graph graph = null;
		
    if (spatialModel != null) {
      graph = spatialModel.getGraph();
    }
		
    int numSegments = 0;
    
    if (graph != null) {
      // consider current segment
      numSegments++;
			
      // check if current destination corresponds to an intersection
      Vertex vertexDest = graph.getVertex(destination.getX(), destination.getY());
      SpatialModelElement elementDest = spatialModel.mapVertexToJunction(vertexDest);
      if (elementDest != null) {
        return numSegments;
      }
			
      // update segments through trip, until next junction is reached
      for (int i=0; i<trip.getPath().size(); i++) {
        Point p = (Point)trip.getPath().get(i);
        Vertex vertex = graph.getVertex(p.getX(), p.getY());
        SpatialModelElement element = spatialModel.mapVertexToJunction(vertex);
        numSegments++;
        if (element != null) {
          break;
        }
      }
    }

    return numSegments;
  }

  /**
   * Computes the distance remaining to trip end. <br>
   * <br>
   * @return the distance remaining to trip end
   */
  public float findDistanceToTripEnd() {
    Graph graph = null;
		
    if (spatialModel != null) {
      graph = spatialModel.getGraph();
    }
		
    float distance = 0.0f;

    if (graph != null) {
      Node owner = (Node)this.owner;
      java.util.Iterator tripIter = trip.getPath().iterator();

      // as this method is invoked before stripping the
      // destination off the node trip, we can simply
      // process the whole trip to get the distance
      Point currPoint = new Point(owner.getPosition().getX(), owner.getPosition().getY());
      Point nextPoint = null;
      while (tripIter.hasNext()) {
        nextPoint = (Point)tripIter.next();
        distance += currPoint.getDistance(nextPoint);
        currPoint = nextPoint;
      }
    }

    return distance;
  }

  /**
   * Chooses a new destination and movement speed
   */
  protected void chooseNewMovement()
  {
    Node owner = (Node)this.owner;

    if (trip.getPath().size()==0) {
      chooseNewPath();
      // compute distance to trip end
      distanceToTripEnd = findDistanceToTripEnd();
    }

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
      segmentsToIntersection = findSegmentsToIntersection();
    }

    // look for a new maximum allowed speed, if any
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
    
    // if the ignoreBorders option is set, assume non-zero speed
    // for vehicles entering the scenario from its borders
    if (ignoreBorders &&
        (oldPosition.getX() == graph.getLeftmostCoordinate() ||
         oldPosition.getX() == graph.getRightmostCoordinate() ||
         oldPosition.getY() == graph.getLowermostCoordinate() ||
         oldPosition.getY() == graph.getUppermostCoordinate())) {
      speed = java.lang.StrictMath.min(minSpeed, allowedSpeed)/2;
      return;
    }

    recalculateSpeed();
  }
  
  /**
   * Update speed according to IDM acceleration
   */
  protected float IDM(float d_speed1, float d_1)
  {
    float newSpeed = 0.0f;
    float dv = 0.0f;

    // compute standard IDM acceleration...
    if (d_1 >= 0) {
      float ss = (float)(s0 + Math.max(speed*t+speed*d_speed1/(2.0*Math.sqrt(a*b)), 0));
      dv = (float)(a*(1-Math.pow(speed/desiredSpeed, 4)-Math.pow(ss/d_1, 2)));
    }
    // ...or try to stop as soon as possible
    // if the safety distance was passed. In
    // such case, a deceleration five times
    // harder than the comfortable one is used
    else
      dv = -kappa*b;

    newSpeed = speed + dv*u.getStepDuration()*recalculation_step;

    if (newSpeed>desiredSpeed)
      newSpeed = desiredSpeed;

    if (speed>allowedSpeed)
      newSpeed = allowedSpeed;

    // do a traffic jam
    if (Float.isNaN(newSpeed)||(newSpeed<0.0f))
      newSpeed = 0.0f;

    u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                             ((Node)owner).getID()+" speed "+
                                             speed*1000+" DeltaV "+
                                             d_speed1*1000+" distance "+d_1+" --> newSpeed "+
                                             newSpeed*1000+" (desired "+desiredSpeed*1000+")"));

    // a minimum threshold speed of
    // 0.001 m/s (0.000001 m/ms) is set
    if(newSpeed < 1.0e-6f)
      newSpeed = 0.0f;

    return newSpeed;
  }
  
  /**
   * Returns the current action code for ahead intersection
   */
  protected int getAction()
  {
    // initialize action code to "green light", always granting movement
    // NOTE: an ad-hoc code for no-action should be defined
    int action = 5; 

    Graph graph = null;
    if (spatialModel != null) {
      graph = spatialModel.getGraph(); 
    }

    Vertex vs = graph.getVertex(oldPosition.getX(), oldPosition.getY());
    Vertex vd = graph.getVertex(destination.getX(), destination.getY());

    Edge edge = null;
    if ((vs!=null)&&(vd!=null))
      edge = spatialModel.findEdge(vs, vd);
	
    int segments = getSegmentsToIntersection();

    // if this vehicle is going to reach its trip end within
    // before reaching the next intersection, return -1
    if(trip.getPath().size() == segments-1)
      action = -1;

    // otherwise, return the action for the next intersection
    else {
      Vertex vertex_b = null;
      Vertex vertex_i = null;
      if (segments >= 3) {
        // get the vertex before next intersection
        Point point_b = (Point)trip.getPath().get(segments-3);
        vertex_b = graph.getVertex(point_b.getPosition().getX(), point_b.getPosition().getY());
        // get the vertex matching next intersection
        Point point_i = (Point)trip.getPath().get(segments-2);
        vertex_i = graph.getVertex(point_i.getPosition().getX(), point_i.getPosition().getY());
      }
      else if (segments == 2) {
        // get the vertex before next intersection
        Position3D position_b = getDestination();
        vertex_b = graph.getVertex(position_b.getX(), position_b.getY());
        // get the vertex matching next intersection
        Point point_i = (Point)trip.getPath().get(segments-2);
        vertex_i = graph.getVertex(point_i.getPosition().getX(), point_i.getPosition().getY());
      }
      else if (segments == 1) {
        // get the vertex before next intersection
        Position3D position_b = getOldPosition();
        vertex_b = graph.getVertex(position_b.getX(), position_b.getY());
        // get the vertex matching next intersection
        Position3D position_i = getDestination();
        vertex_i = graph.getVertex(position_i.getX(), position_i.getY());
      }
      // get the vertex after next intersection
      Point point_a = (Point)trip.getPath().get(segments-1);
      Vertex vertex_a = graph.getVertex(point_a.getPosition().getX(), point_a.getPosition().getY());
      // get the edge before next intersection
      Edge edge_b = spatialModel.findEdge(vertex_b, vertex_i);
      // get action
      action = spatialModel.allowedMovements((Node)this.owner, edge_b, vertex_i, vertex_a);
    }

    u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+((Node)owner).getID()+
                                             " got action " + action));
    return action;
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
      Node front = null;

      Vertex vs = graph.getVertex(oldPosition.getX(), oldPosition.getY());
      Vertex vd = graph.getVertex(destination.getX(), destination.getY());

      Edge currentEdge = null;
      if ((vs!=null)&&(vd!=null))
        currentEdge = spatialModel.findEdge(vs, vd);

      if (currentEdge!=null)
      {
        IDM_IM n1_mf = null;
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
          if (!(n_m instanceof IDM_IM))
            continue;

          IDM_IM n_mf = (IDM_IM)n_m;
          // ignore paused cars
          if ((n_mf.destination==null)||(n_mf.stay!=0))
            continue;

          // ignore cars moving through an intersection
          if ((n_mf.stopManagement && n_mf.stopWaitTime <= 0) ||
               n_mf.lightsManagement)
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
              front = node;
            }
            else
            if (d<d_2)
            {
              d_2 = d;
              d_speed2 = speed - n_mf.speed;
            }
          }
        }

        if(front!=null)
          u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                   ((Node)owner).getID()+" found front node "+
                                                   ((Node)front).getID()));

        // if the ignoreBorders option is set, ignore intersection management
        // as well as end-of-trip issues when heading towards borders of scenario
        if (ignoreBorders && getNextIntersection() != null &&
            (getNextIntersection().getX() == graph.getLeftmostCoordinate() ||
             getNextIntersection().getX() == graph.getRightmostCoordinate() ||
             getNextIntersection().getY() == graph.getLowermostCoordinate() ||
             getNextIntersection().getY() == graph.getUppermostCoordinate())) {
          speed = IDM(d_speed1, d_1);
          return;
        }

        // compute the speed to stop at the trip end.
        // A 1.0 meter bias is introduced so to ensure
        // that the end of trip is actually reached
        float tripEndSpeed = IDM(speed, getDistanceToTripEnd()+s0+1.0f);
        u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                 ((Node)owner).getID()+" trip end speed "+
                                                 tripEndSpeed*1000));

        // if this vehicle is not the first on its
        // lane, use standard IDM speed computation
        if(d_1 != Float.MAX_VALUE)
          speed = Math.min(tripEndSpeed,IDM(d_speed1, d_1));

        // if this vechile is the first on its lane,
        // use intersection management
        else {
          // handle a node approaching an itersection
          float safeDistance = 0.0f;
          int action = getAction();
          switch(action) {
            case -1: // the vehicle is going to reach its trip end
                     // before next intersection, ignore the traffic
                     // signs and decelerate to stop
                     speed = tripEndSpeed;
                     break;

            case 0: // forbidden movement: this should be avoided
                    // by path selection when trip is generated
                    break;

            case 3: // yield sign: not yet implemented
                    break;
 
            case 4: // priority sign: not yet implemented
                    break;

            case 5: // green traffic light: do not stop
                    speed = Math.min(tripEndSpeed,IDM(d_speed1, d_1));
                    u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                             ((Node)owner).getID()+": green light"));
                    break;

            case 1: // red traffic light: stop if safe
                    safeDistance = getDistanceToIntersection() - S;
                    float minimumStopDistance = (float)Math.pow((double)speed,2)/(kappa*b);
                    // if the vechicle is already within safety distance S
                    // from intersection, keep crossing the intersection
                    u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                             ((Node)owner).getID()+": red light |"+
                                                             " distance to stop place "+safeDistance+
                                                             ", minimum stop distance "+minimumStopDistance));
                    if(safeDistance < minimumStopDistance) {
                      speed = Math.min(tripEndSpeed,IDM(d_speed1, d_1));
                      if(lightsManagement == false) {
                        lightsManagement = true;
                        u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                             ((Node)owner).getID()+" setting light management ON"));
                      }
                      else {
                        float covered = (recalculation_step-u.getTimeInSteps()%recalculation_step)*u.getStepDuration()*speed;
                        u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                                   ((Node)owner).getID()+" covered "+covered+
                                                                   ", distance "+getDistanceToIntersection()));
                        // stop crossing the intersection
                        if(getDistanceToIntersection() < covered) {
                          lightsManagement = false;
                          u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                             ((Node)owner).getID()+" setting light management OFF"));
                        }
                      }
                    }
                    // if the vehicle is able to brake
                    // on time, start decelerating
                    else
                      speed = Math.min(tripEndSpeed,IDM(speed, safeDistance));
                    break;

            case 2: // stop sign: stop and wait
                    if(speed != 0) {
                      u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                               ((Node)owner).getID()+" speed != 0"));
                      // if the vehicle is approaching the stop sign, update
                      // speed according to IDM in a way to stop at intersection
                      if(!stopManagement) {
                        safeDistance = getDistanceToIntersection() - S;
                        speed = Math.min(tripEndSpeed,IDM(speed, safeDistance));
                        u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                                 ((Node)owner).getID()+" approaching intersection, "+
                                                                 "distance-S = "+safeDistance+", speed "+speed*1000));
                        // if the IDM computation leads to zero speed, it means
                        // that we reached the stop sign: start to wait
                        if(speed == 0) {
                          Point intersection_p = getNextIntersection();
                          Vertex intersection_v = graph.getVertex(intersection_p.getX(), intersection_p.getY());
                          stopWaitTime = spatialModel.vehiclesInJunction(intersection_v) * stopWaitStep;
                          spatialModel.addVehicleInJunction(intersection_v);
                          u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                                            ((Node)owner).getID()+" add vehicle to intersection " +
                                                                            intersection_p.getX()+","+intersection_p.getY()+
                                                                            " --> total "+spatialModel.vehiclesInJunction(intersection_v)));
                          stopManagement = true;
                        }
                      }
                      // if the vehicle is leaving the intersection,
                      // update its speed according to standard IDM
                      else {
                        speed = Math.min(tripEndSpeed,IDM(d_speed1, d_1));
                      }
                    }
                    // if the vehicle is waiting at stop sign, check
                    // if it can move on or it still has to keep waiting 
                    else {
                      stopWaitTime -= recalculation_step*u.getStepDuration();
                      u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                               ((Node)owner).getID()+" waiting, still "+
                                                               stopWaitTime+" before leaving"));
                      if (stopWaitTime <= 0) {
                        u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                                 ((Node)owner).getID()+" leaving now!"));
                        speed = Math.min(tripEndSpeed,IDM(d_speed1, d_1));
                      }
                    }
          }
            
        }
        
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
            /* u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                               ((Node)owner).getID()+
                                               " ["+owner.getPosition().getX()+
                                               ","+owner.getPosition().getY()+
                                               "], dest ["+destination.getX()+
                                               ","+destination.getY()+
                                               "] --> distance "+owner.getPosition().getDistance(destination)+
                                               ", stepMovement "+m.getLength()+
                                               ", speedChanged "+speedChanged+
                                               ", speed "+speed+" ")); */
            // move to destination
            u.sendNotification(new MovementChangedNotification(this, u, destination, speed*1000f));
            // update distance and segments to next intersection and trip end
            distanceToIntersection -= (float)owner.getPosition().getDistance(destination);
            distanceToTripEnd -= (float)owner.getPosition().getDistance(destination);
            segmentsToIntersection--;
          }
          else
          {
            // move until the next speed change event occur
            u.sendNotification(new MovementChangedNotification(this, u, owner.getPosition().add(movement.mult(dist)), speed*1000f));
            // update distance to next intersection and trip end
            distanceToIntersection -= (float)dist;
            distanceToTripEnd -= (float)dist;
          }
        }
        
        owner.setPosition(owner.getPosition().add(m));
      }
      else
      {
        if (speedChanged) {
          u.sendNotification(new MovementChangedNotification(this, u, destination, speed*1000f));
          // update distance and segmentsto next intersection and trip end
          distanceToIntersection -= (float)m.getLength();
          distanceToTripEnd -= (float)m.getLength();
          segmentsToIntersection--;
        }

        owner.setPosition(destination);
        
        // stop and light managemement handling at destination
        Graph graph = null;
        if (spatialModel != null) {
          graph = spatialModel.getGraph();
        }
        if (graph != null) {
          Point intersection_p = getNextIntersection();
          if (intersection_p != null) {
            u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                     ((Node)owner).getID()+" reached destination [" +
                                                     destination.getX()+","+destination.getY()+
                                                     "], next intersection ["+intersection_p.getX()+
                                                     ","+intersection_p.getY()+"]"));
            // if this vehicle reached the intersection and stop intersection
            // management is active disable it and notify junction
            if (stopManagement &&
                destination.getX() == intersection_p.getX() &&
                destination.getY() == intersection_p.getY()) {
              stopManagement = false;
              Vertex intersection_v = graph.getVertex(intersection_p.getX(), intersection_p.getY());
              spatialModel.removeVehicleInJunction(intersection_v);
              u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                       ((Node)owner).getID()+" remove vehicle from intersection " +
                                                       destination.getX()+","+destination.getY()+
                                                       " --> total "+spatialModel.vehiclesInJunction(intersection_v)));
            }
            // if this vehicle reached the intersection and traffic
            // lights intersection management is active disable it
            if (lightsManagement &&
                destination.getX() == intersection_p.getX() &&
                destination.getY() == intersection_p.getY()) {
              lightsManagement = false;
              u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                       ((Node)owner).getID()+" setting light management OFF"));
            }
          }
          else {
            // JHNote (30/08/2006): if the end of trip is not on an intersection,
            //                      and there is no intersection before the end of trip,
            //                      we cannot reach any intersection. So, just update
            //                      the car position and wait for the next time step
            u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+
                                                     ((Node)owner).getID()+" reached destination [" +
                                                     destination.getX()+","+destination.getY()+
                                                     "] no new intersection before the end of this trip (end of trip on intermediate point"));
            // if stop management is active, turn it off now
            if (stopManagement) {
              stopManagement = false;
            }
            // if lights management is active, turn it off now
            if (lightsManagement) {
              lightsManagement = false;
            }
          }
        }
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
      "Loading IDM-IM extension"));

    super.load(element);
		
    org.w3c.dom.Node n;

    n = element.getElementsByTagName("ignoreBorders").item(0);
    if(n!=null)
      ignoreBorders = Boolean.valueOf(n.getFirstChild().getNodeValue()).booleanValue();
    
    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading IDM-IM extension"));
  }
}
