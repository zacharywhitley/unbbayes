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

import unbbayes.simulation.sampling.GibbsSampling;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel;

/**
 * @author Shou Matsumoto
 *
 */
public class GibbsSamplingOptionPanel extends InferenceAlgorithmOptionPanel {

	private static final long serialVersionUID = -4223727107755931892L;

	private GibbsSampling gibbsAlgorithm;

	private JTextField textField;
	private JLabel label;
	
	
	/** Load resource file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.resources.GuiResources.class.getName());
	
	public GibbsSamplingOptionPanel() {
		super();
		this.setGibbsAlgorithm(new GibbsSampling());
		this.initComponents();
	}
	
	/**
	 * Initialize the content of this panel.
	 */
	protected void initComponents() {
		this.setLayout(new BorderLayout());
		
		this.setLabel(new JLabel(resource.getString("sampleSizeInputMessage")));
		this.add(this.getLabel(), BorderLayout.NORTH);
		
		this.setTextField(new JTextField("" + this.getGibbsAlgorithm().getSampleSize()));
		
		// number format check
		this.getTextField().getDocument().addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent e) {
				try{
					if (getTextField().getText() != null && getTextField().getText().length() > 0) {
						Integer.parseInt(getTextField().getText());
					}
				} catch (Exception exc) {
					e.getEdit().undo();
				}
			}
		});
		
		this.add(this.getTextField(), BorderLayout.CENTER);
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#getInferenceAlgorithm()
	 */
	@Override
	public IInferenceAlgorithm getInferenceAlgorithm() {
		return this.getGibbsAlgorithm();
	}

	/**
	 * @return the gibbsAlgorithm
	 */
	public GibbsSampling getGibbsAlgorithm() {
		return gibbsAlgorithm;
	}

	/**
	 * @param gibbsAlgorithm the gibbsAlgorithm to set
	 */
	public void setGibbsAlgorithm(GibbsSampling gibbsAlgorithm) {
		this.gibbsAlgorithm = gibbsAlgorithm;
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
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#commitChanges()
	 */
	public void commitChanges() {
		if (getTextField().getText() == null || getTextField().getText().length() <= 0) {
			this.getGibbsAlgorithm().setSampleSize(0);
		} else {
			this.getGibbsAlgorithm().setSampleSize(Integer.parseInt(getTextField().getText()));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#revertChanges()
	 */
	public void revertChanges() {
		this.getTextField().setText(String.valueOf(this.getGibbsAlgorithm().getSampleSize()));
	}

}
