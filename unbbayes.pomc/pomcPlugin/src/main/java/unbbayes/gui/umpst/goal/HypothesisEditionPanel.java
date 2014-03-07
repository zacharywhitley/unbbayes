package unbbayes.gui.umpst.goal;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.MainPropertiesEditionPane;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.gui.umpst.selection.SubHipotheseSelectionPane;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.groups.GroupModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.requirements.GoalModel;
import unbbayes.model.umpst.requirements.HypothesisModel;
import unbbayes.util.CommonDataUtil;


public class HypothesisEditionPanel extends IUMPSTPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private  JList hypothesisVinculationList;


	private JButton buttonSave 	           ;
	private JButton buttonCancel           ;
	private JButton buttonSubhypothesis    ;
	private JButton buttonReutilize        ;

	private HypothesisModel hypothesis;
	private HypothesisModel hypothesisFather;
	private Map<String,HypothesisModel> hypothesisChildren;
	private GoalModel goalRelated;

	private MainPropertiesEditionPane mainPropertiesEditionPane ; 

	private UmpstModule janelaPai; 

	private SubHipotheseSelectionPane dialog; 

	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());

	private IconController iconController = IconController.getInstance();
	
	public HypothesisEditionPanel(UmpstModule janelaPai,
			UMPSTProject umpstProject,
			GoalModel goalRelated, 
			HypothesisModel hypothesis, 
			HypothesisModel hypothesisFather){

		super(janelaPai);

		this.setUmpstProject(umpstProject);

		this.janelaPai= janelaPai; 
		this.hypothesis = hypothesis;
		this.hypothesisFather = hypothesisFather;
		this.goalRelated = goalRelated;

		createButtons();
		
		this.setLayout(new GridBagLayout());

		this.setLayout(new GridLayout(1,1));

		JSplitPane upperPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				textPanel(),
				createTraceabilityTable()
				); 


		JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				upperPane,
				createSubhypothesisTable());

		this.add(mainPane);

		upperPane.setDividerLocation(500); 
		mainPane.setDividerLocation(300);

		listeners();

	}

	private void createButtons() {
		
		buttonSave 	     = new JButton(iconController.getSaveObjectIcon());
		buttonSave.setText(resource.getString("btnSave"));
		
		if( hypothesis == null){
			buttonSave.setToolTipText(resource.getString("hpSaveHypothesis"));

		} else {
			buttonSave.setToolTipText(resource.getString("hpUpdateHypothesis"));
		}
		
		buttonCancel     = new JButton(iconController.getReturnIcon());
		buttonCancel.setText(resource.getString("btnReturn")); 
		
		buttonCancel.setToolTipText(resource.getString("hpReturnMainPanel"));
		
		
		buttonSubhypothesis    = new JButton(iconController.getListAddIcon());
		buttonSubhypothesis.setToolTipText(resource.getString("hpSaveSubHypothesis"));
		
		buttonReutilize = new JButton(iconController.getReuseAttributeIcon());
		
	}

	public JPanel textPanel(){

		String title = ""; 

		if( hypothesis == null){
			if (hypothesisFather!=null){
				title = "Sub Hypothesis";
			}
			else{
				title = "Hypothesis";
			}
		} else {
			title =  "Hypothesis";	
		}

		// CREATE FORM 
		mainPropertiesEditionPane = 
				new MainPropertiesEditionPane(buttonCancel, 
						buttonSave, 
						title, 
						"Hypothesis Details",
						null,
						null); 

		if (hypothesis != null){
			mainPropertiesEditionPane.setTitleText(hypothesis.getName());
			mainPropertiesEditionPane.setCommentsText(hypothesis.getComments());
			mainPropertiesEditionPane.setAuthorText(hypothesis.getAuthor());
			mainPropertiesEditionPane.setDateText(hypothesis.getDate());
		}

		mainPropertiesEditionPane.getPanel().setBorder(BorderFactory.createTitledBorder("Hypothesis`s details"));

		return mainPropertiesEditionPane.getPanel(); 
	}


	public void listeners(){

		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( hypothesis == null){

					try {

						if (mainPropertiesEditionPane.getTitleText()==null){
							JOptionPane.showMessageDialog(null, resource.getString("erHypothesisDescriptionEmpty"));
						}
						else{
							HypothesisModel hypothesisAdd = updateMapHypothesis();
//							updateTable(hypothesisAdd);

							changePanel(new HypothesisEditionPanel(
									getFatherPanel(),
									getUmpstProject(),goalRelated,
									hypothesisAdd,
									null));
							//						JOptionPane.showMessageDialog(null, "hypothesis successfully added",null, JOptionPane.INFORMATION_MESSAGE);
						}

					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Error while creating hypothesis", "UnBBayes", JOptionPane.WARNING_MESSAGE);
//						UmpstModule pai = getFatherPanel();
//						changePanel(pai.getMenuPanel());	

					}
				}
				else{
//					if( JOptionPane.showConfirmDialog(null, "Do you want to update this hypothesis?", "UnBBayes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ){

						try{
							hypothesis.setName(mainPropertiesEditionPane.getTitleText());
							hypothesis.setComments(mainPropertiesEditionPane.getCommentsText());
							hypothesis.setAuthor(mainPropertiesEditionPane.getAuthorText());
							hypothesis.setDate(mainPropertiesEditionPane.getDateText());

							/**Updating this hypothesis in MapHypothesis*/
							getUmpstProject().getMapHypothesis().get(hypothesis.getId()).setName(hypothesis.getName());
							getUmpstProject().getMapHypothesis().get(hypothesis.getId()).setComments(hypothesis.getComments());
							getUmpstProject().getMapHypothesis().get(hypothesis.getId()).setAuthor(hypothesis.getAuthor());
							getUmpstProject().getMapHypothesis().get(hypothesis.getId()).setDate(hypothesis.getDate());

							updateTable(hypothesis);
//							JOptionPane.showMessageDialog(null, "hypothesis successfully updated", "UnBBayes", JOptionPane.INFORMATION_MESSAGE);

						}
						catch (Exception e2) {
							JOptionPane.showMessageDialog(null,"Error while ulpating hypothesis", "UnBBayes", JOptionPane.WARNING_MESSAGE);
							UmpstModule pai = getFatherPanel();
							changePanel(pai.getMenuPanel());	
						}
//					}
				}
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UmpstModule pai = getFatherPanel();
				if (hypothesis!=null && (hypothesis.getFather() != null)){
					changePanel(new HypothesisEditionPanel(getFatherPanel(),getUmpstProject(),goalRelated,hypothesis.getFather(),null));
				}else{
					changePanel(pai.getMenuPanel().getGoalsPane().getGoalsPanel().getGoalsAdd(goalRelated)	);
				}
		
			}
		});

		buttonSubhypothesis.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				changePanel(new HypothesisEditionPanel(getFatherPanel(),getUmpstProject(), goalRelated, null, hypothesis));				
			}
		});

		buttonReutilize.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				createReutilizePanel();
			}
		});

	}

	private void createReutilizePanel() {
		dialog = new SubHipotheseSelectionPane(createHypothesisList(), this); 
		dialog.setLocationRelativeTo(janelaPai); 
		dialog.pack();
		dialog.setVisible(true);
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
		changePanel(getFatherPanel().getMenuPanel().getGoalsPane().getGoalsPanel().getGoalsAdd(goalRelated));


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


		HypothesisModel hypothesisAdd = new HypothesisModel(
				idAux,
				mainPropertiesEditionPane.getTitleText(),
				mainPropertiesEditionPane.getCommentsText(), 
				mainPropertiesEditionPane.getAuthorText(), 
				mainPropertiesEditionPane.getDateText(),
				setGoalRelated, 
				hypothesisFather,
				hypothesisChildren,
				null,
				null);

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

		CommonDataUtil.getInstance().setAuthorName(mainPropertiesEditionPane.getAuthorText()); 

		/**TALVEZ AQUI DE PAU POIS ELE CRIOU O HYPOTHESISADD COM O O GOALREALTED ACIMA.
		 *  POREM SO AGORA ELE ADICIONA NO GOALRELATE.HYPOTHESES ESSA HIPOSES*/
		goalRelated.getMapHypothesis().put(hypothesisAdd.getId(), hypothesisAdd);
		getUmpstProject().getMapHypothesis().put(hypothesisAdd.getId(), hypothesisAdd);

		return hypothesisAdd;
	}

	public JPanel createSubhypothesisTable(){

		TableSubhypothesis subhypothesisTable = new TableSubhypothesis(
				getFatherPanel(),
				goalRelated,
				hypothesis);
		
		JTable table = subhypothesisTable.createTable();
		JScrollPane scrollPane = new JScrollPane(table);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		panel.setBorder(BorderFactory.createTitledBorder("List of Subhypothesis"));
		
		if (hypothesis!=null){

			JPanel panelIcons = new JPanel();
			panelIcons.setLayout(new GridLayout(0,16)); 

			panelIcons.add(buttonSubhypothesis);
			panelIcons.add(buttonReutilize); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JLabel()); 
			panelIcons.add(new JButton(iconController.getHelpIcon())); 

			panel.add(panelIcons,BorderLayout.PAGE_START);
		}

		panel.add(scrollPane, BorderLayout.CENTER);

		return panel; 
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
			GroupModel group;
			Set<GroupModel> aux = hypothesis.getFowardTrackingGroups();

			for (Iterator<GroupModel> it = aux.iterator(); it.hasNext(); ) {
				group = it.next();

				i++;
			}
		}


		Object[][] data = new Object[i+1][2];

		if (i < 30){
			data = new Object[30][3];
		}

		String[] columnNames = {"Type", "Name"};
		
		i=0;

		if ( (hypothesis!=null)&&(hypothesis.getFowardTrackingEntity() !=null) ){
			EntityModel entity;
			Set<EntityModel> aux = hypothesis.getFowardTrackingEntity();

			for (Iterator<EntityModel> it = aux.iterator(); it.hasNext(); ) {
				entity = it.next();
				data[i][0] = "Entity";
				data[i][1] = entity.getName();
				i++;
			}
		}

		if ((hypothesis!=null)&&(hypothesis.getMapSubHypothesis()!=null)){
			Set<String> keys = hypothesis.getMapSubHypothesis().keySet();
			TreeSet<String> sortedKeys = new TreeSet<String>(keys);

			for (String key: sortedKeys){
				data[i][0] = "Hypothesis";
				data[i][1] = hypothesis.getMapSubHypothesis().get(key).getName();
				i++;
			}
		}


		if ( (hypothesis!=null)&&(hypothesis.getFowardTrackingGroups() !=null) ){
			GroupModel group;
			Set<GroupModel> aux = hypothesis.getFowardTrackingGroups();

			for (Iterator<GroupModel> it = aux.iterator(); it.hasNext(); ) {
				group = it.next();
				data[i][0] = "Group";
				data[i][1] = group.getName();
				i++;
			}
		}

		
		
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(tableModel);
		table.setEnabled(false);
		table.setGridColor(Color.WHITE); 
		
		table.getColumnModel().getColumn(0).setMaxWidth(100); 
		table.getColumnModel().getColumn(1).setMinWidth(1000); 

		JScrollPane scrollPane = new JScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		scrollPane.setBorder(BorderFactory.createTitledBorder("This Hypothesis Traceability"));

		return scrollPane;
	}

	public String[] createHypothesisList(){

		Set<String> keys = getUmpstProject().getMapHypothesis().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);	

		Set<String> keysHypo;
		TreeSet<String> sortedKeysHypo;
		int i=0;

		// Only calculate the i value! 
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
						if ((getUmpstProject().getMapHypothesis().get(
								hypothesis.getMapSubHypothesis().get(keyHypo).getId()) ) == null )
							i++;
					}

				}
				i++;
			}
		}   

		//Create and Fill the array
		String[] allOtherHypothesis = new String[i];

		i=0;

		for (String key: sortedKeys){

			HypothesisModel hipothesisModel = getUmpstProject().getMapHypothesis().get(key);

			if(hipothesisModel !=hypothesis){
				if(hypothesis.getMapSubHypothesis().size()>0){

					keysHypo = hypothesis.getMapSubHypothesis().keySet();
					sortedKeysHypo = new TreeSet<String>(keysHypo);	

					for (String keyHypo : sortedKeysHypo){
						/**Testing if the hypothesis is already in this goal*/
						if ((getUmpstProject().getMapHypothesis().get(
								hypothesis.getMapSubHypothesis().get(keyHypo).getId()) ) == null ){

							allOtherHypothesis[i] = 
									hipothesisModel.getMapSubHypothesis().get(keyHypo).getName();
							i++;
						}
					}

				}

				allOtherHypothesis[i] = getUmpstProject().getMapHypothesis().get(key).getName();
				i++;

			}
		}

		return allOtherHypothesis;

	}	

	public void addVinculateHypothesis(String hypothesisRelated){

		Set<String> keys = getUmpstProject().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);	

		Boolean achou = false;

		for (String key: sortedKeys){
			
			HypothesisModel hypothesisModel = getUmpstProject().getMapHypothesis().get(key); 
			
			if(hypothesisModel !=null){	

				if (hypothesisModel.getName().equals(hypothesisRelated)){
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
		changePanel(pai.getMenuPanel().getGoalsPane().getGoalsPanel().getGoalsAdd(goalRelated));    			
	}



}