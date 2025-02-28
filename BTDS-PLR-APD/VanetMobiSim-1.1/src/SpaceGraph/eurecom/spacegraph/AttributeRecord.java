package eurecom.spacegraph;

/**
 * <p>Title: SpatialElement Attribute Records</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */

/**
 * Attribute Record
 * @author Jerome Haerri 
 */
public class AttributeRecord {
  
	
  /**
   * Attribute ID
   */
  protected String id=null;
	
	/**
   * Attribute Code
   */
  protected String attributeCode;

	
	/**
   * Attribute Value
   */
  protected String attributeValue;

	
	
	 /**
   * Curvimetric position defining the start of attribute validity
   */
  protected int from;

  /**
   * Curvimetric position defining the end of attribute validity
   */
  protected int to;

  /**
   * Absolute or Relative:
   * 0=absolute
   * 1=relative
   */
  protected int abs;

  /**
   * Direction in which the attribute value is valid:
   * - - negavive,
   * + - positive,
   *   - both
   */
  protected String dir;

  /**
   * Storage for sub-attribute:
   * Key: attribute ID
   */
  protected java.util.ArrayList attributesID = new java.util.ArrayList();
	
	/**
   * Attrbute's description
   */
	protected String Description=null;
	
  /**
   * Constructor
   * @param code Attribute code
	 * @param value Attribute value
   */
  public AttributeRecord(String code, String value) {
    attributeCode = code;
		attributeValue = value;
  }
	
	/**
   * Gets the attribute's code. <br>
   * <br>
   * @return record's code
   */
  public String getCode() {
    return attributeCode;
  }

  /**
   * Gets the attribute's value. <br>
   * <br>
   * @return record's value
   */
  public String getValue() {
    return attributeValue;
  }
	
	/**
   * Sets the attribute's ID. <br>
   * <br>
   * @param id attribute's ID
   */
	 
	public void setID(String id) {
	  this.id=id;
	}
	
	/**
   * Gets the attribute's value. <br>
   * <br>
   * @return record's id
   */
	public String getID() {
	  return id;
	}
}