package de.uni_stuttgart.informatik.canu.tripmodel.generators;

/**
 * <p>Title: Trip Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002-2003</p>
 * <p>Company: University of Stuttgart</p>
 * <p>Patches: </p>
 * <p>	v1.2 (30/11/2005) In order to reflect direction, 
 *												if we use a SpaceGraph elemet, we need to set the 
 *												reflect direction to true in both Elements. 
 *												Plus, the Generator returns the Algorithm used.  </p>
 * @author Illya Stepanov
 * @author v1.2 Jerome Haerri (haerri@ieee.org)
 * @version 1.2
 */

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.tripmodel.core.*;
import de.uni_stuttgart.informatik.canu.tripmodel.pathalgorithms.*;


/**
 * This class generates random trips for a node
 * <p>Patches: </p>
 * <p>	<i> Version 1.2 by Jerome Haerri (haerri@ieee.org) on 11/30/2005: 
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; In order to reflect direction, 
 *												if we use a SpaceGraph element with differentiated flows, we need to set the 
 *												reflect direction to true in both Elements. 
 * <br>  &nbsp;&nbsp;&nbsp;&nbsp; The Generator returns the Algorithm used.  </i></p>
 * @author 1.0-1.1 Illya Stepanov 
 * @author 1.2 Jerome Haerri
 * @version 1.2
 */
public class RandomTripGenerator extends ExtensionModule
                                       implements TripGenerator
{
  
	/**
	* Spatial Model
	*/
	protected SpatialModel spatialModel=null;
	
	/**
   * Minimal stay duration at destination (ms)
   */
  protected int minStay = 0;        // in ms
  /**
   * Maximal stay duration at destination (ms)
   */
  protected int maxStay = 0;        // in ms

  /**
   * Set of points to be used as destinations during node movements
   */
  protected java.util.ArrayList points = new java.util.ArrayList();
  
  /**
   * Path searching algorithm
   */
  protected PathSearchingAlgorithm algo = new Dijkstra();
  
  /**
   * Flag to reflect or ignore the road directions during the path calculation
   */
  protected int reflect_directions = PathSearchingAlgorithm.FLAG_IGNORE_DIRECTIONS;

  /**
   * Constructor
   */
  public RandomTripGenerator()
  {
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Random Trip Generation module";
  }
	
	/**
   * Returns the PathSearch Algorithm used. <br>
   * <br>
   * @return algo Algorithm used for Path Searching
	 * @since 1.2 
   */
	public  PathSearchingAlgorithm getAlgo()
  {
    return algo;
  }
	
  /**
   * Performs the module initialization. <br>
   * <br>
   * The method is called after finishing the scenario file processing.
   */
  public void initialize()
  {
    //SpatialModel spatialModel = (SpatialModel)u.getExtension("SpatialModel");
		// JHNote (06/02/2006): Now the SpatialModel is loaded at the beginning.
		
		Graph graph = spatialModel.getGraph(); 
		
    // check the points
    java.util.Iterator iter = points.iterator();
    while (iter.hasNext())
    {
      Point p = (Point)iter.next();

      if (graph==null)
      { 
        if((p.getX()<0.0)||(p.getY()<0.0)||
           (p.getX()>u.getDimensionX())||(p.getY()>u.getDimensionY()))
        {
          System.err.println("Fatal error: Position is outside Universe dimensions: Position3D("+p.getX()+","+p.getY()+")");
          System.exit(1);
        }
      }
      else
      {
        if((p.getX()<graph.getLeftmostCoordinate())||(p.getY()<graph.getLowermostCoordinate())||
           (p.getX()>graph.getRightmostCoordinate())||(p.getY()>graph.getUppermostCoordinate()))
        {
          System.err.println("Fatal error: Position is outside movement area graph: Position3D("+p.getX()+","+p.getY()+")");
          System.exit(1);
        }
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
    return 0;
  }

  /**
   * Generates a new trip for the node. <br>
   * <br>
   * @param node node
   * @return new trip for node
   */
  public Trip genTrip(Node node)
  {
    java.util.Random rand = u.getRandom();

    //SpatialModel spatialModel = (SpatialModel)u.getExtension("SpatialModel");
		// JHNote (06/02/2006): Now the SpatialModel is loaded at the beginning.
		
		Graph graph = spatialModel.getGraph();
		
    // check if the set of points is defined
    if (points.size()>0)
    {
      Point p = (Point)points.get(rand.nextInt(points.size()));

      if (graph==null)
      {
        Trip trip = new Trip();

        java.util.ArrayList path = trip.getPath();
        path.add(new Point(node.getPosition().getX(), node.getPosition().getY()));
        path.add(new Point(p.getX(), p.getY()));

        return trip;
      }
      else
      {
        // get a path from the current location to the destination
        Trip trip = algo.getPath(spatialModel, node,
          new Point(node.getPosition().getX(), node.getPosition().getY()),
          new Point(p.getX(), p.getY()), reflect_directions);
        if (trip==null)
        {
          // add an empty trip
          trip = new Trip();

          java.util.ArrayList path = trip.getPath();
          path.add(new Point(node.getPosition().getX(), node.getPosition().getY()));
          path.add(new Point(node.getPosition().getX(), node.getPosition().getY()));
        }
        
        return trip;
      }
    }
    else
    {
      if (graph==null)
      {
        // generate a path to a randomly chosen point
        double x = rand.nextDouble()*u.getDimensionX();
        double y = rand.nextDouble()*u.getDimensionY();

        Trip trip = new Trip();

        java.util.ArrayList path = trip.getPath();
        path.add(new Point(node.getPosition().getX(), node.getPosition().getY()));
        path.add(new Point(x, y));
      
        return trip;
      }
      else
      {
        java.util.ArrayList vertices = spatialModel.getGraph().getVertices();

        // choose a random destination
        Vertex vd = (Vertex)vertices.get(rand.nextInt(vertices.size()));

        // get a path from the current location to the destination
        Trip trip = algo.getPath(spatialModel, node,
          new Point(node.getPosition().getX(), node.getPosition().getY()),
          new Point(vd.getX(), vd.getY()), reflect_directions);
        if (trip==null)
        {
          // add an empty trip
          trip = new Trip();

          java.util.ArrayList path = trip.getPath();
          path.add(new Point(node.getPosition().getX(), node.getPosition().getY()));
          path.add(new Point(node.getPosition().getX(), node.getPosition().getY()));
        }
        
        return trip;
      }
    }
  }
  
  /**
   * Chooses a time of staying at the current position. <br>
   * <br>
   * @param node node
   * @return stay duration (in ms)
   */
  public int chooseStayDuration(Node node)
  {
    return (int)(minStay+(maxStay-minStay)*u.getRandom().nextFloat());
  }
  
  /**
   * Initializes the object from XML tag. <br>
	 * <i> Version 1.2 by  Jerome Haerri (haerri@ieee.org) on 11/30/2005: 
	 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Patch to reflect the stong links between reflect_direction in SpaceGraph and here.
	 * <br> &nbsp;&nbsp;&nbsp;&nbsp; In order to reflect direction here, if we use a space graph, 
	 *													we need to set the reflect direction to true in both case. </i>
   * <br>
   * @param element source tag
   * @throws Exception Exception if parameters are invalid
   */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading RandomTripGenerator extension"));

    super.load(element);
		
		// JHNote (06/02/2006): Now the SpatialModel is loaded at the beginning.
		String sm = element.getAttribute("spatial_model");
    if (sm.length()>0) {
      spatialModel = (SpatialModel)u.getExtension(sm);
    }
		
		else {
			spatialModel = (SpatialModel)u.getExtension("SpatialModel");
		}
		
		if(spatialModel==null)
      throw new Exception("A SpatialModel is missing!");
		
    org.w3c.dom.Node n;

    String classTag = element.getAttribute("path_algorithm").trim();
    if (classTag.length()!=0)
    {
      algo = (PathSearchingAlgorithm)Class.forName(classTag).newInstance();
      
      // handle stoch path selection parameters
      if (algo instanceof PedestrianStochPathSelection && !(algo instanceof SpeedPathSelection))
      {
        String param = element.getAttribute("theta").trim();
        if(param.length()==0)
          throw new Exception("\"theta\" attribute of path selection is missing!");
        float theta = Float.parseFloat(param);
        
        ((PedestrianStochPathSelection)algo).setTheta(theta);
      }

      // handle speed path selection parameters
      if (algo instanceof SpeedPathSelection)
      {
        String param = element.getAttribute("speedWeight").trim();
        if(param.length()==0)
          throw new Exception("\"speedWeight\" attribute of path selection is missing!");
        float speedWeight = Float.parseFloat(param);
        
        ((SpeedPathSelection)algo).setSpeedWeight(speedWeight);
      }
    }

    n = element.getElementsByTagName("reflect_directions").item(0);
    if((n!=null)&&(Boolean.valueOf(n.getFirstChild().getNodeValue()).booleanValue()))
    {
      reflect_directions = PathSearchingAlgorithm.FLAG_REFLECT_DIRECTIONS;
    }
		
		// JHNote (30/11/2005) code added to reflect the stong links between reflect_direction in SpaceGraph and here
		// In order to reflect direction here, if we use a space graph, we need to set the 
		// reflect direction to true in both case.
		// JHNote (15/09/2006): the directions are now in SpatialModel
		if ((spatialModel != null) && (spatialModel.getDirections() != Boolean.valueOf(n.getFirstChild().getNodeValue()).booleanValue())) {
			throw new Exception("\"reflect_direction\" attribute of path selection need to be identical to the one on the SpatialModel!");
		}
			
    n = element.getElementsByTagName("points").item(0);
    if(n!=null)
    {
      String fileSource = n.getFirstChild().getNodeValue();

      java.io.BufferedReader source = new java.io.BufferedReader(new java.io.FileReader(fileSource));

      String s;
      // read next record
      while ((s = source.readLine())!=null)
      {
        String ss[] = s.split(" ");

        double x = Double.parseDouble(ss[0]);
        double y = Double.parseDouble(ss[1]);
        
        points.add(new Point(x, y));
      }
    }

    n = element.getElementsByTagName("minstay").item(0);
    if(n==null)
      throw new Exception("<minstay> is missing!");
    minStay = (int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);

    n = element.getElementsByTagName("maxstay").item(0);
    if(n==null)
      throw new Exception("<maxstay> is missing!");
    maxStay = (int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);

    // checkout
    if ( (minStay<0)||(maxStay<minStay) )
      throw new Exception("Trip generation parameters are invalid:\n"
        +"minStay="+(float)minStay/1000+"(s), maxStay="+(float)maxStay/1000+"(s)");

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading RandomTripGenerator extension"));
  }//proc
}
