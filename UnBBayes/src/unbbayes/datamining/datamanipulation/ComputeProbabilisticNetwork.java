package unbbayes.datamining.datamanipulation;

import java.util.*;

import unbbayes.datamining.classifiers.*;
import unbbayes.prs.*;
import unbbayes.prs.bn.*;

/** Esta classe calcula uma rede ProbabilisticNetwork utilizando-se do classificador Naive Bayes
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (23/12/2001)
 */
public class ComputeProbabilisticNetwork
{ /** Este método calcula a rede ProbabilisticNetwork a partir dos parâmetros fornecidos
   *
   *  @param inst base de dados a ser utilizada pelo classificador Naive Bayes
   *  @return a rede ProbabilisticNetwork
   *  @exception Exception mesmas exceções lançadas pelo Naive Bayes
   */
  public void setInstances(InstanceSet inst) throws Exception
  {   this.inst = inst;
      numAtt = inst.numAttributes();
      NaiveBayes naive = new NaiveBayes();
      naive.buildClassifier(inst);
      priors = naive.getPriors();
      counts = naive.getCounts();
      int classIndex = inst.getClassAttribute().getIndex();
      createProbabilisticNodeClass(inst.getAttribute(classIndex));
      for(int i=0; i<numAtt; i++)
      {   if (inst.getAttribute(i).getIndex() != classIndex)
          {   if (inst.getAttribute(i).isNominal())
                  createProbabilisticNode(inst.getAttribute(i));
          }
      }
  }

  /** Retorna a rede bayesiana ProbabilisticNetwork
  	@return Um rede ProbabilisticNetwork
  */
  public ProbabilisticNetwork getProbabilisticNetwork()
  {	  return net;
  }

  /** Cria o nó classe */
  private void createProbabilisticNodeClass(Attribute att)
  {   ProbabilisticNode no = new ProbabilisticNode();
      no.setDescription(att.getAttributeName());
      no.setName(att.getAttributeName());
      Enumeration enum = att.enumerateValues();
      while (enum.hasMoreElements())
      {   no.appendState(""+enum.nextElement());
      }
      if (numAtt == 1)
      {   no.setPosicao(50,30);
      }
      else
      {   no.setPosicao(50 + ((numAtt-2) * 50),30);
      }
      PotentialTable tab = no.getPotentialTable();
      //tab.porVariavel(no);
      tab.addVariable(no);
      int num = att.numValues();
      for (int i=0;i<num;i++)
      {   //tab.porValor(i,priors[i]);
          tab.setValue(i,priors[i]);
      }
      net.addNode(no);
      classAtt = no;
  }

  /** Cria os nós filhos */
  private void createProbabilisticNode(Attribute att)
  {   ProbabilisticNode no = new ProbabilisticNode(); // Criação do nó
      no.setDescription(att.getAttributeName());
      no.setName(att.getAttributeName());

      Enumeration enum = att.enumerateValues();
      while (enum.hasMoreElements())
      {   no.appendState(""+enum.nextElement());
      }

      PotentialTable tab = no.getPotentialTable();  // Criação do Tabela de probabilidades
      //tab.porVariavel(no);
      tab.addVariable(no);
      no.setPosicao(width,100);
      width += 100;
      net.addNode(no);
      Edge arco = new Edge(classAtt,no);
      net.addEdge(arco);

      // Inserção dos valores na tabela de probabilidades
      int numClasses = inst.numClasses();
      int[] coord = new int[numClasses];
      int num = att.numValues();
      int i=0,j=0;
      for (j=0;j<numClasses;j++)
      {   for (i=0;i<num;i++)
          {   coord[0] = i;
              coord[1] = j;
              //tab.porValor(coord,counts[j][k][i]);
              tab.setValue(coord,counts[j][k][i]);
          }
      }
      k++;
  }

  private float[] priors;
  private float[][][] counts;
  private ProbabilisticNode classAtt;
  private int width = 50;
  private int numAtt;
  private int k = 0;
  private ProbabilisticNetwork net = new ProbabilisticNetwork();
  private InstanceSet inst;
}