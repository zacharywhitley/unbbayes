package unbbayes.datamining.classifiers.neuralnetwork;

public class HiddenNeuron extends Neuron{

  private float outputValue;
//  private float net;   ///??????????////verificar se precisa desta variavel
  private float errorTerm;   //sigma

  public HiddenNeuron(ActivationFunction activationFunction, int numberOfInputs){
    this.activationFunction = activationFunction;
    weights = new float[numberOfInputs + 1];
    startWeights();
  }

  public float outputValue(){
    return outputValue;
  }

  public void updateWeights(float learningRate, int[] inputLayer) {
    float deltaW;
//    deltaW[0]

    weights[0] = weights[0] + (learningRate * errorTerm); //bias

    for (int i=1; i<weights.length; i++) {
      deltaW = learningRate * errorTerm * inputLayer[i-1];
      weights[i] = weights[i] + deltaW;
    }
  }

  public float getErrorTerm(){
    return errorTerm;
  }

  public void calculateOutputValue(int[] inputValues){
    float net = weights[0];  //bias value
    for(int i=0; i<inputValues.length; i++){
      net = net + (inputValues[i] * weights[i + 1]);
    }
    outputValue = (float)activationFunction.functionValue(net);
  }

  public void calculateErrorTerm(OutputNeuron[] outputLayer, int neuronIndex){
    float sum = 0;
    for(int i=0; i<outputLayer.length; i++){
      sum = sum + (outputLayer[i].getErrorTerm() * outputLayer[i].getWeight(neuronIndex));
    }
    errorTerm = (float)activationFunction.hiddenErrorTerm(outputValue, sum);
  }
}