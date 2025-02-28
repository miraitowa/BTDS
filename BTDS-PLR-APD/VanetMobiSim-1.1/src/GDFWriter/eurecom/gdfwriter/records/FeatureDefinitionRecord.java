package eurecom.gdfwriter.records;

/**
 * <p>Title: Feature Description Record</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */

/**
 * Feature Description Record
 * @author Jerome Haerri 
 */
public class FeatureDefinitionRecord {
  
	
  /**
   * Feature Class code
   */
  protected String code=null;
	
	/**
   * Feature Class Name
   */
	protected String name;
	
	/**
   * Feature Language Code
   */
	protected String lan="ENG";
	
	/**
   * Feature Alias
	 * <br>
	 * A name of the feature class in a 
	 * language other than English
   */
	protected String alias;
	
  /**
   * Constructor
   * @param code Feature code
   */
  public FeatureDefinitionRecord(String code) {
    this.code = code;
  }
	
	/**
   * Constructor
	 * @param code Feature code
	 * @param name Feature name
	 * @param lan  Feature language code
	 * @param alias Feature alias
   */
  public FeatureDefinitionRecord(String code, String name, String lan, String alias) {
    this.code = code;
		this.name = name;
		this.lan = lan;
		this.alias = alias;
  }
	
	/**
   * Gets the feature recordcode. <br>
   * <br>
   * @return feature code
   */
  public String getCode() {
    return code;
  }
	
	/**
   * Gets the feature name. <br>
   * <br>
   * @return feature name
   */
  public String getName() {
    if (name !=null)
		  return name;
		else
			return "";
			
  }
	
	/**
   * Gets the feature language code. <br>
   * <br>
   * @return feature language code
   */
  public String getLan() {
    if (lan !=null)
			return lan;
		else
			return "ENG";
  }
	
	/**
   * Gets the feature alias. <br>
   * <br>
   * @return feature alias
   */
  public String getAlias() {
    if (alias !=null)
			return alias;
		else 
			return "";
  }
}
