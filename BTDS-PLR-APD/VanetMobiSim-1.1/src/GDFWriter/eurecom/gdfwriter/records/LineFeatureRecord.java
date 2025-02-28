package eurecom.gdfwriter.records;

/**
 * <p>Title: LineFeature Record</p>
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
public class LineFeatureRecord {
  
	
  /**
   * Record ID
   */
  protected String id;
	
	/**
	* Source Description Indentifier
	*/
	protected int sourceDesc;
	
	/**
	* Feature Class Code
	*/
	protected String code;
	
	/**
	* Split Indicator
	* 0 = entire feature
	* 1 = Line represents part of a split feature
	*/
	protected int split;
	
	/**
	 * List of edges record IDs composing the line feature
	 */
	protected java.util.ArrayList edgesID = new java.util.ArrayList();
	
	/**
	 * List of the respective edge directions
	 */
	protected java.util.ArrayList edgesDirID = new java.util.ArrayList();
	
	/**
	 * List of the segmented attributes IDs of this line feature
	 */
	protected java.util.ArrayList attributesID = new java.util.ArrayList();
	
	/**
	* Point record ID of the initial Point feature of this Line Feature
	*/
	protected String fromPointID;
	
	/**
	* Point record ID of the final Point of this Line Feature
	*/
	protected String toPointID;
	
  /**
   * Constructor
   * @param id Edge Record id
	 * @param code Feature Class Code
	 * @param sourceDesc Source Description Indentifier
	 * @param edgesID list of edge record IDs
	 * @param edgesDirID list of edge directions
	 * @param attributesID list of attribute record IDs
	 * @param fromPointID ID of the initial PointFeatureRecord
	 * @param toPointID ID of the final PointFeatureRecord
   */
  public LineFeatureRecord(String id, String code, int sourceDesc, java.util.ArrayList edgesID, 
													 java.util.ArrayList edgesDirID, java.util.ArrayList attributesID, 
													 String fromPointID, String toPointID) {
    this.id = id;
		this.code = code;
		this.sourceDesc = sourceDesc;
		this.fromPointID=fromPointID;
		this.toPointID=toPointID;
		this.attributesID.addAll(attributesID);
		this.edgesID.addAll(edgesID);
		this.edgesDirID.addAll(edgesDirID);
  }
		
	/**
   * Gets the Line Feature Record ID. <br>
   * <br>
   * @return record's ID
   */
  public String getID() {
    return id;
  }
	
	/**
	* Gets the Description ID
	* @return Description ID
	*/
	public String getDescr() {
	  return (String.valueOf(sourceDesc));
	}
	
	/**
	* Gets the Feature code
	* @return code 
	*/
	public String getCode() {
	  return code;
	}
	
	/**
	* Gets the Split indicator
	* @return split 
	*/
	public String getSplit() {
	  return (String.valueOf(split));
	}
	

	/**
	* Gets the list of Edges  contained by this Line feature
	* @return edgesID
	*/
	public java.util.ArrayList getEdgesID() {
	  return edgesID;
	}
	
	/**
	* Gets the list of Edge Directions attached to the Edges contained by this Line feature
	* @return edgesDirID
	*/
	public java.util.ArrayList getEdgesDirID() {
	  return edgesDirID;
	}
	
	/**
	* Gets the list of attributes attached to this Point feature
	* @return attributesID 
	*/
	public java.util.ArrayList getAttributesID() {
	  return attributesID;
	}
	
	/**
	* Gets the intial Point Feature ID
	* @return fromPointID
	*/
	public String getFromPointID() {
	  return fromPointID;
	}
	
	/**
	* Gets the final Point Feature ID
	* @return toPointID
	*/
	public String getToPointID() {
	  return toPointID;
	}
	

}
