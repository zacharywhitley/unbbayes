package unbbayes.datamining.datamanipulation;

import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.jprs.jbn.*;

public class ComputeNaiveBayes
{	public void setProbabilisticNetwork(ProbabilisticNetwork net) throws Exception
	{	int numNodes = net.getNos().size();
		for (int i=0; i<numNodes; i++)
		{	classNode = (ProbabilisticNode)net.getNodeAt(i);
			if (classNode.getParents().size() == 0)
			{	createPriors(classNode);	
			}	
		}
		numClasses = classNode.getStatesSize();
		counts = new double[numClasses][numNodes-1][1];
		for (int i=0; i<numNodes; i++)
		{	ProbabilisticNode node = (ProbabilisticNode)net.getNodeAt(i);
			if (node.getParents().size() != 0)
			{	createCounts(node);				
			}
		}	
		naiveBayes.setCounts(counts);	
	}
	
	private void createPriors(ProbabilisticNode classNode)
	{	PotentialTable tab = classNode.getPotentialTable();
		int num = classNode.getStatesSize();
		double[] priors = new double[num];
      	for (int i=0;i<num;i++)
      	{	priors[i] = tab.getValue(i);
      	}	
		naiveBayes.setPriors(priors);		
	}
	
	private void createCounts(ProbabilisticNode node)
	{	PotentialTable tab = node.getPotentialTable();
		int num = node.getStatesSize();
		
		int i=0,j=0;
      	for (j=0;j<numClasses;j++)
      	{	counts[j][k] = new double[num];
      	}	
		
		int[] coord = new int[numClasses];
      	for (j=0;j<numClasses;j++)
      	{   for (i=0;i<num;i++)
          	{   coord[0] = i;
              	coord[1] = j;
              	counts[j][k][i] = tab.getValue(coord);
          	}
      	}
      	k++;
	}
	
	public NaiveBayes getNaiveBayes()
	{	return naiveBayes;
	}	
	
	private NaiveBayes naiveBayes = new NaiveBayes();
	private ProbabilisticNode classNode;
	private double[][][] counts;
	private int numClasses;
	private int k = 0;
}
