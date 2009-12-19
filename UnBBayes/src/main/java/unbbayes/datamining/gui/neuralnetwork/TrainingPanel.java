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
package unbbayes.datamining.gui.neuralnetwork;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import ptolemy.plot.Plot;
import unbbayes.controller.IconController;
import unbbayes.datamining.classifiers.neuralnetwork.MeanSquaredError;

/**
 *  Class that implements the panel used to plot the mean squared error.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (06/26/2003)
 */
public class TrainingPanel extends JPanel implements MeanSquaredError{

  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;	
	
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
    resource = ResourceBundle.getBundle(
    		unbbayes.datamining.gui.neuralnetwork.resources.NeuralNetworkResource.class.getName());
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

    chart.fillPlot();
  }

  /**
   * Method that implements the interface MeanSquaredError so the neural network
   * may output its mean squared error during training.
   *
   * @param epoch An epoch
   * @param error The mean squared error of the epoch
   */
  public void setMeanSquaredError(int epoch, double meanSquaredError){
    addPoint(epoch, meanSquaredError);
  }

  /**
   * Method used to plot a point in the chart
   *
   * @param x The x coordinate
   * @param y The y coordinate
   */
  private void addPoint(double x, double y){
    if(firstPoint){
      chart.addPoint(0, x, y, false);
      firstPoint = false;
    } else {
      chart.addPoint(0, x, y, true);
    }
  }

  /**
   * Method used to clear the chart
   */
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