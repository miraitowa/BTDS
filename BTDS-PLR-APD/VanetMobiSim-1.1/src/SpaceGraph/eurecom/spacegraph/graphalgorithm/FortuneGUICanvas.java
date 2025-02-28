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

/**
* Canvas for the Fortune GUI.
*/
class FortuneGUICanvas extends Canvas {
	
	Graphics offScreenGraphics;
	Image offScreenImage;
	Fortune domain;
	
	public FortuneGUICanvas(Fortune domain) {
		this.domain = domain;
		offScreenImage = createImage((int)domain.getBounds().width, (int)domain.getBounds().height);
		offScreenGraphics = offScreenImage.getGraphics();
	}
	
	public void update(Graphics g) {
		offScreenGraphics.setClip(g.getClipBounds());
		paint(offScreenGraphics);
		g.drawImage(offScreenImage, 0, 0, this);
	}
	
	public synchronized void paint(Graphics g) {
		domain.paint(g);
	}
}
