/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.classifiers.bayesianlearning;

import java.util.ArrayList;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.prs.Edge;
import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;

public class ParametricLearning
{
  public ParametricLearning(InstanceSet set)
  {
    int i,j,k;

    // inicializar x e dx
    int numAttributes = set.numAttributes();
    Attribute attrib;
    x = new String[numAttributes];
    dx = new ArrayList[numAttributes];
    for(i=0;i<numAttributes;i++)
    {
      attrib = set.getAttribute(i);
      x[i] = attrib.getAttributeName();
      ArrayList<String[]> temp = new ArrayList<String[]>();
      temp.add(attrib.getDistinticNominalValues());
      dx[i] = temp;
    }

    // inicializar freq e dataTemp com as amostras condensadas
    Instance inst, inst2;
    int numInstances = set.numInstances();
    freq = new int[numInstances];
    ArrayList<Instance> dataTemp = new ArrayList<Instance>();
    for(i=0;i<numInstances;i++)
    {
      inst = set.getInstance(i);
      for(j=0;j<dataTemp.size();j++)
      {
        inst2 = (Instance)dataTemp.get(j);
        for(k=0;k<x.length;k++)
        {
          if(inst.getValue(k)!=inst2.getValue(k))
          {
            break;
          }
        }
        if (k==x.length)
        {
          break;
        }
      }
      if(j==dataTemp.size())
      {
        freq[dataTemp.size()]=1;
        dataTemp.add(inst);
      }
      else
      {
        freq[j]=freq[j]+1;
      }
    }

    //inicializar d com base no dataTemp
    d = new int[dataTemp.size()][x.length];
    for(i=0;i<dataTemp.size();i++)
    {
      inst = (Instance)dataTemp.get(i);
      for(j=0;j<x.length;j++)
      {
        d[i][j]= (int) inst.getValue(j);
      }
    }
  }

//----------------------------------------------------------------------------//

  public int [] multiplies (int [] pai)
  {
    int ip;                     //�ndice do pai da intera��o atual
    int np = pai.length;        //n�mero de elementos do pai
    int[] mult;   //matriz de multiplicadores - tamanho np

    np = (np==0)? 1: np;
    mult = new int[np];
    mult[np-1] = 1;
    for(int k=np-2;k>=0;k--)
    {
      ip = pai[k+1];
      mult[k] = dx[ip].size()*mult[k+1];
    }

    return mult;
  }

//----------------------------------------------------------------------------//

  public int computeQ(int[] pai)
  {
    int qi = 1;
    for (int i=0;i<pai.length;i++)
    {
      qi = qi*dx[pai[i]].size();
    }
    return qi;
  }

//----------------------------------------------------------------------------//

  public int[][] computeNijk(int i, int[] pai)
  {
    int j;                         //relativo a um estado de 'pai'
    int k;                         //relativo a um estado de 'i'
    int ip;                        //�ndice do pai
    int fc;                        //frequ�ncia da linha atual
    int qi = computeQ(pai);        //quantidade de estados em 'pai'
    int ri = dx[i].size();         //quantidade de estados da vari�vel 'i'
    int[] mult = multiplies(pai);  //multiplicadores - para linearizar arranjo
    int[][] njk = new int[qi][ri]; //frequ�ncia do estado k com pais no estado j
    int n = x.length;              //n�mero de vari�veis de 'x'
    int nd = d.length;             //observa��es distintas na amostra condensada

    //para cada linha, computa o j-esimo estado do pai, computa o k-esimo estado
    //das variavel 'i' e acrescenta a frequencia da instancia da amostra
    for (int ic=0;ic<nd;ic++)
    {
      j = 0;
      //obter estado dos pais da linha atual
      for (int im=0;im<pai.length;im++)
      {
        ip = pai[im];
        j = j + d[ic][ip]*mult[im];
      }
      k = d[ic][i];
      fc = freq[ic];
      njk[j][k] = njk[j][k] +fc;
    }

    return njk;
  }

//----------------------------------------------------------------------------//

//par�metro pa: ArrayList de arrays de inteiros
//retorno: ArrayList de arrays bidimensionais de float
public ArrayList<float[][]> prob(ArrayList<int[]> pa)
{
  ArrayList<int[][]> nijk;	//ocorr�ncia dos estados das vari�veis relativos aos estados dos pais
  int n = x.length;	//n�mero de vari�veis
  int ri;		//n�mero de estados da vari�vel atual
  int qi;		//n�mero de estados dos pais da vari�vel atual
  int nij;		//n�mero total de ocorr�ncias da vari�vel em determinado estado dos pais
  ArrayList<float[][]> p;		//distribui��o condicional das fam�lias	(ArrayList de arrays bidimensionais)
  float[][] pjk;	//distribui��o condicional da vari�vel atual
  int[][] njk;		//nijk para vari�vel atual(i)

  //calcular nijk
  nijk = new ArrayList<int[][]>();
  for (int i=0;i<n;i++)
  {
    nijk.add(computeNijk(i, (int[])pa.get(i)));
  }

  p = new ArrayList<float[][]>();
  for(int i=0;i<n;i++)
  {
    ri = dx[i].size();
    qi = computeQ((int[])pa.get(i));
    pjk = new float[qi][ri];
    njk = nijk.get(i);

    for(int j=0;j<qi;j++)
    {
      nij = 0;
      for(int k=0;k<ri;k++)
      {
        nij = nij + njk[j][k];
      }
      for(int k=0;k<ri;k++)
      {
	pjk[j][k] = (float)(1+njk[j][k])/(float)(ri+nij);
      }
    }
    p.add(pjk);
  }
  return p;
}
//----------------------------------------------------------------------------//
public ProbabilisticNetwork getProbabilisticNetwork(ArrayList<int[]> pa) throws InvalidParentException
{
	ProbabilisticNetwork net = new ProbabilisticNetwork("net");
    int width = 50;
   	for(int i=0; i<x.length; i++)
    {
    	// Cria��o do n�
    	ProbabilisticNode node = new ProbabilisticNode();
      	node.setDescription(x[i]);
      	node.setName(x[i]);
      	node.setPosition(width,100);
      	width += 100;
      	ArrayList states = dx[i];
      	for (int j=0;j<states.size();j++)
      	{   node.appendState(""+states.get(j));
      	}
      	// Cria��o do Tabela de probabilidades
      	IProbabilityFunction tab = node.getProbabilityFunction();
      	tab.addVariable(node);
		net.addNode(node);
    }
   	for(int i=0; i<x.length; i++)
    {
    	ProbabilisticNode node = (ProbabilisticNode)net.getNodeAt(i);
      	PotentialTable tab = node.getProbabilityFunction();
      	int[] parents = (int[])pa.get(i);
      	for (int j=0;j<parents.length;j++)
      	{
      		Edge arco = new Edge(net.getNodeAt(parents[j]),node);
      		net.addEdge(arco);
      	}

      	ArrayList<float[][]> probs = prob(pa);
      	float[][] pjk = probs.get(i);
    	// Inser��o dos valores na tabela de probabilidades
      	int counter = 0;
        for (int j=0;j<pjk.length;j++)
    	{
    		for (int k=0;k<pjk[j].length;k++)
    		{
    			tab.setValue(counter,pjk[j][k]);
    			counter++;
    		}
    	}

    }

    return net;
}
//----------------------------------------------------------------------------//

  /** vari�veis do problema */
  private String[] x;

  /** dom�nios das vari�veis de x
   * Array de ArrayLists de Strings
   * dx[i]: dom�nios da vari�vel x[i] */
  private ArrayList[] dx;

  /** amostra condensada
   * d[ic,i]=v, sendo 'ic' a linha, 'i' o �ndice da vari�vel e 'v' o valor da
   * vari�vel na linha (representado pelo seu �ndice no dom�nio da vari�vel)) */
  private int[][] d;

  /** frequencias das inst�ncias de d
   * freq[i]: frequ�ncia da linha(ou instancia) i */
  private int[] freq;

}
