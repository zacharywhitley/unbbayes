package unbbayes.datamining.gui.neuralnetwork;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import unbbayes.datamining.classifiers.NeuralNetwork;

public class AdvancedOptionsPanel extends JPanel {
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel advancedOptions = new JPanel();
  private JPanel learningRate = new JPanel();
  private JPanel hiddenLayerPanel = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JCheckBox learningRateCheckBox = new JCheckBox();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private GridLayout gridLayout2 = new GridLayout();
  private JPanel jPanel2 = new JPanel();
  private JPanel jPanel3 = new JPanel();
  private BorderLayout borderLayout4 = new BorderLayout();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JCheckBox hiddenLayerCheckBox = new JCheckBox();
  private JSpinner hiddenSizeSpinner = new JSpinner();
  private JPanel trainningTimePanel = new JPanel();
  private BorderLayout borderLayout6 = new BorderLayout();
  private JPanel jPanel4 = new JPanel();
  private GridLayout gridLayout3 = new GridLayout();
  private JLabel labelTimeLimit = new JLabel();
  private JSpinner trainningTimeSpinner = new JSpinner();
  private JSpinner errorVariationSpinner = new JSpinner();
  private JPanel activationFunctionSteepPanel = new JPanel();
  private BorderLayout borderLayout7 = new BorderLayout();
  private JSpinner fuctionSteepSpinner = new JSpinner();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private SpinnerNumberModel hiddenLayerSizeSpinnerModel = new SpinnerNumberModel(10, 2, 1000, 1);
  private SpinnerNumberModel trainningTimeSpinnerModel = new SpinnerNumberModel(400, 1, 10000, 1);
  private SpinnerNumberModel minimumErrorVariationSpinnerModel = new SpinnerNumberModel(0.1, 0.00001, 1, 0.001);
  private SpinnerNumberModel activationFunctionSteepSpinnerModel = new SpinnerNumberModel(1, 0.1, 2, 0.1);

  private boolean learningRateDecay = false;
  private boolean autoHiddenLayerSize = true;
  private int hiddenLayerSize = 10;
  private int trainningTime = 400;
  private boolean minimumError = false;
  private double minimumErrorVariation = 0.1;
  private double activationFunctionSteep = 1;

  private boolean defaultLearningRateDecay = false;
  private boolean defaultAutoHiddenLayerSize = true;
  private int defaultHiddenLayerSize = 10;
  private int defaultTrainningTime = 400;
  private boolean defaultMinimumError = false;
  private double defaultMinimumErrorVariation = 0.1;
  private double defaultActivationFunctionSteep = 1;

  private Border border1;
  private TitledBorder titledBorder1;
  private Border border2;
  private TitledBorder titledBorder2;
  private Border border3;
  private TitledBorder titledBorder3;
  private Border border4;
  private TitledBorder titledBorder4;
  private JCheckBox errorVariationCheckBox = new JCheckBox();

  public AdvancedOptionsPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    fuctionSteepSpinner.setModel(activationFunctionSteepSpinnerModel);
    errorVariationSpinner.setModel(minimumErrorVariationSpinnerModel);
    trainningTimeSpinner.setModel(trainningTimeSpinnerModel);
    hiddenSizeSpinner.setModel(hiddenLayerSizeSpinnerModel);
    border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder1 = new TitledBorder(border1,"Learning Rate:");
    titledBorder1.setTitleFont(new java.awt.Font("Dialog", 0, 12));
    border2 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder2 = new TitledBorder(border2,"Hidden Layer Size:");
    titledBorder2.setTitleFont(new java.awt.Font("Dialog", 0, 12));
    border3 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder3 = new TitledBorder(border3,"Activation Function Steep:");
    titledBorder3.setTitleFont(new java.awt.Font("Dialog", 0, 12));
    border4 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder4 = new TitledBorder(border4,"Learning Stop Condition:");
    titledBorder4.setTitleFont(new java.awt.Font("Dialog", 0, 12));
    this.setLayout(borderLayout1);
    advancedOptions.setLayout(gridBagLayout1);
    learningRate.setLayout(borderLayout2);
    learningRate.setFont(new java.awt.Font("Dialog", 0, 12));
    learningRate.setBorder(titledBorder1);
    learningRateCheckBox.setFont(new java.awt.Font("Dialog", 0, 12));
    learningRateCheckBox.setText("Learning Rate Decay");
    hiddenLayerPanel.setLayout(borderLayout3);
    hiddenLayerPanel.setBorder(titledBorder2);
    jPanel1.setLayout(gridLayout2);
    gridLayout2.setColumns(1);
    gridLayout2.setRows(2);
    jPanel2.setLayout(borderLayout4);
    jPanel3.setLayout(borderLayout5);
    hiddenLayerCheckBox.setFont(new java.awt.Font("Dialog", 0, 12));
    hiddenLayerCheckBox.setText("Auto");
    hiddenLayerCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hiddenLayerCheckBox_actionPerformed(e);
      }
    });
    hiddenSizeSpinner.setBorder(null);
    hiddenSizeSpinner.setEnabled(false);
    trainningTimePanel.setLayout(borderLayout6);
    jPanel4.setLayout(gridLayout3);
    gridLayout3.setRows(4);
    labelTimeLimit.setFont(new java.awt.Font("Dialog", 0, 12));
    labelTimeLimit.setText("Limit of Epochs:");
    activationFunctionSteepPanel.setBorder(titledBorder3);
    activationFunctionSteepPanel.setLayout(borderLayout7);
    trainningTimePanel.setFont(new java.awt.Font("Dialog", 0, 12));
    trainningTimePanel.setBorder(titledBorder4);
    trainningTimePanel.setDebugGraphicsOptions(0);
    trainningTimeSpinner.setBorder(null);
    errorVariationSpinner.setBorder(null);
    errorVariationSpinner.setEnabled(false);
    fuctionSteepSpinner.setBorder(null);
    errorVariationCheckBox.setFont(new java.awt.Font("Dialog", 0, 12));
    errorVariationCheckBox.setText("Relative Error of the Mean Square Error (%)");
    errorVariationCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        errorVariationCheckBox_actionPerformed(e);
      }
    });
    jPanel1.add(jPanel2, null);
    jPanel2.add(hiddenLayerCheckBox, BorderLayout.CENTER);
    jPanel1.add(jPanel3, null);
    jPanel3.add(hiddenSizeSpinner, BorderLayout.CENTER);
    hiddenLayerPanel.add(jPanel1, BorderLayout.CENTER);
    learningRate.add(learningRateCheckBox, BorderLayout.CENTER);
    this.add(advancedOptions,  BorderLayout.CENTER);
    trainningTimePanel.add(jPanel4,  BorderLayout.CENTER);
    jPanel4.add(labelTimeLimit, null);
    jPanel4.add(trainningTimeSpinner, null);
    jPanel4.add(errorVariationCheckBox, null);
    jPanel4.add(errorVariationSpinner, null);
    advancedOptions.add(learningRate,   new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 61, 1));
    advancedOptions.add(trainningTimePanel,     new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 57, 9));
    advancedOptions.add(activationFunctionSteepPanel,            new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 162, 11));
    advancedOptions.add(hiddenLayerPanel,          new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 99, 6));
    activationFunctionSteepPanel.add(fuctionSteepSpinner,  BorderLayout.CENTER);
    startDefaultValues();

  }

  public void updateValues(){
    learningRateDecay = learningRateCheckBox.isSelected();
    autoHiddenLayerSize = hiddenLayerCheckBox.isSelected();
    hiddenLayerSize = Integer.parseInt(hiddenSizeSpinner.getValue().toString());
    trainningTime = Integer.parseInt(trainningTimeSpinner.getValue().toString());
    minimumError = errorVariationCheckBox.isSelected();
    minimumErrorVariation = Double.parseDouble(errorVariationSpinner.getValue().toString());
    activationFunctionSteep = Double.parseDouble(fuctionSteepSpinner.getValue().toString());
  }

  public void startDefaultValues(){
    learningRateCheckBox.setSelected(defaultLearningRateDecay);
    hiddenLayerCheckBox.setSelected(defaultAutoHiddenLayerSize);
    hiddenSizeSpinner.setValue(new Integer(defaultHiddenLayerSize));
    trainningTimeSpinner.setValue(new Integer(defaultTrainningTime));
    errorVariationCheckBox.setSelected(defaultMinimumError);
    errorVariationSpinner.setValue(new Double(defaultMinimumErrorVariation));
    fuctionSteepSpinner.setValue(new Double(defaultActivationFunctionSteep));
  }

  public void sethiddenLayerSize(int hiddenLayerSize){
      this.hiddenLayerSize = hiddenLayerSize;
      hiddenSizeSpinner.setValue(new Integer(hiddenLayerSize));
  }

  public boolean getLearningRateDecayEnabled(){
    return learningRateDecay;
  }

  public int getHiddenLayerSize(){
    if(hiddenLayerCheckBox.isSelected()){
      return NeuralNetwork.AUTO_HIDDEN_LAYER_SIZE;
    } else {
      return hiddenLayerSize;
    }
  }

  public int getTrainningTime(){
    return trainningTime;
  }

  public double getMinimumErrorVariation(){
    if(errorVariationCheckBox.isSelected()){
      return minimumErrorVariation;
    } else {
      return NeuralNetwork.NO_ERROR_VARIATION_STOP_CRITERION;
    }
  }

  public double getActivationFunctionSteep(){
    return activationFunctionSteep;
  }

  void hiddenLayerCheckBox_actionPerformed(ActionEvent e) {
    hiddenSizeSpinner.setEnabled(!hiddenLayerCheckBox.isSelected());
  }

  void errorVariationCheckBox_actionPerformed(ActionEvent e) {
    errorVariationSpinner.setEnabled(errorVariationCheckBox.isSelected());
  }
}