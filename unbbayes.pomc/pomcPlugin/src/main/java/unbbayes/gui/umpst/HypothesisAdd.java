package unbbayes.gui.umpst;


import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BrokenBarrierException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.groups.GroupsModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;


public class HypothesisAdd extends IUMPSTPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private  JComboBox hypothesisVinculationList;

	private ImageIcon iconSubhypothesis = createImageIcon("images/sub.png");

	
	private GridBagConstraints c     = new GridBagConstraints();
	private GridBagConstraints constraints = new GridBagConstraints();
	private JLabel titulo            = new JLabel();
	
	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	private JButton buttonSubhypothesis    = new JButton("add new subhypothesis");
	
	private JTextField dateText,authorText;
	private JTextField hypothesisText;
	private JTextArea commentsText;
	private HypothesisModel hypothesis,hypothesisFather;
	private Map<String,HypothesisModel> hypothesisChildren;
	private GoalModel goalRelated;


	
	public HypothesisAdd(UmpstModule janelaPai,UMPSTProject umpstProject,GoalModel goalRelated, HypothesisModel hypothesis, HypothesisModel hypothesisFather){
		super(janelaPai);
		
		this.setUmpstProject(umpstProject);
		this.hypothesis = hypothesis;
		this.hypothesisFather = hypothesisFather;
		this.goalRelated = goalRelated;
		
		this.setLayout(new GridBagLayout());
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx=0; constraints.gridy = 0; constraints.weightx=0.5;constraints.weighty=0.5;	
		textPanel();
		constraints.gridx=0; constraints.gridy = 1; constraints.weightx=0.5;constraints.weighty=0.5;	
		createSubhypothesisTable();
		constraints.gridx=1; constraints.gridy =0; constraints.weightx=0.5;constraints.weighty=0.5;	
		add(createTraceabilityTable(),constraints);
		listeners();

		if( hypothesis == null){
			if (hypothesisFather!=null){
				titulo.setText("Add new Sub-Hyphetesis");
			}
			else{
				titulo.setText("Add new Hyphetesis");
			}
			buttonAdd.setText(" Add ");
		} else {
			titulo.setText(" Update hyphotesis");
			buttonAdd.setText(" Update ");
			hypothesisText.setText(hypothesis.getHypothesisName());
			commentsText.setText(hypothesis.getComments());
			authorText.setText(hypothesis.getAuthor());
			dateText.setText(hypothesis.getDate());			
			
		}
		
	}

	public void textPanel(){
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		c.gridx = 0; c.gridy = 2;
		panel.add( new JLabel("Hypothesis Description: "), c);

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
		
			
		hypothesisText = new JTextField(20);
		commentsText = new JTextArea(5,21);
		commentsText.setLineWrap(true); 
		commentsText.setWrapStyleWord(true);
		commentsText.setBorder(BorderFactory.createEtchedBorder());
		authorText     = new JTextField(20);
		dateText       = new JTextField(20);
 

		c.gridx = 1; c.gridy = 2;
		panel.add( hypothesisText, c);
		
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
				
		buttonAdd.setToolTipText("save this hypothesis");
		buttonSubhypothesis.setToolTipText("Add new Sub-hypothesis");
		buttonCancel.setToolTipText("return to the previous panel");
		
		panel.setBorder(BorderFactory.createTitledBorder("Hypothesis`s details"));
		
		
		/*JPanel panelTraceSubhypo = new JPanel();
	    panel.setLayout(new GridBagLayout());
	    GridBagConstraints e = new GridBagConstraints();
	    e.fill =  GridBagConstraints.BOTH;
		e.gridx=0; e.gridy = 0; e.weightx=0.6;e.weighty=0.5;	
		panelTraceSubhypo.add(panel,e);
		e.gridx=1; e.gridy = 0; e.weightx=0.4;e.weighty=0.5;
		panelTraceSubhypo.add(createTraceabilityTable(),e);*/
	
		add(panel,constraints);
	}
	
	
	public void listeners(){
		
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( hypothesis == null){
				


					try {
						
						if (hypothesisText.getText()==null){
							JOptionPane.showMessageDialog(null, "Hypothesis name is empty!");
						}
						else{
						HypothesisModel hypothesisAdd = updateMapHypothesis();
						updateTable(hypothesisAdd);
						JOptionPane.showMessageDialog(null, "hypothesis successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						}
					
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating hypothesis", "UnBBayes", JOptionPane.WARNING_MESSAGE);
						UmpstModule pai = getFatherPanel();
						changePanel(pai.getMenuPanel());	
					
					}
				}
				else{
					if( JOptionPane.showConfirmDialog(null, "Do you want to update this hypothesis?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){
						
						try{
							hypothesis.setHypothesisName(hypothesisText.getText());
							hypothesis.setComments(commentsText.getText());
							hypothesis.setAuthor(authorText.getText());
							hypothesis.setDate(dateText.getText());
							/**Updating this hypothesis in MapHypothesis*/
							getUmpstProject().getMapHypothesis().get(hypothesis.getId()).setHypothesisName(hypothesis.getHypothesisName());
							getUmpstProject().getMapHypothesis().get(hypothesis.getId()).setComments(hypothesis.getComments());
							getUmpstProject().getMapHypothesis().get(hypothesis.getId()).setAuthor(hypothesis.getAuthor());
							getUmpstProject().getMapHypothesis().get(hypothesis.getId()).setDate(hypothesis.getDate());
							
							updateTable(hypothesis);
							JOptionPane.showMessageDialog(null, "hypothesis successfully updated", "UnBBayes", JOptionPane.INFORMATION_MESSAGE);
							
						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while ulpating hypothesis", "UnBBayes", JOptionPane.WARNING_MESSAGE);
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
			    changePanel(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goalRelated)	);
			}
		});
		
		buttonSubhypothesis.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
			    changePanel(new HypothesisAdd(getFatherPanel(),getUmpstProject(), goalRelated, null, hypothesis));				
			}
		});

		hypothesisText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				commentsText.requestFocus();
			}
		});
	/*	
		commentsText.addActionListener(new ActionListener() {
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

    
    public void updateTable(HypothesisModel hypothesisUpdate){
		
	    UmpstModule pai = getFatherPanel();
	    changePanel(getFatherPanel().getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goalRelated));

	    
	   /* TableHypothesis hypothesisTable = pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsMainPanel(goalRelated).getHypothesisTable(goalRelated);
	    JTable table = hypothesisTable.createTable();
	    
	    alterarJanelaAtual(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsMainPanel(goalRelated)	);
	    
	    hypothesisTable.getScrollPanePergunta().setViewportView(table);
	    hypothesisTable.getScrollPanePergunta().updateUI();
	    hypothesisTable.getScrollPanePergunta().repaint();
	    hypothesisTable.updateUI();
	    hypothesisTable.repaint();*/
    }
    
    public HypothesisModel updateMapHypothesis(){
    	String idAux = "";
	
    	
    	Set<String> keys = getUmpstProject().getMapHypothesis().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		//int tamanho = getUmpstProject().getMapHypothesis().size()+1;
		int maior = 0;
		String idAux2 = "";
		int intAux;
		
		if (hypothesisFather==null){
			
			if ( getUmpstProject().getMapHypothesis().size() > 0){
				for (String key: sortedKeys){
					//tamanho = tamanho - getUmpstProject().getMapGoal().get(key).getSubgoals().size();
					idAux= getUmpstProject().getMapHypothesis().get(key).getId();
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
			if (hypothesisFather.getSubHypothesis()!=null){
				idAux = hypothesisFather.getId()+"."+ (hypothesisFather.getSubHypothesis().size()+1);
				
			}
			else{
				idAux = hypothesisFather.getId()+".1";

			}
		}
		
		
		Set<GoalModel> setGoalRelated = new HashSet<GoalModel>();
		setGoalRelated.add(goalRelated);
		
		
		HypothesisModel hypothesisAdd = new HypothesisModel(idAux,hypothesisText.getText(),commentsText.getText(), 
				authorText.getText(), dateText.getText(),setGoalRelated, hypothesisFather,hypothesisChildren,null,null);
		if (hypothesisAdd.getFather()!=null){
			HypothesisModel aux = hypothesisAdd.getFather();
			while (aux!=null){
				aux.getMapSubHypothesis().put(hypothesisAdd.getId(),hypothesisAdd);
				getUmpstProject().getMapHypothesis().get(aux.getId()).getMapSubHypothesis().put(hypothesisAdd.getId(),hypothesisAdd);
				if (aux.getFather()!=null){
					aux = aux.getFather();
				}
				else{
					aux=null;
				}
			}
			//hypothesisFather.getSubHypothesis().put(hypothesisAdd.getId(), hypothesisAdd);
		}
		/**TALVEZ AQUI DE PAU POIS ELE CRIOU O HYPOTHESISADD COM O O GOALREALTED ACIMA.
		 *  POREM SO AGORA ELE ADICIONA NO GOALRELATE.HYPOTHESES ESSA HIPOSES*/
		goalRelated.getMapHypothesis().put(hypothesisAdd.getId(), hypothesisAdd);
		getUmpstProject().getMapHypothesis().put(hypothesisAdd.getId(), hypothesisAdd);
		
		return hypothesisAdd;
    }
    
 public void createSubhypothesisTable(){
    	
	    TableSubhypothesis subhypothesisTable = new TableSubhypothesis(getFatherPanel(),goalRelated,hypothesis);
	    JTable table = subhypothesisTable.createTable();
	    JScrollPane scrollPane = new JScrollPane(table);

	    
	    JPanel panel = new JPanel();
	    panel.setLayout(new GridBagLayout());
	    
	    GridBagConstraints c = new GridBagConstraints();
		
	    if (hypothesis!=null){
	    	c.gridx = 1; c.gridy = 0; c.gridwidth=1;
	    	panel.add(buttonSubhypothesis,c);
	    	c.gridx = 0; c.gridy = 0;c.gridwidth=1;
			panel.add(vinculateHypothesis() , c);
	    }
		
	    c.fill = GridBagConstraints.BOTH;
	    c.gridx=0;c.gridy=1;c.weightx=0.9;c.weighty=0.9;c.gridwidth=6;
	    
	    panel.add(scrollPane,c);
	    panel.setBorder(BorderFactory.createTitledBorder("List of Subhypothesis"));

	   
	    add(panel,constraints);

    }

 public JScrollPane  createTraceabilityTable() {
		
		int i = 0;
		JPanel panel = new JPanel();
 	
		if ( (hypothesis!=null)&&(hypothesis.getFowardTrackingEntity() !=null) ){
			EntityModel entity;
			Set<EntityModel> aux = hypothesis.getFowardTrackingEntity();
			
	    	for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
	    		entity = it.next();
	    		
	    		i++;
	    	}
		}

		if ((hypothesis!=null)&&(hypothesis.getMapSubHypothesis()!=null)){
			Set<String> keys = hypothesis.getMapSubHypothesis().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);
			
			for (String key: sortedKeys){
				
				i++;
			}
		}
 	
		if ( (hypothesis!=null)&&(hypothesis.getFowardTrackingGroups() !=null) ){
			GroupsModel group;
			Set<GroupsModel> aux = hypothesis.getFowardTrackingGroups();
			
	    	for (Iterator<GroupsModel> it = aux.iterator(); it.hasNext(); ) {
	    		group = it.next();
	 
	    		i++;
	    	}
		}
 	
 	
		Object[][] data = new Object[i+1][2];
 	
		String[] columnNames = {"Name","Type"};
		i=0;
		
		if ( (hypothesis!=null)&&(hypothesis.getFowardTrackingEntity() !=null) ){
			EntityModel entity;
			Set<EntityModel> aux = hypothesis.getFowardTrackingEntity();
			
	    	for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
	    		entity = it.next();
	    		data[i][0] = entity.getEntityName();
	    		data[i][1] = "Entity";
	    		i++;
	    	}
		}

		if ((hypothesis!=null)&&(hypothesis.getMapSubHypothesis()!=null)){
			Set<String> keys = hypothesis.getMapSubHypothesis().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);
			
			for (String key: sortedKeys){
				data[i][0] = hypothesis.getMapSubHypothesis().get(key).getHypothesisName();
				data[i][1] = "Hypothesis";
				i++;
			}
		}
		
 	
		if ( (hypothesis!=null)&&(hypothesis.getFowardTrackingGroups() !=null) ){
			GroupsModel group;
			Set<GroupsModel> aux = hypothesis.getFowardTrackingGroups();
			
	    	for (Iterator<GroupsModel> it = aux.iterator(); it.hasNext(); ) {
	    		group = it.next();
	    		data[i][0] = group.getGroupName();
	    		data[i][1] = "Group";
	    		i++;
	    	}
		}

		
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(tableModel);

		JScrollPane scrollPane = new JScrollPane(table);
		
		scrollPane.setBorder(BorderFactory.createTitledBorder("This Hypothesis Traceability"));
		
		return scrollPane;
    }
 

	public JComboBox vinculateHypothesis(){

	    Set<String> keys = getUmpstProject().getMapHypothesis().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
		
		Set<String> keysHypo;
		TreeSet<String> sortedKeysHypo;
		HypothesisModel hypothesisAux;
		int i=0;
		/**This is only to found the number of other hypothesis existents in order to create 
		 *     	    String[] allOtherHypothesis = new String[i];
		 * */
		for (String key: sortedKeys){
			if(getUmpstProject().getMapHypothesis().get(key)!=hypothesis){
				if(hypothesis.getMapSubHypothesis().size()>0){
					
					keysHypo = hypothesis.getMapSubHypothesis().keySet();
					sortedKeysHypo = new TreeSet<String>(keysHypo);	
					
					for (String keyHypo : sortedKeysHypo){
						/**Testing if the hypothesis is already in this goal*/
						if ((getUmpstProject().getMapHypothesis().get(hypothesis.getMapSubHypothesis().get(keyHypo).getId()) ) == null )
							i++;
					}

				}
				i++;
			}
		}   

	    String[] allOtherHypothesis = new String[i];

		 i=0;
			for (String key: sortedKeys){
				if(getUmpstProject().getMapHypothesis().get(key)!=hypothesis){
					if(hypothesis.getMapSubHypothesis().size()>0){
						
						keysHypo = hypothesis.getMapSubHypothesis().keySet();
						sortedKeysHypo = new TreeSet<String>(keysHypo);	
						
						for (String keyHypo : sortedKeysHypo){
							/**Testing if the hypothesis is already in this goal*/
							if ((getUmpstProject().getMapHypothesis().get(hypothesis.getMapSubHypothesis().get(keyHypo).getId()) ) == null ){
								allOtherHypothesis[i] = getUmpstProject().getMapHypothesis().get(key).getMapSubHypothesis().get(keyHypo).getHypothesisName();
								i++;
							}
						}

					}
					allOtherHypothesis[i] = getUmpstProject().getMapHypothesis().get(key).getHypothesisName();
					i++;
				}
			}
	    
		
		hypothesisVinculationList = new JComboBox(allOtherHypothesis);
		hypothesisVinculationList.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					//JOptionPane.showMessageDialog(null, "selecionou "+petList.getSelectedIndex());
					addVinculateHypothesis((String) hypothesisVinculationList.getSelectedItem());
				}
			});
		
		return hypothesisVinculationList;
		
	}
	
	public void addVinculateHypothesis(String hypothesisRelated){
		
		 Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);	

		Boolean achou = false;
	
		for (String key: sortedKeys){
			if(getUmpstProject().getMapHypothesis().get(key) !=null){	
				
				if (getUmpstProject().getMapHypothesis().get(key).getHypothesisName().equals(hypothesisRelated)){
					updateMapHypothesis(getUmpstProject().getMapHypothesis().get(key));
					achou=true;
					break;
				}
				
			}
			if (achou){
				break;
			}
		}  
		
	}
	
	 public void updateMapHypothesis(HypothesisModel hypothesisVinculated){
	    	
		 	/**Toda vez deve atualizar que agora essa hipotese tem outro pai e o goal relacionado agora tem outra hipotese*/
		 	getUmpstProject().getMapHypothesis().get(hypothesisVinculated.getId()).getGoalRelated().add(goalRelated);
		 	hypothesis.getMapSubHypothesis().put(hypothesisVinculated.getId(),hypothesisVinculated);
			
			if (hypothesisVinculated.getMapSubHypothesis()!=null){
				 Set<String> keys = hypothesisVinculated.getMapSubHypothesis().keySet();
		 		 TreeSet<String> sortedKeys = new TreeSet<String>(keys);	
		 		 HypothesisModel hypothesisAux;
	 			for (String key: sortedKeys){
	 				hypothesisAux = hypothesisVinculated.getMapSubHypothesis().get(key);
	 				
	    		 	getUmpstProject().getMapHypothesis().get(hypothesisAux.getId()).getGoalRelated().add(goalRelated);
	    		 	hypothesis.getMapSubHypothesis().put(hypothesisAux.getId(),hypothesisAux);

	 			}

			}
			//PRECISO ATUALIZAR O GOAL RELATED DA HIPOTESE QUE ESTA NO MAPA GERAL
			
			UmpstModule pai = getFatherPanel();
		    changePanel(pai.getMenuPanel().getRequirementsPane().getGoalsPanel().getGoalsAdd(goalRelated));    			
	}

	
}