package de.uni_stuttgart.informatik.canu.uomm;

/**
 * <p>Title: User-Oriented Mobility Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

import de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.tripmodel.core.*;

/**
 * This class is a base class for implementing User-Oriented mobility models
 * @author Illya Stepanov
 */
abstract public class UserOrientedMovement extends Movement
{
  
	/**
   * Initial Position Generator
   */
  protected SpatialModel spatialModel=null;
	
	/**
   * Initial Position Generator
   */
  protected InitialPositionGenerator initialPositionGenerator;

  /**
   * Trip Generator
   */
  protected TripGenerator tripGenerator;

  /**
   * Constructor
   */
  public UserOrientedMovement()
  {
  }

  /**
   * Gets the reference to Initial Position Generator. <br>
   * <br>
   * @return reference to Initial Position Generator
   */
  public InitialPositionGenerator getInitialPositionGenerator()
  {
    return initialPositionGenerator;
  }

  /**
   * Gets the reference to Trip Generator. <br>
   * <br>
   * @return reference to TripGenerator
   */
  public TripGenerator getTripGenerator()
  {
    return tripGenerator;
  }

  /**
   * Initializes the object from XML tag. <br>
   * <br>
   * @param element source tag
   * @throws Exception Exception if parameters are invalid
   */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading UserOrientedMovement extension"));

    super.load(element);

    String s;
		
		s = element.getAttribute("spatial_model");
    if (s.length()>0) {
      spatialModel = (SpatialModel)u.getExtension(s);
    }
		
		else {
			spatialModel = (SpatialModel)u.getExtension("SpatialModel");
		}
		
    s = element.getAttribute("initposgenerator");
    if (s.length()>0)
    {
      initialPositionGenerator = (InitialPositionGenerator)u.getExtension(s);
    }

    s = element.getAttribute("tripgenerator");
    if (s.length()>0)
    {
      tripGenerator = (TripGenerator)u.getExtension(s);
    }

		if(spatialModel==null)
      throw new Exception("A SpatialModel is missing!");
		
    if(initialPositionGenerator==null)
      throw new Exception("initposgenerator is missing!");

    if(tripGenerator==null)
      throw new Exception("tripgenerator is missing!");

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading UserOrientedMovement extension"));
  }
}