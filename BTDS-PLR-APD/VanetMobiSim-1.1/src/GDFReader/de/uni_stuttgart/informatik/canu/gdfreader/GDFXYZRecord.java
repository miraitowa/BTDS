package de.uni_stuttgart.informatik.canu.gdfreader;

import geotransform.coords.Gdc_Coord_3d;
import geotransform.coords.Utm_Coord_3d;
import geotransform.transforms.Gdc_To_Utm_Converter;

/**
 * <p>Title: GDF Reader</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

/**
 * Coordinate Record
 * @author Illya Stepanov 
 */
public class GDFXYZRecord implements Loadable
{
  /**
   * XYZPoint
   */
  public class XYZPoint
  {
    /**
     * X-value
     */
    protected int x;

    /**
     * Y-value
     */
    protected int y;

    /**
     * Z-value
     */
    protected int z;

    /**
     * Constructor
     * @param x x-value
     * @param y y-value
     * @param z z-value
     */
    public XYZPoint(int x, int y, int z)
    {
      this.x = x;
      this.y = y;
      this.z = z;
    }
  }

  /**
   * Parent GDF Section object
   */
  protected GDFSection section;

  /**
   * Array of points
   */
  protected java.util.ArrayList points = new java.util.ArrayList();

  /**
   * Constructor
   * @param section parent GDF Section object
   */
  public GDFXYZRecord(GDFSection section)
  {
    this.section = section;
  }

  /**
   * Loads the XYZ Record
   */
  public void load() throws Exception
  {
    // skip geometry type code
    section.reader.getNextField(1);
    // skip quality code
    section.reader.getNextField(2);
    // skip description identifier
    section.reader.getNextField(5);

    // get number of coordinates
    int n = GDFReader.parseInt(section.reader.getNextField(5));
    for (int i=0; i<n; i++)
    {
      int x = GDFReader.parseInt(section.reader.getNextField(10));
      int y = GDFReader.parseInt(section.reader.getNextField(10));
      int z = GDFReader.parseInt(section.reader.getNextField(10));
			
			//System.out.println("Parsed from GDF x " + x*section.reader.scale_x + " y " + y*section.reader.scale_y + " z " + z);
      // set minimal & maximal values
      if (x<section.x_min)
        section.x_min = x;
      if (x>section.x_max)
        section.x_max = x;

      if (y<section.y_min)
        section.y_min = y;
      if (y>section.y_max)
        section.y_max = y;

      Utm_Coord_3d utm = new Utm_Coord_3d();
      Gdc_To_Utm_Converter.Convert(new Gdc_Coord_3d(y*section.reader.scale_y, x*section.reader.scale_x, 0), utm);
      if (section.reader.origin==null)
        section.reader.origin = utm;
      else
      {
        if (utm.x<section.reader.origin.x)
          section.reader.origin.x = utm.x;
        
        if (utm.y<section.reader.origin.y)
          section.reader.origin.y = utm.y;
      }

      // add new point
      points.add(new XYZPoint(x, y, z));
    }
  }
}