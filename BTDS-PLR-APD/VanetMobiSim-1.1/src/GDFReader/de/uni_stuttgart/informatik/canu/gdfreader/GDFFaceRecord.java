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
 * Face Record
 * @author Illya Stepanov 
 */
public class GDFFaceRecord implements Loadable
{
  /**
   * Parent GDF Section object
   */
  protected GDFSection section;

  /**
   * ID of boudary Edges
   */
  protected java.util.ArrayList edgesID = new java.util.ArrayList();

  /**
   * Boundary Edges
   */
  protected java.util.ArrayList edges = new java.util.ArrayList();

  /**
   * Directions of Edges composing the Face:
   * 0-clockwise
   * 1-counterclockwise
   */
  protected java.util.ArrayList edgesDirections = new java.util.ArrayList();

  /**
   * Constructor
   * @param section parent GDF Section object
   */
  public GDFFaceRecord(GDFSection section)
  {
    this.section = section;
  }

  /**
   * Loads the Face Record
   */
  public void load() throws Exception
  {
    int n;

    // get number of edges
    n = GDFReader.parseInt(section.reader.getNextField(5));
    for (int i=0; i<n; i++)
    {
      edgesID.add(section.reader.getNextField(10));
      edgesDirections.add(new Integer(GDFReader.parseInt(
        section.reader.getNextField(2))));
    }
  }

  /**
   * Resolves "lazy" links
   */
  public void initialise()
  {
    for (int i=0; i<edgesID.size(); i++)
      edges.add(section.edges.get((String)edgesID.get(i)));
  }
}