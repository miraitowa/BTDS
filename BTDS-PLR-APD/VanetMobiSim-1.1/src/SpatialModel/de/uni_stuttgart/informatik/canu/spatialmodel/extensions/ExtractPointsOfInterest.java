package de.uni_stuttgart.informatik.canu.spatialmodel.extensions;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;

/**
 * <p>Title: Spatial Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

/**
 * The module extracts points of interest from the Spatial Model
 * @author Illya Stepanov 
 */
public class ExtractPointsOfInterest extends ExtensionModule
{
  /**
   * Code of elements to be extracted
   */
  protected String code;
  
  /**
   * Output Stream
   */
  protected java.io.PrintStream o = System.err;
   
  /**
   * Spatial Model
   */
  protected SpatialModel spatialModel;

  /**
   * Constructor
   */
  public ExtractPointsOfInterest()
  {
    super("ExtractPointsOfInterest");
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Point of Interest extracting module";
  }

  /**
   * Performs the module initialization. <br>
   * <br>
   * The method is called after finishing the scenario file processing.
   */
  public void initialize()
  {
    java.util.Iterator iter = spatialModel.getElements().values().iterator();
    while (iter.hasNext())
    {
      SpatialModelElement e = (SpatialModelElement)iter.next();
      if ( (e.getGeometry() instanceof Point) &&
           (e.getClassCode()+e.getSubClassCode()).startsWith(code) )
      {
        Point p = (Point)e.getGeometry();
        o.println(p.getX()+" "+p.getY());
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
    return -1;
  }

  /**
   * Loads simulation parameters from XML tag. <br>
   * <br>
   * @throws Exception Exception if parameters are invalid
   */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading ExtractPointsOfInterest extension"));
    
    super.load(element);

    org.w3c.dom.Node n;

    String outName = element.getAttribute("output");
    if (outName.length()>0)
      o = new java.io.PrintStream(new java.io.FileOutputStream(outName));    

    n = element.getElementsByTagName("theme_code").item(0);
    if(n==null)
      throw new Exception("<theme_code> is missing!");
    code = n.getFirstChild().getNodeValue();
		
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
      "Finished loading ExtractPointsOfInterest extension"));
  }
}