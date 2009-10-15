/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.controller;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import unbbayes.gui.MSBNWindow;
import unbbayes.gui.NetworkWindow;
import unbbayes.io.BaseIO;
import unbbayes.io.msbn.impl.DefaultMSBNIO;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.msbn.SingleAgentMSBN;
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
	private SingleAgentMSBN msbn;
	private MSBNWindow window;
	private NetworkWindow active;
	
	private BaseIO msbnIO = DefaultMSBNIO.newInstance();
	
	/**
	 * Creates a controller that controls a MSBNWindow.
	 * The MSBNWindows is created.
	 * @param msbn The msbn to display.
	 * 
	 */
	public MSBNController(SingleAgentMSBN msbn) {
		this.msbn = msbn;
		// TODO solve dependency inversion (a controller calling a GUI might not be very flexible)
		window = new MSBNWindow(msbn, this);
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
			            if (net == active.getSingleEntityNetwork() 
			            	 || JOptionPane.showConfirmDialog(window, "Shift Attention?") != JOptionPane.OK_OPTION) {
			            	return;
			            }
			            try {
			           		NetworkWindow netWindow = new NetworkWindow(net);
			            	changeActive(netWindow);
		            		msbn.shiftAttention(net);
			            	netWindow.changeToPNCompilationPane();
			            	tree.scrollRowToVisible(selRow);
			            } catch (Exception ex) {
			            	ex.printStackTrace();			            	
			            }
		             }
		         }
		     }
		 };
		 
		 tree.addMouseListener(ml);	
	}
	
	/**
	 * Gets the MSBNWindow associated with this controller.
	 * @return JInternalFrame
	 */
	public JInternalFrame getPanel() {
		return window;	
	}
	
	private void init() {
		window.getNetList().setSelectedIndex(0);
		if (msbn.getNetCount() > 0) {
			changeActive(new NetworkWindow(msbn.getNetAt(0)));
		}	
	}
	
	private void changeActive(NetworkWindow newWindow) {				
		if (active != null) {
			SingleEntityNetwork net = active.getSingleEntityNetwork();
			for (int i = 0; i < net.getNodeCount(); i++) {
				net.getNodeAt(i).setSelected(false);			            	
			}
			window.getContentPane().remove(active.getContentPane());			
		}	
		
		active = newWindow;
		if (newWindow == null) {
			return;			
		}
		active.getNetWindowEdition().getBtnCompile().setVisible(false);
		active.getNetWindowCompilation().getEditMode().setVisible(false);
		window.getContentPane().add(active.getContentPane(), BorderLayout.CENTER);
		window.updateUI();
	}
	
	
	private void addListeners() {
		MouseListener mouseListener = new MouseAdapter() {
		     public void mouseClicked(MouseEvent e) {
		     	if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
		     		int index = window.getNetList().locationToIndex(e.getPoint());
		            if (index < 0 || window.getNetList().getModel().getElementAt(index) == active.getSingleEntityNetwork()) {
		            	return;		             	
		            } 
		            
		           	NetworkWindow netWindow = new NetworkWindow(msbn.getNetAt(index));
		            changeActive(netWindow);	            
		     	}
		     }
		};
		
		window.addListMouseListener(mouseListener);
		
		window.addEditionActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				active.changeToPNEditionPane();
				window.showBtnPanel(MSBNWindow.EDITION_PANE);
				window.changeToListView();
			}
		});
		
		final ActionListener newBtnAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				msbn.addNetwork(new SubNetwork("new net " + msbn.getNetCount()));
				if (msbn.getNetCount() == 1) {
					changeActive(new NetworkWindow(msbn.getNetAt(0)));					
				}
				window.getNetList().updateUI();
			}
		};		
		window.addNewBtnActionListener(newBtnAction);
		
		window.addRemoveBtnActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = window.getNetList().getSelectedIndex();
				if (index < 0) {
					return;
				}
				msbn.remove(index);
				if (msbn.getNetCount() > 0) {
					NetworkWindow netWindow = new NetworkWindow(msbn.getNetAt(0));										
					changeActive(netWindow);
				} else {
					newBtnAction.actionPerformed(null);
					changeActive(new NetworkWindow(msbn.getNetAt(0)));					
				}
				window.getNetList().setSelectedIndex(0);
				window.getNetList().repaint();
			}
		});
		
		window.addCompileBtnActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					msbn.compile((SubNetwork) active.getSingleEntityNetwork());
					active.changeToPNCompilationPane();
					window.changeToTreeView(makeJTree());
					window.showBtnPanel(MSBNWindow.COMPILED_PANE);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(window, e.getMessage(), "Compilation error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();			
				}
			}
		});
	}

	/**
	 * @return the msbnIO
	 */
	public BaseIO getMsbnIO() {
		return msbnIO;
	}

	/**
	 * @param msbnIO the msbnIO to set
	 */
	public void setMsbnIO(BaseIO msbnIO) {
		this.msbnIO = msbnIO;
	}
}