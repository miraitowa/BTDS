package de.uni_stuttgart.informatik.canu.mobisim.simulations;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

/**
 * This class finishes the simulation after elapsing of certain time
 * @author Illya Stepanov
 */
public class TimeSimulation extends Simulation
{
  /**
   * Time when the simulation should finish (in milliseconds)
   */
  long nMillis;

  /**
   * Constructor
   */
  public TimeSimulation()
  {
    super("TimeSimulation");
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public String getDescription()
  {
    return "Time Simulation control module";
  }

  /**
   * Gets time when the simulation should finish. <br>
   * <br>
   * @return time, when simulation should finish
   */
  public long getFinishTime()
  {
    return nMillis;
  }

  /**
   * Checks if the simulation should stop (finish condition is met). <br>
   * <br>
   * @return true if the simulation should stop
   */
  public boolean isFinished()
  {
    long time=u.getTime();
    return (time>=nMillis);
  }

  /**
   * Displays the simulation results
   */
  public void printResults()
  {
  }

  /**
   * Initializes simulation parameters from XML tag. <br>
   * <br>
   * @throws Exception Exception if parameters are invalid
   */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    super.load(element);

    float time=Float.parseFloat(element.getAttribute("param"));
    if (time<0)
      throw new Exception("Invalid time value: "+time);
    nMillis=(long)(time*1000f);
  }
}
