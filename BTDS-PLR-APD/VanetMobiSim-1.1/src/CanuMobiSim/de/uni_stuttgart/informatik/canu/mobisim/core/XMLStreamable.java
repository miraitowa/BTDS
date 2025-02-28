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
 * Interface for loading the object from XML tag
 * @author Illya Stepanov
 */
public interface XMLStreamable
{
  /**
    * Initializes the object from XML tag. <br>
    * <br>
    * @param element source tag
    * @throws Exception Exception if parameters are invalid
    */
  void load(org.w3c.dom.Element element) throws Exception;
}