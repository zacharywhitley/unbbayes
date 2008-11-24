/**
 * 
 */
package unbbayes.controller.oobn;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import unbbayes.controller.NetworkController;
import unbbayes.draw.DrawElement;
import unbbayes.draw.DrawEllipse;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.oobn.OOBNClassWindow;
import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.prs.oobn.impl.DefaultOOBNNode;
import unbbayes.prs.oobn.impl.ObjectOrientedBayesianNetwork;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNClassController extends NetworkController {

	private IOOBNClass controlledClass = null;

	
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle
			.getBundle("unbbayes.controller.oobn.resources.OOBNControllerResources");

	
	
	/**
	 * @param singleEntityNetwork
	 * @param screen
	 */
	protected OOBNClassController(SingleEntityNetwork singleEntityNetwork,
			NetworkWindow screen) {
		super(singleEntityNetwork, screen);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param singleEntityNetwork
	 * @param screen
	 */
	public static OOBNClassController newInstance (IOOBNClass oobnClass, NetworkWindow screen) {
		OOBNClassController ret = new OOBNClassController((SingleEntityNetwork)oobnClass.getNetwork(),screen);
		ret.setControlledClass(oobnClass);
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.controller.NetworkController#insertProbabilisticNode(double, double)
	 */
	@Override
	public void insertProbabilisticNode(double x, double y) {
		ProbabilisticNode node = OOBNNodeGraphicalWrapper.newInstance(DefaultOOBNNode.newInstance());
		node.setPosition(x, y);
		node.appendState(resource.getString("firstStateProbabilisticName"));
		node.setName(resource.getString("probabilisticNodeName")
				+ this.getNetwork().getNodeCount());
		node.setDescription(node.getName());
		PotentialTable auxTabProb = ((ITabledVariable) node)
				.getPotentialTable();
		auxTabProb.addVariable(node);
		auxTabProb.setValue(0, 1);
		
		
		this.getNetwork().addNode(node);
	}
	
	
	/**
	 * Insert a new oobn instance node to coordinate (x,y)
	 * @param oobnClass: a class which should be instanciated
	 * @param x: x axis' position of new instance
	 * @param y: y axis' position of new instance
	 */
	public OOBNNodeGraphicalWrapper insertInstanceNode(IOOBNClass oobnClass, double x, double y) {

		// consistency: we cannot insert a instance of a class to itself (no class recursion is allowed)
		if (this.getControlledClass().equals(oobnClass)) {
			throw new RuntimeException(resource.getString("OOBNClassCycle"));
		}
		
		// new oobn node being added
		DefaultOOBNNode wrappedNode = DefaultOOBNNode.newInstance();
		
		// set parameters of the wrapped nodes
		wrappedNode.setParentClass(oobnClass);
		wrappedNode.setType(IOOBNNode.TYPE_INSTANCE);
		
		// Graphical wrapper: Graphical representation of wrappedNode
		OOBNNodeGraphicalWrapper node = OOBNNodeGraphicalWrapper.newInstance(wrappedNode);
		
		// update position of graphical wrapper
		node.setPosition(x, y);		
		
		
		// set parameters of this node (which in most of cases it is not used)
		// but the UnBBayes framework forces us to create node with at least 1 state...
		node.appendState(resource.getString("firstStateProbabilisticName"));
		
		
		node.setName(oobnClass.getClassName() //+ "_"
				+ this.getNetwork().getNodeCount());
		
		node.setDescription(node.getName());
		
		// since it is an empty node, no probability is needed, but let's just add it for compatibility
		PotentialTable auxTabProb = ((ITabledVariable) node)
				.getPotentialTable();
		
		auxTabProb.addVariable(node);
		auxTabProb.setValue(0, 1);
		
		// adds the new node to this network
		this.getNetwork().addNode(node);
		
		
//		// starts inserting the inner nodes to the managed network
//		// the inner nodes are automatically instantiated by the instance node, but it is not
//		// part of network yet
//		for (OOBNNodeGraphicalWrapper innerNode : node.getInnerNodes()) {
//			this.getNetwork().addNode(innerNode);
//		}
		
		
		return node;
	}

	/**
	 * @return the controlledClass
	 */
	public IOOBNClass getControlledClass() {
		return controlledClass;
	}

	/**
	 * @param controlledClass the controlledClass to set
	 */
	protected void setControlledClass(IOOBNClass controlledClass) {
		this.controlledClass = controlledClass;
	}

	
	
	
	

}
