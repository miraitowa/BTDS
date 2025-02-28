package eurecom.gdfwriter.records;

/**
 * <p>Title: Edge Record</p>
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
public class EdgeRecord {
  
	
  /**
   * Record ID
   */
  protected String id;
	
	/**
	 * ID of the XYZRecord containing the coordinates 
	 * of the intermediate "points" of this Edge.
	 */
	 protected String XYZPointsID;
	 
	/**
	 * Node record ID of initial node of this Edge
	 */
	 protected String fromNodeID; 
	 
	 /**
	 * Node record ID of final node of this Edge
	 */
	 protected String toNodeID; 
	 
	  /**
	 * Left Face Identifier 
	 */
	 protected String leftFaceID; 
	 
	  /**
	 * Right Face Identifier 
	 */
	 protected String rightFaceID; 
	 
	 /**
   * Status Code
	 * 1 = (non applicable)
	 * 2 = Normal Edge
	 * 3 = Dataset Border Edge
	 * 4 = Non Section border Edge
   */
  protected int status;
	
  /**
   * Constructor
   * @param id Edge Record id
	 * @param fromNodeID ID of the initialPointRecord
	 * @param toNodeID ID of the finalPointRecord
	 * @param status Status of this Edge
   */
  public EdgeRecord(String id, String fromNodeID, String toNodeID, int status) {
    this.id = id;
		this.fromNodeID=fromNodeID;
		this.toNodeID=toNodeID;
		this.status = status;
  }
	
	
	/**
   * Constructor
   * @param id Edge Record id
	 * @param fromNodeID ID of the initialPointRecord
	 * @param toNodeID ID of the finalPointRecord
	 * @param rightFaceID ID of the FaceRecord on the right hand side
	 * @param leftFaceID ID of the FaceRecord on the left hand side
	 * @param status Status of this Edge
   */
  public EdgeRecord(String id, String fromNodeID, String toNodeID, String rightFaceID, String leftFaceID, int status) {
    this.id = id;
		this.fromNodeID=fromNodeID;
		this.toNodeID=toNodeID;
		this.rightFaceID=rightFaceID;
		this.leftFaceID=leftFaceID;
		this.status = status;
  }
	
	
	/**
   * Gets the Edge Record ID. <br>
   * <br>
   * @return record's ID
   */
  public String getID() {
    return id;
  }
	
	/**
   * Adds a XYZ Record. <br>
   * <br>
   * @param XYZPointsID record's ID
   */
	public void addXYZRecord(String XYZPointsID) {
	  this.XYZPointsID = XYZPointsID; 
	}
	
	/**
	* Gets the ID of the XYZRecord
	* @return XYZPointsID
	*/
	public String getXYZID() {
	  if (XYZPointsID !=null)
			return XYZPointsID;
		else
			return "";
	}
	
	
	/**
	* Gets the intial Node ID
	* @return fromNodeID
	*/
	public String getFromNodeID() {
	  return fromNodeID;
	}
	
	/**
	* Gets the final Node ID
	* @return toNodeID
	*/
	public String getToNodeID() {
	  return toNodeID;
	}
	
	
	/**
	* Gets the ID of the Left Face
	* @return leftFaceID
	*/
	public String getLeftFaceID() {
	  if (leftFaceID !=null)
			return leftFaceID;
		else
			return "";
	}
	
	/**
	* Gets the ID of the Right Face
	* @return rightFaceID
	*/
	public String getRightFaceID() {
	  if (rightFaceID !=null)
			return rightFaceID;
		else
			return "";
	}
	
	/**
	* Gets the Status code
	* @return status
	*/
	public String getStatus() {
	  return (String.valueOf(status));
	}
}
