package de.uni_stuttgart.informatik.canu.tripmodel.core;

/**
 * <p>Title: Trip Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;

/**
 * Interface to choose the node's initial position
 * @author Illya Stepanov
 */
public interface InitialPositionGenerator
{
  /**
   * Chooses the node's initial position. <br>
   * <br>
   * @param node node
   * @return node's initial position
   */
  public Point getInitialPosition(Node node);
}