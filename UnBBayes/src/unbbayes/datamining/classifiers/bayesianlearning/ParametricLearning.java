package unbbayes.datamining.classifiers.bayesianlearning;

import unbbayes.datamining.datamanipulation.*;
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
      dx[i] = attrib.getAttributeValues();
    }

    // inicializar freq e dataTemp com as amostras condensadas
    Instance inst, inst2;
    int numInstances = set.numInstances();
    freq = new int[numInstances];
    ArrayList dataTemp = new ArrayList();
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
    d = new short[dataTemp.size()][x.length];
    for(i=0;i<dataTemp.size();i++)
    {
      inst = (Instance)dataTemp.get(i);
      for(j=0;j<x.length;j++)
      {
        d[i][j]=inst.getValue(j);
      }
    }
  }

//----------------------------------------------------------------------------//

  /* int[] instanciaPai(i,pai) */

//----------------------------------------------------------------------------//

  int [] multiplies (int [] pai)
  {
    int ip;                     //índice do pai da interação atual
    int np = pai.length;        //número de elementos do pai
    int[] mult = new int[np];   //matriz de multiplicadores - tamanho m

    np = (np==0)? 1: np;
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
    System.out.println("qi = "+qi);
    int ri = dx[i].size();         //quantidade de estados da variável 'i'
    int[] mult = multiplies(pai);  //multiplicadores - para linearizar arranjo
    for (int w=0;w<mult.length;w++)
    	System.out.println("mult = "+mult[w]);
    int[][] njk = new int[qi][ri]; //frequência do estado k com pais no estado j
    int n = x.length;              //número de variáveis de 'x'
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

  /*
  ProbNet probs()
  {
  }
  */

//----------------------------------------------------------------------------//

  /** variáveis do problema */
  String[] x;

  /** domínios das variáveis de x
   * Array de ArrayLists de Strings
   * dx[i]: domínios da variável x[i] */
  ArrayList[] dx;

  /** amostra condensada
   * d[ic,i]=v, sendo 'ic' a linha, 'i' o índice da variável e 'v' o valor da
   * variável na linha (representado pelo seu índice no domínio da variável)) */
  short[][] d;

  /** frequencias das instâncias de d
   * freq[i]: frequência da linha(ou instancia) i */
  int[] freq;

}
