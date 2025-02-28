package de.uni_stuttgart.informatik.canu.tripmodel.core;

/**
 * <p>Title: Trip Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002-2003</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

/**
 * This class contains a node trip
 * @author Illya Stepanov
 */
public class Trip
{
  /**
   * Point approximated path
   */
  protected java.util.ArrayList path = new java.util.ArrayList();

  /**
   * Constructor
   */
  public Trip()
  {
  }

  /**
   * Gets the trip points
   */
  public java.util.ArrayList getPath()
  {
    return path;
  }
}