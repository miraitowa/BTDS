package de.uni_stuttgart.informatik.canu.mobisim.mobilitymodels;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.senv.core.Vertex;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * <p>Patches: </p>
 * <p> v1.2 (22/08/2005):	The Graph Walk Mobility Model is able to load
 *			  									a graph from a SpatialModel extension. By doing
 *			  									so, The Graph Walk Mobility Model may be used
 *			  									with a SpaceGraph extension (a random graph generator).</p>
 * @author Canu Research group
 * @author v1.2: Jerome Haerri (haerri@ieee.org) 
 * @version 1.2
 */

/**
 * This class implements the Graph Walk Mobility Model
 * <p>Patches: </p>
 * <p> <i> Version 1.2 by Jerome Haerri (haerri@ieee.org) on 08/22/2005:	
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; The Graph Walk Mobility Model is able to load
 *			  									a graph from a SpatialModel extension. By doing
 *			  									so, The Graph Walk Mobility Model may be used
 *			  									with a SpaceGraph extension (a random graph generator).</i></p>
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.2 Jerome Haerri
 * @version 1.2
 */
public class GraphWalk extends RandomWaypointWalk
{
  /**
   * Associated graph
   */
  protected Graph graph;

  /**
   * Last visited vertex
   */
  protected Vertex lastVertex;
  /**
   * Destination vertex
   */
  protected Vertex destVertex;

  /**
   * Destination point ID
   */
  protected int pointId;
  /**
   * Array of points to pass
   */
  protected java.util.ArrayList path;


  /**
   * Constructor
   */
  public GraphWalk()
  {
    path = new java.util.ArrayList();
  }

  /**
   * Constructor. <br>
   * <br>
   * @param node parent {@link Node Node} object
   */
  public GraphWalk(Node node)
  {
    super(node);
    path = new java.util.ArrayList();
  }


  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Graph Walk movement module";
  }


  /**
   * Gets the last visited vertex. <br>
   * <br>
   * @return last visited vertex
   */
  public de.uni_stuttgart.informatik.canu.senv.core.Vertex getLastVertex()
  {
    return lastVertex;
  }


  /**
   * Gets the destination vertex. <br>
   * <br>
   * @return destination vertex
   */
  public de.uni_stuttgart.informatik.canu.senv.core.Vertex getDestinationVertex()
  {
    return destVertex;
  }


  /**
   * Initializes a movement data
   */
  public void initialize()
  {
    Node owner = (Node)this.owner;

    // checkout
    java.util.ArrayList vertices=graph.getVertices();
    int len=vertices.size();

    Position3D pos=owner.getPosition();

    boolean found=false;
    for (int i=0; i<len; i++)
    {
      Vertex v=(Vertex)vertices.get(i);
      if( (v.getX()==pos.getX()) && (v.getY()==pos.getY()) )
      {
        found=true;
        pointId=i;
        break;
      }
    }

    if (!found)
    {
      System.err.println("Fatal error: source position of "+owner.getID()+
        " is not a point of graph: "+pos);
      System.exit(1);
    }

    lastVertex = (Vertex)vertices.get(pointId);

    u.sendNotification(new DebugNotification(this, u,
      owner.getID()+" located at vertex "+lastVertex.getID()));
  }


  /**
   * Chooses a new movement path. <br>
   * <br>
   * Randomly chooses next destination point of the graph and
   * initiates new movement.
   */
  public void chooseNewPath()
  {
    java.util.Random rand=u.getRandom();
    java.util.ArrayList vect=graph.getVertices();

    // move to random point
    int destId=rand.nextInt(vect.size());

    speed=minSpeed + (maxSpeed-minSpeed)*rand.nextFloat();

    int pathVertices[]=graph.getPathMatrix()[pointId][destId].getVertices();

    for (int i=1; i<pathVertices.length; i++)
      path.add(new Integer(pathVertices[i]));

    u.sendNotification(new DebugNotification(this, u,
      ((Node)owner).getID()+" started movement to vertex "+
      ((Vertex)vect.get(destId)).getID()));
  }


  /**
   * Chooses a new destination point and movement speed. <br>
   * <br>
   * Destination is choosed as the next point of the path or, if empty, a new path
   * is choosen.
   */
  public void chooseNewMovement()
  {
    Node owner = (Node)this.owner;
    java.util.ArrayList vect=graph.getVertices();

    if (path.size()==0)
      chooseNewPath();

    pointId=((Integer)path.get(0)).intValue();
    path.remove(0);

    destVertex=(Vertex)graph.getVertices().get(pointId);
    destination = new Position3D(destVertex.getX(), destVertex.getY(), 0f);

    stay = 0;

    movement = owner.getPosition().getNormalizedDirectionVector(destination).
      mult(speed*u.getStepDuration());

    u.sendNotification(new MovementChangedNotification(this, u,
      destination, speed*1000f));
    u.sendNotification(new DebugNotification(this, u,
      owner.getID()+" moves to checkpoint vertex "+destVertex.getID()));
  }


  /**
   * Continues the movement to destination or, if arrived, chooses time for staying at the current position
   */
  public void chooseNewStayDuration()
  {
    Node owner = (Node)this.owner;

    u.sendNotification(new DebugNotification(this, u,
      owner.getID()+" arrived to checkpoint "+destVertex.getID()));
    lastVertex = destVertex;

    if (path.size()==0)
    {
      // wait at destination
      stay = (int)(minStay+(maxStay-minStay) * u.getRandom().nextFloat());

      u.sendNotification(new DestinationReachedNotification(this, u,
        owner.getPosition(), stay/1000.0f));
    }
    else
    {
      chooseNewMovement();
    }
  }


  /**
    * Initializes the object from XML tag. <br>
    * <br>
		* <i>Version 1.2 by Jerome Haerri (haerri@ieee.org) 
		* <br>
		* &nbsp;&nbsp;&nbsp;&nbsp; Obtain a graph either directly, or through a
		* spatial model</i>
		* <br>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading GraphWalk extension"));

    // get graph
		// JHNote (22/08/2005) : Patched in order to obtain
		//											 a graph either directly, or through a
		//											 a spatial model
    
		String graphName = element.getAttribute("graph");
		if (graphName.length()>0) {
		  graph=(Graph)u.getExtension(graphName);
			if (graph==null)
				throw new Exception("Invalid graph name");
		}
		else {
			String modelName = element.getAttribute("model");
			if (modelName.length()>0) {
				SpatialModel model = (SpatialModel)u.getExtension(modelName);
				if (model !=null) {
					model.rebuildGraph();
					graph = model.getGraph();
					//graph.getInfrastructureGraph().reorganize(false);
					graph.getInfrastructureGraph().calculateShortestPaths();
				}
				else
					throw new Exception("Invalid model name");
			}
			else
					throw new Exception("No spatial model loaded or missing graph name");
		}
    super.load(element);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading GraphWalk extension"));
  }//proc
}