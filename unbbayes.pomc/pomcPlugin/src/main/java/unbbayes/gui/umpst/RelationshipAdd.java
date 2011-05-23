package unbbayes.gui.umpst;


import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import unbbayes.model.umpst.entities.RelationshipModel;


public class RelationshipAdd extends IUMPSTPanel {
	
	private ImageIcon iconHypothesis = createImageIcon("images/hypo.png");
	private ImageIcon iconSubRelationship = createImageIcon("images/sub.png");

	
	private GridBagConstraints c     = new GridBagConstraints();
	private JLabel titulo            = new JLabel();
	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	//private JButton buttonHypothesis = new JButton(iconHypothesis);
	//private JButton buttonSubRelationship    = new JButton(iconSubRelationship);
	
	private JTextField dateText,authorText;
	private JTextField RelationshipText,commentsText;
	private RelationshipModel Relationship,pai;

	
	public RelationshipAdd(UmpstModule janelaPai, RelationshipModel Relationship){
		super(janelaPai);
		
		this.Relationship = Relationship;
		this.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		labels();
		fields();
		buttons();
		listeners();

		if( Relationship == null){
			titulo.setText("Add new Relationship");
			buttonAdd.setText(" Add ");
		} else {
			titulo.setText(" Update Relationship");
			buttonAdd.setText(" Update ");
			RelationshipText.setText(Relationship.getRelationshipName());
			commentsText.setText(Relationship.getComments());
			authorText.setText(Relationship.getAuthor());
			dateText.setText(Relationship.getDate());
			//pai.setText(getPai().RelationshipName);
			
			/*try {
				ID = modelo.getID( colaborador );
			} catch (DefaultException e) {
				JOptionPane.showMessageDialog(null, e.getMsg(), "unbbayes", JOptionPane.WARNING_MESSAGE); 
			}*/
		}
		
	}




	public void labels(){
		c.gridx = 0; c.gridy = 2;
		add( new JLabel("Relationship Description: "), c);
		c.gridx = 0; c.gridy = 3;
		add( new JLabel("Comments: "), c);
		c.gridx = 0; c.gridy = 4;
		add( new JLabel("Author Nome: "), c);
		c.gridx = 0; c.gridy = 5;
		add( new JLabel("Date: "), c);
		

		GridBagConstraints d = new GridBagConstraints();
		d.gridx = 0; d.gridy = 0;
		d.fill = GridBagConstraints.PAGE_START;
		d.gridwidth = 2;
		d.insets = new Insets(0, 0, 0, 0);
		titulo.setFont(new Font("Arial", Font.BOLD, 32));
		titulo.setBackground(new Color(0x4169AA));
		add( titulo, d);
		
	}
	
	
	public void fields(){
		
		RelationshipText = new JTextField(50);
		commentsText = new JTextField(50);
		authorText = new JTextField(20);
		dateText = new JTextField(10);
 

		c.gridx = 1; c.gridy = 2;
		add( RelationshipText, c);
		
		c.gridx = 1; c.gridy = 3;
		add( commentsText, c);
		
		c.gridx = 1; c.gridy = 4;
		add( authorText, c);
		
		c.gridx = 1; c.gridy = 5;
		add( dateText, c);
		
	}
		
		
	
	public void buttons(){
		
		c.gridx = 0; c.gridy = 7; c.gridwidth = 1;
		add( buttonCancel, c);
		c.gridx = 1; c.gridy = 7;
		add( buttonAdd, c);
		
		GridBagConstraints d = new GridBagConstraints();
		
		/*d.gridx = 0; d.gridy = 8; 
		add(buttonHypothesis,d);

		d.gridx = 1; d.gridy = 8; 
		add(buttonSubRelationship,d);
		
		buttonHypothesis.setToolTipText("Add new Hyphotesis");
		buttonSubRelationship.setToolTipText("Add new SubRelationship");*/
	
	}
	
	
	public void listeners(){
		
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( Relationship == null){
					RelationshipModel Relationship = new RelationshipModel(RelationshipText.getText(),commentsText.getText(), authorText.getText(), dateText.getText(),null);
					try {
						JOptionPane.showMessageDialog(null, "Relationship successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						UmpstModule pai = getJanelaPai();
						alterarJanelaAtual(pai.getMenuPanel());	
					
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating Relationship", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getJanelaPai();
						alterarJanelaAtual(pai.getMenuPanel());	
					
					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this Relationship?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						unbbayes.model.umpst.entities.RelationshipModel relationship = new RelationshipModel(RelationshipText.getText(),commentsText.getText(), authorText.getText(), dateText.getText(),null);
						try{
				
							JOptionPane.showMessageDialog(null, "Relationship successfully updated", "UnBBayes", JOptionPane.INFORMATION_MESSAGE);
							UmpstModule pai = getJanelaPai();
							alterarJanelaAtual(pai.getMenuPanel());	
						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while ulpating Relationship", "UnBBayes", JOptionPane.WARNING_MESSAGE);
							UmpstModule pai = getJanelaPai();
							alterarJanelaAtual(pai.getMenuPanel());	
						}
					}
				}
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getJanelaPai();
				alterarJanelaAtual(pai.getMenuPanel());	
			}
		});
		
		
		RelationshipText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				commentsText.requestFocus();
			}
		});
		
		commentsText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				authorText.requestFocus();
			}
		});
		
		authorText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dateText.requestFocus();
			}
		});
		
		dateText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				buttonAdd.requestFocus();
			}
		});
		
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