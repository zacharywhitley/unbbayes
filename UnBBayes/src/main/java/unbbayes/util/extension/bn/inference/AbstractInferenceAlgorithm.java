/**
 * 
 */
package unbbayes.util.extension.bn.inference;

import java.util.ArrayList;
import java.util.List;

import unbbayes.controller.INetworkMediator;
import unbbayes.prs.Graph;

/**
 * Abstract class which offers some default implementation
 * of some methods, like {@link #getMediator()}
 * and {@link #addInferencceAlgorithmListener(IInferenceAlgorithmListener)},
 * {@link #getNetwork()}
 * @author Shou Matsumoto
 */
public abstract class AbstractInferenceAlgorithm implements IInferenceAlgorithm {


	private Graph network;

	private INetworkMediator mediator;
	

	private List<IInferenceAlgorithmListener> inferenceAlgorithmListeners = new ArrayList<IInferenceAlgorithmListener>(0);

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#setNetwork(unbbayes.prs.Graph)
	 */
	public void setNetwork(Graph g) throws IllegalArgumentException {
		network = g;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getNetwork()
	 */
	public Graph getNetwork() {
		return network;
	}


	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#addInferencceAlgorithmListener(unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener)
	 */
	public void addInferencceAlgorithmListener(IInferenceAlgorithmListener listener) {
		// TODO use some kind of proxy, so that events are intercepted and listeners are automatically called by subclasses
		getInferenceAlgorithmListeners().add(listener);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#removeInferencceAlgorithmListener(unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener)
	 */
	public void removeInferencceAlgorithmListener(IInferenceAlgorithmListener listener) {
		// TODO use some kind of proxy, so that events are intercepted and listeners are automatically called by subclasses
		getInferenceAlgorithmListeners().remove(listener);
	}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getMediator()
	 */
	public INetworkMediator getMediator() {
		return this.mediator;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#setMediator(unbbayes.controller.INetworkMediator)
	 */
	public void setMediator(INetworkMediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * @return the inferenceAlgorithmListeners
	 */
	public List<IInferenceAlgorithmListener> getInferenceAlgorithmListeners() {
		return inferenceAlgorithmListeners;
	}

	/**
	 * @param inferenceAlgorithmListeners the inferenceAlgorithmListeners to set
	 */
	public void setInferenceAlgorithmListeners(
			List<IInferenceAlgorithmListener> inferenceAlgorithmListeners) {
		this.inferenceAlgorithmListeners = inferenceAlgorithmListeners;
	}

}
