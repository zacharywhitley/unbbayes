package unbbayes.datamining.gui.neuralnetwork;

import java.awt.*;
import javax.swing.*;

import unbbayes.datamining.classifiers.*;

public class OptionsPanel extends JPanel {
  BorderLayout borderLayout1 = new BorderLayout();
  JLabel labelMomentum = new JLabel();
  JComboBox comboActivationFunction = new JComboBox();
  JLabel labelActivationFunction = new JLabel();
  JPanel panelOptions = new JPanel();
  JLabel labelLearningRate = new JLabel();
  JPanel panelActivationFunction = new JPanel();
  SpinnerNumberModel learningRateSpinnerModel = new SpinnerNumberModel(0.1, 0.01, 1.0, 0.01);
  SpinnerNumberModel momentumSpinnerModel = new SpinnerNumberModel(0.5, 0.0, 1.0, 0.1);
  JSpinner spinnerLearningRate = new JSpinner();
  JSpinner spinnerMomentum = new JSpinner();
  JPanel panelLearningRate = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  JPanel panelMomentum = new JPanel();
  BorderLayout borderLayout3 = new BorderLayout();
  BorderLayout borderLayout4 = new BorderLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  public OptionsPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    panelOptions.setBorder(BorderFactory.createEtchedBorder());
    panelOptions.setLayout(gridBagLayout1);
    labelActivationFunction.setText("Activation Function:");
    labelActivationFunction.setVerticalAlignment(SwingConstants.BOTTOM);
    labelMomentum.setText("Momentum:");
    labelMomentum.setVerticalAlignment(SwingConstants.BOTTOM);
    this.setLayout(borderLayout1);
    labelLearningRate.setVerifyInputWhenFocusTarget(true);
    labelLearningRate.setText("Learning Rate:");
    labelLearningRate.setVerticalAlignment(SwingConstants.BOTTOM);
    borderLayout1.setHgap(3);
    borderLayout1.setVgap(0);
    spinnerLearningRate.setModel(learningRateSpinnerModel);
    spinnerLearningRate.setBorder(null);
    spinnerLearningRate.setDebugGraphicsOptions(0);
    spinnerMomentum.setModel(momentumSpinnerModel);
    spinnerMomentum.setBorder(null);
    spinnerMomentum.setDebugGraphicsOptions(0);
    panelLearningRate.setLayout(borderLayout2);
    panelMomentum.setLayout(borderLayout3);
    panelActivationFunction.setLayout(borderLayout4);
    panelActivationFunction.add(comboActivationFunction, BorderLayout.CENTER);
    panelActivationFunction.add(labelActivationFunction,  BorderLayout.NORTH);
    this.add(panelOptions,  BorderLayout.CENTER);
    panelLearningRate.add(labelLearningRate, BorderLayout.NORTH);
    panelLearningRate.add(spinnerLearningRate, BorderLayout.CENTER);
    panelOptions.add(panelLearningRate,   new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 67, 0));
    panelOptions.add(panelMomentum,   new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 83, 0));
    panelOptions.add(panelActivationFunction,   new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 42, 0));
    panelMomentum.add(labelMomentum, BorderLayout.NORTH);
    panelMomentum.add(spinnerMomentum, BorderLayout.CENTER);
    comboActivationFunction.insertItemAt("Sigmoid", NeuralNetwork.SIGMOID);
    comboActivationFunction.insertItemAt("Tanh", NeuralNetwork.TANH);
    this.setEnabled(false);
    comboActivationFunction.setSelectedIndex(0);
  }

  public void setEnabled(boolean enable){
    spinnerLearningRate.setEnabled(enable);
    spinnerMomentum.setEnabled(enable);
    comboActivationFunction.setEnabled(enable);
  }

  public float getLearningRate() throws NumberFormatException{
    return Float.parseFloat(spinnerLearningRate.getValue().toString());
  }

  public float getMomentum() throws NumberFormatException{
    return Float.parseFloat(spinnerMomentum.getValue().toString());
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