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

import javax.swing.BorderFactory;
import javax.swing.Box;
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
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.SpringLayout.Constraints;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.ibm.icu.impl.duration.impl.YMDDateFormatter;

public class EntitiesSearchPanel extends IUMPSTPanel {
	
	private JLabel labelEntity;
	
	private JButton buttonSearch;
	private JButton buttonAddEntity;
	private JButton buttonAddRelationship;
	
	
	private JTextField textEntity;
	

	
	
	
	public EntitiesSearchPanel(UmpstModule janelaPai){
		super(janelaPai);
		
		this.setLayout(new BorderLayout());
		//GridBagConstraints constraints = new  GridBagConstraints();
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBackground(new Color(0x4169AA));
		
		panel.add(getLabelEntity());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.add(getTextEntity());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		JPanel buttonPane = new JPanel ();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(getButtonAddEntity());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonRelationship());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonSearch());
		

		
		this.add(panel, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);
		
		
		
		}

	private Component getButtonRelationship() {
		if (buttonAddRelationship == null){
			buttonAddRelationship = new JButton ("add new relationship");
			buttonAddRelationship.setForeground(Color.blue);
			buttonAddRelationship.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					alterarJanelaAtual(new RelationshipAdd(getJanelaPai(), null));
				}
			});			
		}
		
		return buttonAddRelationship;
	}
	

	/**
	 * @return the buttonAddEntity
	 */
	public JButton getButtonAddEntity() {
		
		if (buttonAddEntity == null){
			buttonAddEntity = new JButton ("add new entity");
			buttonAddEntity.setForeground(Color.blue);
			buttonAddEntity.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					alterarJanelaAtual(new EntitiesMainPanel(getJanelaPai()));
				}
			});			
		}
		
		return buttonAddEntity;
	} 
	
	
	/**
	 * @return the labelEntity
	 */
	public JLabel getLabelEntity() {
		
		if(labelEntity == null){
			labelEntity = new JLabel("Search for a entity: ");
			labelEntity.setForeground(Color.white);
		}
		
		return labelEntity;
	}


	/**
	 * @return the buttonSearch
	 */
	public JButton getButtonSearch() {
		
		if(buttonSearch == null){
			buttonSearch = new JButton("Search: ");
			buttonSearch.setForeground(Color.blue);
		}
		
		return buttonSearch;
	}


	/**
	 * @return the textEntity
	 */
	public JTextField getTextEntity() {
		
		if (textEntity == null){
			textEntity = new JTextField(10);
		}
		
		return textEntity;
	}
	
	
	

}
