/*
 * InfrastructureGraph.java
 *
 * Date: 07.08.2000
 * Last Change: 28.09.2000
 * Author: Mario Hegele (hegelemo@rupert.informatik.uni-stuttgart.de)
 * 
 * This software is part of a student research project ("Studienarbeit") 
 * at the University of Stuttgart, Institute of Parallel and 
 * Distributed High-Performance Systems (IPVR)
 *
 * Subject: "Entwicklung einer Simulationsumgebung fuer 
 *           kontextbezogenen Informationszugriff in einer mobilen Umgebung"
 *          ("Development of a simulation framework for mobile 
 *            information access")
 *
 * Examiner: Prof. Kurt Rothermel
 * Tutor: Uwe Kubach
 * Processing period: 19.06.00-17.12.00 
 */

package de.uni_stuttgart.informatik.canu.senv.core;

import de.uni_stuttgart.informatik.canu.senv.utils.Path;

/**
 * Represents an infrastructure graph with vertices and edges.
 *
 * @author      Mario Hegele
 * @author      Illya Stepanov
 */
public class InfrastructureGraph
{
    /**
     * contains all the vertices of the infrastructure graph
     */
    private java.util.ArrayList vertices = null;

    /**
     * contains all the vertices of the infrastructure graph
     */
    private java.util.ArrayList edges = null;

    /**
     * a short description of this graph (e.g. the represented area)
     */
    private String description = null;

    /**
     * the leftmost x coordinate of the graph
     */
    private double minX = Double.MAX_VALUE;

    /**
     * the rightmost x coordinate of the graph
     */
    private double maxX = Double.MIN_VALUE;
    
    /**
     * the lowermost y coordinate of the graph
     */
    private double minY = Double.MAX_VALUE;
    
    /**
     * the uppermost y coordinate of the graph
     */
    private double maxY = Double.MIN_VALUE;

    /**
     * this matrix contains the shortest paths between all pairs of vertices
     */
    private Path[][] pathMatrix = null;

    /**
     * Constructor for the InfrastructureGraph class. For setting the
     * vertices, edges, ... see the supported methods
     * @see #addVertex(String id, String name, String x, String y)
     * @see #addEdge(String v1_id, String v2_id) 
     * @see #setDescription(String d)
     */
    public InfrastructureGraph()
    {
      vertices = new java.util.ArrayList();
      edges = new java.util.ArrayList();
      description = new String();
      pathMatrix = null;
    }

    /**
     * Checks if the id of a vertex is already contained in the 
     * infrastructure graph
     *
     * @param v the vertex
     * @return returns true, if the ID of this Vertex already exists
     */
    private boolean vertexExists(Vertex v)
    {
      return vertices.indexOf(v)!=-1;    
    }

    /**
     * Searches in the infrastructure graph for a special vertex
     *
     * @param id the ID of the searched vertex
     * @return returns the searched vertex or null if there exists no 
     * vertex with this ID
     */
    private Vertex getVertex(String id)
    {
      java.util.Iterator iter = vertices.iterator();
      while (iter.hasNext())
      {
        Vertex vertex = (Vertex)iter.next();
        if (vertex.getID().equals(id))
          return vertex;
      } 
      return null;
    }

    /**
     * Checks if the infrastructure graph already contains an edge
     * with the same two vertex IDs. (order not considered)
     *
     * @param v1_id ID of the first vertex
     * @param v2_id ID of the second vertex
     * @return returns true, if there already exists an edge between
     * the two vertices
     */
    private boolean edgeExists(Edge e)
    {
      return edges.indexOf(e)!=-1;      
    }

    /**
     * Set the description of the infrastructure graph
     * @param d the description
     */
    public void setDescription(String d)
    {
      description = d;
    }

    /**
     * Adds a vertex to this infrastructure graph.
     *
     * @param id identifier, should be of type int
     * @param name name of the represented location
     * @param x the x coordinate, should be of type double
     * @param y the y coordinate, should be of type double
     * @return created Vertex object 
     * @throws Exception thrown if the vertex already exists in 
     * this infrastructure graph or parameters were not
     * acceptable for a new vertex
     * @see de.uni_stuttgart.informatik.canu.senv.core.Vertex#Vertex(String, String, String, String)
     */
    public Vertex addVertex(String id, String name, String x, String y) throws Exception
    {
      Vertex vertex = new Vertex(id, name, x, y);
//      if (!vertexExists(vertex))
      {
        vertices.add(vertex);
        vertex.setInternalID(vertices.size()-1);
        if (vertex.getX() < minX) minX = vertex.getX();
        if (vertex.getX() > maxX) maxX = vertex.getX();
        if (vertex.getY() < minY) minY = vertex.getY();
        if (vertex.getY() > maxY) maxY = vertex.getY();
        pathMatrix = null;
      }
//      else
//        throw new Exception("The vertex with ID " + id + " already exists in this graph!");
      return vertex;
    }

    /**
     * Adds an edge to this infrastructure graph. <br>
     * Comment: the vertices which this edge claims to connect
     * need not exist so long.
     * 
     * @param v1_id the ID of the first vertex
     * @param v2_id the ID of the second vertex
     * @return created Edge object
     * @throws Exception thrown if the edge already exists in 
     * this infrastructure graph
     * @see de.uni_stuttgart.informatik.canu.senv.core.Edge#Edge(String, String)
     */
    public Edge addEdge(String v1_id, String v2_id)	throws Exception
    {
      Edge edge = new Edge(v1_id, v2_id);
//      if (!edgeExists(edge))
      {
        edges.add(edge);
        pathMatrix = null;
      }
//      else
//        throw new Exception("The edge between " + v1_id + " and " + v2_id + " already exists in this graph!");
      return edge;
    }

    /**
     * When the construction of the graph is finished, it should be
     * "reorganized":
     * <br>
     * It is checked, if the vertices which an edge claims to connect exist.
     * If they do not exist, the edges are deleted. 
     * <br>
     * It is checked, which neighbour vertices a vertex has. Then it is
     * associated directly to them.
     */
    public void reorganize(boolean debug)
    {
      Vertex v1 = null;
      Vertex v2 = null;

      pathMatrix = null;

      if (debug)
        System.err.println("Reorganizing graph");

      // internal ID of the edges
      int intId = 0;

      // check the existence of the vertices
      java.util.Iterator iter = edges.listIterator();
      while (iter.hasNext())
      {
        Edge edge = (Edge)iter.next();
        v1 = getVertex(edge.getID1());
        v2 = getVertex(edge.getID2());
        if ((v1 != null) && (v2 != null))
        {
          //both vertices exist
          edge.setV1(v1);
          edge.setV2(v2);
          edge.setInternalID(intId);
          intId++;

          //associate every vertex to its neighbours
          v1.addNeighbour(v2);
          v2.addNeighbour(v1);
        }
        else
        {
          //one or both vertices do not exist
          iter.remove();

          if (debug) 
            System.err.println("The edge which claims to connect"
                             + " vertex " + edge.getID1() 
                             + " and vertex " + edge.getID2()
                             + " has been removed, because one"
                             + " or both vertices do not exist");
        }
      }
    }

    /**
     * @return returns all the vertices which belong to this 
     *         infrastructure graph
     */
    public java.util.ArrayList getVertices()
    {
      return vertices;
    }

    /**
     * @return returns all the edges which belong to this
     *         infrastructure graph
     */
    public java.util.ArrayList getEdges()
    {
      return edges;
    }

    /**
     * @return returns the description of this infrastructure graph
     */
    public String getDescription()
    {
      return description;
    }

    /**
     * @return returns the position of leftmost vertex
     */
    public double getLeftmostCoordinate()
    {
      return minX;
    }

    /**
     * @return returns the position of the rightmost vertex
     */
    public double getRightmostCoordinate()
    {
      return maxX;
    }

    /**
     * @return returns the position of the uppermost vertex
     */
    public double getUppermostCoordinate()
    {
      return maxY;
    }

    /**
     * @return returns the position of the leftmost vertex
     */
    public double getLowermostCoordinate()
    {
      return minY;
    }

    /**
     * @return returns the number of vertices this graph contains
     */
    public int getNoOfVertices()
    {
      return vertices.size();
    }

    /**
     * @return returns the number of edges
     */
    public int getNoOfEdges()
    {
      return edges.size();
    }

    /**
     * Checks if this infrastructure graph is connected.
     *
     * @return returns true if it is, otherwise false
     */
    public boolean isGraphConnected()
    {
      /*
       * Algorithm: take a random vertex and add it to the vertex set.
       * Then add the vertex' neighbours to the vertex set.
       * While there is a vertex which is not in the vertex set,
       * but which is a neighbour of a vertex of the vertex set,
       * add it to the vertex set.
       * The graph is connected, if all vertices are in the vertex set.
       */

      int numberOfVertices = vertices.size();

      if (numberOfVertices > 0)
      {
        Vertex[] connectedVertices = new Vertex[numberOfVertices];
	    
        for (int i = 0; i < connectedVertices.length; i++)
          connectedVertices[i] = null;

	    connectedVertices[0] = (Vertex)vertices.get(0);

        //how many vertices have been addes to the graph set so far?
        int vertexCounter = 1;

        //which vertex is checked for neighbours 
        int vertexPos = 0;

        while ((vertexCounter < numberOfVertices)
            && (vertexPos < numberOfVertices) 
            && (connectedVertices[vertexPos] != null))
        {
          java.util.ArrayList neighbours = connectedVertices[vertexPos].getNeighbours();

          //add the neighbours of the vertex to the set of vertices
          //which are connected 
          for (int i = 0; i < neighbours.size(); i++)
          {
            Vertex vertex = (Vertex)neighbours.get(i);
		    
            for (int j = 0; j < vertexCounter; j++)
            {
              //vertex is already in the set of connected vertices
              if (vertex == connectedVertices[j])
              {
                vertex = null;
                break;
              }
            }
            if (vertex != null)
            {
              vertexCounter++;
              if (vertexCounter <= connectedVertices.length)
              {
                connectedVertices[vertexCounter-1] = vertex;
              }
            }
          }
          vertexPos++;
        }

        if (vertexCounter >= numberOfVertices)
          return true;
        else 
          return false;
      }
      else
        return false;
    }

    /**
     * Calculates "All-Pairs Shortest Path" after Floyd-Warshall Algorithm. 
     * <br>
     * O(n^3), but very handy
     *
     * @return returns a matrix with all pairs of shortest paths
     */
    public Path[][] calculateShortestPaths()
    {
      int numberOfVertices = vertices.size();
      Object[] graphEdges = edges.toArray();
      Edge e = null;
      int id1, id2;
      pathMatrix = new Path[numberOfVertices][numberOfVertices];

      // initialize path matrix 
      for (int i = 0; i < numberOfVertices; i++) 
        for (int j = 0; j < numberOfVertices; j++) 
          if (i == j) 
            pathMatrix[i][i] = new Path(i, i, 0);
          else 
             pathMatrix[i][j] = new Path(i, j, Double.MAX_VALUE);
	
      //enter all existing edges into the path matrix
      for (int i = 0; i < graphEdges.length; i++)
      {
        e = (Edge)graphEdges[i];
        id1 = e.getV1().getInternalID();
        id2 = e.getV2().getInternalID();
        pathMatrix[id1][id2] = new Path(id1, id2, e.getDistance(), e.getInternalID());
        pathMatrix[id2][id1] = new Path(id2, id1, e.getDistance(), e.getInternalID());
      }

      //Floyd-Warshall (All-Pairs Shortest Path)
      for (int k = 0; k < numberOfVertices; k++)
        for (int i = 0; i < numberOfVertices; i++)
          for (int j = i + 1; j < numberOfVertices; j++)
          {
            if ((pathMatrix[i][k].getPathLength() + pathMatrix[k][j].getPathLength())
               < pathMatrix[i][j].getPathLength())
            {
              pathMatrix[i][j] = Path.connectPaths(pathMatrix[i][k],
              pathMatrix[k][j]);
              pathMatrix[j][i] = Path.connectPaths(pathMatrix[j][k], pathMatrix[k][i]);
            }
          }

      return pathMatrix;
    }

    /**
     * @return Returns a matrix with the shortest paths between all pairs 
     *         of vertices. (The matrix should have been calculated before).
     * @see #calculateShortestPaths()
     */
    public Path[][] getPathMatrix()
    {
      return pathMatrix;
    }
}
