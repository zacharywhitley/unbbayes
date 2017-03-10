package unbbayes.gui.umpst;

import java.awt.*;  
import java.awt.event.*;  
import java.util.*;  
import javax.swing.*;  
import javax.swing.table.*;  
   

    public class ButtonColumn extends AbstractCellEditor  
        implements TableCellRenderer, TableCellEditor, ActionListener  
    {  
        JTable table;  
        JButton renderButton;  
        JButton editButton;  
        String text;  
   
        public ButtonColumn(JTable table, int column,ImageIcon icon)  
        {  
            super();  
            this.table = table;  
            

            
            renderButton = new JButton(icon);  
   
            editButton = new JButton();  
            editButton.setFocusPainted( false );  
            editButton.addActionListener( this );  
   
            TableColumnModel columnModel = table.getColumnModel();  
            columnModel.getColumn(column).setCellRenderer( this );  
            columnModel.getColumn(column).setCellEditor( this );  
            
        }  
        
        /**
		 * @return the editButton
		 */
		public JButton getEditButton() {
			return editButton;
		}

		/**
		 * @return the renderButton
		 */
		public JButton getRenderButton() {
			return renderButton;
		}

		public void addActionListener(ActionListener a){
        	this.getRenderButton().addActionListener(a);
        	this.getEditButton().addActionListener(a);
        }
        
   
        public Component getTableCellRendererComponent(  
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)  
        {  
            if (hasFocus)  
            {  
                renderButton.setForeground(table.getForeground());  
                renderButton.setBackground(UIManager.getColor("Button.background"));  
            }  
            else if (isSelected)  
            {  
                renderButton.setForeground(table.getSelectionForeground());  
                 renderButton.setBackground(table.getSelectionBackground());  
            }  
            else  
            {  
                renderButton.setForeground(table.getForeground());  
                renderButton.setBackground(UIManager.getColor("Button.background"));  
            }  
   
            renderButton.setText( (value == null) ? "" : value.toString() );  
            return renderButton;  
        }  
   
        public Component getTableCellEditorComponent(  
            JTable table, Object value, boolean isSelected, int row, int column)  
        {  
            text = (value == null) ? "" : value.toString();  
            editButton.setText( text );  
            return editButton;  
        }  
   
        public Object getCellEditorValue()  
        {  
            return text;  
        }  
   
        public void actionPerformed(ActionEvent e)  
        {  
            fireEditingStopped();  
            System.out.println( e.getActionCommand() + " : " + table.getSelectedRow());  
        }  
        
        public void teste(){
        	      	
        	
        }
        
       
    }  
  
