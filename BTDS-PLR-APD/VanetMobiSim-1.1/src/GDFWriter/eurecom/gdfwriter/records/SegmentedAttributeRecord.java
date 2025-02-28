package eurecom.gdfwriter.records;

/**
 * <p>Title: Segmented Attribute Record</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */

/**
 * Segmented Attribute Record
 * @author Jerome Haerri 
 */
public class SegmentedAttributeRecord {
  
	
  /**
   * Attribute ID
   */
  protected String id=null;
	
	/**
   * Global Attribute Source Description
	 * In a future version, this source desc will be included in the attributes
   */
  protected int sourceDesc;
	
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
   * <S> - both
   */
  protected String dir;

  /**
   * Storage for sub-attribute:
   * Key: attribute ID
   */
  protected java.util.Map attributes = new java.util.HashMap();
	
	/**
   * Constructor
	 * @param id Attribute record ID
	 * @param sourceDesc Source Description Indentifier
   */
  public SegmentedAttributeRecord(String id, int sourceDesc) {
    this.id = id;
		this.sourceDesc = sourceDesc;
  }
	
  /**
   * Constructor
	 * @param id Attribute record ID
	 * @param sourceDesc Source Description Indentifier
   * @param code Attribute code
	 * @param value Attribute value
   */
  public SegmentedAttributeRecord(String id, int sourceDesc, String code, String value) {
    this.id = id;
		this.sourceDesc = sourceDesc;
		attributes.put(code,value);
  }
	
	/**
   * Constructor
   * @param id Attribute id
	 * @param sourceDesc Source Description Indentifier
	 * @param newAttributes Map of new attributes
   */
  public SegmentedAttributeRecord(String id, int sourceDesc,java.util.Map newAttributes) {
    this.id = id;
		this.sourceDesc = sourceDesc;
		addAttributes(newAttributes);
  }
	
	/**
   * Constructor
   * @param id Attribute id
	 * @param sourceDesc Source Description Indentifier
	 * @param from Curvimetric position defining the start of attribute validity
	 * @param to Curvimetric position defining the end of attribute validity
	 * @param dir Direction in which the attribute value is valid
	 * @param abs Absolute or Relative:
	 * @param newAttributes Set of attributes contained in this record
   */
  public SegmentedAttributeRecord(String id, int sourceDesc, int from, int to, String dir, int abs, java.util.Map newAttributes) {
    this.id = id;
		this.sourceDesc = sourceDesc;
		this.from = from;
		this.to = to;
		this.dir = dir;
		this.abs = abs;
		addAttributes(newAttributes);
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
	
	/**
   * Sets the attribute's direction value. <br>
   * <br>
   * @param dir attribute's direction
   */
	 
	public void setDir(String dir) {
	  this.dir=dir;
	}
	
	/**
   * Gets the attribute's direction value. <br>
	 * - = negative direction only
	 * + = positive direction only
	 * <S> = both
   * <br>
   * @return record's id
   */
	public String getDir() {
	  if (dir !=null)
			return dir;
		else
			return " ";
	}
	
	/**
   * Sets the attribute's absolute/relative feature. <br>
   * <br>
   * @param abs absolute/relative
   */
	public void setAbs(int abs) {
	  this.abs=abs;
	}
	
	/**
   * Gets the attribute's absolute/relative feature. <br>
   * <br>
   * @return abs absolute/relative
   */
	public String getAbs() {
	  return (String.valueOf(abs));
	}
	
	/**
   * Gets the attribute's From curvimetric position validity. <br>
   * <br>
   * @return from
   */
	public String getFrom() {
	  return (String.valueOf(from));
	}
	
	/**
   * Gets the attribute's To curvimetric position validity. <br>
   * <br>
   * @return to
   */
	public String getTo() {
	  return (String.valueOf(to));
	}
	
	
	/**
   * Adds an attribute to this record. <br>
   * <br>
   * @param code attribute code
	 * @param value attribute value
   */
	 
	public void addAttribute(String code, String value) {
	  attributes.put(code,value);
	}
	
	/**
   * Gets the set of attributes of this record. <br>
   * <br>
   * @return set of attributes
   */
	 
	public java.util.Map getAttributes() {
	  return attributes;
	}
	
	/**
   * Add a set of attributes in this record <br>
   * <br>
   * @param newAttributes a set of new attributes
   */
	 
	public void addAttributes(java.util.Map newAttributes) {
	  attributes.putAll(newAttributes);
	}
	
	/**
	* Gets the Description ID
	* @return Description ID
	*/
	public String getDescr() {
	  return (String.valueOf(sourceDesc));
	}
	
}