package unbbayes.datamining.gui.naivebayes;

import java.util.*;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.jprs.jbn.*;

/** Esta classe calcula uma rede TRP utilizando-se do classificador Naive Bayes
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (23/12/2001)
 */
public class ComputeTRP
{ /** Este método calcula a rede TRP a partir dos parâmetros fornecidos
   *
   *  @param inst base de dados a ser utilizada pelo classificador Naive Bayes
   *  @return a rede TRP
   *  @exception Exception mesmas exceções lançadas pelo Naive Bayes
   */
  public ProbabilisticNetwork setInstances(InstanceSet inst) throws Exception
  {   this.inst = inst;
      numAtt = inst.numAttributes();
      NaiveBayes naive = new NaiveBayes();
      naive.buildClassifier(inst);
      priors = naive.getPriors();
      counts = naive.getCounts();
      int classIndex = inst.getClassAttribute().getIndex();
      createTVPClass(inst.getAttribute(classIndex));
      for(int i=0; i<numAtt; i++)
      {   if (inst.getAttribute(i).getIndex() != classIndex)
          {   if (inst.getAttribute(i).isNominal())
                  createTVP(inst.getAttribute(i));
          }
      }
      return rede;
  }

  /** Cria o nó classe */
  private void createTVPClass(Attribute att)
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
      rede.addNode(no);
      classAtt = no;
  }

  /** Cria os nós filhos */
  private void createTVP(Attribute att)
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
      rede.addNode(no);
      Edge arco = new Edge(classAtt,no);
      rede.addEdge(arco);

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

  private double[] priors;
  private double[][][] counts;
  private ProbabilisticNode classAtt;
  private int width = 50;
  private int numAtt;
  private int k = 0;
  private ProbabilisticNetwork rede = new ProbabilisticNetwork();
  private InstanceSet inst;
}