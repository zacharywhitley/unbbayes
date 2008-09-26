package unbbayes.evaluation.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

public class EvaluationPane extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	// Left pane
	private JSplitPane leftPane;
	private JPanel nodeListPane;
	private JPanel buttonPane;
	private JPanel gcmPane;
	
	// Button pane
	private JLabel sampleLbl;
	private JTextField sampleSizeTxt;
	private JButton runBtn;
	
	// GCM pane
	private JTextArea outputTxt;
	
	// Right pane
	private JSplitPane rightPane;
	
	// Main pane
	private JSplitPane mainPane;
	
	// Nodes lists
	private JList nodeList;
	private JList targetNodeList;
	private JList evidenceNodeList;

	public EvaluationPane() {
		// Left pane
		nodeListPane = new JPanel(new BorderLayout());
		buttonPane = new JPanel();
		gcmPane = new JPanel();

		// Create a list model with an empty list.
		DefaultListModel listModel = new DefaultListModel();
		nodeList = new JList(listModel);
		nodeListPane.add(createPanelForComponent(setUpList(nodeList), "Node List"));
		
		sampleLbl = new JLabel("Sample Size:");
		sampleSizeTxt = new JTextField(6);
		runBtn = new JButton("Run");
		
		buttonPane.add(sampleLbl);
		buttonPane.add(sampleSizeTxt);
		buttonPane.add(runBtn);
		
		outputTxt = new JTextArea(8, 17);
		outputTxt.setEditable(false);
		JScrollPane outputView = new JScrollPane(outputTxt);
		outputView.setPreferredSize(new Dimension(200, 150));
		
		gcmPane.add(createPanelForComponent(outputView, "Global Confusion Matrix"));

		nodeListPane.add(buttonPane, BorderLayout.SOUTH);

		leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				nodeListPane, gcmPane);

		// Right pane
		// Create a list model with an empty list.
		listModel = new DefaultListModel();
		targetNodeList = new JList(listModel);
		listModel = new DefaultListModel();
		evidenceNodeList = new JList(listModel);
		
		rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createPanelForComponent(setUpList(targetNodeList), "Target Node List"),
				createPanelForComponent(setUpList(evidenceNodeList), "Evidence Node List"));

		mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPane, rightPane);

		this.add(mainPane);

	}
	
	/**
	 * Fills the node list with the given values.
	 * @param valueList List all nodes names.
	 */
	public void fillNodeList(List<String> valueList) {
		DefaultListModel listModel = (DefaultListModel)nodeList.getModel();
		for (String value : valueList) {
			listModel.addElement(value);
		}
	}
	
	/**
	 * Returns the text associated to the sample size text field.
	 * @return The text associated to the sample size text field.
	 */
	public String getSampleSizeText () {
		return sampleSizeTxt.getText();
	}
	
	/**
	 * Returns the names of all target nodes.
	 * @return The names of all target nodes.
	 */
	public List<String> getTargetNodeNameList() {
		List<String> targetNodeNameList = new ArrayList<String>();
		DefaultListModel listModel = (DefaultListModel)targetNodeList.getModel();
		for (int i = 0; i < listModel.size(); i++) {
			targetNodeNameList.add((String)listModel.get(i));
		}
		return targetNodeNameList;
	}
	
	/**
	 * Set the action listener to be associated with the run button.
	 * @param action The action listener to associate with the run button.
	 */
	public void setRunBtnActionListener(ActionListener action) {
		runBtn.addActionListener(action);
	}
	
	/**
	 * Returns the names of all evidence nodes.
	 * @return The names of all evidence nodes.
	 */
	public List<String> getEvidenceNodeNameList() {
		List<String> evidenceNodeNameList = new ArrayList<String>();
		DefaultListModel listModel = (DefaultListModel)evidenceNodeList.getModel();
		for (int i = 0; i < listModel.size(); i++) {
			evidenceNodeNameList.add((String)listModel.get(i));
		}
		return evidenceNodeNameList;
	}
	
	/**
	 * Update the output pane with the output information.
	 * @param output The output information to be updated.
	 */
	public void setOutputText(String output) {
		outputTxt.setText(output);
	}

	private JScrollPane setUpList(JList list) {
		
		list.setVisibleRowCount(-1);
		list.getSelectionModel().setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		list.setTransferHandler(new ListTransferHandler());
		list.setDragEnabled(true);
		list.setDropMode(DropMode.INSERT);

		JScrollPane listView = new JScrollPane(list);
		listView.setPreferredSize(new Dimension(200, 150));
		
		return listView;
	}

	public JPanel createPanelForComponent(JComponent comp, String title) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(comp, BorderLayout.CENTER);
		if (title != null) {
			panel.setBorder(BorderFactory.createTitledBorder(title));
		}
		return panel;
	}

	public class ListTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 1L;
		
		private int[] indices = null;
		
		@SuppressWarnings("unused")
		private int addIndex = -1; //Location where items were added
	    @SuppressWarnings("unused")
		private int addCount = 0;  //Number of items added.


		/**
		 * We only support importing strings.
		 */
		public boolean canImport(TransferHandler.TransferSupport info) {
			// Check for String flavor
			if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return false;
			}
			return true;
		}

		/**
		 * Bundle up the selected items in a single list for export. Each line
		 * is separated by a newline.
		 */
		protected Transferable createTransferable(JComponent c) {
			JList list = (JList) c;
			indices = list.getSelectedIndices();
			Object[] values = list.getSelectedValues();

			StringBuffer buff = new StringBuffer();

			for (int i = 0; i < values.length; i++) {
				Object val = values[i];
				buff.append(val == null ? "" : val.toString());
				if (i != values.length - 1) {
					buff.append("\n");
				}
			}

			return new StringSelection(buff.toString());
		}

		/**
		 * We support both copy and move actions.
		 */
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY_OR_MOVE;
		}

		/**
		 * Perform the actual import. This demo only supports drag and drop.
		 */
		public boolean importData(TransferHandler.TransferSupport info) {
			if (!info.isDrop()) {
				return false;
			}

			JList list = (JList) info.getComponent();
			DefaultListModel listModel = (DefaultListModel) list.getModel();
			JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
			int index = dl.getIndex();
			boolean insert = dl.isInsert();

			// Get the string that is being dropped.
			Transferable t = info.getTransferable();
			String data;
			try {
				data = (String) t.getTransferData(DataFlavor.stringFlavor);
			} catch (Exception e) {
				return false;
			}

			// Wherever there is a newline in the incoming data,
			// break it into a separate item in the list.
			String[] values = data.split("\n");

			addIndex = index;
			addCount = values.length;

			// Perform the actual import.
			for (int i = 0; i < values.length; i++) {
				if (insert) {
					listModel.add(index++, values[i]);
				} else {
					// If the items go beyond the end of the current
					// list, add them in.
					if (index < listModel.getSize()) {
						listModel.set(index++, values[i]);
					} else {
						listModel.add(index++, values[i]);
					}
				}
			}
			return true;
		}

		/**
		 * Remove the items moved from the list.
		 */
		protected void exportDone(JComponent c, Transferable data, int action) {
			JList source = (JList) c;
			DefaultListModel listModel = (DefaultListModel) source.getModel();

			if (action == TransferHandler.MOVE) {
				for (int i = indices.length - 1; i >= 0; i--) {
					listModel.remove(indices[i]);
				}
			}

			indices = null;
			addCount = 0;
			addIndex = -1;
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Evaluation Test");
		
		EvaluationPane pane = new EvaluationPane();
		
		List<String> nodeNameList = new ArrayList<String>();
		nodeNameList.add("Node1");
		nodeNameList.add("Node2");
		nodeNameList.add("Node3");
		nodeNameList.add("Node4");
		
		pane.fillNodeList(nodeNameList);
		
		
		frame.add(pane);
		frame.setSize(800, 450);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		pane.setOutputText("aasdfasdfasfasdfsdf\nasdfasdf\nasfasfd\n");
	}

}
