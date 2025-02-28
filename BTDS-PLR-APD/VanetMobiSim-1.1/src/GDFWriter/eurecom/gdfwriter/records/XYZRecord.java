package eurecom.gdfwriter.records;

/**
 * <p>Title: Coordinate Record</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */

/**
 * Coordinate Record
 * @author Jerome Haerri 
 */
public class XYZRecord {
  
	
	/**
   * GDCPoint Geodetic Coordinates Point (long/lat/elev)
   */
  public class GDCPoint {
    /**
     * Longiture-value
     */
    protected int longitude;

    /**
     * Latitude-value
     */
    protected int latitude;

    /**
     * Elevation-value
     */
    protected int elevation;

    /**
     * Constructor
     * @param latitude GDC latitude (in degree)
		 * @param longitude GDC longitude (in degree)
		 * @param elevation GDC elevation (in meter)
     */
    public GDCPoint(int latitude,int longitude,int elevation) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.elevation = elevation;
    }
		
		/**
     * Returns the point latitude
     */
		public int getLatitude() {
		  return latitude;
		}
		
		/**
     * Returns the point longitude
     */
		public int getLongitude() {
		  return longitude;
		}
		
		/**
     * Returns the point elevation
     */
		public int getElevation() {
		  return elevation;
		}
		
		
  }
	
	
  /**
   * Record ID
   */
  protected String id;
	
	/**
	 * Geometry Type Code
	 */
	 protected int geomTypeCode;
	 
	 /**
   * Quality Code
   */
  protected int QualCode;
	
	/**
	* Source Description Indentifier
	*/
	protected int sourceDesc;
	
	/**
   * List of XYZ points
   */
  protected java.util.ArrayList points = new java.util.ArrayList();

	
  /**
   * Constructor
   * @param id Attribute id
	 * @param sourceDesc Source Description Indentifier
   */
  public XYZRecord(String id, int sourceDesc) {
    this.id = id;
		this.sourceDesc = sourceDesc;
  }
	
	
	/**
   * Gets the XYZRecord ID. <br>
   * <br>
   * @return record's ID
   */
  public String getID() {
    return id;
  }

	
	/**
	* Adds a new GDCPoint
	* @param latitude GDC latitude (in degree)
	* @param longitude GDC longitude (in degree)
	* @param elevation GDC elevation (in meter)
	*/
	public void addPoint(int latitude, int longitude, int elevation) {
	  GDCPoint point = new GDCPoint(latitude,longitude,elevation);
		points.add(point);
	}
	
	/**
	* Gets the list of GDC Coordinates
	* @return points
	*/
	public java.util.ArrayList getPoints() {
	  return points;
	}
	
	/**
	* Gets the Quality Type
	* @return QualCode
	*/
	public String getQType() {
	  return (String.valueOf(QualCode));
	}
	
	/**
	* Gets the Geometry Type
	* @return geomTypeCode
	*/
	public String getGType() {
	  return (String.valueOf(geomTypeCode));
	}
	
	
	/**
	* Gets the Description ID
	* @return Description ID
	*/
	public String getDescr() {
	  return (String.valueOf(sourceDesc));
	}
}
