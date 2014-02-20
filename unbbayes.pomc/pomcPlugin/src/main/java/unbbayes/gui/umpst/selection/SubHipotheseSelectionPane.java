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

import unbbayes.controller.IconController;
import unbbayes.gui.umpst.goal.HypothesisAdd;

public class SubHipotheseSelectionPane extends JDialog{

	private IconController iconController = IconController.getInstance();
	
  	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.gui.umpst.resources.Resources.class.getName());
  	
	private JList jlist;
	private JScrollPane scrollListObjectEntity;

	private final JDialog dialog; 
	
	private final HypothesisAdd hypothesisAdd;
	
	JButton btnSelect;
	JButton btnClose; 
	
	public SubHipotheseSelectionPane(JList _jlist, HypothesisAdd _hypothesisAdd){
		
		super();
		
		dialog = this;
		this.hypothesisAdd = _hypothesisAdd; 
		this.setTitle(resource.getString("TtSubhypothesis")); 
		this.setModal(true); 
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.setLayout(new BorderLayout()); 
		
		this.jlist = _jlist; 
		
		scrollListObjectEntity = new JScrollPane(jlist);

		btnSelect = new JButton(resource.getString("queryBtnSelect"));
		btnSelect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(jlist.getSelectedValue() != null){
					hypothesisAdd.addVinculateHypothesis((String) jlist.getSelectedValue());
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

		JLabel label = new JLabel(resource.getString("HpSelectSubHipothesis") + "               ");
		
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