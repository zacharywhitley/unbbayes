/**
 * 
 */
package unbbayes.prs.bn.valueTree.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder;
import unbbayes.prs.Node;
import unbbayes.prs.bn.valueTree.IValueTreeNode;
import unbbayes.prs.bn.valueTree.ValueTreeNode;
import unbbayes.prs.bn.valueTree.ValueTreeProbabilisticNode;
import unbbayes.util.Debug;

/**
 * Just a stub for displaying the value tree hierarchy
 * @author Shou Matsumoto
 *
 */
public class ValueTreePanelBuilder implements IProbabilityFunctionPanelBuilder {

	private ValueTreeProbabilisticNode owner;

	/**
	 * Default constructor is kept public so that plugin infrastructure can instantiate easily
	 */
	public ValueTreePanelBuilder() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#setProbabilityFunctionOwner(unbbayes.prs.Node)
	 */
	public void setProbabilityFunctionOwner(Node node) {
		if (node instanceof ValueTreeProbabilisticNode) {
			owner = (ValueTreeProbabilisticNode) node;
		} else {
			System.err.println("Attempted to handle node " + node + " as a node with value trees, but its class was not " + ValueTreeProbabilisticNode.class.getName());
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#getProbabilityFunctionOwner()
	 */
	public Node getProbabilityFunctionOwner() {
		return owner;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#buildProbabilityFunctionEditionPanel()
	 */
	public JPanel buildProbabilityFunctionEditionPanel() {
		JPanel ret = new JPanel();
		ret.setBackground(Color.WHITE);
		
		// generate a visualization of Value Tree. Start from the root, of course
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(owner);
		
		// fill the tree structure of the value tree. 
		for (IValueTreeNode child : owner.getValueTree().get1stLevelNodes()) {
			this.addChildRecursively(root, child);
		}
		// set up the jtree
		final JTree jTree = new JTree(root);
		jTree.setEditable(false);
		jTree.setDragEnabled(false);
		jTree.setBackground(Color.WHITE);
		jTree.setToolTipText("2R: change prob; ctrlR: set anchor; L: child; ctrlL: name; " +
				"altR: check prob; shiftR: shadow; " +
				"shiftL: delete.");
		
		// add a label of marginals 
		String marginals = "";
		for (int i = 0; i < owner.getStatesSize(); i++) {
			marginals += owner.getStateAt(i) + "=" + owner.getMarginalAt(i) + "; ";
		}
		final JLabel label = new JLabel(marginals);
		
		
		// set up the behavior of the jtree
		jTree.addMouseListener(new MouseListener() {
			private IValueTreeNode anchor = null;
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			/**
			 * Right click: changes prob
			 * Ctrl+Right click: set as anchor
			 * Left click: adds child
			 * Ctrl+left click: changes name
			 */
			public void mouseClicked(MouseEvent e) {
				Object selectedObj = null;
				// extract clicked element
				try {
					selectedObj = ((DefaultMutableTreeNode)((JTree)e.getSource()).getSelectionPath().getLastPathComponent()).getUserObject();
				} catch (Exception exp) {
					exp.printStackTrace();
					JOptionPane.showMessageDialog(null, "Could not extract selected object. Please make sure you have selected a node in the tree.");
					return;
				}
				if (selectedObj == null) {
					JOptionPane.showMessageDialog(null, "Selected object has no user object inside.","Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			
				// check if it was right or left click
				if (e.getButton() == e.BUTTON1) {
					// was right click. See if ctrl was pressed
					if (e.isControlDown()) {
						Debug.println(this.getClass() , "Control right click. Set " + selectedObj +" as anchor");
						if (selectedObj instanceof IValueTreeNode) {
							anchor = (IValueTreeNode) selectedObj;
						} else if (selectedObj instanceof ValueTreeProbabilisticNode) {
							anchor = null;
						} 
						JOptionPane.showMessageDialog(null,  anchor + " was set as anchor.");
					} else if (e.isAltDown()) {
						Debug.println(this.getClass() , "Alt right click. Check probability of " + selectedObj + " given " + anchor);
						
						try {
							// extract probability to display
							float prob = 1f;	
							if (selectedObj instanceof IValueTreeNode) {
								IValueTreeNode valueTreeNode = (IValueTreeNode) selectedObj;
								prob = valueTreeNode.getValueTree().getProb(valueTreeNode, anchor);
							} // if root, then probability is 1 anyway, so don't change prob.
							
							// display probability
							JOptionPane.showMessageDialog(null,  "P("+selectedObj+"|"+anchor+") = " + prob);
						} catch (Exception e2) {
							e2.printStackTrace();
							JOptionPane.showMessageDialog(null, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					} else if (e.isShiftDown()) {
						if (selectedObj instanceof IValueTreeNode) {
							Debug.println(this.getClass() , "Shift right click. Set " + selectedObj + " as shadow node.");
							IValueTreeNode valueTreeNode = (IValueTreeNode) selectedObj;
							valueTreeNode.getValueTree().setAsShadowNode(valueTreeNode);
							// prepare to print content of shadow nodes
							String shadowNodes = "";
							int shadowNodeSize = valueTreeNode.getValueTree().getShadowNodeSize();
							for (int i = 0; i < shadowNodeSize; i++) {
								shadowNodes += "|" + valueTreeNode.getValueTree().getShadowNode(i) ;
								shadowNodes += " (" + valueTreeNode.getValueTree().getRoot().getStateAt(i) + ")|";
							}
							// update marginal
							String marginals = "";
							for (int i = 0; i < owner.getStatesSize(); i++) {
								marginals += owner.getStateAt(i) + "=" + owner.getMarginalAt(i) + "; ";
							}
							label.setText(marginals);
							label.updateUI();
							label.repaint();
							JOptionPane.showMessageDialog(null, "Shadow node added: " + shadowNodes);
						} else if (selectedObj instanceof ValueTreeProbabilisticNode) {
							Debug.println(this.getClass() , "Shift right click. Remove last shadow node.");
							ValueTreeProbabilisticNode node = (ValueTreeProbabilisticNode) selectedObj;
							node.getValueTree().removeLastShadowNode();
							// prepare to print content of shadow nodes
							String shadowNodes = "";
							int shadowNodeSize = node.getValueTree().getShadowNodeSize();
							for (int i = 0; i < shadowNodeSize; i++) {
								shadowNodes += "|" + node.getValueTree().getShadowNode(i) ;
								shadowNodes += " (" + node.getValueTree().getRoot().getStateAt(i) + ")|";
							}
							// update marginal
							String marginals = "";
							for (int i = 0; i < owner.getStatesSize(); i++) {
								marginals += owner.getStateAt(i) + "=" + owner.getMarginalAt(i) + "; ";
							}
							label.setText(marginals);
							label.updateUI();
							label.repaint();
							JOptionPane.showMessageDialog(null, "Last shadow node removed: " + shadowNodes);
						}
					} else if (e.getClickCount() >= 2) {
						Debug.println(this.getClass() , "Right double click. Change probability of " + selectedObj);
						if (selectedObj instanceof IValueTreeNode) {
							IValueTreeNode valueTreeNode = (IValueTreeNode) selectedObj;
							Debug.println(this.getClass() , "Value tree node selected");
							// display what nodes will be affected
							JOptionPane.showMessageDialog(null, valueTreeNode.getValueTree().getHighestRelativeSet(valueTreeNode, anchor), "Affected nodes:", JOptionPane.INFORMATION_MESSAGE);
							// obtain prob from user
							String input = JOptionPane.showInputDialog("Change probability", valueTreeNode.getValueTree().getProb(valueTreeNode, anchor));
							if (input != null && input.trim().length() > 0) {
								valueTreeNode.getValueTree().changeProb(valueTreeNode, anchor, Float.parseFloat(input), null 
//										,true
										);
							}
							
							// needs to refresh tree
							jTree.updateUI();
							jTree.repaint();
							jTree.getParent().repaint();
							// update marginal
							String marginals = "";
							for (int i = 0; i < owner.getStatesSize(); i++) {
								marginals += owner.getStateAt(i) + "=" + owner.getMarginalAt(i) + "; ";
							}
							label.setText(marginals);
							label.updateUI();
							label.repaint();
//						}  else {
//							JOptionPane.showMessageDialog(null,  "Click: changes prob;\nCtrl+click: set as anchor;\nLeft click: adds child;\nCtrl+left click: changes name;\nAlt+click: check probability given anchor;\nShift+click: set as shadow node (if root, then remove last shadow node);\nShift+left click: delete node.");
//							return;
						}
						
					}
				} else if (e.getButton() == e.BUTTON3) {
					// was left click. See if ctrl was pressed
					if (e.isControlDown()) {
						Debug.println(this.getClass() , "Control left click. Change name of " + selectedObj);
						if (selectedObj instanceof IValueTreeNode) {
							IValueTreeNode valueTreeNode = (IValueTreeNode) selectedObj;
							String input = JOptionPane.showInputDialog("Change name", valueTreeNode.getName());
							if (input != null && input.trim().length() > 0) {
								valueTreeNode.setName(input);
							} else {
								JOptionPane.showMessageDialog(null, input + " is an invalid name","Invalid name", JOptionPane.ERROR_MESSAGE);
							}
						} else if (selectedObj instanceof ValueTreeProbabilisticNode) {
							ValueTreeProbabilisticNode node = (ValueTreeProbabilisticNode) selectedObj;
							String input = JOptionPane.showInputDialog("Change name", node.getName());
							if (input != null && input.trim().length() > 0) {
								node.setName(input);
							} else {
								JOptionPane.showMessageDialog(null, input + " is an invalid name","Invalid name", JOptionPane.ERROR_MESSAGE);
								return;
							}
						} 
					} else if (e.isShiftDown()) {
						if (selectedObj instanceof IValueTreeNode) {
							Debug.println(this.getClass() , "Shift left click. Delete " + selectedObj);
							IValueTreeNode valueTreeNode = (IValueTreeNode) selectedObj;
							valueTreeNode.getValueTree().deleteNode(valueTreeNode);
							JOptionPane.showMessageDialog(null, selectedObj + " and decendants were deleted.");
						}
					} else {
						Debug.println(this.getClass() , "Left click. Add child to " + selectedObj);
						if (selectedObj instanceof IValueTreeNode) {
							IValueTreeNode valueTreeNode = (IValueTreeNode) selectedObj;
							String name = JOptionPane.showInputDialog("Name of new value node", "TreeValue"+valueTreeNode.getValueTree().getNodes().size());
							if (name != null && name.trim().length() > 0) {
								String faction = JOptionPane.showInputDialog("Faction of node \n(conditional probability given parent: a value between 0 and 1)", "0.5");
								if (faction != null && faction.trim().length() > 0) {
									try {
										valueTreeNode.getValueTree().addNode(name, valueTreeNode, Float.parseFloat(faction));
									} catch (Exception exp) {
										exp.printStackTrace();
										JOptionPane.showMessageDialog(null, faction + " is an invalid faction","Invalid faction", JOptionPane.ERROR_MESSAGE);
										return;
									}
									
								} 
							} else {
								JOptionPane.showMessageDialog(null, name + " is an invalid name","Invalid name", JOptionPane.ERROR_MESSAGE);
								return;
							}
						} else if (selectedObj instanceof ValueTreeProbabilisticNode) {
							ValueTreeProbabilisticNode node = (ValueTreeProbabilisticNode) selectedObj;
							String name = JOptionPane.showInputDialog("Name of new value node", node.getName() + "_"+node.getValueTree().getNodes().size());
							if (name != null && name.trim().length() > 0) {
								String faction = JOptionPane.showInputDialog("Faction of node \n(conditional probability given parent: a value between 0 and 1)", "0.5");
								if (faction != null && faction.trim().length() > 0) {
									try {
										node.getValueTree().addNode(name, null, Float.parseFloat(faction));
									} catch (NumberFormatException exp) {
										exp.printStackTrace();
										JOptionPane.showMessageDialog(null, faction + " is an invalid faction","Invalid faction", JOptionPane.ERROR_MESSAGE);
										return;
									} catch (NullPointerException e2) {
										e2.printStackTrace();
										JOptionPane.showMessageDialog(null, faction + " is an invalid faction","Invalid faction", JOptionPane.ERROR_MESSAGE);
										return;
									} catch (Exception e2) {
										e2.printStackTrace();
										JOptionPane.showMessageDialog(null, e2.getMessage(),"Could not add new node", JOptionPane.ERROR_MESSAGE);
										return;
									}
									
								} 
							} else {
								JOptionPane.showMessageDialog(null, name + " is an invalid name","Invalid name", JOptionPane.ERROR_MESSAGE);
								return;
							}
						} 
					}
					
					// both actions requres refresh of jtree
					jTree.updateUI();
					jTree.repaint();
					jTree.getParent().repaint();
					label.updateUI();
					label.repaint();
				} else {
					// unused button clicked
					JOptionPane.showMessageDialog(null,  "2-Click: changes prob;\nCtrl+click: set as anchor;\nLeft click: adds child;\nCtrl+left click: changes name;\nAlt+click: check probability given anchor;\nShift+click: set as shadow node (if root, then remove last shadow node);\nShift+left click: delete node.");
				}
			}
		});
		
//		ret.setLayout(new BorderLayout());
		
		// add the jtree to the panel to return
		ret.add(jTree);
//		ret.add(new JScrollPane(jTree), BorderLayout.CENTER);
//		ret.add(new JScrollPane(label), BorderLayout.SOUTH);
				
		
		return ret;
	}
	
	/**
	 * Simply convert hierarchy of value tree to DefaultMutableTreeNode
	 * @param parentTreeNode: where the subtree rooted by childValueTreeNode will be included
	 * @param childValueTreeNode : a sub tree with this node as root will be generated.
	 * Its children will be also accessed and this method will be recursively called in order
	 * to generate the sub tree.
	 */
	private void addChildRecursively(DefaultMutableTreeNode parentTreeNode, IValueTreeNode childValueTreeNode) {
		DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(childValueTreeNode);
		parentTreeNode.add(newTreeNode);
		if (childValueTreeNode.getChildren() != null) {
			for (IValueTreeNode childOfNextRecursion : childValueTreeNode.getChildren()) {
				this.addChildRecursively(newTreeNode, childOfNextRecursion);
			}
		}
	}

}
