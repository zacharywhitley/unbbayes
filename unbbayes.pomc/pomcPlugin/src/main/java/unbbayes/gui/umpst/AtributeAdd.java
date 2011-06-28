package unbbayes.gui.umpst;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entities.AtributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.project.UMPSTProject;


public class AtributeAdd extends IUMPSTPanel {
	


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GridBagConstraints constraints     = new GridBagConstraints();
	private JLabel titulo            = new JLabel();
	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	private JButton buttonSubatribute = new JButton("Add Sub-Atribute");
	//private JButton buttonSubAtribute    = new JButton(iconSubAtribute);
	
	private JTextField dateText,authorText;
	private JTextField AtributeText;
	private JTextArea commentsText;
	private AtributeModel atribute,atributeFather;
	private EntityModel entityRelated;
	
	public AtributeAdd(UmpstModule janelaPai,EntityModel entityRelated, AtributeModel atribute, AtributeModel atributeFather){
		super(janelaPai);
		
		this.entityRelated=entityRelated;
		this.atribute = atribute;
		this.atributeFather=atributeFather;
		
		this.setLayout(new GridBagLayout());
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx=0; constraints.gridy = 0; constraints.weightx=0.5;constraints.weighty=0.5;	
		textPanel();
		constraints.gridx=0; constraints.gridy = 1; constraints.weightx=0.5;constraints.weighty=0.5;	
		createSubAtributeTable();
		listeners();

		if( atribute == null){
			if (atributeFather==null){
				titulo.setText("Add new atribute");
			}
			else{
				titulo.setText("Add new subatribute");
			}
			buttonAdd.setText(" Add ");
		} else {
			titulo.setText(" Update atribute");
			buttonAdd.setText(" Update ");
			AtributeText.setText(atribute.getAtributeName());
			commentsText.setText(atribute.getComments());
			authorText.setText(atribute.getAuthor());
			dateText.setText(atribute.getDate());
			//pai.setText(getPai().AtributeName);
			
			/*try {
				ID = modelo.getID( colaborador );
			} catch (DefaultException e) {
				JOptionPane.showMessageDialog(null, e.getMsg(), "unbbayes", JOptionPane.WARNING_MESSAGE); 
			}*/
		}
		
	}




	public void textPanel(){
		GridBagConstraints c = new GridBagConstraints();
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		

		c.gridx = 0; c.gridy = 2;
		panel.add( new JLabel("atribute Description: "), c);
		c.gridx = 0; c.gridy = 3;
		panel.add( new JLabel("Author Name: "), c);
		c.gridx = 0; c.gridy = 4;
		panel.add( new JLabel("Date: "), c);
		c.gridx = 0; c.gridy = 5;
		panel.add( new JLabel("Comments: "), c);
		

		GridBagConstraints d = new GridBagConstraints();
		d.gridx = 0; d.gridy = 0;
		d.fill = GridBagConstraints.PAGE_START;
		d.gridwidth = 2;
		d.insets = new Insets(0, 0, 0, 0);
		titulo.setFont(new Font("Arial", Font.BOLD, 32));
		titulo.setBackground(new Color(0x4169AA));
		panel.add( titulo, d);
		

	
	
		
		AtributeText = new JTextField(20);
		commentsText = new JTextArea(5,21);
		authorText = new JTextField(20);
		dateText = new JTextField(20);
 

		c.gridx = 1; c.gridy = 2;
		panel.add( AtributeText, c);
		
		c.gridx = 1; c.gridy = 3;
		panel.add( authorText, c);
		
		c.gridx = 1; c.gridy = 4;
		panel.add( dateText, c);
		
		c.gridx = 1; c.gridy = 5;
		panel.add( commentsText, c);
		
		
		c.gridx = 0; c.gridy = 7; c.gridwidth = 1;
		panel.add( buttonCancel, c);
		c.gridx = 1; c.gridy = 7;
		panel.add( buttonAdd, c);
		
		buttonCancel.setToolTipText("Cancel and return to entity Panel");
		buttonAdd.setToolTipText("Save this atribute");
		
		
		panel.setBorder(BorderFactory.createTitledBorder("Atribute details"));
		add(panel,constraints);
		
		//.gridx = 0; d.gridy = 8; 
		//add(buttonRelationship,d);

		//d.gridx = 1; d.gridy = 8; 
		//add(buttonSubAtribute,d);
		
		//buttonRelationship.setToolTipText("Add new Relationship");
		//buttonSubAtribute.setToolTipText("Add new SubAtribute");
	
	}
	
	
	public void listeners(){
		
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( atribute == null){
					try {
						if (AtributeText.getText().equals("")){
							JOptionPane.showMessageDialog(null, "Rule's name is empty!");
						}
						else{
						AtributeModel atributeAdd = updateMapAtribute();
						updateTable(atributeAdd);
						
						
						JOptionPane.showMessageDialog(null, "atribute successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						}
					
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating atribute", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	
					
					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this atribute?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						try{
							
							atribute.setAtributeName(AtributeText.getText());
							atribute.setComments(commentsText.getText());
							atribute.setAuthor(authorText.getText());
							atribute.setDate(dateText.getText());
							
							updateTable(atribute);
							JOptionPane.showMessageDialog(null, "atribute successfully updated", "UnBBayes", JOptionPane.INFORMATION_MESSAGE);
		
						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while ulpating atribute", "UnBBayes", JOptionPane.WARNING_MESSAGE);
							UmpstModule pai = getFatherPanel();
							changePanel(pai.getMenuPanel());	
						}
					}
				}
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				changePanel(pai.getMenuPanel());	
			}
		});
		
		buttonSubatribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePanel(new AtributeAdd(getFatherPanel(), entityRelated,null,atribute));

			}
		});
		
		

		AtributeText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				commentsText.requestFocus();
			}
		});
		
		/*commentsText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				authorText.requestFocus();
			}
		});*/
		
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
        java.net.URL imgURL = MainPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

   public void updateTable(AtributeModel atributeUpdade){
		
	    UmpstModule pai = getFatherPanel();
	    changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesMainPanel(entityRelated));

	    /*
	    TableAtribute atributeTable     = pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesMainPanel(entityRelated).getAtributeTable(entityRelated);
	    JTable table = atributeTable.createTable();
	    
	    alterarJanelaAtual(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesMainPanel(entityRelated));
	    
	    atributeTable.getScrollPanePergunta().setViewportView(table);
	    atributeTable.getScrollPanePergunta().updateUI();
	    atributeTable.getScrollPanePergunta().repaint();
	    atributeTable.updateUI();
	    atributeTable.repaint();*/
    }	
   
   public AtributeModel updateMapAtribute(){
	   String idAux = "";
	   Set<String> keys = UMPSTProject.getInstance().getMapAtribute().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		int maior = 0;
		String idAux2 = "";
		int intAux;
		
		if (atributeFather==null){
			
			if ( UMPSTProject.getInstance().getMapAtribute().size()>0){
				for (String key: sortedKeys){
					//tamanho = tamanho - UMPSTProject.getInstance().getMapGoal().get(key).getSubgoals().size();
					idAux= UMPSTProject.getInstance().getMapAtribute().get(key).getId();
					if (idAux.contains(".")){
						intAux = idAux.indexOf(".");
						idAux2 = idAux.substring(0, intAux);
						if (maior<Integer.parseInt(idAux2)){
							maior = Integer.parseInt(idAux2);
						}
					}
					else{
						if (maior< Integer.parseInt(idAux)){
							maior = Integer.parseInt(idAux);
						}
					}
				}
				maior++;
				idAux = maior+"";
			}
			else{
				idAux = 1+"";
			}
			
		}
		else{
			if (atributeFather.getMapSubAtributes()!=null){
				idAux = atributeFather.getId()+"."+ (atributeFather.getMapSubAtributes().size()+1);
				
			}
			else{
				idAux = atributeFather.getId()+".1";

			}
		}
		
		Set<EntityModel> setEntityRelated = new HashSet<EntityModel>();
		setEntityRelated.add(entityRelated);
		
		
		AtributeModel atributeAdd = new AtributeModel(idAux,AtributeText.getText(),commentsText.getText(), authorText.getText(), dateText.getText(),setEntityRelated, atributeFather,null,null,null,null);
		if (atributeFather!=null){
			atributeFather.getMapSubAtributes().put(atributeAdd.getId(), atributeAdd);
		}
		entityRelated.getMapAtributes().put(atributeAdd.getId(), atributeAdd);
		UMPSTProject.getInstance().getMapAtribute().put(atributeAdd.getId(), atributeAdd);
		
		return atributeAdd;
   }
   
   
   public void createSubAtributeTable(){
   	
	    TableSubatribute subatributeTable = new TableSubatribute(getFatherPanel(),entityRelated,atribute);
	    JTable table = subatributeTable.createTable();
	    JScrollPane scrollPane = new JScrollPane(table);

	    
	    JPanel panel = new JPanel();
	    panel.setLayout(new GridBagLayout());
	    
	    GridBagConstraints c = new GridBagConstraints();
		
	    if (atribute!=null){
	    	c.gridx = 1; c.gridy = 0; c.gridwidth=1;
	    	panel.add(buttonSubatribute,c);
	    	
	    	
	   
	    }
		
	    c.fill = GridBagConstraints.BOTH;
	    c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;
	    
	    panel.add(scrollPane,c);
	    panel.setBorder(BorderFactory.createTitledBorder("List of Subatributes"));

	   
	    add(panel,constraints);

   }
	
}