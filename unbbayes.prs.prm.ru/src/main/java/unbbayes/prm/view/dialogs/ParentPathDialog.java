package unbbayes.prm.view.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import unbbayes.prm.model.AggregateFunctionName;
import unbbayes.prm.model.Attribute;
import unbbayes.prm.util.PathFinderAlgorithm;

public class ParentPathDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private final JPanel contentPanel = new JPanel();
	private List<Attribute[]> possiblePaths;
	private Attribute[] selectedPath;
	private JComboBox comboBox;
	private JComboBox comboAggregateFunction;
	boolean cancelled;
	/**
	 * Create the dialog.
	 * 
	 * @param possiblePaths
	 */
	public ParentPathDialog(List<Attribute[]> possiblePaths) {
		this.possiblePaths = possiblePaths;
		setTitle("Select a parent path");
		setBounds(100, 100, 451, 122);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 99, 32, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 24, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblAggregateFunction = new JLabel("Aggregate function:");
			GridBagConstraints gbc_lblAggregateFunction = new GridBagConstraints();
			gbc_lblAggregateFunction.anchor = GridBagConstraints.EAST;
			gbc_lblAggregateFunction.insets = new Insets(0, 0, 5, 5);
			gbc_lblAggregateFunction.gridx = 0;
			gbc_lblAggregateFunction.gridy = 0;
			contentPanel.add(lblAggregateFunction, gbc_lblAggregateFunction);
		}
		{
			comboAggregateFunction = new JComboBox();
			comboAggregateFunction.setModel(new DefaultComboBoxModel(
					AggregateFunctionName.values()));
			GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
			gbc_comboBox_1.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox_1.gridx = 1;
			gbc_comboBox_1.gridy = 0;
			contentPanel.add(comboAggregateFunction, gbc_comboBox_1);
		}
		{
			JLabel lblSelectAPath = new JLabel("Parent path:");
			lblSelectAPath.setToolTipText("Parent path (direction is child to parent)");
			GridBagConstraints gbc_lblSelectAPath = new GridBagConstraints();
			gbc_lblSelectAPath.anchor = GridBagConstraints.WEST;
			gbc_lblSelectAPath.insets = new Insets(0, 0, 0, 5);
			gbc_lblSelectAPath.gridx = 0;
			gbc_lblSelectAPath.gridy = 1;
			contentPanel.add(lblSelectAPath, gbc_lblSelectAPath);
		}
		{
			comboBox = new JComboBox();

			// add paths
			for (Attribute[] attributes : possiblePaths) {
				comboBox.addItem(PathFinderAlgorithm.pathToString(attributes));
			}

			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.anchor = GridBagConstraints.NORTHWEST;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 1;
			contentPanel.add(comboBox, gbc_comboBox);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int index = comboBox.getSelectedIndex();
						selectedPath = ParentPathDialog.this.possiblePaths
								.get(index);
						cancelled=false;
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						cancelled=true;
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	public Attribute[] getSelectedPath() {
		return selectedPath;
	}

	public AggregateFunctionName getSelectedAggregateFunction() {
		return AggregateFunctionName.valueOf(comboAggregateFunction
				.getSelectedItem().toString());
	}
	
	public boolean isCancelled() {
		return cancelled;
	}

}
