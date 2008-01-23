package unbbayes.datamining.gui.preprocessor;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.discretize.FrequencyDiscretization;
import unbbayes.datamining.discretize.RangeDiscretization;

public class DiscretizationPanel
{ private JComboBox numberStatesComboBox;
  private JComboBox discretizationTypeComboBox;

  public DiscretizationPanel(PreprocessorMain reference,InstanceSet inst,Attribute selectedAttribute)
  {   if ((JOptionPane.showInternalConfirmDialog(reference, buildPanel(), "Discretization "+selectedAttribute.getAttributeName(),
           JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION))
      {   if (discretizationTypeComboBox.getSelectedIndex() == 0)
          {   RangeDiscretization range = new RangeDiscretization(inst);
              try
              {   range.discretizeAttribute(selectedAttribute,(numberStatesComboBox.getSelectedIndex()+1));
                  reference.updateInstances(range.getInstances());
                  reference.setStatusBar("Range discretization successful");
              }
              catch (Exception ex)
              {   reference.setStatusBar(ex.getMessage());
              }
          }
          else if (discretizationTypeComboBox.getSelectedIndex() == 1)
          {   FrequencyDiscretization freq = new FrequencyDiscretization(inst);
              try
              {   freq.discretizeAttribute(selectedAttribute,(numberStatesComboBox.getSelectedIndex()+1));
                  reference.updateInstances(freq.getInstances());
                  reference.setStatusBar("Frequency discretization successful");
              }
              catch (Exception ex)
              {   reference.setStatusBar(ex.getMessage());
              }
          }
      }
  }

  private JPanel buildPanel()
  {   JPanel jPanel5 = new JPanel(new BorderLayout());
      JLabel numberStatesLabel = new JLabel("Number of States :");
      jPanel5.add(numberStatesLabel,  BorderLayout.CENTER);

      JPanel jPanel4 = new JPanel(new BorderLayout());
      numberStatesComboBox = new JComboBox();
      numberStatesComboBox.setMaximumRowCount(5);
      for (int i=0; i<100; i++)
      {   numberStatesComboBox.addItem((i+1)+"");
      }
      jPanel4.add(numberStatesComboBox,  BorderLayout.CENTER);

      JPanel jPanel3 = new JPanel(new BorderLayout());
      JLabel discretizationTypeLabel = new JLabel("Discretization Type :");
      jPanel3.add(discretizationTypeLabel,  BorderLayout.CENTER);

      JPanel jPanel2 = new JPanel(new BorderLayout());
      discretizationTypeComboBox = new JComboBox();
      discretizationTypeComboBox.addItem("Range");
      discretizationTypeComboBox.addItem("Frequency");
      jPanel2.add(discretizationTypeComboBox,  BorderLayout.CENTER);

      JPanel discretizationPanel = new JPanel(new GridLayout(2,2,5,5));
      discretizationPanel.add(jPanel3, null);
      discretizationPanel.add(jPanel2, null);
      discretizationPanel.add(jPanel5, null);
      discretizationPanel.add(jPanel4, null);
      return discretizationPanel;
  }
}