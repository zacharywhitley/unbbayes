package unbbayes.datamining.datamanipulation;

import java.awt.*;

import javax.swing.*;

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
                Options.getInstance().setCompactedFile(true);
            }
        }
    }
}