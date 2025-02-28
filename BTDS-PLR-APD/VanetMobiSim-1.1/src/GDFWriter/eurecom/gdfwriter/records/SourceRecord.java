package eurecom.gdfwriter.records;

/**
 * <p>Title: Source Record</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */

/**
 * Source Record
 * @author Jerome Haerri 
 */
public class SourceRecord {
  
	
	/**
   * Source Description Code
   */
  protected String code=null;
  
	/**
   * Source ID
   */
  protected String descr;
	
  /**
   * Constructor
   * @param code Attribute code
	 * @param descr Source description
   */
  public SourceRecord(String code, String descr) {
    this.code = code;
		this.descr = descr;
  }
	
	
	/**
   * Gets the source record code. <br>
   * <br>
   * @return code
   */
  public String getCode() {
    return code;
  }
	
	/**
   * Gets the source record description. <br>
   * <br>
   * @return descr
   */
  public String getDescr() {
    return descr;
  }
}
