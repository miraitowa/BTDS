package de.uni_stuttgart.informatik.canu.spatialmodel.extensions;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.senv.core.Edge;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;

/**
 * <p>Title: Spatial Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002-2003</p>
 * <p>Company: University of Stuttgart</p>
 * <p>Patches: </p>
 * <p>	 v1.2 (18/10/2005): Replaced the Xgraph format with a XFIG dump format.</p>
 * @author Illya Stepanov
 * @author v1.2: Jerome Haerri (haerri@ieee.org)
 * @version 1.2
 */

/**
 * This class is used to dump the Spatial Model
 * <p>Patches: </p>
 * <p> <i> Version 1.2 by Jerome Haerri (haerri@ieee.org) on 10/18/2005: 
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Replaced the Xgraph format with a XFIG format.</i></p>
 * @author 1.0-1.1 Illya Stepanov 
 * @author 1.2 Jerome Haerri
 * @version 1.2
 */
public class DumpSpatialModel extends ExtensionModule
{
  /**
   * Spatial Model
   */
  protected SpatialModel spatialModel;

  /**
   * Output Stream
   */
  protected java.io.PrintStream o = System.err;

  /**
   * Constructor
   */
  public DumpSpatialModel()
  {
    super("DumpSpatialModel");
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Spatial Model dumping module";
  }

  /**
   * Performs the module initialization. <br>
	 * <i>Version 1.2 by  Jerome Haerri (haerri@ieee.org): 
	 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Replaced the Xgraph format with a XFIG dump format.</i>
   * <br>
   * The method is called after finishing the scenario file processing.
	 * @since v1.2: 
   */
  public void initialize()
  {
    java.util.Map elements = spatialModel.getElements();

    // Number of features belonging to a class
    java.util.Map n_features = new java.util.TreeMap();

    // Iterate the elements
    java.util.Iterator iter = elements.values().iterator();
    while (iter.hasNext())
    {
      SpatialModelElement element = (SpatialModelElement)iter.next();
      String code = element.getClassCode()+element.getSubClassCode();

      Integer i = (Integer)n_features.get(code);
      if (i==null)
        i = new Integer(1);
      else
        i = new Integer(i.intValue()+1);
      n_features.put(code, i);
    }
		
		/* JHNote (18/10/2005): We changed the xgraph format to xfig format */
		o.println("#FIG 3.2");
		o.println("Portrait");
		o.println("Center");
		o.println("Metric");
		o.println("A4 ");     
		o.println("100.00");
		o.println("Single");
		o.println("-2");
		o.println("300 2");
		
		
		o.println("# Summary of features found:");
    iter = n_features.keySet().iterator();
    while (iter.hasNext())
    {
      String code = (String)iter.next();
      int n = ((Integer)n_features.get(code)).intValue();
      o.println("# "+code+" "+n);
    }

    o.println("# Area dimensions: "+u.getDimensionX()+' '+u.getDimensionY());
    
    Graph graph = spatialModel.getGraph();
    o.println("# Movement area dimensions: "+(float)(graph.getRightmostCoordinate()-graph.getLeftmostCoordinate())+
        ' '+(float)(graph.getUppermostCoordinate()-graph.getLowermostCoordinate()));
    o.println("# Graph has "+graph.getVertices().size()+" vertices, "
        +graph.getEdges().size()+" edges ");

    o.println("# Movement area graph (in xfig format):");
    iter = graph.getInfrastructureGraph().getEdges().iterator();
    while (iter.hasNext())
    {
      Edge edge = (Edge)iter.next();
		//	2 1 0 1 0 7 50 -1 -1 0.000 0 0 -1 0 0 2
			SpatialModelElement element = spatialModel.mapEdgeToElement(edge);
			if (element !=null) {
			  if ((String)element.getAttributes().get("NL") != "1") {
					//o.println("2 1 0 2 0 7 0 0 -1 0.000 0 0 -1 0 0 2");
					o.println("2 1 0 3 1 7 50 -1 -1 0.000 0 0 -1 0 0 2");
					//o.println("2 1 0 2 3 7 50 -1 -1 0.000 0 0 -1 0 0 2");
					o.println((int)edge.getV1().getX()+" "+(int)edge.getV1().getY()+" "+(int)edge.getV2().getX()+" "+(int)edge.getV2().getY());
				}
				else {
				  o.println("2 1 0 1 0 7 0 0 -1 0.000 0 0 -1 0 0 2");
					o.println((int)edge.getV1().getX()+" "+(int)edge.getV1().getY()+" "+(int)edge.getV2().getX()+" "+(int)edge.getV2().getY());
				}
			
			}
			else {
			  o.println("2 1 0 1 0 7 0 0 -1 0.000 0 0 -1 0 0 2");
        o.println((int)edge.getV1().getX()+" "+(int)edge.getV1().getY()+" "+(int)edge.getV2().getX()+" "+(int)edge.getV2().getY());
			}
    }
		
		
		
		
		
    // Display a number of features found
    /*o.println("! Summary of features found:");
    iter = n_features.keySet().iterator();
    while (iter.hasNext())
    {
      String code = (String)iter.next();
      int n = ((Integer)n_features.get(code)).intValue();
      o.println("! "+code+" "+n);
    }

    o.println("! Area dimensions: "+u.getDimensionX()+' '+u.getDimensionY());
    
    Graph graph = spatialModel.getGraph();
    o.println("! Movement area dimensions: "+(float)(graph.getRightmostCoordinate()-graph.getLeftmostCoordinate())+
        ' '+(float)(graph.getUppermostCoordinate()-graph.getLowermostCoordinate()));
    o.println("! Graph has "+graph.getVertices().size()+" vertices, "
        +graph.getEdges().size()+" edges ");

    o.println("! Movement area graph (in xgraph.exe format):");
    iter = graph.getInfrastructureGraph().getEdges().iterator();
    while (iter.hasNext())
    {
      Edge edge = (Edge)iter.next();

      o.println(edge.getV1().getX()+" "+edge.getV1().getY());
      o.println(edge.getV2().getX()+" "+edge.getV2().getY());

      o.println("next");
    }*/
  }

  /**
   * Execute the extension. <br>
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
   * Initializes simulation parameters from XML tag. <br>
   * <br>
   * @throws Exception Exception if parameters are invalid
   */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading DumpSpatialModel extension"));
    
    super.load(element);

    String outName = element.getAttribute("output");
    if (outName.length()>0)
      o = new java.io.PrintStream(new java.io.FileOutputStream(outName));    
		
		
		
		// JHNote (06/02/2006): We let the user change the name of the SpatialModel
		//spatialModel = (SpatialModel)u.getExtension("SpatialModel");
    //if (spatialModel==null)
    //  throw new Exception("SpatialModel instance does not exist!");
		
		String sm = element.getAttribute("spatial_model");
    if (sm.length()>0) {
      spatialModel = (SpatialModel)u.getExtension(sm);
    }	
		else {
			spatialModel = (SpatialModel)u.getExtension("SpatialModel");
		}
		
		if(spatialModel==null)
      throw new Exception("SpatialModel instance does not exist!");
		
    
      
    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading DumpSpatialModel extension"));
  }
}