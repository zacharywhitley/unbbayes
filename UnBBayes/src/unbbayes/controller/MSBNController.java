package unbbayes.controller;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
	private class MSBNListModel extends AbstractListModel {
		public int getSize() {
			return msbn.getNetCount();
		}

		public Object getElementAt(int index) {
			return msbn.getNetAt(index);
		}
	}
	
	private MSNetwork msbn;
	private MSBNWindow window;
	private boolean compiled;
	private NetWindow active;
	
	public MSBNController(MSNetwork msbn) {
		this.msbn = msbn;
		window = new MSBNWindow(new MSBNListModel());
		init();
		addListeners();		
	}
	
	public JPanel getPanel() {
		return window;		
	}
	
	private void init() {
		window.getNetList().setSelectedIndex(0);
		changeActive(new NetWindow(msbn.getNetAt(0)));	
	}
	
	private void changeActive(NetWindow newWindow) {
		if (active != null) {
			window.remove(active);
		}
		active = newWindow;
		active.getNetWindowEdition().getCompile().setVisible(false);
		active.getNetWindowCompilation().getEditMode().setVisible(false);
		window.add(active, BorderLayout.CENTER);
		window.updateUI();
	}
	
	public void addListeners() {
		MouseListener mouseListener = new MouseAdapter() {
		     public void mouseClicked(MouseEvent e) {
		     	if (e.getClickCount() == 2 && e.getModifiers() == MouseEvent.BUTTON1_MASK) {
		     		int index = window.getNetList().locationToIndex(e.getPoint());
		            if (index < 0 || window.getNetList().getModel().getElementAt(index) == active.getRede()) {
		            	return;		             	
		            } 
		            
		            if (compiled  
		            		&& JOptionPane.showConfirmDialog(window, "Shift Attention?") != JOptionPane.OK_OPTION) {
		            			
			          	return;		        
		           	}
		           	NetWindow netWindow = new NetWindow(msbn.getNetAt(index));
		            changeActive(netWindow);		            
		            if (compiled) {
		            	msbn.shiftAttention(msbn.getNetAt(index));
		            	netWindow.changeToNetCompilation();
		            }
		     	}
		     }
		};
		
		window.addListMouseListener(mouseListener);
		
		window.addEditionActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				active.changeToNetEdition();
				window.showBtnPanel(MSBNWindow.EDITION_PANE);
				compiled = false;			
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
					window.showBtnPanel(MSBNWindow.COMPILED_PANE);
					compiled = true;
				} catch (Exception e) {
					JOptionPane.showMessageDialog(window, e.getMessage(), "Compilation error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();			
				}
			}
		});
	}
}