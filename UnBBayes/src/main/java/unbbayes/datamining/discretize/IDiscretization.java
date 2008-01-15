package unbbayes.datamining.discretize;

public interface IDiscretization
{	//public InstanceSet discretization(
}
/*public class Discretization
{	private InstanceSet instances;
	/** Frequency discretization */
  	//public static final int FREQUENCY_DISCRETIZATION = 0;
  	/** Range discretization */
  	//public static final int RANGE_DISCRETIZATION = 1;
	/** Gain Ratio Discretization */
	//public static final int GAIN_RATIO_DISCRETIZATION = 2;
	
	/*public Discretization(InstanceSet instances)
	{	this.instances = instances;	
	}
	
	public InstanceSet frequencyDiscretization()
	{
	}
	
	public InstanceSet rangeDiscretization()
	{
	}
	
	public InstanceSet gainRatioDiscretization()
	{
	}	
	
	/** Sort values from array values and add equal values*/
	/*private double[] getDistribution(double[] values)
	{   int size = values.length;
	    int j=0;
	    double currentValue = values[0];
	    double[] resultArray = new double[size+1];
	    Arrays.sort(values);
	    for (int i=0; i<size; i++)
	    {   if (values[i]==currentValue)
	        {   resultArray[j] += values[i];
	        }
	        else
	        {   j++;
	            resultArray[j] = values[i];
	            currentValue = values[i];
	        }
	    }
	    double[] resultArray2 = new double[j];
	    System.arraycopy(resultArray,1,resultArray2,0,resultArray2.length);
	    return resultArray2;
	}*/
	
	/** Sort values from array values and returns an array that contains frequencies*/
	/*private double[] getFrequency(double[] values)
	{   int size = values.length;
	    int j=0;
	    double currentValue = values[0];
	    double[] resultArray = new double[size];
	    Arrays.sort(values);
	    for (int i=0; i<size; i++)
	    {   if (values[i]==currentValue)
	        {   resultArray[j] ++;
	        }
	        else
	        {   j++;
	            resultArray[j] ++;
	            currentValue = values[i];
	        }
	    }
	    double[] resultArray2 = new double[j+1];
	    System.arraycopy(resultArray,0,resultArray2,0,resultArray2.length);
	    return resultArray2;
	}*/
	
	 // Continuous attribute
      /*{   double[] values = new double[inst.numInstances()];
          Enumeration enumInst = inst.enumerateInstances();
          int i=0;
          while (enumInst.hasMoreElements())
          {   Instance instance = (Instance)enumInst.nextElement();
              values[i] = instance.getValue(att);
              i++;
          }
          if (method==FREQUENCY) // Frequency discretization
          {   double total = Utils.sum(values);
              double result = total / discretized;
              double[] changedValues = getDistribution(values);
              double[] freq = getFrequency(values);
              int valorInicial = 0;
              int valorPercorre = 0;
              for (i=0; i<discretized; i++)
              {   double result2 = result;
                  while (result2 >= 0)
                  {   result2 -= changedValues[valorPercorre];
                      if (result2 < 0)
                      {   result2 += changedValues[valorPercorre];
                          break;
                      }
                      valorPercorre++;
                  }
                  if (i != (discretized-1))
                  {   no.insereEstado((changedValues[valorInicial]/freq[valorInicial])+"_"+(changedValues[valorPercorre]/freq[valorPercorre]));
                  }
                  else
                      no.insereEstado((changedValues[valorInicial]/freq[valorInicial])+"_"+values[(values.length - 1)]);
                  valorInicial = valorPercorre;
              }
          }
          else if (method==RANGE) // Range discretization
          {   int maxIndex = Utils.maxIndex(values);
              float ranges = (float)(values[maxIndex]/discretized);
              for (i=0; i<discretized; i++)
              {   DecimalFormat df = new DecimalFormat("0.0#");
                  no.insereEstado(df.format(i*ranges)+"_"+df.format((i+1)*ranges));
              }
          }
          nodeColor = Color.blue;
          no.setCor(nodeColor.getRGB());
      }*/
	  // If continuous attribute
      /*{   double[] values = new double[inst.numInstances()];
          Enumeration enumInst = inst.enumerateInstances();
          int i=0,j=0;
          double[][] resultTable = new double[inst.numClasses()][discretized];

          while (enumInst.hasMoreElements())
          {   Instance instance = (Instance)enumInst.nextElement();
              values[i] = instance.getValue(att);
              i++;
          }

          for (i=0; i<inst.numClasses(); i++)   // Laplace Estimator
                  for (j=0; j<discretized; j++)
                      resultTable[i][j] ++;

          if (method==FREQUENCY) // Frequency discretization
          {   double total = Utils.sum(values);
              double result = total / discretized;
              double[] changedValues = getDistribution(values);
              double[] freq = getFrequency(values);
              int valorInicial = 0;
              int valorPercorre = 0;
              for (i=0; i<discretized; i++)
              {   double result2 = result;
                  while (result2 >= 0)
                  {   result2 -= changedValues[valorPercorre];
                      if (result2 < 0)
                      {   result2 += changedValues[valorPercorre];
                          break;
                      }
                      valorPercorre++;
                  }

                  double limiteInferior = changedValues[valorInicial]/freq[valorInicial];
                  double limiteSuperior;
                  if (i != (discretized-1))
                  {   limiteSuperior = changedValues[valorPercorre]/freq[valorPercorre];
                  }
                  else
                      limiteSuperior = values[(values.length - 1)];

                  enumInst = inst.enumerateInstances();
                  int numInst = 0;
                  while (enumInst.hasMoreElements())
                  {   Instance instance = (Instance)enumInst.nextElement();
                      if (i==(discretized - 1)) // garante que seja pego o último valor
                      {   if (instance.getValue(att)>=limiteInferior && instance.getValue(att)<=limiteSuperior)
                              resultTable[(int)instance.classValue()][i] ++;
                      }
                      else
                      {   if (instance.getValue(att)>=limiteInferior && instance.getValue(att)<limiteSuperior)
                              resultTable[(int)instance.classValue()][i] ++;
                      }
                      numInst++;
                  }
                  valorInicial = valorPercorre;
              }

          }
          else if (method==RANGE) // Range discretization
          {   int maxIndex = Utils.maxIndex(values);
              float ranges = (float)(values[maxIndex]/discretized);
              enumInst = inst.enumerateInstances();
              int numInst = 0;
              while (enumInst.hasMoreElements())
              {   Instance instance = (Instance)enumInst.nextElement();
                  for (i=0; i<discretized; i++)
                      if (i==(discretized - 1)) // Make sure last value will be used
                      {   if (instance.getValue(att)>=(i*ranges) && instance.getValue(att)<=((i+1)*ranges))
                              resultTable[(int)instance.classValue()][i] ++;
                      }
                      else
                      {   if (instance.getValue(att)>=(i*ranges) && instance.getValue(att)<((i+1)*ranges))
                              resultTable[(int)instance.classValue()][i] ++;
                      }
                  numInst++;
              }
          }*/
	
//}