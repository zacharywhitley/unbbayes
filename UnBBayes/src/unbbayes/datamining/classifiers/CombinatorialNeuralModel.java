package unbbayes.datamining.classifiers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import unbbayes.datamining.classifiers.cnmentities.Combination;
import unbbayes.datamining.classifiers.cnmentities.OutputNeuron;
import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *  Class that implements the Combinatorial Neural Model (CNM).
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class CombinatorialNeuralModel extends DistributionClassifier implements Serializable{

  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;		
	
  /**The model's combinations.*/
  private HashMap<String,Combination> model = new HashMap<String,Combination>();

  /**Vector that contains the attributes of the training set.*/
  private Attribute[] attributeVector;

  /**Index of the class attribute.*/
  private int classIndex;

  /**Value of the minimum support after prunning.*/
  private int support;

  /**Value of the minimum confidence after prunning.*/
  private int confidence;

  /**Value of the maximum order of combinations allowed.*/
  private int maxOrder;

  /**The set of instances of the training set*/
  private transient InstanceSet instanceSet;

  /**
   * Class constructor
   *
   * @param maxOrder the maximun order of combinations allowed
   */
  public CombinatorialNeuralModel(int maxOrder) {
	this.maxOrder = maxOrder;
  }

  /**
   * Builds the Combinatorial Neural Model classifier (CNM).
 * @param instanceSet The training data
 * @exception Exception if classifier can't be built successfully
   */
  public void buildClassifier(InstanceSet instanceSet) throws Exception{
	this.instanceSet = instanceSet;
	Instance instance;
	Enumeration instanceEnum = instanceSet.enumerateInstances();
	int attributeNum = instanceSet.numAttributes();
	int numOfInstances = instanceSet.numWeightedInstances();

	attributeVector = instanceSet.getAttributes();      //cria um array com os atributos para serialização
	this.classIndex = instanceSet.getClassIndex();      //guarda o indice da classa para serialização

	while(instanceEnum.hasMoreElements()){
	  instance = (Instance)instanceEnum.nextElement();
	  createCombinations(instance, attributeNum, classIndex);
	}
	punishment(numOfInstances);

  /*  ////////////////////////////////////////
	Iterator it = model.values().iterator();
	while(it.hasNext()){
	  Combination c = (Combination)it.next();
	  OutputNeuron[] saida = c.getOutputArray();
	  String out = c.getKey() + " ";
	  for(int i=0; i<saida.length; i++){
		if(saida[i] != null){
		  out = out + " acc:" + saida[i].getAccumulator() + " sup:" + saida[i].getSupport() + " con:" + saida[i].getConfidence();;
		} else {
		  out = out + " nulo ";
		}
	  }
	  System.out.println(out);
	}
	////*//////////////////////////////////////////
  }

  /**
   * Creates the combinations that composes the model.
   *
   * @param instance the instance to be processed
   * @param attributeNum the number of attributes of the instance
   * @param classIndex the index of the class attribute of the instance
   */
  private void createCombinations(Instance instance, int attributeNum, int classIndex){
	int combinationsSize;
	int evidenceNum;
	String[] combinationsKeys;

	int missingAttNum = 0;
	for(int att=0; att<attributeNum; att++){
	  if(instance.isMissing(att)){
		missingAttNum++;
	  }
	}

	evidenceNum = attributeNum - missingAttNum - 1;    // - 1 do attributo classe
	if(evidenceNum < maxOrder){
	  maxOrder = evidenceNum;
	}

	String[] inputKeys = new String[evidenceNum];      //array com as chaves de entrada
	int keyIndex = 0;
	for(int att=0; att<attributeNum; att++){
	  if(!instance.isMissing(att) && (att != classIndex)){
		inputKeys[keyIndex] = generateInputKey(att, instance.getValue(att));
		keyIndex ++;
	  }
	}

	combinationsKeys = makeCombinations(inputKeys);     //cria todas as combinações dos neuronios de entrada
	combinationsSize = combinationsKeys.length;

	for(int i=0; i<combinationsSize; i++){                    //para cada combinaçao gerada
	  //addCombination(combinationsKeys[i], instance.classValue(), instance.getWeight());
	  addCombination(combinationsKeys[i], (int) instance.classValue(), (int)instance.getWeight());
	}
  }

  private void addCombination(String key, int classValue, int weight){
	Combination combination;

	if(!model.containsKey(key)){
	  OutputNeuron[] outputArray = new OutputNeuron[instanceSet.getClassAttribute().numValues()];
	  for(int i=0; i<outputArray.length; i++){
		outputArray[i] = null;
	  }
	  outputArray[classValue] = new OutputNeuron(weight);
	  combination = new Combination(key, outputArray);
	  model.put(key, combination);
	} else {
	  combination = (Combination)model.get(key);
	  combination.increaseAccumulator(classValue, weight);
	}
  }

  /**
   * Creates a unique key for a combinatorial neuron based on the combination
   * that the combinatorial neuron implements.
   *
   * @param inputKeys the list of input keys that represents the combinations
   * @return the generated key
   */
  private String generateCombKey(String[] inputKeys){
	int inputSize = inputKeys.length;
	String combKey = new String();
	for(int i=0; i<inputSize; i++){
	  combKey = combKey + inputKeys[i] + " ";
	}
	return combKey;
  }

  /**
   * Creates a unique key for an input neuron based on it's attribute index
   * and value.
   *
   * @param attribute the index of the attribute in the training set
   * @param value the value of the attribute
   * @return the generated key
   */
  private String generateInputKey(int attribute, float value){
	return new String(attribute + " " + value);
  }

  /**
   * Creates all possible combinations of input neurons with order limited
   * by the maxOrder parameter.
   *
   * @param inputKeys an array with the input keys to be combined
   * @return the generated combinations of input keys
   */
  private String[] makeCombinations(String[] inputKeys){
	String[] keysArray, tempArray;
	String[] combinationsArray;
	ArrayList<String[]> combinations = new ArrayList<String[]>();
	int inputKeysNum = inputKeys.length;
	int combArraySize, tempSize;

	for(int inputNum=0; inputNum<inputKeysNum; inputNum++){  //para todos os neuronios de entrada
		  combArraySize = combinations.size();                   //pega o tamanho do array de combinações
		  for (int j = 0; j < combArraySize; j++) {              //para todas as combinações já existentes
			tempArray = (String[]) combinations.get(j);
			tempSize = tempArray.length;                         //pega o tamanho da combinação

			if (tempSize < maxOrder) {                           //se tamanho da combinação < ordem máxima
			  keysArray = new String[tempSize + 1];              //cria nova combinação
			  keysArray[tempSize] = inputKeys[inputNum];         //adiciona o neuronio de entrada atual
			  System.arraycopy(tempArray, 0, keysArray, 0, tempSize); //copia o resto da combinação atual
			  combinations.add(keysArray);                       //adiciona nova combinação no array de combinacoes
			}
		  }
		  keysArray = new String[1];                             //cria nova combinação de um elemento
		  keysArray[0] = inputKeys[inputNum];                    //coloca o neuronio de entrada atual nesta combinação
	  combinations.add(keysArray);                           //adiciona nova combinação no array de combinacoes
	}

	combArraySize = combinations.size();
	combinationsArray = new String[combArraySize];
	for(int i=0; i<combArraySize; i++){                      //gera as chaves das combinações
	  combinationsArray[i] = generateCombKey((String[])combinations.get(i));
	}

	return combinationsArray;
  }

  /**
   * Punishes the model after the training phase, calculating the final weight
   * of the arcs and calculating the support and confidence.
   */
  private void punishment(int numOfInstances){
	Iterator i = model.values().iterator();
	Combination tempCombination;

	while(i.hasNext()){
	  tempCombination = (Combination)i.next();
	  tempCombination.punish(numOfInstances);
	}
  }

  /**
   * Makes the pruning of the model after the punishment phase based on a
   * determined threshold.
   *
   * @param threshold the minimum weight value that the arcs
   *                  must be prunned
   */
  public void prunning(int threshold){
	Iterator iterator = model.values().iterator();
	Combination tempCombination;
	ArrayList<String> keysArray = new ArrayList<String>();

	while(iterator.hasNext()){
	  tempCombination = (Combination)iterator.next();
	  tempCombination.prunning(threshold);
	  if(tempCombination.isNull()){
		keysArray.add(tempCombination.getKey());
	  }
	}
	int arraySize = keysArray.size();
	for (int i = 0; i < arraySize; i++) {
	  model.remove( (String) keysArray.get(i));
	}
  }

  /**
   * Makes the pruning of the model after the punishment phase based on the
   * minimum support and the minimum confidence of the arcs.
   *
   * @param minSupport the minimum support that the arcs must be prunned
   * @param minConfidence the minimum confidence that the arcs
   *                      must be prunned
   */
  public void prunning(int minSupport, int minConfidence){
	Iterator iterator = model.values().iterator();
	Combination tempCombination;
	ArrayList<String> keysArray = new ArrayList<String>();

	while(iterator.hasNext()){
	  tempCombination = (Combination)iterator.next();
	  tempCombination.prunning(minSupport, minConfidence);
	  if(tempCombination.isNull()){
		keysArray.add(tempCombination.getKey());
	  }
	}
	int arraySize = keysArray.size();
	for(int i=0; i<arraySize; i++){
	  model.remove((String)keysArray.get(i));
	}
  }

  /**
   * Make inference of an instance on the model.
   *
   * @param instance the instance to make the inference
   * @return an array that contains the arc with greater weight of each
   *         output neuron.
   */
  public Combination[] inference(Instance instance){
	int numAtt = attributeVector.length;
	float value;
	String[] instanceKeys;
	String[] combKeys;
	Combination[] combArray = new Combination[attributeVector[classIndex].numValues()];   //array que conterá os arcos de maior peso de cada neuronio
	int missingAttNum = 0;
	Combination tempCombination;
	OutputNeuron[] tempOutput;

	for(int att=0; att<numAtt; att++){
	  if(instance.isMissing(att)){
		missingAttNum++;
	  }
	}

	instanceKeys = new String[numAtt - missingAttNum - 1];   // - 1 do attributo classe
	int keyIndex = 0;
	for(int att=0; att<numAtt; att++){                  //gera um array com as chaves da instancia atual
	  if(att != classIndex && !instance.isMissing(att)){
		value = instance.getValue(att);
		instanceKeys[keyIndex] = generateInputKey(att, value); //cria a chave atributo-valor da entrada
		keyIndex++;
	  }
	}
	combKeys = makeCombinations(instanceKeys);   //gera as chaves de todas as combinações ativadas por esta instancia

	for(int i=0; i<combKeys.length; i++){
	  if(model.containsKey(combKeys[i])){
		tempCombination = (Combination)model.get(combKeys[i]);
		tempOutput = tempCombination.getOutputArray();
		for(int j=0; j<tempOutput.length; j++){
		  if(combArray[j] == null || combArray[j].getOutputNeuron(j) == null){        //testa se o elemento do array de saida é nulo
			combArray[j] = tempCombination;  //se for pega o valor do array temporário
		  } else if(tempOutput[j] != null){  //testa se o elemento do array temporario é nulo
			if(combArray[j].getOutputNeuron(j).getNetWeight() < tempCombination.getOutputNeuron(j).getNetWeight()){  //compara os pesos
			  combArray[j] = tempCombination;  //se o array de saida for menor então pega o temporario.
			}
		  }
		}
	  }
	}
	for(int i=0; i<combArray.length; i++){
	  if(combArray[i] != null && combArray[i].getOutputNeuron(i) == null){
		combArray[i] = null;
	  }
	}
	return combArray;
  }

  /**
   * Make inference of an instance on the model.
   *
   * @param instance the instance to make the inference
   * @return an array of floats with the distribution of values for the given instance.
   * @throws Exception if classifier can't carry through the inference successfully
   */
  public float[] distributionForInstance(Instance instance) throws Exception{
	float[] distribution = null;
	Combination[] outputArray = inference(instance);
	distribution = new float[outputArray.length];
	for(int i=0; i<distribution.length; i++){
	  if(outputArray[i] != null){
		distribution[i] = outputArray[i].getOutputNeuron(i).getNetWeight();
	  } else {
		distribution[i] = 0;
	  }
	}
	return distribution;
  }

  /**
   * Outputs an array of attributes with the attributes of the training set.
   *
   * @return an attribute array.
   */
  public Attribute[] getAttributeVector(){
	return attributeVector;
  }

  /**
   * Outputs the index of the class attribute of the training set.
   *
   * @return the index of the class attribute
   */
  public int getClassIndex(){
	return classIndex;
  }

  /**
   * Outputs the minimum confidence of the model after the prunning phase.
   *
   * @return the minimum confidence.
   */
  public int getConfidence(){
	return confidence;
  }

  /**
   * Outputs the minimum support of the model after the prunning phase.
   *
   * @return the minimum support.
   */
  public int getSupport(){
	return support;
  }

  /**
   * Retuns an iterator conataining the objects that represents
   * the combinatorial neural model, that is, the combinations.
   *
   * @return an iterator of combinations.
   */
  public Iterator getModel(){
	return model.values().iterator();
  }
}