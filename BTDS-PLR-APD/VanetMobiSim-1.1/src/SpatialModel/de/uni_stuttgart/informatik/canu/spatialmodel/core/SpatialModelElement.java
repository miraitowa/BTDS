package de.uni_stuttgart.informatik.canu.spatialmodel.core;

/**
 * <p>Title: Spatial Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * <p>Patches: </p>
 * <p> v1.2 (16/11/2005) : Modified the Relation Map to a Relation ArrayList </p> 
 *
 * @author Illya Stepanov
 * @author v1.2 Jerome Haerri (haerri@ieee.org)
 * @version 1.2
 */

import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;

/**
 * This class implements an element of Spatial Model
 * <p>Patches: </p>
 * <p> <i> Version 1.2  by Jerome Haerri (haerri@ieee.org) on 11/16/2005: 
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Modified the Relation Map to a Relation ArrayList </i></p> 
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.2 Jerome Haerri
 * @version 1.2 
 */
public class SpatialModelElement
{
  /**
   * Element's ID
   */
  protected String id;

  /**
   * Element's class specifier
   */
  protected String class_code;

  /**
   * Element's SubClass specifier
   */
  protected String subclass_code;

  /**
   * Geometry of element
   */
  protected GeometryElement geometry;

  /**
   * Elements from lower levels
   */
  protected java.util.Collection children = new java.util.ArrayList();

  /**
   * Attributes of element
   */
  protected java.util.Map attributes = new java.util.HashMap();

  /**
   * Relations of element
	 * <br>
	 * @since 1.2
   */
	protected java.util.ArrayList relations = new java.util.ArrayList();
	
	//protected java.util.Map relations = new java.util.HashMap();
	
  /**
   * Constructor. <br>
   * <br>
   * @param id element's ID
   * @param class_code element's class_code
   * @param subclass_code element's subclass_code
   * @param geometry element's geometry
   */
  public SpatialModelElement(String id, String class_code, String subclass_code,
                    GeometryElement geometry)
  {
    this.id = id;
    this.class_code = class_code;
    this.subclass_code = subclass_code;
    this.geometry = geometry;
  }

  /**
   * Gets the element's ID. <br>
   * <br>
   * @return element's ID
   */
  public String getID()
  {
    return id;
  }

  /**
   * Gets the element's class code. <br>
   * <br>
   * @return element's class code
   */
  public String getClassCode()
  {
    return class_code;
  }

  /**
   * Gets the element's subclass code. <br>
   * <br>
   * @return element's subclass code
   */
  public String getSubClassCode()
  {
    return subclass_code;
  }

  /**
   * Gets the element's geometry. <br>
   * <br>
   * @return element's geometry
   */
  public GeometryElement getGeometry()
  {
    return geometry;
  }

  /**
   * Gets the element's child elements. <br>
   * <br>
   * @return element's child elements
   */
  public java.util.Collection getChildren()
  {
    return children;
  }

  /**
   * Gets the element's attributes. <br>
   * <br>
   * @return element's attributes
   */
  public java.util.Map getAttributes()
  {
    return attributes;
  }

  /**
   * Gets the element's relations. <br>
	 * <br>
   * @return element's relations
	 * @since 1.2
   */
  public java.util.ArrayList getRelations()
  {
    return relations;
  }
}