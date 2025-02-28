package de.uni_stuttgart.informatik.canu.tripmodel.pathalgorithms;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels.*;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.Point;
import de.uni_stuttgart.informatik.canu.tripmodel.core.*;
import de.uni_stuttgart.informatik.canu.tripmodel.pathalgorithms.Dijkstra;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;

/**
 * This class implements the Path Selection Algorithm on Uncongested Street Network.
 * 
 * The implementation is based on N. Oppenheim, "Urban Travel Demand Modeling: From Individual Choices to General Equilibrium",
 * ISBN: 0-471-55723-4, Wiley-Interscience, January 1995. <br>
 * <br>
 * @author Illya Stepanov 
 */
public class PedestrianStochPathSelection implements PathSearchingAlgorithm
{
  /**
   * Edge selection probabilities for the given trip source and destination
   * Key: "vs_id:vd_id", Value: array of edges selection probabilities 
   */
  protected java.util.Map edgeProbabilities = new java.util.HashMap();

  /**
   * Shortest path distances to vertices from the given origin vertex
   * Key: origin vertex, Value: array of distances 
   */
  protected java.util.Map shortestPathDistances = new java.util.HashMap();
  
  /**
   * Indicates that edge weights must be calculated based on estimated travel times 
   */
  protected boolean calculateWeights = true;
  
  /**
   * Path selection parameter
   */
  protected float theta;

  /**
   * Gets the value of path selection parameter. <br>
   * <br>
   * @return the value of path selection parameter
   */
  public float getTheta()
  {
    return theta;
  }

  /**
   * Sets the value of path selection parameter. <br>
   * <br>
   * @param theta the value of path selection parameter
   */
  public void setTheta(float theta)
  {
    this.theta = theta;
    edgeProbabilities.clear();
    shortestPathDistances.clear();    
  }
  
  /**
   * Sets the weights' calculation flag. <br>
   * <br> 
   * @param calculateWeights weights' calculation flag
   */
  public void setCalculateWeights(boolean calculateWeights)
  {
    this.calculateWeights = calculateWeights;
  }

  /**
   * Calculates weights for edges of the spatial model graph. <br>
   * <br>
   * @param spatialModel spatial model
   * @param  typicalSpeed typical movement speed of a node
   */
  protected void calculateEdgeWeights(SpatialModel spatialModel, float typicalSpeed)
  {
    Graph graph = spatialModel.getGraph();
    
    // assign edge weights
    java.util.Iterator iter = graph.getEdges().iterator();
    while (iter.hasNext())
    {
      Edge edge = (Edge)iter.next();
    
      // weight is the estimated edge travel time in minutes 
      double weight = edge.getDistance()/typicalSpeed/1000/60;
      edge.setWeight(weight);
    }
  }
  
  /**
   * Gets a path between two vertices. <br>
   * <br>
   * @param spatialModel spatial model
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

    Universe.getReference().sendNotification(new DebugNotification(this, Universe.getReference(),
     "Getting a path from the vertex ("+vs.getX()+" "+vs.getY()+") to the vertex ("+vd.getX()+" "+vd.getY()+")"));

    // get the typical movement speed for the node
    Movement movement = (Movement)node.getExtension("Movement");
    float typicalSpeed = (movement.getMinSpeed() + movement.getMaxSpeed()) / 2 ;
    
    // get the estimated probabilities
    double[] p = (double[])edgeProbabilities.get(vs.getID()+':'+vd.getID()+':'+typicalSpeed);
    if (p==null)
    {
      // calculate edge weights if necessary
      if (calculateWeights)
        calculateEdgeWeights(spatialModel, typicalSpeed);
      
      p = estimateEdgeSelectionProbabilities(spatialModel, vs, vd, flag);
      edgeProbabilities.put(vs.getID()+':'+vd.getID()+':'+typicalSpeed, p);
    }

    double[] dv = (double[])shortestPathDistances.get(vs); 
    
    // result trip
    Trip trip = new Trip();
    java.util.ArrayList trip_points = trip.getPath();

    // add vd-pd if necessary
    Vertex pd_v = graph.getVertex(pd.getX(), pd.getY());
    if (pd_v!=vd)
      trip_points.add(0, pd);

    // add vd
    trip_points.add(0, new Point(vd.getX(), vd.getY()));

    // randomly choose a movement path using a reverse traversal
    Vertex v = vd;
    while (v!=vs)
    {
      double r = Universe.getReference().getRandom().nextDouble();

      // randomly choose a neighbour connected with the incoming edge which has a non-zero selection probability
      Vertex vv = null;
      java.util.Iterator iter = v.getNeighbours().iterator();
      while (iter.hasNext())
      {
        vv = (Vertex)iter.next();
        
        // check if is an incoming edge vv->v
        if (dv[vv.getInternalID()]>=dv[v.getInternalID()])
        {
          vv = null;
          continue;
        }
        
        Edge e = spatialModel.findEdge(vv, v);
        if (p[e.getInternalID()]!=0.0)
        {
          if (r<=p[e.getInternalID()])
            break;

          r-=p[e.getInternalID()];
        }

        vv = null;
      }

      // a path is not found
      if (vv==null)
      {
        // STOCH failed, try the original shortest-path algorithm
        Universe.getReference().sendNotification(new DebugNotification(this, Universe.getReference(),
           "STOCH failed to find a path from the vertex ("+vs.getX()+" "+vs.getY()+") to the vertex ("+vd.getX()+" "+vd.getY()+")"));
        return new Dijkstra().getPath(spatialModel, node, ps, pd, flag);
      }

      v = vv;
      Point v_point = new Point(v.getX(), v.getY());
      
      // add an intermediate point
      if ( (v!=vs) &&
           (!v_point.equals((Point)trip_points.get(0))) )
        trip_points.add(0, v_point);
    }
    
    // add vs
    trip_points.add(0, new Point(vs.getX(), vs.getY()));
    
    // add ps-vs if necessary
    Vertex ps_v = graph.getVertex(ps.getX(), ps.getY());
    if (ps_v!=vs)
      trip_points.add(0, ps);

    return trip;
  }
  
  /**
   * Estimates edge selection probabilities for the trip between the given vertices. <br>
   * <br>
   * @param spatialModel spatial model
   * @param vs source vertex
   * @param vd destination vertex
   * @param flag path-searching flags
   * @return estimated selection probabilities
   */
  protected double[] estimateEdgeSelectionProbabilities(SpatialModel spatialModel, Vertex vs, Vertex vd, int flag)
  {
    Graph graph = spatialModel.getGraph();

    // estimate minimum costs from the trip origin to other graph vertices
    double[] dv = new double[graph.getVertices().size()];
    int[] pv = new int[graph.getVertices().size()];

    applyDijkstra(spatialModel, vs, flag, dv, pv);
    shortestPathDistances.put(vs, dv);

    // estimate initial edge costs 
    double[] a = new double [graph.getEdges().size()];
    java.util.Iterator iter = graph.getEdges().iterator();
    while (iter.hasNext())
    {
      Edge edge = (Edge)iter.next();
    
      if (dv[edge.getV1().getInternalID()]<dv[edge.getV2().getInternalID()])
      {
        a[edge.getInternalID()] = Math.exp(theta*(dv[edge.getV2().getInternalID()]-dv[edge.getV1().getInternalID()]-edge.getWeight()));
      }
      else if (dv[edge.getV2().getInternalID()]<dv[edge.getV1().getInternalID()])
      {
        a[edge.getInternalID()] = Math.exp(theta*(dv[edge.getV1().getInternalID()]-dv[edge.getV2().getInternalID()]-edge.getWeight()));
      }
      else
      {
        // dv[edge.getV2().getInternalID()]==dv[edge.getV1().getInternalID()]
        a[edge.getInternalID()] = 0;
      }
    }

    // forward iterative step
    double[] w = new double[graph.getEdges().size()];
    java.util.Arrays.fill(w, Double.NaN);    
    
    java.util.HashSet verticesToCheck = new java.util.HashSet();
    java.util.HashSet checkedVertices = new java.util.HashSet();
    
    //  estimate weights for the edges outgoing from vs
    checkedVertices.add(vs);
    
    iter = vs.getNeighbours().iterator();
    while (iter.hasNext())
    {
      Vertex v = (Vertex)iter.next();
      Edge e = spatialModel.findEdge(vs, v);
      
      // check if an incoming edge v->vs or an non-efficient edge
      if (dv[v.getInternalID()]<=dv[vs.getInternalID()])
        continue;
      
      if ( ((flag & FLAG_REFLECT_DIRECTIONS)==1) && spatialModel.isMovementProhibited(vs, v) )
        continue;

      w[e.getInternalID()]=a[e.getInternalID()];

      if (v!=vd)
        verticesToCheck.add(v);
    }

outer:
    for (;;)
    {
      // condition to check against loops
      boolean loop_cond = true;
      
      // vertices to be checked during the next steps
      java.util.HashSet verticesToAdd = new java.util.HashSet();
      
      // iterate the vertices under check
      iter = verticesToCheck.iterator();
      while (iter.hasNext())
      {
        Vertex v = (Vertex)iter.next();
        java.util.HashSet succVertices = new java.util.HashSet();
        
        // calculate the sum of weights of incoming edges
        double sum_w = 0;
        boolean res = true;
        java.util.Iterator iter1 = v.getNeighbours().iterator();
        while (iter1.hasNext())
        {
          Vertex v1 = (Vertex)iter1.next();
          
          if (dv[v1.getInternalID()]<dv[v.getInternalID()])
          {
            // an incoming edge v1->v
            if ( ((flag & FLAG_REFLECT_DIRECTIONS)==1) && spatialModel.isMovementProhibited(v1, v) )
              continue;
            
            if (!checkedVertices.contains(v1))
            {
              res = false;
              break;
            }
            else
            {
              Edge e = spatialModel.findEdge(v1, v);
              sum_w+=w[e.getInternalID()];
            }
          }
          else if (dv[v1.getInternalID()]>dv[v.getInternalID()])
          {
            // an outgoing edge v->v1
            if ( ((flag & FLAG_REFLECT_DIRECTIONS)==1) && spatialModel.isMovementProhibited(v, v1) )
              continue;

            succVertices.add(v1);
          }
        }
    
        // break the loop if the destination vertex reached
        if (res && v==vd)
          break outer;
        
        if (res)
        {
          // update weights for the outgoing edges
          iter1 = succVertices.iterator();
          while (iter1.hasNext())
          {
            Vertex v1 = (Vertex)iter1.next();

            Edge e = spatialModel.findEdge(v, v1);
            w[e.getInternalID()] = sum_w*a[e.getInternalID()];
          }
          
          checkedVertices.add(v);
          verticesToAdd.addAll(succVertices);
          
          // remove from the vertices under check
          iter.remove();
          
          loop_cond = false;
        }
      }
      
      if (loop_cond)
        break;
        
      verticesToCheck.addAll(verticesToAdd);
    }

    //  backward iterative step
    double[] p = new double[graph.getEdges().size()];
    
    iter = graph.getEdges().iterator();
    while (iter.hasNext())
    {
      Edge edge = (Edge)iter.next();
    
      Vertex v1, v2;  
      if (dv[edge.getV1().getInternalID()]<dv[edge.getV2().getInternalID()])
      {
        v1 = edge.getV1();
        v2 = edge.getV2();
      }
      else if (dv[edge.getV2().getInternalID()]<dv[edge.getV1().getInternalID()])
      {
        v1 = edge.getV2();
        v2 = edge.getV1();
      }
      else
      {
        // dv[edge.getV2().getInternalID()]==dv[edge.getV1().getInternalID()]
        continue;
      }

      // calculate the sum of weights of incoming edges
      double sum_w = 0;
      java.util.Iterator iter1 = v2.getNeighbours().iterator();
      while (iter1.hasNext())
      {
        Vertex v = (Vertex)iter1.next();
        // check if v->v2 is an incoming edge
        if (dv[v.getInternalID()]<dv[v2.getInternalID()])
        {
          Edge e = spatialModel.findEdge(v, v2);
          
          sum_w+=w[e.getInternalID()];
        }
      }
      
      // calculate the conditional selection probability
      p[edge.getInternalID()] = w[edge.getInternalID()] / sum_w;
      
      // check the value
      if (Double.isInfinite(p[edge.getInternalID()]) || Double.isNaN(p[edge.getInternalID()]) )
        p[edge.getInternalID()] = 0;
    }

    return p;
  }
  
  /**
   * Calculates minimum trip costs from the given vertex using Dijkstra shortest-path algorithm. <br>
   * <br>
   * @param spatialModel spatial model
   * @param vs source vertex
   * @param flag path-searching flags
   * @param dv result cost matrix
   * @param pv result path matrix
   */
  protected void applyDijkstra(SpatialModel spatialModel, Vertex vs, int flag, double dv[], int pv[])
  {
    Graph graph = spatialModel.getGraph();

    boolean known[] = new boolean[graph.getVertices().size()];

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
      for (int i=0; i<graph.getVertices().size(); i++)
        if ( !known[i] && (dv[i]<min_dv) )
        {
          min_dv = dv[i];
          v = (Vertex)graph.getVertices().get(i);
        }

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
           Edge e = (Edge)spatialModel.findEdge(v, w);
           double weight = e.getWeight();

          // update path cost if necessary
          if (dv[v.getInternalID()]+weight<dv[w.getInternalID()])
          {
            dv[w.getInternalID()] = dv[v.getInternalID()]+weight;
            pv[w.getInternalID()] = v.getInternalID();
          }
        }
      }
    }
  }  
}