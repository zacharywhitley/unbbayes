/**
 * 
 */
package unbbayes.prs.mebn.compiler;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.IdentityNode;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.util.ApplicationPropertyHolder;

/**
 * @author Shou Matsumoto
 *
 */
public class IdentityNodeCompiler extends Compiler{


	
	/**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
     * or the first access to SingletonHolder.INSTANCE, not before.
     * This is used for creating singleton instances of compiler
     */
    private static class SingletonHolder { 
    	private static final IdentityNodeCompiler INSTANCE = new IdentityNodeCompiler();
    }
	
	public IdentityNodeCompiler() {}
	

	
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
	public static IdentityNodeCompiler getInstance(ResidentNode node, SSBNNode ssbnnode) {
		IdentityNodeCompiler comp = null;
		try {
			// TODO stop using UnBBayes' global application.properties and start using plugin-specific config
    		if (Boolean.valueOf(ApplicationPropertyHolder.getProperty().get(
    				Compiler.class.getCanonicalName()+".singleton").toString())) {
        		comp = SingletonHolder.INSTANCE;
        	} else {
        		comp = new IdentityNodeCompiler();
        	}
		} catch (Exception e) {
//			Debug.println(Compiler.class, "Using default LPD compiler", e);
			comp = new IdentityNodeCompiler();
		}
		comp.setNode(node);
		comp.setSsbnnode( ssbnnode);
		if (ssbnnode != null) {
			if (ssbnnode.getProbNode() != null) {
				comp.setPotentialTable(ssbnnode.getProbNode().getProbabilityFunction());
			}			
		}
		return comp;
	}
	
	/**
	 * Creates an instance of Compiler. The resident node is necessary
	 * in order to perform semantic consistency check.
	 * @param node: a resident node containing the table to parse
	 * @return a instance of the compiler.
	 * @see {@link Compiler#getInstance(ResidentNode, SSBNNode)}
	 */
	public static IdentityNodeCompiler getInstance(ResidentNode node) {
		return IdentityNodeCompiler.getInstance(node, null);
	}

	public void init(String text) {}

	public void parse() throws MEBNException {}

	public int getIndex() {
		return -1;
	}

	/**
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.ICompiler#generateCPT(unbbayes.prs.mebn.ssbn.SSBNNode)
	 * @deprecated
	 */
	public PotentialTable generateCPT(SSBNNode ssbnnode) throws MEBNException {
		return (PotentialTable) generateLPD(ssbnnode);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.compiler.ICompiler#generateLPD(unbbayes.prs.mebn.ssbn.SSBNNode)
	 */
	public IProbabilityFunction generateLPD(SSBNNode ssbnnode) throws MEBNException {
		
		if (ssbnnode.getArguments().size() < 1) {
			throw new MEBNException(getResource().getString("InvalidArgument"));
		}
		
		// extract the argument
		String argValue = ssbnnode.getArgumentsAsList().get(0).getEntity().getInstanceName();
		
		// find node state by name, equal to the value of argument
		Entity nodeValue = null;
		for (Entity value : ssbnnode.getActualValues()) {
			if (value.getName().equalsIgnoreCase(argValue)) {
				nodeValue = value;
			}
		}
		if (nodeValue == null) {
			throw new MEBNException(getResource().getString("InvalidArgument"));
		}
		
		// remove all states that are not the one we found
		ssbnnode.getActualValues().clear();
		ssbnnode.getActualValues().add(nodeValue);
		
		// also remove states from probabilistic node
		ProbabilisticNode probNode = ssbnnode.getProbNode();
		probNode.removeAllStates();
		probNode.setStateAt(argValue, 0);
		
		// In CPT, set the only state of probNode to 100%
		probNode.getProbabilityFunction().setValue(0, 1);
		
		return probNode.getProbabilityFunction();
	}
	


	public Collection<String> getKeyWords() {
		return Collections.EMPTY_LIST;
	}

	public Collection<Entry<String, String>> getShorthandKeywords() {
		return Collections.EMPTY_LIST;
	}

	
	
	
}
