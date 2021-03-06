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
package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import unbbayes.controller.IconController;
import unbbayes.controller.exception.InconsistentArgumentException;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.ParcialStateException;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.gui.mebn.auxiliary.ListCellRenderer;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.gui.mebn.util.OrganizerUtils;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * Class for insert a query
 *
 * Two panels: 
 * - In the first the user choice the randon variable
 * - In the second choice the arguments
 * 
 * @author Laecio Lima dos Santos
 */

public class QueryPanel extends JDialog{

	private MEBNController mebnController;
	private ResidentNode residentSelected;

	private IconController iconController = IconController.getInstance();
  	private static ResourceBundle resource =
  		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.gui.mebn.resources.Resources.class.getName());

	public QueryPanel(MEBNController mebnController){
		super(UnBBayesFrame.getIUnBBayes());
		this.setTitle(resource.getString("queryPanelTitle")); 
		this.setModal(true); 
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.mebnController = mebnController;
		showRandonVariableListPane(); 
	}

	/**
	 * Pane for arguments selection 
	 * @param _residentNode
	 */
	public void showArgumentsSelection(ResidentNode _residentNode){
		JPanel contentPane = new QueryArgumentsEditionPane(_residentNode);
		setContentPane(contentPane);
		validate(); 
		pack(); 
	}
	
	/**
	 * Pane for randon variable selection
	 */
	public void showRandonVariableListPane(){
		
		final JButton btnSelect;
		final JButton btnClose; 
		
		JPanel contentPane = new JPanel(new BorderLayout());

//		btnSelect = new JButton(resource.getString("queryBtnSelect"));
		btnSelect = new JButton(); 
		btnSelect.setIcon(iconController.getGoNextInstance());
		
		btnSelect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				showArgumentsSelection(residentSelected);
			}
		});
		
//		btnClose = new JButton(resource.getString("closeButton")); 
		btnClose = new JButton(); 
		btnClose.setIcon(iconController.getProcessStopInstance());
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				exit(); 
			}
		});

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setLayout(new GridLayout());
		
		toolBar.add(new JLabel());
		toolBar.add(btnClose);
		toolBar.add(btnSelect);

		RandonVariableListPane randomVariableListPane = new RandonVariableListPane();
		// select the node when enter is pressed
		randomVariableListPane.jlistResident.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					btnSelect.doClick();
					break;
				case KeyEvent.VK_ESCAPE:
					btnClose.doClick();
					break;
				default:
					break;
				}
			}
			public void keyPressed(KeyEvent e) {}
		});

		JLabel label = new JLabel(resource.getString("selectOneVariable") + "               ");
		
		//TODO put options of the algorithm here. 
		
		contentPane.add(label, BorderLayout.PAGE_START); 
		contentPane.add(randomVariableListPane, BorderLayout.CENTER);
		contentPane.add(toolBar, BorderLayout.PAGE_END);

		setContentPane(contentPane);
		validate(); 
		pack(); 
	}
	
	public void exit(){
		this.dispose(); 
	}

	public void makeInvisible(){
		this.setVisible(false); 
	}
	
	private class RandonVariableListPane extends JPanel{

		private JList jlistResident;
		private JScrollPane scrollListObjectEntity;
		private DefaultListModel listModel;

		public RandonVariableListPane(){

			super(new BorderLayout());

			listModel = new DefaultListModel();
			
			List<ResidentNode> listResident = 
				OrganizerUtils.createOrderedResidentNodeList(
						mebnController.getMultiEntityBayesianNetwork()); 
			
			for(ResidentNode node: listResident){
				listModel.addElement(node);
			}

			jlistResident = new JList(listModel);
			scrollListObjectEntity = new JScrollPane(jlistResident);

			jlistResident.setCellRenderer(new ListCellRenderer(iconController.getYellowNodeIcon()));

			jlistResident.addListSelectionListener(
		            new ListSelectionListener(){
		                public void valueChanged(ListSelectionEvent e) {
		                	if(jlistResident.getSelectedValue() != null){
		                	   residentSelected = (ResidentNode)jlistResident.getSelectedValue();
		                	}
		                }
		            }
			 );

			this.add(scrollListObjectEntity, BorderLayout.CENTER);

		}
	}

	/**
	 * Pane contains:
	 * - Name of ResidentNode
	 * - List of arguments
	 * - Selection of state
	 * - buttons for actions
	 *
	 * @author Laecio Lima dos Santos (laecio@gmail.com)
	 * @version 1.0 (09/09/07)
	 */
	private class QueryArgumentsEditionPane extends JPanel{

		private final ResidentNode residentNode;

		private JLabel nodeName;

		private QueryArgumentsPane queryArgumentsPane;

		private JButton btnBack; 
		private JButton btnExecute;
		private JButton btnClose; 

		private JToolBar jtbOptions;

		public QueryArgumentsEditionPane(ResidentNode _residentNode){

			super(new BorderLayout());
			this.residentNode = _residentNode;

			nodeName = new JLabel(residentNode.getName());
			nodeName.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			nodeName.setBackground(Color.YELLOW);

			queryArgumentsPane = new QueryArgumentsPane(residentNode, mebnController);

//			btnBack = new JButton(resource.getString("queryBtnBack"));
			btnBack = new JButton(); 
			btnBack.setIcon(iconController.getGoPreviousInstance()); 
			
//			btnExecute = new JButton(resource.getString("queryBtnExecute"));
			btnExecute = new JButton(); 
			btnExecute.setIcon(iconController.getCompileIcon());
//			iconController.get
//			btnExecute.setBackground(MebnToolkit.getColor1());
			
//			btnClose = new JButton(resource.getString("closeButton"));
			btnClose = new JButton(); 
			btnClose.setIcon(iconController.getProcessStopInstance());

			jtbOptions = new JToolBar();
			
			jtbOptions.setLayout(new GridLayout());
			
			jtbOptions.add(new JLabel());
			jtbOptions.add(btnBack);
			jtbOptions.add(btnClose);
			
			jtbOptions.add(btnExecute);
			jtbOptions.setFloatable(false);

			btnBack.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					showRandonVariableListPane(); 
				}
			});

			btnExecute.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					makeInvisible(); 
					try {
						ObjectEntityInstance[] arguments = queryArgumentsPane.getArguments();
						
						setVisible(false); 
						
						mebnController.getScreen().setCursor(new Cursor(Cursor.WAIT_CURSOR)); 
						
						
						//ALG1
//				        ProbabilisticNetwork network = mebnController.executeQuery(residentNode, arguments);
						
				        
				        
				        //ALG2
						List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
						
						List<Argument> arglist = residentNode.getArgumentList();
						
						if (arglist.size() != arguments.length) {
							throw new InconsistentArgumentException();
						}
						
						for (int i = 1; i <= arguments.length; i++) {

							//TODO It has to get in the right order. For some reason in argList, 
							// sometimes the second argument comes first
							for (Argument argument : arglist) {
								if (argument.getArgNumber() == i) {
									OrdinaryVariable ov = argument.getOVariable(); 
									OVInstance ovInstance = OVInstance.getInstance(
											ov, 
											LiteralEntityInstance.getInstance(arguments[i-1].getName(), ov.getValueType()));
									ovInstanceList.add(ovInstance); 
									break;
								}
							}


						}
						
						Query query = new Query(residentNode, ovInstanceList); 
						
						List<Query> queryList = new ArrayList<Query>();
						queryList.add(query); 
                        mebnController.executeQuery(queryList);
						
				        mebnController.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						
				        exit();
					}
				    catch (MEBNException e0) {
				    	e0.printStackTrace();
							JOptionPane.showMessageDialog(mebnController.getScreen(), 
									e0.getMessage(),
									resource.getString("error"),
									JOptionPane.ERROR_MESSAGE);
					} catch (ParcialStateException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(mebnController.getScreen(), 
								resource.getString("argumentFault"),
								resource.getString("error"),
								JOptionPane.ERROR_MESSAGE);
					} catch (InconsistentArgumentException iae) {
						iae.printStackTrace();
						if(iae.getMessage().isEmpty()){
							JOptionPane.showMessageDialog(mebnController.getScreen(), 
									resource.getString("inconsistentArgument"),
									resource.getString("error"),
									JOptionPane.ERROR_MESSAGE);
						}else{
							JOptionPane.showMessageDialog(mebnController.getScreen(), 
									iae.getMessage(),
									resource.getString("error"),
									JOptionPane.ERROR_MESSAGE);
						}
					} catch (SSBNNodeGeneralException e2) {
						e2.printStackTrace();
						JOptionPane.showMessageDialog(mebnController.getScreen(), 
								e2.getMessage(),
								resource.getString("error"),
								JOptionPane.ERROR_MESSAGE);
					} catch (ImplementationRestrictionException e3) {
						e3.printStackTrace();
						JOptionPane.showMessageDialog(mebnController.getScreen(), 
								e3.getMessage(),
								resource.getString("error"),
								JOptionPane.ERROR_MESSAGE);
					} catch (Exception e4) {
						e4.printStackTrace();
						JOptionPane.showMessageDialog(mebnController.getScreen(), 
								e4.getMessage(),
								resource.getString("error"),
								JOptionPane.ERROR_MESSAGE);
				}
				}
			});

			btnClose.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					exit(); 
				}
			});
			
			this.add(new JLabel(resource.getString("selectArgsValues")), BorderLayout.PAGE_START); 
			this.add(new JScrollPane(queryArgumentsPane), BorderLayout.CENTER);
			
//			optionsPanel = new JPanel(new GridLayout(2,1)); 
//			
//			JToolBar panelAlgorithm = new JToolBar();
//			panelAlgorithm.add(new JLabel("SSBNAlgorithm: "));
//			panelAlgorithm.add(new JButton(mebnController.getSSBNGenerator().toString())); 
//			
//			optionsPanel.add(panelAlgorithm); 
//			
//			optionsPanel.add(jtbOptions); 
			
			// make sure we can go back or forward by pressing enter/escape
			for (JComboBox comboBox : queryArgumentsPane.getArgumentComboBoxes()) {
				comboBox.getEditor().getEditorComponent().addKeyListener(new KeyListener() {
					public void keyTyped(KeyEvent e) {}
					public void keyReleased(KeyEvent e) {
						switch (e.getKeyCode()) {
						case KeyEvent.VK_ENTER:
							btnExecute.doClick();
							break;
						case KeyEvent.VK_ESCAPE:
							btnClose.doClick();
							break;
						default:
							break;
						}
					}
					public void keyPressed(KeyEvent e) {}
				});
			}
			
			this.add(jtbOptions, BorderLayout.PAGE_END);

		}

	}

}
