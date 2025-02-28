/*
 * Vertex.java
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

/**
 * This class represents a physical location in the infrastructure graph.
 *
 * @author      Mario Hegele
 * @author      Illya Stepanov
 */
public class Vertex extends Object
{
    /**
     * the ID of this vertex
     */
    private String id = null;

    /**
     * the internal ID for this vertex (its position in the vertex vector)
     */
    private int internal_id = -1;

    /**
     * the (optional) name of the represented location
     */
    private String name = null;

    /**
     * the x coordinate of this vertex
     */
    private double x = 0.0;

    /**
     * the y coordinate of this vertex
     */
    private double y = 0.0;

    /**
     * the neighboured vertices of this vertex (i.e. they are connected to this
     * vertex by an edge)
     */
    private java.util.ArrayList neighbours = null;

    /**
     * Creates a new vertex.
     *
     * @param id identifier
     * @param name name of the represented location
     * @param x the x coordinate, should be of type double
     * @param y the y coordinate, should be of type double
     * @throws Exception Exception is thrown if the
     * values of the parameters which are handed over are not legal
     */
    public Vertex(String id, String name, String x, String y) throws Exception
    {
      this.id = id;
      this.name = (name == null ? new String() : name);
      this.x = Double.parseDouble( x );
      this.y = Double.parseDouble( y );
      this.neighbours = new java.util.ArrayList();
    }

    /**
     * @return returns the id of this vertex
     */
    public String getID()
    {
      return id;
    }

    /**
     * @return returns the name of this vertex
     */
    public String getName()
    {
      return name;
    }

    /**
     * @return returns the x coordinate of this vertex
     */
    public double getX()
    {
      return x;
    }

    /**
     * @return returns the y coordinate of this vertex
     */
    public double getY()
    {
      return y;
    }

    /**
     * The internal ID indicates the position of the vertex in the 
     * vertex vector, if set properly. It ranges from 0 to size-1.
     * @return returns the internally used ID
     */
    public int getInternalID()
    {
      return internal_id;
    }

    /**
     * Sets the internally used ID
     * @param id the internal ID
     */
    public void setInternalID(int id)
    {
      internal_id = id;
    }

    /**
     * @return returns a list of all direct neighboring vertices
     */
    public java.util.ArrayList getNeighbours()
    {
      return neighbours;
    }

    /**
     * Adds a neighbour of this vertex (i.e. there exists an edge which
     * connects this vertex with another vertex)
     * If the neighbour vertex is already a neighbour vertex, nothing happens
     *
     * @param v the neighbour
     */
    public void addNeighbour(Vertex v)
    {
      if (neighbours.indexOf(v)==-1)
        neighbours.add(v);
    }
    
    /**
     * Indicates whether this vertex is equal to some other vertex. <br>
     * <br>
     * @param o vertex to compare with
     * @return true, if the vertices have the same ID
     *
     */
     public boolean equals(Object o)
     {
       if (o instanceof Vertex)
       {
         Vertex v = (Vertex)o;
         if (id.equals(v.id))
           return true;
       }
       return false;
     }
}
