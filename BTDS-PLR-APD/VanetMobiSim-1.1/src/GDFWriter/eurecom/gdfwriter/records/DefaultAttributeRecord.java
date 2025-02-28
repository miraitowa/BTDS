package eurecom.gdfwriter.records;

/**
 * <p>Title: Default Attribute Records</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */

/**
 * Default Attribute Record
 * @author Jerome Haerri 
 */
public class DefaultAttributeRecord {
  
	
  /**
   * Attribute code
   */
  protected String code=null;
	
	
	 /**
   * Default attribute value
   */
  protected String value;

	
  /**
   * Constructor
   * @param code Attribute code
   */
  public DefaultAttributeRecord(String code) {
    this.code = code;
  }
	
	/**
   * Constructor
   * @param code Attribute code
	 * @param value Default attribute value
   */
  public DefaultAttributeRecord(String code, String value) {
    this.code = code;
		this.value = value;
  }
	
	/**
   * Gets the attribute's code. <br>
   * <br>
   * @return record's code
   */
  public String getCode() {
    return code;
  }

  /**
   * Gets the attribute's value. <br>
   * <br>
   * @return record's value
   */
  public String getValue() {
    if (value !=null)
			return value;
		else
			return "";
  }
}
