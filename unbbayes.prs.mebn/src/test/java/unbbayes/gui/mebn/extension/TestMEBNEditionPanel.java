/**
 * 
 */
package unbbayes.gui.mebn.extension;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.mebn.MEBNNetworkWindow;
import unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * @author Shou Matsumoto
 *
 */
public class TestMEBNEditionPanel extends JPanel implements
		IMEBNEditionPanelBuilder {

	private JLabel topLabel;
	private JTextField centerField;
	private JButton bottomButton;

	private MultiEntityBayesianNetwork mebn;
	private IMEBNMediator mediator;

	/**
	 * 
	 */
	public TestMEBNEditionPanel() {
		this.setLayout(new BorderLayout());
		this.setBackground(Color.WHITE);
		
		topLabel = new JLabel();
		this.add(topLabel, BorderLayout.NORTH);
		
		centerField = new JTextField(10);
		this.add(centerField, BorderLayout.CENTER);
		
		bottomButton = new JButton("Commit");
		bottomButton.setBackground(Color.BLUE);
		bottomButton.setForeground(Color.LIGHT_GRAY);
		this.add(bottomButton, BorderLayout.SOUTH);
		
		bottomButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				topLabel.setText(centerField.getText());
				try {
					mediator.renameMTheory(topLabel.getText());
					((MEBNNetworkWindow)(mediator.getScreen())).getTopTabbedPane().setSelectedIndex(0);
				} catch (Exception exp) {
					JOptionPane.showMessageDialog(mediator.getScreen(),
								exp.getMessage(),
								"Naming error",
								JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
	}

	

	/* (non-Javadoc)
	 * @see java.awt.Component#repaint()
	 */
	public void repaint() {
		if (this.topLabel != null && this.mebn != null) {
			this.topLabel.setText(this.mebn.getName());
		}
		super.repaint();
	}



	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder#buildPanel(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public JPanel buildPanel(MultiEntityBayesianNetwork mebn,
			IMEBNMediator mediator) {
		this.mebn = mebn;
		this.mediator = mediator;
		this.topLabel.setText(this.mebn.getName());
		this.updateUI();
		this.repaint();
		this.updateUI();
		this.setVisible(true);
		return this;
	}

}
