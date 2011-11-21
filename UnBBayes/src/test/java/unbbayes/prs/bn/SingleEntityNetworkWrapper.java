/**
 * 
 */
package unbbayes.prs.bn;

import unbbayes.io.log.ILogManager;

/**
 * @author Shou Matsumoto
 *
 */
public class SingleEntityNetworkWrapper extends SingleEntityNetwork {

	private SingleEntityNetwork wrappedNetwork;
	
	/**
	 * 
	 */
	public SingleEntityNetworkWrapper(SingleEntityNetwork network) {
		super("SingleEntityNetworkWrapper-" + network.getName());
		this.setWrappedNetwork(network);
	}
	
	/**
	 * Makes the logManager of {@link #getWrappedNetwork()} public (it was protected).
	 * @return
	 */
	public ILogManager getLogManager() {
		return this.getWrappedNetwork().logManager;
	}

	/**
	 * @return the wrappedNetwork
	 */
	public SingleEntityNetwork getWrappedNetwork() {
		return wrappedNetwork;
	}

	/**
	 * @param wrappedNetwork the wrappedNetwork to set
	 */
	public void setWrappedNetwork(SingleEntityNetwork wrappedNetwork) {
		this.wrappedNetwork = wrappedNetwork;
	}
}
