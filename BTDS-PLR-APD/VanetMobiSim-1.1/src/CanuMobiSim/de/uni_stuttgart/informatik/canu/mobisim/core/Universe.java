package de.uni_stuttgart.informatik.canu.mobisim.core;

import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * <p>Patches: </p>
 * <p> v1.2 (23/08/2005): The x-y-z dimemsion of the Universe
 *										 			the time step of the simulation must 
 *										 			be loaded before all sebsequent
 *										 			extensions.
 *										 			A seed tag has been added in order to 
 *										 			be able to feed the RNG.</p> 
 * @author Canu Research group
 * @author v1.2: Jerome Haerri (haerri@ieee.org) 
 * @version 1.2 
 */

/**
 * This class implements the "Universe" object
 * <p>Patches: </p>
 * <p> <i> Version 1.2 by Jerome Haerri (haerri@ieee.org) on 08/23/2005:
 * <br> &nbsp;&nbsp;&nbsp;&nbsp; The x-y-z dimemsion of the Universe
 *										 			the time step of the simulation must 
 *										 			be loaded before all sebsequent
 *										 			extensions.
 *										 			A seed tag has been added in order to 
 *										 			be able to feed the RNG.</i></p>
 * @author 1.0-1.1 Illya Stepanov
 * @author 1.0 1.1 Gregor Schiele
 * @author 1.2 Jerome Haerri
 * @version 1.2
 */
public final class Universe extends ExtendableObject
                            implements NotificationListener
{
  // Allow only one instance of this class
  /**
   * Global instance of the object
   */
  private static Universe ui=new Universe();

  /**
   * Gets a reference to the Universe object. <br>
   * <br>
   * @return reference to the Universe object
   */
  public static final Universe getReference()
  {
    return ui;
  }

  /**
   * Duration of single simulation step (in ms)
   */
  private int stepDuration = 1;             //in ms
  /**
   * X-size of Universe (in m)
   */
  private float dimensionX = 0.0f;          //in m
  /**
   * Y-size of Universe (in m)
   */
  private float dimensionY = 0.0f;          //in m
  /**
   * Z-size of Universe (in m)
   */
  private float dimensionZ = 0.0f;          //in m

  /**
   * Simulation events receivers
   */
  private java.util.Collection listeners;

  /**
   * Current simulation time (in steps)
   */
  private long currentTime;
  /**
   * Random number generator
   */
  private java.util.Random rand;

  /**
   * Array of nodes
   */
  private java.util.ArrayList allNodes;

  /**
   * Constructor
   */
  private Universe()
  {
    u = this;
    
    allNodes        = new java.util.ArrayList();
    rand            = new java.util.Random();
    listeners       = new java.util.ArrayList();
  }

  /**
   * Performs the modules' initialization
   */
  public void initialize()
  {
    super.initialize();

    // initialize mobile nodes
    for (int i=0; i<allNodes.size(); i++)
    {
      ((ExtendableObject)allNodes.get(i)).initialize();
    }
    
    u.sendNotification(new DebugNotification(this, u,
      "Dimensions = ("+getDimensionX()+" m x "+
      getDimensionY()+" m x "+getDimensionZ()+" m)"));
    u.sendNotification(new DebugNotification(this, u,
      "Simulation step = "+stepDuration+" ms"));

    // output positions of mobile devices
    for (int i=0; i<allNodes.size(); i++)
    {
      u.sendNotification(new StartingPositionSetNotification(allNodes.get(i), u));
    }
  }

  /**
   * Adds a simulation events receiver. <br>
   * <br>
   * @param listener new event listener
   */
  public void addNotificationListener(NotificationListener listener)
  {
    listeners.add(listener);
  }

  /**
   * Removes a simulation events receiver. <br>
   * <br>
   * @param listener event listener
   */
  public void removeNotificationListener(NotificationListener listener)
  {
    listeners.remove(listener);
  }

  /**
   * Notification passing method. <br>
   * <br>
   * Method is used to pass simulation notifications
   * to listeners. Listener can be registered using the
   * {@link #addNotificationListener addNotificationListener}
   * method.
   * The simulation environment sends the notification to all
   * registered listeners.
   * @param notification notification
   */
  public void sendNotification(Notification notification)
  {
    java.util.Iterator iter = listeners.iterator();
    while (iter.hasNext())
    {
      NotificationListener listener = (NotificationListener)iter.next();
      listener.sendNotification(notification);
    }
  }

  /**
   * Gets an array of nodes. <br>
   * <br>
   * @return array of nodes
   */
  public final java.util.ArrayList getNodes()
  {
    return allNodes;
  }

  /**
   * Searches a node by id. <br>
   * <br>
   * @param id id of node
   * @return node, if found, null otherwise
   */
  public final Node getNode(String id)
  {
    java.util.Iterator iter=allNodes.iterator();
    while (iter.hasNext())
    {
      Node node=(Node)iter.next();
      if (node.id.equals(id))
        return node;
    }

    return null;
  }

  /**
   * Adds a node to simulation. <br>
   * <br>
   * @param aNode node to be added
   */
  public void addNode(Node aNode)
  {
    allNodes.add(aNode);
    sendNotification(new NodeAddedNotification(this, this, aNode));
  }

  /**
   * Removes a node from simulation. <br>
   * <br>
   * @param aNode node to be removed
   */
  public void removeNode(Node aNode)
  {
    allNodes.remove(aNode);
    sendNotification(new NodeRemovedNotification(this, this, aNode));
  }

  /**
   * Gets a reference to the random number generator object. <br>
   * <br>
   * @return reference to the random number generator object
   */
  public final java.util.Random getRandom()
  {
    return rand;
  }

  /**
   * Gets the X-dimension of Universe. <br>
   * <br>
   * @return X-dimension of Universe (in m)
   */
  public float getDimensionX()
  {
    return dimensionX;
  }

  /**
   * Sets the X-dimension of Universe. <br>
   * <br>
   * @param dimensionX X-dimension of Universe (in m)
   */
  public void setDimensionX(float dimensionX)
  {
    this.dimensionX = dimensionX;
  }

  /**
   * Gets the Y-dimension of Universe. <br>
   * <br>
   * @return Y-dimension of Universe (in m)
   */
  public float getDimensionY()
  {
    return dimensionY;
  }

  /**
   * Sets the Y-dimension of Universe. <br>
   * <br>
   * @param dimensionY Y-dimension of Universe (in m)
   */
  public void setDimensionY(float dimensionY)
  {
    this.dimensionY = dimensionY;
  }

  /**
   * Gets the Z-dimension of Universe. <br>
   * <br>
   * @return Z-dimension of Universe (in m)
   */
  public float getDimensionZ()
  {
    return dimensionZ;
  }

  /**
   * Sets the Z-dimension of Universe. <br>
   * <br>
   * @param dimensionZ Z-dimension of Universe (in m)
   */
  public void setDimensionZ(float dimensionZ)
  {
    this.dimensionZ = dimensionZ;
  }

  /**
   * Gets a duration of single simulation time step. <br>
   * <br>
   * @return duration of single simulation time step (in ms)
   */
  public final int getStepDuration()
  {
    return stepDuration;
  }

  /**
   * Gets current time (in steps). <br>
   * <br>
   * @return current time (in steps)
   */
  public long getTimeInSteps()
  {
    return currentTime;
  }


  /**
   * Gets current time (in ms). <br>
   * <br>
   * @return current time (in ms)
   */
  public long getTime()
  {
    return currentTime*stepDuration;
  }

  /**
   * Gets a readable time representation in ms. <br>
   * <br>
   * @return readable time representation (in ms)
   */
  public String getTimeAsString()
  {
    return timeToString(currentTime);
  }

  /**
   * Converts time from steps to readable representation in ms. <br>
   * <br>
   * @return readable time representation (in ms)
   */
  public String timeToString(long time)
  {
    return ""+ (float)getTime()/1000.0+" s";
  }


  /**
   * Executes a single simulation timestep. <br>
   * <br>
   * Performs execution of the exetensions and nodes,
   * increments simulation time.
   */
  public void advanceTime()
  {
    // execute extensions
    super.act();

    // execute mobile nodes
    for (int i=0, n=allNodes.size(); i<n; i++)
    {
      ((Node)allNodes.get(i)).act();
    }

    currentTime++;
  }
	
	/**
    * Re-Initializes all variables. <br>
    * <br>
    * Resets current time, remove all nodes, 
		* deletes extensions, and resets the random number generator
		* @since 1.2
    */
	public void flush() {
	  currentTime = 0;
		allNodes.clear();
		getExtensions().clear();
    rand = new java.util.Random();
    listeners.clear();
		
		
	}
  /**
    * Initializes the object from XML tag. <br>
		* <br>
		* <i>Version 1.2 by Jerome Haerri (haerri@ieee.org) : 
		* <br> &nbsp;&nbsp;&nbsp;&nbsp; - A seed extention has been added
		*	<br> &nbsp;&nbsp;&nbsp;&nbsp; - x-y-z dim + step + seed are loaded before all subsequent extensions.</i>
    * <br>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
  public void load(org.w3c.dom.Element element) throws Exception
  {
		
		//JHNote (23/08/2005) : Patch here in order to do some preloading
		//										  We think that the x-y-z dim + step of the Universe
		//										  should be loaded before all subsequent extensions.
		//										  Moreover, the seed must be set before any 
		//											subsequent load.
		org.w3c.dom.NodeList list = element.getChildNodes();
    int len=list.getLength();
		
		u.sendNotification(new LoaderNotification(this, u,
          "Preprocessing tags"));
    for(int i=0; i<len; i++)
    {
      org.w3c.dom.Node item = list.item(i);
      String tag = item.getNodeName();

      if(tag.equals("#text")) {
        // skip it
        continue;
      }
      else
      if(tag.equals("#comment")) {
        // skip it
        continue;
      }
      else
     	if(tag.equals("seed")) {
				u.sendNotification(new LoaderNotification(this, u,
          "Processing <seed> tag"));
				long seed = (Long.parseLong(item.getFirstChild().getNodeValue()));
				if (seed > 0) {
					//System.out.println("Setting seed to " + seed);
					u.getRandom().setSeed(seed);
				}
				u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <seed> tag"));
			}
			else
      if(tag.equals("step")) {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <step> tag"));

        stepDuration=(int)(Float.parseFloat(item.getFirstChild().
          getNodeValue())*1000);

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <step> tag"));
      }
      else
      if(tag.equals("dimx")) {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <dimx> tag"));

        setDimensionX(Float.parseFloat(item.getFirstChild().getNodeValue()));

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <dimx> tag"));
      }
      else
      if(tag.equals("dimy")) {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <dimy> tag"));

        setDimensionY(Float.parseFloat(item.getFirstChild().getNodeValue()));

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <dimy> tag"));
      }
      else
      if(tag.equals("dimz")) {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <dimz> tag"));

        setDimensionZ(Float.parseFloat(item.getFirstChild().getNodeValue()));

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <dimz> tag"));
      }
		}
		
		// checkout
    if (stepDuration<=0)
      throw new Exception("Simulation step is invalid: "+
        (float)stepDuration/1000);
    if (getDimensionX()<0.0f)
      throw new Exception("Simulation parameter dimX is invalid: "+getDimensionX());
    if (getDimensionY()<0.0f)
      throw new Exception("Simulation parameter dimY is invalid: "+getDimensionY());
    if (getDimensionZ()<0.0f)
      throw new Exception("Simulation parameter dimZ is invalid: "+getDimensionZ());
    if (getDimensionX()+getDimensionY()+getDimensionZ() == 0.0f)
      throw new Exception("Simulation area dimensions are null");
		
		u.sendNotification(new LoaderNotification(this, u,
          "Finished preprocessing tags"));
		
    super.load(element);
		
		u.sendNotification(new LoaderNotification(this, u,
          "Postprocessing tags"));
   /* org.w3c.dom.NodeList list = element.getChildNodes();
    int len=list.getLength();*/

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
      if(tag.equals("node"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <node> tag"));

        // read and add a mobile node
        org.w3c.dom.Element e = (org.w3c.dom.Element)item;

        String classTag = e.getAttribute("class");
        if (classTag.length()==0)
          classTag = Node.class.getName();

        Node node=(Node)Class.forName(classTag).newInstance();
        node.load(e);

        addNode(node);

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <node> tag"));
      }
      else
      if(tag.equals("nodegroup"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <nodegroup> tag"));

        org.w3c.dom.Element e = (org.w3c.dom.Element)item;

        // number of nodes in the group
        int n = Integer.parseInt(e.getAttribute("n"));

        // group id
        String g_id = e.getAttribute("id");
        
        // class of nodes
        String classTag = e.getAttribute("class");
        if (classTag.length()==0)
          classTag = Node.class.getName();

        for (int n_i=0; n_i<n; n_i++)
        {
          u.sendNotification(new LoaderNotification(this, u,
            "Creating mobile node"));
          
          Node node=(Node)Class.forName(classTag).newInstance();
          e.setAttribute("id", g_id+"#"+n_i);
          node.load(e);

          addNode(node);
          
          u.sendNotification(new LoaderNotification(this, u,
            "Finished creating mobile node"));
        }

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <nodegroup> tag"));
      }
      /*else
      if(tag.equals("step"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <step> tag"));

        stepDuration=(int)(Float.parseFloat(item.getFirstChild().
          getNodeValue())*1000);

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <step> tag"));
      }
      else
      if(tag.equals("dimx"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <dimx> tag"));

        setDimensionX(Float.parseFloat(item.getFirstChild().getNodeValue()));

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <dimx> tag"));
      }
      else
      if(tag.equals("dimy"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <dimy> tag"));

        setDimensionY(Float.parseFloat(item.getFirstChild().getNodeValue()));

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <dimy> tag"));
      }
      else
      if(tag.equals("dimz"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <dimz> tag"));

        setDimensionZ(Float.parseFloat(item.getFirstChild().getNodeValue()));

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <dimz> tag"));
      }*/
    }

		u.sendNotification(new LoaderNotification(this, u,
          "Finished postprocessing tags"));
		
   /* // checkout
    if (stepDuration<=0)
      throw new Exception("Simulation step is invalid: "+
        (float)stepDuration/1000);
    if (getDimensionX()<0.0f)
      throw new Exception("Simulation parameter dimX is invalid: "+getDimensionX());
    if (getDimensionY()<0.0f)
      throw new Exception("Simulation parameter dimY is invalid: "+getDimensionY());
    if (getDimensionZ()<0.0f)
      throw new Exception("Simulation parameter dimZ is invalid: "+getDimensionZ());
    if (getDimensionX()+getDimensionY()+getDimensionZ() == 0.0f)
      throw new Exception("Simulation area dimensions are null");*/
  }//proc
}
