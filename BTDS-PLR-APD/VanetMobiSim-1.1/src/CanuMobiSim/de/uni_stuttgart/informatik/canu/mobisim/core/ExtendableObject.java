package de.uni_stuttgart.informatik.canu.mobisim.core;

import de.uni_stuttgart.informatik.canu.mobisim.notifications.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

/**
 * This class is a base class for objects that contain extensions.
 * @author Illya Stepanov
 */
public class ExtendableObject implements XMLStreamable,
                                         NotificationListener
{
  /**
   * Reference to the global instance of Universe object
   */
  protected Universe u;
  
  /**
   * List of extension modules
   */
  protected java.util.ArrayList extensions = new java.util.ArrayList();

  /**
   * Constructor
   */
  public ExtendableObject()
  {
    u = Universe.getReference();
  }

  /**
   * Performs the module initialization. <br>
   * <br>
   * The method is called after finishing the scenario file processing.
   */
  public void initialize()
  {
    // initialize extensions
    for (int i=0; i<extensions.size(); i++)
    {
      ((ExtensionModule)extensions.get(i)).initialize();
    }
  }

  /**
   * Executes all extensions
   */
  public void act()
  {
    java.util.Iterator iter = extensions.iterator();
    while (iter.hasNext())
    {
      ExtensionModule extension = (ExtensionModule)iter.next();
      if (extension.act()==-1)
        iter.remove();  // remove the extension
    }
  }

  /**
   * Gets the extension with the given name. <br>
   * <br>
   * @param name extension's name
   * @return extension module, if found, null otherwise
   */
  public ExtensionModule getExtension(String name)
  {
    java.util.Iterator iter = extensions.iterator();
    while (iter.hasNext())
    {
      ExtensionModule module = (ExtensionModule)iter.next();
      if (module.getName().equals(name))
        return module;
    }

    return null;
  }
  
  /**
   * Gets the list of registered extensions. <br>
   * <br>
   * @return the list of registered extensions
   */
  public java.util.ArrayList getExtensions()
  {
    return extensions;
  }

  /**
   * Passes a notification to the module. <br>
   * <br>
   * @param notification notification
   */
  public void sendNotification(Notification notification)
  {
  }

  /**
    * Initializes the object from XML tag. <br>
    * <br>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
  public void load(org.w3c.dom.Element element) throws Exception
  {
    org.w3c.dom.NodeList list = element.getChildNodes();
    int len=list.getLength();

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
      if(tag.equals("extension"))
      {
        u.sendNotification(new LoaderNotification(this, u,
          "Processing <extension> tag"));

        // read and add the child node
        org.w3c.dom.Element e = (org.w3c.dom.Element)item;

        String classTag = e.getAttribute("class").trim();
        ExtensionModule module=(ExtensionModule)Class.forName(classTag).
          newInstance();
        module.owner = this;

        u.sendNotification(new LoaderNotification(this, u,
          "Loading class "+classTag));

        module.load(e);

        u.sendNotification(new LoaderNotification(this, u,
          "Finished loading class "+classTag));

        extensions.add(module);

        u.sendNotification(new LoaderNotification(this, u,
          "Finished processing <extension> tag"));
      }
    }
  }//proc
}
