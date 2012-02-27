package unbbayes.gui;




import java.awt.Component;

import javax.swing.JDialog;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;

/**
 * Classes implementing this interface can be used by UnBBayes in order to 
 * show a dialog for inserting likelihood evidences to nodes, from GUI.
 * @author Shou Matsumoto
 *
 */
public interface ILikelihoodEvidenceDialogBuilder {

	/**
	 * Generates a dialog responsible for inserting likelihood evidence to nodeToAddLikelihood
	 * @param nodeToAddLikelihood
	 * @param owner : it is going to be used as the owner of the dialog.
	 * It is expected to be a {@link java.awt.Window}, a {@link java.awt.Dialog},
	 * or a {@link java.awt.Frame}, as in {@link JDialog}.
	 * @param graph : the network containing nodeToAddLikelihood
	 * @return
	 */
	public JDialog buildDialog(Graph graph, INode nodeToAddLikelihood, Component owner);

}