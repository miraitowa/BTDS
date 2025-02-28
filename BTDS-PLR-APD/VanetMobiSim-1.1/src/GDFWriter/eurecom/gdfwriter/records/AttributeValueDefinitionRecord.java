package eurecom.gdfwriter.records;

/**
 * <p>Title: Attribute Value Definition Record</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */

/**
 * Attribute Value Definition Record
 * @author Jerome Haerri 
 */
public class AttributeValueDefinitionRecord {
  
	
	/**
   * Record id
   */
  protected String id=null;
	
	
  /**
   * Attribute code
   */
  protected String code=null;
	
	/**
   * Attribute value
   */
  protected String value;
	
	/**
   * Maximum Value Allowed 
   */
  protected String descr;
	

  /**
   * Constructor
   * @param code Attribute code
	 * @param value Attribute value
	 * @param descr Attrbute desc
   */
  public AttributeValueDefinitionRecord(String id, String code, String value,String descr) {
    this.id = id;
		this.code = code;
		this.value = value;
		this.descr = descr;
  }

	
	/**
   * Gets the attribute's code. <br>
   * <br>
   * @return code
   */
  public String getCode() {
    return code;
  }
	
	/**
   * Gets the record's id. <br>
   * <br>
   * @return record's id
   */
  public String getID() {
    return id;
  }
	
	/**
   * Gets the attribute's description. <br>
   * <br>
   * @return attribute's description
   */
	public String getDescription() {
	  if (descr !=null)
			return descr;
		else
			return "";
	}
	
	/**
   * Gets the attribute's description. <br>
   * <br>
   * @return attribute's description
   */
	public String getValue() {
	  if (value !=null)
			return value;
		else 
			return "";
	}
	
}
