package de.uni_stuttgart.informatik.canu.mobisim.core;

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
 * <p> v1.2 (22/08/2005): Position3D is now able to extract a graph from
 *        						 			a SpatialModel extension.</p>
 * <p> v1.3 by Jerome Haerri (haerri@ieee.org) on 03/02/2006: 
 *									Position3D contains an unique identifier. This ID is needed
 * 									in order to create a unique XYZGDFRecord for the GDFWriter.</p>
 * @author Canu Research group
 * @author v1.2-v1.3: Jerome Haerri (haerri@ieee.org)
 * @version 1.3 
 */

/**
 * This class contains a position of mobile node
 * <p>Patches: </p>
 * <p> <i> Version 1.2 by Jerome Haerri (haerri@ieee.org) on 22/08/2005: 
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;  Position3D is now able to extract a graph from
 *        						 			a SpatialModel extension.</i></p>
 * <p> <i> Version 1.3 by Jerome Haerri (haerri@ieee.org) on 03/02/2006: 
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;  Position3D contains an unique identifier. This ID is needed
 * in order to create a unique XYZGDFRecord for the GDFWriter.</i></p>
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.0-1.1 Gregor Schiele
 * @author 1.2-1.3 Jerome Haerri
 * @version 1.3
 */
public class Position3D implements XMLStreamable
{
   /**
    * Coordinates
    */
   protected Vector3D p;

	 /**
    * ID of the Coordinates
		* @since 1.3
    */
		protected String id;
	 
  /**
    * Constructor. <br>
    * <br>
    * @param x x-coordinate
    * @param y y-coordinate
    * @param z z-coordinate
    */
   public Position3D(String id, double x, double y, double z) {
     this.id = id; 
		 this.p = new Vector3D(x,y,z);
   }//endproc
	 
	 /**
    * Constructor. <br>
    * <br>
    * @param x x-coordinate
    * @param y y-coordinate
    * @param z z-coordinate
    */
   public Position3D(double x, double y, double z) {
     this.id = "-1"; 
		 this.p = new Vector3D(x,y,z);
   }//endproc


  /**
    * Constructor. <br>
    * <br>
    * @param v position
    */
   public Position3D(Position3D v) {
      this(v.getID(), v.getX(), v.getY(), v.getZ());
   }//endproc


  /**
    * Constructor. <br>
    * <br>
    * @param v position
    */
   public Position3D(Vector3D v) {
     this.id = "-1"; 
		 this.p = v;
   }//endproc

	 /**
    * Sets the ID. <br>
    * <br>
    * @param id unique identifier
		* @since 1.3
    */
		
		public void setID(String id) {
		  this.id = id;
		}
		
		/**
    * Returns the Position3D id. <br>
    * <br>
    * @return id unique identifier
		* @since 1.3
    */
		
		public String getID() {
		  return id;
		}
		
		
  /**
   * Constructor. <br>
   * <br>
   * @param element source tag
   * @throws Exception Exception if parameters are invalid
   */
   public Position3D(org.w3c.dom.Element element) throws Exception
   {
     this(0.0f, 0.0f, 0.0f);
     this.load(element);
   }


  /**
   * Indicates whether this position is equal to some other position. <br>
   * <br>
   * @param o position to compare with
   * @return true, if the positions are the same
   *
   */
   public boolean equals(Object o) {
     if (o instanceof Position3D)
     {
       Position3D pos = (Position3D)o;
       if ((pos.getX() == this.getX()) && (pos.getY() == this.getY()) &&
           (pos.getZ() == this.getZ()))
           return true; 
     }
     return false;
   }


  /**
   * Returns a string representation of the object. <br>
   * <br>
   * @return string representation of the object
   */
   public String toString() {
     return "( "+getX()+", "+getY()+", "+getZ()+" )";
   }


   /**
    * Gets x-coordinate of the position. <br>
    * <br>
    * @return x-coordinate of the position
    */
   public double getX() {
      return this.p.getX();
   }//endproc


   /**
    * Gets y-coordinate of the position. <br>
    * <br>
    * @return y-coordinate of the position
    */
   public double getY() {
      return this.p.getY();
   }//endproc


   /**
    * Gets z-coordinate of the position. <br>
    * <br>
    * @return z-coordinate of the position
    */
   public double getZ() {
      return this.p.getZ();
   }//endproc


   /**
    * Gets a direction vector to the position. <br>
    * <br>
    * @param dest position
    * @return direction vector to the position
    */
   public Vector3D getDirectionVector(Position3D dest) {
     return dest.p.sub(this.p);
   }


   /**
    * Gets a normalized direction vector to the position. <br>
    * <br>
    * @param dest position
    * @return normalized direction vector to the position
    */
   public Vector3D getNormalizedDirectionVector(Position3D dest) {
     return this.getDirectionVector(dest).normalize();
   }


   /**
    * Gets a distance to position. <br>
    * <br>
    * @param pos position
    * @return distance to position
    */
   public double getDistance(Position3D pos) {
      return this.getDirectionVector(pos).getLength();
   }//endproc


   /**
    * Calculates the sum of the position and a vector. <br>
    * <br>
    * @param v vector to add
    * @return sum of the current position and the vector
    */
   public Position3D add(Vector3D v) {
      return new Position3D(this.p.add(v));
   }//endproc


   /**
    * Calculates the sum of the current and some other position. <br>
    * <br>
    * @param pos position to add
    * @return sum of two positions
    */
   public Position3D add(Position3D pos) {
      return pos.add(this.p);
   }//endproc


  /**
    * Loads the object from XML tag. <br>
    * <br>
		* <i> Version 1.2 by Jerome Haerri (haerri@ieee.org):
		* <br> &nbsp;&nbsp;&nbsp;&nbsp; Either randomly generates the positions, or uses a graph either from a 
		* graph module or a spatial model. </i>
		* <br>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
   public void load(org.w3c.dom.Element element) throws Exception
   {
     Universe u=Universe.getReference();

     
		 // JHNote (22/08/2005): Patched here in order to be able to either
		 //											 randomly generate the positions, or use
		 //											 a graph either from a graph module or
		 //											 a spatial model
		 // get graph
     Graph graph = null;
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
					 graph.getInfrastructureGraph().reorganize(false);
					 graph.getInfrastructureGraph().calculateShortestPaths();
				 }
				 else
					 throw new Exception("Invalid model name");
			 }
		 }
		 
		 /*Graph graph = null;
     String graphName = element.getAttribute("graph");
		 if (graphName.length()>0) {
			 graph=(Graph)u.getExtension(graphName);
			 if (graph==null) {
				 SpatialModel model = (SpatialModel)u.getExtension("SpatialModel");
				 if (model !=null) {
					 model.rebuildGraph();
					 graph = model.getGraph();
					 graph.getInfrastructureGraph().reorganize(false);
					 graph.getInfrastructureGraph().calculateShortestPaths();
				 }
				 else
					 throw new Exception("Invalid Spatial Model name");
			 }
		 }*/
		 
		 
  /*   if (graphName.length()>0)
     {
       graph=(Graph)u.getExtension(graphName);
       if (graph==null)
         throw new Exception("Invalid graph name");
     }*/

     double x=0.0f, y=0.0f, z=0.0f;

     // check for random
     String randTag = element.getAttribute("random");
     if ((randTag.length()>0) && Boolean.valueOf(randTag).booleanValue())
     {
       u.sendNotification(new LoaderNotification(this, u,
         "Processing 'random' attribute"));

       java.util.Random rand=u.getRandom();
       if (graph!=null)
       {
         // get random point in the graph
         java.util.ArrayList vect=graph.getVertices();

         // choose random point
         int pointId=rand.nextInt(vect.size());

         Vertex vertex=(Vertex)vect.get(pointId);

         x=vertex.getX();
         y=vertex.getY();
         z=0f;
       }
       else
       {
         // get random point in the simulation area
         x=rand.nextDouble()*u.getDimensionX();
         y=rand.nextDouble()*u.getDimensionY();
         z=rand.nextDouble()*u.getDimensionZ();
       }

       u.sendNotification(new LoaderNotification(this, u,
         "Finished processing 'random' attribute"));
     }

     // get x, y, z
     org.w3c.dom.NodeList list = element.getChildNodes();
     int len=list.getLength();

     for(int i=0; i<len; i++)
     {
       org.w3c.dom.Node item = list.item(i);
       String tag = item.getNodeName();

       if(tag.equals("#text"))
       {
         // skip it
         continue;
       }
       else
       if(tag.equals("#comment"))
       {
         // skip it
         continue;
       }
       else
       if(tag.equals("x"))
       {
         u.sendNotification(new LoaderNotification(this, u,
           "Processing <x> tag"));

         x = Double.parseDouble(item.getFirstChild().getNodeValue());

         u.sendNotification(new LoaderNotification(this, u,
           "Finished processing <x> tag"));
       }
       else
       if(tag.equals("y"))
       {
         u.sendNotification(new LoaderNotification(this, u,
           "Processing <y> tag"));

         y = Double.parseDouble(item.getFirstChild().getNodeValue());

         u.sendNotification(new LoaderNotification(this, u,
           "Finished processing <y> tag"));
       }
       else
       if(tag.equals("z"))
       {
         u.sendNotification(new LoaderNotification(this, u,
           "Processing <z> tag"));

         z = Double.parseDouble(item.getFirstChild().getNodeValue());

         u.sendNotification(new LoaderNotification(this, u,
           "Finished processing <z> tag"));
       }
     }

     // checkout
     if (graph==null)
     {
       if((x<0.0)||(y<0.0)||(z<0.0)||
          (x>u.getDimensionX())||(y>u.getDimensionY())||(z>u.getDimensionZ()))
             throw new Exception("Position is outside Universe dimensions:"+
               " Position3D("+x+","+y+","+z+")");
     }
     else
     {
       // check point in graph
       boolean found=false;
       java.util.ArrayList vertices=graph.getVertices();
       int size=vertices.size();
       for (int i=0; i<size; i++)
       {
         Vertex v=(Vertex)vertices.get(i);
         if( (v.getX()==x) && (v.getY()==y) )
         {
           found=true;
           break;
         }
       }

       if (!found)
         throw new Exception("Source position is not a point of the graph: "
           +toString());
     }

     p = new Vector3D(x, y, z);
   }//endproc
}//endclass