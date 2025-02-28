/*
 * Path.java
 *
 * Date: 06.09.2000
 * Last Change: 06.09.2000
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

package de.uni_stuttgart.informatik.canu.senv.utils;

/**
 * This class represents a path in a graph. It consists of an array of
 * vertex IDs (int) and an array of distances (double) between these
 * vertices. The order of the vertex IDs in the array is the order
 * in which they form the path.
 * <br>
 * The distance between vertex_i and vertex_i+1 can be found
 * in the distance array at position i.
 * <br>
 * By this class' definition, a path consists of at least an initial and a
 * final vertex which can be the same. The number of distances is
 * the number of vertices minus one (A path with just one vertex
 * has length 0).
 * Alternatively, a path can be "empty" (dummy path)
 *
 */



/*
 * zu ueberarbeiten...welche schnittstellen werden ueberhaupt gebraucht??
 */



public class Path {
    /**
     * the vertices which form a path
     */
    private int[] vertex = null;

    /**
     * the distance between the vertices
     */ 
    private double[] distance = null;

    /**
     * the length of the path
     */
    private double length = 0.0;

    /**
     * the ID of the edge, if the path consists only of one edge
     */
    private int edgeID = -1;

    /*
    public Path() {
	vertex = null;
	distance = null;
	length = 0.0;
	}*/

    public Path(int[] vertices, double[] distances) {
	setPath(vertices, distances);
    }

    public Path(int firstVertex, int secondVertex, double dist) {
	this(firstVertex, secondVertex, dist, -1);
    }

    /**
     * Constructor for paths which consist of an edge
     */
    public Path(int firstVertex, int secondVertex, double dist,
		int ID) {
	vertex = new int[2];
	vertex[0] = firstVertex;
	vertex[1] = secondVertex;
	
	distance = new double[1];
	distance[0] = dist;

	length = distance[0];

	edgeID = ID;
    }

    public Path(int[] vertices, double[] distances, double l) {
	vertex = vertices;
	distance = distances;
	length = l;
	edgeID = -1;
    }

    public int[] getVertices() {
	return vertex;
    }

    public double getPathLength() {
	return length;
    }

    public int getEdgeID() {
	return edgeID;
    }

    /**
     * Sets a path.
     *
     * @return returns "true", if the path is by this class 'definition 
     * a valid path, "false" otherwise
     */
    public boolean setPath(int[] vertices, double[] distances) {
	vertex = vertices;
	distance = distances;

	length = 0.0;
	for(int i = 0; i < distance.length; i++) {
	    length = length + distance[i];
	}
	
	if ((vertex == null)  || (distance == null)
	    || (vertex.length != distance.length-1))
	    return false;
	else
	    return true;
    }

    /**
     * Connects to path, where the end vertex of the first path should be
     * the initial vertex of the second path
     *
     * @param p1 first path
     * @param p2 second path
     */
    public static Path connectPaths(Path p1, Path p2) {
	int[] newVertex = new int[p1.vertex.length + p2.vertex.length - 1];
	double[] newDistance = new double[p1.distance.length + 
					 p2.distance.length];
	
	System.arraycopy(p1.vertex, 0,
			 newVertex, 0,
			 p1.vertex.length);
	System.arraycopy(p2.vertex, 1,
			 newVertex, p1.vertex.length,
			 p2.vertex.length-1);
	System.arraycopy(p1.distance, 0,
			 newDistance, 0,
			 p1.distance.length);
	System.arraycopy(p2.distance, 0,
			 newDistance, p1.distance.length,
			 p2.distance.length);
	return new Path(newVertex, newDistance, p1.length + p2.length);
    }

    /**
     * Determines all paths of a special length with at least 
     * two vertices which are contended in this path.
     * E.g. Path (1,2,3) and length 2 would return (1,2),(2,3)
     *
     * @param length the length of the partial paths
     */
    public Path[] getPartialPaths(int length) {
	Path[] p;
	int[] vert;
	double[] dist;

	if (length < 1)
	    return new Path[0];
	if (length >= vertex.length) {
	    p = new Path[1];
	    p[0] = new Path(vertex, distance);
	    return p;
	}

	p = new Path[vertex.length-length+1];
	for (int i = 0; i < p.length; i++) {
	    vert = new int[length];
	    dist = new double[length-1];

	    System.arraycopy(vertex,
			     i,
			     vert,
			     0,
			     length);
	    System.arraycopy(distance,
			     i,
			     dist,
			     0,
			     length-1);
	    p[i] = new Path(vert, dist);
	}
	return p;
    }

}
