package unbbayes.gui.umpst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import unbbayes.model.umpst.requirements.GoalModel;

public class GoalsSearchPanel extends IUMPSTPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JLabel labelGoal;
	
	private JButton buttonSearch;
	private JButton buttonAddGoal;

	private JTextField textGoal;
	

	
	
	
	public GoalsSearchPanel(UmpstModule janelaPai){
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
		buttonPane.add(getButtonAddGoal());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonSearch());
		

		
		this.add(panel, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);
		
		
		
		}

	/**
	 * @return the buttonAddGoal
	 */
	public JButton getButtonAddGoal() {
		
		if (buttonAddGoal == null){
			buttonAddGoal = new JButton ("add new goal");
			buttonAddGoal.setForeground(Color.blue);
			buttonAddGoal.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					alterarJanelaAtual(getGoalsMainPanel(null));
				}
			});
		}
		
		return buttonAddGoal;
	} 
	
	
	public GoalsMainPanel getGoalsMainPanel(GoalModel goal){
		
		GoalsMainPanel ret = new GoalsMainPanel(getJanelaPai(),goal,null);
		
		return ret;
		
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
