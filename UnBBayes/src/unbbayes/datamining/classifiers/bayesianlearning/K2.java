package unbbayes.datamining.classifiers.bayesianlearning;

import unbbayes.datamining.datamanipulation.*;
import unbbayes.prs.bn.*;
import java.util.*;
import java.lang.Math;

public class K2
{
    // Data
    private ParametricLearning m_pl;
    private ArrayList finalConfiguration = new ArrayList();
    private InstanceSet instanceSet;

    // Construtor
    public K2(InstanceSet instanceSet, ParametricLearning pl)
    {
        m_pl = pl;
        this.instanceSet = instanceSet;
        int numAttributes = instanceSet.numAttributes();
        Attribute[] attributes = new Attribute[numAttributes];
        int[][] parents = new int [numAttributes][0];
        for (int i=0;i<numAttributes;i++)
        {
        	attributes[i] = instanceSet.getAttribute(i);	
        	if (i>0/*&& não tiver mais pais que predecessores*/)
        	{
        		double g = gh(attributes[i],parents[i]);
        		System.out.println("attribute = "+attributes[i]+" g = "+g);
        		boolean flag = true;
        		int numPredessors = attributes[i].getIndex();
        		while (flag && (parents[i].length<=numPredessors))
        		{
        			Object[] results = maxGh(attributes[i],parents[i]);
        			Attribute z = (Attribute)results[0];
        			double gx = ((Double)results[1]).doubleValue();
        			System.out.println("attribute = "+z+" gx = "+gx);
        			double dif = gx - g;
        			if (dif>0)
        			{
        				int[] parentsTemp = new int[parents[i].length+1];
        				System.arraycopy(parents[i],0,parentsTemp,0,parents[i].length);
        				parentsTemp[parents[i].length] = z.getIndex();
        				parents[i] = parentsTemp;	
        			}
        			else
        			{
        				flag = false;
        				System.out.println("false");        			
        			}
        		}
        	}
        	finalConfiguration.add(parents[i]);
        }        
    }
    
    private Object[] maxGh(Attribute attribute,int[] actualParents)
    {
    	Attribute z = null;
    	double max = -1*Double.MAX_VALUE;       
        double maxAux = 0.0;
        for (int i = 0 ; i < attribute.getIndex(); i++ )
        {
        	int[] newParents = union(actualParents,i);
        	maxAux  = gh(attribute,newParents);
            System.out.println("attribute = "+attribute+" i = "+i+" maxAux "+maxAux);
        	if (max < maxAux)
            {
                max = maxAux;
                z = instanceSet.getAttribute(i);
            }
       
        }
        System.out.println("retorno = "+z+" max = "+max);
        return new Object[]{z,new Double(max)};
    }
    
    private int[] union(int[] actualParents,int i)
    {	
    	for (int j=0;j<actualParents.length;j++)
    	{
    		if (actualParents[j]==i)
    			return actualParents;
    	}
    	int[] parentsTemp = new int[actualParents.length+1];
        System.arraycopy(actualParents,0,parentsTemp,0,actualParents.length);
       	parentsTemp[actualParents.length] = i;
       	return parentsTemp;        				
    }

    public ProbabilisticNetwork getProbabilisticNetwork()
    {
    	return m_pl.getProbabilisticNetwork(finalConfiguration);
    }
    
    // Métodos
    double gh(Attribute i, int[] pais)
    {
        int qi = m_pl.computeQ(pais);
        int ri = i.numValues();
        int[][] Nijk = m_pl.computeNijk(i.getIndex(), pais);

        int width = Nijk.length;
        int heigth = Nijk[0].length;

        int[] Nij = new int[width];
        for(int x = 0; x<width; x++)
        {
            Nij[x] = 0;
            for(int y = 0; y<heigth; y++)
            {
                Nij[x] += Nijk[x][y];
            }
        }

        float a = 0;
        for(int j = 0; j<qi; j++)
        {
            a += logfat(ri + Nij[j] - 1);
        }

        float b = 0;
        for(int j = 0; j<qi; j++)
        {
            for(int k = 0; k<ri; k++)
            {
                b += logfat(Nijk[j][k]);
            }
        }


        return qi * logfat(ri - 1) - a + b;
    }

    ///////////////////////////////////////////////////////////////////////////
    // logfat

    private double logfat(int n)
    {
        if( n <= 100)
        {
           return logfatorial(n);
        }
        else
        {
           return logstirling(n);
        }
    }

    private double logfatorial(int n)
    {
        double f = 0;

        for(int i = 1 ; i <= n ; i++)
        {
           f += Math.log(i);
        }
        return f;
    }

    private double logstirling(int n)
    {
       return (0.5*Math.log(2*Math.PI) + (n+0.5)*Math.log(n) - n*Math.log(Math.E));
    }
}
