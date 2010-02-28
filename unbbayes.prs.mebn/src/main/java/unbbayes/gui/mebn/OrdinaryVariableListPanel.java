package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.prs.mebn.OrdinaryVariable;

public class OrdinaryVariableListPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String title; 
	private CommandOrdinaryVariable command1; 
	
	private IconController iconController = IconController.getInstance(); 
	private ImageIcon icon1; 
	
	public OrdinaryVariableListPanel(String title, List<OrdinaryVariable> listOrdinaryVariable){
		super();
		this.title = title; 
		icon1 = iconController.getMoreIcon(); 
		updateList(listOrdinaryVariable);
		
	}
	
	public void updateList(List<OrdinaryVariable> listOrdinaryVariable){
		
		this.removeAll();
		
		int minNumColums = 5; 
		int numColums = (listOrdinaryVariable.size() > 5) ? listOrdinaryVariable.size() + 1 : 5 + 1; 
		
		setLayout(new GridLayout(numColums, 1)); 
		
		//Title
//		JToolBar tool = new JToolBar();
//		tool.setLayout(new BorderLayout()); 
//		JLabel btnTitle = new JLabel(title);
//		btnTitle.setHorizontalAlignment(JLabel.CENTER); 
//		btnTitle.setBackground(new Color(155, 155, 155));
//		btnTitle.setOpaque(true); 
//		tool.add(btnTitle, BorderLayout.CENTER); 
//		add(btnTitle); 
		
		//Elements
		int i = 0; 
		for(OrdinaryVariable ov: listOrdinaryVariable){
			JToolBar toolBar = new JToolBar();
			toolBar.setLayout(new BorderLayout()); 
			toolBar.setFloatable(false); 
			
			JButtonAction1 btnAction = new JButtonAction1(ov, icon1, new CommandOrdinaryVariable());
			btnAction.setBackground(new Color(193, 207, 180)); 
			toolBar.add(btnAction, BorderLayout.LINE_START);
			
			JButtonAction1 btnObject = new JButtonAction1(ov, ov.getName() + "[" + ov.getType() + "]", new CommandOrdinaryVariable());
			btnObject.setBackground(new Color(193, 210, 205)); 
			toolBar.add(btnObject, BorderLayout.CENTER); 
			
			this.add(toolBar); 
			i++; 
		}
		
		for(int j = i; j < minNumColums; j++){
			JLabel empty = new JLabel(); 
			empty.setOpaque(true); 
			empty.setBackground(new Color(193, 210, 205)); 
			this.add(empty); 
		}
		
	}
	
	private class JButtonAction1 extends JButton{
		
		JButtonAction1(final OrdinaryVariable ov, ImageIcon icon, final CommandOrdinaryVariable cmd){
			
			super(icon); 
			
			addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					cmd.execute(ov); 
				}		
			});
		}
		
		JButtonAction1(final OrdinaryVariable ov, String text, final CommandOrdinaryVariable cmd){
			
			super(text); 
			
			addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					cmd.execute(ov); 
				}		
			});
		}
		
	}
	
	public class CommandOrdinaryVariable{
		public void execute(OrdinaryVariable ov){
			
		}
	}
	
}
