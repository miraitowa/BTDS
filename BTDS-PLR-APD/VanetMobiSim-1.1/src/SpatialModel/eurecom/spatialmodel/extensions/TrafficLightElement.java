package eurecom.spatialmodel.extensions;

/**
 * <p>Title: Traffic Light Element</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.core.Position3D;

/**
 * This class implements a traffic light object
 * @author Jerome Haerri
  * @version 1.0
 */
public class TrafficLightElement {
 	
	/**
   * Element's class specifier
   */
  protected final static String class_code = "72";

	
  /**
   * Element's SubClass specifier
   */
  protected final static String subclass_code = "30";

	/**
   * Traffic light ID
   */
	protected String ID;
	
	/**
   * Traffic light status transition step
   */
	protected long step;
	
	/**
   * Traffic light next update time
   */
	protected long nextUpdate;
	
	 /**
   * Traffic Light's Position
   */
  protected Position3D position = new Position3D(0, 0, 0);

	
	/**
   * Spatial Model Element ID on which the green ligth is turned on
   */
	protected String status;

  // |MF| four-road intersections allow
  // simultaneous green light for opposite roads
	/**
   * Spatial Model Element ID opposite to the one the green ligth is turned on for
   */
	protected String opposite;

  // |MF| four-road intersections allow
  // simultaneous green light for opposite roads
  /**
   * Pointer to Universe
   */
  protected Universe u;
  
	/**
   * List of all neighboring roadElements by their ID
   */
	protected java.util.ArrayList adjacentRoadIDs;
	
	/**
   * Iterator to all connected roadelements
   */
	protected java.util.Iterator iter;
	
	
	 /**
   * Returns the spatialElement class. <br>
   * <br>
   * @return SpatialElement GDF class
   */
	public static String getClassCode() {
	   return class_code;
	}
	
	 /**
   * Returns the spatialElement sub-class. <br>
   * <br>
   * @return SpatialElement GDF subclass
   */
	public static String getSubClassCode() {
	   return subclass_code;
	}
	 
	
	/**
   * Constructor
	 * @param ID ID of this Traffic Light
	 * @param pos X-Y coordinate of the traffic light position
	 * @param step transition step of this traffic light
	 * @param adjacentRoadIDs list of all adjacent roadIDs
	 * @param universe Universe
   */
  public TrafficLightElement(String ID, Position3D pos, long step,
                             java.util.ArrayList adjacentRoadIDs, Universe universe) {
		this.ID = ID;
		this.adjacentRoadIDs = adjacentRoadIDs;
		iter = this.adjacentRoadIDs.iterator();
		position = pos;
		status = (String)iter.next();
    // |MF| four-road intersections allow
    // simultaneous green light for opposite roads
    opposite = status;
    u = universe;
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Traffic Light Element";
  }
	
	/**
   * Returns the traffic light ID. <br>
   * <br>
   * @return Traffic Light ID
   */
  public String getID()
  {
    return ID;
  }

	/**
   * Returns the current activated roadElement. <br>
   * <br>
   * @return ID of the active roadElement
   */
	public String status() {
	  return status;
	}
	
	/**
   * Check if the light is green on a particular roadElement . <br>
   * <br>
   * @return true if GREEN on the roadElement's id ID
   */
	public boolean checkStatus(String ID) {
	  if (status.compareTo(ID) == 0 ||
        opposite.compareTo(ID) == 0)
      return true;
		else
			return false;
	}
	
	
	/**
   * Gets the traffic light's adjacent roadElement IDs. <br>
   * <br>
   * @return list of roadElement IDs
   */
	 public java.util.ArrayList getAdjacentRoadIDs() {
	   return adjacentRoadIDs;
	 }
	 
	 /**
   * Gets the traffic light's position. <br>
   * <br>
   * @return x-y coordinates
   */
  public Position3D getPosition() {
    return position;
  }
	
	/**
   * Update the traffic light status <br>
   * <br>
	 * @param now time at which the update takes place
   */
  
	 public void update(long now) {
		 
		 if (!iter.hasNext()) {
			 iter = adjacentRoadIDs.iterator();
		 }

     // |MF| four-road intersections allow
     // simultaneous green light for opposite roads		 
		 if (iter.hasNext()) {
		   status = (String)iter.next();
       opposite = status;
       
       SpatialModel model = (SpatialModel)u.getExtension("SpatialModel");

       if(adjacentRoadIDs.size() == 4) {
         // compute the derivative of road 'status'
         SpatialModelElement roadElement = (SpatialModelElement)model.getElement(status);
         Polyline shape = (Polyline)roadElement.getGeometry();
         java.util.ArrayList points = shape.getPoints();
         Point pointf = (Point)points.get(0);
			   Point pointt = (Point)points.get(points.size()-1);
         Point startS = null;
         Point endS = null;
         // for road 'status', the end point is
         // the one located at the intersection
         if(position.getDistance(pointf.getPosition()) == 0) {
           startS = pointt;
           endS = pointf;
         }
         else {
           startS = pointf;
           endS = pointt;
         }
         double derivativeStatus = (endS.getY()-startS.getY()) / (endS.getX()-startS.getX());
         double atanStatus = java.lang.Math.atan(derivativeStatus);
         /*System.err.println("status start (" + startS.getX() + "," + startS.getY() +
                            ") end (" +  + endS.getX() + "," + endS.getY() +
                            ") --> derivative " + derivativeStatus + " angle " + atanStatus);*/

         // look for the opposite road
         java.util.Iterator tmpIter = adjacentRoadIDs.iterator();
         while(tmpIter.hasNext() && opposite == status) {
           String ID = (String)tmpIter.next();

           // compute the derivative of road 'ID'
			     roadElement = (SpatialModelElement)model.getElement(ID);
           shape = (Polyline)roadElement.getGeometry();
           points = shape.getPoints();
           pointf = (Point)points.get(0);
			     pointt = (Point)points.get(points.size()-1);
           Point startI = null;
           Point endI = null;
           // for road 'ID', the end point is the one
           // located away from the intersection
           if(position.getDistance(pointf.getPosition()) == 0) {
             startI = pointf;
             endI = pointt;
           }
           else {
             startI = pointt;
             endI = pointf;
           }
           double derivativeID = (endI.getY()-startI.getY()) / (endI.getX()-startI.getX());
           double atanID = java.lang.Math.atan(derivativeID);
           /*System.err.println("id " + ID + " start (" + startI.getX() + "," + startI.getY() +
                              ") end (" +  + endI.getX() + "," + endI.getY() +
                              ") --> derivative " + derivativeID + " angle " + atanID);*/
 
           // check if this is the opposite road
           // (i.e. angular difference is within pi/2)
           if(atanStatus - java.lang.Math.PI/4 < atanID &&
              atanID < atanStatus + java.lang.Math.PI/4)
             opposite = ID;
         }
       }
     }
		 else {
		   System.out.println("Error in TrafficLight: no roadElement connected to this traffic light");
			 status = "0";
		 }
			 
		 nextUpdate = now + step;
	 }
	
	 /**
   * Record the time at which this traffic light has been turned on <br>
   * <br>
	 * @param now time at which the traffic light has been turned on.
   */
	 public void start(long now) {
	   nextUpdate = now + step;
	 }
	 
	 

	
}


