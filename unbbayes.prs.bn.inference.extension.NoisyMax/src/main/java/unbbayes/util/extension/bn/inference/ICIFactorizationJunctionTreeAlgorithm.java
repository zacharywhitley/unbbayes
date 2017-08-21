/**
 * 
 */
package unbbayes.util.extension.bn.inference;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.Debug;

/**
 * This is a junction tree algorithm which uses {@link ICINodeFactorizationHandler} in order
 * to check whether nodes use Independence of Causal Influence (ICI),
 * and if so reassembles the network for efficient junction tree propagation.
 * Since this will change the network, callers may need to use cloned network or
 * to provide 
 * @author Shou Matsumoto
 *
 */
public class ICIFactorizationJunctionTreeAlgorithm extends JunctionTreeAlgorithm  {
	
	/** @see #getFactorizationHandlerList() */
	private List<ICINodeFactorizationHandler> factorizationHandlerList = new ArrayList<ICINodeFactorizationHandler>();
	
	/** Lazily initialized at {@link #getReturnToEditModeListener()} */
	private AncestorListener returnToEditModeListener = null;

	private float probErrorMargin = NoisyMaxCPTConverter.DEFAULT_PROBABILITY_ERROR_MARGIN;
	
	/**
	 * Default constructor
	 */
	public ICIFactorizationJunctionTreeAlgorithm() {
		init();
	}

	/**
	 * This is called in the constructor in order to initialize attributes
	 */
	protected void init() {
		List<IInferenceAlgorithmListener> algorithmListeners = getInferenceAlgorithmListeners();
		if (algorithmListeners == null) {
			// initialize, if not initialized yet
			algorithmListeners = new ArrayList<IInferenceAlgorithmListener>();
			setInferenceAlgorithmListeners(algorithmListeners);
		}
		
		// include listeners that will be called before compilation
		algorithmListeners.add(new IInferenceAlgorithmListener() {
			/**
			 * This method will be called before {@link ICIFactorizationJunctionTreeAlgorithm#getReturnToEditModeListener()}
			 * in order to reassemble the network for efficient junction tree propagation when ICI is present.
			 * This will also include a listener to {@link ICIFactorizationJunctionTreeAlgorithm#getMediator()}
			 * in order to undo changes in the network when user returns from compilation panel of GUI to edit panel.
			 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener#onBeforeRun(unbbayes.util.extension.bn.inference.IInferenceAlgorithm)
			 * @see ICIFactorizationJunctionTreeAlgorithm#factorize()
			 * @see ICIFactorizationJunctionTreeAlgorithm#getReturnToEditModeListener()
			 */
			public void onBeforeRun(IInferenceAlgorithm algorithm) {
				// reassemble (factorization) network in order to optimize for junction tree propagation
				factorize();
				
				// insert listener that will undo the above factorization if this was called from GUI
				if (getMediator() != null) {
					try {
						getMediator().getScreen().getNetWindowEdition().removeAncestorListener(getReturnToEditModeListener());	// avoid double insertion
						getMediator().getScreen().getNetWindowEdition().addAncestorListener(getReturnToEditModeListener());		
					} catch (Exception e) {
						Debug.println(getClass(), "Could not add GUI listener to undo factorization when returning to edit mode.", e);
					}
				}
			}
			public void onBeforeReset(IInferenceAlgorithm algorithm) { }
			public void onBeforePropagate(IInferenceAlgorithm algorithm) { }
			public void onAfterRun(IInferenceAlgorithm algorithm) { }
			public void onAfterReset(IInferenceAlgorithm algorithm) { }
			public void onAfterPropagate(IInferenceAlgorithm algorithm) { }
		});
		
		// factorize() will use this handler
		NoisyMaxTemporalFactorizationHandler handler = new NoisyMaxTemporalFactorizationHandler();
		handler.setProbErrorMargin(getProbErrorMargin() );
		getFactorizationHandlerList().add(handler);
	}


	/**
	 * List of handlers for known ICI nodes. Include a new handler for each different type of ICI.
	 * More handlers can be added if new ICI types are implemented.
	 * @return the factorizationHandlerList
	 * @see #factorize()
	 * @see #defactorize()
	 */
	public List<ICINodeFactorizationHandler> getFactorizationHandlerList() {
		return factorizationHandlerList;
	}

	/**
	 * List of handlers for known ICI nodes. Include a new handler for each different type of ICI.
	 * More handlers can be added if new ICI types are implemented.
	 * @param factorizationHandlerList the factorizationHandlerList to set
	 * @see #factorize()
	 * @see #defactorize()
	 */
	public void setFactorizationHandlerList(List<ICINodeFactorizationHandler> factorizationHandlerList) {
		this.factorizationHandlerList = factorizationHandlerList;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#getDescription()
	 */
	public String getDescription() {
		return "Junction tree algorithm optimized for ICI nodes.";
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#getName()
	 */
	public String getName() {
		return "Junction Tree with ICI";
	}

	

	/**
	 * Make all necessary optimizations for known ICI nodes.
	 * @see #defactorize()
	 * @see #getFactorizationHandlerList()
	 */
	public void factorize() {
		if (getFactorizationHandlerList() != null && !getFactorizationHandlerList().isEmpty()) {
			// extract the network we are going to iterate
			ProbabilisticNetwork network = getNet();
			if (network == null) {
				// cannot do anything if network was not specified
				throw new NullPointerException("Network was not specified. getNet() == null");
			}
			// iterate on all nodes, check if ICI, then handle it
			for (Node node : new ArrayList<Node>(network.getNodes())) {	// iterate on a clone of list, because network.getNodes() will change
				// check what handler is compatible with current node
				for (ICINodeFactorizationHandler handler : getFactorizationHandlerList()) {
					if (handler.isICICompatible(node, network)) {
						// execute the 1st handler compatible with node
						handler.treatICI(node, network);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Undo changes performed by {@link #factorize()}
	 * @see #getFactorizationHandlerList()
	 */
	public void defactorize() {
		if (getFactorizationHandlerList() != null) {
			for (ICINodeFactorizationHandler handler : getFactorizationHandlerList()) {
				handler.undo();
			}
		}
	}


	


	/**
	 * This listener is used in order to undo all changes in the network made by this algorithm
	 * when the user returns from compilation mode of GUI to edit mode.
	 * The listener is actually included to {@link #getMediator()} when {@link #run()} is called.
	 * This method lazily instantiates a new listener if listener is null.
	 * @return the returnToEditModeListener
	 */
	public AncestorListener getReturnToEditModeListener() {
		if (returnToEditModeListener == null) {
			returnToEditModeListener = new AncestorListener() {
				/** @see javax.swing.event.AncestorListener#ancestorRemoved(javax.swing.event.AncestorEvent) */
				public void ancestorRemoved(AncestorEvent event) {}
				/** @see javax.swing.event.AncestorListener#ancestorMoved(javax.swing.event.AncestorEvent) */
				public void ancestorMoved(AncestorEvent event) {}
				/**
				 * This is called when the card layout sets the edit mode pane visible.
				 * So, it is being used to undo factorization when user goes from compiled mode to edit mode
				 * @see javax.swing.event.AncestorListener#ancestorAdded(javax.swing.event.AncestorEvent)
				 */
				public void ancestorAdded(AncestorEvent event) {
					defactorize();
				}
			};
		}
		return returnToEditModeListener;
	}


	/**
	 * This listener is used in order to undo all changes in the network made by this algorithm
	 * when the user returns from compilation mode of GUI to edit mode.
	 * The listener is actually included to {@link #getMediator()} when {@link #run()} is called.
	 * @param returnToEditModeListener the returnToEditModeListener to set
	 */
	public void setReturnToEditModeListener(AncestorListener returnToEditModeListener) {
		// remove old listener
		if (this.getMediator() != null) {
			try {
				getMediator().getScreen().getNetWindowEdition().removeAncestorListener(this.returnToEditModeListener);
			} catch (Exception e) {
				e.printStackTrace();
				Debug.println(getClass(), "Could not remove old GUI listener to undo factorization when returning to edit mode.", e);
			}
		}
		this.returnToEditModeListener = returnToEditModeListener;
	}


	/**
	 * @return the probErrorMargin : error margin to be passed to {@link NoisyMaxTemporalFactorizationHandler}
	 * when it is instantiated at {@link #init()}. Default is {@link NoisyMaxCPTConverter#DEFAULT_PROBABILITY_ERROR_MARGIN}.
	 */
	public float getProbErrorMargin() {
		return probErrorMargin;
	}

	/**
	 * @param probErrorMargin : error margin to be passed to {@link NoisyMaxTemporalFactorizationHandler}
	 * when it is instantiated at {@link #init()}. Default is {@link NoisyMaxCPTConverter#DEFAULT_PROBABILITY_ERROR_MARGIN}.
	 */
	public void setProbErrorMargin(float probErrorMargin) {
		this.probErrorMargin = probErrorMargin;
		
		// update handler list if applicable
		if (this.getFactorizationHandlerList() != null) {
			for (ICINodeFactorizationHandler handler : getFactorizationHandlerList()) {
				if (handler instanceof NoisyMaxTemporalFactorizationHandler) {
					((NoisyMaxTemporalFactorizationHandler) handler).setProbErrorMargin(probErrorMargin);
				}
			}
		}
	}
	
	

}
