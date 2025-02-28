package de.uni_stuttgart.informatik.canu.mobisim.extensions;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.senv.utils.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * <p>Patches: </p>
 * <p> v1.2 (22/08/2005):	Since a SpatialModel also implements a Graph object,
 *			  									we forbid a Graph extension to be loaded when
 *        									a SpatialModel is already loaded.</p>
 * @author Canu Research group 
 * @author v1.2: Jerome Haerri (haerri@ieee.org)
 * @version 1.2
 */

/**
 * This module contains an implementation of spatial environment graph
 * <p>Patches: </p>
 * <p> <i>Version 1.2 by Jerome Haerri (haerri@ieee.org) on 08/22/2005:
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Since a SpatialModel also implements a Graph object,
 *			  									we forbid a Graph extension to be loaded when
 *        									a SpatialModel is already loaded.</i></p>
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.0-1.1 Mario Hegele
 * @author 1.2 Jerome Haerri
 * @version 1.2
 */
public class Graph extends ExtensionModule
{
  /**
   * Aggregated graph object
   */
  protected InfrastructureGraph graph;

  /**
   * Constructor
   */
  public Graph()
  {
    graph = new InfrastructureGraph();
  }

  /**
   * Constructor. <br>
   * <br>
   * @param name unique graph's name
   */
  public Graph(String name)
  {
    super(name);
    
    graph = new InfrastructureGraph();
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
   * Returns module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Graph extension module";
  }

  /**
   * Gets the aggregated graph. <br>
   * <br>
   * @return aggregated graph
   */
  public InfrastructureGraph getInfrastructureGraph()
  {
    return graph;
  }

  /**
   * Adds a vertex to the graph.
   *
   * @param id identifier
   * @param name name of the represented location
   * @param x the x coordinate, should be of type double
   * @param y the y coordinate, should be of type double
   * @return vertex reference
   * @throws Exception thrown if the vertex already exists in
   * this infrastructure graph or parameters were not
   * acceptable for a new vertex
   */
  public Vertex addVertex(String id, String name, String x, String y) throws Exception
  {
    return graph.addVertex(id, name, x, y);
  }

  /**
   * Adds an edge to the infrastructure graph. <br>
   * Comment: the vertices which this edge claims to connect
   * need not exist so long.
   *
   * @param v1_id the ID of the first vertex
   * @param v2_id the ID of the second vertex
   * @return edge reference 
   */
  public Edge addEdge(String v1_id, String v2_id)
    throws Exception
  {
    return graph.addEdge(v1_id, v2_id);
  }

  /**
   * Gets the vertices belonging to the graph. <br>
   * <br>
   * @return vertices belonging to the graph
   */
  public java.util.ArrayList getVertices()
  {
    return graph.getVertices();
  }

  /**
   * Gets a vertex with the specified coordinates. <br>
   * <br>
   * @param x x-coordinate
   * @param y y-coordinate
   * @return vertex with the specified coordinates
   */
  public Vertex getVertex(double x, double y)
  {
    java.util.Iterator iter = graph.getVertices().iterator();
    while (iter.hasNext())
    {
      Vertex v = (Vertex)iter.next();
      if ((v.getX()==x)&&(v.getY()==y))
        return v;
    }

    return null;
  }

  /**
   * Gets the closest vertex to the specified point. <br>
   * <br>
   * @param x x-coordinate
   * @param y y-coordinate
   * @return vertex closest to the specified point
   */
  public Vertex getClosestVertex(double x, double y)
  {
    double d_min = Double.MAX_VALUE;
    Vertex v_res = null;
    
    java.util.Iterator iter = graph.getVertices().iterator();
    while (iter.hasNext())
    {
      Vertex v = (Vertex)iter.next();
      double d = Math.sqrt((v.getX()-x)*(v.getX()-x)+(v.getY()-y)*(v.getY()-y));
      if (d<d_min)
      {
        d_min = d;
        v_res = v;
      }
    }

    return v_res;
  }

  /**
   * Gets edges belonging to the graph. <br>
   * <br>
   * @return edges belonging to the graph
   */
  public java.util.ArrayList getEdges()
  {
    return graph.getEdges();
  }

  /**
   * Gets a coordinate of the leftmost vertex. <br>
   * <br>
   * @return position of the leftmost vertex
   */
  public double getLeftmostCoordinate()
  {
    return graph.getLeftmostCoordinate();
  }

  /**
   * Gets a coordinate of the rightmost vertex. <br>
   * <br>
   * @return position of the rightmost vertex
   */
  public double getRightmostCoordinate()
  {
    return graph.getRightmostCoordinate();
  }

  /**
   * Gets a coordinate of the uppermost vertex. <br>
   * <br>
   * @return position of the uppermost vertex
   */
  public double getUppermostCoordinate()
  {
    return graph.getUppermostCoordinate();
  }

  /**
   * Gets a coordinate of the leftmost vertex. <br>
   * <br>
   * @return position of the leftmost vertex
   */
  public double getLowermostCoordinate()
  {
    return graph.getLowermostCoordinate();
  }

  /**
   * Gets a matrix with the shortest paths between all the pairs of vertices. <br>
   * <br>
   * @return matrix with the shortest paths between all the pairs
   *         of vertices. (The matrix should have been calculated before).
   */
  public Path[][] getPathMatrix()
  {
    return graph.getPathMatrix();
  }

 /**
    * Initializes the graph from XML tag. <br>
    * <br>
		* <i>Version 1.2 by Jerome Haerri (haerri@ieee.org): 
		* <br>
		* &nbsp;&nbsp;&nbsp;&nbsp;A Spatial model, if defined, can only be loaded after the graph model. This is done in order to be sure that,
		* when the SpatialModel extension is loaded and a Graph model found, the spatial model is able to load the graph. </i>
		* <br>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading Graph extension"));
		
		SpatialModel model = (SpatialModel)u.getExtension("SpatialModel");
		if (model != null)
			throw new Exception("A Spatial model can only be loaded after the Graph model");
		
    super.load(element);

    // get coefficient
    double k=1.0;
    String kTag = element.getAttribute("k");
    if (kTag.length()>0)
    {
      k=Double.parseDouble(kTag);
    }

    if(k==0.0)
      throw new Exception("Invalid coefficient: "+k);

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
      if(tag.equals("vertex"))
      {
				System.err.println("Deprecated definition of a Graph. Please use UserGraph instead");
         u.sendNotification(new LoaderNotification(this, u,
           "Processing <vertex> tag"));

         org.w3c.dom.Element e=(org.w3c.dom.Element)item;
         org.w3c.dom.Node n;

         n=e.getElementsByTagName("id").item(0);
         if(n==null)
           throw new Exception("Vertex <id> is missing!");
         String id=n.getFirstChild().getNodeValue();

         n=e.getElementsByTagName("name").item(0);
         String name;
         if(n==null)
           name="";
          else
           name=n.getFirstChild().getNodeValue();

         n=e.getElementsByTagName("x").item(0);
         if(n==null)
           throw new Exception("Vertex <x> is missing!");
         String x=n.getFirstChild().getNodeValue();

         n=e.getElementsByTagName("y").item(0);
         if(n==null)
           throw new Exception("Vertex <y> is missing!");
         String y=n.getFirstChild().getNodeValue();

         double d_x=Double.parseDouble(x)*k;
         double d_y=Double.parseDouble(y)*k;

         graph.addVertex(id, name, Double.toString(d_x), Double.toString(d_y));

         u.sendNotification(new LoaderNotification(this, u,
           "Finished processing <vertex> tag"));
      }
      else
      if(tag.equals("edge"))
      {
         System.err.println("Deprecated definition of a Graph. Please use UserGraph instead");
				 u.sendNotification(new LoaderNotification(this, u,
           "Processing <edge> tag"));

         org.w3c.dom.Element e=(org.w3c.dom.Element)item;
         org.w3c.dom.Node n;

         n=e.getElementsByTagName("v1").item(0);
         if(n==null)
           throw new Exception("Edge <v1> is missing!");
         String v1=n.getFirstChild().getNodeValue();

         n=e.getElementsByTagName("v2").item(0);
         if(n==null)
           throw new Exception("Edge <v2> is missing!");
         String v2=n.getFirstChild().getNodeValue();

         graph.addEdge(v1, v2);

         u.sendNotification(new LoaderNotification(this, u,
           "Finished processing <edge> tag"));
      }
    }

    graph.reorganize(false);
    graph.calculateShortestPaths();

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading Graph extension"));
  }
}