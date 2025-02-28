package de.uni_stuttgart.informatik.canu.spatialmodel.extensions;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.SpatialModel;

/**
 * Title:        Spatial Model
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

/**
 * The module calculates nodes' density. <br>
 * <br>
 * Reports nodes' density information with a certain time interval
 * @author Illya Stepanov
 */
public class NodeDensityMonitor extends ExtensionModule
{
  
	/**
	* Spatial Model
	*/
	protected SpatialModel spatialModel=null;
	
	/**
   * Step for time monitoring (in ms)
   */
  protected int step;
  
  /**
   * Cell size
   */
  protected int cell_size;

  /**
   * Output Stream
   */
  protected java.io.PrintStream o = System.err;

  /**
   * Constructor
   */
  public NodeDensityMonitor()
  {
    super("NodeDensityMonitor");
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
    Universe u = Universe.getReference();

    if ( (u.getTime() % step == 0) )
    {
      activate();
    }
    
    return 0;
  }

  /**
   * Calculates and outputs the data
   */
  public void activate()
  {
    if (u.getTimeInSteps()==0)
      return;

    o.println("Density information at time: "+u.getTimeAsString());

    // get the dimensions of the area
    int min_x=0, max_x=(int)u.getDimensionX();
    int min_y=0, max_y=(int)u.getDimensionY();

    // check if the Spatial Model presents
   // SpatialModel spatialModel = (SpatialModel)u.getExtension("SpatialModel");
    if (spatialModel!=null)
    {
      Graph graph = spatialModel.getGraph();
      min_x = (int)graph.getLeftmostCoordinate();
      max_x = (int)graph.getRightmostCoordinate();
      min_y = (int)graph.getLowermostCoordinate();
      max_y = (int)graph.getUppermostCoordinate();
    }

    // output information about the concentration of nodes in single cell
    java.util.ArrayList nodes = (java.util.ArrayList)u.getNodes().clone();
    for (int y=min_y; y<max_y; y+=cell_size)
    {
      float my = y+cell_size;

      for (int x=min_x; x<max_x; x+=cell_size)
      {
        float mx = x+cell_size;

        int n=0;

        // iterate the nodes
        java.util.Iterator iter = nodes.iterator();
        while (iter.hasNext())
        {
          Node node=(Node)iter.next();

          // check if node is in the cell
          if ((node.getPosition().getX()>=x)&&(node.getPosition().getX()<=mx)
            &&(node.getPosition().getY()>=y)&&(node.getPosition().getY()<=my))
          {
            n++;

            // avoid counting twice the node on the border
            iter.remove();
          }
        }

        o.print("\t"+n);
      }
      o.println();
    }

    o.println();
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Density monitoring module";
  }

  /**
    * Initializes the object from XML tag. <br>
    * <br>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
  public void load(org.w3c.dom.Element element) throws java.lang.Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading DensityMonitor extension"));

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
      throw new Exception("SpatialModel instance does not exist!");
		
    org.w3c.dom.Node n;
		
    String outName = element.getAttribute("output");
    if (outName.length()>0)
      o = new java.io.PrintStream(new java.io.FileOutputStream(outName));    

    n=element.getElementsByTagName("step").item(0);
    if(n==null)
      throw new Exception("<step> is missing!");
    step=(int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000.0f);

    // checkout
    if (step<=0)
      throw new Exception("Step value is invalid: "+step);

    n = element.getElementsByTagName("cell_size").item(0);
    if(n==null)
      throw new Exception("<cell_size> is missing!");
    cell_size = Integer.parseInt(n.getFirstChild().getNodeValue());

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading DensityMonitor extension"));
  }
}
