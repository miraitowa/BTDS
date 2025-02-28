package de.uni_stuttgart.informatik.canu.tripmodel.pathalgorithms;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels.Movement;
import de.uni_stuttgart.informatik.canu.senv.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.Point;
import de.uni_stuttgart.informatik.canu.tripmodel.core.*;
import de.uni_stuttgart.informatik.canu.uomm.FluidTrafficMotion;
import de.uni_stuttgart.informatik.canu.uomm.IntelligentDriverMotion;

/**
 * This class implements the Path Selection Algorithm on Congested Street Network. <br>
 * <br>
 * @author Illya Stepanov 
 */
public class VehicularStochPathSelection extends PedestrianStochPathSelection
{
  /**
   * Calculates weights for edges of the spatial model graph. <br>
   * <br>
   * @param spatialModel spatial model
   * @param typicalSpeed typical movement speed of a node
   */
  protected void calculateEdgeWeights(SpatialModel spatialModel, float typicalSpeed)
  {
    Graph graph = spatialModel.getGraph();
    
    // assign edge weights
    java.util.Iterator iter = graph.getEdges().iterator();
    while (iter.hasNext())
    {
      Edge edge = (Edge)iter.next();
      SpatialModelElement element = spatialModel.mapEdgeToElement(edge);

      // weight is the estimated edge travel time in minutes 
      double t0 = edge.getDistance()/typicalSpeed/1000/60;
    
      // get parameters of BPR function for the link

      // typical values
      double alpha = 0.15;
      double beta = 4;
      
      // calculate current traffic volume on the link
      // find other nodes on the same edge
      double sum_speed = 0;
      java.util.Iterator iter1 = Universe.getReference().getNodes().iterator();
      while (iter1.hasNext())
      {
        Node node = (Node)iter1.next();
        
        Vertex vs = null;
        Vertex vd = null;

        Movement m = (Movement)node.getExtension("Movement");
        if (m instanceof FluidTrafficMotion)
        {
          FluidTrafficMotion mf = (FluidTrafficMotion)m;
          if (mf.getDestination()!=null)
          {
            vs = graph.getVertex(mf.getOldPosition().getX(), mf.getOldPosition().getY());
            vd = graph.getVertex(mf.getDestination().getX(), mf.getDestination().getY());
            
            if ( ((vs==edge.getV1())&&(vd==edge.getV2())))
            {
              sum_speed+=mf.getSpeed();
            }
          }
        }
        else
        if (m instanceof IntelligentDriverMotion)
        {
          IntelligentDriverMotion mf = (IntelligentDriverMotion)m;
          if (mf.getDestination()!=null)
          {
            vs = graph.getVertex(mf.getOldPosition().getX(), mf.getOldPosition().getY());
            vd = graph.getVertex(mf.getDestination().getX(), mf.getDestination().getY());
            
            if ( ((vs==edge.getV1())&&(vd==edge.getV2())))
            {
              sum_speed+=mf.getSpeed();
            }
          }
        }
      }
      
      // calculate link traffic volume in (veh./h)
      double traffic_volume = (sum_speed/edge.getDistance())*3600.0*1000.0;
      
      // apply BPR function
      double link_capacity = 1200.0;
      double weight = t0*(1+alpha*Math.pow(traffic_volume/link_capacity, beta));
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
    // always recalculate link weights
    setCalculateWeights(true);
    // clear previous results
    shortestPathDistances.clear();
    edgeProbabilities.clear();
    
    // get a path
    return super.getPath(spatialModel, node, ps, pd, flag);
  }
}