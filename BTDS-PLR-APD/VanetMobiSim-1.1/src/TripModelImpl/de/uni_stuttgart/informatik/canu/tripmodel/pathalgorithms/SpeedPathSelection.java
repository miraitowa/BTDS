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
 * This class implements the Speed-based Path Selection Algorithm. <br>
 * <br>
 * @author Marco Fiore
 */
public class SpeedPathSelection extends PedestrianStochPathSelection
{
 /**
   * Probability that the highest speed road will be chosen
   * 
   */
  protected float speedWeight = 0.8f;
	
  public void setSpeedWeight(float sw) {
    this.speedWeight = sw;
  }
	
  /**
   * Calculates weights for edges of the spatial model graph. <br>
   * <br>
   * @param spatialModel spatial model
   * @param typicalSpeed typical movement speed of a node
   */
  protected void calculateEdgeWeights(SpatialModel spatialModel, float typicalSpeed)
  {
    Graph graph = spatialModel.getGraph();
    Universe uni = Universe.getReference();
    if (uni == null) {
      System.out.println("No instance of universe");
      System.exit(-1);
    }
			
    java.util.Random rand=uni.getRandom();
    // assign edge weights
    java.util.Iterator iter = graph.getEdges().iterator();
    while (iter.hasNext())
    {
      Edge edge = (Edge)iter.next();
      SpatialModelElement element = spatialModel.mapEdgeToElement(edge);

      // weight is the estimated edge travel time in minutes,
      // accounting for speed limits on topology roads
      double allowedSpeed = 80e-3f;
      String segmentAttribute = (String)element.getAttributes().get("SP");
      if (segmentAttribute != null)
        allowedSpeed = Float.parseFloat(segmentAttribute)/1000.0f;

      double weight = (speedWeight+rand.nextDouble()*(1-speedWeight)) * (edge.getDistance()/allowedSpeed/1000/60);
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
