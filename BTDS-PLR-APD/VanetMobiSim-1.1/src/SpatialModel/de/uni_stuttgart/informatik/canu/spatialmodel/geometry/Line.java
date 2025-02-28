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
 * This class implements a line connecting two points
 * @author Illya Stepanov 
 */
public class Line extends Polyline
{
  /**
   * Constructor
   */
  public Line()
  {
  }

  /**
   * Constructor. <br>
   * <br>
   * @param point1 first point of line
   * @param point2 second point of line
   */
  public Line(Point point1, Point point2)
  {
    points.add(point1);
    points.add(point2);
  }

  /**
   * Gets the first point of line. <br>
   * <br>
   * @return first point of line
   */
  public Point getPoint1()
  {
    return (Point)points.get(0);
  }

  /**
   * Gets the second point of line. <br>
   * <br>
   * @return second point of line
   */
  public Point getPoint2()
  {
    return (Point)points.get(1);
  }

  /**
   * Gets an intersection point with a line. <br>
   * <br>
   * @param line line
   * @return point of intersection or null if not found
   */
  public Point intersect(Line line)
  {
    Point p1 = getPoint1();
    Point p2 = getPoint2();
    Point p3 = line.getPoint1();
    Point p4 = line.getPoint2();

    double ua =  ( (p4.getX()-p3.getX())*(p1.getY()-p3.getY()) -
      (p4.getY()-p3.getY())*(p1.getX()-p3.getX()) ) /
      ( (p4.getY()-p3.getY())*(p2.getX()-p1.getX()) -
      (p4.getX()-p3.getX())*(p2.getY()-p1.getY()) );

    double x0 = p1.getX() + ua*(p2.getX() - p1.getX());
    double y0 = p1.getY() + ua*(p2.getY() - p1.getY());

    // check if within edges
    double minx1 = Math.min(p1.getX(), p2.getX());
    double maxx1 = Math.max(p1.getX(), p2.getX());
    double miny1 = Math.min(p1.getY(), p2.getY());
    double maxy1 = Math.max(p1.getY(), p2.getY());

    double minx2 = Math.min(p3.getX(), p4.getX());
    double maxx2 = Math.max(p3.getX(), p4.getX());
    double miny2 = Math.min(p3.getY(), p4.getY());
    double maxy2 = Math.max(p3.getY(), p4.getY());

    if ( (x0>=minx1)&&(x0<=maxx1)&&(x0>=minx2)&&(x0<=maxx2)&&
         (y0>=miny1)&&(y0<=maxy1)&&(y0>=miny2)&&(y0<=maxy2) )
      return new Point(x0, y0);
    else
      return null;
  }

  /**
   * Gets a distance from a point to this line. <br>
   * <br>
   * @param p point
   * @return distance from a point to this line
   */
  public double getDistance(Point p)
  {
    Point point1 = getPoint1();
    Point point2 = getPoint2();

    double u =  ( (p.getX()-point1.getX())*(point2.getX()-point1.getX()) +
      (p.getY()-point1.getY())*(point2.getY()-point1.getY()) ) /
      ( Math.pow(point2.getY()-point1.getY(), 2)+
        Math.pow(point2.getX()-point1.getX(), 2) );

    double x0 = point1.getX() + u*(point2.getX() - point1.getX());
    double y0 = point1.getY() + u*(point2.getY() - point1.getY());

    return p.getDistance(new Point(x0, y0));
  }

  /**
   * Checks if this line contains another point. <br>
   * <br>
   * @param p point being checked
   */
  public boolean contains(Point p)
  {
    double d = getDistance(p);

    // check if the distance from the point to the line is close to 0
    if (Math.abs(d)>1e-2)
      return false;

    Point point1 = getPoint1();
    Point point2 = getPoint2();

    // check if within edges
    double minx1 = Math.min(point1.getX(), point2.getX());
    double maxx1 = Math.max(point1.getX(), point2.getX());
    double miny1 = Math.min(point1.getY(), point2.getY());
    double maxy1 = Math.max(point1.getY(), point2.getY());

    if ( (p.getX()>=minx1)&&(p.getX()<=maxx1)&&
         (p.getY()>=miny1)&&(p.getY()<=maxy1) )
      return true;

    return false;
  }
}