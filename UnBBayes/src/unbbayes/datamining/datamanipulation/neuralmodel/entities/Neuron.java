package unbbayes.datamining.datamanipulation.neuralmodel.entities;

import java.io.*;

/**
 *  Abstract class that defines how a neuron must be.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public abstract class Neuron implements Serializable{
  /**Key that identifies solely a neuron*/
  protected String key;

  /**
   * Outputs the unique key that identifies the neuron.
   *
   * @return the unique key
   */
  public String getKey(){
    return key;
  }

  /**
   * Used to prunne the network
   *
   * @param key the key
   */
  public abstract void prunning(String key);

  /**
   * Used to verify if the neuron has been activated.
   *
   * @return <code>true</code> if the neuron has been activated;
   *         <code>false</code> otherwise.
   */
  public abstract boolean getSignal();
}