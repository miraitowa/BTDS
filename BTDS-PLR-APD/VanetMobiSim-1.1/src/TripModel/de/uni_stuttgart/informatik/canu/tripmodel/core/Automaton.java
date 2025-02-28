package de.uni_stuttgart.informatik.canu.tripmodel.core;

import de.uni_stuttgart.informatik.canu.mobisim.core.Universe;
import de.uni_stuttgart.informatik.canu.mobisim.notifications.DebugNotification;

/**
 * <p>Title: Trip Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002-2003</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.3
 */

/**
 * This class implements an automaton of activity sequences
 * @author Illya Stepanov
 */
public class Automaton implements Cloneable
{
  /**
   * Automaton's states
   */
  protected java.util.ArrayList states = new java.util.ArrayList();

  /**
   * Automaton's transition matrix
   */
  protected float[][] matrix;

  /**
   * Index of the current state
   */
  protected int currentState;

  /**
   * Constructor
   */
  public Automaton()
  {
    currentState = 0;
  }

  /**
   * Gets an index of the specified state from array of states. <br>
   * <br>
   * @param state state
   * @return index of state
   */
  protected int getStateIndex(State state)
  {
    for (int i=0; i<states.size(); i++)
    {
      State s = (State)states.get(i);
      if (s==state)
        return i;
    }

    return -1;
  }

  /**
   * Gets all automaton's states. <br>
   * <br>
   * @return automaton's states
   */
  public java.util.ArrayList getStates()
  {
    return states;
  }

  /**
   * Gets the automaton's transition matrix. <br>
   * <br>
   * @return automaton's transition matrix
   */
  public float[][] getTransitionMatrix()
  {
    return matrix;
  }

  /**
   * Adds a new state to the automaton. <br>
   * <br>
   * @param state new state of the automaton
   */
  public void addState(State state)
  {
    states.add(state);
  }

  /**
   * Adds a new transition between the states. <br>
   * <br>
   * @param source source state
   * @param dest destination state
   * @param p transition probability
   */
  public void addTransition(State source, State dest, float p)
  {
    if ((matrix==null)||(matrix.length<states.size()))
    {
      // resize the matrix
      float[][] newMatrix = new float[states.size()][states.size()];

      // copy the source matrix
      if (matrix!=null)
        for (int i=0; i<matrix.length; i++)
          System.arraycopy(matrix[i], 0, newMatrix[i], 0, matrix.length);

      matrix = newMatrix;
    }

    matrix[getStateIndex(source)][getStateIndex(dest)] = p;
  }

  /**
   * Gets the current state of automaton. <br>
   * <br>
   * @return current state of the automaton
   */
  public State getCurrentState()
  {
    return (State)states.get(currentState);
  }

  /**
   * Switches the automaton to the next state
   */
  public void switchToNextState()
  {
    Universe u = Universe.getReference();
    java.util.Random rand = u.getRandom();

    float alpha = rand.nextFloat();

    for (int i=1; i<=states.size(); i++)
    {
      int ind = (currentState+i)%states.size();
      float f = matrix[currentState][ind];
      if (f==0.0f)
        continue;
      if (alpha<=f)
      {
        currentState = ind;
        break;
      }

      alpha-=f;
    }
    
    String stateID = getCurrentState().getID();
    if (stateID==null)
      stateID="";
    
    u.sendNotification(new DebugNotification(this, u, "Automaton switched to state "+currentState+" ("+stateID+")"));
  }

  /**
   * Checks if the state is final. <br>
   * <br>
   * @param state state
   * @return true if the state is final
   */
  public boolean isFinalState(State state)
  {
    // the transition probability from the final state is zero
    int state_ind = getStateIndex(state);
    float f = 0.0f;
    for (int i=0; i<states.size(); i++)
    {
      f+=matrix[state_ind][i];
    }
    
    return (f==0.0f);
  }

  /**
   * Creates a clone of the automaton
   */
  public Object clone()
  {
    Object o = null;
    try
    {
      o = super.clone();
    }
    catch(CloneNotSupportedException e)
    {
      e.printStackTrace(System.err);
      System.err.println("Simulation aborted");
      System.exit(1);
    }
    return o;
  }
}