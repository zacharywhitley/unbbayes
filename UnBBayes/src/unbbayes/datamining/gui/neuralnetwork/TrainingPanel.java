package unbbayes.datamining.gui.neuralnetwork;

import java.awt.*;
import javax.swing.*;
import ptolemy.plot.*;
import java.awt.event.*;
import java.awt.print.*;
import unbbayes.datamining.classifiers.neuralnetwork.*;
import unbbayes.controller.IconController;

public class TrainingPanel extends JPanel implements QuadraticAverageError{
  private ImageIcon fillIcon;
  private ImageIcon resetSizeIcon;
  private ImageIcon gridIcon;
  private ImageIcon printIcon;
  private IconController iconController = IconController.getInstance();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel trainingPanel = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  Plot chart = new Plot();
  boolean first = true;
  JToolBar jToolBar1 = new JToolBar();
  JButton buttonFill = new JButton();
  JButton buttonPrint = new JButton();
  JButton buttonGrid = new JButton();
  JButton buttonReset = new JButton();
  JLabel spaceLabel = new JLabel();

  public TrainingPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    fillIcon = iconController.getFillIcon();
    resetSizeIcon = iconController.getResetSizeIcon();
    gridIcon = iconController.getGridIcon();
    printIcon = iconController.getPrintIcon();
    this.setLayout(borderLayout1);
    trainingPanel.setLayout(borderLayout2);
    buttonFill.setToolTipText("Fill");
    buttonFill.setIcon(fillIcon);
    buttonFill.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonFill_actionPerformed(e);
      }
    });
    jToolBar1.setBorder(BorderFactory.createEtchedBorder());
    jToolBar1.setFloatable(false);
    buttonPrint.setToolTipText("Print");
    buttonPrint.setIcon(printIcon);
    buttonPrint.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonPrint_actionPerformed(e);
      }
    });
    buttonGrid.setToolTipText("Add and Remove the grid");
    buttonGrid.setIcon(gridIcon);
    buttonGrid.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonGrid_actionPerformed(e);
      }
    });
    buttonReset.setToolTipText("Reset to default size");
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
    chart.setTitle("Erro quadrado médio X Época");
    chart.setYRange(0, 0.5);
    chart.setXRange(0, 1000);
    chart.setYLabel("Erro Quadrado Médio");
    chart.setXLabel("Épocas");
  }

  public void setQuadraticAverageError(int epoch, double error){
    addPoint(epoch, error);
  }

  public void addPoint(double x, double y){
    if(first){
      chart.addPoint(0, x, y, false);
      first = false;
    } else {
      chart.addPoint(0, x, y, true);
    }
  }

  public void clear(){
    chart.clear(true);
    first = true;
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
    job.setPrintable(/*PlotBox.this*/chart, format);
    if (job.printDialog()) {
      try {
        job.print();
      } catch (Exception ex) {
        Component ancestor = getTopLevelAncestor();
        JOptionPane.showMessageDialog(ancestor, "Printing failed:\n" + ex.toString(), "Print Error", JOptionPane.WARNING_MESSAGE);
      }
    }
  }

}