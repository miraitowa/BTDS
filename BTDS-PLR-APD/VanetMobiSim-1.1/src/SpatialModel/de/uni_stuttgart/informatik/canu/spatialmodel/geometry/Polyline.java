package de.uni_stuttgart.informatik.canu.spatialmodel.geometry;

/**
 * <p>Title: Spatial Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

/**
 * This class implements a line connecting several points
 * @author Illya Stepanov 
 */
public class Polyline extends GeometryElement
{
  /**
   * Array of points
   */
  protected java.util.ArrayList points = new java.util.ArrayList();

  /**
   * Constructor
   */
  public Polyline()
  {
  }

  /**
   * Gets the array of points. <br>
   * <br>
   * @return array of points
   */
  public java.util.ArrayList getPoints()
  {
    return points;
  }

  /**
   * Checks if this geometry element contains a given element. <br>
   * <br>
   * @param e element being checked
   */
  public boolean contains(GeometryElement e)
  {
    // polyline can contain only point, line or polyline
    if (e instanceof Point)
    {
      Point point = (Point)e;
      // check if one of the lines of polyline contains this point
      for (int i=0; (i+1)<points.size(); i++)
      {
        Line line = new Line((Point)points.get(i), (Point)points.get(i+1));
        if (line.contains(point))
          return true;
      }

      return false;
    }
    else
    if (e instanceof Polyline)
    {
      // check if every point of polyline belongs to this polyline
      Polyline line = (Polyline)e;
      java.util.Iterator iter = line.getPoints().iterator();
      while (iter.hasNext())
      {
        Point p = (Point)iter.next();
        if (!contains(p))
          return false;
      }

      return true;
    }

    return false;
  }

  /**
   * Gets the closest line segment to a point. <br>
   * <br>
   * @param p point
   * @return closest line segment to a point
   */
  public Line getClosestSegment(Point p)
  {
    Line line = null;
    double minDist = Double.MAX_VALUE;

    // iterate all the lines
    for (int i=0; i<points.size()-1; i++)
    {
      Line tempLine = new Line((Point)points.get(i), (Point)points.get(i+1));
      if (tempLine.contains(p))
        return tempLine;

      double f = tempLine.getDistance(p);
      if (f<minDist)
      {
        line = tempLine;
        minDist = f;
      }
    }

    return line;
  }
}