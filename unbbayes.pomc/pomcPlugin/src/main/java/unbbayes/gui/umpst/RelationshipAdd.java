package unbbayes.gui.umpst;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.entities.RelationshipModel;
import unbbayes.model.umpst.project.UMPSTProject;


public class RelationshipAdd extends IUMPSTPanel {
	
	private ImageIcon iconHypothesis = createImageIcon("images/hypo.png");
	private ImageIcon iconSubRelationship = createImageIcon("images/sub.png");

	
	private GridBagConstraints constraint     = new GridBagConstraints();
	private JLabel titulo            = new JLabel();
	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	//private JButton buttonHypothesis = new JButton(iconHypothesis);
	//private JButton buttonSubRelationship    = new JButton(iconSubRelationship);
	
	private JTextField dateText,authorText;
	private JTextField RelationshipText;
	private JTextArea commentsText;
	private RelationshipModel relationship,pai;
	
	private JList list,listAux; 
    private DefaultListModel listModel = new DefaultListModel();
	private DefaultListModel listModelAux = new DefaultListModel();
	
	private JList listAtribute,listAtributeAux; 
    private DefaultListModel listAtributeModel = new DefaultListModel();
	private DefaultListModel listAtributeModelAux = new DefaultListModel();

	
	public RelationshipAdd(UmpstModule janelaPai, RelationshipModel relationship){
		super(janelaPai);
		
		this.relationship = relationship;
		this.setLayout(new GridBagLayout());
		constraint.fill = GridBagConstraints.HORIZONTAL;
		
		constraint.gridx=0;constraint.gridy=0;constraint.weightx=0.5;constraint.weighty=0.5;
		textPanel();

		
		GridBagConstraints c     = new GridBagConstraints();
		JPanel panelBacktracking = new JPanel();
		panelBacktracking.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.BOTH;
		c.gridx=0;c.gridy=0;c.weightx=0.5;c.weighty=0.5;
		panelBacktracking.add(createBacktrackingEntity(),c);
		c.gridx=0;c.gridy=1;c.weightx=0.5;c.weighty=0.5;
		panelBacktracking.add(createBacktrackingAtribute(),c);
		
		constraint.gridx=0;constraint.gridy=1;constraint.weightx=0.5;constraint.weighty=0.5;
		add(panelBacktracking,constraint);
		constraint.gridx=1;constraint.gridy=0;constraint.weightx=0.5;constraint.weighty=0.5;		
		createRelationshipTable();
		listeners();

		if( relationship == null){
			titulo.setText("Add new relationship");
			buttonAdd.setText(" Add ");
		} else {
			titulo.setText(" Update relationship");
			buttonAdd.setText(" Update ");
			RelationshipText.setText(relationship.getRelationshipName());
			commentsText.setText(relationship.getComments());
			authorText.setText(relationship.getAuthor());
			dateText.setText(relationship.getDate());
			//pai.setText(getPai().RelationshipName);
			
			/*try {
				ID = modelo.getID( colaborador );
			} catch (DefaultException e) {
				JOptionPane.showMessageDialog(null, e.getMsg(), "unbbayes", JOptionPane.WARNING_MESSAGE); 
			}*/
		}
		
	}

	public void textPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0; c.gridy = 2;
		panel.add( new JLabel("relationship Description: "), c);
		c.gridx = 0; c.gridy = 5;
		panel.add( new JLabel("Comments: "), c);
		c.gridx = 0; c.gridy = 3;
		panel.add( new JLabel("Author Nome: "), c);
		c.gridx = 0; c.gridy = 4;
		panel.add( new JLabel("Date: "), c);
		

		GridBagConstraints d = new GridBagConstraints();
		d.gridx = 0; d.gridy = 0;
		d.fill = GridBagConstraints.PAGE_START;
		d.gridwidth = 2;
		d.insets = new Insets(0, 0, 0, 0);
		titulo.setFont(new Font("Arial", Font.BOLD, 32));
		titulo.setBackground(new Color(0x4169AA));
		panel.add( titulo, d);
		
	
	
	
		
		RelationshipText = new JTextField(20);
		commentsText = new JTextArea(5,21);
		authorText = new JTextField(20);
		dateText = new JTextField(20);
 

		c.gridx = 1; c.gridy = 2;
		panel.add( RelationshipText, c);
		
		c.gridx = 1; c.gridy = 3;
		panel.add( commentsText, c);
		
		c.gridx = 1; c.gridy = 4;
		panel.add( authorText, c);
		
		c.gridx = 1; c.gridy = 5;
		panel.add( dateText, c);
		
	
			
		c.gridx = 0; c.gridy = 7; c.gridwidth = 1;
		panel.add( buttonCancel, c);
		c.gridx = 1; c.gridy = 7;
		panel.add( buttonAdd, c);
		
		panel.setBorder(BorderFactory.createTitledBorder("Relationship Detail's"));
		
		add(panel,constraint);
	
	
	}
	
	
	public void listeners(){
		
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( relationship == null){
					try {
						
						if(RelationshipText.getText().equals("")){
							JOptionPane.showMessageDialog(null, "Relationship name is empty!");
						}
						else{
							RelationshipModel relationshipAdd = updateMapRelationship();
							updateBacktracking(relationshipAdd);
							JOptionPane.showMessageDialog(null, "relationship successfully added",null, JOptionPane.INFORMATION_MESSAGE);
							UmpstModule father = getFatherPanel();
						    alterarJanelaAtual(father.getMenuPanel());
						}
					
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating relationship", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						alterarJanelaAtual(pai.getMenuPanel());	
					
					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this relationship?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						//RelationshipText.getText(),commentsText.getText(), authorText.getText(), dateText.getText(),null,null,null);
						try{
							relationship.setRelationshipName(RelationshipText.getText());
							relationship.setComments(commentsText.getText());
							relationship.setAuthor(authorText.getText());
							relationship.setDate(dateText.getText());
							
							updateBacktracking(relationship);
							
							JOptionPane.showMessageDialog(null, "relationship successfully updated", "UnBBayes", JOptionPane.INFORMATION_MESSAGE);
							UmpstModule pai = getFatherPanel();
							alterarJanelaAtual(pai.getMenuPanel());	
						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while ulpating relationship", "UnBBayes", JOptionPane.WARNING_MESSAGE);
							UmpstModule pai = getFatherPanel();
							alterarJanelaAtual(pai.getMenuPanel());	
						}
					}
				}
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				alterarJanelaAtual(pai.getMenuPanel());	
			}
		});
		
		
		RelationshipText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				commentsText.requestFocus();
			}
		});
		
/*		commentsText.addActionListener(new ActionListener() {
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
	
	
	  public RelationshipModel updateMapRelationship(){
	    	String idAux = "";
			int intAux = 0;
			int tamanho = UMPSTProject.getInstance().getMapRelationship().size()+1;
			

			
						
				if ( UMPSTProject.getInstance().getMapRelationship().size()!=0){
					idAux = tamanho+"";
				}
				else{
					idAux = "1";
				}
		
			
			RelationshipModel relatiionshipAdd = new RelationshipModel(idAux,RelationshipText.getText(),commentsText.getText(), authorText.getText(), dateText.getText(),null,null,null);
			
			
		    UMPSTProject.getInstance().getMapRelationship().put(relatiionshipAdd.getId(), relatiionshipAdd);	
		    
		    return relatiionshipAdd;
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

    public  Box createBacktrackingEntity(){
		Box box = Box.createHorizontalBox();
		JButton buttonCopy, buttonDelete;

		
		Set<String> keys = UMPSTProject.getInstance().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			listModel.addElement(UMPSTProject.getInstance().getMapEntity().get(key).getEntityName());
		}
		
		
		/**This IF is responsable to update the first JList with all requirements elemente MINUS those 
		 * who are already registred as backtracking.
		 * */
		if (relationship!=null){
			listAux = relationship.getBacktrackingEntity();
			for (int i = 0; i < listAux.getModel().getSize();i++) {
				listModelAux.addElement((listAux.getModel().getElementAt(i)));
				if (listModel.contains(listAux.getModel().getElementAt(i))){
					listModel.remove(listModel.indexOf(listAux.getModel().getElementAt(i)));
				}
			}
			
		}
		
		list = new JList(listModel); //data has type Object[]
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		list.setVisibleRowCount(-1);
		
		
		
		

		JScrollPane listHypothesisScroller = new JScrollPane(list);
		listHypothesisScroller.setMinimumSize(new Dimension(100,150));
				
		box.add(listHypothesisScroller);
	
	
		
		buttonCopy = new JButton("copy >>");
		box.add(buttonCopy);
		buttonCopy.addActionListener(
				new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						listModelAux.addElement(list.getSelectedValue());	
						listModel.removeElement(list.getSelectedValue());

					}
				}
		
		);
		
		buttonDelete = new JButton("<< delete");
		box.add(buttonDelete);
		buttonDelete.addActionListener(
				new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						listModel.addElement(listAux.getSelectedValue());	
						listModelAux.removeElement(listAux.getSelectedValue());
					}
				}
		
		);	
		
		listAux = new JList(listModelAux);

		listAux.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listAux.setLayoutOrientation(JList.VERTICAL_WRAP);
		listAux.setVisibleRowCount(-1);
	
		JScrollPane listHypothesisScrollerAux = new JScrollPane(listAux);
		listHypothesisScrollerAux.setMinimumSize(new Dimension(100,150));
		box.add(listHypothesisScrollerAux);
				
		box.setBorder(BorderFactory.createTitledBorder("Adding backtracking from entities"));
		
		return box;
		//add(box,constraint);
		
	}
	
	
    public  Box createBacktrackingAtribute(){
		Box box = Box.createHorizontalBox();
		JButton buttonCopy, buttonDelete;

		
		Set<String> keys = UMPSTProject.getInstance().getMapAtribute().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			listAtributeModel.addElement(UMPSTProject.getInstance().getMapAtribute().get(key).getAtributeName());
		}
		
		
		/**This IF is responsable to update the first JList with all requirements elemente MINUS those 
		 * who are already registred as backtracking.
		 * */
		if (relationship!=null){
			listAtributeAux = relationship.getBacktrackingAtribute();
			for (int i = 0; i < listAtributeAux.getModel().getSize();i++) {
				listAtributeModelAux.addElement((listAtributeAux.getModel().getElementAt(i)));
				if (listAtributeModel.contains(listAtributeAux.getModel().getElementAt(i))){
					listAtributeModel.remove(listAtributeModel.indexOf(listAtributeAux.getModel().getElementAt(i)));
				}
			}
			
		}
		
		listAtribute = new JList(listAtributeModel); //data has type Object[]
		listAtribute.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listAtribute.setLayoutOrientation(JList.VERTICAL_WRAP);
		listAtribute.setVisibleRowCount(-1);
		
		
		
		

		JScrollPane listHypothesisScroller = new JScrollPane(listAtribute);
		listHypothesisScroller.setMinimumSize(new Dimension(100,150));
				
		box.add(listHypothesisScroller);
	
	
		
		buttonCopy = new JButton("copy >>");
		box.add(buttonCopy);
		buttonCopy.addActionListener(
				new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						listAtributeModelAux.addElement(listAtribute.getSelectedValue());	
						listAtributeModel.removeElement(listAtribute.getSelectedValue());

					}
				}
		
		);
		
		buttonDelete = new JButton("<< delete");
		box.add(buttonDelete);
		buttonDelete.addActionListener(
				new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						listAtributeModel.addElement(listAtributeAux.getSelectedValue());	
						listAtributeModelAux.removeElement(listAtributeAux.getSelectedValue());
					}
				}
		
		);	
		
		listAtributeAux = new JList(listAtributeModelAux);

		listAtributeAux.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listAtributeAux.setLayoutOrientation(JList.VERTICAL_WRAP);
		listAtributeAux.setVisibleRowCount(-1);
	
		JScrollPane listHypothesisScrollerAux = new JScrollPane(listAtributeAux);
		listHypothesisScrollerAux.setMinimumSize(new Dimension(100,150));
		box.add(listHypothesisScrollerAux);
				
		box.setBorder(BorderFactory.createTitledBorder("Adding backtracking from atribute"));
		
		return box;
		//add(box,constraint);
		
	}
    
	
	public void updateBacktracking(RelationshipModel relationship){
		String keyWord = "";
		Set<String> keys = UMPSTProject.getInstance().getMapEntity().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		
		
		if (listAux !=null){
			for (int i = 0; i < listAux.getModel().getSize();i++) {
				keyWord = listAux.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if (keyWord.equals( UMPSTProject.getInstance().getMapEntity().get(key).getEntityName()) ){
						UMPSTProject.getInstance().getMapEntity().get(key).getFowardTrackingRelationship().add(relationship);
					}			
				
				}
			}
			relationship.setBacktrackingEntity(listAux);

		}
		
		if (listAtributeAux !=null){
			for (int i = 0; i < listAtributeAux.getModel().getSize();i++) {
				keyWord = listAtributeAux.getModel().getElementAt(i).toString();
				for (String key: sortedKeys){
					if ( UMPSTProject.getInstance().getMapHypothesis().get(key)!=null){
						if (keyWord.equals( UMPSTProject.getInstance().getMapHypothesis().get(key).getHypothesisName()) ){
							UMPSTProject.getInstance().getMapAtribute().get(key).getFowardTrackingRelationship().add(relationship);
						}	
					}
				
				}
			}
			relationship.setBacktrackingAtribute(listAtributeAux);

		}
		
	}
	
	
	 public void createRelationshipTable(){
	    	
		    TableRelationship relationshipTable = new TableRelationship(getFatherPanel());
		    JTable table = relationshipTable.createTable();
		    JScrollPane scrollPane = new JScrollPane(table);

		    
		   
		    
		    scrollPane.setBorder(BorderFactory.createTitledBorder("Table of Relationship"));
		   
		    add(scrollPane,constraint);

	    }
	
}