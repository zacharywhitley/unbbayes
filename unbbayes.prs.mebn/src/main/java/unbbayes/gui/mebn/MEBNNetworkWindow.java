/**
 * 
 */
package unbbayes.gui.mebn;


import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JViewport;

import unbbayes.controller.ConfigurationsController;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.EvidenceTree;
import unbbayes.gui.NetworkWindow;
import unbbayes.io.BaseIO;
import unbbayes.io.FileExtensionIODelegator;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.ResourceController;
import unbbayes.util.extension.UnBBayesModule;

/**
 * Codes from NetworkWindow which was dealing  MEBN was migrated into here.
 * This class represents a MEBN module, by also extending UnBBayesModule.
 * @author Shou Matsumoto
 *
 */
public class MEBNNetworkWindow extends NetworkWindow {

	public static final Integer MEBN_MODE = 1;
	
	private static final String MEBN_PANE_MEBN_EDITION_PANE = "mebnEditionPane";

	private static final String MEBN_PANE_SSBN_COMPILATION_PANE = "ssbnCompilationPane";
	
	private MEBNEditionPane mebnEditionPane = null;

	private SSBNCompilationPane ssbnCompilationPane = null;
	
	/** The resource is not static, so that hotplug would become easier */
	private ResourceBundle resource;
	
//	private static final String[] SUPPORTED_FILE_EXTENSIONS_MEBN = { unbbayes.io.mebn.UbfIO.FILE_EXTENSION };
	

	/**
	 * Default constructor.
	 * It is made public because of plugin support. It is not recommended
	 * to use this constructor unless you are extending this class.
	 * @deprecated
	 */
	public MEBNNetworkWindow() {
		super();
		this.resource = ResourceController.newInstance().getBundle(
				unbbayes.gui.mebn.resources.Resources.class.getName());
	}

	/**
	 * Initializes a MEBN module using a network as a parameter
	 * @param net
	 */
	public MEBNNetworkWindow(Network net) {
		this();
		this.setModuleName(net.getName());
		this.setTitle(net.getName());
		this.setName(this.getName());
		
		// the below code is a copy from superclass
		
		this.setNet(net); 
		this.setFileName(null); 
		
		Container contentPane = getContentPane();
		this.setCardLayout(new CardLayout());
		contentPane.setLayout(this.getCardLayout());
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

		// instancia variaveis de instancia
		this.setGraphViewport(new JViewport());
		this.setController(new MEBNController((MultiEntityBayesianNetwork) net, this));
		this.setMebnEditionPane(((MEBNController)this.getController()).getMebnEditionPane());
		
		this.setModuleName(this.resource.getString("MEBNModuleName"));

		this.setGraphPane(new MEBNGraphPane(this.getController(), this.getGraphViewport()));

		this.setJspGraph(new JScrollPane(this.getGraphViewport()));
		
		this.setBCompiled(false);

		//by young
		long width = (long)Node.getDefaultSize().getX();
		long height = (long)Node.getDefaultSize().getY();
		
		this.getGraphPane().getGraphViewport().reshape(0, 0,
				(int) (this.getGraphPane().getBiggestPoint().getX() + width),
				(int) (this.getGraphPane().getBiggestPoint().getY() + height));
		
		this.getGraphPane().getGraphViewport().setViewSize(
				new Dimension(
						(int) (this.getGraphPane().getBiggestPoint().getX() + width),
						(int) (this.getGraphPane().getBiggestPoint().getY() + height)));

		// set the content and size of graphViewport
		this.getGraphViewport().setView(this.getGraphPane());
		this.getGraphViewport().setSize(800, 600);

		this.getJspGraph().getHorizontalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						getGraphPane().update();
					}
				});

		this.getJspGraph().getVerticalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						getGraphPane().update();
					}
				});

		// set default values for jspGraph
		this.getJspGraph().setHorizontalScrollBar(this.getJspGraph().createHorizontalScrollBar());
		this.getJspGraph().setVerticalScrollBar(this.getJspGraph().createVerticalScrollBar());
		this.getJspGraph().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.getJspGraph().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		this.setMode(MEBN_MODE);
		this.setSsbnCompilationPane(new SSBNCompilationPane());
		contentPane.add(this.getMebnEditionPane(), MEBN_PANE_MEBN_EDITION_PANE);
		contentPane.add(this.getSsbnCompilationPane(), MEBN_PANE_SSBN_COMPILATION_PANE);
		
		// inicia com a tela de edicao de rede(PNEditionPane)
		this.getMebnEditionPane().getGraphPanel().setBottomComponent(this.getJspGraph());
		this.getCardLayout().show(getContentPane(), MEBN_PANE_MEBN_EDITION_PANE);

		setVisible(true);
		this.getGraphPane().update();
	}
	
	/**
	 * This method changes the main screen from compilation pane to edition pane
	 */
	public void changeToMEBNEditionPane() {

		if (this.getMode() == MEBN_MODE) {
			//by young
			//Node.setSize(MultiEntityNode.getDefaultSize().getX(), MultiEntityNode.getDefaultSize().getY());
			this.getGraphPane().addKeyListener(this.getController());

			try{
				((MEBNController)this.getController()).setEditionMode();
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("The controller of this network window must be set to MEBNController", e);
			}
			
			this.getGraphPane().resetGraph();
			
			// starts the network edition screen (PNEditionPane)
			this.getMebnEditionPane().getGraphPanel().setBottomComponent(this.getJspGraph());
			this.getMebnEditionPane().updateToPreferredSize();
			this.getCardLayout().show(getContentPane(), MEBN_PANE_MEBN_EDITION_PANE);
		}
	}
	
	/**
	 * This method changes the main screen from edition pane to 
	 * compilation pane
	 */
	public void changeToSSBNCompilationPane(SingleEntityNetwork ssbn) {

		if (this.getMode()  == MEBN_MODE) {
			//by young
			//Node.setSize(Node.getDefaultSize().getX(), Node.getDefaultSize().getY());

			Container contentPane = getContentPane();
			contentPane.remove(this.getSsbnCompilationPane());

			this.setSsbnCompilationPane(new SSBNCompilationPane(ssbn, this,this.getController()));
			this.getGraphPane().resetGraph();
			ssbnCompilationPane.getCenterPanel().setRightComponent(this.getJspGraph());
			ssbnCompilationPane.setStatus(this.getStatus().getText());
			ssbnCompilationPane.getEvidenceTree().setRootVisible(true);
			ssbnCompilationPane.getEvidenceTree().expandRow(0);
			ssbnCompilationPane.getEvidenceTree().setRootVisible(false);
			//by young2
			ssbnCompilationPane.getEvidenceTree().updateTree(true);
			
			contentPane.add(ssbnCompilationPane,
					MEBN_PANE_SSBN_COMPILATION_PANE);
			
			ssbnCompilationPane.updateToPreferredSize(); 
			
			CardLayout layout = (CardLayout) contentPane.getLayout();
			layout.show(getContentPane(), MEBN_PANE_SSBN_COMPILATION_PANE);
		}
	}
	
	/**
	 * Opens a new desktop window into currently used java desktop
	 * @see unbbayes.util.extension.UnBBayesModule#openFile(java.io.File)
	 */
	@Override
	public UnBBayesModule openFile(File file) throws IOException {
		
		Graph g = null;
		
		// This IO is instantiated at MEBNController' constructor.
		// Note that NetworkWindow#getIO() actually calls MEBNController#getBaseIO()
		BaseIO io = this.getIO();
		
		try {
			g = io.load(file);
		} catch (FileExtensionIODelegator.MoreThanOneCompatibleIOException e) {
			// More than one I/O was found to be compatible. Ask user to select one.
			String[] possibleValues = FileExtensionIODelegator.getNamesFromIOs(e.getIOs());
	    	String selectedValue = (String)JOptionPane.showInputDialog(
	    			this, 
	    			resource.getString("IOConflictMessage"), 
	    			resource.getString("IOConflictTitle"),
	    			JOptionPane.INFORMATION_MESSAGE, 
	    			null,
	    			possibleValues, 
	    			possibleValues[0]);
	    	if (selectedValue != null) {
	    		g = FileExtensionIODelegator.findIOByName(e.getIOs(), selectedValue).load(file);
	    	} else {
	    		// user appears to have cancelled
	    		this.dispose();
		    	return null;
	    	}
		}
		
		MEBNNetworkWindow window = null;
		
		try {
			ConfigurationsController.getInstance().addFileToListRecentFiles(file); 
			window = new MEBNNetworkWindow((Network)g);	
			window.setFileName(file.getName().toLowerCase()); 
		} catch (Exception e) {
			throw new RuntimeException(this.resource.getString("unsupportedGraphFormat"),e);
		}
		
		// we do not use this current instance. Instead, dispose it and return the new instance of window
		this.dispose();
		return window;
	}
	
	/**
	 * Retorna a rede probabil_stica <code>(ProbabilisticNetwork)</code>
	 * 
	 * @return a rede probabil_stica
	 * @see ProbabilisticNetwork
	 */
	public MultiEntityBayesianNetwork getMultiEntityBayesianNetwork() {
		return (MultiEntityBayesianNetwork) this.getController().getNetwork();
	}

	/**
	 * @return the mebnEditionPane
	 */
	public MEBNEditionPane getMebnEditionPane() {
		return mebnEditionPane;
	}

	/**
	 * @param mebnEditionPane the mebnEditionPane to set
	 */
	public void setMebnEditionPane(MEBNEditionPane mebnEditionPane) {
		this.mebnEditionPane = mebnEditionPane;
	}
	
	/**
	 * Obtains the evidence tree
	 * 
	 * @return  (<code>JTree</code>)
	 * @see JTree
	 */
	public EvidenceTree getEvidenceTree() {
		if (ssbnCompilationPane != null) {
			return ssbnCompilationPane.getEvidenceTree();
		} else {
			return null;
		}
	}
	
	/**
	 * Updates the status shown at compilation pane
	 * 
	 * @param status
	 *            status message.
	 */
	public void setStatus(String status) {
		try {
			super.setStatus(status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ssbnCompilationPane != null) {
			ssbnCompilationPane.setStatus(status);
		}
	}

	/**
	 * @return the ssbnCompilationPane
	 */
	public SSBNCompilationPane getSsbnCompilationPane() {
		return ssbnCompilationPane;
	}

	/**
	 * @param ssbnCompilationPane the ssbnCompilationPane to set
	 */
	public void setSsbnCompilationPane(SSBNCompilationPane ssbnCompilationPane) {
		this.ssbnCompilationPane = ssbnCompilationPane;
	}

}
