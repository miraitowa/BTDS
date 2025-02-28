package de.uni_stuttgart.informatik.canu.spatialmodel.core;

/**
 * <p>Title: Spatial Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * <p>Patches: </p>
 * <p>	v1.2 (24/08/2005): Added a set of cluster of obstacles
 *											   that control the density of 
 *										 	   roads within each cluster. The set
 *											   of clusters span the movement area.</p>
 * <p>	v1.3 (24/09/2005): Added Methods that returns current 
 *												 allowed movements and particular GDF
 *												 codes for intersection management. </p>
 * <p>	v1.4 (16/11/2005): Added Canu's ability to display 
 *												 user-defined graphs. The original CANU Mobisim 
 *												 is not able to display a simulation where a spatial 
 *												 model is not used </p>
 * <p>	v1.5 (17/11/2005): Added Methods to manage vehicles' statistics in intersections. </p>
 *
 * @author v1.1 Illya Stepanov
 * @author v1.2-v1.5 Jerome Haerri (haerri@ieee.org), Marco Fiore (fiore@tlc.polito.it)
 * @version 1.5
 */

import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import eurecom.spacegraph.*;
import eurecom.spatialmodel.extensions.*;

/**
 * This class implements Spatial Model
 * <p>Patches: </p>
 * <p> <i> Version 1.2 by  Jerome Haerri (haerri@ieee.org), Marco Fiore (fiore@tlc.polito.it) on 08/24/2005: 
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Added a set of cluster of obstacles
 *											   that control the density of 
 *										 	   roads within each cluster. The set
 *											   of clusters span the movement area.</i></p>
 * <p> <i> Version 1.3 by  Jerome Haerri (haerri@ieee.org) on 09/24/2005: 
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Added Methods that returns current 
 *												 allowed movements and particular GDF
 *												 codes for intersection management. </i></p>
 * <p>	<i> Version 1.4 by Jerome Haerri (haerri@ieee.org) on 11/16/2005: 
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Added Canu's ability to display 
 *												 user-defined graphs. The original CANU Mobisim 
 *												 is not able to display a simulation where a spatial 
 *												 model is not used </i></p>
 * <p>	<i> Version 1.5  by Jerome Haerri (haerri@ieee.org) on 11/17/2005: 
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Added Methods to manage vehicles' statistics in intersections. </i></p>
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.2 Marco Fiore
 * @author 1.2-1.5 Jerome Haerri
 * @version 1.5
 */
public class SpatialModel extends ExtensionModule
{
  /**
   * Spatial Model elements
   */
  protected java.util.Map elements = new java.util.HashMap();

  /**
   * Graph of the movement area
   */
  protected Graph graph;

  /**
   * Edge - spatial model element mapping
   * Key: Edge, Value: parent SpatialModelElement object
   */
  protected java.util.Map edgesParents;

  /**
   * Vertex-to-Junction mapping
   * Key: Vertex, Value: corresponding junction (SpatialModelElement object)
   */
  protected java.util.Map verticesToJunctions;
	
	 /**
   * Junction-to-Intersection mapping
   * Key: Junction, Value: level-2 Intersection where the junction belongs to (SpatialModelElement object)
	 * @since 1.3
   */
  protected java.util.Map verticesToIntersections;
	
	/**
   * Number of vehicles in intersection
   * Key: Junction ID, Value: Number of vehicles stopped in the junction.
	 * @since 1.3
   */
  protected java.util.Map nbVehiclesInJunction;
  
  /**
   * Vertex - edge mapping
   * Key: V1_ID:V2_ID, Value: associated edge
   */
  protected java.util.Map edgesCache;

	
	/**
   * Clipping Region
   */
  protected Polygon clipArea;
	
	/**
   * Clipping Region extreme values
   */
	public float min_x_clip = Float.NaN, max_x_clip = Float.NaN, min_y_clip = Float.NaN, max_y_clip = Float.NaN;
	
	
	 /**
   * Semaphore to enable Graph-based (non-spatialModel-based) visualization
	 * <br> 
	 * The original CANU Mobisim is not able to display a simulation where a spatial model
	 * is not used (in the case of user-defined Graphs). 
	 * @since 1.4
   */
	protected boolean externalGraph = false;
	
	/**
	 * name of the traffic light extension
	 * @since 1.5
	 */
	private static String trafficLightName= null;
	
	/**
   * Trafficlight Model
   */
	protected TrafficLight trafficLight=null;
	
	/**
   * Number of lanes per direction flow on each roadElement
   */
	protected int numberLane = 1;
	
	/**
   * Maximum Number of Traffic Lights in the SpacetialModel
   */
	protected int maxTrafficLight = 5;
	
	/**
   * Maximum Number of lanes in the SpatialModel
   */
	protected int maxNumberMultilane = 4;
	
	/**
	* All roads are to be considered for multilane
	*/
	protected boolean allRoads = true;
	
	/**
	* Separated traffic flows enabled
	*/
	protected boolean directional = true;
	
	/**
	* Separated roadElements disabled
	*/
	protected boolean doubleFlow = false;
	
	/**
	 * array that keeps track of nodes that are on the simulation boundary
   */
	public java.util.ArrayList bounderyPoints = new java.util.ArrayList();
	
	/**
	* Access method to gain access to the clipping area
	* @return the clipping area
	*/
	public Polygon getClipArea() {
	  return clipArea;
	}
	
	/**
	* Access method: 
	* @return 1 : if streets have differentiated flows
	*         0 : if streets do not have differentiated flows
	*/
	public boolean getDirections() {
	  return doubleFlow;
	}
	
/**
	*
	*Considers the type of vehicule, and the roadElement where
  *the vehicule is and the Junction it will reach, and returns an array
  *of allowed movements.
	* <br>
  *For example, due to the car type, or traffic signs, the car could not
  *turn on a particular roadElement.
  *This method should be used by the Dijikstra Algorithm when computing
  *the shortest path.
  *By default, we can admit that all movements are permitted.
	*<br>
	* @param me the node corrently moving
	* @param whereIam The edge on which the node is currently moving
	* @param whichJunctionIamreaching The junction the node is about to reach
	* @return java.util.ArrayList the list of all possible vertices which the node can reach 
	* @since 1.3
	**/
		public java.util.ArrayList allowedMovements(Node me, Edge whereIam, Vertex whichJunctionIamreaching) {
		 
		  java.util.ArrayList nbVertices = whichJunctionIamreaching.getNeighbours();
		  java.util.ArrayList allowed = new java.util.ArrayList(nbVertices.size());
      for (int i=0; i<nbVertices.size(); i++) {
        Vertex vertex = (Vertex)nbVertices.get(i);
				Edge edge = findEdge(whichJunctionIamreaching, vertex);
				
			  if ((edge !=null) && !isMovementProhibited(whichJunctionIamreaching,vertex) && !isMovementProhibited(whereIam,whichJunctionIamreaching,edge,me)) {
				  // means that the movement is neither prohibited by traffic flow nor by traffic signs
					allowed.add(vertex);
				}
			}
	  return allowed;
	}
	
	/** 
	 * Returns the Point element that is the next intersection based on the actual edge and the directional flow
	 * @param edge on which the node is currently moving
	 * @return de.uni_stuttgart.informatik.canu.spatialmodel.geometry.Point Next Intersection
	 * @since  1.3 
	 */
	public Point getNextIntersection(Edge edge) {
	  
		SpatialModelElement roadElement = mapEdgeToElement(edge);
		if (roadElement == null)
			return null;
		
		Polyline shape = (Polyline)roadElement.getGeometry();
		java.util.ArrayList points = shape.getPoints();
		Point destPoint = null;
	  
		if ((String)roadElement.getAttributes().get("DF") == "3") {
			destPoint = (Point)points.get(points.size()-1);
			//System.out.println("DF 3 Going to point " + ((Point)points.get(points.size()-1)).getX() + " " + ((Point)points.get(points.size()-1)).getY());
		}
		
		else if ((String)roadElement.getAttributes().get("DF") == "2") {
			destPoint = (Point)points.get(0); 
			//System.out.println("DF 2 Going to point " + ((Point)points.get(0)).getX() + " " + ((Point)points.get(0)).getY());
		}
		else {
		  //System.out.println("getDistanceToNextInteresection does not work for non differentiated roads");
		}
		
		return destPoint;
	}
		
   /**
   * Keeps track of vehicles that are currently inside an intersection
   * @param whichJunctionIamreaching the current junction
	 * @return int the number of vehicles inside the specified junction
	 * @since 1.5
   */
	public int vehiclesInJunction(Vertex whichJunctionIamreaching) {
		int nb = 0;
		String junctionID = null;
		SpatialModelElement junctionElement = mapVertexToJunction(whichJunctionIamreaching); 
		if (junctionElement != null) {
			junctionID = junctionElement.getID();
			Integer result = (Integer)nbVehiclesInJunction.get(junctionID);
			if (result != null) {
				nb = result.intValue();
			}
		}
	  return nb;
	}
	
	/**
   * Adds a vehicle in a junction
   * @param whichJunctionIamreaching the current junction
	 * @since 1.5
   */
	public void addVehicleInJunction(Vertex whichJunctionIamreaching) {
		int nb = 0;
		String junctionID = null;
		SpatialModelElement junctionElement = mapVertexToJunction(whichJunctionIamreaching); 
		if (junctionElement != null) {
			junctionID = junctionElement.getID();
		
			Integer result = (Integer)nbVehiclesInJunction.get(junctionID);
			if (result != null) {
				nb = result.intValue();
				nb++;
				result = new Integer(nb);
				nbVehiclesInJunction.put(junctionID,result);
			}
			else {
				nbVehiclesInJunction.put(junctionID,new Integer(1));
			}
		}
	}
	
	/**
   * Removes a vehicle in a junction
   * @param whichJunctionIamreaching the current junction
	 * @since 1.5 
   */
	public void removeVehicleInJunction(Vertex whichJunctionIamreaching) {
		int nb = 0;
		String junctionID = null;
		SpatialModelElement junctionElement = mapVertexToJunction(whichJunctionIamreaching); 
		if (junctionElement != null) {
			junctionID = junctionElement.getID();
		
			Integer result = (Integer)nbVehiclesInJunction.get(junctionID);
			if (result != null) {
				nb = result.intValue();
				nb--;
				result = new Integer(nb);
				nbVehiclesInJunction.put(junctionID,result);
			}
		}
	}
	
	/**
	* Considers the type of vehicule, and the roadElement where    
  *the vehicule is and the Junction it will reach, to know if
  *the requested movement to a new RoadElement is allowed.
  *Possible return values are listed below:
  *  0 movement forbidden
  *  1 stop and wait (traffic light red)
  *  2 stop and yield (stop sign)
  *  3 reduce speed and yield (yield sign)
  *  4 priority (priority sign)
  *  5 do not stop (traffic light green or yellow)
  *  By default, we can admit return 2: stop and yield 
	* @param me the node corrently moving
	* @param whereIam The edge on which the node is currently moving
	* @param whichJunctionIamreaching The junction the node is about to reach
	* @param whereIwantToGo The junction the node wishes to reach
	* @return int the allowed movement code
	* @since 1.3
	**/
		public int allowedMovements(Node me, Edge whereIam, Vertex whichJunctionIamreaching, Vertex whereIwantToGo) {
			//System.out.println("in allowedMovements");
			Edge edge = findEdge(whichJunctionIamreaching, whereIwantToGo);
			if (edge ==null)
				System.out.println("edge is null");
			
			// we check the node's direction regarding the edge.
			boolean plusDirection = true;
			SpatialModelElement roadElement = mapEdgeToElement(whereIam);
			if (roadElement == null) {
				//System.out.println("Either no roadElements has been defined (Graph-based SpaceGraph), or no roadElement attached to the edge. Cars stop");
				return 2;									 
			}
			
			SpatialModelElement junctionElement = mapVertexToJunction(whichJunctionIamreaching); 
			if (junctionElement == null) {
				//System.out.println("The node reached an intermediate point and not a Junction. Movement is always granted");
				return 4;
			}
			
			// JHNote (02/04/2006): In the case of a graph-based spatialmodel, we should exit here, since no relationship
			//											records have been implemented. We grant the movement.
			java.util.ArrayList testRelationships = roadElement.getRelations();
		  if ((testRelationships == null) || (testRelationships.size() == 0)) {
				//System.out.println("The Spatial Model does not implement relationships between traffic signs and roadElements. Cars stop");
				return 2;
			}
			
			Polyline shape = (Polyline)roadElement.getGeometry();
			if (shape == null)
				System.out.println("shape is NULL");
			
			java.util.ArrayList points = shape.getPoints();
			if (points == null)
				System.out.println("points is NULL");
			
			Point pointf = (Point)points.get(0); // initial point
			Point pointt = (Point)points.get(points.size()-1); // end point. As for GDF, "Plus" means from pointf to pointt and "Minus" the reverse.
			
			Point destination = (Point)junctionElement.getGeometry();
			if (destination == null)
				System.out.println("destination is NULL");
			
			
			// plusDirection only indicates the direction we are interested. Here, we are interested on outcoming direction. So first, we determine
				// the outcoming direction, then if this direction is open (DF).
				
			if(destination.contains(pointf))
				plusDirection = false;
			else
				plusDirection = true;
			
			// obtaining the node's type for traffic rules appliance
			String myVehicleCode = (String)me.getAttributes().get("VT");
			
				
			if ((edge !=null) && !isMovementProhibited(whichJunctionIamreaching,whereIwantToGo) && !isMovementProhibited(whereIam,whichJunctionIamreaching,edge,me)) {
				// means we can go in that direction. Now we want to know what we must do before doing so
				
				// relationship codes
				// JHNote (10/11/2005) Not implemented yet
				//String priorityRelationshipCode = "2104";
				
				String trafficSignPlusCode = "2301";
				String trafficSignMinusCode = "2302";
				String trafficLightPlusCode = "2303";
				String trafficLightMinusCode = "2304";
				
				//attribute codes
				String trafficSignCode = "7220";
				String trafficLightCode = "7230";
				String TrafficSignAttributeCode = "TS";
				String TrafficSignSymbolCode = "SY"; // 0 for all traffic
				String rightofWayCode = "50";
				String directionalCode = "51";
				String yieldCode = "15"; // 50 for right of ways and 15 for yield 
				String stopCode = "16";
				String priorityCode = "10";
				
				String allVehicleCode = "0";
				
				java.util.ArrayList relationships = roadElement.getRelations();
				for (int i = 0; i < relationships.size(); i++) {
					RelationshipRecord record = (RelationshipRecord)relationships.get(i);
					if (record ==null){
						continue;
					}
					if ((record.getCode() == trafficSignPlusCode && plusDirection) 
							|| (record.getCode() == trafficSignMinusCode && !plusDirection) )   {
						SpatialModelElement trafficSign = (SpatialModelElement)elements.get(record.getFeatures().get(0));
						if (trafficSign == null) {
							System.out.println("Traffic Sign is missing in the relationship");
							continue;
						}
						if ((String)trafficSign.getAttributes().get("TS") == rightofWayCode) {
						  // if the traffic sign applies to our type of car or to all cars
							if (((String)trafficSign.getAttributes().get("SY") == allVehicleCode) 
									|| ( (myVehicleCode != null) && ((String)trafficSign.getAttributes().get("SY") == myVehicleCode)) ) {
								if ((String)trafficSign.getAttributes().get("50") == yieldCode) {
									return 3;
								}
								else if ((String)trafficSign.getAttributes().get("50") == stopCode) {
									return 2;
								}
								else if ((String)trafficSign.getAttributes().get("50") == priorityCode) {
									return 4;
								}
								else
									return 2;
	
						 }
						 // else we ignore the traffic sign since it does not apply to us.
						 else {
							 System.out.println("Traffic light not appyable to us");
							 return 4;
						 }
						}
						else {
						  System.out.println("******************Should never enter here");
						}
					}
					else if ( ((record.getCode() == trafficLightPlusCode) && plusDirection) 
									 ||((record.getCode() == trafficLightMinusCode) && !plusDirection) ) {
						//SpatialModelElement trafficLight = (SpatialModelElement)elements.get(record.getFeatures().get(0));
						//System.out.println("checking traffic ligths");
						
						//JHNote (06/02/2006): The name of the TrafficLight extension is loaded at the beginning 
						//TrafficLight trafficLight = (TrafficLight)u.getExtension("TrafficLight");
						TrafficLight trafficLight = (TrafficLight)u.getExtension(trafficLightName);
						if (trafficLight == null) {
							return 2;
						}
						TrafficLightElement trafficLightElement = (TrafficLightElement)trafficLight.getTrafficLights().get(junctionElement.getID());
						
						if (trafficLightElement == null) {
							continue;
						}
						
						if (trafficLightElement.checkStatus(roadElement.getID())) {
							//System.out.println("Traffic light is GREEN");
						  return 5;
						}
						else {
							//System.out.println("Traffic light is RED");
							return 1;
						}
					}
					else {
					  //System.out.println("Ignoring traffic ligth or signs since they should not apply to us (wrong direction).....");
					}
				}
			}
		//System.out.println("Node "+ me.getID() + " returned action " + 0);
		return 0;
	}

  



	/**
	 * Cluster of obstacles elements
	 *
	 * A cluster in the Spatial Model may 
	 * be a downtown cluster, or a suburban
	 * cluster. This determines the density
	 * and the type of roads implemented 
	 * by the Spatial Model in that cluster.
	 *
	 * The clusters span the Simulation area.
	 * @since 1.2
	 */
	protected java.util.ArrayList clusters;
  
	 
	 /**
   * Constructor
   */
  public SpatialModel()
  {
    super("SpatialModel");
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Spatial Model extension";
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
    return 0;
  }

  /**
   * Visualizes a collection of elements. <br>
   * <br>
	 * <i>Version 1.5 by Jerome Haerri (haerri@ieee.org):
	 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Added new colors depending on the number of lanes and traffic lights
	 * <br>	&nbsp;&nbsp;&nbsp;&nbsp;						Rule: 
	 * <br>	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;						  - Multi-lane: CYAN
	 * <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;          - Red traffic light: RED
   * <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;					- Green traffic light: GREEN </i>
	 * <br>
   * @param g graphic context
   * @param collection collection of elements
   * @param color color
   */
  protected static void visualizeCollection(java.awt.Graphics g, java.util.Collection collection, java.awt.Color color)
  {
    java.util.Iterator iter = collection.iterator();
    while (iter.hasNext())
    {
      g.setColor(color);
        
      SpatialModelElement element = (SpatialModelElement)iter.next();
      GeometryElement geometry = element.getGeometry();
			
			/* JHNote (17/11/2005): Gets the Universe reference in order to have access to the traffic light extension.
			 * 											Then, we get access to the traffic light status (red, green) and use the appropriate color.
			*/
			Universe uni = Universe.getReference();
			if (uni == null) {
			  System.out.println("No instance of universe");
				System.exit(-1);
			}
			
			//JHNote (06/02/2006): The name of the TrafficLight extension is loaded at the beginning 
			//TrafficLight trafficLight = (TrafficLight)u.getExtension("TrafficLight");
			TrafficLight trafficLight = (TrafficLight)uni.getExtension(trafficLightName);
						
		//	if (trafficLight == null) {
		//	  System.out.println("No instance of TrafficLight");
		//		System.exit(-1);
		//	}
		
		  // check for roadElements
			 if (element.getClassCode().equals("41") && element.getSubClassCode().equals("10") ) {
			   String NL = (String)element.getAttributes().get("NL");
				 if (NL != null) {
				   int nl = Integer.parseInt(NL);
				   if (nl > 1) {
					   g.setColor(java.awt.Color.CYAN);
				   }
				 }
			 }
			 
			if (trafficLight != null) {
			java.util.Map trafficLights = trafficLight.getTrafficLights();
			java.util.Iterator trafficIterator = trafficLights.values().iterator();
			
			while (trafficIterator.hasNext()) {
				 TrafficLightElement trafficLightElement = (TrafficLightElement)trafficIterator.next();
				 if (trafficLightElement.getAdjacentRoadIDs().contains(element.getID())) { 
				   if(trafficLightElement.checkStatus(element.getID())) {
					   g.setColor(java.awt.Color.GREEN);
				   }
				   else {
					  g.setColor(java.awt.Color.RED);
				   }
				 }
			 }
			 }
			 
			 
      // call corresponding drawing routine
      if (geometry instanceof Polygon)
        visualizePolygon(g, (Polygon)geometry);
      else
      if (geometry instanceof Polyline)
        visualizePolyline(g, (Polyline)geometry);
      else
      if (geometry instanceof Point)
        visualizePoint(g, (Point)geometry);
		
		}
  }

  /**
   * Visualizes a point. <br>
   * <br>
   * @param g graphic context
   * @param point point
   */
  protected static void visualizePoint(java.awt.Graphics g, Point point)
  {
    g.drawLine((int)point.getX(), (int)point.getY(),
               (int)point.getX(), (int)point.getY());
  }

  /**
   * Visualizes a polyline. <br>
   * <br>
   * @param g graphic context
   * @param polyline polyline
   */
  protected static void visualizePolyline(java.awt.Graphics g, Polyline polyline)
  {
    
		int nPoints = polyline.getPoints().size();
    if (nPoints==0)
      return;

    int[] xPoints = new int[nPoints];
    int[] yPoints = new int[nPoints];

    // Copy the points' coordinates
    for (int i=0; i<nPoints; i++)
    {
      Point p = (Point)polyline.getPoints().get(i);

      xPoints[i] = (int)p.getX();
      yPoints[i] = (int)p.getY();
    }

    g.drawPolyline(xPoints, yPoints, nPoints);
  }

  /**
   * Visualizes a polygon. <br>
   * <br>
   * @param g graphic context
   * @param polygon polygon
   */
  protected static void visualizePolygon(java.awt.Graphics g, Polygon polygon)
  {
    int nPoints = polygon.getPoints().size();
    if (nPoints==0)
      return;
      
    int[] xPoints = new int[nPoints];
    int[] yPoints = new int[nPoints];

    // Copy the points' coordinates
    for (int i=0; i<nPoints; i++)
    {
      Point p = (Point)polygon.getPoints().get(i);

      xPoints[i] = (int)p.getX();
      yPoints[i] = (int)p.getY();
    }

    g.drawPolygon(xPoints, yPoints, nPoints);
  }

  /**
   * Draws the contents of Spatial Model in a given frame. <br>
   * <br>
	 * <i> Version 1.4 by Jerome Haerri (haerri@ieee.org) by 17/11/2005: 
	  * <br>&nbsp;&nbsp;&nbsp;&nbsp; 	Patched the Canu code such that we can
	 * 															display the spatialModel obtained from a 
	 *										  				user defined Graph (xml tag: Graph) </i>
   * @param g graphic context
   */
  public void visualize(java.awt.Graphics g)
  {
    // save old color
	java.awt.Color oldColor = g.getColor();
  	
    // possible element categories
    java.util.ArrayList administrativeAreas = new java.util.ArrayList();
    java.util.ArrayList namedAreas = new java.util.ArrayList();
    java.util.ArrayList roads = new java.util.ArrayList();
    java.util.ArrayList railways = new java.util.ArrayList();
    java.util.ArrayList waterways = new java.util.ArrayList();
    java.util.ArrayList buildings  = new java.util.ArrayList();
    java.util.ArrayList woodlands = new java.util.ArrayList();
    java.util.ArrayList parks = new java.util.ArrayList();
    java.util.ArrayList islands = new java.util.ArrayList();
    java.util.ArrayList services = new java.util.ArrayList();

    // sort the Spatial Model elements into categories
    java.util.Iterator iter = elements.values().iterator();
    while (iter.hasNext())
    {
      SpatialModelElement element = (SpatialModelElement)iter.next();
      if (element.getClassCode().equals("11"))
        administrativeAreas.add(element);
      else
      if (element.getClassCode().equals("31"))
        namedAreas.add(element);
      else
      if (element.getClassCode().equals("41"))
        roads.add(element);
      else
      if (element.getClassCode().equals("42"))
        railways.add(element);
      else
      if (element.getClassCode().equals("43"))
        waterways.add(element);
      else
      if (element.getClassCode().equals("71"))
      {
        if (element.getSubClassCode().equals("10"))
          buildings.add(element);
        else
        if (element.getSubClassCode().equals("20"))
          woodlands.add(element);
        else
        if (element.getSubClassCode().equals("70"))
          parks.add(element);
        else
        if (element.getSubClassCode().equals("80"))
          islands.add(element);
      }
      else
      if (element.getClassCode().equals("73"))
        services.add(element);
    }

    // display elements in given order
    visualizeCollection(g, administrativeAreas, java.awt.Color.LIGHT_GRAY);
    visualizeCollection(g, namedAreas, java.awt.Color.LIGHT_GRAY);
    visualizeCollection(g, islands, java.awt.Color.DARK_GRAY);
    visualizeCollection(g, woodlands, new java.awt.Color(128, 64, 64));
    visualizeCollection(g, parks, java.awt.Color.GREEN);
    visualizeCollection(g, railways, java.awt.Color.ORANGE);
    visualizeCollection(g, waterways, java.awt.Color.BLUE);
    visualizeCollection(g, buildings, new java.awt.Color(128, 128, 0)); 
    visualizeCollection(g, roads, java.awt.Color.BLACK);
    visualizeCollection(g, services, java.awt.Color.YELLOW);
    
		// JHNote (17/11/2005): Patched the Canu code such that we can
		// 											display the spatialModel obtained from a 
		//										  user defined Graph (xml tag: Graph)
		if (externalGraph && (elements.values().size() == 0) && (graph != null ) ) {
		  java.util.ArrayList edges = graph.getEdges();
			for (int i = 0; i < edges.size(); i++) {
			  Edge edge = (Edge)edges.get(i);
				Point from = new Point(edge.getV1().getX(), edge.getV1().getY());
				Point to = new Point(edge.getV2().getX(), edge.getV2().getY());
	
				Polyline poly = new Polyline();
				java.util.ArrayList points = poly.getPoints();
				
				points.add(from);
				points.add(to);
				
				visualizePolyline(g, poly);
			}
		}
    // restore old color
    g.setColor(oldColor);
  }

  /**
   * Gets Spatial Model elements
   * @return map map of Spatial Model elements
   */
  public java.util.Map getElements()
  {
    return elements;
  }

  /**
   * Gets a specific element from collection of Spatial Model Elements. <br>
   * <br>
   * @param id element ID
   * @return Spatial Model Element with given ID
   */
  public SpatialModelElement getElement(String id)
  {
    return (SpatialModelElement)elements.get(id);
  }

  /**
   * Gets the movement area's graph. <br>
   * <br>
   * @return movement area's graph
   */
  public Graph getGraph()
  {
    return graph;
  }
	
	/**
   * Gets the set of clusters within the movement area. <br>
   * <br>
	 * @param clusters set of Clusters
	 * @since 1.2
   */
  public void setClusters(java.util.ArrayList clusters)
  {
    this.clusters = clusters;
  }
	
	/**
   * Gets the set of clusters within the movement area. <br>
   * <br>
   * @return movement area's clusters
	 * @since 1.2
   */
  public java.util.ArrayList getClusters()
  {
    return clusters;
  }

  /**
   * Rebuilds the movement area's graph
   */
  public void rebuildGraph() throws Exception
  {
    java.util.Random rand=u.getRandom();
		
		//System.out.println("Rebuilding Graph");
		
		graph = new Graph(name+"_graph");
    verticesToJunctions = new java.util.HashMap();
    edgesParents = new java.util.HashMap();
    edgesCache = new java.util.HashMap();

    java.util.ArrayList elements_arr = new java.util.ArrayList(elements.values());

    // build a street network graph
    int v_id = 0;
    Vertex v1 = null;
    Edge edge = null;    
    for (int i=0; i<elements_arr.size(); i++)
    {
			SpatialModelElement element = (SpatialModelElement)elements_arr.get(i);
			
      // check if a road element
      if ( !element.getClassCode().equals("41") ||
           !element.getSubClassCode().equals("10") ||
           (element.getGeometry()==null) ||
           (element.getGeometry() instanceof Polygon) ||
           (element.getGeometry() instanceof Point) ||
           (element.getChildren().size()>0) )
        continue;
        
      // process polyline
      Polyline polyline = (Polyline)element.getGeometry();
      if (polyline.getPoints().size()<2)
        continue;

      // add initial point
      Point p = (Point)polyline.getPoints().get(0);
      Vertex v = graph.getVertex(p.getX(), p.getY());
      if (v==null)
      {
        v = graph.addVertex(""+v_id, "", Double.toString(p.getX()), Double.toString(p.getY()));
        v_id++;
      }

      // add intermediate points
      for (int j=1; j<polyline.getPoints().size()-1; j++)
      {
        p = (Point)polyline.getPoints().get(j);

        v1 = graph.getVertex(p.getX(), p.getY());
        if (v1==null)
        {
          v1 = graph.addVertex(""+v_id, "", Double.toString(p.getX()), Double.toString(p.getY()));
          v_id++;
        }

        edge = findEdge(v, v1);
        if (edge==null)
        {
          edge = graph.addEdge(v.getID(), v1.getID());
          edgesParents.put(edge, element);
          edgesCache.put(edge.getID1()+":"+edge.getID2(), edge);
        }

        v = v1;
      }

      // add last point
      p = (Point)polyline.getPoints().get(polyline.getPoints().size()-1);
      v1 = graph.getVertex(p.getX(), p.getY());
      if (v1==null)
      {
        v1 = graph.addVertex(""+v_id, "", Double.toString(p.getX()), Double.toString(p.getY()));
        v_id++;
      }

      edge = findEdge(v, v1);
      if (edge==null)
      {
        edge = graph.addEdge(v.getID(), v1.getID());
        edgesParents.put(edge, element);
        edgesCache.put(edge.getID1()+":"+edge.getID2(), edge);
      }
    }
    
    graph.getInfrastructureGraph().reorganize(false);

    u.setDimensionX((float)graph.getRightmostCoordinate());
    u.setDimensionY((float)graph.getUppermostCoordinate());
    
    // initialize vertex-to-junction mappings
    for (int i=0; i<elements_arr.size(); i++)
    {
      SpatialModelElement element = (SpatialModelElement)elements_arr.get(i);

      // check if a junction
      if ( !element.getClassCode().equals("41") ||
           !element.getSubClassCode().equals("20") ||
           (element.getGeometry()==null) ||
           (!(element.getGeometry() instanceof Point)) ||
           (element.getChildren().size()>0) )
        continue;
      
      Point p = (Point)element.getGeometry();
      v1 = graph.getVertex(p.getX(), p.getY());
      if (v1!=null)
      {
        verticesToJunctions.put(v1, element);
      }
    }
  }

  /**
   * Finds an edge between two points. <br>
   * <br>
   * @param v1 first vertex of the edge
   * @param v2 second vertex of the edge
   * @return the edge between vertices
   */
  public Edge findEdge(Vertex v1, Vertex v2)
  {
		  String key1 = ""+v1.getID()+":"+v2.getID();
      String key2 = ""+v2.getID()+":"+v1.getID();

      Edge res = (Edge)edgesCache.get(key1);
      if (res==null)
        res = (Edge)edgesCache.get(key2);
		
      return res;
  }
	
	/**
   * Finds the direction of an edge between two points. <br>
   *  0 if from v1 to v2
	 *	1 if from v2 to v1
	 *  -1 if no edge is found 
	 *<br>
   * @param v1 first vertex of the edge
   * @param v2 second vertex of the edge
   * @return direction of the edge
   */
  public int findDirEdge(Vertex v1, Vertex v2)
  {
		  String key1 = ""+v1.getID()+":"+v2.getID();
      String key2 = ""+v2.getID()+":"+v1.getID();

      Edge res = (Edge)edgesCache.get(key1);
			if (res !=null)
				return 0;
			else {
				res = (Edge)edgesCache.get(key2);
				if (res !=null)
					return 1; 
			}
			return -1;
  }
	
  /**
   * Maps the edge to a corresponding Spatial Model element.<br>
   * <br>
   * @param edge edge
   * @return associated Spatial Model element
   */
  public SpatialModelElement mapEdgeToElement(Edge edge)
  {
    return (SpatialModelElement)edgesParents.get(edge);
  }

  /**
   * Maps the vertex to a corresponding junction element.<br>
   * <br>
   * @param vertex vertex
   * @return associated junction element
   */
  public SpatialModelElement mapVertexToJunction(Vertex vertex)
  {
    return (SpatialModelElement)verticesToJunctions.get(vertex);
  }

  /**
   * Determines if the movement on the edge is prohibited in the given direction. <br>
   * <br>
   * @param vs source vertex of the edge
   * @param vd destination vertex of the edge
   * @return true if the movement is prohibited
   */
  public boolean isMovementProhibited(Vertex vs, Vertex vd)
  {
    boolean res = false;
    Edge edge = findEdge(vs, vd);
		
    SpatialModelElement parent = mapEdgeToElement(edge);
    if (parent!=null)
    {
      String d_s = (String)parent.getAttributes().get("DF");
      if (d_s!=null)
      {
        int d = Integer.parseInt(d_s);
				//System.out.println("d is  "+d);
        if ( ((edge.getV1()==vs)&&(edge.getV2()==vd)&&((d==2)||(d==4)))
            ||((edge.getV1()==vd)&&(edge.getV2()==vs)&&((d==3)||(d==4))))
        {
          // movement is prohibited
          res = true;
        }
      }
    }
		//System.out.println("Movement between Vertex " + vs.getID() + " and Vertex " + vd.getID() + " is " + res);
    return res;
  }
	
	/**
   * Determines if the movement on the edge is prohibited in the given direction. <br>
   * <br>
   * @param es source edge
	 * @param ed destination edge
   * @param v  vertex connecting source and destination edges
	 * @param nd vehicle
   * @return true if the movement is prohibited
	 * @since 1.3
   */
  public boolean isMovementProhibited(Edge es , Vertex v, Edge ed, Node nd) {
    boolean res = false;

    SpatialModelElement roadSource = mapEdgeToElement(es);
		SpatialModelElement roadDest = mapEdgeToElement(ed);
		SpatialModelElement junction = mapVertexToJunction(v);
		
		String prohibited_code = "2102";
		
    if ((roadSource!=null) && (roadSource!=null) && (roadSource!=null)) {
      
			java.util.ArrayList relationships = roadSource.getRelations();
			for (int i = 0; i < relationships.size(); i++) {
				RelationshipRecord record = (RelationshipRecord)relationships.get(i);
				if (record ==null) {
					continue;
				}
				if (record.getCode() == prohibited_code) {
				  SpatialModelElement element = (SpatialModelElement)elements.get(record.getFeatures().get(0));
					if (element == null) {
						continue;
					}
					if(roadSource.getID() == element.getID()) {
						  SpatialModelElement element2 = (SpatialModelElement)elements.get(record.getFeatures().get(1));
							if (element2 == null) {
							  continue;
							}
							if(junction.getID() == element2.getID()) {
								  // means that the prohibited manoeuvre if from the same road, through the same junction
									SpatialModelElement element3 = (SpatialModelElement)elements.get(record.getFeatures().get(3));
									if (element3 == null) {
									  continue;
									}
									if(roadDest.getID() == element3.getID()) {
										// now we check if the relation applies to the type of vehicle. The forth feature is the traffic sign ID
										// and we obtain the Spatial Element, then its attributes.
										SpatialModelElement signElement = (SpatialModelElement)elements.get(record.getFeatures().get(4));
										if (signElement == null) {
											return true;
										}
										else {
										  String attributeSign = (String)signElement.getAttributes().get("VT");
										  String attributeCar = (String)nd.getAttributes().get("VT");
										  if (attributeSign == attributeCar) {
												return true;
											}
										}
									}
								}
						}
				}
			}
		}
		//System.out.println("isMovementProhibited(......) returned "+ res);
    return res;
  }
	
	
	 /**
   * Clips the Element. <br>
   * <br>
   * @param elem element to clip
   */
  public void clip(SpatialModelElement elem)
  {
		java.util.Random rand=u.getRandom();
		//java.util.Map elements = spatialModel.getElements();
		
		java.util.ArrayList points = ((Polyline)elem.getGeometry()).getPoints();
    java.util.ArrayList resPoints = new java.util.ArrayList();
		
    // iterate all points
    boolean flag = true;
    for (int i=0; i<points.size(); i++)
    {
      Point point = (Point)points.get(i);
      if (clipArea.contains(point))
      {
        if (flag)
          resPoints.add(point);
        else
        {
          // do clipping
          Line tempLine = new Line((Point)points.get(i-1), point);

          // add clipping point
          for (int j=0; j<clipArea.getPoints().size(); j++)
          {
            Line boundLine = new Line((Point)clipArea.getPoints().get(j),(Point)clipArea.getPoints().get((j+1)%clipArea.getPoints().size()));
            Point p = tempLine.intersect(boundLine);
            if (p!=null) {
              String pointID = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							String xyzID = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							p.setIDs(pointID,xyzID);
							resPoints.add(p);
							bounderyPoints.add(p);
							
							// JHNote (11/02/2006): Corrected a bug here: If a Edge point is out of the boundaries
							//											createLineFeatureElement will discard the spatialModelElement
							//											Junction. But, here, we are adding a boudary point, which will
							//											not be related to any spatialElement.
							String newId_junction = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							// adding a new Junction element
							//System.out.println("1 : Adding a boundary point with pos X " + p.getX() + " Y " + p.getY());
							
							SpatialModelElement newJunction = new SpatialModelElement(newId_junction, "41", "20", p);
							newJunction.getAttributes().put("JT","1"); // junction type : mini roundabout
							// clip
							if (clipArea!=null) {
								if (clipArea.contains(p)) {
									elements.put(newId_junction, newJunction);
									//System.out.println("1: clipping it");
								}
						
							}
							else
								elements.put(newId_junction, newJunction);
							
							
              break;
            }
						if (boundLine.contains(point)) {
							System.out.println("Adding boundary point");
						  bounderyPoints.add(point);
						}
						if (boundLine.contains((Point)points.get(i-1))) {
							System.out.println("Adding boundary point");
						  bounderyPoints.add((Point)points.get(i-1));
						}
          }

          // add point
          resPoints.add(point);
        }

        flag = true;
      }
      else
      {
        if ((flag)&&(i>0))
        {
          // do clipping
          Line tempLine = new Line((Point)points.get(i-1), point);

          // add clipping point
          for (int j=0; j<clipArea.getPoints().size(); j++)
          {
            Line boundLine = new Line((Point)clipArea.getPoints().get(j),
              (Point)clipArea.getPoints().get((j+1)%clipArea.getPoints().size()));
            Point p = tempLine.intersect(boundLine);
            if (p!=null) {
              String pointID = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							String xyzID = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							p.setIDs(pointID,xyzID);
							resPoints.add(p);
							bounderyPoints.add(p);
							
							// JHNote (11/02/2006): Corrected a bug here: If a Edge point is out of the boundaries
							//											createLineFeatureElement will discard the spatialModelElement
							//											Junction. But, here, we are adding a boudary point, which will
							//											not be related to any spatialElement.
							String newId_junction = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							// adding a new Junction element
							//System.out.println("2: Adding a boundary point with pos X " + p.getX() + " Y " + p.getY());
							SpatialModelElement newJunction = new SpatialModelElement(newId_junction, "41", "20", p);
							newJunction.getAttributes().put("JT","1"); // junction type : mini roundabout
							// clip
							if (clipArea!=null) {
								if (clipArea.contains(p)) {
									elements.put(newId_junction, newJunction);
									//System.out.println("2: clipping it");
								}
						
							}
							else
								elements.put(newId_junction, newJunction);
							
              break;
            }
						if (boundLine.contains(point)) {
							System.out.println("Adding boundary point");
						  bounderyPoints.add(point);
						}
						if (boundLine.contains((Point)points.get(i-1))) {
							System.out.println("Adding boundary point");
						  bounderyPoints.add((Point)points.get(i-1));
						}
          }
        }

        flag = false;
      }
    }

    points.clear();
    points.addAll(resPoints);
  }
	
	/**
    * Returns the X-Y dimensions of the clipping area.  <br>
    * <br>
		* 
		*	@throws Exception Exception is the clipping are contains less than 4 points
		* @return double[] an array containing the X-Y dimension of the clipping area.
    */
	public double[] getClipAreaXY() throws Exception {
		java.util.ArrayList points = clipArea.getPoints();
		double clipAreaX,clipAreaY; 
		if (points.size() == 4) {
				Point point1 = (Point)points.get(0);
				Point point2 = (Point)points.get(1);
				Point point3 = (Point)points.get(2);
				Point point4 = (Point)points.get(3);
			
				clipAreaX = point2.getX() - point1.getX();
				clipAreaY = point3.getY() - point1.getY();
		}
		else
			throw new Exception("Clipping Area does not contains at least 4 points");
		
		double[] clipAreaXY = new double[2];
		clipAreaXY[0] = clipAreaX;
		clipAreaXY[1] = clipAreaY;
		return clipAreaXY;
	}
	
	
	/**
		* Constructs elements from loaded obstacle features <br>
    * <br>
    * @throws Exception Exception if more than 2 coordinates per obstacle
		*/
  protected void createTrafficSigns() throws Exception {
		int a,b;
		Double p,q;
		try {
				//java.util.Map elements = spatialModel.getElements();
				java.util.Random rand=u.getRandom();
				
				
				// JHNote (10/11/2005): Now we also add traffic signs and traffic lights
				String trafficSignClass_code = "72";
				String trafficSignSubclass_code = "20";
				String trafficLightClass_code = "72";
				String trafficLightSubclass_code = "30";
				
				// JHNote (10/11/2005): Now we also add relationships between traffic signs and traffic lights
				String trafficSignPlusRelation_code = "2301";
				String trafficSignMinusRelation_code = "2302";
				String trafficLightPlusRelation_code = "2303";
				String trafficLightMinusRelation_code = "2304";
				
				// now adding traffic signs
				// As a first step, we only add stop signs on each side of a road element.
				// then, when building complex features, we will replace some of the stop signs by traffic lights
				
				java.util.ArrayList tmpElements = new java.util.ArrayList(elements.values());
				for (int i=0; i<tmpElements.size(); i++) {
				  SpatialModelElement tmpElement = (SpatialModelElement)tmpElements.get(i);
			 
					// check for roadElements
					if ( !tmpElement.getClassCode().equals("41") || !tmpElement.getSubClassCode().equals("10") )
					 continue;
					
					Polyline shape = (Polyline)tmpElement.getGeometry();
					java.util.ArrayList points = shape.getPoints();
				
					Point pointf = (Point)points.get(0); // initial point
					Point pointt = (Point)points.get(points.size()-1); // end point. As for GDF, "Plus" means from pointf to pointt and "Minus" the reverse.
					
					String id1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					String id2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					Point trafficSignPlus = new Point(pointt.getPosition());
					Point trafficSignMinus = new Point(pointf.getPosition());
					
					if (Integer.parseInt((String)tmpElement.getAttributes().get("DF")) == 3) {
					  SpatialModelElement trafficSignElementPlus = new SpatialModelElement(id1, trafficSignClass_code, trafficSignSubclass_code, trafficSignPlus);
					
					  // adding attributes
					  trafficSignElementPlus.getAttributes().put("TS","50"); // adding a right of way traffic sign
					  trafficSignElementPlus.getAttributes().put("50","16"); // adding a stop traffic sign as the right of way
					  trafficSignElementPlus.getAttributes().put("SY","0"); // sign valid for all traffic
					
					  // adding relations
					  int index = 0;
						String relationID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					  RelationshipRecord relation1 = new RelationshipRecord(relationID1, trafficSignPlusRelation_code);
					  relation1.getFeatures().add(index,trafficSignElementPlus.getID());
						relation1.getCat().add(index,"1");
					  index++;
					  relation1.getFeatures().add(index,tmpElement.getID());
						relation1.getCat().add(index,"2");
					
					  // adding the relationship to the roadElement
					  tmpElement.getRelations().add(relation1);
	
					  // also adding the relationship to the traffic sign element
					  trafficSignElementPlus.getRelations().add(relation1);
					
					
						// now clipping
					  if (clipArea!=null) {
						  if (clipArea.contains(trafficSignPlus))
						  	elements.put(id1, trafficSignElementPlus);
					  }
					  else
						  elements.put(id1, trafficSignElementPlus);
					
					}
					
					else if (Integer.parseInt((String)tmpElement.getAttributes().get("DF")) == 2) {
					
					  SpatialModelElement trafficSignElementMinus = new SpatialModelElement(id2, trafficSignClass_code, trafficSignSubclass_code, trafficSignMinus);
					
					  // adding attributes
					  trafficSignElementMinus.getAttributes().put("TS","50"); // adding a right of way traffic sign
					  trafficSignElementMinus.getAttributes().put("50","16"); // adding a stop traffic sign as the right of way
					  trafficSignElementMinus.getAttributes().put("SY","0"); // sign valid for all traffic
					
					  // adding relations
					   int index = 0;
						 String relationID2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					  RelationshipRecord relation2 = new RelationshipRecord(relationID2, trafficSignMinusRelation_code);
					  relation2.getFeatures().add(index,trafficSignElementMinus.getID());
						relation2.getCat().add(index,"1");
					  index++;
					  relation2.getFeatures().add(index,tmpElement.getID());
						relation2.getCat().add(index,"2");
					
					  // adding the relationship to the roadElement
					  tmpElement.getRelations().add(relation2);
					
					  // also adding the relationship to the traffic sign element
					  trafficSignElementMinus.getRelations().add(relation2);
					
					  if (clipArea!=null) {
					  	if (clipArea.contains(trafficSignMinus))
						  	elements.put(id2, trafficSignElementMinus);
					  }
					  else
						  elements.put(id2, trafficSignElementMinus);
					
					 }
					 else if (Integer.parseInt((String)tmpElement.getAttributes().get("DF")) == 1) {
							
							SpatialModelElement trafficSignElementPlus = new SpatialModelElement(id1, trafficSignClass_code, trafficSignSubclass_code, trafficSignPlus);
					
							// adding attributes
							trafficSignElementPlus.getAttributes().put("TS","50"); // adding a right of way traffic sign
							trafficSignElementPlus.getAttributes().put("50","16"); // adding a stop traffic sign as the right of way
							trafficSignElementPlus.getAttributes().put("SY","0"); // sign valid for all traffic
					
							// adding relations
							int index = 0;
							String relationID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							RelationshipRecord relation1 = new RelationshipRecord(relationID1, trafficSignPlusRelation_code);
							relation1.getFeatures().add(index,trafficSignElementPlus.getID());
							relation1.getCat().add(index,"1");
							index++;
							relation1.getFeatures().add(index,tmpElement.getID());
							relation1.getCat().add(index,"2");
					
							// adding the relationship to the roadElement
							tmpElement.getRelations().add(relation1);
					
							// also adding the relationship to the traffic sign element
							trafficSignElementPlus.getRelations().add(relation1);
					
					
							// now clipping
							if (clipArea!=null) {
								if (clipArea.contains(trafficSignPlus))
									elements.put(id1, trafficSignElementPlus);
							}
							else
								elements.put(id1, trafficSignElementPlus);
					
							SpatialModelElement trafficSignElementMinus = new SpatialModelElement(id2, trafficSignClass_code, trafficSignSubclass_code, trafficSignMinus);
					
							// adding attributes
							trafficSignElementMinus.getAttributes().put("TS","50"); // adding a right of way traffic sign
							trafficSignElementMinus.getAttributes().put("50","16"); // adding a stop traffic sign as the right of way
							trafficSignElementMinus.getAttributes().put("SY","0"); // sign valid for all traffic
					
							// adding relations
							index = 0;
							String relationID2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							RelationshipRecord relation2 = new RelationshipRecord(relationID2, trafficSignMinusRelation_code);
							relation2.getFeatures().add(index,trafficSignElementMinus.getID());
							relation2.getCat().add(index,"1");
							index++;
							relation2.getFeatures().add(index,tmpElement.getID());
							relation2.getCat().add(index,"2");
					
							// adding the relationship to the roadElement
							tmpElement.getRelations().add(relation2);
					
							// also adding the relationship to the traffic sign element
							trafficSignElementMinus.getRelations().add(relation2);
					
							if (clipArea!=null) {
								if (clipArea.contains(trafficSignMinus))
									elements.put(id2, trafficSignElementMinus);
							}
							else
								elements.put(id2, trafficSignElementMinus);
							
					  }
						else {
						  System.out.println("Non correct attribute DF value "+(String)tmpElement.getAttributes().get("DF"));
						}
				}
		}
		catch (Exception e2) {
				System.out.println("Error in createPointFeatureElements");
				e2.printStackTrace();
			}
		}
	
	
	/**
   * Constructs elements from loaded complex features
   */
 	 protected void createComplexFeatureElements() {
		 
		int nb_traffic_light = 0;
   
		java.util.Random rand=u.getRandom();
		
		// we add traffic lights
		
		// JHNote (10/11/2005): Now we also add traffic signs and traffic lights
		String trafficLightClass_code = "72";
		String trafficLightSubclass_code = "30";
				
		// JHNote (10/11/2005): Now we also add relationships between traffic signs and traffic lights
		String trafficLightPlusRelation_code = "2303";
		String trafficLightMinusRelation_code = "2304";
		String trafficSignPlusRelation_code = "2301";
		String trafficSignMinusRelation_code = "2302";
				
		// JHNote (15/11/2005) : first, a single traffic light
		if (maxTrafficLight <= 0)
				return;
		
		java.util.ArrayList tmpElements = new java.util.ArrayList(elements.values());
		for (int i=0; i<tmpElements.size(); i++) {
			SpatialModelElement tmpElement = (SpatialModelElement)tmpElements.get(i);
			
			// check for junctions
			if ( !tmpElement.getClassCode().equals("41") || !tmpElement.getSubClassCode().equals("20") )
			 continue;
			
			Point junctionPoint = (Point)tmpElement.getGeometry();
			Vertex vs = graph.getVertex(junctionPoint.getX(), junctionPoint.getY());
		
      // |MF| avoid traffic lights on border junctions
      if(vs.getX() == min_x_clip || vs.getX() == max_x_clip ||
         vs.getY() == min_y_clip || vs.getY() == max_y_clip)
        continue;

 			java.util.ArrayList neighboringVertices = vs.getNeighbours();
			
			java.util.Iterator iter = neighboringVertices.iterator();
			
			boolean plusDirection = false;
			
			while (iter.hasNext()) {
				Vertex vd = (Vertex)iter.next();
				Edge edge = findEdge(vs, vd);
				SpatialModelElement roadElement = mapEdgeToElement(edge);
				
				
				Polyline shape = (Polyline)roadElement.getGeometry();
				java.util.ArrayList points = shape.getPoints();
			
				Point pointf = (Point)points.get(0); // initial point
				Point pointt = (Point)points.get(points.size()-1); // end point. As for GDF, "Plus" means from pointf to pointt and "Minus" the reverse.
				
				// plusDirection only indicates the direction we are interested. Here, we are interested on incomming direction. So first, we determine
				// the incoming direction, then if this direction is open (DF).
				
				if(junctionPoint.contains(pointf))
					plusDirection = false;
				else
					plusDirection = true;
				
				// first we remove the relation with the traffic sign and the traffic sign itself
				java.util.ArrayList relationships = roadElement.getRelations();
				for (int j = 0; j < relationships.size(); j++) {
					RelationshipRecord record = (RelationshipRecord)relationships.get(j);
					if (record ==null){
						continue;
					}
					if (((record.getCode() == trafficSignPlusRelation_code) && plusDirection) 
							|| ((record.getCode() == trafficSignMinusRelation_code) && !plusDirection) )   {
						
						// we remove the traffic sign to replace if by a traffic light.
						// if we have a single flow road, if we are on a reverse flow, we should
						relationships.remove(j);
						elements.remove(record.getFeatures().get(0));
					}
				}
				boolean incoming = true;
				String d_s = (String)roadElement.getAttributes().get("DF");
				if ((!plusDirection && (d_s == "3")) || (plusDirection && (d_s == "2")) ||(d_s == "4")) {
				  incoming = false;
				}
				
				// Now, depending on the driving direction, we add a traffic light element and its relation with the roadElement
				if (incoming) {
					if (plusDirection) {
						String id1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					
						Point trafficLightPlus = new Point(pointt.getPosition());
					
					
						SpatialModelElement trafficLightElementPlus = new SpatialModelElement(id1, trafficLightClass_code, trafficLightSubclass_code, trafficLightPlus);
					
						// adding attributes
						trafficLightElementPlus.getAttributes().put("SY","0"); // sign valid for all traffic
					
						// creating relations
						int index = 0;
						String relationID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
						RelationshipRecord relation1 = new RelationshipRecord(relationID1,trafficLightPlusRelation_code);
						relation1.getFeatures().add(index,trafficLightElementPlus.getID());
						relation1.getCat().add(index,"1");
						index++;
						relation1.getFeatures().add(index,roadElement.getID());
						relation1.getCat().add(index,"2");
						
						// adding relations
						//System.out.println("Added a plus Relation with code " + relation1.getCode());
						roadElement.getRelations().add(relation1);
						trafficLightElementPlus.getRelations().add(relation1);
					
						if (clipArea!=null) {
							if (clipArea.contains(trafficLightPlus))
								elements.put(id1, trafficLightElementPlus);
						}
						else
							elements.put(id1, trafficLightElementPlus);
					
					}
					else {
						String id2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					
						Point trafficLightMinus = new Point(pointf.getPosition());
				
						SpatialModelElement trafficLightElementMinus = new SpatialModelElement(id2, trafficLightClass_code, trafficLightSubclass_code, trafficLightMinus);
					
						// adding attributes
						trafficLightElementMinus.getAttributes().put("SY","0"); // sign valid for all traffic
					
						// creating relations
						int index = 0;
						String relationID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
						RelationshipRecord relation1 = new RelationshipRecord(relationID1,trafficLightMinusRelation_code);
						relation1.getFeatures().add(index,trafficLightElementMinus.getID());
						relation1.getCat().add(index,"1");
						index++;
						relation1.getFeatures().add(index,roadElement.getID());
						relation1.getCat().add(index,"2");
					
						//adding relations
						//System.out.println("Added a minus Relation");
						roadElement.getRelations().add(relation1);
						trafficLightElementMinus.getRelations().add(relation1);
					
						if (clipArea!=null) {
							if (clipArea.contains(trafficLightMinus))
								elements.put(id2, trafficLightElementMinus);
						}
						else
							elements.put(id2, trafficLightElementMinus);
					}
				}
			}
			nb_traffic_light++;
			if (nb_traffic_light >= maxTrafficLight)
				break;
	  }
  }

	public void setDoubleFlow(boolean value) {
	  doubleFlow = value;
	}
	
	public void setTrafficLightName(String name) {
	  trafficLightName=name;
	}
	
	/**
	* Adds multiple lanes per roadElement. Depending on the configuration, it may
	* add multiple lanes on a complete street (from one border to another) or
	* on all roadElement. It is also able to focus on a single directional flow. <br>
	*/
	protected void createMultilaneRoads() {
		
			java.util.ArrayList tmpElements = new java.util.ArrayList(elements.values());
			int reflect = 0;
			if (doubleFlow)
				reflect = 1;
			
	  	
			String numberLane_class_code = "NL";
			

			de.uni_stuttgart.informatik.canu.tripmodel.pathalgorithms.Dijkstra algo = new de.uni_stuttgart.informatik.canu.tripmodel.pathalgorithms.Dijkstra();
			
			if (!allRoads) {
				Node tmpNode = new Node();
				String laneNumberString = String.valueOf(numberLane);	
				
				java.util.Random rand=u.getRandom();
				java.util.ArrayList tmpBoundaryPoints = new java.util.ArrayList();
				
				tmpBoundaryPoints.addAll(bounderyPoints);
			
				//int maxNumberMultilane = 4;
			
				int randIterator = 0;
				for (int i = 0; i < maxNumberMultilane; i++) {
					if (tmpBoundaryPoints.size() - 2 < 0)
						break;
					
					randIterator = rand.nextInt(tmpBoundaryPoints.size());
					Point boundaryPoint1 = (Point)tmpBoundaryPoints.remove(randIterator);
					randIterator = rand.nextInt(tmpBoundaryPoints.size());
					Point boundaryPoint2 = (Point)tmpBoundaryPoints.remove(randIterator);
					
					de.uni_stuttgart.informatik.canu.tripmodel.core.Trip trip = algo.getPath(this, null , boundaryPoint1, boundaryPoint2,reflect);
					
					if (trip == null)
						continue;
					
					java.util.ArrayList path = trip.getPath();
					Point init = (Point)path.get(0);
					for (int j=1; j<path.size(); j++) {
						Point next = (Point)path.get(j);
						Vertex vertex1 = graph.getVertex(init.getX(),init.getY()); 
						Vertex vertex2 = graph.getVertex(next.getX(),next.getY());
						init = next;
					
						if ((vertex1 ==null) ||(vertex2 ==null))
							System.out.println("vertices null");
					
						Edge edge = findEdge(vertex1, vertex2);
					
					
						if (edge == null)
							System.out.println("edge null");
					
						SpatialModelElement edgeElement = mapEdgeToElement(edge);
					
						if (edgeElement == null)
							System.out.println("edgeElement null");
					
						edgeElement.getAttributes().put("NL",laneNumberString);	
					}
					if (reflect == 1 && directional) {
					//	if (reflect == 1) {
					  // we need to find the reverse way since we have different lanes
						trip = algo.getPath(this, null , boundaryPoint2, boundaryPoint1,reflect);
						
						if (trip == null)
						  continue;
						
						path = trip.getPath();
						init = (Point)path.get(0);
						for (int j=1; j<path.size(); j++) {
							Point next = (Point)path.get(j);
							Vertex vertex1 = graph.getVertex(init.getX(),init.getY()); 
							Vertex vertex2 = graph.getVertex(next.getX(),next.getY());
							init = next;
					
							if ((vertex1 ==null) ||(vertex2 ==null))
								System.out.println("vertices null");
					
							Edge edge = findEdge(vertex1, vertex2);
					
					
							if (edge == null)
								System.out.println("edge null");
					
							SpatialModelElement edgeElement = mapEdgeToElement(edge);
					
							if (edgeElement == null)
								System.out.println("edgeElement null");
					
							edgeElement.getAttributes().put("NL",laneNumberString);	
						}	
					}
				}
			}
			else {
				for (int i=0; i<tmpElements.size(); i++) {
				  	SpatialModelElement tmpElement = (SpatialModelElement)tmpElements.get(i);
						// check for roadElements
						if ( !tmpElement.getClassCode().equals("41") || !tmpElement.getSubClassCode().equals("10") )
							continue;
						String laneNumberString = String.valueOf(numberLane);	
						tmpElement.getAttributes().put("NL",laneNumberString);	
				}
			}
		}
	
		/**
		* Creates differentiated traffic flows. Physically separates each roadElement for each directional flow. <br>
		*/
		public void createDoubleFlowRoads() {
				String class_code = "41";
				String subclass_code = "10";
				
				java.util.Random rand=u.getRandom();
			  java.util.ArrayList tmpElements = new java.util.ArrayList(elements.values());
				for (int i=0; i<tmpElements.size(); i++) {
				  SpatialModelElement tmpElement = (SpatialModelElement)tmpElements.get(i);
					// check for roadElements
					if ( !tmpElement.getClassCode().equals("41") || !tmpElement.getSubClassCode().equals("10") )
					 continue;
					
					
					java.util.Map tmpAttributes = tmpElement.getAttributes();
					//keeping record of the roadType
					
					Polyline shape = (Polyline)tmpElement.getGeometry();
					java.util.ArrayList points = shape.getPoints();
					if (points.size() <= 1)
						continue;
					
					String id = tmpElement.getID();
					
					// removing this element from the set of all elements
					elements.remove(id);					
					//////////////////////////////////////
					// |MF| new road split calculations //
					//////////////////////////////////////
					
					// define constant distances for road split
					// d: on-road distance from vertex (0 to D)
					// s: ortogonal distance from road (D to S)
					//
					//     d
					//  +----+
					//  |    |
					//  O====D========== ... ==1
					//   \   |
					//    \  | s
					//     \ |
					//      \|__________
					//       S
					//
					double d = 20.0; // in meters
					double s = 7.0;  // in meters
					
					// get init and last points coords
					double initX = ((Point)points.get(0)).getX();
					double initY = ((Point)points.get(0)).getY();
					double lastX = ((Point)points.get(1)).getX();
					double lastY = ((Point)points.get(1)).getY();
					
					// compute distances along axes
					double deltaX = Math.abs(initX-lastX);
					double deltaY = Math.abs(initY-lastY);
					
					// compute road inclination angle
					double theta = Math.PI/2;
					if(deltaY != 0) theta = Math.atan(deltaX/deltaY);
					
					// value of d is overridden to 10% of road length
					d = 0.1 * Math.sqrt(Math.pow(deltaX,2) + Math.pow(deltaY,2));
					
					// compute relative coords of D wrt 0
					double dX = d * Math.sin(theta);
					double dY = d * Math.cos(theta);
					
					// compute relative coords S wrt D
					double sX = s * Math.sin(Math.PI/2 - theta);
					double sY = s * Math.cos(Math.PI/2 - theta);
					
					// compute absolute D and S coords, according
					// to one of the possible orientation cases
					//   D_0  :  on-road distance wrt init point
					//   D_1  :  on-road distance wrt last point
					//   S_Xd :  ortogonal distance from D_X on 0 to 1 flow
					//   S_Xr :  ortogonal distance from D_X on 1 to 0 flow
					Point D_0, D_1, S_0d, S_0r, S_1d, S_1r;
					
					
					if(initX > lastX) {
						if(initY > lastY) {
							/////////////
							//     0   //
							//    /    //
							//   1     //
							/////////////
							D_0  = new Point(initX-dX, initY-dY);
							D_1  = new Point(lastX+dX, lastY+dY);
							S_0d = new Point(D_0.getX()-sX, D_0.getY()+sY);
							S_0r = new Point(D_0.getX()+sX, D_0.getY()-sY);
							S_1d = new Point(D_1.getX()-sX, D_1.getY()+sY);
							S_1r = new Point(D_1.getX()+sX, D_1.getY()-sY);
						}
						else {
							/////////////////////////
							//   1                 //
							//    \    ,   1 - 0   //
							//     0               //
							/////////////////////////
							D_0  = new Point(initX-dX, initY+dY);
							D_1  = new Point(lastX+dX, lastY-dY);
							S_0d = new Point(D_0.getX()+sX, D_0.getY()+sY);
							S_0r = new Point(D_0.getX()-sX, D_0.getY()-sY);
							S_1d = new Point(D_1.getX()+sX, D_1.getY()+sY);
							S_1r = new Point(D_1.getX()-sX, D_1.getY()-sY);
						}
					}
					else {
						if(initY > lastY) {
							//////////////////////
							//   0          0   //
							//    \    ,    |   //
							//     1        1   //
							//////////////////////
							D_0  = new Point(initX+dX, initY-dY);
							D_1  = new Point(lastX-dX, lastY+dY);
							S_0d = new Point(D_0.getX()-sX, D_0.getY()-sY);
							S_0r = new Point(D_0.getX()+sX, D_0.getY()+sY);
							S_1d = new Point(D_1.getX()-sX, D_1.getY()-sY);
							S_1r = new Point(D_1.getX()+sX, D_1.getY()+sY);
						}
						else {
							/////////////////////////////////
							//     1       1               //
							//    /    ,   |   ,   0 - 1   //
							//   0         0               //
							/////////////////////////////////
							D_0  = new Point(initX+dX, initY+dY);
							D_1  = new Point(lastX-dX, lastY-dY);
							S_0d = new Point(D_0.getX()+sX, D_0.getY()-sY);
							S_0r = new Point(D_0.getX()-sX, D_0.getY()+sY);
							S_1d = new Point(D_1.getX()+sX, D_1.getY()-sY);
							S_1r = new Point(D_1.getX()-sX, D_1.getY()+sY);
						}
					}
					
					// set-up 0 to 1 direction and add it to the graph
					
					String pointID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					String xyzID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					S_0d.setIDs(pointID1,xyzID1);
					
					String pointID2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					String xyzID2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					S_1d.setIDs(pointID2,xyzID2);
					
					Polyline dirTrunc = new Polyline();
					java.util.ArrayList dirPoints = dirTrunc.getPoints();
					dirPoints.add((Point)points.get(0));
					dirPoints.add(S_0d);
					dirPoints.add(S_1d);
					dirPoints.add((Point)points.get(1));
					SpatialModelElement dirElement = new SpatialModelElement(id, class_code, subclass_code, dirTrunc);
					dirElement.getAttributes().putAll(tmpAttributes);
					dirElement.getAttributes().remove("DF");
					dirElement.getAttributes().put("DF","3"); // directional flow : 2 = from last to init point

					// set-up 1 to 0 direction and add it to the graph
          String pointID3 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					String xyzID3 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					S_0r.setIDs(pointID3,xyzID3);
					
					String pointID4 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					String xyzID4 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					S_1r.setIDs(pointID4,xyzID4);
					
				  Polyline revTrunc = new Polyline();
					java.util.ArrayList revPoints = revTrunc.getPoints();
					String revId = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					revPoints.add((Point)points.get(0));
					revPoints.add(S_0r);
					revPoints.add(S_1r);
					revPoints.add((Point)points.get(1));
					SpatialModelElement revElement = new SpatialModelElement(revId, class_code, subclass_code, revTrunc);
					revElement.getAttributes().putAll(tmpAttributes);
					revElement.getAttributes().remove("DF");
					revElement.getAttributes().put("DF","2"); // directional flow : 2 = from init to last point
                                                                                
						// clip
				  if (clipArea!=null) {
						clip(dirElement);
						if (dirTrunc.getPoints().size()>0)
							elements.put(id, dirElement);
					}
					else
						elements.put(id, dirElement);
					
					
					
					// adding the reverse road
					if (clipArea!=null) {
						clip(revElement);
						if (revTrunc.getPoints().size()>0)
							elements.put(revId, revElement);
					}
					else
						elements.put(revId, revElement);
			}
		
		
		}
		
	 /**
   * Constructs complex elements from random data
	 * This method creates intersections and roads.
	 * It also creates complex traffic signs, such as
	 * movement restrictions.
	 * It finally create advance features for roads, such
	 * as multilanes, or higher class roads.
   */
  public void createSecondLayerElements() {
    try {
			createTrafficSigns();
			
			if (numberLane > 1) {
				createMultilaneRoads();
			}
			
			TrafficLight trafficLight = (TrafficLight)u.getExtension(trafficLightName);
			if (trafficLight != null) {
				createComplexFeatureElements();
			}
		}
		catch (Exception e2) {
				System.out.println("Error in createSecondLayerElements");
				e2.printStackTrace();
				System.exit(-1);
			}
  }
	
	/**
   * Initializes simulation parameters from XML tag. <br>
   * <br>
	 * <i> Version 1.4 by Jerome Haerri (haerri@ieee.org):
	 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Used in order to load a user defined graph.</i>
	 *
   * @throws Exception Exception if parameters are invalid
   */
  public void load(org.w3c.dom.Element element) throws Exception {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading SpatialModel extension"));
    
    super.load(element);
		
		java.util.Random rand=u.getRandom();
		
		String s;
		
		s = element.getAttribute("traffic_light");
    if (s.length()>0) {
      trafficLightName = s;
    }
		
		else {
			trafficLightName = "TrafficLight";
		}
		
		if(trafficLightName==null) {
			throw new Exception("A SpatialModel is missing!");
		}
		
		s = element.getAttribute("min_x");
    if(s.length()>0) 
      min_x_clip = Float.parseFloat(s);
		
    s = element.getAttribute("max_x");
    if(s.length()>0)
      max_x_clip = Float.parseFloat(s);

    s = element.getAttribute("min_y");
    if(s.length()>0)
      min_y_clip = Float.parseFloat(s);

    s = element.getAttribute("max_y");
    if(s.length()>0)
      max_y_clip = Float.parseFloat(s);

    if ( Float.isNaN(min_x_clip)&&Float.isNaN(max_x_clip) &&Float.isNaN(min_y_clip)&&Float.isNaN(max_y_clip) ){
    }
    else {
      if ( (Float.isNaN(min_x_clip)||Float.isNaN(max_x_clip)
            ||Float.isNaN(min_y_clip)||Float.isNaN(max_y_clip))
         ||((min_x_clip>=max_x_clip)||(min_y_clip>=max_y_clip)) )
        throw new Exception("Invalid clip region");

      clipArea = new Polygon();
			
			String pointID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
			String xyzID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
      clipArea.getPoints().add(new Point(pointID1,min_x_clip, min_y_clip,xyzID1));
			
			String pointID2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
			String xyzID2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
      clipArea.getPoints().add(new Point(pointID2,max_x_clip, min_y_clip,xyzID2));
			
			String pointID3 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
			String xyzID3 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
      clipArea.getPoints().add(new Point(pointID3,max_x_clip, max_y_clip,xyzID3));
			
			String pointID4 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
			String xyzID4 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
      clipArea.getPoints().add(new Point(pointID4,min_x_clip, max_y_clip,xyzID4));
    }
		
		
		org.w3c.dom.Node n;
		
		n = element.getElementsByTagName("reflect_directions").item(0);
    if(n!=null)
			doubleFlow = Boolean.valueOf(n.getFirstChild().getNodeValue()).booleanValue();
		
		n = element.getElementsByTagName("max_traffic_lights").item(0);
    if(n!=null) {
     maxTrafficLight = Integer.parseInt(n.getFirstChild().getNodeValue());
		}
		
    n = element.getElementsByTagName("number_lane").item(0);
		if(n!=null)
		  numberLane = Integer.parseInt(n.getFirstChild().getNodeValue());
		
		org.w3c.dom.NodeList laneList = element.getElementsByTagName("number_lane");
		  if ((laneList !=null) && (laneList.getLength()== 1)) {
		    org.w3c.dom.Element laneElement = (org.w3c.dom.Element)laneList.item(0);
			  if (laneElement !=null) {
				  s = laneElement.getAttribute("full");
				  if (s.length()>0)
				    allRoads = Boolean.valueOf(s).booleanValue();
				
				  if (!allRoads) {
				    s = laneElement.getAttribute("max");
					  if(s.length()>0) {
						   maxNumberMultilane = (Integer.valueOf(s).intValue());
					  }
				  }
				  s = laneElement.getAttribute("dir");
				  if (s.length()>0) {
				    directional = Boolean.valueOf(s).booleanValue();
				    /*if (directional && !doubleFlow)
					    throw new Exception("\"dir\" attribute must be identical to reflect_directions !");*/
				  }
			 }
		 }
		
		nbVehiclesInJunction = new java.util.HashMap();
		
    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading SpatialModel extension"));
  }
	
}