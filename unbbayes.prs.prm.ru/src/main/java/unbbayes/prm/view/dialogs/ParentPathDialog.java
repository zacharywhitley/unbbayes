package unbbayes.prm.view.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JComboBox;

import unbbayes.prm.model.Attribute;
import unbbayes.prm.util.PathFinderAlgorithm;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ParentPathDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private List<Attribute[]> possiblePaths;
	private Attribute[] selectedPath;
	private JComboBox comboBox;

	/**
	 * Create the dialog.
	 * 
	 * @param possiblePaths
	 */
	public ParentPathDialog(List<Attribute[]> possiblePaths) {
		this.possiblePaths = possiblePaths;
		setTitle("Select a parent path");
		setBounds(100, 100, 450, 102);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 99, 32, 0 };
		gbl_contentPanel.rowHeights = new int[] { 24, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblSelectAPath = new JLabel("Parent path:");
			GridBagConstraints gbc_lblSelectAPath = new GridBagConstraints();
			gbc_lblSelectAPath.anchor = GridBagConstraints.WEST;
			gbc_lblSelectAPath.insets = new Insets(0, 0, 0, 5);
			gbc_lblSelectAPath.gridx = 0;
			gbc_lblSelectAPath.gridy = 0;
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
			gbc_comboBox.gridy = 0;
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

}
