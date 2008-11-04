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

import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.PNEditionPane;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.exception.InvalidParentException;
/**
 * @author M�rio Henrique
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AlgorithmController {

  private NetworkWindow netWindow;

  public AlgorithmController(InstanceSet instanceSet, String algorithm, String metric) throws InvalidParentException
  {
//    System.out.println("cheguei");

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

    netWindow = new NetworkWindow(net);
    PNEditionPane edition = netWindow.getNetWindowEdition();
    edition.getCenterPanel().setBottomComponent(netWindow.getJspGraph());
    /*EditNet editNet = netWindow.getEditNet();
    editNet.getCenterPanel().setBottomComponent(netWindow.getJspGraph());*/

    //netWindow.changeToEditNet();
  }

  public NetworkWindow getNetWindow()
  {
    return netWindow;
  }

}
