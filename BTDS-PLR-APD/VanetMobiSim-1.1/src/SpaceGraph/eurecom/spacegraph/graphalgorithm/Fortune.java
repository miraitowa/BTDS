package eurecom.spacegraph.graphalgorithm;
 
/**
 * <p>Title: Fortune Algorithm</p>
 * <p>Description: Creates a Voronoi Tesselation according to the Fortune's Algorithm </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */
 
import eurecom.spacegraph.SpaceGraph;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import java.awt.*;
import java.io.*;
import java.util.Collections;


/**
 * This class is used to generate voronoi diagrams according
 * to the Fortune's Algorithm. 
 * This algorithm runs in O(nlogn) time (average and worst case) 
 * and it uses O(n) storage.
 *
 * @author Jerome Haerri
 * @version 1.0
 */
public class Fortune  implements Voronoi {

	int XPos;
	boolean drawCircles, drawBeach, drawVoronoiLines, drawDelaunay;
	EventQueue Events;
	ArcTree Arcs;
	java.util.ArrayList voronoi;
	java.util.ArrayList delaunay;
	EventXComparator exc;
	Rectangle rectangleArea;
	
	/**
   * Fortune Object given the rectangular bounded box
   */
	public Fortune(int x, int y, int width, int height) {
		drawCircles = false;
		drawBeach = true;
		drawVoronoiLines = true;
		drawDelaunay = false;
		setBounds(x,y,width,height);
		voronoi = new java.util.ArrayList();
		delaunay = new java.util.ArrayList();
		exc = new EventXComparator();
		XPos = 0;
		Arcs = new ArcTree();
		Events = new EventQueue();
	}
	
	/**
	* Fortune Object given a {@link eurecom.spacegraph#SpaceGraph() SpaceGraph} object
   */
	public Fortune(SpaceGraph graph) {
		try {
			int x = (int)graph.getSpatialModel().min_x_clip;
			int y = (int)graph.getSpatialModel().min_y_clip ;
			int width = (int)graph.getClipAreaXY()[0];
			int height = (int)graph.getClipAreaXY()[1];
		
			drawCircles = false;
			drawBeach = true;
			drawVoronoiLines = true;
			drawDelaunay = false;
			setBounds(x,y,width,height);
			voronoi = new java.util.ArrayList();
			delaunay = new java.util.ArrayList();
			exc = new EventXComparator();
			XPos = 0;
			Arcs = new ArcTree();
			Events = new EventQueue();
		}
		catch (Exception e) {
		  System.out.println("Could not get the clip area X and Y values");
			System.exit(-1);
		}
	}
	/**
   * Gets the bounding Rectangle of this area.
   */
	public Rectangle getBounds() {
	  return rectangleArea;
	}
	
	/**
   * Sets the bounding Rectangle of this area.
   */
	public void setBounds(double x, double y, double width, double height) {
		rectangleArea = new Rectangle((int)x,(int)y,(int)width,(int)height);
	}
	
	/**
   * Draws the Voronoi Diagrams and perform degenete checks
	 * @throws Exception e : Cannot open obstacle.in or write to voronoiEdges.out
   */
	public void drawVoronoi(java.util.ArrayList obstacles, java.util.ArrayList voronoiEdges) {
	  String obstaclesFileName = new String("obstacles.in");
		String edgeFileName = new String("voronoiEdges.out");
		//System.out.println("New Voronoi");
		//String edgeFileName = new String("voronoiEdges_new_full.out");
		//JHNote(20/10/2005): used to debug degenerate cases such as two different
		//									  points with the same X coordinate
		//String voronoiSort = new String("voroSort.out");
		String line;
		BufferedReader din; 
		PrintWriter res;
		PrintWriter res1;
		double p,q;
		
		/*try {
		 din = new BufferedReader(new InputStreamReader(new FileInputStream(obstaclesFileName)));
		 res = new PrintWriter(new FileWriter(edgeFileName));*/
		 //res1 = new PrintWriter(new FileWriter(voronoiSort));
		
		/* while ((line = din.readLine()) != null) {
			 String[] result = line.split("\\s");
			 if(result.length==0) {
				 break;
			 }
			 if (result.length != 2) {
				 throw new Exception("Not exactly 2 coodinates per line in voronoi input");
			 }
			
			 p = (new Double(result[0])).doubleValue();
				
			 q = (new Double(result[1])).doubleValue();
			 
			 voronoi.add(new MyPoint(p, q));
			 
		 }*/
		 
		/* for (int i =0; i < obstacles.size(); i++) {
		   java.awt.Point newP = (java.awt.Point)obstacles.get(i);
			 voronoi.add(new MyPoint(newP));
		 }*/
		 
		 voronoi.addAll(obstacles);
		 
		 // Sort the voronoi points by the increasing order of their X coordinate 
		 //	(in order to remove degenerated cases)
		 Collections.sort(voronoi,exc);
		 
		 // 
		 checkDegenerate();
		
		 for(int i = 0; i < voronoi.size(); i++) {
			 Events.insert(new EventPoint((MyPoint)voronoi.get(i)));
			 MyPoint voroPoint = (MyPoint)voronoi.get(i);
			 //res1.println(voroPoint.x + " " + voroPoint.y);
		 }
		
		
		 while(Events.Events != null || XPos < 1000 + getBounds().width) {
			 voronoiStep(); 
		 }
		
		 for(int i = 0; i < voronoi.size(); i++) {
			 if(voronoi.get(i) instanceof MyLine) {
				 MyLine voroEdge = (MyLine)voronoi.get(i);
				 //java.awt.Point p1 = new java.awt.Point(voroEdge.P1.x,voroEdge.P1.y);
				 voronoiEdges.add(voroEdge.P1);
				 //java.awt.Point p2 = new java.awt.Point(voroEdge.P2.x,voroEdge.P2.y);
				 voronoiEdges.add(voroEdge.P2);
				/* res.println(voroEdge.P1.x + " " + voroEdge.P1.y + " " + voroEdge.P2.x + " " + voroEdge.P2.y);*/
			 }
		 }
		/* res.close();*/
		 
		//res1.close();
	/*	}
		catch (IOException e2) {
		    System.err.println("Caught an IO Exception while 'drawVoronoi' ");
		}*/
	}
	
	/**
   * Draws the Voronoi Diagrams and perform degenete checks
	 * @throws Exception e : Cannot open obstacle.in or write to voronoiEdges.out
   */
	public void drawVoronoi() throws Exception {
	  String obstaclesFileName = new String("obstacles.in");
		String edgeFileName = new String("voronoiEdges.out");
		//String edgeFileName = new String("voronoiEdges_new_full.out");
		//JHNote(20/10/2005): used to debug degenerate cases such as two different
		//									  points with the same X coordinate
		//String voronoiSort = new String("voroSort.out");
		String line;
		BufferedReader din; 
		PrintWriter res;
		PrintWriter res1;
		double p,q;
		
		try {
		 din = new BufferedReader(new InputStreamReader(new FileInputStream(obstaclesFileName)));
		 res = new PrintWriter(new FileWriter(edgeFileName));
		 //res1 = new PrintWriter(new FileWriter(voronoiSort));
		
		while ((line = din.readLine()) != null) {
			 String[] result = line.split("\\s");
			 if(result.length==0) {
				 break;
			 }
			 if (result.length != 2) {
				 throw new Exception("Not exactly 2 coodinates per line in voronoi input");
			 }
			
			 p = (new Double(result[0])).doubleValue();
				
			 q = (new Double(result[1])).doubleValue();
			 
			 voronoi.add(new MyPoint(p, q));
		 }
		 
		 // Sort the voronoi points by the increasing order of their X coordinate 
		 //	(in order to remove degenerated cases)
		 Collections.sort(voronoi,exc);
		 
		 // 
		 checkDegenerate();
		
		 for(int i = 0; i < voronoi.size(); i++) {
			 Events.insert(new EventPoint((MyPoint)voronoi.get(i)));
			 MyPoint voroPoint = (MyPoint)voronoi.get(i);
			 //res1.println(voroPoint.x + " " + voroPoint.y);
		 }
		
		
		 while(Events.Events != null || XPos < 1000 + getBounds().width) {
			 voronoiStep(); 
		 }
		
		 for(int i = 0; i < voronoi.size(); i++) {
			 if(voronoi.get(i) instanceof MyLine) {
				 MyLine voroEdge = (MyLine)voronoi.get(i);
				
				res.println(voroEdge.P1.x + " " + voroEdge.P1.y + " " + voroEdge.P2.x + " " + voroEdge.P2.y);
			 }
		 }
		 res.close();
		 
		//res1.close();
		}
		catch (IOException e2) {
		    System.err.println("Caught an IO Exception while 'drawVoronoi' ");
		}
	}
	
	/**
   * Step of the Fortune's Algorithm. In each step, it includes a new point and create
	 * either the siteEvents and the CircleEvents which will create the edges.
   */
	public void voronoiStep() {
		if(Events.Events == null || (double)XPos < Events.Events.x)
			XPos++;

		while(Events.Events != null && (double)XPos >= Events.Events.x) {
			EventPoint eventpoint = Events.pop();
			XPos = Math.max(XPos, (int)eventpoint.x);
			eventpoint.action(this);
			Arcs.checkBounds(this, XPos);
		}
		if(XPos > getBounds().width && Events.Events == null)
			Arcs.checkBounds(this, XPos);
	}
	
	/**
	* Perform degenerate checks to see if two (or more) points have the same X coordinate.
	* In that case, the X coordinate is increased by one unit. If one increased point 
	* is translated out of bounds, we simply remove this obstacle.
   */
	public void checkDegenerate() {
		if(voronoi.size() > 1) {
			for(int i = 1; i < voronoi.size(); i++) {
				MyPoint first = (MyPoint)voronoi.get(i-1);
				MyPoint next  = (MyPoint)voronoi.get(i);
					if(next.x == first.x) {
						(next.x)++;
						System.out.println("Degenerate Case: increased the x field");
						if(next.x >= (getBounds().x + getBounds().width)) {
							System.out.println("Removed an obstacle which is out of bounds");
							voronoi.remove(i);
							i--;
						}
					}	
			}
		}
	}
	
	
	public void paintDelaunay(Graphics g) {
		for(int i = 0; i < delaunay.size(); i++) {
			((Paintable)delaunay.get(i)).paint(g);
		}
	}
	
	public void paintVoronoi(Graphics g) {
		for(int i = 0; i < voronoi.size(); i++) {
			((Paintable)voronoi.get(i)).paint(g);
		}
	}
	
	public void paint(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, (int)getBounds().width, (int)getBounds().height);
		g.setColor(Color.blue);
		paintVoronoi(g);
		g.setColor(Color.red);
		g.drawLine(XPos, 0, XPos, (int)getBounds().height);
		if(Events != null && Arcs != null) {
			g.setColor(Color.black);
			Events.paint(g, drawCircles);
			Arcs.paint(g, XPos, drawVoronoiLines, drawBeach);
		}
		if(drawDelaunay) {
			g.setColor(Color.gray);
			paintDelaunay(g);
		}
	}
	
	// JHNote (20/10/2005): Originally. this method main was thought for debuging and to visually create voronoi diagrams
	//											by the lack of time, I only generated the voronoi edges in a file.
	/**
	 * Method main for commande line invocations  
   */
	public static void main(String args[]) {
		
		if (args.length != 4) {
		  System.out.println("usage: java Fortune X Y width height");
			System.out.println("           where: X: 		  bounding box top left corner X coordinate");
			System.out.println("           				Y: 		  bounding box top left corner Y coordinate");
			System.out.println("           				widht:  bounding box width");
			System.out.println("           				height: bounding box height");
			System.exit(-1);
		}
		
		int x = (new Integer(args[0])).intValue();
		int y = (new Integer(args[1])).intValue();
		int width = (new Integer(args[2])).intValue();
		int height = (new Integer(args[3])).intValue();
	  Fortune fortuneVoronoi = new Fortune(x,y,width,height);
		try {
			fortuneVoronoi.drawVoronoi();
		}
		// JHNote (20/10/2005) GUI: TBD
		/*	Frame f = new FortuneGUI(fortuneVoronoi);
			f.setTitle("Voronoi Diagrams");
			f.setSize(640,400);
			f.setVisible(true);
		}*/
		catch(Exception e) {
		  System.out.println("Could not draw voronoi diagrams");
		}
	}
}
