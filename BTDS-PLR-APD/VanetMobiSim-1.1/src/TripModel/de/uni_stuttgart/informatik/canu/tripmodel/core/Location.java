package de.uni_stuttgart.informatik.canu.tripmodel.core;

/**
 * <p>Title: Trip Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.Point;

/**
 * This class implements a location for activity execution
 * @author Illya Stepanov
 */
public class Location
{
  /**
   * Point
   */
  private Point point;

  /**
   * Minimal duration of activity execution (in ms)
   */
  private int minStay;

  /**
   * Maximal duration of activity execution (in ms)
   */
  private int maxStay;

  /**
   * Location's attractiveness
   */
  private float p;

  /**
   * Constructor
   */
  public Location()
  {
  }
    
  /**
   * Sets the location's point. <br>
   * <br>
   * @param point location's point
   */
  public void setPoint(Point point)
  {
    this.point = point;
  }

  /**
   * Gets the location's point. <br>
   * <br>
   * @return location's point
   */
  public Point getPoint()
  {
    return point;
  }

  /**
   * Sets the minimal duration of activity execution. <br>
   * <br>
   * @param minStay minimal duration of activity execution (in ms)
   */
  public void setMinStay(int minStay)
  {
    this.minStay = minStay;
  }

  /**
   * Gets the minimal duration of activity execution. <br>
   * <br>
   * @return minimal duration of activity execution (in ms)
   */
  public int getMinStay()
  {
    return minStay;
  }

  /**
   * Sets the maximal duration of activity execution. <br>
   * <br>
   * @param maxStay maximal duration of activity execution (in ms)
   */
  public void setMaxStay(int maxStay)
  {
    this.maxStay = maxStay;
  }

  /**
   * Gets maximal duration of activity execution. <br>
   * <br>
   * @return maximal duration of activity execution (in ms)
   */
  public int getMaxStay()
  {
    return maxStay;
  }

  /**
   * Sets the location's attractiveness. <br>
   * <br>
   * @param p location's attractiveness
   */
  public void setP(float p)
  {
    this.p = p;
  }

  /**
   * Gets the location's attractiveness. <br>
   * <br>
   * @return location's attractiveness
   */
  public float getP()
  {
    return p;
  }
}
