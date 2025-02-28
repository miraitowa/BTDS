package de.uni_stuttgart.informatik.canu.tripmodel.core;

/**
 * <p>Title: Trip Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002-2004</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.3
 */

/**
 * This class implements an activity to execute belonging to automaton
 * @author Illya Stepanov
 */
public class State
{
  /**
   * State ID
   */
  private String id;

  /**
   * Set of locations for the activity execution
   */
  protected java.util.ArrayList locations = new java.util.ArrayList();

  /**
   * Constructor
   */
  public State()
  {
  }
  
  /**
   * Sets the ID. <br>
   * <br>
   * @param id id to set
   */
  public void setID(String id)
  {
    this.id = id;
  }

  /**
   * Gets the ID. <br>
   * <br>
   * @return id
   */
  public String getID()
  {
    return id;
  }

  /**
   * Gets a set of locations associated with the activity. <br>
   * <br>
   * @return set of location associated with the activity
   */
  public java.util.ArrayList getLocations()
  {
    return locations;
  }
}