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
  protected int accumulator;
  protected int weigth;
  protected float reliability;
  protected Neuron combinationNeuron;

  public Arc(Neuron combinationNeuron){
    this.combinationNeuron = combinationNeuron;
    this.accumulator = 1;
  }

  public Arc(Neuron combinationNeuron, int accumulator){
    this.combinationNeuron = combinationNeuron;
    this.accumulator = accumulator;
  }

  public Neuron getCombinationNeuron(){
    return combinationNeuron;
  }

  public float getReliability(){
    return reliability;
  }

  public int getWeigth(){
    return weigth;
  }

  public int getAccumulator(){
    return accumulator;
  }

  public void setWeight(int weigth){
    this.weigth = weigth;
  }
}