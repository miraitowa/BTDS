package de.uni_stuttgart.informatik.canu.spatialmodel.geometry;

/**
 * <p>Title: Spatial Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * <p>Patches: </p>
 * <p> <i> Version 1.2 by Jerome Haerri (haerri@ieee.org) on 03/02/2006: 
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;  GeometryElement contains an unique identifier. This ID is needed
 * in order to create a unique Point/Line/AreaGDFFeature Record for the GDFWriter.</i></p>
 * @author 1.1 Illya Stepanov
 * @author 1.2 Jerome Haerri
 * @version 1.2 
 */

/**
 * This class is a base class for geometrical elements
 * <p>Patches: </p>
 * <p> <i> Version 1.2 by Jerome Haerri (haerri@ieee.org) on 03/02/2006: 
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;  GeometryElement contains an unique identifier. This ID is needed
 * in order to create a unique Point/Line/AreaGDFFeature Record for the GDFWriter.</i></p>
 * @author 1.1 Illya Stepanov
 * @author 1.2 Jerome Haerri
 * @version 1.2 
 */
abstract public class GeometryElement
{
  
	/**
	* GeometryElement unique identifier
	*/
	protected String id = null;
	
	/**
   * Constructor
   */
  public GeometryElement(){
  }

	/**
   * Sets the ID. <br>
   * <br>
   * @param id unique identifier
	 * @since 1.2
  */	
	public void setID(String id) {
	  this.id = id;
	}
	
	/**
   * Returns the Position3D id. <br>
   * <br>
   * @return id unique identifier
	 * @since 1.2
  */
	public String getID() {
	  return id;
	}
	
  /**
   * Checks if this geometry element contains another element. <br>
   * <br>
   * @param e element being checked
   */
  abstract public boolean contains(GeometryElement e);
}