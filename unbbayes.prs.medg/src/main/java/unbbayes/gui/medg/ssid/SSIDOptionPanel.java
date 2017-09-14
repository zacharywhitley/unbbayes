/**
 * 
 */
package unbbayes.gui.medg.ssid;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.medg.ssid.SSIDGenerator;

/**
 * @author Shou Matsumoto
 *
 */
public class SSIDOptionPanel extends JPanel implements ISSBNOptionPanelBuilder {

	private ISSBNGenerator ssidGenerator;
	private JCheckBox connectDecisionCheckBox;
	private JCheckBox onlyConnectSameResidentDecisionCheckBox;
	
	private Logger log = Logger.getLogger(getClass());

	/**
	 * 
	 */
	public SSIDOptionPanel() {
	}

	
	protected void initComponents() {
		this.setLayout(new GridLayout(0, 1));
		this.setBorder(new TitledBorder("SSID"));
		
		Boolean isToPostProcessDecisionNodes = null;
		Boolean isToConnectSameResidentDecisionNodes = null;
		if (ssidGenerator != null && ssidGenerator instanceof SSIDGenerator) {
			isToPostProcessDecisionNodes = ((SSIDGenerator)ssidGenerator).isToPostProcessDecisionNodes();
			isToConnectSameResidentDecisionNodes = ((SSIDGenerator)ssidGenerator).isToConnectSameResidentDecisionNodes();
		} else {
			getLog().warn("Unknown type of ssid generator: " + ssidGenerator);
		}

		connectDecisionCheckBox = new JCheckBox("Automatically connect decision node instances", (isToPostProcessDecisionNodes!=null)?isToPostProcessDecisionNodes:false);
		onlyConnectSameResidentDecisionCheckBox = new JCheckBox("Only connect decision nodes that were generated from same resident decision nodes.",  (isToConnectSameResidentDecisionNodes!=null)?isToConnectSameResidentDecisionNodes:false);
		
		this.add(connectDecisionCheckBox);
		this.add(onlyConnectSameResidentDecisionCheckBox);
		
		if (isToPostProcessDecisionNodes == null) {
			connectDecisionCheckBox.setEnabled(false);
		}
		if (isToConnectSameResidentDecisionNodes == null || !connectDecisionCheckBox.isSelected()) {
			onlyConnectSameResidentDecisionCheckBox.setEnabled(false);
		}
		
		// automatically enable/disable second check box if first check box is selected/deselected
		connectDecisionCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == e.SELECTED) {
					onlyConnectSameResidentDecisionCheckBox.setEnabled(true);
				} else if (e.getStateChange() == e.DESELECTED) {
					onlyConnectSameResidentDecisionCheckBox.setEnabled(false);
				}
				onlyConnectSameResidentDecisionCheckBox.updateUI();
				onlyConnectSameResidentDecisionCheckBox.repaint();
				updateUI();
				repaint();
			}
		});
		
		this.updateUI();
		this.repaint();
	}

	

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.IPanelBuilder#getPanel()
	 */
	public JComponent getPanel() {
		return this;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder#commitChanges()
	 */
	public void commitChanges() {
		if (ssidGenerator != null && ssidGenerator instanceof SSIDGenerator) {
			((SSIDGenerator)ssidGenerator).setToPostProcessDecisionNodes(connectDecisionCheckBox.isSelected());
			((SSIDGenerator)ssidGenerator).setToConnectSameResidentDecisionNodes(onlyConnectSameResidentDecisionCheckBox.isSelected());
		}  else {
			getLog().warn("Could not commit changes to SSID generator due to unknown type of generator: " + ssidGenerator);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder#discardChanges()
	 */
	public void discardChanges() {
		if (ssidGenerator != null && ssidGenerator instanceof SSIDGenerator) {
			connectDecisionCheckBox.setSelected(((SSIDGenerator)ssidGenerator).isToPostProcessDecisionNodes());
			onlyConnectSameResidentDecisionCheckBox.setSelected(((SSIDGenerator)ssidGenerator).isToConnectSameResidentDecisionNodes());
			this.updateUI();
			this.repaint();
		}  else {
			getLog().warn("Could not revert changes to SSID generator due to unknown type of generator: " + ssidGenerator);
		}

	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder#setSSBNGenerator(unbbayes.prs.mebn.ssbn.ISSBNGenerator)
	 */
	public void setSSBNGenerator(ISSBNGenerator ssbnGenerator) {
		this.ssidGenerator = ssbnGenerator;
		this.initComponents();
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder#getSSBNGenerator()
	 */
	public ISSBNGenerator getSSBNGenerator() {
		return this.ssidGenerator;
	}


	/**
	 * @return the log
	 */
	public Logger getLog() {
		if (log == null) {
			log = Logger.getLogger(getClass());
		}
		return log;
	}


	/**
	 * @param log the log to set
	 */
	public void setLog(Logger log) {
		this.log = log;
	}

}
