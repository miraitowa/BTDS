package de.uni_stuttgart.informatik.canu.mobisim.core;

import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * <p>Patches: </p>
 * <p> v1.2 (15/11/2005):  Added Nodes's attributes:
 *         									VT : vehicle type
 *         									LN : lane number on which the car is moving. </p>
 * @author Canu Research group
 * @author v1.2: Jerome Haerri (haerri@ieee.org)
 * @version 1.2
 */

/**
 * Mobile node implementation.
 * <p>Patches: </p>
 * <p><i> Version 1.2 by Jerome Haerri (haerri@ieee.org) on 11/15/2005: 
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Added Nodes's attributes:
 * <br>  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;      									VT : vehicle type
 * <br>  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;        								LN : lane number on which the car is moving.</i> </p>
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.0-1.1 Gregor Schiele
 * @author 1.2 Jerome Haerri
 * @version 1.2
 */
public class Node extends ExtendableObject implements Comparable {
	 
	
  //environment
  
	/**
   * Node's ID
   */
  protected String   id;

  /**
   * Node's Position
   */
  protected Position3D position = new Position3D(0, 0, 0);

  
	/**
   * Node's Attributes (GDF style)
	 * @since 1.2
   */
  protected java.util.Map attributes = new java.util.HashMap();
	
	/**
   * Constructor
	 * <br>
	 * Creates a node and specifies its type (GDF style: VT = 0 (no specific vehicle))
	 * and the starting lane number (most right lane by default)
	 * @since 1.2														
   */
  public Node() {
		/* added the node's attribute VT = 0 (no specific user) */
		attributes.put("VT","0");
		attributes.put("LN","1");
  }


  // ---------------------------------------------------------------------------
  // accessors
  // ---------------------------------------------------------------------------


  /**
   * Gets the node's ID. <br>
   * <br>
   * @return node's ID
   */
  public final String getID()
  {
    return id;
  }

	
	/**
   * Gets the node's attributes (GDF style). <br>
   * <br>
   * @return node's attributes
	 * @since 1.2
   */
  public java.util.Map getAttributes()
  {
    return attributes;
  }

  /**
   * Returns a string representation of the object. <br>
   * <br>
   * @return string representation of the object
   */
  public String toString()
  {
    return id;
  }


  /**
   * Indicates whether this node is equals to some other node. <br>
   * <br>
   * @param o node to compare with
   * @return true, if the nodes are the same
   *
   */
  public boolean equals(Object o)
  {
    if (o instanceof Node)
    {
      Node node = (Node)o;
      if (id.equals(node.id))
        return true;
    }
    return false;
  }


  /**
   * Compares this object with another object for sort order. <br>
   * <br>
   * @param o another object
   * @return a negative integer, zero, or a positive integer as this object
   * is less, equal, or greater than the specified object
   */
  public int compareTo(Object o)
  {
    return id.compareTo(((Node)o).id);
  }


  /**
   * Gets the node's current position. <br>
   * <br>
   * @return node's current position
   */
  public final Position3D getPosition()
  {
    return position;
  }


  /**
   * Sets the node's current position. <br>
   * <br>
   * @param position new position
   */
  public void setPosition(Position3D position)
  {
    this.position = position;
  }

	/**
   * Get the node's current lane on a roadElement (GDF style). <br>
   * <br>
   * @return Lane number on the roadElement, or 1 if no multilane features.
	 * @since 1.2
   */
	public int getLane() {
     String n_l = (String)attributes.get("LN");
     if (n_l !=null) {
			 int laneNumber = Integer.parseInt(n_l);
       return laneNumber;
     }
     else
        return 1;
  } 
	
	/**
   * set the node's current lane on a roadElement (GDF style). <br>
   * <br>
   * @param laneNumber Lane number on the roadElement.
	 * @since 1.2
   */
	public void setLane(int laneNumber) {
		String laneNumberString = String.valueOf(laneNumber);
		attributes.put("LN",laneNumberString);
  } 
	
  /**
    * Initializes the object from XML tag. <br>
    * <br>
		* <i> Version 1.2 by Jerome Haerri (haerri@ieee.org): 
		* <br> &nbsp;&nbsp;&nbsp;&nbsp; A node is able to load a new attribute "VT", vehicle type.</i>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    super.load(element);

    // set id
    String id_attr = element.getAttribute("id");
    this.id=id_attr;

    // process child tags
    org.w3c.dom.NodeList list = element.getChildNodes();
    int len=list.getLength();

    for(int i=0; i<len; i++)
    {
      org.w3c.dom.Node item = list.item(i);
      String tag = item.getNodeName();

      if(tag.equals("#text"))
      {
        // skip it
        continue;
      }
      else
      if(tag.equals("#comment"))
      {
        // skip it
        continue;
      }
			else
      if(tag.equals("type"))
      {
        u.sendNotification(new LoaderNotification(this, u,
           "Processing <type> tag"));
				
				/* (NEWCOM Specific) : allow xml to load different type of user */
				String vehicleType = item.getFirstChild().getNodeValue();
				if (vehicleType == "ped")
					attributes.put("VT","25");
				else if (vehicleType == "car")
					attributes.put("VT","11");
				else if (vehicleType == "truck")
					attributes.put("VT","20");
				else if (vehicleType == "bus")
					attributes.put("VT","17");
				else
					attributes.put("VT","0");
				
        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <type> tag"));
      }
      else
      if(tag.equals("position"))
      {
        u.sendNotification(new LoaderNotification(this, u,
           "Processing <position> tag"));

        // read and set position
        this.position = new Position3D((org.w3c.dom.Element)item);

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <position> tag"));
      }
    }

    // checkout
    if (id.length()==0)
      throw new Exception("Node "+toString()+" misses id definition");
  }//proc
}
