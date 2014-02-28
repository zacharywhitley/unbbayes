package unbbayes.gui.umpst.goal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;

public class GoalsSearchPanel extends IUMPSTPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JLabel labelGoal;
	private JButton buttonSearch;
	private JButton buttonAddGoal,buttonCancel;
	private JTextField textGoal;
	
	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());

	public GoalsSearchPanel(UmpstModule janelaPai, UMPSTProject umpstProject){
		super(janelaPai);

		this.setUmpstProject(umpstProject);

		this.setLayout(new BorderLayout());
		//GridBagConstraints constraints = new  GridBagConstraints();

		JPanel searchPanel = new JPanel();

		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.PAGE_AXIS));
		searchPanel.setBackground(new Color(0x4169AA));

		searchPanel.add(getLabelGoal());
		searchPanel.add(Box.createRigidArea(new Dimension(0,5)));
		searchPanel.add(getTextGoal());
		searchPanel.add(Box.createRigidArea(new Dimension(0,5)));
		searchPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		//------------------- Button Pane --------------------------------------
		JPanel buttonPane = new JPanel ();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		
		buttonPane.add(getButtonSearch());
		buttonPane.add(getButtonCancel());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonAddGoal());

		this.add(searchPanel, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);

	}

	/**
	 * @return the buttonCancel
	 */
	public JButton getButtonCancel() {

		if (buttonCancel == null){
			buttonCancel = new JButton (IconController.getInstance().getEditClear());
			buttonCancel.setToolTipText(resource.getString("hpCleanSearch"));
			buttonCancel.setForeground(Color.blue);
			buttonCancel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					textGoal.setText("");
					returnTableGoals();
				}
			});
		}

		return buttonCancel;
	} 

	/**
	 * @return the buttonAddGoal
	 */
	public JButton getButtonAddGoal() {

		if (buttonAddGoal == null){
			buttonAddGoal = new JButton (IconController.getInstance().getAddIconP());
			buttonAddGoal.setToolTipText(resource.getString("hpAddGoal"));
			buttonAddGoal.setForeground(Color.blue);
			buttonAddGoal.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					changePanel(getGoalsAdd(null));
				}
			});
		}

		return buttonAddGoal;
	} 

	public GoalsEditionPanel getGoalsAdd(GoalModel goal){

		GoalsEditionPanel ret = new GoalsEditionPanel(getFatherPanel(),getUmpstProject(),goal,null);

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
			buttonSearch = new JButton(IconController.getInstance().getSearchIcon());
			buttonSearch.setToolTipText(resource.getString("hpSearchGoal"));
			buttonSearch.setForeground(Color.blue);
		}

		buttonSearch.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (!textGoal.getText().equals("")){
					updateTableGoals();
				}
				else{
					JOptionPane.showMessageDialog(null, "Search is empty!");
				}
			}
		});

		return buttonSearch;
	}


	/**
	 * @return the textGoal
	 */
	public JTextField getTextGoal() {

		if (textGoal == null){
			textGoal = new JTextField(10);
			
			textGoal.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (!textGoal.getText().equals("")){
						updateTableGoals();
					}
					else{
						JOptionPane.showMessageDialog(null, "Search is empty!");
					}
				}
			});
		}

		return textGoal;
	}

	public void updateTableGoals(){
		String[] columnNames = {"ID","Goal","","",""};

		Set<GoalModel> aux = getUmpstProject().getMapSearchGoal().get(textGoal.getText()).getGoalsRelated();
		GoalModel goal;
		Object[][] data = new Object[getUmpstProject().getMapSearchGoal().get(textGoal.getText()).getGoalsRelated().size()][5];
		//Object[][] data = new Object[setGoal2.size()][5];

		Integer i=0;


		for (Iterator<GoalModel> it = aux.iterator(); it.hasNext(); ) {
			goal = it.next();  // No downcasting required.

			data[i][0] = goal.getId();
			data[i][1] = goal.getName();			
			data[i][2] = "";
			data[i][3] = "";
			data[i][4] = "";
			i++;
		}



		UmpstModule pai = getFatherPanel();
		changePanel(pai.getMenuPanel());

		TableGoals goalsTable = pai.getMenuPanel().getRequirementsPane().getGoalsTable();
		JTable table = goalsTable.createTable(columnNames,data);

		goalsTable.getScrollPanePergunta().setViewportView(table);
		goalsTable.getScrollPanePergunta().updateUI();
		goalsTable.getScrollPanePergunta().repaint();
		goalsTable.updateUI();
		goalsTable.repaint();
	}

	public void returnTableGoals(){
		String[] columnNames = {"ID","Goal","","",""};

		Object[][] data = new Object[getUmpstProject().getMapGoal().size()][5];
		Integer i=0;

		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);

		for (String key: sortedKeys){
			data[i][0] = getUmpstProject().getMapGoal().get(key).getId();
			data[i][1] = getUmpstProject().getMapGoal().get(key).getName();			
			data[i][2] = "";
			data[i][3] = "";
			data[i][4] = "";
			i++;
		}

		UmpstModule pai = getFatherPanel();
		changePanel(pai.getMenuPanel());

		TableGoals goalsTable = pai.getMenuPanel().getRequirementsPane().getGoalsTable();
		JTable table = goalsTable.createTable(columnNames,data);

		goalsTable.getScrollPanePergunta().setViewportView(table);
		goalsTable.getScrollPanePergunta().updateUI();
		goalsTable.getScrollPanePergunta().repaint();
		goalsTable.updateUI();
		goalsTable.repaint();
	}



}
