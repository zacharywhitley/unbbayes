package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.datamanipulation.neuralmodel.*;
import unbbayes.datamining.datamanipulation.neuralmodel.entities.*;

public class RulesPanel extends JPanel {
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTable jTable1;
  private InstanceSet instanceSet;
  private CombinatorialNetwork combinatorialNetwork;
  private ArrayList tableLinesArray = new ArrayList();

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
  }

  public void setRulesPanel(CombinatorialNetwork combinatorialNetwork, InstanceSet instanceSet){
    this.combinatorialNetwork = combinatorialNetwork;
    this.instanceSet = instanceSet;
    this.createTableLines();
    jTable1 = new JTable(new RulesTableModel());
    jScrollPane1.getViewport().add(jTable1, null);
  }

  public void createTableLines(){
    Arc tempArc;                                           // arco temporário
    Enumeration outputEnum = combinatorialNetwork.getOutputLayer().elements();  // enumeraçao da camada de saida
    Enumeration arcEnum;                                   // enumeracao dos arcos de um neuronio de saida
    TableLine tableLine;                                   // linha da tabela de regras
    InputNeuron[] inputList;                               // lista de neuronios de entrada
    OutputNeuron tempOutputNeuron;                         // neuronio de saida temporário

    while(outputEnum.hasMoreElements()){                   // para todos os neuronios de saida
      tempOutputNeuron = (OutputNeuron)outputEnum.nextElement();
      arcEnum = tempOutputNeuron.getCombinationsEnum();

      while(arcEnum.hasMoreElements()){                    // para todos os arcos dos neuronios de saida
        tempArc = (Arc)arcEnum.nextElement();
        if(tempArc.getCombinationNeuron() instanceof InputNeuron){ // se o neuronio de combinaçao for de entrada
          inputList = new InputNeuron[1];
          inputList[0] = (InputNeuron)tempArc.getCombinationNeuron();
        } else {                                            //se o neuronio de combinaçao for combinatorial
          inputList = ((CombinatorialNeuron)tempArc.getCombinationNeuron()).getInputList();
        }
        tableLine = new TableLine(inputList, tempOutputNeuron, tempArc);
        tableLinesArray.add(tableLine);
      }
    }
  }

  class RulesTableModel extends AbstractTableModel{
    public String getColumnName(int col) {
      String[] columnNames = {"Indice", "SE", "ENTÃO", "Classe", "Confiança", "Casos", "Suporte"};
      return columnNames[col].toString();
    }

    public int getColumnCount() {
      return 7;
    }

    public int getRowCount() {
      return tableLinesArray.size();
    }

    public Object getValueAt(int row, int col) {
      TableLine tableLine = (TableLine)tableLinesArray.get(row);
      String tableCellStr = new String();
      Attribute att;

      switch(col){
        case 0:
          return new Integer(row + 1);
        case 1:
          tableCellStr = new String("SE ");
          for(int i=0; i<tableLine.inputList.length; i++){
            att = instanceSet.getAttribute((tableLine.inputList[i]).getAttributeIndex());
            tableCellStr = tableCellStr + att.getAttributeName() + " = " + att.value(tableLine.inputList[i].getValue()) + " ";
            if(i < (tableLine.inputList.length - 1)){
              tableCellStr = tableCellStr + " E ";
            }
          }
          return tableCellStr;
        case 2:
          tableCellStr = new String("ENTÃO ");
          att = instanceSet.getClassAttribute();
          tableCellStr = tableCellStr + att.getAttributeName() + " = " + att.value(tableLine.outputNeuron.getValue());
          return tableCellStr;
        case 3:
          return null;
        case 4:
          return null;
        case 5:
          return new String("" + tableLine.arc.getAccumulator());
        case 6:
          return new String("" + tableLine.arc.getWeigth());
        default: return null;
      }
    }
  }

  class TableLine{
    InputNeuron[] inputList;
    OutputNeuron outputNeuron;
    Arc arc;

    public TableLine(InputNeuron[] inputList, OutputNeuron outputNeuron, Arc arc){
      this.inputList = inputList;
      this.outputNeuron = outputNeuron;
      this.arc = arc;
    }
  }
}