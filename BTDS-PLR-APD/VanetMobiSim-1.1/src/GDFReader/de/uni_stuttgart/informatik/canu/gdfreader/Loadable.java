package de.uni_stuttgart.informatik.canu.gdfreader;

/**
 * <p>Title: GDF Reader</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

/**
 * The interface declares a loadable object
 * @author Illya Stepanov
 */
public interface Loadable
{
  /**
    * Loads the object. <br>
    * <br>
    * @throws Exception Exception if an error occured
    */
  public void load() throws Exception;
}