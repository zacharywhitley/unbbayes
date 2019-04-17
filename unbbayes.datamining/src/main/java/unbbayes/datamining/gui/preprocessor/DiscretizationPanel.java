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
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.discretize.DiscretizationWrapper;
import unbbayes.datamining.discretize.FrequencyDiscretization;
import unbbayes.datamining.discretize.FrequencyDiscretizationWithZero;
import unbbayes.datamining.discretize.IDiscretization;
import unbbayes.datamining.discretize.RangeDiscretization;
import unbbayes.datamining.discretize.RangeDiscretizationWithZero;
import unbbayes.util.Debug;

public class DiscretizationPanel
{ private JComboBox numberStatesComboBox;
  private JComboBox discretizationTypeComboBox;
  
  private boolean isToUseMiscDiscretization = true;
  private PreprocessorMain reference;
  private InstanceSet inst;
  private Attribute selectedAttribute;

  public DiscretizationPanel(PreprocessorMain reference,InstanceSet inst,Attribute selectedAttribute) {   
	this.reference = reference;
	this.inst = inst;
	this.selectedAttribute = selectedAttribute;
	if ((JOptionPane.showInternalConfirmDialog(reference, buildPanel(), "Discretization "+selectedAttribute.getAttributeName(),
           JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)) {  
		  
		  try {
			  DiscretizationWrapper wrapper = (DiscretizationWrapper) discretizationTypeComboBox.getSelectedItem();
			  IDiscretization selectedDiscretization = wrapper.getWrapped();
			  selectedDiscretization.transformAttribute( selectedAttribute, (numberStatesComboBox.getSelectedIndex() + 1 ) );
			  reference.updateInstances( selectedDiscretization.getInstances() );
              reference.setStatusBar( selectedDiscretization.getName() + ": success" );
		  } catch (Exception e) {
			  Debug.println(getClass(), "Failed to use discretization", e);
        	  reference.setStatusBar(e.getMessage());
		  }
		  // following code was substituted with above
//		  if (discretizationTypeComboBox.getSelectedIndex() == 0) {   
//			  RangeDiscretization range = new RangeDiscretization(inst);
//              try {   
//            	  range.discretizeAttribute(selectedAttribute,(numberStatesComboBox.getSelectedIndex()+1));
//                  reference.updateInstances(range.getInstances());
//                  reference.setStatusBar("Range discretization successful");
//              } catch (Exception ex) {   
//            	  Debug.println(getClass(), "Failed to use RangeDiscretization", ex);
//            	  reference.setStatusBar(ex.getMessage());
//              }
//          } else if (discretizationTypeComboBox.getSelectedIndex() == 1) {   
//        	  FrequencyDiscretization freq = new FrequencyDiscretization(inst);
//              try {   
//            	  freq.discretizeAttribute(selectedAttribute,(numberStatesComboBox.getSelectedIndex()+1));
//                  reference.updateInstances(freq.getInstances());
//                  reference.setStatusBar("Frequency discretization successful");
//              } catch (Exception ex) {   
//            	  Debug.println(getClass(), "Failed to use FrequencyDiscretization", ex);
//            	  reference.setStatusBar(ex.getMessage());
//              }
//          } else if (isToUseMiscDiscretization()) {
//        	  if (discretizationTypeComboBox.getSelectedIndex() == 2) {
//        		  FrequencyDiscretizationWithZero freq = new FrequencyDiscretizationWithZero(inst);
//        		  try {   
//        			  freq.discretizeAttribute(selectedAttribute,(numberStatesComboBox.getSelectedIndex()+1));
//        			  reference.updateInstances(freq.getInstances());
//        			  reference.setStatusBar("Frequency discretization (with zeros) successful");
//        		  } catch (Exception ex) {   
//        			  Debug.println(getClass(), "Failed to use FrequencyDiscretizationWithZero", ex);
//        			  reference.setStatusBar(ex.getMessage());
//        		  }
//        	  } else if (discretizationTypeComboBox.getSelectedIndex() == 3) {
//        		  RangeDiscretizationWithZero discretization = new RangeDiscretizationWithZero(inst);
//        		  try {   
//        			  discretization.discretizeAttribute(selectedAttribute,(numberStatesComboBox.getSelectedIndex()+1));
//                      reference.updateInstances(discretization.getInstances());
//                      reference.setStatusBar("Range discretization successful");
//                  } catch (Exception ex) {   
//                	  Debug.println(getClass(), "Failed to use RangeDiscretizationWithZero", ex);
//                	  reference.setStatusBar(ex.getMessage());
//                  }
//        	  }
//          }
      }
  }


  private JPanel buildPanel() {  
	  JPanel jPanel5 = new JPanel(new BorderLayout());
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
//      discretizationTypeComboBox.addItem("Range");
//      discretizationTypeComboBox.addItem("Frequency");
      discretizationTypeComboBox.addItem(new DiscretizationWrapper(new RangeDiscretization(this.inst)));
      discretizationTypeComboBox.addItem(new DiscretizationWrapper(new FrequencyDiscretization(this.inst)));
      if (isToUseMiscDiscretization()) {
//    	  discretizationTypeComboBox.addItem("Frequency (single bin for zeros)");
//    	  discretizationTypeComboBox.addItem("Range (single bin for zeros)");
          discretizationTypeComboBox.addItem(new DiscretizationWrapper(new FrequencyDiscretizationWithZero(this.inst)));
          discretizationTypeComboBox.addItem(new DiscretizationWrapper(new RangeDiscretizationWithZero(this.inst)));
      }
      jPanel2.add(discretizationTypeComboBox,  BorderLayout.CENTER);

      JPanel discretizationPanel = new JPanel(new GridLayout(2,2,5,5));
      discretizationPanel.add(jPanel3, null);
      discretizationPanel.add(jPanel2, null);
      discretizationPanel.add(jPanel5, null);
      discretizationPanel.add(jPanel4, null);
      return discretizationPanel;
  }

/**
 * @return the isToUseMiscDiscretization
 */
public boolean isToUseMiscDiscretization() {
	return isToUseMiscDiscretization;
}

/**
 * @param isToUseMiscDiscretization the isToUseMiscDiscretization to set
 */
public void setToUseMiscDiscretization(boolean isToUseMiscDiscretization) {
	this.isToUseMiscDiscretization = isToUseMiscDiscretization;
}

/**
 * @return the reference
 */
public PreprocessorMain getReference() {
	return reference;
}

/**
 * @param reference the reference to set
 */
public void setReference(PreprocessorMain reference) {
	this.reference = reference;
}

/**
 * @return the selectedAttribute
 */
public Attribute getSelectedAttribute() {
	return selectedAttribute;
}

/**
 * @param selectedAttribute the selectedAttribute to set
 */
public void setSelectedAttribute(Attribute selectedAttribute) {
	this.selectedAttribute = selectedAttribute;
}

/**
 * @return the inst
 */
public InstanceSet getInst() {
	return inst;
}

/**
 * @param inst the inst to set
 */
public void setInst(InstanceSet inst) {
	this.inst = inst;
}
}