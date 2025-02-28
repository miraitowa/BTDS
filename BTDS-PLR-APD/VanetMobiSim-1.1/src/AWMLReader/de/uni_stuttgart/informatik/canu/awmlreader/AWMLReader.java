package de.uni_stuttgart.informatik.canu.awmlreader;

/**
 * <p>Title: AWML Reader</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

import javax.xml.parsers.*;
import geotransform.coords.*;
import geotransform.transforms.*;
import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;

/**
 * This class reads geographic data in AWML format. <br>
 * <br>
  * @author Illya Stepanov
 */
public class AWMLReader extends ExtensionModule
{
    /**
     * Source stream
     */
    protected String fileSource;

    /**
     * Spatial Model
     */
    protected SpatialModel spatialModel;

    /**
     * Spatial Model elements loaded 
     */
    protected java.util.ArrayList elements = new java.util.ArrayList();

    /**
     * Min x-coordinate value
     */
    protected double min_x = Double.MAX_VALUE;

    /**
     * Min y-coordinate value 
     */
    protected double min_y = Double.MAX_VALUE;

    /**
     * Max x-coordinate value
     */
    protected double max_x = Double.MIN_VALUE;
    
    /**
     * Max y-coordinate value 
     */
    protected double max_y = Double.MIN_VALUE;

    /**
     * Min lat-coordinate value
     */
    protected double min_lat = Double.MAX_VALUE;

    /**
     * Min lon-coordinate value
     */
    protected double min_lon = Double.MAX_VALUE;

    /**
     * Max lat-coordinate value
     */
    protected double max_lat = Double.MIN_VALUE;

    /**
     * Max lon-coordinate value
     */
    protected double max_lon = Double.MIN_VALUE;

    /**
     * Clipping Region
     */
    protected Polygon clipArea;

    /**
     * Constructor
     */
    public AWMLReader()
    {
      super("AWMLReader");
    }

    /**
     * Returns the module's description. <br>
     * <br>
     * @return extension module's description
     */
    public String getDescription()
    {
      return "AWML Reader module";
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
     * Creates geometry elements from GML-based textual description. <br>
     * <br>
     * @return array of geometry elements
     */
    protected java.util.ArrayList processGeometry(String nexusGmlString) throws Exception
    {
        java.util.ArrayList res = new java.util.ArrayList();

        Class class_instance = null;
        if (nexusGmlString.startsWith("LINESTRING"))
          class_instance = Class.forName(Polyline.class.getName());
        else
        if (nexusGmlString.startsWith("MULTIPOLYGON"))        
          class_instance = Class.forName(Polygon.class.getName());
        
        if (class_instance!=null)
        {
          int i1, i2=0;
          
          for (;;)
          {
            Polyline shape = (Polyline)class_instance.newInstance();

            i1 = nexusGmlString.indexOf('(', i2);
            if (i1==-1)
            {
              if (i2==0)
                // exception
                throw new Exception("Error processing geometry: "+nexusGmlString);
              else
                break;
            }   
        
            // skip redundant '('
            while ( (i1<nexusGmlString.length()) && ((nexusGmlString.charAt(i1)=='(')||(nexusGmlString.charAt(i1)==' ')) )
              i1++;
            if (i1==nexusGmlString.length())
              break;
            
            // search for next matching ')'
            i2 = i1+1;
            while ( (i2<nexusGmlString.length()) && (nexusGmlString.charAt(i2)!=')') )
              i2++;
            if (i2==nexusGmlString.length())
              // exception
              throw new Exception("Error processing geometry: "+nexusGmlString);              

            String substr = nexusGmlString.substring(i1, i2).replace(',', '.');

            boolean bLat = false;             
            double lon = 0;
            java.util.StringTokenizer st = new java.util.StringTokenizer(substr);
            while (st.hasMoreTokens())
            {
              String t = st.nextToken();
              
              // remove trailing ','
              if (t.endsWith("."))
                t = t.substring(0, t.length()-1);
                
              double lat = Double.parseDouble(t);
              
              if (bLat)
              {
                Utm_Coord_3d utm = new Utm_Coord_3d();
                Gdc_To_Utm_Converter.Convert(new Gdc_Coord_3d(lat, lon, 0), utm);
                
                Point pc = new Point(utm.x, utm.y);
                shape.getPoints().add(pc);
                
                // update min and max values
                if (pc.getX()<min_x)
                  min_x = pc.getX();
                if (pc.getY()<min_y)
                  min_y = pc.getY();
                if (pc.getX()>max_x)
                  max_x = pc.getX();
                if (pc.getY()>max_y)
                  max_y = pc.getY();

                if (lat<min_lat)
                  min_lat = lat;
                if (lon<min_lon)
                  min_lon = lon;
                if (lat>max_lat)
                  max_lat = lat;
                if (lon>max_lon)
                  max_lon = lon;
              }
              else
                lon = lat;
                
              bLat ^= true;
            }
            
            res.add(shape);
          }
        }
        else
          throw new Exception("Error processing geometry: "+nexusGmlString);        
        
        return res;
    }

    /**
     * Processes the "Building" element. <br>
     * <br>
     * @param e element
     */
    protected void processBuilding(org.w3c.dom.Element e) throws Exception
    {
        String id = e.getAttribute("NOL");
        
        // process geometry        
        java.util.ArrayList shapes = null;
        org.w3c.dom.Element extent_node = (org.w3c.dom.Element)e.getElementsByTagName("extent").item(0);
        if (extent_node!=null)
        {
            org.w3c.dom.Node n = extent_node.getFirstChild();
            if (n!=null)
            {
                String s = n.getNodeValue();
                shapes = processGeometry(s);
            }
        }

        // create elements
        if ((shapes!=null)&&(shapes.size()>0))
        {
          for (int i=0; i<shapes.size(); i++)
          {
            SpatialModelElement element = new SpatialModelElement(id+'.'+i, "71", "10", (GeometryElement)shapes.get(i));
            elements.add(element);

          }
        }
        else
        {
          SpatialModelElement element = new SpatialModelElement(id, "71", "10", null);
          elements.add(element);          
        }
    }

    /**
     * Processes the "Road" element. <br>
     * <br>
     * @param e element
     */
    protected void processRoad(org.w3c.dom.Element e) throws Exception
    {
        String id = e.getAttribute("NOL");

        // process geometry
        java.util.ArrayList shapes = null;
        org.w3c.dom.Element roadRun_node = (org.w3c.dom.Element)e.getElementsByTagName("roadRun").item(0);
        if (roadRun_node!=null)
        {
            org.w3c.dom.Node n = roadRun_node.getFirstChild();
            if (n!=null)
            {
                String s = n.getNodeValue();
                shapes = processGeometry(s);
            }
        }

        // create elements
        java.util.ArrayList loc_elements = new java.util.ArrayList();
        if ((shapes!=null)&&(shapes.size()>0))
        {
          for (int i=0; i<shapes.size(); i++)
          {
            SpatialModelElement element = new SpatialModelElement(id+'.'+i, "41", "10", (GeometryElement)shapes.get(i));
            loc_elements.add(element);
          }
        }
        else
        {
          SpatialModelElement element = new SpatialModelElement(id, "41", "10", null);
          loc_elements.add(element);          
        }

        // process attributes

        // add "Official Name"
        org.w3c.dom.Element name_node = (org.w3c.dom.Element)e.getElementsByTagName("name").item(0);
        if (name_node!=null)
        {
            org.w3c.dom.Node n = name_node.getFirstChild();
            if (n!=null)
            {
                String s = n.getNodeValue();
                
                for (int i=0; i<loc_elements.size(); i++)
                  ((SpatialModelElement)loc_elements.get(i)).getAttributes().put("ON", s);
            }
        }
        
        // add localy created elements
        elements.addAll(loc_elements);
    }
    
    /**
     * Normalizes the coordinates of loaded elements
     */
    protected void normalizeElements()
    {
      java.util.Iterator iter = elements.iterator();
      while (iter.hasNext())
      {
        SpatialModelElement element = (SpatialModelElement)iter.next();
        
        // scale the element's geometry
        java.util.ArrayList points = ((Polyline)element.getGeometry()).getPoints();
        for (int i=0; i<points.size(); i++)
        {
          Point point = (Point)points.get(i);
          Point norm_point = new Point(point.getPosition().getX()-min_x, point.getPosition().getY()-min_y);          

          points.set(i, norm_point);
        }
      }
    }
    
    /**
     * Adds the loaded elements to Spatial Model
     */
    protected void addElements()
    {
      // add elements to Spatial Model
      java.util.Iterator iter = elements.iterator();
      while (iter.hasNext())
      {
        SpatialModelElement element = (SpatialModelElement)iter.next();
        
        // clip
        if (clipArea!=null)
        {
          clip(element);
          if (((Polyline)element.getGeometry()).getPoints().size()>0)
            spatialModel.getElements().put(element.getID(), element);
        }
        else
          spatialModel.getElements().put(element.getID(), element);
      }
    }
    
    /**
     * Loads AWML data from the source stream
     */
    protected void doLoad() throws Exception
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();

      org.w3c.dom.Document document = builder.parse(new java.io.FileInputStream(fileSource));

      org.w3c.dom.Element root=document.getDocumentElement();

      String rootTag=root.getNodeName();
      if (!rootTag.equals("awml"))
        throw new Exception("Invalid parent tag: "+rootTag);

      // process the nexus objects
      org.w3c.dom.NodeList obj_list = root.getElementsByTagName("nexusobject");
      for (int i=0; i<obj_list.getLength(); i++)
      {
        org.w3c.dom.Element object = (org.w3c.dom.Element)obj_list.item(i);
        String type = object.getAttribute("type");
        String kind = object.getAttribute("kind");

        if (kind.equalsIgnoreCase("real"))
        {
          if (type.equalsIgnoreCase("building"))
            processBuilding(object);
          else
          if (type.equalsIgnoreCase("road"))
            processRoad(object);
          else
            u.sendNotification(new LoaderNotification(this, u, "Unknown type of object: "+type));
        }
      }
    }

    /**
     * Clips the Element. <br>
     * <br>
     * @param elem element to clip
     */
    protected void clip(SpatialModelElement elem)
    {
      java.util.ArrayList points = ((Polyline)elem.getGeometry()).getPoints();
      java.util.ArrayList resPoints = new java.util.ArrayList();

      // iterate all points
      boolean flag = true;
      for (int i=0; i<points.size(); i++)
      {
        Point point = (Point)points.get(i);
        if (clipArea.contains(point))
        {
          if (flag)
            resPoints.add(point);
          else
          {
            // do clipping
            Line tempLine = new Line((Point)points.get(i-1), point);

            // add clipping point
            for (int j=0; j<clipArea.getPoints().size(); j++)
            {
              Line boundLine = new Line((Point)clipArea.getPoints().get(j),
                (Point)clipArea.getPoints().get((j+1)%clipArea.getPoints().size()));
              Point p = tempLine.intersect(boundLine);
              if (p!=null)
              {
                resPoints.add(p);
                break;
              }
            }

            // add point
            resPoints.add(point);
          }

          flag = true;
        }
        else
        {
          if ((flag)&&(i>0))
          {
            // do clipping
            Line tempLine = new Line((Point)points.get(i-1), point);

            // add clipping point
            for (int j=0; j<clipArea.getPoints().size(); j++)
            {
              Line boundLine = new Line((Point)clipArea.getPoints().get(j),
                (Point)clipArea.getPoints().get((j+1)%clipArea.getPoints().size()));
              Point p = tempLine.intersect(boundLine);
              if (p!=null)
              {
                resPoints.add(p);
                break;
              }
            }
          }

          flag = false;
        }
      }

      points.clear();
      points.addAll(resPoints);
    }

    /**
     * Initializes the object from XML tag. <br>
     * <br>
     * @param element source tag
     * @throws Exception Exception if parameters are invalid
     */
    public void load(org.w3c.dom.Element element) throws Exception
    {
      u.sendNotification(new LoaderNotification(this, u, "Loading AWMLReader extension"));

      super.load(element);

      spatialModel = (SpatialModel)u.getExtension("SpatialModel");

      String s;

      float min_x_clip = Float.NaN, max_x_clip = Float.NaN,
            min_y_clip = Float.NaN, max_y_clip = Float.NaN;

      s = element.getAttribute("min_x");
      if(s.length()>0)
        min_x_clip = Float.parseFloat(s);

      s = element.getAttribute("max_x");
      if(s.length()>0)
        max_x_clip = Float.parseFloat(s);

      s = element.getAttribute("min_y");
      if(s.length()>0)
        min_y_clip = Float.parseFloat(s);

      s = element.getAttribute("max_y");
      if(s.length()>0)
        max_y_clip = Float.parseFloat(s);

      if ( Float.isNaN(min_x_clip)&&Float.isNaN(max_x_clip)
         &&Float.isNaN(min_y_clip)&&Float.isNaN(max_y_clip) )
      {
      }
      else
      {
        if ( (Float.isNaN(min_x_clip)||Float.isNaN(max_x_clip)
              ||Float.isNaN(min_y_clip)||Float.isNaN(max_y_clip))
              ||((min_x_clip>=max_x_clip)||(min_y_clip>=max_y_clip)) )
            throw new Exception("Invalid clip region");

        clipArea = new Polygon();
        clipArea.getPoints().add(new Point(min_x_clip, min_y_clip));
        clipArea.getPoints().add(new Point(max_x_clip, min_y_clip));
        clipArea.getPoints().add(new Point(max_x_clip, max_y_clip));
        clipArea.getPoints().add(new Point(min_x_clip, max_y_clip));
      }

      s = element.getAttribute("source");
      if(s.length()==0)
        throw new Exception("Invalid source name: "+s);

      fileSource = s;

      // prepare the converter, use wgs'84 by default
      Gdc_To_Utm_Converter.Init();

      doLoad();
      normalizeElements();
      addElements();
      
      u.sendNotification(new LoaderNotification(this, u, "Processed area dimensions:"));
      u.sendNotification(new LoaderNotification(this, u, "Min_x="+min_x+" Max_x="+max_x+" Min_y="+min_y+" Max_y="+max_y));      
      u.sendNotification(new LoaderNotification(this, u, "Min_lat="+min_lat+" Max_lat="+max_lat+" Min_lon="+min_lon+" Max_lon="+max_lon));
    
      spatialModel.rebuildGraph();

      u.sendNotification(new LoaderNotification(this, u, "Finished loading AWMLReader extension"));
  }
}
