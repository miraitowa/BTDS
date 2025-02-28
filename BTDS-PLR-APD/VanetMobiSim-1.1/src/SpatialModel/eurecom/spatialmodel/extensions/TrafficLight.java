package eurecom.spatialmodel.extensions;

/**
 * <p>Title: Traffic Light Extention Module</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */

import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import eurecom.spacegraph.*;

/**
 * This class implements a traffic light functionality
 * @author Jerome Haerri
 * @version 1.0
 */
public class TrafficLight extends ExtensionModule {
 
	/**
   * Spatial Model
   */
  protected SpatialModel spatialModel;
	
	/**
   * Map containing all traffic light elements
   */
	protected java.util.Map trafficLights = new java.util.HashMap();
	
	/**
   * Traffic Lights' update step 
   */
	protected long trafficStep = 10000;
	
	/**
   * Constructor
   */
  public TrafficLight() {
    super("TrafficLight");
		trafficStep = 10000;
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Traffic Light module";
  }
	
	/**
   * Returns a map of all traffic lights attached to this module. <br>
   * <br>
   * @return extension module's traffic lights
   */
  public java.util.Map getTrafficLights()
  {
    return trafficLights;
  }

	/**
   * Performs the module initialization. <br>
   * <br>
   * The method is called after finishing the scenario file processing.
   */
  public void initialize() {
		super.initialize();
		
		u.sendNotification(new LoaderNotification(this, u,
																								"Initializing TrafficLight extension"));
		
    java.util.Map elements = spatialModel.getElements();
		java.util.Random rand=u.getRandom();
		String trafficLightPlusCode = "2303";
		String trafficLightMinusCode = "2304";
		
		
		Graph graph = spatialModel.getGraph();
		java.util.ArrayList adjacentRoadsID;
		
		//System.out.println("Initializing  TrafficLight extension");
		java.util.Iterator iter = elements.values().iterator();
		while (iter.hasNext()) {
			SpatialModelElement tmpElement = (SpatialModelElement)iter.next();	
			// check for junctions
			if ( !tmpElement.getClassCode().equals("41") || !tmpElement.getSubClassCode().equals("20") )
			  continue;
			
			adjacentRoadsID = new java.util.ArrayList();
			Point junctionPoint = (Point)tmpElement.getGeometry();
			Vertex vs = graph.getVertex(junctionPoint.getX(),junctionPoint.getY());
			
			java.util.ArrayList NeighborVertices = vs.getNeighbours();
			//System.out.println("init here 1");
			java.util.Iterator neighborIter = NeighborVertices.iterator();
			while (neighborIter.hasNext()) {
				//System.out.println("init here 2");
				boolean plusDirection = false;
				boolean incoming = true;
				Vertex vd = (Vertex)neighborIter.next();
				Edge edge = spatialModel.findEdge(vs,vd);
				SpatialModelElement roadElement = spatialModel.mapEdgeToElement(edge);
				
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
				
				String d_s = (String)roadElement.getAttributes().get("DF");
				if ((!plusDirection && (d_s == "3")) || (plusDirection && (d_s == "2")) || (d_s == "4")) {
				  incoming = false;
				}
				
				// actually, in the case of single flow roads, we could only check if there is a single traffic light 
				// ( it can only be plus if direction is + and reverse since we are single flow)
																																			
				java.util.ArrayList relationships = roadElement.getRelations();
				for (int i = 0; i < relationships.size(); i++) {
					RelationshipRecord record = (RelationshipRecord)relationships.get(i);
					if (record ==null){
						continue;
					}
					if (incoming && (((record.getCode() == trafficLightPlusCode) && plusDirection) 
							|| ((record.getCode() == trafficLightMinusCode) && !plusDirection)) )   {
						
						// means we have a traffic light related to incoming traffic 
					 	 adjacentRoadsID.add(roadElement.getID());
					}
				}
			}
			if ((adjacentRoadsID!= null) && (adjacentRoadsID.size() > 0)) {
				String id = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
			  TrafficLightElement trafficLightElement = new TrafficLightElement(id, junctionPoint.getPosition(), trafficStep, adjacentRoadsID, u);
				trafficLightElement.start(u.getTime());
				// we put the junction ID as key for fast retreive.
				trafficLights.put(tmpElement.getID(),trafficLightElement);
			}
		}
		u.sendNotification(new LoaderNotification(this, u,"Finished initializing TrafficLight extension"));
	}
	
	/**
   * Execute the extension. <br>
   * <br>
   * The method is called on every simulation timestep. On each timestep, the traffic light decides if it needs to change the
	 * light in a particular direction. Rule: Round Robin.
   * @return 0 - the module should be executed on next timesteps,
   *        -1 - the module should not be executed on further timesteps and should be removed from the extensions' list
   */
  public int act() {

		// first increment the iterator
		// second get the Edge from the vertex
		// update the greenLight to the SpatialElement
		u.sendNotification(new LoaderNotification(this, u,
																								"Changing TrafficLight Status"));
		
		long time = u.getTime();
		if ((time % trafficStep) == 0) {
		  // change the traffic light every 30 seconds
			 java.util.Iterator trafficIterator = trafficLights.values().iterator();
			 while (trafficIterator.hasNext()) {
				 //System.out.println("************************************Updating next traffic light");
				 TrafficLightElement trafficLightElement = (TrafficLightElement)trafficIterator.next();	
				 trafficLightElement.update(u.getTime());
				 if (trafficLightElement.status() == "0") {
					 return -1;
				 }
			 }
		}
		u.sendNotification(new LoaderNotification(this, u,
																								"Finished changing TrafficLight Status"));
    return 0;
  }
	
	/**
   * Initializes simulation parameters from XML tag. <br>
   * <br>
   * @throws Exception Exception if parameters are invalid
   */
  public void load(org.w3c.dom.Element element) throws Exception {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading TrafficLight extension"));
    
    super.load(element);
		
		String s;
		
		s = element.getAttribute("spatial_model");
    if (s.length()>0) {
      spatialModel = (SpatialModel)u.getExtension(s);
    }
		
		else {
			spatialModel = (SpatialModel)u.getExtension("SpatialModel");
		}
		
		
		if (spatialModel==null)
      throw new Exception("SpatialModel instance does not exist!");
		
		spatialModel.setTrafficLightName(name);
		
		org.w3c.dom.Node n;
		
		s = element.getAttribute("step");
		if(s.length()>0)
			trafficStep = (Integer.valueOf(s).intValue());
		
    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading TrafficLight extension"));
  }
	
}


