package de.uni_stuttgart.informatik.canu.gdfreader;

/**
 * <p>Title: GDF Section</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

/**
 * GDF Section
 * @author Illya Stepanov 
 */
public class GDFSection implements Loadable
{
  /**
   * Parent GDF Reader object
   */
  protected GDFReader reader;

  /**
   * Maximum X-value
   */
  protected int x_max = Integer.MIN_VALUE;

  /**
   * Maximum Y-value
   */
  protected int y_max = Integer.MIN_VALUE;

  /**
   * Minimum X-value
   */
  protected int x_min = Integer.MAX_VALUE;

  /**
   * Minimum Y-value
   */
  protected int y_min = Integer.MAX_VALUE;

  /**
   * Storage for coordinate records:
   * Key - Coordinate id, Value - associated GDFXYZRecord
   */
  protected java.util.Map coordinates = new java.util.HashMap();

  /**
   * Storage for node records:
   * Key - Node id, Value - associated GDFNodeRecord
   */
  protected java.util.Map nodes = new java.util.HashMap();

  /**
   * Storage for edge records:
   * Key - Edge id, Value - associated GDFEdgeRecord
   */
  protected java.util.Map edges = new java.util.HashMap();

  /**
   * Storage for face records:
   * Key - Face id, Value - associated GDFFaceRecord
   */
  protected java.util.Map faces = new java.util.HashMap();

  /**
   * Storage for point features records:
   * Key - Point Feature id, Value - associated GDFPointFeatureRecord
   */
  protected java.util.Map pointFeatures = new java.util.HashMap();

  /**
   * Storage for line features records:
   * Key - Line Feature id, Value - associated GDFLineFeatureRecord
   */
  protected java.util.Map lineFeatures = new java.util.HashMap();

  /**
   * Storage for area features records:
   * Key - Area Feature id, Value - associated GDFAreaFeatureRecord
   */
  protected java.util.Map areaFeatures = new java.util.HashMap();

  /**
   * Storage for complex features records:
   * Key - Complex Feature id, Value - associated GDFComplexFeatureRecord
   */
  protected java.util.Map complexFeatures = new java.util.HashMap();

  /**
   * Storage for relationships records:
   * Key - Relationship id, Value - associated GDFRelationshipRecord
   */
  protected java.util.Map relationships = new java.util.HashMap();

  /**
   * Storage for attributes records:
   * Key - Attribute id, Value - associated GDFAttributeRecord
   */
  protected java.util.Map attributes = new java.util.HashMap();

  /**
   * Constructor
   * @param reader parent GDF Reader object
   */
  public GDFSection(GDFReader reader)
  {
    this.reader = reader;
  }

  /**
   * Processes the Section Border Record
   */
  protected void processSectionBorderRecord() throws Exception
  {
    String s;

    // skip x-y multiplication factor
    s = reader.getNextField(2);

    // skip z multiplication factor
    s = reader.getNextField(2);

    // skip x-offset
    s = reader.getNextField(10);

    // skip y-offset
    s = reader.getNextField(10);

    // skip z-offset
    s = reader.getNextField(10);

    s = reader.getNextField(10);
    x_max = GDFReader.parseInt(s);

    s = reader.getNextField(10);
    y_max = GDFReader.parseInt(s);

    s = reader.getNextField(10);
    x_min = GDFReader.parseInt(s);

    s = reader.getNextField(10);
    y_min = GDFReader.parseInt(s);
  }

  /**
   * Processes the XYZ Record
   */
  public void processXYZRecord() throws Exception
  {
    String id = reader.getNextField(10);

    GDFXYZRecord record = new GDFXYZRecord(this);
    coordinates.put(id, record);

    record.load();
  }

  /**
   * Processes the Node Record
   */
  public void processNodeRecord() throws Exception
  {
    String id = reader.getNextField(10);

    GDFNodeRecord record = new GDFNodeRecord(this);
    nodes.put(id, record);

    record.load();
  }

  /**
   * Processes the Edge Record
   */
  public void processEdgeRecord() throws Exception
  {
    String id = reader.getNextField(10);

    GDFEdgeRecord record = new GDFEdgeRecord(this);
    edges.put(id, record);

    record.load();
  }

  /**
   * Processes the Face Record
   */
  public void processFaceRecord() throws Exception
  {
    String id = reader.getNextField(10);

    GDFFaceRecord record = new GDFFaceRecord(this);
    faces.put(id, record);

    record.load();
  }

  /**
   * Processes the Attribute Record
   */
  public void processAttributeRecord() throws Exception
  {
    String id = reader.getNextField(10);

    GDFAttributeRecord record = new GDFAttributeRecord(this);
    attributes.put(id, record);

    record.load();
  }

  /**
   * Processes the Relationship Record
   */
  public void processRelationshipRecord() throws Exception
  {
    String id = reader.getNextField(10);

    GDFRelationshipRecord record = new GDFRelationshipRecord(this);
    relationships.put(id, record);

    record.load();
  }

  /**
   * Processes the Point Feature Record
   */
  public void processPointFeatureRecord() throws Exception
  {
    String id = reader.getNextField(10);
		
    GDFPointFeatureRecord record = new GDFPointFeatureRecord(this);
    pointFeatures.put(id, record);

    record.load();
  }

  /**
   * Processes the Line Feature Record
   */
  public void processLineFeatureRecord() throws Exception
  {
    String id = reader.getNextField(10);

    GDFLineFeatureRecord record = new GDFLineFeatureRecord(this);
    lineFeatures.put(id, record);

    record.load();
  }

  /**
   * Processes the Area Feature Record
   */
  public void processAreaFeatureRecord() throws Exception
  {
    String id = reader.getNextField(10);

    GDFAreaFeatureRecord record = new GDFAreaFeatureRecord(this);
    areaFeatures.put(id, record);

    record.load();
  }

  /**
   * Processes the Complex Feature Record
   */
  public void processComplexFeatureRecord() throws Exception
  {
    String id = reader.getNextField(10);

    GDFComplexFeatureRecord record = new GDFComplexFeatureRecord(this);
    complexFeatures.put(id, record);

    record.load();
  }

  /**
   * Loads the GDF Section
   */
  public void load() throws Exception
  {
    String s;
    // read until eof reached
    while ((s = reader.getNextRecord())!=null)
    {
      // check record type code
      switch (GDFReader.parseInt(s))
      {
        case 16:
          // check subcode
          int subCode = GDFReader.parseInt(reader.getNextField(2));
          if (subCode==1)
          {
            // New section started. Exit to global loader.
            reader.ind-=2; // reset the counter
            return;
          }
          else
          if (subCode==7)
            processSectionBorderRecord();
          break;

        case 23:
          processXYZRecord();
          break;

        case 24:
          processEdgeRecord();
          break;

        case 25:
          processNodeRecord();
          break;

        case 29:
          processFaceRecord();
          break;

        case 44:
          processAttributeRecord();
          break;

        case 50:
          processRelationshipRecord();
          break;

        case 51:
          processPointFeatureRecord();
          break;

        case 52:
          processLineFeatureRecord();
          break;

        case 53:
          processAreaFeatureRecord();
          break;

        case 54:
          processComplexFeatureRecord();
          break;
      }
    }
  }

  /**
   * Resolves "lazy" links
   */
  public void initialise()
  {
    java.util.Iterator iter;
		//System.out.println("Initializing lazy links");
    iter = nodes.values().iterator();
    while (iter.hasNext())
    {
			((GDFNodeRecord)iter.next()).initialise();
    }

    iter = edges.values().iterator();
    while (iter.hasNext())
    {
      ((GDFEdgeRecord)iter.next()).initialise();
    }

    iter = faces.values().iterator();
    while (iter.hasNext())
    {
      ((GDFFaceRecord)iter.next()).initialise();
    }

    iter = pointFeatures.values().iterator();
    while (iter.hasNext())
    {
			((GDFPointFeatureRecord)iter.next()).initialise();
    }

    iter = lineFeatures.values().iterator();
    while (iter.hasNext())
    {
      ((GDFLineFeatureRecord)iter.next()).initialise();
    }

    iter = areaFeatures.values().iterator();
    while (iter.hasNext())
    {
      ((GDFAreaFeatureRecord)iter.next()).initialise();
    }

    iter = complexFeatures.values().iterator();
    while (iter.hasNext())
    {
      ((GDFComplexFeatureRecord)iter.next()).initialise();
    }

    iter = relationships.values().iterator();
    while (iter.hasNext())
    {
      ((GDFRelationshipRecord)iter.next()).initialise();
    }
  }
}

