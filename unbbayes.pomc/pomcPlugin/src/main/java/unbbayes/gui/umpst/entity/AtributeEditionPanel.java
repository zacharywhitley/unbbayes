package unbbayes.gui.umpst.entity;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.MainPanel;
import unbbayes.gui.umpst.MainPropertiesEditionPane;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.entities.AttributeModel;
import unbbayes.model.umpst.entities.EntityModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.util.CommonDataUtil;


public class AtributeEditionPanel extends IUMPSTPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GridBagConstraints constraints     = new GridBagConstraints();

	private JButton buttonAdd 	     = new JButton();
	private JButton buttonCancel     = new JButton("Cancel");
	private JButton buttonSubatribute = new JButton("Add Sub-Atribute");

	private AttributeModel atribute,atributeFather;

	private EntityModel entityRelated;

	private MainPropertiesEditionPane mainPropertiesEditionPane ; 

	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());

	public AtributeEditionPanel(UmpstModule fatherWindow,
			UMPSTProject umpstProject,
			EntityModel entityRelated, 
			AttributeModel atribute, 
			AttributeModel atributeFather){

		super(fatherWindow);
		this.setUmpstProject(umpstProject);
		this.entityRelated=entityRelated;
		this.atribute = atribute;
		this.atributeFather=atributeFather;
		
		this.setLayout(new GridLayout(1,1));
		
		JSplitPane splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createTextPanel(),
				createSubAtributeTable()); 
		
		splitPanel.setDividerLocation(300); 
		
		listeners();
		
		this.add(splitPanel); 

		if( atribute == null){
			buttonAdd.setText(" Add ");
		} else {
			buttonAdd.setText(" Update ");
		}
		
	}

	public JPanel createTextPanel(){

		String title            = resource.getString("ttAtribute");

		mainPropertiesEditionPane = 
				new MainPropertiesEditionPane(buttonCancel, 
						buttonAdd, 
						title, 
						"Atribute Details", 
						null,
						null); 

		if (atribute != null){
			mainPropertiesEditionPane.setTitleText(atribute.getAtributeName());
			mainPropertiesEditionPane.setCommentsText(atribute.getComments());
			mainPropertiesEditionPane.setAuthorText(atribute.getAuthor());
			mainPropertiesEditionPane.setDateText(atribute.getDate());
		}

		return mainPropertiesEditionPane.getPanel();

	}


	public void listeners(){

		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( atribute == null){
					try {
						if (mainPropertiesEditionPane.getTitleText().equals("")){
							JOptionPane.showMessageDialog(null, "Rule's name is empty!");
						}
						else{
							AttributeModel atributeAdd = updateMapAtribute();
							updateTable(atributeAdd);
							
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

							atribute.setAtributeName(mainPropertiesEditionPane.getTitleText());
							atribute.setComments(mainPropertiesEditionPane.getCommentsText());
							atribute.setAuthor(mainPropertiesEditionPane.getAuthorText());
							atribute.setDate(mainPropertiesEditionPane.getDateText());

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
				if(atribute.getFather() == null){
					changePanel(new EntitiesEditionPanel(getFatherPanel(),getUmpstProject(),entityRelated));	
				}else{
					changePanel(new AtributeEditionPanel(getFatherPanel(),getUmpstProject(), entityRelated,atribute.getFather(),null));
				}

			}
		});

		buttonSubatribute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePanel(new AtributeEditionPanel(getFatherPanel(),getUmpstProject(), entityRelated,null,atribute));
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

	public void updateTable(AttributeModel atributeUpdade){

		UmpstModule pai = getFatherPanel();
		
		changePanel(new AtributeEditionPanel(pai,
			this.getUmpstProject(),
			entityRelated, 
			atributeUpdade, 
			atributeUpdade.getFather())); 
		
//		changePanel(pai.getMenuPanel().getEntitiesPane().getEntitiesPanel().getEntitiesPanel(entityRelated));

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

	public AttributeModel updateMapAtribute(){
		String idAux = "";
		Set<String> keys = getUmpstProject().getMapAtribute().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		int maior = 0;
		String idAux2 = "";
		int intAux;

		if (atributeFather==null){

			if ( getUmpstProject().getMapAtribute().size()>0){
				for (String key: sortedKeys){
					//tamanho = tamanho - getUmpstProject().getMapGoal().get(key).getSubgoals().size();
					idAux= getUmpstProject().getMapAtribute().get(key).getId();
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


		AttributeModel atributeAdd = new AttributeModel(idAux,
				mainPropertiesEditionPane.getTitleText(),
				mainPropertiesEditionPane.getCommentsText(), 
				mainPropertiesEditionPane.getAuthorText(), 
				mainPropertiesEditionPane.getDateText(),
				setEntityRelated, 
				atributeFather,
				null,
				null,
				null,
				null);
		
		if (atributeFather!=null){
			atributeFather.getMapSubAtributes().put(atributeAdd.getId(), atributeAdd);
		}

		CommonDataUtil.getInstance().setAuthorName(mainPropertiesEditionPane.getAuthorText()); 

		entityRelated.getMapAtributes().put(atributeAdd.getId(), atributeAdd);
		getUmpstProject().getMapAtribute().put(atributeAdd.getId(), atributeAdd);

		return atributeAdd;
	}


	public JPanel createSubAtributeTable(){

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

		return panel;

	}

}