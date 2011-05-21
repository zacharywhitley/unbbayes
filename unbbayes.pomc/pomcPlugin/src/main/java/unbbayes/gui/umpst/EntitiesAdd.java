package unbbayes.gui.umpst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.peer.ScrollPanePeer;
import java.util.EventObject;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;


public class EntitiesAdd extends IUMPSTPanel {
	
	
	private JLabel labelGoal;
	private JLabel labelDetailedGoal;
	private JLabel labelComments;
	private JLabel labelQueries;
	
	private JTextArea textGoal;
	private JTextArea textDetailedGoal;
	private JTextArea textComments;
	
	
	private JButton buttonAddGoal;
	private JButton buttonAddHipotesis;
	
	private JTable table;
	private JScrollPane scrollpanePergunta;
	
	ImageIcon iconAdd = createImageIcon("images/add.gif");
	ImageIcon iconDel = createImageIcon("images/del.gif");
	ImageIcon iconEdit = createImageIcon("images/edit.gif");

	
	



	public EntitiesAdd(UmpstModule janelaPai){
		super(janelaPai);
		setLayout(new GridLayout(0,1));
		
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		panel.add(getLabelGoal());

		c.gridx = 1;
		c.gridy = 0;
		panel.add(getTextGoal());

		c.gridx = 0;
		c.gridy = 1;
		panel.add(getLabelComments());

		c.gridx = 1;
		c.gridy = 1;
		panel.add(getTextComments());
		
		panel.setBackground(new Color(0x4169AA));
				
		add(panel);

	}

	/**
	 * @return the labelGoal
	 */
	public JLabel getLabelGoal() {
		if(labelGoal == null){
			labelGoal = new JLabel("Entity: ") {
				public String toString() {
					// TODO Auto-generated method stub
					return this.getText();
				}
			};
			labelGoal.setForeground(Color.white);
		}
		return labelGoal;
	}

	/**
	 * @return the labelDetailedGoal
	 */
	public JLabel getLabelDetailedGoal() {
		if(labelDetailedGoal == null){
			labelDetailedGoal = new JLabel("Detailed Goal"){
				public String toString() {
					// TODO Auto-generated method stub
					return this.getText();
				}
			};
			}
		return labelDetailedGoal;
	}

	/**
	 * @return the labelComments
	 */
	public JLabel getLabelComments() {
		if(labelComments == null){
			labelComments = new JLabel("Comments: ");
			labelComments.setForeground(Color.white);
		}
		return labelComments;
	}

	/**
	 * @return the labelQueries
	 */
	public JLabel getLabelQueries() {
		if(labelQueries == null){
			labelQueries = new JLabel("Queries");
		}
		return labelQueries;
	}

	/**
	 * @return the textGoal
	 */
	public JTextArea getTextGoal() {
		if(textGoal == null){
			textGoal = new JTextArea(5,20);
		}
		return textGoal;
	}

	/**
	 * @return the textDetailedGoal
	 */
	public JTextArea getTextDetailedGoal() {
		if(textDetailedGoal == null){
			textDetailedGoal = new JTextArea(5,20);
		}
		return textDetailedGoal;
	}

	/**
	 * @return the textComments
	 */
	public JTextArea getTextComments() {
		if(textComments == null){
			textComments = new JTextArea(5,20);
		}
		return textComments;
	}
	
	
	 
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = MenuPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    

	 
}
