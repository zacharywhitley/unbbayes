package unbbayes.controller;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import unbbayes.gui.MSBNWindow;
import unbbayes.gui.NetWindow;
import unbbayes.prs.msbn.MSNetwork;
import unbbayes.prs.msbn.SubNetwork;

/**
 * @author Michael Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MSBNController {
	private MSNetwork msbn;
	private MSBNWindow window;
	private NetWindow active;
	
	public MSBNController(MSNetwork msbn) {
		this.msbn = msbn;
		window = new MSBNWindow(msbn);
		init();
		addListeners();		
	}
	
	private JTree makeJTree() {		
		TreeNode node = getTreeNode(msbn.getNetAt(0));
		JTree tree = new JTree(node, true);
		tree.setToggleClickCount(Integer.MAX_VALUE);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		addTreeMouseListener(tree);		
		return tree;
	}
	
	private MutableTreeNode getTreeNode(SubNetwork net) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(net);
		for (int i = net.getAdjacentsSize()-1; i>=0; i--) {
			node.add(getTreeNode(net.getAdjacentAt(i)));						
		}
		return node;
	}
	
	private void addTreeMouseListener(final JTree tree) {
		MouseListener ml = new MouseAdapter() {
		     public void mousePressed(MouseEvent e) {
		         int selRow = tree.getRowForLocation(e.getX(), e.getY());
		         if(selRow != -1) {
					 if(e.getModifiers() == MouseEvent.BUTTON1_MASK) {					 	
					 	DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
					 	SubNetwork net = (SubNetwork) node.getUserObject();
			            if (net == active.getRede() 
			            	 || JOptionPane.showConfirmDialog(window, "Shift Attention?") != JOptionPane.OK_OPTION) {
			            	return;
			            }
			            
			            for (int i = 0; i < net.getNodeCount(); i++) {
			            	net.getNodeAt(i).setSelected(false);			            	
			            }
			            
			           	NetWindow netWindow = new NetWindow(net);
			            changeActive(netWindow);            
		            	msbn.shiftAttention(net);
		            	netWindow.changeToNetCompilation();
		            	tree.scrollRowToVisible(selRow);
		             }
		         }
		     }
		 };
		 
		 tree.addMouseListener(ml);	
	}
	
	public JInternalFrame getPanel() {
		return window;	
	}
	
	private void init() {
		window.getNetList().setSelectedIndex(0);
		changeActive(new NetWindow(msbn.getNetAt(0)));	
	}
	
	private void changeActive(NetWindow newWindow) {
		if (active != null) {
			window.getContentPane().remove(active.getContentPane());
		}
		active = newWindow;
		active.getNetWindowEdition().getCompile().setVisible(false);
		active.getNetWindowCompilation().getEditMode().setVisible(false);
		window.getContentPane().add(active.getContentPane(), BorderLayout.CENTER);
		window.updateUI();
	}
	
	
	public void addListeners() {
		MouseListener mouseListener = new MouseAdapter() {
		     public void mouseClicked(MouseEvent e) {
		     	if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
		     		int index = window.getNetList().locationToIndex(e.getPoint());
		            if (index < 0 || window.getNetList().getModel().getElementAt(index) == active.getRede()) {
		            	return;		             	
		            } 
		            
		           	NetWindow netWindow = new NetWindow(msbn.getNetAt(index));
		            changeActive(netWindow);	            
		     	}
		     }
		};
		
		window.addListMouseListener(mouseListener);
		
		window.addEditionActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				active.changeToNetEdition();
				window.showBtnPanel(MSBNWindow.EDITION_PANE);
				window.changeToListView();
			}
		});
		
		window.addNewBtnActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				msbn.addNetwork(new SubNetwork("new net " + msbn.getNetCount()));
				window.getNetList().updateUI();
			}
		});
		
		window.addRemoveBtnActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = window.getNetList().getSelectedIndex();
				if (index < 0) {
					return;
				}
				msbn.remove(index);
				window.getNetList().repaint();
			}
		});
		
		window.addCompileBtnActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					msbn.compile();
					active.changeToNetCompilation();
					window.changeToTreeView(makeJTree());
					window.showBtnPanel(MSBNWindow.COMPILED_PANE);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(window, e.getMessage(), "Compilation error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();			
				}
			}
		});
	}
}