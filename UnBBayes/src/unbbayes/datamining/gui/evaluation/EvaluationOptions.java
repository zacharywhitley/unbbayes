package unbbayes.datamining.gui.evaluation;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import unbbayes.prs.bn.*;

public class EvaluationOptions
{ private JTable statesTable = new JTable();
  private JLabel statesLabel = new JLabel("Enter new Values: ");
  private JPanel statesPanel = new JPanel(new BorderLayout());
  private ProbabilisticNode classNode;

  public EvaluationOptions(ProbabilisticNode classNode,EvaluationPanel parent)
  {   this.classNode = classNode;
      statesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      statesTable.getTableHeader().setReorderingAllowed(false);
      statesTable.getTableHeader().setResizingAllowed(false);
      statesTable.setColumnSelectionAllowed(false);
      statesTable.setRowSelectionAllowed(false);
      StatesTableModel model = new StatesTableModel(classNode);
      statesTable.setModel(model);
      JComboBox comboBox = new JComboBox();
      /*comboBox.addActionListener(new ActionListener()
      {   public void actionPerformed(ActionEvent e)
          {   System.out.println("combo");
          }
      });*/
      int size = classNode.getStatesSize();
      for (int i=0;i<size;i++)
      {   comboBox.addItem(new Integer(i));
      }
      TableColumnModel columnModel = statesTable.getColumnModel();
      TableColumn priorityColumn = columnModel.getColumn(0);
      priorityColumn.setCellEditor(new DefaultCellEditor(comboBox));

      statesPanel.add(statesLabel,BorderLayout.NORTH);
      statesPanel.add(statesTable,BorderLayout.CENTER);

      if ((JOptionPane.showInternalConfirmDialog(parent, statesTable, "Enter new Values:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION))
      {   int[] ints = model.getPriorityClassValues();
          float[] floats = model.getPriorityProbabilities();
          int[] priorityClassValues = new int[ints.length];
          float[] priorityProbabilities = new float[ints.length];
          for (int i=0;i<ints.length;i++)
          {   for (int j=0;j<ints.length;j++)
              {   if (ints[j] == i)
                  {   priorityClassValues[i] = j;
                      priorityProbabilities[i] = floats[j];
                  }
              }
          }
          parent.setAbsoluteValues(priorityClassValues,priorityProbabilities);
      }
  }

  /**
   * A table model that looks at the names of attributes and maintains
   * a list of attributes that have been "selected".
   */
  private class StatesTableModel extends AbstractTableModel
  {   private ProbabilisticNode classNode;
      private String[] columnNames = {"Priority","State","Probability" };
      private Object[][] cells;
      private int statesSize = 1;

      /**
      * Creates the tablemodel with the given set of instances.
      * @param instances the initial set of Instances
      */
      public StatesTableModel(ProbabilisticNode classNode)
      {   this.classNode = classNode;
          statesSize = classNode.getStatesSize();
          cells = new Object[statesSize][3];
          for (int row = 0; row < statesSize; row++)
          {   cells [row][0] = new Integer(row);
              cells [row][1] = classNode.getStateAt(row);
              cells [row][2] = ""+classNode.getMarginalAt(row);
          }
      }

      /**
      * Gets the number of attributes.
      * @return the number of attributes.
      */
      public int getRowCount()
      {   return statesSize;
      }

      /**
      * Gets the number of columns: 3
      * @return 3
      */
      public int getColumnCount()
      {   return 3;
      }

      /**
      * Gets a table cell
      * @param row the row index
      * @param column the column index
      * @return the value at row, column
      */
      public Object getValueAt(int row, int col)
      {   /*switch (column)
          {   case 0:   return new Integer(row);
              case 1:   return classNode.getStateAt(row);
              case 2:   return ""+classNode.getMarginalAt(row);
              default:  return null;
          }*/
          return cells[row][col];
      }

      /**
      * Gets the name for a column.
      * @param column the column index.
      * @return the name of the column.
      */
      public String getColumnName(int column)
      {   /*switch (column)
          {   case 0:   return new String("Priority");
              case 1:   return new String("State");
              case 2:   return new String("Probability");
              default:  return null;
          }*/
          return columnNames[column];
      }

    /**
     * Sets the value at a cell.
     * @param value the new value.
     * @param row the row index.
     * @param col the column index.
     */
    public void setValueAt(Object value, int row, int col)
    {   cells[row][col] = value;
    }

      /**
      * Gets the class of elements in a column.
      * @param col the column index.
      * @return the class of elements in the column.
      */
      public Class getColumnClass(int col)
      {   return getValueAt(0, col).getClass();
      }

      /**
      * Returns true if the column is the "selected" column.
      * @param row ignored
      * @param col the column index.
      * @return true if col == 1.
      */
      public boolean isCellEditable(int row, int col)
      {   if ((col == 2) || (col == 0))
          {   return true;
          }
          return false;
      }

      public int[] getPriorityClassValues()
      {   int[] priorityClassValues = new int[statesSize];
          for (int i=0;i<statesSize;i++)
          {   priorityClassValues[i] = ((Integer)cells[i][0]).intValue();
          }
          return priorityClassValues;
      }

      public float[] getPriorityProbabilities()
      {   float[] priorityProbabilities = new float[statesSize];
          for (int i=0;i<statesSize;i++)
          {   priorityProbabilities[i] = Float.parseFloat(""+cells[i][2]);
          }
          return priorityProbabilities;
      }

  }
}