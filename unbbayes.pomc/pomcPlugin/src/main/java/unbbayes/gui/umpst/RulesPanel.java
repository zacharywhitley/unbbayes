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

public class RulesPanel extends IUMPSTPanel {
	
	private JLabel labelGoal;
	
	private JButton buttonSearch;
	private JButton buttonAddRule;
	private JButton buttonAddTracking;

	/**
	 * @return the buttonAddTracking
	 */
	public JButton getButtonAddTracking() {
		
		buttonAddTracking = new JButton ("add backtracking");
		buttonAddTracking.setForeground(Color.blue);
		buttonAddTracking.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				alterarJanelaAtual(new TrackingPanel(getJanelaPai()));
			}
		});
		
		return buttonAddTracking;
	}


	private JTextField textGoal;
	

	
	
	
	public RulesPanel(UmpstModule janelaPai){
		super(janelaPai);
		
		this.setLayout(new BorderLayout());
		//GridBagConstraints constraints = new  GridBagConstraints();
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBackground(new Color(0x4169AA));
		
		panel.add(getLabelGoal());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.add(getTextGoal());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		JPanel buttonPane = new JPanel ();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(getButtonAddRule());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonSearch());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		//buttonPane.add(getButtonAddTracking());
		
		
		this.add(panel, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);
		
		
		
		}

	/**
	 * @return the buttonAddGoal
	 */
	public JButton getButtonAddRule() {
		
		if (buttonAddRule == null){
			buttonAddRule = new JButton ("add new Rule");
			buttonAddRule.setForeground(Color.blue);
			buttonAddRule.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					alterarJanelaAtual(new GoalsAdd(getJanelaPai(),null));
				}
			});
		}
		
		return buttonAddRule;
	} 
	
	
	/**
	 * @return the labelGoal
	 */
	public JLabel getLabelGoal() {
		
		if(labelGoal == null){
			labelGoal = new JLabel("Search for a goal: ");
			labelGoal.setForeground(Color.white);
		}
		
		return labelGoal;
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
	 * @return the textGoal
	 */
	public JTextField getTextGoal() {
		
		if (textGoal == null){
			textGoal = new JTextField(10);
		}
		
		return textGoal;
	}
	
	
	

}
