package unbbayes.datamining.datamanipulation.neuralmodel.entities;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Arc {
  public int accumulator = 1;
  public int weigth;
  public Neuron combinatorialNeuron;
  public Arc(Neuron combNeuron){
    combinatorialNeuron = combNeuron;
  }
}