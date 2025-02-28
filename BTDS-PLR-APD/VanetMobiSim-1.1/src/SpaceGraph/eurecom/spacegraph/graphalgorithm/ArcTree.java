package eurecom.spacegraph.graphalgorithm;

import java.awt.Graphics;
import java.io.PrintStream;

class ArcTree {
	
	ArcNode Arcs;
	
	public void insert(MyPoint mypoint, double d, EventQueue eventqueue) {
		if(Arcs == null) {
			Arcs = new ArcNode(mypoint);
			return;
		}
		try {
			ParabolaPoint parabolapoint = new ParabolaPoint(mypoint);
			parabolapoint.init(d);
			Arcs.init(d);
			Arcs.insert(parabolapoint, d, eventqueue);
			return;
		}
		catch(Throwable _ex) {
			System.out.println("*** error: No parabola intersection during ArcTree.insert()");
		}
	}

	public void checkBounds(Fortune domain, double d) {
		if(Arcs != null) {
			Arcs.init(d);
			Arcs.checkBounds(domain, d);
		}
	}

	public void paint(Graphics g, double d, boolean flag, boolean drawBeach) {
		if(Arcs != null) {
			Arcs.init(d);
			Arcs.paint(g, d, 0.0D, flag, drawBeach);
		}
	}
}
