import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import javax.xml.parsers.*;

/**
 * Title:        Canu Mobility Simulation Environment
 * Description:
 * Copyright:    Copyright (c) 2001-2003
 * Company:      University of Stuttgart
 * @author Canu Research group
 * @version 1.1
 */

/**
 * This class launches the simulation environment.
 * @author Illya Stepanov
 * @author Gregor Schiele
 */
public class Runner
{
  // global variables
  /**
   * Scenario file's name
   */
  static String modelSource;

  /**
   * Constructor
   */
  public Runner()
  {
    Universe u=Universe.getReference();

    if (!loadModel())
      return;

    u.initialize();

    // advance time in loop
    for (;;)
      u.advanceTime();

    // an extension has to stop us ...
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

    try
    {
      // parse the scenario
        
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();

      org.w3c.dom.Document document = builder.parse(new java.io.FileInputStream(modelSource));

      org.w3c.dom.Element root=document.getDocumentElement();

      String rootTag=root.getNodeName();
      if (!rootTag.equals("universe"))
        throw new Exception("Invalid parent tag: "+rootTag);

      u.load(root);
    }
    catch(Exception e)
    {
      System.err.println("Error loading model from "+modelSource);
      e.printStackTrace(System.err);
      retval=false;
    }

    return retval;
  }

  //----------------------------------------------------------------------------
  //----------------------------------------------------------------------------
  public static void main(String[] args)
  {
    if ( (args.length!=1)||(args[0].equals("-help")) )
    {
      System.out.println("Usage: Runner modelSource.xml\n");
      return;
    }

    modelSource=args[0];

    new Runner();
  }//endproc

}//endclass
