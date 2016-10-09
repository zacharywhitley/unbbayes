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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.standard.DialogTypeSelection;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.mebn.auxiliary.FocusListenerTextField;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityContainer;
import unbbayes.prs.mebn.entity.exception.ObjectEntityHasInstancesException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.prs.mebn.exception.DuplicatedNameException;
import unbbayes.prs.mebn.exception.ReservedWordException;

/**
 * Pane for edition of object entities: 
 *       - Create, 
 *       - Delete
 *       - Edit 
 *       - View
 *  Atributes editables: 
 *       - Name
 *       - isOrdenable property      
 *       
 *  @author Laécio Lima dos Santos (laecio@gmail.com)     
 */

public class EntityEditionPane extends JPanel{

	private MEBNController mebnController; 

//	private List<ObjectEntity> listEntity; 

	private JPanel jpInformation; 

	private JTextField txtName; 
//	private JTextField txtType; 
	private JCheckBox checkIsOrdereable; 
	private JButton jbNew; 
	private JButton jbDelete;

//	private JList jlEntities; 
//	private DefaultListModel listModel;
	
	private JTree jtEntities;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode root;
	private DefaultMutableTreeNode selectedTreeNode;
	private ObjectEntity selected; 

	private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
	private Matcher matcher;	

	private final IconController iconController = IconController.getInstance();

	/** Load resource file from this package */
	private static ResourceBundle resource = 
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.gui.mebn.resources.Resources.class.getName());

	/**
	 * 
	 * @param mebnController Controller for objects of this pane
	 */
	public EntityEditionPane(MEBNController mebnController){

		super(); 

		this.mebnController = mebnController; 

		this.setBorder(MebnToolkit.getBorderForTabPanel(
				resource.getString("EntityTitle"))); 

		setLayout(new BorderLayout());
		
		// JList code
		//buildJlEntities();
		//JScrollPane listScrollPane = new JScrollPane(jlEntities);
		
		// JTree code
		buildJtEntities();
		JScrollPane listScrollPane = new JScrollPane(getJTreeEntities());
		
		buildJpInformation();

		this.add(BorderLayout.SOUTH, jpInformation); 
		this.add(BorderLayout.CENTER, listScrollPane);

		selected = null; 
		update(); 
		addListListener(); 
		addButtonsListeners(); 

	}
	
	// JTree code
	private void buildJtEntities() {
				
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

		ImageIcon icon = iconController.getObjectEntityIcon();
		
		renderer.setLeafIcon(icon);
		renderer.setClosedIcon(icon);
		renderer.setOpenIcon(icon);
				
		treeModel = mebnController.getMultiEntityBayesianNetwork().getObjectEntityContainer().getEntityTreeModel();
		
		setJTreeEntities(new JTree(treeModel));
		getJTreeEntities().setCellRenderer(renderer);
		getJTreeEntities().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
	}

	// JList code
//	private void buildJlEntities() {
//		listModel = new DefaultListModel(); 
//
//		jlEntities = new JList(listModel); 
//		jlEntities.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		jlEntities.setLayoutOrientation(JList.VERTICAL);
//		jlEntities.setVisibleRowCount(-1);
//		jlEntities.setCellRenderer(new ListCellRenderer(iconController.getObjectEntityIcon())); 
//
//	}

	private void buildJpInformation() {
		jpInformation = new JPanel(new GridLayout(3, 0)); 

		JLabel label; 
		JToolBar toolBar; 

		toolBar = new JToolBar(); 
		toolBar.setLayout(new GridLayout(0, 2));
		
		jbNew = new JButton(iconController.getMoreIcon()); 
		jbNew.setToolTipText(resource.getString("newEntityToolTip")); 
		toolBar.add(jbNew);
		
		jbDelete = new JButton(iconController.getLessIcon()); 
		jbDelete.setToolTipText(resource.getString("delEntityToolTip")); 
		toolBar.add(jbDelete);
		
		toolBar.setFloatable(false);	    
		jpInformation.add(toolBar); 

		toolBar = new JToolBar(); 
		toolBar.setLayout(new BorderLayout()); 
		label = new JLabel(resource.getString("nameLabel")); 
		label.setPreferredSize(new Dimension(50, 5));
		toolBar.add(label, BorderLayout.LINE_START); 
		txtName = new JTextField(10);
		txtName.setEditable(false); 
		toolBar.add(txtName, BorderLayout.CENTER); 
		toolBar.setFloatable(false);
		jpInformation.add(toolBar);

//		toolBar = new JToolBar(); 
//		toolBar.setLayout(new BorderLayout()); 
//		label = new JLabel(resource.getString("typeLabel")); 
//		label.setPreferredSize(new Dimension(50, 5));
//		toolBar.add(label, BorderLayout.LINE_START); 
//		txtType = new JTextField(10);
//		txtType.setEditable(false); 
//		toolBar.add(txtType, BorderLayout.CENTER); 
//		toolBar.setFloatable(false);
//		jpInformation.add(toolBar); 

		toolBar = new JToolBar(); 
		toolBar.setLayout(new BorderLayout()); 
		checkIsOrdereable = new JCheckBox(); 
		checkIsOrdereable.setEnabled(false); 
		toolBar.add(checkIsOrdereable, BorderLayout.LINE_START); 
		label = new JLabel(resource.getString("ordereableLabel")); 
		toolBar.add(label, BorderLayout.CENTER); 
		toolBar.setFloatable(false);
		jpInformation.add(toolBar); 
	}

	/**
	 * Reloads and updates the list of entities.
	 */
	public void reloadEntityList(){
		this.update();
	}
	
	/**
	 *  update the list of entities 
	 **/
	private void update(){

		
		//JList code	
//		ObjectEntity antSelected = selected; 
//	
//		listModel.clear(); 
//
//		listEntity = mebnController.getMultiEntityBayesianNetwork().getObjectEntityContainer().getListEntity(); 
//
//		listModel = new DefaultListModel(); 
//		for(Entity entity: listEntity){
//			listModel.addElement(entity); 
//		}
//
//		jlEntities.setModel(listModel);
//		
//		selected = antSelected;
		
		//JTree code
		treeModel = mebnController.getMultiEntityBayesianNetwork().getObjectEntityContainer().getEntityTreeModel();
		jtEntities.setModel(treeModel);
		
		expandTreeNodes();

	}

	private void addListListener(){
		
		// JList code 
//		jlEntities.addListSelectionListener(
//				new ListSelectionListener(){
//					public void valueChanged(ListSelectionEvent e) {
//									
//						selected = (ObjectEntity)jlEntities.getSelectedValue(); 
//						if(selected != null){
//							txtName.setText(selected.getName()); 
//							txtName.setEditable(true); 
//							checkIsOrdereable.setEnabled(true); 
//							checkIsOrdereable.setSelected(selected.isOrdereable()); 
////							txtType.setText(selected.getType().getName());
//						}
//										
//					}
//				}  	
//		);
		
		// JTree code
		getJTreeEntities().addTreeSelectionListener(
				new TreeSelectionListener(){
					public void valueChanged(TreeSelectionEvent e) {
						
						DefaultMutableTreeNode selectedTreeNode = (DefaultMutableTreeNode) getJTreeEntities().getLastSelectedPathComponent();
						if(selectedTreeNode == null || selectedTreeNode.isRoot() ) {
							return;
						}
						
						selected = (ObjectEntity) selectedTreeNode.getUserObject();
						if(selected != null){
							txtName.setText(selected.getName()); 
							txtName.setEditable(true); 
							checkIsOrdereable.setEnabled(true); 
							checkIsOrdereable.setSelected(selected.isOrdereable()); 
//							txtType.setText(selected.getType().getName());
						}
						
					}
				}  	
		);
	}

	private void addButtonsListeners(){

		txtName.addFocusListener(new FocusListenerTextField()); 
		txtName.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {

				if ((e.getKeyCode() == KeyEvent.VK_ENTER) 
						&& (txtName.getText().length()>0)) {
					try {
						String nameValue = txtName.getText(0,txtName.getText().length());
						matcher = wordPattern.matcher(nameValue);
						if (matcher.matches()) {
							try{
								try {
									mebnController.renameObjectEntity(selected, nameValue);
									
									// JList code 
									//jlEntities.setSelectedValue(selected, true);
									selectedTreeNode = (DefaultMutableTreeNode) getJTreeEntities().getLastSelectedPathComponent();
									selected = (ObjectEntity) selectedTreeNode.getUserObject();
									
									txtName.setText(selected.getName());
									
									txtName.setEditable(false); 
									checkIsOrdereable.setEnabled(false); 
//									txtType.setText(selected.getType().getName());
									
									update();
									
								} 
//									catch (DuplicatedNameException e1) {
//									JOptionPane.showMessageDialog(mebnController.getScreen(),
//											resource.getString("nameDuplicated"),
//											resource.getString("nameError"),
//											JOptionPane.ERROR_MESSAGE);
//								} 
								catch (ReservedWordException e2) {
	  	  							JOptionPane.showMessageDialog(mebnController.getScreen(),
	  	  									resource.getString("nameReserved"),
	  	  									resource.getString("nameError"),
	  	  									JOptionPane.ERROR_MESSAGE);
								}

							}
							catch (TypeException typeException){
								JOptionPane.showMessageDialog(null, 
										resource.getString("nameDuplicated"), 
										resource.getString("nameError"), 
										JOptionPane.ERROR_MESSAGE);
								txtName.selectAll();
							}
						}  else {
							txtName.setBackground(MebnToolkit.getColorTextFieldError()); 
							txtName.setForeground(Color.WHITE); 
							JOptionPane.showMessageDialog(null, 
									resource.getString("nameException"), 
									resource.getString("nameError"), 
									JOptionPane.ERROR_MESSAGE);
							txtName.selectAll();
						}
					}
					catch (javax.swing.text.BadLocationException ble) {
						ble.printStackTrace();
					}
				}
			}

			public void keyReleased(KeyEvent e){
				try{
					String name = txtName.getText(0,txtName.getText().length());
					matcher = wordPattern.matcher(name);
					if (!matcher.matches()) {
						txtName.setBackground(MebnToolkit.getColorTextFieldError()); 
						txtName.setForeground(Color.WHITE); 
					}
					else{
						txtName.setBackground(MebnToolkit.getColorTextFieldSelected());
						txtName.setForeground(Color.BLACK); 
					}
				}
				catch(Exception efd){

				}

			}
		});

		checkIsOrdereable.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				JCheckBox checkBox = (JCheckBox)e.getSource(); 
				try {
					mebnController.setIsOrdereableObjectEntityProperty(selected, checkBox.isSelected());
				} catch (ObjectEntityHasInstancesException e1) {
					JOptionPane.showMessageDialog(null, 
							resource.getString("objectEntityHasInstance"), 
							resource.getString("operationFail"), 
							JOptionPane.ERROR_MESSAGE);
				}
			}

		}); 

		jbNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try{

					// JList code
//					selected = mebnController.createObjectEntity();

//					update();  

//					jlEntities.setSelectedValue(selected, true);
					
//					txtType.setText(selected.getType().getName()); 
//					txtName.setEditable(true); 
//					checkIsOrdereable.setEnabled(true); 
//					txtName.setText(selected.getName());
//					txtName.selectAll(); 
//					txtName.requestFocus();
					
					// JTree Code
					selectedTreeNode = (DefaultMutableTreeNode) getJTreeEntities().getLastSelectedPathComponent();
					if(selectedTreeNode == null) {
						
						// Create resources to tell that no TreeNode is selected.
						// Check if strings are good enough.
						
//						JOptionPane.showMessageDialog(null,
//								resource.getString("selectEntityFirst"),
//								resource.getString("warning"),
//								JOptionPane.ERROR_MESSAGE);
//						
//						return;
						// select root by default
						selectedTreeNode = (DefaultMutableTreeNode) getJTreeEntities().getModel().getRoot();
//						getJTreeEntities().setSelectionRow(0);
					} 
					
					selected = mebnController.createObjectEntity((ObjectEntity) selectedTreeNode.getUserObject());
								
					txtName.setEditable(true); 
					checkIsOrdereable.setEnabled(true); 
					txtName.setText(selected.getName());
					txtName.selectAll(); 
					txtName.requestFocus();
					
					update();

//					selectedTreeNode = (DefaultMutableTreeNode) treeModel.getChild(
//							(ObjectEntity) selectedTreeNode.getUserObject(), 
//							selectedTreeNode.getChildCount() + 1  );

					getJTreeEntities().setSelectionPath(new TreePath(selectedTreeNode.getPath()));
					
				}
				catch(TypeException e){
					JOptionPane.showMessageDialog(null, 
							resource.getString("nameDuplicated"), 
							resource.getString("nameError"), 
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		jbDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// JList Code
//				if(selected != null){
//					try{
//						mebnController.removeObjectEntity(selected);
//					}
//					catch(Exception e){
//						e.printStackTrace(); 
//					}
//					update(); 
//					txtName.setText(" "); 
////					txtType.setText(" "); 
//					txtName.setEditable(false); 
//					checkIsOrdereable.setEnabled(false);
//				}
				
				//JTreeCode				
				selectedTreeNode = (DefaultMutableTreeNode) getJTreeEntities().getLastSelectedPathComponent();
				ObjectEntity selectedObjectEntity = (ObjectEntity) selectedTreeNode.getUserObject();
				ObjectEntityContainer container = mebnController.getMultiEntityBayesianNetwork().getObjectEntityContainer();
				
				if(selectedTreeNode.isRoot()) {
					
					JOptionPane.showMessageDialog(null, resource.getString("removeRootWarning"),
							 resource.getString("warning"), JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(container.getDescendantsAndSelf(selectedObjectEntity).size() > 1) {
					
	                int dialogButton = JOptionPane.showConfirmDialog(null, resource.getString("removingEntityWarning"),
	                		resource.getString("warning"),JOptionPane.OK_CANCEL_OPTION);

	                if(dialogButton != JOptionPane.OK_OPTION){ 
	                	return;
	                }
					
				}
										
				if(selectedTreeNode != null){
					try{
						mebnController.removeObjectEntity(selectedObjectEntity);
					}
					// Add pop-up
					catch(Exception e){
						e.printStackTrace(); 
					}
					
					update(); 
					txtName.setText(" "); 
//					txtType.setText(" "); 
					txtName.setEditable(false); 
					checkIsOrdereable.setEnabled(false);
				}	
			}
		});
	}
	
	/**
	 *  Expand all TreePaths in jtEntities.
	 */
	
	private void expandTreeNodes() {
		for (int i = 0; i < getJTreeEntities().getRowCount(); i++) {
			getJTreeEntities().expandRow(i);
		}
	}
	
	/**
	 * @return This is a {@link JTree} which organizes the hierarchy of {@link ObjectEntity}
	 * @see ObjectEntityContainer
	 */
	public JTree getJTreeEntities() {
		return jtEntities;
	}

	/**
	 * @param jtEntities : This is a {@link JTree} which organizes the hierarchy of {@link ObjectEntity}
	 * @see ObjectEntityContainer
	 */
	public void setJTreeEntities(JTree jtEntities) {
		this.jtEntities = jtEntities;
	}

}



