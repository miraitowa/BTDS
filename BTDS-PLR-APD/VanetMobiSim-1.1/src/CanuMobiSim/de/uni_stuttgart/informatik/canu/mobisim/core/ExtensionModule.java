package de.uni_stuttgart.informatik.canu.mobisim.core;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

/**
 * This class is a base class for extension modules.
 * @author Illya Stepanov
 */
public abstract class ExtensionModule implements XMLStreamable,
                                                 NotificationListener
{
  /**
   * Extendable object owning the module
   */
  protected ExtendableObject owner;
  
  /**
   * Unique extension's name
   */
  protected String name;

  /**
   * Reference to the global instance of Universe object
   */
  protected Universe u;

  /**
   * Constructor
   */
  public ExtensionModule()
  {
    u = Universe.getReference();    
  }

  /**
   * Constructor. <br>
   * <br>
   * @param name unique extension's module name
   */
  public ExtensionModule(String name)
  {
    this();
    this.name = name;
  }

  /**
   * Constructor. <br>
   * <br>
   * @param owner owner of extension module
   * @param name unique extension module's name
   */
  public ExtensionModule(ExtendableObject owner, String name)
  {
    this(name);
    this.owner = owner;
  }

  /**
   * Returns the unique extension module's name. <br>
   * <br>
   * @return extension module's name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the owner of extension module. <br>
   * <br>
   * @return owner of extension module
   */
  public ExtendableObject getOwner()
  {
    return owner;
  }

  /**
   * Returns the module's description. <br>
   * <br>
   * @return extension module's description
   */
  public abstract String getDescription();

  /**
   * Performs the module initialization. <br>
   * <br>
   * The method is called after finishing the scenario file processing.
   */
  public void initialize()
  {
  }

  /**
   * Executes the extension. <br>
   * <br>
   * The method is called on every simulation timestep.
   * @return 0 - the module should be executed on next timesteps,
   *        -1 - the module should not be executed on further timesteps and should be removed from the extensions' list
   */
  public abstract int act();

  /**
   * Notification passing method. <br>
   * <br>
   * @param notification notification
   */
  public void sendNotification(Notification notification)
  {
  }

  /**
    * Loads the object from XML tag. <br>
    * <br>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    String newName = element.getAttribute("name");
    if (newName.length()>0)
      name = newName;

    if(name==null)
      throw new Exception("Missing extension's name");
  }
}
