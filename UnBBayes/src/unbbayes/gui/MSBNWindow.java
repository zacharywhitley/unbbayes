package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;

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
public class MSBNWindow extends JPanel {
	
	private class MSBNListModel extends AbstractListModel {
		public int getSize() {
			return msbn.getNetCount();
		}

		public Object getElementAt(int index) {
			return msbn.getNetAt(index);
		}
	}
	
	private boolean compiled;
	private MSNetwork msbn;
	private NetWindow active;
	private JList netList;
	
	public MSBNWindow(MSNetwork msbn) {
		this.msbn = msbn;		
		netList = new JList(new MSBNListModel());
		setLayout(new BorderLayout());
		add(makeListPanel(), BorderLayout.WEST);
		addListeners();
		init();
	}
	
	private void init() {
		netList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		netList.setSelectedIndex(0);
		active = new NetWindow(msbn.getNetAt(0));
		add(active, BorderLayout.CENTER);	
	}
	
	private JPanel makeListPanel() {
		JPanel pane = new JPanel(new BorderLayout());
		pane.add(new JLabel("Networks"), BorderLayout.NORTH);
		JScrollPane jsp = new JScrollPane(netList);
		pane.add(jsp, BorderLayout.CENTER);
		pane.add(makeButtons(), BorderLayout.SOUTH);		
		
		return pane;
	}
	
	private JPanel makeButtons() {
		final CardLayout card = new CardLayout();
		final JPanel pane = new JPanel(card);
		
		JPanel editionPane = new JPanel(new GridLayout(0,1));
		pane.add(editionPane, "editionPane");
		JButton novo = new JButton("New");				
		editionPane.add(novo);
		
		JButton remove = new JButton("Remove");		
		editionPane.add(remove);
				
		JButton compile = new JButton("Compile");		
		editionPane.add(compile);
		
		JPanel compiledPane = new JPanel();
		JButton editionBtn = new JButton("Edit MSBN");
		compiledPane.add(editionBtn);
		pane.add(compiledPane, "compiledPane");
		
		editionBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				active.changeToNetEdition();
				card.show(pane, "editionPane");
				compiled = false;			
			}
		});
		
		novo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				msbn.addNetwork(new SubNetwork("new net " + msbn.getNetCount()));
				netList.updateUI();
			}
		});
		
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = netList.getSelectedIndex();
				if (index < 0) {
					return;
				}
				msbn.remove(index);
				netList.repaint();
			}
		});
		
		compile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					msbn.compile();					
					active.changeToNetCompilation();
					card.show(pane, "compiledPane");
					compiled = true;
				} catch (Exception e) {
					e.printStackTrace();					
				}
			}
		});
		
		
		card.show(pane, "editionPane");
		return pane;
	}
	
	private void addListeners() {
		MouseListener mouseListener = new MouseAdapter() {
		     public void mouseClicked(MouseEvent e) {
		     	if (e.getClickCount() == 2 && e.getModifiers() == MouseEvent.BUTTON1_MASK) {
		     		int index = netList.locationToIndex(e.getPoint());
		            if (index < 0 || netList.getModel().getElementAt(index) == active.getRede()) {
		            	return;		             	
		            }	            
		            
		            if (compiled  
		            		&& JOptionPane.showConfirmDialog(netList, "Shift Attention?") != JOptionPane.OK_OPTION) {
		            			
			          	return;		        
		           	}
		            remove(active);
		            active = new NetWindow(msbn.getNetAt(index));
		            add(active, BorderLayout.CENTER);
		            updateUI();
		            if (compiled) {
		            	msbn.shiftAttention(msbn.getNetAt(index));
		            	active.changeToNetCompilation();
		            }
		     	}
		     }
		};
		netList.addMouseListener(mouseListener);
	}
}
