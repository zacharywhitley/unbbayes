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
package unbbayes.datamining.gui.datamanipulation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import unbbayes.datamining.datamanipulation.ArffLoader;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Loader;
import unbbayes.datamining.datamanipulation.TxtLoader;
import unbbayes.datamining.gui.UtilsGUI;
import unbbayes.datamining.gui.UtilsGUI.CheckBoxEditor;
import unbbayes.datamining.gui.UtilsGUI.ComboBoxEditor;
import unbbayes.datamining.gui.UtilsGUI.ComboBoxRenderer;
import unbbayes.datamining.gui.UtilsGUI.EachRowEditor;
import unbbayes.datamining.gui.UtilsGUI.EachRowRenderer;
import unbbayes.datamining.gui.UtilsGUI.RadioButtonEditor;
import unbbayes.datamining.gui.UtilsGUI.RadioButtonRenderer;
import unbbayes.datamining.gui.UtilsGUI.RowHeaderRenderer;

/**
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 22/11/2006
 */
public class AttributeTypeChooserController {
	/** Load resource file for this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.datamining.gui.datamanipulation.resources.AttributeTypeChooserResource.class.getName());

	/**
	 * Stores the type of an attribute: 0 - Numeric 1 - Nominal 2 - Cyclic
	 * numeric
	 */
	private byte[] attributeType;

	/** Number of attributes */
	private int numAttributes;

	/**
	 * The index of the dataset's column that represents the counter attribute.
	 * Assumes always the last column of the internal dataset as the counter
	 * attribute.
	 */
	private int counterIndex = -1;

	/** Tells that the attributes' content are all String values */
	private boolean[] attributeIsString;

	private StreamTokenizer tokenizer;

	private Object[][] instances;

	/** Desired number of instances read from the file */
	private int numInstancesAux;

	private String[] attributeName;

	private InstanceSet instanceSet;

	private boolean[] counterIndexAux;

	private Hashtable<Object, Object> attTypes;

	private Loader loader;

	private Object[][] chooserData;

	private byte[] attributeTypeOriginal;
	
	private UtilsGUI utilsGUI = new UtilsGUI();

	public AttributeTypeChooserController(File file, Component parent)
			throws Exception {
		/* Set the desired number of instances read from the file */
		numInstancesAux = 100;

		run(file, parent);
	}

	private void run(File file, Component parent) throws Exception {
		/* Build the principal panel */
		JPanel principalPanel = buildWindow(file);

		/* Show window */
		if ((JOptionPane.showInternalConfirmDialog(parent, principalPanel,
				resource.getString("windowTitle"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)) {

			int size = numAttributes;

			/* First, check if the counter index has been set */
			InstanceSet instanceSet = loader.getInstanceSet();
			String counterAttributeName = instanceSet.getCounterAttributeName();
			if (counterIndex != -1) {
				--numAttributes;
				counterAttributeName = attributeName[counterIndex];
			}

			attributeType = new byte[numAttributes];
			attributeIsString = new boolean[numAttributes];

			/* Set the attribute's characteristics */
			int attIndex = 0;
			String[] attributeNameAux = new String[numAttributes];
			for (int att = 0; att < size; att++) {
				if (att != counterIndex) {
					attributeType[attIndex] = (Byte) attTypes
							.get(chooserData[0][att]);
					attributeIsString[attIndex] = (Boolean) chooserData[1][att];
					attributeNameAux[attIndex] = attributeName[att];
					++attIndex;
				}
			}
			attributeName = attributeNameAux;

			if (loader instanceof TxtLoader) {
				loader = new TxtLoader(loader.file, -1);
			} else {
				loader = new ArffLoader(loader.file, -1);
			}

			/* Set counter attribute */
			loader.setAttributeIsString(attributeIsString);
			loader.setAttributeType(attributeType);
			loader.setCounterAttribute(counterIndex);
			loader.setCounterAttributeName(counterAttributeName);
			loader.setAttributeName(attributeName);
			if (counterIndex != -1) {
				loader.setCompacted(true);
			}
			loader.setNumAttributes(numAttributes);

			if (loader instanceof ArffLoader) {
				/*
				 * In the case the user has changed the header information from
				 * a nominal attribute to a numeric or cyclic one, it's
				 * necessary to build the attribute from the dataset. Otherwise,
				 * the attribute must be built from the header information of
				 * the Arff file.
				 */
				boolean[] buildNominalFromHeader = new boolean[numAttributes];
				for (int att = 0; att < numAttributes; att++) {
					if (attributeTypeOriginal[att] == InstanceSet.NOMINAL
							&& attributeType[att] == InstanceSet.NOMINAL) {
						buildNominalFromHeader[att] = true;
					} else {
						buildNominalFromHeader[att] = false;
					}
				}

				((ArffLoader) loader).buildAttributes(buildNominalFromHeader,
						instanceSet.attributes);
			}
		} else {
			/* Canceled by the user */
			loader = null;
		}
	}

	private JPanel buildWindow(File file) throws Exception {
		/* Build a preliminary header and read a sample of the file */
		getPreliminaries(file);

		/* Build the data table */
		JTable dataTable = new JTable(instances, attributeName) {
			private static final long serialVersionUID = 1L;

			/* Make the table impossible for edition */
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		dataTable.getTableHeader().setReorderingAllowed(false);
		dataTable.getTableHeader().setResizingAllowed(false);
		dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		/* Build scroll pane for the data table */
		JScrollPane dataTableScroll = new JScrollPane(dataTable);
		dataTableScroll.setHorizontalScrollBarPolicy(JScrollPane
				.HORIZONTAL_SCROLLBAR_ALWAYS);

		/* Build the chooser table */
		JTable chooserTable = buildChooserTable();
		chooserTable.getTableHeader().setReorderingAllowed(false);
		chooserTable.getTableHeader().setResizingAllowed(false);

		/* Build the row header of the chooser table */
		final String[] chooserTableRowHeaderData;
		chooserTableRowHeaderData = new String[3];
		chooserTableRowHeaderData[0] = resource.getString("attributeType")
				+ "  ";
		chooserTableRowHeaderData[1] = resource.getString("isString") + "  ";
		chooserTableRowHeaderData[2] = resource.getString("counter") + "  ";
		JList chooserTableRowHeader = new JList(chooserTableRowHeaderData);
		chooserTableRowHeader.setFixedCellWidth(150);
		chooserTableRowHeader.setFixedCellHeight(dataTable.getRowHeight());
		chooserTableRowHeader.setCellRenderer(utilsGUI.new RowHeaderRenderer(
				chooserTable, true));

		/*
		 * Build a dummy row header for the data table. It provides horizontal
		 * alignment of the data table and the chooser table.
		 */
		int numLines = dataTable.getRowCount();
		final String[] dummyRowHeaderData = new String[numLines];
		for (int i = 0; i < numLines; i++) {
			dummyRowHeaderData[i] = "";
		}
		JList dataTableRowHeader = new JList(dummyRowHeaderData);
		dataTableRowHeader.setFixedCellWidth(chooserTableRowHeader
				.getFixedCellWidth());
		dataTableRowHeader.setFixedCellHeight(dataTable.getRowHeight());
		dataTableRowHeader.setCellRenderer(utilsGUI.new RowHeaderRenderer(
				chooserTable, false));
		dataTableScroll.setRowHeaderView(dataTableRowHeader);

		/* Build scroll pane for the chooser table and get rid of its header */
		JScrollPane chooserScroll = new JScrollPane(chooserTable) {
			private static final long serialVersionUID = 1L;

			public void setColumnHeaderView(Component view) {
			}
		};
		chooserScroll.setRowHeaderView(chooserTableRowHeader);
		chooserScroll.setVerticalScrollBarPolicy(JScrollPane
				.VERTICAL_SCROLLBAR_ALWAYS);
		chooserScroll.setHorizontalScrollBarPolicy(JScrollPane
				.HORIZONTAL_SCROLLBAR_NEVER);

		/* Get rid of the vertical bar of the chooser table */
		chooserScroll.getVerticalScrollBar().setEnabled(false);

		/*
		 * Bind the horizontal bar of the chooser table and data table together
		 * so when you move the data table horizontal bar the chooser table also
		 * moves.
		 */
		JScrollBar bar1 = dataTableScroll.getHorizontalScrollBar();
		final JScrollBar bar2 = chooserScroll.getHorizontalScrollBar();
		bar1.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				bar2.setValue(e.getValue());
			}
		});

		/* Set the window's size */
		chooserScroll.setPreferredSize(new Dimension(500, 50));
		dataTableScroll.setPreferredSize(new Dimension(500, 150));

		/* Get rid of the borders */
		chooserScroll.setBorder(new EmptyBorder(dataTableScroll.getInsets()));
		dataTableScroll.setBorder(new EmptyBorder(dataTableScroll.getInsets()));

		/* Build the principal panel */
		JPanel principalPanel = new JPanel();
		principalPanel.setLayout(new BorderLayout());
		principalPanel.add(chooserScroll, BorderLayout.CENTER);
		principalPanel.add(dataTableScroll, BorderLayout.SOUTH);

		return principalPanel;
	}

	private JTable buildChooserTable() {
		/* Prepare combo box first */
		attTypes = new Hashtable<Object, Object>();
		attTypes.put("numeric", (byte) 0);
		attTypes.put("nominal", (byte) 1);
		attTypes.put("cyclic", (byte) 2);
		attTypes.put((byte) 0, "numeric");
		attTypes.put((byte) 1, "nominal");
		attTypes.put((byte) 2, "cyclic");
		String[] comboValues = {
				(String) attTypes.get((byte) 0),
				(String) attTypes.get((byte) 1),
				(String) attTypes.get((byte) 2),
		};

		/* First, we build the recipient table */
		AbstractTableModel chooserModel = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			public int getColumnCount() {
				return numAttributes;
			}

			public int getRowCount() {
				return 3;
			}

			public Object getValueAt(int row, int col) {
				return chooserData[row][col];
			}

			public void setValueAt(Object value, int row, int col) {
				int last = counterIndex;

				if (row == 2) {
					if (col == counterIndex) {
						/* Deselect the radio button */
						counterIndex = -1;
						chooserData[row][col] = false;
					} else {
						if (counterIndex != -1) {
							chooserData[row][counterIndex] = false;
						}
						chooserData[row][col] = true;
						counterIndex = col;
					}
				} else {
					chooserData[row][col] = value;
					if (row == 0 && value != "nominal") {
						setValueAt(new Boolean(false), 1, col);
					}
				}

				fireTableCellUpdated(row, col);
				if (counterIndex != -1) {
					fireTableCellUpdated(row, last);
				}
			}

			public boolean isCellEditable(int row, int col) {
				if (row == 1) {
					if (chooserData[0][col] == "nominal") {
						return true;
					} else {
						return false;
					}
				}

				return true;
			}
		};

		/* Add components to the chooser data model */
		chooserData = new Object[3][numAttributes];
		for (int att = 0; att < numAttributes; att++) {
			chooserData[0][att] = (String) attTypes.get(attributeType[att]);
			chooserData[1][att] = attributeIsString[att];
			chooserData[2][att] = counterIndexAux[att];
		}

		JTable chooserTable = new JTable(chooserModel);
		TableColumnModel columnModel = chooserTable.getColumnModel();
		JCheckBox checkBox = new JCheckBox();
		checkBox.setHorizontalAlignment(JCheckBox.CENTER);
		for (int att = 0; att < numAttributes; att++) {
			/* Next, we set the editor used in this table */
			EachRowEditor rowEditor = utilsGUI.new EachRowEditor(chooserTable);
			EachRowRenderer rowRenderer = utilsGUI.new EachRowRenderer();

			rowEditor.setEditorAt(0, utilsGUI.new ComboBoxEditor(comboValues));
			rowRenderer.add(0, utilsGUI.new ComboBoxRenderer(comboValues));

			rowEditor.setEditorAt(1, new CheckBoxEditor());
			rowRenderer.add(1, new CheckBoxRenderer());

			rowEditor.setEditorAt(2, utilsGUI.new RadioButtonEditor());
			rowRenderer.add(2, utilsGUI.new RadioButtonRenderer());

			/* Set the cell renderer and editor for the current column */
			columnModel.getColumn(att).setCellEditor(rowEditor);
			columnModel.getColumn(att).setCellRenderer(rowRenderer);
		}

		chooserTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		chooserTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		return chooserTable;
	}

	private void getPreliminaries(File file) throws Exception {
		String fileName = file.getName();

		/* Constructs a preliminary header */
		if (fileName.regionMatches(true, fileName.length() - 5, ".arff", 0, 5)) {
			/* Read the arff header */
			loader = new ArffLoader(file, numInstancesAux);
		} else if (fileName.regionMatches(true, fileName.length() - 4, ".txt",
				0, 4)) {
			/*
			 * Read header of the txt file. All attributes will be read as
			 * nominal (String values)
			 */
			loader = new TxtLoader(file, numInstancesAux);
		} else {
			throw new IOException(resource.getString("fileExtensionException"));
		}

		/* Build the loader header */
		loader.buildHeader();

		if (loader.getnumInitialInstances() < numInstancesAux) {
			numInstancesAux = loader.getnumInitialInstances();
		}
		
		/* Read the first 'numInstancesAux' instances from 'file' */
		boolean reading = true;
		int i = 0;
		while (reading && i < numInstancesAux) {
			reading = loader.getInstance();
			i++;
		}
		instanceSet = loader.getInstanceSet();
		counterIndex = loader.getLikelyCounterIndex();

		/* Set the initial attributes' characteristics */
		numAttributes = instanceSet.numAttributes;
		attributeType = instanceSet.attributeType;
		attributeTypeOriginal = attributeType.clone();

		/* Get columns' labels and 'isString' vector */
		attributeName = new String[numAttributes];
		attributeIsString = new boolean[numAttributes];
		for (int att = 0; att < numAttributes; att++) {
			attributeName[att] = instanceSet.getAttribute(att)
					.getAttributeName();
			attributeIsString[att] = instanceSet.getAttribute(att).isString();
		}

		/*
		 * Create the data table with the first 'numInstancesAux' instances read
		 * from the file.
		 */
		Instance instance;
		instances = new Object[numInstancesAux][numAttributes];
		for (int inst = 0; inst < numInstancesAux; inst++) {
			instance = instanceSet.instances[inst];
			for (int att = 0; att < numAttributes; att++) {
				instances[inst][att] = instance.stringValue(att);
			}
		}

		/*
		 * If the file is an txt file, analize the sample and check which
		 * attributes contains at least one String value.
		 */
		if (loader instanceof TxtLoader) {
			attributeIsString = new boolean[numAttributes];
			attributeType = new byte[numAttributes];

			for (int att = 0; att < numAttributes; att++) {
				attributeIsString[att] = false;
				attributeType[att] = 0;
				for (int inst = 0; inst < numInstancesAux; inst++) {
					instance = instanceSet.instances[inst];
					try {
						Float.parseFloat(instance.stringValue(att));
					} catch (Exception e) {
						attributeIsString[att] = true;
						attributeType[att] = 1;
						break;
					}
				}
			}
		}

		/* Set counterIndexAux and counter stuff */
		counterIndexAux = new boolean[numAttributes];
		for (int att = 0; att < numAttributes; att++) {
			counterIndexAux[att] = false;
			if (att == counterIndex) {
				counterIndexAux[att] = true;
			}
		}
		if (counterIndex != -1 && !attributeIsString[counterIndex]) {
			attributeType[counterIndex] = InstanceSet.NUMERIC;
		}
	}

	/**
	 * Initializes the StreamTokenizer used for reading the TXT file.
	 * 
	 * @param tokenizer
	 *            Stream tokenizer
	 */
	protected void initTokenizer() {
		tokenizer.resetSyntax();
		tokenizer.wordChars(' ' + 1, '\u00FF');
		tokenizer.wordChars('_', '_');
		tokenizer.wordChars('-', '-');
		tokenizer.whitespaceChars(0, ' ');
		tokenizer.whitespaceChars('\t', '\t');
		tokenizer.commentChar('%');
		tokenizer.quoteChar('"');
		tokenizer.eolIsSignificant(true);
		tokenizer.parseNumbers();
	}

	public Loader getLoader() {
		return loader;
	}

	/**
	 * This is a specific checkbox renderer which disables the string option of
	 * an attribute when this attribute is numeric.
	 * @author Emerson
	 *
	 */
	private class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

		private static final long serialVersionUID = -9005292408131451196L;

		public CheckBoxRenderer() {
			setHorizontalAlignment(JCheckBox.CENTER);
		}

		public Component getTableCellRendererComponent(JTable table, 
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			/* Disable string checkbox when attribute is numeric */
			if (chooserData[0][column] == "nominal") {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
			setSelected((value != null && ((Boolean) value).booleanValue()));
			
			return this;
		}
	}

}