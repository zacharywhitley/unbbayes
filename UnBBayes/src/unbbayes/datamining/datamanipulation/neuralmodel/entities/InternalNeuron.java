package unbbayes.datamining.datamanipulation.neuralmodel.entities;

/**
 *  Abstract class that defines how an internal neuron (input and
 * combinatorial neuron) must be.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public abstract class InternalNeuron extends Neuron {

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