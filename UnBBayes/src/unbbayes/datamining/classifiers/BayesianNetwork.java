package unbbayes.datamining.classifiers;

import java.util.Enumeration;
import java.util.HashMap;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.TreeVariable;

/**
 * Class implementing a Bayesian Network.
 *
 * @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (17/02/2002)
 */
public class BayesianNetwork extends DistributionClassifier
{
  /** Atributo classe do conjunto de dados */
  private Attribute classAttribute;
  /** N� classe na rede bayesiana */
  private ProbabilisticNode classNode;
  /** �ndice da classe na rede vayesiana */
  private int classNodeIndex = -1;
  /** �ndice da classe no conjunto de dados */
  private int classAttributeIndex = -1;
  /** Refer�ncia para rede bayesiana usada nesta classe */
  private ProbabilisticNetwork net;
  /** Refer�ncia para o conjunto de inst�ncias usado nesta classe */
  private InstanceSet instanceSet;
  /** N�mero de n�s na rede bayesiana */
  private int numNodes;
  /** Mapeamento dos n�s da rede bayesiana para os atributos do conjunto de dados */
  private int[] indexAttributes;
  /** Guarda as distribui��es de probabilidades j� calculadas.
   * Usado para otimiza��o.
   */
  private HashMap<Integer, float[]> hashMap;
  /** Multiplicadores para otimiza��o com hashMap */
  private int[] multipliers;
  /** Guarda o n�mero de classes */
  private int numClasses = 0;

	public BayesianNetwork(ProbabilisticNetwork net,InstanceSet instanceSet) throws Exception
	{
          this.net = net;
          this.instanceSet = instanceSet;
          this.net.compile();
          numNodes = net.getNodeCount();
          multipliers = new int[numNodes-1];
          indexAttributes = new int[instanceSet.numAttributes()];
          Enumeration enumeration = instanceSet.enumerateAttributes();
          int i = 0;
          String attributeName;
          while (enumeration.hasMoreElements())
          {
            attributeName = ((Attribute)enumeration.nextElement()).getAttributeName();
            indexAttributes[i] = net.getNodeIndex(attributeName);
            if (indexAttributes[i] == -1)
            {
              throw new Exception("Atributo n�o encontrado na rede: "+attributeName);
            }
            i++;
          }
	}

	private boolean compareClasses(Attribute classAttribute,ProbabilisticNode node)
	{	boolean equals = false;
		if (((classAttribute.getAttributeName().compareToIgnoreCase(node.getName())) == 0) &&
                    (classAttribute.numValues() <= node.getStatesSize()))
                {	classNode = node;
			equals = true;
		}
		return equals;
	}

	/**
   	* Generates the classifier.
   	*
   	* @param instances Set of instances serving as training data
   	* @exception Exception if the classifier has not been generated successfully
   	*/
  	public void buildClassifier(InstanceSet instances) throws Exception
	{	}

  	/**
  	 * Calculates the class membership probabilities for the given test instance.
  	 *
  	 * @param instance the instance to be classified
  	 * @return predicted class probability distribution
  	 * @exception Exception if distribution can't be computed
  	 */
  	public float[] distributionForInstance(Instance instance) throws Exception
	{
            float[] probs = new float[numClasses];
            short instanceValue;

            if (classNodeIndex < 0)
            {
              throw new Exception("Classe n�o definida.");
            }

            // Calcula um hashCode para a inst�ncia
            int j,i=1,hashCode=0,k=0;
            for (j=0; j<numNodes; j++)
            {
              if (j != classAttributeIndex)
              {
                 instanceValue = instance.getValue(j);
                 if (instanceValue != Instance.MISSING_VALUE)
                 {
                   hashCode+=instanceValue*multipliers[k];
                   k++;
                 }
                 else
                 {
                   int numValues = instance.getDataset().getAttribute(j).numValues();
                   hashCode+=numValues*multipliers[k];
                   k++;
                 }

              }
            }

            Integer hashInt = new Integer(hashCode);
            /** Se a inst�ncia j� tiver sido propagada, a c�pia dela retornada */
            if (hashMap.containsKey(hashInt))
            {
              //float[] hashProbs = (float[])hashMap.get(hashInt);
              probs = hashMap.get(hashInt);
              //System.arraycopy(hashProbs,0,probs,0,hashProbs.length);
            }
            /** Sen�o a inst�ncia � propagada e inserida no HashMap*/
            else
            {
              net.initialize();

              for (j=0; j<numNodes; j++)
              {
                int actualNode = indexAttributes[j];
                if (actualNode != classNodeIndex)
                {
                  instanceValue = instance.getValue(j);
                  if (instanceValue != Instance.MISSING_VALUE)
                  {
                    ((TreeVariable)net.getNodeAt(actualNode)).addFinding(instanceValue);
                  }
                }
              }
              net.updateEvidences();
              for (j=0;j<numClasses;j++)
              {
                probs[j] = classNode.getMarginalAt(j);
              }
              hashMap.put(hashInt,probs);
            }
            return probs;
  	}

	/**
   	* Returns a description of the classifier.
   	*
   	* @return a description of the classifier as a string.
   	*/
	public String toString()
	{	try
		{	StringBuffer text = new StringBuffer("Bayesian Network\n");
      		        for (int i=0; i<numNodes; i++)
			{	ProbabilisticNode node = (ProbabilisticNode)net.getNodeAt(i);
				text.append(node+"\n");
			}
			return text.toString();
    	        }
		catch (Exception e)
		{	return resource.getString("exception5");
    	        }
	}

	public Attribute getClassAttribute()
	{   return classAttribute;
	}

        public ProbabilisticNode getClassNode()
        {   return classNode;
        }

	public void setClassAttribute(Attribute classAttribute) throws Exception
	{
          int i,j;
          // Procura pela classe
          boolean result = false;
          for (i=0; i<numNodes; i++)
          {
            result = compareClasses(classAttribute,(ProbabilisticNode)net.getNodeAt(i));
            if (result == true)
            {
              this.classAttribute = classAttribute;
              classNodeIndex = net.getNodeIndex(classAttribute.getAttributeName());
              classAttributeIndex = classAttribute.getIndex();

              numClasses = classNode.getStatesSize();
              originalDistribution = new float[numClasses];
              for (j=0; j < numClasses; j++)
              {
                originalDistribution[j] = (float)classNode.getMarginalAt(j);
              }
              break;
            }
          }
          // se a classe n�o for encontrada
          if (result == false)
          {
            throw new Exception("Attributo classe n�o encontrado na rede");
          }
          else
          {
            // Inicializa com a capacidade inicial 1500 para conter pelo menos 1000 elementos sem precisar fazer o rehash().
            hashMap = new HashMap<Integer, float[]>(1500);
            // Calcula os multiplicadores para calcular o hashCode
            int k=0;
            i=1;
            for (j=0; j<numNodes; j++)
            {
              	if (j != classAttributeIndex)
              	{
                    multipliers[k]=i;
                    i*=(instanceSet.getAttribute(j).numValues()+1);
                    k++;
              	}
            }
          }
	}

        public void resetNet()
        {
          try
          {
            net.initialize();
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
}