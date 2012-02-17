/**
 * 
 */
package unbbayes.gui.option;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import unbbayes.controller.INetworkMediator;
import unbbayes.gui.PNCompilationPane;
import unbbayes.prs.bn.ILikelihoodExtractor;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.LikelihoodExtractor;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel;

/**
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeOptionPanel extends InferenceAlgorithmOptionPanel {

	private static final long serialVersionUID = 7734979988420335938L;
	
	private IInferenceAlgorithm inferenceAlgorithm;

	private ActionListener clearVirtualNodesActionListener;

	private JCheckBox removeVirtualNodeCheckBox;

	private JCheckBox useSoftEvidenceCheckBox;

	private ILikelihoodExtractor softEvidenceLikelihoodExtractor = JeffreyRuleLikelihoodExtractor.newInstance();
	
	private ILikelihoodExtractor likelihoodEvidenceLikelihoodExtractor = LikelihoodExtractor.newInstance();
	
	
	public JunctionTreeOptionPanel() {
		super();
		JunctionTreeAlgorithm alg = new JunctionTreeAlgorithm();
		alg.setOptionPanel(this);
		this.setInferenceAlgorithm(alg);
		this.initComponents();
		this.initListeners();
	}

	
	protected void initComponents() {
		this.setLayout(new GridLayout(0, 1));
		
		// verify if checkbox for removing virtual nodes should be checked
		boolean isSelected = false;
		// verify if the button to return to edit mode contains the action listener to remove all virtual nodes
		try {
			for (ActionListener al : this.getMediator().getScreen().getNetWindowCompilation().getEditMode().getActionListeners()) {
				if (al.equals(getClearVirtualNodesActionListener())) {
					isSelected = true;
					break;
				}
			}
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		// checkbox to remove all virtual nodes after returning to edit mode
		setRemoveVirtualNodeCheckBox(new JCheckBox("Delete virtual nodes when returning to edit mode", isSelected));
		this.add(getRemoveVirtualNodeCheckBox());
		

		// verify if the checkbox for soft evidence should be checked
		isSelected = false;
		if (getInferenceAlgorithm() instanceof JunctionTreeAlgorithm) {
			JunctionTreeAlgorithm junctionTreeAlgorithm = (JunctionTreeAlgorithm) getInferenceAlgorithm();
			isSelected = this.getSoftEvidenceLikelihoodExtractor().equals(junctionTreeAlgorithm.getLikelihoodExtractor());
		}
		
		// checkbox to use soft evidence instead of likelihood evidence
		setUseSoftEvidenceCheckBox(new JCheckBox("Use soft evidence instead of likelihood evidence", isSelected));
		this.add(getUseSoftEvidenceCheckBox());
		
	}

	protected void initListeners() {
		// listener to remove all virtual nodes when returning to edit mode
		setClearVirtualNodesActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getInferenceAlgorithm() instanceof JunctionTreeAlgorithm) {
					JunctionTreeAlgorithm junctionTreeAlgorithm = (JunctionTreeAlgorithm) getInferenceAlgorithm();
					try {
						junctionTreeAlgorithm.clearVirtualNodes();
					} catch (Exception t) {
						t.printStackTrace();
						// TODO use resources
						try {
							JOptionPane.showMessageDialog(JunctionTreeOptionPanel.this, "Could not clear virtual nodes: " + t.getMessage());
						} catch (Throwable t2) {
							t2.printStackTrace();
						}
					}
				}
			}
		});
		
		
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#commitChanges()
	 */
	public void commitChanges() {
		// remove listener that removes all virtual nodes from network
		if (this.getMediator() != null && this.getClearVirtualNodesActionListener() != null) {
			// remove listener from old module
			this.getMediator().getScreen().getNetWindowCompilation().getEditMode().removeActionListener(this.getClearVirtualNodesActionListener());
		}
		// add again if the check box for removing virtual nodes is checked
		if (this.getRemoveVirtualNodeCheckBox().isSelected()) {
			// remove and add listener again
			if (this.getMediator() != null && this.getClearVirtualNodesActionListener() != null) {
				// remove listener from old module
				this.getMediator().getScreen().getNetWindowCompilation().getEditMode().addActionListener(this.getClearVirtualNodesActionListener());
			}
		}
		
		// the checkbox for soft evidence...
		if (getInferenceAlgorithm() instanceof JunctionTreeAlgorithm) {
			JunctionTreeAlgorithm junctionTreeAlgorithm = (JunctionTreeAlgorithm) getInferenceAlgorithm();
			if (this.getUseSoftEvidenceCheckBox().isSelected()) {
				junctionTreeAlgorithm.setLikelihoodExtractor(this.getSoftEvidenceLikelihoodExtractor());
			} else
				junctionTreeAlgorithm.setLikelihoodExtractor(this.getLikelihoodEvidenceLikelihoodExtractor()); {
			}
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#getInferenceAlgorithm()
	 */
	public IInferenceAlgorithm getInferenceAlgorithm() {
		return this.inferenceAlgorithm;
	}

	/**
	 * @param inferenceAlgorithm the inferenceAlgorithm to set
	 */
	public void setInferenceAlgorithm(IInferenceAlgorithm inferenceAlgorithm) {
		this.inferenceAlgorithm = inferenceAlgorithm;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#revertChanges()
	 */
	public void revertChanges() {
		// nothing to change
	}

	/**
	 * Besides changing the mediator, it adds an action listener so that {@link JunctionTreeAlgorithm#clearVirtualNodes()}
	 * is called when {@link PNCompilationPane#getEditMode()} is pressed (i.e. all virtual nodes are cleared when
	 * returning to edit mode).
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#setMediator(unbbayes.controller.INetworkMediator)
	 */
	public void setMediator(INetworkMediator mediator) {
		if (this.getMediator() != null && this.getClearVirtualNodesActionListener() != null) {
			// remove listener from old module
			this.getMediator().getScreen().getNetWindowCompilation().getEditMode().removeActionListener(this.getClearVirtualNodesActionListener());
		}
		super.setMediator(mediator);
		if (this.getMediator() != null && this.getClearVirtualNodesActionListener() != null) {
			// update listener and add listener to new module
			this.getMediator().getScreen().getNetWindowCompilation().getEditMode().addActionListener(this.getClearVirtualNodesActionListener());
		}
	}

	/**
	 * @return the clearVirtualNodesActionListener
	 */
	public ActionListener getClearVirtualNodesActionListener() {
		return clearVirtualNodesActionListener;
	}

	/**
	 * @param clearVirtualNodesActionListener the clearVirtualNodesActionListener to set
	 */
	public void setClearVirtualNodesActionListener(
			ActionListener clearVirtualNodesActionListener) {
		this.clearVirtualNodesActionListener = clearVirtualNodesActionListener;
	}


	/**
	 * @param removeVirtualNodeCheckBox the removeVirtualNodeCheckBox to set
	 */
	public void setRemoveVirtualNodeCheckBox(JCheckBox removeVirtualNodeCheckBox) {
		this.removeVirtualNodeCheckBox = removeVirtualNodeCheckBox;
	}


	/**
	 * @return the removeVirtualNodeCheckBox
	 */
	public JCheckBox getRemoveVirtualNodeCheckBox() {
		return removeVirtualNodeCheckBox;
	}


	/**
	 * @param useSoftEvidenceCheckBox the useSoftEvidenceCheckBox to set
	 */
	public void setUseSoftEvidenceCheckBox(JCheckBox useSoftEvidenceCheckBox) {
		this.useSoftEvidenceCheckBox = useSoftEvidenceCheckBox;
	}


	/**
	 * @return the useSoftEvidenceCheckBox
	 */
	public JCheckBox getUseSoftEvidenceCheckBox() {
		return useSoftEvidenceCheckBox;
	}


	/**
	 * This is the likelihood extractor to be used by a junction tree algorithm with soft evidences
	 * instead of likelihood.
	 * @param softEvidenceLikelihoodExtractor the softEvidenceLikelihoodExtractor to set
	 */
	public void setSoftEvidenceLikelihoodExtractor(
			ILikelihoodExtractor softEvidenceLikelihoodExtractor) {
		this.softEvidenceLikelihoodExtractor = softEvidenceLikelihoodExtractor;
	}


	/**
	 * This is the likelihood extractor to be used by a junction tree algorithm with soft evidences
	 * instead of likelihood.
	 * @return the softEvidenceLikelihoodExtractor
	 */
	public ILikelihoodExtractor getSoftEvidenceLikelihoodExtractor() {
		return softEvidenceLikelihoodExtractor;
	}


	/**
	 * This is the last likelihood extractor used by {@link #getInferenceAlgorithm()} before
	 * it was changed to {@link #getSoftEvidenceLikelihoodExtractor()}.
	 * @param likelihoodEvidenceLikelihoodExtractor the likelihoodEvidenceLikelihoodExtractor to set
	 */
	public void setLikelihoodEvidenceLikelihoodExtractor(
			ILikelihoodExtractor previousLikelihoodExtractor) {
		this.likelihoodEvidenceLikelihoodExtractor = previousLikelihoodExtractor;
	}


	/**
	 * This is the last likelihood extractor used by {@link #getInferenceAlgorithm()} before
	 * it was changed to {@link #getSoftEvidenceLikelihoodExtractor()}.
	 * @return the likelihoodEvidenceLikelihoodExtractor
	 */
	public ILikelihoodExtractor getLikelihoodEvidenceLikelihoodExtractor() {
		return likelihoodEvidenceLikelihoodExtractor;
	}

}
