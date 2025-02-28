package de.uni_stuttgart.informatik.canu.mobisimadd.extensions;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.SpatialModel;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import javax.swing.JFrame;
import javax.swing.JComponent;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.Color;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

/**
 * <p>Title: Canu Mobility Simulation Environment Extra Modules</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003-2004</p>
 * <p>Company: University of Stuttgart</p>
 * <p>Patches: </p>
 * <p> v1.2 (15/11/2005): - Modified the GUI main color
 *											 - Modified the edges colors depending on the number of lanes:
 *									 				- One lane: BLACK
 *									 				- Two lanes: MAGENTA
 *									 				- Three lanes: YELLOW
 *								 	 				- For lanes: WHITE </p>
 * @author Illya Stepanov
 * @author v1.2: Jerome Haerri (haerri@ieee.org)
 * @version 1.2
 */

/**
 * This class implements Graphical User Interface
 * <p>Patches: </p>
 * <p> <i>Version 1.2 by Jerome Haerri (haerri@ieee.org) on 11/15/2005 : 
 * <br>	&nbsp;&nbsp;&nbsp;&nbsp;			- Modified the GUI main color
 * <br>	&nbsp;&nbsp;&nbsp;&nbsp;		  - Modified the edges colors depending on the number of lanes:
 * <br>	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;				- One lane: BLACK
 * <br>	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;				- Two lanes: MAGENTA
 * <br>	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;				- Three lanes: YELLOW
 * <br>	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;				- For lanes: WHITE </i></p>
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.2 Jerome Haerri
 * @version 1.2
 */
public class GUI extends ExtensionModule
{
  /**
   * Main component
   */
  public class GUIComponent extends JComponent  implements MouseMotionListener
  {
    
		/**
     * Border width
     */
    protected final float border_width = 80.0f;
    /**
     * Border height
     */
    protected final float border_height = 80.0f;
    /**
     * Image of the spatial model
     */
    protected BufferedImage spatialModelImage;
    /**
     * X-position of mouse relatively to the content pane
     */
    protected int mouseX;
    /**
     * Y-position of mouse relatively to the content pane
     */
    protected int mouseY;

    /**
     * Constructor
     */
    public GUIComponent()
    {
      setDoubleBuffered(true);
      addMouseMotionListener(this);
    }
		
		
     /** Paints the component
     */
    public void paint(Graphics g)
    {
      super.paint(g);
      redraw(g);
    }
    
    /**
     * Updates the component
     */
    public void update(Graphics g)
    {
      redraw(g);
    }
    
    /**
     * Redraws the window
		 * <br><i>Version 1.2 by Jerome Haerri (haerri@ieee.org): </i>
		 *  <br>&nbsp;&nbsp;&nbsp;&nbsp; 						- Modified the GUI main color
		 *	<br>&nbsp;&nbsp;&nbsp;&nbsp;					 - Modified the edges colors depending on the number of lanes:
		 *	<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;					 - One lane: BLACK
		 *	<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;					 - Two lanes: MAGENTA
		 *	<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;					 - Three lanes: YELLOW
		 *	<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;					 - For lanes: WHITE
     */
    protected void redraw(Graphics g)
    {
      Graphics2D g2 = (Graphics2D)g;
      
      Rectangle clientRect = getBounds();
      Rectangle oriClientRect = new Rectangle(clientRect);
      
      //g2.setBackground(new Color(204, 204, 204));
			g2.setBackground(Color.WHITE);
      
      float dimx = u.getDimensionX();
      float dimy = u.getDimensionY();
      float dx = 0.0f;
      float dy = 0.0f;
      
      //SpatialModel spatialModel = (SpatialModel)u.getExtension("SpatialModel");
      Graph graph = spatialModel.getGraph();
      if ((spatialModel!=null)&&(graph!=null))
      {
        dimx = (float) (graph.getRightmostCoordinate()-graph.getLeftmostCoordinate());
        dimy = (float) (graph.getUppermostCoordinate()-graph.getLowermostCoordinate());
        dx = (float) graph.getLeftmostCoordinate();
        dy = (float) graph.getLowermostCoordinate();
      }
      
      // set transformations 
      float kx = (clientRect.width-border_width)/dimx;
      float ky = (clientRect.height-border_height)/dimy;
      
      if ((kx>2.0f)||(ky>2.0f))
      {
        kx = Math.min(kx, 2.0f);
        ky = Math.min(ky, 2.0f);
        
        int desiredWidth = (int)(dimx*kx+border_width);
        int desiredHeight = (int)(dimy*ky+border_height);
        
        clientRect.setSize(desiredWidth, desiredHeight);
      }

      // create an image of the spatial model if necessary
      if ( (spatialModel!=null)&&
           ((spatialModelImage==null)
          ||(spatialModelImage.getWidth()!=clientRect.width)
          ||(spatialModelImage.getHeight()!=clientRect.height)) )
      {
        spatialModelImage = new BufferedImage(clientRect.width, clientRect.height+10, BufferedImage.TYPE_INT_RGB);
        Graphics2D spatialModelGraphics = spatialModelImage.createGraphics();
        spatialModelGraphics.setBackground(g2.getBackground());
        spatialModelGraphics.clearRect(0, 0, spatialModelImage.getWidth(), spatialModelImage.getHeight());
      
        spatialModelGraphics.setTransform(new AffineTransform(kx, 0, 0, -ky, border_width/2.0f-dx*kx, clientRect.height-border_height/2.0f+dy*ky));
        
        // default color
        spatialModelGraphics.setColor(Color.BLACK);

        // visualize the spatial model
        if (spatialModel!=null)
          spatialModel.visualize(spatialModelGraphics);
        
        spatialModelGraphics.dispose();
      }
      
      // update the component
      g2.clearRect(0, 0, oriClientRect.width, oriClientRect.height);
      
      // default color
      g2.setColor(Color.BLACK);

      if (spatialModelImage!=null)
        g2.drawImage(spatialModelImage, null, 0, 0);

      // display the area border
      g2.drawRect((int)(border_width/2.0f), (int)(border_width/2.0f), (int)(clientRect.width-border_width), (int)(clientRect.height-border_height));
      
      // display positions of mobile nodes
      java.util.Iterator iter = u.getNodes().iterator();
      while (iter.hasNext())
      {
        Node node = (Node)iter.next();
				// Code addded here to visually reflect cars overpassing (since we only see one lane per direction)
				int numberLane = node.getLane();
				switch (numberLane) {
				  case 1: 	
						g2.setColor(Color.BLACK);
						break;
					case 2: 	
						g2.setColor(Color.MAGENTA);
						break;
					case 3: 	
						g2.setColor(Color.YELLOW);
						break;
					default:	
						g2.setColor(Color.WHITE);
				}
				
        Position3D pos = node.getPosition();

        int posx = (int)((pos.getX()-dx)*kx+border_width/2.0f);
        int posy = (int)((dy-pos.getY())*ky+clientRect.height-border_width/2.0f);

        g2.drawRect(posx-2, posy-2, 4, 4);
        g2.drawString(node.getID(), posx+6, posy+6);
      }
      g2.setColor(Color.BLACK);
      // display statistics
      float pointX = ((float)mouseX-border_width/2.0f)/kx+dx;
      float pointY = dy-((float)mouseY-(clientRect.height-border_height/2.0f))/ky;
      g2.drawString("Simulation time: "+u.getTimeAsString()+" Pointer: ("+(int)pointX+" "+(int)pointY+")",
          border_width/2.0f, oriClientRect.height-border_height/4.0f);
    }
    
    /**
     * Updates the coordinates of mouse relatively to the content pane
     */
    protected void updateMousePosition(MouseEvent e)
    {
      mouseX = e.getX();
      mouseY = e.getY();
      repaint();
    }
    
    /**
     * Invoked when a mouse button is pressed on a component and then dragged
     */
    public void mouseDragged(MouseEvent e)
    {
      updateMousePosition(e);
    }
    
    /**
     * Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed
     */
    public void mouseMoved(MouseEvent e)
    {
      updateMousePosition(e);
    }
  }
  
  /**
   * Frame
   */
  protected class GUIFrame extends JFrame
  {
    /**
     * Constructor
     */
    public GUIFrame(String title)
    {
      super(title);
      
      GUIComponent guiComponent = new GUIComponent();
      getContentPane().add(guiComponent);
    }
  }
	
	
	/**
	 * Spatial Model
	*/ 
	protected SpatialModel spatialModel=null;
		
  /**
   * Visualisation window
   */
  protected GUIFrame frame = null;

  /**
   * Width of the visualisation screen (in pixels)
   */
  protected int width = 0; // in pixels

  /**
   * Height of the visualisation screen (in pixels)
   */
  protected int height = 0; // in pixels

  /**
   * Update interval (in ms)
   */
  protected int step = 0;   // in ms

	
  /**
   * Constructor
   */
  public GUI()
  {
    super("GUI");
  }

  /**
   * Gets drawing frame. <br>
   * <br>
   * @return drawing frame
   */
  public JFrame getJFrame()
  {
    return frame;
  }

  /**
   * Performs the module initialization
   */
  public void initialize()
  {
			frame = new GUIFrame("VanetMobiSim");
			frame.setSize(width, height);
			frame.setVisible(true);
  }

  /**
   * Executes the extension. <br>
   * <br>
   * The method is called on every simulation timestep. 
   * @return 0 - the module should be executed on next timesteps,
   *        -1 - the module should not be executed on further timesteps and should be removed from the extensions' list
   */
  public int act()
  {
    if ( (u.getTimeInSteps()==0)||((step!=0)&&(u.getTime() % step == 0)) )
    {
      // activate
				frame.repaint();
    }
    
    return 0;
  }
	
  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "GUI module";
  }

  /**
    * Initializes the object from XML tag. <br>
    * <br>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
  public void load(org.w3c.dom.Element element) throws java.lang.Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading GUI extension"));

    super.load(element);
		
		// JHNote (06/02/2006): Now the SpatialModel is loaded at the beginning.
		String sm = element.getAttribute("spatial_model");
    if (sm.length()>0) {
      spatialModel = (SpatialModel)u.getExtension(sm);
    }
		
		else {
			spatialModel = (SpatialModel)u.getExtension("SpatialModel");
		}
		
		if(spatialModel==null)
      throw new Exception("A SpatialModel is missing!");
		
    org.w3c.dom.Node n;
    n=element.getElementsByTagName("width").item(0);
    if(n==null)
      throw new Exception("<width> is missing!");
    width=Integer.parseInt(n.getFirstChild().getNodeValue());

    n=element.getElementsByTagName("height").item(0);
    if(n==null)
      throw new Exception("<height> is missing!");
    height=Integer.parseInt(n.getFirstChild().getNodeValue());

    n=element.getElementsByTagName("step").item(0);
    if(n!=null)
      step=(int) (Float.parseFloat(n.getFirstChild().getNodeValue())*1000);
		
    // checkout
    if ( width<0 )
      throw new Exception("width value is invalid: "+width);
    if ( height<0 )
      throw new Exception("height value is invalid: "+height);
    if ( step<0 )
        throw new Exception("step value is invalid: "+step/1000);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading GUI extension"));
  }
}