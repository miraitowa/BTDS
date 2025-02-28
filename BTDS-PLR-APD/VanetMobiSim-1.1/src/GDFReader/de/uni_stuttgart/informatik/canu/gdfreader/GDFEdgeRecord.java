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
 * Edge Record
 * @author Illya Stepanov 
 */
public class GDFEdgeRecord implements Loadable
{
  /**
   * Parent GDF Section object
   */
  protected GDFSection section;

  /**
   * Intermediate Points of the Edge ID
   */
  protected String pointsID;

  /**
   * Intermediate Points of the Edge
   */
  protected GDFXYZRecord points;

  /**
   * From Node of the Edge ID
   */
  protected String fNodeID;

  /**
   * From Node of the Edge
   */
  protected GDFNodeRecord fNode;

  /**
   * To Node of the Edge ID
   */
  protected String tNodeID;

  /**
   * To Node of the Edge
   */
  protected GDFNodeRecord tNode;

  /**
   * Constructor
   * @param section parent GDF Section object
   */
  public GDFEdgeRecord(GDFSection section)
  {
    this.section = section;
  }

  /**
   * Loads the Node Record
   */
  public void load() throws Exception
  {
    pointsID = section.reader.getNextField(10);
    fNodeID = section.reader.getNextField(10);
    tNodeID = section.reader.getNextField(10);
  }

  /**
   * Resolves "lazy" links
   */
  public void initialise()
  {
    points = (GDFXYZRecord)section.coordinates.get(pointsID);
    fNode = (GDFNodeRecord)section.nodes.get(fNodeID);
		//System.out.println("Got an edge starting from node X: " + ((GDFXYZRecord.XYZPoint)(fNode.point.points.get(0))).x + " Y: " + ((GDFXYZRecord.XYZPoint)(fNode.point.points.get(0))).y );
    tNode = (GDFNodeRecord)section.nodes.get(tNodeID);
  }
}