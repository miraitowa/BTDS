package eurecom.spacegraph;

/**
 * <p>Title: Cluster Object</p>
 * <p>Description: A cluster represents a gemoetrical sub-division of the mouvement area. Each cluster has its own urban caracteristics </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Institut Eurecom, Polito</p>
 * @author Jerome Haerri, Marco Fiore
 * @version 1.0
 */

import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;



/**
 * This class represents a cluster of obstacles object  <br>
 * <br>
 * Each cluster has its own urban caracteristics
 *
 * @author Jerome Haerri, Marco Fiore
 */
public class Cluster {
 
	protected String name;
	protected double density;
	protected double xDim;
	protected double yDim;
	protected Point startPoint;
	
	/**
   * Constructor. <br>
   * <br>
   * @param name Name of the type of cluster
	 * @param density Density of obstacles within this cluster
	 * @param xDim The X dimention of this cluster
	 * @param yDim The Y dimention of this cluster
   */
	public Cluster(String name, double density, double xDim, double yDim) {
	  this.name = name;
		this.density = density;
		this.xDim = xDim;
		this.yDim = yDim;
	}
	
	/**
   * Constructor. <br>
   * <br>
   * @param name Name of the type of cluster
	 * @param density Density of obstacles within this cluster
   */
	public Cluster(String name, double density) {
	  this.name = name;
		this.density = density;
		this.xDim = 0;
		this.yDim = 0;
	}
	
	/**
   * Method to set the start coodinates of this cluster <br>
   * <br>
	 * @param X The X coordiate of the StartPoint
	 * @param Y The Y coordiate of the StartPoint
   */
	protected void setStartPoints(double X, double Y) {
	  startPoint = new Point(X,Y);
	}
	
	/**
   * Method to set the dimensions of this cluster <br>
   * <br>
	 * @param xDim The X dimention of this cluster
	 * @param yDim The Y dimention of this cluster
   */
	protected void setDimPoints(double xDim, double yDim) {
	  this.xDim = xDim;
		this.yDim = yDim;
	}
	
	protected double getX() {
	  return xDim;
	}
	protected double getY() {
	  return yDim;
	}
	protected String getName() {
	  return name;
	}
	protected double getDensity() {
	  return density;
	}
	protected double getStartX() {
	  return startPoint.getX();
	}
	protected double getStartY() {
	  return startPoint.getY();
	}
}

