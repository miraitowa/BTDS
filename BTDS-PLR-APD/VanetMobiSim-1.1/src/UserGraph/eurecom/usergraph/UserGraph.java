package eurecom.usergraph;

/**
 * <p>Title: UserGraph creator</p>
 * <p>Description: Creates a User Defined Graph </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */


import java.io.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import eurecom.spatialmodel.extensions.*;

/**
 * This class is used to randomly generate a Spatial Model out of 
 * a user defined graph and user defined attributes/
 *
 * @author Jerome Haerri 
 * @version 1.0
 */
public class UserGraph extends ExtensionModule {
  

 /**
	 * contains all the vertices of the infrastructure graph
	 */
	private java.util.HashMap vertices = null;

	/**
	 * contains all the vertices of the infrastructure graph
	 */
	private java.util.ArrayList edges = null;
	
	/**
	 * contains the speed values for each edge in the infrastructure graph
	 */
	private java.util.Map speedAttribute = null;
		
  /**
   * Storage for default attributes values:
   * Key - Attribute id, Value - default value
   */
  protected java.util.Map defaultAttributesValues = new java.util.HashMap();

  /**
   * Spatial Model
   */
  protected SpatialModel spatialModel=null;

	/**
   * Clipping Region
   */
  protected Polygon clipArea;
	
	/**
   * Double Flow disabled
   */
	protected boolean doubleFlow = false;
	
	/**
   * Constructor
   */
  public UserGraph()
  {
    super("UserGraph");
		
		vertices = new java.util.HashMap();
		edges = new java.util.ArrayList();
		speedAttribute = new java.util.HashMap();

  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "UserGraph creator module";
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
    * Constructs elements from loaded voronoi edges. <br>
    * <br>
    * @throws Exception Exception if more than 4 coordinates per roadElement
    */
	
    protected void createFeatureElements() throws Exception {
			try {
				
				java.util.Map elements = spatialModel.getElements();
				java.util.Random rand=u.getRandom();
				
				// JHNote (18/08/2005): We only consider roadElements and junctions.
				String class_code = "41";
				String subclass_code = "10";
				String subclass_code_junction = "20";
				
				String line;
				String id,id_junction1,id_junction2;
				SpatialModelElement element,junction2,junction1;

				
				java.util.Iterator graphEdgeIter = edges.iterator();
				while (graphEdgeIter.hasNext()) {
					 Edge edge = (Edge)graphEdgeIter.next();
					 
					 Vertex vertex1 = (Vertex)vertices.get(edge.getID1());
					 Vertex vertex2 = (Vertex)vertices.get(edge.getID2());
					 
					 if (vertex1==null || vertex2==null)
						 continue;
					 
					 id = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					 
					// We add now the first road with the initial directional flow
					Polyline shape = new Polyline();
					java.util.ArrayList points = shape.getPoints();
					
					Point pointf = new Point(vertex1.getID(),vertex1.getX(),vertex1.getY(),vertex1.getX()+"_"+vertex1.getY());
					points.add(pointf);
					
					Point pointt = new Point(vertex2.getID(),vertex2.getX(),vertex2.getY(),vertex2.getX()+"_"+vertex2.getY());
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
						if (clipArea!=null) {
							if (clipArea.contains(pointf)) {
							  elements.put(id_junction1, junction1);  
							}
						}
						else {
							elements.put(id_junction1, junction1);  
						}
					}
					
					if (newPointt) {
						id_junction2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
						junction2 = new SpatialModelElement(id_junction2, class_code, subclass_code_junction, pointt);
						junction2.getAttributes().put("JT","1"); // junction type : mini roundabout
						
						if (clipArea!=null) {
							if (clipArea.contains(pointt)) {
							  elements.put(id_junction2, junction2);
							}
						}
						else {
							elements.put(id_junction2, junction2);  
						}
					}
					
					element = new SpatialModelElement(id, class_code, subclass_code, shape);
					element.getAttributes().put("DF","1"); // directional flow : 1= open to all direction
					element.getAttributes().put("NL","1"); // number of lane : 1= one lane in each directions
					element.getAttributes().put("VT","0"); // vehicle type : 0= all vehicles

					
					String speed = (String)speedAttribute.get(edge.getID1()+"_"+edge.getID2());
					if (speed != null)
					  element.getAttributes().put("SP",speed); // Speed Restriction
					else	
						element.getAttributes().put("SP","13.88"); // Speed Restriction (maxmimum speed) : 13.88m/s= 50km/h
					
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
   * Constructs simple elements from user defined data
	 * This method creates junctions and road elements.
   */
  protected void createFirstLayerElements() {
    try {
			createFeatureElements();
			if (doubleFlow)
				spatialModel.createDoubleFlowRoads();
		}
		catch (Exception e2) {
				System.out.println("Error in createFirstLayerElements");
				e2.printStackTrace();
				System.exit(-1);
			}
  }
	
	
	/**
    * Initializes the graph from XML tag. <br>
    * <br>
		* <i>Version 1.2 by Jerome Haerri (haerri@ieee.org): 
		* <br>
		* &nbsp;&nbsp;&nbsp;&nbsp;A Spatial model, if defined, can only be loaded after the graph model. This is done in order to be sure that,
		* when the SpatialModel extension is loaded and a Graph model found, the spatial model is able to load the graph. </i>
		* <br>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading UserGraph extension"));
		
		super.load(element);
		
		String s;
		
		s = element.getAttribute("spatial_model");
    if (s.length()>0) {
      spatialModel = (SpatialModel)u.getExtension(s);
    }
		
		else {
			spatialModel = (SpatialModel)u.getExtension("SpatialModel");
		}
		
		if (spatialModel == null)
			throw new Exception("The Spatial Model extention has not been loaded");
		
		clipArea = spatialModel.getClipArea();

    // get coefficient
    double k=1.0;
    String kTag = element.getAttribute("k");
    if (kTag.length()>0)
    {
      k=Double.parseDouble(kTag);
    }

    if(k==0.0)
      throw new Exception("Invalid coefficient: "+k);

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
      if(tag.equals("vertex"))
      {
         u.sendNotification(new LoaderNotification(this, u,
           "Processing <vertex> tag"));

         org.w3c.dom.Element e=(org.w3c.dom.Element)item;
         org.w3c.dom.Node n;

         n=e.getElementsByTagName("id").item(0);
         if(n==null)
           throw new Exception("Vertex <id> is missing!");
				 
         String id=n.getFirstChild().getNodeValue();

         n=e.getElementsByTagName("name").item(0);
         String name;
         if(n==null)
           name="";
          else
           name=n.getFirstChild().getNodeValue();

         n=e.getElementsByTagName("x").item(0);
         if(n==null)
           throw new Exception("Vertex <x> is missing!");
				 
         String x=n.getFirstChild().getNodeValue();

         n=e.getElementsByTagName("y").item(0);
         if(n==null)
           throw new Exception("Vertex <y> is missing!");
				 
         String y=n.getFirstChild().getNodeValue();

         double d_x=Double.parseDouble(x)*k;
         double d_y=Double.parseDouble(y)*k;
				 
				 Vertex vertex = new Vertex(id, name, Double.toString(d_x), Double.toString(d_y));
				 vertices.put(id,vertex);
				 
         u.sendNotification(new LoaderNotification(this, u,
           "Finished processing <vertex> tag"));
      }
      else
      if(tag.equals("edge"))
      {
         u.sendNotification(new LoaderNotification(this, u,
           "Processing <edge> tag"));

         org.w3c.dom.Element e=(org.w3c.dom.Element)item;
         org.w3c.dom.Node n;

         n=e.getElementsByTagName("v1").item(0);
         if(n==null)
           throw new Exception("Edge <v1> is missing!");
         String v1=n.getFirstChild().getNodeValue();

         n=e.getElementsByTagName("v2").item(0);
         if(n==null)
           throw new Exception("Edge <v2> is missing!");
         String v2=n.getFirstChild().getNodeValue();
				 
				 Edge edge = new Edge(v1,v2);
				 edges.add(edge);
				 
				 String speed = null;
				 n=e.getElementsByTagName("speed").item(0);
         if(n!=null)
					 speed=n.getFirstChild().getNodeValue();
				 else
					 speed = "13.88"; // default speed set to 50km/h
				 
				 speedAttribute.put(v1+"_"+v2,speed);
				 
         u.sendNotification(new LoaderNotification(this, u,
           "Finished processing <edge> tag"));
      }
    }
		
		doubleFlow = spatialModel.getDirections();
		
		createFirstLayerElements();
		spatialModel.rebuildGraph();
		
		spatialModel.createSecondLayerElements();
    
		u.sendNotification(new LoaderNotification(this, u,
      "Finished loading UserGraph extension"));
  }
}

