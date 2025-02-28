/*
 * Edge.java
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

import de.uni_stuttgart.informatik.canu.senv.core.Vertex;

/**
 * Represents an edge in the graph. An edge is defined through the two vertices
 * it connects.
 *
 * @author      Mario Hegele
 * @author      Illya Stepanov
 */
public class Edge
{
    
	 /**
     * the ID of the Edge
     */
    private String id = null;
	
		/**
     * the first vertex this edge connects
     */
    private Vertex v1 = null;

    /**
     * the second vertex this edge connects
     */
    private Vertex v2 = null;

    /**
     * the ID of the first vertex
     */
    private String id1 = null;

    /**
     * the ID of the second vertex
     */
    private String id2 = null;

    /**
     * the edge's weight
     */
    private double weight = 0.0;

    /**
     * the internally used ID of the edge (position in the EdgeVector object
     * of the infrastructure class)
     */
    private int internal_id = -1;

    /**
     * Creates a new edge with a connection between two vertices. <br>
     * The vertex IDs are ordered ascendantly. <br>
     * The IDs must be different and of type int.
     *
     * @param v1_id the first vertex ID
     * @param v2_id the second vertex ID
     */
    public Edge(String v1_id, String v2_id)
    {
      id1 = v1_id;
      id2 = v2_id;
    }
		
		/**
     * Sets the edge's ID
     * @param id the ID of the edge
     */
    public void setID(String id)
    {
      this.id=id;
    }
		/**
     * Gets the edge's ID
     * @return the ID of the edge
     */
    public String getID()
    {
      return id1;
    }
		
    /**
     *
     * @return the first ID of the vertex
     */
    public String getID1()
    {
      return id1;
    }

    /**
     *
     * @return the second ID of the vertex
     */
    public String getID2()
    {
      return id2;
    }

    /**
     * Returns the first of the vertices this edge connects
     *
     * @return the first vertex
     */
    public Vertex getV1()
    {
      return v1;
    }

    /**
     * Returns the second of the vertices this edge connects
     *
     * @return the second vertex
     */
    public Vertex getV2()
    {
      return v2;
    }

    /**
     * Sets the first vertex which the edge connects to the second vertex
     *
     * @param v the first vertex
     */
    public void setV1(Vertex v)
    {
      v1 = v;
    }

    /**
     * Sets the second vertex which the edge connects to the first vertex
     *
     * @param v the second vertex
     */
    public void setV2(Vertex v)
    {
      v2 = v;
    }

    /**
     * Sets the internal ID
     *
     * @param i the internal ID
     */
    public void setInternalID(int i)
    {
      internal_id = i;
    }

    /**
     * Gets the internal ID
     *
     * @return the second vertex
     */
    public int getInternalID()
    {
      return internal_id;
    }

    /**
     * @return returns the distance between the two connected vertices
     */
    public double getDistance()
    {
      return Math.sqrt( ((v1.getX() - v2.getX()) 
                       * (v1.getX() - v2.getX()))
                      + ((v1.getY() - v2.getY()) 
                       * (v1.getY() - v2.getY())) );
    }

    /**
     * Sets the edge's weight
     *
     * @param weight the edge's weight
     */
    public void setWeight(double weight)
    {
      this.weight = weight;
    }

    /**
     * Gets the edge's weight
     *
     * @return the edge's weight
     */
    public double getWeight()
    {
      return weight;
    }
   
    /**
     * Indicates whether this edge is equal to some other edge. <br>
     * <br>
     * @param o edge to compare with
     * @return true, if the edges are the same
     */
     public boolean equals(Object o)
     {
       if (o instanceof Edge)
       {
         Edge e = (Edge)o;
         if ( id1.equals(e.id1) && id2.equals(e.id2) && (weight==e.weight) )
           return true;
       }
       return false;
     }
}
