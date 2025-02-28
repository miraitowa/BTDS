package eurecom.gdfwriter.records;

/**
 * <p>Title: PointFeature Record</p>
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
public class PointFeatureRecord {
  
	
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
	 * List of node record IDs composing the line feature
	 */
	protected java.util.ArrayList nodesID = new java.util.ArrayList();
	
	
	/**
	 * List of the segmented attributes IDs of this line feature
	 */
	protected java.util.ArrayList attributesID = new java.util.ArrayList();
	
	
  /**
   * Constructor
   * @param id Edge Record id
	 * @param code Feature Class Code
	 * @param sourceDesc Source Description Indentifier
	 * @param nodesID list of node record IDs
	 * @param attributesID list of attribute record IDs
   */
  public PointFeatureRecord(String id, String code, int sourceDesc, java.util.ArrayList nodesID, 
													 java.util.ArrayList attributesID) {
    this.id = id;
		this.code = code;
		this.sourceDesc = sourceDesc;
		this.attributesID.addAll(attributesID);
		this.nodesID.addAll(nodesID);
  }
		
	/**
   * Gets the Point Feature Record ID. <br>
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
	* Gets the list of Nodes contained in this Point feature
	* @return nodesID
	*/
	public java.util.ArrayList getNodesID() {
	  return nodesID;
	}
	
	/**
	* Gets the list of attributes attached to this Point feature
	* @return attributesID 
	*/
	public java.util.ArrayList getAttributesID() {
	  return nodesID;
	}
	
}
