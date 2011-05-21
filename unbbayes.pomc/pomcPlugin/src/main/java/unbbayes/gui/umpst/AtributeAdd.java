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

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MenuPanel;
import unbbayes.gui.umpst.RelationshipAdd;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entities.AtributeModel;
import unbbayes.model.umpst.entities.RelationshipModel;


public class AtributeAdd extends IUMPSTPanel {
	
	private ImageIcon iconRelationship = createImageIcon("images/hypo.png");
	private ImageIcon iconSubAtribute = createImageIcon("images/sub.png");

	
	private GridBagConstraints c     = new GridBagConstraints();
	private JLabel titulo            = new JLabel();
	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	private JButton buttonRelationship = new JButton(iconRelationship);
	private JButton buttonSubAtribute    = new JButton(iconSubAtribute);
	
	private JTextField dateText,authorText;
	private JTextField AtributeText,commentsText;
	private AtributeModel Atribute,pai;

	
	public AtributeAdd(UmpstModule janelaPai, AtributeModel Atribute){
		super(janelaPai);
		
		this.Atribute = Atribute;
		this.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		labels();
		fields();
		buttons();
		listeners();

		if( Atribute == null){
			titulo.setText("Add new Atribute");
			buttonAdd.setText(" Add ");
		} else {
			titulo.setText(" Update Atribute");
			buttonAdd.setText(" Update ");
			AtributeText.setText(Atribute.getAtributeName());
			commentsText.setText(Atribute.getComments());
			authorText.setText(Atribute.getAuthor());
			dateText.setText(Atribute.getDate());
			//pai.setText(getPai().AtributeName);
			
			/*try {
				ID = modelo.getID( colaborador );
			} catch (DefaultException e) {
				JOptionPane.showMessageDialog(null, e.getMsg(), "unbbayes", JOptionPane.WARNING_MESSAGE); 
			}*/
		}
		
	}




	public void labels(){
		c.gridx = 0; c.gridy = 2;
		add( new JLabel("Atribute Description: "), c);
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
		
		AtributeText = new JTextField(50);
		commentsText = new JTextField(50);
		authorText = new JTextField(20);
		dateText = new JTextField(10);
 

		c.gridx = 1; c.gridy = 2;
		add( AtributeText, c);
		
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
		
		d.gridx = 0; d.gridy = 8; 
		add(buttonRelationship,d);

		d.gridx = 1; d.gridy = 8; 
		add(buttonSubAtribute,d);
		
		buttonRelationship.setToolTipText("Add new Hyphotesis");
		buttonSubAtribute.setToolTipText("Add new SubAtribute");
	
	}
	
	
	public void listeners(){
		
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( Atribute == null){
					AtributeModel Atribute = new AtributeModel(AtributeText.getText(),commentsText.getText(), authorText.getText(), dateText.getText(),null);
					try {
						JOptionPane.showMessageDialog(null, "Atribute successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						UmpstModule pai = getJanelaPai();
						alterarJanelaAtual(pai.getMenuPanel());	
					
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating Atribute", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getJanelaPai();
						alterarJanelaAtual(pai.getMenuPanel());	
					
					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this Atribute?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						AtributeModel Atribute = new AtributeModel(AtributeText.getText(),commentsText.getText(), authorText.getText(), dateText.getText(),null);
						try{
				
							JOptionPane.showMessageDialog(null, "Atribute successfully updated", "UnBBayes", JOptionPane.INFORMATION_MESSAGE);
							UmpstModule pai = getJanelaPai();
							alterarJanelaAtual(pai.getMenuPanel());	
						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while ulpating Atribute", "UnBBayes", JOptionPane.WARNING_MESSAGE);
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
		
		buttonRelationship.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				alterarJanelaAtual(new RelationshipAdd(getJanelaPai(), null));

			}
		});
		
		

		AtributeText.addActionListener(new ActionListener() {
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