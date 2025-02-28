package eurecom.tigerreader;

/**
 * <p>Title: TIGER Reader</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Institut Eurecom</p>
 * <p>	v1.2 (14/08/2006): Added the option to load the 
 *												 intermediate points from TIGER files. The intermediate
 *												 points are parsed from a second TIGER file ".TR2".
 *												 This feature has not been yet validated for the simulator
 *												 as it is not compatible with IDM_IM and IDM_LC. Other features
 * 												 includes a faster cross-referencing for junctions (ID=coord: "X_Y")
 *												 and limitation to road parsing (roadType A..). Finally, a major bug
 *												 in coordiate parsing has been removed, and as a factor for misunderstanding, the scale factors
 * 												 have been removed from the xml configuration definition. The scale factor is set by default in TIGER
 *												 files to 1e-6.</p>
 * @author Jerome Haerri (haerri@ieee.org)
 * @version 1.2 
 */

import java.io.*;
import eurecom.spacegraph.graphalgorithm.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import eurecom.spacegraph.*;
import eurecom.spatialmodel.extensions.*;

/**
 * This class is used to parse and read geographic data in TIGER format
 * @version 1.2
 * @author Jerome Haerri 
 */
public class TIGERReader extends ExtensionModule
{
  /**
   * Source stream
   */
  protected BufferedReader source;
	
	/**
	* Input Stream
	* @since 1.0
	*/
	protected java.io.InputStream is =null;
  /**
   * Text from current record
   */
  protected java.util.ArrayList lines = new java.util.ArrayList();
	
  /**
   * Index of next symbol in lines[0] under analysis
   */
  protected int ind;

  /**
   * Spatial Model
   */
  protected SpatialModel spatialModel;
	
  /**
   * Scale factor for longitude-coordinates
   */
  protected double scale_long = 0.000001;

  /**
   * Scale factor for lattitude-coordinates
   */
  protected double scale_lat = 0.000001;
  
  /**
   * Clipping Region
   */
  protected Polygon clipArea;
	
	/**
   * Clipping Region extreme values in UTM. It overrides the SpatialModel clipping area
   */
	public float min_x_clip = Float.NaN, max_x_clip = Float.NaN, min_y_clip = Float.NaN, max_y_clip = Float.NaN;
	
	/**
   * Clipping size. It overrides the SpatialModel clipping area
   */
	public float sizeX, sizeY;
	
	/**
   * Coordinates of the central point of the clipping area. Along with the size, it defines the clipping area.
   */
	public double centerLatitude, centerLongitude;
	
	/**
   * TIGER record extreme values in TIGER coordinates (lat/long)
   */
	protected double minLat, maxLat, minLong, maxLong;
	
	/**
   * Double Flow disabled
   */
	protected boolean doubleFlow = false;
	
	/**
   * Double Flow disabled
   */
	protected String tigerAddress = null;
	
	/**
   * Maping between road types and speed limitations
   */
	protected java.util.HashMap speedMap = null;
	
	/**
	 * array that keeps track of nodes that are on the simulation boundary
   */
	public java.util.ArrayList bounderyPoints = new java.util.ArrayList();
	
  /**
   * Constructor
   */
  public TIGERReader()
  {
    super("TIGERReader");
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "TIGER Reader module";
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
    return -1;
  }

  /**
   * Loads TIGER records of type 1 (.TR1)
   */
  protected void loadRecord() throws Exception {
    // clear buffer
    ind = 0;
    lines.clear();

    // mark the beginning of the record
    source.mark(8192);

    String s;
		
		double longStart,latStart,longEnd,latEnd;
		
		String sign1,sign2,sign3,sign4=null;
		
      
     minLat=minLong=1000000000000.0;
     maxLat=maxLong=-999999999999.0;     
    // read next record
    while ((s = source.readLine())!=null) {
			
			// loading the records AND extracting the bounding box
			
			longStart=Double.parseDouble((s.substring(190, 200)).trim());
			latStart=Double.parseDouble((s.substring(200, 209)).trim());
			longEnd=Double.parseDouble((s.substring(209, 219)).trim());
			latEnd=Double.parseDouble((s.substring(219, 228)).trim());
				 
			if (latStart>maxLat) {
			  maxLat=latStart;
			}
			if (latStart<minLat) {
				 minLat=latStart;
			}
			if (longStart>maxLong) {
				 maxLong=longStart;
			}
			if (longStart<minLong) {
				 minLong=longStart;
			}
			
			if (latEnd>maxLat) {
				 maxLat=latEnd;
			}
			 
			if (latEnd<minLat) {
				 minLat=latEnd;
			}
			 
	    if (longEnd>maxLong) {
				 maxLong=longEnd;
			}
			
			if (longEnd<minLong) {
				 minLong=longEnd;
			}
			
      lines.add(s);
    }
		
		
			Point origin = convertToLocalCoordinates(minLong,minLat);
			Point extrem = convertToLocalCoordinates(maxLong,maxLat);
			
			/*System.out.println("minLat "+minLat+" minLong "+minLong+" maxLat "+maxLat+" maxLong "+maxLong);
			System.out.println("origin X" + origin.getX() + "origin Y"+ origin.getY());
			System.out.println("extrem X" + extrem.getX() + "extrem Y"+ extrem.getY());*/
		
  }

  /**
   * Gets the next field of currently read record. <br>
   * <br>
   * Gets next field of currently read record.
   * Set size to -1 to get the rest of current line
   * @param size size of the field (in symbols)
   * @return field value or null if eof reached
   */
  protected String getNextField(int size) throws Exception {
    String s;
    // get next field from the record
 
    if (lines.size()==0)
       return null;
		
		s = (String)lines.get(0);
		
    s = s.substring(ind, ind+size);
    ind+=size;

    return s;
  }

	
	/**
   * Loads TIGER records Type 2 (.TR2)
   */
  protected void loadRecord2() throws Exception {
    // clear buffer
    ind = 0;
    lines.clear();

    // mark the beginning of the record
    source.mark(8192);

    String s;
		
    // load next record
    while ((s = source.readLine())!=null) {
			lines.add(s);
		}
	}
	
	/**
	* Processes the TIGER records type 2 (.TR2)
	*
	*/
	protected void processTigerRecord2() throws Exception {
		
		java.io.InputStream is2 = null;
		boolean removedLine=false;
		String roadID=null;
		String s;		
		double recordType;
		java.util.ArrayList intPoints= new java.util.ArrayList();
		java.util.Map elements = spatialModel.getElements();
		
		String fullAddress = tigerAddress+".RT2";
		try {
			java.net.URL url2 = new java.net.URL(fullAddress);
			is2 = url2.openStream();
			source = new BufferedReader(new InputStreamReader(is2, "ISO-8859-1"));
		}
		catch (java.net.MalformedURLException mue) {
      mue.printStackTrace();
			source = new BufferedReader(new InputStreamReader(new FileInputStream(fullAddress), "ISO-8859-1"));
    }
		finally {
			double long1,lat1;
			String sign1, sign2 =null;
			// read until eof reached
			loadRecord2();
			while (lines.size() != 0) {
				ind = 0;
				recordType=parseInt(getNextField(5));
				roadID= getNextField(10).trim();
			
				if (elements.containsKey(roadID)) {
					SpatialModelElement tmpRoadElement = (SpatialModelElement)elements.get(roadID);
					if ( !tmpRoadElement.getClassCode().equals("41") || !tmpRoadElement.getSubClassCode().equals("10") ) {
						lines.remove(0);
						intPoints.clear();
						continue;
					}
							
					int sequenceNumber = parseInt(getNextField(3));
						
					long1=parseDouble(getNextField(10));
						
					lat1=parseDouble(getNextField(9));
						
					//System.out.println("Start1 "+long1+"Start1" + lat1);
						
					if ((long1 < minLong) ||  (long1 > maxLong) || (lat1 < minLat) || (lat1 > maxLat)) {
						/*lines.remove(0);
						intPoints.clear();
						continue;*/
					}
					else {
						Point internalPoints = convertToLocalCoordinates(long1,lat1);
						intPoints.add(internalPoints);
					}
					removedLine = getInternalPoints(intPoints, roadID, sequenceNumber);
					
					Polyline shapeEdge = (Polyline)tmpRoadElement.getGeometry();
					java.util.ArrayList points = shapeEdge.getPoints();
					
					java.lang.Object init = points.remove(0);
					java.lang.Object end =  points.remove(0);
						
					if (points.size() > 0) {
					  System.out.println("BUGBUG here...");
						System.exit(-1);
					}
						
					points.add(init);
					for (int i=0; i < intPoints.size(); i++) {
						points.add(intPoints.get(i));
					}
					intPoints.clear();
					points.add(end);
					
					if(points.size() > 0)
						spatialModel.clip(tmpRoadElement);
					
				}
				if (!removedLine)
					lines.remove(0);
				else {
					intPoints.clear();
					removedLine=false;
				}
			}
		}
		
		try {
			is2.close();
		} 
		catch (java.io.IOException ioe) {
      ioe.printStackTrace();
	  }
	}
	
	/**
	* Extracts internal points (also called Shape Points in TIGER nomoclature) given the road ID and the sequence number <br>
	*
	* @param intPoints List originally empty containing the shape points
	* @param roadID Road ID for which the shape points will be extracted
	* @param sequenceNumber Sequence number related the the road and the shape points in case more than a line of TIGER is needed
	* @return 1 - the method ended on a new line for a different roadID. So we shall not remove it in the mother method (as not yet processed, since for an other road)
	*         0 - the method ended normally on the line we processed. So the mother method will be allowed to remove this line (as fully processed)
	*/
	protected boolean getInternalPoints(java.util.ArrayList intPoints, String roadID, int sequenceNumber) throws Exception {
		
		double long1,lat1=0.0;
		String sign1,sign2=null;
		boolean removedLine = false;
		while (true) {

			long1=parseDouble(getNextField(10));
		
			if ((long1==0) || (ind >= 208) )
				break;
			
			lat1=parseDouble(getNextField(9));
			
						
			if ((long1 < minLong) ||  (long1 > maxLong) || (lat1 < minLat) || (lat1 > maxLat)) {
				//continue;
			}
			else {				
				Point internalPoints = convertToLocalCoordinates(long1,lat1);
				intPoints.add(internalPoints);
			}	
			if ((lat1 > 0) && (ind == 208)) {
				// this means more internal points will be found in the immediate next lines
			  lines.remove(0);
				ind = 0;
				
				String recordType=getNextField(5);
				String newRoadID= getNextField(10).trim();
				
				if (parseInt(newRoadID) != parseInt(roadID)) {
					// means the last line was full but this new line is for a different roadElement
					return true;
				}
				else {
					int newSequenceNumber = parseInt(getNextField(3));
					if (newSequenceNumber == (sequenceNumber+1)) {
						sequenceNumber=newSequenceNumber;
						removedLine = getInternalPoints(intPoints, roadID, sequenceNumber);
						break;
					}
				}
			}
		}
		return removedLine;
	}
	
	/**
	* Loads the clipping area from the central point and the size of the rectangular area. It overrides the clipping area from SpatialModel <br>
	*/
	public void loadClippingArea() {
		
		Point centerPoint = convertToLocalCoordinates(centerLongitude,centerLatitude);
	  
		double halfLengthX = sizeX/2;
		double halfLengthY = sizeY/2;
		
		min_x_clip = (float)(centerPoint.getX()-halfLengthX);
		max_x_clip = (float)(centerPoint.getX()+halfLengthX);
		
		min_y_clip = (float)(centerPoint.getY()-halfLengthY);
		
		max_y_clip = (float)(centerPoint.getY()+halfLengthY);
		
		//System.out.println("Lower Clipping point is: X= "+min_x_clip+" Y= "+min_y_clip);
		//System.out.println("Higher Clipping point is: X= "+max_x_clip+" Y= "+max_y_clip);
		
		clipArea = spatialModel.getClipArea();
		// TIGERReader override the clipArea defined in SpatialModel, as we define the area in a different way.
		clipArea.getPoints().clear();
		clipArea.getPoints().add(new Point(min_x_clip, min_y_clip));
		clipArea.getPoints().add(new Point(max_x_clip, min_y_clip));
		clipArea.getPoints().add(new Point(max_x_clip, max_y_clip));
		clipArea.getPoints().add(new Point(min_x_clip, max_y_clip));
	}
	
	
	/**
	* Processes the contents of the TIGER records type 1 (.TR1)
	*
	*/
	protected void processTigerRecord1() throws Exception {
		
		String roadID=null;
		String roadName = null;
		String roadType = null;
		
		double longStart,latStart,longEnd,latEnd,recordType;
		String sign1, sign2,sign3,sign4=null;
		java.util.Random rand=u.getRandom();
		java.util.Map elements = spatialModel.getElements();
		
		// JHNote (15/05/2006): We only consider roadElements and junctions.
		String class_code = "41";
		String subclass_code = "10";
		String subclass_code_junction = "20";
		

		double minLat, maxLat, minLon, maxLon;
      
     minLat=minLon=1000000.0;
     maxLat=maxLon=-999999.0;      
		 
		String s;		
		// read until eof reached
    loadRecord();
		
		loadClippingArea();
		
		while (lines.size() != 0) {
			ind = 0;
			
			recordType=parseInt(getNextField(5));
			
			roadID= getNextField(10).trim();
			
			ind=19; // jumping to the next interesting field
			
			roadName=getNextField(30);
			
			ind=55; // jumping to the next interesting field
			roadType=getNextField(3).trim();
			
			// JHNote (08/14/2006): In order to only consider "roads"
			if(roadType.charAt(0) != 'A') {
			  lines.remove(0);
				continue;
			}
			
			ind=190;
			longStart=parseDouble(getNextField(10));
			
			latStart=parseDouble(getNextField(9));
			
			longEnd=parseDouble(getNextField(10));
			
			latEnd=parseDouble(getNextField(9));
			
			lines.remove(0);
			
			//System.out.print("StartLong "+longStart+"StartLat" + latStart);
			
		
			Point shapeFrom = convertToLocalCoordinates(longStart,latStart);
			Point shapeTo = convertToLocalCoordinates(longEnd,latEnd);
			
			if (!clipArea.contains(shapeFrom) && !clipArea.contains(shapeTo) ){
			   continue; 
			}
			String newJunctionID1 = shapeFrom.getX()+"_"+shapeFrom.getY();
			String newJunctionID2 = shapeTo.getX()+"_"+shapeTo.getY();
			
			//System.out.println("shapeFrom X" + shapeFrom.getX() + "shapeFrom Y"+ shapeFrom.getY());
			SpatialModelElement element,junction2,junction1;
				
			// first checking if the junction already exists
			boolean newPointf = true;
			boolean newPointt = true;
			
			if(elements.containsKey(newJunctionID1))
				 newPointf = false;
			
			if(elements.containsKey(newJunctionID2))
				 newPointt = false;
			
		
			if (newPointf) {
				junction1 = new SpatialModelElement(newJunctionID1, class_code, subclass_code_junction, shapeFrom);
				junction1.getAttributes().put("JT","1"); // junction type : mini roundabout
			  // clip
				if (clipArea!=null) {
					if (clipArea.contains(shapeFrom)) 
						elements.put(newJunctionID1, junction1);
							//else
							//	System.out.println("Could not clip point with pos X " + pointf.getX() + " Y " + pointf.getY());
					  
					}
					else
					  elements.put(newJunctionID1, junction1);	
					  
			}
					
			if (newPointt) {
				junction2 = new SpatialModelElement(newJunctionID2, class_code, subclass_code_junction, shapeTo);
				junction2.getAttributes().put("JT","1"); // junction type : mini roundabout
				// clip
				if (clipArea!=null) {
					if (clipArea.contains(shapeTo)) {
						elements.put(newJunctionID2, junction2);
					}
							//else
							//	System.out.println("Could not clip point with pos X " + pointt.getX() + " Y " + pointt.getY());
						
				}
				else
					elements.put(newJunctionID2, junction2);
			
			}
			
			// now adding the roadElement
			Polyline shapeEdge = new Polyline();
      java.util.ArrayList points = shapeEdge.getPoints();
			points.add(shapeFrom);
			points.add(shapeTo);
			
			element = new SpatialModelElement(roadID, class_code, subclass_code, shapeEdge);
			element.getAttributes().put("RT",roadType);
			element.getAttributes().put("RN",roadName);
			element.getAttributes().put("DF","1"); // directional flow : 1= open to all direction
			element.getAttributes().put("NL","1"); // number of lane : 1= one lane in each directions
			element.getAttributes().put("VT","0"); // vehicle type : 0= all vehicles
			//  we set the maximum speed according the road type
			setSpeed(element,roadType);
			
			// clip
			if (clipArea!=null) {
				spatialModel.clip(element);
				if (shapeEdge.getPoints().size()>0)
					elements.put(roadID, element);
			}
			else
				elements.put(roadID, element);
			
		 }

		
		try {
				is.close();
		} 
			catch (java.io.IOException ioe) {
        ioe.printStackTrace();
	  }
		
	}
	
	
	/**
   * Converts the coordinates to the local cartesian coordinate system. <br>
   * <br>
   * @param longitude Longitude of the Point
	 * @param latitude Latitude of the Point
   * @return Point in the local cartesian coordinate system
	 * @see Point
   */
  protected Point convertToLocalCoordinates(double longitude, double latitude) {
   
		double deg2Radian = (3.14285/180);
	  double radiusEarth = (12756*1000/2); // meters
	
		//System.out.println("Old Point : X = " + longitude*scale_long + " Y = " + latitude*scale_lat);
	
		double relativeLong=(longitude-minLong)*scale_long;
		double relativeLat=(latitude-minLat)*scale_lat;
	
		double X = radiusEarth*relativeLong*deg2Radian;
	
		double tmpLat= relativeLat*deg2Radian;
	
		double Y=radiusEarth*0.5*Math.log((1+Math.sin(tmpLat))/(1-Math.sin(tmpLat)));
	
		Point res = new Point(X,Y);
	
		//System.out.println("New Point : X = " + res.getPosition().getX() + " Y = " + res.getPosition().getY());
		return res;	
  }
	
	
  /**
   * Parses the string argument as an integer
   * @param s string to be parsed
   * @return integer value from argument
   */
  protected static double parseDouble(String s)
  {
    s = s.trim();
    if (s.length()==0)
      s="0";
    
    return Double.parseDouble(s);
    //return new Integer(s).intValue();
  }
  
  /**
   * Parses the string argument as an integer
   * @param s string to be parsed
   * @return integer value from argument
   */
  protected static int parseInt(String s)
  {
    s = s.trim();
    if (s.length()==0)
      s="0";
    
    return new Integer(s).intValue();
  }
	
		/**
   * The the maximum speed on roadElement according to the road type and California speed limits or from a User-defined file. <br>
   * <br>
   * @param element RoadElement
	 * @param roadType type of the road in TIGER format
   */
		protected void setSpeed(SpatialModelElement element, String roadType) {
			
			if(speedMap !=null) { // User provided speed limitations
			  double mile = 1.609;
				String speed = (String)speedMap.get(roadType);
				 
				if(speed != null) {
					
					 double globalSpeed = (Double.parseDouble(speed)*mile)/3.6f; // conversion from miles/hour to meters/second
					 
					 element.getAttributes().put("SP",String.valueOf(globalSpeed)); // Speed Restriction (maxmimum speed)
				}
				else { // if we use the global value (A1 instead of A11)
					
				  String subRoadType = roadType.substring(0,2);
					speed = (String)speedMap.get(subRoadType);
					if(speed != null) {
					  double globalSpeed = (Double.parseDouble(speed)*mile)/3.6f; // conversion from miles/hour to meters/second
						
						element.getAttributes().put("SP",String.valueOf(globalSpeed)); // Speed Restriction (maxmimum speed)
					}
					else {
						element.getAttributes().put("SP","15.6"); // Speed Restriction (maxmimum speed) : 15.6m/s= 56.3km/h (35mph)
					}
				}
			}
			
			else { // default California speed limitations
				//System.out.println("3: Default");
				if(roadType.charAt(1) == '1') {
					element.getAttributes().put("SP","28.22"); // Speed Restriction (maxmimum speed) : 33.33m/s= 104km/h (65mph)
				}
				else if (roadType.charAt(1) == '2') {
					element.getAttributes().put("SP","24.58"); // Speed Restriction (maxmimum speed) : 24.58m/s= 88.5km/h (55mph)
				}
				else if (roadType.charAt(1) == '3') {
					element.getAttributes().put("SP","20.1"); // Speed Restriction (maxmimum speed) : 20.1m/s= 72.4km/h (45mph)
				}
				else if (roadType.charAt(1) == '4') {
					element.getAttributes().put("SP","15.6"); // Speed Restriction (maxmimum speed) : 15.6m/s= 56.3km/h (35mph)
				}
				else if (roadType.charAt(1) == '5') {
					element.getAttributes().put("SP","6.77"); // Speed Restriction (maxmimum speed) : 6.77m/s= 24.14km/h (15mph)
				}
				else if (roadType.charAt(1) == '6') {
					element.getAttributes().put("SP","11.17"); // Speed Restriction (maxmimum speed) : 11.17m/s= 40.23km/h (25mph)
				}
				else {
					element.getAttributes().put("SP","15.6"); // Speed Restriction (maxmimum speed) : 15.6m/s= 56.3km/h (35mph)
				}
			}
		}
		
  /**
    * Initializes the object from XML tag. <br>
    * <br>
		* <i>Version 1.2 by Jerome Haerri (haerri@ieee.org) (20/01/2006): 
		* <br> &nbsp;&nbsp;&nbsp;&nbsp; Changed the path style to a GDF file from a file system's style to URL style
		*													(similar to absolute system-dependant address but with the prefix &nbsp;&nbsp;&nbsp;&nbsp; "file://" )</i>
		* 
		* <br>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading TIGERReader extension"));

    super.load(element);
		
		// the Tigier file .TR2 contains the shape coordiate of the Tiger segments
		boolean tr2 = false;
		
    String s;
		
		s = element.getAttribute("spatial_model");
    if (s.length()>0) {
      spatialModel = (SpatialModel)u.getExtension(s);
    }
		
		else {
			spatialModel = (SpatialModel)u.getExtension("SpatialModel");
		}
		
		s = element.getAttribute("shapeCoord");
		if((s.length()>0) && (Boolean.valueOf(s).booleanValue())) {
			tr2 = true;
		}
		
		s = element.getAttribute("center_lat");
		if(s.length()>0)
      centerLatitude = Float.parseFloat(s);
		
		s = element.getAttribute("center_long");
		if(s.length()>0)
      centerLongitude = Float.parseFloat(s);
		
		s = element.getAttribute("size_x");
    if(s.length()>0)
      sizeX = Float.parseFloat(s);

    s = element.getAttribute("size_y");
    if(s.length()>0)
      sizeY = Float.parseFloat(s);
		
    s = element.getAttribute("source");
    if(s.length()==0)
      throw new Exception("Invalid source name: "+s);
		
		tigerAddress=s;
		String fullAddress = tigerAddress+".RT1";
		try {
      // JHNote (24/01/2006): In order to ease the compatibility between the Applet version and 
			//											the java version, all references to scenario and source files
			//											will be done using a URL style address 
			//											(similar to absolute system-dependant address but with the prefix "file://" )
			//
																																			
			java.net.URL url = new java.net.URL(fullAddress);
      is = url.openStream();
			source = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
		}
		catch (java.net.MalformedURLException mue) {
      System.out.println("MalformedURLException for URL " + fullAddress);
      mue.printStackTrace();
			source = new BufferedReader(new InputStreamReader(new FileInputStream(fullAddress), "ISO-8859-1"));
    }
		catch (java.io.IOException ioe) {
      System.out.println("IOException for " + fullAddress);
      ioe.printStackTrace();
		}
		
		finally {
		  org.w3c.dom.Node n;
    
			n = element.getElementsByTagName("speed").item(0);
      if (n!=null) {
        String speeedFile = n.getFirstChild().getNodeValue();
				if (speeedFile !=null) { // userProvided Speed Limitations
					java.io.BufferedReader source = new java.io.BufferedReader(new java.io.FileReader(speeedFile));
					if(source !=null) {
						speedMap = new java.util.HashMap();
						String sf;
						// read next record
						while ((sf = source.readLine())!=null) {
							String speedArray[] = sf.split(" ");
							speedMap.put(speedArray[0],speedArray[1]);
						} 
					}
					else
						throw new Exception("Could not open speedFile: "+speeedFile);
					
				}
				else 
					throw new Exception("No speed file indicated in <speed> tag ");
				
			}
			
			doubleFlow = spatialModel.getDirections();
			processTigerRecord1();
				
			// JHNote (August 14th 2006): If we are interested in curved roadElements, we may include the shape points
			//														however, as for GDF, this is not compliant with IDM_IM or IDM_LC yet.
			if(tr2)
				processTigerRecord2();
			
			if(doubleFlow && !tr2) {
				spatialModel.createDoubleFlowRoads();
			}
			
			
			spatialModel.rebuildGraph();
			
			
			spatialModel.createSecondLayerElements();
			
			u.sendNotification(new LoaderNotification(this, u, "Finished loading TIGERReader extension"));
		}
  }
}