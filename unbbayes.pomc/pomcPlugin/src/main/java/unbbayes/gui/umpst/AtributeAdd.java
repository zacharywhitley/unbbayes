package unbbayes.gui.umpst;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MenuPanel;
import unbbayes.gui.umpst.RelationshipAdd;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entities.AtributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.HypothesisModel;


public class AtributeAdd extends IUMPSTPanel {
	
	private ImageIcon iconRelationship = createImageIcon("images/hypo.png");
	private ImageIcon iconSubAtribute = createImageIcon("images/sub.png");

	
	private GridBagConstraints c     = new GridBagConstraints();
	private JLabel titulo            = new JLabel();
	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	private JButton buttonRelationship = new JButton(iconRelationship);
	//private JButton buttonSubAtribute    = new JButton(iconSubAtribute);
	
	private JTextField dateText,authorText;
	private JTextField AtributeText,commentsText;
	private AtributeModel atribute,atributeFather;
	private EntityModel entityRelated;
	
	public AtributeAdd(UmpstModule janelaPai,EntityModel entityRelated, AtributeModel atribute, AtributeModel atributeFather){
		super(janelaPai);
		
		this.entityRelated=entityRelated;
		this.atribute = atribute;
		this.atributeFather=atributeFather;
		
		this.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		labels();
		fields();
		buttons();
		listeners();

		if( atribute == null){
			titulo.setText("Add new atribute");
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




	public void labels(){
		c.gridx = 0; c.gridy = 2;
		add( new JLabel("atribute Description: "), c);
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
						
						
						AtributeModel atributeAdd = updateMapAtribute();
						updateTable(atributeAdd);
						
						
						JOptionPane.showMessageDialog(null, "atribute successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						
					
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating atribute", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getJanelaPai();
						alterarJanelaAtual(pai.getMenuPanel());	
					
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

   public void updateTable(AtributeModel atributeUpdade){
		
	    UmpstModule pai = getJanelaPai();

	    TableAtribute atributeTable     = pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesMainPanel(entityRelated).getAtributeTable(entityRelated);
	    JTable table = atributeTable.createTable();
	    
	    alterarJanelaAtual(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesMainPanel(entityRelated));
	    
	    atributeTable.getScrollPanePergunta().setViewportView(table);
	    atributeTable.getScrollPanePergunta().updateUI();
	    atributeTable.getScrollPanePergunta().repaint();
	    atributeTable.updateUI();
	    atributeTable.repaint();
    }	
   
   public AtributeModel updateMapAtribute(){
	   String idAux = "";
	   Set<String> keys = UMPSTProject.getInstance().getMapAtribute().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		int maior = 0;
		String idAux2 = "";
		int intAux;
		
		if (atributeFather==null){
			
			if ( UMPSTProject.getInstance().getMapAtribute().size()!=0){
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
		
		AtributeModel atributeAdd = new AtributeModel(idAux,AtributeText.getText(),commentsText.getText(), authorText.getText(), dateText.getText(),entityRelated, atributeFather,null);
		if (atributeFather!=null){
			atributeFather.getMapSubAtributes().put(atributeAdd.getId(), atributeAdd);
		}
		entityRelated.getMapAtributes().put(atributeAdd.getId(), atributeAdd);
		UMPSTProject.getInstance().getMapAtribute().put(atributeAdd.getId(), atributeAdd);
		
		return atributeAdd;
   }
	
}