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
package unbbayes.datamining.gui.preprocessor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 * Creates a panel that displays the attributes contained in a set of
 * instances, letting the user toggle whether each attribute is selected
 * or not (eg: so that unselected attributes can be removed before
 * classification).
 */
public class AttributeSelectionPanel extends JPanel
{ 
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;	
	
  /** Carrega o arquivo de recursos para internacionaliza��o da localidade padr�o */
  private ResourceBundle resource;
  /**
   * A table model that looks at the names of attributes and maintains
   * a list of attributes that have been "selected".
   */
  private class AttributeTableModel extends AbstractTableModel
  { 
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
		
	/** The instances who's attribute structure we are reporting */
    protected InstanceSet m_Instances;

    /** The flag for whether the instance will be included */
    protected boolean [] m_Selected;

    /**
     * Creates the tablemodel with the given set of instances.
     * @param instances the initial set of Instances
     */
    public AttributeTableModel(InstanceSet instances)
    { setInstances(instances);
    }

    /**
     * Sets the tablemodel to look at a new set of instances.
     * @param instances the new set of Instances.
     */
    public void setInstances(InstanceSet instances)
    { m_Instances = instances;
      m_Selected = new boolean [m_Instances.numAttributes()];
      for (int i=0; i<m_Selected.length; i++)
      {   m_Selected[i] = true;
      }
    }

    /**
     * Gets the number of attributes.
     * @return the number of attributes.
     */
    public int getRowCount()
    { return m_Selected.length;
    }

    /**
     * Gets the number of columns: 3
     * @return 3
     */
    public int getColumnCount()
    { return 3;
    }

    /**
     * Gets a table cell
     * @param row the row index
     * @param column the column index
     * @return the value at row, column
     */
    public Object getValueAt(int row, int column)
    { switch (column)
      {   case 0:   return new Integer(row + 1);
          case 1:   return new Boolean(m_Selected[row]);
          case 2:   return m_Instances.getAttribute(row).getAttributeName();
          default:  return null;
      }
    }

    /**
     * Gets the name for a column.
     * @param column the column index.
     * @return the name of the column.
     */
    public String getColumnName(int column)
    { switch (column)
      {   case 0:   return new String("No.");
          case 1:   return new String("");
          case 2:   return new String(resource.getString("name2"));
          default:  return null;
      }
    }

    /**
     * Sets the value at a cell.
     * @param value the new value.
     * @param row the row index.
     * @param col the column index.
     */
    public void setValueAt(Object value, int row, int col)
    {   if (col == 1)
        {   m_Selected[row] = ((Boolean) value).booleanValue();
        }
    }

    /**
     * Gets the class of elements in a column.
     * @param col the column index.
     * @return the class of elements in the column.
     */
    public Class<?> getColumnClass(int col)
    {   return getValueAt(0, col).getClass();
    }

    /**
     * Returns true if the column is the "selected" column.
     * @param row ignored
     * @param col the column index.
     * @return true if col == 1.
     */
    public boolean isCellEditable(int row, int col)
    {   if (col == 1)
        {   return true;
        }
        return false;
    }

    /**
     * Gets an array containing the indices of all selected attributes.
     * @return the array of selected indices.
     */
    public int [] getSelectedAttributes()
    { int [] r1 = new int[getRowCount()];
      int selCount = 0;
      for (int i = 0; i < getRowCount(); i++)
      {   if (m_Selected[i])
          {   r1[selCount++] = i;
	  }
      }
      int [] result = new int[selCount];
      System.arraycopy(r1, 0, result, 0, selCount);
      return result;
    }
  }

  /** The table displaying attribute names and selection status */
  protected JTable m_Table = new JTable();

  /** The table model containingn attribute names and selection status */
  protected AttributeTableModel m_Model;

  /**
   * Creates the attribute selection panel with no initial instances.
   */
  public AttributeSelectionPanel() {
   resource = ResourceBundle.getBundle("unbbayes.datamining.gui.preprocessor.resources.PreprocessorResource");
    m_Table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    m_Table.getTableHeader().setReorderingAllowed(false);
    m_Table.getTableHeader().setResizingAllowed(false);
    m_Table.setColumnSelectionAllowed(false);
    m_Table.setPreferredScrollableViewportSize(new Dimension(250, 150));
    m_Table.setToolTipText(resource.getString("selectedAttributes"));

    setLayout(new BorderLayout());
    add(new JScrollPane(m_Table), BorderLayout.CENTER);
  }

    /**
   * Sets the instances who's attribute names will be displayed.
   *
   * @param newInstances the new set of instances
   */
  public void setInstances(InstanceSet newInstances) {

    if (m_Model == null) {
      m_Model = new AttributeTableModel(newInstances);
      m_Table.setModel(m_Model);
      TableColumnModel tcm = m_Table.getColumnModel();
      tcm.getColumn(0).setMaxWidth(60);
      tcm.getColumn(1).setMaxWidth(tcm.getColumn(1).getMinWidth());
      tcm.getColumn(2).setMinWidth(100);
    } else {
      m_Model.setInstances(newInstances);
      m_Table.clearSelection();
    }
    m_Table.sizeColumnsToFit(2);
    m_Table.revalidate();
    m_Table.repaint();
  }

  /**
   * Gets an array containing the indices of all selected attributes.
   *
   * @return the array of selected indices.
   */
  public int[] getSelectedAttributes() {

    return m_Model.getSelectedAttributes();
  }

  /**
   * Gets the selection model used by the table.
   *
   * @return a value of type 'ListSelectionModel'
   */
  public ListSelectionModel getSelectionModel() {

    return m_Table.getSelectionModel();
  }

} // AttributeSelectionPanel