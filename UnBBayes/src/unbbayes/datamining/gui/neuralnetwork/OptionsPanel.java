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
    labelActivationFunction.setText("Activation Function:");
    labelActivationFunction.setVerticalAlignment(SwingConstants.BOTTOM);
    fieldHiddenSize.setText("");
    fieldTrainningTime.setText("");
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
    jPanel2.add(labelLearningRate, null);
    jPanel2.add(fieldLearningRate, null);
    jPanel2.add(labelMomentum, null);
    jPanel2.add(fieldMomentum, null);
    jPanel2.add(LabelHiddenSize, null);
    jPanel2.add(fieldHiddenSize, null);
    jPanel2.add(labelTrainningTime, null);
    jPanel2.add(fieldTrainningTime, null);
    jPanel2.add(labelActivationFunction, null);
    jPanel2.add(comboActivationFunction, null);
    this.add(jPanel2,  BorderLayout.CENTER);

    comboActivationFunction.insertItemAt("Sigmoid", NeuralNetwork.SIGMOID);
    comboActivationFunction.insertItemAt("Tanh", NeuralNetwork.TANH);
  }

  public void enable(boolean enable){
    this.setEnabled(enable);
  }

  public float getLearningRate() throws NumberFormatException{
    return Float.parseFloat(fieldLearningRate.getText());
  }

  public float getMomentum() throws NumberFormatException{
    return Float.parseFloat(fieldMomentum.getText());
  }

  public float getTrainingTime() throws NumberFormatException{
    return Float.parseFloat(fieldTrainningTime.getText());
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