package unbbayes.datamining.classifiers.neuralnetwork;

public class OutputNeuron extends Neuron{

  private float net;   ///??????????////verificar se precisa desta variavel
  private float outputValue;
  private float instantaneousError;   //????????/verificar se precisa desta variavel
  private float errorTerm;    //sigma

  public OutputNeuron(ActivationFunction activationFunction, int numberOfInputs) {
    this.activationFunction = activationFunction;
    weights = new float[numberOfInputs + 1];
    startWeights();
  }

  public float outputValue(){
    return outputValue;
  }

  public void updateWeights(float learningRate, HiddenNeuron[] hiddenLayer){
    weights[0] = weights[0] + (learningRate * errorTerm);  //bias

    for(int i=1; i<weights.length; i++){
      weights[i] = weights[i] + (learningRate * errorTerm * hiddenLayer[i-1].outputValue());
    }
  }

  public float getErrorTerm(){   //sigma
    return errorTerm;
  }

  public float getWeight(int weightIndex){
    return weights[weightIndex + 1];   //mais um para considerar o bias
  }

  public float calculateOutputValue(HiddenNeuron[] inputs, int expectedOutput){
    net = weights[0];  //bias value
    for(int i=0; i<inputs.length; i++){
      net = net + (inputs[i].outputValue() * weights[i + 1]);
    }
    outputValue = (float)activationFunction.functionValue(net);
    instantaneousError = expectedOutput - outputValue;
    errorTerm = (float)activationFunction.outputErrorTerm(expectedOutput, outputValue);  //calculo de sigma
    return instantaneousError;
  }
}


