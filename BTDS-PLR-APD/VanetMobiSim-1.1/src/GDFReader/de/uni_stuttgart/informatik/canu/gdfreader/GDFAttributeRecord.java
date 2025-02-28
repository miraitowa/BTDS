package de.uni_stuttgart.informatik.canu.gdfreader;

/**
 * <p>Title: GDF Reader</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

/**
 * Attribute Record
 * @author Illya Stepanov
 */
public class GDFAttributeRecord implements Loadable
{
  /**
   * Parent GDF Section object
   */
  protected GDFSection section;

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
   * Storage for attribute values:
   * Key - Attribute id, Value - String value
   */
  protected java.util.Map attributes = new java.util.HashMap();

  /**
   * Constructor
   * @param section parent GDF Section object
   */
  public GDFAttributeRecord(GDFSection section)
  {
    this.section = section;
  }

  /**
   * Loads the Attribute Record
   */
  public void load() throws Exception
  {
    from = GDFReader.parseInt(section.reader.getNextField(5));
    to = GDFReader.parseInt(section.reader.getNextField(5));
    
    // the field is absent in NavTech data sources
    if (section.reader.supplierName.toLowerCase().indexOf("navtech")==-1)
      abs = GDFReader.parseInt(section.reader.getNextField(1));
    
    dir = section.reader.getNextField(1);

    int n;

    // get number of attributes
    n = GDFReader.parseInt(section.reader.getNextField(5));
    for (int i=0; i<n; i++)
    {
      String type = section.reader.getNextField(2).trim();

      // skip Description Identifier
      section.reader.getNextField(5);

      String value = section.reader.getNextField(10).trim();

      // a hack for "NavTech"
      if (section.reader.supplierName.toLowerCase().indexOf("navtech")!=-1)
      {
        // direction of traffic flow
        if (type.equals("DF"))
        {
          // convert the value to original GDF
          if (value.equals("2"))
            value = "3";
          else
          if (value.equals("3"))
            value = "2";
        }
      }
      
      attributes.put(type, value);
    }
  }
}