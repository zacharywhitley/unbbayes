package unbbayes.gui.umpst.selection;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import unbbayes.gui.umpst.entity.EntitiesEditionPanel;

public class EntitySelectionPanel extends JDialog{

	private IconController iconController = IconController.getInstance();
	
  	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.gui.umpst.resources.Resources.class.getName());
  	
	private JList jlist;
	private JScrollPane scrollListObjectEntity;

	private final JDialog dialog; 
	
	private final EntitiesEditionPanel entitiesEditionPanel;
	
	JButton btnSelect;
	JButton btnClose; 
	
	public EntitySelectionPanel(String[] _entityList, 
			EntitiesEditionPanel _entitiesEditionPanel){
		
		super();
		
		dialog = this;
		
		this.entitiesEditionPanel = _entitiesEditionPanel; 
		this.setTitle(resource.getString("ttAttributes")); 
		this.setModal(true); 
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.setLayout(new BorderLayout()); 
		
		jlist = new JList(_entityList);
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
		
		scrollListObjectEntity = new JScrollPane(jlist);

		btnSelect = new JButton(resource.getString("btnSelect"));
		btnSelect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(jlist.getSelectedValue() != null){
					entitiesEditionPanel.addVinculateAtribute((String) jlist.getSelectedValue());
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

		JLabel label = new JLabel(resource.getString("hpSelectAttribute") + "               ");
		
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
