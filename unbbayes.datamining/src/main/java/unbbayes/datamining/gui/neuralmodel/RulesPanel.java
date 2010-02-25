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
package unbbayes.datamining.gui.neuralmodel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import unbbayes.controller.IconController;
import unbbayes.datamining.classifiers.CombinatorialNeuralModel;
import unbbayes.datamining.classifiers.cnmentities.Combination;
import unbbayes.datamining.classifiers.cnmentities.OutputNeuron;
import unbbayes.datamining.datamanipulation.Attribute;

/**
 *  Class that implements a panel that shows the rules extracted from the model.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class RulesPanel extends JPanel {
	
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;		
	
  private ImageIcon printIcon;
  private ImageIcon printPreviewIcon;
  private ResourceBundle resource;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTable tableRules;
  private CombinatorialNeuralModel combinatorialNetwork;
  private ArrayList<TableLine> tableLinesArray;
  private Attribute[] attributeVector;
  private int classIndex;
  private Object[] longValues = new Object[6];
  private RulesTableModel rulesTableModel;
  private JPanel jPanel1 = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel jPanel2 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private JLabel labelMinSupport = new JLabel();
  private JComboBox comboMinConfidence = new JComboBox();
  private JLabel labelMinConfidence = new JLabel();
  private JComboBox comboMinSupport = new JComboBox();
  private JPanel jPanel3 = new JPanel();
  private JButton printButton = new JButton();
  private JButton previewButton = new JButton();
  private GridLayout gridLayout2 = new GridLayout();
  private NeuralModelController controller;
  protected IconController iconController = IconController.getInstance();

  /**
   * Builds a new panel.
   *
   * @param NeuralModelController the main controller.
   */
  public RulesPanel(NeuralModelController controller){
    this();
    setController(controller);
  }

  private RulesPanel() {
    try {
      resource = unbbayes.util.ResourceController.newInstance().getBundle(
    		  unbbayes.datamining.gui.neuralmodel.resources.NeuralModelResource.class.getName());
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    printIcon = iconController.getPrintTableIcon();
    printPreviewIcon = iconController.getPrintPreviewTableIcon();
    this.setLayout(borderLayout1);
    jPanel1.setLayout(borderLayout2);
    jPanel2.setLayout(gridLayout1);
    gridLayout1.setColumns(5);
    gridLayout1.setHgap(5);
    labelMinSupport.setHorizontalAlignment(SwingConstants.RIGHT);
    labelMinSupport.setText(resource.getString("minimumSupport"));
    labelMinConfidence.setHorizontalAlignment(SwingConstants.RIGHT);
    labelMinConfidence.setText(resource.getString("minimumConfidence"));
    jPanel2.setBorder(BorderFactory.createEtchedBorder());
    comboMinSupport.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        combo_actionPerformed(e);
      }
    });
    comboMinConfidence.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        combo_actionPerformed(e);
      }
    });
    printButton.setIcon(printIcon);
    printButton.setToolTipText(resource.getString("printTableToolTip"));
    printButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        printButton_actionPerformed(e);
      }
    });
    previewButton.setIcon(printPreviewIcon);
    previewButton.setToolTipText(resource.getString("previewTableToolTip"));
    previewButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        previewButton_actionPerformed(e);
      }
    });
    jPanel3.setLayout(gridLayout2);
    gridLayout2.setColumns(2);
    gridLayout2.setHgap(4);
    this.add(jScrollPane1,  BorderLayout.CENTER);
    this.add(jPanel1, BorderLayout.NORTH);
    jPanel1.add(jPanel2,  BorderLayout.CENTER);
    jPanel2.add(labelMinSupport, null);
    jPanel2.add(comboMinSupport, null);
    jPanel2.add(labelMinConfidence, null);
    jPanel2.add(comboMinConfidence, null);
    jPanel2.add(jPanel3, null);
    jPanel3.add(previewButton, null);
    jPanel3.add(printButton, null);

    for(int i=0; i<101; i++){
      comboMinSupport.addItem(new String(i + "%"));
      comboMinConfidence.addItem(new String(i + "%"));
    }

    longValues[0] = new Integer(999);
    longValues[1] = new String();
    longValues[2] = new String();
    longValues[3] = new String("100,0%");
    longValues[4] = new Integer(999);
    longValues[5] = new String("100,0%");
  }

  /**
   * Used to set the controller of this class.
   *
   * @param inferencePanel the controller.
   */
  public void setController(NeuralModelController controller){
    this.controller = controller;
  }

  /**
   * Used to start the rules panel, showing the rules extracted from the generated model.
   *
   * @param combinatorialNetwork the generated model.
   */
  public void setRulesPanel(CombinatorialNeuralModel combinatorialNetwork){
    setRulesPanel(combinatorialNetwork, 60, 7);    //valores default de confian�a e suporte
  }

  /**
   * Used to start the rules panel, showing the rules extracted from the generated model.
   *
   * @param combinatorialNetwork the generated model.
   * @param confidence the minimum confidence entered by the user.
   * @param support the minimum support entered by the user.
   */
  public void setRulesPanel(CombinatorialNeuralModel combinatorialNetwork, int confidence, int support){
    this.combinatorialNetwork = combinatorialNetwork;

    comboMinSupport.setSelectedIndex(support);
    comboMinConfidence.setSelectedIndex(confidence);

    attributeVector = combinatorialNetwork.getAttributeVector();
    classIndex = combinatorialNetwork.getClassIndex();

    createTableLines(support, confidence);

    rulesTableModel = new RulesTableModel();
      //    TableSorter sorter = new TableSorter(rulesTableModel);   //adicionada

    tableRules = new JTable(rulesTableModel);

//    tableRules = new JTable(sorter);    //adicionada
//    sorter.addMouseListenerToHeaderInTable(tableRules);   //adicionada

      initColumnSizes(tableRules, rulesTableModel);
      jScrollPane1.getViewport().add(tableRules, null);
  }

  private void createTableLines(int minSupport, int minConfidence){
    Attribute att;
    ArrayList inputList;
    Iterator combinations = combinatorialNetwork.getModel();
    TableLine tableLine;                                   // linha da tabela de regras
    String inputCell, outputCell, confidence, support;
    Integer numberOfCases;
    OutputNeuron[] outputArray;
    OutputNeuron tempOutput;
    DecimalFormat numFormat = new DecimalFormat("##0.0");
    tableLinesArray = new ArrayList<TableLine>();

    while(combinations.hasNext()){                   // para todos os neuronios de saida
      Combination combination = (Combination)combinations.next();
      outputArray = combination.getOutputArray();
      inputList = extractInputs(combination);

      for(int i=0; i<outputArray.length; i++){
        tempOutput = outputArray[i];
        if(tempOutput != null && tempOutput.getConfidence() >= minConfidence && tempOutput.getSupport() >= minSupport){

          //constroi a string da entrada "SE"
          inputCell = resource.getString("if") + " ";
          for(int j=0; j<inputList.size(); j++){
            int[] input = (int[])inputList.get(j);
            att = attributeVector[input[0]];
            inputCell = inputCell + att.getAttributeName() + " = " + att.value(input[1]) + " ";
            if(j < (inputList.size() - 1)){
              inputCell = inputCell + " " + resource.getString("and") + " ";
            }
          }
          if(((String)longValues[1]).length() < inputCell.length()){  //atualiza o array que cont�m a maior string formada
            longValues[1] = inputCell;
          }

          //constroi a string de saida "ENTAO"
          outputCell = resource.getString("then") + " "; //new String("ENT�O ");
          att = attributeVector[classIndex];

          outputCell = outputCell + att.getAttributeName() + " = " + att.value(i);

          if(((String)longValues[2]).length() < outputCell.length()){  //atualiza o array que cont�m a maior string formada
            longValues[2] = outputCell;
          }

          // constroi o valor do numero de casos
          numberOfCases = new Integer(tempOutput.getAccumulator());

          // constroi o valor da confian�a
          confidence = new String(numFormat.format(tempOutput.getConfidence()) + "%");

          // constroi o valor do suporte
          support = new String(numFormat.format(tempOutput.getSupport()) + "%");

          tableLine = new TableLine(inputCell, outputCell, confidence, numberOfCases, support);
          tableLinesArray.add(tableLine);
        }
      }
    }
  }

  private ArrayList extractInputs(Combination combination){
    String key = combination.getKey();
    StringTokenizer strTokenizer = new StringTokenizer(key, " ");
    int[] input;
    int numOfTokens = strTokenizer.countTokens();
    ArrayList<int[]> inputArray = new ArrayList<int[]>();

    for(int i=0; i<numOfTokens; i++){
      input = new int[2];
      input[0] = (int) Float.parseFloat(strTokenizer.nextToken());
      i++;
      input[1] = (int) Float.parseFloat(strTokenizer.nextToken());
      inputArray.add(input);
    }
    return inputArray;
  }


 /**
  * This method picks good column sizes.
  */
  private void initColumnSizes(JTable table, RulesTableModel tableModel) {
    TableColumn column = null;
    Component component = null;
    int headerWidth = 0;
    int cellWidth = 0;
    TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

    for (int i=0; i<6; i++) {
      column = table.getColumnModel().getColumn(i);

      component = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
      headerWidth = component.getPreferredSize().width;

      component = table.getDefaultRenderer(tableModel.getColumnClass(i)).
                  getTableCellRendererComponent(table, longValues[i], false, false, 0, i);
      cellWidth = component.getPreferredSize().width;

      column.setPreferredWidth(Math.max(headerWidth, cellWidth));
    }
  }

  void combo_actionPerformed(ActionEvent e) {
    if(rulesTableModel != null){
      int minConfidence = comboMinConfidence.getSelectedIndex();
      int minSupport = comboMinSupport.getSelectedIndex();
      createTableLines(minSupport, minConfidence);
      rulesTableModel.fireTableDataChanged();
    }
  }

  void printButton_actionPerformed(ActionEvent e) {
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    controller.printTable(tableRules);
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void previewButton_actionPerformed(ActionEvent e) {
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    controller.printPreviewer(tableRules);
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  /**
   * Return the minimum support entered by the user.
   *
   * @return the minimum support
   */
  public int getSupport(){
    return comboMinSupport.getSelectedIndex();
  }

  /**
   * Return the minimum confidence entered by the user.
   *
   * @return the minimum confidence
   */
  public int getConfidence(){
    return comboMinConfidence.getSelectedIndex();
  }

  class RulesTableModel extends AbstractTableModel{
    
	  /** Serialization runtime version number */
	  private static final long serialVersionUID = 0;	
		
	  public String getColumnName(int col) {
      String[] columnNames = {resource.getString("index"),
                              resource.getString("if"),
                              resource.getString("then"),
                              resource.getString("confidence"),
                              resource.getString("cases"),
                              resource.getString("support")
      };
      return columnNames[col].toString();
    }

    public int getColumnCount() {
      return 6;
    }

    public int getRowCount() {
      return tableLinesArray.size();
    }

    public Object getValueAt(int row, int col) {
      TableLine tableLine = (TableLine)tableLinesArray.get(row);

      switch(col){
        case 0:
          return new Integer(row + 1);
        case 1:
          return tableLine.input;
        case 2:
          return tableLine.output;
        case 3:
          return tableLine.confidence;
        case 4:
          return tableLine.numberOfCases;
        case 5:
          return tableLine.support;
        default:
          return null;
      }
    }
  }

  class TableLine{
    String input;
    String output;
    String confidence;
    Integer numberOfCases;
    String support;

    public TableLine(String input, String output, String confidence, Integer numberOfCases, String support){
      this.input = input;
      this.output = output;
      this.confidence = confidence;
      this.numberOfCases = numberOfCases;
      this.support = support;
    }
  }
}