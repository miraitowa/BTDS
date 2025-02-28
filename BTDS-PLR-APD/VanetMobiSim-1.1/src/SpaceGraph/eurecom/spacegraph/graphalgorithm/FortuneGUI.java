package eurecom.spacegraph.graphalgorithm;

/**
 * <p>Title: Fortune Algorithm GUI</p>
 * <p>Description: Graphical visualisation of the Fortune's Voronoi Diagrams </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */
 
import java.awt.*;

class FortuneGUI extends Frame {
	
	FortuneGUICanvas fortuneCanvas;
	
	public FortuneGUI(Fortune domain) {
		fortuneCanvas = new FortuneGUICanvas(domain);
		setLayout(new BorderLayout());
		add("Center", fortuneCanvas);
		fortuneCanvas.repaint();
	}
}
