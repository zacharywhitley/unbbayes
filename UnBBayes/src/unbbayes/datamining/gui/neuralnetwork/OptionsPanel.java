package unbbayes.datamining.gui.neuralnetwork;

import java.awt.*;
import javax.swing.*;
import unbbayes.datamining.classifiers.*;

public class OptionsPanel extends JPanel {
  BorderLayout borderLayout1 = new BorderLayout();
  GridLayout gridLayout1 = new GridLayout();
  JLabel LabelHiddenSize = new JLabel();
  JLabel labelTrainningTime = new JLabel();
  JTextField fieldLearningRate = new JTextField();
  JLabel labelMomentum = new JLabel();
  JTextField fieldTrainningTime = new JTextField();
  JTextField fieldHiddenSize = new JTextField();
  JComboBox comboActivationFunction = new JComboBox();
  JLabel labelActivationFunction = new JLabel();
  JTextField fieldMomentum = new JTextField();
  JPanel jPanel2 = new JPanel();
  JLabel labelLearningRate = new JLabel();
  JPanel panelLearningRate = new JPanel();
  JPanel panelMomentum = new JPanel();
  JPanel panelHiddenSize = new JPanel();
  JPanel panelTrainningTime = new JPanel();
  JPanel panelActivationFunction = new JPanel();
  FlowLayout flowLayout1 = new FlowLayout();
  FlowLayout flowLayout2 = new FlowLayout();
  FlowLayout flowLayout3 = new FlowLayout();
  FlowLayout flowLayout4 = new FlowLayout();

  public OptionsPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    jPanel2.setBorder(BorderFactory.createEtchedBorder());
    jPanel2.setLayout(gridLayout1);
    fieldMomentum.setText("");
    fieldMomentum.setColumns(15);
    labelActivationFunction.setText("Activation Function:");
    labelActivationFunction.setVerticalAlignment(SwingConstants.BOTTOM);
    fieldHiddenSize.setText("");
    fieldHiddenSize.setColumns(15);
    fieldTrainningTime.setText("");
    fieldTrainningTime.setColumns(15);
    labelMomentum.setText("Momentum:");
    labelMomentum.setVerticalAlignment(SwingConstants.BOTTOM);
    labelTrainningTime.setText("Trainning Time:");
    labelTrainningTime.setVerticalAlignment(SwingConstants.BOTTOM);
    LabelHiddenSize.setText("Hidden Layer Size:");
    LabelHiddenSize.setVerticalAlignment(SwingConstants.BOTTOM);
    gridLayout1.setRows(10);
    gridLayout1.setVgap(0);
    gridLayout1.setHgap(5);
    gridLayout1.setColumns(1);
    this.setLayout(borderLayout1);
    labelLearningRate.setVerifyInputWhenFocusTarget(true);
    labelLearningRate.setText("Learning Rate:");
    labelLearningRate.setVerticalAlignment(SwingConstants.BOTTOM);
    fieldLearningRate.setColumns(15);
    fieldLearningRate.setHorizontalAlignment(SwingConstants.LEADING);
    panelLearningRate.setLayout(flowLayout1);
    flowLayout1.setAlignment(FlowLayout.LEFT);
    flowLayout1.setHgap(1);
    panelMomentum.setLayout(flowLayout2);
    flowLayout2.setAlignment(FlowLayout.LEFT);
    flowLayout2.setHgap(1);
    flowLayout2.setVgap(5);
    panelHiddenSize.setLayout(flowLayout3);
    flowLayout3.setAlignment(FlowLayout.LEFT);
    flowLayout3.setHgap(1);
    flowLayout3.setVgap(5);
    panelTrainningTime.setLayout(flowLayout4);
    flowLayout4.setAlignment(FlowLayout.LEFT);
    flowLayout4.setHgap(1);
    borderLayout1.setHgap(3);
    borderLayout1.setVgap(0);
    jPanel2.add(labelLearningRate, null);
    jPanel2.add(panelLearningRate, null);
    panelLearningRate.add(fieldLearningRate, null);
    jPanel2.add(labelMomentum, null);
    jPanel2.add(panelMomentum, null);
    panelMomentum.add(fieldMomentum, null);
    jPanel2.add(LabelHiddenSize, null);
    jPanel2.add(panelHiddenSize, null);
    panelHiddenSize.add(fieldHiddenSize, null);
    jPanel2.add(labelTrainningTime, null);
    jPanel2.add(panelTrainningTime, null);
    panelTrainningTime.add(fieldTrainningTime, null);
    jPanel2.add(labelActivationFunction, null);
    jPanel2.add(panelActivationFunction, null);
    panelActivationFunction.add(comboActivationFunction, null);
    this.add(jPanel2,  BorderLayout.CENTER);
    comboActivationFunction.insertItemAt("Sigmoid", NeuralNetwork.SIGMOID);
    comboActivationFunction.insertItemAt("Tanh", NeuralNetwork.TANH);
    comboActivationFunction.setSelectedIndex(0);
    this.setEnabled(false);
  }

  public void setEnabled(boolean enable){
    fieldLearningRate.setEnabled(enable);
    fieldMomentum.setEnabled(enable);
    fieldTrainningTime.setEnabled(enable);
    fieldHiddenSize.setEnabled(enable);
    comboActivationFunction.setEnabled(enable);
  }

  public float getLearningRate() throws NumberFormatException{
    return Float.parseFloat(fieldLearningRate.getText());
  }

  public float getMomentum() throws NumberFormatException{
    return Float.parseFloat(fieldMomentum.getText());
  }

  public String getTrainingTime(){
    return fieldTrainningTime.getText();
  }

  public int getHiddenLayerSize() throws NumberFormatException{
    return Integer.parseInt(fieldHiddenSize.getText());
  }

  public int getSelectedActivationFunction(){
    if(comboActivationFunction.getSelectedIndex() == NeuralNetwork.SIGMOID){
      return NeuralNetwork.SIGMOID;
    } else if(comboActivationFunction.getSelectedIndex() == NeuralNetwork.TANH){
      return NeuralNetwork.TANH;
    }
    return NeuralNetwork.SIGMOID;
  }

}