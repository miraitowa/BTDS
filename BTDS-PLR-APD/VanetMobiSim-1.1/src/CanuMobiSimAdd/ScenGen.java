import java.util.Iterator;
import java.util.HashMap;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Comment;
import de.uni_stuttgart.informatik.canu.mobisim.core.Universe;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.LoaderOutput;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.DebugOutput;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.NSOutput;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.GlomosimOutput;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.ReportNodePositions;
import de.uni_stuttgart.informatik.canu.mobisim.simulations.TimeSimulation;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.SpatialModel;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.SpatialModelElement;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.Point;
import de.uni_stuttgart.informatik.canu.tripmodel.core.Automaton;
import de.uni_stuttgart.informatik.canu.tripmodel.core.Location;
import de.uni_stuttgart.informatik.canu.tripmodel.core.State;
import de.uni_stuttgart.informatik.canu.tripmodel.generators.ActivityBasedTripGenerator;
import de.uni_stuttgart.informatik.canu.tripmodel.pathalgorithms.PedestrianStochPathSelection;
import de.uni_stuttgart.informatik.canu.uomm.ConstantSpeedMotion;
import de.uni_stuttgart.informatik.canu.uomm.IntelligentDriverMotion;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2004
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.3
 */

/**
 * This class generates a simulation scenario from position traces.
 * @author Illya Stepanov
 */
public class ScenGen
{
  // global variables
  /**
   * Radius of a point of interest (in m)
   */
  static float radiusOfPOI = 5f;

  /**
   * Minimal duration of activity execution (in s)
   */
  static float minActivityDuration = 90f;
  
  /**
   * Scenario file's name
   */
  static String modelSource;
  
  /**
   * Source document
   */
  protected Document sourceDocument;

  /**
   * Trace file's name
   */
  static String traceFileName;
  
  /**
   * Name of the result scenario file
   */
  static String resultFile;
  
  /**
   * Spatial model
   */
  protected SpatialModel spatialModel;
  
  /**
   * Activities created from the spatial model
   * Key: activity name (String), value: State 
   */
  protected HashMap activities;
  
  /**
   * Trace data: String node ID -> TreeMap of Double time -> Point 
   */
  protected HashMap traceData;

  /**
   * Result trip chain
   */
  protected Automaton resultTripChain;
  
  /**
   * Constructor
   */
  public ScenGen()
  {
    Universe u=Universe.getReference();

    if (!loadModel())
      return;

    u.initialize();

    // derive parameters for user trip model
    
    // step 1: associate locations with activities
    createActivities();
    
    // step 2: create a trip chain
    createTripChain();
    
    // step 3: save the scenario file
    saveScenarioFile();
  }

  /**
   * Creates activities based on the spatial model
   */
  protected void createActivities()
  {
    activities = new HashMap();
    
    // add well-known activities
    State unclassified = new State();
    unclassified.setID("unclassified");
    activities.put(unclassified.getID(), unclassified);
    
    // associate points of interest with activities
    Iterator iter = spatialModel.getElements().values().iterator();
    while (iter.hasNext())
    {
      SpatialModelElement element = (SpatialModelElement)iter.next();
      if ((element.getClassCode().equals("73"))&&(element.getGeometry() instanceof Point))
      {
        // create a location
        Location location = new Location();
        location.setPoint((Point)element.getGeometry());
        location.setMinStay(-1);
        location.setMaxStay(-1);

        // hotel, city hall, post office, bank, warehouse, cargo center, embassy, goverment office
        // business facility, city center, exhibition or conference center
        if (element.getSubClassCode().equals("14")
          ||element.getSubClassCode().equals("23")
          ||element.getSubClassCode().equals("24")
          ||element.getSubClassCode().equals("28")
          ||element.getSubClassCode().equals("31")
          ||element.getSubClassCode().equals("51")
          ||element.getSubClassCode().equals("65")
          ||element.getSubClassCode().equals("67")
          ||element.getSubClassCode().equals("78")
          ||element.getSubClassCode().equals("79")
          ||element.getSubClassCode().equals("85"))
        {
          addActivityLocation("business", location);
        }
        // theater, cultural center, community center
        else if (element.getSubClassCode().equals("18")
               ||element.getSubClassCode().equals("19")
               ||element.getSubClassCode().equals("63"))
        {
          addActivityLocation("cultural", location);
        }
        // school, university or college
        else if (element.getSubClassCode().equals("72")
               ||element.getSubClassCode().equals("77"))
        {
          addActivityLocation("educational", location);
        }
        // restaurant, roadside dinner
        else if (element.getSubClassCode().equals("15")
               ||element.getSubClassCode().equals("71"))
        {
          addActivityLocation("meal", location);
        }
        // parking garage, open parking area
        else if (element.getSubClassCode().equals("13")
               ||element.getSubClassCode().equals("69"))
        {
          addActivityLocation("parking", location);
        }
        // swimming pool, camping, recreation facility, rest area
        else if (element.getSubClassCode().equals("38")
               ||element.getSubClassCode().equals("60")
               ||element.getSubClassCode().equals("70")
               ||element.getSubClassCode().equals("95"))
        {
          addActivityLocation("recreation", location);
        }
        // shopping center
        else if (element.getSubClassCode().equals("73"))
        {
          addActivityLocation("shopping", location);
        }
        // tourist office, museum, tourist attraction
        else if (element.getSubClassCode().equals("16")
               ||element.getSubClassCode().equals("17")
               ||element.getSubClassCode().equals("76"))
        {
          addActivityLocation("sightseeing", location);
        }
        else
        {
          // the location is unclassified
          addActivityLocation("unclassified", location);
        }
      }
    }
    
    // display the activities
    System.out.println("Possible activities:");
    iter = activities.values().iterator();
    while (iter.hasNext())
    {
      State activity = (State)iter.next();
      System.out.println(activity.getID()+", "+activity.getLocations().size()+" locations");
    }
    System.out.println();
  }

  /**
   * Adds the location for performing the activity. <br>
   * <br>
   * @param activityName name of the activity
   * @param location location
   */
  protected void addActivityLocation(String activityName, Location location)
  {
    State state = (State)activities.get(activityName);
    if (state==null)
    {
      state = new State();
      state.setID(activityName);
      activities.put(state.getID(), state);
    }
    state.getLocations().add(location);
  }
  
  /**
   * Gets the activity which corresponds to the point. <br>
   * <br> 
   * @param p point
   * @return array containing {State, Location} or {null, null} if not found
   */
  protected Object[] getActivity(Point p)
  {
    State resActivity = null;
    Location resLocation = null;
    
    // get the "initial" activity
    State initial = (State)activities.get("initial");

    Iterator iter1 = activities.values().iterator();
    while  (iter1.hasNext())
    {
      State activity = (State)iter1.next();
      
      Iterator iter2 = activity.getLocations().iterator();
      while (iter2.hasNext())
      {
        Location l = (Location)iter2.next();
        if (isPointWithinPointOfInterest(p, l.getPoint()))
        {
          if (resLocation!=null)
          {
            // check if the current location is closer
            if ((resLocation.getPoint().getDistance(p)<l.getPoint().getDistance(p))
                ||((resActivity!=initial)&&(resLocation.getPoint().getDistance(p)==l.getPoint().getDistance(p))))
              continue;
          }
          
          resLocation = l;
          resActivity = activity;
        }
      }
    }
    
    Object[] res = {resActivity, resLocation};
    return res;
  }

  /**
   * Gets the location within the given activity which corresponds to the point. <br>
   * <br> 
   * @param activity activity
   * @param p point
   * @return location or null if not found
   */
  protected Location getLocation(State activity, Point p)
  {
    Location location = null;
    Iterator iter = activity.getLocations().iterator();
    while (iter.hasNext())
    {
      Location l = (Location)iter.next();
      if (isPointWithinPointOfInterest(p, l.getPoint()))
        if ((location==null)||(l.getPoint().getDistance(p)<location.getPoint().getDistance(p)))
          location = l;
    }
    
    return location;
  }

  /**
   * Creates a trip chain from trace data
   */
  protected void createTripChain()
  {
    resultTripChain = new Automaton();
    
    // add the "initial" activity
    State initial = new State();
    initial.setID("initial");
    activities.put(initial.getID(), initial);
    resultTripChain.addState(initial);
    
    // get the "unclassified" activity
    State unclassified = (State)activities.get("unclassified");

    // independently process trace data for every mobile node
    Iterator iter1 = traceData.values().iterator();
    while (iter1.hasNext())
    {
      TreeMap traceEntries = (TreeMap)iter1.next();
      
      State previousActivity = null;
      Location previousLocation = null;
      double timeEnteringPreviousLocation = Double.NaN;
      Point initialPoint = null;
      Point previousPoint = null;
      double timeEnteringPreviousPoint = Double.NaN;
      boolean inMovement = false;
      
      // analyze current trace point
      Iterator iter2 = traceEntries.keySet().iterator();
      while (iter2.hasNext())
      {
        Double time = (Double)iter2.next();
        Point p = (Point)traceEntries.get(time);
        
        if (previousActivity==null)
        {
          // it is the initial point, so add it to the "initial" activity
          // if the location has already been added, update the number of selections
          Location location = getLocation(initial, p);
          if (location==null)
          {
            // add the location to the "initial" activity
            location = new Location();
            location.setPoint(p);
            location.setMinStay(-1);
            location.setMaxStay(-1);
            location.setP(1);
            initial.getLocations().add(location);
          }
          else
            location.setP(location.getP()+1);

          previousActivity = initial;
          previousLocation = location;
          timeEnteringPreviousLocation = time.doubleValue();
          initialPoint = location.getPoint();
          previousPoint = location.getPoint();
          timeEnteringPreviousPoint = time.doubleValue();
          
          // proceed to the next point
          continue;
        }

        // check if still at the initial position
        if ((initialPoint!=null)&&(isPointWithinPointOfInterest(p, previousLocation.getPoint())))
          continue;
        
        // skip later checks for the initial position
        initialPoint = null;
        
        Object[] a = getActivity(p);
        State activity = (State)a[0];
        Location location = (Location)a[1];
       
        // check if stayed long enough at this location
        if (!isPointWithinPointOfInterest(p, previousPoint))
        {
          previousPoint = p;
          timeEnteringPreviousPoint = time.doubleValue();
        }
        else
        if ((location==null)
             &&(time.doubleValue()-timeEnteringPreviousPoint>=minActivityDuration))
        {
          // it seems to be a point of interest
          // add it to the "unclassifed" activity
          location = new Location();
          location.setPoint(previousPoint);
          location.setMinStay(-1);
          location.setMaxStay(-1);
          location.setP(0);
          unclassified.getLocations().add(location);
          activity = unclassified;
          previousLocation = location;
          timeEnteringPreviousLocation = timeEnteringPreviousPoint;
        }
        
        if ((location!=null)&&(inMovement)&&(location==previousLocation)
            &&(time.doubleValue()-timeEnteringPreviousLocation>=minActivityDuration))
        {
          // activity execution is detected
          // update the number of selections of the location
          location.setP(location.getP()+1);
          
          // update the number of transitions
          int ind1 = resultTripChain.getStates().indexOf(previousActivity);
          int ind2 = resultTripChain.getStates().indexOf(activity);
          if (ind2==-1)
          {
            // insert the activity
            resultTripChain.addState(activity);
            resultTripChain.addTransition(previousActivity, activity, 0);
            ind2 = resultTripChain.getStates().indexOf(activity);
          }
          resultTripChain.getTransitionMatrix()[ind1][ind2]++;
          
          // set the new activity
          previousActivity = activity;
          
          inMovement = false;
        }
        
        // check if the location has been changed
        if (location==previousLocation)
          continue;
        
        if (previousLocation!=null)
        {
          // the node departed from the location
          // calculate the duration of activity execution
          double activityDuration = time.doubleValue()-timeEnteringPreviousLocation;
          // set minimal and maximal values
          if (activityDuration>=minActivityDuration)
          {
            int activityDurationInMS = (int)(activityDuration*1000);
            if ((previousLocation.getMinStay()==-1)||(previousLocation.getMinStay()>activityDurationInMS))
              previousLocation.setMinStay(activityDurationInMS);
            if ((previousLocation.getMaxStay()==-1)||(previousLocation.getMaxStay()<activityDurationInMS))
              previousLocation.setMaxStay(activityDurationInMS);
          }
        }
        
        // continue if not within a point of interest
        previousLocation = location;
        timeEnteringPreviousLocation = time.doubleValue();
        inMovement = true;
      }
    }
    
    // refine activity information
    iter1 = resultTripChain.getStates().iterator();
    while (iter1.hasNext())
    {
      State activity = (State)iter1.next();
      
      // calculate the sum of selections of the locations and refine activity durations
      float n = 0;
      Iterator iter2 = activity.getLocations().iterator();
      while (iter2.hasNext())
      {
        Location location = (Location)iter2.next();
        n += location.getP();
        
        if (location.getMinStay()==-1)
          location.setMinStay(0);
        if (location.getMaxStay()==-1)
          location.setMaxStay(0);
      }
      
      // calculate probabilities of selecting a location
      iter2 = activity.getLocations().iterator();
      while (iter2.hasNext())
      {
        Location location = (Location)iter2.next();
        if (location.getP()!=0.0f)
          // calculate the probability
          location.setP(location.getP()/n);
        else
          // remove the location with 0 probability
          iter2.remove();
      }
    }

    // display transitions
    System.out.println("Transitions:");
    for (int i=0; i<resultTripChain.getTransitionMatrix().length; i++)
    {
      for (int j=0; j<resultTripChain.getTransitionMatrix().length; j++)
        System.out.print(" "+(int)resultTripChain.getTransitionMatrix()[i][j]);
      System.out.println();
    }
    System.out.println();

    // calculate probabilities of transitions
    for (int i = 0; i<resultTripChain.getStates().size(); i++)
    {
      float n = 0;
      
      // calculate the number of transitions from the state 
      for (int j = 0; j<resultTripChain.getStates().size(); j++)
      {
        n += resultTripChain.getTransitionMatrix()[i][j];
      }
      
      // calculate probabilities
      for (int j = 0; j<resultTripChain.getStates().size(); j++)
      {
        resultTripChain.getTransitionMatrix()[i][j] = resultTripChain.getTransitionMatrix()[i][j] / n;
      }
    }
    
    // display the result trip chain
    System.out.println("The result trip chain:\n\nActivities:");
    for (int i=0; i<resultTripChain.getStates().size(); i++)
    {
      State activity = (State)resultTripChain.getStates().get(i);

      System.out.println("Activity "+i+" ("+activity.getID()+")");

      for (int j=0; j<activity.getLocations().size(); j++)
      {
        Location location = (Location)activity.getLocations().get(j);
        System.out.println(" "+location.getPoint().getX()+", "+location.getPoint().getY()
            +" p="+location.getP()+" minActivityDuration="+(float)location.getMinStay()/1000
            +" (s) maxActivityDuration="+(float)location.getMaxStay()/1000+" (s)");
      }
    }
    System.out.println();
    
    System.out.println("Transition matrix:");
    for (int i=0; i<resultTripChain.getTransitionMatrix().length; i++)
    {
      for (int j=0; j<resultTripChain.getTransitionMatrix().length; j++)
        System.out.print(" "+resultTripChain.getTransitionMatrix()[i][j]);
      System.out.println();
    }
  }

  /**
   * Checks if the point is within the point of interest. <br>
   * <br>
   * @param p point
   * @param pointOfInterest point of interest
   * @return true if the point is within the point of interest
   */
  protected boolean isPointWithinPointOfInterest(Point p, Point pointOfInterest)
  {
    // TODO: use real dimensions as provided by AWMLReader, GDF does not offer such information
    if ( (Math.abs(p.getX()-pointOfInterest.getX())<radiusOfPOI)
       &&(Math.abs(p.getY()-pointOfInterest.getY())<radiusOfPOI) )
      return true;
    else
      return false;
  }
  
  /**
   * Initializes the model from source file. <br>
   * <br>
   * @return true, if the model was successfully loaded
   */
  protected boolean loadModel()
  {
    boolean retval=true;

    Universe u=Universe.getReference();

    try
    {
      // parse the scenario
        
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();

      Document document = builder.parse(new FileInputStream(modelSource));
      sourceDocument = document;

      Element root=document.getDocumentElement();

      String rootTag=root.getNodeName();
      if (!rootTag.equals("universe"))
        throw new Exception("Invalid parent tag: "+rootTag);

      u.load(root);

      // read the trace
      traceData = new HashMap();
      BufferedReader traceReader = new BufferedReader(new FileReader(traceFileName));
      
      // read the trace
      String s;
      // read next record
      while ((s = traceReader.readLine())!=null)
      {
        String ss[] = s.split(" ");

        String nodeID = ss[0].trim();
        Double time = new Double(ss[1].trim());
        double x = Double.parseDouble(ss[2].trim());
        double y = Double.parseDouble(ss[3].trim());
        
        TreeMap traceEntry = (TreeMap)traceData.get(nodeID);
        if (traceEntry==null)
        {
          traceEntry = new TreeMap();
          traceData.put(nodeID, traceEntry);
        }
        
        traceEntry.put(time, new Point(x, y));
      }

      // get the spatial model
      spatialModel = (SpatialModel)u.getExtension("SpatialModel");
      if (spatialModel==null)
        throw new Exception("Spatial Model does not exist!");
    }
    catch(Exception e)
    {
      System.err.println("Error loading model from "+modelSource);
      e.printStackTrace(System.err);
      retval=false;
    }

    return retval;
  }

  /**
   * Saves the scenario file
   */
  protected void saveScenarioFile()
  {
    try
    {
      // use the original document as a template
      Document document = sourceDocument;
      
      // add some hints as comments
      // loader output
      Comment commentOutputLoaderBasic = document.createComment("TODO: uncomment the next line to track scenario file processing");
      document.getDocumentElement().appendChild(commentOutputLoaderBasic);
      Comment commentOutputLoader = document.createComment("<extension class=\""+LoaderOutput.class.getName()+"\"/>");
      document.getDocumentElement().appendChild(commentOutputLoader);

      // mobility traces
      Comment commentOutputBasic = document.createComment("TODO: uncomment an extension to output mobility traces");
      document.getDocumentElement().appendChild(commentOutputBasic);
      Comment commentOutputDebug = document.createComment("<extension class=\""+DebugOutput.class.getName()+"\"/>");
      document.getDocumentElement().appendChild(commentOutputDebug);
      Comment commentOutputNs = document.createComment("<extension class=\""+NSOutput.class.getName()+"\" output=\"ns_trace_filename\"/>");
      document.getDocumentElement().appendChild(commentOutputNs);
      Comment commentOutputGlomosim = document.createComment("<extension class=\""+GlomosimOutput.class.getName()+"\"/>");
      document.getDocumentElement().appendChild(commentOutputGlomosim);
      Comment commentOutputReport = document.createComment("<extension class=\""+ReportNodePositions.class.getName()
          +"\" output=\"trace_filename\"><step>report_step_value</step></extension>");
      document.getDocumentElement().appendChild(commentOutputReport);
      
      // simulation time
      Comment commentTimeSimulationBasic = document.createComment("TODO: set a simulation end time");
      document.getDocumentElement().appendChild(commentTimeSimulationBasic);
      Element timeSimulation = document.createElement("extension");
      timeSimulation.setAttribute("class", TimeSimulation.class.getName());
      timeSimulation.setAttribute("param", "simulation_end_time");
      document.getDocumentElement().appendChild(timeSimulation);

      // add an "activity-based trip generator"
      Element tripGen = document.createElement("extension");
      final String generatedTripChainName = "GeneratedTripChain"; 
      tripGen.setAttribute("name", generatedTripChainName);
      tripGen.setAttribute("class", ActivityBasedTripGenerator.class.getName());
      tripGen.setAttribute("path_algorithm", PedestrianStochPathSelection.class.getName());
      tripGen.setAttribute("theta", ""+Float.MAX_VALUE);
      document.getDocumentElement().appendChild(tripGen);
      
      // add activities
      Iterator iter1 = resultTripChain.getStates().iterator();
      while (iter1.hasNext())
      {
        State state = (State)iter1.next();
        
        Element activity = document.createElement("activity");
        activity.setAttribute("id", state.getID());
        tripGen.appendChild(activity);
        
        // add locations
        Iterator iter2 = state.getLocations().iterator();
        while (iter2.hasNext())
        {
          Location loc = (Location)iter2.next();
          Element location = document.createElement("location");
          activity.appendChild(location);
          
          Element x = document.createElement("x");
          Node textX = document.createTextNode(""+loc.getPoint().getX());
          x.appendChild(textX);
          location.appendChild(x);
          
          Element y = document.createElement("y");
          Node textY = document.createTextNode(""+loc.getPoint().getY());
          y.appendChild(textY);
          location.appendChild(y);
          
          Element p = document.createElement("p");
          Node textP = document.createTextNode(""+(float)loc.getP());
          p.appendChild(textP);
          location.appendChild(p);

          Element minStay = document.createElement("minstay");
          Node minStayText = document.createTextNode(""+(float)loc.getMinStay()/1000);
          minStay.appendChild(minStayText);
          location.appendChild(minStay);

          Element maxStay = document.createElement("maxstay");
          Node maxStayText = document.createTextNode(""+(float)loc.getMaxStay()/1000);
          maxStay.appendChild(maxStayText);
          location.appendChild(maxStay);
        }
      }
      
      // add transitions
      for (int i=0; i<resultTripChain.getStates().size(); i++)
      {
        State s = (State)resultTripChain.getStates().get(i);
        
        for (int j=0; j<resultTripChain.getStates().size(); j++)
        {
          float prob = resultTripChain.getTransitionMatrix()[i][j];
          if (prob==0.0)
            continue;

          State d = (State)resultTripChain.getStates().get(j);

          Element transition = document.createElement("transition");
          tripGen.appendChild(transition);
          
          Element src = document.createElement("src");
          Node textSrc = document.createTextNode(""+s.getID());
          src.appendChild(textSrc);
          transition.appendChild(src);
          
          Element dest = document.createElement("dest");
          Node textDest = document.createTextNode(""+d.getID());
          dest.appendChild(textDest);
          transition.appendChild(dest);
          
          Element p = document.createElement("p");
          Node textP = document.createTextNode(""+prob);
          p.appendChild(textP);
          transition.appendChild(p);
        }
      }

      // mobile nodes
      Comment commentMobileNodes = document.createComment("TODO: set a number of mobile nodes");
      document.getDocumentElement().appendChild(commentMobileNodes);
      Element nodegroup = document.createElement("nodegroup");
      nodegroup.setAttribute("n", "number_of_mobile_nodes");
      document.getDocumentElement().appendChild(nodegroup);
      
      // movement dynamics
      Comment commentMovementDynamics = document.createComment("TODO: uncomment a required movement dynamics model");
      nodegroup.appendChild(commentMovementDynamics);
      
      Comment commentMovementDynamicsConstantSpeed = document.createComment("<extension class=\""
          +ConstantSpeedMotion.class.getName()
          +"\" initposgenerator=\""+generatedTripChainName
          +"\" tripgenerator=\""+generatedTripChainName+"\">"
          +"<minspeed>minspeed_value</minspeed>"
          +"<maxspeed>maxspeed_value</maxspeed>"
          +"<minpause>minpause_at_crossing_value</minpause>"
          +"<maxpause>max_pause_at_crossing_value</maxpause>"
          +"</extension>");
      nodegroup.appendChild(commentMovementDynamicsConstantSpeed);
      
      Comment commentMovementDynamicsIdm = document.createComment("<extension class=\""
          +IntelligentDriverMotion.class.getName()
          +"\" initposgenerator=\""+generatedTripChainName
          +"\" tripgenerator=\""+generatedTripChainName+"\">"
          +"<minspeed>minspeed_value</minspeed>"
          +"<maxspeed>maxspeed_value</maxspeed>"
          +"<step>parameters_recalculation_step_value</step>"
          +"</extension>");
      nodegroup.appendChild(commentMovementDynamicsIdm);

      // save the document
      OutputFormat format = new OutputFormat("xml", "UTF-8", true);
      XMLSerializer serializer = new XMLSerializer(new FileOutputStream(resultFile), format);
      serializer.serialize(document);
    }
    catch (Exception e)
    {
      System.err.println("Error writing the scenario file "+resultFile);
      e.printStackTrace(System.err);
    }
  }
  
  //----------------------------------------------------------------------------
  //----------------------------------------------------------------------------
  public static void main(String[] args)
  {
    if ( (args.length!=3)||(args[0].equals("-help")) )
    {
      System.out.println("Usage: ScenGen modelSource.xml tracefile resultFile.xml\n");
      return;
    }

    modelSource=args[0];
    traceFileName=args[1];
    resultFile=args[2];

    new ScenGen();
  }
}
