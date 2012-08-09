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
import javax.swing.JCheckBox;
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
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.prm.AttributeValue;
import unbbayes.prs.prm.IAttributeDescriptor;
import unbbayes.prs.prm.IAttributeValue;
import unbbayes.prs.prm.IPRMClass;
import unbbayes.prs.prm.IPRMObject;
import unbbayes.prs.prm.PRMObject;
import unbbayes.prs.prm.compiler.IBNInferenceAlgorithmHolder;

/**
 * A panel basically containing a table to edit attribute values (object values,
 * i.e. rows of PRM tables)
 * 
 * @author Shou Matsumoto
 * 
 */
public class ObjectPanel extends JPanel {

	private static final int COLOMUM_MIN_WIDTH = 100;
	private static final int BORDER_VGAP = 20;
	private static final int BORDER_HGAP = 10;
	private SkeletonPanel upperPanel;
	private IPRMClass prmClass;
	private JTable objectDataTable;
	private JScrollPane objectDataTableScrollPane;
	private JToolBar objectDataToolBar;

	private IconController iconController;
	private JButton addObjectButton;
	private JButton removeObjectButton;
	private JButton compilePRMButton;
	private JCheckBox showBNCheckBox;

	/**
	 * At least one constructor must be visible for subclasses to allow
	 * inheritance;
	 */
	protected ObjectPanel() {
		// Load a icon controller from core.
		this.iconController = IconController.getInstance();
	}

	/**
	 * Default construction method
	 * 
	 * @param upperPanel
	 *            : SkeletonPanel containing this panel
	 * @param prmClass
	 *            : class containing objects to be rendered
	 * @return an instance of ObjectPanel
	 */
	public static ObjectPanel newInstance(SkeletonPanel upperPanel,
			IPRMClass prmClass) {
		ObjectPanel ret = new ObjectPanel();
		ret.upperPanel = upperPanel;
		ret.prmClass = prmClass;
		ret.initComponents();
		ret.initListeners();
		return ret;
	}

	/**
	 * Remove all components and calls {@link #initComponents()} and then
	 * {@link #initListeners()}
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

		this.setLayout(new BorderLayout(BORDER_HGAP, BORDER_VGAP));

		// Tool bar to edit objects
		objectDataToolBar = new JToolBar("Edit objects");
		this.add(objectDataToolBar, BorderLayout.NORTH);

		// Button to add object
		addObjectButton = (new JButton(iconController.getMoreIcon()));
		addObjectButton.setToolTipText("Add a new entry (PRM object/row).");
		objectDataToolBar.add(addObjectButton);

		// Button to remove selected object
		removeObjectButton = (new JButton(iconController.getLessIcon()));
		removeObjectButton
				.setToolTipText("Remove selected entry (PRM object/row).");
		objectDataToolBar.add(removeObjectButton);

		// Separator for object edition tool bar
		objectDataToolBar.addSeparator();

		// Button to compile PRM using selected cell value
		compilePRMButton = (new JButton(iconController.getCompileIcon()));
		compilePRMButton.setToolTipText("Compile PRM using selected cell.");
		objectDataToolBar.add(compilePRMButton);

		// Checkbox to show BN after compilation or just show a text message
		// create new checkbox, but reuse the last one if it exists. Default =
		// true
		showBNCheckBox = (new JCheckBox("Show generated BN",
				((showBNCheckBox != null) ? (showBNCheckBox.isSelected())
						: (true))));
		showBNCheckBox
				.setToolTipText("Display the generated Bayesian Network after compilation. If unchecked, only a message will be displayed");
		objectDataToolBar.add(showBNCheckBox);

		// build header (name of attributes) of table
		String[] columns = new String[prmClass.getAttributeDescriptors().size()];
		for (int i = 0; i < prmClass.getAttributeDescriptors().size(); i++) {
			columns[i] = prmClass.getAttributeDescriptors().get(i).getName();
		}

		// build data (actual values) of table
		String[][] data = new String[prmClass.getPRMObjects().size()][columns.length];
		if (data.length != 0 && columns.length != 0) {
			// there is at least one cell, so, render them all
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < columns.length; j++) { // note that
															// data[0].length ==
															// columns.length
					IAttributeValue val = prmClass
							.getPRMObjects()
							.get(i)
							.getAttributeValueMap()
							.get(prmClass
									.findAttributeDescriptorByName(columns[j]));
					data[i][j] = (val != null) ? val.getValue() : null;
				}
			}
		}

		// The jtable itself
		objectDataTable = new JTable(data, columns);

		// enable single cell selection
		objectDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		objectDataTable.setColumnSelectionAllowed(false);
		objectDataTable.setRowSelectionAllowed(false);
		objectDataTable.setCellSelectionEnabled(true);

		// scroll pane containing the object's table
		objectDataTableScrollPane = new JScrollPane(this.objectDataTable);
		this.add(objectDataTableScrollPane, BorderLayout.CENTER);

		// Change header colors
		// objectDataTable.getTableHeader().setBackground(Color.YELLOW);
		objectDataTable.getTableHeader().setForeground(Color.BLACK);
		objectDataTable.getTableHeader().setFont(
				new Font("SansSerif", Font.BOLD, 12));
		objectDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// Set minimum column size
		for (int i = 0; i < objectDataTable.getColumnModel().getColumnCount(); i++) {
			objectDataTable.getColumnModel().getColumn(i)
					.setMinWidth(COLOMUM_MIN_WIDTH);
		}
	}

	/**
	 * Initialize listeners
	 */
	protected void initListeners() {

		// add/update value when updating table
		objectDataTable.getModel().addTableModelListener(
				new TableModelListener() {
					public void tableChanged(TableModelEvent e) {

						// this is the PRM representation of a cell.
						// Lets fill it after extracting the descriptors of
						// column (prm attribute) and row (prm object)
						IAttributeValue cellDescriptor = null;

						// Change the PRM cell or reset to its previous value.
						try {
							// Extract the PRM attribute to fill
							IAttributeDescriptor columnDescriptor = prmClass
									.findAttributeDescriptorByName(objectDataTable
											.getColumnModel()
											.getColumn(e.getColumn())
											.getHeaderValue().toString());
							if (columnDescriptor == null) {
								JOptionPane
										.showMessageDialog(
												ObjectPanel.this,
												"Could not obtain attribute (column) descriptor from the selected cell. Check data consistency.",
												"No column descriptor",
												JOptionPane.ERROR_MESSAGE);
								return;
							}

							// Extract the PRM object to fill
							IPRMObject rowDescriptor = prmClass.getPRMObjects()
									.get(e.getLastRow());
							if (rowDescriptor == null) {
								JOptionPane
										.showMessageDialog(
												ObjectPanel.this,
												"Could not obtain object (row) descriptor from the selected cell. Check data consistency.",
												"No row descriptor",
												JOptionPane.ERROR_MESSAGE);
								return;
							}

							// Extract the PRM attribute value to update (if it
							// exists)
							cellDescriptor = rowDescriptor
									.getAttributeValueMap().get(
											columnDescriptor);

							// Extract value from JTable
							String valueText = objectDataTable.getValueAt(
									e.getLastRow(), e.getColumn()).toString();
							if (valueText != null && valueText.length() <= 0) {
								// if string is empty, use null instead of empty
								// string.
								// I dont want to use trim because space may be
								// important for a user...
								valueText = null;
							}

							// Create new cell descriptor, since it does not
							// exist
							if (cellDescriptor == null) {
								cellDescriptor = AttributeValue.newInstance(
										rowDescriptor, columnDescriptor);
							}

							// just update cell value, because it already exists
							cellDescriptor.setValue(valueText);

						} catch (Exception exc) {
							exc.printStackTrace();
							JOptionPane.showMessageDialog(ObjectPanel.this,
									exc.getMessage(),
									"Exception updating PRM object",
									JOptionPane.ERROR_MESSAGE);
							objectDataTable.revalidate();
							if (cellDescriptor != null) {
								// Revert value
								objectDataTable.setValueAt(
										cellDescriptor.getValue(),
										e.getLastRow(), e.getColumn());
							}
						}
					}
				});

		// Add new object button.
		addObjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PRMObject.newInstance(prmClass); // also automatically
													// associates with
													// getPrmClass

				// Update
				resetComponents();
			}
		});

		// Remove selected object button
		removeObjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// Assertion
				if (objectDataTable.getSelectedRow() < 0) {
					// Nothing selected
					return;
				}

				// Extract the PRM object to delete.
				IPRMObject rowDescriptor = prmClass.getPRMObjects().get(
						objectDataTable.getSelectedRow());
				if (rowDescriptor == null) {
					JOptionPane
							.showMessageDialog(
									ObjectPanel.this,
									"Could not obtain object (row) descriptor from the selected cell. Check data consistency.",
									"No row descriptor",
									JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Extract the prm attribute to delete value from
				IAttributeDescriptor columnDescriptor = prmClass
						.findAttributeDescriptorByName(objectDataTable
								.getColumnModel()
								.getColumn(objectDataTable.getSelectedColumn())
								.getHeaderValue().toString());
				if (columnDescriptor == null) {
					JOptionPane
							.showMessageDialog(
									ObjectPanel.this,
									"Could not obtain attribute (column) descriptor from the selected cell. Check data consistency.",
									"No column descriptor",
									JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Extract the prm attribute value to delete
				IAttributeValue cellDescriptor = rowDescriptor
						.getAttributeValueMap().get(columnDescriptor);

				// Delete cell from attribute
				columnDescriptor.getAttributeValues().remove(cellDescriptor);

				// We do not need to delete value from object, since object will
				// be deleted

				// Delete object from class
				rowDescriptor.getPRMClass().getPRMObjects()
						.remove(rowDescriptor);

				// Update view
				resetComponents();
			}
		});

		// Compile PRM button
		compilePRMButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// ////////// Assertion ////////////
				// If any cell is selected.
				if (objectDataTable.getSelectedRow() < 0) {
					JOptionPane.showMessageDialog(ObjectPanel.this,
							"Select a cell as a compilation pivot.",
							"No selection", JOptionPane.WARNING_MESSAGE);
					return;
				}

				// Extract the PRM object from selected row.
				IPRMObject rowDescriptor = prmClass.getPRMObjects().get(
						objectDataTable.getSelectedRow());
				// Error extracting PRM Object
				if (rowDescriptor == null) {
					JOptionPane
							.showMessageDialog(
									ObjectPanel.this,
									"Could not obtain object (row) descriptor from the selected cell. Check data consistency.",
									"No row descriptor",
									JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Column name of the selected cell.
				String columName = objectDataTable.getColumnModel()
						.getColumn(objectDataTable.getSelectedColumn())
						.getHeaderValue().toString();

				// Extract the PRM attribute to compile.
				IAttributeDescriptor columnDescriptor = prmClass
						.findAttributeDescriptorByName(columName);
				// Error extracting RPM attribute.
				if (columnDescriptor == null) {
					JOptionPane
							.showMessageDialog(
									ObjectPanel.this,
									"Could not obtain attribute (column) descriptor from the selected cell. Check data consistency.",
									"No column descriptor",
									JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Extract the PRM attribute value to compile.
				IAttributeValue cellDescriptor = rowDescriptor
						.getAttributeValueMap().get(columnDescriptor);

				// Check cell existence
				if (cellDescriptor == null) {
					JOptionPane
							.showMessageDialog(
									ObjectPanel.this,
									"Could not obtain value (cell) descriptor from the selected cell. Check data consistency.",
									"No cell descriptor",
									JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Check if selected cell is evidence
				if (cellDescriptor.getValue() != null
						&& (cellDescriptor.getValue().length() > 0)) {
					// There is a value: this is not a random variable anymore,
					// so, no compilation is needed to know its value
					JOptionPane.showMessageDialog(ObjectPanel.this,
							"This cell is an evidence, and its known value is: \""
									+ cellDescriptor.getValue() + "\"",
							"No compilation needed",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				// Check if it is mandatory
				if (columnDescriptor.isMandatory()) {
					// there must be a value: this is not a random variable, so,
					// no compilation is needed.
					JOptionPane
							.showMessageDialog(
									ObjectPanel.this,
									"This is a mandatory field (thus, it is not going to become a random variable). Select another cell.",
									"Mandatory column",
									JOptionPane.WARNING_MESSAGE);
					return;
				}

				// Random FK is not supported by ALPHA version
				if (columnDescriptor.isForeignKey()) {
					JOptionPane
							.showMessageDialog(
									ObjectPanel.this,
									"Random foreign key is not supported by ALPHA version.",
									"ALPHA restriction",
									JOptionPane.WARNING_MESSAGE);
					return;
				}

				// Prepare compilation query
				Collection<IAttributeValue> query = new ArrayList<IAttributeValue>();
				query.add(cellDescriptor);
				Graph compiledPRM = null;

				// ///// COMPILE ////////
				try {
					compiledPRM = upperPanel.getController().compilePRM(
							prmClass.getPRM(), query);
					// reuse inference algorithm if
				} catch (Exception exc) {
					exc.printStackTrace();
					JOptionPane.showMessageDialog(ObjectPanel.this,
							exc.getMessage(), "Compile error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				// If the BN might be showed.
				if (!showBNCheckBox.isSelected()) {
					// just show a simple message
					try {
						// generate a message containing the probabilities of
						// the queried node (cell)
						// assume the name of the nodes in compiledPRM is
						// IAttributeValue.toString();
						// TODO find a better way to retrieve the queried node,
						// instead of using naming conversion
						TreeVariable result = (TreeVariable) ((Network) compiledPRM)
								.getNode(cellDescriptor.toString());
						String msg = result.getName() + ": \n";
						for (int i = 0; i < result.getStatesSize(); i++) {
							msg += "\t" + result.getStateAt(i) + " = "
									+ result.getMarginalAt(i) + ";\n";
						}
						JOptionPane.showMessageDialog(ObjectPanel.this, msg,
								"Probability of the queried cell",
								JOptionPane.INFORMATION_MESSAGE);
					} catch (Exception e2) {
						e2.printStackTrace();
						JOptionPane
								.showMessageDialog(
										ObjectPanel.this,
										"Could not obtain the query node from generated BN. Try checking the \"show BN\" checkbox.",
										e2.getMessage(),
										JOptionPane.ERROR_MESSAGE);
					}
					
				// Show the created BN
				} else if ((compiledPRM != null)
						&& (compiledPRM instanceof Network)) {
					// Display generated BN
					NetworkWindow netWindow = new NetworkWindow(
							(Network) compiledPRM);
					if (upperPanel.getPrmWindow().getDesktopPane() instanceof MDIDesktopPane) {
						((MDIDesktopPane) upperPanel.getPrmWindow()
								.getDesktopPane()).add(netWindow);
					} else {
						upperPanel.getPrmWindow().getDesktopPane()
								.add(netWindow);
					}

					netWindow.setVisible(true);

					// Show the already compiled BN, if possible
					try {
						// Reuse the same inference algorithm used by the
						// controller to compile PRM.
						if (upperPanel.getController() instanceof IBNInferenceAlgorithmHolder) {
							if (((IBNInferenceAlgorithmHolder) upperPanel
									.getController()).getBNInferenceAlgorithm() != null) {
								netWindow
										.getController()
										.setInferenceAlgorithm(
												((IBNInferenceAlgorithmHolder) upperPanel
														.getController())
														.getBNInferenceAlgorithm());
							}
						}
						// go directly to the compiled pane
						netWindow.changeToPNCompilationPane();
					} catch (Exception exc) {
						// we can still show the BN without the findings, so,
						// ignore exception
						exc.printStackTrace();
					}

					// update desktop pane
					upperPanel.getPrmWindow().getDesktopPane().updateUI();
					upperPanel.getPrmWindow().getDesktopPane().repaint();
				} else {
					////////////// ERROR ////////////
					JOptionPane
							.showMessageDialog(
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

}
