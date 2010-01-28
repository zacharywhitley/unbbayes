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
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import unbbayes.controller.IconController;
import unbbayes.datamining.classifiers.NeuralNetwork;
import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.Utils;
import unbbayes.datamining.gui.AttributesTree;
import unbbayes.datamining.gui.IInferencePanel;

/**
 *  Class that implements the the panel used to make inferences on the model.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class InferencePanel extends JPanel implements IInferencePanel{

  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;	
	
  private ResourceBundle resource;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private JPanel jPanel2 = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel jPanel3 = new JPanel();
  private JPanel jPanel4 = new JPanel();
  private JPanel panelMessages = new JPanel();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JScrollPane treeScrollPane/* = new JScrollPane()*/;
  private BorderLayout borderLayout4 = new BorderLayout();
  private AttributesTree attributesTree;
  private JSplitPane splitPane1 = new JSplitPane();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JToolBar jToolBar1 = new JToolBar();
  private JButton expandButton = new JButton();
  private JButton colapseButton = new JButton();
  private JButton classifyButton = new JButton();
  private Icon colapseIcon;
  private Icon expandIcon;
  private Icon propagateIcon;
  private DecimalFormat numFormat = new DecimalFormat("##0.0");
  private BorderLayout borderLayout6 = new BorderLayout();
  private JSplitPane splitPane2 = new JSplitPane();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private JScrollPane jScrollPane3 = new JScrollPane();
  private JTextPane textPaneResults = new JTextPane();
  private JTextPane textPaneRules = new JTextPane();
  private NeuralNetwork neuralNetwork;
  protected IconController iconController = IconController.getInstance();

  public InferencePanel() {
    try {
      resource = unbbayes.util.ResourceController.newInstance().getBundle(
    		  unbbayes.datamining.gui.neuralnetwork.resources.NeuralNetworkResource.class.getName());
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    colapseIcon = iconController.getColapseIcon();
    expandIcon = iconController.getExpandIcon();
    propagateIcon = iconController.getPropagateIcon();
    this.setLayout(borderLayout1);
    jPanel1.setLayout(borderLayout5);
    jPanel2.setLayout(borderLayout2);
    panelMessages.setLayout(borderLayout3);
    jPanel3.setLayout(borderLayout4);
    expandButton.setToolTipText(resource.getString("expandToolTip"));
    expandButton.setIcon(expandIcon);
    expandButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        expandButton_actionPerformed(e);
      }
    });
    colapseButton.setToolTipText(resource.getString("collapseToolTip"));
    colapseButton.setIcon(colapseIcon);
    colapseButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        colapseButton_actionPerformed(e);
      }
    });
    classifyButton.setToolTipText(resource.getString("inference"));
    classifyButton.setIcon(propagateIcon);
    classifyButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        classifyButton_actionPerformed(e);
      }
    });
    jPanel4.setLayout(borderLayout6);
    splitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
    textPaneResults.setEditable(false);
    textPaneRules.setEditable(false);
    this.add(jPanel1,  BorderLayout.CENTER);
    jPanel2.add(jPanel3,  BorderLayout.CENTER);
    splitPane1.add(jPanel4, JSplitPane.RIGHT);
    jPanel4.add(splitPane2,  BorderLayout.CENTER);
    jPanel1.add(splitPane1, BorderLayout.CENTER);
    splitPane1.add(jPanel2, JSplitPane.LEFT);
    this.add(panelMessages,  BorderLayout.SOUTH);
    this.add(jToolBar1, BorderLayout.NORTH);
    jToolBar1.add(colapseButton, null);
    jToolBar1.add(expandButton, null);
    jToolBar1.add(classifyButton, null);
    splitPane2.add(jScrollPane2, JSplitPane.TOP);
    jScrollPane2.getViewport().add(textPaneResults, null);
    splitPane2.add(jScrollPane3, JSplitPane.BOTTOM);
    jScrollPane3.getViewport().add(textPaneRules, null);
    jToolBar1.setFloatable(false);
    splitPane1.setDividerLocation(20);
  }

  /**
   * Methdo used to set model that will be used to make the inferences.
   *
   * @param combinatorialNetwork the model that will be used to make the inferences.
   */
  public void setNetwork(NeuralNetwork neuralNetwork){
    this.neuralNetwork = neuralNetwork;
    attributesTree = new AttributesTree();
    attributesTree.setAttributes(neuralNetwork.getAttributeVector(), neuralNetwork.getClassIndex());
    attributesTree.expandTree();
    attributesTree.setController(this);

    splitPane1.setDividerLocation(0.4);
    splitPane2.setDividerLocation(0.6);
    treeScrollPane = new JScrollPane(attributesTree);
    jPanel3.removeAll();
    jPanel3.add(treeScrollPane,  BorderLayout.CENTER);
    jPanel3.updateUI();

    textPaneResults.setText("");
    textPaneRules.setText("");
  }

  private void printResults(float[] results, Instance instance){
//    float[] distributionNormalized = new float[results.length];
    Attribute[] attributeVector = neuralNetwork.getAttributeVector();
    Attribute classAtt, att;
    String[] initString, initStyles;
    int maxValue;
    Document docResults;
    DecimalFormat numFormat = new DecimalFormat("##0.000");

//    for(int i=0; i<results.length; i++){
//      distributionNormalized[i] = results[i];
//    }
//    Utils.normalize(distributionNormalized);

    classAtt = attributeVector[neuralNetwork.getClassIndex()];

    initString = new String[results.length + 1];
    initString[0] = resource.getString("class") + ": " + classAtt.getAttributeName() + "\n";
    for(int i=0; i<results.length; i++){
      initString[i + 1] = "- " + classAtt.value(i) +
          ":  " + numFormat.format(results[i]) +
        "\n"; // "    " + numFormat.format(distributionNormalized[i] * 100) + "%\n";
    }

    initStyles = new String[initString.length];
    initStyles[0] = "largeBold";
    maxValue = Utils.maxIndex(results);
    for(int i=0; i<results.length; i++){
      if(i == maxValue){
        initStyles[i+1] = "bold";
      } else {
        initStyles[i+1] = "regular";
      }
    }

    initStylesForTextPane(textPaneResults);
    docResults = textPaneResults.getDocument();
    try {
      docResults.remove(0, docResults.getLength());
      for (int i=0; i<initString.length; i++) {
        docResults.insertString(docResults.getLength(), initString[i], textPaneResults.getStyle(initStyles[i]));
      }
    } catch (BadLocationException ble) {
      System.out.println("InferencePanel - Couldn't insert initial text.");
    }
  }

  private void initStylesForTextPane(JTextPane textPane) {
      Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

      Style regular = textPane.addStyle("regular", def);
      StyleConstants.setFontFamily(def, "SansSerif");
      StyleConstants.setFontSize(def, 16);

      Style s = textPane.addStyle("italic", regular);
      StyleConstants.setItalic(s, true);

      s = textPane.addStyle("bold", regular);
      StyleConstants.setBold(s, true);

      s = textPane.addStyle("large", regular);
      StyleConstants.setFontSize(s, 18);

      s = textPane.addStyle("largeBold", s);
      StyleConstants.setBold(s, true);
  }

  /**
   * Method used to print the rule that classified the instance.
   *
   * @param tree the tree used to insert a new instance for classification.
   */
  public void printSelectedAttributes(Instance instance){
    Attribute[] attArray = neuralNetwork.getAttributeVector();
    String rule = new String();
    int classIndex = neuralNetwork.getClassIndex();
    int numAtt = attArray.length;
    Document docRules = textPaneRules.getDocument();
    initStylesForTextPane(textPaneRules);
	Attribute att;
	String value;
	
	for (int attIndex = 0; attIndex < numAtt; attIndex++) {
		if (attIndex != classIndex && !instance.isMissing(attIndex)) {
			att = attArray[attIndex];
			if (att.isNominal()) {
				value = att.value((int) instance.getValue(attIndex));
			} else {
				value = String.valueOf(instance.getValue(attIndex)) ;
			}
			rule = rule + att.getAttributeName() + ": " + value + "\n";
		}
	}

    try {
      docRules.remove(0, docRules.getLength());
      docRules.insertString(docRules.getLength(), rule, textPaneRules.getStyle( /*initStyles[i]*/"bold"));
    }
    catch (BadLocationException ble) {
      System.out.println("InferencePanel - Couldn't insert initial text.");
    }
  }

  private void classifyButton_actionPerformed(ActionEvent e) {
    try{
      Instance instance = attributesTree.getInstance();
      printResults(neuralNetwork.distributionForInstance(instance), instance);
    } catch(Exception exception){
      System.out.println(exception);
      exception.printStackTrace();
    }
  }

  private void expandButton_actionPerformed(ActionEvent e) {
    attributesTree.expandTree();
    repaint();
  }

  private void colapseButton_actionPerformed(ActionEvent e) {
    attributesTree.collapseTree();
  }
}
