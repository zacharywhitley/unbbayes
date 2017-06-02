/**
 * 
 */
package unbbayes.gui.mebn.extension.node;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.mebn.ArgumentEditionPane;
import unbbayes.gui.mebn.PossibleValuesEditionPane;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.prs.mebn.IdentityNode;
import unbbayes.prs.mebn.ResidentNode;

/**
 * @author Shou Matsumoto
 *
 */
public class IdentityNodePanel extends JPanel {

	
	private static final long serialVersionUID = -1968637663345656388L;
	
	private CardLayout cardLayout = new CardLayout();
	private JPanel mainPanel; 
	private IMEBNMediator mediator; 
	private IconController iconController = IconController.getInstance(); 
	private String title = null;
	
	/** Name of the card used by {@link #mainPanel} and {@link #cardLayout} to display the panel to edit arguments */
	public static final String ARGUMENT_CARD_NAME = "ARGUMENT_CARD";
	/** Name of the card used by {@link #mainPanel} and {@link #cardLayout} to display the panel to edit states of nodes*/
	public static final String STATE_CARD_NAME = "STATE_CARD";
	
	
  	private ResourceBundle resource =unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.gui.resources.GuiResources.class.getName(),
			Locale.getDefault(), getClass().getClassLoader());

	private IdentityNode identityNode;

	private IdentityNodeToolBar nodeToolBar;
	
	/**
	 * Default constructor is kept protected to avoid access, but enable extension
	 * @see #MEDGNodePanel(MEBNController, ResidentNode)
	 */
	protected IdentityNodePanel(){}
	
	/**
	 * Create a panel to edit MEDG nodes. 
	 * @param mediator : the controller
	 * @param identityNode : the node to edit
	 * @param title
	 */
	public IdentityNodePanel(IMEBNMediator mediator, IdentityNode identityNode, String title){
		super();
		this.mediator = mediator;
		this.identityNode = identityNode; 
		this.title = title;
		
		this.resetComponents();
	    
	}

	/**
	 * Resets all components. It will initialize listeners as well.
	 */
	public void resetComponents() {
		this.removeAll();
		this.setBorder(MebnToolkit.getBorderForTabPanel(getTitle())); 
		this.setPreferredSize(new Dimension(100, 100));
		
		if (getIdentityNode() != null) {
			try {
				mainPanel = new JPanel(getCardLayout());
				mainPanel.setPreferredSize(new Dimension(100, 100));
				JPanel internalPanel = new ArgumentEditionPane((MEBNController) getMediator(), getIdentityNode());
				internalPanel.setSize(100, 100);
				
				// force the tool bar of state pane to be invisible...
				((BorderLayout)internalPanel.getLayout()).getLayoutComponent(BorderLayout.NORTH).setVisible(false);
				mainPanel.add(ARGUMENT_CARD_NAME, internalPanel);
				
				// now, include the panel to add states
				internalPanel = new PossibleValuesEditionPane((MEBNController) getMediator(), getIdentityNode());
				internalPanel.setBorder(MebnToolkit.getBorderForTabPanel(getResource().getString("ArgumentTitle")));
				mainPanel.add(STATE_CARD_NAME, internalPanel);
			} catch (Exception e) {
				// prepare a string to print
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				e.printStackTrace(ps);
				JPanel errorPanel = new JPanel();
				errorPanel.add(new TextArea(baos.toString()));
				setMainPanel(errorPanel);
			}
		}
	    
		this.setLayout(new BorderLayout()); 
	    
		setMEDGNodeToolBar(new IdentityNodeToolBar());
		this.add(getNodeToolBar(), BorderLayout.NORTH); 
	    this.add(getMainPanel(), BorderLayout.CENTER);
	    
	    
	    this.getNodeToolBar().getBtnEditArguments().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getMainPanel() != null && getCardLayout() != null) {
					getCardLayout().show(getMainPanel(), ARGUMENT_CARD_NAME);
				}
			}
		});
	    this.getNodeToolBar().getBtnEditStates().addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		if (getMainPanel() != null && getCardLayout() != null) {
	    			getCardLayout().show(getMainPanel(), STATE_CARD_NAME);
	    		}
	    	}
	    });
	    this.getNodeToolBar().getBtnEditTable().addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		if (getMediator() != null && getIdentityNode() != null && getIdentityNode() != null) {
	    			getMediator().openCPTDialog(getIdentityNode());
	    		}
	    	}
	    });
	    
	    
	}

	/**
	 * @return the mainPanel: this is a panel instantiated at {@link #resetComponents()}
	 * whose layout is {@link #getCardLayout()}.
	 */
	public JPanel getMainPanel() {
		return this.mainPanel;
	}

	/**
	 * @param mainPanel the mainPanel to set: this is a panel instantiated at {@link #resetComponents()}
	 * whose layout is {@link #getCardLayout()}.
	 */
	protected void setMainPanel(JPanel mainPanel) {
		this.mainPanel = mainPanel;
	}


	/**
	 * @return the mediator
	 */
	public IMEBNMediator getMediator() {
		return this.mediator;
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(IMEBNMediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * @return the iconController
	 */
	public IconController getIconController() {
		return this.iconController;
	}

	/**
	 * @param iconController the iconController to set
	 */
	public void setIconController(IconController iconController) {
		this.iconController = iconController;
	}

	/**
	 * @return the resource
	 */
	public ResourceBundle getResource() {
		return this.resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(ResourceBundle resource) {
		this.resource = resource;
	}

	/**
	 * @return the identityNode
	 */
	public IdentityNode getIdentityNode() {
		return this.identityNode;
	}

	/**
	 * @param identityNode the identityNode to set
	 */
	public void setIdentityNode(IdentityNode identityNode) {
		this.identityNode = identityNode;
	}

	/**
	 * @return the nodeToolBar
	 */
	public IdentityNodeToolBar getNodeToolBar() {
		return nodeToolBar;
	}

	/**
	 * @param nodeToolBar the nodeToolBar to set
	 */
	public void setMEDGNodeToolBar(IdentityNodeToolBar nodeToolBar) {
		this.nodeToolBar = nodeToolBar;
	}

	/**
	 * @return the title: the title of {@link #getBorder()} to be set at {@link #resetComponents()}
	 */
	public String getTitle() {
		if (title == null) {
			title = getResource().getString("nodeName");
		}
		return title;
	}

	/**
	 * @param title : the title of {@link #getBorder()} to be set at {@link #resetComponents()}
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the cardLayout. This is used as the layout of {@link #getMainPanel()}
	 */
	public CardLayout getCardLayout() {
		return cardLayout;
	}

	/**
	 * @param cardLayout the cardLayout to set. This is used as the layout of {@link #getMainPanel()}
	 */
	public void setCardLayout(CardLayout cardLayout) {
		this.cardLayout = cardLayout;
	}
	

	
}
