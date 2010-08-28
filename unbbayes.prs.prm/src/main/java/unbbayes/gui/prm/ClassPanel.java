/**
 * 
 */
package unbbayes.gui.prm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicArrowButton;

import unbbayes.controller.IconController;
import unbbayes.prs.prm.DependencyChain;
import unbbayes.prs.prm.IAttributeDescriptor;
import unbbayes.prs.prm.IDependencyChain;
import unbbayes.prs.prm.IForeignKey;
import unbbayes.prs.prm.IPRMClass;
import unbbayes.prs.prm.IPRMDependency;
import unbbayes.prs.prm.cpt.AggregateFunctionMode;
import unbbayes.prs.prm.cpt.IPRMCPT;

/**
 * This is a panel to edit PRM class' details
 * @author Shou Matsumoto
 * TODO refactor inner classes (JPanels) as new .java files, and use Builder Pattern and dependency injection here in order to allow
 * this panel to be attached to subclasses of such currently inner classes (JPanels).
 */
public class ClassPanel extends JPanel {

	private SchemaPanel schemaPanel;
	
	private IPRMClass prmClass;
	private JPanel namePanel;
	private JTextField nameTextField;
	private JList attributeList;
	private JPanel attributePanel;
	private JScrollPane attributeScrollPane;
	private JToolBar attributeEditionToolbar;
	
	private IconController iconController;
	private JButton addAttributeButton;
	private JButton removeAttributeButton;
	private JPanel commitCancelPanel;
	private JButton commitButton;
	private JButton cancelButton;

	private JButton editAttributeButton;

	private JButton editKeysButton;
	
	private List<IAttributeDescriptor> attributeDescriptorsCopy;
	
	
	/**
	 * At least one constructor is visible for subclasses
	 * to allow inheritance
	 */
	protected ClassPanel () {
		super();
		this.iconController = IconController.getInstance();
		this.attributeDescriptorsCopy = new ArrayList<IAttributeDescriptor>();
	}
	
	/**
	 * Default construction method
	 * @param prmClass
	 * @return
	 */
	public static ClassPanel newInstance(SchemaPanel schemaPanel, IPRMClass prmClass) {
		ClassPanel ret = new ClassPanel();
		ret.schemaPanel = schemaPanel;
		ret.prmClass = prmClass;
		// use copies of attributes, in order to allow rollback
		ret.setAttributeDescriptorsCopy(new ArrayList<IAttributeDescriptor>(ret.prmClass.getAttributeDescriptors()));
		ret.initComponents();
		ret.initListeners();
		ret.setVisible(true);
		return ret;
	}
	
	

	/**
	 * Resets and builds up the components of this panel
	 */
	protected void initComponents() {
		// reset components
		this.removeAll();
		if (this.getPrmClass() == null) {
			// nothing to render
			return;
		}
		
		this.setLayout(new BorderLayout(10,10));
		this.setName(this.getPrmClass().getName());
		
		// building panel to edit class's name
		this.setNamePanel(new JPanel(new BorderLayout()));
		this.add(this.getNamePanel(), BorderLayout.NORTH);
		
		// initialize the label and text field for name
		this.getNamePanel().add(new JLabel("Entity Name: "), BorderLayout.WEST);
		this.setNameTextField(new JTextField(this.getPrmClass().getName(), 50));
		this.getNamePanel().add(this.getNameTextField(), BorderLayout.CENTER);
		
		// attribute edition panel and scroll pane
		this.setAttributePanel(new JPanel(new BorderLayout(5,5)));
		this.setAttributeScrollPane(new JScrollPane(this.getAttributePanel()));
		this.add(this.getAttributeScrollPane(), BorderLayout.CENTER);
		
		// list of attributes
		attributeList = new JList(new AbstractListModel() {
			public Object getElementAt(int index) {
				return getAttributeDescriptorsCopy().get(index);
			}
			public int getSize() {
				return getAttributeDescriptorsCopy().size();
			}
			
		});
		this.getAttributePanel().add(attributeList, BorderLayout.CENTER);
		
		// Toolbar to edit list of attributes
		this.setAttributeEditionToolbar(new JToolBar("Attributes"));
		this.getAttributePanel().add(this.getAttributeEditionToolbar(), BorderLayout.NORTH);
		
		// add and remove attributes buttons
		this.setAddAttributeButton(new JButton(this.getIconController().getNewIcon()));
		this.setRemoveAttributeButton(new JButton(this.getIconController().getDeleteClassIcon()));
		this.getAddAttributeButton().setToolTipText("Add attribute");
		this.getRemoveAttributeButton().setToolTipText("Remove attribute");
		this.getAttributeEditionToolbar().add(this.getAddAttributeButton());
		this.getAttributeEditionToolbar().add(this.getRemoveAttributeButton());
		
		// button to edit attribute
		this.setEditAttributeButton(new JButton(this.getIconController().getEdit()));
		this.getEditAttributeButton().setToolTipText("Edit attribute");
		this.getAttributeEditionToolbar().add(this.getEditAttributeButton());
		
		// button for key edition
		this.setEditKeysButton(new JButton(this.getIconController().getEdgeIcon()));
		this.getEditKeysButton().setToolTipText("Edit keys and references");
		this.getAttributeEditionToolbar().add(this.getEditKeysButton());
		
		// commit/cancel panel
		this.setCommitCancelPanel(new JPanel(new FlowLayout(FlowLayout.CENTER,5,5)));
		this.add(this.getCommitCancelPanel(), BorderLayout.SOUTH);
		
		// commit/cancel buttons
		this.setCommitButton(new JButton("Commit"));
		this.setCancelButton(new JButton("Cancel"));
		this.getCommitButton().setToolTipText("Commit changes");
		this.getCancelButton().setToolTipText("Cancel changes");
		this.getCommitCancelPanel().add(this.getCommitButton());
		this.getCommitCancelPanel().add(this.getCancelButton());
		
//		this.add(new JLabel(this.getPrmClass().getName()));
		// TODO
	}
	
	/**
	 * Initialize listeners
	 */
	protected void initListeners() {
		
		// listeners for commit button
		this.getCommitButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getPrmClass().setName(getNameTextField().getText());
				getSchemaPanel().resetClassTree();
				getSchemaPanel().repaint();
			}
		});
		
		// listeners for cancel button
		this.getCancelButton().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				getAttributeList().clearSelection();
				getNameTextField().setText(getPrmClass().getName());
				getNameTextField().repaint();
			}
		});
		
		// listeners for commit and cancel buttons to commit or undo attribute changes
		// listeners for commit button
		this.getCommitButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// update class
				getPrmClass().setAttributeDescriptors(new ArrayList<IAttributeDescriptor>(getAttributeDescriptorsCopy()));
				getAttributeList().repaint();
			}
		});
		
		// listeners for cancel button
		this.getCancelButton().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// revert the copy
				setAttributeDescriptorsCopy(new ArrayList<IAttributeDescriptor>(getPrmClass().getAttributeDescriptors()));
				getAttributeList().updateUI();
				getAttributeList().repaint();
			}
		});
		
		
		// listener to add attribute
		this.getAddAttributeButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IAttributeDescriptor newAttribute = getPrmClass().getAttributeDescriptorBuilder().buildPRMAttributeDescriptor(getPrmClass());
				newAttribute.appendState("NewState0");	// default state
				getAttributeDescriptorsCopy().add(newAttribute);
				// since buildPRMAttributeDescriptor automatically adds an attribute to prmClass and we want commit/cancel, then undo the automatic add operation
				getPrmClass().getAttributeDescriptors().remove(newAttribute);	
				getAttributeList().updateUI();
				getAttributeList().repaint();
			}
		});
		
		// listener to remove attribute
		this.getRemoveAttributeButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getAttributeList().getSelectedIndex() >= 0) {
					getAttributeDescriptorsCopy().remove(getAttributeList().getSelectedIndex());
					getAttributeList().updateUI();
					getAttributeList().repaint();
				}
			}
		});
		
		// listener to edit attribute
		this.getEditAttributeButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!getAttributeList().isSelectionEmpty()) {
					// open a popup to edit attribute descriptor
					JDialog dialog = new AttributePanel(ClassPanel.this, (IAttributeDescriptor)getAttributeList().getSelectedValue()).buildDialog();
					dialog.setVisible(true);
				}
			}
		});
		
		
		// listener to edit keys
		this.getEditKeysButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getAttributeList().getModel().getSize() > 0) {
					// open a popup to edit attribute descriptor
					JDialog dialog = new ForeignKeyPanel(ClassPanel.this, getPrmClass()).buildDialog();
					dialog.setVisible(true);
				}
			}
		});
		
	}

	/**
	 * @return the prmClass
	 */
	public IPRMClass getPrmClass() {
		return prmClass;
	}

	/**
	 * @param prmClass the prmClass to set
	 */
	public void setPrmClass(IPRMClass prmClass) {
		this.prmClass = prmClass;
	}

	/**
	 * @return the namePanel
	 */
	public JPanel getNamePanel() {
		return namePanel;
	}

	/**
	 * @param namePanel the namePanel to set
	 */
	public void setNamePanel(JPanel namePanel) {
		this.namePanel = namePanel;
	}

	/**
	 * @return the nameTextField
	 */
	public JTextField getNameTextField() {
		return nameTextField;
	}

	/**
	 * @param nameTextField the nameTextField to set
	 */
	public void setNameTextField(JTextField nameTextField) {
		this.nameTextField = nameTextField;
	}

	/**
	 * @return the attributeList
	 */
	public JList getAttributeList() {
		return attributeList;
	}

	/**
	 * @param attributeList the attributeList to set
	 */
	public void setAttributeList(JList attributeList) {
		this.attributeList = attributeList;
	}

	/**
	 * @return the attributePanel
	 */
	public JPanel getAttributePanel() {
		return attributePanel;
	}

	/**
	 * @param attributePanel the attributePanel to set
	 */
	public void setAttributePanel(JPanel attributePanel) {
		this.attributePanel = attributePanel;
	}

	/**
	 * @return the attributeScrollPane
	 */
	public JScrollPane getAttributeScrollPane() {
		return attributeScrollPane;
	}

	/**
	 * @param attributeScrollPane the attributeScrollPane to set
	 */
	public void setAttributeScrollPane(JScrollPane attributeScrollPane) {
		this.attributeScrollPane = attributeScrollPane;
	}

	/**
	 * @return the attributeEditionToolbar
	 */
	public JToolBar getAttributeEditionToolbar() {
		return attributeEditionToolbar;
	}

	/**
	 * @param attributeEditionToolbar the attributeEditionToolbar to set
	 */
	public void setAttributeEditionToolbar(JToolBar attributeEditionToolbar) {
		this.attributeEditionToolbar = attributeEditionToolbar;
	}

	/**
	 * @return the iconController
	 */
	public IconController getIconController() {
		return iconController;
	}

	/**
	 * @param iconController the iconController to set
	 */
	public void setIconController(IconController iconController) {
		this.iconController = iconController;
	}

	/**
	 * @return the addAttributeButton
	 */
	public JButton getAddAttributeButton() {
		return addAttributeButton;
	}

	/**
	 * @param addAttributeButton the addAttributeButton to set
	 */
	public void setAddAttributeButton(JButton addAttributeButton) {
		this.addAttributeButton = addAttributeButton;
	}

	/**
	 * @return the removeAttributeButton
	 */
	public JButton getRemoveAttributeButton() {
		return removeAttributeButton;
	}

	/**
	 * @param removeAttributeButton the removeAttributeButton to set
	 */
	public void setRemoveAttributeButton(JButton removeAttributeButton) {
		this.removeAttributeButton = removeAttributeButton;
	}

	/**
	 * @return the commitCancelPanel
	 */
	public JPanel getCommitCancelPanel() {
		return commitCancelPanel;
	}

	/**
	 * @param commitCancelPanel the commitCancelPanel to set
	 */
	public void setCommitCancelPanel(JPanel commitCancelPanel) {
		this.commitCancelPanel = commitCancelPanel;
	}

	/**
	 * @return the commitButton
	 */
	public JButton getCommitButton() {
		return commitButton;
	}

	/**
	 * @param commitButton the commitButton to set
	 */
	public void setCommitButton(JButton commitButton) {
		this.commitButton = commitButton;
	}

	/**
	 * @return the cancelButton
	 */
	public JButton getCancelButton() {
		return cancelButton;
	}

	/**
	 * @param cancelButton the cancelButton to set
	 */
	public void setCancelButton(JButton cancelButton) {
		this.cancelButton = cancelButton;
	}

	/**
	 * @return the schemaPanel
	 */
	public SchemaPanel getSchemaPanel() {
		return schemaPanel;
	}

	/**
	 * @param schemaPanel the schemaPanel to set
	 */
	public void setSchemaPanel(SchemaPanel schemaPanel) {
		this.schemaPanel = schemaPanel;
	}

	/**
	 * @return the editAttributeButton
	 */
	public JButton getEditAttributeButton() {
		return editAttributeButton;
	}

	/**
	 * @param editAttributeButton the editAttributeButton to set
	 */
	public void setEditAttributeButton(JButton editAttributeButton) {
		this.editAttributeButton = editAttributeButton;
	}

	/**
	 * @return the editKeysButton
	 */
	public JButton getEditKeysButton() {
		return editKeysButton;
	}

	/**
	 * @param editKeysButton the editKeysButton to set
	 */
	public void setEditKeysButton(JButton editKeysButton) {
		this.editKeysButton = editKeysButton;
	}

	/**
	 * @return the attributeDescriptorsCopy
	 */
	public List<IAttributeDescriptor> getAttributeDescriptorsCopy() {
		return attributeDescriptorsCopy;
	}

	/**
	 * @param attributeDescriptorsCopy the attributeDescriptorsCopy to set
	 */
	public void setAttributeDescriptorsCopy(
			List<IAttributeDescriptor> attributeDescriptorsCopy) {
		this.attributeDescriptorsCopy = attributeDescriptorsCopy;
	}
	
	
	/**
	 * Builds a new JTable for CPT
	 * @author Shou Matsumoto
	 *
	 */
	public class CPTTableBuilder {
		public JTable buildTable(IPRMCPT prmCPT) {
			return PRMTableGUI.newInstance(prmCPT);
		}
	}
	
	/**
	 * Panel to edit attribute descriptor
	 * @author Shou Matsumoto
	 *
	 */
	public class AttributePanel extends JPanel {

		private ClassPanel classPanel; 
		private IAttributeDescriptor attribute;
		private JTextField attributeNameTextField;
		private JTextField typeTextField;
		private BasicArrowButton typeDownArrowButton;
		private JCheckBox checkBoxPK;
		private JCheckBox checkBoxMandatory;
		private JScrollPane cptEditionScrollPane;
		private JToolBar cptEditionToolBar;
		
		private CPTTableBuilder cptTableBuilder;
		private JTable cptTable;
		private JButton addStateCPTButton;
		private JButton removeStateCPTButton;
		private JButton addParentCPTButton;
		private JButton removeParentCPTButton;
		private JButton commitCPTButton;
		private JButton cancelCPTButton;
		private JDialog upperDialog;
		private JToolBar attributeNameToolbar;
		private JToolBar typeToolBar;
		private JToolBar checkBoxToolBar;
		private JScrollPane tableScrollPane;
		private JButton editParentCPTButton;
		
		/**
		 * The defautl constructor is visible to subclass
		 * to simplify inheritance
		 * @see JPanel#JPanel()
		 */
		protected AttributePanel() {
			super();
			this.cptTableBuilder = new CPTTableBuilder();
		}
		
		/**
		 * At least one constructor must be visible to subclass
		 * to allow inheritance
		 * @see JPanel#JPanel()
		 */
		public AttributePanel(ClassPanel classPanel, IAttributeDescriptor attribute) {
			this();
			this.classPanel = classPanel;
			this.attribute = attribute;
			this.initComponents();
			this.initListeners();
		}
		
		/**
		 * Resets this component.
		 * Clears all components, calls {@link #initComponents()} and
		 * {@link #initListeners()}.
		 */
		public void resetComponents() {
			this.removeAll();
			this.initComponents();
			this.initListeners();
		}

		/**
		 * Initialize components
		 */
		protected void initComponents() {
			this.setLayout(new BorderLayout(10,10));
			
			// a panel for name, type, PK and mandatory
			JPanel attributeNameTypePKMandatoryPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			this.add(new JScrollPane(attributeNameTypePKMandatoryPanel) , BorderLayout.NORTH);
			
			// Toolbar for name
			this.setAttributeNameToolbar(new JToolBar("Name and properties"));
			attributeNameTypePKMandatoryPanel.add(this.getAttributeNameToolbar());
			
			// label
			this.getAttributeNameToolbar().add(new JLabel("Name: "));
			
			// text field for name
			this.setAttributeNameTextField(new JTextField(this.getAttributeDescriptor().getName(), 20));
			this.getAttributeNameToolbar().add(this.getAttributeNameTextField());
			
			// panel for type list (TODO this is a stub)
//			this.setTypeToolBar(new JToolBar("Type"));
			this.setTypeToolBar(this.getAttributeNameToolbar());
			JLabel typeLabel = new JLabel("Type: ");
			typeLabel.setEnabled(false);
			this.getTypeToolBar().add(typeLabel);
			attributeNameTypePKMandatoryPanel.add(this.getTypeToolBar());
			
			// type dropdown list (TODO this is a stub)
			this.setTypeTextField(new JTextField("String", 8));
			this.getTypeTextField().setEditable(false);
			this.getTypeTextField().setEnabled(false);
			this.getTypeToolBar().add(this.getTypeTextField());
			
			// the down arrow button for type popup menu (TODO this is a stub)
			this.setTypeDownArrowButton(new BasicArrowButton(BasicArrowButton.SOUTH));
			this.getTypeDownArrowButton().setEnabled(false);
			this.getTypeDownArrowButton().setToolTipText("Only \"String\" is allowed in ALPHA version.");
			this.getTypeToolBar().add(this.getTypeDownArrowButton());
			

			// checkbox panel for PK and mandatory
//			this.setCheckBoxToolBar(new JToolBar("Properties"));
			this.setCheckBoxToolBar(this.getAttributeNameToolbar());
//			this.getCheckBoxToolBar().add(new JLabel("Properties: "));
			attributeNameTypePKMandatoryPanel.add(this.getCheckBoxToolBar());
			
			// PK checkbox
			this.setCheckBoxPK(new JCheckBox("PK", this.getAttributeDescriptor().isPrimaryKey()));
			this.getCheckBoxPK().setToolTipText("Mark this attribute as a primary key");
			this.getCheckBoxToolBar().add(this.getCheckBoxPK());
			
			// mandatory checkbox
			this.setCheckBoxMandatory(new JCheckBox("Mandatory", this.getAttributeDescriptor().isMandatory()));
			this.getCheckBoxMandatory().setToolTipText("Mark this attribute as non-null value");
			this.getCheckBoxToolBar().add(this.getCheckBoxMandatory());
			
			// mark as disabled if PK is on
			if (this.getCheckBoxPK().isSelected()) {
				this.getCheckBoxMandatory().setEnabled(false);
			}
			
			// Panel to edit probabilistic dependency
			JPanel cptEditionPanel = new JPanel(new BorderLayout(10,10));
			cptEditionPanel.setBorder(new TitledBorder("Probabilistic dependency"));
			
			// scroll pane for the panel to edit probabilistic dependency
			this.setCptEditionScrollPane(new JScrollPane(cptEditionPanel));
			this.add(this.getCptEditionScrollPane(), BorderLayout.CENTER);
			
			// mark probabilistic dependency as invisible if attribute is mandatory
			if (this.getCheckBoxMandatory().isSelected()) {
				getCptEditionScrollPane().setEnabled(false);
				getCptEditionScrollPane().setVisible(false);
			} else {
				getCptEditionScrollPane().setEnabled(true);
				getCptEditionScrollPane().setVisible(true);
			}
			
			// toolbars to edit CPT
			this.setCptEditionToolBar(new JToolBar("Edit CPT"));
			cptEditionPanel.add(this.getCptEditionToolBar(), BorderLayout.NORTH);
			
			// state buttons for the cpt edition toolbar
//			JPanel addRemoveStateButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
//			addRemoveStateButtonPanel.add(new JLabel("Possible Values "));
//			this.getCptEditionToolBar().add(addRemoveStateButtonPanel);
			this.getCptEditionToolBar().add(new JLabel("Values: ", getIconController().getGrayBorderBoxIcon(), JLabel.LEADING));
			
			// button to add state
			this.setAddStateCPTButton(new JButton(getIconController().getMoreIcon()));
			this.getAddStateCPTButton().setToolTipText("Add a possible value");
//			addRemoveStateButtonPanel.add(this.getAddStateCPTButton());
			this.getCptEditionToolBar().add(this.getAddStateCPTButton());
			
			// button to remove state
			this.setRemoveStateCPTButton(new JButton(getIconController().getLessIcon()));
			this.getRemoveStateCPTButton().setToolTipText("Remove a possible value");
//			addRemoveStateButtonPanel.add(this.getRemoveStateCPTButton());
			this.getCptEditionToolBar().add(this.getRemoveStateCPTButton());
			
			// separator between states and parents
			this.getCptEditionToolBar().addSeparator();
			
			// parents buttons for the cpt edition toolbar
//			JPanel addRemoveParentButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
//			addRemoveParentButtonPanel.add(new JLabel("Parents "));
//			this.getCptEditionToolBar().add(addRemoveParentButtonPanel);
			this.getCptEditionToolBar().add(new JLabel("Parents: ", getIconController().getYellowBallIcon(), JLabel.LEADING));
			
			
			// button to add parent
			this.setAddParentCPTButton(new JButton(getIconController().getMoreIcon()));
			this.getAddParentCPTButton().setToolTipText("Add a probabilistic dependency");
//			addRemoveParentButtonPanel.add(this.getAddParentCPTButton());
			this.getCptEditionToolBar().add(this.getAddParentCPTButton());
			
			// button to remove parent
			this.setRemoveParentCPTButton(new JButton(getIconController().getLessIcon()));
			this.getRemoveParentCPTButton().setToolTipText("Remove a probabilistic dependency - disabled in ALPHA.");
//			addRemoveParentButtonPanel.add(this.getRemoveParentCPTButton());
			this.getCptEditionToolBar().add(this.getRemoveParentCPTButton());
			// TODO enable getRemoveParentCPTButton
			this.getRemoveParentCPTButton().setEnabled(false);

			// buton to edit parent's aggregation
			this.setEditParentCPTButton(new JButton(getIconController().getEditIcon()));
			this.getEditParentCPTButton().setToolTipText("Edit probabilistic dependency's aggregation function - disabled in ALPHA");
			this.getCptEditionToolBar().add(this.getEditParentCPTButton());
			// TODO enable getEditParentCPTButton
			this.getEditParentCPTButton().setEnabled(false);
			
			// the cpt itself
			this.setCptTable(this.getCptTableBuilder().buildTable(this.getAttributeDescriptor().getPRMDependency().getCPT()));
			this.setTableScrollPane(new JScrollPane(this.getCptTable()));
			this.getTableScrollPane().setPreferredSize(this.getCptTable().getSize());
			cptEditionPanel.add(this.getTableScrollPane(), BorderLayout.CENTER);
			
			// panel to commit changes
			JPanel commitChangesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			this.add(commitChangesPanel, BorderLayout.SOUTH);
			
			// button to commit changes
			this.setCommitCPTButton(new JButton("Commit"));
			this.getCommitCPTButton().setToolTipText("Commit changes to attribute and the conditional probability table");
			commitChangesPanel.add(this.getCommitCPTButton());
			
			// button to cancel changes
			this.setCancelCPTButton(new JButton("Cancel"));
			this.getCancelCPTButton().setToolTipText("Cancel changes and close upperDialog");
			commitChangesPanel.add(this.getCancelCPTButton());
			
//			typeDropDownList = new JList
		}

		/**
		 * Initialize listeners of the components created by {@link #initComponents()}
		 */
		protected void initListeners() {
			
			// if primary key is selected, select mandatory as well
			this.getCheckBoxPK().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (getCheckBoxPK().isSelected()) {
						getCheckBoxMandatory().setSelected(false); // assure it is false before flip
						getCheckBoxMandatory().doClick();	// flip to true
						getCheckBoxMandatory().setEnabled(false);
						getCheckBoxMandatory().updateUI();
						getCheckBoxMandatory().repaint();
					} else {
						getCheckBoxMandatory().setEnabled(true);
						getCheckBoxMandatory().repaint();
					}
				}
			});
			
			// if it is mandatory, do not treat as random variable
			this.getCheckBoxMandatory().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (getCheckBoxMandatory().isSelected()) {
						getCptEditionScrollPane().setEnabled(false);
						getCptEditionScrollPane().setVisible(false);
					} else {
						getCptEditionScrollPane().setEnabled(true);
						getCptEditionScrollPane().setVisible(true);
						getCptEditionScrollPane().repaint();
					}
					AttributePanel.this.updateUI();
					AttributePanel.this.repaint();
				}
			});
			
			// commit changes on attribute name
			this.getCommitCPTButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (getCheckBoxPK().isSelected()) {
						// 2 attributes cannot be PK at same time in ALPHA version
						for (IAttributeDescriptor attribute : getAttributeDescriptorsCopy()) {
							if (attribute.isPrimaryKey() && !attribute.equals(getAttributeDescriptor())) {
								JOptionPane.showMessageDialog(
										AttributePanel.this, 
										"ALPHA version does not allow more than 1 primary key.", 
										"ALPHA restriction", 
										JOptionPane.ERROR_MESSAGE);
								return;
//								throw new RuntimeException("ALPHA version does not allow more than 1 primary key.");
							}
						}
					}
					
					// update name and properties
					getAttributeDescriptor().setName(getAttributeNameTextField().getText());
					getAttributeDescriptor().setPrimaryKey(getCheckBoxPK().isSelected());
					getAttributeDescriptor().setMandatory(getCheckBoxMandatory().isSelected());
					
					// update attribute list
					getClassPanel().getAttributeList().updateUI();
					getClassPanel().getAttributeList().repaint();
					
					// close popup
					getUpperDialog().setVisible(false);
					getUpperDialog().dispose();
				}
			});
			
			// cancel changes
			this.getCancelCPTButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// TODO revert CPT and state's names
					getUpperDialog().setVisible(false);
					getUpperDialog().dispose();
				}
			});
			
			// add state
			this.getAddStateCPTButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getAttributeDescriptor().appendState("NewState" + getAttributeDescriptor().getStatesSize());
					getCptTable().updateUI();
					getCptTable().repaint();
				}
			});
			
			// remove state
			this.getRemoveStateCPTButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (getAttributeDescriptor().getStatesSize() > 1) {
						getAttributeDescriptor().removeLastState();
						getCptTable().clearSelection();
						getCptTable().updateUI();
						getCptTable().repaint();
					}
				}
			});
			
			// add dependency
			this.getAddParentCPTButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JDialog chainDialog = new DependencyChainPanel(AttributePanel.this, getAttributeDescriptor().getPRMDependency()).buildDialog();
					chainDialog.setModal(true);
					chainDialog.setVisible(true);
				}
			});
			
			// TODO remove dependency
			
			// TODO commit changes of CPT
			
			
		}

		/**
		 * Obtains a JDialog containing this panel.
		 * Use this if you want the {@link #getCancelCPTButton()} or {@link #getCommitCPTButton()}
		 * to close the dialog.
		 * @return
		 */
		public JDialog buildDialog() {
			this.setUpperDialog(new JDialog());
			this.getUpperDialog().getContentPane().add(this);
			this.getUpperDialog().setPreferredSize(new Dimension(600,400));
			this.getUpperDialog().pack();
			this.getUpperDialog().setTitle(((IAttributeDescriptor)getAttributeList().getSelectedValue()).getName());
//			this.getUpperDialog().setAlwaysOnTop(true);
			return this.getUpperDialog();
		}
		
		/**
		 * @return the classPanel
		 */
		public ClassPanel getClassPanel() {
			return classPanel;
		}

		/**
		 * @param classPanel the classPanel to set
		 */
		public void setClassPanel(ClassPanel classPanel) {
			this.classPanel = classPanel;
		}

		/**
		 * @return the attribute
		 */
		public IAttributeDescriptor getAttributeDescriptor() {
			return attribute;
		}

		/**
		 * @param attribute the attribute to set
		 */
		public void setAttributeDescriptor(IAttributeDescriptor attribute) {
			this.attribute = attribute;
		}

		/**
		 * @return the attributeNameTextField
		 */
		public JTextField getAttributeNameTextField() {
			return attributeNameTextField;
		}

		/**
		 * @param attributeNameTextField the attributeNameTextField to set
		 */
		public void setAttributeNameTextField(JTextField attributeNameTextField) {
			this.attributeNameTextField = attributeNameTextField;
		}

		/**
		 * @return the typeTextField
		 */
		public JTextField getTypeTextField() {
			return typeTextField;
		}

		/**
		 * @param typeTextField the typeTextField to set
		 */
		public void setTypeTextField(JTextField typeTextField) {
			this.typeTextField = typeTextField;
		}

		/**
		 * @return the typeDownArrowButton
		 */
		public BasicArrowButton getTypeDownArrowButton() {
			return typeDownArrowButton;
		}

		/**
		 * @param typeDownArrowButton the typeDownArrowButton to set
		 */
		public void setTypeDownArrowButton(BasicArrowButton typeDownArrowButton) {
			this.typeDownArrowButton = typeDownArrowButton;
		}

		/**
		 * @return the checkBoxPK
		 */
		public JCheckBox getCheckBoxPK() {
			return checkBoxPK;
		}

		/**
		 * @param checkBoxPK the checkBoxPK to set
		 */
		public void setCheckBoxPK(JCheckBox checkBoxPK) {
			this.checkBoxPK = checkBoxPK;
		}

		/**
		 * @return the checkBoxMandatory
		 */
		public JCheckBox getCheckBoxMandatory() {
			return checkBoxMandatory;
		}

		/**
		 * @param checkBoxMandatory the checkBoxMandatory to set
		 */
		public void setCheckBoxMandatory(JCheckBox checkBoxMandatory) {
			this.checkBoxMandatory = checkBoxMandatory;
		}

		/**
		 * @return the cptEditionScrollPane
		 */
		public JScrollPane getCptEditionScrollPane() {
			return cptEditionScrollPane;
		}

		/**
		 * @param cptEditionScrollPane the cptEditionScrollPane to set
		 */
		public void setCptEditionScrollPane(JScrollPane cptEditionScrollPane) {
			this.cptEditionScrollPane = cptEditionScrollPane;
		}

		/**
		 * @return the cptEditionToolBar
		 */
		public JToolBar getCptEditionToolBar() {
			return cptEditionToolBar;
		}

		/**
		 * @param cptEditionToolBar the cptEditionToolBar to set
		 */
		public void setCptEditionToolBar(JToolBar cptEditionToolBar) {
			this.cptEditionToolBar = cptEditionToolBar;
		}

		/**
		 * @return the cptTableBuilder
		 */
		public CPTTableBuilder getCptTableBuilder() {
			return cptTableBuilder;
		}

		/**
		 * @param cptTableBuilder the cptTableBuilder to set
		 */
		public void setCptTableBuilder(CPTTableBuilder cptTableBuilder) {
			this.cptTableBuilder = cptTableBuilder;
		}

		/**
		 * @return the cptTable
		 */
		public JTable getCptTable() {
			return cptTable;
		}

		/**
		 * @param cptTable the cptTable to set
		 */
		public void setCptTable(JTable cptTable) {
			this.cptTable = cptTable;
		}

		/**
		 * @return the addStateCPTButton
		 */
		public JButton getAddStateCPTButton() {
			return addStateCPTButton;
		}

		/**
		 * @param addStateCPTButton the addStateCPTButton to set
		 */
		public void setAddStateCPTButton(JButton addStateCPTButton) {
			this.addStateCPTButton = addStateCPTButton;
		}

		/**
		 * @return the removeStateCPTButton
		 */
		public JButton getRemoveStateCPTButton() {
			return removeStateCPTButton;
		}

		/**
		 * @param removeStateCPTButton the removeStateCPTButton to set
		 */
		public void setRemoveStateCPTButton(JButton removeStateCPTButton) {
			this.removeStateCPTButton = removeStateCPTButton;
		}

		/**
		 * @return the addParentCPTButton
		 */
		public JButton getAddParentCPTButton() {
			return addParentCPTButton;
		}

		/**
		 * @param addParentCPTButton the addParentCPTButton to set
		 */
		public void setAddParentCPTButton(JButton addParentCPTButton) {
			this.addParentCPTButton = addParentCPTButton;
		}

		/**
		 * @return the removeParentCPTButton
		 */
		public JButton getRemoveParentCPTButton() {
			return removeParentCPTButton;
		}

		/**
		 * @param removeParentCPTButton the removeParentCPTButton to set
		 */
		public void setRemoveParentCPTButton(JButton removeParentCPTButton) {
			this.removeParentCPTButton = removeParentCPTButton;
		}

		/**
		 * @return the commitCPTButton
		 */
		public JButton getCommitCPTButton() {
			return commitCPTButton;
		}

		/**
		 * @param commitCPTButton the commitCPTButton to set
		 */
		public void setCommitCPTButton(JButton commitCPTButton) {
			this.commitCPTButton = commitCPTButton;
		}

		/**
		 * @return the cancelCPTButton
		 */
		public JButton getCancelCPTButton() {
			return cancelCPTButton;
		}

		/**
		 * @param cancelCPTButton the cancelCPTButton to set
		 */
		public void setCancelCPTButton(JButton cancelCPTButton) {
			this.cancelCPTButton = cancelCPTButton;
		}

		/**
		 * @return the upperDialog
		 */
		public JDialog getUpperDialog() {
			return upperDialog;
		}

		/**
		 * @param upperDialog the upperDialog to set
		 */
		public void setUpperDialog(JDialog upperDialog) {
			this.upperDialog = upperDialog;
		}

		/**
		 * @return the attributeNameToolbar
		 */
		public JToolBar getAttributeNameToolbar() {
			return attributeNameToolbar;
		}

		/**
		 * @param attributeNameToolbar the attributeNameToolbar to set
		 */
		public void setAttributeNameToolbar(JToolBar attributeNameToolbar) {
			this.attributeNameToolbar = attributeNameToolbar;
		}

		/**
		 * @return the typeToolBar
		 */
		public JToolBar getTypeToolBar() {
			return typeToolBar;
		}

		/**
		 * @param typeToolBar the typeToolBar to set
		 */
		public void setTypeToolBar(JToolBar typeToolBar) {
			this.typeToolBar = typeToolBar;
		}

		/**
		 * @return the checkBoxToolBar
		 */
		public JToolBar getCheckBoxToolBar() {
			return checkBoxToolBar;
		}

		/**
		 * @param checkBoxToolBar the checkBoxToolBar to set
		 */
		public void setCheckBoxToolBar(JToolBar checkBoxToolBar) {
			this.checkBoxToolBar = checkBoxToolBar;
		}

		/**
		 * @return the tableScrollPane
		 */
		public JScrollPane getTableScrollPane() {
			return tableScrollPane;
		}

		/**
		 * @param tableScrollPane the tableScrollPane to set
		 */
		public void setTableScrollPane(JScrollPane tableScrollPane) {
			this.tableScrollPane = tableScrollPane;
		}

		/**
		 * @return the editParentCPTButton
		 */
		public JButton getEditParentCPTButton() {
			return editParentCPTButton;
		}

		/**
		 * @param editParentCPTButton the editParentCPTButton to set
		 */
		public void setEditParentCPTButton(JButton editParentCPTButton) {
			this.editParentCPTButton = editParentCPTButton;
		}


	}

	
	/**
	 * This class is a panel to edit foreign keys.
	 * @author Shou Matsumoto
	 *
	 */
	public class ForeignKeyPanel extends JPanel{
		
		private JDialog upperDialog;
		
		private IPRMClass prmClass;

		private ClassPanel classPanel;
		
		/** A copy of foreign keys of the class */
		private List<IForeignKey> foreignKeyCopy;

		private JToolBar pkNameToolBar;

		private TextField pkNameTextField;

		private JToolBar fkListToolBar;

		private JButton addFKButton;

		private JButton removeFKButton;

		private JButton editFKButton;

		private JList fkList;

		private JButton commitFKButton;

		private JButton cancelFKButton;
		
		/**
		 * Default constructor initializing fields
		 * @param prmClass
		 */
		public ForeignKeyPanel(ClassPanel classPanel , IPRMClass prmClass) {
			super();
			this.classPanel = classPanel;
			this.prmClass = prmClass;
			this.foreignKeyCopy = new ArrayList<IForeignKey>(prmClass.getForeignKeys());
			this.initComponents();
			this.initListeners();
		}

		/**
		 * Initialize components
		 */
		protected void initComponents() {
			this.setLayout(new BorderLayout());
			
			// a panel in the top side (contains PK name and add/remove FK button)
			JPanel northPanel = new JPanel(new GridLayout(0,1));
			this.add(northPanel, BorderLayout.NORTH);
			
			// tool bar for PK's name (label + text field)
			this.setPkNameToolBar(new JToolBar("PK name"));
			this.getPkNameToolBar().add(new JLabel("Primary key name: "));
			this.setPkNameTextField(new TextField(this.getPrmClass().getPrimaryKeyName(), 30));
			this.getPkNameToolBar().add(this.getPkNameTextField());
			
			// hide PK name toolbar if class does not have a PK
			if (getPrmClass().getPrimaryKeys().isEmpty()) {
				this.getPkNameToolBar().setVisible(false);
			} else {
				this.getPkNameToolBar().setVisible(true);
				northPanel.add(this.getPkNameToolBar());
			}
			
			// tool bar to add/remove FK
			this.setFkListToolBar(new JToolBar("Add/remove foreign keys"));
			northPanel.add(this.getFkListToolBar());
			
			// add FK button
			this.setAddFKButton(new JButton(getIconController().getMoreIcon()));
			this.getAddFKButton().setToolTipText("Add new foreign key");
			this.getFkListToolBar().add(this.getAddFKButton());
			
			// remove FK button
			this.setRemoveFKButton(new JButton(getIconController().getLessIcon()));
			this.getRemoveFKButton().setToolTipText("Remove selected foreign key");
			this.getFkListToolBar().add(this.getRemoveFKButton());
			
			// edit FK button
			this.setEditFKButton(new JButton(getIconController().getEditIcon()));
			this.getEditFKButton().setToolTipText("Edit selected foreign key");
			this.getFkListToolBar().add(this.getEditFKButton());
			
			// FK list
			this.setFkList(new JList(new AbstractListModel() {
				public Object getElementAt(int index) {
					return getForeignKeyCopy().get(index);
				}
				public int getSize() {
					return getForeignKeyCopy().size();
				}
			}));
			this.getFkList().setBorder(new TitledBorder("Foreign Keys"));
			this.add(new JScrollPane(this.getFkList()), BorderLayout.CENTER);
			
			
			// commit/cancel buttons panel
			JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			this.add(southPanel, BorderLayout.SOUTH);
			
			// commit button
			this.setCommitFKButton(new JButton("Commit"));
			this.getCommitFKButton().setToolTipText("Commit changes");
			southPanel.add(this.getCommitFKButton());
			
			// cancel button
			this.setCancelFKButton(new JButton("Cancel"));
			this.getCancelFKButton().setToolTipText("Cancel changes");
			southPanel.add(this.getCancelFKButton());
			
		}

		/**
		 * Initialize listeners
		 */
		protected void initListeners() {
			// commit pk name and fk list
			this.getCommitFKButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// update pk name
					getPrmClass().setPrimaryKeyName(getPkNameTextField().getText());
					// update fk list
					getPrmClass().setForeignKeys(getForeignKeyCopy());
					// close dialog
					getUpperDialog().setVisible(false);
					getUpperDialog().dispose();
				}
			});
			
			// cancel
			this.getCancelFKButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// just close the dialog
					getUpperDialog().setVisible(false);
					getUpperDialog().dispose();
				}
			});
			
			// add new fk
			this.getAddFKButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// add to the temporary list
					IForeignKey fk = getPrmClass().getForeignKeyBuilder().buildForeignKey();
					fk.setClassFrom(getPrmClass());
					getForeignKeyCopy().add(fk);
					
					// popup dialog
					new FKEditionPanel(ForeignKeyPanel.this, fk).buildDialog().setVisible(true);
					
					// repaint JList
					getFkList().updateUI();
					getFkList().repaint();
				}
			});
			
			// remove selected fk
			this.getRemoveFKButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!getFkList().isSelectionEmpty()) {
						// remove selected element from the temporary list
						getForeignKeyCopy().remove(getFkList().getSelectedIndex());
						// clear selection
						getFkList().clearSelection();
						// repaint JList
						getFkList().updateUI();
						getFkList().repaint();
					}
				}
			});
			
			
			// edit selected fk
			this.getEditFKButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!getFkList().isSelectionEmpty()) {
						// popup panel
						new FKEditionPanel(ForeignKeyPanel.this, (IForeignKey)getFkList().getSelectedValue()).buildDialog().setVisible(true);
						
						// update list
						getFkList().updateUI();
						getFkList().repaint();
					}
				}
			});
			
			
		}

		/**
		 * Obtains a JDialog containing this panel.
		 * Use this if you want the {@link #getCancelCPTButton()} or {@link #getCommitCPTButton()}
		 * to close the dialog.
		 * @return
		 */
		public JDialog buildDialog() {
			this.setUpperDialog(new JDialog());
			this.getUpperDialog().getContentPane().add(this);
			this.getUpperDialog().setPreferredSize(new Dimension(600,400));
			this.getUpperDialog().pack();
			this.getUpperDialog().setTitle("FK of " + getPrmClass().getName());
//			this.getUpperDialog().setAlwaysOnTop(true);
			return this.getUpperDialog();
		}
		
		/**
		 * @return the prmClass
		 */
		public IPRMClass getPrmClass() {
			return prmClass;
		}

		/**
		 * @param prmClass the prmClass to set
		 */
		public void setPrmClass(IPRMClass prmClass) {
			this.prmClass = prmClass;
		}

		/**
		 * @return the upperDialog
		 */
		public JDialog getUpperDialog() {
			return upperDialog;
		}

		/**
		 * @param upperDialog the upperDialog to set
		 */
		public void setUpperDialog(JDialog upperDialog) {
			this.upperDialog = upperDialog;
		}

		/**
		 * @return the classPanel
		 */
		public ClassPanel getClassPanel() {
			return classPanel;
		}

		/**
		 * @param classPanel the classPanel to set
		 */
		public void setClassPanel(ClassPanel classPanel) {
			this.classPanel = classPanel;
		}

		/**
		 * @return the foreignKeyCopy
		 */
		public List<IForeignKey> getForeignKeyCopy() {
			return foreignKeyCopy;
		}

		/**
		 * @param foreignKeyCopy the foreignKeyCopy to set
		 */
		public void setForeignKeyCopy(List<IForeignKey> foreignKeyCopy) {
			this.foreignKeyCopy = foreignKeyCopy;
		}

		/**
		 * @return the pkNameToolBar
		 */
		public JToolBar getPkNameToolBar() {
			return pkNameToolBar;
		}

		/**
		 * @param pkNameToolBar the pkNameToolBar to set
		 */
		public void setPkNameToolBar(JToolBar pkNameToolBar) {
			this.pkNameToolBar = pkNameToolBar;
		}

		/**
		 * @return the pkNameTextField
		 */
		public TextField getPkNameTextField() {
			return pkNameTextField;
		}

		/**
		 * @param pkNameTextField the pkNameTextField to set
		 */
		public void setPkNameTextField(TextField pkNameTextField) {
			this.pkNameTextField = pkNameTextField;
		}

		/**
		 * @return the fkListToolBar
		 */
		public JToolBar getFkListToolBar() {
			return fkListToolBar;
		}

		/**
		 * @param fkListToolBar the fkListToolBar to set
		 */
		public void setFkListToolBar(JToolBar fkListToolBar) {
			this.fkListToolBar = fkListToolBar;
		}

		/**
		 * @return the addFKButton
		 */
		public JButton getAddFKButton() {
			return addFKButton;
		}

		/**
		 * @param addFKButton the addFKButton to set
		 */
		public void setAddFKButton(JButton addFKButton) {
			this.addFKButton = addFKButton;
		}

		/**
		 * @return the removeFKButton
		 */
		public JButton getRemoveFKButton() {
			return removeFKButton;
		}

		/**
		 * @param removeFKButton the removeFKButton to set
		 */
		public void setRemoveFKButton(JButton removeFKButton) {
			this.removeFKButton = removeFKButton;
		}

		/**
		 * @return the editFKButton
		 */
		public JButton getEditFKButton() {
			return editFKButton;
		}

		/**
		 * @param editFKButton the editFKButton to set
		 */
		public void setEditFKButton(JButton editFKButton) {
			this.editFKButton = editFKButton;
		}

		/**
		 * @return the fkList
		 */
		public JList getFkList() {
			return fkList;
		}

		/**
		 * @param fkList the fkList to set
		 */
		public void setFkList(JList fkList) {
			this.fkList = fkList;
		}

		/**
		 * @return the commitFKButton
		 */
		public JButton getCommitFKButton() {
			return commitFKButton;
		}

		/**
		 * @param commitFKButton the commitFKButton to set
		 */
		public void setCommitFKButton(JButton commitFKButton) {
			this.commitFKButton = commitFKButton;
		}

		/**
		 * @return the cancelFKButton
		 */
		public JButton getCancelFKButton() {
			return cancelFKButton;
		}

		/**
		 * @param cancelFKButton the cancelFKButton to set
		 */
		public void setCancelFKButton(JButton cancelFKButton) {
			this.cancelFKButton = cancelFKButton;
		}
	}
	
	/**
	 * panel to edit FK parameters
	 * @author Shou Matsumoto
	 *
	 */
	protected class FKEditionPanel extends JPanel {
		
		private ForeignKeyPanel upperPanel;
		private IForeignKey fk;
		private JDialog upperDialog;
		private JTextField fkNameTextField;
		private JList attributesFromList;
		
		private List<IAttributeDescriptor> tempFrom;
		
		private JList classesToList;
		private JButton commitFKButton;
		private JButton cancelFKButton;
		
		public FKEditionPanel (ForeignKeyPanel upperPanel, IForeignKey fk) {
			super();
			this.upperPanel = upperPanel;
			this.fk = fk;
			this.tempFrom = new ArrayList<IAttributeDescriptor>(fk.getClassFrom().getAttributeDescriptors());
			
			this.initComponents();
			this.initListeners();
		}
		
		/**
		 * Initialize components
		 */
		protected void initComponents() {
			this.setLayout(new BorderLayout());
			
			// name edition toolbar
			JToolBar nameEditionToolbar = new JToolBar("Name of FK");
			nameEditionToolbar.add(new JLabel("Name: "));
			fkNameTextField = new JTextField(fk.getName(), 20);
			nameEditionToolbar.add(fkNameTextField);
			this.add(nameEditionToolbar, BorderLayout.NORTH);
			
			
			// list of attributes "from"
			attributesFromList = new JList(new AbstractListModel() {
				public Object getElementAt(int index) {
					return getPrmClass().getAttributeDescriptors().get(index);
				}
				public int getSize() {
					return getPrmClass().getAttributeDescriptors().size();
				}
			});
			attributesFromList.setBorder(new TitledBorder(getPrmClass().getName() + " (commited)"));
			// Alpha version allows only 1 PK
			attributesFromList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			// mark initial selection of attributesFromList and add it to panel
			List<Integer> selectionIndex = new ArrayList<Integer>();
			for (int i = 0; i < tempFrom.size(); i++) {
				if (fk.getKeyAttributesFrom().contains(tempFrom.get(i))) {
					selectionIndex.add(i);
				}
			}
			//... problems converting list of Integer to array of int... convert manually...
			int selectionIndexArray[] = new int[selectionIndex.size()];
			for (int i = 0; i < selectionIndexArray.length; i++) {
				selectionIndexArray[i] = selectionIndex.get(i);
			}
			attributesFromList.setSelectedIndices(selectionIndexArray);
			attributesFromList.updateUI();
			attributesFromList.repaint();

			// List for remote class ("to")
			classesToList = new JList(new AbstractListModel() {
				public Object getElementAt(int index) {
					return getPrmClass().getPRM().getIPRMClasses().get(index);
				}
				public int getSize() {
					return getPrmClass().getPRM().getIPRMClasses().size();
				}
			});
			classesToList.setBorder(new TitledBorder("Select a class to reference"));
			classesToList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			// initialize selection
			if (fk.getClassTo() != null) {
				classesToList.setSelectedValue(fk.getClassTo(), true);
			}
			
			// split pane to edit references (referenced class and referenced attributes)
			JSplitPane referenceSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
															new JScrollPane(attributesFromList), 
															new JScrollPane(classesToList));
			this.add(referenceSplitPane, BorderLayout.CENTER);
			
			// panel for commit/cancel button
			JPanel commitCancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			this.add(commitCancelPanel, BorderLayout.SOUTH);
			
			// commit/cancel button.
			commitFKButton = new JButton("Commit");
			commitFKButton.setToolTipText("Commit changes");
			commitCancelPanel.add(commitFKButton);
			
			cancelFKButton = new JButton("Cancel");
			cancelFKButton.setToolTipText("Cancel changes");
			commitCancelPanel.add(cancelFKButton);
		}

		/**
		 * Initialize listeners
		 */
		protected void initListeners() {
			// listeners for commit button
			commitFKButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// update name
					FKEditionPanel.this.fk.setName(fkNameTextField.getText());
					
					// obtain selected attributes
					Object[] selectedAttributes = attributesFromList.getSelectedValues();
					// converts array to set
					Set<IAttributeDescriptor> selectedAttributesSet = new HashSet<IAttributeDescriptor>();
					for (int i = 0; i < selectedAttributes.length; i++) {
						selectedAttributesSet.add((IAttributeDescriptor)selectedAttributes[i]);
					}			
					// update attributes from
					FKEditionPanel.this.fk.setKeyAttributesFrom(selectedAttributesSet);
					
					// update reference to
					FKEditionPanel.this.fk.setClassTo((IPRMClass)classesToList.getSelectedValue());
					
					// update attribute to
					FKEditionPanel.this.fk.setKeyAttributesTo(new HashSet<IAttributeDescriptor>(((IPRMClass)classesToList.getSelectedValue()).getPrimaryKeys()));
					
					// close dialog
					upperDialog.setVisible(false);
					upperDialog.dispose();
				}
			});
			
			// listeners for cancel button
			cancelFKButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// remove fk if it was added
//					FKEditionPanel.this.upperPanel.getForeignKeyCopy().remove(FKEditionPanel.this.fk);
					// close dialog
					upperDialog.setVisible(false);
					upperDialog.dispose();
				}
			});
			
		}

		/**
		 * Obtains a JDialog containing this panel.
		 * @return
		 */
		public JDialog buildDialog() {
			this.upperDialog = new JDialog((Frame)null,true);
			this.upperDialog.getContentPane().add(this);
			this.upperDialog.setPreferredSize(new Dimension(500,500));
			this.upperDialog.pack();
			this.upperDialog.setTitle(this.fk.getName());
			return this.upperDialog;
		}
	}
	
	/**
	 * A panel to edit chain of dependencies
	 * @author Shou Matsumoto
	 *
	 */
	public class DependencyChainPanel extends JPanel {

		private JDialog upperDialog;
		private JLabel navigationLabel;
		private AttributePanel upperPanel;

		private IDependencyChain currentChain;
		private JButton commitChainButton;
		private JList chainAttributeList;
		private JList fkList;
		private JButton buttonGoFK;
		private JButton buttonBackFK;
		private JToolBar aggregateFunctionToolbar;
		private JTextField aggregateFunctionTextField;
		private BasicArrowButton aggregateFunctionDownArrowButton;
		
		/**
		 * Fill components and listeners.
		 * @param prmDependency : where new {@link IDependencyChain} will be attached.
		 * @param upperPanel : caller of this panel/dialog. This reference will be used by this panel in order to
		 * repaint CPT table on commit
		 */
		public DependencyChainPanel(AttributePanel upperPanel, IPRMDependency prmDependency) {
			super();
			this.currentChain = DependencyChain.newInstance();
			this.currentChain.setDependencyTo(prmDependency);	// set prmDependency as child 
			this.currentChain.setDependencyFrom(prmDependency);	// temporary reference to where we are in navigation
			this.upperPanel = upperPanel;
			this.initComponents();
			this.initListeners();
		}
		
		/**
		 * Initialize components
		 */
		protected void initComponents() {
			this.setLayout(new BorderLayout());
			
			// a label indicating current navigation hierarchy (i.e. "ClassA > ParentOfClassA > ParentOfParentOfClassA > ..." )
			this.setNavigationLabel(new JLabel("Path: " + getCurrentChain().getDependencyTo().getAttributeDescriptor().getPRMClass().getName()));
			this.add(this.getNavigationLabel(), BorderLayout.NORTH);
			
			// panel containing a list and editor for currently selected class' attributes
			JPanel targetAttributePanel = new JPanel(new BorderLayout());
			
			// toolbar to append aggregate function
			this.setAggregateFunctionToolbar(new JToolBar("Aggregate function"));
			targetAttributePanel.add(this.getAggregateFunctionToolbar(), BorderLayout.NORTH);
			
			// inner panel to group aggregate function drop down list
			JPanel aggregateFunctionDropDownListPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			this.getAggregateFunctionToolbar().add(aggregateFunctionDropDownListPanel);
			
			// a dropdown list to select aggregation function
			// TODO enable dropdown list, because ALPHA version disables it as default - this is a stub
			JLabel aggregateFunctionLabel = new JLabel("Aggregation: ");
			aggregateFunctionLabel.setEnabled(false);
			aggregateFunctionDropDownListPanel.add(aggregateFunctionLabel);
			
			// aggregate function dropdown list (TODO this is a stub)
			this.setAggregateFunctionTextField(new JTextField("Mode", 8));
			this.getAggregateFunctionTextField().setEditable(false);
			this.getAggregateFunctionTextField().setEnabled(false);
			aggregateFunctionDropDownListPanel.add(this.getAggregateFunctionTextField());
			
			// the down arrow button for aggregate function popup menu (TODO this is a stub)
			this.setAggregateFunctionDownArrowButton(new BasicArrowButton(BasicArrowButton.SOUTH));
			this.getAggregateFunctionDownArrowButton().setEnabled(false);
			this.getAggregateFunctionDownArrowButton().setToolTipText("Only \"Mode\" is allowed as aggregation function in ALPHA version.");
			aggregateFunctionDropDownListPanel.add(this.getAggregateFunctionDownArrowButton());
			
			// list containing attributes of currently selected class
			this.setChainAttributeList(new JList(new AbstractListModel() {
				public Object getElementAt(int index) {
					return getCurrentChain().getDependencyFrom().getAttributeDescriptor().getPRMClass().getAttributeDescriptors().get(index);
				}
				public int getSize() {
					return getCurrentChain().getDependencyFrom().getAttributeDescriptor().getPRMClass().getAttributeDescriptors().size();
				}
			}));
			this.getChainAttributeList().setBorder(new TitledBorder("Select parent attribute"));
			this.getChainAttributeList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			targetAttributePanel.add(this.getChainAttributeList(), BorderLayout.CENTER);
			
			// panel to navigate through FK
			JPanel fkPanel = new JPanel(new BorderLayout());
			
			// list of FK
			this.setFkList(new JList(new AbstractListModel() {
				public Object getElementAt(int index) {
					// TODO optimize
					// These are the FKs (outgoing references)
					List<IForeignKey> allFKs = new ArrayList<IForeignKey>(getCurrentChain().getDependencyFrom().getAttributeDescriptor().getPRMClass().getForeignKeys());
					// These are the inverse FKs (incoming references)
					allFKs.addAll(getCurrentChain().getDependencyFrom().getAttributeDescriptor().getPRMClass().getIncomingForeignKeys());
					return allFKs.get(index);
				}
				public int getSize() {
					return getCurrentChain().getDependencyFrom().getAttributeDescriptor().getPRMClass().getForeignKeys().size()	// incoming FK
						 + getCurrentChain().getDependencyFrom().getAttributeDescriptor().getPRMClass().getIncomingForeignKeys().size(); // inverse (outgoing) FK
				}
			}));
			this.getFkList().setBorder(new TitledBorder("Foreign keys"));
			this.getFkList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			fkPanel.add(this.getFkList(), BorderLayout.CENTER);
			
			// panel/toolbar for buttons to navigate inside FK
			JToolBar fkNavigationButtonPanel = new JToolBar();
			fkPanel.add(fkNavigationButtonPanel, BorderLayout.NORTH);
			
			// button to go inside FK
			this.setButtonGoFK(new JButton(getIconController().getGoNextInstance()));
			this.getButtonGoFK().setToolTipText("Go to the class referenced by selected foreign key");
			fkNavigationButtonPanel.add(this.getButtonGoFK());
			
			// button to go back from FK
			this.setButtonBackFK(new JButton(getIconController().getGoPreviousInstance()));
			this.getButtonBackFK().setToolTipText("Go back to previous class (disabled in ALPHA)");
			fkNavigationButtonPanel.add(this.getButtonBackFK());
			// TODO enable button to go back
			this.getButtonBackFK().setEnabled(false);
			
			// split pane containing attributes on left and FKs on right
			JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(targetAttributePanel),new JScrollPane(fkPanel));
			centerSplitPane.setDividerLocation(250);
			this.add(centerSplitPane, BorderLayout.CENTER);
			
			// panel containing commit 
			JPanel commitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			this.add(commitPanel, BorderLayout.SOUTH);
			
			// button to commit
			this.setCommitChainButton(new JButton("Commit"));
			this.getCommitChainButton().setToolTipText("Commit changes on this dependency chain.");
			commitPanel.add(this.getCommitChainButton());
		}

		/**
		 * Initialize listeners
		 */
		protected void initListeners() {
			
			// go FK button
			this.getButtonGoFK().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					// obtain selected FK
					IForeignKey selectedFK = (IForeignKey)getFkList().getSelectedValue();
					
					// assertion
					if (selectedFK == null) {
						// there was no selection...
						JOptionPane.showMessageDialog(
								DependencyChainPanel.this, 
								"Select a foreign key.", 
								"No selection", 
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					// verify if the currently selected FK is inverse (incoming) or not (outgoing)
					// becaulse getFkList() contains inverse FKs at the end of the list, all elements put after all outgoing FKs are inverse FKs
					boolean isInverseFK = 
						getFkList().getSelectedIndex() >= getCurrentChain().getDependencyFrom().getAttributeDescriptor().getPRMClass().getForeignKeys().size();
					
					
					// extract target attribute (usually, they are FKs or PKs of prm classes
					IAttributeDescriptor targetAttribute = null;
					if (isInverseFK) {
						// incoming FK -> use from
						if (selectedFK.getKeyAttributesFrom() != null && !selectedFK.getKeyAttributesFrom().isEmpty()) {
							targetAttribute = selectedFK.getKeyAttributesFrom().iterator().next();
						}
					} else {
						// outgoing FK -> use to
						if (selectedFK.getKeyAttributesTo()!= null && !selectedFK.getKeyAttributesTo().isEmpty()) {
							targetAttribute = selectedFK.getKeyAttributesTo().iterator().next();
						}
					}
					
					// assertion
					if (targetAttribute == null || targetAttribute.getPRMDependency() == null) {
						// no target attribute was extracted... There was an error...
						JOptionPane.showMessageDialog(
								DependencyChainPanel.this, 
								"No attribute could be extracted from foreign key. Check FK or attribute consistency.", 
								"Invalid FK", 
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					// add FK to chain (note that the FK chain' order must be from parent to current node, so, use inverse order)
					getCurrentChain().getForeignKeyChain().add(0,selectedFK);	// add at the beginning
					if (isInverseFK) {
						getCurrentChain().markAsInverseForeignKey(selectedFK, true);
					} else {
						// We know FKs are initially marked as non-inverse, but let's explicitly mark it as so.
						getCurrentChain().markAsInverseForeignKey(selectedFK, false);
					}
					
					// if FK is an inverse Fk, add selected aggregate function
					if (isInverseFK) {
						// TODO use value obtained by getAggregateFunctionTextField() (currently, this is a stub)
						getCurrentChain().setAggregateFunction(AggregateFunctionMode.newInstance(getCurrentChain()));
					}
					
					// update the target attribute of getCurrentChain() (which is where the dependency is coming "from")
					getCurrentChain().setDependencyFrom(targetAttribute.getPRMDependency());
					
					// update navigation label 
					getNavigationLabel().setText(getNavigationLabel().getText() + " > " + targetAttribute.getPRMClass().getName());
					getNavigationLabel().repaint();
					
					// update attribute list ((no structure change is needed, because the list model accesses getCurrentChain().getDependencyFrom()))
					getChainAttributeList().updateUI();
					getChainAttributeList().repaint();
					
					// update fk list ((no structure change is needed, because the list model accesses getCurrentChain().getDependencyFrom()))
					getFkList().updateUI();
					getFkList().repaint();
					
					// TODO ALPHA version does not allow a FK chain above 1 level (it only allows a direct FK chain or its inverse)
					if (getCurrentChain().getForeignKeyChain().size() >= 1) {
						getButtonGoFK().setEnabled(false);
						getButtonGoFK().setToolTipText("ALPHA version allows only 1 level in FK chain.");
					}
					
					DependencyChainPanel.this.updateUI();
					DependencyChainPanel.this.repaint();
				}
			});
			
			
			// commit button
			this.getCommitChainButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// assertion
					if (getChainAttributeList().getSelectedIndex() < 0) {
						// no selection
						JOptionPane.showMessageDialog(
								DependencyChainPanel.this, 
								"Select an attribute as a parent.", 
								"No parent", 
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					// assert that no mandatory attribute was selected
					if (((IAttributeDescriptor)getChainAttributeList().getSelectedValue()).isMandatory()) {
						JOptionPane.showMessageDialog(
								DependencyChainPanel.this, 
								"ALPHA version does not allow non-random (mandatory) fields as parent.", 
								"Invalid parent", 
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					// update the attribute "from" of the chain using the selected one from the list
					getCurrentChain().setDependencyFrom(((IAttributeDescriptor)getChainAttributeList().getSelectedValue()).getPRMDependency());
					
					// assert no direct cycle
					if (getCurrentChain().getDependencyFrom().equals(getCurrentChain().getDependencyTo())) {
						if (getCurrentChain().getForeignKeyChain() == null || getCurrentChain().getForeignKeyChain().isEmpty()) {
							// cycle to itself...
							JOptionPane.showMessageDialog(
									DependencyChainPanel.this, 
									"An attribute cannot be a parent of itself.", 
									"Cycle", 
									JOptionPane.WARNING_MESSAGE);
							return;
						}
					}
					
					// add chain to attribute "from" (which is the parent) as outgoing chain
					if (!getCurrentChain().getDependencyFrom().getDependencyChains().contains(getCurrentChain())) {
						getCurrentChain().getDependencyFrom().getDependencyChains().add(getCurrentChain());
					}
					
					// add chain to attribute "to" (which is the current attribute on edition) as incoming chain
					if (!getCurrentChain().getDependencyTo().getIncomingDependencyChains().contains(getCurrentChain())) {
						getCurrentChain().getDependencyTo().getIncomingDependencyChains().add(getCurrentChain());
					}
					
					// update CPT (reset the whole panel)
					getUpperPanel().resetComponents();
					getUpperPanel().updateUI();
					getUpperPanel().repaint();
					
					
					// close dialog
					if (upperDialog != null) {
						upperDialog.setVisible(false);
						upperDialog.dispose();
					}
					
				}
			});
			
		}

		/**
		 * Creates a JDialog containing this panel
		 * @return
		 */
		public JDialog buildDialog() {
			this.upperDialog = new JDialog((Frame)null,true);
			this.upperDialog.getContentPane().add(this);
			this.upperDialog.setPreferredSize(new Dimension(500,500));
			this.upperDialog.pack();
			this.upperDialog.setTitle("Dependency of " + getCurrentChain().getDependencyTo().getAttributeDescriptor().getName());
			return this.upperDialog;
		}

		/**
		 * @return the navigationLabel
		 */
		public JLabel getNavigationLabel() {
			return navigationLabel;
		}

		/**
		 * @param navigationLabel the navigationLabel to set
		 */
		public void setNavigationLabel(JLabel navigationLabel) {
			this.navigationLabel = navigationLabel;
		}

		/**
		 * @return the upperPanel
		 */
		public AttributePanel getUpperPanel() {
			return upperPanel;
		}

		/**
		 * @param upperPanel the upperPanel to set
		 */
		public void setUpperPanel(AttributePanel upperPanel) {
			this.upperPanel = upperPanel;
		}

		/**
		 * Chain currently being created
		 * @return the currentChain
		 */
		public IDependencyChain getCurrentChain() {
			return currentChain;
		}

		/**
		 * Chain currently being created
		 * @param currentChain the currentChain to set
		 */
		public void setCurrentChain(IDependencyChain currentChain) {
			this.currentChain = currentChain;
		}

		/**
		 * @return the commitChainButton
		 */
		public JButton getCommitChainButton() {
			return commitChainButton;
		}

		/**
		 * @param commitChainButton the commitChainButton to set
		 */
		public void setCommitChainButton(JButton commitChainButton) {
			this.commitChainButton = commitChainButton;
		}

		/**
		 * @return the chainAttributeList
		 */
		public JList getChainAttributeList() {
			return chainAttributeList;
		}

		/**
		 * @param chainAttributeList the chainAttributeList to set
		 */
		public void setChainAttributeList(JList chainAttributeList) {
			this.chainAttributeList = chainAttributeList;
		}

		/**
		 * @return the fkList
		 */
		public JList getFkList() {
			return fkList;
		}

		/**
		 * @param fkList the fkList to set
		 */
		public void setFkList(JList fkList) {
			this.fkList = fkList;
		}

		/**
		 * @return the buttonGoFK
		 */
		public JButton getButtonGoFK() {
			return buttonGoFK;
		}

		/**
		 * @param buttonGoFK the buttonGoFK to set
		 */
		public void setButtonGoFK(JButton buttonGoFK) {
			this.buttonGoFK = buttonGoFK;
		}

		/**
		 * @return the buttonBackFK
		 */
		public JButton getButtonBackFK() {
			return buttonBackFK;
		}

		/**
		 * @param buttonBackFK the buttonBackFK to set
		 */
		public void setButtonBackFK(JButton buttonBackFK) {
			this.buttonBackFK = buttonBackFK;
		}

		/**
		 * @return the aggregateFunctionToolbar
		 */
		public JToolBar getAggregateFunctionToolbar() {
			return aggregateFunctionToolbar;
		}

		/**
		 * @param aggregateFunctionToolbar the aggregateFunctionToolbar to set
		 */
		public void setAggregateFunctionToolbar(JToolBar aggregateFunctionToolbar) {
			this.aggregateFunctionToolbar = aggregateFunctionToolbar;
		}

		/**
		 * @return the aggregateFunctionTextField
		 */
		public JTextField getAggregateFunctionTextField() {
			return aggregateFunctionTextField;
		}

		/**
		 * @param aggregateFunctionTextField the aggregateFunctionTextField to set
		 */
		public void setAggregateFunctionTextField(JTextField aggregateFunctionTextField) {
			this.aggregateFunctionTextField = aggregateFunctionTextField;
		}

		/**
		 * @return the aggregateFunctionDownArrowButton
		 */
		public BasicArrowButton getAggregateFunctionDownArrowButton() {
			return aggregateFunctionDownArrowButton;
		}

		/**
		 * @param aggregateFunctionDownArrowButton the aggregateFunctionDownArrowButton to set
		 */
		public void setAggregateFunctionDownArrowButton(
				BasicArrowButton aggregateFunctionDownArrowButton) {
			this.aggregateFunctionDownArrowButton = aggregateFunctionDownArrowButton;
		}

		
	}

}
