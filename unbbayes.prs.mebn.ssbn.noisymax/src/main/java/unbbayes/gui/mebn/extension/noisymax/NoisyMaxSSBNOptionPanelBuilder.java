/**
 * 
 */
package unbbayes.gui.mebn.extension.noisymax;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder;
import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.extension.noisyMax.NoisyMaxSSBNAlgorithm;
import unbbayes.util.extension.bn.inference.NoisyMaxCPTConverter;

/**
 * This is just a placeholder for a GUI to change settings of {@link unbbayes.prs.mebn.ssbn.extension.noisyMax.NoisyMaxSSBNAlgorithm}
 * @author Shou Matsumoto
 *
 */
public class NoisyMaxSSBNOptionPanelBuilder extends JPanel implements ISSBNOptionPanelBuilder {

	private static final long serialVersionUID = -1503691964820355018L;
	
	private NoisyMaxSSBNAlgorithm ssbnGenerator;
	

	private JLabel label;

	private JTextField textField;

	/**
	 * UnBBayes' plugin framework requires default constructor to be public
	 */
	public NoisyMaxSSBNOptionPanelBuilder() {
	}
	

	/**
	 * Initializes swing components of this panel
	 */
	protected void initComponents() {
		// reset
		this.removeAll();
		this.setLayout(new BorderLayout());
//		this.setBackground(Color.WHITE);
		
		label = new JLabel("ε : ");
//		label.setBackground(Color.WHITE);
		if (ssbnGenerator != null) {
			textField = new JTextField("" + ssbnGenerator.getProbErrorMargin(), 30);
		} else {
			Logger.getLogger(getClass()).warn("SSBN generator was null while attempting to initialize option panel");
			textField = new JTextField(""+NoisyMaxCPTConverter.DEFAULT_PROBABILITY_ERROR_MARGIN, 30);
		}
		textField.setBackground(Color.WHITE);
		
		
		this.setSize(100, 50);
		this.setPreferredSize(new Dimension(100, 50));
		this.add(label, BorderLayout.WEST);
		this.add(textField, BorderLayout.CENTER);
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
		// read value from text field
		float error = ssbnGenerator.getProbErrorMargin();
		try {
			error = Float.parseFloat(textField.getText());
		} catch (NullPointerException e){
			this.discardChanges();
			throw new IllegalArgumentException("Number must be specified: " + ssbnGenerator.getClass().getName(), e);
		} catch (NumberFormatException e) {
			this.discardChanges();
			throw new IllegalArgumentException(e.getClass().getName() + ":" + ssbnGenerator.getClass().getName(), e);
		}
		
		if (error < 0 || error > 1) {
			this.discardChanges();
			throw new IllegalArgumentException("ε must not be negative or larger than 1 : " + ssbnGenerator.getClass().getName());
		}
		
		// update inference algorithm
		Logger.getLogger(getClass()).debug("Setting error margin to " + error);
		ssbnGenerator.setProbErrorMargin(error);
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder#discardChanges()
	 */
	public void discardChanges() {
		textField.setText("" + ssbnGenerator.getProbErrorMargin());
		textField.updateUI();
		textField.repaint();
		this.updateUI();
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder#setSSBNGenerator(unbbayes.prs.mebn.ssbn.ISSBNGenerator)
	 */
	public void setSSBNGenerator(ISSBNGenerator ssbnGenerator) {
		this.ssbnGenerator = (NoisyMaxSSBNAlgorithm) ssbnGenerator;
		initComponents();
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder#getSSBNGenerator()
	 */
	public ISSBNGenerator getSSBNGenerator() {
		return this.ssbnGenerator;
	}

}
