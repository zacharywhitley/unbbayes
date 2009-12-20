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
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import unbbayes.datamining.classifiers.NeuralNetwork;

/**
 *  Class that implements the the panel used to set advanced options.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (06/26/2003)
 */
public class AdvancedOptionsPanel extends JPanel {
	
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;	
	
  private ResourceBundle resource;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel advancedOptions = new JPanel();
  private JPanel learningRatePanel = new JPanel();
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
  private SpinnerNumberModel hiddenLayerSizeSpinnerModel = new SpinnerNumberModel(10, 2, 1000, 1);
  private SpinnerNumberModel trainningTimeSpinnerModel = new SpinnerNumberModel(400, 1, 10000, 1);
  private SpinnerNumberModel minimumErrorVariationSpinnerModel = new SpinnerNumberModel(0.1, 0.00001, 1, 0.001);
  private SpinnerNumberModel activationFunctionSteepSpinnerModel = new SpinnerNumberModel(1, 0.1, 2, 0.1);

  private boolean learningRateDecay = false;
  private int numericalInputNormalization = NeuralNetwork.NO_NORMALIZATION;
  private boolean autoHiddenLayerSize = true;
  private int hiddenLayerSize = 10;
  private int trainningTime = 400;
  private boolean minimumError = false;
  private double minimumErrorVariation = 0.1;
  private double activationFunctionSteep = 1;

  private boolean defaultLearningRateDecay = false;
  private boolean defaultNumericalInputNormalization = false;
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
  private Border border5;
  private TitledBorder titledBorder5;
  private JCheckBox errorVariationCheckBox = new JCheckBox();
  private JPanel inputNormalizationPanel = new JPanel();
  private BorderLayout borderLayout8 = new BorderLayout();
  private JCheckBox normalizationCheckBox = new JCheckBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JPanel normalizationPanel = new JPanel();
  private Border border6;
  private TitledBorder titledBorder6;
  private BorderLayout borderLayout9 = new BorderLayout();
  private JPanel jPanel6 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private JRadioButton radioLinearNormalization = new JRadioButton();
  private JRadioButton radioMean0SD1 = new JRadioButton();
  private ButtonGroup normalizationGroup = new ButtonGroup();

  public AdvancedOptionsPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    resource = unbbayes.util.ResourceController.newInstance().getBundle(
    		unbbayes.datamining.gui.neuralnetwork.resources.NeuralNetworkResource.class.getName());
    fuctionSteepSpinner.setModel(activationFunctionSteepSpinnerModel);
    errorVariationSpinner.setModel(minimumErrorVariationSpinnerModel);
    trainningTimeSpinner.setModel(trainningTimeSpinnerModel);
    hiddenSizeSpinner.setModel(hiddenLayerSizeSpinnerModel);
    border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder1 = new TitledBorder(border1,resource.getString("learningRateLabel"));
    titledBorder1.setTitleFont(new java.awt.Font("Dialog", 0, 12));
    border2 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder2 = new TitledBorder(border2,resource.getString("hiddenLayerSize"));
    titledBorder2.setTitleFont(new java.awt.Font("Dialog", 0, 12));
    border3 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder3 = new TitledBorder(border3,resource.getString("activationFunctionSteep"));
    titledBorder3.setTitleFont(new java.awt.Font("Dialog", 0, 12));
    border4 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder4 = new TitledBorder(border4,resource.getString("learningStopCondition"));
    titledBorder4.setTitleFont(new java.awt.Font("Dialog", 0, 12));
    border5 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder5 = new TitledBorder(border5,resource.getString("numericInput"));
    titledBorder5.setTitleFont(new java.awt.Font("Dialog", 0, 12));
    border6 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder6 = new TitledBorder(border6,resource.getString("normalizationAlgorithm"));
    titledBorder6.setTitleFont(new java.awt.Font("Dialog", 0, 12));
    this.setLayout(borderLayout1);
    advancedOptions.setLayout(gridBagLayout1);
    learningRatePanel.setLayout(borderLayout2);
    learningRatePanel.setBorder(titledBorder1);
    learningRateCheckBox.setFont(new java.awt.Font("Dialog", 0, 12));
    learningRateCheckBox.setText(resource.getString("learningRateDecay"));
    hiddenLayerPanel.setLayout(borderLayout3);
    hiddenLayerPanel.setBorder(titledBorder2);
    jPanel1.setLayout(gridLayout2);
    gridLayout2.setColumns(1);
    gridLayout2.setRows(2);
    jPanel2.setLayout(borderLayout4);
    jPanel3.setLayout(borderLayout5);
    hiddenLayerCheckBox.setFont(new java.awt.Font("Dialog", 0, 12));
    hiddenLayerCheckBox.setText(resource.getString("auto"));
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
    labelTimeLimit.setText(resource.getString("limitOfEpochs") + ":");
    activationFunctionSteepPanel.setBorder(titledBorder3);
    activationFunctionSteepPanel.setLayout(borderLayout7);
    trainningTimePanel.setBorder(titledBorder4);
    trainningTimePanel.setDebugGraphicsOptions(0);
    trainningTimeSpinner.setBorder(null);
    errorVariationSpinner.setBorder(null);
    errorVariationSpinner.setEnabled(false);
    fuctionSteepSpinner.setBorder(null);
    errorVariationCheckBox.setFont(new java.awt.Font("Dialog", 0, 12));
    errorVariationCheckBox.setText(resource.getString("relativeError"));
    errorVariationCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        errorVariationCheckBox_actionPerformed(e);
      }
    });
    inputNormalizationPanel.setLayout(borderLayout8);
    inputNormalizationPanel.setBorder(titledBorder5);
    normalizationCheckBox.setFont(new java.awt.Font("Dialog", 0, 12));
    normalizationCheckBox.setText(resource.getString("normalizeNumericInput"));
    normalizationCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        normalizationCheckBox_actionPerformed(e);
      }
    });
    normalizationPanel.setBorder(titledBorder6);
    normalizationPanel.setLayout(borderLayout9);
    jPanel6.setLayout(gridLayout1);
    gridLayout1.setRows(2);
    radioLinearNormalization.setEnabled(false);
    radioLinearNormalization.setFont(new java.awt.Font("Dialog", 0, 12));
    radioLinearNormalization.setSelectedIcon(null);
    radioLinearNormalization.setText(resource.getString("linearNormalization"));
    radioMean0SD1.setEnabled(false);
    radioMean0SD1.setFont(new java.awt.Font("Dialog", 0, 12));
    radioMean0SD1.setText(resource.getString("mean0StandardDeviation1"));
    jPanel1.add(jPanel2, null);
    jPanel2.add(hiddenLayerCheckBox, BorderLayout.CENTER);
    jPanel1.add(jPanel3, null);
    jPanel3.add(hiddenSizeSpinner, BorderLayout.CENTER);
    trainningTimePanel.add(jPanel4, BorderLayout.CENTER);
    activationFunctionSteepPanel.add(fuctionSteepSpinner, BorderLayout.CENTER);
    hiddenLayerPanel.add(jPanel1, BorderLayout.CENTER);
    learningRatePanel.add(learningRateCheckBox, BorderLayout.CENTER);
    this.add(advancedOptions,  BorderLayout.CENTER);
    jPanel4.add(labelTimeLimit, null);
    jPanel4.add(trainningTimeSpinner, null);
    jPanel4.add(errorVariationCheckBox, null);
    jPanel4.add(errorVariationSpinner, null);
    advancedOptions.add(learningRatePanel,   new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 127, 0));
    advancedOptions.add(inputNormalizationPanel,   new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 107, 0));
    advancedOptions.add(hiddenLayerPanel,   new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 193, 0));
    advancedOptions.add(trainningTimePanel,   new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 7, 0));
    advancedOptions.add(activationFunctionSteepPanel,   new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 237, 0));
    inputNormalizationPanel.add(normalizationCheckBox, BorderLayout.NORTH);
    inputNormalizationPanel.add(normalizationPanel,  BorderLayout.CENTER);
    normalizationPanel.add(jPanel6,  BorderLayout.CENTER);
    jPanel6.add(radioLinearNormalization, null);
    jPanel6.add(radioMean0SD1, null);

    startDefaultValues();
    normalizationGroup.add(radioLinearNormalization);
    normalizationGroup.add(radioMean0SD1);

  }

  /**
   * Method used to update the actual parameters by the new ones inserted.
   */
  public void updateValues(){
    learningRateDecay = learningRateCheckBox.isSelected();
    if(normalizationCheckBox.isSelected()){
      if(radioLinearNormalization.isSelected()){
        numericalInputNormalization = NeuralNetwork.LINEAR_NORMALIZATION;
      } else {
        numericalInputNormalization = NeuralNetwork.MEAN_0_STANDARD_DEVIATION_1_NORMALIZATION;
      }
    } else {
      numericalInputNormalization = NeuralNetwork.NO_NORMALIZATION;
    }
    autoHiddenLayerSize = hiddenLayerCheckBox.isSelected();
    hiddenLayerSize = Integer.parseInt(hiddenSizeSpinner.getValue().toString());
    trainningTime = Integer.parseInt(trainningTimeSpinner.getValue().toString());
    minimumError = errorVariationCheckBox.isSelected();
    minimumErrorVariation = Double.parseDouble(errorVariationSpinner.getValue().toString());
    activationFunctionSteep = Double.parseDouble(fuctionSteepSpinner.getValue().toString());
  }

  /**
   * Method that sets the defalt values
   */
  public void startDefaultValues(){
    learningRateCheckBox.setSelected(defaultLearningRateDecay);
    normalizationCheckBox.setSelected(defaultNumericalInputNormalization);
    radioLinearNormalization.setSelected(true);
    hiddenLayerCheckBox.setSelected(defaultAutoHiddenLayerSize);
    hiddenSizeSpinner.setValue(new Integer(defaultHiddenLayerSize));
    trainningTimeSpinner.setValue(new Integer(defaultTrainningTime));
    errorVariationCheckBox.setSelected(defaultMinimumError);
    errorVariationSpinner.setValue(new Double(defaultMinimumErrorVariation));
    fuctionSteepSpinner.setValue(new Double(defaultActivationFunctionSteep));
  }

  /**
   * Method tha sets a hidden layer size.
   *
   * @param hiddenLayerSize The hidden layer size
   */
  public void sethiddenLayerSize(int hiddenLayerSize){
      this.hiddenLayerSize = hiddenLayerSize;
      defaultHiddenLayerSize = hiddenLayerSize;
      hiddenSizeSpinner.setValue(new Integer(hiddenLayerSize));
  }

  /**
   * Method used to see if the learning rate decay was activated
   *
   * @return True if the learning rate decay is active an False otherway.
   */
  public boolean getLearningRateDecayEnabled(){
    return learningRateDecay;
  }

  /**
   * Method used to see if the input normalizatio is active an what
   * normalization method sould be used.
   *
   * @return The normalization method or a value indicating no normalization
   */
  public int getNumericalInputNormalization(){
    return numericalInputNormalization;
  }

  /**
   * Method used to get the hidden layer size defined by the user
   *
   * @return The hidden layer size.
   */
  public int getHiddenLayerSize(){
    if(hiddenLayerCheckBox.isSelected()){
      return NeuralNetwork.AUTO_HIDDEN_LAYER_SIZE;
    } else {
      return hiddenLayerSize;
    }
  }

  /**
   * Method used to get the training time specified by the user
   *
   * @return The training time (in epochs)
   */
  public int getTrainningTime(){
    return trainningTime;
  }

  /**
   * Method that returns the minimum error variation or a value that
   * indicates that the minimum error variation criterion should
   * not be used.
   *
   * @return The minimum error variation or a value that indicates that the
   * minimum error variation criterion should not be used.
   */
  public double getMinimumErrorVariation(){
    if(errorVariationCheckBox.isSelected()){
      return minimumErrorVariation;
    } else {
      return NeuralNetwork.NO_ERROR_VARIATION_STOP_CRITERION;
    }
  }

  /**
   * Method that returns the activation function steep.
   *
   * @return The activation function steep
   */
  public double getActivationFunctionSteep(){
    return activationFunctionSteep;
  }

  void hiddenLayerCheckBox_actionPerformed(ActionEvent e) {
    hiddenSizeSpinner.setEnabled(!hiddenLayerCheckBox.isSelected());
    hiddenSizeSpinner.setValue(new Integer(defaultHiddenLayerSize));
  }

  void errorVariationCheckBox_actionPerformed(ActionEvent e) {
    errorVariationSpinner.setEnabled(errorVariationCheckBox.isSelected());
  }

  void normalizationCheckBox_actionPerformed(ActionEvent e) {
    radioLinearNormalization.setEnabled(normalizationCheckBox.isSelected());
    radioMean0SD1.setEnabled(normalizationCheckBox.isSelected());
  }
}