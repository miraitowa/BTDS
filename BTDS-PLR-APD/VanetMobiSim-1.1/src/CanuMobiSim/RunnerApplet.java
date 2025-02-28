import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisimadd.extensions.*;
// import de.uni_stuttgart.informatik.canu.mobisimadd.extensions.GUI.GUIComponent;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.xml.parsers.*;
import javax.swing.JFrame;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComponent;
import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.SpatialModel;
import de.uni_stuttgart.informatik.canu.mobisim.extensions.Graph;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.Color;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.Dimension;

public class RunnerApplet extends JApplet implements ActionListener {
	
		
	/**
   * Scenario file's name
   */
  String modelSource;
	
	GUIComponent guiComponent;
	
	
	SwingWorker worker;
	boolean forcedOut = false;
	
	
	JPanel main;
	JPanel control;
	JPanel acks;
	
	JButton first=new JButton("GDF-based"),
	second=new JButton("User-Defined"), 
	third=new JButton("Random-Graph"), 
	fourth=new JButton("AIDM"),
	fifth =new JButton("Traffic"),
	sixth =new JButton("MOBIL");
	
	/**
   * Width of the visualisation screen (in pixels)
   */
  protected int width = 640; // in pixels

  /**
   * Height of the visualisation screen (in pixels)
   */
  protected int height = 400; // in pixels

	protected int step = 0;   // in ms
  /**
   * Constructor
   */
  public void RunApplet() {
    //System.out.println("Starting RunApplet");
		Universe u=Universe.getReference();
		//loadAppletParameters();
    if (!loadModel())
      return;

    u.initialize();
		
		//GUI gui = (GUI)u.getExtension("GUI");
		//JFrame guiFrame = gui.getJFrame();
		
		/*JComponent guiComponent = gui.getGUIComponent();
		guiComponent.setVisible(true);
		getContentPane().add(guiComponent);*/
		
    // advance time in loop
		guiComponent.removeAll();
		
    for (;;) {
			if(forcedOut) {
				break;
			}
      u.advanceTime();
		  if (timeToRefresh()) {
				// guiComponent.repaint();
			 // System.out.println("notifying the thread");
				try {
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						Universe u=Universe.getReference();
            public void run() {
							  //System.out.println("repainting at "+u.getTime());
								
                guiComponent.repaint();
            }
				  });
				}
				catch (Exception e) {
          System.err.println("createGUI didn't successfully complete");
        }
			}
		}
		
		System.out.println("Simulation is over");

    // an extension will stop us ...
  }
	
	public boolean timeToRefresh() {
		Universe u=Universe.getReference();
	  if ( (u.getTimeInSteps()==0)||((step!=0)&&(u.getTime() % step == 0)) )
			return true;
		else
			return false;
	}
	
  void loadAppletParameters() {
			 String scen = getParameter("scenario");
			 if (scen != null)
				 modelSource = scen;
			 else
				 modelSource = "samples/newcomDemo2.xml";
			 
			 String stp = getParameter("step");
			 if (stp !=null) {
				 int stepInt = Integer.parseInt(stp);
				 step = stepInt;
			 }
			 else
				 step = 1;
			 
   }
		

  /**
   * Initializes the model from source file. <br>
   * <br>
   * @return true, if the model was successfully loaded
   */
  protected boolean loadModel()
  {
    boolean retval=true;

    Universe u=Universe.getReference();
		java.io.InputStream is=null; 
    try {
      // parse the scenario
        
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
			
			java.net.URL url = new java.net.URL(modelSource);
			
      is = url.openStream();         // throws an IOException
			org.w3c.dom.Document document = builder.parse(is);
     
		 // JHNote (24/01/2006): In order to remove the .java.policy requirements to read 
		 //                      from a user disk, we load a file from the applet basedir URL
		 // org.w3c.dom.Document document = builder.parse(new java.io.FileInputStream(modelSource));

      org.w3c.dom.Element root=document.getDocumentElement();

      String rootTag=root.getNodeName();
      if (!rootTag.equals("universe"))
        throw new Exception("Invalid parent tag: "+rootTag);

      u.load(root);
    }
		catch (java.net.MalformedURLException mue) {
         System.out.println("MalformedURLException for URL " + modelSource);
         mue.printStackTrace();
    }
    catch(Exception e) {
      System.err.println("Error loading model from "+modelSource);
      e.printStackTrace(System.err);
      retval=false;
    }
		
		finally {
			try {
				is.close();
			} 
			catch (java.io.IOException ioe) {
            // just going to ignore this one
			}
    }
    return retval;
  }	
		
	public synchronized void actionPerformed(ActionEvent e) {
    // RunApplet();
		Universe u=Universe.getReference();
		String command=e.getActionCommand();
		
		guiComponent.removeAll();
		JLabel statusLabel = new JLabel("Loading Graph...", JLabel.CENTER);
    statusLabel.setForeground(Color.black);
    guiComponent.add(statusLabel, BorderLayout.CENTER);
	  guiComponent.repaint();
	
    if(command.equals("GDF")){
			//System.out.println("GDF");
			clear();
			//worker.interrupt();
			modelSource = getCodeBase()+"samples/newcomAppletDemo1.xml";
			//u.flush();
			startup();
		}
		else if (command.equals("User")){
			//System.out.println("User");
			clear();
			//worker.interrupt();
			modelSource = getCodeBase()+"samples/newcomAppletDemo2.xml";
			//u.flush();
			startup();
		}
		else if (command.equals("Random")){
			//System.out.println("Random");
			clear();
			//worker.interrupt();
			modelSource = getCodeBase()+"samples/newcomAppletDemo3.xml";
			//u.flush();
			startup();
		}
		else if (command.equals("AIDM")){
			//System.out.println("AIDM");
			clear();
			//worker.interrupt();
			modelSource = getCodeBase()+"samples/newcomAppletDemo4.xml";
			//u.flush();
			startup();
		}
		else if (command.equals("Traffic")){
			//System.out.println("Traffic");
			clear();
			//worker.interrupt();
			modelSource = getCodeBase()+"samples/newcomAppletDemo5.xml";
			//u.flush();
			startup();
		}
		else if (command.equals("MOBIL")){
			//System.out.println("Traffic");
			clear();
			//worker.interrupt();
			modelSource = getCodeBase()+"samples/newcomAppletDemo6.xml";
			//u.flush();
			startup();
		}
		else {
		  System.out.println("Command Name not supported");
		}
			
		System.out.println("Loading sample " + modelSource);
		
		
		
		
		//guiComponent.repaint();
  }
		
	public synchronized void init() {
		//System.out.println("Applet initialized");
		loadAppletParameters();
		setSize(width, height+50);
		setVisible(true);
		
		main = new JPanel(); 
		main.setBackground(Color.WHITE);
		guiComponent = new GUIComponent();
		guiComponent.setVisible(true);
		guiComponent.setOpaque(true);
		guiComponent.setPreferredSize(new Dimension(width,height));
		main.add(guiComponent,BorderLayout.CENTER);
		
		
		//initialisation right panel 
		control = new JPanel(); 
		//control.setPreferredSize(new Dimension(widthControl,numLegend*25));
		//control.setLayout(new GridLayout(5,1));
		control.setBackground(Color.WHITE);
		//JScrollPane controlPane=new JScrollPane(pnControl);
		main.add(control,BorderLayout.SOUTH);
		
	  control.add(first);
		control.add(second);
		control.add(third);
		control.add(fourth);
		control.add(fifth);
		control.add(sixth);
		//acks = new JPanel(); 
		//acks.setPreferredSize(new Dimension(widthControl,numLegend*25));
		//acks.setLayout(new GridLayout(1,3));
		
		
		
    //setContentPane(guiComponent);
		setContentPane(main);
		
		
		first.setActionCommand("GDF");
		second.setActionCommand("User");
		third.setActionCommand("Random");
		fourth.setActionCommand("AIDM");
		fifth.setActionCommand("Traffic");
		
		first.addActionListener(this);
		second.addActionListener(this);
		third.addActionListener(this);
		fourth.addActionListener(this);
		fifth.addActionListener(this);
		sixth.addActionListener(this);
		
		//RunApplet();
		/*final SwingWorker worker = new SwingWorker() {
									 public Object construct() {
										 System.out.println("running applet");
										 String done = "done";
										 RunApplet();
										 return done;
									 }
	 };
	 worker.start();*/
		
		
	}

	public synchronized void startup() {	
	/*	try {
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
								System.out.println("Applet started");
                 RunApplet();*/
								 /*final SwingWorker worker = new SwingWorker() {
									 public Object construct() {
										 System.out.println("trying to repaint");
										 RunApplet();
										 return null;
									 }
								 };
								 worker.start();*/
    /*        }
        });
    } catch (Exception e) {
        System.err.println("createGUI didn't successfully complete");
    }*/
		forcedOut = false;
		worker = new SwingWorker() {
									 public Object construct() {
										// System.out.println("running applet");
										 String done = "done";
										 RunApplet();
										 return done;
									 }
									 
	 };
	 worker.start();
		
	}
	
	public synchronized void start() {	
	  	JLabel statusLabel = new JLabel("Choose your simulation from the buttoms...", JLabel.CENTER);
			statusLabel.setForeground(Color.black);
			guiComponent.add(statusLabel, BorderLayout.CENTER);
	}
	
	public synchronized void stop() {
	}
	
	public synchronized void clear() {
		Universe u=Universe.getReference();
	  forcedOut = true;
		if (worker !=null) {
		  worker.interrupt();
		}
		u.flush();
	}
	
	/**
   * Main component
   */
  public class GUIComponent extends JPanel implements MouseMotionListener
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
			super(new BorderLayout());
     // addMouseMotionListener(this);
			setDoubleBuffered(true);
    }
		
		
     /** Paints the component
     */
    public void paint(Graphics g)
    {
     super.paint(g);
		 // 
			//System.out.println("paint");
      redraw(g);
			
			//
			
    }
    
    /**
     * Updates the component
     */
    public void update(Graphics g)
    {
      //System.out.println("draw");
			redraw(g);
			
    }
    
    /**
     * Redraws the window
     */
    protected void redraw(Graphics g)
    {
      //System.out.println("Redrawing");
			Universe u=Universe.getReference();
			Graphics2D g2 = (Graphics2D)g;
      Rectangle clientRect = getBounds();
      Rectangle oriClientRect = new Rectangle(clientRect);
      //g2.setBackground(new Color(204, 204, 204));
			g2.setBackground(Color.WHITE);
      float dimx = u.getDimensionX();
      float dimy = u.getDimensionY();
      float dx = 0.0f;
      float dy = 0.0f;
      SpatialModel spatialModel = (SpatialModel)u.getExtension("SpatialModel");
			if (spatialModel == null)
				return;
			
			
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
      
      // display statistics
      float pointX = ((float)mouseX-border_width/2.0f)/kx+dx;
      float pointY = dy-((float)mouseY-(clientRect.height-border_height/2.0f))/ky;
      g2.drawString("Simulation time: "+u.getTimeAsString()+" Pointer: ("+(int)pointX+" "+(int)pointY+")",
          border_width/2.0f, oriClientRect.height-border_height/4.0f);
    }
    		
		 protected void paintComponent(Graphics g) {
			 super.paintComponent(g);
			 //System.out.println("redraw");
       redraw(g);     
			 
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
	
}
