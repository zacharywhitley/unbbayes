/**
 * 
 */
package unbbayes.gui.option;

import java.awt.BorderLayout;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import unbbayes.simulation.likelihoodweighting.inference.LikelihoodWeightingInference;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel;

/**
 * @author Shou Matsumoto
 *
 */
public class LikelihoodWeightingOptionPanel extends
		InferenceAlgorithmOptionPanel {

	private static final long serialVersionUID = 4236243956885735027L;

	private LikelihoodWeightingInference likelihoodWeightingInference;

	private JTextField textField;
	private JLabel label;
	
	
	/** Load resource file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.resources.GuiResources.class.getName());
	
	public LikelihoodWeightingOptionPanel() {
		super();
		this.setLikelihoodWeightingInference(new LikelihoodWeightingInference());
		this.initComponents();
	}

	/**
	 * Initialize the content of this panel.
	 */
	protected void initComponents() {
		this.setLayout(new BorderLayout());
		
		this.setLabel(new JLabel(resource.getString("sampleSizeInputMessage")));
		this.add(this.getLabel(), BorderLayout.NORTH);
		
		this.setTextField(new JTextField("" + this.getLikelihoodWeightingInference().getNTrials()));
		
		// number format check
		this.getTextField().getDocument().addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent e) {
				try{
					if (getTextField().getText() != null && (getTextField().getText().length() > 0)) {
						Integer.parseInt(getTextField().getText());
					}
				} catch (Exception exc) {
					e.getEdit().undo();
				}
			}
		});
		
		this.add(this.getTextField(), BorderLayout.CENTER);
		
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#commitChanges()
	 */
	public void commitChanges() {
		if (getTextField().getText() == null || (getTextField().getText().length() <= 0)) {
			this.getLikelihoodWeightingInference().setNTrials(0);
		} else {
			this.getLikelihoodWeightingInference().setNTrials(Integer.parseInt(getTextField().getText()));
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#getInferenceAlgorithm()
	 */
	public IInferenceAlgorithm getInferenceAlgorithm() {
		return this.getLikelihoodWeightingInference();
	}

	/**
	 * @return the likelihoodWeightingInference
	 */
	public LikelihoodWeightingInference getLikelihoodWeightingInference() {
		return likelihoodWeightingInference;
	}

	/**
	 * @param likelihoodWeightingInference the likelihoodWeightingInference to set
	 */
	public void setLikelihoodWeightingInference(
			LikelihoodWeightingInference likelihoodWeightingInference) {
		this.likelihoodWeightingInference = likelihoodWeightingInference;
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
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#revertChanges()
	 */
	public void revertChanges() {
		this.getTextField().setText(String.valueOf(this.getLikelihoodWeightingInference().getNTrials()));
	}

}
