package unbbayes.datamining.classifiers.neuralnetwork;

public abstract class Neuron {

  float[] weights;
  ActivationFunction activationFunction;

  public abstract float outputValue();

//  public abstract void updateWeights(float learningRate);

  public abstract float getErrorTerm();

  void startWeights(){
    double weight;
    for(int i=0; i<weights.length; i++){
      weight = Math.random();
      weights[i] = (float)(weight - 0.5);
    }
  }


  //////////////////////////////////
  public void printWeights(){
    for(int i=0; i<weights.length; i++){
      System.out.println(i + " :" + weights[i]);
    }

  }
  ////////////////////////////////

}