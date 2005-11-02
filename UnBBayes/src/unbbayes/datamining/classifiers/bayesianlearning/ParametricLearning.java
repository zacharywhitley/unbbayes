package unbbayes.datamining.classifiers.bayesianlearning;

import unbbayes.datamining.datamanipulation.*;
import unbbayes.prs.bn.*;
import unbbayes.prs.*;
import java.util.*;

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
      temp.add(attrib.getAttributeValues());
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
          if(inst.getByteValue(k)!=inst2.getByteValue(k))
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
        d[i][j]=inst.getByteValue(j);
      }
    }
  }

//----------------------------------------------------------------------------//

  public int [] multiplies (int [] pai)
  {
    int ip;                     //índice do pai da interação atual
    int np = pai.length;        //número de elementos do pai
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
    int ip;                        //índice do pai
    int fc;                        //frequência da linha atual
    int qi = computeQ(pai);        //quantidade de estados em 'pai'
    int ri = dx[i].size();         //quantidade de estados da variável 'i'
    int[] mult = multiplies(pai);  //multiplicadores - para linearizar arranjo
    int[][] njk = new int[qi][ri]; //frequência do estado k com pais no estado j
    int nd = d.length;             //observações distintas na amostra condensada

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

//parâmetro pa: ArrayList de arrays de inteiros
//retorno: ArrayList de arrays bidimensionais de float
public ArrayList prob(ArrayList pa)
{
  ArrayList nijk;	//ocorrência dos estados das variáveis relativos aos estados dos pais
  int n = x.length;	//número de variáveis
  int ri;		//número de estados da variável atual
  int qi;		//número de estados dos pais da variável atual
  int nij;		//número total de ocorrências da variável em determinado estado dos pais
  ArrayList p;		//distribuição condicional das famílias	(ArrayList de arrays bidimensionais)
  float[][] pjk;	//distribuição condicional da variável atual
  int[][] njk;		//nijk para variável atual(i)

  //calcular nijk
  nijk = new ArrayList();
  for (int i=0;i<n;i++)
  {
    nijk.add(computeNijk(i, (int[])pa.get(i)));
  }

  p = new ArrayList();
  for(int i=0;i<n;i++)
  {
    ri = dx[i].size();
    qi = computeQ((int[])pa.get(i));
    pjk = new float[qi][ri];
    njk = (int[][])nijk.get(i);

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
public ProbabilisticNetwork getProbabilisticNetwork(ArrayList pa)
{
	ProbabilisticNetwork net = new ProbabilisticNetwork("net");
    int width = 50;
   	for(int i=0; i<x.length; i++)
    {
    	// Criação do nó
    	ProbabilisticNode node = new ProbabilisticNode();
      	node.setDescription(x[i]);
      	node.setName(x[i]);
      	node.setPosition(width,100);
      	width += 100;
      	ArrayList states = dx[i];
      	for (int j=0;j<states.size();j++)
      	{   node.appendState(""+states.get(j));
      	}
      	// Criação do Tabela de probabilidades
      	PotentialTable tab = node.getPotentialTable();
      	tab.addVariable(node);
		net.addNode(node);
    }
   	for(int i=0; i<x.length; i++)
    {
    	ProbabilisticNode node = (ProbabilisticNode)net.getNodeAt(i);
      	PotentialTable tab = node.getPotentialTable();
      	int[] parents = (int[])pa.get(i);
      	for (int j=0;j<parents.length;j++)
      	{
      		Edge arco = new Edge(net.getNodeAt(parents[j]),node);
      		net.addEdge(arco);
      	}

      	ArrayList probs = prob(pa);
      	float[][] pjk = (float[][])probs.get(i);
    	// Inserção dos valores na tabela de probabilidades
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

  /** variáveis do problema */
  private String[] x;

  /** domínios das variáveis de x
   * Array de ArrayLists de Strings
   * dx[i]: domínios da variável x[i] */
  private ArrayList[] dx;

  /** amostra condensada
   * d[ic,i]=v, sendo 'ic' a linha, 'i' o índice da variável e 'v' o valor da
   * variável na linha (representado pelo seu índice no domínio da variável)) */
  private int[][] d;

  /** frequencias das instâncias de d
   * freq[i]: frequência da linha(ou instancia) i */
  private int[] freq;

}
