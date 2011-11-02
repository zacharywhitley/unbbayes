/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.BooleanStateEntity;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.DuplicatedNameException;
import unbbayes.prs.mebn.exception.ReservedWordException;

/**
 * Panel for selection of the possible values (states) of a resident node.
 * 
 * The states may be: 
 * - Categorical State Entity
 * - Object Entity
 * - Boolean State 
 * (all the states may be of the same type). 
 *
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class PossibleValuesEditionPane extends JPanel {

	private final String PANEL_CATEGORY_STATES = "category";
	private final String PANEL_BOOLEAN_STATES = "boolean";
	private final String PANEL_OBJECT_STATES = "object";
	
	private ResidentNode residentNode;

	private MEBNController mebnController;

	private JPanel jtbOptions;

	private StatesPanel panelStates;

	private ListStatesPanel listStatesPanel;

	private CardLayout cardLayout;

	private CategoryStatesPanel panelCategoryStates;

	private BooleanStatesPanel panelBooleanStates;

	private ObjectStatesPanel panelObjectStates;

	private List<StateLink> statesList;

	private JList statesJList;

	private DefaultListModel statesListModel;

	private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");

	private Matcher matcher;

	/** Load resource file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.mebn.resources.Resources.class.getName());

	private final IconController iconController = IconController.getInstance();

	/**
	 * Create a empty panel.
	 */
	public PossibleValuesEditionPane() {
		super();
	}

	/**
	 * Create the panel for edition of possible values of the resident node.
	 *
	 * @param _controller
	 * @param _residentNode
	 */
	public PossibleValuesEditionPane(MEBNController _controller,
			ResidentNode _residentNode) {

		super(new BorderLayout());

		residentNode = _residentNode;
		mebnController = _controller;

		buildListStates();
		jtbOptions = new OptionsPanel();
		listStatesPanel = new ListStatesPanel();
		panelStates = new StatesPanel();

		add(jtbOptions, BorderLayout.NORTH);
		add(listStatesPanel, BorderLayout.CENTER);
		add(panelStates, BorderLayout.SOUTH);
	}

	/**
	 * Altera o painel para refletir a entidade que o usu�rio selecionou na
	 * lista de estados.
	 *
	 * @param entitySelected
	 */
	public void selectState(StateLink stateLink) {

		Entity entitySelected = stateLink.getState(); 
		
		if (entitySelected instanceof CategoricalStateEntity) {
			showCategoryStatesPanel(stateLink);
		} else {
			if (entitySelected instanceof BooleanStateEntity) {
				showBooleanStatesPanel(stateLink);
			} else {
				if (entitySelected instanceof ObjectEntity) {
					showObjectStatesPanel(stateLink);
				}
			}
		}

	}

	public void showCategoryStatesPanel(StateLink entitySelected) {
		cardLayout.show(panelStates, PANEL_CATEGORY_STATES);
		panelCategoryStates.selectState(entitySelected);
	}

	public void showBooleanStatesPanel(StateLink entitySelected) {
		cardLayout.show(panelStates, PANEL_BOOLEAN_STATES);
		panelBooleanStates.selectState(entitySelected);
	}

	public void showObjectStatesPanel(StateLink entitySelected) {
		cardLayout.show(panelStates, PANEL_OBJECT_STATES);
		panelObjectStates.selectState(entitySelected);
	}

	public void buildListStates() {
		statesList = residentNode.getPossibleValueLinkList();

		statesListModel = new DefaultListModel();
		for (StateLink entity : statesList){
			statesListModel.addElement(entity);
		}

		statesJList = new JList(statesListModel);
		statesJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//statesJList.setSelectedIndex(0);
		statesJList.setCellRenderer(new StateCellRenderer());
		statesJList.setLayoutOrientation(JList.VERTICAL);
		statesJList.setVisibleRowCount(-1);
	}
	
	public JList buildListAllStates() {
		
		List<CategoricalStateEntity> list =  mebnController.getMultiEntityBayesianNetwork().getCategoricalStatesEntityContainer().getListEntity();  
		DefaultListModel model = new DefaultListModel(); 
		JList listAllStates = new JList(); 
		
		model = new DefaultListModel();
		for (Entity entity : list){
			model.addElement(entity);
		}

		listAllStates = new JList(model);
		listAllStates.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listAllStates.setSelectedIndex(0);
		listAllStates.setCellRenderer(new StateCellRenderer());
		listAllStates.setLayoutOrientation(JList.VERTICAL);
		listAllStates.setVisibleRowCount(-1);
		
		return listAllStates; 
	}


	/**
	 * Build a popup with the categorical states of the MTheory for the user
	 * select the states of the node. 
	 */
	public JFrame buildPopupStateSelection(){
		
		final JFrame newFrame = new JFrame(); 
		
		final JList listAllStates = buildListAllStates(); 
		ListAllStatesPanel paneAllStates = new ListAllStatesPanel(listAllStates); 
		
		
		JToolBar barButtons = new JToolBar(); 
		barButtons.setLayout(new GridLayout(1,2)); 
		
		JButton btnAdd = new JButton(iconController.getMoreIcon());
		btnAdd.setToolTipText(resource.getString("addSelectedStatesTip")); 
		
		JButton btnExit = new JButton(resource.getString("closeButton"));
		
		btnAdd.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				Object[] list = (Object[])listAllStates.getSelectedValues(); 
				
				if (!(residentNode.getPossibleValueLinkList().isEmpty())
						&& (residentNode.getTypeOfStates() != IResidentNode.CATEGORY_RV_STATES)) {
					int answer = JOptionPane.showConfirmDialog(
							mebnController.getMebnEditionPane(),
							resource.getString("warningDeletStates"),
							resource.getString("confirmation"),
							JOptionPane.YES_NO_OPTION);
					if (answer == JOptionPane.YES_OPTION) {
						mebnController
						.removeAllPossibleValues(residentNode);
						residentNode.setTypeOfStates(IResidentNode.CATEGORY_RV_STATES);	
					}
					else{
						return; 					
					}
				}
				
				for(int i = 0; i < list.length; i++){
					mebnController.addPossibleValue(residentNode, (CategoricalStateEntity)list[i]); 
				}
				
				listStatesPanel.update();
			}
			
		}); 
		
		btnExit.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				newFrame.dispose();
			}
			
		});
		
		barButtons.add(btnAdd); 
		barButtons.add(btnExit);
		barButtons.setFloatable(false); 
		
		JPanel panel = new JPanel(new BorderLayout()); 
		panel.add(paneAllStates, BorderLayout.CENTER); 
		panel.add(barButtons, BorderLayout.PAGE_END); 
		

		newFrame.setContentPane(panel); 
		newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
		newFrame.setVisible(true); 
		newFrame.setSize(200, 300); 
		newFrame.setTitle(resource.getString("existentStatesDialogTip")); 
		newFrame.setLocationRelativeTo(null);
		newFrame.validate(); 
	
		
		return newFrame; 
	}
	
	
	
	/**
	 * ScroolPane with the state list of the node.
	 */
	private class ListStatesPanel extends JScrollPane {

		private boolean listenerActive = true; 
		
		public ListStatesPanel() {
			super(statesJList);

			statesJList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent arg0) {
					if(listenerActive)
					selectState((StateLink) statesJList.getSelectedValue());
				}
			});
		}

		/**
		 * Update the list of category states.
		 */
		public void update() {

			listenerActive = false; 
			statesListModel.clear();
			
			statesList = residentNode.getPossibleValueLinkList();

			for (StateLink entity : statesList) {
				statesListModel.addElement(entity);
			}
			
		    listenerActive = true; 
			statesJList.validate(); 
			this.validate();
		}

	}
	
	/**
	 * ScroolPane contendo a lista de estados.
	 */
	private class ListAllStatesPanel extends JScrollPane {

		private JList list; 
		
		public ListAllStatesPanel(JList list) {
			super(list);

			this.list = list; 
			
			list.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent arg0) {
					//selectState((Entity) statesJList.getSelectedValue());
				}

			});	
		}
		
	}

	private class StatesPanel extends JPanel {

		private int selectedPanel;

		public StatesPanel() {
			super();
			cardLayout = new CardLayout();
			this.setLayout(cardLayout);

			panelCategoryStates = new CategoryStatesPanel();
			panelObjectStates = new ObjectStatesPanel();
			panelBooleanStates = new BooleanStatesPanel();

			add(PANEL_CATEGORY_STATES, panelCategoryStates);
			add(PANEL_OBJECT_STATES, panelObjectStates);
			add(PANEL_BOOLEAN_STATES, panelBooleanStates);

			switch (residentNode.getTypeOfStates()) {
			case IResidentNode.OBJECT_ENTITY:
				cardLayout.show(this, PANEL_OBJECT_STATES);
				selectedPanel = IResidentNode.OBJECT_ENTITY;
				
				break;
			case IResidentNode.CATEGORY_RV_STATES:
				cardLayout.show(this, PANEL_CATEGORY_STATES);
				selectedPanel = IResidentNode.CATEGORY_RV_STATES;
				break;
			case IResidentNode.BOOLEAN_RV_STATES:
				cardLayout.show(this, PANEL_BOOLEAN_STATES);
				selectedPanel = IResidentNode.BOOLEAN_RV_STATES;
				break;
			}

		}

		public int getSelectedPanel() {
			return selectedPanel;
		}

		public void setSelectedPanel(int selectedPanel) {
			this.selectedPanel = selectedPanel;
		}

	}

	/**
	 * Painel para a edi��o de estados do tipo categ�rico.
	 */

	private class CategoryStatesPanel extends JPanel {

		private JButton btnAdd;

		private JButton btnRemove;
		
		private JButton btnListPossibleStates; 

		private JCheckBox checkGloballyExclusive;

		private StateLink selectEntity;

		private final JTextField txtName = new JTextField(10);

		public CategoryStatesPanel() {

			/*------------------------- Build Panel ---------------------------*/

			super(new GridLayout(3, 1));
			btnAdd = new JButton(iconController.getMoreIcon());
			btnAdd.setToolTipText(resource.getString("addStateTip"));
			btnRemove = new JButton(iconController.getLessIcon());
			btnRemove.setToolTipText(resource.getString("removeState"));
			btnListPossibleStates = new JButton(iconController.getStateIcon()); 
			btnListPossibleStates.setToolTipText(resource.getString("addPreDefinedState")); 
			
			JToolBar barOptions = new JToolBar();
			barOptions.setLayout(new GridLayout(1, 3));
			barOptions.setFloatable(false);
			barOptions.add(btnAdd);
			barOptions.add(btnRemove);
			barOptions.add(btnListPossibleStates); 
			
			JToolBar barName = new JToolBar();
			barName.setFloatable(false);
			JLabel labelName = new JLabel(resource.getString("nameLabel") + " ");
			barName.add(labelName);
			barName.add(txtName);

			JToolBar toolGloballyExclusive = new JToolBar();
			toolGloballyExclusive.setFloatable(false);
			JLabel labelExclusive = new JLabel(resource
					.getString("isGloballyExclusive"));
			checkGloballyExclusive = new JCheckBox();
			checkGloballyExclusive.setSelected(false);
			
			checkGloballyExclusive.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					if(selectEntity != null){
						mebnController.setGloballyExclusiveProperty(selectEntity, checkGloballyExclusive.isSelected()); 
					}
				}
				
			}); 
			toolGloballyExclusive.add(checkGloballyExclusive);
			toolGloballyExclusive.add(labelExclusive);

			add(barOptions);
			add(barName);
			add(toolGloballyExclusive);

			/*--------------------------- Add listeners ----------------------*/

			txtName.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if ((e.getKeyCode() == KeyEvent.VK_ENTER)
							&& (txtName.getText().length() > 0)) {
						addState();
					}
				}
			});

			btnAdd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (txtName.getText().length() > 0) {
						addState();
					}
				}
			});

			btnRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeState();
				}
			});

			btnListPossibleStates.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					buildPopupStateSelection(); 
				}
				
			}); 
			
		}

		/**
		 * Show the state into edition pane.
		 */
		public void selectState(StateLink entity) {
			selectEntity = entity;
			if (entity != null) {
				txtName.setText(entity.getState().getName());
				checkGloballyExclusive
						.setSelected(entity.isGloballyExclusive());
			} else {
				txtName.setText("");
				checkGloballyExclusive.setSelected(false);
			}
		}

		public StateLink getSelectEntity() {
			return selectEntity;
		}

		public void addState() {
			try {
				String nameValue = txtName.getText(0, txtName.getText()
						.length());
				matcher = wordPattern.matcher(nameValue);
				if (matcher.matches()) {
					if(mebnController.existPossibleValue(nameValue)){
						JOptionPane.showMessageDialog(null, resource
								.getString("nameException"), resource
								.getString("nameAlreadyExists"),
								JOptionPane.ERROR_MESSAGE);
					}else{
						if (!residentNode.existsPossibleValueByName(nameValue)){
							if (!(residentNode.getPossibleValueLinkList().isEmpty())
									&& (residentNode.getTypeOfStates() != IResidentNode.CATEGORY_RV_STATES)) {
								int answer = JOptionPane.showConfirmDialog(
										mebnController.getMebnEditionPane(),
										resource.getString("warningDeletStates"),
										resource.getString("confirmation"),
										JOptionPane.YES_NO_OPTION);
								if (answer == JOptionPane.YES_OPTION) {
									mebnController
									.removeAllPossibleValues(residentNode);
									StateLink stateLink;
									try {
										stateLink = mebnController.addPossibleValue(residentNode,
												nameValue);

										stateLink.setGloballyExclusive(checkGloballyExclusive.isSelected());
										residentNode
										.setTypeOfStates(IResidentNode.CATEGORY_RV_STATES);
										
									} catch (DuplicatedNameException e) {
		  	  							JOptionPane.showMessageDialog(mebnController.getScreen(),
		  	  									resource.getString("nameException"),
		  	  									resource.getString("nameDuplicated"),
		  	  									JOptionPane.ERROR_MESSAGE);
									} catch (ReservedWordException e2) {
		  	  							JOptionPane.showMessageDialog(mebnController.getScreen(),
		  	  									resource.getString("nameException"),
		  	  									resource.getString("nameReserved"),
		  	  									JOptionPane.ERROR_MESSAGE);
									}
								}
							} else {
								StateLink stateLink;
								try {
									stateLink = mebnController.addPossibleValue(residentNode, nameValue);
									stateLink.setGloballyExclusive(checkGloballyExclusive.isSelected());
								} catch (DuplicatedNameException e) {
	  	  							JOptionPane.showMessageDialog(mebnController.getScreen(),
	  	  									resource.getString("nameException"),
	  	  									resource.getString("nameDuplicated"),
	  	  									JOptionPane.ERROR_MESSAGE);
								} catch (ReservedWordException e2) {
	  	  							JOptionPane.showMessageDialog(mebnController.getScreen(),
	  	  									resource.getString("nameException"),
	  	  									resource.getString("nameReserved"),
	  	  									JOptionPane.ERROR_MESSAGE);
								}
							}
							
						} else {
							JOptionPane.showMessageDialog(null, resource
									.getString("nameException"), resource
									.getString("nameDuplicated"),
									JOptionPane.ERROR_MESSAGE);
						}
						
						txtName.setText("");
						checkGloballyExclusive.setSelected(false);
					}
				} else {
					JOptionPane.showMessageDialog(null, resource
							.getString("nameException"), resource
							.getString("nameError"),
							JOptionPane.ERROR_MESSAGE);
					txtName.selectAll();
				}
				
				listStatesPanel.update();
				
			} catch (javax.swing.text.BadLocationException ble) {
				System.out.println(ble.getMessage());
			}
		}

		public void removeState() {
			if (statesJList.getSelectedValue() != null) {
				mebnController.removePossibleValue(residentNode, statesJList
						.getSelectedValue().toString());
			} else {
				String nameValue;
				try {
					nameValue = txtName.getText(0, txtName.getText().length());

					matcher = wordPattern.matcher(nameValue);
					if (matcher.matches()) {
						boolean teste = mebnController.existsPossibleValue(
								residentNode, nameValue);
						if (teste) {
							mebnController.removePossibleValue(residentNode,
									nameValue);
						}
					}
				} catch (BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			listStatesPanel.update();
		}
	}

	private class ObjectStatesPanel extends JPanel {

		final JComboBox comboEntities = new JComboBox(mebnController
				.getMultiEntityBayesianNetwork().getObjectEntityContainer()
				.getListEntity().toArray());;

		private JCheckBox checkGloballyExclusive;		
				
		JButton btnAdd = new JButton(iconController.getMoreIcon());

		public ObjectStatesPanel() {
			super(new GridLayout(3, 1));

			btnAdd.setToolTipText(resource.getString("addStateTip"));

			JToolBar barEdition = new JToolBar();
			barEdition.setFloatable(false);
			barEdition.add(btnAdd);
			barEdition.add(comboEntities);

			JToolBar toolGloballyExclusive = new JToolBar();
			toolGloballyExclusive.setFloatable(false);
			JLabel labelExclusive = new JLabel(resource
					.getString("isGloballyExclusive"));
			checkGloballyExclusive = new JCheckBox();
			checkGloballyExclusive.setSelected(false);

			toolGloballyExclusive.add(checkGloballyExclusive);
			toolGloballyExclusive.add(labelExclusive);
			
			add(barEdition);
			add(toolGloballyExclusive);
			add(new JLabel());
			
			btnAdd.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (comboEntities.getSelectedItem() != null) {
						if (!(residentNode.getPossibleValueLinkList().isEmpty())) {
							int answer = JOptionPane.showConfirmDialog(
									mebnController.getMebnEditionPane(),
									resource.getString("warningDeletStates"),
									resource.getString("confirmation"),
									JOptionPane.YES_NO_OPTION);
							if (answer == JOptionPane.YES_OPTION) {
								residentNode.removeAllPossibleValues();
								
								StateLink link = mebnController.addObjectEntityAsPossibleValue(residentNode, (ObjectEntity)comboEntities.getSelectedItem()); 
								mebnController.setGloballyExclusiveProperty(link, checkGloballyExclusive.isSelected()); 
								
								residentNode.setTypeOfStates(IResidentNode.OBJECT_ENTITY);
								listStatesPanel.update();
							}
						} else {
							StateLink link = mebnController.addObjectEntityAsPossibleValue(residentNode, (ObjectEntity)comboEntities.getSelectedItem()); 
							mebnController.setGloballyExclusiveProperty(link, checkGloballyExclusive.isSelected()); 
							
							residentNode
									.setTypeOfStates(IResidentNode.OBJECT_ENTITY);
							listStatesPanel.update();
						}

					}
				}

			});

		}
		
		public void selectState(StateLink entity) {
			StateLink selectEntity = entity;
			if (entity != null) {
				checkGloballyExclusive.setSelected(entity.isGloballyExclusive());
			} else {
				checkGloballyExclusive.setSelected(false);
			}
		}

	}

	private class BooleanStatesPanel extends JPanel {

		private JButton btnAdd;

		private JCheckBox checkGloballyExclusive;

		private JTextField txtName = new JTextField();
		
		private StateLink selectEntity; 

		public BooleanStatesPanel() {

			super(new GridLayout(3, 1));

			btnAdd = new JButton(iconController.getMoreIcon());
			btnAdd.setToolTipText(resource.getString("addStateTip"));

			JToolBar barAddStates = new JToolBar();
			barAddStates.setFloatable(false);
			barAddStates.setLayout(new GridLayout(1, 3));
			barAddStates.add(new JLabel());
			barAddStates.add(btnAdd);
			barAddStates.add(new JLabel());

			JToolBar barName = new JToolBar();
			barName.setFloatable(false);
			JLabel labelName = new JLabel(resource.getString("nameLabel") + " ");
			barName.add(labelName);
			
			txtName.setEditable(false); 
			barName.add(txtName);

			JToolBar toolGloballyExclusive = new JToolBar();
			toolGloballyExclusive.setFloatable(false);
			JLabel labelExclusive = new JLabel(resource
					.getString("isGloballyExclusive"));
			checkGloballyExclusive = new JCheckBox();
			checkGloballyExclusive.setSelected(false);
			
			checkGloballyExclusive.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					if(selectEntity != null){
						mebnController.setGloballyExclusiveProperty(selectEntity, checkGloballyExclusive.isSelected()); 
					}
				}
				
			}); 
			
			toolGloballyExclusive.add(checkGloballyExclusive);
			toolGloballyExclusive.add(labelExclusive);

			add(barAddStates);
			add(barName);
			add(toolGloballyExclusive);

			btnAdd.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					if (!(residentNode.getPossibleValueLinkList().isEmpty())) {
						int answer = JOptionPane.showConfirmDialog(
								mebnController.getMebnEditionPane(), resource
										.getString("warningDeletStates"),
								resource.getString("confirmation"),
								JOptionPane.YES_NO_OPTION);
						if (answer == JOptionPane.YES_OPTION) {
							mebnController
									.removeAllPossibleValues(residentNode);
							mebnController
									.addBooleanAsPossibleValue(residentNode);
							residentNode
									.setTypeOfStates(IResidentNode.BOOLEAN_RV_STATES);
							listStatesPanel.update();
						}
					} else {
						mebnController.addBooleanAsPossibleValue(residentNode);
						residentNode
								.setTypeOfStates(IResidentNode.BOOLEAN_RV_STATES);
						listStatesPanel.update();
					}

				}

			});

		}
		
		public void selectState(StateLink entity) {
			selectEntity = entity;
			if (entity != null) {
				txtName.setText(entity.getState().getName());
				checkGloballyExclusive
						.setSelected(entity.isGloballyExclusive());
			} else {
				txtName.setText("");
				checkGloballyExclusive.setSelected(false);
			}
		}

	}

	/**
	 * Painel para sele��o do tipo de argumento que o resident node ter�.
	 * Apresenta um bot�o para cada op��o poss�vel e um r�tulo indicando a op��o
	 * selecionada.
	 *
	 * @author Laecio Lima dos Santos.
	 */
	private class OptionsPanel extends JPanel {

		private JButton btnCategoryStates;

		private JButton btnObjectStates;

		private JButton btnBooleanStates;

		public OptionsPanel() {

			btnCategoryStates = new JButton(iconController
					.getCategoryStateIcon());
			btnCategoryStates.setToolTipText(resource
					.getString("categoryStatesTip"));
			btnObjectStates = new JButton(iconController.getEntityStateIcon());
			btnObjectStates.setToolTipText(resource
					.getString("objectStatesTip"));
			btnBooleanStates = new JButton(iconController.getBooleanStateIcon());
			btnBooleanStates.setToolTipText(resource
					.getString("booleanStatesTip"));

			btnCategoryStates.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showCategoryStatesPanel(null);
				}
			});

			btnBooleanStates.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showBooleanStatesPanel(null);
				}
			});

			btnObjectStates.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showObjectStatesPanel(null);
				}
			});

			this.setLayout(new GridLayout(1, 3));
			this.add(btnCategoryStates);
			this.add(btnObjectStates);
			this.add(btnBooleanStates);
		}

	}

	/**
	 * Renderizador para celula da lista de estados.
	 */
	private class StateCellRenderer extends DefaultListCellRenderer {

		private ImageIcon iconObjectState = iconController
				.getObjectEntityIcon();

		private ImageIcon iconCategoryState = iconController.getStateIcon();

		private ImageIcon iconBooleanState = iconController.getBooleanIcon();

		public StateCellRenderer() {
			super();
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			switch (residentNode.getTypeOfStates()) {
			case IResidentNode.OBJECT_ENTITY:
				setIcon(iconObjectState);
				break;
			case IResidentNode.CATEGORY_RV_STATES:
				setIcon(iconCategoryState);
				break;
			case IResidentNode.BOOLEAN_RV_STATES:
				setIcon(iconBooleanState);
				break;
			}

			if (isSelected) {
				super.setBorder(BorderFactory.createEtchedBorder());
			}

			return this;
		}

	}

}
