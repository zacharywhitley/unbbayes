package unbbayes.datamining.gui.neuralnetwork;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import unbbayes.datamining.classifiers.*;

/**
 * Class that implements a panel with the basic options used in a
 * neural network using backpropagation algorithm.
 *
 * @author Rafael Moraes Noivo
 * @version $1.0 $ (06/26/2003)
 */
public class OptionsPanel extends JPanel {
  private ResourceBundle resource;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel labelMomentum = new JLabel();
  private JComboBox comboActivationFunction = new JComboBox();
  private JLabel labelActivationFunction = new JLabel();
  private JPanel panelOptions = new JPanel();
  private JLabel labelLearningRate = new JLabel();
  private JPanel panelActivationFunction = new JPanel();
  private SpinnerNumberModel learningRateSpinnerModel = new SpinnerNumberModel(0.3, 0.01, 1.0, 0.01);
  private SpinnerNumberModel momentumSpinnerModel = new SpinnerNumberModel(0.2, 0.0, 1.0, 0.01);
  private JSpinner spinnerLearningRate = new JSpinner();
  private JSpinner spinnerMomentum = new JSpinner();
  private JPanel panelLearningRate = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel panelMomentum = new JPanel();
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout4 = new BorderLayout();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  public OptionsPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.neuralnetwork.resources.NeuralNetworkResource");
    panelOptions.setBorder(BorderFactory.createEtchedBorder());
    panelOptions.setLayout(gridBagLayout1);
    labelActivationFunction.setText(resource.getString("activationFunctionLabel") + ":");
    labelActivationFunction.setVerticalAlignment(SwingConstants.BOTTOM);
    labelMomentum.setText(resource.getString("momentumLabel") + ":");
    labelMomentum.setVerticalAlignment(SwingConstants.BOTTOM);
    this.setLayout(borderLayout1);
    labelLearningRate.setVerifyInputWhenFocusTarget(true);
    labelLearningRate.setText(resource.getString("learningRateLabel") + ":");
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
    comboActivationFunction.insertItemAt(resource.getString("sigmoid"), NeuralNetwork.SIGMOID);
    comboActivationFunction.insertItemAt(resource.getString("tanh"), NeuralNetwork.TANH);
    this.setEnabled(false);
    comboActivationFunction.setSelectedIndex(0);
  }

  /**
   * Enables or disables the panel.
   *
   * @param enable A boolean informing if the panel should be enabled or disabled
   */
  public void setEnabled(boolean enable){
    spinnerLearningRate.setEnabled(enable);
    spinnerMomentum.setEnabled(enable);
    comboActivationFunction.setEnabled(enable);
  }

  /**
   * Method used to get the learning rate selected by the user
   *
   * @return The learning rate
   * @throws NumberFormatException If the user typed an invalid number
   */
  public float getLearningRate() throws NumberFormatException{
    return Float.parseFloat(spinnerLearningRate.getValue().toString());
  }

  /**
   * Method used to get the momentum selected by the user
   *
   * @return The momentum
   * @throws NumberFormatException If the user typed an invalid number
   */
  public float getMomentum() throws NumberFormatException{
    return Float.parseFloat(spinnerMomentum.getValue().toString());
  }

  /**
   * Method used to get the type of activation function selected by the user
   *
   * @return The selected type of activation function.
   */
  public int getSelectedActivationFunction(){
    if(comboActivationFunction.getSelectedIndex() == NeuralNetwork.SIGMOID){
      return NeuralNetwork.SIGMOID;
    } else if(comboActivationFunction.getSelectedIndex() == NeuralNetwork.TANH){
      return NeuralNetwork.TANH;
    }
    return NeuralNetwork.SIGMOID;
  }
}