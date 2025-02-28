package polito.uomm;

/**
 * <p>Title: Module for User-Oriented Mobility Model</p>
 * <p>Description: Intelligent Driver Model with Lane Changing (IDM-LC)
 *    adds lane changing features to the Intelligent Driver Model with
 *    Intersection Management (IDM-IM)</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Politenico di Torino</p>
 * @author Marco Fiore
 * @version 0.6
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
 * @version 0.6
 */
public class IDM_LC extends IDM_IM
{
  /**
   * Maximum safe deceleration (in meters/ms^2) 
   */
  protected float bsave = 4e-6f;               // in m/ms^2
  
  /**
   * Politeness factor
   */
  protected float p = 0.5f;

  /**
   * Threshold acceleration for lane change, must be below
   * the acceleration IDM parameter 'a' (in meters/ms^2)
   */
  protected float athr = 0.2e-6f;              // in m/ms^2

  /**
   * Minimum distance between cars when changing lane (in meters)
   */
  protected float minGap = 2.0f;               // in m

  /**
   * Constant value to increase advantage in moving to right lane
   * to reflect real drivers behavior (in meters/ms^2)
   */
  protected float biasRight = 0.2e-6f;         // in m/ms^2

  /**
   * Constructor
   */
  public IDM_LC()
  {
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Intelligent Driver Model with Lane Changing Behavior";
  }

  /**
   * Returns new IDM acceleration between two nodes. <br>
   * <br>
   *
   * @param backNode the back Node
   * @param frontNode the ahead Node
   * @return the acceleration of the backNode, if frontNode is the node ahead of it
   */
  protected float computeAcceleration(Node backNode, Node frontNode) {
    IDM_LC backMobil, frontMobil;
    float gap = Float.MAX_VALUE;
    float speedDiff = 0.0f;
 
    if (backNode != null)
      backMobil = (IDM_LC)backNode.getExtension("Movement");
    else
      return 0.0f;

    if (frontNode != null) {
      frontMobil = (IDM_LC)frontNode.getExtension("Movement");
      float backDistance = backMobil.getDistanceToIntersection();
      float frontDistance = frontMobil.getDistanceToIntersection();
      gap = backDistance - frontDistance - frontMobil.vehicleLength;
      speedDiff = backMobil.speed - frontMobil.speed;
    }
    else {
      //gap = (float)backNode.getPosition().getDistance(oldPosition);
      speedDiff = backMobil.speed - 0.0f;
    }

    float ss = (float)(s0 + Math.max(backMobil.speed*t+backMobil.speed*speedDiff/(2.0*Math.sqrt(a*b)), 0));
    float dv = (float)(a*(1-Math.pow(backMobil.speed/backMobil.desiredSpeed, 4)-Math.pow(ss/gap, 2)));
    
    return dv;
  }

  /**
   * Performs lane change according to the MOBIL lane changing model
   */
  protected void changeLane() {
    Graph graph = null;

    if (spatialModel != null) {
      graph = spatialModel.getGraph(); 
    }

    if (graph != null) {
      Node owner = (Node)this.owner;

      Vertex vs = graph.getVertex(oldPosition.getX(), oldPosition.getY());
      Vertex vd = graph.getVertex(destination.getX(), destination.getY());

      Edge currentEdge = null;
      if (vs != null && vd != null)
        currentEdge = spatialModel.findEdge(vs, vd);

      SpatialModelElement nextRoadElement = null;
      int numLanes = 1;
      if (currentEdge != null) {
        nextRoadElement = spatialModel.mapEdgeToElement(currentEdge);
        numLanes = Integer.parseInt((String)nextRoadElement.getAttributes().get("NL"));
      }

      // proceed only if the current road has multiple lanes
      if (nextRoadElement != null && numLanes > 1) {

        // initialize front and back nodes nodes on the current, right and left lanes
        Node currFrontNode = null, currBackNode = null;
        float currFrontGap = Float.MAX_VALUE;
        float currBackGap = Float.MAX_VALUE;
        Node  rightFrontNode = null, rightBackNode = null;
        float rightFrontGap = Float.MAX_VALUE;
        float rightBackGap = Float.MAX_VALUE;
        Node leftFrontNode = null, leftBackNode = null;
        float leftFrontGap = Float.MAX_VALUE;
        float leftBackGap = Float.MAX_VALUE;

        // find front and back nodes nodes on the current, right and left lanes
        java.util.Iterator iter = u.getNodes().iterator();
        while (iter.hasNext()) {
          Node node = (Node)iter.next();

          if (owner==node)
            continue;

          Movement n_m = (Movement)node.getExtension("Movement");
          if (!(n_m instanceof IDM_LC))
            continue;
          
          IDM_LC n_mf = (IDM_LC)n_m;
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
          
          // current vehicle is on same lane
          if (node.getLane() == owner.getLane()) {
            float thisDistance = getDistanceToIntersection();
            float nodeDistance = n_mf.getDistanceToIntersection();
            // check if front vehicle
            if (thisDistance >= nodeDistance) {
              float d = thisDistance - nodeDistance - n_mf.vehicleLength;
              if(d < currFrontGap) {
                currFrontNode = node;
                currFrontGap = d;
              }
            }
            // check if back vehicle
            else {
              float d = nodeDistance - thisDistance - vehicleLength;
              if(d < currBackGap) {
                currBackNode = node;
                currBackGap = d;
              }
            }
          }
  
          // current vehicle is on the right lane
          else if (node.getLane() == owner.getLane()-1) {
            float thisDistance = getDistanceToIntersection();
            float nodeDistance = n_mf.getDistanceToIntersection();
            // check if front vehicle
            if (thisDistance >= nodeDistance) {
              float d = thisDistance - nodeDistance - n_mf.vehicleLength;
              if(d < rightFrontGap) {
                rightFrontNode = node;
                rightFrontGap = d;
              }
            }
            // check if back vehicle
            else {
              float d = nodeDistance - thisDistance - vehicleLength;
              if(d < rightBackGap) {
                rightBackNode = node;
                rightBackGap = d;
              }
            }
          }

          // current vehicle is on the left lane
          else if (node.getLane() == owner.getLane()+1) {
            float thisDistance = getDistanceToIntersection();
            float nodeDistance = n_mf.getDistanceToIntersection();
            // check if front vehicle
            if (thisDistance >= nodeDistance) {
              float d = thisDistance - nodeDistance - n_mf.vehicleLength;
              if(d < leftFrontGap) {
                leftFrontNode = node;
                leftFrontGap = d;
              }
            }
            // check if back vehicle
            else {
              float d = nodeDistance - thisDistance - vehicleLength;
              if(d < leftBackGap) {
                leftBackNode = node;
                leftBackGap = d;
              }
            }
          }
      
          // current vehicle is more than one lane far away
          else
            continue;
        }
        
        // debug
        u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+((Node)owner).getID()));
        if (currFrontNode != null)
          u.sendNotification(new DebugNotification(this, u, "               [MOBIL] currFrontNode "+currFrontNode.getID()+", gap "+currFrontGap));
        else
          u.sendNotification(new DebugNotification(this, u, "               [MOBIL] currFrontNode NULL, gap "+currFrontGap));
        if (currBackNode != null)
          u.sendNotification(new DebugNotification(this, u, "               [MOBIL] currBackNode "+currBackNode.getID()+", gap "+currBackGap));
        else
          u.sendNotification(new DebugNotification(this, u, "               [MOBIL] currBackNode NULL, gap "+currBackGap));
        if (rightFrontNode != null)
          u.sendNotification(new DebugNotification(this, u, "               [MOBIL] rightFrontNode "+rightFrontNode.getID()+", gap "+rightFrontGap));
        else
          u.sendNotification(new DebugNotification(this, u, "               [MOBIL] rightFrontNode NULL, gap "+rightFrontGap));
        if (rightBackNode != null)
          u.sendNotification(new DebugNotification(this, u, "               [MOBIL] rightBackNode "+rightBackNode.getID()+", gap "+rightBackGap));
        else
          u.sendNotification(new DebugNotification(this, u, "               [MOBIL] rightBackNode NULL, gap "+rightBackGap));
        if (leftFrontNode != null)
          u.sendNotification(new DebugNotification(this, u, "               [MOBIL] leftFrontNode "+leftFrontNode.getID()+", gap "+leftFrontGap));
        else
          u.sendNotification(new DebugNotification(this, u, "               [MOBIL] leftFrontNode NULL, gap "+leftFrontGap));
        if (leftBackNode != null)
          u.sendNotification(new DebugNotification(this, u, "               [MOBIL] leftBackNode "+leftBackNode.getID()+", gap "+leftBackGap));
        else
          u.sendNotification(new DebugNotification(this, u, "               [MOBIL] leftBackNode NULL, gap "+leftBackGap));

        // if right lane exists and safety conditions
        // are respected, evaluate movement to right lane
        float rightAdv = 0.0f;
        if (owner.getLane()-1 > 0 &&
            computeAcceleration(rightBackNode, owner) > -bsave &&           
            rightFrontGap > minGap && rightBackGap > minGap) {
          // compute advantage adding the right bias
          rightAdv = computeAcceleration(owner, rightFrontNode) -
                     computeAcceleration(owner, currFrontNode) +
                     biasRight;
          // compute disadvantage considering 'pushing' vehicles in our lane
          float disadv = computeAcceleration(rightBackNode, rightFrontNode) +
                         computeAcceleration(currBackNode, owner) -
                         computeAcceleration(rightBackNode, owner) -
                         computeAcceleration(currBackNode, currFrontNode);
          if(disadv < 0)
            disadv = 0;
          // get overall advantage
          rightAdv -= p * disadv; 
        }

        // if left lane exists and safety conditions
        // are respected, evaluate movement to left lane
        float leftAdv = 0.0f;
        if (owner.getLane()+1 <= numLanes &&
            computeAcceleration(leftBackNode, owner) > -bsave &&
            leftFrontGap > minGap && leftBackGap > minGap) {
          // compute advantage subtracting the right bias
          leftAdv = computeAcceleration(owner, leftFrontNode) -
                    computeAcceleration(owner, currFrontNode) -
                    biasRight;
          // compute disadvantage ignoring 'pushing' vehicles in our lane
          float disadv = computeAcceleration(leftBackNode, leftFrontNode) +
                         computeAcceleration(leftBackNode, owner);
          if(disadv < 0)
            disadv = 0;
          // get overall advantage
          leftAdv -= p * disadv;
        }

        u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+((Node)owner).getID()+
                                                          " [MOBIL] rightAdv "+rightAdv+ ", leftAdv "+leftAdv));

        // move to right lane
        if(rightAdv >= leftAdv && rightAdv > athr) {
          owner.setLane(owner.getLane()-1);
          u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+((Node)owner).getID()+
                                                            " [MOBIL] move to right lane,"));
        }
        // move to left lane
        if(leftAdv > rightAdv && leftAdv > athr) {
          owner.setLane(owner.getLane()+1);
          u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+" "+((Node)owner).getID()+
                                                            " [MOBIL] move to left lane,"));
        }
      }

      // if current road has only one lane, force this vehicle to stay
      // on it (this is useful when a vehicle ends its trip on a multiple
      // lane road and then restarts its movement on a single lane road)
      else {
        owner.setLane(1);
      }
    }

    return;
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
        IDM_LC n1_mf = null;
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
          if (!(n_m instanceof IDM_LC))
            continue;
          
          IDM_LC n_mf = (IDM_LC)n_m;
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
          
          // if 1) this is the first vehicle on its lane (verified)
          //    2) this vehicle is not going to end its trip before
          //       or right at next intersection (i.e. action != -1)
          //    3) this vehicle is traveling on a lane which does
          //       not exist on the road it will take after the junction
          // then force the vehicle to move to a right-hand lane, by
          // tweaking the values of distance and speed with respect to
          // the front car, as if a stopped car was present on current
          // lane right before next intersection.
          int segments = getSegmentsToIntersection();
          if (action != -1) {
            // get the vertex matching next intersection
            Vertex vertex_i = null;
            if (segments >=2) {
              Point point_i = (Point)trip.getPath().get(segments-2);
              vertex_i = graph.getVertex(point_i.getPosition().getX(), point_i.getPosition().getY());
            }
            else {
              Position3D position_i = getDestination();
              vertex_i = graph.getVertex(position_i.getX(), position_i.getY());
            }
            // get the vertex after next intersection
            Point point_a = (Point)trip.getPath().get(segments-1);
            Vertex vertex_a = graph.getVertex(point_a.getPosition().getX(), point_a.getPosition().getY());
            // get the edge after next intersection
            Edge edge_a  = spatialModel.findEdge(vertex_i, vertex_a);
            SpatialModelElement road_a = spatialModel.mapEdgeToElement(edge_a);
            if (Integer.parseInt((String)road_a.getAttributes().get("NL")) < owner.getLane()) {
              d_1 = getDistanceToIntersection() - S;
              d_speed1 = speed;
              u.sendNotification(new DebugNotification(this, u, "at "+u.getTimeAsString()+
                                                       " "+((Node)owner).getID()+ " current lane "+owner.getLane()+
                                                       ", next road has"+Integer.parseInt((String)road_a.getAttributes().get("NL"))+
                                                       " lanes --> force merging to the right"));    
            }
          }

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
                    float minimumStopDistance = (float)Math.pow((double)speed,2)/(5*b);
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
      // perform lane changing computation one simulation step before AIDM computation
      if (u.getTimeInSteps()%recalculation_step == recalculation_step-1) {
        changeLane();
      }

      if ((!speedChanged) && (u.getTimeInSteps()%recalculation_step==0))
      {
        recalculateSpeed();
        speedChanged = true;
      }

      // do not move in a jam, wait for the next recalculation
      if (speed==0.0f)
        return 0;

      // move towards destination
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
        if (spatialModel !=null) {
          graph = spatialModel.getGraph();
	}
        if (graph!=null) {
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
      "Loading IDM-LC extension"));

    super.load(element);
		
    org.w3c.dom.Node n;
    
    n = element.getElementsByTagName("bsave").item(0);
    if(n != null)
      bsave = Float.parseFloat(n.getFirstChild().getNodeValue())/1e6f;

    n = element.getElementsByTagName("p").item(0);
    if(n != null)
      p = Float.parseFloat(n.getFirstChild().getNodeValue());

    n = element.getElementsByTagName("athr").item(0);
    if(n != null)
      athr = Float.parseFloat(n.getFirstChild().getNodeValue())/1e6f;

    // check
    if (bsave<0)
      throw new Exception("Invalid <bsave> value: "+(float)bsave*1e6f);
    if (p<0)
      throw new Exception("Invalid <p> value: "+p);
    if (athr<=0)
      throw new Exception("Invalid <athr> value: "+athr*1e6f);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading IDM-LC extension"));
  }
}
