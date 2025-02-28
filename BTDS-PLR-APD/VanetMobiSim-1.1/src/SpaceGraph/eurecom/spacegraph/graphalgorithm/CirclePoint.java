package eurecom.spacegraph.graphalgorithm;

import java.awt.Graphics;

class CirclePoint extends EventPoint {
	
	double radius;
	ArcNode arc;
	
	CirclePoint(double X, double Y, ArcNode arcnode) {
		super(X, Y);
		arc = arcnode;
		radius = distance(arcnode);
		x += radius;
	}

	public void paint(Graphics g) {
		super.paint(g);
		double d = radius;
		g.drawOval((int)(x - 2D * d), (int)(y - d), (int)(2D * d), (int)(2D * d));
	}

	public void action(Fortune domain) {
		ArcNode arcnode = arc.Prev;
		ArcNode arcnode1 = arc.Next;
		MyPoint mypoint = new MyPoint(x - radius, y);
		arc.completeTrace(domain, mypoint);
		arcnode.completeTrace(domain, mypoint);
		arcnode.startOfTrace = mypoint;
		arcnode.Next = arcnode1;
		arcnode1.Prev = arcnode;
		if(arcnode.circlePoint != null) {
			domain.Events.remove(arcnode.circlePoint);
			arcnode.circlePoint = null;
		}
		if(arcnode1.circlePoint != null) {
			domain.Events.remove(arcnode1.circlePoint);
			arcnode1.circlePoint = null;
		}
		arcnode.checkCircle(domain.Events);
		arcnode1.checkCircle(domain.Events);
	}
}
