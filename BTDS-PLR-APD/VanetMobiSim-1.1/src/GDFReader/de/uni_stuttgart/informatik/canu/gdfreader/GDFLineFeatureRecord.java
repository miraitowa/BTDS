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
 * Line Feature Record
 * @author Illya Stepanov 
 */
public class GDFLineFeatureRecord implements Loadable
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
   * ID of Edges composing the Line Feature
   */
  protected java.util.ArrayList edgesID = new java.util.ArrayList();

  /**
   * Edges composing the Line Feature
   */
  protected java.util.ArrayList edges = new java.util.ArrayList();

  /**
   * Directions of Edges composing the Line Feature:
   * 0-in the direction of edge
   * 1-in opposite direction
   */
  protected java.util.ArrayList edgesDirections = new java.util.ArrayList();

  /**
   * Attributes ID of the Feature
   */
  protected java.util.ArrayList attributesID = new java.util.ArrayList();

  /**
   * Attributes of the Feature
   */
  protected java.util.ArrayList attributes = new java.util.ArrayList();

  /**
   * From Point ID
   */
  protected String fPointID;

  /**
   * From Point
   */
  protected GDFPointFeatureRecord fPoint;

  /**
   * To Point ID
   */
  protected String tPointID;

  /**
   * To Point
   */
  protected GDFPointFeatureRecord tPoint;

  /**
   * Constructor
   * @param section parent GDF Section object
   */
  public GDFLineFeatureRecord(GDFSection section)
  {
    this.section = section;
  }

  /**
   * Loads the Line Feature Record
   */
  public void load() throws Exception
  {
    // skip Description Identifier
    section.reader.getNextField(5);

    featureClassCode = section.reader.getNextField(4);

    // skip split indicator
    section.reader.getNextField(1);

    int n;

    // get number of edges
    n = GDFReader.parseInt(section.reader.getNextField(5));
    for (int i=0; i<n; i++)
    {
      edgesID.add(section.reader.getNextField(10));

      int pos_neg = Integer.parseInt(section.reader.getNextField(2).trim());
      // a hack for "NavTech"
      if (section.reader.supplierName.toLowerCase().indexOf("navtech")!=-1)
      {
        // convert the value as in standard
        if (pos_neg==0)
          pos_neg = 1;
        else
        if (pos_neg==1)
          pos_neg = 0;
      }
      
      edgesDirections.add(new Integer(pos_neg));
    }

    // get number of attributes
    n = GDFReader.parseInt(section.reader.getNextField(5));
    for (int i=0; i<n; i++)
      attributesID.add(section.reader.getNextField(10));

    fPointID = section.reader.getNextField(10);
		
		//System.out.println("fPointID is "+ fPointID);
    
		tPointID = section.reader.getNextField(10);
  }

  /**
   * Resolves "lazy" links
   */
  public void initialise()
  {
    for (int i=0; i<edgesID.size(); i++)
      edges.add(section.edges.get((String)edgesID.get(i)));

    for (int i=0; i<attributesID.size(); i++)
      attributes.add(section.attributes.get((String)attributesID.get(i)));

    fPoint = (GDFPointFeatureRecord)section.pointFeatures.get(fPointID);
		/*if (fPoint ==null)
			System.out.println("Should not be null");*/
		
    tPoint = (GDFPointFeatureRecord)section.pointFeatures.get(tPointID);
  }
}