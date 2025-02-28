package eurecom.spacegraph;

/**
 * <p>Title: SpatialElement Relationshop Records</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */

/**
 * Relationship Record
 * @author Jerome Haerri 
 */
public class RelationshipRecord {
  
	/**
   * Relationship ID
   */
  protected String id = null;
	
	/**
   * Relationship Source Description
   */
  protected int sourceDesc;
	
	/**
   * Relationship Code
   */
  protected String relationshipCode;

  /**
   * ID of all kind of Features which are in the Relationship
   */
  protected java.util.ArrayList featuresID = new java.util.ArrayList();
	
	/**
   * Category of Features which are in the Relationship
	 * Point=1
	 * Line= 2
	 * Area = 3
	 * Complex = 4
   */
  protected java.util.ArrayList featuresCat = new java.util.ArrayList();
  
	
	/**
   * Optional Attribute Records of the Relationship
   */
  protected java.util.ArrayList attributesID = new java.util.ArrayList();


  /**
   * Constructor
   * @param id Relationship' unique id
	 * @param code Relationship' code
   */
  public RelationshipRecord(String id, String code) {
    this.id = id;
		relationshipCode = code;
  }
	
	/**
   * Sets Relationship ID
   * @param id Relationship' ID
   */
  public void setID(String id) {
    this.id = id;
  }
	
	/**
   * gets Relationship ID
   * @return Relationship' ID
   */
  public String getID() {
    return id;
  }
	
	/**
   * Gets the record's attributes. <br>
   * <br>
   * @return record's attributes
   */
  public java.util.ArrayList getAttributes() {
    return attributesID;
  }
	
	/**
   * Gets the record's features category. <br>
   * <br>
   * @return record's features category
   */
  public java.util.ArrayList getCat() {
    return featuresCat;
  }

  /**
   * Gets the record's relations. <br>
   * <br>
   * @return record's featuresIDs
   */
  public java.util.ArrayList getFeatures() {
    return featuresID;
  }
	
	/**
   * Gets the record's code. <br>
   * <br>
   * @return record's code
   */
  public String getCode() {
    return relationshipCode;
  }
	
	/**
	* Gets the Description ID
	* @return Description ID
	*/
	public String getDescr() {
	  return (String.valueOf(sourceDesc));
	}
}