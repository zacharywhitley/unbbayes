package unbbayes.prm.view.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class DialogNewDBSchema extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField txtUrl;

	/**
	 * Flag if the dialog was canceled or accpeted.
	 */
	private boolean dialogAccepted = false;
	/**
	 * Data source URL.
	 */
	private String url;

	/**
	 * Create the dialog.
	 */
	public DialogNewDBSchema() {
		setTitle("New data source");
		setBounds(100, 100, 450, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JRadioButton rdbtnDatabase = new JRadioButton("Database");
		rdbtnDatabase.setSelected(true);
		rdbtnDatabase.setBounds(76, 32, 99, 23);
		contentPanel.add(rdbtnDatabase);

		JRadioButton rdbtnDbSchemaDefinition = new JRadioButton(
				"DB schema definition XML");
		rdbtnDbSchemaDefinition.setEnabled(false);
		rdbtnDbSchemaDefinition.setBounds(179, 32, 216, 23);
		contentPanel.add(rdbtnDbSchemaDefinition);

		txtUrl = new JTextField();
		txtUrl
				.setText("jdbc:derby:/home/dav/workspace-unb/unbbayes.prs.prm2/examples/bloodType/BloodType.db");
		txtUrl.setBounds(83, 75, 312, 19);
		contentPanel.add(txtUrl);
		txtUrl.setColumns(10);

		JLabel lblSource = new JLabel("Source:");
		lblSource.setBounds(12, 36, 70, 15);
		contentPanel.add(lblSource);

		JLabel lblUrl = new JLabel("URL:");
		lblUrl.setBounds(33, 77, 32, 15);
		contentPanel.add(lblUrl);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						url = txtUrl.getText();
						dialogAccepted=true;
						close();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						close();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	/**
	 * Close
	 */
	private void close() {
		this.dispose();
	}

	public boolean isDialogAccepted() {
		return dialogAccepted;
	}
	public String getUrl() {
		return url;
	}
}
