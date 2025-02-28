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
 * Area Feature Record
 * @author Illya Stepanov
 */
public class GDFAreaFeatureRecord implements Loadable
{
  /**
   * Parent GDF Section object
   */
  protected GDFSection section;

  /**
   * Feature Class Code
   */
  protected String featureClassCode;

  /**
   * ID of Faces composing the Area Feature
   */
  protected java.util.ArrayList facesID = new java.util.ArrayList();

  /**
   * Faces composing the Area Feature
   */
  protected java.util.ArrayList faces = new java.util.ArrayList();

  /**
   * Attributes ID of the Feature
   */
  protected java.util.ArrayList attributesID = new java.util.ArrayList();

  /**
   * Attributes of the Feature
   */
  protected java.util.ArrayList attributes = new java.util.ArrayList();

  /**
   * Constructor
   * @param section parent GDF Section object
   */
  public GDFAreaFeatureRecord(GDFSection section)
  {
    this.section = section;
  }

  /**
   * Loads the Area Feature Record
   */
  public void load() throws Exception
  {
    // skip Description Identifier
    section.reader.getNextField(5);

    featureClassCode = section.reader.getNextField(4);

    // skip split indicator
    section.reader.getNextField(1);

    int n;

    // get number of faces
    n = GDFReader.parseInt(section.reader.getNextField(5));
    for (int i=0; i<n; i++)
    {
      facesID.add(section.reader.getNextField(10));
    }

    // get number of attributes
    n = GDFReader.parseInt(section.reader.getNextField(5));
    for (int i=0; i<n; i++)
      attributesID.add(section.reader.getNextField(10));
  }

  /**
   * Resolves "lazy" links
   */
  public void initialise()
  {
   for (int i=0; i<facesID.size(); i++)
      faces.add(section.faces.get((String)facesID.get(i)));

    for (int i=0; i<attributesID.size(); i++)
      attributes.add(section.attributes.get((String)attributesID.get(i)));
  }
}