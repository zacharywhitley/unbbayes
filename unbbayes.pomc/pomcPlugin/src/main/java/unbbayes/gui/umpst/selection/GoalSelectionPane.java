package unbbayes.gui.umpst.selection;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import unbbayes.controller.IconController;
import unbbayes.gui.umpst.selection.interfaces.GoalAddition;
import unbbayes.model.umpst.requirements.GoalModel;

public class GoalSelectionPane extends JDialog{

	private IconController iconController = IconController.getInstance();
	
  	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.gui.umpst.resources.Resources.class.getName());
  	
	private JList jlist;
	private JScrollPane scrollListObjectEntity;

	private final JDialog dialog; 
	
	private GoalAddition fatherPanel; 
	
	JButton btnSelect;
	JButton btnClose; 
	
	public GoalSelectionPane(Collection<GoalModel> _goals, GoalAddition _fatherPanel){
		
		super();
		
		dialog = this; 
		
		this.fatherPanel = _fatherPanel; 
		
		GoalModel[] goals = (GoalModel[])_goals.toArray(new GoalModel[0]); 
		
		this.setTitle(resource.getString("ttGoals")); 
		
		this.setModalityType(DEFAULT_MODALITY_TYPE); 
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.setLayout(new BorderLayout()); 
		
		this.jlist = new JList(goals); 
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
		
		scrollListObjectEntity = new JScrollPane(jlist);

		btnSelect = new JButton(resource.getString("queryBtnSelect"));
		btnSelect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(jlist.getSelectedValue() != null){
					List<GoalModel> listGoals = new ArrayList<GoalModel>();
					listGoals.add((GoalModel) jlist.getSelectedValue()); 
					fatherPanel.addGoalList(listGoals);
					dialog.dispose();
				}
			}
		});
		
		btnClose = new JButton(resource.getString("closeButton")); 
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				dialog.dispose();
			}
		});

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setLayout(new GridLayout());
		toolBar.add(new JLabel());
		toolBar.add(btnSelect);
		toolBar.add(btnClose);

		JLabel label = new JLabel(resource.getString("hpSelectGoals") + "               ");
		
		//TODO put options of the algorithm here. 
		
		this.add(label, BorderLayout.PAGE_START); 
		this.add(scrollListObjectEntity, BorderLayout.CENTER);
		this.add(toolBar, BorderLayout.PAGE_END);

//		setContentPane(contentPane);
		setPreferredSize(new Dimension(500,300));
		validate(); 
		pack(); 
	}
}