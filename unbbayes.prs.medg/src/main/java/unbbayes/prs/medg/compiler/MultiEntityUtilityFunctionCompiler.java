/**
 * 
 */
package unbbayes.prs.medg.compiler;

import java.util.Locale;

import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.compiler.Compiler;
import unbbayes.prs.mebn.compiler.ICompiler;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.medg.IMEDGNode;
import unbbayes.util.ApplicationPropertyHolder;
import unbbayes.util.Debug;

/**
 * This is the compiler for utility nodes in MEDG.
 * This is basically the same of the one for resident nodes, but the values are not normalized 
 * (because we are declaring utilities instead of probabilities).
 * @author Shou Matsumoto
 *
 */
public class MultiEntityUtilityFunctionCompiler extends Compiler {

	
	private IMEDGNode medgNode;


	/**
	 * The default constructor is protected.
	 * Use {@link #getInstance(ResidentNode)} instead
	 */
	protected MultiEntityUtilityFunctionCompiler() {
		try {
			setResource(unbbayes.util.ResourceController.newInstance().getBundle(
						unbbayes.prs.mebn.compiler.resources.Resources.class.getName(),
						Locale.getDefault(),
						MultiEntityUtilityFunctionCompiler.class.getClassLoader()));
		} catch (Throwable t) {
			Debug.println(getClass(), "Could not initialize resource " + unbbayes.prs.mebn.compiler.resources.Resources.class.getName(), t);
		}
		
		// indicate that this is unnormalized (it's not declaring probabilities).
		this.setToNormalize(false);
	}
	
	/**
	 * Creates an instance of Compiler. The resident node is necessary
	 * in order to perform semantic consisntency check.
	 * Depending on the application.properties file read by {@link ApplicationPropertyHolder}, 
	 * this method may return a singleton instance.
	 * @param node: a resident node containing the table to parse
	 * @param ssbnnode : a node actually generating cpt table at ssbn generation time. It is optional
	 * @return a instance of the compiler.
	 * @see {@link ApplicationPropertyHolder}
	 */
	public static ICompiler newInstance(IMEDGNode node, SSBNNode ssbnnode) {
		MultiEntityUtilityFunctionCompiler comp = new MultiEntityUtilityFunctionCompiler();
		comp.setNode(node);
		comp.setSSBNNode(ssbnnode);
		if (comp.getSSBNNode() != null) {
			if (comp.getSSBNNode().getProbNode() != null) {
				comp.setPotentialTable(comp.getSSBNNode().getProbNode().getProbabilityFunction());
			}			
		}
		try {
			node.setCompiler(comp);
		} catch (Exception e) {}
		return comp;
	}
	
	
	/**
	 * @param medgNode an instance of {@link IMEDGNode} which owns this compiler
	 */
	public void setNode(IMEDGNode medgNode) {
		this.medgNode = medgNode;
		ResidentNode residentNode = medgNode.asResidentNode();
		if (residentNode != null) {
			super.setNode(residentNode);
		}
	}

	/**
	 * Creates an instance of Compiler. The resident node is necessary
	 * in order to perform semantic consisntency check.
	 * @param node: a MEDG node node containing the table to parse
	 * @return a instance of the compiler.
	 * @see {@link Compiler#getInstance(ResidentNode, SSBNNode)}
	 */
	public static ICompiler newInstance(IMEDGNode node) {
		// since we are not using other specific pseudocode Compilers, and we do not use Builders/Factories,
		// it is not necessary to have a constructor method...
		return MultiEntityUtilityFunctionCompiler.newInstance(node, null);
	}
	
	/**
	 * @deprecated
	 * @see #newInstance(ResidentNode, SSBNNode)
	 */
	public static ICompiler getInstance(IMEDGNode node, SSBNNode ssbnnode){
		return (MultiEntityUtilityFunctionCompiler)MultiEntityUtilityFunctionCompiler.newInstance(node, ssbnnode);
	}
	/**
	 * @deprecated
	 * @see #newInstance(ResidentNode)
	 */
	public static ICompiler getInstance(IMEDGNode node) {
		return (MultiEntityUtilityFunctionCompiler)MultiEntityUtilityFunctionCompiler.newInstance(node);
	}

	/**
	 * @return the medgNode
	 * @see #getNode()
	 * @see #setNode(IMEDGNode)
	 */
	public IMEDGNode getMEDGNode() {
		return this.medgNode;
	}

	/**
	 * @param medgNode the medgNode to set
	 * @see #setNode(IMEDGNode)
	 * @see #getNode()
	 */
	public void setMEDGNode(IMEDGNode medgNode) {
		this.medgNode = medgNode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.Compiler#generateLPD(unbbayes.prs.mebn.ssbn.SSBNNode)
	 */
	public IProbabilityFunction generateLPD(SSBNNode ssbnnode) throws MEBNException {
		if (ssbnnode.getProbNode().getProbabilityFunction() == null) {
			return null;
		}
		return super.generateLPD(ssbnnode);
	}
	
}
