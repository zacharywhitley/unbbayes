package unbbayes.gui.mebn;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class MTheoryTree extends JTree {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7557085958154752664L;

	public MTheoryTree() {
		DefaultMutableTreeNode categoryMTheory = new DefaultMutableTreeNode("MTheory");
	    createNodes(categoryMTheory);
	    treeModel = new DefaultTreeModel(categoryMTheory);
	    this.setModel(treeModel);
	    this.updateUI();
	    for (int i = 0; i < this.getRowCount(); i++) {
	    	this.expandRow(i);
	    }
	}
	
	private void createNodes(DefaultMutableTreeNode categoryMTheory) {
	    DefaultMutableTreeNode categoryNode = null;
	    DefaultMutableTreeNode categoryMFrag = null;
	    DefaultMutableTreeNode element = null;
	    
	    for (int i = 1; i < 4; i++) {
		    categoryMFrag = new DefaultMutableTreeNode("MFrag " + i);
		    
		    categoryNode = new DefaultMutableTreeNode("Context Node");		    
		    element = new DefaultMutableTreeNode("Context Node 1");
		    categoryNode.add(element);
		    element = new DefaultMutableTreeNode("Context Node 2");
		    categoryNode.add(element);
		    categoryMFrag.add(categoryNode);
		    
		    categoryNode = new DefaultMutableTreeNode("Input Node");		    
		    element = new DefaultMutableTreeNode("Input Node 1");
		    categoryNode.add(element);
		    element = new DefaultMutableTreeNode("Input Node 2");
		    categoryNode.add(element);
		    categoryMFrag.add(categoryNode);
		    
		    categoryNode = new DefaultMutableTreeNode("Resident Node");		    
		    element = new DefaultMutableTreeNode("Resident Node 1");
		    categoryNode.add(element);
		    element = new DefaultMutableTreeNode("Resident Node 2");
		    categoryNode.add(element);
		    categoryMFrag.add(categoryNode);
		    
		    categoryMTheory.add(categoryMFrag);
	    }
	}

}
