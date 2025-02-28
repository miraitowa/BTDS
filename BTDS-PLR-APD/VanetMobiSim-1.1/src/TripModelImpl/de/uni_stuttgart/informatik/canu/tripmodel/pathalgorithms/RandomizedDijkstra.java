package de.uni_stuttgart.informatik.canu.tripmodel.pathalgorithms;

/**
 * <p>Title: Trip Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Politenico di Torino</p>
 * @author Marco Fiore
 * @version 1.0
 */

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.Point;
import de.uni_stuttgart.informatik.canu.tripmodel.core.*;

/**
 * This class implements Randomized Dijkstra Shortest-Path Algorithm
 * @author Marco Fiore
 */
public class RandomizedDijkstra implements PathSearchingAlgorithm
{
  /**
   * Searches the shortest path between two vertices for the given mobile node,
   * avoiding deterministic selection in presence of multiple available paths. <br>
   * <br>
   * @param spatialModel Spatial Model
   * @param node node
   * @param ps source point
   * @param pd destination point
   * @param flag path-searching flags
   * @return path between vertices (array of points)
   */
  public Trip getPath(SpatialModel spatialModel, Node node, Point ps, Point pd, int flag)
  {
    Graph graph = spatialModel.getGraph();
    Vertex vs = graph.getClosestVertex(ps.getX(), ps.getY());
    Vertex vd = graph.getClosestVertex(pd.getX(), pd.getY());

    boolean known[] = new boolean[graph.getVertices().size()];
    double dv[] = new double[graph.getVertices().size()];
    int pv[] = new int[graph.getVertices().size()];

    // get random number generator
    Universe uni = Universe.getReference();
    if (uni == null) {
      System.out.println("Randomized Djikstra: no instance of universe");
      System.exit(-1);
    }		
    java.util.Random rand = uni.getRandom();

    // init buffer
    for (int i=0; i<graph.getVertices().size(); i++)
    {
      dv[i]=Double.MAX_VALUE;
      pv[i]=-1;
    }

    // currently selected vertex
    dv[vs.getInternalID()] = 0;

    for (;;)
    {
      // choose unknown vertex with the smallest cost
      double min_dv = Double.MAX_VALUE;
      Vertex v = null;
      // parse the buffer randomly
      boolean seen[] = new boolean[graph.getVertices().size()];
      for(int i=0; i<graph.getVertices().size(); i++)
        seen[i] = false;
      for(int j=0; j<graph.getVertices().size(); j++) {
        int sel = rand.nextInt(graph.getVertices().size()-j);
        int checked = 0;
        int k = 0;
        for(k=0; k<graph.getVertices().size(); k++) {
          if(!seen[k]) {
            if(checked == sel) break;
            checked++;
          }
        }
        seen[k] = true;
        if ( !known[k] && (dv[k]<=min_dv) )
        {
          // if two or more vertices have the same
          // smallest cost, pick one of them randomly
          if ( dv[k]==min_dv && rand.nextInt(2) == 0 )
              continue;
          min_dv = dv[k];
          v = (Vertex)graph.getVertices().get(k);
        }
      }

      /*
      boolean lastseen = false;
      int start = rand.nextInt(graph.getVertices().size());
      for (int i=start; lastseen==false; i = (i+1)%graph.getVertices().size()) {
        if ( !known[i] && (dv[i]<=min_dv) )
        {
          // if two or more vertices have the same
          // smallest cost, pick one of them randomly
          if ( dv[i]==min_dv && rand.nextInt(2) == 0 )
              continue;
          min_dv = dv[i];
          v = (Vertex)graph.getVertices().get(i);
        }
        if((start==0 && i==graph.getVertices().size()-1) || i==start-1)
          lastseen = true;
      }*/

      if (v==null)
        break;

      known[v.getInternalID()] = true;

      // update costs of adjacent vertices
      java.util.ArrayList vv = v.getNeighbours();
      for (int i=0; i<vv.size(); i++)
      {
        Vertex w = (Vertex)vv.get(i);

        // check if v-w edge is a one-way road
        if ( ((flag & FLAG_REFLECT_DIRECTIONS)==1) && spatialModel.isMovementProhibited(v, w) )
        {
          // skip it
          continue;
        }

        if (!known[w.getInternalID()])
        {
           double distance = Math.sqrt( ((v.getX() - w.getX())
                                        * (v.getX() - w.getX()))
                                       + ((v.getY() - w.getY())
                                        * (v.getY() - w.getY())) );

          // update path cost if necessary
          if (dv[v.getInternalID()]+distance<dv[w.getInternalID()])
          {
            dv[w.getInternalID()] = dv[v.getInternalID()]+distance;
            pv[w.getInternalID()] = v.getInternalID();
          }
        }
      }
    }

    // get path via reverse traversal
    java.util.ArrayList rev_path = new java.util.ArrayList();
    int i = vd.getInternalID();
    do
    {
      rev_path.add(new Integer(i));
      i = pv[i];
    }
    while (i!=-1);

    // check if the path doesn't exist
    if (rev_path.size()==1)
      return null;

    // result trip
    Trip trip = new Trip();
    java.util.ArrayList trip_points = trip.getPath();
    
    // add ps-vs if necessary
    Vertex ps_v = graph.getVertex(ps.getX(), ps.getY());
    if (ps_v!=vs)
      trip_points.add(ps);
    
    // reverse the path & construct the trip
    for (i=rev_path.size()-1; i>=0; i--)
    {
      int v_i = ((Integer)rev_path.get(i)).intValue();
      Vertex v = (Vertex)graph.getVertices().get(v_i);
      
      Point p = new Point(v.getX(), v.getY());
      if ( (trip_points.size()==0) ||
           (!p.equals((Point)trip_points.get(trip_points.size()-1))) )
        trip_points.add(p);
    }

    // add vd-pd if necessary
    Vertex pd_v = graph.getVertex(pd.getX(), pd.getY());
    if (pd_v!=vd)
      trip_points.add(pd);

    return trip;
  }
}
