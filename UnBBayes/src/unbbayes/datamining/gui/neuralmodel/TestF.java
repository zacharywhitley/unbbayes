package unbbayes.datamining.gui.neuralmodel;

import unbbayes.fronteira.*;
import javax.swing.*;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class TestF extends JFrame{

  MDIDesktopPane desktop = new MDIDesktopPane();
//  JInternalFrame cnm = new NeuralModelMain();
  NeuralModelMain cnm = new NeuralModelMain();

  JInternalFrame jif = new JInternalFrame("rafael", true, true, true, true);

  public TestF() {
    this.getContentPane().add(/*new JScrollPane*/(desktop));

    jif.getContentPane().add(new JLabel("rafael"));
    desktop.add(jif);

    System.out.println("vou adicionar");

    desktop.add(cnm);
    System.out.println("adicionado");
  }
}