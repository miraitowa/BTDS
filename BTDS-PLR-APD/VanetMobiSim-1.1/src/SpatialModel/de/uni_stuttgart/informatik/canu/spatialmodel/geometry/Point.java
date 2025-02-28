package de.uni_stuttgart.informatik.canu.spatialmodel.geometry;

/**
 * <p>Title: Spatial Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

import de.uni_stuttgart.informatik.canu.mobisim.core.Position3D;

/**
 * This class implements a point
 * @author Illya Stepanov 
 */
public class Point extends GeometryElement
{
  /**
   * Point coordinates
   */
  protected Position3D position;

  /**
   * Constructor
   */
  public Point()
  {
    position = new Position3D("-1", 0, 0, 0);
  }

  /**
   * Constructor. <br>
   * <br>
	 * @param pointID ID of the point
   * @param x x-coordinate
   * @param y y-coordinate
	 * @param xyzID id of the Position3D
   */
  public Point(String pointID, double x, double y, String xyzID)
  {
    id = pointID;
		position = new Position3D(xyzID, x, y, 0);
  }
	
	/**
   * Constructor. <br>
   * <br>
   * @param x x-coordinate
   * @param y y-coordinate
   */
  public Point(double x, double y)
  {
		position = new Position3D("-1", x, y, 0);
  }

  /**
   * Constructor. <br>
   * <br>
   * @param pos position
   */
  public Point(Position3D pos)
  {
    position = new Position3D(pos.getID(), pos.getX(), pos.getY(), 0);
  }

  /**
   * Gets the point's x-coordinate. <br>
   * <br>
   * @return x-coordinate
   */
  public double getX()
  {
    return position.getX();
  }

  /**
   * Gets the point's y-coordinate. <br>
   * <br>
   * @return y-coordinate
   */
  public double getY()
  {
    return position.getY();
  }

   /**
    * Gets the aggregated Position3D object. <br>
    * <br>
    * @return aggregated Position3D object
    */
   public Position3D getPosition()
   {
      return position;
   }

  /**
   * Indicates whether this point is equal to another point. <br>
   * <br>
   * @param o point
   * @return true, if the points refer to the same position
   *
   */
   public boolean equals(Object o)
   {
     if (o instanceof Point)
     {
       Point point = (Point)o;
       if (position.equals(point.getPosition()))
         return true;       
     }
     return false;
   }

   /**
    * Calculates distance to a given point. <br>
    * <br>
    * @param p point
    * @return distance to the given point
    */
   public double getDistance(Point p)
   {
      return position.getDistance(p.position);
   }

  /**
   * Checks if this geometry element contains another element. <br>
   * <br>
   * @param e element being checked
   */
  public boolean contains(GeometryElement e)
  {
    // point can contain only other point
    if (e instanceof Point)
    {
      Point p = (Point)e;
      if ( position.equals(p.position) )
        return true;
    }

    return false;
  }
	
	public void setIDs(String pointID, String xyzID) {
	  setID(pointID);
		position.setID(xyzID);
	}
}