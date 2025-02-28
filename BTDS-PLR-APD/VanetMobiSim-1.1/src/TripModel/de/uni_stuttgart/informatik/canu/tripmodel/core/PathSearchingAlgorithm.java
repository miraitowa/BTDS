package de.uni_stuttgart.informatik.canu.tripmodel.core;

/**
 * <p>Title: Trip Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002-2003</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;

/**
 * This interface is for searching a path between two points
 * @author Illya Stepanov
 */
public interface PathSearchingAlgorithm
{
  /**
   * Signals the path-searching algorithm to ignore attributes of the movement area,
   * e.g., one-way roads, etc.
   */
  public static final int FLAG_IGNORE_DIRECTIONS = 0;

  /**
   * Signals the path-searching algorithm to reflect attributes of the movement area,
   * e.g., one-way roads, etc.
   */
  public static final int FLAG_REFLECT_DIRECTIONS = 1;

  /**
   * Calculates a path for the node between two vertices. <br>
   * <br>
   * @param spatialModel Spatial Model
   * @param node node
   * @param ps source point
   * @param pd destination point
   * @param flags path-searching flags
   * @return path points
   */
  public Trip getPath(SpatialModel spatialModel, Node node, Point ps, Point pd, int flags);
}