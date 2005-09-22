package unbbayes.datamining.gui.preprocessor;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.discretize.*;

public class DiscretizationPanel implements ActionListener
{ private JComboBox numberStatesComboBox;
  private JComboBox discretizationTypeComboBox;
  private JLabel numberStatesLabel;
  private InstanceSet inst;

  public DiscretizationPanel(PreprocessorMain parent,InstanceSet inst,Attribute selectedAttribute)
  {   
  		this.inst = inst;
	  if ((JOptionPane.showInternalConfirmDialog(parent, buildPanel(), "Discretization "+selectedAttribute.getAttributeName(),
           JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION))
      {   if (discretizationTypeComboBox.getSelectedIndex() == 0)
          {   
    	  	RangeDiscretization range = new RangeDiscretization(inst);
              try
              {   range.discretizeAttribute(selectedAttribute,(numberStatesComboBox.getSelectedIndex()+1));
                  parent.updateInstances(range.getInstances());
                  parent.setStatusBar("Range discretization successful");
              }
              catch (Exception ex)
              {   parent.setStatusBar(ex.getMessage());
              }
          }
          else if (discretizationTypeComboBox.getSelectedIndex() == 1)
          {   
        	  FrequencyDiscretization freq = new FrequencyDiscretization(inst);
              try
              {   freq.discretizeAttribute(selectedAttribute,(numberStatesComboBox.getSelectedIndex()+1));
                  parent.updateInstances(freq.getInstances());
                  parent.setStatusBar("Frequency discretization successful");
              }
              catch (Exception ex)
              {   parent.setStatusBar(ex.getMessage());
              }
          }
          else if (discretizationTypeComboBox.getSelectedIndex() == 2)
          {   
        	  EntropyDiscretization entropy = 
        	  new EntropyDiscretization(inst);
              try
              {   
            	  entropy.discretizeAttribute(selectedAttribute,(numberStatesComboBox.getSelectedIndex()));
                  //entropy.discretizeAttribute(selectedAttribute);
            	  parent.updateInstances(entropy.getInstances());
                  parent.setStatusBar("Frequency discretization successful");
              }
              catch (Exception ex)
              {   parent.setStatusBar(ex.getMessage());
              }
          }      		
      }
  }
  
  public void actionPerformed(ActionEvent evt){
	  JComboBox source = (JComboBox)evt.getSource();
	  if (source.getSelectedIndex() == 2) {
		numberStatesLabel.setText("Select Class :");
		numberStatesComboBox.removeAllItems();
	    int numAtt = inst.numAttributes();
	      for(int i=0; i<numAtt; i++)
	      {   numberStatesComboBox.addItem(inst.getAttribute(i).getAttributeName());
	          if(i == (numAtt - 1))
	        	  numberStatesComboBox.setSelectedItem(inst.getAttribute(i).getAttributeName());
	      }	  
	  } else {
		numberStatesLabel.setText("Number of States :");
		numberStatesComboBox.removeAllItems();
	      for (int i=0; i<100; i++)
	      {   numberStatesComboBox.addItem((i+1)+"");
	      }
	  }
  }
  
  private JPanel buildPanel()
  {   JPanel jPanel5 = new JPanel(new BorderLayout());
      numberStatesLabel = new JLabel("Number of States :");
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
      discretizationTypeComboBox.addItem("Entropy");
      jPanel2.add(discretizationTypeComboBox,  BorderLayout.CENTER);
      discretizationTypeComboBox.addActionListener(this);

      JPanel discretizationPanel = new JPanel(new GridLayout(2,2,5,5));
      discretizationPanel.add(jPanel3, null);
      discretizationPanel.add(jPanel2, null);
      discretizationPanel.add(jPanel5, null);
      discretizationPanel.add(jPanel4, null);
      return discretizationPanel;
  }
}