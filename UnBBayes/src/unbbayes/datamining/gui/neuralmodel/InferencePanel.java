package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import unbbayes.datamining.classifiers.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.datamanipulation.neuralmodel.entities.*;

public class InferencePanel extends JPanel {
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
  private CombinatorialNeuralModel combinatorialNetwork;
  private JScrollPane treeScrollPane/* = new JScrollPane()*/;
  private BorderLayout borderLayout4 = new BorderLayout();
  private InferenceTree inferenceTree;
  private JSplitPane splitPane1 = new JSplitPane();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JToolBar jToolBar1 = new JToolBar();
  private JButton expandButton = new JButton();
  private JButton colapseButton = new JButton();
  private JButton classifyButton = new JButton();
  private Icon colapseIcon;
  private Icon expandIcon;
  private Icon propagateIcon;
  private JTextArea textAreaResults = new JTextArea();
  private DecimalFormat numFormat = new DecimalFormat("##0.0");
  private BorderLayout borderLayout6 = new BorderLayout();
  private JSplitPane splitPane2 = new JSplitPane();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private JScrollPane jScrollPane3 = new JScrollPane();
  private JTextPane textPaneResults = new JTextPane();
  private JTextPane textPaneRules = new JTextPane();

  public InferencePanel() {
    try {
      resource = ResourceBundle.getBundle("unbbayes.datamining.gui.neuralmodel.resources.NeuralModelResource");
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    colapseIcon = new ImageIcon(getClass().getResource("/icons/contract-nodes.gif"));
    expandIcon = new ImageIcon(getClass().getResource("/icons/expand-nodes.gif"));
    propagateIcon = new ImageIcon(getClass().getResource("/icons/propagate.gif"));
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
    textAreaResults.setEditable(false);
    textAreaResults.setRows(3);
    jPanel4.setLayout(borderLayout6);
    splitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
    textPaneResults.setEditable(false);
    textPaneRules.setEditable(false);
    this.add(jPanel1,  BorderLayout.CENTER);
    jPanel2.add(jPanel3,  BorderLayout.CENTER);
//    jPanel3.add(treeScrollPane,  BorderLayout.CENTER);
    splitPane1.add(jPanel4, JSplitPane.RIGHT);
    jPanel4.add(splitPane2,  BorderLayout.CENTER);
    jPanel1.add(splitPane1, BorderLayout.CENTER);
    splitPane1.add(jPanel2, JSplitPane.LEFT);
    this.add(panelMessages,  BorderLayout.SOUTH);
    panelMessages.add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(textAreaResults, null);
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

  public void setNetwork(CombinatorialNeuralModel combinatorialNetwork){
    this.combinatorialNetwork = combinatorialNetwork;
    inferenceTree = new InferenceTree();
    inferenceTree.setAttributes(combinatorialNetwork.getAttributeVector(), combinatorialNetwork.getClassIndex());
    inferenceTree.expandTree();
    inferenceTree.setController(this);

    splitPane1.setDividerLocation(0.4);
    splitPane2.setDividerLocation(0.6);
    treeScrollPane = new JScrollPane(inferenceTree);
    jPanel3.removeAll();
    jPanel3.add(treeScrollPane,  BorderLayout.CENTER);
    jPanel3.updateUI();

    textPaneResults.setText("");
    textPaneRules.setText("");
    textAreaResults.setText("");
  }

  private void printResults(Arc[] results, Instance instance){
    float[] distributionNormalized = new float[results.length];
    Attribute[] attArray = combinatorialNetwork.getAttributeVector();

    for(int i=0; i<results.length; i++){
      distributionNormalized[i] = results[i].getNetWeigth();
    }
    Utils.normalize(distributionNormalized);

    Attribute classAtt = (combinatorialNetwork.getAttributeVector())[combinatorialNetwork.getClassIndex()];

    String[] initString = new String[results.length + 1];
    initString[0] = resource.getString("class") + ": " + classAtt.getAttributeName() + "\n";
    for(int i=0; i<results.length; i++){
      initString[i+1] = "- " + classAtt.value(i) +
                        ":  " + numFormat.format(results[i].getNetWeigth()) +
                        "    " + numFormat.format(distributionNormalized[i] * 100) + "%\n";
    }

    String[] initStyles = new String[initString.length];
    initStyles[0] = "largeBold";
    int maxValue = Utils.maxIndex(distributionNormalized);
    for(int i=0; i<results.length; i++){
      if(i == maxValue){
        initStyles[i+1] = "bold";
      } else {
        initStyles[i+1] = "regular";
      }
    }

    initStylesForTextPane(textPaneResults);
    Document docResults = textPaneResults.getDocument();
    try {
      docResults.remove(0, docResults.getLength());
      for (int i=0; i<initString.length; i++) {
        docResults.insertString(docResults.getLength(), initString[i], textPaneResults.getStyle(initStyles[i]));
      }
    } catch (BadLocationException ble) {
      System.out.println("InferencePanel - Couldn't insert initial text.");
    }


    //codigo para escrever a regra utilizada
    InputNeuron[] inputList;
    Attribute att;

    if(results[maxValue].getCombinationNeuron() instanceof InputNeuron){ // se o neuronio de combinaçao for de entrada
      inputList = new InputNeuron[1];
      inputList[0] = (InputNeuron)results[maxValue].getCombinationNeuron();
    } else {                                            //se o neuronio de combinaçao for combinatorial
      inputList = ((CombinatorialNeuron)results[maxValue].getCombinationNeuron()).getInputList();
    }

    String rule = resource.getString("if") + " ";
    for(int i=0; i<inputList.length; i++){
      att = attArray[inputList[i].getAttributeIndex()];

      rule = rule + att.getAttributeName() + " = " + att.value(inputList[i].getValue()) + " ";
      if(i < (inputList.length - 1)){
        rule = rule + " " + resource.getString("and") + " ";
      }
    }
    rule = rule + resource.getString("then") + " " + classAtt.getAttributeName() + " = " + classAtt.value(maxValue);

    //codigo para escrever suporte e confianca
    String supportAndConfidence = new String();
    supportAndConfidence = supportAndConfidence + resource.getString("confidence") + ": "
                           + numFormat.format(results[maxValue].getConfidence()) + "%  "
                           + resource.getString("support") + ": "
                           + numFormat.format(results[maxValue].getSupport()) + "%";

    String title = new String();
    title = resource.getString("rule");
    rule = rule + "   ";
    int ruleSize = rule.length();
    for(int i=0; i<ruleSize - resource.getString("rule").length(); i++){
      title = title + " ";
    }

    title = title + resource.getString("confidence") + "  ";
    rule = rule + numFormat.format(results[maxValue].getConfidence()) + "%";
    ruleSize = rule.length();
    for(int i=0; i<title.length() - ruleSize; i++){
      rule = rule + " ";
    }

    title = title + resource.getString("support");
    rule = rule + numFormat.format(results[maxValue].getSupport()) + "%";
    textAreaResults.setText(title + "\n" + rule);
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

  public void printRule(InferenceTree tree){
    if(tree.equals(inferenceTree)){
      Instance instance = inferenceTree.getInstance();
      Attribute[] attArray = combinatorialNetwork.getAttributeVector();
      String rule = new String();
      int classIndex = combinatorialNetwork.getClassIndex();
      int numAtt = attArray.length;
      Document docRules = textPaneRules.getDocument();
      initStylesForTextPane(textPaneRules);

      for(int i=0; i<numAtt; i++){
        if(i!=classIndex && !instance.isMissing(i)){
          Attribute att = attArray[i];
          short value = instance.getValue(i);
          rule = rule + att.getAttributeName() + ": " + att.value(value) + "\n";
        }
      }

      try {
        docRules.remove(0, docRules.getLength());
        docRules.insertString(docRules.getLength(), rule, textPaneRules.getStyle(/*initStyles[i]*/"bold"));
      } catch (BadLocationException ble) {
        System.out.println("InferencePanel - Couldn't insert initial text.");
      }
    }
  }

  void classifyButton_actionPerformed(ActionEvent e) {
    Arc[] arcArray;
    try{
      Instance instance = inferenceTree.getInstance();
      arcArray = combinatorialNetwork.inference(instance);
      printResults(arcArray, instance);
    } catch(Exception exception){
      System.out.println(exception);
    }
  }

  void expandButton_actionPerformed(ActionEvent e) {
    inferenceTree.expandTree();
    repaint();
  }

  void colapseButton_actionPerformed(ActionEvent e) {
    inferenceTree.collapseTree();
  }
}