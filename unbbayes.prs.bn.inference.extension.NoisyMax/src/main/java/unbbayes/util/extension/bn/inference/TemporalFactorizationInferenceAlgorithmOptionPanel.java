/**
 * 
 */
package unbbayes.util.extension.bn.inference;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JTextField;

import unbbayes.util.Debug;


/**
 * @author Shou Matsumoto
 *
 */
public class TemporalFactorizationInferenceAlgorithmOptionPanel extends InferenceAlgorithmOptionPanel {

	private static final long serialVersionUID = 320504845349281555L;
	
	private ICIFactorizationJunctionTreeAlgorithm inferenceAlgorithm;

	private JLabel label;

	private JTextField textField;

	/**
	 * Default constructor must be public so that plugin infrastructure
	 * can instantiate it easily
	 */
	public TemporalFactorizationInferenceAlgorithmOptionPanel() {
		inferenceAlgorithm = new ICIFactorizationJunctionTreeAlgorithm();
		inferenceAlgorithm.setOptionPanel(this);
		
		this.initComponents();
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
		textField = new JTextField("" + inferenceAlgorithm.getProbErrorMargin(), 30);
		textField.setBackground(Color.WHITE);
		
		this.add(label, BorderLayout.WEST);
		this.add(textField, BorderLayout.CENTER);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#getInferenceAlgorithm()
	 */
	public IInferenceAlgorithm getInferenceAlgorithm() {
		return inferenceAlgorithm;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#commitChanges()
	 */
	public void commitChanges() {
		// read value from text field
		float error = inferenceAlgorithm.getProbErrorMargin();
		try {
			error = Float.parseFloat(textField.getText());
		} catch (NullPointerException e){
//			e.printStackTrace();
//			JOptionPane.showMessageDialog(this, "Number must be specified." + e.getMessage(), getName(), JOptionPane.ERROR_MESSAGE);
			this.revertChanges();
			throw new IllegalArgumentException("Number must be specified: " + inferenceAlgorithm.getName(), e);
//			return;
		} catch (NumberFormatException e) {
//			e.printStackTrace();
//			JOptionPane.showMessageDialog(this, e.getMessage(), getName(), JOptionPane.ERROR_MESSAGE);
			this.revertChanges();
			throw new IllegalArgumentException(e.getClass().getName() + ":" + inferenceAlgorithm.getName(), e);
//			return;
		}
		
		if (error < 0 || error > 1) {
//			JOptionPane.showMessageDialog(this, "ε must not be negative or larger than 1", getName(), JOptionPane.ERROR_MESSAGE);
			this.revertChanges();
			throw new IllegalArgumentException("ε must not be negative or larger than 1 : " + inferenceAlgorithm.getName());
//			return;
		}
		
		// update inference algorithm
		Debug.println(getClass(), "Setting error margin to " + error);
		inferenceAlgorithm.setProbErrorMargin(error);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#revertChanges()
	 */
	public void revertChanges() {
		textField.setText("" + inferenceAlgorithm.getProbErrorMargin());
		textField.updateUI();
		textField.repaint();
		this.updateUI();
		this.repaint();
	}

	/**
	 * @return the label
	 */
	public JLabel getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(JLabel label) {
		this.label = label;
	}

	/**
	 * @param inferenceAlgorithm the inferenceAlgorithm to set
	 */
	public void setInferenceAlgorithm(ICIFactorizationJunctionTreeAlgorithm inferenceAlgorithm) {
		this.inferenceAlgorithm = inferenceAlgorithm;
	}

	/**
	 * @return the textField
	 */
	public JTextField getTextField() {
		return textField;
	}

	/**
	 * @param textField the textField to set
	 */
	public void setTextField(JTextField textField) {
		this.textField = textField;
	}

}
