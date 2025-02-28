package de.uni_stuttgart.informatik.canu.tripmodel.generators;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.tripmodel.core.*;
import de.uni_stuttgart.informatik.canu.tripmodel.pathalgorithms.*;

/**
 * <p>Title: Trip Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002-2003</p>
 * <p>Company: University of Stuttgart</p>
 * <p>Patches: </p>
 * <p>	v1.2 (30/11/2005) In order to reflect direction, 
 *												if we use a SpaceGraph element, we need to set the 
 *												reflect direction to true in both Elements. </p>
 * @author Illya Stepanov
 * @author v1.2: Jerome Haerri (haerri@ieee.org)
 * @version 1.2
 */

/**
 * This class generates node trips according to automaton of activity sequences
 * <p>Patches: </p>
 * <p>	<i> Version 1.2 by Jerome Haerri (haerri@ieee.org) on 11/30/2005:
 * <br> &nbsp;&nbsp;&nbsp;&nbsp;  In order to reflect directions, if we use a 
 *																SpaceGraph element with differentiated flows, we need to set the 
 *																reflect direction to true in both Elements. </i></p>
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.2 Jerome Haerri
 * @version 1.2
 */
public class ActivityBasedTripGenerator extends ExtensionModule
                                      implements InitialPositionGenerator,
                                                  TripGenerator
{
  
	/**
	* Spatial Model
	*/ 
	protected SpatialModel spatialModel=null;
	
	/**
   * Path searching algorithm
   */
  protected PathSearchingAlgorithm algo = new Dijkstra();

  /**
   * Template for automaton of activity sequences
   */
  protected Automaton template_automaton = new Automaton();

  /**
   * Automata for every node
   */
  protected java.util.Map automata = new java.util.HashMap();

  /**
   * Destinations of movements for every node
   */
  protected java.util.Map destinations = new java.util.HashMap();

  /**
   * Flag to reflect or ignore the road directions during the path calculation
   */
  protected int reflect_directions = PathSearchingAlgorithm.FLAG_IGNORE_DIRECTIONS;

  /**
   * Constructor
   */
  public ActivityBasedTripGenerator()
  {
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Activity-Based Trip Model";
  }

  /**
   * Performs the module initialization. <br>
   * <br>
   * The method is called after finishing the scenario file processing.
   */
  public void initialize()
  {
    // JHNote (06/02/2006): Now the SpatialModel is loaded at the beginning.		
		//SpatialModel spatialModel = (SpatialModel)u.getExtension("SpatialModel");
		
    Graph graph = spatialModel.getGraph();
		
    // check the points
    java.util.Iterator iter1 = template_automaton.getStates().iterator();
    while (iter1.hasNext())
    {
      State s = (State)iter1.next();
      
      java.util.Iterator iter2 = s.getLocations().iterator();
      while (iter2.hasNext())
      {
        Point p = ((Location)iter2.next()).getPoint();

        if (graph==null)
        { 
          if((p.getX()<0.0)||(p.getY()<0.0)||
             (p.getX()>u.getDimensionX())||(p.getY()>u.getDimensionY()))
          {
            System.err.println("Fatal error: Position is outside Universe dimensions: Position3D("+p.getX()+","+p.getY()+")");
            System.exit(1);
          }
        }
        else
        {
          if((p.getX()<graph.getLeftmostCoordinate())||(p.getY()<graph.getLowermostCoordinate())||
             (p.getX()>graph.getRightmostCoordinate())||(p.getY()>graph.getUppermostCoordinate()))
          {
            System.err.println("Fatal error: Position is outside movement area graph: Position3D("+p.getX()+","+p.getY()+")");
            System.exit(1);
          }
        }
      }
    }
  }

  /**
   * Executes the extension. <br>
   * <br>
   * The method is called on every simulation timestep. 
   * @return 0 - the module should be executed on next timesteps,
   *        -1 - the module should not be executed on further timesteps and should be removed from the extensions' list
   */
  public int act()
  {
    return 0;
  }

  /**
   * Chooses a node initial position according to the automaton of activity sequences. <br>
   * <br>
   * @param node node
   * @return new initial position
   */
  public Point getInitialPosition(Node node)
  {
    java.util.Random rand = u.getRandom();

    // create the automaton for the node
    Automaton a = (Automaton)template_automaton.clone();
    automata.put(node, a);
    
    java.util.ArrayList locations = a.getCurrentState().getLocations();
    Location ld = (Location)locations.get(rand.nextInt(locations.size()));
    destinations.put(node, ld);
    
    return ld.getPoint();
  }

  /**
   * Generates a new trip for the node according to the automaton of activity sequences. <br>
   * <br>
   * @param node node
   * @return new trip for node
   */
  public Trip genTrip(Node node)
  {
    java.util.Random rand = u.getRandom();
    //SpatialModel spatialModel = (SpatialModel)u.getExtension("SpatialModel");
    // JHNote (06/02/2006): Now the SpatialModel is loaded at the beginning.
		
    Position3D pos = node.getPosition();
    Point ps = new Point(pos);

    // switch automata to the next state
    Automaton a = (Automaton)automata.get(node);
    a.switchToNextState();

    // choose a trip destination
    java.util.ArrayList locations = a.getCurrentState().getLocations();
    Location ld = (Location)locations.get(rand.nextInt(locations.size()));
    destinations.put(node, ld);
    
    Point pd = ld.getPoint();

    Graph graph = spatialModel.getGraph();
		
		if (graph==null)
    {
      // generate a straight path to the point
      Trip trip = new Trip();
      
      java.util.ArrayList path = trip.getPath();
      path.add(ps);
      path.add(pd);
      
      return trip;
    }
    else
    {
      // use the path searching algorithm to calculate the path
      Trip trip = algo.getPath(spatialModel, node, ps, pd, reflect_directions);
      if (trip==null)
      {
        // add an empty trip
        trip = new Trip();

        java.util.ArrayList path = trip.getPath();
        path.add(new Point(node.getPosition().getX(), node.getPosition().getY()));
        path.add(new Point(node.getPosition().getX(), node.getPosition().getY()));
      }
        
      return trip;
    }
  }

  /**
   * Chooses a time of staying at the current position. <br>
   * <br>
   * @param node node
   * @return stay duration (in ms)
   */
  public int chooseStayDuration(Node node)
  {
    // check if the automaton is in the final state
    Automaton a = (Automaton)automata.get(node);
    if (a.isFinalState(a.getCurrentState()))
    {
      // infinite state duration
      return Integer.MAX_VALUE;
    }

    // choose an appropriate stay duration for the current location
    Location ll = (Location)destinations.get(node);
    return (int)(ll.getMinStay()+(ll.getMaxStay()-ll.getMinStay())*u.getRandom().nextFloat());
  }

  /**
   * Initializes the object from XML tag. <br>
	 * <i> Version 1.2 by Jerome Haerri (haerri@ieee.org) on 11/30/2005: 
	 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Patch to reflect the stong links between reflect_direction in SpaceGraph and here.
	 * <br>	&nbsp;&nbsp;&nbsp;&nbsp; In order to reflect direction here, if we use a space graph, 
	 *													we need to set the reflect direction to true in both case. </i>
	 * <br>
   * @param element source tag
   * @throws Exception Exception if parameters are invalid
   */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading ActivityBasedTripGenerator extension"));

    super.load(element);

		// JHNote (06/02/2006): Now the SpatialModel is loaded at the beginning.
		String sm = element.getAttribute("spatial_model");
    if (sm.length()>0) {
      spatialModel = (SpatialModel)u.getExtension(sm);
    }
		
		else {
			spatialModel = (SpatialModel)u.getExtension("SpatialModel");
		}
		
		if(spatialModel==null)
      throw new Exception("A SpatialModel is missing!");
		
    org.w3c.dom.Node n;
    String classTag = element.getAttribute("path_algorithm").trim();
    if (classTag.length()!=0)
    {
      algo = (PathSearchingAlgorithm)Class.forName(classTag).newInstance();
      
      // handle stoch path selection parameters
      if (algo instanceof PedestrianStochPathSelection && !(algo instanceof SpeedPathSelection))
      {
        String param = element.getAttribute("theta").trim();
        if(param.length()==0)
          throw new Exception("\"theta\" attribute of path selection is missing!");
        float theta = Float.parseFloat(param);
        
        ((PedestrianStochPathSelection)algo).setTheta(theta);
      }

      // handle speed path selection parameters
      if (algo instanceof SpeedPathSelection)
      {
        String param = element.getAttribute("speedWeight").trim();
        if(param.length()==0)
          throw new Exception("\"speedWeight\" attribute of path selection is missing!");
        float speedWeight = Float.parseFloat(param);
        
        ((SpeedPathSelection)algo).setSpeedWeight(speedWeight);
      }
    }

    n = element.getElementsByTagName("reflect_directions").item(0);
    if((n!=null)&&(Boolean.valueOf(n.getFirstChild().getNodeValue()).booleanValue()))
    {
      reflect_directions = PathSearchingAlgorithm.FLAG_REFLECT_DIRECTIONS;	
    }
		
		// JHNote (30/11/2005): code added to reflect the stong links between reflect_direction in SpaceGraph and here
		// In order to reflect direction here, if we use a space graph, we need to set the 
		// reflect direction to true in both case.
		// JHNote (15/09/2006): the directions are now in SpatialModel
		if ((spatialModel != null) && (spatialModel.getDirections() != Boolean.valueOf(n.getFirstChild().getNodeValue()).booleanValue())) {
			throw new Exception("\"reflect_direction\" attribute of path selection need to be identical to the one on the SpatialModel!");
		}


    // initialize activities
    java.util.Map activities = new java.util.HashMap();
    org.w3c.dom.NodeList act_list = element.getElementsByTagName("activity");
    if(act_list.getLength()==0)
      throw new Exception("<activity> are missing!");
    for (int i=0; i<act_list.getLength(); i++)
    {
      State state = new State();

      org.w3c.dom.Element act_element = (org.w3c.dom.Element)act_list.item(i);
      String id = act_element.getAttribute("id");
      if (id.length()==0)
        throw new Exception("\"id\" attribute is missing!");

      state.setID(id);
      activities.put(id, state);      
      
      // add locations
      float cum_p = 0.0f;
      // check if the set of points is given
      n = act_element.getElementsByTagName("points").item(0);
      if (n!=null)
      {
        String fileSource = n.getFirstChild().getNodeValue();

        n = act_element.getElementsByTagName("minstay").item(0);
        if(n==null)
          throw new Exception("<minstay> is missing!");
        int dmin = (int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);        
        
        n = act_element.getElementsByTagName("maxstay").item(0);
        if(n==null)
          throw new Exception("<maxstay> is missing!");
        int dmax = (int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);        

        // check
        if (dmin<0)
          throw new Exception("Invalid <minstay> value: "+(float)dmin/1000);
        if (dmax<dmin)
          throw new Exception("Invalid <maxstay> value: "+(float)dmax/1000);
 
        // read the points
        java.io.BufferedReader source = new java.io.BufferedReader(new java.io.FileReader(fileSource));

        String s;
        // read next record
        while ((s = source.readLine())!=null)
        {
          String ss[] = s.split(" ");

          double x = Double.parseDouble(ss[0]);
          double y = Double.parseDouble(ss[1]);
        
          Location loc = new Location();
          loc.setPoint(new Point(x, y));
          loc.setMinStay(dmin);
          loc.setMaxStay(dmax);
          
          state.getLocations().add(loc);          
        }
        
        // check if not empty
        if (state.getLocations().size()==0)
          throw new Exception("The source "+fileSource+" does not contain any point!");
        
        // calculate a probability
        float p = 1.0f/state.getLocations().size();
        cum_p = 1.0f;
                
        java.util.Iterator iter = state.getLocations().iterator();
        while (iter.hasNext())
        {
          Location loc = (Location)iter.next();
          loc.setP(p);
        }
      }
      else
      {
        org.w3c.dom.NodeList loc_list = act_element.getElementsByTagName("location");
        if(loc_list.getLength()==0)
          throw new Exception("<location> are missing!");
        for (int l=0; l<loc_list.getLength(); l++)
        {
          org.w3c.dom.Element loc_element = (org.w3c.dom.Element)loc_list.item(l);

          n = loc_element.getElementsByTagName("x").item(0);
          if(n==null)
            throw new Exception("<x> is missing!");
          double x = Double.parseDouble(n.getFirstChild().getNodeValue());

          n = loc_element.getElementsByTagName("y").item(0);
          if(n==null)
            throw new Exception("<y> is missing!");
          double y = Double.parseDouble(n.getFirstChild().getNodeValue());
        
          n = loc_element.getElementsByTagName("p").item(0);
          if(n==null)
            throw new Exception("<p> is missing!");
          float p = Float.parseFloat(n.getFirstChild().getNodeValue());

          n = loc_element.getElementsByTagName("minstay").item(0);
          if(n==null)
            throw new Exception("<minstay> is missing!");
          int dmin = (int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);        
        
          n = loc_element.getElementsByTagName("maxstay").item(0);
          if(n==null)
            throw new Exception("<maxstay> is missing!");
          int dmax = (int)(Float.parseFloat(n.getFirstChild().getNodeValue())*1000);        

          // check
          if (dmin<0)
            throw new Exception("Invalid <minstay> value: "+(float)dmin/1000);
          if (dmax<dmin)
            throw new Exception("Invalid <maxstay> value: "+(float)dmax/1000);
          
          Location loc = new Location();
          loc.setPoint(new Point(x, y));
          loc.setP(p);
          loc.setMinStay(dmin);
          loc.setMaxStay(dmax);
        
          cum_p+=p;

          state.getLocations().add(loc);
        }
      }
      
      // check the locations' probabilities
      if (cum_p!=1.0f)
        throw new Exception("Invalid locations' probability!");
        
      template_automaton.addState(state);
    }
    
    // process the transition matrix
    java.util.Map cum_p_map = new java.util.HashMap();
    org.w3c.dom.NodeList trans_list = element.getElementsByTagName("transition");
    if(trans_list.getLength()==0)
      throw new Exception("<transition> are missing!");
    for (int l=0; l<trans_list.getLength(); l++)
    {
      org.w3c.dom.Element trans_element = (org.w3c.dom.Element)trans_list.item(l);

      n = trans_element.getElementsByTagName("src").item(0);
      if(n==null)
        throw new Exception("<src> is missing!");
      String s_id = n.getFirstChild().getNodeValue();
      State s = (State)activities.get(s_id);
      if (s==null)
        throw new Exception("Invalid transition source state: "+s_id+"!");

      n = trans_element.getElementsByTagName("dest").item(0);
      if(n==null)
        throw new Exception("<dest> is missing!");
      String d_id = n.getFirstChild().getNodeValue();
      State d = (State)activities.get(d_id);
      if (d==null)
        throw new Exception("Invalid transition destination state: "+d_id+"!");
      
      n = trans_element.getElementsByTagName("p").item(0);
      if(n==null)
        throw new Exception("<p> is missing!");
      float p = Float.parseFloat(n.getFirstChild().getNodeValue());

      Float cum_p = (Float)cum_p_map.get(s_id);
      if (cum_p==null)
        cum_p_map.put(s_id, new Float(p));
      else
        cum_p_map.put(s_id, new Float(cum_p.floatValue()+p));      

      template_automaton.addTransition(s, d, p);
    }

    // check the transition probabilities
    java.util.Iterator iter = cum_p_map.keySet().iterator();
    while (iter.hasNext())
    {
      String state_id = (String)iter.next();
      float cum_p = ((Float)cum_p_map.get(state_id)).floatValue();

      if (cum_p!=1.0f)
        throw new Exception("Invalid cummulative transition probability for the state: "+state_id+"!");
    }
    
    u.sendNotification(new DebugNotification(this, u, "Automaton of activity sequences"));

    for (int i=0; i<template_automaton.getStates().size(); i++)
    {
      State state = (State)template_automaton.getStates().get(i);
      StringBuffer s = new StringBuffer();

      s.append("State "+i+" ("+state.getID()+")\n");

      for (int j=0; j<state.getLocations().size(); j++)
      {
        Location loc = (Location)state.getLocations().get(j);
        s.append(" "+loc.getPoint().getX()+","+loc.getPoint().getY()+" p="+loc.getP()
          +" minstay="+loc.getMinStay()+" maxstay="+loc.getMaxStay()+"\n");
      }

      u.sendNotification(new DebugNotification(this, u, s.toString()));
    }

    u.sendNotification(new DebugNotification(this, u, "Transition Matrix"));
    for (int i=0; i<template_automaton.getTransitionMatrix().length; i++)
    {
      StringBuffer s = new StringBuffer();
      for (int j=0; j<template_automaton.getTransitionMatrix().length; j++)
        s.append(" "+template_automaton.getTransitionMatrix()[i][j]);

      u.sendNotification(new DebugNotification(this, u, s.toString()));
    }

    u.sendNotification(new DebugNotification(this, u, " "));

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading ActivityBasedTripGenerator extension"));
  }
}
