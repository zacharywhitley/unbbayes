package unbbayes.datamining.classifiers.bayesianlearning;

import unbbayes.datamining.datamanipulation.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import unbbayes.prs.bn.*;
import unbbayes.prs.*;
import unbbayes.gui.*;
/**
 * @author M�rio Henrique
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AlgorithmController {

  private NetWindow netWindow;

  public AlgorithmController(InstanceSet instanceSet, String algorithm, String metric)
  {
    System.out.println("cheguei");

    //testes
    ParametricLearning x = new ParametricLearning(instanceSet);
    /*int[] pai = new int[2];
    pai[0]=1;
    pai[1]=2;
    int [][] xibungo = x.computeNijk(0,pai);*/

    int[] array = new int[1];
    array[0]=4;
    ArrayList xumbrega = new ArrayList();
    //xumbrega.add(new int[0]);   //pa[0]
    xumbrega.add(array);   //pa[0]
    xumbrega.add(array);   //pa[1]
    xumbrega.add(array);   //pa[2]
    xumbrega.add(array);        //pa[3]
    xumbrega.add(new int[0]);        //pa[4]
    //xumbrega = x.prob(xumbrega);
    
    /*for (int i=0;i<xumbrega.size();i++)
    {
    	float[][] pjk = (float[][])xumbrega.get(i);
    	System.out.println("vari�vel i");
    	for (int j=0;j<pjk.length;j++)
    	{
    		for (int k=0;k<pjk[j].length;k++)
    		{
    			System.out.println(pjk[j][k]);
    		}
    		System.out.println("mais");    		
    	}    	
    }*/

    ProbabilisticNetwork net = x.getProbabilisticNetwork(xumbrega);

    netWindow = new NetWindow(net);
    NetWindowEdition edition = netWindow.getNetWindowEdition();
    edition.getCenterPanel().setBottomComponent(netWindow.getJspGraph());
    /*EditNet editNet = netWindow.getEditNet();
    editNet.getCenterPanel().setBottomComponent(netWindow.getJspGraph());*/

    //netWindow.changeToEditNet();
  }

  public NetWindow getNetWindow()
  {
    return netWindow;
  }

}
