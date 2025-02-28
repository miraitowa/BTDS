package eurecom.gdfwriter.records;

/**
 * <p>Title: New Node Record</p>
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
public class NodeRecord {
  
	
  /**
   * Record ID
   */
  protected String id;
	
	/**
	 * XYZ record ID
	 */
	 protected String xyzID;
	 
	/**
	 * face record ID
	 */
	 protected String faceID; 
	 
	 /**
   * Status Code
	 * 1 = Section border node
	 * 2 = normal Node
	 * 3 = Dataset border Node
	 * 4 = End of Stubble
	 * 5 = Non Section border Node
   */
  protected int status;
	
  /**
   * Constructor
   * @param id Node Record id
	 * @param xyzID XYZRecord ID
   */
  public NodeRecord(String id, String xyzID, int stauts) {
    this.id = id;
		this.xyzID=xyzID;
		this.status = status;
  }
	
	/**
   * Constructor
   * @param id Node Record id
	 * @param xyzID XYZRecord ID
	 * @param faceID FaceRecord ID
   */
  public NodeRecord(String id, String xyzID, String faceID, int status) {
    this.id = id;
		this.xyzID=xyzID;
		this.faceID=faceID;
		this.status = status;
  }
	
	
	/**
   * Gets the Node Record ID. <br>
   * <br>
   * @return record's ID
   */
  public String getID() {
    return id;
  }

	/**
	* Gets the ID of the XYZRecord
	* @return xyzID
	*/
	public String getXYZID() {
	  if (xyzID !=null)
			return xyzID;
		else 
			return "";
	}
	
	/**
	* Gets the ID of the XYZRecord
	* @return faceID
	*/
	public String getFaceID() {
	  if (faceID !=null)
			return faceID;
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
