package de.uni_stuttgart.informatik.canu.tripmodel.core;

/**
 * <p>Title: Trip Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002-2003</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

import de.uni_stuttgart.informatik.canu.mobisim.core.Node;

/**
 * This interface is to generate a new node's trip
 * @author Illya Stepanov 
 */
public interface TripGenerator
{
  /**
   * Generates a new node's trip. <br>
   * <br>
   * @param node node
   * @return new node's trip
   */
  public Trip genTrip(Node node);

  /**
   * Chooses time of node staying at the current position. <br>
   * <br>
   * @param node node
   * @return stay duration (in ms)
   */
  public int chooseStayDuration(Node node);
}