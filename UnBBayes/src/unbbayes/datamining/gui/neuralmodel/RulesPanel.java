package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import unbbayes.datamining.classifiers.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.datamanipulation.neuralmodel.entities.*;
//import unbbayes.datamining.datamanipulation.neuralmodel.util.*;

/**
 *  Class that implements a panel that shows the rules extracted from the model.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class RulesPanel extends JPanel {
  private ImageIcon printIcon;
  private ImageIcon printPreviewIcon;
  private ResourceBundle resource;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTable tableRules;
  private CombinatorialNeuralModel combinatorialNetwork;
  private ArrayList tableLinesArray;
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
      resource = ResourceBundle.getBundle("unbbayes.datamining.gui.neuralmodel.resources.NeuralModelResource");
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    printIcon = new ImageIcon(getClass().getResource("/icons/print-table.gif"));
    printPreviewIcon = new ImageIcon(getClass().getResource("/icons/preview-table.gif"));
    this.setLayout(borderLayout1);
    jPanel1.setLayout(borderLayout2);
    jPanel2.setLayout(gridLayout1);
    gridLayout1.setColumns(5);
    gridLayout1.setHgap(5);
    labelMinSupport.setText(resource.getString("minimumSupport"));
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
    setRulesPanel(combinatorialNetwork, 60, 7);    //valores default de confiança e suporte
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
    Arc tempArc;                                           // arco temporário
    Attribute att;
    Enumeration outputEnum = combinatorialNetwork.getOutputLayer().elements();  // enumeraçao da camada de saida
    Enumeration arcEnum;                                   // enumeracao dos arcos de um neuronio de saida
    TableLine tableLine;                                   // linha da tabela de regras
    InputNeuron[] inputList;                               // lista de neuronios de entrada
    OutputNeuron tempOutputNeuron;                         // neuronio de saida temporário
    String inputCell, outputCell, confidence, support;
    Integer numberOfCases;
    DecimalFormat numFormat = new DecimalFormat("##0.0");
    tableLinesArray = new ArrayList();

    while(outputEnum.hasMoreElements()){                   // para todos os neuronios de saida
      tempOutputNeuron = (OutputNeuron)outputEnum.nextElement();
      arcEnum = tempOutputNeuron.getCombinationsEnum();

      while(arcEnum.hasMoreElements()){                    // para todos os arcos dos neuronios de saida
        tempArc = (Arc)arcEnum.nextElement();
        if(tempArc.getConfidence() > minConfidence && tempArc.getSupport() > minSupport){

          if(tempArc.getCombinationNeuron() instanceof InputNeuron){ // se o neuronio de combinaçao for de entrada
            inputList = new InputNeuron[1];
            inputList[0] = (InputNeuron)tempArc.getCombinationNeuron();
          } else {                                            //se o neuronio de combinaçao for combinatorial
            inputList = ((CombinatorialNeuron)tempArc.getCombinationNeuron()).getInputList();
          }

          //constroi a string da entrada "SE"
          inputCell = resource.getString("if") + " ";
          for(int i=0; i<inputList.length; i++){
            att = attributeVector[(inputList[i]).getAttributeIndex()];

            inputCell = inputCell + att.getAttributeName() + " = " + att.value(inputList[i].getValue()) + " ";
            if(i < (inputList.length - 1)){
              inputCell = inputCell + " " + resource.getString("and") + " ";
            }
          }
          if(((String)longValues[1]).length() < inputCell.length()){  //atualiza o array que contém a maior string formada
            longValues[1] = inputCell;
          }

          //constroi a string de saida "ENTAO"
          outputCell = resource.getString("then") + " "; //new String("ENTÃO ");
          att = attributeVector[classIndex];

          outputCell = outputCell + att.getAttributeName() + " = " + att.value(tempOutputNeuron.getValue());

          if(((String)longValues[2]).length() < outputCell.length()){  //atualiza o array que contém a maior string formada
            longValues[2] = outputCell;
          }

          // constroi o valor do numero de casos
          numberOfCases = new Integer(tempArc.getAccumulator());

          // constroi o valor da confiança
          confidence = new String(numFormat.format(tempArc.getConfidence()) + "%");

          // constroi o valor do suporte
          support = new String(numFormat.format(tempArc.getSupport()) + "%");

          tableLine = new TableLine(inputCell, outputCell, confidence, numberOfCases, support);
          tableLinesArray.add(tableLine);
        }
      }
    }
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
    public String getColumnName(int col) {
//      String[] columnNames = {"Indice", "SE", "ENTÃO", "Confiança", "Casos", "Suporte"};
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