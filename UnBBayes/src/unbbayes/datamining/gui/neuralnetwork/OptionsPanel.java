package unbbayes.datamining.gui.neuralnetwork;

import java.awt.*;
import javax.swing.*;
import java.text.*;
import javax.swing.JFormattedTextField.*;

import unbbayes.datamining.classifiers.*;
import javax.swing.border.*;

public class OptionsPanel extends JPanel {
  BorderLayout borderLayout1 = new BorderLayout();
  GridLayout gridLayout1 = new GridLayout();
  JLabel LabelHiddenSize = new JLabel();
  JLabel labelTrainningTime = new JLabel();
  JLabel labelMomentum = new JLabel();
  JTextField fieldTrainningTime = new JTextField();
  JComboBox comboActivationFunction = new JComboBox();
  JLabel labelActivationFunction = new JLabel();
  JPanel jPanel2 = new JPanel();
  JLabel labelLearningRate = new JLabel();
  JPanel panelTrainningTime = new JPanel();
  JPanel panelActivationFunction = new JPanel();
  FlowLayout flowLayout4 = new FlowLayout();
  JLabel jLabel1 = new JLabel();

  SpinnerNumberModel learningRateSpinnerModel = new SpinnerNumberModel(0.1, 0.01, 1.0, 0.01);
  SpinnerNumberModel momentumSpinnerModel = new SpinnerNumberModel(0.5, 0.0, 1.0, 0.1);
  SpinnerNumberModel hiddenSizeSpinnerModel = new SpinnerNumberModel(3, 1, 100, 1);
  SpinnerNumberModel steepSpinnerModel = new SpinnerNumberModel(1.0, 0.1, 2.0, 0.1);

  JSpinner spinnerSteep = new JSpinner();
  JSpinner spinnerLearningRate = new JSpinner();
  JSpinner spinnerMomentum = new JSpinner();
  JSpinner spinnerHiddenSize = new JSpinner();


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
    labelActivationFunction.setText("Activation Function:");
    labelActivationFunction.setVerticalAlignment(SwingConstants.BOTTOM);
    fieldTrainningTime.setText("");
    fieldTrainningTime.setColumns(17);
    labelMomentum.setText("Momentum:");
    labelMomentum.setVerticalAlignment(SwingConstants.BOTTOM);
    labelTrainningTime.setText("Trainning Time:");
    labelTrainningTime.setVerticalAlignment(SwingConstants.BOTTOM);
    LabelHiddenSize.setText("Hidden Layer Size:");
    LabelHiddenSize.setVerticalAlignment(SwingConstants.BOTTOM);
    gridLayout1.setRows(12);
    gridLayout1.setVgap(0);
    gridLayout1.setHgap(5);
    gridLayout1.setColumns(1);
    this.setLayout(borderLayout1);
    labelLearningRate.setVerifyInputWhenFocusTarget(true);
    labelLearningRate.setText("Learning Rate:");
    labelLearningRate.setVerticalAlignment(SwingConstants.BOTTOM);
    panelTrainningTime.setLayout(flowLayout4);
    flowLayout4.setAlignment(FlowLayout.LEFT);
    flowLayout4.setHgap(1);
    borderLayout1.setHgap(3);
    borderLayout1.setVgap(0);
    jLabel1.setText("Activation Function Steep:");
    spinnerLearningRate.setModel(learningRateSpinnerModel);
    spinnerLearningRate.setBorder(null);
    spinnerLearningRate.setDebugGraphicsOptions(0);
    spinnerMomentum.setModel(momentumSpinnerModel);
    spinnerMomentum.setBorder(null);
    spinnerMomentum.setDebugGraphicsOptions(0);
    spinnerSteep.setModel(steepSpinnerModel);
    spinnerSteep.setBorder(null);
    spinnerHiddenSize.setModel(hiddenSizeSpinnerModel);
    spinnerHiddenSize.setBorder(null);
    jPanel2.add(labelLearningRate, null);
    jPanel2.add(spinnerLearningRate, null);
    jPanel2.add(labelMomentum, null);
    jPanel2.add(spinnerMomentum, null);
    jPanel2.add(LabelHiddenSize, null);
    jPanel2.add(spinnerHiddenSize, null);
    jPanel2.add(labelTrainningTime, null);
    jPanel2.add(panelTrainningTime, null);
    panelTrainningTime.add(fieldTrainningTime, null);
    jPanel2.add(labelActivationFunction, null);
    jPanel2.add(panelActivationFunction, null);
    panelActivationFunction.add(comboActivationFunction, null);
    jPanel2.add(jLabel1, null);
    jPanel2.add(spinnerSteep, null);
    this.add(jPanel2,  BorderLayout.CENTER);
    comboActivationFunction.insertItemAt("Sigmoid", NeuralNetwork.SIGMOID);
    comboActivationFunction.insertItemAt("Tanh", NeuralNetwork.TANH);
    this.setEnabled(false);

    fieldTrainningTime.setText("400");
    comboActivationFunction.setSelectedIndex(0);

  }

  public void setEnabled(boolean enable){
    spinnerLearningRate.setEnabled(enable);
    spinnerMomentum.setEnabled(enable);
    spinnerHiddenSize.setEnabled(enable);
    spinnerSteep.setEnabled(enable);
    fieldTrainningTime.setEditable(enable);
    comboActivationFunction.setEnabled(enable);
  }

  public float getLearningRate() throws NumberFormatException{
    return Float.parseFloat(spinnerLearningRate.getValue().toString());
  }

  public float getMomentum() throws NumberFormatException{
    return Float.parseFloat(spinnerMomentum.getValue().toString());
  }

  public String getTrainningTime(){
    return fieldTrainningTime.getText();
  }

  public int getHiddenLayerSize() throws NumberFormatException{
    return Integer.parseInt(spinnerHiddenSize.getValue().toString());
  }

  public int getSelectedActivationFunction(){
    if(comboActivationFunction.getSelectedIndex() == NeuralNetwork.SIGMOID){
      return NeuralNetwork.SIGMOID;
    } else if(comboActivationFunction.getSelectedIndex() == NeuralNetwork.TANH){
      return NeuralNetwork.TANH;
    }
    return NeuralNetwork.SIGMOID;
  }
/*
  public void setLearningRate(String value){
    spinnerLearningRate.setText(value);
  }

  public void setMomentum(String value){
    spinnerMomentum.setText(value);
  }

   public void setHiddenLayerSize(int value){
     fieldHiddenSize.setText("" + value);
   }
*/
  public void getTrainningTime(String value){
    fieldTrainningTime.setText(value);
  }
}