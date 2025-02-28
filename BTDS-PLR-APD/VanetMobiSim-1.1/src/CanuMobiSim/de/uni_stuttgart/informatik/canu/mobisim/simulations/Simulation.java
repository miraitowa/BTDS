package de.uni_stuttgart.informatik.canu.mobisim.simulations;

import de.uni_stuttgart.informatik.canu.mobisim.core.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

/**
 * This class is a base class for simulation controlling modules
 * @author Illya Stepanov
 */
abstract public class Simulation extends ExtensionModule
{
  /**
    * Constructor
    */
  public Simulation()
  {
  }

  /**
    * Constructor. <br>
    * <br>
    * @param name extension name
    */
  public Simulation(String name)
  {
    super(name);
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
    if (isFinished())
    {
      printResults();
      System.exit(0);
    }
    
    return 0;
  }


  /**
    * Checks if the simulation is finished (finish condition is met). <br>
    * <br>
    * Derived classes should overwrite this method to perform custom
    * simulation control.
    * <br>
    * @return true if the simulation should finish
    */
  abstract public boolean isFinished();


  /**
    * Displays the simulation results. <br>
    * <br>
    * Derived classes should override this method to display
    * the results.
    */
  abstract public void printResults();


  /**
    * Initializes simulation parameters from XML tag. <br>
    * <br>
    * Derived classes should override this method to perform
    * custom initialization from tag. <br>
    * <br>
    * @throws Exception Exception if parameters are invalid
    */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    super.load(element);
  }
}
