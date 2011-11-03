/**
 * 
 */
package unbbayes.prs.bn;

import java.util.Locale;
import java.util.ResourceBundle;

import unbbayes.controller.INetworkMediator;
import unbbayes.prs.Graph;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel;

/**
 * Class for junction tree compiling algorithm.
 * It includes basic consistency check for single entity networks.
 * 
 * By now, this is still a wrapper for {@link ProbabilisticNetwork#compile()}.
 * 
 * TODO gradually migrate every compilation routine from ProbabilisticNetwork to here.
 * 
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeAlgorithm implements IInferenceAlgorithm {
	
	private static ResourceBundle generalResource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.controller.resources.ControllerResources.class.getName(),
			Locale.getDefault(),
			JunctionTreeAlgorithm.class.getClassLoader());
	
	private ProbabilisticNetwork net;
	
	private InferenceAlgorithmOptionPanel optionPanel;

	/** Load resource file from util */
  	private static ResourceBundle utilResource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.util.resources.UtilResources.class.getName());
	

  	/** Load resource file from this package */
  	protected static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.prs.bn.resources.BnResources.class.getName());
  	
  	
	/**
	 * Default constructor for plugin support
	 */
	public JunctionTreeAlgorithm() {
		super();
	}
	
	
	/**
	 * Constructor using fields
	 */
	public JunctionTreeAlgorithm(ProbabilisticNetwork net) {
		this();
		this.setNet(net);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getDescription()
	 */
	public String getDescription() {
		return this.utilResource.getString("junctionTreeAlgorithmDescription");
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getName()
	 */
	public String getName() {
		return this.utilResource.getString("junctionTreeAlgorithmName");
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#run()
	 */
	public void run() throws IllegalStateException {
		if (this.getNet() == null
				|| this.getNet().getNodes().size() == 0) {
			throw new IllegalStateException(resource.getString("EmptyNetException"));
		}
		try {
			// TODO gradually migrate all compile routines to here
			this.getNet().compile();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
    
    

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#setNetwork(unbbayes.prs.Graph)
	 */
	public void setNetwork(Graph g) throws IllegalArgumentException {
		this.setNet((ProbabilisticNetwork)g);
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getNetwork()
	 */
	public Graph getNetwork() {
		return this.getNet();
	}

	/**
	 * @return the net
	 */
	public ProbabilisticNetwork getNet() {
		return net;
	}

	/**
	 * @param net the net to set
	 */
	public void setNet(ProbabilisticNetwork net) {
		this.net = net;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#reset()
	 */
	public void reset() {
		try {
			this.getNet().initialize();
			if (this.getMediator() != null) {
				// if we have access to the controller, update status label
				float totalEstimateProb = this.getNet().PET();
				this.getMediator().getScreen().setStatus(this.getResource().getString("statusReady"));
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#propagate()
	 */
	public void propagate() {
		try {
			this.getNet().updateEvidences();
			if (this.getMediator() != null) {
				// if we have access to the controller, update status label
				float totalEstimateProb = this.getNet().PET();
				this.getMediator().getScreen().setStatus(this.getResource()
						.getString("statusEvidenceProbabilistic")
						+ (totalEstimateProb * 100.0) + "%");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * This is the option panel related to this algorithm
	 * @return the optionPanel
	 */
	public InferenceAlgorithmOptionPanel getOptionPanel() {
		return optionPanel;
	}


	/**
	 * This is the option panel related to this algorithm
	 * @param optionPanel the optionPanel to set
	 */
	public void setOptionPanel(InferenceAlgorithmOptionPanel optionPanel) {
		this.optionPanel = optionPanel;
	}

	/**
	 * @return the mediator
	 */
	public INetworkMediator getMediator() {
		if (this.getOptionPanel() != null) {
			return this.getOptionPanel().getMediator();
		}
		return null;
	}
	
	/**
	 * @return the resource
	 */
	public static ResourceBundle getResource() {
		return generalResource;
	}

	/**
	 * @param resource the resource to set
	 */
	public static void setResource(ResourceBundle resource) {
		JunctionTreeAlgorithm.generalResource = resource;
	}
	
	
}
