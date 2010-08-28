/**
 * 
 */
package unbbayes.gui.prm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import unbbayes.controller.IconController;
import unbbayes.gui.MDIDesktopPane;
import unbbayes.gui.NetworkWindow;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.prm.AttributeValue;
import unbbayes.prs.prm.IAttributeDescriptor;
import unbbayes.prs.prm.IAttributeValue;
import unbbayes.prs.prm.IPRMClass;
import unbbayes.prs.prm.IPRMObject;
import unbbayes.prs.prm.PRMObject;
import unbbayes.prs.prm.compiler.IBNInferenceAlgorithmHolder;

/**
 * A panel basically containing a table to edit attribute values
 * (object values, i.e. rows of PRM tables)
 * @author Shou Matsumoto
 *
 */
public class ObjectPanel extends JPanel {

	private SkeletonPanel upperPanel;
	private IPRMClass prmClass;
	private JTable objectDataTable;
	private JScrollPane objectDataTableScrollPane;
	private JToolBar objectDataToolBar;
	
	private IconController iconController;
	private JButton addObjectButton;
	private JButton removeObjectButton;
	private JButton compilePRMButton;
	
	/**
	 * At least one constructor must be visible for subclasses to allow
	 * inheritance;
	 */
	protected ObjectPanel() {
		// TODO Auto-generated constructor stub
		this.iconController = IconController.getInstance();
	}

	/**
	 * Default construction method
	 * @param upperPanel : SkeletonPanel containing this panel
	 * @param prmClass : class containing objects to be rendered
	 * @return an instance of ObjectPanel
	 */
	public static ObjectPanel newInstance(SkeletonPanel upperPanel, IPRMClass prmClass) {
		ObjectPanel ret = new ObjectPanel();
		ret.upperPanel = upperPanel;
		ret.prmClass = prmClass;
		ret.initComponents();
		ret.initListeners();
		return ret;
	}
	
	/**
	 * Remove all components and calls {@link #initComponents()} and
	 * then {@link #initListeners()}
	 */
	public void resetComponents(){
		this.removeAll();
		this.initComponents();
		this.initListeners();
	}

	/**
	 * Initialize components
	 */
	protected void initComponents() {
		
		this.setLayout(new BorderLayout(10,20));
		
		// tool bar to edit objects
		this.setObjectDataToolBar(new JToolBar("Edit objects"));
		this.add(this.getObjectDataToolBar(), BorderLayout.NORTH);
		
		// button to add object
		this.setAddObjectButton(new JButton(this.getIconController().getMoreIcon()));
		this.getAddObjectButton().setToolTipText("Add a new entry (PRM object/row).");
		this.getObjectDataToolBar().add(this.getAddObjectButton());
		
		// button to remove selected object
		this.setRemoveObjectButton(new JButton(this.getIconController().getLessIcon()));
		this.getRemoveObjectButton().setToolTipText("Remove selected entry (PRM object/row).");
		this.getObjectDataToolBar().add(this.getRemoveObjectButton());
		
		// separator for object edition tool bar
		this.getObjectDataToolBar().addSeparator();
		
		// button to compile PRM using selected cell value
		this.setCompilePRMButton(new JButton(this.getIconController().getCompileIcon()));
		this.getCompilePRMButton().setToolTipText("Compile PRM using selected cell.");
		this.getObjectDataToolBar().add(this.getCompilePRMButton());
		
		// build header (name of attributes) of table
		String[] columns = new String[this.getPrmClass().getAttributeDescriptors().size()];
		for (int i = 0; i < this.getPrmClass().getAttributeDescriptors().size(); i++) {
			columns[i] = this.getPrmClass().getAttributeDescriptors().get(i).getName();
		}
		
		// build data (actual values) of table
		String[][] data = new String[this.getPrmClass().getPRMObjects().size()][columns.length];
		if (data.length != 0 && columns.length != 0) {
			// there is at least one cell, so, render them all
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < columns.length; j++) { // note that data[0].length == columns.length
					IAttributeValue val = this.getPrmClass().getPRMObjects().get(i).getAttributeValueMap().get(this.getPrmClass().findAttributeDescriptorByName(columns[j]));
					data[i][j] = (val != null)?(val.getValue()):(null);
				}
			}
		}
		
		// the jtable itself
		this.setObjectDataTable(new JTable(data, columns));
		
		// enable single cell selection
		this.getObjectDataTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.getObjectDataTable().setColumnSelectionAllowed(false);
		this.getObjectDataTable().setRowSelectionAllowed(false);
		this.getObjectDataTable().setCellSelectionEnabled(true);
		
		// scroll pane containing the object's table
		this.setObjectDataTableScrollPane(new JScrollPane(this.getObjectDataTable()));
		this.add(this.getObjectDataTableScrollPane(), BorderLayout.CENTER);
		
		// change header colors
//		this.getObjectDataTable().getTableHeader().setBackground(Color.YELLOW);
		this.getObjectDataTable().getTableHeader().setForeground(Color.BLACK);
		this.getObjectDataTable().getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
		this.getObjectDataTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		// set minimum column size
		for (int i = 0; i < this.getObjectDataTable().getColumnModel().getColumnCount(); i++) {
			this.getObjectDataTable().getColumnModel().getColumn(i).setMinWidth(100); 
		}
	}

	/**
	 * Initialize listeners
	 */
	protected void initListeners() {
		
		// add/update value when updating table
		this.getObjectDataTable().getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				
				// this is the PRM representation of a cell. 
				// Lets fill it after extracting the descriptors of column (prm attribute) and row (prm object)
				IAttributeValue cellDescriptor = null;
				
				// Change the PRM cell or reset to its previous value.
				try {
					// extract the prm attribute to fill
					IAttributeDescriptor columnDescriptor = 
									getPrmClass().findAttributeDescriptorByName(
												getObjectDataTable().getColumnModel().getColumn(e.getColumn()).getHeaderValue().toString());
					if (columnDescriptor == null) {
						JOptionPane.showMessageDialog(
								ObjectPanel.this, 
								"Could not obtain attribute (column) descriptor from the selected cell. Check data consistency.", 
								"No column descriptor", 
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					// extract the prm object to fill
					IPRMObject rowDescriptor = getPrmClass().getPRMObjects().get(e.getLastRow());
					if (rowDescriptor  == null) {
						JOptionPane.showMessageDialog(
								ObjectPanel.this, 
								"Could not obtain object (row) descriptor from the selected cell. Check data consistency.", 
								"No row descriptor", 
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					// extract the prm attribute value to update (if it exists)
					cellDescriptor = rowDescriptor.getAttributeValueMap().get(columnDescriptor);
					
					// extract value from JTable
					String valueText = getObjectDataTable().getValueAt(e.getLastRow(),e.getColumn()).toString();
					if (valueText != null && valueText.length() <= 0) {
						// if string is empty, use null instead of empty string.
						// I dont want to use trim because space may be important for a user...
						valueText = null;
					}
					
					// Create new cell descriptor, since it does not exist
					if (cellDescriptor == null) {
						cellDescriptor = AttributeValue.newInstance(rowDescriptor, columnDescriptor);
					} 
					
					// just update cell value, because it already exists
					cellDescriptor.setValue(valueText);
										
				} catch (Exception exc) {
					exc.printStackTrace();
					JOptionPane.showMessageDialog(
							ObjectPanel.this, 
							exc.getMessage(), 
							"Exception updating PRM object", 
							JOptionPane.ERROR_MESSAGE);
					getObjectDataTable().revalidate();
					if (cellDescriptor != null) {
						// revert value
						getObjectDataTable().setValueAt(
								cellDescriptor.getValue(),
								e.getLastRow(), 
								e.getColumn());
					}
				}
			}
		});
		
		
		// add new object button
		this.getAddObjectButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PRMObject.newInstance(getPrmClass());	// also automatically associates with getPrmClass
				
				// update
				resetComponents();
			}
		});
		
		// remove selected object button
		this.getRemoveObjectButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// assertion
				if (getObjectDataTable().getSelectedRow() < 0) {
					// nothing selected
					return;
				}
				
				// extract the prm object to delete
				IPRMObject rowDescriptor = getPrmClass().getPRMObjects().get(getObjectDataTable().getSelectedRow());
				if (rowDescriptor  == null) {
					JOptionPane.showMessageDialog(
							ObjectPanel.this, 
							"Could not obtain object (row) descriptor from the selected cell. Check data consistency.", 
							"No row descriptor", 
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// extract the prm attribute to delete value from
				IAttributeDescriptor columnDescriptor = 
								getPrmClass().findAttributeDescriptorByName(
											getObjectDataTable().getColumnModel().getColumn(getObjectDataTable().getSelectedColumn()).getHeaderValue().toString());
				if (columnDescriptor == null) {
					JOptionPane.showMessageDialog(
							ObjectPanel.this, 
							"Could not obtain attribute (column) descriptor from the selected cell. Check data consistency.", 
							"No column descriptor", 
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// extract the prm attribute value to delete
				IAttributeValue cellDescriptor = rowDescriptor.getAttributeValueMap().get(columnDescriptor);
				
				// delete cell from attribute
				columnDescriptor.getAttributeValues().remove(cellDescriptor);
				
				// we do not need to delete value from object, since object will be deleted
				
				// delete object from class
				rowDescriptor.getPRMClass().getPRMObjects().remove(rowDescriptor);
				
				// update view
				resetComponents();
			}
		});
		
		// compile PRM button
		this.getCompilePRMButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// assertion
				if (getObjectDataTable().getSelectedRow() < 0) {
					// nothing selected
					JOptionPane.showMessageDialog(
							ObjectPanel.this, 
							"Select a cell as a compilation pivot.", 
							"No selection", 
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				// extract the prm object to compile
				IPRMObject rowDescriptor = getPrmClass().getPRMObjects().get(getObjectDataTable().getSelectedRow());
				if (rowDescriptor  == null) {
					JOptionPane.showMessageDialog(
							ObjectPanel.this, 
							"Could not obtain object (row) descriptor from the selected cell. Check data consistency.", 
							"No row descriptor", 
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// extract the prm attribute to compile
				IAttributeDescriptor columnDescriptor = 
								getPrmClass().findAttributeDescriptorByName(
											getObjectDataTable().getColumnModel().getColumn(getObjectDataTable().getSelectedColumn()).getHeaderValue().toString());
				if (columnDescriptor == null) {
					JOptionPane.showMessageDialog(
							ObjectPanel.this, 
							"Could not obtain attribute (column) descriptor from the selected cell. Check data consistency.", 
							"No column descriptor", 
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// extract the prm attribute value to compile
				IAttributeValue cellDescriptor = rowDescriptor.getAttributeValueMap().get(columnDescriptor);
				
				// check cell existence
				if (cellDescriptor ==  null) {
					JOptionPane.showMessageDialog(
							ObjectPanel.this, 
							"Could not obtain value (cell) descriptor from the selected cell. Check data consistency.", 
							"No cell descriptor", 
							JOptionPane.ERROR_MESSAGE);
					return;
				} 
				
				// check if selected cell is evidence
				if (cellDescriptor.getValue() != null && (cellDescriptor.getValue().length() > 0)) {
					// there is a value: this is not a random variable anymore, so, no compilation is needed to know its value
					JOptionPane.showMessageDialog(
							ObjectPanel.this, 
							"This cell is an evidence, and its known value is: \"" + cellDescriptor.getValue() + "\"", 
							"No compilation needed", 
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				
				// check if it is mandatory
				if (columnDescriptor.isMandatory()) {
					// there must be a value: this is not a random variable, so, no compilation is needed.
					JOptionPane.showMessageDialog(
							ObjectPanel.this, 
							"This is a mandatory field (thus, it is not going to become a random variable). Select another cell.", 
							"Mandatory column", 
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				// random FK is not supported by ALPHA version
				if (columnDescriptor.isForeignKey()) {
					JOptionPane.showMessageDialog(
							ObjectPanel.this, 
							"Random foreign key is not supported by ALPHA version.", 
							"ALPHA restriction", 
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				// prepare compilation query
				Collection<IAttributeValue> query = new ArrayList<IAttributeValue>();
				query.add(cellDescriptor);
				Graph compiledPRM = null;
				try {
					compiledPRM = getUpperPanel().getController().compilePRM(getPrmClass().getPRM(), query);
					// reuse inference algorithm if 
				} catch (Exception exc) {
					exc.printStackTrace();
					JOptionPane.showMessageDialog(
							ObjectPanel.this, 
							exc.getMessage(), 
							"Compile error", 
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if ((compiledPRM != null) && (compiledPRM instanceof Network)) {
					NetworkWindow netWindow = new NetworkWindow((Network)compiledPRM);
					if (getUpperPanel().getPrmWindow().getDesktopPane() instanceof MDIDesktopPane) {
						((MDIDesktopPane)getUpperPanel().getPrmWindow().getDesktopPane()).add(netWindow);
					} else {
						getUpperPanel().getPrmWindow().getDesktopPane().add(netWindow);
					}
					
					netWindow.setVisible(true);
					
					// show the already compiled BN, if possible
					try {
						// reuse the same inference algorithm used by the controller to compile PRM
						if (getUpperPanel().getController() instanceof IBNInferenceAlgorithmHolder) {
							if (((IBNInferenceAlgorithmHolder)getUpperPanel().getController()).getBNInferenceAlgorithm() != null) {
								netWindow.getController().setInferenceAlgorithm(((IBNInferenceAlgorithmHolder)getUpperPanel().getController()).getBNInferenceAlgorithm());
							}
						}
						// go directly to the compiled pane
						netWindow.changeToPNCompilationPane();
					} catch (Exception exc) {
						// we can still show the BN without the findings, so, ignore exception
						exc.printStackTrace();
					}
					
					// update desktop pane
					getUpperPanel().getPrmWindow().getDesktopPane().updateUI();
					getUpperPanel().getPrmWindow().getDesktopPane().repaint();
				} else {
					JOptionPane.showMessageDialog(
							ObjectPanel.this, 
							"The compiled PRM does not form a consistent network class format. Check PRM compilation component.", 
							"Compiled PRM format error", 
							JOptionPane.ERROR_MESSAGE);
				}
				
				// update view
				resetComponents();
			}
		});
		
	}

	/**
	 * @return the upperPanel
	 */
	public SkeletonPanel getUpperPanel() {
		return upperPanel;
	}

	/**
	 * @param upperPanel the upperPanel to set
	 */
	public void setUpperPanel(SkeletonPanel upperPanel) {
		this.upperPanel = upperPanel;
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
	 * @return the objectDataTable
	 */
	public JTable getObjectDataTable() {
		return objectDataTable;
	}

	/**
	 * @param objectDataTable the objectDataTable to set
	 */
	public void setObjectDataTable(JTable objectDataTable) {
		this.objectDataTable = objectDataTable;
	}

	/**
	 * @return the objectDataTableScrollPane
	 */
	public JScrollPane getObjectDataTableScrollPane() {
		return objectDataTableScrollPane;
	}

	/**
	 * @param objectDataTableScrollPane the objectDataTableScrollPane to set
	 */
	public void setObjectDataTableScrollPane(JScrollPane objectDataTableScrollPane) {
		this.objectDataTableScrollPane = objectDataTableScrollPane;
	}

	/**
	 * @return the objectDataToolBar
	 */
	public JToolBar getObjectDataToolBar() {
		return objectDataToolBar;
	}

	/**
	 * @param objectDataToolBar the objectDataToolBar to set
	 */
	public void setObjectDataToolBar(JToolBar objectDataToolBar) {
		this.objectDataToolBar = objectDataToolBar;
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
	 * @return the addObjectButton
	 */
	public JButton getAddObjectButton() {
		return addObjectButton;
	}

	/**
	 * @param addObjectButton the addObjectButton to set
	 */
	public void setAddObjectButton(JButton addObjectButton) {
		this.addObjectButton = addObjectButton;
	}

	/**
	 * @return the removeObjectButton
	 */
	public JButton getRemoveObjectButton() {
		return removeObjectButton;
	}

	/**
	 * @param removeObjectButton the removeObjectButton to set
	 */
	public void setRemoveObjectButton(JButton removeObjectButton) {
		this.removeObjectButton = removeObjectButton;
	}

	/**
	 * @return the compilePRMButton
	 */
	public JButton getCompilePRMButton() {
		return compilePRMButton;
	}

	/**
	 * @param compilePRMButton the compilePRMButton to set
	 */
	public void setCompilePRMButton(JButton compilePRMButton) {
		this.compilePRMButton = compilePRMButton;
	}

}
