package de.uni_stuttgart.informatik.canu.tripmodel.generators;

/**
 * <p>Title: Trip Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002-2003</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.tripmodel.core.*;

/**
 * This class randomly chooses initial position for a node
 * @author Illya Stepanov
 */
public class RandomInitialPositionGenerator extends ExtensionModule
                                            implements InitialPositionGenerator
{
  
	/** 
	* Spatial Model
	*/ 
	protected SpatialModel spatialModel = null;
	
	/**
   * Set of points to be used as node initial positions
   */
  protected java.util.ArrayList points = new java.util.ArrayList();
  
  /**
   * Constructor
   */
  public RandomInitialPositionGenerator()
  {
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Random Initial Position Generation module";
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
   * Chooses the node's initial position. <br>
   * <br>
   * @param node node
   * @return node's initial position
   */
  public Point getInitialPosition(Node node)
  {
    java.util.Random rand = u.getRandom();

    // check if the set of points is defined
    if (points.size()>0)
    {
      return (Point)points.get(rand.nextInt(points.size()));
    }
    else
    {
      // get a random point of the movement area
      //SpatialModel spatialModel = (SpatialModel)u.getExtension("SpatialModel");
			// JHNote (06/02/2006): Now the SpatialModel is loaded at the beginning.
			
			Graph graph = spatialModel.getGraph();
		
      // check if restricted with the graph
      if (graph==null)
      {
        double x=rand.nextDouble()*u.getDimensionX();
        double y=rand.nextDouble()*u.getDimensionY();
      
        return new Point(x, y);
      }
      else
      {
        Vertex v = (Vertex)graph.getVertices().get(rand.nextInt(graph.getVertices().size()));

        return new Point(v.getX(), v.getY());
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
      "Loading RandomInitialPositionGenerator extension"));

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

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading RandomInitialPositionGenerator extension"));
  }  
}