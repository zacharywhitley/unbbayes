package unbbayes.datamining.gui.neuralnetwork;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import ptolemy.plot.*;
import unbbayes.controller.*;
import unbbayes.datamining.classifiers.neuralnetwork.*;

public class TrainingPanel extends JPanel implements QuadraticAverageError{
  private ResourceBundle resource;
  private ImageIcon fillIcon;
  private ImageIcon resetSizeIcon;
  private ImageIcon gridIcon;
  private ImageIcon printIcon;
  private IconController iconController = IconController.getInstance();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel trainingPanel = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private Plot chart = new Plot();
  private boolean firstPoint = true;
  private JToolBar jToolBar1 = new JToolBar();
  private JButton buttonFill = new JButton();
  private JButton buttonPrint = new JButton();
  private JButton buttonGrid = new JButton();
  private JButton buttonReset = new JButton();
  private JLabel spaceLabel = new JLabel();

  public TrainingPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.neuralnetwork.resources.NeuralNetworkResource");
    fillIcon = iconController.getFillIcon();
    resetSizeIcon = iconController.getResetSizeIcon();
    gridIcon = iconController.getGridIcon();
    printIcon = iconController.getPrintIcon();
    this.setLayout(borderLayout1);
    trainingPanel.setLayout(borderLayout2);
    buttonFill.setToolTipText(resource.getString("fillToolTip"));
    buttonFill.setIcon(fillIcon);
    buttonFill.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonFill_actionPerformed(e);
      }
    });
    jToolBar1.setBorder(BorderFactory.createEtchedBorder());
    jToolBar1.setFloatable(false);
    buttonPrint.setToolTipText(resource.getString("printToolTip"));
    buttonPrint.setIcon(printIcon);
    buttonPrint.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonPrint_actionPerformed(e);
      }
    });
    buttonGrid.setToolTipText(resource.getString("gridToolTip"));
    buttonGrid.setIcon(gridIcon);
    buttonGrid.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonGrid_actionPerformed(e);
      }
    });
    buttonReset.setToolTipText(resource.getString("resetButtonToolTip"));
    buttonReset.setIcon(resetSizeIcon);
    buttonReset.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonReset_actionPerformed(e);
      }
    });
    spaceLabel.setText("  ");
    jToolBar1.add(buttonFill, null);
    jToolBar1.add(buttonReset, null);
    jToolBar1.add(buttonGrid, null);
    jToolBar1.add(spaceLabel, null);
    jToolBar1.add(buttonPrint, null);
    this.add(trainingPanel,  BorderLayout.CENTER);
    trainingPanel.add(chart,  BorderLayout.CENTER);
    trainingPanel.add(jToolBar1, BorderLayout.NORTH);

    chart.setSize(400,300);
    chart.setTitle(resource.getString("chartTitle"));
    chart.setYRange(0, 0.5);
    chart.setXRange(0, 1000);
    chart.setYLabel(resource.getString("YAxisTitle"));
    chart.setXLabel(resource.getString("XAxisTitle"));
  }

  public void setQuadraticAverageError(int epoch, double error){
    addPoint(epoch, error);
  }

  public void addPoint(double x, double y){
    if(firstPoint){
      chart.addPoint(0, x, y, false);
      firstPoint = false;
    } else {
      chart.addPoint(0, x, y, true);
    }
  }

  public void clear(){
    chart.clear(true);
    firstPoint = true;
  }

  void buttonFill_actionPerformed(ActionEvent e) {
    chart.fillPlot();
  }

  void buttonReset_actionPerformed(ActionEvent e) {
    chart.resetAxes();
  }

  void buttonGrid_actionPerformed(ActionEvent e) {
      chart.setGrid(!chart.getGrid());
      chart.repaint();
  }

  void buttonPrint_actionPerformed(ActionEvent e) {
    PrinterJob job = PrinterJob.getPrinterJob();
    PageFormat format = job.pageDialog(job.defaultPage());
    job.setPrintable(chart, format);
    if (job.printDialog()) {
      try {
        job.print();
      } catch (Exception ex) {
        Component ancestor = getTopLevelAncestor();
        JOptionPane.showMessageDialog(ancestor, resource.getString("printingFailed") + "\n" + ex.toString(), "Print Error", JOptionPane.WARNING_MESSAGE);
      }
    }
  }

}