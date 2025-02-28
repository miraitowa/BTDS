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
 * Complex Feature Record
 * @author Illya Stepanov 
 */
public class GDFComplexFeatureRecord implements Loadable
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
   * ID of Point Features which are the elements of the Complex Feature
   */
  protected java.util.ArrayList pointFeaturesID = new java.util.ArrayList();

  /**
   * Point Features which are the elements of the Complex Feature
   */
  protected java.util.ArrayList pointFeatures = new java.util.ArrayList();

  /**
   * ID of Line Features which are the elements of the Complex Feature
   */
  protected java.util.ArrayList lineFeaturesID = new java.util.ArrayList();

  /**
   * Line Features which are the elements of the Complex Feature
   */
  protected java.util.ArrayList lineFeatures = new java.util.ArrayList();

  /**
   * ID of Area Features which are the elements of the Complex Feature
   */
  protected java.util.ArrayList areaFeaturesID = new java.util.ArrayList();

  /**
   * Area Features which are the elements of the Complex Feature
   */
  protected java.util.ArrayList areaFeatures = new java.util.ArrayList();

  /**
   * ID of Complex Features which are the elements of the Complex Feature
   */
  protected java.util.ArrayList complexFeaturesID = new java.util.ArrayList();

  /**
   * Complex Features which are the elements of the Complex Feature
   */
  protected java.util.ArrayList complexFeatures = new java.util.ArrayList();

  /**
   * Attributes ID of the Feature
   */
  protected java.util.ArrayList attributesID = new java.util.ArrayList();

  /**
   * Attributes of the Feature
   */
  protected java.util.ArrayList attributes = new java.util.ArrayList();

  /**
   * From Complex Feature ID
   */
  protected String fComplexFeatureID;

  /**
   * From Complex Feature
   */
  protected GDFComplexFeatureRecord fComplexFeature;

  /**
   * To Complex Feature ID
   */
  protected String tComplexFeatureID;

  /**
   * To Complex Feature
   */
  protected GDFComplexFeatureRecord tComplexFeature;

  /**
   * Constructor
   * @param section parent GDF Section object
   */
  public GDFComplexFeatureRecord(GDFSection section)
  {
    this.section = section;
  }

  /**
   * Loads the Complex Feature Record
   */
  public void load() throws Exception
  {
    // skip Description Identifier
    section.reader.getNextField(5);

    featureClassCode = section.reader.getNextField(4);

    // skip split indicator
    section.reader.getNextField(1);

    int n;

    // get number of features
    n = GDFReader.parseInt(section.reader.getNextField(5));
    for (int i=0; i<n; i++)
    {
      int code = GDFReader.parseInt(section.reader.getNextField(2));
      String id = section.reader.getNextField(10);

      // get child feature category code
      switch (code)
      {
        case 1:
          pointFeaturesID.add(id);
          break;

        case 2:
          lineFeaturesID.add(id);
          break;

        case 3:
          areaFeaturesID.add(id);
          break;

        case 4:
          complexFeaturesID.add(id);
          break;
      }
    }

    // get number of attributes
    n = GDFReader.parseInt(section.reader.getNextField(5));
    for (int i=0; i<n; i++)
      attributesID.add(section.reader.getNextField(10));

    fComplexFeatureID = section.reader.getNextField(10);
    tComplexFeatureID = section.reader.getNextField(10);
  }

  /**
   * Resolves "lazy" links
   */
  public void initialise()
  {
    for (int i=0; i<pointFeaturesID.size(); i++)
      pointFeatures.add(section.pointFeatures.get((String)pointFeaturesID.get(i)));

    for (int i=0; i<lineFeaturesID.size(); i++)
      lineFeatures.add(section.lineFeatures.get((String)lineFeaturesID.get(i)));

    for (int i=0; i<areaFeaturesID.size(); i++)
      areaFeatures.add(section.areaFeatures.get((String)areaFeaturesID.get(i)));

    for (int i=0; i<complexFeaturesID.size(); i++)
      complexFeatures.add(section.complexFeatures.get((String)complexFeaturesID.get(i)));

    for (int i=0; i<attributesID.size(); i++)
      attributes.add(section.attributes.get((String)attributesID.get(i)));

    fComplexFeature = (GDFComplexFeatureRecord)
      section.complexFeatures.get(fComplexFeatureID);
    tComplexFeature = (GDFComplexFeatureRecord)
      section.complexFeatures.get(tComplexFeatureID);
  }
}