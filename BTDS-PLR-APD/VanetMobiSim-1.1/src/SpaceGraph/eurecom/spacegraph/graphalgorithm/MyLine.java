package eurecom.spacegraph.graphalgorithm;

import java.awt.Graphics;

class MyLine implements Paintable {
	
	MyPoint P1, P2;
	
	MyLine(MyPoint mypoint, MyPoint mypoint1) {
		P1 = mypoint;
		P2 = mypoint1;
	}

	public void paint(Graphics g) {
		g.drawLine((int)P1.x, (int)P1.y, (int)P2.x, (int)P2.y);
	}

}
