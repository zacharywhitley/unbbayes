package unbbayes.datamining.classifiers.bayesianlearning;

import unbbayes.datamining.datamanipulation.*;
import javax.swing.*;
import java.awt.*;

import unbbayes.prs.bn.*;
import unbbayes.prs.*;
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

    ProbabilisticNetwork net = new ProbabilisticNetwork("mario1213");
    ProbabilisticNode node = new ProbabilisticNode();
    node.setName("mario");
    net.addNode(node);

    netWindow = new NetWindow(net);
    /*NetWindowEdition edition = netWindow.getNetWindowEdition();
    edition.getCenterPanel().setBottomComponent(netWindow.getJspGraph());*/
    EditNet editNet = netWindow.getEditNet();
    editNet.getCenterPanel().setBottomComponent(netWindow.getJspGraph());

    netWindow.changeToEditNet();
  }

  public NetWindow getNetWindow()
  {
    return netWindow;
  }

}
