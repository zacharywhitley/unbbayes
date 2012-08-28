/**
 * 
 */
package unbbayes.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import unbbayes.controller.INetworkMediator;
import unbbayes.controller.IconController;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.ICompilationPanelBuilder;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * @author Shou Matsumoto
 *
 */
public class AssetCompilationPanelBuilder implements ICompilationPanelBuilder {
	
	private String userManagementTabTitle = "User Management";
	private String probabilityTabTitle = "Probability";
	
	private Map<String,Graph> userToassetNetMap = new HashMap<String,Graph>();

	private PNCompilationPane compilationPane;
	private JTabbedPane mainTabPane;
	private JScrollPane mainScrollPane;
	private IAssetNetAlgorithm algorithm;
	private INetworkMediator mediator;
	private ChangeListener tabChangeListener;
	private ControlPanel controllPanel;

	/**
	 * 
	 */
	public AssetCompilationPanelBuilder() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.ICompilationPanelBuilder#buildCompilationPanel(unbbayes.util.extension.bn.inference.IInferenceAlgorithm, unbbayes.controller.INetworkMediator)
	 */
	public JComponent buildCompilationPanel(IInferenceAlgorithm algorithm, INetworkMediator mediator) {
		this.algorithm = (IAssetNetAlgorithm) algorithm;
		this.mediator = mediator;
		
		this.initComponents();
		this.initListeners();
		
		return this.getMainScrollPane();
	}


	protected void initComponents() {
		this.setMainTabPane(new JTabbedPane());
		this.setMainScrollPane(new JScrollPane(this.getMainTabPane()));	
		
		this.setControllPanel(new ControlPanel());
		this.getMainTabPane().add(new JScrollPane(getControllPanel()), this.getUserManagementTabTitle());
		
		
		this.setCompilationPane(mediator.getScreen().getNetWindowCompilation());
		this.getMainTabPane().add(getCompilationPane(), getProbabilityTabTitle());
		
//		try {
//			this.addAssetNetToTab(getAlgorithm().createAssetNetFromProbabilisticNet(getAlgorithm().getRelatedProbabilisticNetwork()), "User 1");
//		} catch (InvalidParentException e) {
//			JOptionPane.showMessageDialog(this.getMainScrollPane(), "Could not create first user view.");
//		}
	}
	

	protected void initListeners() {
		tabChangeListener = new ChangeListener() {
			

			public void stateChanged(ChangeEvent e) {
				Debug.println(getClass(), "View changed to user " + getMainTabPane().getTitleAt(getMainTabPane().getSelectedIndex()));
				if (getUserToassetNetMap().containsKey(getMainTabPane().getTitleAt(getMainTabPane().getSelectedIndex()))
						&& (getMainTabPane().getSelectedComponent() instanceof PNCompilationPane) ) {
					// if selected tab is a user tab, then update evidence tree
					PNCompilationPane assetPane = (PNCompilationPane) getMainTabPane().getSelectedComponent();
					assetPane.getEvidenceTree().updateTree(true);
					
					// update currently selected user
					Graph graph = getUserToassetNetMap().get(getMainTabPane().getTitleAt(getMainTabPane().getSelectedIndex()));
					try {
						getAlgorithm().setAssetNetwork((AssetNetwork) graph);
					}catch (Exception exc) {
						exc.printStackTrace();
						JOptionPane.showMessageDialog(getMainTabPane(), "Could not switch user to " 
								+ getMainTabPane().getTitleAt(getMainTabPane().getSelectedIndex())
								+ ": " + exc.getMessage());
					}
				} else if (getMainTabPane().getTitleAt(getMainTabPane().getSelectedIndex()).equals(getUserManagementTabTitle())) {
					// update currently selected user label
					getControllPanel().getCurrentlySelectedUserLabel().setText(getAlgorithm().getAssetNetwork().getName());
					getControllPanel().getCurrentlySelectedUserLabel().updateUI();
					getControllPanel().getCurrentlySelectedUserLabel().repaint();
				}
			}
		};
		this.getMainTabPane().addChangeListener(tabChangeListener);
	}
	
	/**
	 * Add a new tab given an asset net
	 * @param assetNet
	 */
	public void addAssetNetToTab(Graph assetNet, String name) {
		
		// init graphical requirements of network, because they are not usually initialized when called from non-GUI apps.
		if (assetNet instanceof SingleEntityNetwork) {
			SingleEntityNetwork singleEntityNetwork = (SingleEntityNetwork) assetNet;
			if (singleEntityNetwork.getHierarchicTree() == null) {
				singleEntityNetwork.setHierarchicTree(new HierarchicTree(new DefaultTreeModel(new DefaultMutableTreeNode("root"))));
			}
		}
		// init graphical requirements of nodes, because they are not usually initialized when called from non-GUI apps
		for (Node node : assetNet.getNodes()) {
			if (node instanceof AssetNode) {
				AssetNode assetNode = (AssetNode) node;
				assetNode.setPosition(10.0, 10.0);
				assetNode.setSize(50.0, 50.0);
				assetNode.setColor(Color.YELLOW);
			}
		}
		
		NetworkWindow assetWindow = new NetworkWindow((Network) assetNet);
		assetWindow.changeToPNCompilationPane();
		
		PNCompilationPane assetCompPane = assetWindow.getNetWindowCompilation();
		assetCompPane.getJtbCompilation().setVisible(false);
		assetCompPane.getTopPanel().remove(assetCompPane.getJtbCompilation());
		this.getMainTabPane().add(assetCompPane,name);
		
		this.getUserToassetNetMap().put(name, assetNet);
		getAlgorithm().setAssetNetwork((AssetNetwork) assetNet);
		getControllPanel().getCurrentlySelectedUserLabel().setText(((Network) assetNet).getName());
		getControllPanel().getCurrentlySelectedUserLabel().updateUI();
		getControllPanel().getCurrentlySelectedUserLabel().repaint();
	}

	/**
	 * @return the compilationPane
	 */
	public PNCompilationPane getCompilationPane() {
		return compilationPane;
	}

	/**
	 * @param compilationPane the compilationPane to set
	 */
	public void setCompilationPane(PNCompilationPane compilationPane) {
		this.compilationPane = compilationPane;
	}

	/**
	 * @return the mainPanel
	 */
	public JTabbedPane getMainTabPane() {
		return mainTabPane;
	}

	/**
	 * @param mainTabPane the mainTabPane to set
	 */
	public void setMainTabPane(JTabbedPane mainTabPane) {
		this.mainTabPane = mainTabPane;
	}

	/**
	 * @return the mainScrollPane
	 */
	public JScrollPane getMainScrollPane() {
		return mainScrollPane;
	}

	/**
	 * @param mainScrollPane the mainScrollPane to set
	 */
	public void setMainScrollPane(JScrollPane mainScrollPane) {
		this.mainScrollPane = mainScrollPane;
	}

	/**
	 * @return the algorithm
	 */
	public IAssetNetAlgorithm getAlgorithm() {
		return algorithm;
	}

	/**
	 * @param algorithm the algorithm to set
	 */
	public void setAlgorithm(IAssetNetAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

//	/**
//	 * @return the tabChangeListener
//	 */
//	public ChangeListener getTabChangeListener() {
//		return tabChangeListener;
//	}
//
//	/**
//	 * @param tabChangeListener the tabChangeListener to set
//	 */
//	public void setTabChangeListener(ChangeListener tabChangeListener) {
//		this.tabChangeListener = tabChangeListener;
//	}

	/**
	 * @return the controllPanel
	 */
	public ControlPanel getControllPanel() {
		return controllPanel;
	}

	/**
	 * @param controllPanel the controllPanel to set
	 */
	public void setControllPanel(ControlPanel controllPanel) {
		this.controllPanel = controllPanel;
	}

	/**
	 * @return the mediator
	 */
	public INetworkMediator getMediator() {
		return mediator;
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(INetworkMediator mediator) {
		this.mediator = mediator;
	}

	
	public class ControlPanel extends JPanel {

		private JButton addUserButton;
		private JTextField userNameTextField;
		private JButton returnEditModeButton;
		private JLabel currentlySelectedUserLabel;

		public ControlPanel() {
			this(true);
		}

		public ControlPanel(boolean isDoubleBuffered) {
			this(new GridLayout(0, 1), isDoubleBuffered);
		}
		
		public ControlPanel(LayoutManager layout) {
			this(layout, true);
		}

		public ControlPanel(LayoutManager layout, boolean isDoubleBuffered) {
			super(layout, isDoubleBuffered);
			
			
			
			userNameTextField = new JTextField("Place new user name here", 25);
			addUserButton = new JButton("Create user");
			addUserButton.setToolTipText("Add a new user");
			
			JPanel addUserPanel = new JPanel();
			addUserPanel.add(userNameTextField);
			addUserPanel.add(addUserButton);
			
			this.add(addUserPanel);
			
			addUserButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						Network net = (Network) getAlgorithm().createAssetNetFromProbabilisticNet(getAlgorithm().getRelatedProbabilisticNetwork());
						net.setName(getUserNameTextField().getText());
						addAssetNetToTab(net , net.getName());
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(ControlPanel.this, "Could not create new user: " + e1.getMessage());
					}
				}
			});
			
			userNameTextField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addUserButton.doClick();
				}
			});
			
			returnEditModeButton = new JButton(IconController.getInstance().getEditIcon());
			returnEditModeButton.setToolTipText("Return to BN edit mode");
//			this.add(returnEditModeButton);
			addUserPanel.add(returnEditModeButton);
			returnEditModeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getMediator().getScreen().changeToPNEditionPane();
				}
			});
			
			currentlySelectedUserLabel = new JLabel(getAlgorithm().getAssetNetwork().getName());
			JPanel currentUserNamePanel = new JPanel();
			currentUserNamePanel.add(new JLabel("Currently selected user: "));
//			this.add(currentlySelectedUserLabel);
			currentUserNamePanel.add(currentlySelectedUserLabel);
			this.add(currentUserNamePanel);
		}

		/**
		 * @return the userNameTextField
		 */
		public JTextField getUserNameTextField() {
			return userNameTextField;
		}

		/**
		 * @param userNameTextField the userNameTextField to set
		 */
		public void setUserNameTextField(JTextField userNameTextField) {
			this.userNameTextField = userNameTextField;
		}

		/**
		 * @return the currentlySelectedUserLabel
		 */
		public JLabel getCurrentlySelectedUserLabel() {
			return currentlySelectedUserLabel;
		}

		/**
		 * @param currentlySelectedUserLabel the currentlySelectedUserLabel to set
		 */
		public void setCurrentlySelectedUserLabel(JLabel currentlySelectedUserLabel) {
			this.currentlySelectedUserLabel = currentlySelectedUserLabel;
		}

		
	}



	/**
	 * @return the userToassetNetMap
	 */
	public Map<String, Graph> getUserToassetNetMap() {
		return userToassetNetMap;
	}

	/**
	 * @param userToassetNetMap the userToassetNetMap to set
	 */
	public void setUserToassetNetMap(Map<String, Graph> userToassetNetMap) {
		this.userToassetNetMap = userToassetNetMap;
	}

	/**
	 * @return the userManagementTabTitle
	 */
	public String getUserManagementTabTitle() {
		return userManagementTabTitle;
	}

	/**
	 * @param userManagementTabTitle the userManagementTabTitle to set
	 */
	public void setUserManagementTabTitle(String userManagementTabTitle) {
		this.userManagementTabTitle = userManagementTabTitle;
	}

	/**
	 * @return the probabilityTabTitle
	 */
	public String getProbabilityTabTitle() {
		return probabilityTabTitle;
	}

	/**
	 * @param probabilityTabTitle the probabilityTabTitle to set
	 */
	public void setProbabilityTabTitle(String probabilityTabTitle) {
		this.probabilityTabTitle = probabilityTabTitle;
	}
	
}
