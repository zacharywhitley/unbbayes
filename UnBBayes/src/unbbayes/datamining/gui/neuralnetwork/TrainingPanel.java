package unbbayes.datamining.gui.neuralnetwork;

import java.awt.*;
import javax.swing.*;
import ptolemy.plot.*;
import java.awt.event.*;
import unbbayes.datamining.classifiers.neuralnetwork.*;

public class TrainingPanel extends JPanel implements QuadraticAverageError{
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

  public TrainingPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    trainingPanel.setLayout(borderLayout2);
    buttonFill.setText("fill");
    buttonFill.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonFill_actionPerformed(e);
      }
    });
    jToolBar1.setFloatable(false);
    buttonPrint.setText("Print");
    buttonPrint.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonPrint_actionPerformed(e);
      }
    });
    buttonGrid.setText("Grid");
    buttonGrid.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonGrid_actionPerformed(e);
      }
    });
    buttonReset.setText("reset size");
    buttonReset.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonReset_actionPerformed(e);
      }
    });
    jToolBar1.add(buttonFill, null);
    jToolBar1.add(buttonReset, null);
    jToolBar1.add(buttonGrid, null);
    jToolBar1.add(buttonPrint, null);
    this.add(trainingPanel,  BorderLayout.CENTER);
    trainingPanel.add(chart,  BorderLayout.CENTER);
    trainingPanel.add(jToolBar1, BorderLayout.NORTH);

    chart.setSize(400,300);
    chart.setTitle("Erro quadrado médio X Época");
    chart.setYRange(0, 0.5);
    chart.setXRange(0, 100);
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

  }

}