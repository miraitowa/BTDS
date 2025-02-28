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
 * Point Feature Record
 * @author Illya Stepanov 
 */
public class GDFPointFeatureRecord implements Loadable
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
   * ID of Nodes containing geometry of the Point Feature
   */
  protected java.util.ArrayList nodesID = new java.util.ArrayList();

  /**
   * Nodes containing geometry of the Point Feature
   */
  protected java.util.ArrayList nodes = new java.util.ArrayList();

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
  public GDFPointFeatureRecord(GDFSection section)
  {
    this.section = section;
  }

  /**
   * Loads the Point Feature Record
   */
  public void load() throws Exception
  {
    // skip Description Identifier
    section.reader.getNextField(5);

    featureClassCode = section.reader.getNextField(4);

    int n;

    // get number of nodes
    n = GDFReader.parseInt(section.reader.getNextField(5));
    for (int i=0; i<n; i++)
      nodesID.add(section.reader.getNextField(10));

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
    //System.out.println("trying to initialize Point Features");
		for (int i=0; i<nodesID.size(); i++) {
			//System.out.println("Node ID " + (String)nodesID.get(i));
			GDFNodeRecord nodeRec = (GDFNodeRecord)section.nodes.get((String)nodesID.get(i));
			GDFXYZRecord.XYZPoint X = (GDFXYZRecord.XYZPoint)nodeRec.point.points.get(0);
			//System.out.println("Got node X " + X.x + " Y " + X.y );
      nodes.add(section.nodes.get((String)nodesID.get(i)));
		}

    for (int i=0; i<attributesID.size(); i++)
      attributes.add(section.attributes.get((String)attributesID.get(i)));
  }
}