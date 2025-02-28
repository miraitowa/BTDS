package de.uni_stuttgart.informatik.canu.uomm;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;

/**
 * <p>Title: User-Oriented Mobility Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.2
 */

/**
 * This class implements the Constant Speed Movement Behavior
 * with deviations
 * @author Illya Stepanov
 */
public class DeviatedConstantSpeedMotion extends ConstantSpeedMotion
{
  /**
   * Name of a notification listener for the non-deviated movement
   */
  protected String altListenerName;
  /**
   * Notification listener for the non-deviated movement
   */
  protected NotificationListener altListener;
  /**
   * Maximum deviation of a position (m)
   */
  protected float eps = 0.0f;   // in m
  /**
   * Number of intermediate points of a movement
   */
  protected int n_points = 5; 
  /**
   * Original speed (in meters/ms)
   */
  protected float ori_speed = 0.0f;    // in m/ms
  /**
   * Destination of the original movement
   */
  protected Position3D ori_destination;

  /**
   * Constructor
   */
  public DeviatedConstantSpeedMotion()
  {
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Constant Speed Motion Movement Behavior with Deviations";
  }

  /**
   * Performs the modules' initialization
   */
  public void initialize()
  {
    super.initialize();
    
    if (altListenerName!=null)
      altListener = u.getExtension(altListenerName);
  }

  /**
   * Chooses a new destination and movement speed
   */
  protected void chooseNewMovement()
  {
    java.util.Random rand = u.getRandom();
    Node owner = (Node)this.owner;

    if (trip.getPath().size()==0)
    {
      chooseNewPath();
      ori_speed = speed;
    }

    Point p = (Point)trip.getPath().get(0);
    trip.getPath().remove(0);

    destination = p.getPosition();
    
    Position3D ori_position = (ori_destination==null)? owner.getPosition() : ori_destination;
    ori_destination = destination;
        
    // deviate the destination
    if (trip.getPath().size()!=0)
    {
      // calculate deviations
      double dx = rand.nextFloat();
      if (rand.nextBoolean())
        dx = 1-dx;
      else
        dx = dx-1;
      dx*=eps/Math.sqrt(2);
  
      double dy = rand.nextFloat();
      if (rand.nextBoolean())
        dy = 1-dy;
      else
        dy = dy-1;
      dy*=eps/Math.sqrt(2);
  
      // deviate the destination
      double x = destination.getX()+dx;
      double y = destination.getY()+dy;
      
      // check if within the simulation area
      if (x<0)
        x=0;
      else
      if (x>u.getDimensionX())
        x = u.getDimensionX();
  
      if (y<0)
        y=0;
      else
      if (y>u.getDimensionY())
        y = u.getDimensionY();
      
      destination = new Position3D(x, y, 0);
    }
    
    speed = (float)(owner.getPosition().getDistance(destination)/ori_position.getDistance(ori_destination)*ori_speed);

    stay = 0;

    movement = owner.getPosition().getNormalizedDirectionVector(destination).
      mult(speed*u.getStepDuration());

    u.sendNotification(new MovementChangedNotification(this, u,
      destination, speed*1000f));
      
    if (altListener!=null)
    {
      // report the original path
      altListener.sendNotification(new MovementChangedNotification(this,
        altListener, ori_destination, ori_speed*1000f));
    }
  }

  /**
   * Executes the extension.
   */
  public int act()
  {
    if (u.getTimeInSteps()==0)
    {
      // remove the alternative listener from the list, we'll keep him informed directly
      if (altListener!=null)
        u.removeNotificationListener(altListener);
    }

    return super.act();
  }

  /**
   * Initializes the object from XML tag. <br>
   * <br>
   * @param element source tag
   * @throws Exception Exception if parameters are invalid
   */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    u.sendNotification(new LoaderNotification(this, u,
      "Loading DeviatedConstantSpeedMotion extension"));

    super.load(element);

    // process child tags
    org.w3c.dom.NodeList list = element.getChildNodes();
    int len=list.getLength();

    for(int i=0; i<len; i++)
    {
      org.w3c.dom.Node item = list.item(i);
      String tag = item.getNodeName();

      if(tag.equals("alt_listener"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <alt_listener> tag"));

        // read a name of the alternative listener 
        altListenerName=item.getFirstChild().getNodeValue();

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <alt_listener> tag"));
      }
      else
      if(tag.equals("eps"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <eps> tag"));

        // read eps
        eps=Float.parseFloat(item.getFirstChild().getNodeValue());

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <eps> tag"));
      }
      else
      if(tag.equals("n_points"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <n_points> tag"));

         // read and convert a number of intermediate points
        n_points=Integer.parseInt(item.getFirstChild().getNodeValue());

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <n_points> tag"));
      }
    }

    // checkout
    if (eps<0)
      throw new Exception("Invalid <eps> value: "+eps);
    if (n_points<1)
      throw new Exception("Invalid <n_points> value: "+n_points);

    u.sendNotification(new LoaderNotification(this, u,
      "Finished loading DeviatedConstantSpeedMotion extension"));
  }//proc
}
