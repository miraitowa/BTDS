package eurecom.spacegraph;

/**
 * <p>Title: SpaceGraph creator</p>
 * <p>Description: Creates a Voronoi Tesselation </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */


import java.io.*;
import eurecom.spacegraph.graphalgorithm.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import eurecom.spatialmodel.extensions.*;
/**
 * This class is used to randomly generate a Spatial Model out of 
 * a random voronoi tesselation and random attributes/
 *
 * @author Jerome Haerri 
 * @version 1.0
 */
public class SpaceGraph extends ExtensionModule {
  
  /**
   * Storage for default attributes values:
   * Key - Attribute id, Value - default value
   */
  protected java.util.Map defaultAttributesValues = new java.util.HashMap();

  /**
   * Spatial Model
   */
  protected SpatialModel spatialModel;

	
  /**
   * Scale factor for x-coordinates
   */
  protected double scale_x = 1;

  /**
   * Scale factor for y-coordinates
   */
  protected double scale_y = 1;
  
	/**
   * Clipping Region
   */
  protected Polygon clipArea;
	
	/**
   * Maximum number of obstacles in the clipping region
   */
	protected int maxObstacles = 40;
		
	/**
   * Double Flow disabled
   */
	protected boolean doubleFlow = false;
	
	/**
   * List of obstacles in the Space Graph
   */
	public java.util.ArrayList obstacles = new java.util.ArrayList();
	
	/**
   * List of voronoi edges in the Space Graph
   */
	public java.util.ArrayList voronoiEdges = new java.util.ArrayList();
	
	/**
	 * array that keeps track of nodes that are on the simulation boundary
   */
	public java.util.ArrayList bounderyPoints = new java.util.ArrayList();
	
	/**
	 * Mapping between cluster types and speed limitations
   */
	java.util.HashMap speedAttribute = null;
	
  /**
   * Constructor
   */
  public SpaceGraph()
  {
    super("SpaceGraph");
		speedAttribute = new java.util.HashMap();
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "SpaceGraph creator module";
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
    return -1;
  }

  /**
   * Parses the string argument as an integer
   * @param s string to be parsed
   * @return integer value from argument
   */
  protected static int parseInt(String s)
  {
    s = s.trim();
    if (s.length()==0)
      s="0";

    return new Integer(s).intValue();
  }

	/**
	* Access methods. Returns the clipping area
	* @return clipping area 
	*/
	public Polygon getClipArea() {
	  return spatialModel.getClipArea();
	}
	
	/**
	* Access methods. Returns the size of the clipping area
	* @return double array containing the X/Y size of the clipping are 
	*/
	public double[] getClipAreaXY() {
		try {
			return spatialModel.getClipAreaXY();
		}
		catch (Exception e2) {
					System.out.println("Error in SpaceGraph.getClipAreaXY");
					e2.printStackTrace();
					return new double[2];
			}
	}
	
	/**
	* Access methods. Returns the spatialModel
	* @return SpatialModel
	*/
	public SpatialModel getSpatialModel() {
	  return spatialModel;
	}
	 /**
	* Constructs elements from loaded voronoi edges. <br>
	* <br>
	* @throws Exception Exception if more than 4 coordinates per roadElement
	*/

	protected void createLineFeatureElements() throws Exception {
		int a,b,c,d;
		Double p,q,r,s;
		try {
			
			java.util.Map elements = spatialModel.getElements();
			java.util.Random rand=u.getRandom();
			
			// JHNote (18/08/2005): We only consider roadElements and junctions.
			String class_code = "41";
			String subclass_code = "10";
			String subclass_code_junction = "20";
			
			String line;
			String id,id_junction1,id_junction2;
			String id_R,id_junction1_R,id_junction2_R;
			SpatialModelElement element,junction2,junction1;
			SpatialModelElement element_R,junction2_R,junction1_R;
			
			
			java.util.Iterator voroiIter = voronoiEdges.iterator();
			while (voroiIter.hasNext()) {
				 MyPoint p1 = (MyPoint)voroiIter.next();
				 MyPoint p2 = (MyPoint)voroiIter.next();
					
				
				id = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
				
				p = new Double((new Double(p1.x)).doubleValue()/scale_x);
				a = p.intValue();
			
				q = new Double((new Double(p1.y)).doubleValue()/scale_y);
				b = q.intValue();
				
				r = new Double((new Double(p2.x)).doubleValue()/scale_x);
				c = r.intValue();
				
				s = new Double((new Double(p2.y)).doubleValue()/scale_y);
				d = s.intValue();
				
				
				// We add now the first road
				Polyline shape = new Polyline();
				java.util.ArrayList points = shape.getPoints();
				
				String pointID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
				String xyzID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
				Point pointf = new Point(pointID1,a,b,xyzID1);
				points.add(pointf);
				
				//points.add(int1);
				String pointID2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
				String xyzID2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
				Point pointt = new Point(pointID2,c,d,xyzID2);
				points.add(pointt);
				
				
				// adding the junctions but first we check if the junctions alread exist.
				
				boolean newPointf = true;
				boolean newPointt = true;
	
				 java.util.Iterator iter = elements.values().iterator();
				 while (iter.hasNext()) {
					 SpatialModelElement tmpElement = (SpatialModelElement)iter.next();	
					 // check for junctions
					 if ( !tmpElement.getClassCode().equals("41") || !tmpElement.getSubClassCode().equals("20") )
						 continue;
					 
					 Point tmpPoint = (Point)tmpElement.getGeometry();
					 if(tmpPoint.contains(pointf)) {
						 newPointf = false;
					 }
					 if(tmpPoint.contains(pointt)) {
						 newPointt = false;
					 }
				 }
				
				if (newPointf) {
					id_junction1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					junction1 = new SpatialModelElement(id_junction1, class_code, subclass_code_junction, pointf);
					junction1.getAttributes().put("JT","1"); // junction type : mini roundabout
					// clip
					if (clipArea!=null) {
						if (clipArea.contains(pointf)) {
							elements.put(id_junction1, junction1);
						}
					}
					else
						elements.put(id_junction1, junction1);	
					
				}
				
				if (newPointt) {
					id_junction2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					junction2 = new SpatialModelElement(id_junction2, class_code, subclass_code_junction, pointt);
					junction2.getAttributes().put("JT","1"); // junction type : mini roundabout
					// clip
					if (clipArea!=null) {
						if (clipArea.contains(pointt)) {
							elements.put(id_junction2, junction2);
						}
					}
					else
						elements.put(id_junction2, junction2);
					
				}
				
				// adding polyline
				element = new SpatialModelElement(id, class_code, subclass_code, shape);
				element.getAttributes().put("DF","1"); // directional flow : 1= open to all direction
				element.getAttributes().put("NL","1"); // number of lane : 1= one lane in each directions
				element.getAttributes().put("VT","0"); // vehicle type : 0= all vehicles
				setSpeed(element);
				
				// clip
			 if (clipArea!=null) {
					spatialModel.clip(element);
					if (shape.getPoints().size()>0)
						elements.put(id, element);
				}
				else
					elements.put(id, element);
				
			}
		} 
		catch (Exception e2) {
			System.out.println("Error in createLineFeatureElements");
			e2.printStackTrace();
		}
	}
		
		
		/**
		* Constructs elements from loaded obstacle features <br>
    * <br>
    * @throws Exception Exception if more than 2 coordinates per obstacle
		*/
  protected void createPointFeatureElements() throws Exception {
		int a,b;
		Double p,q;
		try {
				java.util.Map elements = spatialModel.getElements();
				java.util.Random rand=u.getRandom();
				
				// JHNote (18/08/2005): We only consider buildings.
				String class_code = "71";
				String subclass_code = "10";
				
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
				
				for (int i = 0; i < obstacles.size(); i++) {
					MyPoint p1 = (MyPoint)obstacles.get(i);
					
					String id = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					p = new Double((new Double(p1.x)).doubleValue()/scale_x);
					a = p.intValue();
		
					q = new Double((new Double(p1.y)).doubleValue()/scale_y);
					b = p.intValue();
					
					String pointID = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					String xyzID = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					Point shape = new Point(pointID,a,b,xyzID);
					
					SpatialModelElement element = new SpatialModelElement(id, class_code, subclass_code, shape);
				
					// clip
					if (clipArea!=null) {
						if (clipArea.contains(shape))
							elements.put(id, element);
					}
					else
						elements.put(id, element);
					
				}
				
				// now adding traffic lights and traffic signs
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
					
					if ((String)tmpElement.getAttributes().get("DF") == "3") {
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
					
					else if ((String)tmpElement.getAttributes().get("DF") == "2") {
					
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
						
					 else if ((String)tmpElement.getAttributes().get("DF") == "1") {
							
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
						  System.out.println("Non correct attribute DF value");
						}
				}
		}
		catch (Exception e2) {
			if (e2 instanceof IOException) 
				System.err.println("Caught an IO Exception while 'createPointFeatureElements'");
			else	{
				System.out.println("Error in createPointFeatureElements");
				e2.printStackTrace();
			}
		}
	}
 
  /**
   * Constructs simple elements from random data
	 * This method creates junctions and road elements.
	 * It also creates simple traffic signs and lights
	 * such as multidirectional Stop or Yield signs.
	 * We let the createSecondLayerElements() method
	 * create more complex traffic signs since we
	 * would need access to the graph powerful methods.
   */
  protected void createFirstLayerElements()
  {
    try {
			createLineFeatureElements();
			if (doubleFlow)
				spatialModel.createDoubleFlowRoads();
			
			createPointFeatureElements();
			
		}
		catch (Exception e2) {
				System.out.println("Error in createFirstLayerElements");
				e2.printStackTrace();
				System.exit(-1);
			}
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
    * Compute the common X-Y dimensions of each cluster given the simulation area and the density of clusters.  <br>
    * <br>
		* 
		* @param clustersDensity density of clusters per 1000*1000 squared area.
		* @return double[] an array containing the number of clusters and their X-Y dimension.
    */
		
	protected double[] getClustersDim(double clustersDensity) {
		double[] clusterDim = new double[3];
		try {
			double[] clipAreaXY = spatialModel.getClipAreaXY();
			double clipAreaX = clipAreaXY[0];
			double clipAreaY = clipAreaXY[1]; 
			int nb_cluster = (int)(clustersDensity*(clipAreaX*clipAreaY));
				
			java.util.Vector dividers = new java.util.Vector();
	
			// find the dividers of nb_cluster.
			for (int i= 1; i <=nb_cluster;i++) {
				if ((nb_cluster % i) == 0)
					dividers.add(new Integer(i));
			}
			double ratio = clipAreaX / clipAreaY;
			float select = Float.MAX_VALUE;
			int nb_cluster_x = 1;
			
			// Methode to find the number of clusters
			// to spread in the X and Y coordinates
			// first ratio: X/Y
			// second ratio: Div/(nb_cluster/Div)=Div^2 / nb_cluster;
			// iterate for each divider and find the closest to the
			// first ratio. Then, the number of clusters
			// to spread in X is the closest divider.
				
			for (int i = 0; i < dividers.size();i++) {
				float a = (float)(Math.pow(((double)((Integer)dividers.get(i)).intValue()),2.0)) / nb_cluster;
				if (Math.abs(ratio - a) < Math.abs(ratio - select)) {
					select = a;
					nb_cluster_x = ((Integer)dividers.get(i)).intValue();
				}
			}
			int nb_cluster_y = (int)(nb_cluster / nb_cluster_x);
			double clusterDimX = clipAreaX / nb_cluster_x;
			double clusterDimY = clipAreaY / nb_cluster_y;
	
			clusterDim[0] = (double)nb_cluster;
			clusterDim[1] = clusterDimX;
			clusterDim[2] = clusterDimY;
			return clusterDim;
		}
		catch (Exception e2) {
			System.out.println("Error in getClustersDim");
			return clusterDim;
		}
	}
	
	
	
	/**
    * Generates the clusters of obstacles given the XML input file.  <br>
    * <br>
		* 
		* @param clusterList XML input file
		* @param clustersDensity density of clusters per 1000*1000 squared area.
		* @throws Exception Exception if XML tags are missing, 
		*					if the total ratio of clusters is not 1,
		*					or if the number of generated clusters is not equal to the one desired in the input file.
    */
		
	protected void generateClusters(org.w3c.dom.NodeList clusterList, double clustersDensity) throws Exception {
		
		double totalRatio = 0.0;
		int totalNCluster = 0;
		org.w3c.dom.Node n;
		java.util.ArrayList clusters = spatialModel.getClusters();
		
		try {
			double[] clusterDim = getClustersDim(clustersDensity);
		
			int nClusters = (int)clusterDim[0];
			double xDim = clusterDim[1];
			double yDim = clusterDim[2];
			//System.out.println("In generateClusters" + clustersDensity + " " +  nClusters +  " " + xDim +  " " + yDim);
			u.sendNotification(new DebugNotification(this, u,
																								 "clustersDensity " + clustersDensity + " nClusters " 
																								 +  nClusters +  " xDim " + xDim +  " yDim " + yDim ));
			
			clusters = new java.util.ArrayList(nClusters);
			spatialModel.setClusters(clusters);
			
			for (int i=0; i<clusterList.getLength(); i++) {
				org.w3c.dom.Element clusterElement = (org.w3c.dom.Element)clusterList.item(i);
				String id = clusterElement.getAttribute("id");
				if (id.length()==0)
					throw new Exception("\"id\" attribute is missing!");
			
				n = clusterElement.getElementsByTagName("density").item(0);
				if (n==null) 
					throw new Exception("<density> is missing!");
				
				double dens = (Double.valueOf(n.getFirstChild().getNodeValue()).doubleValue());
			
				n = clusterElement.getElementsByTagName("ratio").item(0);
				if (n==null) 
					throw new Exception("<ratio> is missing!");
				double ratio = (Double.valueOf(n.getFirstChild().getNodeValue()).doubleValue());
				
				totalRatio +=ratio;
				
				int nCluster = (int)(nClusters*ratio);
				totalNCluster+=nCluster;
				
				n = clusterElement.getElementsByTagName("speed").item(0);
				String speed = null;
				
				if (n!=null)
					speed = n.getFirstChild().getNodeValue();
					
				else 
					speed = "13.88"; //default speed of 50km/h
				
				speedAttribute.put(id,speed);
				
				u.sendNotification(new DebugNotification(this, u,
																								 "generateClusters is about to add " + nCluster + " " +  id 
																								 + "clusters of " + dens + " ratio " + ratio +" and speed "+speed));
				//System.out.println("Is about to add " + nCluster + " " +  id + "clusters of " + dens + " and ratio " + ratio);  
				for (int j = 0; j < nCluster; j++) {
					Cluster cluster = new Cluster(id, dens,xDim,yDim);
					clusters.add(cluster);
				}
			}
			if (totalRatio !=1)
				throw new Exception("The total ratio of clusters must be 1");
			
			
			Cluster lowDensCluster = null;
			double lowestDens = 1.0;
			int diff = nClusters - totalNCluster; 
			if (diff > 0) {
			  for (int j = 0; j < clusters.size(); j++) {
					Cluster cluster = (Cluster)(clusters.get(j));
					double density = cluster.getDensity();
					if (density < lowestDens)
						lowDensCluster = cluster;
				}
				if (lowDensCluster != null) {
					u.sendNotification(new DebugNotification(this, u,
																									 "Added  " + diff + " " +  lowDensCluster.getName() 
																									 + " clusters of " + lowDensCluster.getDensity() + "size " 
																									 +lowDensCluster.getX() + " " + lowDensCluster.getY()));
																									 
					//System.out.println("Added  " + diff + " " +  lowDensCluster.getName() + " clusters of " + lowDensCluster.getDensity() + "size " +lowDensCluster.getX() + " " + lowDensCluster.getY() );  
					for (int j = 0; j < diff; j++) {
						Cluster cluster = new Cluster(lowDensCluster.getName(), lowDensCluster.getDensity(),lowDensCluster.getX(),lowDensCluster.getY());
						clusters.add(cluster);
					}
				}
				else
					throw new Exception("The total number clusters is not reached.");
			}
		}
		catch (Exception e2) {
			System.out.println("Error in generateClusters");
			System.out.println(e2);
			System.exit(-1);
		}
	}
	
	/**
    * Randomly generates the clusters of obstacles.  <br>
    * <br>
		* 
		* @param clustersDensity density of clusters per 1000*1000 squared area.
    */
	protected void generateRandomClusters(double clustersDensity) {
		// JHNote (24/08/2005) : initial density computed on a 100*100 squared cluster
		//											 nb_obs_down = 25; nb_obs_res = 9; nb_obs_sub = 1
		
		java.util.ArrayList clusters = spatialModel.getClusters();
		
		double res_dens = 0.0009;
		double down_dens = 0.0025;
		double sub_dens = 0.0001;
		
		// JHNote (24/08/2005) : initial cluster density computed on a 1000*1000 squared simulation area
		//											 nb_cluster = 4;
		
		double cluster_dens = 0.000004;
		
		// JHNote (24/08/2005) : initial percentage of different kind of clusters. This is based on only
		//											 3 different kind of clusters.
		double per_down = 0.1;
		double per_res = 0.3;
		double per_sub = 0.6;
		
		if (clustersDensity !=0)
			cluster_dens =clustersDensity;
		
		try {
			
			double[] clusterDim = getClustersDim(cluster_dens);
			
			int nb_cluster = (int)clusterDim[0];
			double xDim = clusterDim[1];
			double yDim = clusterDim[2];
			
			//System.out.println(cluster_dens + " " +  nb_cluster +  " " + xDim +  " " + yDim);
				u.sendNotification(new DebugNotification(this, u,
																								 "cluster_dens " + cluster_dens + " nb_cluster " 
																								 +  nb_cluster +  " xDim " + xDim +  " yDim " + yDim ));
			/*double[] clipAreaXY = spatialModel.getClipAreaXY();
			double nb_cluster = (int)cluster_dens*(clipAreaXY[0]*clipAreaXY[1]);*/
		
			int nb_cluster_down = (int)(nb_cluster*per_down);
			int nb_cluster_res = (int)(nb_cluster*per_res);
			int nb_cluster_sub = (int)(nb_cluster*per_sub);
		
			nb_cluster_sub += (nb_cluster - (nb_cluster_down + nb_cluster_res + nb_cluster_sub));
		
			
		
			clusters = new java.util.ArrayList(nb_cluster);
			spatialModel.setClusters(clusters);
			
			speedAttribute.put("downtown","13.88"); // maximum speed : 50km/h
			speedAttribute.put("residential","8.33"); // maximum speed : 30km/h
			speedAttribute.put("suburban","22.22"); // maximum speed : 80km/h
			
			for (int i = 0; i < nb_cluster_down; i++) {
				Cluster cluster = new Cluster("downtown",down_dens,xDim,yDim);
				clusters.add(cluster);
			}
			for (int i = 0; i < nb_cluster_res; i++) {
				Cluster cluster = new Cluster("residential",res_dens,xDim,yDim);
				clusters.add(cluster);
			}
			for (int i = 0; i < nb_cluster_sub; i++) {
				Cluster cluster = new Cluster("suburban",sub_dens,xDim,yDim);
				clusters.add(cluster);
			}
		}
		catch (Exception e2) {
			System.out.println("Error in generateRandomClusters");
			System.out.println(e2);
		}
	}
	
	/**
    * Set the clusters of obstacles in the simulation area.  <br>
    * <br>
    */
		
	protected void setClusters() {
		try {
			double[] clipAreaXY = spatialModel.getClipAreaXY();
			double clipAreaX = clipAreaXY[0];
			double clipAreaY = clipAreaXY[1]; 
			
			java.util.ArrayList clusters = spatialModel.getClusters();
			
		
			// Two methodes here: either we scramble the ArrayList and pop
			// or we randomly generate an ArrayList index and extract the
			// value.
			java.util.Random rand=u.getRandom();
		
			// shalow cloning here. We just clone the pointers, s.th we keep
			// the original clusters unaltered.
			java.util.ArrayList clustersClone = (java.util.ArrayList)clusters.clone();
		
			double baseX = 0.0;
			double baseY = 0.0;

			while(clustersClone.size() != 0) {
				int index = rand.nextInt(clustersClone.size());
				Cluster cluster = (Cluster)clustersClone.remove(index);
		
				cluster.setStartPoints(baseX,baseY);
				u.sendNotification(new DebugNotification(this, u,
																								 "Cluster " + cluster.getName() + " of density " + cluster.getDensity() 
																								 + " initial point " + baseX + " " + baseY
																								 + " xDim " + cluster.getX() + " yDim " + cluster.getY() ));
				// System.out.println("Cluster " + cluster.getName() + " initial point " + baseX + " " + baseY);
				baseX += cluster.getX();
				if(baseX >= clipAreaX) {
					baseX = 0.0;
					baseY += cluster.getY();
				}
			}
			
		}
		catch (Exception e2) {
					System.out.println("Error in setClusters");
					System.out.println(e2);
		}
	}
	
	/**
    * Distributes the obstacles in the clusters.  <br>
    * <br>
    */
	protected void distributeClusterObstacles() {
		java.util.Random rand=u.getRandom();
		java.util.ArrayList clusters = spatialModel.getClusters();
	
			for (int i = 0; i < clusters.size(); i++) {
				Cluster cluster = (Cluster)clusters.get(i);
				int maxObstacles = (int)(cluster.getDensity()*(cluster.getX()*cluster.getY()));
				for (int j = 0; j < maxObstacles; j++) {
					float floatRandom1 = rand.nextFloat();
					float floatRandom2 = rand.nextFloat();
					int x = (int)(floatRandom1*(cluster.getX()) + cluster.getStartX()); 
					int y = (int)(floatRandom2*(cluster.getY()) + cluster.getStartY()); 
					
					MyPoint p = new MyPoint((double)x,(double)y);
					obstacles.add(p);
					
					//res.println(x + ". " + y + ".");
				}
			}
	}
	
		/**
   * The the maximum speed on roadElement according to the cluster type. <br>
   * <br>
   * @param element RoadElement
	 **/
	protected void setSpeed(SpatialModelElement element) {
		java.util.ArrayList clusters = spatialModel.getClusters();
		Polygon polyCluster = new Polygon();
	  for (int i = 0; i < clusters.size(); i++) {
				Cluster cluster = (Cluster)clusters.get(i);
				polyCluster.getPoints().clear();
				polyCluster.getPoints().add(new Point(cluster.getStartX(), cluster.getStartY()));
				polyCluster.getPoints().add(new Point(cluster.getStartX()+cluster.getX(), cluster.getStartY()));
				polyCluster.getPoints().add(new Point(cluster.getStartX()+cluster.getX(), cluster.getStartY()+cluster.getY()));
				polyCluster.getPoints().add(new Point(cluster.getStartX(), cluster.getStartY()+cluster.getY()));
					
				Polyline line = (Polyline)element.getGeometry();
				java.util.ArrayList points = line.getPoints();
				Point point1 = (Point)points.get(0);
				Point point2 = (Point)points.get(points.size()-1);
				
				if(polyCluster.contains(point1) && polyCluster.contains(point2)) { // only the cluster that fully contains the roadElement update its speed
					String speed = (String)speedAttribute.get(cluster.getName());
					if (speed != null)
					  element.getAttributes().put("SP",speed); // Speed Restriction
					else	
						element.getAttributes().put("SP","13.88"); // Speed Restriction (maxmimum speed) : 13.88m/s= 50km/h
				}
				else if(polyCluster.contains(point1) || polyCluster.contains(point2)) { // such that the speed will be updated by all clusters sharing the roadElement
					String speed = (String)speedAttribute.get(cluster.getName());
					if (speed != null)
					  element.getAttributes().put("SP",speed); // Speed Restriction 
					else	
						element.getAttributes().put("SP","13.88"); // Speed Restriction (maxmimum speed) : 13.88m/s= 50km/h
				}
			}
	}
	
	// JHNote (25/08/2005) first straight methods that we implemented. Now, this method
	// 										 has been refined by several subMethods :generateClusters()-
	//										 setClusters()-distributeClusterObstacles().
	
	/**
    * Distributes clusters of obstacles within the simulation area.  <br>
    * <br>
		* @deprecated Replaced by a sequence of method calls: generateRandomClusters(double)-setClusters()-distributeClusterObstacles().
		* @see #generateRandomClusters(double)
		* @see #setClusters()
		* @see #distributeClusterObstacles()
		* @throws Exception Exception 
    */
	protected void oldDistributeClusterObstacles() throws Exception {
	  // JHNote (24/08/2005) : initial density computed on a 100*100 squared cluster
		//											 nb_obs_down = 25; nb_obs_res = 9; nb_obs_sub = 1
		
		double res_dens = 0.0009;
		double down_dens = 0.0025;
		double sub_dens = 0.0001;
		
		// JHNote (24/08/2005) : initial cluster density computed on a 1000*1000 squared simulation area
		//											 nb_cluster = 4;
		double cluster_dens = 0.000004;
		
		// JHNote (24/08/2005) : initial percentage of different kind of clusters. This is based on only
		//											 3 different kind of clusters.
		double per_down = 0.1;
		double per_res = 0.3;
		double per_sub = 0.6;
		
		java.util.ArrayList clusters = spatialModel.getClusters();
		
		// find the number of clusters
		java.util.ArrayList points = clipArea.getPoints();
		double clipAreaX,clipAreaY; 
		if (points.size() == 4) {
			try {
				Point point1 = (Point)points.get(0);
				Point point2 = (Point)points.get(1);
				Point point3 = (Point)points.get(2);
				Point point4 = (Point)points.get(3);
			
				clipAreaX = point2.getX() - point1.getX();
				clipAreaY = point3.getY() - point1.getY();
		
				int nb_cluster = (int)(cluster_dens*(clipAreaX*clipAreaY));
				int nb_cluster_down = (int)(nb_cluster*per_down);
				int nb_cluster_res = (int)(nb_cluster*per_res);
				int nb_cluster_sub = (int)(nb_cluster*per_sub);
		
				nb_cluster_sub += (nb_cluster - (nb_cluster_down + nb_cluster_res + nb_cluster_sub));
		
		
				clusters = new java.util.ArrayList(nb_cluster);
				spatialModel.setClusters(clusters);
				
				java.util.Vector dividers = new java.util.Vector();
	
				// find the dividers of nb_cluster.
				for (int i= 1; i <=nb_cluster;i++) {
					if ((nb_cluster % i) == 0)
						dividers.add(new Integer(i));
				}
				double ratio = clipAreaX / clipAreaY;
				float select = Float.MAX_VALUE;
				int nb_cluster_x = 1;
				
				// Methode to find the number of clusters
				// to spread in the X and Y coordinates
				// first ratio: X/Y
				// second ratio: Div/(nb_cluster/Div)=Div^2 / nb_cluster;
				// iterate for each divider and find the closest to the
				// first ratio. Then, the number of clusters
				// to spread in X is the closest divider.
				
				for (int i = 0; i < dividers.size();i++) {
					float a = (float)(Math.pow(((double)((Integer)dividers.get(i)).intValue()),2.0)) / nb_cluster;
					if (Math.abs(ratio - a) < Math.abs(ratio - select)) {
						select = a;
						nb_cluster_x = ((Integer)dividers.get(i)).intValue();
					}
				}
				int nb_cluster_y = (int)(nb_cluster / nb_cluster_x);
				double clusterDimX = clipAreaX / nb_cluster_x;
				double clusterDimY = clipAreaY / nb_cluster_y;
				
				System.out.println(cluster_dens + " " +  nb_cluster +  " " + clusterDimX +  " " + clusterDimY);
				
				for (int i = 0; i < nb_cluster_down; i++) {
					Cluster cluster = new Cluster("downtown",down_dens,clusterDimX,clusterDimY);
					clusters.add(cluster);
				}
				for (int i = 0; i < nb_cluster_res; i++) {
					Cluster cluster = new Cluster("residential",res_dens,clusterDimX,clusterDimY);
					clusters.add(cluster);
				}
				for (int i = 0; i < nb_cluster_sub; i++) {
					Cluster cluster = new Cluster("suburban",sub_dens,clusterDimX,clusterDimY);
					clusters.add(cluster);
				}
				
				// Two methodes here: either we scramble the ArrayList and pop
				// or we randomly generate an ArrayList index and extract the
				// value.
				java.util.Random rand=u.getRandom();
		
				// shalow cloning here. We just clone the pointers, s.th we keep
				// the original clusters unaltered.
				java.util.ArrayList clustersClone = (java.util.ArrayList)clusters.clone();
		
				double baseX = 0.0;
				double baseY = 0.0;

				while(clustersClone.size() != 0) {
					int index = rand.nextInt(clustersClone.size());
					Cluster cluster = (Cluster)clustersClone.remove(index);
		
					cluster.setStartPoints(baseX,baseY);
					baseX += cluster.getX();
					if(baseX >= clipAreaX) {
						baseX = 0.0;
						baseY += cluster.getY();
					}
				}
		
				u.sendNotification(new DebugNotification(this, u,
																								 "Dimensions of ClipArea = (" + clipAreaY +" m x " + clipAreaY + " m )"));
			  
				for (int i = 0; i < clusters.size(); i++) {
					Cluster cluster = (Cluster)clusters.get(i);
					int maxObstacles = (int)(cluster.getDensity()*(cluster.getX()*cluster.getY()));
					for (int j = 0; j < maxObstacles; j++) {
						float floatRandom1 = rand.nextFloat();
						float floatRandom2 = rand.nextFloat();
						int x = (int)(floatRandom1*(cluster.getX()) + cluster.getStartX()); 
						int y = (int)(floatRandom2*(cluster.getY()) + cluster.getStartY()); 
						
						MyPoint p = new MyPoint((double)x,(double)y);
						obstacles.add(p);
					
					}
				}
			}
			catch (Exception e2) {
					System.out.println("Error in distributeClusterObstacles");
					e2.printStackTrace();
			}
		}
	}
	
		/**
    * Uniformly distributes the obstacles within the simulation area.<br>
    * <br>
    */
	protected void distributeRandomObstacles() throws Exception {
	  java.util.Random rand=u.getRandom();
		java.util.ArrayList points = clipArea.getPoints();
		
		double clipAreaX,clipAreaY; 
		if (points.size() == 4) {
		  Point point1 = (Point)points.get(0);
			Point point2 = (Point)points.get(1);
			Point point3 = (Point)points.get(2);
			Point point4 = (Point)points.get(3);
			
			clipAreaX = point2.getX() - point1.getX();
			clipAreaY = point3.getY() - point1.getY();
			
			u.sendNotification(new DebugNotification(this, u,
      "Dimensions of ClipArea = (" + clipAreaY +" m x " + clipAreaY + " m )"));
			for (int i = 0; i < maxObstacles; i++) {
				int x = rand.nextInt((int)clipAreaX);
				int y = rand.nextInt((int)clipAreaY);
					
				MyPoint p = new MyPoint((double)x,(double)y);
				obstacles.add(p);
			}
	  }
	}
	
	/**
    * Constructs the voronoi diagrams from the fast Fortune's algorithm  <br>
    * <br>
    */
	protected void makeFortuneVoronoi() {
	   Fortune fortuneVoronoi = new Fortune(this);
		 try {
			fortuneVoronoi.drawVoronoi(obstacles,voronoiEdges);
		
			u.sendNotification(new LoaderNotification(this, u,
																								"Finished generating Fortune's Voronoi Diagrams"));
		 }
		 catch(Exception e) {
		  System.out.println("Could not draw Fortune's voronoi diagrams");
		}
		
		
		///////////////////////////////////////////////
    // |MF| clear Voronoi graph from split edges //
    ///////////////////////////////////////////////
    java.util.ArrayList checkEdges = new java.util.ArrayList();
    java.util.ArrayList dupEdges = new java.util.ArrayList();
 
    java.util.Iterator copyIter = voronoiEdges.iterator();
    while (copyIter.hasNext()) {
      MyPoint p = (MyPoint)copyIter.next();
      checkEdges.add(p);
      dupEdges.add(p);
    }
    voronoiEdges.clear();
    java.util.Iterator iter = checkEdges.iterator();
    int iterIndex = 0;
 
    // for all the edges
    while (iter.hasNext()) {
			MyPoint p1 = (MyPoint)iter.next();
      MyPoint p2 = (MyPoint)iter.next();
      iterIndex ++;
			//System.out.println("--> Processing " + p1.x + " " + p1.y + " " + p2.x + " " + p2.y);
			java.util.Iterator dupIter = dupEdges.iterator();
			int dupIterIndex = 0;
			boolean dup = false;
			
			// if another edge with the same starting point is present,
			// then that is a split edge, so the middle point is removed
			while (dupIter.hasNext()) {
				MyPoint p1dup = (MyPoint)dupIter.next();
				MyPoint p2dup = (MyPoint)dupIter.next();
				dupIterIndex++;
				//System.out.println("    against " + p1dup.x + " " + p1dup.y + " " + p2dup.x + " " + p2dup.y);
				if (p1dup.x == p1.x && p1dup.y == p1.y && (p2dup.x != p2.x || p2dup.y != p2.y)) {
					dup = true;
					//System.out.println("        found duplicate!");
					if (iterIndex < dupIterIndex) {
						voronoiEdges.add(p2);
						voronoiEdges.add(p2dup);
						//System.out.println("        fist time seen: add " + p2.x + " " + p2.y + " " + p2dup.x + " " + p2dup.y);
					}
					break;
				}
			}
 
			if (!dup) {
				voronoiEdges.add(p1);
				voronoiEdges.add(p2);
				//System.out.println("        not duplicate " + p1.x + " " + p1.y + " " + p2.x + " " + p2.y);
			}
		}
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
      "Loading SpaceGraph extension"));

    super.load(element);

		org.w3c.dom.Node n;
		boolean rand_obs = true;

		java.util.Random rand=u.getRandom();
    
	
		String s;
		
		s = element.getAttribute("spatial_model");
    if (s.length()>0) {
      spatialModel = (SpatialModel)u.getExtension(s);
    }
		
		else {
			spatialModel = (SpatialModel)u.getExtension("SpatialModel");
		}
		
		if(spatialModel==null)
      throw new Exception("A SpatialModel is missing!");

    s = element.getAttribute("max_obstacle");
    if(s.length()>0) {
      maxObstacles = (Integer.valueOf(s).intValue());
		}
		
    s = element.getAttribute("cluster");
    if((s.length()>0) && (Boolean.valueOf(s).booleanValue())) {
     
      org.w3c.dom.NodeList clustersList = element.getElementsByTagName("clusters");
			int nClustersType = clustersList.getLength();
			if(nClustersType > 1)
				throw new Exception("There cannot be more than one <clusters> tag !");	
			else if (nClustersType == 1) {
				rand_obs = false;
				org.w3c.dom.Element clustersElement = (org.w3c.dom.Element)clustersList.item(0);
		
				s = clustersElement.getAttribute("density");
				if(s.length()<=0)
					throw new Exception("density in <clusters> is missing!");
		
				double clustersDensity = Double.parseDouble(s);
				try {
					org.w3c.dom.NodeList clusterList = clustersElement.getElementsByTagName("cluster");
					if (clusterList.getLength() > 0) {
						generateClusters(clusterList,clustersDensity);
					}
					else {
						generateRandomClusters(clustersDensity);
					}
					setClusters();
				}
				catch (Exception e2) {
					System.out.println("Error in load SpaceGraph");
				}
			}
			else {
				rand_obs = false;
				generateRandomClusters(0);
				setClusters();
			}
		}
		
	
	  doubleFlow = spatialModel.getDirections();
		clipArea = spatialModel.getClipArea();
	
    if(rand_obs)
      distributeRandomObstacles();
		else
			distributeClusterObstacles();

		makeFortuneVoronoi();
		
		createFirstLayerElements();
		
		
		spatialModel.rebuildGraph();
		
		
		spatialModel.createSecondLayerElements();
		
		u.sendNotification(new LoaderNotification(this, u,
																								"Finished loading SpaceGraph extension"));
  }
}