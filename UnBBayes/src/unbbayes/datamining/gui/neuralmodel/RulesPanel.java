package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.datamanipulation.neuralmodel.*;
import unbbayes.datamining.datamanipulation.neuralmodel.entities.*;
//import unbbayes.datamining.datamanipulation.neuralmodel.util.*;
import java.text.*;

public class RulesPanel extends JPanel {
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTable tableRules;
  private InstanceSet instanceSet;
  private CombinatorialNetwork combinatorialNetwork;
  private ArrayList tableLinesArray = new ArrayList();
  private Object[] longValues = new Object[6];

  public RulesPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    this.add(jScrollPane1,  BorderLayout.CENTER);
    longValues[0] = new Integer(999);
    longValues[1] = new String();
    longValues[2] = new String();
    longValues[3] = new String("100,0%");
    longValues[4] = new Integer(999);
    longValues[5] = new String("100,0%");
  }

  public void setRulesPanel(CombinatorialNetwork combinatorialNetwork, InstanceSet instanceSet, int confidence, int support){
    this.combinatorialNetwork = combinatorialNetwork;
    this.instanceSet = instanceSet;
    this.createTableLines(support, confidence);
    RulesTableModel rulesTableModel = new RulesTableModel();
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

    while(outputEnum.hasMoreElements()){                   // para todos os neuronios de saida
      tempOutputNeuron = (OutputNeuron)outputEnum.nextElement();
      arcEnum = tempOutputNeuron.getCombinationsEnum();

      while(arcEnum.hasMoreElements()){                    // para todos os arcos dos neuronios de saida
        tempArc = (Arc)arcEnum.nextElement();
        if(tempArc.getConfidence() > minConfidence && tempArc.getSupport() > minSupport && tempArc.getAccumulator() > 1){

          if(tempArc.getCombinationNeuron() instanceof InputNeuron){ // se o neuronio de combinaçao for de entrada
            inputList = new InputNeuron[1];
            inputList[0] = (InputNeuron)tempArc.getCombinationNeuron();
          } else {                                            //se o neuronio de combinaçao for combinatorial
            inputList = ((CombinatorialNeuron)tempArc.getCombinationNeuron()).getInputList();
          }

          //constroi a string da entrada "SE"
          inputCell = new String("SE ");
          for(int i=0; i<inputList.length; i++){
            att = instanceSet.getAttribute((inputList[i]).getAttributeIndex());
            inputCell = inputCell + att.getAttributeName() + " = " + att.value(inputList[i].getValue()) + " ";
            if(i < (inputList.length - 1)){
              inputCell = inputCell + " E ";
            }
          }
          if(((String)longValues[1]).length() < inputCell.length()){  //atualiza o array que contém a maior string formada
            longValues[1] = inputCell;
          }

          //constroi a string de saida "ENTAO"
          outputCell = new String("ENTÃO ");
          att = instanceSet.getClassAttribute();
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


  class RulesTableModel extends AbstractTableModel{

    public String getColumnName(int col) {
      String[] columnNames = {"Indice", "SE", "ENTÃO", "Confiança", "Casos", "Suporte"};
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