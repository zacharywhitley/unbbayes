/**
 * 
 */
package unbbayes.util.extension.bn.inference;

import javax.swing.JComponent;

import unbbayes.controller.INetworkMediator;
import unbbayes.prs.Graph;

/**
 * This is an interface for creating a panel which displays a network
 * compiled using an instance of {@link IInferenceAlgorithm}.
 * @author Shou Matsumoto
 *
 */
public interface ICompilationPanelBuilder {

	/**
	 * Generates a panel to display a compiled network
	 * @param algorithm : algorithm to be used to compile graph
	 * @param graph	: network to compile
	 * @param mediator	: link to the main controller
	 * @return
	 */
	public JComponent buildCompilationPanel(IInferenceAlgorithm algorithm, INetworkMediator mediator);
	
}
