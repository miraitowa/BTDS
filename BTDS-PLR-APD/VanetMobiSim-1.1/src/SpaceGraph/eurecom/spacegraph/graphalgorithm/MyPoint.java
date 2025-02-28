package eurecom.spacegraph.graphalgorithm;

import java.awt.Graphics;
import java.awt.Point;

public class MyPoint implements Paintable {

	public double x, y;
	
	public MyPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public MyPoint(MyPoint mypoint) {
		x = mypoint.x;
		y = mypoint.y;
	}

	public MyPoint(Point point){
		x = point.x;
		y = point.y;
	}

	public void paint(Graphics g) {
		g.fillOval((int)(x - 3.0), (int)(y - 3.0), 7, 7);
	}

	public double distance(MyPoint mypoint) {
		double d = mypoint.x - x;
		double d1 = mypoint.y - y;
		return Math.sqrt(d * d + d1 * d1);
	}
	
}
