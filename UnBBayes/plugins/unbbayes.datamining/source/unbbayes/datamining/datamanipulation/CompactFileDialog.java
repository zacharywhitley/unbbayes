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
package unbbayes.datamining.datamanipulation;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class CompactFileDialog
{   public CompactFileDialog(Loader loader,Component parent)
    {   if ((JOptionPane.showInternalConfirmDialog(parent, "Compacted File?", "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION))
        {   JComboBox attributesComboBox = new JComboBox();
            attributesComboBox.setMaximumRowCount(5);
            Attribute aux;
            int numAttributes = loader.getInstanceSet().numAttributes();
            for (int i = 0 ; i < numAttributes ;i++ )
            {	aux = (Attribute)loader.getInstanceSet().getAttribute(i);
                attributesComboBox.addItem(aux.getAttributeName());
            }
            JLabel counterLabel = new JLabel("Select Counter Attribute");
            JPanel counterPanel = new JPanel(new GridLayout(2,1));
            counterPanel.add(counterLabel, null);
            counterPanel.add(attributesComboBox, null);

            if ((JOptionPane.showInternalConfirmDialog(parent, counterPanel, "", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION))
            {   int selectedAttribute = attributesComboBox.getSelectedIndex();
                loader.setCounterAttribute(selectedAttribute);
                loader.getInstanceSet().setCounterAttributeName(attributesComboBox.getSelectedItem()+"");
                loader.getInstanceSet().setCompacted(true);
            }
        }
    }
}