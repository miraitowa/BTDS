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
 * Node Record
 * @author Illya Stepanov 
 */
public class GDFNodeRecord implements Loadable
{
  /**
   * Parent GDF Section object
   */
  protected GDFSection section;

  /**
   * Node Location ID
   */
  String pointID;

  /**
   * Node Location
   */
  GDFXYZRecord point;

  /**
   * Face ID, which contains the Node
   */
  String faceID;

  /**
   * Face, which contains the Node
   */
  GDFFaceRecord face;

  /**
   * Constructor
   * @param section parent GDF Section object
   */
  public GDFNodeRecord(GDFSection section)
  {
    this.section = section;
  }

  /**
   * Loads the Node Record
   */
  public void load() throws Exception
  {
    pointID = section.reader.getNextField(10);
    faceID = section.reader.getNextField(10);
  }

  /**
   * Resolves "lazy" links
   */
  public void initialise()
  {
    point = (GDFXYZRecord)section.coordinates.get(pointID);
		//System.out.println("Got an Node starting from node X: " + ((GDFXYZRecord.XYZPoint)(point.points.get(0))).x + " Y: " + ((GDFXYZRecord.XYZPoint)(point.points.get(0))).y );
    face = (GDFFaceRecord)section.faces.get(faceID);
  }
}