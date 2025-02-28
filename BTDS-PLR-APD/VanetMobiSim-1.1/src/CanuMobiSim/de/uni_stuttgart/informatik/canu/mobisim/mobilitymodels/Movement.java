package de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * Patches:      Version 1.2 by Marco Fiore (fiore@tlc.polito.it) on 22/05/2006:
 *               Speed attribute moved at Movement class level
 *
 * @author 1.0-1.1 Canu Research group
 * @author 1.2 Marco Fiore
 * @version 1.2
 */

/**
 * This class is a base class for implementations of mobility models
 * @author Illya Stepanov
 */
public abstract class Movement extends ExtensionModule
{
  /**
   * Minimal speed (meters/ms)
   */
  protected float minSpeed = 0.0f;  // in meters/ms
  /**
   * Maximal speed (meters/ms)
   */
  protected float maxSpeed = 0.0f;  // in meters/ms

  /**
   * Current speed (meters/ms)
   */
  protected float speed = 0.0f;  // in meters/ms

  /**
   * Constructor
   */
  public Movement()
  {
    super("Movement");
  }

  /**
   * Constructor. <br>
   * <br>
   * @param node parent {@link Node Node} object
   */
  public Movement(Node node)
  {
    super(node, "Movement");
  }

  /**
   * Gets the minimal movement speed. <br>
   * <br>
   * @return minimal movement speed
   */
  public float getMinSpeed()
  {
    return minSpeed;
  }

  /**
   * Gets the maximal movement speed. <br>
   * <br>
   * @return maximal movement speed
   */
  public float getMaxSpeed()
  {
    return maxSpeed;
  }

  /**
   * Gets the current movement speed. <br>
   * <br>
   * @return current movement speed
   */
  public float getSpeed()
  {
    return speed;
  }
  
  /**
   * Initializes the object from XML tag. <br>
   * <br>
   * Child classes should override this method to implement
   * custom initialization procedure from tag.
   * @param element source tag
   * @throws Exception Exception if parameters are invalid
   */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    super.load(element);
  }
}
