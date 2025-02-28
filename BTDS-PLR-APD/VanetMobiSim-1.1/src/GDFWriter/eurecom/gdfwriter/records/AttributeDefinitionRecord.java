package eurecom.gdfwriter.records;

/**
 * <p>Title: Attribute Definition Record</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */

/**
 * Attribute Definition Record
 * @author Jerome Haerri 
 */
public class AttributeDefinitionRecord {
  
	
  /**
   * Attribute code
   */
  protected String code=null;
	
	/**
	 * The size of the attribute's value
	 */
	 protected String fieldSize;
	 
	 /**
   * Data Type
   */
  protected String dataType;

  /**
   * Data Unit Code of the SI-unit or other standard unit or form in which
	 * the data values are expressedCurvimetric position defining the end of attribute validity
   */
  protected String dataUnit;
	
	/**
   * Unit Exponent 10LOG of the factor with which the data values have 
	 * to be multiplied to obtain the unit as specified in Data Unit
   */
  protected String unitExp;
	
	/**
   * No Data. Value if no data is being sent
   */
  protected String noData;
	
	/**
   * Minimum Value Allowed 
   */
  protected String minValue;
	
	/**
   * Maximum Value Allowed 
   */
  protected String maxValue;
	
	/**
	 * Attribute Definition
	 */
	protected String definition;
  
	/**
   * Constructor
   * @param code Attribute code
   */
  public AttributeDefinitionRecord(String code, int fieldSize) {
    this.code = code;
		this.fieldSize = String.valueOf(fieldSize);
		noData = "<S>";
  }
	
	
	/**
   * Constructor
   * @param code 
	 * @param dataType
	 * @param dataUnit 
	 * @param unitExp 
	 * @param minVal 
	 * @param maxVal 
	 * @param definition 
   */
  public AttributeDefinitionRecord(String code, int fieldSize, 
				     String dataType, String dataUnit, int unitExp, int minVal, 
						 int maxVal, String definition) {
    this.code = code;
		this.fieldSize = String.valueOf(fieldSize);
		this.noData = "<S>";
		this.dataType = dataType;
		this.dataUnit = dataUnit;
		
		this.unitExp = String.valueOf(unitExp);
		this.minValue = String.valueOf(minVal);
		this.maxValue = String.valueOf(maxVal);
		
		this.definition = definition;
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
   * Sets the minimum value allowed. <br>
   * <br>
   * @param minVal minimum value
   */
	public void setMinVal(int minVal) {
	  this.minValue=String.valueOf(minVal);
	}
	
	/**
   * Gets the minimum value allowed. <br>
   * <br>
   * @return minVal minimum value
   */
	public String getMinVal() {
	  if (minValue !=null)
			return minValue;
		else
			return "";
	}
	
	
	/**
   * Sets the maximum value allowed. <br>
   * <br>
   * @param maxVal maximum value
   */
	public void setMaxVal(int maxVal) {
	  this.maxValue=String.valueOf(maxVal);
	}
	
	/**
   * Gets the maximum value allowed. <br>
   * <br>
   * @return minVal maximum value
   */
	public String getMaxVal() {
	  
		if (maxValue !=null)
			return maxValue;
		else
			return "";
	}
	
	/**
   * Sets the attribute's definition. <br>
   * <br>
   * @param definition attribute's definition
   */
	 
	public void setDescription(String definition) {
	  this.definition=definition;
	}
	
	/**
   * Gets the attribute's definition. <br>
   * <br>
   * @return attribute's definition
   */
	public String getDescription() {
	  if (definition !=null)
			return definition;
		else
			return "";
	}
	
	/**
   * Gets the attribute's data type. <br>
   * <br>
   * @return attribute's dataType
   */
	public String getDataType() {
	  if (dataType !=null)
			return dataType;
		else
			return "";
	}
	
	/**
   * Gets the attribute's data Unit. <br>
   * <br>
   * @return attribute's dataUnit
   */
	public String getDataUnit() {
	  if (dataUnit != null)
			return dataUnit;
		else
			return "";
	}
	
	/**
   * Gets the attribute's field size. <br>
   * <br>
   * @return attribute's field size
   */
	public String getFieldSize() {
		// JHNote (09/02/2006) "*" is when the values are String (names, addresses, etc...)
	  if (fieldSize !=null)
			return fieldSize;
		else
			return "*";
	}
	
	/**
   * Gets the attribute's no_data . <br>
   * Value if no data is being sent.
	 * "<S>" if no value.
	 *<br>
   * @return noData
   */
	public String getNoData() {
	  if (noData != null)
			return noData;
		else
			return "<S>";
	}
	
}
