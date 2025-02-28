package de.uni_stuttgart.informatik.canu.gdfreader;

/**
 * <p>Title: GDF Reader</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * <p>Patches: </p>
 * <p> v1.2 (20/01/2006): Changed the path style to a GDF file
 *													file system's style to URL style
 *													(similar to absolute system-dependant address but with the prefix "file://"). </p>
 *  
 * @author Illya Stepanov
 * @author v1.2: Jerome Haerri (haerri@ieee.org)
 * @version 1.2
 */

import geotransform.coords.Gdc_Coord_3d;
import geotransform.coords.Utm_Coord_3d;
import geotransform.transforms.Gdc_To_Utm_Converter;

import java.io.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;

/**
 * This class is used to parse and read geographic data in GDF format
 * <p>Patches: </p>
 * <p> <i>Version 1.2  by Jerome Haerri (haerri@ieee.org) on 01/20/2006:
 * <br>		&nbsp;&nbsp;&nbsp;&nbsp; Changed the path style to a GDF file from a file system's style to URL style
 *				(similar to absolute system-dependant address but with the prefix "file://").</i> </p>
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.2 Jerome Haerri 
 * @version 1.2
 */
public class GDFReader extends ExtensionModule
{
  /**
   * Source stream
   */
  protected BufferedReader source;
	
	/**
	* Input Stream
	* @since 1.2
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
   * Name of the GDF-file producer
   */
  protected String supplierName="";

  /**
   * Storage for GDF sections:
   * Key - Section id, Value - associated GDFSection
   */
  protected java.util.Map sections = new java.util.HashMap();

  /**
   * Storage for default attributes values:
   * Key - Attribute id, Value - default value
   */
  protected java.util.Map defaultAttributesValues = new java.util.HashMap();

  /**
   * Spatial Model
   */
  protected SpatialModel spatialModel;

  /**
   * Scale factor for x-coordinates
   */
  protected double scale_x = 0.01;

  /**
   * Scale factor for y-coordinates
   */
  protected double scale_y = 0.01;
  
  /**
   * UTM-coordinates of the point with minimal X- and Y-values
   */
  protected Utm_Coord_3d origin;

  /**
   * Clipping Region
   */
  protected Polygon clipArea;

		/**
   * Double Flow disabled
   */
	protected boolean doubleFlow = false;
	
  /**
   * Constructor
   */
  public GDFReader()
  {
    super("GDFReader");
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "GDF Reader module";
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
   * Reads the next record
   * @return record type or null if eof reached
   */
  protected String getNextRecord() throws Exception
  {
    // clear buffer
    ind = 0;
    lines.clear();

    // mark the beginning of the record
    source.mark(8192);

    String s;
    // read next record
    while ((s = source.readLine())!=null)
    {
      lines.add(s);

      // if part of multi-line record, check for continuation prefix
      if (lines.size()>1)
      {
        if (!s.startsWith("00"))
          throw new Exception("Invalid continuation prefix!");
      }

      // check if single-line record
      if (s.charAt(79)=='0')
        break;
    }

    // eof reached
    if (lines.size()==0)
      return null;

    // get record type
    s = ((String)lines.get(0)).substring(0, 2);
    ind = 2;

    return s;
  }

  /**
   * Gets the next field of currently read record. <br>
   * <br>
   * Gets next field of currently read record.
   * Set size to -1 to get the rest of current line
   * @param size size of the field (in symbols)
   * @return field value or null if eof reached
   */
  protected String getNextField(int size) throws Exception
  {
    String s;
    // get next field from the record
    for (;;)
    {
      if (lines.size()==0)
        return null;

      s = (String)lines.get(0);

      // get rest of this line
      if (size==-1)
      {
        s = s.substring(ind, 79);

        // advance to next line
        ind = 2;      // skip continuation symbols
        lines.remove(0);

        return s;
      }

      // check if move to next line
      if (ind+size>79)
      {
        ind = 2;      // skip continuation symbols
        lines.remove(0);
        continue;
      }

      break;
    }

    s = s.substring(ind, ind+size);
    ind+=size;

    return s;
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
   * Processes the Volume Header Record
   */
  protected void processVolumeHeaderRecord() throws Exception
  {
  	supplierName = getNextField(20).trim();
  }

  /**
   * Processes the Default Attribute Record
   */
  protected void processDefaultAttributeRecord() throws Exception
  {
    String type = getNextField(2).trim();
    String value = getNextField(10).trim();

    // a hack for "NavTech"
    if (supplierName.toLowerCase().indexOf("navtech")!=-1)
    {
      // direction of traffic flow
      if (type.equals("DF"))
      {
        // convert the value to original GDF
        if (value.equals("2"))
          value = "3";
        else
        if (value.equals("3"))
          value = "2";
      }
    }
    
    defaultAttributesValues.put(type, value);
  }

  /**
   * Processes the Section Header Record
   */
  protected void processSectionHeaderRecord() throws Exception
  {
    String id = getNextField(10);
    GDFSection section = new GDFSection(this);
    sections.put(id, section);

    section.load();
  }

  /**
   * Loads GDF data from source stream
   */
  protected void doLoad() throws Exception
  {
    String s;

    // read until eof reached
    while ((s = getNextRecord())!=null)
    {
			// check record type code
      switch (parseInt(s))
      {
      	case 01:
      	  processVolumeHeaderRecord();
      	  break;

        case 15:
          processDefaultAttributeRecord();
          break;

        case 16:
          // check subcode
          int subCode = parseInt(getNextField(2));
          if (subCode==1)
          {
            processSectionHeaderRecord();
            break;
          }

          // fall through...

        // Sometimes the header for single section is omitted.
        // If one from data elements is found, create new section and
        // let it process it.
        case 23:
        case 24:
        case 25:
        case 29:
        case 41:
        case 44:
        case 45:
        case 46:
        case 50:
        case 51:
        case 52:
        case 53:
        case 54:
          if (sections.size()>0)
            throw new Exception("Invalid Data in Global Context!");

          // reset the stream
          source.reset();

          GDFSection section = new GDFSection(this);
          sections.put("", section);

          section.load();
          break;
      }
    }
		/* v1.2 JHNote (24/01/2006): We close the input stream here */ 
	/*	try {
				is.close();
		} 
			catch (java.io.IOException ioe) {
            // just going to ignore this one
	  }*/
  }

  /**
   * Resolves "lazy" links
   */
  protected void resolveLinks()
  {
    java.util.Iterator iter = sections.values().iterator();
    while (iter.hasNext())
    {
      ((GDFSection)iter.next()).initialise();
    }
  }

  /**
   * Constructs elements from loaded point features
   */
  protected void createPointFeatureElements()
  {
    java.util.Map elements = spatialModel.getElements();
    Object sections[] = this.sections.values().toArray();
    GDFSection section = (GDFSection)sections[0];
    // process point features
		
    java.util.Iterator iter = section.pointFeatures.keySet().iterator();
    while (iter.hasNext())
    {
			String id = (String)iter.next();
			GDFPointFeatureRecord feature = (GDFPointFeatureRecord)
        section.pointFeatures.get(id);
      String class_code = feature.featureClassCode.substring(0, 2);
      String subclass_code = feature.featureClassCode.substring(2, 4);

      // check if the feature is correct
      if (feature.nodes.size()==0)
        continue;
      if (feature.nodes.get(0)==null)
        continue;

      // get geometry
      GDFXYZRecord.XYZPoint coord = (GDFXYZRecord.XYZPoint)
        ((GDFNodeRecord)feature.nodes.get(0)).point.points.get(0);
      Point shape = convertToLocalCoordinates(coord);

      SpatialModelElement element = new SpatialModelElement(id, class_code, subclass_code, shape);

      // copy default attributes
      element.getAttributes().putAll(defaultAttributesValues);

      // copy attributes
      for (int i=0; i<feature.attributes.size(); i++)
      {
        GDFAttributeRecord attr = (GDFAttributeRecord)feature.attributes.get(i);
        if (attr==null)
          continue;

        element.getAttributes().putAll(attr.attributes);
      }

      // clip
      if (clipArea!=null)
      {
        if (clipArea.contains(shape)) {
          elements.put(id, element);
				}
      }
      else
        elements.put(id, element);
    }
  }

  /**
   * Constructs elements from loaded line features
   */
  protected void createLineFeatureElements()
  {
    java.util.Map elements = spatialModel.getElements();

    Object sections[] = this.sections.values().toArray();
    GDFSection section = (GDFSection)sections[0];
		
		//System.out.println("size of the lineFeature Map: "+ section.lineFeatures.size());
    // process line features
    java.util.Iterator iter = section.lineFeatures.keySet().iterator();
outer:
    while (iter.hasNext())
    {
      //System.out.println("Am I here?");
			String id = (String)iter.next();
      GDFLineFeatureRecord feature = (GDFLineFeatureRecord)
        section.lineFeatures.get(id);
      String class_code = feature.featureClassCode.substring(0, 2);
      String subclass_code = feature.featureClassCode.substring(2, 4);

      // check if the feature is correct
      if (feature.fPoint==null)
        continue outer;
      if (feature.tPoint==null)
        continue outer;
      for (int l=0; l<feature.edges.size(); l++)
        if (feature.edges.get(l)==null)
          continue outer;

      Polyline shape = new Polyline();
      java.util.ArrayList points = shape.getPoints();

      GDFXYZRecord.XYZPoint point;

      // add intermediate edges
      for (int l=0; l<feature.edges.size(); l++)
      {
        GDFEdgeRecord edge = (GDFEdgeRecord)feature.edges.get(l);
        int direction = ((Integer)feature.edgesDirections.get(l)).intValue();

        if (l==0)
        {
          // add starting point of an edge
          if (direction==0)
            point = (GDFXYZRecord.XYZPoint)edge.fNode.point.points.get(0);
          else
            point = (GDFXYZRecord.XYZPoint)edge.tNode.point.points.get(0);

          points.add(convertToLocalCoordinates(point));
        }

        // add intermediate points
        if (edge.points!=null)
        {
          java.util.ArrayList coordinates = edge.points.points;
          int m = (direction==0)? 0 : coordinates.size()-1;

          while ((m>=0)&&(m<coordinates.size()))
          {
            point = (GDFXYZRecord.XYZPoint)coordinates.get(m);
            points.add(convertToLocalCoordinates(point));

            if (direction==0)
              m++;
            else
              m--;
          }
        }

        // add ending point of an edge
        if (direction==0)
          point = (GDFXYZRecord.XYZPoint)edge.tNode.point.points.get(0);
        else
          point = (GDFXYZRecord.XYZPoint)edge.fNode.point.points.get(0);

        points.add(convertToLocalCoordinates(point));
      }

      // add junctions if no edges are specified
      if (points.size()==0)
      {
        // add starting Junction
        point = (GDFXYZRecord.XYZPoint)((GDFNodeRecord)
          feature.fPoint.nodes.get(0)).point.points.get(0);
        points.add(convertToLocalCoordinates(point));

        // add ending Junction
        point = (GDFXYZRecord.XYZPoint)((GDFNodeRecord)
          feature.tPoint.nodes.get(0)).point.points.get(0);
        points.add(convertToLocalCoordinates(point));
      }

      SpatialModelElement element = new SpatialModelElement(id, class_code, subclass_code, shape);

      // copy default attributes
      element.getAttributes().putAll(defaultAttributesValues);

      // copy attributes
      for (int i=0; i<feature.attributes.size(); i++)
      {
        GDFAttributeRecord attr = (GDFAttributeRecord)feature.attributes.get(i);
        if (attr==null) {
          element.getAttributes().put("DF","1"); // directional flow : 1= open to all direction
					element.getAttributes().put("NL","1"); // number of lane : 1= one lane in each directions
					element.getAttributes().put("VT","0"); // vehicle type : 0= all vehicles
					element.getAttributes().put("SP","13.88"); // Speed Restriction (maxmimum speed) : 13.88m/s= 50km/h
					continue;
				}

        element.getAttributes().putAll(attr.attributes);
				
				// JHNote (15/09/2006): these attributes are necessary for VanetMobiSim to work. So, if GDF does not include them,
				//                      by default, we override it.
				if(element.getAttributes().get("DF")==null)
					element.getAttributes().put("DF","1"); // directional flow : 1= open to all direction
				if(element.getAttributes().get("NL")==null)
					element.getAttributes().put("NL","1"); // number of lane : 1= one lane in each directions
				if(element.getAttributes().get("VT")==null)
					element.getAttributes().put("VT","0"); // vehicle type : 0= all vehicles
				if(element.getAttributes().get("SP")==null)
					element.getAttributes().put("SP","13.88"); // Speed Restriction (maxmimum speed) : 13.88m/s= 50km/h
				
      }

      // clip
      if (clipArea!=null)
      {
        spatialModel.clip(element);
        if (shape.getPoints().size()>0) {
          //System.out.println("clipped");
					elements.put(id, element);
				}
      }
      else
        elements.put(id, element);
    }
		
  }

  /**
   * Constructs elements from loaded area features
   */
  protected void createAreaFeatureElements()
  {
    java.util.Map elements = spatialModel.getElements();

    Object sections[] = this.sections.values().toArray();
    GDFSection section = (GDFSection)sections[0];

    // process area features
    java.util.Iterator iter = section.areaFeatures.keySet().iterator();
outer:
    while (iter.hasNext())
    {
      String id = (String)iter.next();
      GDFAreaFeatureRecord feature = (GDFAreaFeatureRecord)
        section.areaFeatures.get(id);
      String class_code = feature.featureClassCode.substring(0, 2);
      String subclass_code = feature.featureClassCode.substring(2, 4);

      Polygon shape = new Polygon();
      java.util.ArrayList points = shape.getPoints();

      // check if the feature is correct
      for (int i=0; i<feature.faces.size(); i++)
      {
        GDFFaceRecord face = (GDFFaceRecord)feature.faces.get(i);
        if (face==null)
          continue outer;

        for (int l=0; l<face.edges.size(); l++)
        {
          GDFEdgeRecord edge = (GDFEdgeRecord)face.edges.get(l);
          if (edge==null)
            continue outer;
        }
      }

      for (int i=0; i<feature.faces.size(); i++)
      {
        GDFFaceRecord face = (GDFFaceRecord)feature.faces.get(i);

        GDFXYZRecord.XYZPoint point;

        // add intermediate edges
        for (int l=0; l<face.edges.size(); l++)
        {
          GDFEdgeRecord edge = (GDFEdgeRecord)face.edges.get(l);

          int direction = ((Integer)face.edgesDirections.get(l)).intValue();

          if (l==0)
          {
            // add starting point of an edge
            if (direction==0)
              point = (GDFXYZRecord.XYZPoint)edge.fNode.point.points.get(0);
            else
              point = (GDFXYZRecord.XYZPoint)edge.tNode.point.points.get(0);

            points.add(convertToLocalCoordinates(point));
          }

          // add intermediate points
          if (edge.points!=null)
          {
            java.util.ArrayList coordinates = edge.points.points;
            int m = (direction==0)? 0 : coordinates.size()-1;

            while ((m>=0)&&(m<coordinates.size()))
            {
              point = (GDFXYZRecord.XYZPoint)coordinates.get(m);
              points.add(convertToLocalCoordinates(point));

              if (direction==0)
                m++;
              else
                m--;
            }
          }

          // add ending point of an edge
          if (direction==0)
            point = (GDFXYZRecord.XYZPoint)edge.tNode.point.points.get(0);
          else
            point = (GDFXYZRecord.XYZPoint)edge.fNode.point.points.get(0);

          points.add(convertToLocalCoordinates(point));
        }
      }

      SpatialModelElement element = new SpatialModelElement(id, class_code, subclass_code, shape);

      // copy default attributes
      element.getAttributes().putAll(defaultAttributesValues);

      // copy attributes
      for (int i=0; i<feature.attributes.size(); i++)
      {
        GDFAttributeRecord attr = (GDFAttributeRecord)feature.attributes.get(i);
        if (attr==null)
          continue;

        element.getAttributes().putAll(attr.attributes);
				
      }

      // clip
      if (clipArea!=null)
      {
        spatialModel.clip(element);
        if (shape.getPoints().size()>0)
          elements.put(id, element);
      }
      else
        elements.put(id, element);
    }
  }

  /**
   * Constructs elements from loaded complex features
   */
  protected void createComplexFeatureElements()
  {
    java.util.Map elements = spatialModel.getElements();

    Object sections[] = this.sections.values().toArray();
    GDFSection section = (GDFSection)sections[0];

    // process complex features
    java.util.Iterator iter = section.complexFeatures.keySet().iterator();
    while (iter.hasNext())
    {
      String id = (String)iter.next();
      GDFComplexFeatureRecord feature = (GDFComplexFeatureRecord)
        section.complexFeatures.get(id);
      String class_code = feature.featureClassCode.substring(0, 2);
      String subclass_code = feature.featureClassCode.substring(2, 4);

      SpatialModelElement element = new SpatialModelElement(id, class_code, subclass_code, null);

      // add child elements
      for (int i=0; i<feature.pointFeaturesID.size(); i++)
      {
        SpatialModelElement e = spatialModel.getElement((String)feature.pointFeaturesID.get(i));
        if (e!=null)
          element.getChildren().add(e);
      }

      for (int i=0; i<feature.lineFeaturesID.size(); i++)
      {
        SpatialModelElement e = spatialModel.getElement((String)feature.lineFeaturesID.get(i));
        if (e!=null)
          element.getChildren().add(e);
      }

      for (int i=0; i<feature.areaFeaturesID.size(); i++)
      {
        SpatialModelElement e = spatialModel.getElement((String)feature.areaFeaturesID.get(i));
        if (e!=null)
          element.getChildren().add(e);
      }

      for (int i=0; i<feature.complexFeaturesID.size(); i++)
      {
        SpatialModelElement e = spatialModel.getElement((String)feature.complexFeaturesID.get(i));
        if (e!=null)
          element.getChildren().add(e);
      }

      // copy default attributes
      element.getAttributes().putAll(defaultAttributesValues);

      // copy attributes
      for (int i=0; i<feature.attributes.size(); i++)
      {
        GDFAttributeRecord attr = (GDFAttributeRecord)feature.attributes.get(i);
        if (attr==null)
          continue;

        element.getAttributes().putAll(attr.attributes);
      }

      elements.put(id, element);
    }
  }

  /**
   * Constructs elements from loaded GDF data
   */
  protected void createElements()
  {
    createPointFeatureElements();
    createLineFeatureElements();
    createAreaFeatureElements();
    createComplexFeatureElements();
  }

  /**
   * Converts the Point to the local cartesian coordinate system. <br>
   * <br>
   * @param point point
   * @return point in the local cartesian coordinate system
   */
  protected Point convertToLocalCoordinates(GDFXYZRecord.XYZPoint point)
  {
    Utm_Coord_3d utm = new Utm_Coord_3d();
    Gdc_To_Utm_Converter.Convert(new Gdc_Coord_3d(point.y*scale_y, point.x*scale_x, 0), utm);
		
		//System.out.println("Old Point : X = " + point.x*scale_x + " Y = " + point.y*scale_y);
    Point res = new Point(utm.x-origin.x, utm.y-origin.y);
		//System.out.println("New Point : X = " + res.getPosition().getX() + " Y = " + res.getPosition().getY());
    return res;
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
      "Loading GDFReader extension"));

    super.load(element);

    spatialModel = (SpatialModel)u.getExtension("SpatialModel");

    String s;
		
    s = element.getAttribute("scale_x");
    if(s.length()>0)
      scale_x = Float.parseFloat(s);

    s = element.getAttribute("scale_y");
    if(s.length()>0)
      scale_y = Float.parseFloat(s);

    s = element.getAttribute("source");
    if(s.length()==0)
      throw new Exception("Invalid source name: "+s);
		
		try {
      // JHNote (24/01/2006): In order to ease the compatibility between the Applet version and 
			//											the java version, all references to scenario and source files
			//											will be done using a URL style address 
			//											(similar to absolute system-dependant address but with the prefix "file://" )
			//
																																			
			java.net.URL url = new java.net.URL(s);
      is = url.openStream();
			source = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
		}
		catch (java.net.MalformedURLException mue) {
      System.out.println("MalformedURLException for URL " + s);
      mue.printStackTrace();
			source = new BufferedReader(new InputStreamReader(new FileInputStream(s), "ISO-8859-1"));
    }
		catch (java.io.IOException ioe) {
      System.out.println("IOException for " + s);
      ioe.printStackTrace();
		}
		finally {
			  
				// prepare the converter, use wgs'84 by default
				clipArea = spatialModel.getClipArea();
				Gdc_To_Utm_Converter.Init();
				doLoad();
				resolveLinks();
				createElements();
				spatialModel.rebuildGraph();
			  
				spatialModel.createSecondLayerElements();
				
				u.sendNotification(new LoaderNotification(this, u,
																									"Finished loading GDFReader extension"));
		}
  }
}