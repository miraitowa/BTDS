package eurecom.spacegraph.graphalgorithm;

/**
 * <p>Title: Fortune Algorithm</p>
 * <p>Description: Creates a Voronoi Tesselation according to the Fortune's Algorithm </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */

public interface Voronoi {
   public void drawVoronoi() throws Exception;
	 public void drawVoronoi(java.util.ArrayList obstacles, java.util.ArrayList edges);
}

