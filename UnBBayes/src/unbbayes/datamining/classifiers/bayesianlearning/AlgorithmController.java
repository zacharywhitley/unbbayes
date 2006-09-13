package unbbayes.datamining.classifiers.bayesianlearning;

import unbbayes.datamining.datamanipulation.*;

import unbbayes.prs.bn.*;
import unbbayes.gui.*;
/**
 * @author Mário Henrique
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
    K2 k2 = new K2(instanceSet,new ParametricLearning(instanceSet));    
    /*double ghReturn = 0;
    
    int[] array = new int[2];
    array[0]=1;
    array[1]=3;
    ghReturn = k2.gh(instanceSet.getAttribute(4),array);
    System.out.println("gh = "+ghReturn);
    array = new int[2];
    array[0]=3;
    array[1]=1;
    ghReturn = k2.gh(instanceSet.getAttribute(4),array);
    System.out.println("gh = "+ghReturn);
    array = new int[1];
    array[0]=3;
    ghReturn = k2.gh(instanceSet.getAttribute(4),array);
    System.out.println("gh = "+ghReturn);
    array = new int[1];
    array[0]=1;
    ghReturn = k2.gh(instanceSet.getAttribute(4),array);
    System.out.println("gh = "+ghReturn);
    array = new int[2];
    array[0]=3;
    array[1]=6;
    ghReturn = k2.gh(instanceSet.getAttribute(4),array);
    System.out.println("gh = "+ghReturn);
    array = new int[2];
    array[0]=6;
    array[1]=3;
    ghReturn = k2.gh(instanceSet.getAttribute(4),array);
    System.out.println("gh = "+ghReturn);
    array = new int[2];
    array[0]=1;
    array[1]=6;
    ghReturn = k2.gh(instanceSet.getAttribute(4),array);
    System.out.println("gh = "+ghReturn);
    array = new int[2];
    array[0]=6;
    array[1]=1;
    ghReturn = k2.gh(instanceSet.getAttribute(4),array);
    System.out.println("gh = "+ghReturn);
        
    // weather
    /*array = new int[1];
    array[0]=4;
    ArrayList xumbrega = new ArrayList();
    xumbrega.add(array);   //pa[0]
    xumbrega.add(array);   //pa[1]
    xumbrega.add(array);   //pa[2]
    xumbrega.add(array);        //pa[3]
    xumbrega.add(new int[0]);        //pa[4]*/
    /*ArrayList xumbrega = new ArrayList();
    xumbrega.add(new int[0]);   //pa[0] asias
    array = new int[1];
    array[0]=0;
    xumbrega.add(array);   //pa[1] tuber
    xumbrega.add(new int[0]);   //pa[2] fumante
    array = new int[1];
    array[0]=2;
    xumbrega.add(array);        //pa[3] cancer
    array = new int[2];
    array[0]=1;
    array[1]=3;
    xumbrega.add(array);  //pa[4] tborca
    array = new int[1];
    array[0]=4;
    xumbrega.add(array);        //pa[5] xray
    array = new int[1];
    array[0]=2;
    xumbrega.add(array);        //pa[6] bronq
    array = new int[2];
    array[0]=6;
    array[1]=4;
    xumbrega.add(array);*/        //pa[7] disp    
    

    ProbabilisticNetwork net = k2.getProbabilisticNetwork();

    netWindow = new NetWindow(net);
    PNEditionPane edition = netWindow.getNetWindowEdition();
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
