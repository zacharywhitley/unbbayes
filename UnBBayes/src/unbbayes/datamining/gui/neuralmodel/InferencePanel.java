package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;

import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.classifiers.*;
import unbbayes.datamining.datamanipulation.neuralmodel.entities.*;

public class InferencePanel extends JPanel {
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private JPanel jPanel2 = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel jPanel3 = new JPanel();
  private JPanel jPanel4 = new JPanel();
  private NeuralModelMain mainController;
  private JPanel panelMessages = new JPanel();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private BorderLayout borderLayout3 = new BorderLayout();
  private CombinatorialNeuralModel combinatorialNetwork;
  private JScrollPane treeScrollPane/* = new JScrollPane()*/;
  private BorderLayout borderLayout4 = new BorderLayout();
  private InferenceTree inferenceTree;
  private JSplitPane jSplitPane1 = new JSplitPane();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JToolBar jToolBar1 = new JToolBar();
  private JButton jButton1 = new JButton();
  private JButton jButton2 = new JButton();
  private JButton buttonClassify = new JButton();
  private Icon colapseIcon;
  private Icon expandIcon;
  private Icon propagateIcon;
  private JTextArea textAreaResults = new JTextArea();
  private JTextPane textPaneResults = new JTextPane();
  private BorderLayout borderLayout6 = new BorderLayout();

  public InferencePanel() {
    try {
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
    jButton1.setIcon(expandIcon);
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jButton1_actionPerformed(e);
      }
    });
    jButton2.setIcon(colapseIcon);
    jButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jButton2_actionPerformed(e);
      }
    });
    buttonClassify.setIcon(propagateIcon);
    buttonClassify.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonClassify_actionPerformed(e);
      }
    });
    textAreaResults.setEditable(false);
    textAreaResults.setRows(4);
    jPanel4.setLayout(borderLayout6);
    textPaneResults.setEditable(false);
    this.add(jPanel1,  BorderLayout.CENTER);
    jPanel2.add(jPanel3,  BorderLayout.CENTER);
//    jPanel3.add(treeScrollPane,  BorderLayout.CENTER);
    jSplitPane1.add(jPanel4, JSplitPane.RIGHT);
    jPanel4.add(textPaneResults,  BorderLayout.CENTER);
    jPanel1.add(jSplitPane1, BorderLayout.CENTER);
    jSplitPane1.add(jPanel2, JSplitPane.LEFT);
    this.add(panelMessages,  BorderLayout.SOUTH);
    panelMessages.add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(textAreaResults, null);
    this.add(jToolBar1, BorderLayout.NORTH);
    jToolBar1.add(jButton2, null);
    jToolBar1.add(jButton1, null);
    jToolBar1.add(buttonClassify, null);
    jToolBar1.setFloatable(false);
    jSplitPane1.setDividerLocation(20);
  }


  public void setNetwork(CombinatorialNeuralModel combinatorialNetwork){
    this.combinatorialNetwork = combinatorialNetwork;
    inferenceTree = new InferenceTree();
    inferenceTree.setAttributes(combinatorialNetwork.getAttributeVector(), combinatorialNetwork.getClassIndex());
    inferenceTree.expandTree();

    jSplitPane1.setDividerLocation(0.4);
    treeScrollPane = new JScrollPane(inferenceTree);
    jPanel3.removeAll();
    jPanel3.add(treeScrollPane,  BorderLayout.CENTER);
    jPanel3.updateUI();

    textPaneResults.setText("");
    textAreaResults.setText("");
  }
/*
  private void printResults(float[] results, Instance instance){
    float[] distributionNormalized = new float[results.length];
    System.arraycopy(results, 0, distributionNormalized, 0, results.length);
    Utils.normalize(distributionNormalized);

    Attribute classAtt = (combinatorialNetwork.getAttributeVector())[combinatorialNetwork.getClassIndex()];

    String[] initString = new String[results.length + 1];
    initString[0] = "Classe: " + classAtt.getAttributeName() + "\n\n";
    for(int i=0; i<results.length; i++){
      initString[i+1] = "- " + classAtt.value(i) +
                        ":  " + results[i] +
                        "    " + distributionNormalized[i] * 100 + "%\n\n";
    }

    String[] initStyles = new String[initString.length];
    initStyles[0] = "largeBold";
    int maxValue = Utils.maxIndex(results);
    for(int i=0; i<results.length; i++){
      if(i == maxValue){
        initStyles[i+1] = "bold";
      } else {
        initStyles[i+1] = "regular";
      }
    }

    initStylesForTextPane(textPaneResults);
    Document doc = textPaneResults.getDocument();
    try {
      doc.remove(0, doc.getLength());
      for (int i=0; i<initString.length; i++) {
        doc.insertString(doc.getLength(), initString[i], textPaneResults.getStyle(initStyles[i]));
      }
    } catch (BadLocationException ble) {
      System.err.println("Couldn't insert initial text.");
    }

    //parte de impressão do text area

    String attributeValues = new String();
    Attribute[] attVector = combinatorialNetwork.getAttributeVector();
    int classIndex = combinatorialNetwork.getClassIndex();
    int numAtt = attVector.length;
    for(int i=0; i<numAtt; i++){
      if(i!=classIndex && !instance.isMissing(i)){
        Attribute att = attVector[i];
        short value = instance.getValue(i);
        attributeValues = attributeValues + att.getAttributeName() + ": " + att.value(value) + "    ";
//        if(i != numAtt-1){
//          attributeValues = attributeValues + ", ";
//        }
      }
    }

    String classValue = "Classe: " + classAtt.getAttributeName();
    for(int i=0; i<results.length; i++){
      if(i==maxValue){
        classValue = classValue + " = " + classAtt.value(i);
      }
    }

    textAreaResults.append(attributeValues + "\n" + classValue + "\n");
  }
*/

  private void printResults(Arc[] results, Instance instance){
    float[] distributionNormalized = new float[results.length];
    for(int i=0; i<results.length; i++){
      distributionNormalized[i] = results[i].getNetWeigth();
    }
    Utils.normalize(distributionNormalized);

    Attribute classAtt = (combinatorialNetwork.getAttributeVector())[combinatorialNetwork.getClassIndex()];

    String[] initString = new String[results.length + 1];
    initString[0] = "Classe: " + classAtt.getAttributeName() + "\n\n";
    for(int i=0; i<results.length; i++){
      initString[i+1] = "- " + classAtt.value(i) +
                        ":  " + results[i].getNetWeigth() +
                        "    " + distributionNormalized[i] * 100 + "%\n\n";
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
    Document doc = textPaneResults.getDocument();
    try {
      doc.remove(0, doc.getLength());
      for (int i=0; i<initString.length; i++) {
        doc.insertString(doc.getLength(), initString[i], textPaneResults.getStyle(initStyles[i]));
      }
    } catch (BadLocationException ble) {
      System.err.println("Couldn't insert initial text.");
    }

    //parte de impressão do text area

    String attributeValues = new String();
    Attribute[] attVector = combinatorialNetwork.getAttributeVector();
    int classIndex = combinatorialNetwork.getClassIndex();
    int numAtt = attVector.length;
    for(int i=0; i<numAtt; i++){
      if(i!=classIndex && !instance.isMissing(i)){
        Attribute att = attVector[i];
        short value = instance.getValue(i);
        attributeValues = attributeValues + att.getAttributeName() + ": " + att.value(value) + "    ";
//        if(i != numAtt-1){
//          attributeValues = attributeValues + ", ";
//        }
      }
    }

    String classValue = "Classe: " + classAtt.getAttributeName();
    for(int i=0; i<results.length; i++){
      if(i == maxValue){
        classValue = classValue + " = " + classAtt.value(i);
      }
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

    String rule = new String("Regra: SE ");
    for(int i=0; i<inputList.length; i++){
      att = attVector[inputList[i].getAttributeIndex()];

      rule = rule + att.getAttributeName() + " = " + att.value(inputList[i].getValue()) + " ";
      if(i < (inputList.length - 1)){
        rule = rule + " E ";
      }
    }

    rule = rule + " ENTÃO " + classAtt.getAttributeName() + " = " + classAtt.value(maxValue);

    //codigo para escrever suporte e confianca
    String supportAndConfidence = new String();
    supportAndConfidence = supportAndConfidence + "Confiança: " + results[maxValue].getConfidence()
                           + "  Suporte: " + results[maxValue].getSupport();

    textAreaResults.append(attributeValues + "\n" + classValue + "\n" + rule + "\n" + supportAndConfidence);
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

/*
    void buttonClassify_actionPerformed(ActionEvent e) {
    float[] distribution;
    try{
      Instance instance = inferenceTree.getInstance();
      distribution = combinatorialNetwork.distributionForInstance(instance);
      printResults(distribution, instance);

    } catch(Exception exception){
      System.out.println(exception);
    }
  }
  */

  void buttonClassify_actionPerformed(ActionEvent e) {
    Arc[] arcVector;
    try{
      Instance instance = inferenceTree.getInstance();
      arcVector = combinatorialNetwork.inference(instance);
      printResults(arcVector, instance);

    } catch(Exception exception){
      System.out.println(exception);
    }
  }

  void jButton1_actionPerformed(ActionEvent e) {
    inferenceTree.expandTree();
    repaint();
  }

  void jButton2_actionPerformed(ActionEvent e) {
    inferenceTree.collapseTree();
  }
}