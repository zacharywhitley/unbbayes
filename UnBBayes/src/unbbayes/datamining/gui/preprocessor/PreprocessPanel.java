package unbbayes.datamining.gui.preprocessor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import unbbayes.datamining.datamanipulation.*;
import unbbayes.util.GraphPaperLayout;

public class PreprocessPanel extends JPanel
{ /** Carrega o arquivo de recursos para internacionalização da localidade padrão */
  private ResourceBundle resource;
  private JPanel jPanel7 = new JPanel();
  private JPanel jPanel6 = new JPanel();
  private JLabel jLabel6 = new JLabel();
  private JLabel jLabel5 = new JLabel();
  private JLabel jLabel4 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel1 = new JLabel();
  private BorderLayout borderLayout9 = new BorderLayout();
  private BorderLayout borderLayout8 = new BorderLayout();
  private BorderLayout borderLayout7 = new BorderLayout();
  private BorderLayout borderLayout6 = new BorderLayout();
  private BorderLayout borderLayout5 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private BorderLayout borderLayout13 = new BorderLayout();
  private BorderLayout borderLayout12 = new BorderLayout();
  private BorderLayout borderLayout11 = new BorderLayout();
  private BorderLayout borderLayout10 = new BorderLayout();
  private JPanel jPanel23 = new JPanel();
  private JPanel jPanel22 = new JPanel();
  private JPanel jPanel21 = new JPanel();
  private JPanel jPanel20 = new JPanel();
  private JPanel jPanel19 = new JPanel();
  private JPanel jPanel18 = new JPanel();
  private JPanel jPanel13 = new JPanel();
  private JPanel jPanel12 = new JPanel();
  private JPanel jPanel11 = new JPanel();
  private JPanel jPanel10 = new JPanel();
  private GridLayout gridLayout3 = new GridLayout();
  private GridLayout gridLayout2 = new GridLayout();
  private BorderLayout borderLayout32 = new BorderLayout();
  private BorderLayout borderLayout31 = new BorderLayout();
  private BorderLayout borderLayout30 = new BorderLayout();
  private JPanel jPanel40 = new JPanel();
  private JPanel jPanel9 = new JPanel();
  private BorderLayout borderLayout29 = new BorderLayout();
  private BorderLayout borderLayout28 = new BorderLayout();
  private BorderLayout borderLayout27 = new BorderLayout();
  private BorderLayout borderLayout26 = new BorderLayout();
  private BorderLayout borderLayout25 = new BorderLayout();
  private BorderLayout borderLayout24 = new BorderLayout();
  private BorderLayout borderLayout23 = new BorderLayout();
  private BorderLayout borderLayout22 = new BorderLayout();
  private JPanel jPanel39 = new JPanel();
  private BorderLayout borderLayout21 = new BorderLayout();
  private JPanel jPanel38 = new JPanel();
  private JPanel jPanel37 = new JPanel();
  private JLabel jLabel9 = new JLabel();
  private JPanel jPanel36 = new JPanel();
  private JPanel jPanel35 = new JPanel();
  private JPanel jPanel34 = new JPanel();
  private JPanel jPanel33 = new JPanel();
  private JPanel jPanel32 = new JPanel();
  private JPanel jPanel31 = new JPanel();
  private JPanel jPanel30 = new JPanel();
  private BorderLayout borderLayout17 = new BorderLayout();
  private JPanel jPanel29 = new JPanel();
  private JPanel jPanel28 = new JPanel();
  private JPanel jPanel27 = new JPanel();
  private JLabel jLabel16 = new JLabel();
  private JLabel jLabel15 = new JLabel();
  private JLabel jLabel14 = new JLabel();
  private JLabel jLabel13 = new JLabel();
  private JLabel jLabel12 = new JLabel();
  private JLabel jLabel11 = new JLabel();
  private JLabel jLabel10 = new JLabel();
  private GridLayout gridLayout5 = new GridLayout();
  private ButtonGroup buttonGroup2 = new ButtonGroup();
  private TitledBorder titledBorder4;
  private TitledBorder titledBorder3;
  private TitledBorder titledBorder2;
  private TitledBorder titledBorder1;
  private Border border1;
  private InstanceSet instances;
  private BorderLayout borderLayout34 = new BorderLayout();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private BorderLayout borderLayout35 = new BorderLayout();
  private JTable jTable1 = new JTable();
  private JPanel jPanel2 = new JPanel();
  private PreprocessorMain reference;
  private GridLayout gridLayout4 = new GridLayout();
  private JPanel jPanel14 = new JPanel();
  private JButton jButton1 = new JButton();
  private JButton jButton2 = new JButton();
  private JPanel jPanel15 = new JPanel();
  private BorderLayout borderLayout14 = new BorderLayout();
  private AttributeSelectionPanel jPanel8 = new AttributeSelectionPanel();
  private int selectedAttribute;
  private GridLayout gridLayout6 = new GridLayout();
  private GraphPaperLayout gridLayout1 = new GraphPaperLayout(new Dimension(6,6));
  private AttributeStats[] attributeStats;

  public PreprocessPanel(PreprocessorMain reference)
  { this.reference = reference;
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.preprocessor.resources.PreprocessorResource");
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception
  {
    titledBorder4 = new TitledBorder(border1,resource.getString("continuousAttributes"));
    titledBorder3 = new TitledBorder(border1,resource.getString("attributeInfo"));
    titledBorder2 = new TitledBorder(border1,resource.getString("fileAttributes"));
    titledBorder1 = new TitledBorder(border1,resource.getString("file"));
    border1 = BorderFactory.createLineBorder(new Color(153, 153, 153),1);
    jPanel7.setLayout(gridLayout2);
    jLabel6.setText(resource.getString("attributes"));
    jLabel5.setText(resource.getString("instances"));
    jLabel4.setText(resource.getString("none"));
    jLabel3.setText(resource.getString("none"));
    jLabel2.setText(resource.getString("none"));
    jLabel1.setText(resource.getString("relation"));
    jPanel23.setLayout(borderLayout13);
    jPanel22.setLayout(borderLayout12);
    jPanel21.setLayout(borderLayout11);
    jPanel20.setLayout(borderLayout10);
    jPanel19.setLayout(borderLayout9);
    jPanel18.setLayout(borderLayout8);
    jPanel13.setLayout(borderLayout7);
    jPanel12.setLayout(borderLayout6);
    jPanel11.setLayout(borderLayout5);
    jPanel10.setLayout(gridLayout3);
    gridLayout3.setColumns(2);
    gridLayout2.setRows(2);
    gridLayout2.setVgap(3);
    this.setLayout(gridLayout1);
    jPanel40.setLayout(borderLayout27);
    jPanel9.setLayout(borderLayout17);
    jPanel39.setLayout(borderLayout28);
    jPanel38.setLayout(borderLayout29);
    jPanel37.setLayout(borderLayout30);
    jLabel9.setText(resource.getString("name"));
    jPanel36.setLayout(borderLayout32);
    jPanel35.setLayout(borderLayout31);
    jPanel34.setLayout(borderLayout26);
    jPanel33.setLayout(borderLayout25);
    jPanel32.setLayout(borderLayout22);
    jPanel31.setLayout(borderLayout23);
    jPanel30.setLayout(borderLayout24);
    jPanel29.setLayout(borderLayout21);
    jPanel28.setLayout(gridLayout5);
    jLabel16.setText(resource.getString("distinct"));
    jLabel15.setText(resource.getString("none"));
    jLabel14.setText(resource.getString("missing"));
    jLabel13.setText(resource.getString("none"));
    jLabel12.setText(resource.getString("none"));
    jLabel11.setText(resource.getString("type"));
    jLabel10.setText(resource.getString("none"));
    gridLayout5.setRows(2);
    gridLayout5.setColumns(2);
    jPanel7.setBorder(titledBorder1);
    jPanel6.setBorder(titledBorder2);
    jPanel6.setLayout(borderLayout34);
    jPanel9.setBorder(titledBorder3);
    jPanel27.setLayout(borderLayout35);
    jPanel2.setLayout(gridLayout4);
    jButton1.setEnabled(false);
    jButton1.setText(resource.getString("discretizeAttribute"));
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jPanel2.setBorder(titledBorder4);
    jButton2.setEnabled(false);
    jButton2.setText(resource.getString("instancesEditor"));
    jButton2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton2_actionPerformed(e);
      }
    });
    jPanel15.setLayout(borderLayout14);
    jScrollPane2.setBorder(null);
    jPanel8.setToolTipText(resource.getString("selectedAttributes"));
    gridLayout6.setColumns(2);
    jPanel6.add(jPanel15, BorderLayout.SOUTH);
    jPanel15.add(jButton2, BorderLayout.CENTER);
    jPanel6.add(jPanel8,  BorderLayout.CENTER);
    jPanel13.add(jPanel22, BorderLayout.CENTER);
    jPanel22.add(jLabel3, BorderLayout.CENTER);
    jPanel13.add(jPanel23, BorderLayout.WEST);
    jPanel23.add(jLabel6, BorderLayout.CENTER);
    jPanel10.add(jPanel12, null);
    jPanel12.add(jPanel20, BorderLayout.CENTER);
    jPanel20.add(jLabel2, BorderLayout.CENTER);
    jPanel12.add(jPanel21, BorderLayout.WEST);
    jPanel21.add(jLabel5, BorderLayout.CENTER);
    jPanel10.add(jPanel13, null);
    jPanel7.add(jPanel11, null);
    jPanel11.add(jPanel18, BorderLayout.WEST);
    jPanel18.add(jLabel1, BorderLayout.CENTER);
    jPanel11.add(jPanel19, BorderLayout.CENTER);
    jPanel19.add(jLabel4, BorderLayout.CENTER);
    jPanel7.add(jPanel10, null);
    jPanel9.add(jPanel27, BorderLayout.CENTER);
    jPanel27.add(jScrollPane2,  BorderLayout.CENTER);
    jScrollPane2.getViewport().add(jTable1, null);
    jPanel9.add(jPanel28, BorderLayout.NORTH);
    jPanel29.add(jPanel33, BorderLayout.WEST);
    jPanel33.add(jLabel9, BorderLayout.CENTER);
    jPanel29.add(jPanel34, BorderLayout.CENTER);
    jPanel34.add(jLabel10, BorderLayout.CENTER);
    jPanel28.add(jPanel32, null);
    jPanel28.add(jPanel29, null);
    jPanel32.add(jPanel35, BorderLayout.WEST);
    jPanel35.add(jLabel11, BorderLayout.CENTER);
    jPanel32.add(jPanel36, BorderLayout.CENTER);
    jPanel36.add(jLabel12, BorderLayout.CENTER);
    jPanel28.add(jPanel31, null);
    jPanel31.add(jPanel37, BorderLayout.CENTER);
    jPanel37.add(jLabel13, BorderLayout.CENTER);
    jPanel31.add(jPanel38, BorderLayout.WEST);
    jPanel38.add(jLabel14, BorderLayout.CENTER);
    jPanel28.add(jPanel30, null);
    jPanel30.add(jPanel39, BorderLayout.CENTER);
    jPanel39.add(jLabel15, BorderLayout.CENTER);
    jPanel30.add(jPanel40, BorderLayout.WEST);
    jPanel40.add(jLabel16, BorderLayout.CENTER);
    jPanel2.add(jPanel14, null);
    jPanel14.add(jButton1, null);
    jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jTable1.getTableHeader().setReorderingAllowed(false);
    jTable1.getTableHeader().setResizingAllowed(false);
    jTable1.setColumnSelectionAllowed(false);
    jTable1.setRowSelectionAllowed(false);
    jPanel8.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {   public void valueChanged(ListSelectionEvent e)
        {   jPanel8_valueChanged(e);
	}
    });
    this.add(jPanel7, new Rectangle(0,0,3,1));
    this.add(jPanel6, new Rectangle(0,1,3,5));
    this.add(jPanel2, new Rectangle(3,0,3,1));
    this.add(jPanel9, new Rectangle(3,1,3,5));
  }

  public void setBaseInstances(InstanceSet inst)
  {   instances = inst;
      setTable(null,0); //Ao abrir um arquivo não mostra informações anteriores
      jLabel4.setText(inst.getRelationName());
      jLabel2.setText(inst.numWeightedInstances()+"");
      jLabel3.setText(inst.numAttributes()+"");
      attributeStats = inst.getAllAttributeStats();
      jPanel8.setInstances(instances);
      jButton2.setEnabled(true);
  }

  /**
   * Creates a tablemodel for the attribute being displayed
   */
  protected void setTable(AttributeStats as, int index)
  {   if (as == null)
      {   jTable1.setModel(new ValuesTableModel());
      }
	else if (as.getNumericStats() != null)
	{   Object [] colNames = {resource.getString("statistic"),resource.getString("value")};
		Object [][] data = new Object [4][2];
		Stats stats = as.getNumericStats();
		data[0][0] = resource.getString("minimum"); data[0][1] = new Float((float)stats.getMin());
		data[1][0] = resource.getString("maximum"); data[1][1] = new Float((float)stats.getMax());
		data[2][0] = resource.getString("mean");    data[2][1] = new Float((float)stats.getMean());
		data[3][0] = resource.getString("stdDev");  data[3][1] = new Float((float)stats.getStdDev());
		jTable1.setModel(new ValuesTableModel(data, colNames));
		return;
	}
      else if (as.getNominalCountsWeighted() != null)
      {   Attribute att = instances.getAttribute(index);
          Object [] colNames = {resource.getString("label"),resource.getString("count")};
      	  Object [][] data = new Object [as.getNominalCounts().length][2];
      	  for (int i = 0; i < as.getNominalCountsWeighted().length; i++)
          {   data[i][0] = att.value(i);
              data[i][1] = new Integer(as.getNominalCountsWeighted()[i]);
      	  }
      	  jTable1.setModel(new ValuesTableModel(data, colNames));
      }
      else
      {   jTable1.setModel(new ValuesTableModel());
      }
  }

  private class ValuesTableModel extends DefaultTableModel
  {   public boolean isCellEditable(int row, int col)
      {   return false;
      }

      public ValuesTableModel()
      {   super();
      }

      public ValuesTableModel(Object[][] data,Object[] colNames)
      {   super(data,colNames);
      }
  }

  /*private class ValuesTableModel extends AbstractTableModel
  {   public ValuesTableModel(AttributeStats as, int index,InstanceSet instances)
      {   if (as == null)
          {}
          if (as.getNominalCounts() != null)
          {   colNames = new String[2];
              colNames[0] = resource.getString("label");
              colNames[1] = resource.getString("count");
              Attribute att = instances.getAttribute(index);
              data = new Object [as.getNominalCounts().length][2];
      	      for (int i = 0; i < as.getNominalCounts().length; i++)
              {   data[i][0] = att.value(i);
                  data[i][1] = new Integer(as.getNominalCounts()[i]);
      	      }
          }
          else if (as.getNumericStats() != null)
          {   colNames = new String[2];
              colNames[0] = resource.getString("statistic");
              colNames[1] = resource.getString("value");
              Object [][] data = new Object [4][2];
              Stats stats = as.getNumericStats();
      	      data[0][0] = resource.getString("minimum"); data[0][1] = new Float((float)stats.getMin());
      	      data[1][0] = resource.getString("maximum"); data[1][1] = new Float((float)stats.getMax());
      	      data[2][0] = resource.getString("mean");    data[2][1] = new Float((float)stats.getMean());
      	      data[3][0] = resource.getString("stdDev");  data[3][1] = new Float((float)stats.getStdDev());
          }
      }

      public ValuesTableModel()
      {}

      public int getRowCount()
      {   return rowCount;
      }

      public int getColumnCount()
      {   return columnCount;
      }

      public Object getValueAt(int row, int column)
      {   return data[row][column];

      }

      public boolean isCellEditable(int row, int col)
      {   return false;
      }

      public String getColumnName(int c)
      {   return colNames[c];
      }

      private int rowCount = 0;
      private int columnCount = 0;
      private String[] colNames;
      private Object[][] data;
  }*/

  void jPanel8_valueChanged(ListSelectionEvent e)
  {   if (!e.getValueIsAdjusting())
      {   ListSelectionModel lm = (ListSelectionModel) e.getSource();
          for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++)
          {   if (lm.isSelectedIndex(i))
              {   try
                  {   selectedAttribute = i;
                      Attribute att = instances.getAttribute(selectedAttribute);
                      int type = att.getAttributeType();
                      if (type == Attribute.NOMINAL)
                      {   jLabel12.setText(resource.getString("nominal"));
                          jButton1.setEnabled(false);
                      }
                      else
                      {   jLabel12.setText(resource.getString("numeric"));
                          jButton1.setEnabled(true);
                      }
                      jLabel10.setText(att.getAttributeName());
                      AttributeStats attStats = attributeStats[selectedAttribute];
                      long percent = Math.round(100.0 * attStats.getMissingCountWeighted() / instances.numWeightedInstances());
                      jLabel13.setText("" + attStats.getMissingCountWeighted() + " (" + percent + "%)");
                      jLabel15.setText("" + attStats.getDistinctCount());
                      setTable(attStats, selectedAttribute);
                  }
                  catch (NullPointerException npe)
                  {}
                  break;
              }
          }
      }
  }

  //Chama editor de instâncias
  void jButton2_actionPerformed(ActionEvent e)
  {
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   new DiscretizationPanel(reference,instances,instances.getAttribute(selectedAttribute));
  }
}