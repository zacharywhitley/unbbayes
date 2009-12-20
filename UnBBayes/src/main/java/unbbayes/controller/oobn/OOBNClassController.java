/**
 * 
 */
package unbbayes.controller.oobn;

import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import unbbayes.controller.NetworkController;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.prs.oobn.impl.DefaultOOBNNode;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNClassController extends NetworkController {

	private IOOBNClass controlledClass = null;

	
	/** Load resource file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.controller.oobn.resources.OOBNControllerResources.class.getName());

	
	
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
	public Node insertProbabilisticNode(double x, double y) {
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
		
		return node;
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
		
		wrappedNode.setName(oobnClass.getClassName() //+ "_"
				+ this.getNetwork().getNodeCount());
		
		// set parameters of the wrapped nodes
		wrappedNode.setParentClass(oobnClass);
		wrappedNode.setType(IOOBNNode.TYPE_INSTANCE);
		
		// Graphical wrapper: Graphical representation of wrappedNode
		OOBNNodeGraphicalWrapper node = OOBNNodeGraphicalWrapper.newInstance(wrappedNode);
		
		// update position of graphical wrapper
		//by young
		//node.setPosition(x, y);		
		
		//by young
		node.setInstanceNodePositionAndSize((int)x, (int)y);
		  
		// set parameters of this node (which in most of cases it is not used)
		// but the UnBBayes framework forces us to create node with at least 1 state...
		node.appendState(resource.getString("firstStateProbabilisticName"));
		
		
		node.setName(wrappedNode.getName());
		
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

	/* (non-Javadoc)
	 * @see unbbayes.controller.NetworkController#keyPressed(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		Debug.println(this.getClass(), "A key was pressed!!");
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            Object selected = this.getScreen().getGraphPane().getSelected();
            this.getSENController().deleteSelected(selected);
            for (int i = 0; i < this.getScreen().getGraphPane().getSelectedGroup().size(); i++) {
                selected = this.getScreen().getGraphPane().getSelectedGroup().get(i);
                this.getSENController().deleteSelected(selected);
            }
        }
		super.keyPressed(e);
	}

	
	
	
	

}
