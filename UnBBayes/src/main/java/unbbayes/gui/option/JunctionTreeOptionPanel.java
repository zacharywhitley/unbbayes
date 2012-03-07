/**
 * 
 */
package unbbayes.gui.option;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import unbbayes.controller.INetworkMediator;
import unbbayes.gui.ILikelihoodEvidenceDialogBuilder;
import unbbayes.gui.LikelihoodEvidenceDialogBuilder;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.SoftEvidenceDialogBuilder;
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

	private AncestorListener clearVirtualNodesAncestorListener;

	private JCheckBox removeVirtualNodeCheckBox;

	private JCheckBox useSoftEvidenceCheckBox;

	private ILikelihoodExtractor softEvidenceLikelihoodExtractor = JeffreyRuleLikelihoodExtractor.newInstance();
	
	private ILikelihoodExtractor likelihoodEvidenceLikelihoodExtractor = LikelihoodExtractor.newInstance();

	private ILikelihoodEvidenceDialogBuilder likelihoodEvidenceDialogBuilder = new LikelihoodEvidenceDialogBuilder();

	private ILikelihoodEvidenceDialogBuilder softEvidenceDialogBuilder = new SoftEvidenceDialogBuilder();
	
	
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
		// verify if the edit panel contains the listener to remove all virtual nodes
		try {
			for (AncestorListener listener : this.getMediator().getScreen().getNetWindowEdition().getAncestorListeners()) {
				if (listener.equals(getClearVirtualNodesAncestorListener())) {
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
		setClearVirtualNodesAncestorListener(new AncestorListener() {
			public void ancestorRemoved(AncestorEvent event) {}
			public void ancestorMoved(AncestorEvent event) {}
			/**
			 * This is called when the card layout sets the edit mode pane visible
			 */
			public void ancestorAdded(AncestorEvent event) {
				if (getInferenceAlgorithm() instanceof JunctionTreeAlgorithm) {
					JunctionTreeAlgorithm junctionTreeAlgorithm = (JunctionTreeAlgorithm) getInferenceAlgorithm();
					try {
						junctionTreeAlgorithm.clearVirtualNodes();
						getMediator().getScreen().changeToPNEditionPane();
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
		if (this.getMediator() != null && this.getClearVirtualNodesAncestorListener() != null) {
			// remove listener from old module
			this.getMediator().getScreen().getNetWindowEdition().removeAncestorListener(this.getClearVirtualNodesAncestorListener());
		}
		// add again if the check box for removing virtual nodes is checked
		if (this.getRemoveVirtualNodeCheckBox().isSelected()) {
			// remove and add listener again
			if (this.getMediator() != null && this.getClearVirtualNodesAncestorListener() != null) {
				// remove listener from old module
				this.getMediator().getScreen().getNetWindowEdition().addAncestorListener(this.getClearVirtualNodesAncestorListener());
			}
		}
		
		// the checkbox for soft evidence...
		if (getInferenceAlgorithm() instanceof JunctionTreeAlgorithm) {
			JunctionTreeAlgorithm junctionTreeAlgorithm = (JunctionTreeAlgorithm) getInferenceAlgorithm();
			if (this.getUseSoftEvidenceCheckBox().isSelected()) {
				junctionTreeAlgorithm.setLikelihoodExtractor(this.getSoftEvidenceLikelihoodExtractor());
				if (getMediator() != null) {
					getMediator().getScreen().getEvidenceTree().setLikelihoodEvidenceDialogBuilder(getSoftEvidenceDialogBuilder());
				}

			} else {
				junctionTreeAlgorithm.setLikelihoodExtractor(this.getLikelihoodEvidenceLikelihoodExtractor()); 
				if (getMediator() != null) {
					getMediator().getScreen().getEvidenceTree().setLikelihoodEvidenceDialogBuilder(getLikelihoodEvidenceDialogBuilder());
				}
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
	 * Besides changing the mediator, it adds an ancestor listener so that {@link JunctionTreeAlgorithm#clearVirtualNodes()}
	 * is called when {@link NetworkWindow#getNetWindowEdition()} gets active (i.e. all virtual nodes are cleared when
	 * returning to edit mode).
	 * @see unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel#setMediator(unbbayes.controller.INetworkMediator)
	 */
	public void setMediator(INetworkMediator mediator) {
		if (this.getMediator() != null && this.getClearVirtualNodesAncestorListener() != null) {
			// remove listener from old module
			this.getMediator().getScreen().getNetWindowEdition().removeAncestorListener(this.getClearVirtualNodesAncestorListener());
		}
		super.setMediator(mediator);
		if (this.getMediator() != null && this.getClearVirtualNodesAncestorListener() != null) {
			// update listener and add listener to new module
			this.getMediator().getScreen().getNetWindowEdition().addAncestorListener(this.getClearVirtualNodesAncestorListener());
		}
	}

	/**
	 * This ancestor listener is going to be added to {@link NetworkWindow#getNetWindowEdition()}, accessible from {@link #getMediator()},
	 * when {@link #getRemoveVirtualNodeCheckBox()} is checked and {@link #commitChanges()} is called. 
	 * This is basically for removing virtual nodes from network when GUI switches to edit mode.
	 * @return the clearVirtualNodesAncestorListener
	 */
	public AncestorListener getClearVirtualNodesAncestorListener() {
		return clearVirtualNodesAncestorListener;
	}

	/**
	 * This ancestor listener is going to be added to {@link NetworkWindow#getNetWindowEdition()}, accessible from {@link #getMediator()},
	 * when {@link #getRemoveVirtualNodeCheckBox()} is checked and {@link #commitChanges()} is called. 
	 * This is basically for removing virtual nodes from network when GUI switches to edit mode.
	 * @param clearVirtualNodesAncestorListener the clearVirtualNodesAncestorListener to set
	 */
	public void setClearVirtualNodesAncestorListener(AncestorListener clearVirtualNodesAncestorListener) {
		this.clearVirtualNodesAncestorListener = clearVirtualNodesAncestorListener;
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


	/**
	 * @param likelihoodEvidenceDialogBuilder the likelihoodEvidenceDialogBuilder to set
	 */
	public void setLikelihoodEvidenceDialogBuilder(
			ILikelihoodEvidenceDialogBuilder likelihoodEvidenceDialogBuilder) {
		this.likelihoodEvidenceDialogBuilder = likelihoodEvidenceDialogBuilder;
	}


	/**
	 * @return the likelihoodEvidenceDialogBuilder
	 */
	public ILikelihoodEvidenceDialogBuilder getLikelihoodEvidenceDialogBuilder() {
		return likelihoodEvidenceDialogBuilder;
	}


	/**
	 * @param softEvidenceDialogBuilder the softEvidenceDialogBuilder to set
	 */
	public void setSoftEvidenceDialogBuilder(ILikelihoodEvidenceDialogBuilder softEvidenceDialogBuilder) {
		this.softEvidenceDialogBuilder = softEvidenceDialogBuilder;
	}


	/**
	 * @return the softEvidenceDialogBuilder
	 */
	public ILikelihoodEvidenceDialogBuilder getSoftEvidenceDialogBuilder() {
		return softEvidenceDialogBuilder;
	}

}
